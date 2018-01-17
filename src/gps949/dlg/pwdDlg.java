package gps949.dlg;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gps949.block.P10pack;

public class pwdDlg extends Dialog {

	private P10pack myP10p;
	private Text pwdBox;

	public pwdDlg(Shell parent, P10pack _P10p) {
		super(parent);
		myP10p = _P10p;

	}

	protected int getShellStyle() {
		return SWT.TITLE;
	}

	protected void createButtonsForButtonBar(Composite parent) {
	}

	protected Control createDialogArea(Composite parent) {
		Shell myshell = this.getShell();
		myshell.setText("请设置私钥密码");

		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gdlay = new GridLayout();
		gdlay.numColumns = 3;
		container.setLayout(gdlay);

		pwdBox = new Text(container, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		pwdBox.setFont(new Font(null, "宋体", 10, SWT.BOLD));
		pwdBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		myshell.setSize(myshell.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return container;
	}

	protected void okPressed() {
		myP10p.pwd = pwdBox.getText();
		super.okPressed();
	}

	protected void cancelPressed() {
		myP10p.pwd = "";
		super.cancelPressed();
	}

	protected void initializeBounds() {
		Composite comp = (Composite) getButtonBar();
		super.createButton(comp, IDialogConstants.OK_ID, "确定", true);
		super.createButton(comp, IDialogConstants.CANCEL_ID, "取消", false);
		Shell myshell = this.getShell();

		OS.SetWindowPos(myshell.handle, OS.HWND_TOPMOST, 0, 0, 0, 0, SWT.NULL);

		myshell.setSize(myshell.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		Rectangle bounds = Display.getDefault().getPrimaryMonitor().getBounds();
		Rectangle rect = myshell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		myshell.setLocation(x, y);
	}

}
