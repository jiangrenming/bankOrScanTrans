package com.nld.starpos.wxtrade.utils.jsonUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取对象字段名和字段值工具
 */
public class ParseObjectUtils {
	
	/** 数据类型 */
	private static final List<String> TYPES = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;

		{add("int");add("boolean");add("char");add("float");add("double");add("long");add("short");add("byte");
		add("java.lang.Integer");add("java.lang.String");add("java.lang.Boolean");add("java.lang.Character");
		add("java.lang.Float");add("java.lang.Double");add("java.lang.Long");add("java.lang.Short");add("java.lang.Byte");
		add("java.util.List");add("java.util.Map");}
	};
	
	public static String toString(Object obj) {
		if (obj == null) {
			return "null";
		}
		
		StringBuilder sb = new StringBuilder();
		Field[] fields = obj.getClass().getDeclaredFields();
		
		int fLength = fields.length - 1;
		if (fLength == -1) {
			return "{}";
		}
		
		for (int i = 0; i <= fLength; i++) {
			Field field = fields[i];
			field.setAccessible(true);
			sb.append(field.getName());
			sb.append("=");
			
			if (TYPES.contains(field.getType().getName()) || field.getType().getEnumConstants() != null) {
				try {
					sb.append(field.get(obj));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					if (field.getName().contains("this")) {
						continue;
					}
					sb.append("{");
					sb.append(toString(field.get(obj)));
					sb.append("}");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if (i < fLength) {
				sb.append(", ");
			}
		}
		
		return sb.toString();
	}
}
