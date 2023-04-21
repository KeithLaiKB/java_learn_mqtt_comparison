package com.learn.mqtt.comparison.versionone.countingversion.versionone.scenario1.hivemqttclient.publisher;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedListener;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;

/**
 * 
 * 
 * <p>
 * 							description:																				</br>	
 * &emsp;						qos 0																					</br>	
 * &emsp;						if it couldn't connect, still wait though there are something wrong during connection	</br>																							</br>
 *
 *
 * @author laipl
 *
 */
public class TestMain_Hivemqmqttclient_Publisher {


	public static void main(String[] args) {

        int statusUpdate		=0;
        int statusUpdateMaxTimes=50;

        
        final InetSocketAddress LOCALHOST_EPHEMERAL1 = new InetSocketAddress("192.168.239.137",1883);																		// set broker address	
        // 所以初步认为 MqttAsyncClient 是包含了 MqttRxClient 
        Mqtt5SimpleAuth simpleAuth = Mqtt5SimpleAuth.builder().username("IamPublisherOne").password("123456".getBytes()).build();									// authentication
        Mqtt5Connect connectMessage = Mqtt5Connect.builder().cleanStart(true).simpleAuth(simpleAuth).build();
        Mqtt5AsyncClient client1 = Mqtt5Client.builder().serverAddress(LOCALHOST_EPHEMERAL1).identifier("JavaSample_sender").buildAsync();	// create publisher
        
        CompletableFuture<Mqtt5ConnAck> cplfu_connect_rslt = client1.connect(connectMessage);		// publisher connect
        
        while(client1.getState().isConnected()==false) {
        	//do nothing, just wait for connected
        }
		//System.out.println("connected");
        
    	com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder.Send<CompletableFuture<Mqtt5PublishResult>>  publishBuilder1 = client1.publishWith();
    	com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder.Send.Complete<CompletableFuture<Mqtt5PublishResult>> c1 = publishBuilder1.topic("Resource1");		// topic setting
    	c1.qos(MqttQos.AT_MOST_ONCE);																																		// qos setting

        while(statusUpdate<=statusUpdateMaxTimes-1) {
        	statusUpdate = statusUpdate+1;
        	
        	c1.payload(("Hello World!" + statusUpdate).getBytes());		// set payload
        	c1.send();									// publish

        	try {
        		Thread.sleep(500);
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        }

        client1.disconnect();

    }

}
