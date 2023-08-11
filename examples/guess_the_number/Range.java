package guess_the_number;

public class Range {

	private int min, max;
	
	public Range(int min, int max) {
		this.min = Math.min(min, max);
		this.max = Math.max(min, max);
	}
	
	public int getMin() {return min;}
	public int getMax() {return max;}
	
}
