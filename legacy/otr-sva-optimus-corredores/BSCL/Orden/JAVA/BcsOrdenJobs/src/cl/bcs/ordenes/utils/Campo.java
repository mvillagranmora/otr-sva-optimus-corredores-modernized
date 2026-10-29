package cl.bcs.ordenes.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class Campo {
	String fieldName;
	String fieldType;
	
	public Campo(String fn,String ft) {
		this.fieldName = fn;
		this.fieldType = ft;
	}

	
	public static String getCreateTableString(ArrayList<Campo> campos,String tableName) {
		String sql = "Create table " +tableName + "(";
		for (int i = 0; i < campos.size(); i++) {
			sql+="\""+campos.get(i).fieldName+"\""+ campos.get(i).fieldType + "";
			if (i < campos.size() - 1) {
				sql += ",\n";
			}
		}
		sql +=");";
		return sql;
	}
	
	public static String getDropTableString(String tableName) {
		String sql = "drop table " +tableName;
		return sql;
	}
	
	public static String getInsertBatchString(Class c,String tableName) {
		Field[] fields = c.getFields();
		String sql1 = "INSERT INTO " +tableName + "(";
		String sql2 = "VALUES (";
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].getName().startsWith("_")) continue;
			sql1+="\""+fields[i].getName()+"\"";
			sql2+=":"+fields[i].getName();
			if (i < fields.length - 1) {
				sql1 += ",";
				sql2 += ",";
			}
		}
		sql1 +=")\n";
		sql2 +=")";
		return sql1 + sql2;
	}
	
	public static String getInsertBatchString(ArrayList<Campo> campos,String tableName) {
		String sql1 = "INSERT INTO " +tableName + "(";
		String sql2 = "VALUES (";
		for (int i = 0; i < campos.size(); i++) {
			sql1+="\""+campos.get(i).fieldName+"\"";
			sql2+=":"+campos.get(i).fieldName;
			if (i < campos.size() - 1) {
				sql1 += ",";
				sql2 += ",";
			}
		}
		sql1 +=")\n";
		sql2 +=")";
		return sql1 + sql2;
	}
}