package monitoring;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class ThrBatchMeans {
	protected final LinkedBlockingQueue<Double> unprocessedSamples = new LinkedBlockingQueue<>(); //ExitTimes
	public final BatchMeans thr;
	protected final double samplingTime;
	
	private double lastBlockBeginsAt = Double.NaN;
	private int lastBlockSamples = -1;
	private boolean firstBlock = true;
	
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
		//System.out.println("******TBM****** "+newSamples.size());
		if(newSamples.size() > 0) {
			newSamples.sort(Double::compare);
			
			int[] samples = new int[0];
			if(firstBlock) {
				samples = new int[(int)((newSamples.get(newSamples.size()-1)-newSamples.get(0))/this.samplingTime)+1];
				for(double t:newSamples) {
					samples[Math.max((int)((t-newSamples.get(0))/this.samplingTime),0)]++;
				}
				lastBlockBeginsAt = newSamples.get(0) + this.samplingTime*(samples.length-1);
				lastBlockSamples = samples[samples.length-1];
				firstBlock = false;
			} else {
				samples = new int[(int)((newSamples.get(newSamples.size()-1)-lastBlockBeginsAt)/this.samplingTime)+1];
				samples[0] = lastBlockSamples;
				for(double t:newSamples) {
					samples[Math.max((int)((t-lastBlockBeginsAt)/this.samplingTime),0)]++;
				}
				lastBlockBeginsAt += this.samplingTime*(samples.length-1);
				lastBlockSamples = samples[samples.length-1];
			}
			
			//System.out.println("******nSlots****** "+samples.length);
			// drop last sample: it is incomplete
			for(int i=0; i<samples.length-1; i++) {
				thr.add(samples[i]/this.samplingTime);
			}
			thr.updateStats();
		}
	}
}
