package com.learn.mqtt.comparison.versionone.testingpacketversion.scenario1.hivemqttclient.subscriber;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedListener;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient.Mqtt5SubscribeAndCallbackBuilder;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

public class TestMain_Hivemqmqttclient_Subscriber {
    
	private int expectedNumberOfMessages 	= 100;
	private volatile int numberOfMessages 			= 0;

	
    public TestMain_Hivemqmqttclient_Subscriber() {
    	
    }

	public static void main(String[] args) {
		new TestMain_Hivemqmqttclient_Subscriber().run();
    }

	
	private void run() {  

        final InetSocketAddress LOCALHOST_EPHEMERAL1 = new InetSocketAddress("192.168.50.178",1883);																	// set broker address
        
        Mqtt5SimpleAuth simpleAuth = Mqtt5SimpleAuth.builder().username("IamPublisherOne").password("123456".getBytes()).build();								// authentication
        Mqtt5Connect connectMessage = Mqtt5Connect.builder().cleanStart(true).simpleAuth(simpleAuth).build();
        Mqtt5AsyncClient client1 = Mqtt5Client.builder().serverAddress(LOCALHOST_EPHEMERAL1).identifier("JavaSample_recver").buildAsync();		// create publisher
        
        CompletableFuture<Mqtt5ConnAck> cplfu_connect_rslt = client1.connect(connectMessage);												// subscriber connect
        
        while(client1.getState().isConnected()==false) {
        	//do nothing, just wait for connected
        }
        //System.out.println("connected");
        
        Mqtt5AsyncClient.Mqtt5SubscribeAndCallbackBuilder.Start subscribeBuilder1 = client1.subscribeWith();
        Mqtt5SubscribeAndCallbackBuilder.Start.Complete c1 = subscribeBuilder1.topicFilter("Resource1");			// topic setting

    	c1.qos(MqttQos.AT_MOST_ONCE);																																		// qos0 setting
    	//c1.qos(MqttQos.AT_LEAST_ONCE);																																		// qos1 setting
        
        c1.callback(publish -> {
        			numberOfMessages = numberOfMessages +1;
        			System.out.println(new String(publish.getPayloadAsBytes())); 
        		}); 	// set callback
        c1.send();		//subscribe callback and something 
        
        
        while(numberOfMessages < expectedNumberOfMessages) {
        	/*
        	try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			*/
        }

        client1.disconnect();
        //System.exit(0);				//if using clean false, disconnect couldn't finished the program

	}

}
