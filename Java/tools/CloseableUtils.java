package org.xzd.tools;

import java.io.Closeable;
import java.io.IOException;

/**
 *  资源释放类
 * @author: xu_zhidan
 * @date: 2016年11月25日 上午9:35:43
 */
public class CloseableUtils {
	// 关闭资源
	public static void close(Closeable object) {
		if (object == null)
			return;
		try {
			object.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
