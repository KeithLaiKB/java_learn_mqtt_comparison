package com.learn.mqtt.comparison.versionone.scenario2.hivemqttclient.publisher;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;


import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientBuilder;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedListener;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
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

	private boolean connected = false;
	
    public TestMain_Hivemqmqttclient_Publisher() {
    	
    }
	public static void main(String[] args) {
		new TestMain_Hivemqmqttclient_Publisher().run();

    }
	private void run() {

        int statusUpdate		=0;
        int statusUpdateMaxTimes=50;


    	String serverCaCrt_file					="s_cacert.crt";
    	String serverCaCrt_file_dir				="/mycerts/hivemqttclient/sender/other_own";
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
		
		//MqttSslInitializer a;

        
        final InetSocketAddress LOCALHOST_EPHEMERAL1 = new InetSocketAddress("192.168.239.137",8883);																		// set broker address	
        // 所以初步认为 MqttAsyncClient 是包含了 MqttRxClient 
        Mqtt5SimpleAuth simpleAuth = Mqtt5SimpleAuth.builder().username("IamPublisherOne").password("123456".getBytes()).build();									// authentication
        
        //-------------set TLS/SSL-------
        MqttClientBuilder mqttClientBuilder = MqttClient.builder().serverAddress(LOCALHOST_EPHEMERAL1).identifier("JavaSample_sender");
        mqttClientBuilder.sslConfig(MqttClientSslConfig.builder()
                      .keyManagerFactory(null)
                      .trustManagerFactory(tmf)		//.hostnameVerifier(hostnameVerifier)
                      .protocols(Arrays.asList("TLSv1.3"))		//这里指定TLSv1.3
                      .hostnameVerifier(new HostnameVerifier() {
                          public boolean verify(String s, SSLSession sslSession) {
                              return true;
                          }})
                      .build());

        Mqtt5RxClient client1_rx = mqttClientBuilder.useMqttVersion5().simpleAuth(simpleAuth).addConnectedListener(new MyConnectedListener()).buildRx();
        Mqtt5AsyncClient client1 = client1_rx.toAsync();
        // -------------------------------------------------------------------------
        
        Mqtt5Connect connectMessage = Mqtt5Connect.builder().cleanStart(true).simpleAuth(simpleAuth).build();
        CompletableFuture<Mqtt5ConnAck> cplfu_connect_rslt = client1.connect(connectMessage);												// publisher connect
        while(connected==false) {
        	try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
    	com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder.Send<CompletableFuture<Mqtt5PublishResult>>  publishBuilder1 = client1.publishWith();
    	com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder.Send.Complete<CompletableFuture<Mqtt5PublishResult>> c1 = publishBuilder1.topic("Resource1");		// topic setting
    	c1.qos(MqttQos.AT_MOST_ONCE);																																		// qos setting

        while(statusUpdate<=statusUpdateMaxTimes-1) {
        	statusUpdate = statusUpdate+1;
        	String str_content_tmp = "Hello World!" + statusUpdate;

        	c1.payload(str_content_tmp.getBytes());		// set payload
        	c1.send();									// publish
        	//System.out.println("kk");
        	try {
        		Thread.sleep(500);
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
        }

        client1.disconnect();
    }
	// 我们可以通过关闭掉 docker,来调试
	private class MyConnectedListener implements MqttClientConnectedListener {

		@Override
		public void onConnected(MqttClientConnectedContext context) {
			// TODO Auto-generated method stub
			//System.out.println(context.toString());			//可以发现 只有成功connect 才会显示这个, connect 不成功是不显示的(例如 docker关了)
			connected=true;
		}
		
	}
}
