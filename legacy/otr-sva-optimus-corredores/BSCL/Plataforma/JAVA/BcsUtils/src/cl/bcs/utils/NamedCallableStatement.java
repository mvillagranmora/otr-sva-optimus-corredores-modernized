package cl.bcs.utils;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class wraps around a {@link CallableStatement} and allows the programmer to set parameters by name instead of by
 * index. This eliminates any confusion as to which parameter index represents what. This also means that rearranging
 * the SQL statement or adding a parameter doesn't involve renumbering your indices. 
 * 
 * Symbols:
 * : => scalar input parameters 
 * @ => scalar out  parameters
 * $ => Table out parameters (not count as indeces)
 * 
 * @author kaiser (Callable), Original from adam_crume
 */

public class NamedCallableStatement {
	/** The statement this object is wrapping. */
	private final CallableStatement statement;

	/** Maps parameter names to arrays of ints which are the parameter indices. */
	private Map<String, int[]> indexMap;
	
	/** Maps parameter names to arrays of ints which are the parameter indices. */
	private Map<String,  int[]> indexMapOut;
	
	/** Maps parameter names to arrays of ints which are the parameter indices. */
	private Map<String,  int[]> indexMapTabOut;	

	/**
	 * Creates a NamedParameterStatement. Wraps a call to c.{@link Connection#prepareStatement(java.lang.String)
	 * prepareStatement}.
	 * 
	 * @param connection
	 *            the database connection
	 * @param query
	 *            the parameterized query
	 * @throws SQLException
	 *             if the statement could not be created
	 */
	public NamedCallableStatement(Connection connection, String query) throws SQLException {
		String parsedQuery = parse(query);
		statement = connection.prepareCall(parsedQuery);
	}

	/**
	 * Parses a query with named parameters. The parameter-index mappings are put into the map, and the parsed query is
	 * returned. DO NOT CALL FROM CLIENT CODE. This method is non-private so JUnit code can test it.
	 * 
	 * @param query
	 *            query to parse
	 * @param paramMap
	 *            map to hold parameter-index mappings
	 * @return the parsed query
	 */
	final String parse(String query) {
		// I was originally using regular expressions, but they didn't work well for ignoring
		// parameter-like strings inside quotes.
		int length = query.length();
		StringBuffer parsedQuery = new StringBuffer(length);
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		int index = 1;
		HashMap<String, List<Integer>> indexes    = new HashMap<String, List<Integer>>(10);
		HashMap<String, List<Integer>> indexesOut = new HashMap<String, List<Integer>>(10);
		HashMap<String, List<Integer>> indexesTabOut = new HashMap<String, List<Integer>>(10);

		for (int i = 0; i < length; i++) {
			char c = query.charAt(i);
			if (inSingleQuote) {
				if (c == '\'') {
					inSingleQuote = false;
				}
			} else if (inDoubleQuote) {
				if (c == '"') {
					inDoubleQuote = false;
				}
			} else {
				if (c == '\'') {
					inSingleQuote = true;
				} else if (c == '"') {
					inDoubleQuote = true;
				} else if ((c == ':' || c == '@' ||  c == '$')  && i + 1 < length && Character.isJavaIdentifierStart(query.charAt(i + 1))) {
					int j = i + 2;
					char type = c;
					while (j < length && Character.isJavaIdentifierPart(query.charAt(j))) {
						j++;
					}
					String name = query.substring(i + 1, j);
					c = '?'; // replace the parameter with a question mark
					i += name.length(); // skip past the end if the parameter
					if (type==':') {
						List<Integer> indexList = indexes.get(name);
						if (indexList == null) {
							indexList = new LinkedList<Integer>();
							indexes.put(name, indexList);
						}
						indexList.add(Integer.valueOf(index));
						index++;
					}
					else if (type=='$') { //out tables
						List<Integer> indexList = indexesOut.get(name);
						if (indexList == null) {
							indexList = new LinkedList<Integer>();
							indexesTabOut.put(name, indexList);
						}
						indexList.add(Integer.valueOf(index));						
					}
					else {
						List<Integer> indexList = indexesOut.get(name);
						if (indexList == null) {
							indexList = new LinkedList<Integer>();
							indexesOut.put(name, indexList);
						}
						indexList.add(Integer.valueOf(index));	
						index++;
					}					
				}
			}
			parsedQuery.append(c);
		}

		//IN
		indexMap = new HashMap<String, int[]>(indexes.size());
		// replace the lists of Integer objects with arrays of ints
		for (Map.Entry<String, List<Integer>> entry : indexes.entrySet()) {
			List<Integer> list = entry.getValue();
			int[] intIndexes = new int[list.size()];
			int i = 0;
			for (Integer x : list) {
				intIndexes[i++] = x.intValue();
			}
			indexMap.put(entry.getKey(), intIndexes);
		}

		//OUT
		indexMapOut = new HashMap<String, int[]>(indexes.size());
		// replace the lists of Integer objects with ints
		for (Map.Entry<String, List<Integer>> entry : indexesOut.entrySet()) {
			List<Integer> list = entry.getValue();
			int[] intIndexes = new int[list.size()];
			int i = 0;
			for (Integer x : list) {
				intIndexes[i++] = x.intValue();
			}
			indexMapOut.put(entry.getKey(), intIndexes);
		}
		
		//OUT-TAB
		indexMapTabOut = new HashMap<String, int[]>(indexes.size());
		// replace the lists of Integer objects with ints
		for (Map.Entry<String, List<Integer>> entry : indexesTabOut.entrySet()) {
			List<Integer> list = entry.getValue();
			int[] intIndexes = new int[list.size()];
			int i = 0;
			for (Integer x : list) {
				intIndexes[i++] = x.intValue();
			}
			indexMapTabOut.put(entry.getKey(), intIndexes);
		}		

		return parsedQuery.toString();
	}

