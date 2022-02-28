package com.learn.mqtt.comparison.versionone.myutil;

import java.util.LinkedHashMap;
import java.util.Map;

public class MyTimeUtil {
	/*
	public static double countUsedTimeToShow_nano(long startTime) {
		double usedTimeSec = MyTimeUtil.countUsedTime_nano(startTime);

		
		return usedTimeSec;
	}
	public static double countUsedTimeToShow_Sec(long startTime) {
		double usedTimeSec = MyTimeUtil.countUsedTime_nano(startTime);
		return usedTimeSec;
	}

	
	
	public static double countUsedTime_Sec(long startTime) {
		double usedTime = MyTimeUtil.countUsedTime_nano(startTime);
		double usedTimeSec = usedTime/1000000000.0;
		return usedTimeSec;
	}
	*/
	public static Map countUsedTime(long startTime) {
        //
		long endTime			=System.nanoTime();   		//nanoTime 会比 currentTimeMillis更加精确
		double usedTime_Nano	= endTime - startTime;
		double usedTime_Sec		= usedTime_Nano/1000000000.0;

		//System.out.println("start time:"+startTime+"_endtime:"+endTime);
		
		Map<String,Object> myResult1 = new LinkedHashMap<String,Object>();
		myResult1.put("startTime", startTime);
		myResult1.put("endTime", endTime);
		myResult1.put("usedTime", endTime);
		myResult1.put("usedTime_sec", usedTime_Sec);
		return myResult1;
	}
}
