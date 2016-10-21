package cn.com.agree.ab.a4.pub.logreader.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import cn.com.agree.ab.a4.pub.logreader.Bean.TradeLogBean;
import cn.com.agree.ab.a4.pub.logreader.Bean.TradeLogSegment;
import cn.com.agree.ab.a4.pub.logreader.tool.DateUtil;
import cn.com.agree.ab.a4.pub.logreader.tool.MessageUtils;

/**
 * 客户端与服务端日志合并的实现
 * @author Administrator
 */
public class ClientSerMerge {

	/**
	 * 其中用到的标识
	 */
	private static final String invoRequest = "InvokeRequest";
	private static final String issResponse = "IssueResponse";
	private static final String invoResponse = "InvokeResponse";
	private static final String issRequest = "IssueRequest";

	/**
	 * 标记日志的归属
	 */
	private static final String cFlag = "Terminal-client";
	private static final String sFlag = "Terminal-server";

	/**
	 * 客户端交易日志的分割片段
	 */
	private static List<TradeLogSegment> cLogList = new LinkedList<TradeLogSegment>();

	/**
	 * 服务端交易日志的分割片段
	 */
	private static List<TradeLogSegment> sLogList = new LinkedList<TradeLogSegment>();
	/**
	 * 得到的混合了c端与s端的交易片段
	 */
	private static List<TradeLogSegment> CSLogList = null;

	/**
	 * 记录下c端日志中的所有mac地址
	 */
	private static Set<String> oidSet = new HashSet<String>();

	/**
	 * 辅助oidSet的记录
	 */
	private static int csOid = 0;

	/**
	 * 标记正在分割的日志文件属于c端还是s端
	 */
	private static int cs = 0;

	/**
	 * 合并交易的整个方法
	 * 
	 * @param clientPath
	 *            传入的日志路径
	 * @param serverPath
	 *            传入的日志路径，两者没有区别
	 * @return 处理完成的交易日志
	 */
	public static List<TradeLogSegment> clientServerMerger(String clientPath,
			String serverPath)
	{
		csOid = 0;
		if (cLogList.size() != 0 || sLogList.size() != 0 || oidSet.size() != 0)
		{
			cLogList.clear();
			sLogList.clear();
			oidSet.clear();
		}

		if (clientPath.equals(serverPath))
		{
			showMessage("两个文件相同");
			return null;
		}

		ToSegment(clientPath, oidSet);
		ToSegment(serverPath, oidSet);

		// cLogList = ToSegment(clientPath, invoRequest, issResponse, cFlag,
		// oidSet);
		// sLogList = ToSegment(serverPath, invoResponse, issRequest, sFlag,
		// null);

		if (!cLogList.isEmpty())
		{
			sLogList = SelOid(sLogList, oidSet);
		}

		if (cLogList.isEmpty() && sLogList.isEmpty())
		{
			CSLogList = null;
		}
		CSLogList = CSMerger(cLogList, sLogList);

		return CSLogList;
	}

	/**
	 * 分割好的c端日志片段与s端日志片段合并起来
	 */
	private static List<TradeLogSegment> CSMerger(List<TradeLogSegment> cList,
			List<TradeLogSegment> sList)
	{
		// TODO Auto-generated method stub

		List<TradeLogSegment> list = new LinkedList<TradeLogSegment>();
		List<TradeLogSegment> tradeSegments = new LinkedList<TradeLogSegment>();
		List<TradeLogBean> listBean = new LinkedList<TradeLogBean>();

		TradeLogSegment seg = new TradeLogSegment();
		TradeLogSegment segment = new TradeLogSegment();
		TradeLogBean bean = new TradeLogBean();

		int snum = 0;

		if (cList.isEmpty() && !sList.isEmpty())
		{
			list = sList;
		} else
		{
			for (int i = 0; i < cList.size(); i++)
			{
				seg = cList.get(i);
				list.add(seg);

				while (snum < sList.size())
				{
					if (seg.getTradeName().equals(
							sList.get(snum).getTradeName()))
					{
						list.add(sList.get(snum));
						snum++;
						break;
					} else
					{
						snum++;
					}
				}
			}
		}

		// 将拼好的交易片段以交易为单位分割开

		String tradeName = list.get(0).getTradeName();
		segment.setOid(list.get(0).getOid());
		segment.setTime(list.get(0).getTime());
		segment.setTradeName(list.get(0).getTradeName());

		String flag = "";

		for (int i = 0; i < list.size(); i++)
		{
			seg = list.get(i);
			flag = seg.getFlag();

			listBean = seg.getLogBeanList();

			for (int j = 0; j < listBean.size(); j++)
			{
				bean = listBean.get(j);
				bean.setFlag(flag);
				if (!tradeName.equals(bean.getTradeName()))
				{
					tradeSegments.add(segment);

					segment = new TradeLogSegment();
					segment.setOid(bean.getOid());
					segment.setTime(bean.getDate());
					segment.setTradeName(bean.getTradeName());

					tradeName = bean.getTradeName();
				}

				segment.addLogBean(bean);
			}

		}

		tradeSegments.add(segment);

		return tradeSegments;
	}

