package wrapper;

import java.util.Comparator;

public class VolumeComparator implements Comparator<LinkedFakeServer> {

	@Override
	public int compare(LinkedFakeServer o1, LinkedFakeServer o2) {
		
		return o1.getVolumeCapacity()-o2.getVolumeCapacity();
	}


}
