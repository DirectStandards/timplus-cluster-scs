package org.directtruststandards.timplus.cluster.routing;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jivesoftware.openfire.PacketException;
import org.jivesoftware.openfire.RemotePacketRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.cluster.NodeID;
import org.jivesoftware.openfire.spi.RoutingTableImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
public abstract class SpringBaseTest 
{
	@Autowired
	protected ApplicationContext context;
	
	protected RemotePacketRouter router;
	
	@BeforeAll
	public static void beforeEach()
	{
		XMPPServer server = mock(XMPPServer.class);
		
		when(server.getNodeID()).thenReturn(NodeID.getInstance(new byte[] {0,0,0,0}));
		
		final TestRoutingTableImp rTable = new TestRoutingTableImp();
		
		when(server.getRoutingTable()).thenReturn(rTable);
		
		MockedStatic<XMPPServer> theMock = Mockito.mockStatic(XMPPServer.class);
		
		theMock.when(XMPPServer::getInstance).thenReturn(server);
		
		
	}
	
	@BeforeEach
	public void setUp()
	{
		/*
		 * The router could just as easily be setup via @Autoconfiguration
		 * since we have that capability with this test class, but prefer to test
		 * the class factory with each unit test instead as it will emulator more closely
		 * what will be done in the server.
		 */
		final SCSDelegatedRemotePacketRouterFactory factory = new SCSDelegatedRemotePacketRouterFactory();
		router = factory.getInstance();
		
		assertNotNull(router);

		final TestRoutingTableImp impl = (TestRoutingTableImp)XMPPServer.getInstance().getRoutingTable();
		impl.packet = null;
		impl.recipJID = null;
	}

	public static class TestRoutingTableImp extends RoutingTableImpl
	{
		public Packet packet;
		
		public JID recipJID;
		
		@Override
	    public void routePacket(JID jid, Packet packet, boolean fromServer) throws PacketException
	    {
			this.recipJID = null;
	    	this.packet = packet;
	    }
	}
	
}
