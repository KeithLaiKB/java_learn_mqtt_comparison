package com.learn.mqtt.comparison.versionone;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.learn.mqtt.comparison.versionone.myutil.MyTimeUtil;
import com.learn.mqtt.comparison.versionone.scenario1.hivemqttclient.publisher.TestMain_Hivemqmqttclient_Publisher;
import com.learn.mqtt.comparison.versionone.scenario1.hivemqttclient.subscriber.TestMain_Hivemqmqttclient_Subscriber;
import com.learn.mqtt.comparison.versionone.scenario1.pahomqtt.publisher.TestMain_Pahomqtt_Publisher;
import com.learn.mqtt.comparison.versionone.scenario1.pahomqtt.subscriber.TestMain_Pahomqtt_Subscriber;

class test1a {

	long startTime = 0;
	long endTime = 0;
	
	static Map timeRs_testPahomqtt			= null;
	static Map timeRs_testHivemqttclient	= null;
	
	static Integer loopClientTmp = 0;
	
	private static final Logger LOGGER = LogManager.getLogger(test1a.class);
	
	@BeforeAll
	static void preparation() {

	}
	
	@AfterAll
	static void toEnd() {
		System.out.println("pahomqtt total starttime:"+timeRs_testPahomqtt.get("startTime")+"/endtime:"+timeRs_testPahomqtt.get("endTime")+"/usedtime:"+timeRs_testPahomqtt.get("usedTime")+"/usedtime_sec:"+timeRs_testPahomqtt.get("usedTime_sec"));
		System.out.println("hivemqtt total starttime:"+timeRs_testHivemqttclient.get("startTime")+"/endtime:"+timeRs_testHivemqttclient.get("endTime")+"/usedtime:"+timeRs_testHivemqttclient.get("usedTime")+"/usedtime_sec:"+timeRs_testHivemqttclient.get("usedTime_sec"));
	}
	
	@BeforeEach
	void beforesomething() {
		startTime			=System.nanoTime(); 
		LOGGER.error("This is an error1 level log message!");
		LOGGER.info("This is an infoooerror level log message!");
	}
	
	
	@AfterEach
	void aftersomething() {
		//Map timeRs1	= MyTimeUtil.countUsedTime(startTime);	
		//System.out.println("total starttime:"+timeRs1.get("startTime")+"/endtime:"+timeRs1.get("endTime")+"/usedtime:"+timeRs1.get("usedTime")+"/usedtime_sec:"+timeRs1.get("usedTime_sec"));
	}
	
	
	@Test
	void testPahomqtt_1Server1Client() {
		int clientNum=3;
		testPahomqtt_specifyClientNum(clientNum);
	}
	
	@Test
	void testHivemqtt_1Server1Client() {
		int clientNum=3;
		testHivemqtt_specifyClientNum(clientNum);
	}
	
	void testPahomqtt_specifyClientNum(int clientNum) {
		ArrayList<Thread> arr_thdClient = new ArrayList<Thread>();
		
		//start server
		Runnable server1 = new Runnable() {
			public void run() {
				long serverStartTime			=System.nanoTime();   		//nanoTime 会比 currentTimeMillis更加精确
				TestMain_Pahomqtt_Publisher.main(null);
				Map timeRs1 = MyTimeUtil.countUsedTime(serverStartTime);
				System.out.println("pahomqtt publisher starttime:"+timeRs1.get("startTime")+"/endtime:"+timeRs1.get("endTime")+"/usedtime:"+timeRs1.get("usedTime")+"/usedtime_sec:"+timeRs1.get("usedTime_sec"));
			}
		};
		Thread t1 = new Thread(server1);
		t1.start();
		
		//start client
		for(int i=0; i<=clientNum-1; i++) {	
			String seqTmp = String.valueOf(i);
			Runnable client1 = new Runnable() {
				public void run() {
					long clientStartTime			=System.nanoTime();   		//nanoTime 会比 currentTimeMillis更加精确
					
					//paho 不能用同一个clientId 去连接一个topic
					//TestMain_Pahomqtt_Subscriber.main(null);
					//所以每一个clientId需要独立的给予一个clientId
					String[] inputArrTmp = {"JavaSample_recver"+seqTmp};
					TestMain_Pahomqtt_Subscriber.main(inputArrTmp);
					
					Map timeRs1 = MyTimeUtil.countUsedTime(clientStartTime);
					System.out.println("pahomqtt subscriber"+seqTmp+" starttime:"+timeRs1.get("startTime")+"/endtime:"+timeRs1.get("endTime")+"/usedtime:"+timeRs1.get("usedTime")+"/usedtime_sec:"+timeRs1.get("usedTime_sec"));
					LOGGER.info("pahomqtt subscriber"+seqTmp+" starttime:"+timeRs1.get("startTime")+"/endtime:"+timeRs1.get("endTime")+"/usedtime:"+timeRs1.get("usedTime")+"/usedtime_sec:"+timeRs1.get("usedTime_sec"));	
				}
			};		
			
			Thread t2 = new Thread(client1);
			arr_thdClient.add(t2);
			t2.start();
		}
		
		//keep going
		boolean someClientRunning = true;
		while(t1.isAlive()==true || someClientRunning==true) {
			int loopTmp = 0;
			for(; loopTmp<=clientNum-1; loopTmp++) {
				if(arr_thdClient.get(loopTmp).isAlive()) {
					break;
				}
			}
			if(loopTmp>clientNum-1) {
				someClientRunning = false;	//all clients finished
			}
			//do nothing
		}

		//count time
		timeRs_testPahomqtt	= MyTimeUtil.countUsedTime(startTime);	
	}
	
