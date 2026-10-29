package cl.bcs.reports.report;

import java.lang.reflect.Field;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.Struct;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.serial.SerialArray;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;
import javax.sql.rowset.serial.SerialStruct;

import com.sun.rowset.CachedRowSetImpl;
import com.sun.rowset.internal.Row;

/**
 * Faithful port of the legacy cl.bcs.utils.ListMapResultSet (BcsUtils); only the package changed.
 * It extends the JDK-internal com.sun.rowset.CachedRowSetImpl and writes its private fields
 * (rvh, numRows) reflectively, so on JDK 9+ the JVM needs:
 *   --add-exports java.sql.rowset/com.sun.rowset=ALL-UNNAMED
 *   --add-exports java.sql.rowset/com.sun.rowset.internal=ALL-UNNAMED
 *   --add-opens   java.sql.rowset/com.sun.rowset=ALL-UNNAMED
 * (set in the Dockerfile ENTRYPOINT, the spring-boot plugin jvmArguments and the surefire argLine).
 * Column types are inferred from the first row only (String->VARCHAR, Number->DECIMAL,
 * java.sql.Date->DATE, byte[]->BLOB, anything else JAVA_OBJECT), exactly as in legacy.
 *
 * @author Marco Alarcon (legacy author)
 */
public class ListMapResultSet extends CachedRowSetImpl {

	private static final long serialVersionUID = 1L;

	private static RowSetMetaDataImpl metadata;

	public ListMapResultSet() throws SQLException {
		super();
	}

	@SuppressWarnings("unchecked")
	public <T> void populate(List<T> list) throws Exception {
		Map<String, Class<?>> localMap = getTypeMap();

		if (list == null) {
			throw new SQLException("Input parameter is null");
		}

		metadata = new RowSetMetaDataImpl();
		setMetaData(metadata);

		initMetaData(metadata, list);

		int j = metadata.getColumnCount();
		Row localRow = null;
		Vector<Object> rvh = new Vector<Object>(list.size());

		for (T item : list) {
			localRow = new Row(j);
			Map<String, Object> bean = (Map<String, Object>) item;

			int k = 1;
			for (int col = 1; col <= metadata.getColumnCount(); col++) {
				Object localObject = bean.get(metadata.getColumnName(col));

				if (localObject instanceof Struct)
					localObject = new SerialStruct((Struct) localObject, localMap);
				else if (localObject instanceof SQLData)
					localObject = new SerialStruct((SQLData) localObject, localMap);
				else if (localObject instanceof Blob)
					localObject = new SerialBlob((Blob) localObject);
				else if (localObject instanceof byte[])
					localObject = new SerialBlob((byte[]) localObject);
				else if (localObject instanceof Clob)
					localObject = new SerialClob((Clob) localObject);
				else if (localObject instanceof Array) {
					if (localMap != null)
						localObject = new SerialArray((Array) localObject, localMap);
					else {
						localObject = new SerialArray((Array) localObject);
					}
				}
				localRow.initColumnObject(k, localObject);
				k++;
			}
			rvh.add(localRow);
		}

		setField("rvh", rvh);
		setField("numRows", list.size());

		notifyRowSetChanged();
	}

	private <T> void initMetaData(RowSetMetaDataImpl metadata, List<T> list) throws SQLException {
		@SuppressWarnings("unchecked")
		Map<String, Object> bean = (Map<String, Object>) list.get(0);
		metadata.setColumnCount(bean.size());
		int j = 1;
		for (Entry<String, Object> entry : bean.entrySet()) {
			metadata.setAutoIncrement(j, false);
			metadata.setCaseSensitive(j, false);
			metadata.setCurrency(j, false);
			metadata.setNullable(j, ResultSetMetaData.columnNullable);
			metadata.setSigned(j, true);
			metadata.setSearchable(j, false);

			int displaySize = 8000;
			metadata.setColumnDisplaySize(j, displaySize);
			metadata.setColumnLabel(j, entry.getKey());
			metadata.setColumnName(j, entry.getKey());
			metadata.setSchemaName(j, "SCHEMA");

			int precision = 20;
			metadata.setPrecision(j, precision);

			int scale = 6;
			metadata.setScale(j, scale);
			metadata.setTableName(j, "TABLE");
			metadata.setCatalogName(j, "CATALOG");
			metadata.setColumnType(j, getColumnType(entry.getValue()));
			metadata.setColumnTypeName(j, null);

			j++;
		}
	}

	private int getColumnType(Object obj) {
		if (obj instanceof String) {
			return Types.VARCHAR;
		}
		if (obj instanceof Number) {
			return Types.DECIMAL;
		}
		if (obj instanceof Date) {
			return Types.DATE;
		}
		if (obj instanceof byte[]) {
			return Types.BLOB;
		}

		return Types.JAVA_OBJECT;
	}

	private void setField(String field, Object value) throws Exception {
		Field f = getClass().getSuperclass().getDeclaredField(field);
		f.setAccessible(true);
		f.set(this, value);
	}
}
