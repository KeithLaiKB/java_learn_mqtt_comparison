package com.learn.mqtt.comparison.versionone.scenario1.hivemqttclient.publisher;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

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
 * 注意 这里的 connected 不一定正确的, 所以我们最终是不采用这种版本的, 我使用的是counting version的版本,
 * 		对于判断是否connect, 在正确的版本中 我用了getState来保证 
 * 			client1.getState().isConnected()
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
        int statusUpdateMaxTimes=1000;

        
        final InetSocketAddress LOCALHOST_EPHEMERAL1 = new InetSocketAddress("192.168.239.137",1883);																		// set broker address	
        // 所以初步认为 MqttAsyncClient 是包含了 MqttRxClient 
        Mqtt5SimpleAuth simpleAuth = Mqtt5SimpleAuth.builder().username("IamPublisherOne").password("123456".getBytes()).build();									// authentication
        Mqtt5Connect connectMessage = Mqtt5Connect.builder().cleanStart(true).simpleAuth(simpleAuth).build();
        Mqtt5AsyncClient client1 = Mqtt5Client.builder().serverAddress(LOCALHOST_EPHEMERAL1).identifier("JavaSample_sender").addConnectedListener(new MyConnectedListener()).buildAsync();	// create publisher
        
        CompletableFuture<Mqtt5ConnAck> cplfu_connect_rslt = client1.connect(connectMessage);		// publisher connect
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
    	c1.qos(MqttQos.AT_LEAST_ONCE);																																		// qos setting

        while(statusUpdate<=statusUpdateMaxTimes-1) {
        	statusUpdate = statusUpdate+1;
        	String str_content_tmp = "Hello World!" + statusUpdate;

        	c1.payload(str_content_tmp.getBytes());		// set payload
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
