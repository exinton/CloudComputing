package RESTAPI;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.core.transport.HttpResponse;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.common.DLPayload;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.common.payloads.InputStreamPayload;
import org.openstack4j.model.identity.v3.Token;
import org.openstack4j.model.storage.object.SwiftObject;
import org.openstack4j.model.storage.object.options.ObjectListOptions;
import org.openstack4j.openstack.OSFactory;

import req.Request;


public class OPENSTACKClientBase {
	Token token;
	OSClientV3 os;
	public OPENSTACKClientBase(String sys){
		Properties prop = new Properties();
	   	String dir = System.getProperty("user.dir");		
	   	String confFile=dir+File.separator+"conf"+File.separator+sys+"_conf"+File.separator+sys+".conf";
	    InputStream input = null;
	    try {
				input = new FileInputStream(confFile);
				prop.load(input);
		} catch (IOException e) {
				e.printStackTrace();
		}     
		String projectName=prop.getProperty("projectName");
		String domainName=prop.getProperty("domainName");		
		String userId=prop.getProperty("userId");
		String password=prop.getProperty("password");
		String url=prop.getProperty("url");
		Identifier domainIdent = Identifier.byName(domainName);
		Identifier projectIdent = Identifier.byName(projectName);		
		os = OSFactory.builderV3()
	              .endpoint(url)
	              .credentials(userId, password,Identifier.byName(domainName))
	              .scopeToProject(projectIdent, domainIdent)
	              .authenticate();
		this.token =  os.getToken();
		
	}
	
	public long read(Request request) throws Exception {
		int firstSeparator=secondSeparator(request.path);
		String containerName = request.path.substring(0,firstSeparator);
		String name=normalizePath(request.path.substring(firstSeparator+1));
		DLPayload dlPayload=os.objectStorage().objects().download(containerName, name);
		if(dlPayload==null){
			throw new NoImplementionException("not supported by openstack");
		}
		HttpResponse reponse = dlPayload.getHttpResponse();
		Map<String,String> map = reponse.headers();
		long res= Long.valueOf(map.get("Content-Length"));	
		request.start=0;
		request.end=res;
		return res;
		
	}
	
	public void create_file(Request request) throws Exception{
		int size=(int) (request.end-request.start);
		int firstSeparator=secondSeparator(request.path);
		String containerName = request.path.substring(0,firstSeparator);
		String name=normalizePath(request.path.substring(firstSeparator+1));
		byte[] payload = new byte[(int) size];
		Arrays.fill(payload, (byte)1);
		InputStream inputstream = new ByteArrayInputStream(payload);
		InputStreamPayload inputload = new InputStreamPayload(inputstream);		
		String etag = os.objectStorage().objects().put(containerName,name,inputload);
		if(etag==null)
			throw new NoImplementionException("not supported by openstack");		
	}
	
	public void create_dir(Request request) throws Exception {
		ActionResponse response=os.objectStorage().containers().create(request.path);
		if(!response.isSuccess())
			throw new NoImplementionException("not supported by openstack");		
	}
	
	public void force_delete_file(Request request) throws Exception {
		int firstSeparator=secondSeparator(request.path);
		String containerName = request.path.substring(0,firstSeparator);
		String name=normalizePath(request.path.substring(firstSeparator+1));
		ActionResponse response=os.objectStorage().objects().delete(containerName,name);
		if(!response.isSuccess())
			throw new NoImplementionException("not supported by openstack");
	}
	
	public void force_delete_dir(Request request) throws Exception {	
		int firstSeparator=secondSeparator(request.path);
		String containerName = request.path.substring(0,firstSeparator);
	
		ActionResponse response=os.objectStorage().containers().delete(containerName);
		if(!response.isSuccess())
			throw new NoImplementionException("not supported by openstack");
	}
	
	public void ls(Request request) throws Exception {
		int firstSeparator=secondSeparator(request.path);
		String containerName = request.path.substring(0,firstSeparator);
		
		List<? extends SwiftObject> objs = os.objectStorage().objects()
				.list(containerName, ObjectListOptions
						.create()
						.path(getPseudoPath(request)));
		
		if(objs==null)
			throw new NoImplementionException("not supported by openstack");
	}
	
	public int secondSeparator(String fileName){
		for(int i=0;i<fileName.toCharArray().length;i++){
			if(fileName.charAt(i)=='\\'){
					return i;
			}
		}
		return -1;
	}
	
	public String normalizePath(String path){
		return path.replace('\\', '/');
		 
	}
	
	public int lastSeparator(String fileName){
		for(int i=fileName.toCharArray().length-1;i>=0;i--){
			if(fileName.charAt(i)==File.separatorChar){
				return i;
			}
		}
		return -1;
	}

	public String getPseudoPath(Request request){
		int firstSeparator=secondSeparator(request.path);
		String containerName = request.path.substring(0,firstSeparator);
		String name=normalizePath(request.path.substring(firstSeparator+1));
		return name;
	}
	
	
}
