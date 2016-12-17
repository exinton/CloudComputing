package test;

import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

public class test_conf {
	public static void main(String[] args) {
		try {
			Wini conf=new Wini(new File("conf/sample/request.ini"));
			Ini.Section runtime=conf.get("generator");
			int countdown=runtime.get("time",int.class,-1);
			System.out.println(countdown);
		} catch (InvalidFileFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
}
