package cn.com.agree.ab.a4.pub.logreader.tool;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class MessageUtils
{
	public static void showError(Shell shell, String message)
	{
		MessageBox dialog = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
		dialog.setMessage(message);
		dialog.open();
	}
}
