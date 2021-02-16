package org.directtruststandards.timplus.cluster.routing;

import java.io.StringReader;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.dom4j.Element;
import org.dom4j.io.XMPPPacketReader;
import org.jivesoftware.openfire.PacketRouteStatus;
import org.jivesoftware.openfire.RemotePacketRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.net.MXParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

/**
 * RemotePacketRouter implementation that sends and receives packets to and from other TIM+ servers
 * in that make up the "cluster."  This implementation uses the SpringCloud Stream functional
 * programming paradigm.
 */
@Component("SCSClusteredPacketRouter")
@ConditionalOnProperty(value="timplus.server.enableClustering", havingValue = "true", matchIfMissing=false)
public class SCSClusteredPacketRouter implements RemotePacketRouter
{	
	protected final EmitterProcessor<Message<?>> processor = EmitterProcessor.create();

	@Autowired
	protected ObjectMapper mapper;
	
	@Override
	public PacketRouteStatus routePacket(byte[] nodeID, JID recipient, Packet packet) 
	{
		final ClusteredPacket clustPacket = new ClusteredPacket();
		clustPacket.setDestNode(nodeID);
		clustPacket.setPacket(packet.toXML());
		
		if (recipient != null)
		{
			clustPacket.setRecipLocal(recipient.getNode());
			clustPacket.setRecipDomain(recipient.getDomain());
			clustPacket.setRecipResource(recipient.getResource());
		}
		
		
        final Message<?> msg = new ClusteredPacketMessageConverter(mapper).toStreamMessage(clustPacket);
        
        processor.onNext(msg);
        
        return PacketRouteStatus.ROUTED_TO_CLUSTER;
	}

	@Override
	public void broadcastPacket(org.xmpp.packet.Message packet) 
	{
		
	}

	/**
	 * Spring Cloud supplier for sending packets on to the remotePacketSupplier-out-0 binding.
	 * @return A Flux of Clustered packet objects that will be delivered to all servers in the "cluster"
	 */
	@Bean 
    public Supplier<Flux<Message<?>>> remotePacketSupplier() 
	{
		return () -> this.processor;
    }
	
	/**
	 * Spring Cloud consumer for receiving packets on the remotePacketConsumer-in-0 binding.
	 * @return A Consumer object that checks if the message is destined for this "cluster node"
	 * and hands off the message to the internal routing table.
	 */
	@Bean 
	public Consumer<Message<?>> remotePacketConsumer() 
	{
		return msg ->
		{
			final ClusteredPacket clusteredPacket = new ClusteredPacketMessageConverter(mapper).fromStreamMessage(msg);
			
			// make sure this message is destined to us
			final byte[] destNode = clusteredPacket.getDestNode();
			
			if (destNode == null || (XMPPServer.getInstance() != null && XMPPServer.getInstance().getNodeID().equals(destNode)))
			{
				Packet thePacket = xmlToXMPPPacket(clusteredPacket.getPacket());
				
				try
				{

					final JID recipJid = new JID(clusteredPacket.getRecipLocal(), clusteredPacket.getRecipDomain(), clusteredPacket.getRecipResource());
					
					XMPPServer.getInstance().getRoutingTable().routePacket(recipJid, thePacket, false);
				}
				catch (Exception e)
				{
					throw new MessagingException("Failed to process remote packet.", e);
				}
			}
		};
	}
	
	protected static Packet xmlToXMPPPacket(String XML)
	{
		try
		{
			Packet retVal = null;
			
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance(MXParser.class.getName(), null);
	        factory.setNamespaceAware(true);
	        final XMPPPacketReader parser = new XMPPPacketReader();
	        parser.setXPPFactory( factory );
	        
	        
			
			Element doc = parser.read(new StringReader(XML)).getRootElement();
			
			String tag = doc.getName();
			
			switch(tag)
			{
				case "message":
				{
					retVal = new org.xmpp.packet.Message(doc, true);
					break;
				}
				case "presence":
				{
					
					retVal = new Presence(doc, true);
					break;	
				}
				case "iq":
				{
					retVal = new IQ(doc, true);
					break;	
				}
					
			}
			
			return retVal;
		}
		catch (Exception e)
		{
			throw new MessagingException("Failed to convert cluster packet from internal structure");
		}
	}
}
