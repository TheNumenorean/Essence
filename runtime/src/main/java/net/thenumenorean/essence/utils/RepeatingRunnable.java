/**
 * 
 */
package net.thenumenorean.essence.utils;

/**
 * This class is a framework for classes which need to run repeatedly at
 * specific intervals until instructed to stop.
 * 
 * This is especially useful for programs which are doing many concurrent operations.
 * 
 * @author Francesco Macagno
 *
 */
public abstract class RepeatingRunnable implements Runnable {

	private boolean stop;
	private int wait;
	private Thread thr;

	/**
	 * Creates a repeating runnable which waits the given amount of time between
	 * each run.
	 * 
	 * @param wait
	 *            The time (in ms) to wait.
	 */
	public RepeatingRunnable(int wait) {
		stop = false;
		this.wait = wait;
	}

	/**
	 * Signal this RepeatingRunnable to stop, and immediately return. The
	 * currently running code will be allowed to finish.
	 */
	public void stop() {
		stop = true;
		thr.interrupt();
	}

	/**
	 * Stop and wait for the thread to finish executing, then return.
	 * 
	 */
	public void stopAndWait() {
		stop();
		try {
			thr.join();
		} catch (Exception e) {
		}
	}

	/**
	 * Is this RepeatingRunnable still running
	 * 
	 * @return True if yes, false otherwise
	 */
	public boolean isRunning() {
		return thr != null;
	}

	@Override
	public void run() {

		thr = Thread.currentThread();

		while (!stop) {

			runOnce();

			try {
				Thread.sleep(wait);
			} catch (InterruptedException e) {
			}

		}

		thr = null;

	}

	/**
	 * A method which will be called repeatedly until stop is called, pausing
	 * the initialized time between each run.
	 */
	public abstract void runOnce();

}
