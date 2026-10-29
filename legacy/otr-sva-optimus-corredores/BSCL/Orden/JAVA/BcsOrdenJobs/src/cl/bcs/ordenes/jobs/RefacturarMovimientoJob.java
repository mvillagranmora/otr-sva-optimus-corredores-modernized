
package cl.bcs.ordenes.jobs;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.ordenes.RefacturarMovimiento;
import cl.bcs.ordenes.RefacturarMovimiento.RefacturarMovimiento_Entrada;
import cl.bcs.ordenes.RefacturarMovimiento.Asignacion_Entrada;
import cl.bcs.ordenes.RefacturarMovimiento.CobroModificado_Entrada;
import cl.bcs.ordenes.RefacturarMovimiento.Cobro_Fijo;
import cl.bcs.ordenes.RefacturarMovimiento.Entidad_Refacturar;
import cl.bcs.ordenes.RefacturarMovimiento.RefacturarMovimiento_Salida;



public class RefacturarMovimientoJob extends BcsJob implements JavaCall{

	private final Log log = new Log("ORD.RefacturarMovimientoJob");

	private final String SQL_UPDATE_ID_PROGRAMA = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaLog\" = :RutaLog WHERE \"IdPrograma\" = :IdPrograma";
	
			
	
	
	public static class ActualizarPrograma{
		public String RutaLog;
		public Long IdPrograma;
		
		public ActualizarPrograma(String RutaLog,Long IdPrograma){
			this.RutaLog = RutaLog;
			this.IdPrograma = IdPrograma;
		}
	}
	
 

	
	
	@Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	

