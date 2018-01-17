package gps949;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class CertDlg extends Dialog {

	private KeyStore ks;
	private List<String> aliases;
	private AliasCan ac;

	public CertDlg(Shell parent, KeyStore _ks, List<String> _aliases, AliasCan _ac) {
		super(parent);
		ks = _ks;
		aliases = _aliases;
		ac = _ac;
		ac.alias = "ERROR";
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
		org.eclipse.swt.widgets.List certList = new org.eclipse.swt.widgets.List(container, SWT.NONE);
		for (int i = 0; i < aliases.size(); i++) {
			try {
				certList.add(((X509Certificate) ks.getCertificate(aliases.get(i))).getSubjectDN().getName());
			} catch (KeyStoreException e) {
			}
		}
		SelectionListener listLsn = new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				ac.alias = aliases.get(certList.getSelectionIndex());
			}
		};
		certList.addSelectionListener(listLsn);
		certList.select(0);
		ac.alias = aliases.get(0);
		this.getShell().setSize(this.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return container;
	}

	protected void initializeBounds() {
		Composite comp = (Composite) getButtonBar();
		super.createButton(comp, IDialogConstants.OK_ID, "签名", false);
		super.createButton(comp, IDialogConstants.CANCEL_ID, "取消", true);
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
