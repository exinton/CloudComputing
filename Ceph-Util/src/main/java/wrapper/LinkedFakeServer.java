package wrapper;

import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;


public class LinkedFakeServer implements Comparable<LinkedFakeServer> {
	
	FakeServer head;
	private int size;
	WrappedAddress realServer;


	public LinkedFakeServer(WrappedAddress realServer){
		head=new FakeServer();
		head.next=head;
		head.prev=head;
		this.realServer=realServer;
		size=0;	//tail and head doesn't count;
		
	}
	
	public boolean insertHead(FakeServer server){
		if(head==null)	
				return false;	//if head is null, return false
		server.next=head.next;
		head.next.prev=server;
		head.next=server;
		server.prev=head;
		size++;
		return true;
	}

	
	public boolean delete(FakeServer server){
		if(server.next==null)
			return false;
		server.next.prev=server.prev;
		server.prev.next=server.next;
		size--;
		return true;
	}
	
	public FakeServer pop(){
		if(size==0)
			return null;
		FakeServer tmp=head.next;
		delete(tmp);
		size--;
		return tmp;
	}
	
	public void setNodeOK(){
		FakeServer tmp = head.next;
		while(!tmp.isHead){
			tmp.setFail(false);
			tmp=tmp.next;
		}
	}
	
	public void setNodeFail(){
		FakeServer tmp = head.next;
		while(!tmp.isHead){
			tmp.setFail(true);
			tmp=tmp.next;
		}
	}
	
	public int getSize() {
		return size;
	}


	@Override
	public int compareTo(LinkedFakeServer o) {
		return this.size-o.size;	//could be any other factors other than size of fake servers
	}


}
