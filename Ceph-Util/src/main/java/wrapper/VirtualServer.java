package wrapper;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(
		  generator = ObjectIdGenerators.PropertyGenerator.class, 
		  property = "identity",scope=VirtualServer.class)
public
class VirtualServer {
	
	private boolean fail;
	private boolean overload;
	public boolean isFail() {
		return fail;
	}


	public void setFail(boolean fail) {
		this.fail = fail;
	}


	public boolean isOverload() {
		return overload;
	}


	public void setOverload(boolean overload) {
		this.overload = overload;
	}

	private int size;
	public VirtualServer next;
	public VirtualServer getPrev() {
		return prev;
	}


	public void setPrev(VirtualServer prev) {
		this.prev = prev;
	}

	VirtualServer prev;
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
	VirtualServer(WrappedAddress fakeAddr,WrappedAddress realAddr){
		this.realServerAddr=realAddr;
		this.fakeServerAddr=fakeAddr;
		identity=fakeAddr.getIp()+":"+fakeAddr.getPort()+"->"+realAddr.getIp()+":"+realAddr.getPort();
	}
	
	VirtualServer(WrappedAddress realAddr){
		this.realServerAddr=realAddr;
		identity=realAddr.getIp()+":"+realAddr.getPort();
	}
	VirtualServer(){
		
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



	public VirtualServer getNext() {
		return next;
	}


	public void setNext(VirtualServer next) {
		this.next = next;
	}


	public boolean isHead() {
		return isHead;
	}


	public void setHead(boolean isHead) {
		this.isHead = isHead;
	}

	

}

