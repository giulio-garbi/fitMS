package monitoring;

public class JustMean {
	protected double first;
	protected double last;
	protected int exitCount = 0;
	
	public double mean = Double.NaN;
	public double[] CI = {Double.NaN, Double.NaN};
	public int totalNumCompletedBatches = 0; 
	
	public void add(double t) {
		if(exitCount == 0) {
			this.first = t;
			this.last = t;
			this.exitCount = 1;
		} else {
			this.first = Math.min(t, first);
			this.last = Math.max(t, last);
			this.exitCount++;
			this.mean = exitCount/(last-first);
			this.CI[0] = this.CI[1] = mean;
		}
		totalNumCompletedBatches = exitCount;
	}
	
	public void updateStats() {}
}
