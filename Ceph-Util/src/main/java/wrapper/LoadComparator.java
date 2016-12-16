package wrapper;

import java.util.Comparator;

public class LoadComparator implements Comparator<LinkedFakeServer> {

	@Override
	public int compare(LinkedFakeServer o1, LinkedFakeServer o2) {
		return o1.getLoadCapacity()-o2.getLoadCapacity();
	}
	

}
