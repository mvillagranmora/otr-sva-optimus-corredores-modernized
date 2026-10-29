package cl.bcs.utils;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;

import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import datadog.trace.api.Trace;

/**
 * * Small class that makes it easy to execute sql statements
 * Support collections for DML and Procedures.  
 * * @author kaiser  
 */
public class SQL   {
    
    private Long timePrepareStatament=null;
    private Long timeExecuteStatament=null; 
    private Long timeTotalStatament=null;
    private Long timeParseStatement=null;
    public static boolean CASE_SENSITIVE = true;
    static final Logger logger = LogManager.getLogger(SQL.class);
        
    /**
     * Constructor for SQL.
     */
    public SQL() {
        super();
    }
    
    //Helpers para query tipo orderby
    private final String SQL_ORDERBY = "SELECT * FROM (%s) ORDER BY \"%s\" %s";
    private String getOrderBy(String sql, String sort) throws SQLException {
        if(sort==null || sort.isEmpty() || sort.split("::").length!=2) {
            logger.error("Invalid order by, format: field::dir");
            return sql;
        }
        return String.format(SQL_ORDERBY, sql.replaceAll(";", ""),sort.split("::")[0], sort.split("::")[1]);
    }
    
    //helpers para query tipo limit
    private final String SQL_LIMIT = "%s LIMIT %d OFFSET %d";
    private String getLimit(String sql, Integer take, Integer skip) {
        if(take==null || skip==null) {
            logger.error(" Invalid limit or take, must be numbers");
            return sql;
        }
        return String.format(SQL_LIMIT, sql.replaceAll(";", ""), take, skip);
    }
    
    //helpers para query tipo count
    private final String SQL_COUNT = "SELECT COUNT(1) AS \"Contador\" FROM (%s)";
    public static class Count{
        public Long Contador;
        public Count() {
            this.Contador=-1L;
        }
    }

    /**
     * Identifica dinamicamente si la conexion actual es hacia PostgreSQL.
     */
    public static boolean isPostgresConnection(Connection conn) {
        try {
            return conn.getMetaData().getDatabaseProductName().toLowerCase().contains("postgresql");
        } catch (SQLException e) {
            logger.warn("No se pudo inferir el dialecto de la base de datos. Se asume HANA por defecto.", e);
            return false;
        }
    }

    /**
     * Capa JIT de Traducción de Dialecto: 
     * Reforzada para el manejo de Nulos y Secuencias.
     */
    public static String translateToPostgresDialect(String sql) {
        if (sql == null) return null;
        String pgSql = sql;

        // 1. Traducción de IFNULL -> COALESCE
        // PostgreSQL utiliza COALESCE para manejar valores nulos de forma estándar
        pgSql = pgSql.replaceAll("(?i)\\bIFNULL\\s*\\(", "COALESCE(");

        // 2. Traducción de Secuencias: "Esquema::Secuencia".NEXTVAL -> nextval('"Esquema::Secuencia"')
        pgSql = pgSql.replaceAll("(?i)\"([^\"]+)\"\\.NEXTVAL", "nextval('\"$1\"')");

        // 3. Traducción de TO_VARCHAR a TO_CHAR
        pgSql = pgSql.replaceAll("(?i)\\bTO_VARCHAR\\s*\\(", "TO_CHAR(");

        // 4. Traducción de TO_INT -> CAST(... AS INTEGER)
        pgSql = pgSql.replaceAll("(?i)\\bTO_INT\\s*\\((.*?)\\)", "CAST($1 AS INTEGER)");

        // 5. Traducción de ADD_SECONDS -> Aritmética de Intervalos
        pgSql = pgSql.replaceAll("(?i)\\bADD_SECONDS\\s*\\(([^,]+),(.+?)\\)", "($1 + (interval '1 second' * ($2)))");

        // 6. Constantes de tiempo y Limpieza de DUMMY
        pgSql = pgSql.replaceAll("(?i)\\bCURRENT_UTCTIMESTAMP\\b", "(CURRENT_TIMESTAMP AT TIME ZONE 'UTC')");
        pgSql = pgSql.replaceAll("(?i)\\bFROM\\s+DUMMY\\b", "");

        return pgSql;
    }

    /**
     * Time in milliseconds
     * @return milliseconds
     */
    public long getTimePrepareStatament() {
        return timePrepareStatament;
    }

