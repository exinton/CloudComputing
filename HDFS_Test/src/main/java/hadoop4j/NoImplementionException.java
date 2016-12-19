package hadoop4j;

public class NoImplementionException extends Exception{
	String error ="The request type is not supported or implemented";
	 public NoImplementionException() {
	        
	    }
	 public NoImplementionException(String message) {
	        super(message);
	    }
	 
	public void f() throws NoImplementionException {
        throw new NoImplementionException(error);
    }
}
