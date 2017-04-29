package org.xzd.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 *  把session的标准输出包装成InputStream，用于接收目标服务器上的控制台返回结果
 * @author: xu_zhidan
 * @date: 2016年11月25日 上午9:42:27
 */
public class StreamGobbler extends Thread {
	private InputStream is;
	private String type;
	private OutputStream os;
	private boolean printLog = true;

	public StreamGobbler(InputStream is, String type) {
		this(is, type, null);
	}

	public StreamGobbler(InputStream is, String type, OutputStream redirect) {
		this.is = is;
		this.type = type;
		this.os = redirect;
	}

	@Override
	public void run() {
		InputStreamReader isr = null;
		BufferedReader br = null;
		PrintWriter pw = null;
		try {
			if (os != null)
				pw = new PrintWriter(os);

			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if (pw != null)
					pw.println(line);

				if (printLog)
					System.out.println(type + ">" + line);
			}

			if (pw != null)
				pw.flush();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			CloseableUtils.close(pw);
			CloseableUtils.close(br);
			CloseableUtils.close(isr);
		}
	}

	public void setPrintLog(boolean printLog) {
		this.printLog = printLog;
	}

}