	/**
	 * Returns the indexes keys array
	 * 
	 * @return parameter indexes
	 */
	public String[] getIndexes() {
		return indexMap.keySet().toArray(new String[0]);
	}
	
	/**
	 * Returns the indexes keys array
	 * 
	 * @return parameter indexes
	 */
	public String[] getIndexesOut() {
		return indexMapOut.keySet().toArray(new String[0]);
	}
	
	
	/**
	 * Returns the indexes tabs keys array
	 * 
	 * @return parameter indexes
	 */
	public String[] getIndexesTabOut() {
		return indexMapTabOut.keySet().toArray(new String[0]);
	}
		
	
	/**
	 * Returns the indexes for a parameter.
	 * 
	 * @param name
	 *            parameter name
	 * @return parameter indexes
	 * @throws IllegalArgumentException
	 *             if the parameter does not exist
	 */
	private int[] getIndexes(String name) {
		int[] indexes = indexMap.get(name);
		if (indexes == null) {
			throw new IllegalArgumentException("Parameter not found: " + name);
		}
		return indexes;
	}

	/**
	 * Returns the indexes for a parameter.
	 * 
	 * @param name
	 *            parameter name
	 * @return parameter indexes
	 * @throws IllegalArgumentException
	 *             if the parameter does not exist
	 */
	private int[] getIndexesOut(String name) {
		int[] indexes = indexMapOut.get(name);
		if (indexes == null) {
			throw new IllegalArgumentException("Parameter not found: " + name);
		}
		return indexes;
	}
	
	
	/**
	 * Sets a parameter.
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @throws SQLException
	 *             if an error occurred
	 * @throws IllegalArgumentException
	 *             if the parameter does not exist
	 * @see PreparedStatement#setObject(int, java.lang.Object)
	 */
	public void setObject(String name, Object value) throws SQLException {
		int[] indexes = getIndexes(name);
		for (int i = 0; i < indexes.length; i++) {
			statement.setObject(indexes[i], value);
		}
	}

	

	/**
	 * Sets a parameter.
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @throws SQLException
	 *             if an error occurred
	 * @throws IllegalArgumentException
	 *             if the parameter does not exist
	 * @see PreparedStatement#setObject(int, java.lang.Object)
	 */
	public void setNull(String name) throws SQLException {
		int[] indexes = getIndexes(name);
		for (int i = 0; i < indexes.length; i++) {
			statement.setNull(indexes[i], Types.VARCHAR);
		}
	}
	

	
	/**
	 * Sets a parameter.
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @throws SQLException
	 *             if an error occurred
	 * @throws IllegalArgumentException
	 *             if the parameter does not exist
	 * @see PreparedStatement#setString(int, java.lang.String)
	 */
	public void setString(String name, String value) throws SQLException {
		int[] indexes = getIndexes(name);
		for (int i = 0; i < indexes.length; i++) {
			statement.setString(indexes[i], value);
		}
	}

	/**
	 * Sets a parameter.
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @throws SQLException
	 *             if an error occurred
	 * @throws IllegalArgumentException
	 *             if the parameter does not exist
	 * @see PreparedStatement#setString(int, java.lang.String)
	 */
	public void setOutString(String name) throws SQLException {
		int[] indexes = getIndexesOut(name);
		for (int i = 0; i < indexes.length; i++) {
			statement.registerOutParameter(indexes[i], Types.NVARCHAR);
		}
	}
	
	/**
	 * Gets a value parameter.
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @throws SQLException
	 *             if an error occurred
	 * @throws IllegalArgumentException
	 *             if the parameter does not exist
	 * @see PreparedStatement#setString(int, java.lang.String)
	 */
	public String getOutString(String name) throws SQLException {
		return statement.getString(name);
	}
		
	
	/**
	 * Sets a parameter.
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @throws SQLException
	 *             if an error occurred
	 * @throws IllegalArgumentException
	 *             if the parameter does not exist
	 * @see PreparedStatement#setInt(int, int)
	 */
	public void setInt(String name, int value) throws SQLException {
		int[] indexes = getIndexes(name);
		for (int i = 0; i < indexes.length; i++) {
			statement.setInt(indexes[i], value);
		}
	}
	
