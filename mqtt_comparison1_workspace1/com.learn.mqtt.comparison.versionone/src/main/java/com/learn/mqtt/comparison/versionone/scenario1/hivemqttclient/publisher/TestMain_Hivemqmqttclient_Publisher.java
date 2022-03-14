package com.learn.mqtt.comparison.versionone.scenario1.hivemqttclient.publisher;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;



import com.hivemq.client.internal.mqtt.MqttRxClient;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublishBuilder;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilderBase;
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

        //------------------------------- create client --------------------------------------
        final InetSocketAddress LOCALHOST_EPHEMERAL1 = new InetSocketAddress("127.0.0.1",1883);
        // 所以初步认为 MqttAsyncClient 是包含了 MqttRxClient 
        Mqtt5SimpleAuth simpleAuth = Mqtt5SimpleAuth.builder().username("IamPublisherOne").password("123456".getBytes()).build();
        Mqtt5Connect connectMessage = Mqtt5Connect.builder().cleanStart(true).simpleAuth(simpleAuth).build();
        Mqtt5AsyncClient client1 = Mqtt5Client.builder().serverAddress(LOCALHOST_EPHEMERAL1).identifier("JavaSample_sender").simpleAuth(simpleAuth).buildAsync();
        //------------------------------- client connect --------------------------------------
        CompletableFuture<Mqtt5ConnAck> cplfu_connect_rslt = client1.connect(connectMessage);
		//------------------------------- client publish --------------------------------------
    	com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder.Send<CompletableFuture<Mqtt5PublishResult>>  publishBuilder1 = client1.publishWith();
    	com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder.Send.Complete<CompletableFuture<Mqtt5PublishResult>> c1 = publishBuilder1.topic("Resource1");
    	c1.qos(MqttQos.AT_MOST_ONCE);
    	//
        while(statusUpdate<=statusUpdateMaxTimes-1) {
        	statusUpdate = statusUpdate+1;
        	String str_content_tmp = "Hello World!" + statusUpdate;

        	c1.payload(str_content_tmp.getBytes());
        	c1.send();

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
