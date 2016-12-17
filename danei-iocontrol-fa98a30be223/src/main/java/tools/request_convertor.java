package tools;

public class request_convertor {
	
	public String[] convert(String request){
		request = request.replaceAll("\\\\", "/");
		String[] res = new String[4];
		String tmp1 = request.split(",")[0]; 
		String tmp2 = request.split(",")[1];
		res[0] = tmp1.split(":")[0].trim();
		res[1] = "/"+tmp1.split(":")[1].trim();
		res[2] = tmp2.split(":")[0].trim();
		res[3] = tmp2.split(":")[1].trim();
		return res;
	}
}
