package com.octo.tools.crud.utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

/**
 * Helper class for reflection operations
 * 
 * @author OCTO
 * 
 */
public class ReflectionUtils {

	private ReflectionUtils() {}
	
	private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
		if (type.isAnnotationPresent(Entity.class) || type.isAnnotationPresent(MappedSuperclass.class)) {
			fields.addAll(Arrays.asList(type.getDeclaredFields()));			
			if (type.getSuperclass() != null) {
				return getAllFields(fields, type.getSuperclass());
			}
		}
	
		return fields;
	}

	
	/**
	 * @param type class to introspect
	 * @return the list of all fields of this class (including fields of superclasses)
	 */
	public static List<Field> getAllFields(Class<?> type) {
		return getAllFields(new ArrayList<Field>(), type);
	}

	
	/**
	 * @param javaType entity class
	 * @return true if javaType is not annotated with {@link org.hibernate.envers.RevisionEntity}
	 */
	public static boolean isEntityExposed(Class javaType) {
		return javaType != null && !javaType.isAnnotationPresent(RevisionEntity.class)
				&& !javaType.equals(DefaultRevisionEntity.class);
	}

	/**
	 * @param pd PropertyDescriptor
	 * @param f field linked to pd
	 * @return SimpleName of return type for non collection fields, else &lt;Generic type of collection&gt; 
	 */
	public static String getReturnTypeAsString(PropertyDescriptor pd, Field f) {
		String simpleName = pd.getReadMethod().getReturnType().getName();
		if(Collection.class.isAssignableFrom(pd.getPropertyType())) {
			ParameterizedType genericType = (ParameterizedType) f.getGenericType();
			if(genericType != null) {
				StringBuilder sb = new StringBuilder(simpleName + "<");
				for(Type t : genericType.getActualTypeArguments()) {
					sb.append(t.getTypeName()).append(">");
				}
				return sb.toString(); 
			}	
		}
		return simpleName;
	}
	
	

	/**
	 * @param pds array of the {@link PropertyDescriptor} of the class
	 * @param f field to get PropertyDescriptor
	 * @return the {@link PropertyDescriptor} of f, null if not found
	 */
	public static PropertyDescriptor getPropertyDescriptor(
			PropertyDescriptor[] pds, Field f)  {
		for(PropertyDescriptor p : pds) {
			if(f.getName().equals(p.getName())) {
				return p;
			}
		}
		return null;
	}
	
	
	/**
	 * @param c Instance class
	 * @return true if c is a {@link Number} subclass (or primitive number type)
	 */
	public static boolean isNumber(Class c) {
		if(Number.class.isAssignableFrom(c) || 
				(c.isPrimitive() && isPrimitiveNumber(c)))
			return true;
		return false;
	}

	private static boolean isPrimitiveNumber(Class c) {
		return c.equals(int.class) || c.equals(double.class) || c.equals(long.class) || c.equals(short.class);
	}

	/**
	 * @param fieldType Instance class
	 * @return true if fieldType is {@link Boolean} or boolean primitive type
	 */
	public static boolean isBoolean(Class<?> fieldType) {
		return fieldType.equals(Boolean.class) || fieldType.equals(boolean.class);
	}


	/**
	 * @param field Field to test
	 * @return true if field is annotated with {@link OneToMany} or {@link ManyToMany}
	 */
	public static boolean hasCollections(Field field) {
		return field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class);
	}

	/**
	 * @param field Field to test
	 * @return true if field is annotated with {@link ManyToOne} or {@link OneToOne}
	 */
	public static boolean hasLinks(Field field) {
		return field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class);
	}

	
	/**
	 * Get the simple name of the class T when a field is a Collection<T>
	 * @param f the field holding the Collection
	 * @return the simple name of the first parameterized type of the collection, null if f is not a generic parameterized type
	 * @throws ClassNotFoundException if the parameterized type is not found in the classpath
	 */
	public static String getGenericCollectionType(Field f) throws ClassNotFoundException {
		ParameterizedType genericType = (ParameterizedType) f.getGenericType();
		if(genericType != null) {
			for(Type t : genericType.getActualTypeArguments()) {
				return Class.forName(t.getTypeName()).getSimpleName();
			}
		}
		return null;
	}

}
