package org.directtruststandards.timplus.cluster.routing;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.messaging.Message;
import org.xmpp.packet.JID;

public class SCSClusteredPacketRouter_routePacketTest extends SpringBaseTest
{
	@Test
	public void testRoutePacket_assertRouted() throws Exception
	{
		final JID recipJid = new JID("gm2552", "test.com", null);
		final JID fromJid = new JID("ah4626", "test.com", null);
		
		
		final org.xmpp.packet.Message msg = new org.xmpp.packet.Message();
		msg.setBody("Hello");
		msg.setFrom(fromJid);
		msg.setTo(recipJid);
		
		
		router.routePacket(new byte[] {0,0,0,0}, recipJid, msg);
		
		OutputDestination outputDestination = context.getBean(OutputDestination.class);
		
		Message<?> outputMessage = outputDestination.receive(2000);
		
		assertNotNull(outputMessage);
		
	}
}
