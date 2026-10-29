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
 * gsonBuilder.registerTypeAdapter(java.sql.Timestamp.class, new GsonTimestampDeserializer());
 * @author cstange
 *
 */
public class GsonTimestampDeserializer implements JsonDeserializer<java.sql.Timestamp > {
	  @Override
	  public java.sql.Timestamp deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
	      String date = element.getAsString();
	      SimpleDateFormat format;
	      if (date.length()==10) format = new SimpleDateFormat("yyyy-MM-dd");
	      else if (date.length()==19) format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	      else format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	      try {
	          return new java.sql.Timestamp(format.parse(date).getTime());
	      } catch (ParseException exp) {	    	  
	          System.err.println("Failed to parse Date:" + exp.getMessage());
	          return null;
	      }
	   }
}
