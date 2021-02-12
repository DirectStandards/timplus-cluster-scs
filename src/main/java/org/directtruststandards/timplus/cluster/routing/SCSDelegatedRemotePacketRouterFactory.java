package org.directtruststandards.timplus.cluster.routing;


import org.jivesoftware.openfire.RemotePacketRouter;
import org.springframework.context.ApplicationContext;

public class SCSDelegatedRemotePacketRouterFactory implements DelegatedRemotePacketRouterFactory
{
	public SCSDelegatedRemotePacketRouterFactory()
	{
		
	}

	@Override
	public RemotePacketRouter getInstance() 
	{
		final ApplicationContext ctx = RoutingConfiguration.getApplicationContext();
		
		if (ctx == null)
			throw new IllegalStateException("Application context cannot be null");

		final RemotePacketRouter router = ctx.getBean("SCSClusteredPacketRouter", RemotePacketRouter.class);
		
		return router;
	}
}
