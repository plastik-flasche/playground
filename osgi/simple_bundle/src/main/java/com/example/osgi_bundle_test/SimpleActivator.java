package com.example.osgi_bundle_test;

import java.io.File;
import java.io.IOException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class SimpleActivator implements BundleActivator {
	Thread t;

	@Override
	public void start(BundleContext context) {
		System.out.println("Bundle: Starting");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					File file = new File(System.getProperty("user.home") + File.separator + "INSECURE");
					if (file.createNewFile()) {
						System.out.println("File created: " + file.getName());
					} else {
						System.out.println("File already exists.");
					}
				} catch (IOException e) {
					System.out.println("An error occurred.");
					e.printStackTrace();
				} catch (SecurityException e) {
					System.out.println("Security error occurred.");
					e.printStackTrace();
				}
			}
		});
		t.start();
		System.out.println("Bundle: Started");
	}

	@Override
	public void stop(BundleContext context) {
		System.out.println("Bundle: Stopping");
		t.interrupt();
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Bundle: Stopped");
	}
}
