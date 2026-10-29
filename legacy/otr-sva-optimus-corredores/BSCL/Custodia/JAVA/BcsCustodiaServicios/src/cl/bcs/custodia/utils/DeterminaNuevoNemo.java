package cl.bcs.custodia.utils;

public class DeterminaNuevoNemo {
	
	/**
	 * Calcula el nuevo nemotecnico para "*" y "&" por pago cupon
	 * Logica migrada desde el cobol LMRUCANE.SRC
	 * @param nemoActual  Nemotecnico actual del instrumento
	 * @param fechaProceso Fecha de Proceso 
	 * @return Nuevo nemotecnico calculado
	 * @throws Exception
	 */

    public static String NuevoNemo(String nemoActual, String fechaProceso) throws Exception 
    {	
    	//REEMPLAZA *
        if ( nemoActual.indexOf("**") > -1  ) 
        {
        	return ( nemoActual.replace("**", " *") );   //Ej: ABNA12**02  >>  ABNA12 *02
        }          
        else if (nemoActual.indexOf(" *") > -1 ) 
        {
        	return ( nemoActual.replace(" *", "01") );   //Ej: ABNA12 *02  >>  ABNA120102
        } 
          
        
        //REEMPLAZA &
        else if (nemoActual.indexOf("&&") > -1 ) 
        {
        	return ( nemoActual.replace("&&", " &") );  // Ej: ABNA12&&13  >>   ABNA12 &13
        } 
        else if (nemoActual.indexOf(" &") > -1 ) 
        {	
        	
        	StringBuilder nemoNuevo = new StringBuilder(nemoActual);

        	String mesNemo = nemoActual.substring(8, 10);  // ABNA12 &13  >>  13
        	nemoNuevo.replace(6, 8, mesNemo);          // Ej: ABNA12 &13  >>   ABNA121313
        	
        	String anoProceso = fechaProceso.substring(2, 4);  // 2018-12-15  >>   18
        	String mesProceso = fechaProceso.substring(5, 7);  // 2018-12-15  >>   12
        	

        	if (Integer.parseInt(mesNemo) < Integer.parseInt(mesProceso))
        	{
        		nemoNuevo = nemoNuevo.replace(8, 10, anoProceso);    // Ej: ABNA121313  >>   ABNA121318
        	}
        	else
        	{
        		if (anoProceso.equalsIgnoreCase("00") || anoProceso.equalsIgnoreCase("01"))
        		{	
        			nemoNuevo.replace(8, 10, "99");   // Ej: ABNA121313  >>   ABNA121399
        		}
        		else
        		{
        			Integer anoNemo = Integer.parseInt(anoProceso) - 1;
        			nemoNuevo.replace(8, 10, anoNemo.toString());  // Ej: ABNA121313  >>   ABNA121317
        		}
        	}
        	
        	return nemoNuevo.toString();
        }
        
        //Retorno el mismo nemo en caso de no haber cambios 
        return nemoActual;
    }

}

