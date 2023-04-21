package com.learn.mqtt.comparison.versionone.countingversion.versionone.scenario1.pahomqtt.publisher;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
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
 * */
public class TestMain_Pahomqtt_Publisher {

	public static void main(String[] args) {

		int statusUpdateMaxTimes = 50;
		int statusUpdate = 0;

        try {
        	//MqttAsyncClient sampleClient = new MqttAsyncClient("tcp://192.168.239.137:1883", "JavaSample_sender", new MemoryPersistence());
        	MqttClient client1 = new MqttClient("tcp://192.168.239.137:1883", "JavaSample_sender", new MemoryPersistence());
        	//MqttAsyncClient client1 = new MqttAsyncClient("tcp://192.168.239.137:1883", "JavaSample_sender", new MemoryPersistence());
        	//MqttClient client1 = new MqttClient("tcp://138.229.113.84:1883", "JavaSample_sender", new MemoryPersistence());
        	
            MqttConnectionOptions connOpts = new MqttConnectionOptions();
            
            connOpts.setCleanStart(true);

            
            connOpts.setUserName("IamPublisherOne");								// authentication
            connOpts.setPassword("123456".getBytes());								// authentication

            // connect to broker
            client1.connect(connOpts);												//如果是MqttClient 贼需要这个
            //client1.connect(connOpts, null, null).waitForCompletion(-1); 			//如果是MqttAsyncClient 贼需要这个
            
            MqttMessage message_tmp=null;
            while(statusUpdate<=statusUpdateMaxTimes-1) {
            	statusUpdate= statusUpdate+1;
            	
            	message_tmp = new MqttMessage(("Hello World!"+statusUpdate).toString().getBytes());
            	message_tmp.setQos(1);
            	message_tmp.setRetained(false);
       
            	client1.publish("Resource1", message_tmp);
                
                Thread.sleep(500);
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
