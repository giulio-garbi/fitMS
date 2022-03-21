package monitoring;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class ThrBatchMeans {
	protected final LinkedBlockingQueue<Double> unprocessedSamples = new LinkedBlockingQueue<>(); //ExitTimes
	public final BatchMeans thr;
	protected final double samplingTime;
	
	public ThrBatchMeans(int batchSize, double samplingTime) {
		this.thr = new BatchMeans(batchSize);
		this.samplingTime = samplingTime;
	}
	
	public void add(double exitTime) {
		unprocessedSamples.add(exitTime);
	}
	
	public synchronized void updateStats() {
		ArrayList<Double> newSamples = new ArrayList<>();
		this.unprocessedSamples.drainTo(newSamples);
		newSamples.sort(Double::compare);
		int[] samples = new int[(int)((newSamples.get(newSamples.size()-1)-newSamples.get(0))/this.samplingTime)+1];
		for(double t:newSamples) {
			samples[(int)((t-newSamples.get(0))/this.samplingTime)]++;
		}
		// drop last sample: it is incomplete
		for(int i=0; i<samples.length-1; i++) {
			thr.add(samples[i]/this.samplingTime);
		}
		thr.updateStats();
	}
}
