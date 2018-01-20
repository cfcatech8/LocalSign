package gps949;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import gps949.block.P12pack;

public class pfxSaver {

	public pfxSaver(P12pack P12p) {
		Display display = Display.getDefault();
		Shell tmpShell = new Shell(display);
		FileDialog dialog = new FileDialog(tmpShell, SWT.SAVE);
		dialog.setText("请妥善保存您的PFX文件");
		dialog.setFilterNames(new String[] { "pfx文件 (*.pfx)", "p12文件(*.p12)" });// 设置扩展名
		dialog.setFilterExtensions(new String[] { "*.pfx", "*.p12" });// 设置文件扩展名
		P12p.P12 = dialog.open(); // 获得保存的文件名
	}

}
