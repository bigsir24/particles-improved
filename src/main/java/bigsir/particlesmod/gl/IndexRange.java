package bigsir.particlesmod.gl;

public class IndexRange {
	public int minIndex;
	public int maxIndex;

	public IndexRange(int minIndex, int maxIndex) {
		this.minIndex = minIndex;
		this.maxIndex = maxIndex;
	}

	public boolean mergeIfWithin(int minIndexOther, int maxIndexOther, int mergeThreshold) {
		// Extend range by threshold

		int distance1 = minIndex - maxIndexOther - mergeThreshold;
		int distance2 = maxIndex - minIndexOther + mergeThreshold;

		//System.out.println(maxIndexOther + ", " + distance1 + ", " + distance2);

		if (distance1 * distance2 <= 0) { //Overlap
			if (minIndexOther < minIndex) minIndex = minIndexOther;
			if (maxIndexOther > maxIndex) maxIndex = maxIndexOther;
			return true;
		}

		return false;
	}

	public int length() {
		return maxIndex - minIndex;
	}
}