	/**
	 * 筛选出s端中与c端对应的日志片段
	 */
	private static List<TradeLogSegment> SelOid(List<TradeLogSegment> sLogList,
			Set<String> oidSet)
	{
		// TODO Auto-generated method stub

		List<TradeLogSegment> list = new LinkedList<TradeLogSegment>();

		for (int i = 0; i < sLogList.size(); i++)
		{
			for (String s : oidSet)
			{
				if (s.equals(sLogList.get(i).getOid()))
				{
					list.add(sLogList.get(i));
				}
			}

		}

		return list;
	}

	/**
	 * 分割日志片段，对c端s端路径有区别
	 */
	@SuppressWarnings("resource")
	public static List<TradeLogSegment> ToSegment(String file, String request,
			String response, String flag, Set<String> oidSet)
			throws IOException
	{

		File cFile = new File(file);

		InputStreamReader cisr = new InputStreamReader(new FileInputStream(
				cFile), "utf-8");
		BufferedReader cbr = new BufferedReader(cisr);

		String cLog = null;
		TradeLogBean tradeLog = new TradeLogBean();
		StringBuffer message = new StringBuffer();
		TradeLogSegment tradeSegment = new TradeLogSegment();

		List<TradeLogSegment> cLogList = new LinkedList<TradeLogSegment>();

		int jumpflag = 0;

		try
		{

			while ((cLog = cbr.readLine()) != null)
			{
				if ("".equals(cLog.trim()))
				{
					continue;
				}
				//
				String[] space;

				if (cLog.charAt(0) >= '0' && cLog.charAt(0) <= '9')
				{

					space = cLog.split(" ");

					if (space.length < 8)
					{
						return null;
					}

					if (message.length() != 0)
					{
						tradeLog.setMessage(message.toString());

						tradeSegment.addLogBean(tradeLog);
						message = new StringBuffer();
					}

					if (jumpflag == 1)
					{
						tradeSegment.setOid(tradeLog.getOid());
						tradeSegment.setTradeName(tradeLog.getTradeName());
						tradeSegment.setTime(tradeLog.getDate());
						cLogList.add(tradeSegment);

						tradeSegment = new TradeLogSegment();

						jumpflag = 0;
					}

					if (cLog.contains(request) || cLog.contains(response))
					{
						jumpflag = 1;
					}

					tradeLog = new TradeLogBean();

					tradeLog.setFlag(flag);

					String ss = "";
					ss = ss + space[2] + "/" + DateUtil.monthToInt(space[1])
							+ "/" + space[0] + " " + space[3] + " ";

					tradeLog.setDate(ss);
					tradeLog.setThread(space[4]);
					tradeLog.setOid(space[5]);
					tradeLog.setTradeName(space[6]);
					message.append(space[7]);

					if (oidSet != null)
					{
						oidSet.add(space[5]);
					}
				} else
				{
					message.append(cLog + "\n");
				}
			}

			tradeLog.setMessage(message.toString());

			tradeSegment.setOid(tradeLog.getOid());
			tradeSegment.setTradeName(tradeLog.getTradeName());
			tradeSegment.setTime(tradeLog.getDate());
			tradeSegment.addLogBean(tradeLog);
			cLogList.add(tradeSegment);

		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally
		{
			cisr.close();
		}

		return cLogList;
	}

	/**
	 * 分割日志片段，对c端s端路径没有要求
	 * 
	 * @param fileName
	 *            要分割的交易日志路径
	 * @param oidSet
	 *            记录c端中的mac地址集合
	 * @throws IOException
	 */
	public static void ToSegment(String fileName, Set<String> oidSet)
	{
		if (oidSet.size() != 0)
		{
			csOid = 1;
		}

		if (cs != 0)
		{
			cs = 0;
		}

		File cFile = new File(fileName);
		if (cFile.length() <= 0)
		{
			return;
		}

		InputStreamReader cisr = null;
		BufferedReader cbr = null;
		try
		{
			cisr = new InputStreamReader(new FileInputStream(cFile), "utf-8");
		} catch (UnsupportedEncodingException e1)
		{
			showMessage("不支持该编码类型的文件");
			e1.printStackTrace();
			return;
		} catch (FileNotFoundException e1)
		{
			showMessage("找不到" + cFile.getAbsolutePath() + "文件");
			e1.printStackTrace();
			return;
		}
		cbr = new BufferedReader(cisr);

		String logLine = null;
		TradeLogBean tradeLog = new TradeLogBean();
		StringBuffer message = new StringBuffer();
		TradeLogSegment tradeSegment = new TradeLogSegment();

		int jumpflag = 0;

		try
		{
			while ((logLine = cbr.readLine()) != null)
			{
				if ("".equals(logLine.trim()))
				{
					continue;
				}

				String[] space;

				if ((logLine.charAt(0) >= '0' && logLine.charAt(0) <= '9'))
				{
					space = logLine.split(" ");

					if (space.length < 8)
					{
						MessageUtils.showError(Display.getCurrent().getShells()[0], "日志文件格式错误");
						break;
					}

					if (message.length() != 0)
					{
						tradeLog.setMessage(message.toString());

						tradeSegment.addLogBean(tradeLog);
						message = new StringBuffer();
					}

					if (jumpflag == 1)
					{
						tradeSegment.setOid(tradeLog.getOid());
						tradeSegment.setTradeName(tradeLog.getTradeName());
						tradeSegment.setTime(tradeLog.getDate());
						tradeSegment.setFlag(cFlag);
						cLogList.add(tradeSegment);

						tradeSegment = new TradeLogSegment();

						jumpflag = 0;
					}
					if (jumpflag == 2)
					{
						tradeSegment.setOid(tradeLog.getOid());
						tradeSegment.setTradeName(tradeLog.getTradeName());
						tradeSegment.setTime(tradeLog.getDate());
						tradeSegment.setFlag(sFlag);
						sLogList.add(tradeSegment);

						tradeSegment = new TradeLogSegment();

						jumpflag = 0;
					}

					// 检测到为c端日志，记录，并且分割标识
					if (logLine.contains(invoRequest)
							|| logLine.contains(issResponse))
					{
						jumpflag = 1;
						cs = 1;
					}
					// 检测到为s端日志，记录，并且分割标识
					if (logLine.contains(invoResponse)
							|| logLine.contains(issRequest))
					{
						jumpflag = 2;
						cs = 2;
					}

					tradeLog = new TradeLogBean();

					String ss = "";
					ss = ss + space[2] + "/" + DateUtil.monthToInt(space[1])
							+ "/" + space[0] + " " + space[3] + " ";

					tradeLog.setDate(ss);
					tradeLog.setThread(space[4]);
					tradeLog.setOid(space[5]);
					tradeLog.setTradeName(space[6]);

					for (int i = 7; i < space.length; i++)
					{
						message.append(space[i] + " ");
					}

					if (csOid == 0)
					{
						oidSet.add(space[5]);
					}
				} else
				{
					message.append("\n" + logLine);
				}
			}

			tradeLog.setMessage(message.toString());

			tradeSegment.setOid(tradeLog.getOid());
			tradeSegment.setTradeName(tradeLog.getTradeName());
			tradeSegment.setTime(tradeLog.getDate());
			tradeSegment.addLogBean(tradeLog);

			if (cs == 1 || (cs == 0 && message.length() != 0))
			{
				tradeSegment.setFlag(cFlag);
				cLogList.add(tradeSegment);
			} else if (cs == 2)
			{
				if (csOid == 0)
				{
					oidSet.clear();
				}
				tradeSegment.setFlag(sFlag);
				sLogList.add(tradeSegment);
			}

		} catch (IOException e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				if(cisr != null)
				{
					cisr.close();
				}
				if(cbr != null)
				{
					cbr.close();
				}
			} catch (IOException e)
			{
				
			}
		}
	}

	/**
	 * 弹窗提示
	 * 
	 * @param s
	 *            即要显示的信息
	 */
	protected static void showMessage(String s)
	{
		MessageBox dialog = new MessageBox(Display.getCurrent().getShells()[0],
				SWT.OK | SWT.ICON_ERROR);
		dialog.setMessage(s);
		dialog.open();
	}
}
