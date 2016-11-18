package crush;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FileListInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3707411957993055543L;

	class FileInfo implements Serializable{
		Long size;
		boolean transferred;
	}
	
	Map <String,FileInfo> list;
	
	public FileListInfo(){
		list = new HashMap<String,FileInfo>();
	}
	public Map<String,FileInfo> getList() {
		return list;
	}

	public void setList(Map<String,FileInfo> list) {
		this.list = list;
	}
	
	public void addFile(String name, Long size){
		FileInfo newFile = new FileInfo();
		newFile.size = size;
		newFile.transferred = false;
		list.put(name,newFile);
	}
	
	public void delFile(String name){
		if(list.containsKey(name))
			list.remove(name);
	}
	public void setOverloaded(String name){
		if(list.containsKey(name)){
			FileInfo changedInfo = list.get(name);
			changedInfo.transferred = true;
			list.put(name, changedInfo);
		}
	}
	public boolean isOverloaded(String name){
		if(list.containsKey(name)){
			return list.get(name).transferred;
		}
		return false;
	}
	public void print(){
		for(String name : list.keySet()){
			System.out.println(name+"     "+list.get(name).size+"     "+list.get(name).transferred);
		}
	}
}
