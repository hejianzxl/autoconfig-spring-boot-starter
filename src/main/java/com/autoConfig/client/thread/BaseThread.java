package com.autoConfig.client.thread;

/**
 * 抽象线程
 * @author hejian
 *
 */
public abstract class BaseThread implements Runnable {

	@Override
	public void run() {
		this.execute();
	}
	
	public abstract void execute();
}
