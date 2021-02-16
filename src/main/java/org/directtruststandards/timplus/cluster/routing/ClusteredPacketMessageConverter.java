package org.directtruststandards.timplus.cluster.routing;

import static org.springframework.messaging.support.MessageBuilder.createMessage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


import org.apache.commons.io.IOUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClusteredPacketMessageConverter 
{
	protected static final String CLUSTER_NODE_HEADER = "clusterNode";
	
	protected ObjectMapper mapper;
	
	public ClusteredPacketMessageConverter(ObjectMapper mapper)
	{
		this.mapper = mapper;
	}
	
	public Message<?> toStreamMessage(ClusteredPacket packet)
	{
		return toStreamMessage(packet, null);
	}
	
	public Message<?> toStreamMessage(ClusteredPacket msg, Map<String, Object> hdrs)
	{
	    final Map<String, Object> headerMap = new HashMap<>();

	    byte[] out = null;
	    
    	if (hdrs != null)
    		hdrs.forEach((key, value) -> headerMap.put(key, value));

	    try
	    {
	    	out = mapper.writeValueAsBytes(msg);
	    }
	    catch (Exception e)
	    {
	    	throw new org.springframework.messaging.MessagingException("Failed to convert clustered packet to internal structure", e);
	    }
	    
	    final MessageHeaders headers = new MessageHeaders(headerMap);
		
	    return createMessage(out, headers);
	}
	
	@SuppressWarnings("deprecation")
	public ClusteredPacket fromStreamMessage(Message<?> msg)
	{
		final Object payload = msg.getPayload();
		
		if (!(payload instanceof String) && !(payload instanceof byte[]))
			return null;
		
		final InputStream inStream = (payload instanceof String)
				? IOUtils.toInputStream(String.class.cast(payload), Charset.defaultCharset())
				: new ByteArrayInputStream(byte[].class.cast(payload));
			
		try
		{
			return  mapper.readValue(inStream, ClusteredPacket.class);
		}
		catch (Exception e)
		{
			throw new org.springframework.messaging.MessagingException("Failed to convert clustered packet from internal structure", e);
		}
		finally
		{
			IOUtils.closeQuietly(inStream);
		}
	}	
}
