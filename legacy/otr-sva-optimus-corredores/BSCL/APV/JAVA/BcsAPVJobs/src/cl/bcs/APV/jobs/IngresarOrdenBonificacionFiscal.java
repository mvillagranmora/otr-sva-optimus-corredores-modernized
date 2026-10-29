package cl.bcs.APV.jobs;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.ordenes.CrearOrdenes;
//import cl.bcs.ordenes.jobs.CargarTransaccionesBolsa;
import cl.bcs.plataforma.bat.AgregarLogPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.evt.NotificarAdministrador;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.socionegocio.blo.BloqueoAutomaticoClientesPorConfiguracion;
import cl.bcs.socionegocio.blo.BloqueoAutomaticoClientesPorTemporalidad;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;
import cl.bcs.utils.SQL;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

public class IngresarOrdenBonificacionFiscal extends BcsJob{
	private final Log log = new Log("INGRESO_ORDEN_BONIFICACION_FISCAL");
	private final String IdTipoPrograma = "INGRESAR_ORDEN_BONIFICACION_FISCAL";
	
	@Override
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		return logica(conn,false);
	}

	@Override
	public void onError(Throwable t) {
        log.write("****** ERROR  *******");
        log.write(t);
        t.printStackTrace();

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	public boolean logica(Connection con,boolean debug) throws InterruptedException, Throwable {
    	log.write("INICIO JOB");
    	SQL _sql=new SQL();
        
        Long IdEmpresa = 0L;
        Long IdPrograma = 0L;
        if (debug) {
        	IdPrograma = -1L;
            IdEmpresa = 1L;
        } else{
			IdPrograma = this.getJobId();
        	IdEmpresa = 1L;
        }
        
		AgregarLogPrograma.logica(con, IdPrograma, IdTipoPrograma,"PRUEBA APV V1",true);

		List<CrearOrdenes.ObjOrdenesEntrada> ListOrden = new ArrayList<CrearOrdenes.ObjOrdenesEntrada>();
		CrearOrdenes.ObjOrdenesEntrada Orden = new CrearOrdenes.ObjOrdenesEntrada();							 //Ordenes
		Orden.IdInterno = 1L;
		Orden.FechaRecepcion = "0001-01-01 00:00:00";
		Orden.FechaVigencia = "2017-11-09";
		Orden.FechaEmision = "0001-01-01";
		Orden.FechaOperacion = "0001-01-01";
		Orden.IdCondicionDeLiquidacion = "T2"; 
		Orden.IdTipoOperacion = "COMACN";
		Orden.IdTipoVigencia = "SH";
		Orden.Glosa = "";
		Orden.Cantidad = BigDecimal.valueOf(2l);
		Orden.MontoNeto = BigDecimal.ZERO;
		Orden.ValorRescate = BigDecimal.valueOf(0L);
		Orden.IdBoveda = "DCV";
		Orden.IdFormaCustodia = "CUS";
		Orden.IdPorcentajeVisibilidad = 1L;
		Orden.PrecioLimite = BigDecimal.valueOf(406.26);
		Orden.EsFacturaInmediata = "N";
		Orden.IdTipoPrecio = "M";
		Orden.IdPortafolio = 20021L;
		Orden.IdTipoCanalIngreso = "AGE";
		Orden.IdTipoReajuste = "";
		Orden.IdEmisor = "AGUAS";
		Orden.IdBolsa = "BCS";
		Orden.EspecificaBolsa = "S";
		Orden.IdMonedaLiquidacionOrden = "CLP";
		Orden.TipoCambio = null;
		Orden.IdInstrumento = 31L;
		Orden.IdInstrumentoSubyacente = 0L;
		Orden.IdCondicionDeLiquidacionPlazo = "";
		Orden.TasaVentaCorta = BigDecimal.valueOf(0);
		Orden.PlazoVentaCorta = 0L;
		Orden.EsVentaCorta = "N";
		Orden.TasaSimultanea = BigDecimal.valueOf(0);
		Orden.PlazoSimultanea = 0L;
		Orden.PlazoIF = 0L;
		Orden.TasaRFIF = BigDecimal.valueOf(0);
		Orden.EsOD = "N";
		Orden.EsFungible = "N";
		Orden.IdTipoLibroOrden = "NORMAL";
		Orden.IdLibroOrdenes = null;
		Orden.IdCuentaCustodioPortafolio = null;
		Orden.EsPorMonto = "S";
		Orden.IdCorredorCorresponsal = null;
		Orden.IdPersonaAutorizadoDarOrdenes = null;
		Orden.IdTipoCanalDistribucion = "MAN";
		Orden.EsOrdenEspecial = "N";
		Orden.PagoCobroCuentaInversion = "N";
		Orden.IdPreferenciaPagoPortafolio = 20021L;
		Orden.BaseTasa = 0L;
		Orden.TipoInteres = "";
		Orden.IdMecanismoCaptacion = "OV";
		
		ListOrden.add(Orden);

		//ORDENES
		CrearOrdenes.CrearOrdenesEntrada paramsOrdenesEntrada = new CrearOrdenes.CrearOrdenesEntrada();
	    
		paramsOrdenesEntrada.p_IdEmpresa = 1L;
		paramsOrdenesEntrada.p_IdUsuario = 5L;
		paramsOrdenesEntrada.p_IdModulo = "APV";
		paramsOrdenesEntrada.p_simularOperacion = "N";
		paramsOrdenesEntrada.p_UsaReglasNegocio = "S";
		paramsOrdenesEntrada.p_Ordenes = ListOrden;
	    
		CrearOrdenes procesoOrdenes =new CrearOrdenes();

			try {
				CrearOrdenes.CrearOrdenesSalida ObjOut=procesoOrdenes.logica(con, paramsOrdenesEntrada);
				System.out.println(ObjOut.toString());
				con.commit();
			}
			catch(Exception er) {
			//er.printStackTrace();	
			con.rollback();
		}
		
		
		
		con.commit();
		return true;
	}
	
	//DEBUG
    public static void main(String[] args) {
        //System.out.println("== START ==");
        try {
            Connection con = PlataformaUtils.getDESAConnection();
            // ---------------------------------
            try {
/*
    			List<AgregarPrograma.InAgregarPrograma.Parametro> paramsParametros = new ArrayList<AgregarPrograma.InAgregarPrograma.Parametro>();
    			AgregarPrograma.InAgregarPrograma.Parametro	paramIdEmpresa = new AgregarPrograma.InAgregarPrograma.Parametro();
    			paramIdEmpresa.NombreParametro 	= "IdEmpresa";
    			paramIdEmpresa.Valor			= "1";
    			paramsParametros.add(paramIdEmpresa);
    			
    			String FechaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, 1L,"PLATAFORMA", "PLA.FechaSistema");
    			AgregarPrograma.InAgregarPrograma paramsIn = new AgregarPrograma.InAgregarPrograma(); 
    			paramsIn.p_IdEmpresa		= 1L;
    			paramsIn.p_IdModulo			= "APV";
    			paramsIn.p_IdUsuario		= 23L;
    			paramsIn.p_IdTipoPrograma	= "INGRESAR_ORDEN_BONIFICACION_FISCAL";
    			paramsIn.p_Segundos			= 0L;
    			paramsIn.p_FechaSistema		= FechaSistema; 
    			paramsIn.p_Parametros		= paramsParametros;
    			AgregarPrograma AP = new AgregarPrograma();
    			AgregarPrograma.OutAgregarPrograma result = AP.logica(con, paramsIn);
            	
  */          	
            	
            	IngresarOrdenBonificacionFiscal demo=new IngresarOrdenBonificacionFiscal();
            	demo.logica(con,true);
                con.commit();
            } catch (Throwable er) {
                er.printStackTrace();
                con.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("== END ==");       
    }
}
