package wrapper;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(
		  generator = ObjectIdGenerators.PropertyGenerator.class, 
		  property = "identity",scope=FakeServer.class)
class FakeServer {
	
	private int size;
	public FakeServer next;
	public FakeServer getPrev() {
		return prev;
	}


	public void setPrev(FakeServer prev) {
		this.prev = prev;
	}

	FakeServer prev;
	private WrappedAddress fakeServerAddr;
	public WrappedAddress getFakeServerAddr() {
		return fakeServerAddr;
	}


	public void setFakeServerAddr(WrappedAddress fakeServerAddr) {
		this.fakeServerAddr = fakeServerAddr;
	}

	private WrappedAddress realServerAddr;
	public WrappedAddress getRealServerAddr() {
		return realServerAddr;
	}


	public void setRealServerAddr(WrappedAddress realServerAddr) {
		this.realServerAddr = realServerAddr;
		this.updateIdentity();
	}

	private String identity;
	public String getIdentity() {
		return identity;
	}


	public void setIdentity(String identity) {
		this.identity = identity;
	}

	private boolean isHead;


	private boolean isFail;
	
	FakeServer(WrappedAddress fakeAddr,WrappedAddress realAddr){
		this.realServerAddr=realAddr;
		this.fakeServerAddr=fakeAddr;
		identity=fakeAddr.getIp()+":"+fakeAddr.getPort()+"->"+realAddr.getIp()+":"+realAddr.getPort();
	}
	
	FakeServer(WrappedAddress realAddr){
		this.realServerAddr=realAddr;
		identity=realAddr.getIp()+":"+realAddr.getPort();
	}
	FakeServer(){
		
	}

	public void updateIdentity() {
		if(fakeServerAddr==null)
			this.identity = realServerAddr.getIp()+":"+realServerAddr.getPort();
		else
			this.identity = fakeServerAddr.getIp()+":"+fakeServerAddr.getPort()+"->"+realServerAddr.getIp()+":"+realServerAddr.getPort();
	}


	public int getSize() {
		return size;
	}


	public void setSize(int size) {
		this.size = size;
	}



	public FakeServer getNext() {
		return next;
	}


	public void setNext(FakeServer next) {
		this.next = next;
	}


	public boolean isHead() {
		return isHead;
	}


	public void setHead(boolean isHead) {
		this.isHead = isHead;
	}


	public boolean isFail() {
		return isFail;
	}

	public void setFail(boolean isFail) {
		this.isFail = isFail;
	}

	

}

