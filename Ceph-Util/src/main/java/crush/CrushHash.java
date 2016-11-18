package crush;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */




/**
 *
 * @author Ashwin
 */
public class CrushHash{
    
    long a,b,c;
    private static final long MAX_VALUE = 0xFFFFFFFFL;
	private static final double MAX_NODE = 15359.0;
    
	
	private long add(long val, long add) {
		return (val + add) & MAX_VALUE;
	}
	
	/**
	 * Do subtraction and turn into 4 bytes. 
	 */
	private long subtract(long val, long subtract) {
		return (val - subtract) & MAX_VALUE;
	}
	
	/**
	 * Left shift val by shift bits and turn in 4 bytes. 
	 */
	private long xor(long val, long xor) {
		return (val ^ xor) & MAX_VALUE;
	}
	
	/**
	 * Left shift val by shift bits.  Cut down to 4 bytes. 
	 */
	private long leftShift(long val, int shift) {
		return (val << shift) & MAX_VALUE;
	}
	
	
    
    public double hashFunction(String s1,int r,String cid){
            
           a = s1.hashCode();
           b = r & MAX_VALUE;
           c = cid.hashCode();
        
           a = subtract(a, b); a = subtract(a, c); a = xor(a, c >> 13);
	   b = subtract(b, c); b = subtract(b, a); b = xor(b, leftShift(a, 8));
	   c = subtract(c, a); c = subtract(c, b); c = xor(c, (b >> 13));
	   a = subtract(a, b); a = subtract(a, c); a = xor(a, (c >> 12));
	   b = subtract(b, c); b = subtract(b, a); b = xor(b, leftShift(a, 16));
	   c = subtract(c, a); c = subtract(c, b); c = xor(c, (b >> 5));
	   a = subtract(a, b); a = subtract(a, c); a = xor(a, (c >> 3));
	   b = subtract(b, c); b = subtract(b, a); b = xor(b, leftShift(a, 10));
	   c = subtract(c, a); c = subtract(c, b); c = xor(c, (b >> 15));
           
           
        return (c%MAX_NODE)/MAX_NODE;
    }
    
    
}
