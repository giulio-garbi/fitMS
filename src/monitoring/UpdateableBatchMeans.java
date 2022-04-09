package monitoring;

import java.math.BigDecimal;
import java.util.HashMap;

public class UpdateableBatchMeans {
	/*
	 *   Each square is a bucket, whose time width is this.samplingTime.
	 *   N buckets form a block.
	 *   In each bucket we count how many terminations in that time slice
	 * 
	 *   +---+---+---+---+- .... -+---+
	 *   |   |   |   |   |        |   |
	 *   +---+---+---+---+- .... -+---+
	 *   |   |   |   |   |        |   |
	 *   +---+---+---+---+- .... -+---+
	 *   |   |   |   |   |        |   |
	 *   +---+---+---+---+- .... -+---+
	 */
	
	protected final double samplingTime=0.05; // time span of a bucket
	protected final int N=30; // nr of buckets in a block;
	
	protected final double referenceTime; // where bucket #0 begins. Bucket indexes might be negative!
	protected int firstBucketIndex;
	protected int lastBucketIndex;
	protected int firstBlockIndex;
	protected int lastBlockIndex;
	
	protected boolean empty = true;
	
	protected HashMap<Long, Integer> bucketCount = new HashMap<>(); // how many terminations in each bucket
	protected HashMap<Integer, Integer> blockCount = new HashMap<>(); // how many terminations in each block
	protected HashMap<Integer, BigDecimal> bucketThroughputMeanInBlock = new HashMap<>(); // average throughput in each block
	protected BigDecimal sumBTMIB = BigDecimal.ZERO; // sum of bucketThroughputMeanInBlock
	protected BigDecimal sum2BTMIB = BigDecimal.ZERO; // sum of squares bucketThroughputMeanInBlock
	
	// answers
	public double mean = Double.NaN;
	public double[] CI = {Double.NaN, Double.NaN};
	
	public UpdateableBatchMeans(double referenceTime) {
		this.referenceTime = referenceTime;
	}
	
	public void add(double t) {
		long buckIdx = getBucketIndex(t);
		int blkIdx = getBlockIndex(buckIdx);
		
		
	}
	
	protected long getBucketIndex(double t) {
		return (long) Math.floor((t-referenceTime)/samplingTime);
	}
	
	protected int getBlockIndex(long bucketIndex) {
		return (int)(bucketIndex/N);
	}
}
