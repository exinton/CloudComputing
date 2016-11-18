package wrapper;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import cephmapnode.CephMap;
import cephmapnode.CephNode;

public class WrapperUtilsTest {
	

	@Test
	public void testGetCephMap() {
		CephMap cephmap = WrapperUtils.getCephMap();
		ArrayList<CephNode> list =cephmap.getAvailableOSDList();
		Properties prop = WrapperUtils.getProperties("init_map.properties");
		int row=Integer.valueOf(prop.getProperty("row"));
		int cabinet=Integer.valueOf(prop.getProperty("cabinet"));
		int disk=Integer.valueOf(prop.getProperty("disk"));
		assertEquals(row*disk*cabinet,list.size());
		String fileName=System.getenv("CEPH_HOME")+ File.separator+"newCephMap.json";
		File file = new File(fileName);		
		WrapperUtils.saveToJSON(file, cephmap);
		
	}

	@Test
	public void testSetIP() {
		CephNode node1 = new CephNode();
		node1.setAddress("1.2.3.0", 0);
		CephNode node2 = new CephNode();
		boolean res =WrapperUtils.setIP(node1, node2, 234);
		assertEquals(true,res);
		assertEquals("1.2.3.234",node2.getAddress().getIp());
		assertEquals(node2.getAddress().getPort(),1000);
		
		//
		CephNode node0 = new CephNode();
		node0.setAddress("1.0.0.0", 0);
		CephNode node4 = new CephNode();
		boolean res1 =WrapperUtils.setIP(node0, node4, 231);
		assertEquals(true,res1);
		assertEquals("1.231.0.0",node4.getAddress().getIp());
		assertEquals(0,node4.getAddress().getPort());
	}

	@Test
	public void testGenerateRowCephNode() {
		//generateRowCephNode(CephNode parent,int[] parentId,Object number,String type,int level){
		CephNode node0 = new CephNode();
		node0.setAddress("1.2.0.0", 0);
		Properties prop = WrapperUtils.getProperties("init_map.properties");
		int[] parentId=new int[]{123};
		
		ArrayList<CephNode> list =WrapperUtils.generateRowCephNode(node0, parentId, prop.getProperty("cabinet"), "cabinet", 2);
		for(CephNode node:list){
			assertEquals(node.getLevelNo(),2);
			assertEquals(node.getType(),"cabinet");
			assertEquals(node.getAddress().getPort(),0);
		}
		assertEquals(prop.get("cabinet"),String.valueOf(list.size()));
		
	}

	@Test
	public void testGetProperties() {
		Properties prop = WrapperUtils.getProperties("init_map.properties");
		assertEquals(prop.get("disk"),"50");
		assertEquals(prop.get("cabinet"),"5");
		assertEquals(prop.get("row"),"2");

	}
	
	@Test
	public void testgetPhysicalNodes(){
		Properties prop = WrapperUtils.getProperties("physicalNodeRedundancy.properties");
		List<WrappedAddress> list=WrapperUtils.getPhysicalNodes();
		int size=Integer.valueOf(prop.getProperty("numberOfNodes"));
		assertEquals(size,list.size());
	}
	
	@Test
	public void testloadFromJSON(){
		String fileName=System.getenv("CEPH_HOME")+ File.separator+"wrapper.json";
		File file = new File(fileName);
		Wrapper wrapper = WrapperUtils.loadFromJSON(file);
		assertEquals(wrapper.map.size()>0,true);
		wrapper.printLayOut();
		WrapperUtils.rebalanceWrapper(wrapper);
	}
	
	@Test
	public void testrebalanceWrapper(){
		String fileName=System.getenv("CEPH_HOME")+ File.separator+"wrapper.json";
		File file = new File(fileName);
		Wrapper wrapper = WrapperUtils.loadFromJSON(file);
		wrapper.addRealServer(new WrappedAddress("10.10.10.100",9567));
		System.out.println("rebalance begin!");
		Wrapper wrappers = WrapperUtils.rebalanceWrapper(wrapper);
		WrapperUtils.saveToJSON(file, wrappers);
		wrappers.printLayOut();
	}
	
	@Test
	public void testrebalanceWrapperAfterRemoving(){
		String fileName=System.getenv("CEPH_HOME")+ File.separator+"wrapper.json";
		File file = new File(fileName);
		Wrapper wrapper = WrapperUtils.loadFromJSON(file);
		wrapper.addRealServer(new WrappedAddress("10.10.10.100",9567));
		System.out.println("before removing, rebalance begin!");
		Wrapper wrappers = WrapperUtils.rebalanceWrapper(wrapper);
		wrappers.printLayOut();
		if(!wrappers.removeRealServer("10.10.10.100", 9567)){
			System.out.println("unable to remove");
			return;
		}	
		wrappers=WrapperUtils.rebalanceWrapper(wrappers);
		System.out.println("after removing, rebalance begin!");
		wrappers.printLayOut();
		WrapperUtils.saveToJSON(file, wrappers);
	}

}
