/**
 * 
 */
package net.thenumenorean.essence.utils;

/**
 * This class is a framework for classes which need to run repeatedly at
 * specific intervals until instructed to stop.
 * 
 * This is especially useful for programs which are doing many concurrent
 * operations.
 * 
 * @author Francesco Macagno
 *
 */
public abstract class RepeatingRunnable implements Runnable {

	private boolean stopped;
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
		stopped = false;
		this.wait = wait;
	}

	/**
	 * Signal this RepeatingRunnable to stop, and immediately return. The
	 * currently running code will be allowed to finish.
	 */
	public void stop() {
		stopped = true;
		if (thr != null)
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
	 * Get whether stopped has been called
	 * @return
	 */
	protected boolean stoppedCalled() {
		return stopped;
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
		
		//Set stopped to false so this can be re-run
		stopped = false;

		runBefore();

		thr = Thread.currentThread();

		while (!stopped) {

			loop();

			try {
				Thread.sleep(wait);
			} catch (InterruptedException e) {
			}

		}

		thr = null;

		runAfter();

	}

	/**
	 * A method which will be called repeatedly until stop is called, pausing
	 * the initialized time between each run.
	 */
	public abstract void loop();

	/**
	 * A function which is run before the main loop for setup. Can be overridden
	 * by subclasses.
	 */
	public void runBefore() {
	}

	/**
	 * A function which is run after the main loop ends. Can be overridden by
	 * subclasses.
	 */
	public void runAfter() {
	}

}
