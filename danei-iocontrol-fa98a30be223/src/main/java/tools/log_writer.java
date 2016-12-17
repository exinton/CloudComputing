package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class log_writer {
	String file_name;
	FileWriter fw = null;
	BufferedWriter writer = null;

	public log_writer(String file_name) {
		this.file_name = file_name;
		try {
			fw = new FileWriter(file_name);
			writer = new BufferedWriter(fw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void write(String content) {
		try {
			writer.append(content+"\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void close_log_writer() {
		try {
			writer.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
