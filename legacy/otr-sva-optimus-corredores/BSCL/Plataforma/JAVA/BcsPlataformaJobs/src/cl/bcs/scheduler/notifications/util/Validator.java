package cl.bcs.scheduler.notifications.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
	
	private static final String PATTERN_EMAIL = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	
	public static boolean emailValidate(String email) {
		 
        // Compiles the given regular expression into a pattern.
        Pattern pattern = Pattern.compile(PATTERN_EMAIL);
 
        // Match the given input against this pattern
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
 
    }

	public static boolean rutValidate(String rut) {
		 
		boolean validacion = false;
		try {
			rut = rut.toUpperCase();
			rut = rut.replace(".", "");
			rut = rut.replace("-", "");
			int rutAux = Integer.parseInt(rut.substring(0, rut.length() - 1));

			char dv = rut.charAt(rut.length() - 1);

			int m = 0, s = 1;
			for (; rutAux != 0; rutAux /= 10) {
				s = (s + rutAux % 10 * (9 - m++ % 6)) % 11;
			}
			if (dv == (char) (s != 0 ? s + 47 : 75)) {
				validacion = true;
			}

		} catch (java.lang.NumberFormatException e) {
		} catch (Exception e) {
		}
		return validacion;
	}
	
	public static List<Long> duplicateRemoveInteger(List<Long> a)
	{
	    if(a!=null){
	        Set<Long> WithoutDuplicates = new LinkedHashSet<Long>(a);
                a.clear();
                a.addAll(WithoutDuplicates);
	    }else{
	        a = new ArrayList<Long>();
	    }
		
		return a;
	}
	public static List<String> duplicateRemoveString(List<String> a)
	{
		Set<String> hs = new HashSet<>();
		hs.addAll(a);
		a.clear();
		a.addAll(hs);

		return a;
	}
	
	
}
