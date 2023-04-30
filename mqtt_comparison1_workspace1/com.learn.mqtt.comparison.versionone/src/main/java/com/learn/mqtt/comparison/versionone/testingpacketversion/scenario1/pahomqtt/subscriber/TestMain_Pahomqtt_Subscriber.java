package com.learn.mqtt.comparison.versionone.testingpacketversion.scenario1.pahomqtt.subscriber;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.mqttv5.client.IMqttToken;

import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

public class TestMain_Pahomqtt_Subscriber {
	private int expectedNumberOfMessages 			= 100;
	private volatile int numberOfMessages			= 0;

    private static final Logger LOGGER = LogManager.getLogger(TestMain_Pahomqtt_Subscriber.class);
    
    public TestMain_Pahomqtt_Subscriber() {
    	
    }

	public static void main(String[] args) {
		new TestMain_Pahomqtt_Subscriber().run();
    }
	
	private void run() {

        //final Logger LOGGER = LoggerFactory.getLogger(TestMain_Pahomqtt_Subscriber.class);

        try {
        	//MqttAsyncClient sampleClient = new MqttAsyncClient("tcp://192.168.239.137:1883", this.clientId, new MemoryPersistence());
        	MqttClient client1 = new MqttClient("tcp://192.168.50.178:1883", "JavaSample_recver", new MemoryPersistence());
        	//MqttAsyncClient client1 = new MqttAsyncClient("tcp://192.168.239.137:1883", this.clientId, new MemoryPersistence());
        	//MqttClient client1 = new MqttClient("tcp://138.229.113.84:1883", this.clientId, new MemoryPersistence());

            MqttConnectionOptions connOpts = new MqttConnectionOptions();
            
            
            connOpts.setUserName("IamPublisherOne");		// authentication
            connOpts.setPassword("123456".getBytes());		// authentication
            
            connOpts.setCleanStart(true);

            client1.setCallback(new MyMqttCallback());
            
            client1.connect(connOpts);																// connect
            //client1.connect(connOpts, null, null).waitForCompletion(-1);								// connect
            
            client1.subscribe("Resource1",0);						// subscribe
            //client1.subscribe("Resource1",1);						// subscribe
            
            while(numberOfMessages < expectedNumberOfMessages) {
    			//Thread.sleep(200);
            }

            client1.disconnect();
            client1.close();
            //System.exit(0);
        } catch(MqttException me) {
            me.printStackTrace();
        } 
	}
	
	private class MyMqttCallback implements MqttCallback{

		@Override
		public void disconnected(MqttDisconnectResponse disconnectResponse) {
			LOGGER.info("mqtt disconnected:"+disconnectResponse.toString());
		}

		@Override
		public void mqttErrorOccurred(MqttException exception) {
			LOGGER.info("mqtt error occurred");
			
		}

		@Override
		public void deliveryComplete(IMqttToken token) {
			LOGGER.info("mqtt delivery complete");
		}

		@Override
		public void connectComplete(boolean reconnect, String serverURI) {

			LOGGER.info("mqtt connect complete");
		}

		@Override
		public void authPacketArrived(int reasonCode, MqttProperties properties) {
			LOGGER.info("mqtt auth Packet Arrived");
		}

		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			System.out.println(new String(message.getPayload()));
			numberOfMessages = numberOfMessages +1;
			//LOGGER.info("message Arrived:\t"+ new String(message.getPayload()));
		}
	}
}
