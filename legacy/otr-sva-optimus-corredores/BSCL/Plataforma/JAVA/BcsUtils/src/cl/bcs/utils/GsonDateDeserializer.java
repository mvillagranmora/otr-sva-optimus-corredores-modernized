package cl.bcs.utils;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Use:  
 * GsonBuilder gsonBuilder = new GsonBuilder();
 * gsonBuilder.registerTypeAdapter(java.sql.Date.class, new GsonDateDeserializer());
 * @author cstange
 *
 */
public class GsonDateDeserializer implements JsonDeserializer<java.sql.Date > {
	  @Override
	  public java.sql.Date deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
	      String date = element.getAsString();
	      SimpleDateFormat format;
	      if (date.length()==10) format = new SimpleDateFormat("yyyy-MM-dd");
	      else if (date.length()==19) format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	      else format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	      try {
	          return new java.sql.Date(format.parse(date).getTime());
	      } catch (ParseException exp) {	    	  
	          System.err.println("Failed to parse Date:" + exp.getMessage());
	          return null;
	      }
	   }
}
