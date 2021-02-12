package org.directtruststandards.timplus.cluster.routing;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for Spring Cloud Streams based packet routing.
 * Spring cloud streams connection information can set using spring configuration (properties files, 
 * spring cloud config, etc).
 * @author Greg Meyer
 * @since 1.0
 */
@Configuration
public class RoutingConfiguration implements ApplicationContextAware
{	
	protected static ApplicationContext ctx;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException 
	{
		ctx = applicationContext;
	}
	
	public static ApplicationContext getApplicationContext()
	{
		return ctx;
	}
}
