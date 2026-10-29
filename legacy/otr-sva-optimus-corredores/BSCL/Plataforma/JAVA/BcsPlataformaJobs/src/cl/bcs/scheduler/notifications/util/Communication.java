package cl.bcs.scheduler.notifications.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.google.gson.Gson;



public class Communication {
	
	public static void main(String[] args)
	{
		try {
			EvalTemplate c = new EvalTemplate();
			String context = "{\"data\":{\"TagGenerales\":{\"Footer\":\"<p><span style='font-size:8.0pt;font-family:'Arial',sans-serif;color:#4d4d4d'>La Bolsa 64, Santiago, Chile</span></p> <p><strong><a href='http://www.bolsadesantiago.com/' target='_blank'>www.bolsadesantiago.com</a></strong></p><p><img alt='' src='https://fbcdn-photos-b-a.akamaihd.net/hphotos-ak-xfp1/v/t1.0-0/p200x200/13606907_10209971355221972_3084385476925284781_n.jpg?oh=da2414ca5332aa51fd96199e890d8702&amp;oe=582760D0&amp;__gda__=1475021609_faa8ae56174d768b31c9bfd7638b70a1' style='width: 144px; height: 54px;' /></p><p>(es) La información contenida en este correo electrónico es para el uso exclusivo de sus destinatarios. Su contenido puede ser confidencial y cualquier uso no autorizado está estrictamente prohibido. Si usted ha recibido esta comunicación por error, le rogamos notificar de inmediato al remitente y eliminar todas las copias que posea. (en) The information contained in this e-mail is intended for the exclusive use of the individual named above. The contents may be confidential and any unauthorized use of whatever kind is strictly prohibited. If you have received this communication in error, please notify the sender immediately and delete all copies of it.  </p>\",\"Cabecera\":\"<p style='text-align: center;'><img alt='' src='https://fbcdn-photos-b-a.akamaihd.net/hphotos-ak-xfp1/v/t1.0-0/p200x200/13606907_10209971355221972_3084385476925284781_n.jpg?oh=da2414ca5332aa51fd96199e890d8702&amp;oe=582760D0&amp;__gda__=1475021609_faa8ae56174d768b31c9bfd7638b70a1' style='width: 500px; height: 117px;' /></p>\",\"Confirmar\":\"<#macro CONFIRMACION> <a href='::URL::'> <#nested> </a></#macro>\",\"Codigo\":\"Para acceder ingrese los últimos 4 digitos de su rut\"},\"CIRCUST\":{\"Apellido\":\"Espinoza\"},\"INGRESOORDEN\":{\"IdOrden\":\"10256\",\"FechaIngreso\":\"2016-07-08 09:30:00\"},\"INGRESOTRANSACCION\":{\"IdTransaccion\":\"10101\",\"FechaIngreso\":\"2016-07-08 09:30:00\"},\"CUMPLEANIOS\":{\"Nombre\":\"Exequiel\",\"Apellido\":\"Espinoza\"},\"NOTICUCLI\":{\"Glosa\":\"Esto es una glosa\"},\"CREACION_EVENTO_AUTORIZACION\":{\"Autorizacion\":\"DEFAULT\",\"Glosa\":\"Aqui va la glosa\",\"HoraRegistro\":\"01-01-2016 00:00:00\",\"HoraVigencia\":\"01-01-2016 01:00:00\"},\"DEMO\":{\"TAG1\":\"VALOR1\",\"TAG2\":\"VALOR2\",\"TAG3\":\"VALOR3\"}}}";
			String template = "<#macro CONFIRMACION> <a href='http://161.238.34.90:9025/BSCL/Plataforma/GWJAVA/PORTAL/validate?token=B3E3A4DEDE31F852B4D47D04BC11EBF6784522492F6E906C62C5B9F6A9974746'> <#nested> </a></#macro>&lt;p&gt;== DEMO PLANTILA ==&lt;/p&gt;&lt;p&gt;ESTO ES UN TILDE: &iacute;&ntilde;&lt;/p&gt;&lt;p&gt;&nbsp;&lt;/p&gt;&lt;p&gt;${data.DEMO.TAG1!}&lt;br /&gt;&lt;/p&gt;&lt;p&gt;&nbsp;&lt;/p&gt;&lt;p&gt;${data.DEMO.TAG2!}&lt;br /&gt;&lt;br /&gt;&lt;br /&gt;${data.DEMO.TAG3!}&lt;/p&gt;&lt;p&gt;&lt;@CONFIRMACION&gt; AQUI &lt;/@CONFIRMACION&gt;&lt;/p&gt;&lt;p&gt;== FIN DEMO PLANTILLA ==&lt;/p&gt;";
			
			System.out.println(EvalTemplate.eval(context,template));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	 public static String excutePost(String token) throws UnsupportedEncodingException
	  {
		 String urlParameters =
			        "campos=" + URLEncoder.encode(token, "UTF-8");
		String targetUrl = "http://localhost:8085/BSCL/Plataforma/GWJAVA/PLA/template?";
	    URL url;
	    HttpURLConnection connection = null;  
	    try {
	      //Create connection
	      url = new URL(targetUrl);
	      connection = (HttpURLConnection)url.openConnection();
	      connection.setRequestMethod("POST");
	      connection.setRequestProperty("Content-Type", 
	           "application/x-www-form-urlencoded");
				
	      connection.setRequestProperty("Content-Length", "" + 
	               Integer.toString(urlParameters.getBytes().length));
	      connection.setRequestProperty("Content-Language", "en-US");  
				
	      connection.setUseCaches (false);
	      connection.setDoInput(true);
	      connection.setDoOutput(true);

	      //Send request
	      DataOutputStream wr = new DataOutputStream (
	                  connection.getOutputStream ());
	      wr.writeBytes (urlParameters);
	      wr.flush ();
	      wr.close ();

	      //Get Response	
	      InputStream is = connection.getInputStream();
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	      String line;
	      StringBuffer response = new StringBuffer(); 
	      while((line = rd.readLine()) != null) {
	        response.append(line);
	        response.append('\r');
	      }
	      rd.close();
	      return response.toString();

	    } catch (Exception e) {

	      e.printStackTrace();
	      return null;

	    } finally {

	      if(connection != null) {
	        connection.disconnect(); 
	      }
	    }
	  }
	
 
	

}
