package wrapper;

public interface IWrapperHolder {
	
	boolean updateWrapper(Wrapper wrapper);
	boolean startUpdatingWrapper();
	boolean stopUpdatingWrapper();
	Wrapper obtainWrapper();
	
}
