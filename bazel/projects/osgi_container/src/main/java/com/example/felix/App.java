package com.example.felix;

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.framework.Felix;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

public class App {
	public String getGreeting() {
		return "Hello World!";
	}

	public static void main(String[] args) {
		// Configuration properties for the OSGi framework
		Map<String, Object> configMap = new HashMap<>();
		configMap.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

		Felix felix = new Felix(configMap);

		try {
			// Start the OSGi framework
			felix.start();
			BundleContext context = felix.getBundleContext();

			// Install and start a bundle
			Bundle bundle = context.installBundle("file:bundle.jar");
			bundle.start();

			// Stop the OSGi framework
			felix.stop();
			felix.waitForStop(0);

		} catch (BundleException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
