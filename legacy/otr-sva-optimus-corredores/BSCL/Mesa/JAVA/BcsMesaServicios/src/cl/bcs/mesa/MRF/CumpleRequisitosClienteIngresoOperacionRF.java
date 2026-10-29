package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.pla.SaldoFlujoCaja;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.BcsPojo;

public class CumpleRequisitosClienteIngresoOperacionRF implements JavaCall{

    public static final Logger logger = LogManager.getLogger(CumpleRequisitosClienteIngresoOperacionRF.class);
    
    private final String SQL_SELECT_PORTAFOLIO_PREFERENCIA = loadResourceToString("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_SelectPortafolioPreferencia.sql");
    private final String SQL_SELECT_PORTAFOLIO_MRF = loadResourceToString("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_SelectPortafolioMRF.sql");
    private final String SQL_SELECT_PORTAFOLIO_MRF_FINAL = loadResourceToString("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_SelectPortafolioMRFFinal.sql");
    private final String SQL_SELECT_CLIENTES_VAL = loadResourceToString("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_SelectClientesVal.sql");
    private final String SQL_SELECT_RESULTADO = loadResourceToString("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_SelectResultado.sql");
    
    private final String SQL_CREATE_TEMPORARY_TABLE_PORTAFOLIOS_PREFERENCIA = loadResourceToString("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_CreateTempTablePortafolioPreferencia.sql");
    private final String SQL_CREATE_TEMPORARY_TABLE_PORTAFOLIO_MRF = loadResourceToString("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_CreateTempTablePortafolioMRF.sql");
    private final String SQL_CREATE_TEMPORARY_TABLE_PORTAFOLIO_MRF_FINAL = loadResourceToString("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_CreateTempTablePortafolioMRFFinal.sql");
    private final String SQL_CREATE_TEMPORARY_TABLE_CLIENTES_VAL = loadResourceToString("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_CreateTempTableClientesVal.sql");
    private final String SQL_CREATE_TEMPORARY_TABLE_FLUJO_CAJA = loadResourceToString("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_CreateTempTableFlujoCaja.sql");
    private final String SQL_CREATE_TEMPORARY_TABLE_RESULTADO = loadResourceToString("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_CreateTempTableResultado.sql");
    
    private final String SQL_INSERT_TEMPORARY_TABLE_PORTAFOLIOS_PREFERENCIA = loadResourceToString("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_InsertTempTablePortafoliosPreferencia.sql");
    private final String SQL_INSERT_TEMPORARY_TABLE_PORTAFOLIO_MRF = loadResourceToString("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_InsertTempTablePortafolioMRF.sql");
    private final String SQL_INSERT_TEMPORARY_TABLE_PORTAFOLIO_MRF_FINAL = loadResourceToString("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_InsertTempTablePortafolioMRFFinal.sql");
    private final String SQL_INSERT_TEMPORARY_TABLE_CLIENTES_VAL = loadResourceToString("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_InsertTempTableClientesVal.sql");
    private final String SQL_INSERT_TEMPORARY_TABLE_FLUJO_CAJA = loadResourceToString("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_InsertTempTableFlujoCaja.sql");
    private final String SQL_INSERT_TEMPORARY_TABLE_RESULTADO= loadResourceToString("cl/bcs/mesa/MRF/sql/CumpleRequisitosClienteIngresoOperacionRF_InsertTempTableResultado.sql");
    
	public static class EntradaCumpleRequisitosClienteIngresoOperacionRF{
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public List<p_dato> P_DATOS;
        
        public EntradaCumpleRequisitosClienteIngresoOperacionRF() {
        	this.P_DATOS 		= new ArrayList<p_dato>();
        }
        public String toString() {
			return Utils.printJson(this, false);
		};
    }
	
