package cl.bcs.utils;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public interface  JavaCallExtra {

	/**
	 * Interface used to manage plugins
	 * @param conn
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public  com.google.gson.JsonObject call(java.sql.Connection conn,com.google.gson.JsonObject in,com.google.gson.JsonObject out) throws Exception;
	

	/**
	 * Load resources from path package
	 * @param path
	 * @return
	 */
	default String loadResourceToString(final String path){		
	    final InputStream stream = this.getClass().getClassLoader().getResourceAsStream(path);
	    if (stream==null) {
	    	throw new IllegalStateException("can't load resource:"+path);
	    }
	    try{
	        return IOUtils.toString(stream);
	    } catch(final Exception e){
	        throw new IllegalStateException(e);
	    } finally{
	        IOUtils.closeQuietly(stream);
	    }
	}	 
	
	/**
	 * Load default gson format
	 * @return
	 */
	default Gson getGsonBuilder() {
		GsonBuilder gbuild=new GsonBuilder();
		gbuild.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		gbuild.registerTypeAdapter(java.sql.Timestamp.class, new GsonTimestampDeserializer());
		gbuild.registerTypeAdapter(java.sql.Date.class, new GsonDateDeserializer());
		Gson gson = gbuild.serializeSpecialFloatingPointValues().serializeNulls().create();		
		return gson;
	}
}
