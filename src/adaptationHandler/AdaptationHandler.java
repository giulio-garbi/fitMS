package adaptationHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

import Server.SimpleTask;
import net.spy.memcached.MemcachedClient;

public class AdaptationHandler implements Runnable {

	private SimpleTask task = null;
	private MemcachedClient memcachedClient = null;

	public AdaptationHandler(SimpleTask task, String jedisHost) {
		this.task = task;
		try {
			this.memcachedClient = new MemcachedClient(new InetSocketAddress(jedisHost, 11211));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			Integer swCore = Integer.valueOf(String.valueOf(memcachedClient.get(this.task.getName() + "_sw")));
			Float hwCore = Float.valueOf(String.valueOf(memcachedClient.get(this.task.getName() + "_hw")));
			if (hwCore != null) {
				int swcore = Math.max(1, Double.valueOf(Math.ceil(hwCore)).intValue());
				try {
					this.task.setThreadPoolSize(swcore);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					System.out.println(String.format("%s-%s", String.valueOf(swcore), String.valueOf(hwCore)));
				}
				this.task.setHwCore(hwCore);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SimpleTask getTask() {
		return task;
	}

	public void setTask(SimpleTask task) {
		this.task = task;
	}

}
