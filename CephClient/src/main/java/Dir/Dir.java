package Dir;

import java.io.File;
import java.net.URL;

public class Dir {

	public static void main(String[] args) {
		ClassLoader classLoader = Dir.class.getClassLoader();
	    File classpathRoot = new File(classLoader.getResource("").getPath());

	    String res= classpathRoot.getPath();
	    System.out.println(res);
	    final String dir = System.getProperty("user.dir");
        System.out.println("current dir = " + dir);
	}

}
