package org.directtruststandards.timplus.cluster.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jivesoftware.openfire.cluster.NodeID;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.messaging.Message;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;


public class SCSClusteredPacketRouter_routePacketTest extends SpringBaseTest
{
	@Test
	public void testRoutePackets_assertRouted() throws Exception
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
		
		final ClusteredPacket packet = new ClusteredPacketMessageConverter(mapper).fromStreamMessage(outputMessage);
		
		assertEquals(msg.toXML(), packet.getPacket());
		assertEquals(NodeID.getInstance(new byte[] {0,0,0,0}), NodeID.getInstance(packet.getDestNode()));
		assertEquals(recipJid.getNode(), packet.getRecipLocal());
		assertEquals(recipJid.getDomain(), packet.getRecipDomain());
		assertEquals(recipJid.getResource(), packet.getRecipResource());
		
		final Packet xmppPacket = SCSClusteredPacketRouter.xmlToXMPPPacket(packet.getPacket());
		
		assertTrue(xmppPacket instanceof org.xmpp.packet.Message);
	}
	
	@Test
	public void testRoutePresense_assertRouted() throws Exception
	{
		final JID recipJid = new JID("gm2552", "test.com", null);
		final JID fromJid = new JID("ah4626", "test.com", null);
		
		
		final Presence pres = new Presence();
		pres.setType(Presence.Type.subscribed);
		pres.setFrom(fromJid);
		pres.setTo(recipJid);
		
		
		router.routePacket(new byte[] {0,0,0,0}, recipJid, pres);
		
		OutputDestination outputDestination = context.getBean(OutputDestination.class);
		
		Message<?> outputMessage = outputDestination.receive(2000);
		
		assertNotNull(outputMessage);
		
		final ClusteredPacket packet = new ClusteredPacketMessageConverter(mapper).fromStreamMessage(outputMessage);
		
		assertEquals(pres.toXML(), packet.getPacket());
		assertEquals(NodeID.getInstance(new byte[] {0,0,0,0}), NodeID.getInstance(packet.getDestNode()));
		assertEquals(recipJid.getNode(), packet.getRecipLocal());
		assertEquals(recipJid.getDomain(), packet.getRecipDomain());
		assertEquals(recipJid.getResource(), packet.getRecipResource());
		
		final Packet xmppPacket = SCSClusteredPacketRouter.xmlToXMPPPacket(packet.getPacket());
		
		assertTrue(xmppPacket instanceof org.xmpp.packet.Presence);
	}	
	
	@Test
	public void testRouteIQ_assertRouted() throws Exception
	{
		final JID recipJid = new JID("gm2552", "test.com", null);
		final JID fromJid = new JID("ah4626", "test.com", null);
		
		
		final IQ iq = new IQ();
		iq.setType(IQ.Type.set);
		iq.setFrom(fromJid);
		iq.setTo(recipJid);
		
		
		router.routePacket(new byte[] {0,0,0,0}, recipJid, iq);
		
		OutputDestination outputDestination = context.getBean(OutputDestination.class);
		
		Message<?> outputMessage = outputDestination.receive(2000);
		
		assertNotNull(outputMessage);
		
		final ClusteredPacket packet = new ClusteredPacketMessageConverter(mapper).fromStreamMessage(outputMessage);
		
		assertEquals(iq.toXML(), packet.getPacket());
		assertEquals(NodeID.getInstance(new byte[] {0,0,0,0}), NodeID.getInstance(packet.getDestNode()));
		assertEquals(recipJid.getNode(), packet.getRecipLocal());
		assertEquals(recipJid.getDomain(), packet.getRecipDomain());
		assertEquals(recipJid.getResource(), packet.getRecipResource());
		
		final Packet xmppPacket = SCSClusteredPacketRouter.xmlToXMPPPacket(packet.getPacket());
		
		assertTrue(xmppPacket instanceof org.xmpp.packet.IQ);
	}		
}
