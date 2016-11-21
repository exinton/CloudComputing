package wrapper;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import net.Address;
class WrappedAddress extends Address implements Comparable<WrappedAddress>{
	private static final long serialVersionUID = 1L;
	private String serverAddr;
	private Address addr;
	public String getServerAddr() {
		return serverAddr;
	}

	public void setServerAddr(String serverAddr) {
		this.serverAddr = serverAddr;
	}

	public Address getAddr() {
		return addr;
	}

	public void setAddr(Address addr) {
		this.addr = addr;
	}

	public WrappedAddress(String ip,int port){
		super( ip, port);
		serverAddr=ip+":"+port;
	}
	
	public WrappedAddress(String ipport){
		super(ipport.split(":")[0],Integer.valueOf(ipport.split(":")[1]));
		serverAddr=ipport;
	}
	
	public WrappedAddress(){
		
	}
	
	public WrappedAddress(Address addr){
		super(addr.getIp(),addr.getPort());
		serverAddr=addr.getIp()+":"+addr.getPort();
	}
	
	@Override
	public int compareTo(WrappedAddress o) {
		return stringToInt(this.getIp(),this.getPort())-stringToInt(o.getIp(),o.getPort());
	}
	
	public int stringToInt(String str,int i){
		return str.hashCode()+i;
	}
	
	public boolean equals(WrappedAddress o) {	//compare two address
		if(o.getIp().equals(this.getIp()) && o.getPort()==this.getPort())
			return true;
		else
			return false;
	}
	
	
}
