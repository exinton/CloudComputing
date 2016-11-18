package wrapper;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import net.Address;

public class WrappedAddress extends Address implements Comparable<WrappedAddress>{
	private static final long serialVersionUID = 1L;
	String serverAddr;
	Address addr;
	public WrappedAddress(String ip,int port){
		super( ip, port);
		serverAddr=ip+":"+port;
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
