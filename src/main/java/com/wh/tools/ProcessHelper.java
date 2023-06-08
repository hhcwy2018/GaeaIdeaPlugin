package com.wh.tools;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.*;

public class ProcessHelper {

	private static final String OS = System.getProperty("os.name").toLowerCase();

	public static class ProcessInfo {
		public String name;
		public String pid;

		@Override
		public String toString() {
			return name;
		}

		public ProcessInfo(String name, String pid) {
			this.name = name;
			this.pid = pid;
		}

	}

	public static boolean isLinux() {
		return OS.indexOf("linux") >= 0;
	}

	public static boolean isMacOS() {
		return OS.indexOf("mac") >= 0 && OS.indexOf("os") > 0 && OS.indexOf("x") < 0;
	}

	public static boolean isMacOSX() {
		return OS.indexOf("mac") >= 0 && OS.indexOf("os") > 0 && OS.indexOf("x") > 0;
	}

	public static boolean isWindows() {
		return OS.indexOf("windows") >= 0;
	}

	protected static List<ProcessInfo> readProcess(String command, int nameIndex, int pidIndex) throws IOException {
		List<String> tasklist = new ArrayList<String>();
		Process process = Runtime.getRuntime().exec(command);
		try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));) {
			String s = "";
			while ((s = br.readLine()) != null) {
				if ("".equals(s.trim())) {
					continue;
				}
				tasklist.add(s.trim());
			}
		}

		// 定义映像名称数组
		Map<String, ProcessInfo> unqNames = new TreeMap<>();
		for (int i = 0; i < tasklist.size(); i++) {
			String data = tasklist.get(i) + "";
			data = data.replaceAll("[ ]+", " ");
			String[] datas = data.split(" ");

			if (datas.length <= nameIndex)
				continue;

			String name = datas[nameIndex].trim();
			unqNames.put(name.toLowerCase(), new ProcessInfo(name, datas[pidIndex].toLowerCase().trim()));
		}

		return new ArrayList<>(unqNames.values());
	}

	protected static List<ProcessInfo> readProcess(String command) throws IOException {
		List<ProcessInfo> list = readProcess("jps", 1, 0);
		Map<String, ProcessInfo> map = new HashMap<>();
		for (ProcessInfo processInfo : list) {
			map.put(processInfo.pid, processInfo);
		}

		list = readProcess(command, 0, 1);
		for (ProcessInfo processInfo : list) {
			ProcessInfo jpsInfo = map.get(processInfo.pid);
			if (jpsInfo != null) {
				processInfo.name = processInfo.name + "[" + jpsInfo.name + "]";
			} else {
				map.put(processInfo.name, processInfo);
			}
		}

		return new ArrayList<>(map.values());
	}

	public static List<ProcessInfo> getProcessList() throws IOException {

		Properties prop = System.getProperties();
		// 获取操作系统名称
		String os = prop.getProperty("os.name");
		if (os != null && os.toLowerCase().indexOf("linux") > -1) {
			return readProcess("ps -ef");
		} else {
			// 2.适应与windows
			return readProcess("tasklist /nh");
		}

	}

	public static final int getProcessId() {
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		return Integer.valueOf(runtimeMXBean.getName().split("@")[0]).intValue();
	}

	public static int getProcessId(String name) throws IOException {
		List<ProcessInfo> processInfos = getProcessList();
		for (ProcessInfo processInfo : processInfos) {
			if (processInfo.name.equalsIgnoreCase(name)) {
				return Integer.parseInt(processInfo.pid);
			}
		}

		return -1;
	}

	public static void killProcessForPid(String pid) throws IOException {
		if (isWindows()) {
			Runtime.getRuntime().exec("taskkill /f /pid " + pid);
		} else
			Runtime.getRuntime().exec("kill -s 9 " + pid);
	}

	public static void killProcessforName(String name) throws IOException {
		int pid = getProcessId(name);
		if (pid == -1)
			return;

		killProcessForPid(String.valueOf(pid));
	}

	public static void executeProcess(File commandFile, String commandName) throws IOException {
		String name = commandName == null || commandName.isEmpty() ? commandFile.getName() : commandName;
		String command = commandFile.getAbsolutePath();
		if (commandFile.getName().toLowerCase().endsWith(".jar")) {
			command = commandFile.getAbsolutePath().substring(0, 1) + ":\r\n" + "cd " + commandFile.getParent() + "\r\n"
					+ "java -jar -Dloader.path=resources,lib ./" + name + " -XX:+UseG1GC\r\ncmd /k";
		}
		executeProcess(command, name);
	}

	public static void executeProcess(String command, String processName) throws IOException {
		int pid = ProcessHelper.getProcessId(processName);
		if (pid != -1) {
			throw new IOException("service started!");
		}

		File file = new File(command);
		if (file.exists())
			Desktop.getDesktop().open(file);
		else
			Runtime.getRuntime().exec(command);
	}

	public static File getJavaExecuteFile() {
		File emptyJava = new File("java");

		if (isWindows()) {
			File javaHome = new File("C:\\Program Files\\Java");
			if (!javaHome.exists())
				return emptyJava;

			File[] files = javaHome.listFiles();
			if (files == null || files.length == 0)
				return emptyJava;

			for (File file : files) {
				if (!file.isDirectory())
					continue;

				String name = file.getName().toLowerCase();
				if (name.contains("jdk") || name.contains("jre")) {
					File javaFile = new File(file, "/bin/java.exe");
					if (javaFile.exists())
						return javaFile;
					else {
						return emptyJava;
					}
				}
			}
		}
		return emptyJava;
	}
}
