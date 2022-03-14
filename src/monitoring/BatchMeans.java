package monitoring;

import java.util.ArrayList;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class BatchMeans {
	protected final ArrayList<ArrayList<Long>> samples = new ArrayList<>();
	protected long lastBatchSum = 0;
	protected final SummaryStatistics meanOfBatch = new SummaryStatistics(); //skip first batch
	public final int batchSize;
	public double mean = Double.NaN;
	public double[] CI = {Double.NaN, Double.NaN};
	
	public double confLvl = 0.99;
	
	public BatchMeans(int batchSize) {
		this.batchSize = batchSize;
	}
	
	public void add(long x) {
		if(samples.size() == 0 || samples.get(samples.size()-1).size() == this.batchSize) {
			samples.add(new ArrayList<>());
			lastBatchSum = 0;
		}
		samples.get(samples.size()-1).add(x);
		lastBatchSum += x;
		if(samples.get(samples.size()-1).size() == this.batchSize && samples.size()>=2) {
			long lastBatchMean = lastBatchSum/this.batchSize;
			this.meanOfBatch.addValue(lastBatchMean);
		}
		System.out.println("B "+samples.size()+" - "+samples.get(samples.size()-1).size()+" x "+x);
	}
	
	public void updateStats() {
		if(this.meanOfBatch.getN()>=2) {
			// see https://gist.github.com/gcardone/5536578
			long df = this.meanOfBatch.getN()-1;
			TDistribution tDist = new TDistribution(df);
			double critVal = tDist.inverseCumulativeProbability(1.0 - (1 - confLvl) / 2);
			double ciWidth = critVal * this.meanOfBatch.getStandardDeviation() / Math.sqrt(this.meanOfBatch.getN());
			double mean = this.meanOfBatch.getMean();
			double lowCi = this.meanOfBatch.getMean() - ciWidth;
			double upCi = this.meanOfBatch.getMean() + ciWidth;
			this.mean = mean;
			this.CI = new double[]{lowCi, upCi};
		} else {
			this.mean = Double.NaN;
			this.CI = new double[]{Double.NaN, Double.NaN};
		}
	}
}
