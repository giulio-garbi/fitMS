package test_informal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import jni.GetThreadID;

public class testJni {
	// Engaged in two infinite loops, you can see two threads with 100% CPU usage
	public static void main(String[] args) {
		int tid = GetThreadID.get_tid();
		System.out.println("main TID=" + tid);
		TestOtherThr t = new TestOtherThr();
		t.start();
		//GetThreadID.setAffinity(tid, 1, 1);

		//String[] cmds = new String[] { "taskset", "-cp", String.valueOf(tid) };
		/*try {
			Process p = Runtime.getRuntime().exec(cmds);
			BufferedReader is = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = is.readLine()) != null)
				System.out.println(line);
			is.close();
			p.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}

	public static class TestOtherThr extends Thread {
		public void run(){
			int tid = GetThreadID.get_tid();
                	System.out.println("inner TID=" + tid);
		}
	}
}
