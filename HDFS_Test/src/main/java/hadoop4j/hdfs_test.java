package hadoop4j;

public class hdfs_test {
		public static void main(String[] args) {
			HDFS_Client hdfs =new HDFS_Client();
			try {
				hdfs.seq_read("/test2/bc9c8ba7ff351f3e","6777482","44525194");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
}