	void testHivemqtt_specifyClientNum(int clientNum) {
		ArrayList<Thread> arr_thdClient = new ArrayList<Thread>();
		
		//start server
		Runnable server1 = new Runnable() {
			public void run() {
				long serverStartTime			=System.nanoTime();   		//nanoTime 会比 currentTimeMillis更加精确
				TestMain_Hivemqmqttclient_Publisher.main(null);
				Map timeRs1 = MyTimeUtil.countUsedTime(serverStartTime);
				System.out.println("hivemqtt publisher starttime:"+timeRs1.get("startTime")+"/endtime:"+timeRs1.get("endTime")+"/usedtime:"+timeRs1.get("usedTime")+"/usedtime_sec:"+timeRs1.get("usedTime_sec"));
			}
		};
		Thread t1 = new Thread(server1);
		t1.start();
		
		//start client
		for(int i=0; i<=clientNum-1; i++) {	
			String seqTmp = String.valueOf(i);
			Runnable client1 = new Runnable() {
				public void run() {
					long clientStartTime			=System.nanoTime();   		//nanoTime 会比 currentTimeMillis更加精确
					
					//paho 不能用同一个clientId 去连接一个topic
					//TestMain_Hivemqmqttclient_Subscriber.main(null);
					//所以每一个clientId需要独立的给予一个clientId
					String[] inputArrTmp = {"JavaSample_recver"+seqTmp};	
					TestMain_Hivemqmqttclient_Subscriber.main(inputArrTmp);
					
					Map timeRs1 = MyTimeUtil.countUsedTime(clientStartTime);
					System.out.println("hivemqtt subscriber"+seqTmp+" starttime:"+timeRs1.get("startTime")+"/endtime:"+timeRs1.get("endTime")+"/usedtime:"+timeRs1.get("usedTime")+"/usedtime_sec:"+timeRs1.get("usedTime_sec"));
					LOGGER.info("hivemqtt subscriber"+seqTmp+" starttime:"+timeRs1.get("startTime")+"/endtime:"+timeRs1.get("endTime")+"/usedtime:"+timeRs1.get("usedTime")+"/usedtime_sec:"+timeRs1.get("usedTime_sec"));	
				}
			};		
			
			Thread t2 = new Thread(client1);
			arr_thdClient.add(t2);
			t2.start();
		}
		
		//keep going
		boolean someClientRunning = true;
		while(t1.isAlive()==true || someClientRunning==true) {
			int loopTmp = 0;
			for(; loopTmp<=clientNum-1; loopTmp++) {
				if(arr_thdClient.get(loopTmp).isAlive()) {
					break;
				}
			}
			if(loopTmp>clientNum-1) {
				someClientRunning = false;	//all clients finished
			}
			//do nothing
		}

		//count time
		timeRs_testHivemqttclient	= MyTimeUtil.countUsedTime(startTime);	
	}
}
