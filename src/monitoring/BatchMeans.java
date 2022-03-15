package monitoring;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class BatchMeans {
	protected final LinkedBlockingQueue<Double> unprocessedSamples = new LinkedBlockingQueue<>();
	protected double lastBatchSum = 0;
	protected int lastBatchLen = 0;
	public int totalNumCompletedBatches = 0;
	protected final SummaryStatistics meanOfBatch = new SummaryStatistics(); //skip first batch
	protected long lastMOB_N = -1;
	public final int batchSize;
	public double mean = Double.NaN;
	public double[] CI = {Double.NaN, Double.NaN};
	
	public double confLvl = 0.95;
	
	public BatchMeans(int batchSize) {
		this.batchSize = batchSize;
	}
	
	public void add(double x) {
		unprocessedSamples.add(x);
	}
	
	private void processSamples() {
		ArrayList<Double> newSamples = new ArrayList<>();
		this.unprocessedSamples.drainTo(newSamples);
		
		for(double x : newSamples) {
			this.lastBatchSum += x;
			this.lastBatchLen++;
			if(this.lastBatchLen == this.batchSize) {
				if(this.totalNumCompletedBatches > 0) {
					double lastBatchMean = this.lastBatchSum/this.batchSize;
					this.meanOfBatch.addValue(lastBatchMean);
				}
				this.lastBatchSum = 0;
				this.lastBatchLen = 0;
				this.totalNumCompletedBatches++;
			}
		}
		
		//System.out.println("B "+this.totalNumCompletedBatches+" + "+this.lastBatchLen+" x "+x);
	}
	
	private void computeCI() {
		if(this.lastMOB_N != this.meanOfBatch.getN()) {
			this.lastMOB_N = this.meanOfBatch.getN();
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
	
	public synchronized void updateStats() {
		this.processSamples();
		this.computeCI();
	}
}