	/**
	 * Sets a parameter.
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @throws SQLException
	 *             if an error occurred
	 * @throws IllegalArgumentException
	 *             if the parameter does not exist
	 * @see PreparedStatement#setInt(int, int)
	 */
	public void setOutInt(String name) throws SQLException {
		int[] indexes = getIndexesOut(name);
		for (int i = 0; i < indexes.length; i++) {
			statement.registerOutParameter(indexes[i], Types.INTEGER);
		}
	}	
	
	/**
	 * Gets a parameter.
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @throws SQLException
	 *             if an error occurred
	 * @throws IllegalArgumentException
	 *             if the parameter does not exist
	 * @see PreparedStatement#setInt(int, int)
	 */
	public int getOutInt(String name) throws SQLException {
		return statement.getInt(name);
	}	

	/**
	 * Sets a parameter.
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @throws SQLException
	 *             if an error occurred
	 * @throws IllegalArgumentException
	 *             if the parameter does not exist
	 * @see PreparedStatement#setInt(int, int)
	 */
	public void setLong(String name, long value) throws SQLException {
		int[] indexes = getIndexes(name);
		for (int i = 0; i < indexes.length; i++) {
			statement.setLong(indexes[i], value);
		}
	}


	/**
	 * Sets a parameter.
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @throws SQLException
	 *             if an error occurred
	 * @throws IllegalArgumentException
	 *             if the parameter does not exist
	 * @see PreparedStatement#setInt(int, int)
	 */
	public void setOutLong(String name) throws SQLException {
		int[] indexes = getIndexesOut(name);
		for (int i = 0; i < indexes.length; i++) {
			statement.registerOutParameter(indexes[i], Types.BIGINT);
		}
	}	
	
	
	/**
	 * Gets a parameter.
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @throws SQLException
	 *             if an error occurred
	 * @throws IllegalArgumentException
	 *             if the parameter does not exist
	 * @see PreparedStatement#setInt(int, int)
	 */
	public long getOutLong(String name) throws SQLException {
		return statement.getLong(name);
	}

	
	/**
	 * Sets a parameter.
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @throws SQLException
	 *             if an error occurred
	 * @throws IllegalArgumentException
	 *             if the parameter does not exist
	 */
	public void setDecimal(String name, BigDecimal value) throws SQLException {
		int[] indexes = getIndexes(name);
		for (int i = 0; i < indexes.length; i++) {
			statement.setBigDecimal(indexes[i], value);
		}
	}
	
	
	/**
	 * Sets a parameter.
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @throws SQLException
	 *             if an error occurred
	 * @throws IllegalArgumentException
	 *             if the parameter does not exist
	 */
	public void setOutDecimal(String name) throws SQLException {
		int[] indexes = getIndexesOut(name);
		for (int i = 0; i < indexes.length; i++) {
			statement.registerOutParameter(indexes[i], Types.DECIMAL);
		}
	}
	
	/**
	 * Gets a parameter.
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @throws SQLException
	 *             if an error occurred
	 * @throws IllegalArgumentException
	 *             if the parameter does not exist
	 */
	public BigDecimal getOutDecimal(String name) throws SQLException {
		return statement.getBigDecimal(name);
	}	
	
	/**
	 * Sets a parameter.
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @throws SQLException
	 *             if an error occurred
	 * @throws IllegalArgumentException
	 *             if the parameter does not exist
	 * @see PreparedStatement#setTimestamp(int, java.sql.Timestamp)
	 */
	public void setTimestamp(String name, Timestamp value) throws SQLException {
		int[] indexes = getIndexes(name);
		for (int i = 0; i < indexes.length; i++) {
			statement.setTimestamp(indexes[i], value);
		}
	}

	
	/**
	 * Sets a parameter.
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @throws SQLException
	 *             if an error occurred
	 * @throws IllegalArgumentException
	 *             if the parameter does not exist
	 * @see PreparedStatement#setTimestamp(int, java.sql.Timestamp)
	 */
	public void setOutTimestamp(String name) throws SQLException {
		int[] indexes = getIndexesOut(name);
		for (int i = 0; i < indexes.length; i++) {
			statement.registerOutParameter(indexes[i], Types.TIMESTAMP);
		}
	}
	
	
	/**
	 * Returns the underlying statement.
	 * 
	 * @return the statement
	 */
	public CallableStatement getStatement() {
		return statement;
	}


	/**
	 * Executes the statement, which must be an SQL INSERT, UPDATE or DELETE statement; or an SQL statement that returns
	 * nothing, such as a DDL statement.
	 * 
	 * @return number of rows affected
	 * @throws SQLException
	 *             if an error occurred
	 * @see PreparedStatement#executeUpdate()
	 */
	public int executeUpdate() throws SQLException {
		return statement.executeUpdate();
	}

	/**
	 * Closes the statement.
	 * 
	 * @throws SQLException
	 *             if an error occurred
	 * @see Statement#close()
	 */
	public void close() throws SQLException {
		statement.close();
	}

}
