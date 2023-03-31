package com.learn.mqtt.comparison.versionone.scenario2.pahomqtt.subscriber;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttCallback;
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

    	String serverCaCrt_file					="s_cacert.crt";
    	String serverCaCrt_file_dir				="/mycerts/pahomqtt/receiver/other_own";
    	String serverCaCrt_file_loc = null;
        
		String myusr_path = System.getProperty("user.dir");
		serverCaCrt_file_loc 							= 	myusr_path	+ serverCaCrt_file_dir		+"/" + 	serverCaCrt_file;
        
		
		//s_cacert.crt ->FileInputStream->BufferedInputStream-> ca Certificate
        FileInputStream fis= null;
        CertificateFactory cf = null;
        Certificate ca=null;
        InputStream caInput =null;
		try {
			cf = CertificateFactory.getInstance("X.509");
			fis = new FileInputStream(serverCaCrt_file_loc);
			caInput = new BufferedInputStream(fis);

			ca = cf.generateCertificate(caInput);
		} catch (FileNotFoundException | CertificateException e1) {
			e1.printStackTrace();
		} 
		finally {
			try {
				caInput.close();	//关闭 s_cacert.crt 的stream  
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Create a KeyStore containing our trusted CAs
		// KeyStore set ca Certificate -> TrustManagerFactory
		String keyStoreType = KeyStore.getDefaultType();
		KeyStore keyStore=null;
		TrustManagerFactory tmf = null;
		try {
			// Create a KeyStore containing our trusted CAs
			keyStoreType = KeyStore.getDefaultType();
			keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(null, null);
			keyStore.setCertificateEntry("ca", ca);

			String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
			tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
			tmf.init(keyStore);
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e3) {
			e3.printStackTrace();
		} 
		

		// finally, create SSL socket factory
		SSLContext context=null;
		SSLSocketFactory mysocketFactory=null;
		try {
			//context = SSLContext.getInstance("SSL");
			context = SSLContext.getInstance("TLSv1.3");
			context.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());
		} catch (NoSuchAlgorithmException | KeyManagementException e2) {
			e2.printStackTrace();
		} 
		
		
		mysocketFactory = context.getSocketFactory();
		
		try {
        	MqttAsyncClient client1 = new MqttAsyncClient("ssl://192.168.239.137:8883", this.clientId, new MemoryPersistence());

        	MqttConnectionOptions connOpts = new MqttConnectionOptions();
            
            connOpts.setCleanStart(true);
            
            connOpts.setUserName("IamPublisherOne");								// authentication
            connOpts.setPassword("123456".getBytes());								// authentication

            //-------------set TLS/SSL-------
            connOpts.setSocketFactory(mysocketFactory);
            connOpts.setHttpsHostnameVerificationEnabled(false);
            // -------------------------------------------------------------------------

            client1.setCallback(new MqttCallback() {

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
            
            // connect to broker
            client1.connect(connOpts, null, null).waitForCompletion(-1); 	//如果是MqttAsyncClient 贼需要这个
            //client1.connect(connOpts, null, null).waitForCompletion(5000); 	//如果是MqttAsyncClient 贼需要这个
            
            client1.subscribe("Resource1",0);						// subscribe
            while(numberOfMessages < expectedNumberOfMessages) {
    			Thread.sleep(200);
            }
            
            client1.disconnect();
            client1.close();
            //System.exit(0);
        } catch(MqttException me) {
            me.printStackTrace();
        } catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	
	}
}
