package org.xzd.tools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class StringUtils {

	/**
	 * 字符串转化为key-value形式
	 * 
	 * @param str
	 * @return
	 * @throws IOException
	 */
	public static Properties str2Properties(String str) throws IOException {
		Properties prop = new Properties();
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(str.getBytes());
			prop.load(is);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
		return prop;
	}

	/**
	 * 字符串是否为空
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}

	public static List<String> strToList(String str, String split) {
		List<String> list = new ArrayList<String>();
		if (!StringUtils.isEmpty(str)) {
			// 去除括号
			if (str.contains("(") && str.contains(")")) {
				int start = str.indexOf("(");
				int end = str.indexOf(")");
				str = str.substring(start + 1, end);
			}

			String[] ss = str.split(split);
			for (String s : ss) {
				list.add(s);
			}
		}

		return list;
	}

	public static String makeIpString(String[] ips, int count) {
		StringBuilder builder = new StringBuilder();

		builder.append("(");
		for (int i = 0; i < count; i++) {
			builder.append(ips[i]);
			if (i == count - 1) {
				builder.append(")");
			} else {
				builder.append(" ");
			}
		}
		return builder.toString();
	}

	public static String join(String[] array) {
		StringBuffer sb = new StringBuffer();
		int length = array.length;
		for (int i = 0; i < length; i++) {
			sb.append(array[i]).append(",");
		}

		return sb.substring(0, sb.length() - 1).toString();
	}

	/**
	 * 将list的stirng元素转为以逗号相连接的字符串
	 * 
	 * @author 26820
	 * @param list
	 *            元素如[1,2,3,4,5]
	 * @return 1,2,3,4,5,6
	 */
	public static String listToSTring(List<String> list) {
		if (list == null || list.size() == 0) {
			return "";
		}

		String result = "";
		for (String str : list) {
			if (!StringUtils.isEmpty(str.trim()))
				result += (str + ",");
		}
		if (result.length() == 0) {
			return "";
		}
		return result.substring(0, result.length() - 1);
	}

	/**
	 * 将strs2中的元素从strs1中移除
	 * 
	 * @author 26820
	 * @param strs1
	 *            如1,2,3,4
	 * @param strs2
	 *            如2,4
	 * @return 返回1,3
	 */
	public static String removeString(String strs1, String strs2) {

		String[] strArr1 = strs1.split(",");
		String[] strArr2 = strs2.split(",");
		List<String> arrayList1 = new ArrayList<String>(1);
		List<String> arrayList2 = new ArrayList<String>(1);
		Collections.addAll(arrayList1, strArr1);
		Collections.addAll(arrayList2, strArr2);

		for (String e : arrayList2) {
			if (!StringUtils.isEmpty(e))
				arrayList1.remove(e);
		}

		return listToSTring(arrayList1);
	}

	/**
	 * 字符数组转list
	 * 
	 * @param strArray
	 * @return
	 */
	public static List<String> asList(String[] strArray) {
		List<String> list = new ArrayList<String>();
		if (strArray != null) {
			for (String s : strArray)
				list.add(s);
		}
		return list;
	}

	/**
	 * 如果字符串为空返回空字符串, 否则返回原字符串
	 * 
	 * @param str
	 * @return
	 */
	public static String getEmptyStringIfIsEmpty(String str) {
		if (isEmpty(str))
			return "";

		return str;
	}

}
