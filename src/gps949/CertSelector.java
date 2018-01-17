package gps949;

import java.security.KeyStore;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class CertSelector {

	public CertSelector(KeyStore ks, List<String> _aliases, AliasCan ac) {
		AliasCan tempAC = new AliasCan();
		Display display = Display.getDefault();
		Shell tmpShell = new Shell(display);
		CertDlg certDlg = new CertDlg(tmpShell, ks, _aliases, tempAC);
		certDlg.setBlockOnOpen(true);
		if (certDlg.open() == Dialog.OK) {
			certDlg.close();
			tmpShell.dispose();
			ac.alias = tempAC.alias;
		} else {
			ac.alias = "ERROR";
		}

	}

}
