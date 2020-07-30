package classifier;

public class Words {
	public String text;
	
	public String wordCategory;
	
	public int count;
	
	public double freq;
	
	public double prob;
	
	
	/*
	 * Constructor
	 */
	
	public Words(String text, String wordCategory, int count) {
		this.text = text;
		this.wordCategory = wordCategory;
		this.count = count;
		this.freq = 0.0;
		this.prob = 0.0;
	}
}
