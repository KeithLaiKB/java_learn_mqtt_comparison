package com.learn.mqtt.comparison.versionone.scenario1.hivemqttclient.subscriber;

import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient.Mqtt5SubscribeAndCallbackBuilder;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.learn.mqtt.comparison.versionone.scenario1.pahomqtt.subscriber.TestMain_Pahomqtt_Subscriber;

public class TestMain_Hivemqmqttclient_Subscriber {
    
	private int expectedNumberOfMessages = 30;
	private int numberOfMessages = 0;
	private String clientId     			= "JavaSample_revcevier";
	
    public TestMain_Hivemqmqttclient_Subscriber() {
    	
    }
    public TestMain_Hivemqmqttclient_Subscriber(String clientId) {
    	this.clientId = clientId;
    }
	public static void main(String[] args) {
		if (args.length!=0) {
			new TestMain_Hivemqmqttclient_Subscriber(args[0]).run();
		}
		else {
			new TestMain_Hivemqmqttclient_Subscriber().run();
		}
    }

	
	private void run() {
		
        String brokerAddress  	= "127.0.0.1";				// broker address
        int brokerPort			= 1883;						// broker port

        String myuserName	= "IamPublisherOne";
        String mypwd		= "123456";
        
        //------------------------------- create client --------------------------------------
        final InetSocketAddress LOCALHOST_EPHEMERAL1 = new InetSocketAddress(brokerAddress,brokerPort);;

        Mqtt5SimpleAuth simpleAuth = Mqtt5SimpleAuth.builder().username(myuserName).password(mypwd.getBytes()).build();
        Mqtt5Connect connectMessage = Mqtt5Connect.builder().cleanStart(true).simpleAuth(simpleAuth).build();
        Mqtt5AsyncClient client1 = Mqtt5Client.builder().serverAddress(LOCALHOST_EPHEMERAL1).identifier(this.clientId).simpleAuth(simpleAuth).buildAsync();
        //------------------------------- client connect --------------------------------------
        CompletableFuture<Mqtt5ConnAck> cplfu_connect_rslt = client1.connect(connectMessage);
        
        //-------------------------------  to subscribe  --------------------------------------
        Mqtt5AsyncClient.Mqtt5SubscribeAndCallbackBuilder.Start subscribeBuilder1 = client1.subscribeWith();
        Mqtt5SubscribeAndCallbackBuilder.Start.Complete c1 = subscribeBuilder1.topicFilter("Resource1");
        c1.qos(MqttQos.AT_MOST_ONCE);
        c1.callback(publish -> {
        			numberOfMessages = numberOfMessages +1;
        			System.out.println(new String(publish.getPayloadAsBytes())); 
        		}); 	// set callback
        c1.send();		//subscribe callback and something 
        
        
        while(numberOfMessages < expectedNumberOfMessages) {
        	try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        client1.disconnect();
        //System.exit(0);				//if using clean false, disconnect couldn't finished the program
		
		
	}
}
