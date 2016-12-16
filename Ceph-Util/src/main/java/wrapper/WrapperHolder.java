package wrapper;

import java.io.File;

public class WrapperHolder {
	private Wrapper wrapper;
	private File jsonFile;
	private WrapperHolderType type;
	public WrapperHolder(Wrapper wrapper){
		wrapper=wrapper;
	}
	public WrapperHolderType getType() {
		return type;
	}
	public void setType(WrapperHolderType type) {
		this.type = type;
	}
	public WrapperHolder(){

	}
	public Wrapper getWrapper() {
		return wrapper;
	}
	public void setWrapper(Wrapper wrapper) {
		this.wrapper = wrapper;
	}
	public File getJsonFile() {
		return jsonFile;
	}
	public void setJsonFile(File jsonFile) {
		this.jsonFile = jsonFile;
	}
	

}