	public static List<Entidad_Refacturar> parseEntidades(String targetStream) throws JSONException{
        ObjectMapper mapper= new ObjectMapper();        
        List<Entidad_Refacturar> lstEntidad = new ArrayList<Entidad_Refacturar>();
        
		try 
		{
			lstEntidad = mapper.readValue(targetStream, mapper.getTypeFactory().constructCollectionType(List.class, Entidad_Refacturar.class));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return lstEntidad;	
	}
	

	
	public static List<Asignacion_Entrada> parseAsignaciones(String targetStream) throws JSONException{
        ObjectMapper mapper= new ObjectMapper();        
        List<Asignacion_Entrada> lstAsignaciones = new ArrayList<Asignacion_Entrada>();
        
		try 
		{
			lstAsignaciones = mapper.readValue(targetStream, mapper.getTypeFactory().constructCollectionType(List.class, Asignacion_Entrada.class));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return lstAsignaciones;	
	}
	
	
	
	public static List<CobroModificado_Entrada> parseCobroModificado(String targetStream) throws JSONException{
        ObjectMapper mapper= new ObjectMapper();        
        List<CobroModificado_Entrada> lstCobroModificado = new ArrayList<CobroModificado_Entrada>();
        
		try 
		{
			lstCobroModificado = mapper.readValue(targetStream, mapper.getTypeFactory().constructCollectionType(List.class, CobroModificado_Entrada.class));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return lstCobroModificado;	
	}
	
	

	public static List<Cobro_Fijo> parseCobroFijo(String targetStream) throws JSONException{
        ObjectMapper mapper= new ObjectMapper();        
        List<Cobro_Fijo> lstCobroFijo = new ArrayList<Cobro_Fijo>();
        
		try 
		{
			lstCobroFijo = mapper.readValue(targetStream, mapper.getTypeFactory().constructCollectionType(List.class, Cobro_Fijo.class));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return lstCobroFijo;	
	}
	
	
	public boolean logica(Connection con,boolean debug) throws InterruptedException, Throwable {
    	log.write("INICIO JOB RefacturarMovimientoJob");

    	
    	RefacturarMovimiento servicioRefacturarMovimiento = new RefacturarMovimiento();
    	RefacturarMovimiento_Entrada entradaRefacturarMovimiento = new RefacturarMovimiento_Entrada();
    	RefacturarMovimiento_Salida salidaRefacturarMovimiento = new RefacturarMovimiento_Salida();
    	

    	
        Long 	idEmpresa;
        Long 	idUsuario;
    	String 	idModulo;    	
    	String 	usaReglasNegocio;
    	String 	origen;
    	String 	tipo;		  
    	String 	entidades;
    	String 	asignaciones;
    	String 	cobrosModificados;
    	String 	cobrosFijos;
		   

	    
        if (debug) 
        {
        	idEmpresa 			= 1L;       
        	idUsuario			= 5L;
        	idModulo			= "ORD";         	
        	usaReglasNegocio	= "S"; 
        	origen				= "COMP";
        	tipo				= "REFACTURACION";        	
        	entidades 			= "[{\"IdEntidad\":757191}]";
        	asignaciones 		= "[{\"Cantidad\":50,\"IdBoveda\":\"DCV\",\"IdCuentaCustodioPortafolio\":null,\"IdFormaCustodia\":\"CUS\",\"IdInterno\":13099,\"IdMonedaLiquidacionOrden\":\"CLP\",\"IdPortafolio\":153970,\"IdPreferenciaPagoPortafolio\":null,\"IdTransaccion\":70291668,\"PagoCobroCuentaInversion\":\"S\",\"PlazoVentaCorta\":0,\"TasaVentaCorta\":0,\"TipoCambio\":0,\"IdAsignacion\":null}, {\"Cantidad\":81,\"IdBoveda\":\"DCV\",\"IdCuentaCustodioPortafolio\":null,\"IdFormaCustodia\":\"CUS\",\"IdInterno\":13100,\"IdMonedaLiquidacionOrden\":\"CLP\",\"IdPortafolio\":1450263,\"IdPreferenciaPagoPortafolio\":null,\"IdTransaccion\":70291668,\"PagoCobroCuentaInversion\":\"N\",\"PlazoVentaCorta\":0,\"TasaVentaCorta\":0,\"TipoCambio\":0,\"IdAsignacion\":null}]";
        	cobrosModificados 	= "[{\"IdInterno\":13099,\"IdCobro\":\"COVA\",\"ValorCobroAplicado\":0.05}, {\"IdInterno\":13099,\"IdCobro\":\"DEFI\",\"ValorCobroAplicado\":6000}, {\"IdInterno\":13099,\"IdCobro\":\"GFOD\",\"ValorCobroAplicado\":5100}, {\"IdInterno\":13099,\"IdCobro\":\"IVA\",\"ValorCobroAplicado\":19}, {\"IdInterno\":13100,\"IdCobro\":\"COVA\",\"ValorCobroAplicado\":0.05}, {\"IdInterno\":13100,\"IdCobro\":\"DEFI\",\"ValorCobroAplicado\":6000}, {\"IdInterno\":13100,\"IdCobro\":\"GFOD\",\"ValorCobroAplicado\":5100}, {\"IdInterno\":13100,\"IdCobro\":\"IVA\",\"ValorCobroAplicado\":19}]";
        	cobrosFijos 		= "[{\"IdInterno\":13099,\"IdInternoCobro\":0,\"IdPortafolio\":153970,\"IdBolsa\":\"BCS\",\"IdTipoProducto\":\"INAC\",\"IdTipoOperacion\":\"COMACN\",\"IdTipoCanalIngreso\":\"TRA\",\"IdCobro\":\"COFI\",\"ValorCobro\":265,\"FormaCobro\":\"F\",\"ValorTopeMinimoOrden\":0,\"IdMoneda\":\"CLP\",\"CodigoCobroClasificacion\":\"COMISION\",\"PermiteCambiarValor\":\"S\",\"ValorCobroAplicado\":\"265\"}, {\"IdInterno\":13099,\"IdInternoCobro\":1,\"IdPortafolio\":153970,\"IdBolsa\":\"BCS\",\"IdTipoProducto\":\"INAC\",\"IdTipoOperacion\":\"COMACN\",\"IdTipoCanalIngreso\":\"TRA\",\"IdCobro\":\"DEFI\",\"ValorCobro\":0,\"FormaCobro\":\"F\",\"ValorTopeMinimoOrden\":0,\"IdMoneda\":\"CLP\",\"CodigoCobroClasificacion\":\"DERECHO\",\"PermiteCambiarValor\":\"S\",\"ValorCobroAplicado\":\"0\"}, {\"IdInterno\":13099,\"IdInternoCobro\":2,\"IdPortafolio\":153970,\"IdBolsa\":\"BCS\",\"IdTipoProducto\":\"INAC\",\"IdTipoOperacion\":\"COMACN\",\"IdTipoCanalIngreso\":\"TRA\",\"IdCobro\":\"GAFI\",\"ValorCobro\":0,\"FormaCobro\":\"F\",\"ValorTopeMinimoOrden\":0,\"IdMoneda\":\"CLP\",\"CodigoCobroClasificacion\":\"GASTO\",\"PermiteCambiarValor\":\"S\",\"ValorCobroAplicado\":\"0\"}, {\"IdInterno\":13099,\"IdInternoCobro\":3,\"IdPortafolio\":153970,\"IdBolsa\":\"BCS\",\"IdTipoProducto\":\"INAC\",\"IdTipoOperacion\":\"COMACN\",\"IdTipoCanalIngreso\":\"TRA\",\"IdCobro\":\"IVA\",\"ValorCobro\":19,\"FormaCobro\":\"F\",\"ValorTopeMinimoOrden\":0,\"IdMoneda\":\"CLP\",\"CodigoCobroClasificacion\":\"IMPUESTO\",\"PermiteCambiarValor\":\"S\",\"ValorCobroAplicado\":\"19\"}, {\"IdInterno\":13100,\"IdInternoCobro\":4,\"IdPortafolio\":1450263,\"IdBolsa\":\"BCS\",\"IdTipoProducto\":\"INAC\",\"IdTipoOperacion\":\"COMACN\",\"IdTipoCanalIngreso\":\"TRA\",\"IdCobro\":\"COFI\",\"ValorCobro\":265,\"FormaCobro\":\"F\",\"ValorTopeMinimoOrden\":0,\"IdMoneda\":\"CLP\",\"CodigoCobroClasificacion\":\"COMISION\",\"PermiteCambiarValor\":\"S\",\"ValorCobroAplicado\":\"265\"}, {\"IdInterno\":13100,\"IdInternoCobro\":5,\"IdPortafolio\":1450263,\"IdBolsa\":\"BCS\",\"IdTipoProducto\":\"INAC\",\"IdTipoOperacion\":\"COMACN\",\"IdTipoCanalIngreso\":\"TRA\",\"IdCobro\":\"DEFI\",\"ValorCobro\":0,\"FormaCobro\":\"F\",\"ValorTopeMinimoOrden\":0,\"IdMoneda\":\"CLP\",\"CodigoCobroClasificacion\":\"DERECHO\",\"PermiteCambiarValor\":\"S\",\"ValorCobroAplicado\":\"0\"}, {\"IdInterno\":13100,\"IdInternoCobro\":6,\"IdPortafolio\":1450263,\"IdBolsa\":\"BCS\",\"IdTipoProducto\":\"INAC\",\"IdTipoOperacion\":\"COMACN\",\"IdTipoCanalIngreso\":\"TRA\",\"IdCobro\":\"GAFI\",\"ValorCobro\":0,\"FormaCobro\":\"F\",\"ValorTopeMinimoOrden\":0,\"IdMoneda\":\"CLP\",\"CodigoCobroClasificacion\":\"GASTO\",\"PermiteCambiarValor\":\"S\",\"ValorCobroAplicado\":\"0\"}, {\"IdInterno\":13100,\"IdInternoCobro\":7,\"IdPortafolio\":1450263,\"IdBolsa\":\"BCS\",\"IdTipoProducto\":\"INAC\",\"IdTipoOperacion\":\"COMACN\",\"IdTipoCanalIngreso\":\"TRA\",\"IdCobro\":\"IVA\",\"ValorCobro\":19,\"FormaCobro\":\"F\",\"ValorTopeMinimoOrden\":0,\"IdMoneda\":\"CLP\",\"CodigoCobroClasificacion\":\"IMPUESTO\",\"PermiteCambiarValor\":\"S\",\"ValorCobroAplicado\":\"19\"}]";	    
        } 
        else
        {        	
         	idEmpresa			= Long.parseLong(this.getParam("p_IdEmpresa"));
        	idUsuario			= Long.parseLong(this.getParam("p_IdUsuario"));
        	idModulo			= this.getParam("p_IdModulo");        	
        	usaReglasNegocio	= this.getParam("p_UsaReglasNegocio"); 
        	origen				= this.getParam("p_Origen"); 
        	tipo				= this.getParam("p_Tipo");
        	entidades 			= this.getParam("p_Entidades");
        	asignaciones 		= this.getParam("p_Asignaciones");
        	cobrosModificados 	= this.getParam("p_CobrosModificados");
        	cobrosFijos 		= this.getParam("p_CobrosFijos");
        }
        
        

        
        entradaRefacturarMovimiento.p_IdEmpresa = idEmpresa;        
        entradaRefacturarMovimiento.p_IdUsuario = idUsuario;
        entradaRefacturarMovimiento.p_IdModulo = idModulo;
        entradaRefacturarMovimiento.p_UsaReglasNegocio = usaReglasNegocio;		
        entradaRefacturarMovimiento.p_Origen = origen;
        entradaRefacturarMovimiento.p_Tipo = tipo;
        entradaRefacturarMovimiento.p_Entidades = parseEntidades(entidades);
        entradaRefacturarMovimiento.p_Asignaciones = parseAsignaciones(asignaciones);
        entradaRefacturarMovimiento.p_CobrosModificados = parseCobroModificado(cobrosModificados);
        entradaRefacturarMovimiento.p_CobrosFijos = parseCobroFijo(cobrosFijos);
        entradaRefacturarMovimiento.p_EsEjecucionBatch = "S";

		try{

			SQL sql = new SQL();

			salidaRefacturarMovimiento = servicioRefacturarMovimiento.logica(con, entradaRefacturarMovimiento);


			if(salidaRefacturarMovimiento.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0)
			{
				con.commit();
				
				if(salidaRefacturarMovimiento.P_OUT_RUTAARCHIVOLOG != "")
				{
					List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
					actualizar.add(new ActualizarPrograma(salidaRefacturarMovimiento.P_OUT_RUTAARCHIVOLOG, this.getJobId()));
					sql.batch(con,SQL_UPDATE_ID_PROGRAMA , actualizar,ActualizarPrograma.class);					
				}
				
				return true;
			}
			else
			{				
				con.rollback();	
				
				if(salidaRefacturarMovimiento.P_OUT_RUTAARCHIVOLOG != "")
				{
					List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
					actualizar.add(new ActualizarPrograma(salidaRefacturarMovimiento.P_OUT_RUTAARCHIVOLOG, this.getJobId()));
					sql.batch(con,SQL_UPDATE_ID_PROGRAMA , actualizar,ActualizarPrograma.class);
					con.commit();
				}
				
				return false;
			}
			

			
		
		}catch(Exception e){
			con.rollback();
			e.printStackTrace();			
			throw e;
		}
		
    }


	
    // @Override
    public void onError(Throwable t) {
    	log.write("****** ERROR  *******");
        log.write(t);
        t.printStackTrace();
    }

    // @Override
    public void pause() {

    }
    
    @Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

    
    public static void main(String[] args) throws IOException { 
    	
    	System.out.println("== INICIO PRUEBA ==");
    	
        try {
        	Connection con = PlataformaUtils.getDESAConnection();
        	RefacturarMovimientoJob demo = new RefacturarMovimientoJob();
        	demo.logica(con,true);
        
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        System.out.println("== FIN PRUEBA ==");
    }

    
}