    /**
     * Time in milliseconds
     * @return milliseconds
     */
    public long getTimeExecuteStatament() {
        return timeExecuteStatament;
    }

    /**
     * Time in milliseconds
     * @return milliseconds
     */
    public long getTimeTotalStatament() {
        return timeTotalStatament;
    }

    
    /**
     * Execute a batch of SQL INSERT, UPDATE, or DELETE queries.
     *
     * @param <T> type
     * @param conn The Connection to use to run the query.  The caller is
     * responsible for closing this Connection.
     * @param sql The SQL to execute.
     * @param params Object parameters
     * @param type Type used to map objects
     * @return List with objects.
     * @throws SQLException if a database access error occurs
     */
    @Trace
    public <T> List<T> query(Connection conn, String sql,HashMap<String,Object> params,Class<T> type)
            throws SQLException {
        
        if (conn == null) throw new SQLException("Null connection");
        if (sql == null) throw new SQLException("Null SQL statement");
        if (params == null) throw new SQLException("Null Class Params. If parameters aren't need, pass an empty");
        if (type == null) throw new SQLException("Null Class Type");

        logger.trace("Start query");
        logger.trace("Query " + sql);
        long initTime=System.currentTimeMillis();
        NamedParameterStatement stmt = null;
        ResultSet rst = null;
        List<T> result = new ArrayList<>();

        boolean isPostgres = isPostgresConnection(conn);

        try {          
            HashMap<String,Object> paramsT=new HashMap<>();
            String sqlWcomments = getSqlParams(sql, params, paramsT);
            
            if (isPostgres) {
                sqlWcomments = translateToPostgresDialect(sqlWcomments);
                logger.trace("Postgres JIT Translated Query: " + sqlWcomments);
            }
            
            stmt = new NamedParameterStatement(conn,sqlWcomments);
            this.timePrepareStatament=System.currentTimeMillis()-initTime;   
            
            for (Map.Entry<String, Object> entry : paramsT.entrySet()) {
                if ( entry.getValue()==null) stmt.setNull(entry.getKey());
                else stmt.setObject(entry.getKey(), entry.getValue());
            }
            
            rst=stmt.executeQuery();
            RowSetFactory factory = RowSetProvider.newFactory();
            CachedRowSet rowset = factory.createCachedRowSet();
             
            rowset.populate(rst);
            closeQuietly(rst);
            long size = rowset.size() * rowset.getMetaData().getColumnCount();
            if (size > 1000000) {
                logger.warn("rowset too big:" + size);
                logger.warn(sqlWcomments);
                logger.warn(params.toString());
            }
            
            this.timeExecuteStatament=System.currentTimeMillis()-this.timePrepareStatament - initTime;
            
            if (rowset.next()) {                
                ResultSetMetaData rsmd = rowset.getMetaData();
                int cols=rsmd.getColumnCount();
                do  {
                    T obj=type.newInstance();
                    result.add(obj);
                    for (int i=1;i<=cols;i++) {
                        String colN=rsmd.getColumnLabel(i);  
                        Field field=null;
                        try {
                            field=obj.getClass().getField(colN);
                        }catch (Exception er) {
                            field=null;
                        }
                        if (field==null && CASE_SENSITIVE==false) {
                            try { field=obj.getClass().getField(colN.toLowerCase()); }
                            catch (Exception er) { field=null; }                  
                        }
                        if (field==null && CASE_SENSITIVE==false) {
                            try { field=obj.getClass().getField(colN.toUpperCase()); }
                            catch (Exception er) { field=null; }                  
                        }
                        
                        if (field!=null) {
                            Object val=rowset.getObject(i);
                            
                            if (isPostgres) {
                                mapStrictPostgresField(obj, field, val);
                            } else {
                                mapLenientHanaField(obj, field, val);
                            }
                        }                       
                    }                                           
                }while (rowset.next());             
            }
            
            this.timeParseStatement=System.currentTimeMillis()-this.timeExecuteStatament - initTime;
            
        } catch (InstantiationException | IllegalAccessException e) {
            throw new SQLException(e.getMessage(),e);       
        } catch (SQLException e) {
            this.rethrow(e, sql, params);
        } finally {
            if (stmt!=null && stmt.getStatement()!=null) {
                closeQuietly(stmt.getStatement());
            }
        }
        this.timeTotalStatament=System.currentTimeMillis()-initTime;
        if (this.getTimeTotalStatament() > 2000) {
            logger.trace("lento");
            logger.debug("SQL:" + sql);
            logger.debug("Query total time " + this.getTimeTotalStatament());
            logger.debug("Query prep time " + this.timePrepareStatament);
            logger.debug("Query exec time " + this.timeExecuteStatament);
            logger.debug("Query parse time " + this.timeParseStatement);
        }
        return result;
    }
    
