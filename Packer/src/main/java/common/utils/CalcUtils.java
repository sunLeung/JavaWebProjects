package common.utils;

import java.util.Calendar;

/**
 * 
 * @Description 计算工具类
 * @author liangyx
 * @date 2013-7-1
 * @version V1.0
 */
public class CalcUtils {
	public static int getDelayHour(int targetHour){
		Calendar c=Calendar.getInstance();
		int h=c.get(Calendar.HOUR_OF_DAY);
		if(h<=targetHour){
			return targetHour-h;
		}else{
			return targetHour+24-h;
		}
	}
}
