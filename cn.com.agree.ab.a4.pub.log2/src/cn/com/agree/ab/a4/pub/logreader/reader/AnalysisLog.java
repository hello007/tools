package cn.com.agree.ab.a4.pub.logreader.reader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import cn.com.agree.ab.a4.pub.logreader.Bean.TradeLogBean;
import cn.com.agree.ab.a4.pub.logreader.Bean.TradeLogSegment;
import cn.com.agree.ab.a4.pub.logreader.tool.DateUtil;
import cn.com.agree.ab.a4.pub.logreader.tool.MessageUtils;

public class AnalysisLog extends Shell
{
	/**
	 * 选择的client，server端日志路径
	 */
	private String serverPath = "";
	private String clientPath = "";

	// 要导出的文件路径
	private String newPath;

	private String startTime = "";
	private String endTime = "";

	/**
	 * 记录完整的日志合并结果
	 */
	private List<TradeLogSegment> logList = null;

	/**
	 * 记录搜索的结果
	 */
	private List<TradeLogSegment> seekList = new LinkedList<TradeLogSegment>();

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String args[])
	{
		try
		{
			Display display = Display.getDefault();
			AnalysisLog shell = new AnalysisLog(display);
			shell.open();
			shell.layout();
			while (!shell.isDisposed())
			{
				if (!display.readAndDispatch())
				{
					display.sleep();
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * Create the shell.
	 * 
	 * @param display
	 */
	public AnalysisLog(Display display)
	{
		super(display, SWT.SHELL_TRIM);
		setLayout(new FillLayout(SWT.HORIZONTAL));

		createContents();
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents()
	{
		setText("日志分析工具");
		setSize(800, 600);
		setMinimumSize(400, 300);

		SashForm sashForm = new SashForm(this, SWT.VERTICAL);

		Composite composite = new Composite(sashForm, SWT.NONE);
		Composite console = new Composite(sashForm, SWT.NONE);

		sashForm.setWeights(new int[] { 9, 1 });
		createComposite(composite);
		createConsole(console);

		addListeners();

		clientPath = "F:\\workspace\\Ameba\\ab\\AB_Client\\Basic\\cn.com.agree.ab.a4.client\\ROOT\\log\\flow.log";
		serverPath = "F:\\workspace\\Ameba\\ab\\AB_Server\\Basic\\cn.com.agree.ab.a4.server\\ROOT\\log\\flow.log";

		logList = ClientSerMerge.clientServerMerger(clientPath, serverPath);
		// 初始化数据
		tableComplete(logList);
	}

	Text clientPathText, serverPathText, exportPathText;
	Button clientPathButton, serverPathButton, mergeButton, exportPathButton;

	private void createComposite(Composite composite)
	{
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 7;
		composite.setLayout(gridLayout);

		clientPathText = new Text(composite, SWT.BORDER);
		clientPathText.setEnabled(false);
		clientPathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		clientPathButton = new Button(composite, SWT.NONE);
		clientPathButton.setText("客户端日志");

		serverPathText = new Text(composite, SWT.BORDER);
		serverPathText.setEnabled(false);
		serverPathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		serverPathButton = new Button(composite, SWT.NONE);
		serverPathButton.setText("服务端日志");
		
		mergeButton = new Button(composite, SWT.NONE);
		mergeButton.setText("  合并   ");

		exportPathText = new Text(composite, SWT.BORDER);
		exportPathText.setEnabled(false);
		exportPathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		exportPathButton = new Button(composite, SWT.NONE);
		exportPathButton.setText("导出的日志");

		// 第二行
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		Composite parentComposite = new Composite(composite, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 7;
		gridData.verticalSpan = 1;
		parentComposite.setLayoutData(gridData);
		parentComposite.setLayout(new FillLayout());

		Composite dateComposite = new Composite(parentComposite, SWT.NONE);

		Composite searchComposite = new Composite(parentComposite, SWT.NONE);
		createSearchComposite(dateComposite, searchComposite);

		// table
		Composite tableComposite = new Composite(composite, SWT.BORDER);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 7;
		tableComposite.setLayoutData(gridData);
		tableComposite.setLayout(new FillLayout());
		createTableComposite(tableComposite);

	}

	private void createConsole(Composite console)
	{
		console.setLayout(new FillLayout());
		consoleText = new Text(console, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL
				| SWT.READ_ONLY | SWT.H_SCROLL);
	}

	Text startTimeText, endTimeText, tradePathText, consoleText;
	Button startTimeButton, endTimeButton, searchButton;
	private TableViewer tableViewer;
	private Table table;
	int tableRow = 0;

	private void createTableComposite(Composite tableComposite)
	{
		tableViewer = new TableViewer(tableComposite, SWT.MULTI
				| SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		final TableColumn newColumnTableColumn = new TableColumn(table,
				SWT.CENTER);
		newColumnTableColumn.setWidth(40);
		newColumnTableColumn.setText("行号");

		final TableColumn newColumnTableColumn_1 = new TableColumn(table,
				SWT.NONE);
		newColumnTableColumn_1.setWidth(200);
		newColumnTableColumn_1.setText("时间");

		final TableColumn newColumnTableColumn_2 = new TableColumn(table,
				SWT.NONE);
		newColumnTableColumn_2.setWidth(140);
		newColumnTableColumn_2.setText("终端");

		final TableColumn newColumnTableColumn_3 = new TableColumn(table,
				SWT.NONE);
		newColumnTableColumn_3.setWidth(450);
		newColumnTableColumn_3.setText("交易名");

		final TableColumn newColumnTableColumn_4 = new TableColumn(table,
				SWT.NONE);
		newColumnTableColumn_4.setWidth(850);
		newColumnTableColumn_4.setText("信息");

		// 设置内容器
		tableViewer.setContentProvider(new ContentProvider());
		// 设置标签器
		tableViewer.setLabelProvider(new TableLabelProvider());
		// 把数据集合给tableView
	}

	private void createSearchComposite(Composite dateComposite,
			Composite searchComposite)
	{
		dateComposite.setLayout(new FillLayout());
		Composite startComposite = new Composite(dateComposite, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		startComposite.setLayout(gridLayout);
		startTimeText = new Text(startComposite, SWT.BORDER);
		startTimeText.setEnabled(false);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		startTimeText.setLayoutData(gridData);
		startTimeButton = new Button(startComposite, SWT.NONE);
		startTimeButton.setText("...");

		Composite endComposite = new Composite(dateComposite, SWT.NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		endComposite.setLayout(gridLayout);
		endTimeText = new Text(endComposite, SWT.BORDER);
		endTimeText.setEnabled(false);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		endTimeText.setLayoutData(gridData);
		endTimeButton = new Button(endComposite, SWT.NONE);
		endTimeButton.setText("...");

		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		searchComposite.setLayout(gridLayout);
		tradePathText = new Text(searchComposite, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		tradePathText.setLayoutData(gridData);
		searchButton = new Button(searchComposite, SWT.NONE);
		searchButton.setText("搜索");

	}

	private void addListeners()
	{
		startTimeButton.addMouseListener(mouseListener);
		endTimeButton.addMouseListener(mouseListener);
		clientPathButton.addMouseListener(mouseListener);
		serverPathButton.addMouseListener(mouseListener);
		exportPathButton.addMouseListener(mouseListener);
		mergeButton.addMouseListener(mergeListener);
		searchButton.addMouseListener(mouseListener);
		table.addMouseListener(mouseListener);

		tradePathText.addKeyListener(new KeyListener()
		{
			@Override
			public void keyReleased(KeyEvent e)
			{
				if (e.keyCode == 13)
				{
					searchButton.notifyListeners(SWT.MouseUp, new Event());
				}
			}

			@Override
			public void keyPressed(KeyEvent e)
			{

			}
		});
	}

	MouseListener mergeListener = new MouseAdapter()
	{
		@Override
		public void mouseUp(MouseEvent e)
		{
			if (clientPath != null && serverPath != null)
			{
				logList = ClientSerMerge.clientServerMerger(clientPath,
						serverPath);
				if (logList != null)
				{
					tableComplete(logList);
				} else
				{
					showMessage("日志文件存在问题或内容为空，请注意日志格式是否一致");
				}
			} else
			{
				MessageUtils.showError(AnalysisLog.this, "请检查文件路径后重试！");
			}
		}
	};

	MouseListener mouseListener = new MouseAdapter()
	{
		@Override
		public void mouseUp(final MouseEvent e)
		{

			if (startTimeButton == e.widget || endTimeButton == e.widget)
			{
				final Shell dialog = new Shell(AnalysisLog.this,
						SWT.DIALOG_TRIM);
				dialog.setLayout(new GridLayout(3, false));

				final DateTime calendar = new DateTime(dialog, SWT.CALENDAR
						| SWT.BORDER);

				new Label(dialog, SWT.NONE);
				new Label(dialog, SWT.NONE);
				Button ok = new Button(dialog, SWT.PUSH);
				ok.setText("OK");
				ok.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
						false));
				ok.addSelectionListener(new SelectionAdapter()
				{
					@Override
					public void widgetSelected(SelectionEvent event)
					{

						if (startTimeButton == e.widget)
						{
							startTime = calendar.getYear() + "/"
									+ (calendar.getMonth() + 1) + "/"
									+ calendar.getDay();
							dialog.close();
							startTimeText.setText(startTime);

							if (endTime != ""
									&& DateUtil.compareDate(startTime, endTime) > 0)
							{
								showMessage("时间不合法");
							}
						} else
						{
							endTime = calendar.getYear() + "/"
									+ (calendar.getMonth() + 1) + "/"
									+ calendar.getDay();
							dialog.close();
							endTimeText.setText(endTime);

							if (startTime != ""
									&& DateUtil.compareDate(startTime, endTime) > 0)
							{
								showMessage("时间不合法");
							}
						}
					}
				});
				dialog.setDefaultButton(ok);
				dialog.pack();
				dialog.open();
			} else if (clientPathButton == e.widget
					|| serverPathButton == e.widget)
			{
				FileDialog client = new FileDialog(AnalysisLog.this);
				client.setText("选择目标文件");
				client.setFilterPath("F://");

				if (clientPathButton == e.widget)
				{
					clientPath = client.open();
					if (clientPath != null && clientPath.trim().equals(""))
					{
						String[] ss = clientPath.split("\\\\");
						int i = ss.length;
						String s = clientPath;
						if (i > 4)
						{
							s = ss[0] + "\\" + ss[1] + "\\" + "..." + "\\"
									+ ss[i - 2] + "\\" + ss[i - 1];
						}
						clientPathText.setText(s);
					}
				} else
				{
					serverPath = client.open();
					if (serverPath != null && serverPath.trim().equals(""))
					{
						String[] ss = serverPath.split("\\\\");
						int i = ss.length;
						String s = serverPath;
						if (i > 4)
						{
							s = ss[0] + "\\" + ss[1] + "\\" + "..." + "\\"
									+ ss[i - 2] + "\\" + ss[i - 1];
						}
						serverPathText.setText(s);
					}
				}

			} else if (exportPathButton == e.widget)
			{
				if (logList.size() == 0)
				{
					showMessage("无可导出日志");
				} else
				{
					FileDialog f = new FileDialog(AnalysisLog.this);
					f.setText("选择导出位置");
					String[] filter = { "*.log;*.txt" };
					f.setFilterPath("F://");
					f.setFilterExtensions(filter);
					newPath = f.open();

					if (newPath != null && !newPath.trim().equals(""))
					{
						exportPathText.setText(newPath);
						exportLocal(logList, newPath);
					}
				}
			} else if (searchButton == e.widget)
			{
				List<TradeLogSegment> list = null;
				TradeLogSegment seg = new TradeLogSegment();

				if (seekList.size() != 0)
				{
					seekList.clear();
				}
				seekList.addAll(logList);

				if (startTime != "")
				{
					list = new LinkedList<TradeLogSegment>();
					for (int i = 0; i < seekList.size(); i++)
					{
						seg = seekList.get(i);

						if (DateUtil.compareDate(startTime, seg.getTime()) <= 0)
						{
							list.add(seg);
						}
					}

					seekList.clear();
					seekList.addAll(list);
				}

				if (endTime != "")
				{
					list = new LinkedList<TradeLogSegment>();
					for (int i = 0; i < seekList.size(); i++)
					{
						seg = seekList.get(i);

						if (DateUtil.compareDate(endTime, seg.getTime()) >= 0)
						{
							list.add(seg);
						}
					}

					seekList.clear();
					seekList.addAll(list);
				}

				if (tradePathText.getText() != "")
				{
					String search = tradePathText.getText();
					search = search.replace("\\", "/");
					list = new LinkedList<TradeLogSegment>();
					for (int i = 0; i < seekList.size(); i++)
					{
						seg = seekList.get(i);

						if (seg.getTradeName().toLowerCase()
								.contains(search.toLowerCase()))
						{
							list.add(seg);
						}
					}
					seekList.clear();
					seekList.addAll(list);
				}

				tableComplete(seekList);
			} else if (table == e.widget)
			{
				if (e.button == 1)
				{
					if (table.getItemCount() > 0)
					{

						int ii = table.getSelectionIndex();
						if (ii < 0 || ii >= table.getItemCount())
						{
							return;
						}
						StringBuffer sb = new StringBuffer();
						TableItem item = table.getItem(ii);

						sb.append(item.getText(1) + "   " + item.getText(2)
								+ "   " + item.getText(4));

						consoleText.setText(sb.toString());
					}
				}
			}
		}

	};

	/**
	 * 弹窗提示
	 * 
	 * @param s
	 *            即要显示的信息
	 */
	protected void showMessage(String message)
	{
		MessageUtils.showError(AnalysisLog.this, message);
	}

	/**
	 * 
	 * @param list
	 *            要导出的交易集合
	 * @param path
	 *            要导出的路径
	 * @throws IOException
	 */
	public void exportLocal(List<TradeLogSegment> list, String path)
	{
		List<TradeLogBean> logList = new LinkedList<TradeLogBean>();
		TradeLogBean tBean = new TradeLogBean();

		File f = new File(path);
		if (!f.exists())
		{
			try
			{
				f.createNewFile();
			} catch (IOException e)
			{
				showMessage("新建文件失败，请手动创建");
				return;
			}
		}
		TradeLogSegment segment = new TradeLogSegment();
		Iterator<TradeLogSegment> iterator = list.iterator();

		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		BufferedWriter writer = null;
		try
		{
			fos = new FileOutputStream(f);
			osw = new OutputStreamWriter(fos,"UTF-8");
			writer = new BufferedWriter(osw);
			while (iterator.hasNext())
			{
				segment = (TradeLogSegment) iterator.next();
				logList = segment.getLogBeanList();
				for (int i = 0; i < logList.size(); i++)
				{
					tBean = logList.get(i);
					writer.write(tBean.getDate() + " " + tBean.getFlag() + " "
							+ tBean.getTradeName() + " " + tBean.getMessage());
					writer.newLine();
					writer.flush();
				}
			}
		} catch (FileNotFoundException e)
		{

		} catch (UnsupportedEncodingException e)
		{
			
		} catch (IOException e)
		{
			
		} finally
		{
			try
			{
				if(writer != null)
					writer.close();
				if(osw != null)
					osw.close();
				if(fos != null)
					fos.close();
			} catch (IOException e2)
			{
			}
		}

	}

	/**
	 * 表格的数据插入动作
	 * 
	 * @param list
	 *            表格要显示的数据集合
	 */
	protected void tableComplete(List<TradeLogSegment> list)
	{
		if (list == null)
		{
			return;
		}
		table.removeAll();
		tableRow = 0;
		tableViewer.setInput(list);
	}

	@Override
	protected void checkSubclass()
	{
	}

	public class ContentProvider implements IStructuredContentProvider
	{
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement)
		{
			if (inputElement instanceof List)
			{
				List<TradeLogSegment> list = (List<TradeLogSegment>) inputElement;
				List<TradeLogBean> beanList = new LinkedList<TradeLogBean>();
				for (TradeLogSegment segment : list)
				{
					beanList.addAll(segment.getLogBeanList());
				}
				return beanList.toArray();
			} else
			{
				return new Object[0];
			}
		}

		public void dispose()
		{
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
		{
		}
	}

	public class TableLabelProvider implements ITableLabelProvider
	{
		public String getColumnText(Object element, int columnIndex)
		{
			if (element instanceof TradeLogBean)
			{
				TradeLogBean bean = (TradeLogBean) element;
				if (columnIndex == 0)
				{
					tableRow++;
					return tableRow + "";
				} else if (columnIndex == 1)
				{
					return bean.getDate();
				} else if (columnIndex == 2)
				{
					return bean.getFlag();
				} else if (columnIndex == 3)
				{
					return bean.getTradeName();
				} else if (columnIndex == 4)
				{
					return bean.getMessage();
				}
			}
			return null;
		}

		public Image getColumnImage(Object element, int columnIndex)
		{
			return null;
		}

		@Override
		public void addListener(ILabelProviderListener arg0)
		{

		}

		@Override
		public void dispose()
		{

		}

		@Override
		public boolean isLabelProperty(Object arg0, String arg1)
		{
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener arg0)
		{

		}

	}
}