	public static class p_dato extends BcsPojo{ 
		public Long IdPortafolio;
	    public String IdTipoOperacion;
	    public String IdCondicionDeLiquidacion;
	    public String FechaOperacion;
	    public String FechaLiquidacion;
	    public String IdMoneda;
	    public BigDecimal MontoOperacion;
	}
	
    public static class PortafolioPreferencia extends BcsPojo{
        public String PAR_IdTipoOperacion;
        public String PAR_IdMoneda;
        public String PAR_IdCondicionDeLiquidacion;
        public String PAR_FechaOperacion;
        public String PAR_FechaLiquidacion;
        public BigDecimal PAR_MontoOperacion;
        public String CLI_DocumentoIdentidad;
        public String CLI_IdEstado;
        public String CLI_IdEstadoPersona;
        public String CLI_NombreCompleto;
        public Long POR_IdPortafolio;
        public String POR_CodigoPortafolio;
        public String POR_DescripcionPortafolio;
        public String POR_Proposito;
        public Long POR_IdCliente;
        public String POR_IdEstado;
        public String POR_Estado;
        public Date POR_FechaBloqueo;
        public String POR_ComentarioBloqueo;
        public String POR_IdTipoBloqueo;
        public String POR_IdPerfilComercialBaseOrigen;
        public String POR_IdPerfilProductos;
        public String POR_IdPerfilProductosExtra;
        public String POR_IdPerfilCobros;
        public String POR_IdPerfilCobrosExtra;
        public String POR_IdPerfilLimites;
        public String POR_IdPerfilLimitesExtra;
        public String POR_IdPerfilCondicionesOperativas;
        public String POR_IdPerfilCondicionesOperativasExtra;
        public String POR_IdEstadoAutorizacion;
        public Date POR_FechaIngreso;
        public Date POR_FechaCreacion;
        public Date POR_FechaModificacion;
        public Long POR_IdDireccionEnvioCorrespondencia;
        public Long POR_IdTelefonoContacto;
        public Long POR_IdCarga;
        public Long POR_IdEmpleadoEjecutivo;
        public String POR_EsEjecutivoExclusivo;
        public String POR_CodigoPropositoPortafolio;
        public String POR_NumeroFicha;
        public Long POR_IdNombreFantasia;
        public String POR_Alias;
        public String POR_EsCuentaPropia;
        public Long POR_IdEmpresaCuentaPropia;
        public String PRE_OperaCustodia;
        public String PRE_SectorEconomico;
        public String PRE_CodigoOTC;
        public String PRE_DataTec;
        public String PRE_OperaCuentaInversion;
        public String PRE_SobregiroCuentaInversion;
        public String IND_GrupoIndicador;
        public String IND_NombreTipoIndicador;
        public String IND_ValorIndicador;
        public String REQ_IdTipoEtapa;
        public String REQ_IdCondicionOperativa;
        public String REQ_CodigoTipoIndicador;
        public String REQ_IdTipoComportamiento;
    }
    
    public static class PortafolioMRF extends BcsPojo{
    	public String PAR_IdTipoOperacion;
        public String PAR_IdMoneda;
        public String PAR_IdCondicionDeLiquidacion;
        public String PAR_FechaOperacion;
        public String PAR_FechaLiquidacion;
        public BigDecimal PAR_MontoOperacion;

        public String CLI_DocumentoIdentidad;
        public String CLI_IdEstado;
        public String CLI_IdEstadoPersona;
        public String CLI_NombreCompleto;

