package wrapper;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cephmap.cephmapmonitors.CephGlobalParameter;
import cephmapnode.CephMap;
import net.Address;

public class WrapperTest {
	
	Wrapper wrapper=null;
	ObjectMapper mapper=null;
	
	@Before	
	public void setup(){
	        wrapper=new Wrapper();
	}


	@Test
	public void testInitWrapper() {
		assertEquals(true,wrapper.initWrapper());
	}

	@Test
	public void testSearch() {
		assertEquals(wrapper.fakeServerLists.peek().realServer,wrapper.search(wrapper.fakeServerLists.peek().head.next.getFakeServerAddr()));
		assertEquals(true,wrapper.fakeServerLists.peek().realServer.equals(wrapper.search(wrapper.fakeServerLists.peek().head.next.next.getFakeServerAddr())));
	}

	@Test
	public void testRemoveRealServer() {
		assertEquals(true,wrapper.removeRealServer(wrapper.fakeServerLists.peek().realServer.getIp(), wrapper.fakeServerLists.peek().realServer.getPort()));
	}

	@Test
	public void testAddRealServer() {
		WrappedAddress addr= new  WrappedAddress("1.1.1.1",1112);
		wrapper.addRealServer(addr);
		assertEquals(addr.equals(wrapper.fakeServerLists.peek().realServer),true);
	}

	@Test
	public void testSetFakeServerListFail() {
		WrappedAddress server=wrapper.fakeServerLists.peek().realServer;
		wrapper.setFakeServerListFail(server);
		LinkedFakeServer linkedFakeServer = wrapper.realServerMap.get(server.serverAddr);
		FakeServer tmp =linkedFakeServer.head.next;				
		while(!tmp.isHead){
			assertEquals(true,tmp.isFail());
			tmp=tmp.next;
		}
	}

	@Test
	public void testSetFakeServerListOk() {
		WrappedAddress server=wrapper.fakeServerLists.peek().realServer;
		wrapper.setFakeServerListOk(server);
		LinkedFakeServer linkedFakeServer = wrapper.realServerMap.get(server.serverAddr);
		FakeServer tmp =linkedFakeServer.head.next;				
		while(!tmp.isHead){
			assertEquals(false,tmp.isFail());
			tmp=tmp.next;
		}
	}
	
	@Test
	public void testsaveToJSON(){
		
		String fileName=System.getenv("CEPH_HOME")+ File.separator+"wrapper.json";
		System.out.println(fileName);
		File file = new File(fileName);		
		assertEquals(true,WrapperUtils.saveToJSON(file, wrapper));
	}
	
	@Test
	public void testsearchFile(){
		String fileName="test1.txt";
		List<Address> list =wrapper.searchFile(fileName);
		for(Address addr:list)
			System.out.println("selected physical server:"+addr.getIp());
		assertEquals(2,list.size());
	}
	
	

}
