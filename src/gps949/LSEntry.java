package gps949;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

public class LSEntry {

	public static void main(String[] args) {

		new HostsWritter().update("LocalSign.gps949.com", "127.0.0.1");
		Display display = new Display();

		// 禁用掉了最大化按钮
		final Shell shell = new Shell(display, SWT.SHELL_TRIM ^ SWT.MAX);
		shell.setText("LocalSign");
		// 取系统中预置的图标，省得测试运行时还得加个图标文件

		// 构造系统栏控件
		final Tray tray = display.getSystemTray();
		final TrayItem trayItem = new TrayItem(tray, SWT.NONE);
		trayItem.setVisible(true);
		trayItem.setToolTipText("LocalSign");
		trayItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// 点击托盘图标动作
			}
		});

		final Menu trayMenu = new Menu(shell, SWT.POP_UP);
		MenuItem aboutMenuItem = new MenuItem(trayMenu, SWT.PUSH);
		aboutMenuItem.setText("Copyright@小北极星");
		aboutMenuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				// 版权按钮动作
			}
		});
		trayMenu.setDefaultItem(aboutMenuItem);

		new MenuItem(trayMenu, SWT.SEPARATOR);
		// 系统栏中的退出菜单，程序只能通过这个菜单退出
		MenuItem exitMenuItem = new MenuItem(trayMenu, SWT.PUSH);
		exitMenuItem.setText("退出(&x)");
		exitMenuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				shell.dispose();
			}
		});

		// 在系统栏图标点击鼠标右键时的事件，弹出系统栏菜单
		trayItem.addMenuDetectListener(new MenuDetectListener() {
			public void menuDetected(MenuDetectEvent e) {
				trayMenu.setVisible(true);
			}
		});
		trayItem.setImage(display.getSystemImage(SWT.ICON_WORKING));
		shell.setSize(0, 0);

		Listener lsn = new Listener();
		try {
			lsn.run(null);
		} catch (Exception e1) {
		}

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		try {
			lsn.close();
		} catch (Exception e1) {
		}
		display.dispose();
		System.exit(0);
	}
}