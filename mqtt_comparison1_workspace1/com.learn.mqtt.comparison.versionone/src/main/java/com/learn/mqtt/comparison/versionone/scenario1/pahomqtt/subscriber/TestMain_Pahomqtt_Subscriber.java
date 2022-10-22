package com.learn.mqtt.comparison.versionone.scenario1.pahomqtt.subscriber;

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
	private int expectedNumberOfMessages 	= 30;
	private int numberOfMessages			= 0;
	private String clientId     			= "JavaSample_recver";
	
    private static final Logger LOGGER = LogManager.getLogger(TestMain_Pahomqtt_Subscriber.class);
    
    public TestMain_Pahomqtt_Subscriber() {
    	
    }
    public TestMain_Pahomqtt_Subscriber(String clientId) {
    	this.clientId = clientId;
    }
	public static void main(String[] args) {
		if (args.length!=0) {
			new TestMain_Pahomqtt_Subscriber(args[0]).run();
		}
		else {
			new TestMain_Pahomqtt_Subscriber().run();
		}
    }
	
	private void run() {

        //final Logger LOGGER = LoggerFactory.getLogger(TestMain_Pahomqtt_Subscriber.class);

        try {
            MqttClient sampleClient = new MqttClient("tcp://localhost:1883", this.clientId, new MemoryPersistence());		// create mqtt client
            //MqttClient sampleClient = new MqttClient(broker, clientId);

            MqttConnectionOptions connOpts = new MqttConnectionOptions();
            
            
            connOpts.setUserName("IamPublisherOne");		// authentication
            connOpts.setPassword("123456".getBytes());		// authentication
            
            connOpts.setCleanStart(true);

            sampleClient.setCallback(new MqttCallback() {

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
			});
            
            sampleClient.connect(connOpts);								// connect
            
            sampleClient.subscribe("Resource1",0);						// subscribe
            while(numberOfMessages < expectedNumberOfMessages) {
    			Thread.sleep(200);
            }

            sampleClient.disconnect();
            sampleClient.close();
            //System.exit(0);
        } catch(MqttException me) {
            me.printStackTrace();
        } catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
