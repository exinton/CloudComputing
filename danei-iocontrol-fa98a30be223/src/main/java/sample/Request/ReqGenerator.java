package sample.Request;

import org.ini4j.Ini;
import org.ini4j.Wini;

import RESTAPI.RESTAPI;
import api.RequestAdapterAPI;
import cephrados.CephRados;
import dhtAPI.DHTAPI;
import req.DynamicTree;
import req.Request;
import req.RequestCallback;
import req.StaticTree;
import req.Rand.ExpGenerator;
import req.Rand.RequestGenerator;
import req.Rand.UniformGenerator;
import req.Rand.ZipfGenerator;
import tools.log_writer;
import util.AutoLock;
import util.Log;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReqGenerator{
	static Log log=Log.get();

	public static class RequestThread implements Runnable{
		UniformGenerator uniform;
		ExpGenerator exp;
		ZipfGenerator zipf;
		RequestGenerator reqGen;
		Map<Request.ReqType, RequestCallback> callbacks;
		int unit;
		int max_rand;
		int max_seq;
		StaticTree sTree;
		DynamicTree dTree;
		CountDownLatch start;
		ReadWriteLock lock;
		double dynamicInsertRatio;
		AtomicLong counter;
		long count;

		public RequestThread(UniformGenerator uniform,
		                     ExpGenerator exp,
		                     ZipfGenerator zipf,
		                     RequestGenerator reqGen,
		                     Map<Request.ReqType, RequestCallback> callbacks,
		                     int unit,
		                     int max_rand,
		                     int max_seq,
		                     StaticTree sTree,
		                     DynamicTree dTree,
		                     double dynamicInsertRatio,
		                     ReadWriteLock lock,
		                     CountDownLatch start,
		                     AtomicLong counter,
		                     long count){
			this.uniform=uniform;
			this.exp=exp;
			this.zipf=zipf;
			this.reqGen=reqGen;
			this.callbacks=callbacks;
			this.unit=unit;
			this.max_rand=max_rand;
			this.max_seq=max_seq;
			this.sTree=sTree;
			this.dTree=dTree;
			this.start=start;
			this.lock=lock;
			this.dynamicInsertRatio=dynamicInsertRatio;
			this.counter=counter;
			this.count=count;
		}

		boolean RWSize(Request request){
			if(request.end<=0) return false;
			if((request.end-request.start)>unit){
				boolean seq=(request.type==Request.ReqType.SEQ_READ || request.type==Request.ReqType.SEQ_WRITE);
				long len=(uniform.nextLong(Math.min(request.end/unit,seq ? max_seq : max_rand)+1))*unit;
//				System.out.println("len:"+len+" end:"+request.end+" unit:"+unit+" r:"+(int)(request.end/unit)+" minus:"+(request.end-len));
				request.start=0;
				request.end=request.start+len;
			}
			return true;
		}

		@Override
		public void run(){
			int sTreeFiles=sTree.getFileSize();
			int sTreeHeadFiles=(int)(sTreeFiles*dynamicInsertRatio);
			int sTreeDirs=sTree.getNonEmptyDirSize();
			int sTreeHeadDirs=(int)(sTreeDirs*dynamicInsertRatio);
			long reqCounter=0;
			long reqTimeSum=0;
			long overheadSum=0;
			long intervalSum=0;
			while(counter!=null &&
					counter.getAndIncrement()<count &&
					!Thread.currentThread().isInterrupted()){
				try{
					long t1=System.currentTimeMillis();
					long interval=exp.nextInt();
					long stopExpectation=t1+interval;
					Request.ReqType type=reqGen.next();
					Request request=null;
					if(dTree==null){
						if(type==Request.ReqType.LS)
							request=sTree.ls(zipf.nextInt(sTreeDirs-1));
						else if(type==Request.ReqType.APPEND){
							request=sTree.append(zipf.nextInt(sTreeFiles-1),(uniform.nextLong(max_seq)+1)*unit);
						}else if(type==Request.ReqType.SEQ_READ ||
								type==Request.ReqType.SEQ_WRITE ||
								type==Request.ReqType.RANDOM_READ ||
								type==Request.ReqType.RANDOM_WRITE){
							request=sTree.fileInfo(zipf.nextInt(sTreeFiles-1));
							request.type=type;
							if(!RWSize(request)) continue;
						}else continue;
						long t2=System.currentTimeMillis();
						callbacks.get(type).call(request);
						long t4=System.currentTimeMillis();
						reqCounter+=1;
						reqTimeSum+=(t4-t2);
						overheadSum+=(t2-t1);
					}else{
						try(AutoLock auto=AutoLock.lock(lock.readLock())){
							if(type==Request.ReqType.CREATE_DIR ||
									type==Request.ReqType.CREATE_FILE ||
									type==Request.ReqType.DELETE ||
									type==Request.ReqType.RMDIR){
								lock.readLock().unlock();
								lock.writeLock().lock();
								if(type==Request.ReqType.CREATE_DIR){
									request=dTree.createDir(zipf.nextInt(dTree.getAllDirSize()-1));
								}else if(type==Request.ReqType.CREATE_FILE){
									request=dTree.createFile(zipf.nextInt(dTree.getAllDirSize()-1));
								}else if(type==Request.ReqType.DELETE){
									if(dTree.getFileSize()==0){
										lock.writeLock().unlock();
										continue;
									}
									request=dTree.delete(zipf.nextInt(dTree.getFileSize()-1));
								}else if(type==Request.ReqType.RMDIR){
									if(dTree.getEmptyDirSize()==0){
										lock.writeLock().unlock();
										continue;
									}
									request=dTree.rmdir(zipf.nextInt(dTree.getEmptyDirSize()-1));
								}
								lock.readLock().lock();
								lock.writeLock().unlock();
							}else{
								if(type==Request.ReqType.LS){
									int n=zipf.nextInt(sTreeDirs+dTree.getNonEmptyDirSize()-1);
									if(n<sTreeHeadDirs)
										request=sTree.ls(n);
									else{
										n-=sTreeHeadDirs;
										if(n<dTree.getNonEmptyDirSize())
											request=dTree.ls(n);
										else{
											n=n-dTree.getNonEmptyDirSize()+sTreeHeadDirs;
											request=sTree.ls(n);
										}
									}
								}else{
									int n=zipf.nextInt(sTreeFiles+dTree.getFileSize()-1);
									StaticTree t=sTree;
									if(n>=sTreeHeadFiles){
										n-=sTreeHeadFiles;
										if(n<dTree.getFileSize()) t=dTree;
										else n=n-dTree.getFileSize()+sTreeHeadFiles;
									}
									if(type==Request.ReqType.APPEND){
										request=t.append(n,(uniform.nextLong(max_seq)+1)*unit);
									}else if(type==Request.ReqType.SEQ_READ ||
											type==Request.ReqType.SEQ_WRITE ||
											type==Request.ReqType.RANDOM_READ ||
											type==Request.ReqType.RANDOM_WRITE){
										request=t.fileInfo(n);
										request.type=type;
										if(!RWSize(request)) continue;
									}else continue;
								}
							}
							int loop=0;
							overheadSum+=(System.currentTimeMillis()-t1);
							do{
								long t2=System.currentTimeMillis();
								callbacks.get(type).call(request);
								reqTimeSum+=(System.currentTimeMillis()-t2);
								loop+=1;
								request=request.next;
							}while(request!=null);
							reqCounter+=loop;
							while((--loop)>0){
								stopExpectation+=exp.nextInt();
							}
						}
					}
					long t3=System.currentTimeMillis();
					if(t3<stopExpectation){
						Thread.sleep(stopExpectation-t3);
					}
					intervalSum+=(System.currentTimeMillis()-t1);
				}catch(InterruptedException ignored){
					break;
				}
			}
			log.i(String.format("Total req: %d, average request time: %d ms, overhead: %d ms, interval: %d ms",
					reqCounter,reqTimeSum/reqCounter,overheadSum/reqCounter,intervalSum/reqCounter));
			 start.countDown();
		}
	}

	public static ExecutorService generate(
			//  input files
			String staticFile,  //  immutable tree file
			String staticRankFile,    //  ranking file, nullable
			String dynamicFile,    //  mutable tree file, nullable
			double dynamicInsertRatio,  //  [0,1], specify which point should dynamic tree be inserted into static tree.
			//  parameters
			//  exponential generator
			double lambda,  //  curve
			int duration,   //  tail
			//  zipf generator
			double alpha,
			//  io
			int unit,
			int max_rand,
			int max_seq,
			//  request
			Map<Request.ReqType, Double> ratio,
			Map<Request.ReqType, RequestCallback> callbacks,
			CountDownLatch start,
			int numThreads,
			long count
	) throws IOException{
		if(ratio.size()!=callbacks.size())
			throw new IllegalArgumentException(String.format("Length of request ratio %d and callback %d do not match.",ratio.size(),callbacks.size()));
		if(dynamicFile!=null && (dynamicInsertRatio<0 || dynamicInsertRatio>1))
			throw new IllegalArgumentException("dynamicInsertRatio out of range: "+dynamicInsertRatio);
		UniformGenerator uniform=new UniformGenerator();
		StaticTree sTree=StaticTree.getStaticTree(staticFile,uniform);
		if(staticRankFile!=null) sTree.shuffleFilesUneven(staticRankFile);
		DynamicTree dTree=dynamicFile!=null ? DynamicTree.getDynamicTree(dynamicFile,uniform) : null;
		ReadWriteLock lock=dynamicFile!=null ? new ReentrantReadWriteLock() : null;
		ExpGenerator exp=new ExpGenerator(lambda,duration,uniform);
		ZipfGenerator zipf=new ZipfGenerator(alpha,sTree.getFileSize(),uniform);
		RequestGenerator reqGen=new RequestGenerator(ratio,uniform);
		ExecutorService pool=Executors.newFixedThreadPool(numThreads);
		AtomicLong counter=count>0 ? new AtomicLong(0) : null;
		for(int i=0;i<numThreads;++i){
			pool.execute(new RequestThread(uniform,exp,zipf,reqGen,callbacks,unit,max_rand,max_seq,sTree,dTree,dynamicInsertRatio,lock,start,counter,count));
		}
		return pool;
	}

	static class dump implements RequestCallback{
		String server_type;
		log_writer lw  ;
		RequestAdapterAPI api; 
		public dump(String server_type,log_writer lw){
			this.server_type=server_type;
			this.lw=lw;

			if(this.server_type.equals("Ceph")){
				this.api= new RESTAPI(lw,"ceph");
			}else if(this.server_type.equals("Swift")){
				this.api= new RESTAPI(lw,"swift");
			}else if(this.server_type.equals("Rados")){
				this.api= new CephRados(lw);
			}else if(this.server_type.equals("DHT")){
				this.api= new DHTAPI(lw);
			}
		}
		
		@Override
		public List<Integer> call(Request request){
			 System.out.println(request.toString());
			 
			//log.i(request.toString());
		    api.transfer(request);
			return null;
		}
	}

	public static void run_request_generator(String server_type,String request_log) throws IOException, InterruptedException{
		
		log_writer lw =new log_writer(request_log);
		
		Wini conf=new Wini(new File("conf/sample/request.ini"));
		Map<Request.ReqType, Double> ratio=new HashMap<>();
		Map<Request.ReqType, RequestCallback> callbacks=new HashMap<>();
		RequestCallback call=new dump(server_type,lw);
		Map<String, String> map=conf.get("request");
		String seq_read=map.get("seq_read");
		if(seq_read!=null){
			ratio.put(Request.ReqType.SEQ_READ,Double.parseDouble(seq_read));
			callbacks.put(Request.ReqType.SEQ_READ,call);
		}
		String rand_read=map.get("rand_read");
		if(rand_read!=null){
			ratio.put(Request.ReqType.RANDOM_READ,Double.parseDouble(rand_read));
			callbacks.put(Request.ReqType.RANDOM_READ,call);
		}
		String seq_write=map.get("seq_write");
		if(seq_write!=null){
			ratio.put(Request.ReqType.SEQ_WRITE,Double.parseDouble(seq_write));
			callbacks.put(Request.ReqType.SEQ_WRITE,call);
		}
		String rand_write=map.get("rand_write");
		if(rand_write!=null){
			ratio.put(Request.ReqType.RANDOM_WRITE,Double.parseDouble(rand_write));
			callbacks.put(Request.ReqType.RANDOM_WRITE,call);
		}
		String append=map.get("append");
		if(append!=null){
			ratio.put(Request.ReqType.APPEND,Double.parseDouble(append));
			callbacks.put(Request.ReqType.APPEND,call);
		}
		String create_dir=map.get("create_dir");
		if(create_dir!=null){
			ratio.put(Request.ReqType.CREATE_DIR,Double.parseDouble(create_dir));
			callbacks.put(Request.ReqType.CREATE_DIR,call);
		}
		String create_file=map.get("create_file");
		if(create_file!=null){
			ratio.put(Request.ReqType.CREATE_FILE,Double.parseDouble(create_file));
			callbacks.put(Request.ReqType.CREATE_FILE,call);
		}
		String delete=map.get("delete");
		if(delete!=null){
			ratio.put(Request.ReqType.DELETE,Double.parseDouble(delete));
			callbacks.put(Request.ReqType.DELETE,call);
		}
		String ls=map.get("ls");
		if(ls!=null){
			ratio.put(Request.ReqType.LS,Double.parseDouble(ls));
			callbacks.put(Request.ReqType.LS,call);
		}
		Ini.Section runtime=conf.get("generator");
		int threads=runtime.get("thread",Integer.class,8);
		String stree=runtime.get("stree",String.class,"files/test3.txt");
		String dtree=runtime.get("dtree",String.class,"files/test4.txt");
		long requests=runtime.get("requests",long.class,-1L);
		int countdown=runtime.get("time",int.class,-1);
		 
		CountDownLatch start=new CountDownLatch(threads);
		ExecutorService service=generate(stree,null,dtree,
				conf.get("request","dyn_ratio",Double.class),
				conf.get("time","lambda",Double.class),
				conf.get("time","duration",Integer.class),
				conf.get("zipf","alpha",Double.class),
				conf.get("io","unit",Integer.class),
				conf.get("io","rand",Integer.class),
				conf.get("io","seq",Integer.class),
				ratio,callbacks,start,threads,
				requests);
		try{
			if(countdown>0){
				System.out.println("countdown: "+countdown+"s");
				start.await(countdown,TimeUnit.SECONDS);
				 
			}else start.await();
		}catch(InterruptedException ignored){
		}finally{
			
			service.shutdownNow();
			while(!service.awaitTermination(1,TimeUnit.SECONDS)){
				service.shutdownNow();
				System.out.println("Total time reached : waiting to terminate ...");
			}
		}
		lw.close_log_writer();
	}
	
}