        public Long POR_IdPortafolio;
        public String POR_CodigoPortafolio;
        public String POR_DescripcionPortafolio;
        public String POR_Proposito;
        public Long POR_IdCliente;
        public String POR_IdEstado;
        public String POR_Estado;
        public Date POR_FechaBloqueo;
        public String POR_ComentarioBloqueo;
        public String POR_IdTipoBloqueo;
        public String POR_IdPerfilComercialBaseOrigen;
        public String POR_IdPerfilProductos;
        public String POR_IdPerfilProductosExtra;
        public String POR_IdPerfilCobros;
        public String POR_IdPerfilCobrosExtra;
        public String POR_IdPerfilLimites;
        public String POR_IdPerfilLimitesExtra;
        public String POR_IdPerfilCondicionesOperativas;
        public String POR_IdPerfilCondicionesOperativasExtra;
        public String POR_IdEstadoAutorizacion;
        public Date POR_FechaIngreso;
        public Date POR_FechaCreacion;
        public Date POR_FechaModificacion;
        public Long POR_IdDireccionEnvioCorrespondencia;
        public Long POR_IdTelefonoContacto;
        public Long POR_IdCarga;
        public Long POR_IdEmpleadoEjecutivo;
        public String POR_EsEjecutivoExclusivo;
        public String POR_CodigoPropositoPortafolio;
        public String POR_NumeroFicha;
        public Long POR_IdNombreFantasia;
        public String POR_Alias;
        public String POR_EsCuentaPropia;
        public Long POR_IdEmpresaCuentaPropia;

        public String PRE_OperaCustodia;
        public String PRE_SectorEconomico;
        public String PRE_CodigoOTC;
        public String PRE_DataTec;
        public String PRE_OperaCuentaInversion;
        public String PRE_SobregiroCuentaInversion;

        public String IND_GrupoIndicador;
        public String IND_NombreTipoIndicador;
        public String IND_ValorIndicador;

        public String REQ_IdTipoEtapa;
        public String REQ_IdCondicionOperativa;
        public String REQ_CodigoTipoIndicador;
        public String REQ_IdTipoComportamiento;

        public BigDecimal FCA_Saldo;

        public String PRO_IdTipoProducto;
        public String PRO_IdTipoOperacion;
    }
    
    public static class Resultado extends BcsPojo{
    	public Long IdInterno;
        public Long IdResultado;
        public String Codigo;
        public String NombreParametro1;
        public String ValorParametro1;
        public String NombreParametro2;
        public String ValorParametro2;
    }

    public static class ClienteCtaInversion extends BcsPojo{
    	public Long IdPortafolio;
    	public String IdMoneda;
    	public String FechaLiquidacion;
    	
    	public ClienteCtaInversion(Long idPortafolio, String idMoneda, String fechaLiquidacion) {
    		this.IdPortafolio = idPortafolio;
    		this.IdMoneda = idMoneda;
    		this.FechaLiquidacion = fechaLiquidacion;
    	}
    }
    
    public static class FlujoCaja extends BcsPojo{
    	public String	NombreUso; 
		public String	FechaSaldo; 
		public Long		IdPortafolio; 
		public String	IdMoneda; 
		public BigDecimal	Saldo;
    }
	
    static class SalidaCumpleRequisitosClienteIngresoOperacionRF extends BcsPojo{
    	public List<MensajeSalida>			P_OUT_MENSAJES;
    	public Long 						P_OUT_ADVERTENCIASCONTADOR;
		public Long							P_OUT_ERRORESCONTADOR;
	
		public SalidaCumpleRequisitosClienteIngresoOperacionRF (){
			P_OUT_ADVERTENCIASCONTADOR	= 0L;
			P_OUT_ERRORESCONTADOR 		= 0L;
			P_OUT_MENSAJES 				= new ArrayList<MensajeSalida>();
		}
    }

    @Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCumpleRequisitosClienteIngresoOperacionRF jin = gson.fromJson(in.toString(), EntradaCumpleRequisitosClienteIngresoOperacionRF.class);
		SalidaCumpleRequisitosClienteIngresoOperacionRF out = logica(conn,jin);
	
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	// ------------------------------------------------------------
	// LOGICA
	// ------------------------------------------------------------

