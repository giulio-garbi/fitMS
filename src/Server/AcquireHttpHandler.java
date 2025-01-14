package Server;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import memcachedPool.PooledMemcachedClient;
import net.spy.memcached.MemcachedClient;

public class AcquireHttpHandler implements HttpHandler {
	
	private static final boolean QLENS_ENABLED = false;

	private SimpleTask task = null;
	HttpExchange req = null;
	ArrayList<Runnable> backlog = null;
	private ThreadLocalRandom rnd = null;
	private PooledMemcachedClient memcachedClient = null;

	public AcquireHttpHandler(SimpleTask task) {
		this.task = task;
		this.backlog = new ArrayList<Runnable>();
		this.rnd = ThreadLocalRandom.current();
		this.memcachedClient=QLENS_ENABLED?this.task.getMemcachedPool().getConnection():null;
	}

//	public void measure(String entry, String snd) {
//		
//		Jedis jedis = this.getTask().getJedisPool().getResource();
//		Transaction t = jedis.multi();
//
//		if (snd.equals("think"))
//			t.decr("think");
//		else
//			t.decr(String.format("%s_ex", snd));
//
//		t.incr(String.format("%s_bl", entry));
//		t.exec();
//		t.close();
//		jedis.close();
//	}

	public void measure(String entry, String snd) {
		if(QLENS_ENABLED) {
			try {
				if (snd.equals("think")) {
					// this.memcachedClient.decr("think", 1);
					MCAtomicUpdater.AtomicIncr(this.memcachedClient, -1, "think", 100);
				} else {
					// this.memcachedClient.decr(String.format("%s_ex", snd), 1);
					MCAtomicUpdater.AtomicIncr(this.memcachedClient, -1, String.format("%s_ex", snd), 100);
				}
				// this.memcachedClient.incr(String.format("%s_bl", entry), 1);
				MCAtomicUpdater.AtomicIncr(this.memcachedClient, 1, String.format("%s_bl", entry), 100);
	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void handle(HttpExchange req) throws IOException {
		SimpleTask.getLogger().debug(String.format("%s recieved", this.task.getName()));
		Map<String, String> params = this.getTask().queryToMap(req.getRequestURI().getQuery());
		if (params.get("entry") == null || params.get("entry").equals("")) {
			SimpleTask.getLogger().error("Request with no specified entry");
		}
		if (params.get("snd") == null || params.get("snd").equals("")) {
			SimpleTask.getLogger().error("Request with no specified sender");
		}
		this.measure(params.get("entry"), params.get("snd"));
		try {
			Constructor<? extends Runnable> c = null;
			if (this.task.getEntries().get(params.get("entry")) == null) {
				SimpleTask.getLogger().error(String.format("No class registered for entry %s at task %s", params.get("entry"), this.task.name));
			}
			if (this.task.getsTimes().get(params.get("entry")) == null) {
				SimpleTask.getLogger()
						.error(String.format("No service time registered for entry %s at task %s", params.get("entry"), this.task.name));
			}
			// System.out.println(this.task.getEntries().get(params.get("entry")));
			c = this.task.getEntries().get(params.get("entry")).getDeclaredConstructor(SimpleTask.class,
					HttpExchange.class, long.class);

			// this.backlog.add(c.newInstance(this.getTask(),
			// req,this.task.getsTimes().get(params.get("entry"))));
			// PER QUESTA APPLICAZIONE NON SERVE GPS
			// SimpleTask.getLogger().debug("GPS choice made");
			// this.task.getThreadpool().submit(this.backlog.get(this.rnd.nextInt(this.backlog.size())));
			long stime = System.nanoTime();
			if (params.get("stime") != null)
				stime = Long.valueOf(params.get("stime"));
			else
				stime = System.nanoTime();
			
			this.task.getEnqueueTime().put(params.get("id"), stime);

			// implemento fcfs usando la coda del threadpool.
			this.task.getThreadpool()
					.submit(c.newInstance(this.getTask(), req, this.task.getsTimes().get(params.get("entry"))));
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}finally {
			if(QLENS_ENABLED) {
				this.memcachedClient.close();
			}
		}
	}

	public SimpleTask getTask() {
		return task;
	}

	public void setTask(SimpleTask task) {
		this.task = task;
	}

}