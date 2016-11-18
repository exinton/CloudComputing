package wrapper;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import net.Address;

@JsonIdentityInfo(
		  generator = ObjectIdGenerators.PropertyGenerator.class, 
		  property = "identity")
public class FakeServer {
	
	int size;
	FakeServer prev;
	FakeServer next;
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

	String identity;
	boolean isHead;
	private boolean isFail;
	
	FakeServer(WrappedAddress fakeAddr,WrappedAddress realAddr){
		this.realServerAddr=realAddr;
		this.fakeServerAddr=fakeAddr;
		identity=fakeAddr.getIp()+":"+fakeAddr.getPort()+"->"+realAddr.getIp()+":"+realAddr.getPort();
	}
	
	
	FakeServer(){
		isHead=true;
	}
	public void updateIdentity() {
		this.identity = fakeServerAddr.getIp()+":"+fakeServerAddr.getPort()+"->"+realServerAddr.getIp()+":"+realServerAddr.getPort();
	}


	public boolean isFail() {
		return isFail;
	}

	public void setFail(boolean isFail) {
		this.isFail = isFail;
	}

	

}
