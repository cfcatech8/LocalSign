package gps949;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class HostsWritter {
	public synchronized boolean update(String hostName, String ip) {
		String splitter = " ";
		String fileName = "C://WINDOWS//system32//drivers//etc//hosts";

		if (!new File(fileName).exists()) {
			try {
				MoveOutFile("NewHost.bat");
				Process p = Runtime.getRuntime().exec(System.getProperty("user.dir") + "\\NewHost.bat");
				p.waitFor();
			} catch (Exception e) {
			}
		}
		int waitCounter = 150;
		while (!new File(fileName).exists() && waitCounter > 0) {
			try {
				Thread.sleep(100);
				waitCounter--;
			} catch (InterruptedException e) {
			}
		}
		if (waitCounter <= 0) {
			return false;
		}
		// 更新设定文件
		List<String> lines = null;
		try {
			lines = FileUtils.readLines(new File(fileName));
		} catch (IOException e1) {
		}
		List<String> newLines = new ArrayList<String>();
		boolean findFlag = false;
		boolean updateFlag = false;
		for (String strLine : lines) {
			if (strLine.replace("/t", " ").replace(" ", "").equals("") || strLine.startsWith("#")) {
				newLines.add(strLine);
			} else {
				int index = strLine.toLowerCase().indexOf(hostName.toLowerCase());
				if (index == -1) {
					newLines.add(strLine);
				} else {
					strLine = strLine.replaceAll("/t", splitter);
					String[] array = strLine.trim().split(splitter);
					if (array[0].equals(ip)) {
						findFlag = true;
						newLines.add(strLine);
					} else {
						updateFlag = true;
						StringBuilder sb = new StringBuilder();
						sb.append(array[0]);
						for (int i = 1; i < array.length; i++) {
							if (!hostName.equalsIgnoreCase(array[i])) {
								sb.append(splitter).append(array[i]);
							}
						}
						if (sb.toString().trim().split(splitter).length != 1)
							newLines.add(sb.toString());
					}
				}
			}
		}
		// 如果没有正确Host名设定，则追加
		if (!findFlag) {
			newLines.add(new StringBuilder(ip).append(splitter).append(hostName).toString());
		}
		if (!findFlag || updateFlag) {
			try {
				fileName = "hosts";
				FileUtils.writeLines(new File(fileName), newLines);
				MoveOutFile("FixHost.bat");
				Runtime.getRuntime().exec(System.getProperty("user.dir") + "\\FixHost.bat");
			} catch (Exception e) {
			}
		}

		return !findFlag;
	}

	// 把项目内的文件写到外部
	public void MoveOutFile(String srcPath) throws Exception {
		int index;
		byte[] bytes = new byte[1024];
		InputStream fis = getClass().getClassLoader().getResourceAsStream(srcPath);
		FileOutputStream fos = new FileOutputStream(srcPath);
		while ((index = fis.read(bytes)) != -1) {
			fos.write(bytes, 0, index);
			fos.flush();
		}
		fos.close();
		fis.close();
	}
}
