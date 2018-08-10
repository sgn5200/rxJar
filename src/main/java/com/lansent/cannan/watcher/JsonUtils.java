package com.lansent.cannan.watcher;

/**
 * Description    :
 * CreateAuthor: Cannan
 * CreateTime   : 2018/8/7     11:10
 * Project          : MyNetPackage
 * PackageName :  com.shang.cannan.mynetpackage.util;
 */

public class JsonUtils
{
	/**
	 * @date  2017/8/24
	 * @description 将字符串格式化成JSON的格式
	 */
	public static String stringToJSON(String strJson) {
		// 计数tab的个数
		int tabNum = 0;
		StringBuffer jsonFormat = new StringBuffer();
		int length = strJson.length();

		char last = 0;
		for (int i = 0; i < length; i++) {
			char c = strJson.charAt(i);
			if (c == '{') {
				tabNum++;
				jsonFormat.append(c + "\n");
				jsonFormat.append(getSpaceOrTab(tabNum));
			}
			else if (c == '}') {
				tabNum--;
				jsonFormat.append("\n");
				jsonFormat.append(getSpaceOrTab(tabNum));
				jsonFormat.append(c);
			}
			else if (c == ',') {
				jsonFormat.append(c + "\n");
				jsonFormat.append(getSpaceOrTab(tabNum));
			}
			else if (c == ':') {
				jsonFormat.append(c + " ");
			}
			else if (c == '[') {
				tabNum++;
				char next = strJson.charAt(i + 1);
				if (next == ']') {
					jsonFormat.append(c);
				}
				else {
					jsonFormat.append(c + "\n");
					jsonFormat.append(getSpaceOrTab(tabNum));
				}
			}
			else if (c == ']') {
				tabNum--;
				if (last == '[') {
					jsonFormat.append(c);
				}
				else {
					jsonFormat.append("\n" + getSpaceOrTab(tabNum) + c);
				}
			}
			else {
				jsonFormat.append(c);
			}
			last = c;
		}
		return jsonFormat.toString();
	}

	private static String getSpaceOrTab(int tabNum) {
		StringBuffer sbTab = new StringBuffer();
		for (int i = 0; i < tabNum; i++) {
			sbTab.append("\t\t\t\t");
		}
		return sbTab.toString();
	}
}
