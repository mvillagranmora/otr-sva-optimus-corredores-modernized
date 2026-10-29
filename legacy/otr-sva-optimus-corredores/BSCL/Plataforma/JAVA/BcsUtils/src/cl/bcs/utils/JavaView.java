package cl.bcs.utils;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonObject;

public interface JavaView {

	/**
	 * Interface used to manage plugins
	 * @param conn
	 * @param filters TODO
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public JsonObject call(java.sql.Connection conn, JsonObject jin, Integer skip, Integer take, String sort, String filters) throws Exception;
	
	default <T> List<T> limitResponse(List<T> elements, Integer skip, Integer take, Class<T> type){
		if(elements!=null && !elements.isEmpty()) {
			if(skip!=null && take!=null) {
				Integer total = skip + take;
				if(elements.size()>= total) {
					return  elements.subList(skip, total);	
				}else if(elements.size()> skip) {
					return elements.subList(skip, elements.size());
				}else {
					return elements.subList(0, 0);
				}
			}
		}
		return elements;
	}
	/**
	 * UNIMPLEMENTED
	 * @param jin
	 * @param filters
	 * @return
	 */
	default JsonObject filterResult(JsonObject jin, String filters){
		
		return null;
	}
	

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
}