    public SalidaCumpleRequisitosClienteIngresoOperacionRF logica(Connection conn, EntradaCumpleRequisitosClienteIngresoOperacionRF entrada) throws Exception {
        SalidaCumpleRequisitosClienteIngresoOperacionRF out = new SalidaCumpleRequisitosClienteIngresoOperacionRF();
        SQL _sql = new SQL();
    	
    	try {
	        List<Long> portFlujo = new ArrayList<Long>();
			List<String> monedaFlujo = new ArrayList<String>();
			SaldoFlujoCaja saldoFlujoCaja = new SaldoFlujoCaja();
	        String fechaSistemaStr = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
	        Date fechaSistema = Fecha.toDate(fechaSistemaStr);
	        Long IdInterno = 0L;
	        
	        HashMap<String, Object> params = new HashMap<String, Object>();
	        
	        if(entrada.P_DATOS != null && !entrada.P_DATOS.isEmpty()) {
	        	for(p_dato dato : entrada.P_DATOS) {
	        		params.put("IdPortafolio", dato.IdPortafolio);
		 	        params.put("IdTipoOperacion", dato.IdTipoOperacion);
		 	        params.put("IdMoneda", dato.IdMoneda);
		 	        params.put("IdCondicionDeLiquidacion", dato.IdCondicionDeLiquidacion);
		 	        params.put("FechaOperacion", dato.FechaOperacion);
		 	        params.put("FechaLiquidacion", dato.FechaLiquidacion);
		 	        params.put("MontoOperacion", dato.MontoOperacion);
		 	        IdInterno = dato.IdPortafolio;
	        	}
	        }
	       
	        List<PortafolioPreferencia> vPortafoliosPreferencia = _sql.query(conn, SQL_SELECT_PORTAFOLIO_PREFERENCIA, params, PortafolioPreferencia.class);
	        SQL.exec(conn, SQL_CREATE_TEMPORARY_TABLE_PORTAFOLIOS_PREFERENCIA);
			_sql.batch(conn, SQL_INSERT_TEMPORARY_TABLE_PORTAFOLIOS_PREFERENCIA, vPortafoliosPreferencia, PortafolioPreferencia.class);
			
	        List<ClienteCtaInversion> vPortafolioCtaInv = vPortafoliosPreferencia.stream()
	        		.filter(p -> "S".equals(p.PRE_OperaCuentaInversion))
	        		.filter(p -> p.PAR_FechaLiquidacion != null && !p.PAR_FechaLiquidacion.isEmpty())
	        		.filter(p -> "SALDOFLUJOCAJA".equals(p.REQ_IdCondicionOperativa))
	        		.map(p -> new ClienteCtaInversion(p.POR_IdPortafolio, p.PAR_IdMoneda,p.PAR_FechaLiquidacion))
	        		.collect(Collectors.toList());
	        
	        SQL.exec(conn, SQL_CREATE_TEMPORARY_TABLE_FLUJO_CAJA);
	        if(!vPortafolioCtaInv.isEmpty()) {    	
	        	Date fechaDesde = Fecha.toDate("9999-09-09");
	            Date fechaHasta = Fecha.toDate("0001-01-01");
	            
	            for(ClienteCtaInversion cli : vPortafolioCtaInv) {
	            	Date fechaLiquidacion = Fecha.toDate(cli.FechaLiquidacion);
	            	
	            	if(fechaLiquidacion.before(fechaDesde)) {
	            		fechaDesde = fechaLiquidacion;
	            	}
	            	if(fechaLiquidacion.after(fechaHasta)) {
	            		fechaHasta = fechaLiquidacion;
	            	}
	            }
	            
	            SaldoFlujoCaja.ServiceEntrada entradaSaldoFlujoCaja = new SaldoFlujoCaja.ServiceEntrada();
        		entradaSaldoFlujoCaja.P_IDEMPRESA = entrada.p_IdEmpresa;
        		entradaSaldoFlujoCaja.P_FECHADESDE = Fecha.stringToSqlDate(fechaDesde);
        		entradaSaldoFlujoCaja.P_FECHAHASTA = Fecha.stringToSqlDate(fechaHasta);
        		entradaSaldoFlujoCaja.P_USO = "MESA_RF";
        		entradaSaldoFlujoCaja.P_DESPLEGARENUI = "N";
        		entradaSaldoFlujoCaja.P_AGRUPARPORORIGEN = "N";
        		entradaSaldoFlujoCaja.P_PORTAFOLIOACONSULTAR = new ArrayList<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR>();
        		
        		SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR saldo = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
				saldo.IdPortafolio = entrada.P_DATOS.get(0).IdPortafolio;
				saldo.IdMoneda = entrada.P_DATOS.get(0).IdMoneda;
				entradaSaldoFlujoCaja.P_PORTAFOLIOACONSULTAR.add(saldo);
				if(!portFlujo.contains(entrada.P_DATOS.get(0).IdPortafolio)) portFlujo.add(entrada.P_DATOS.get(0).IdPortafolio);
				if(!monedaFlujo.contains(entrada.P_DATOS.get(0).IdMoneda)) monedaFlujo.add(entrada.P_DATOS.get(0).IdMoneda);
				
				
				SaldoFlujoCaja.ServiceSalida salidaFlujoCaja = new SaldoFlujoCaja.ServiceSalida();
				salidaFlujoCaja.P_OUT_FLUJOCAJA = new ArrayList<SaldoFlujoCaja.ObjFLUJOCAJA>();
				if(entradaSaldoFlujoCaja.P_PORTAFOLIOACONSULTAR.size() > 0){
					salidaFlujoCaja = saldoFlujoCaja.logica(conn, entradaSaldoFlujoCaja);
					List<SaldoFlujoCaja.ObjFLUJOCAJA> flujosCaja = salidaFlujoCaja.P_OUT_FLUJOCAJA;
					_sql.batch(conn, SQL_INSERT_TEMPORARY_TABLE_FLUJO_CAJA, flujosCaja, SaldoFlujoCaja.ObjFLUJOCAJA.class);
				}
	        }
	        List<PortafolioMRF> vPortafolioMRF = _sql.query(conn, SQL_SELECT_PORTAFOLIO_MRF, new HashMap<>(), PortafolioMRF.class);
			SQL.exec(conn, SQL_CREATE_TEMPORARY_TABLE_PORTAFOLIO_MRF);
			_sql.batch(conn, SQL_INSERT_TEMPORARY_TABLE_PORTAFOLIO_MRF, vPortafolioMRF, PortafolioMRF.class);
				
			List<PortafolioMRF> vPortafolioMRFFinal = _sql.query(conn, SQL_SELECT_PORTAFOLIO_MRF_FINAL, new HashMap<>(), PortafolioMRF.class);
			SQL.exec(conn, SQL_CREATE_TEMPORARY_TABLE_PORTAFOLIO_MRF_FINAL);
			_sql.batch(conn, SQL_INSERT_TEMPORARY_TABLE_PORTAFOLIO_MRF_FINAL, vPortafolioMRFFinal, PortafolioMRF.class);
	            
			List<PortafolioMRF> vClientesVal = _sql.query(conn, SQL_SELECT_CLIENTES_VAL , new HashMap<>(), PortafolioMRF.class);
			SQL.exec(conn, SQL_CREATE_TEMPORARY_TABLE_CLIENTES_VAL);
			_sql.batch(conn, SQL_INSERT_TEMPORARY_TABLE_CLIENTES_VAL, vClientesVal, PortafolioMRF.class);
				
			HashMap<String, Object> paramsResultado = new HashMap<String, Object>();
			paramsResultado.put("FechaSistema", fechaSistema);
			List<Resultado> RespuestavResultado = _sql.query(conn, SQL_SELECT_RESULTADO ,  paramsResultado, Resultado.class);
				
			SQL.exec(conn, SQL_CREATE_TEMPORARY_TABLE_RESULTADO);
			_sql.batch(conn, SQL_INSERT_TEMPORARY_TABLE_RESULTADO, RespuestavResultado, Resultado.class);
			
			HashMap<String, String> valores = new HashMap<String, String>();
			for(Resultado resultParams : RespuestavResultado) {
				if("PORTAFOLIO_BLOQUEADO".equals(resultParams.Codigo)) {
					valores.put("CodigoPortafolio", (resultParams.ValorParametro1 != null) ? String.valueOf(resultParams.ValorParametro1) : "");
				}
				if("CLIENTE_BLOQUEADO".equals(resultParams.Codigo)) {
					valores.put("DocumentoIdentidad", (resultParams.ValorParametro1 != null) ? String.valueOf(resultParams.ValorParametro1) : "");
				}
				if("PERSONA_NO_ACTIVA".equals(resultParams.Codigo)) {
					valores.put("NombrePersona", (resultParams.ValorParametro1 != null) ? String.valueOf(resultParams.ValorParametro1) : "");
				}
				if("FLUJO_CAJA_INSUFICIENTE_PORTAFOLIO".equals(resultParams.Codigo)) {
					valores.put("CodigoPortafolio", (resultParams.ValorParametro2 != null) ? String.valueOf(resultParams.ValorParametro2) : "");
				}
			}
			
			for(Resultado result : RespuestavResultado) {
				if(result.IdResultado == 4) {
					out.P_OUT_ERRORESCONTADOR ++;
					out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(IdInterno , PlataformaUtils.MensajeSalida.MENSAJE_ERROR,result.Codigo, valores, conn, entrada.p_IdEmpresa));
				}
				if(result.IdResultado == 2) {
					out.P_OUT_ADVERTENCIASCONTADOR++;
					out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(IdInterno , PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA,result.Codigo, valores, conn, entrada.p_IdEmpresa));
				}
			}
			//Eliminar datos de la tabla temporal
			SQL.exec(conn, "DROP TABLE #CumpleRequisitosClienteIngresoOperacionRF_PortafoliosPreferencia_Temp");
	        SQL.exec(conn, "DROP TABLE #CumpleRequisitosClienteIngresoOperacionRF_PortafolioMRF_Temp");
	        SQL.exec(conn, "DROP TABLE #CumpleRequisitosClienteIngresoOperacionRF_PortafolioMRFFinal_Temp");
	        SQL.exec(conn, "DROP TABLE #CumpleRequisitosClienteIngresoOperacionRF_ClientesVal_Temp");
	        SQL.exec(conn, "DROP TABLE #CumpleRequisitosClienteIngresoOperacionRF_FlujoCaja_Temp");
	        SQL.exec(conn, "DROP TABLE #CumpleRequisitosClienteIngresoOperacionRF_Resultado_Temp");
    	} catch (Exception er) { 
			er.printStackTrace();
		}
        return out;
    }
    
    public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"S\",\"P_DATOS\":[{\"IdPortafolio\":8067,\"IdTipoOperacion\":\"VENTER\",\"IdMoneda\":\"USD\",\"IdCondicionDeLiquidacion\":\"T0\",\"FechaOperacion\":\"2025-03-05\",\"FechaLiquidacion\":\"2025-03-05\",\"MontoOperacion\":1000}]}";
			EntradaCumpleRequisitosClienteIngresoOperacionRF Objin = gson.fromJson(json, EntradaCumpleRequisitosClienteIngresoOperacionRF.class);
			CumpleRequisitosClienteIngresoOperacionRF demo = new CumpleRequisitosClienteIngresoOperacionRF();				
			try {
				SalidaCumpleRequisitosClienteIngresoOperacionRF ObjOut = demo.logica(conn, Objin);
				conn.rollback();
				System.out.println(ObjOut.toString());
			}
			catch(Exception er) {
				er.printStackTrace();	
				conn.rollback();
			}
		};
		System.out.println("== END ==");
	}
}
