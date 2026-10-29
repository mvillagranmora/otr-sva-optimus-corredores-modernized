package cl.bcs.loaderapp.interfaces;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.IOUtils;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;

public interface DbLoader {
	
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral in, List<Object> lines);

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
