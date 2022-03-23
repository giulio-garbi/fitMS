package monitoring;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import net.spy.memcached.MemcachedClient;

public class rtSampler implements Runnable {

	//private ConcurrentLinkedQueue<rtSample> rt = null;
	private MemcachedClient memcachedClient = null;
	private String monirotHost = null;
	private String name = null;
	private BatchMeans bmRT = new BatchMeans(30);
	private ThrBatchMeans bmThr = new ThrBatchMeans(30, 0.05);

	public rtSampler(String monirotHost, String name) {
		//this.rt = new ConcurrentLinkedQueue<rtSample>();
		this.monirotHost = monirotHost;
		this.name = name;
		try {
			this.memcachedClient = new MemcachedClient(new InetSocketAddress(this.monirotHost, 11211));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		//rtSample[] samples = this.rt.toArray(new rtSample[0]);
		//this.saveRT(samples);
		this.saveBM();
	}
	
	private void saveBM() {
		bmRT.updateStats();
		bmThr.updateStats();
		try {
			System.out.println(this.name+" is writing");
			this.memcachedClient.set("rt_"+this.name, 3600, String.valueOf(bmRT.mean)).get();
			this.memcachedClient.set("lowCI_rt_"+this.name, 3600, String.valueOf(bmRT.CI[0])).get();
			this.memcachedClient.set("upCI_rt_"+this.name, 3600, String.valueOf(bmRT.CI[1])).get();
			this.memcachedClient.set("batches_rt_"+this.name, 3600, String.valueOf(bmRT.totalNumCompletedBatches)).get();
			this.memcachedClient.set("thr_"+this.name, 3600, String.valueOf(bmThr.thr.mean)).get();
			this.memcachedClient.set("lowCI_thr_"+this.name, 3600, String.valueOf(bmThr.thr.CI[0])).get();
			this.memcachedClient.set("upCI_thr_"+this.name, 3600, String.valueOf(bmThr.thr.CI[1])).get();
			this.memcachedClient.set("batches_thr_"+this.name, 3600, String.valueOf(bmThr.thr.totalNumCompletedBatches)).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public void addSample(rtSample sample) {
		//this.rt.add(sample);
		this.bmRT.add(sample.getRT());
		this.bmThr.add(sample.getEndSec());
	}

}
