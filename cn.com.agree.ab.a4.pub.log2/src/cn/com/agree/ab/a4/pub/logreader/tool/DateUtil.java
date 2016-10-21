package cn.com.agree.ab.a4.pub.logreader.tool;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

	public static int compareDate(String one, String another)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		try
		{
			Date oneDate = sdf.parse(one);
			
			Date anotherDate = sdf.parse(another);
			
			int r = oneDate.compareTo(anotherDate);
			
			return r;
		} catch (ParseException e)
		{
			e.printStackTrace();
		}
		
		return -1;
	}

	public static int monthToInt(String s)
	{
		if(s != null)
		{
			s = s.trim();
		}
		if("一月".equals(s))
		{
			return 1;
		}
		
		if("二月".equals(s))
		{
			return 2;
		}
		
		if("三月".equals(s))
		{
			return 3;
		}
		
		if("四月".equals(s))
		{
			return 4;
		}
		
		if("五月".equals(s))
		{
			return 5;
		}
		
		if("六月".equals(s))
		{
			return 6;
		}
		if("七月".equals(s))
		{
			return 7;
		}
		if("八月".equals(s))
		{
			return 8;
		}
		if("九月".equals(s))
		{
			return 9;
		}
		if("十月".equals(s))
		{
			return 10;
		}
		if("十一月".equals(s))
		{
			return 11;
		}
		if("十二月".equals(s))
		{
			return 12;
		}
		return -1;
	}
	
}
