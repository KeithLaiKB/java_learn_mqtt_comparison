package com.learn.mqtt.comparison.versionone.scenario2.pahomqtt.publisher;

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

import org.eclipse.paho.mqttv5.client.DisconnectedBufferOptions;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.client.persist.MqttDefaultFilePersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
/**
 * 
 * @author laipl
 *
 *	我想要做到
 *	step1(数据):	publisher 	发送 123
 *	step2(数据):	subscriber 	接受123
 *
 *	step3(操作):	关闭 subscriber 
 *
 *	step4(数据):	publisher 	发送45678
 *  
 *	step8(操作):	然后 启动 subscriber
 *	step9(数据):	然后 subscriber 能接受 
 *								1 2 3
 *								      和
 *								4 5 6 7 8
 *
 *  publisher(online)	-------------> 	mosquitto(online)  -------------->	subscriber(online)
 *  publisher(online) 	----123------> 	mosquitto(online)  -------------->	subscriber(online)
 *  publisher(online) 	-------------> 	mosquitto(online)  -------------->	subscriber(online)
 *                     						123
 *  publisher(online) 	-------------> 	mosquitto(online)  ------123----->	subscriber(online)
 *  publisher(online) 	-------------> 	mosquitto(online)  ------123----->	subscriber(online)
 *  																			1 2 3
 *  
 *  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  +++++++++++++++++++++++++			turn off subscriber		+++++++++++++++++++++++++++++++
 *  ++++++	要设置 subscriber 的 setCleantStart(false) 和 interval, 	使得 subscriber 重启 后   broker     仍然记得 这个subscriber 						+++++++
 *  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  publisher(online) 	-------------> 	mosquitto(online)  -------------->	subscriber(offline)
 *  publisher(online) 	----45678----> 	mosquitto(online)  -------------->	subscriber(offline)
 *  publisher(online) 	-------------> 	mosquitto(online)  -------------->	subscriber(offline)
 *  									   4 5 6 7 8
 *  
 *  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  ++++++++++++++++++++++++++ 			turn on subscriber			+++++++++++++++++++++++++++
 *  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  publisher(online)	-------------> 	mosquitto(online)  --4-5-6-7-8--->	subscriber(online)
 *  									   4 5 6 7 8
 *  publisher(online)	-------------> 	mosquitto(online)  -------------->	subscriber(online)
 *  							     										4 5 6 7 8							
 *
 * 
 * 因为broker需要记得 subscriber 在这里只需要设置 subscriber 
 * 	connOpts.setCleanStart(false);
 * 	connOpts.setSessionExpiryInterval(500L);		//500是个时间 你可以随便设置
 * 
 * 注意 你还需要设置subscriber 的qos不能为0
 * 因为 subscriber的 qos0 是无法reconnect的时候 或者  重新启动这个subscribe(从connect到 subscribe)继续 获得信息
 * 
 * subscriber关闭后	 重启 		就可以直接获得 45678
 *
 *
 * 如果你不关闭 broker, 那么就 不需要 在mosquitto.config 中 设置 persistence true
 */
/*
 * mqtt 不需要像 coap那样的resource 所以 循环可以直接放在主函数
 * 
 * 
 * 
 * 
 *   MqttClientMqtt 被换成 AsyncClient
 *   因此
 *   sampleClient.connect(connOpts); 也要换成 sampleClient.connect(connOpts, null, null).waitForCompletion(-1);
 * */
public class TestMain_Pahomqtt_Publisher {

	public static void main(String[] args) {

        int qos             = 0;
        
		int statusUpdateMaxTimes = 50;
		int statusUpdate = 0;

        
        
        
    	String serverCaCrt_file					="s_cacert.crt";
    	String serverCaCrt_file_dir				="/mycerts/pahomqtt/sender/other_own";
    	String serverCaCrt_file_loc = null;
        

        
        
        
		String myusr_path = System.getProperty("user.dir");
		serverCaCrt_file_loc 							= 	myusr_path	+ serverCaCrt_file_dir		+"/" + 	serverCaCrt_file;
        //////////////////// file->FileInputStream->BufferedInputStream->X509Certificate //////////////////////////////////////
        // ref: https://gist.github.com/erickok/7692592
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
		String keyStoreType = KeyStore.getDefaultType();
		KeyStore keyStore=null;
		TrustManagerFactory tmf = null;
		try {
			// Create a KeyStore containing our trusted CAs
			keyStoreType = KeyStore.getDefaultType();
			keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(null, null);
			keyStore.setCertificateEntry("ca", ca);

			// Create a TrustManager that trusts the CAs in our KeyStore
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
        	//MqttClient sampleClient = new MqttClient(brokerUri, clientId, new MemoryPersistence());
        	//MqttClient sampleClient = new MqttClient("tcp://127.0.0.1:1883", "JavaSample_sender", new MemoryPersistence());
        	MqttAsyncClient sampleClient = new MqttAsyncClient("ssl://127.0.0.1:8883", "JavaSample_sender", new MemoryPersistence());
        	//MqttAsyncClient sampleClient = new MqttAsyncClient(broker, clientId, new MemoryPersistence());
        	//
        	// -----------------------set connection options-------------------------
        	MqttConnectionOptions connOpts = new MqttConnectionOptions();
            
            connOpts.setCleanStart(true);

            
            connOpts.setUserName("IamPublisherOne");								// authentication
            connOpts.setPassword("123456".getBytes());								// authentication

            //-------------set TLS/SSL-------
            connOpts.setSocketFactory(mysocketFactory);
            connOpts.setHttpsHostnameVerificationEnabled(false);
            //
            // -------------------------------------------------------------------------
            // -----------------------set  disconnected buffer options------------------
            //
            DisconnectedBufferOptions disconnect_bfOpt_1=new DisconnectedBufferOptions();
            // 初始化disconnectedBufferOptions
            disconnect_bfOpt_1.setBufferSize(100);				//离线后最多缓存100条
            disconnect_bfOpt_1.setPersistBuffer(true);  		//一直持续留存
            disconnect_bfOpt_1.setDeleteOldestMessages(false);	//不删除旧消息
            disconnect_bfOpt_1.setBufferEnabled(true);			//断开连接后进行缓存
            sampleClient.setBufferOpts(disconnect_bfOpt_1);
            // -------------------------------------------------------------------------
            // connect to broker
            //sampleClient.connect(connOpts);									//如果是MqttClient 贼需要这个
            // waitForCompletion(-1) -> waitForCompletion(timeout)-> waitForResponse(timeout)
            // -> if (timeout <= 0) {responseLock.wait();}
            // -> Object类 的  wait(){wait(0)} 
            // -> wait()
            // -> public final native void wait(long timeout) throws InterruptedException;
            // 也就是说 相当于 wait(0) 也就是不等待
            // 例如 broker 没打开 连接不到 就当做连接失败
            // connect to broker
            //sampleClient.connect(connOpts);
            sampleClient.connect(connOpts, null, null).waitForCompletion(-1); 	//如果是MqttAsyncClient 贼需要这个
            //
            //
        
            MqttMessage message_tmp=null;
            while(statusUpdate<=statusUpdateMaxTimes-1) {
            	statusUpdate= statusUpdate+1;
            	message_tmp = new MqttMessage(new String("Hello World!"+statusUpdate).toString().getBytes());
            	message_tmp.setQos(qos);
            	message_tmp.setRetained(false);
       
                sampleClient.publish("Resource1", message_tmp);
                
                Thread.sleep(500);
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
