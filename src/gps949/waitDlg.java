package gps949;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class waitDlg extends Dialog {
	private String msg;
	private DlgChannel dc;

	protected waitDlg(Shell parentShell, String _msg, DlgChannel _dc) {
		super(parentShell);
		msg = _msg;
		dc = _dc;
	}

	protected int getShellStyle() {
		return SWT.NO_TRIM;
	}

	protected void createButtonsForButtonBar(Composite parent) {
	}

	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gdlay = new GridLayout();
		gdlay.numColumns = 3;
		container.setLayout(gdlay);
		Label msgShow = new Label(container, SWT.NONE);
		msgShow.setFont(new Font(null, "宋体", 24, SWT.BOLD));
		msgShow.setText(msg);
		Shell myshell = this.getShell();

		OS.SetWindowPos(myshell.handle, OS.HWND_TOPMOST, 0, 0, 0, 0, SWT.NULL);

		myshell.setSize(myshell.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		Rectangle bounds = Display.getDefault().getPrimaryMonitor().getBounds();
		Rectangle rect = myshell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		myshell.setLocation(x, y);

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				while (!dc.flag) {
					try {
						Thread.sleep(100);
					} catch (Exception e) {
					}
				}
				myshell.dispose();
			}
		});
		return container;
	}
}
