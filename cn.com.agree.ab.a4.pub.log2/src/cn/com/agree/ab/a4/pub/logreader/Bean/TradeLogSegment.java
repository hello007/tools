package cn.com.agree.ab.a4.pub.logreader.Bean;

import java.util.LinkedList;
import java.util.List;

public class TradeLogSegment {
	
	private List <TradeLogBean> logBeanList = null;
	
	private String time;
	
	private String oid;
	
	private String tradeName;
	
	private String flag;
	
	

	public TradeLogSegment()
	{
		if(logBeanList == null)
		{
			logBeanList = new LinkedList<TradeLogBean>();
		}
	}
	
	public void addLogBean(TradeLogBean trade)
	{
		if(logBeanList == null)
		{
			logBeanList = new LinkedList<TradeLogBean>();
		}
		
		logBeanList.add(trade);
	}
	
	public void addLogBeanList(TradeLogSegment s)
	{
		if(logBeanList == null)
		{
			logBeanList = new LinkedList<TradeLogBean>();
		}
		
		logBeanList.addAll(s.getLogBeanList());
	}
	
	public List<TradeLogBean> getLogBeanList() {
		return logBeanList;
	}

	public void setLogBeanList(List<TradeLogBean> tradeSegment) {
		this.logBeanList = tradeSegment;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public String getTradeName() {
		return tradeName;
	}

	public void setTradeName(String tradeName) {
		this.tradeName = tradeName;
	}
	
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
	
	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}
}
