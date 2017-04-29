package org.xzd.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CmdExecUtil {

	private InputStream inputStream;
	private BufferedReader reader;
	private String cmd;
	private String[] cmds;
	private Process process;
	private boolean printLog = true;

	public CmdExecUtil(String cmd) {
		this.cmd = cmd;
	}

	public CmdExecUtil(String[] cmds) {
		this.cmds = cmds;
	}

	public BufferedReader getExecReader() throws IOException {
		if (!StringUtils.isEmpty(cmd))
			process = Runtime.getRuntime().exec(cmd);
		else if (cmds != null && cmds.length > 0)
			process = Runtime.getRuntime().exec(cmds);

		StreamGobbler errReader = new StreamGobbler(process.getErrorStream(), "ERRORSTREAM");
		errReader.setPrintLog(printLog);
		errReader.start();
		inputStream = process.getInputStream();
		reader = new BufferedReader(new InputStreamReader(inputStream));

		return reader;
	}

	public void kill() {
		if (process != null) {
			process.destroy();
		}
	}

	/**
	 * 获取进程的推出码
	 * 
	 * @return
	 */
	public int getExitValue() {
		int value = -1;
		if (process == null)
			return value;

		try {
			value = process.exitValue();
		} catch (Exception e) {
		}
		return value;
	}

	public void close() {
		CloseableUtils.close(reader);
		CloseableUtils.close(inputStream);
	}

	public String getCmd() {
		return cmd;
	}

	public void waitFor() throws InterruptedException {
		process.waitFor();
	}

	public String[] getCmds() {
		return cmds;
	}

	public void setPrintLog(boolean printLog) {
		this.printLog = printLog;
	}

}
