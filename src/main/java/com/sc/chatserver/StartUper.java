package com.sc.chatserver;

import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.servlet.ServletContext;
import java.util.*;

public class StartUper implements Extension, ServletExtension {
	private static boolean startup = true;

	private final Set<Class<?>> startupBeans = new LinkedHashSet<Class<?>>();
	private final List<Class<?>> coreBeans = new ArrayList<Class<?>>();

	<X> void processBean(@Observes ProcessBean<X> event) {

		if (!startup)
			return;

		if (event.getAnnotated().isAnnotationPresent(Startup.class) && event.getAnnotated().isAnnotationPresent(ApplicationScoped.class)) {
			if (!startupBeans.contains(event.getBean().getBeanClass())) {
				startupBeans.add(event.getBean().getBeanClass());
			}
		}


	}

	void afterDeploymentValidation(@Observes AfterDeploymentValidation event) {

		if (!startup)
			return;

		log();

		new Thread(new Runnable() {
			public void run() {
				for (Class<?> bean : startupBeans) {
					try {
						System.out.println("Initializing [" + bean.getName() + "]...");
						CDI.current().select(bean).get().toString();
					} catch (Exception e) {
						System.out.println("Error in initializing [" + bean.getName() + "]");
						e.printStackTrace();
					}
				}

				startup = false;

				System.out.println("**********************************************************");
				System.out.println("******************* SERVER IS READY!!! *******************");
				System.out.println("**********************************************************");

			}
		}).start();

	}

	public static boolean isStartup() {
		return startup;
	}

	private void log() {
		System.out.println("********* BEANS TO START (IN RIGHT ORDER) *********");
		for (Class<?> bean : startupBeans) {
			System.out.println("Regular bean:" + bean.getName());
		}
		System.out.println("***************************************************");
	}

	public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext) {

	}
}
