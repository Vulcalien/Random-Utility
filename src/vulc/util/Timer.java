/*******************************************************************************
 * Copyright 2019 Vulcalien
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package vulc.util;

/**
 * Timer is a simple timer that performs an action periodically.
 * @author Vulcalien
 */
public class Timer {

	private final Runnable runnable;
	private boolean mayContinue = false;
	private boolean running = false;

	/**
	 * Creates the timer. It has to be started.
	 * @param nanoseconds the timer delay in nanoseconds
	 * @param action the action to perform
	 */
	public Timer(long nanoseconds, Runnable action) {
		this.runnable = new Runnable() {
			public void run() {
				running = true;

				long nanosPerTick = nanoseconds;
				long unprocessedNanos = 0;
				long lastTime = System.nanoTime();

				while(mayContinue) {
					long now = System.nanoTime();
					long passedTime = now - lastTime;
					lastTime = now;
					if(passedTime < 0) passedTime = 0;

					unprocessedNanos += passedTime;

					while(unprocessedNanos > nanosPerTick) {
						action.run();
						unprocessedNanos -= nanosPerTick;
					}

					if(mayContinue) try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				running = false;
			}
		};
	}

	/**
	 * Creates the timer. It has to be started.
	 * @param milliseconds the timer delay in milliseconds
	 * @param action the action to perform
	 */
	public Timer(int milliseconds, Runnable action) {
		this(milliseconds * 1_000_000L, action);
	}

	/**
	 * If the timer is not running, starts a new thread.
	 * @return false - if the timer was already running
	 */
	public boolean start() {
		if(!running) {
			mayContinue = true;
			new Thread(runnable).start();
			return true;
		}
		return false;
	}

	/**
	 * Stops the timer.<br>
	 * isRunning() may return true even if stop() was called.
	 */
	public void stop() {
		mayContinue = false;
	}

	/**
	 * @return true - if the timer is running
	 */
	public boolean isRunning() {
		return running;
	}

}