    /**
     * Mapeador indulgente para SAP HANA (Legado)
     */
    private <T> void mapLenientHanaField(T obj, Field field, Object val) throws IllegalAccessException, SQLException {
        if (val instanceof SerialBlob) {
            SerialBlob b = (SerialBlob)val;
            if (b.length() == 0) field.set(obj, new byte[0]);
            else field.set(obj, b.getBytes(1,(int) b.length()));
        }
        else if (val instanceof SerialClob) {
            SerialClob b = (SerialClob)val;
            if (b.length() == 0) field.set(obj, "");
            else field.set(obj, b.getSubString(1,(int)b.length()));
        } 
        else {
            field.set(obj, val);    
        }
    }

    private <T> void mapStrictPostgresField(T obj, Field field, Object val) throws IllegalAccessException, SQLException {
        if (val == null) {
            if (!field.getType().isPrimitive()) {
                field.set(obj, null);
            }
            return;
        }

        if (val instanceof SerialBlob) {
            SerialBlob b = (SerialBlob) val;
            if (b.length() == 0) field.set(obj, new byte[0]);
            else field.set(obj, b.getBytes(1, (int) b.length()));
            return;
        }
        
        if (val instanceof SerialClob) {
            SerialClob b = (SerialClob) val;
            if (b.length() == 0) field.set(obj, "");
            else field.set(obj, b.getSubString(1, (int) b.length()));
            return;
        }

        Class<?> fieldType = field.getType();
        
        if (val instanceof Number) {
            Number num = (Number) val;
            if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
                val = num.longValue();
            } else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
                val = num.intValue();
            } else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
                val = num.doubleValue();
            } else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
                val = num.floatValue();
            } else if (fieldType.equals(Short.class) || fieldType.equals(short.class)) {
                val = num.shortValue();
            } else if (fieldType.equals(BigDecimal.class)) {
                val = new BigDecimal(num.toString());
            }
        } 
        // Desempaquetado de Objetos Propietarios de Postgres
        else if (val.getClass().getName().equals("org.postgresql.util.PGobject")) {
            if (fieldType.equals(String.class)) {
                val = val.toString();
            }
        }
        
        field.set(obj, val);
    }

    
    /**
     * Agrega ORDER BY a una consulta sql para ordenar por campo y direccion<br>
     * @param conn
     * @param sql
     * @param params
     * @param type
     * @param sort "campo::direccion" ej: FolioCartera::ASC
     * @return
     * @throws SQLException
     */
    @Trace
    public <T> List<T> query(Connection conn, String sql,HashMap<String,Object> params,Class<T> type, String sort) throws SQLException{
        return query(conn, getOrderBy(sql, sort), params, type);
    }
        
    /**
     * Agrega LIMIT a la consulta SQL para obtener un rango de los datos de la consulta<br>
     * EJ: <br>
     * - take 200, skip 0 traera los primeros 200 datos de la consulta<br>
     * - take 200, skip 200 obtentra los 200 registros posteriores a los primeros 200.<br>
     * @param conn
     * @param sql
     * @param params
     * @param type
     * @param take cantidad de datos a obtener por la consulta
     * @param skip cantidad de registros que se debe saltar para la consulta
     * @return
     * @throws SQLException
     */
    @Trace
    public <T> List<T> query(Connection conn, String sql,HashMap<String,Object> params,Class<T> type, Integer take, Integer skip) throws SQLException{      
        return query(conn, getLimit(sql, take, skip), params, type);
    }
    
    /**
     * Agrega order by y limit a una consulta sql, permite ordenar y restringir la cantidad de registros.<br>
     * EJ: <br>
     * - take 200, skip 0, sort FolioCartera:desc traera los primeros 200 datos de la consulta ordenados por FolioCartera de forma descendiente<br>
     * - take 200, skip 200, sort FolioCartera:asc obtentra los 200 registros posteriores a los primeros 200 ordenados por FolioCartera de forma ascendiente.<br>
     * @param conn
     * @param sql
     * @param params
     * @param type
     * @param take take cantidad de datos a obtener por la consulta
     * @param skip cantidad de registros que se debe saltar para la consulta
     * @param sort "campo::direccion" ej: FolioCartera::ASC
     * @return
     * @throws SQLException
     */
    @Trace
    public <T> List<T> query(Connection conn, String sql,HashMap<String,Object> params,Class<T> type, Integer take, Integer skip, String sort) throws SQLException{
        return query(conn, getLimit(getOrderBy(sql, sort), take, skip), params, type);
    }
   
    /**
     * Metodo utilitario para obtener la cantidad de registros totales de una query.
     * @param conn
     * @param sql
     * @param params
     * @param type
     * @return Long con la cantidad de registros.
     * @throws SQLException
     */
    @SuppressWarnings("rawtypes")
    @Trace
    public Long countQuery(Connection conn, String sql, HashMap<String, Object> params) throws SQLException {
        return query(conn, String.format(SQL_COUNT, sql.replaceAll(";", "")), params, Count.class).stream().findFirst().orElse(new Count()).Contador;
    }
    
    /**
     * * EN FASE BETA!!
     * NO SOPORTA SELECT <NOMBRE_COLUMNA> AS <NUEVO_NOMBRE_COLUMNA>
     * */
    @Trace
    public <T> List<T> queryParallel(Connection conn, String sql,HashMap<String,Object> params,Class<T> type)
            throws SQLException {
        
        if (conn == null) throw new SQLException("Null connection");
        if (sql == null) throw new SQLException("Null SQL statement");
        if (params == null) throw new SQLException("Null Class Params. If parameters aren't need, pass an empty");
        if (type == null) throw new SQLException("Null Class Type");

        logger.trace("Start query");
        logger.trace("Query " + sql);
        long initTime=System.currentTimeMillis();
        NamedParameterStatement stmt = null;
        List<T> result = new ArrayList<>();

        boolean isPostgres = isPostgresConnection(conn);

        try {          
            HashMap<String,Object> paramsT=new HashMap<>();
            String sqlWcomments = getSqlParams(sql, params, paramsT);
            
            if (isPostgres) {
                sqlWcomments = translateToPostgresDialect(sqlWcomments);
            }
            
            stmt = new NamedParameterStatement(conn,sqlWcomments);
            this.timePrepareStatament=System.currentTimeMillis()-initTime;   
                        
            for (Map.Entry<String, Object> entry : paramsT.entrySet()) {
                if ( entry.getValue()==null) stmt.setNull(entry.getKey());
                else stmt.setObject(entry.getKey(), entry.getValue());
            }
            logger.trace("Init execute Query");
            try(ResultSet rst=stmt.executeQuery();){
                logger.trace("Execute Query took ("+(System.currentTimeMillis()-initTime)+")");
                
                RowSetDynaClass rsdc = new RowSetDynaClass(rst, false);
                //TODO - Posible bug al usar AS en una query,
                List<DynaProperty> properties = Arrays.asList(rsdc.getDynaProperties());
                long size = properties.size() * rsdc.getRows().size();
                if (size > 1000000) {
                    logger.warn("rowset too big:" + size);
                    logger.warn(sqlWcomments);
                    logger.warn(params.toString());
                }
                result = rsdc.getRows().parallelStream().map(row -> {
                    T obj = null;
                    try {
                        obj = type.newInstance();
                        for(DynaProperty prop : properties) {
                            Field field = null;
                            try {
                                field = obj.getClass().getField(prop.getName());
                                if (field!=null) {
                                    Object val= row.get(prop.getName());
                                    
                                    if (isPostgres) {
                                        mapStrictPostgresField(obj, field, val);
                                    } else {
                                        mapLenientHanaField(obj, field, val);
                                    } 
                                }  
                            } catch (NoSuchFieldException | SecurityException | SQLException e) {
                                logger.error(e);
                            }
                        }
                    } catch (InstantiationException | IllegalAccessException e) {
                        logger.debug(e);
                    }
                    return obj;
                }).collect(Collectors.toList());
            }
            
            this.timeExecuteStatament=System.currentTimeMillis()-initTime-this.timePrepareStatament;

        } catch (SQLException e) {
            this.rethrow(e, sql, params);
        } finally {
            if (stmt!=null && stmt.getStatement()!=null) {
                closeQuietly(stmt.getStatement());
            }
        }
        this.timeTotalStatament=System.currentTimeMillis()-initTime;
        logger.debug("Query total time " + this.getTimeTotalStatament());
        return result;
    }

    private String getSqlParams(String sql, HashMap<String, Object> params, HashMap<String, Object> paramsT) {
        String sqlWcomments = sql.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)","");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if ( entry.getValue() instanceof ArrayList) {
                int items=0;
                List<Object> list=(List<Object>)entry.getValue();
                String newItems="";
                if (list.isEmpty()) {      
                    String nkey=entry.getKey()+"_"+items++;
                    newItems=":"+nkey;
                    paramsT.put(nkey,null);
                }
                else {
                    for (Object obj : list) {
                        String nkey=entry.getKey()+"_"+items++;
                        newItems+=":"+nkey+",";
                        paramsT.put(nkey,obj);                      
                    }
                    newItems=newItems.substring(0, newItems.length()-1);
                }                                           
                sqlWcomments=sqlWcomments.replace(":"+entry.getKey(), newItems);
            }else {
                paramsT.put(entry.getKey(), entry.getValue());
            }
        }
        return sqlWcomments;
    }
            
    /**
     * Execute a batch of SQL INSERT, UPDATE, or DELETE queries.
     *
     * @param conn The Connection to use to run the query.  The caller is
     * responsible for closing this Connection.
     * @param sql The SQL to execute.
     * @param params An List of query replacement parameters.  Each row in
     * this Bean is one set of batch replacement values.
     * @param type Type used to map objects
     * @return The number of rows updated per statement.
     * @throws SQLException if a database access error occurs
     */     
    @Trace
    public  <T> int[] batch(Connection conn, String sql, List<T> params,Class<T> type) throws SQLException {
        if (conn == null) throw new SQLException("Null connection");
        if (sql == null) throw new SQLException("Null SQL statement");
        if (params == null) throw new SQLException("Null parameters. If parameters aren't need, pass an empty");
        if (type == null) throw new SQLException("Null Class Type.");       

        logger.trace("Start Batch, params size:" + params.size());
        
        long initTime=System.currentTimeMillis();
        NamedParameterStatement stmt = null;
        int[] rows = null;
        try {
            String sqlWcomments = sql.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)","");
            
            if (isPostgresConnection(conn)) {
                sqlWcomments = translateToPostgresDialect(sqlWcomments);
            }
            
            stmt = new NamedParameterStatement(conn,sqlWcomments);
            this.timePrepareStatament=System.currentTimeMillis()-initTime; 
            String[] props=stmt.getIndexes();
            
            for (int i = 0; i < params.size(); i++) {
                try {
                    T pObj=type.cast(params.get(i));
                    for (int k=0;k<props.length;k++) {
                        Field field=pObj.getClass().getField(props[k]);
                        if (field==null) throw new SQLException("Object field unknow "+props[k]);                   
                        Object val=field.get(pObj);
                        if ( val==null) {
                            stmt.setNull(props[k]);
                        }
                        else {
                            if (field.getType().isAssignableFrom(BigDecimal.class)) {
                                val = ((BigDecimal)val).setScale(16, RoundingMode.HALF_DOWN);
                            }
                            
                            if (field.getType().isAssignableFrom(InputStream.class)) {
                                stmt.setBinaryStream(props[k], (InputStream) val);  
                            }
                            else {
                                stmt.setObject(props[k], val);  
                            }
                        }
                    }
                }catch (Exception er) {
                     throw new SQLException("Map Object failed",er);
                }
                stmt.addBatch();
            }
            
            if (props.length == 0 && params.isEmpty()) {
                stmt.addBatch();
            }
            logger.trace("Prepare statement end");
            
            rows = stmt.executeBatch();
            
            this.timeExecuteStatament=System.currentTimeMillis()-initTime-this.timePrepareStatament;
            
        } catch (SQLException e) {
        	e.printStackTrace();
            this.rethrow(e, sql, params.toArray());
        } finally {
            if (stmt!=null && stmt.getStatement()!=null) {
                closeQuietly(stmt.getStatement());
            }
        }
        this.timeTotalStatament=System.currentTimeMillis()-initTime;
        logger.trace("Batch total time " + this.getTimeTotalStatament());
        return rows;
    }   
    
    /**
     * * @param conn
     * @param sql
     * @return
     * @throws SQLException 
     */
    @Trace
    public final static boolean exec(Connection conn, String sql) throws SQLException {
        if (conn == null) throw new SQLException("Null connection");
        if (sql == null) throw new SQLException("Null SQL statement");            
        
        try (Statement st=conn.createStatement();) {
            return st.execute(sql);
        }
    }
    
    /**
     * Execute a procedure in database.
     *
     * @param conn The Connection to use to run the query.  The caller is
     * responsible for closing this Connection.
     * @param sql The SQL to execute.
     * @param paramsIn params Object parameters
     * @param paramsOut Hash with Class Type used to map objects with resultsets
     * @return Hashmap with scalar and resultsets.     
     * @throws SQLException if a database access error occurs
     */ 
    @Trace
    public HashMap<String,Object> call(Connection conn, String sql,HashMap<String,Object> paramsIn,HashMap<String,Class> paramsOut) 
            throws SQLException {
        if (conn == null) throw new SQLException("Null connection");
        if (sql == null) throw new SQLException("Null SQL statement");
        if (paramsIn == null) throw new SQLException("Null Class Parameter IN. If parameters aren't need, pass an empty");
        if (paramsOut == null) throw new SQLException("Null Class Parameter OUT. If parameters aren't need, pass an empty");

        long initTime=System.currentTimeMillis();
        NamedCallableStatement stmt = null;
        HashMap<String,Object> result = new HashMap<String,Object>();

        boolean isPostgres = isPostgresConnection(conn);

        try {          
            String sqlWcomments = sql.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)","");
            if (isPostgres) {
                sqlWcomments = translateToPostgresDialect(sqlWcomments);
            }

            stmt = new NamedCallableStatement(conn,sqlWcomments);
            
            this.timePrepareStatament=System.currentTimeMillis()-initTime; 
            
            for (String pIn:stmt.getIndexes()) {
                if (paramsIn.containsKey(pIn)==false ||  paramsIn.get(pIn)==null) stmt.setNull(pIn);
                else stmt.setObject(pIn, paramsIn.get(pIn));
            }

            for (String pOut:stmt.getIndexesOut()) {                        
                if (paramsOut.containsKey(pOut)==false ||  paramsOut.get(pOut)==null) 
                    throw new SQLException("Parameter out defined as statement but not as class property");             
                else  {
                    String  typeOut=paramsOut.get(pOut).getTypeName();       
                    if (typeOut.equals("java.lang.String")) stmt.setOutString(pOut);
                    else if (typeOut.equals("java.math.BigDecimal")) stmt.setOutDecimal(pOut);
                    else if (typeOut.equals("java.lang.Long")) stmt.setOutLong(pOut);           
                    else throw new SQLException("Class Type "+typeOut+" for "+paramsOut+" not implemented");                 
                }
            }           
            
            HashMap<String,String> TabsOut     =new HashMap<String,String>();
            for (String pOut:stmt.getIndexesTabOut()) {                             
                if (paramsOut.containsKey(pOut)==false ||  paramsOut.get(pOut)==null) 
                    throw new SQLException("Parameter out defined as statement but not as class property"); 
                else 
                    TabsOut.put(pOut.toUpperCase(), pOut);              
            }               
            
            stmt.executeUpdate();   
            //Read Scalar
            for (String pOut:stmt.getIndexesOut()) {    
                String  typeOut=paramsOut.get(pOut).getTypeName();       
                if (typeOut.equals("java.lang.String"))   result.put(pOut, stmt.getOutString(pOut));
                else if (typeOut.equals("java.math.BigDecimal")) result.put(pOut, stmt.getOutDecimal(pOut));
                else if (typeOut.equals("java.lang.Long")) result.put(pOut, stmt.getOutLong(pOut));             
                else throw new SQLException("Class Type "+typeOut+" for "+paramsOut+" not implemented");                    
            }
            
            //Read Tables
            int resultCount=0;
            do {                    
                try (ResultSet rs = stmt.getStatement().getResultSet();) {
                    if (rs==null) break;
                    ResultSetMetaData mt = rs.getMetaData();
                    String propName="ResultSet_"+resultCount++;
                    String tabName=mt.getTableName(1);
                    if (tabName.length() == 0) {
                        tabName = propName;
                    }
                    tabName = tabName.substring(0,tabName.length() -2);
                    if (tabName.length()>0 && tabName.contains("_")) {
                        int    trimposI=0,trimposF=0;
                        if (tabName.startsWith("_SYS_")) {                                          
                            trimposI=tabName.indexOf("_",19)+1;
                            trimposF=tabName.lastIndexOf("_");                                              
                        }else {
                            trimposI=0;
                            trimposF=tabName.lastIndexOf("_");                                                                          
                        }
                        
                        String xtabName = "";
                        
                        try {
                            xtabName=tabName.substring(trimposI,trimposF);  
                        }
                        catch(Exception e) {
                            xtabName=tabName.substring(5,tabName.length()); 
                        }
                        if (TabsOut.containsKey(xtabName.toUpperCase())) {
                            tabName=xtabName.toUpperCase();
                            propName=TabsOut.get(tabName);                          
                            resultCount--;
                        }
                    }                   
                    boolean             isTabDefined=TabsOut.containsKey(tabName);
                    int                 numberOfColumns= mt.getColumnCount();
                    ResultSetMetaData   rsmd=rs.getMetaData();
                    List<Object>        rows=new ArrayList<> ();
                    result.put(propName, rows);                 
                    while (rs!=null && rs.next()) {
                        //Defined table
                        if (isTabDefined) {
                            Object obj=null;            
                            obj=paramsOut.get(propName).newInstance();
                            rows.add(obj);
                            for (int i=1;i<=numberOfColumns;i++) {
                                String colN=rsmd.getColumnName(i);  
                                Field field=null;
                                try {
                                     field=obj.getClass().getField(colN);
                                }catch (Exception er) {
                                    field=null;
                                }
                                if (field!=null) {
                                    Object val=rs.getObject(i);
                                    if (isPostgres) {
                                        mapStrictPostgresField(obj, field, val);
                                    } else {
                                        mapLenientHanaField(obj, field, val);
                                    }
                                }                               
                            }                                                       
                        }
                        //Not defined table
                        else {
                            HashMap<String,Object> obj=new HashMap<>();
                            rows.add(obj);
                            for (int i = 1; i <= numberOfColumns; i++) {
                                obj.put(mt.getColumnLabel(i),rs.getObject(i));
                            }
                        }
                    }
                }
            } while (stmt.getStatement().getMoreResults()); 
            
             this.timeExecuteStatament=System.currentTimeMillis()-initTime-this.timePrepareStatament;
            
        } catch (InstantiationException | IllegalAccessException e) {
            throw new SQLException(e.getMessage(),e);       
        } catch (SQLException e) {
            this.rethrow(e, sql, paramsIn);
        } finally {
            if (stmt!=null && stmt.getStatement()!=null) {
                closeQuietly(stmt.getStatement());
            }
        }
        this.timeTotalStatament=System.currentTimeMillis()-initTime;
        return result;      
    }   
    
    protected void rethrow(SQLException cause, String sql, Object... params)throws SQLException {
        String causeMessage = cause.getMessage();
        if (cause.getCause() != null)
            causeMessage = causeMessage  + " " + cause.getCause().getMessage();   
        StringBuffer msg = new StringBuffer(causeMessage);

        msg.append(" Query: ");
        msg.append(sql);
        msg.append(" Parameters: ");

        if (params == null) {
            msg.append("[]");
        } else {
            msg.append(Arrays.deepToString(params));
        }

        SQLException e = new SQLException(msg.toString(), cause.getSQLState(),cause.getErrorCode());
        e.setNextException(cause);
        
        throw e;
    }   
    
    public static void closeQuietly(ResultSet rs) {
        try {
            if (rs!=null) {
                rs.close();
            }
        } catch (SQLException e) { // NOPMD
            // quiet
        }
    }   
    
    public static void closeQuietly(Statement stmt) {
        try {
            if (stmt!=null) {
                stmt.close();
            }
        } catch (SQLException e) { // NOPMD
            // quiet
        }
    }   
}