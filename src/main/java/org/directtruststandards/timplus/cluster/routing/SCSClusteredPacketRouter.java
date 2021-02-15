package org.directtruststandards.timplus.cluster.routing;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jivesoftware.openfire.PacketRouteStatus;
import org.jivesoftware.openfire.RemotePacketRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

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
	protected static final String CLUSTER_NODE_HEADER = "clusterNode";
	
	protected final EmitterProcessor<Message<ClusteredPacket>> processor = EmitterProcessor.create();

	@Override
	public PacketRouteStatus routePacket(byte[] nodeID, JID receipient, Packet packet) 
	{
		final ClusteredPacket clustPacket = new ClusteredPacket();
		clustPacket.setDestNode(nodeID);
		clustPacket.setPacket(packet);
		clustPacket.setReceipient(receipient);
		
		
        final Message<ClusteredPacket> msg = MessageBuilder.withPayload(clustPacket)
                .setHeader(CLUSTER_NODE_HEADER, nodeID)
                .build();
        
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
    public Supplier<Flux<Message<ClusteredPacket>>> remotePacketSupplier() 
	{
		return () -> this.processor;
    }
	
	/**
	 * Spring Cloud consumer for receiving packets on the remotePacketConsumer-in-0 binding.
	 * @return A Consumer object that checks if the message is destined for this "cluster node"
	 * and hands off the message to the internal routing table.
	 */
	@Bean 
	public Consumer<Message<ClusteredPacket>> remotePacketConsumer() 
	{
		return msg ->
		{
			// make sure this message is destined to us
			final byte[] destNode = msg.getHeaders().get(CLUSTER_NODE_HEADER, byte[].class);
			
			if (destNode == null || XMPPServer.getInstance().getNodeID().equals(destNode))
			{
				final ClusteredPacket packet = msg.getPayload();
				
				XMPPServer.getInstance().getRoutingTable().routePacket(packet.getReceipient(), packet.getPacket(), false);
	
			}
		};
	}
}
