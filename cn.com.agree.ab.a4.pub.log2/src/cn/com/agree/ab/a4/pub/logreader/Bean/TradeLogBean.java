package cn.com.agree.ab.a4.pub.logreader.Bean;

public class TradeLogBean
{
	private String date;
	
	private String thread;
	
	private String oid;
	
	private String tradeName;
	
	private String flag;
	
	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}
	
	private String message;

	public String getDate()
	{
		return date;
	}

	public void setDate(String date)
	{
		this.date = date;
	}

	public String getThread()
	{
		return thread;
	}

	public void setThread(String thread)
	{
		this.thread = thread;
	}

	public String getOid()
	{
		return oid;
	}

	public void setOid(String oid)
	{
		this.oid = oid;
	}

	public String getTradeName()
	{
		return tradeName;
	}

	public void setTradeName(String tradeName)
	{
		this.tradeName = tradeName;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}
	
	
}
