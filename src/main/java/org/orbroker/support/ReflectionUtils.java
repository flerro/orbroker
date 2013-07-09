package org.orbroker.support;

import static java.lang.String.format;

import java.lang.reflect.Method;

import org.springframework.util.StringUtils;

public class ReflectionUtils {

	public static Class<?> forName(String clazz) {
		Class<?> forName = null;
		try {
			forName = Class.forName(clazz);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
		return forName;
	}

	/*
	public static Field findField(Class<?> targetClass, String propertyName){
		Field field = org.springframework.util.ReflectionUtils.findField(targetClass, propertyName);
		if (field == null){
			throw new IllegalStateException(format("Unable to find field %s in %s",
															propertyName, targetClass.getName()));
		}
		return field;
	}
	*/
	
	public static Method findSetterMethod(Class<?> targetClass, String propertyName, Class<?> ... arguments){
		String methodName = "set" + StringUtils.capitalize(propertyName);
		return findMethod(targetClass, methodName, arguments);
	}
	
	public static Method findMethod(Class<?> targetClass, String methodName, Class<?> ... arguments){
		Method method = org.springframework.util.ReflectionUtils.findMethod(targetClass, methodName, arguments);
		if (method == null){
			StringBuilder args = new StringBuilder();
			for (Class<?> clazz : arguments){
				args.append(',').append(clazz.getName());
			}
			throw new IllegalStateException(format("Unable to find method %s(%s) in %s",
												methodName, args.substring(1), targetClass.getName()));
		}
		return method;
	}

}
