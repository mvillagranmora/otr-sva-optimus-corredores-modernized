package cl.bcs.normativa.circulares;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
//import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class GenerarAnexo1Circular20 implements JavaCall {
	
	private static final Logger LOGGER = LogManager.getLogger(GenerarAnexo1Circular20.class);
	
	public static class GenerarAnexo1Circular20_Entrada {
		public Long			p_IdEmpresa;
		public Long			p_IdUsuario;
		public String 		p_IdModulo;
		public String		p_FechaSistema;
		public String		p_FechaInicio;
		public String		p_FechaFin;
		public String		p_Ruta;
		public Long			p_SecuenciaSolicitud;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class GenerarAnexo1Circular20_Salida {
		public String P_OUT_NOMBRE_ARCHIVO;
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
	}
	
	public static class FilaDepositosAhorroPrevisionalVoluntarioPorPlan {
	    public Long NumeroPolizaRunFondoCodigoPlan;
	    public String Serie;
	    //NumeroCuentasApvVigentes
	    public Long NumeroCuentasApvVigentes_LetraA_Masculino;
	    public Long NumeroCuentasApvVigentes_LetraA_Femenino;
	    public Long NumeroCuentasApvVigentes_Total_Masculino;
	    public Long NumeroCuentasApvVigentes_Total_Femenino;
	    //NumeroAseguradosParticipesAportantesClientesPlan;
	    public Long NumeroAseguradosParticipesAportantesClientesPlan_LetraA_Masculino;
	    public Long NumeroAseguradosParticipesAportantesClientesPlan_LetraA_Femenino;
	    public Long NumeroAseguradosParticipesAportantesClientesPlan_Total_Masculino;
	    public Long NumeroAseguradosParticipesAportantesClientesPlan_Total_Femenino;
	    //SaldoTotalAcumulado
	    public BigDecimal SaldoTotalAcumulado_LetraA_Masculino;
	    public BigDecimal SaldoTotalAcumulado_LetraA_Femenino;
	    public BigDecimal SaldoTotalAcumulado_LetraB_Masculino;
	    public BigDecimal SaldoTotalAcumulado_LetraB_Femenino;
	    //Bonificacion
	    public BigDecimal Bonificacion_LetraA_Masculino;
	    public BigDecimal Bonificacion_LetraA_Femenino;
	    //NumeroDepositos
	    public Long NumeroDepositos_LetraA_Masculino;
	    public Long NumeroDepositos_LetraA_Femenino;
	    public Long NumeroDepositos_LetraB_Masculino;
	    public Long NumeroDepositos_LetraB_Femenino;
	    //MontoTotalDepositos
	    public BigDecimal MontoTotalDepositos_LetraA_Masculino;
	    public BigDecimal MontoTotalDepositos_LetraA_Femenino;
	    public BigDecimal MontoTotalDepositos_LetraB_Masculino;
	    public BigDecimal MontoTotalDepositos_LetraB_Femenino;
	    //NumeroTraspasosRecibidos
	    public Long NumeroTraspasosRecibidos_Masculino;
	    public Long NumeroTraspasosRecibidos_Femenino;
	    //MontoTotalTraspasosRecibidos
	    public BigDecimal MontoTotalTraspasosRecibidos_Masculino;
	    public BigDecimal MontoTotalTraspasosRecibidos_Femenino;
	    //NumeroRetiros
	    public Long NumeroRetiros_LetraA_Masculino;
	    public Long NumeroRetiros_LetraA_Femenino;
	    public Long NumeroRetiros_LetraB_Masculino;
	    public Long NumeroRetiros_LetraB_Femenino;
	    //MontoTotalRetiros
	    public BigDecimal MontoTotalRetiros_LetraA_Masculino;
	    public BigDecimal MontoTotalRetiros_LetraA_Femenino;
	    public BigDecimal MontoTotalRetiros_LetraB_Masculino;
	    public BigDecimal MontoTotalRetiros_LetraB_Femenino;
	    //NumeroTraspasosRealizados
	    public Long NumeroTraspasosRealizados_Masculino;
	    public Long NumeroTraspasosRealizados_Femenino;
	    //MontoTotalTraspasosRealizados
	    public BigDecimal MontoTotalTraspasosRealizados_Masculino;
	    public BigDecimal MontoTotalTraspasosRealizados_Femenino;
	    public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class TraspasosDetalle {
		public String Accion;
		//Afp
		public Long Afp_NumeroTraspasos;
		public BigDecimal Afp_Miles;
	    //BancosInstitucionesFinancieras
	    public Long BancosInstitucionesFinancieras_NumeroTraspasos;
	    public BigDecimal BancosInstitucionesFinancieras_Miles;
	    //CorredoresBolsa
	    public Long CorredoresBolsa_NumeroTraspasos;
	    public BigDecimal CorredoresBolsa_Miles;
	    //AgentesValores
	    public Long AgentesValores_NumeroTraspasos;
	    public BigDecimal AgentesValores_Miles;
	    //FondosMutuos
	    public Long FondosMutuos_NumeroTraspasos;
	    public BigDecimal FondosMutuos_Miles;
	    //FondosInversion
	    public Long FondosInversion_NumeroTraspasos;
	    public BigDecimal FondosInversion_Miles;
	    //CompaniasSeguros
	    public Long CompaniasSeguros_NumeroTraspasos;
	    public BigDecimal CompaniasSeguros_Miles;
	    //FondosVivienda
	    public Long FondosVivienda_NumeroTraspasos;
	    public BigDecimal FondosVivienda_Miles;
	    //OtrasEntidadesAutorizadasSvs
	    public Long OtrasEntidadesAutorizadasSvs_NumeroTraspasos;
	    public BigDecimal OtrasEntidadesAutorizadasSvs_Miles;
	        public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class NumeroPersonasSaldoEdadGenero {
		public String Saldo; //Saldo_0_5
		public Long Saldo_Edad_20_Masculino;
		public Long Saldo_Edad_20_Femenino;
		public Long Saldo_Edad_20_25_Masculino;
		public Long Saldo_Edad_20_25_Femenino;
		public Long Saldo_Edad_25_30_Masculino;
		public Long Saldo_Edad_25_30_Femenino;
		public Long Saldo_Edad_30_35_Masculino;
		public Long Saldo_Edad_30_35_Femenino;
		public Long Saldo_Edad_35_40_Masculino;
		public Long Saldo_Edad_35_40_Femenino;
		public Long Saldo_Edad_40_45_Masculino;
		public Long Saldo_Edad_40_45_Femenino;
		public Long Saldo_Edad_45_50_Masculino;
		public Long Saldo_Edad_45_50_Femenino;
		public Long Saldo_Edad_50_55_Masculino;
		public Long Saldo_Edad_50_55_Femenino;
		public Long Saldo_Edad_55_60_Masculino;
		public Long Saldo_Edad_55_60_Femenino;
		public Long Saldo_Edad_60_65_Masculino;
		public Long Saldo_Edad_60_65_Femenino;
		public Long Saldo_Edad_65_70_Masculino;
		public Long Saldo_Edad_65_70_Femenino;
		public Long Saldo_Edad_70_Mas_Masculino;
		public Long Saldo_Edad_70_Mas_Femenino;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class FilaDepositosConvenidosPorPlan {
		public Long NumeroPolizaRunFondoCodigoPlan;
		public String Serie;
		//NumeroCuentasApvVigentes
		public Long NumeroCuentasApvVigentes_Masculino;
		public Long NumeroCuentasApvVigentes_Femenino;
		//NumeroAseguradosParticipesAportantesClientesPlan
		public Long NumeroAseguradosParticipesAportantesClientesPlan_Masculino;
		public Long NumeroAseguradosParticipesAportantesClientesPlan_Femenino;
		//SaldoTotalAcumulado
		public BigDecimal SaldoTotalAcumulado_Masculino;
		public BigDecimal SaldoTotalAcumulado_Femenino;
		//NumeroDepositos
		public Long NumeroDepositos_Masculino;
		public Long NumeroDepositos_Femenino;
		//MontoTotalDepositos
		public BigDecimal MontoTotalDepositos_Masculino;
		public BigDecimal MontoTotalDepositos_Femenino;
		//NumeroTraspasosRecibidos
		public Long NumeroTraspasosRecibidos_Masculino;
		public Long NumeroTraspasosRecibidos_Femenino;
		//MontoTotalTraspasosRecibidos
		public BigDecimal MontoTotalTraspasosRecibidos_Masculino;
		public BigDecimal MontoTotalTraspasosRecibidos_Femenino;
		//NumeroTraspasosRealizados
		public Long NumeroTraspasosRealizados_Masculino;
		public Long NumeroTraspasosRealizados_Femenino;
		//MontoTotalTraspasosRealizados
		public BigDecimal MontoTotalTraspasosRealizados_Masculino;
		public BigDecimal MontoTotalTraspasosRealizados_Femenino;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class FilaDepositosAhorroPrevisionalVoluntarioColectivoPorPlan {
	    public Long NumeroPolizaRunFondoCodigoPlan;
	    public String Serie;
	    public Long NumeroContratosApvc;
	    //NumeroCuentasIndividualesApvcVigentes
	    public Long NumeroCuentasIndividualesApvcVigentes_LetraA_Masculino;
	    public Long NumeroCuentasIndividualesApvcVigentes_LetraA_Femenino;
	    public Long NumeroCuentasIndividualesApvcVigentes_Total_Masculino;
	    public Long NumeroCuentasIndividualesApvcVigentes_Total_Femenino;
	    //NumeroAseguradosParticipesAportantesClientesPlan
	    public Long NumeroAseguradosParticipesAportantesClientesPlan_LetraA_Masculino;
	    public Long NumeroAseguradosParticipesAportantesClientesPlan_LetraA_Femenino;
	    public Long NumeroAseguradosParticipesAportantesClientesPlan_Total_Masculino;
	    public Long NumeroAseguradosParticipesAportantesClientesPlan_Total_Femenino;
	    //SaldoTotalAcumulado
	    public BigDecimal SaldoTotalAcumulado_Empleador_Masculino;
	    public BigDecimal SaldoTotalAcumulado_Empleador_Femenino;
	    public BigDecimal SaldoTotalAcumulado_Trabajador_LetraA_Masculino;
	    public BigDecimal SaldoTotalAcumulado_Trabajador_LetraA_Femenino;
	    public BigDecimal SaldoTotalAcumulado_Trabajador_LetraB_Masculino;
	    public BigDecimal SaldoTotalAcumulado_Trabajador_LetraB_Femenino;
	    //Bonificacion
	    public BigDecimal Bonificacion_LetraA_Masculino;
	    public BigDecimal Bonificacion_LetraA_Femenino;
	    //NumeroAportes
	    public Long NumeroAportes_Empleador_Masculino;
	    public Long NumeroAportes_Empleador_Femenino;
	    public Long NumeroAportes_Trabajador_LetraA_Masculino;
	    public Long NumeroAportes_Trabajador_LetraA_Femenino;
	    public Long NumeroAportes_Trabajador_LetraB_Masculino;
	    public Long NumeroAportes_Trabajador_LetraB_Femenino;
	    //MontoTotalAportes
	    public BigDecimal MontoTotalAportes_Empleador_Masculino;
	    public BigDecimal MontoTotalAportes_Empleador_Femenino;
	    public BigDecimal MontoTotalAportes_Trabajador_LetraA_Masculino;
	    public BigDecimal MontoTotalAportes_Trabajador_LetraA_Femenino;
	    public BigDecimal MontoTotalAportes_Trabajador_LetraB_Masculino;
	    public BigDecimal MontoTotalAportes_Trabajador_LetraB_Femenino;
	    //NumeroTraspasosRecibidos
	    public Long NumeroTraspasosRecibidos_Empleador_Masculino;
	    public Long NumeroTraspasosRecibidos_Empleador_Femenino;
	    public Long NumeroTraspasosRecibidos_Trabajador_Masculino;
	    public Long NumeroTraspasosRecibidos_Trabajador_Femenino;
	    //MontoTotalTraspasosRecibidos
	    public BigDecimal MontoTotalTraspasosRecibidos_Empleador_Masculino;
	    public BigDecimal MontoTotalTraspasosRecibidos_Empleador_Femenino;
	    public BigDecimal MontoTotalTraspasosRecibidos_Trabajador_Masculino;
	    public BigDecimal MontoTotalTraspasosRecibidos_Trabajador_Femenino;
	    //NumeroRetiros
	    public Long NumeroRetiros_Empleador_Masculino;
	    public Long NumeroRetiros_Empleador_Femenino;
	    public Long NumeroRetiros_Trabajador_LetraA_Masculino;
	    public Long NumeroRetiros_Trabajador_LetraA_Femenino;
	    public Long NumeroRetiros_Trabajador_LetraB_Masculino;
	    public Long NumeroRetiros_Trabajador_LetraB_Femenino;
	    //MontoTotalRetiros
	    public BigDecimal MontoTotalRetiros_Empleador_Masculino;
	    public BigDecimal MontoTotalRetiros_Empleador_Femenino;
	    public BigDecimal MontoTotalRetiros_Trabajador_LetraA_Masculino;
	    public BigDecimal MontoTotalRetiros_Trabajador_LetraA_Femenino;
	    public BigDecimal MontoTotalRetiros_Trabajador_LetraB_Masculino;
	    public BigDecimal MontoTotalRetiros_Trabajador_LetraB_Femenino;
	    //NumeroTraspasosRealizados
	    public Long NumeroTraspasosRealizados_Empleador_Masculino;
	    public Long NumeroTraspasosRealizados_Empleador_Femenino;
	    public Long NumeroTraspasosRealizados_Trabajador_Masculino;
	    public Long NumeroTraspasosRealizados_Trabajador_Femenino;
	    //MontoTotalTraspasosRealizados
	    public BigDecimal MontoTotalTraspasosRealizados_Empleador_Masculino;
	    public BigDecimal MontoTotalTraspasosRealizados_Empleador_Femenino;
	    public BigDecimal MontoTotalTraspasosRealizados_Trabajador_Masculino;
	    public BigDecimal MontoTotalTraspasosRealizados_Trabajador_Femenino;
	    
		public String toString() {
			return Utils.printJson(this, true);
		}
	}

	public static class CantidadCuentasVigentesAPV {
		
		public Long CantidadCuentas;
		public String Codigo;
		public Long IdPlanTipoInversion;
		public String Sexo;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}

	public static class CantidadClientesVigentesAPV {

		public Long CantidadCuentas;
		public Long CantidadClientes;
		public String Codigo;
		public Long IdPlanTipoInversion;
		public String Sexo;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}

	public static class SaldoAcumuladoAPV {

		public Long IdPlanTipoInversion;
		public String Sexo;
		public String Codigo;
		public BigDecimal SaldoAcumulado;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}

	public static class ParametroFinanciero {

		public String IdParametroFinanciero;
		public BigDecimal Valor;
		public String IdMonedaValor;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}

	public static class ParametroSaldoCuentaInversion {

		public String FechaHasta;
		public BigDecimal ValorDolar;
		public Long SecuenciaSolicitud;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class ParametroSaldoAcumuladoDetalle {

		public Long SecuenciaSolicitud;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class ParametroSaldoCustodiaDetalle {

		public Long SecuenciaSolicitud;
		public BigDecimal ValorDolar;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}

	public static class ParametroSaldoCustodiaPaso {

		public String Fecha;
		public String FechaFin;
		public String IdEmpresa;
		public Long SecuenciaSolicitud;
		public String TipoFechaConsulta;
		public Long IdPersonaInstitucional;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}

	public static class ParametroSaldoCustodia {

		public BigDecimal ValorDolar;
		public Long SecuenciaSolicitud;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}

	public static class IdPersonaInstitucional {

		public Long IdPersona;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}

	public static class IdPais {

		public Long IdPais;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	    
	private static FileLoader loader = new FileLoader();	
	private static String SQL_SELECT_DEPOSITOS_AHORRO_PREVISIONAL_VOLUNTARIO_PLAN = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_SEL_DepositosAhorroPrevisionalVoluntario_PorPlan.sql");
	private static String SQL_SELECT_DEPOSITOS_AHORRO_PREVISIONAL_VOLUNTARIO_TRASPASOS_DETALLE = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_SEL_DepositosAhorroPrevisionalVoluntario_TraspasosDetalle.sql");
	private static String SQL_SELECT_DEPOSITOS_AHORRO_PREVISIONAL_VOLUNTARIO_ESTADISTICA = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_SEL_DepositosAhorroPrevisionalVoluntario_NumeroPersonasSaldoEdadGenero.sql");
	private static String SQL_SELECT_DEPOSITOS_CONVENIDOS_PLAN = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_SEL_DepositosConvenidos_PorPlan.sql");
	private static String SQL_SELECT_DEPOSITOS_CONVENIDOS_TRASPASOS_DETALLE = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_SEL_DepositosConvenidos_TraspasosDetalle.sql");
	private static String SQL_SELECT_DEPOSITOS_CONVENIDOS_ESTADISTICA = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_SEL_DepositosConvenidos_NumeroPersonasSaldoEdadGenero.sql");
	private static String SQL_SELECT_DEPOSITOS_AHORRO_PREVISIONAL_VOLUNTARIO_COLECTIVO_PLAN = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_SEL_DepositosAhorroPrevisionalVoluntarioColectivo_PorPlan.sql");
	private static String SQL_SELECT_DEPOSITOS_AHORRO_PREVISIONAL_VOLUNTARIO_COLECTIVO_TRASPASOS_DETALLE = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_SEL_DepositosAhorroPrevisionalVoluntarioColectivo_TraspasosDetalle.sql");
	private static String SQL_SELECT_CANTIDAD_CLIENTES_APV = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_SEL_CantidadClientesAportantes.sql");
	private static String SQL_SELECT_SALDO_ACUMULADO = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_SEL_SaldoAcumulado.sql");
	private static String SQL_SELECT_PARAMETROS_FINANCIEROS = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_SEL_ParametroFinanciero.sql");
	private static String SQL_SELECT_ID_PAIS = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_SEL_IdPais.sql");
	
	private static String SQL_SELECT_CLIENTES_INSTITUCIONALES = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_SEL_ClientesInstitucionales.sql");
	private static String SQL_DELETE_SALDOS_CUENTA_INVERSION = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_DELETE_SaldosCuentaInversion.sql");
	private static String SQL_INSERT_SALDOS_CUENTA_INVERSION = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_INSERT_SaldosCuentaInversion.sql");
	private static String SQL_DELETE_SALDOS_CUSTODIA_PASO = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_DELETE_SaldosCustodiaPaso.sql");
	private static String SQL_INSERT_SALDOS_CUSTODIA_PASO = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_INSERT_SaldosCustodiaPaso.sql");
	private static String SQL_DELETE_SALDOS_CUSTODIA = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_DELETE_SaldosCustodia.sql");
	private static String SQL_INSERT_SALDOS_CUSTODIA = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_INSERT_SaldosCustodia.sql");
	
	private static String SQL_INSERT_SALDOACUMULADO_DETALLE = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_Insert_SaldoAcumuladoDetalle.sql");
	private static String SQL_INSERT_SALDOCUSTODIA_DETALLE = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_Insert_SaldoCustodiaDetalle.sql");
	
	
	/*******************
	 * metodo principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		GenerarAnexo1Circular20_Entrada in=gson.fromJson(jin.toString() ,GenerarAnexo1Circular20_Entrada.class);
		GenerarAnexo1Circular20_Salida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * logica del programa
	 **********************/
	
	public GenerarAnexo1Circular20_Salida logica(Connection con, GenerarAnexo1Circular20_Entrada entrada) throws Exception {
		GenerarAnexo1Circular20_Salida out = new GenerarAnexo1Circular20_Salida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_MENSAJE = "";
    	try {
    		String documentoIdentidadEmpresaDv = "";
    		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
    		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
    		
    		String [] parts = empresa.DocumentoIdentidad.split("-");
    		String documentoIdentidadEmpresa = parts[0];
    		
    		if (parts.length > 1){ // es rut con - else pasaporte
    			documentoIdentidadEmpresaDv = parts[1];
    		}
    		
    		final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	        final java.util.Date da = df.parse(entrada.p_FechaInicio.toString());
	        final Calendar cal = Calendar.getInstance();
	        cal.setTime(da);
	        
	        Integer year = cal.get(Calendar.YEAR);
	        Integer month = cal.get(Calendar.MONTH)+1;
	        
	        out.P_OUT_NOMBRE_ARCHIVO = "Circular20Anexo1".concat(year.toString()).concat(month.toString().concat(".xml"));
	        
	        LOGGER.info("GENERAR ANEXO 1 CIRCULAR 20 :: Inicio");
			//String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			
			SQL sql = new SQL();
			
			HashMap<String,Object> paramsParametrosFinancieros = new HashMap<String,Object>();
			paramsParametrosFinancieros.put("Fecha", entrada.p_FechaFin);
			
	        List<ParametroFinanciero> lstParametrosFinancieros = new ArrayList<ParametroFinanciero>();
	        lstParametrosFinancieros = sql.query(con, SQL_SELECT_PARAMETROS_FINANCIEROS, paramsParametrosFinancieros, ParametroFinanciero.class);
	        
	        BigDecimal ValorDolar = BigDecimal.valueOf(0);
	        if(lstParametrosFinancieros.size()>0)
	        	ValorDolar = lstParametrosFinancieros.stream().filter(s -> "USD".equalsIgnoreCase(s.IdParametroFinanciero)).findFirst().get().Valor;	
			
			LOGGER.info("GenerarAnexo1Circular20: Limpia tabla temporal de SaldosCuentaInversion, SaldosCustodia_paso y SaldosCustodia");
			sql.batch(con, SQL_DELETE_SALDOS_CUENTA_INVERSION, new ArrayList<>(), Object.class);
			sql.batch(con, SQL_DELETE_SALDOS_CUSTODIA_PASO, new ArrayList<>(), Object.class);
			sql.batch(con, SQL_DELETE_SALDOS_CUSTODIA, new ArrayList<>(), Object.class);
			
			LOGGER.info("GenerarAnexo1Circular20: Inserta saldos en tablas temporales de SaldosCuentaInversion, SaldosCustodia_paso y SaldosCustodia");
			
			//Se obtienen los idPersona de los institucionales a NO considerar en lo extraido de Custodia
			HashMap<String,Object> params_idCliente = new HashMap<String,Object>();
	        List<IdPersonaInstitucional> lstIdsInstitucionales = new ArrayList<IdPersonaInstitucional>();
	        
	        lstIdsInstitucionales = sql.query(con, SQL_SELECT_CLIENTES_INSTITUCIONALES, params_idCliente, IdPersonaInstitucional.class);
			
			List<ParametroSaldoCuentaInversion> paramsCuentaInversion = new ArrayList<ParametroSaldoCuentaInversion>();
			ParametroSaldoCuentaInversion paramCI = new ParametroSaldoCuentaInversion();
			paramCI.FechaHasta = entrada.p_FechaFin;
			paramCI.SecuenciaSolicitud= entrada.p_SecuenciaSolicitud;
			paramCI.ValorDolar = ValorDolar;
			paramsCuentaInversion.add(paramCI);
			
			sql.batch(con, SQL_INSERT_SALDOS_CUENTA_INVERSION,paramsCuentaInversion,ParametroSaldoCuentaInversion.class);
			
			//Obtiene ultimo dia habil
			HashMap<String,Object> params_idPais = new HashMap<String,Object>();
	        List<IdPais> IdPais = new ArrayList<IdPais>();
	        
	        IdPais = sql.query(con, SQL_SELECT_ID_PAIS, params_idPais, IdPais.class);
			
			
			cal.add(Calendar.MONTH, 1);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
            String FechaFinCustodia = dateFormat.format(cal.getTime());
	        
		    ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
		    InObtenerDiaAnterior inObtenerDiaAnterior = new InObtenerDiaAnterior();
		    inObtenerDiaAnterior.idPais = IdPais.get(0).IdPais;
		    inObtenerDiaAnterior.diaHabil = "S";
		    inObtenerDiaAnterior.soloCaeDiaHabil = "S";
		    InObtenerDiaAnterior.FechasIn fechaInicial = new InObtenerDiaAnterior.FechasIn();
		    fechaInicial.fechaInicial = FechaFinCustodia;
		    fechaInicial.numeroDias = 1L;
		    inObtenerDiaAnterior.fechas.add(fechaInicial);
		    OutObtenerDiaAnterior outObtenerDiaAnterior = obtenerDiaAnterior.logica(con, inObtenerDiaAnterior);
		    FechaFinCustodia = outObtenerDiaAnterior.fechas.get(0).fechaCalculada;
		    
		    //Fin obtiene ultimo dia habil
			
			List<ParametroSaldoCustodiaPaso> paramsSaldoCusPaso = new ArrayList<ParametroSaldoCustodiaPaso>();
			ParametroSaldoCustodiaPaso paramSCP = new ParametroSaldoCustodiaPaso();
			paramSCP.Fecha = FechaFinCustodia;
			paramSCP.FechaFin = entrada.p_FechaFin;
			paramSCP.IdEmpresa= entrada.p_IdEmpresa.toString();
			paramSCP.SecuenciaSolicitud = entrada.p_SecuenciaSolicitud;
			paramSCP.TipoFechaConsulta = "ADQ";
			if(lstIdsInstitucionales.size()>0)
				paramSCP.IdPersonaInstitucional = lstIdsInstitucionales.get(0).IdPersona;
				
			else
				paramSCP.IdPersonaInstitucional = -1L;
			paramsSaldoCusPaso.add(paramSCP);

			sql.batch(con, SQL_INSERT_SALDOS_CUSTODIA_PASO,paramsSaldoCusPaso,ParametroSaldoCustodiaPaso.class);
			
			List<ParametroSaldoCustodia> paramsSaldoCustodia = new ArrayList<ParametroSaldoCustodia>();
			ParametroSaldoCustodia paramSC = new ParametroSaldoCustodia();
			paramSC.SecuenciaSolicitud= entrada.p_SecuenciaSolicitud;
			paramSC.ValorDolar = ValorDolar;
			paramsSaldoCustodia.add(paramSC);
			
			sql.batch(con, SQL_INSERT_SALDOS_CUSTODIA,paramsSaldoCustodia,ParametroSaldoCustodia.class);
			
			LOGGER.info("GenerarAnexo1Circular20: Conteo Cantidad de cuentas y clientes apv hasta la fecha"+ entrada.p_FechaFin);
			
			HashMap<String,Object> params = new HashMap<String,Object>();
	        List<CantidadClientesVigentesAPV> lstCuentasClientesVigentesPorPlan = new ArrayList<CantidadClientesVigentesAPV>();
	        
	        lstCuentasClientesVigentesPorPlan = sql.query(con, SQL_SELECT_CANTIDAD_CLIENTES_APV, params, CantidadClientesVigentesAPV.class);
	        
	        Long NumeroCuentasApvVigentes_LetraA_Masculino_1 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "M".equalsIgnoreCase(s.Sexo) && "APV-A".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 1L).mapToLong(s -> s.CantidadCuentas).sum());			 
	        Long NumeroCuentasApvVigentes_LetraA_Femenino_1 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "F".equalsIgnoreCase(s.Sexo) && "APV-A".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 1L).mapToLong(s -> s.CantidadCuentas).sum());
	        Long NumeroCuentasApvVigentes_Total_Masculino_1 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "M".equalsIgnoreCase(s.Sexo) && "TOTAL".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 1L).mapToLong(s -> s.CantidadCuentas).sum());
	        Long NumeroCuentasApvVigentes_Total_Femenino_1 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "F".equalsIgnoreCase(s.Sexo) && "TOTAL".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 1L).mapToLong(s -> s.CantidadCuentas).sum());
	        Long NumeroCuentasApvVigentes_LetraA_Masculino_2 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "M".equalsIgnoreCase(s.Sexo) && "APV-A".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 2L).mapToLong(s -> s.CantidadCuentas).sum());
	        Long NumeroCuentasApvVigentes_LetraA_Femenino_2 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "F".equalsIgnoreCase(s.Sexo) && "APV-A".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 2L).mapToLong(s -> s.CantidadCuentas).sum());
	        Long NumeroCuentasApvVigentes_Total_Masculino_2 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "M".equalsIgnoreCase(s.Sexo) && "TOTAL".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 2L).mapToLong(s -> s.CantidadCuentas).sum());
	        Long NumeroCuentasApvVigentes_Total_Femenino_2 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "F".equalsIgnoreCase(s.Sexo) && "TOTAL".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 2L).mapToLong(s -> s.CantidadCuentas).sum());
	        
	        Long NumeroClientesApvVigentes_LetraA_Masculino_1 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "M".equalsIgnoreCase(s.Sexo) && "APV-A".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 1L).mapToLong(s -> s.CantidadClientes).sum());
	        Long NumeroClientesApvVigentes_LetraA_Femenino_1 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "F".equalsIgnoreCase(s.Sexo) && "APV-A".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 1L).mapToLong(s -> s.CantidadClientes).sum());
	        Long NumeroClientesApvVigentes_Total_Masculino_1 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "M".equalsIgnoreCase(s.Sexo) && "TOTAL".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 1L).mapToLong(s -> s.CantidadClientes).sum());
	        Long NumeroClientesApvVigentes_Total_Femenino_1 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "F".equalsIgnoreCase(s.Sexo) && "TOTAL".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 1L).mapToLong(s -> s.CantidadClientes).sum());
	        Long NumeroClientesApvVigentes_LetraA_Masculino_2 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "M".equalsIgnoreCase(s.Sexo) && "APV-A".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 2L).mapToLong(s -> s.CantidadClientes).sum());
	        Long NumeroClientesApvVigentes_LetraA_Femenino_2 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "F".equalsIgnoreCase(s.Sexo) && "APV-A".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 2L).mapToLong(s -> s.CantidadClientes).sum());
	        Long NumeroClientesApvVigentes_Total_Masculino_2 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "M".equalsIgnoreCase(s.Sexo) && "TOTAL".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 2L).mapToLong(s -> s.CantidadClientes).sum());
	        Long NumeroClientesApvVigentes_Total_Femenino_2 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "F".equalsIgnoreCase(s.Sexo) && "TOTAL".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 2L).mapToLong(s -> s.CantidadClientes).sum());
	        
	        //DC-R, DC-AL y DC-DL
	        Long NumeroCuentasApvVigentes_Masculino_DC_1 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "M".equalsIgnoreCase(s.Sexo) && "DC".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 1L).mapToLong(s -> s.CantidadCuentas).sum());			 
	        Long NumeroCuentasApvVigentes_Femenino_DC_1 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "F".equalsIgnoreCase(s.Sexo) && "DC".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 1L).mapToLong(s -> s.CantidadCuentas).sum());
	        Long NumeroCuentasApvVigentes_Masculino_DC_2 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "M".equalsIgnoreCase(s.Sexo) && "DC".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 2L).mapToLong(s -> s.CantidadCuentas).sum());
	        Long NumeroCuentasApvVigentes_Femenino_DC_2 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "F".equalsIgnoreCase(s.Sexo) && "DC".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 2L).mapToLong(s -> s.CantidadCuentas).sum());

	        Long NumeroClientesApvVigentes_Masculino_DC_1 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "M".equalsIgnoreCase(s.Sexo) && "DC".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 1L).mapToLong(s -> s.CantidadClientes).sum());
	        Long NumeroClientesApvVigentes_Femenino_DC_1 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "F".equalsIgnoreCase(s.Sexo) && "DC".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 1L).mapToLong(s -> s.CantidadClientes).sum());
	        Long NumeroClientesApvVigentes_Masculino_DC_2 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "M".equalsIgnoreCase(s.Sexo) && "DC".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 2L).mapToLong(s -> s.CantidadClientes).sum());
	        Long NumeroClientesApvVigentes_Femenino_DC_2 = validaLongNulo(lstCuentasClientesVigentesPorPlan.stream().filter(s -> "F".equalsIgnoreCase(s.Sexo) && "DC".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 2L).mapToLong(s -> s.CantidadClientes).sum());

	        //SaldoAcumuladoAPV
	        List<SaldoAcumuladoAPV> lstSaldosAcumuladosPorPlan = new ArrayList<SaldoAcumuladoAPV>();
	        lstSaldosAcumuladosPorPlan = sql.query(con, SQL_SELECT_SALDO_ACUMULADO, params, SaldoAcumuladoAPV.class);
	       
	        List<ParametroSaldoAcumuladoDetalle> paramsSaldoAcumuladoDetalle = new ArrayList<ParametroSaldoAcumuladoDetalle>();
	        ParametroSaldoAcumuladoDetalle paramsSaldoAcumuladoDeta = new ParametroSaldoAcumuladoDetalle();
	        paramsSaldoAcumuladoDeta.SecuenciaSolicitud= entrada.p_SecuenciaSolicitud;
	        paramsSaldoAcumuladoDetalle.add(paramsSaldoAcumuladoDeta);
	        
	        sql.batch(con,SQL_INSERT_SALDOACUMULADO_DETALLE,paramsSaldoAcumuladoDetalle,ParametroSaldoAcumuladoDetalle.class);
	        
	        List<ParametroSaldoCustodiaDetalle> paramsSaldoCustodiaDetalle = new ArrayList<ParametroSaldoCustodiaDetalle>();
	        ParametroSaldoCustodiaDetalle paramsSaldoCustodiaDet = new ParametroSaldoCustodiaDetalle();
	        paramsSaldoCustodiaDet.SecuenciaSolicitud= entrada.p_SecuenciaSolicitud;
	        paramsSaldoCustodiaDet.ValorDolar = ValorDolar;
	        paramsSaldoCustodiaDetalle.add(paramsSaldoCustodiaDet);
	        sql.batch(con,SQL_INSERT_SALDOCUSTODIA_DETALLE,paramsSaldoCustodiaDetalle,ParametroSaldoCustodiaDetalle.class);
	        
	        
	        BigDecimal Bonificacion_Masculino_1 = validaBigDecimalNulo(lstSaldosAcumuladosPorPlan.stream().filter(s -> "M".equalsIgnoreCase(s.Sexo) && "BF".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 1L).mapToDouble(s -> s.SaldoAcumulado.doubleValue()).sum());
	        BigDecimal Bonificacion_Femenino_1 = validaBigDecimalNulo(lstSaldosAcumuladosPorPlan.stream().filter(s -> "F".equalsIgnoreCase(s.Sexo) && "BF".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 1L).mapToDouble(s -> s.SaldoAcumulado.doubleValue()).sum());
	        BigDecimal Bonificacion_Masculino_2 = validaBigDecimalNulo(lstSaldosAcumuladosPorPlan.stream().filter(s -> "M".equalsIgnoreCase(s.Sexo) && "BF".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 2L).mapToDouble(s -> s.SaldoAcumulado.doubleValue()).sum());
	        BigDecimal Bonificacion_Femenino_2 = validaBigDecimalNulo(lstSaldosAcumuladosPorPlan.stream().filter(s -> "F".equalsIgnoreCase(s.Sexo) && "BF".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 2L).mapToDouble(s -> s.SaldoAcumulado.doubleValue()).sum());
	        BigDecimal SaldoTotalAcumulado_LetraA_Masculino_1 = validaBigDecimalNulo(lstSaldosAcumuladosPorPlan.stream().filter(s -> "M".equalsIgnoreCase(s.Sexo) && "APV-A".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 1L).mapToDouble(s -> s.SaldoAcumulado.doubleValue()).sum()).add(Bonificacion_Masculino_1);
	        BigDecimal SaldoTotalAcumulado_LetraA_Femenino_1 = validaBigDecimalNulo(lstSaldosAcumuladosPorPlan.stream().filter(s -> "F".equalsIgnoreCase(s.Sexo) && "APV-A".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 1L).mapToDouble(s -> s.SaldoAcumulado.doubleValue()).sum()).add(Bonificacion_Femenino_1);
	        BigDecimal SaldoTotalAcumulado_LetraB_Masculino_1 = validaBigDecimalNulo(lstSaldosAcumuladosPorPlan.stream().filter(s -> "M".equalsIgnoreCase(s.Sexo) && "APV-B".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 1L).mapToDouble(s -> s.SaldoAcumulado.doubleValue()).sum());
	        BigDecimal SaldoTotalAcumulado_LetraB_Femenino_1 = validaBigDecimalNulo(lstSaldosAcumuladosPorPlan.stream().filter(s -> "F".equalsIgnoreCase(s.Sexo) && "APV-B".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 1L).mapToDouble(s -> s.SaldoAcumulado.doubleValue()).sum());
	        BigDecimal SaldoTotalAcumulado_LetraA_Masculino_2 = validaBigDecimalNulo(lstSaldosAcumuladosPorPlan.stream().filter(s -> "M".equalsIgnoreCase(s.Sexo) && "APV-A".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 2L).mapToDouble(s -> s.SaldoAcumulado.doubleValue()).sum()).add(Bonificacion_Masculino_2);
	        BigDecimal SaldoTotalAcumulado_LetraA_Femenino_2 = validaBigDecimalNulo(lstSaldosAcumuladosPorPlan.stream().filter(s -> "F".equalsIgnoreCase(s.Sexo) && "APV-A".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 2L).mapToDouble(s -> s.SaldoAcumulado.doubleValue()).sum()).add(Bonificacion_Femenino_2);
	        BigDecimal SaldoTotalAcumulado_LetraB_Masculino_2 = validaBigDecimalNulo(lstSaldosAcumuladosPorPlan.stream().filter(s -> "M".equalsIgnoreCase(s.Sexo) && "APV-B".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 2L).mapToDouble(s -> s.SaldoAcumulado.doubleValue()).sum());
	        BigDecimal SaldoTotalAcumulado_LetraB_Femenino_2 = validaBigDecimalNulo(lstSaldosAcumuladosPorPlan.stream().filter(s -> "F".equalsIgnoreCase(s.Sexo) && "APV-B".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 2L).mapToDouble(s -> s.SaldoAcumulado.doubleValue()).sum());
	        
	        //DC-R, DC-AL y DC-DL

	        BigDecimal SaldoTotalAcumulado_Masculino_DC_1 = validaBigDecimalNulo(lstSaldosAcumuladosPorPlan.stream().filter(s -> "M".equalsIgnoreCase(s.Sexo) && "DC".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 1L).mapToDouble(s -> s.SaldoAcumulado.doubleValue()).sum());
	        BigDecimal SaldoTotalAcumulado_Femenino_DC_1 = validaBigDecimalNulo(lstSaldosAcumuladosPorPlan.stream().filter(s -> "F".equalsIgnoreCase(s.Sexo) && "DC".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 1L).mapToDouble(s -> s.SaldoAcumulado.doubleValue()).sum());
	        BigDecimal SaldoTotalAcumulado_Masculino_DC_2 = validaBigDecimalNulo(lstSaldosAcumuladosPorPlan.stream().filter(s -> "M".equalsIgnoreCase(s.Sexo) && "DC".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 2L).mapToDouble(s -> s.SaldoAcumulado.doubleValue()).sum());
	        BigDecimal SaldoTotalAcumulado_Femenino_DC_2 = validaBigDecimalNulo(lstSaldosAcumuladosPorPlan.stream().filter(s -> "F".equalsIgnoreCase(s.Sexo) && "DC".equalsIgnoreCase(s.Codigo) && s.IdPlanTipoInversion == 2L).mapToDouble(s -> s.SaldoAcumulado.doubleValue()).sum());
	        
	        LOGGER.info("GenerarAnexo1Circular20: Obtencion de data para la secuenciaSolicitud: "+ entrada.p_SecuenciaSolicitud.toString());
	        HashMap<String,Object> a = new HashMap<String,Object>();
	        a.put("SecuenciaSolicitud", entrada.p_SecuenciaSolicitud);
	        a.put("CantCuentas_A_M_1", NumeroCuentasApvVigentes_LetraA_Masculino_1);
	        a.put("CantCuentas_A_F_1", NumeroCuentasApvVigentes_LetraA_Femenino_1);
	        a.put("CantCuentas_T_M_1", NumeroCuentasApvVigentes_Total_Masculino_1);
	        a.put("CantCuentas_T_F_1", NumeroCuentasApvVigentes_Total_Femenino_1);
	        a.put("CantCuentas_A_M_2", NumeroCuentasApvVigentes_LetraA_Masculino_2);
	        a.put("CantCuentas_A_F_2", NumeroCuentasApvVigentes_LetraA_Femenino_2);
	        a.put("CantCuentas_T_M_2", NumeroCuentasApvVigentes_Total_Masculino_2);
	        a.put("CantCuentas_T_F_2", NumeroCuentasApvVigentes_Total_Femenino_2);

	        a.put("CantClientes_A_M_1", NumeroClientesApvVigentes_LetraA_Masculino_1);
	        a.put("CantClientes_A_F_1", NumeroClientesApvVigentes_LetraA_Femenino_1);
	        a.put("CantClientes_T_M_1", NumeroClientesApvVigentes_Total_Masculino_1);
	        a.put("CantClientes_T_F_1", NumeroClientesApvVigentes_Total_Femenino_1);
	        a.put("CantClientes_A_M_2", NumeroClientesApvVigentes_LetraA_Masculino_2);
	        a.put("CantClientes_A_F_2", NumeroClientesApvVigentes_LetraA_Femenino_2);
	        a.put("CantClientes_T_M_2", NumeroClientesApvVigentes_Total_Masculino_2);
	        a.put("CantClientes_T_F_2", NumeroClientesApvVigentes_Total_Femenino_2);

	        a.put("SaldoAcumulado_A_M_1", SaldoTotalAcumulado_LetraA_Masculino_1);
	        a.put("SaldoAcumulado_A_F_1", SaldoTotalAcumulado_LetraA_Femenino_1);
	        a.put("SaldoAcumulado_B_M_1", SaldoTotalAcumulado_LetraB_Masculino_1);
	        a.put("SaldoAcumulado_B_F_1", SaldoTotalAcumulado_LetraB_Femenino_1);
	        a.put("SaldoAcumulado_BF_M_1", Bonificacion_Masculino_1);
	        a.put("SaldoAcumulado_BF_F_1", Bonificacion_Femenino_1);
	        a.put("SaldoAcumulado_A_M_2", SaldoTotalAcumulado_LetraA_Masculino_2);
	        a.put("SaldoAcumulado_A_F_2", SaldoTotalAcumulado_LetraA_Femenino_2);
	        a.put("SaldoAcumulado_B_M_2", SaldoTotalAcumulado_LetraB_Masculino_2);
	        a.put("SaldoAcumulado_B_F_2", SaldoTotalAcumulado_LetraB_Femenino_2);
	        a.put("SaldoAcumulado_BF_M_2", Bonificacion_Masculino_2);
	        a.put("SaldoAcumulado_BF_F_2", Bonificacion_Femenino_2);
	        
	        List<FilaDepositosAhorroPrevisionalVoluntarioPorPlan> lstFilaDepositosAhorroPrevisionalVoluntarioPorPlan = new ArrayList<FilaDepositosAhorroPrevisionalVoluntarioPorPlan>();
	        lstFilaDepositosAhorroPrevisionalVoluntarioPorPlan = sql.query(con, SQL_SELECT_DEPOSITOS_AHORRO_PREVISIONAL_VOLUNTARIO_PLAN, a, FilaDepositosAhorroPrevisionalVoluntarioPorPlan.class);
	       
	        HashMap<String,Object> b = new HashMap<String,Object>();
	        b.put("SecuenciaSolicitud", entrada.p_SecuenciaSolicitud);
	        List<TraspasosDetalle> lstDepositosAhorroPrevisionalVoluntarioTraspasosDetalle = new ArrayList<TraspasosDetalle>();
	        lstDepositosAhorroPrevisionalVoluntarioTraspasosDetalle = sql.query(con, SQL_SELECT_DEPOSITOS_AHORRO_PREVISIONAL_VOLUNTARIO_TRASPASOS_DETALLE, b, TraspasosDetalle.class);
	        
	        HashMap<String,Object> c = new HashMap<String,Object>();
			c.put("Fecha", entrada.p_FechaFin);
	        List<NumeroPersonasSaldoEdadGenero> lstDepositosAhorroPrevisionalVoluntarioNumeroPersonasSaldoEdadGenero = new ArrayList<NumeroPersonasSaldoEdadGenero>();
	        lstDepositosAhorroPrevisionalVoluntarioNumeroPersonasSaldoEdadGenero = sql.query(con, SQL_SELECT_DEPOSITOS_AHORRO_PREVISIONAL_VOLUNTARIO_ESTADISTICA, c, NumeroPersonasSaldoEdadGenero.class);
	        
	        HashMap<String,Object> d = new HashMap<String,Object>();
	        d.put("SecuenciaSolicitud", entrada.p_SecuenciaSolicitud);
	        d.put("CantCuentas_DC_M_1", NumeroCuentasApvVigentes_Masculino_DC_1);
	        d.put("CantCuentas_DC_F_1", NumeroCuentasApvVigentes_Femenino_DC_1);
	        d.put("CantCuentas_DC_M_2", NumeroCuentasApvVigentes_Masculino_DC_2);
	        d.put("CantCuentas_DC_F_2", NumeroCuentasApvVigentes_Femenino_DC_2);

	        d.put("CantClientes_DC_M_1", NumeroClientesApvVigentes_Masculino_DC_1);
	        d.put("CantClientes_DC_F_1", NumeroClientesApvVigentes_Femenino_DC_1);
	        d.put("CantClientes_DC_M_2", NumeroClientesApvVigentes_Masculino_DC_2);
	        d.put("CantClientes_DC_F_2", NumeroClientesApvVigentes_Femenino_DC_2);

	        d.put("SaldoAcumulado_DC_M_1", SaldoTotalAcumulado_Masculino_DC_1);
	        d.put("SaldoAcumulado_DC_F_1", SaldoTotalAcumulado_Femenino_DC_1);
	        d.put("SaldoAcumulado_DC_M_2", SaldoTotalAcumulado_Masculino_DC_2);
	        d.put("SaldoAcumulado_DC_F_2", SaldoTotalAcumulado_Femenino_DC_2);
	        List<FilaDepositosConvenidosPorPlan> lstFilaDepositosConvenidosPorPlan = new ArrayList<FilaDepositosConvenidosPorPlan>();
	        lstFilaDepositosConvenidosPorPlan = sql.query(con, SQL_SELECT_DEPOSITOS_CONVENIDOS_PLAN, d, FilaDepositosConvenidosPorPlan.class);
	        
	        HashMap<String,Object> e = new HashMap<String,Object>();
	        e.put("SecuenciaSolicitud", entrada.p_SecuenciaSolicitud);
	        List<TraspasosDetalle> lstDepositosConvenidosTraspasosDetalle = new ArrayList<TraspasosDetalle>();
	        lstDepositosConvenidosTraspasosDetalle = sql.query(con, SQL_SELECT_DEPOSITOS_CONVENIDOS_TRASPASOS_DETALLE, e, TraspasosDetalle.class);
	        
	        HashMap<String,Object> g = new HashMap<String,Object>();
			g.put("Fecha", entrada.p_FechaFin);
	        List<NumeroPersonasSaldoEdadGenero> lstDepositosConvenidosNumeroPersonasSaldoEdadGenero = new ArrayList<NumeroPersonasSaldoEdadGenero>();
	        lstDepositosConvenidosNumeroPersonasSaldoEdadGenero = sql.query(con, SQL_SELECT_DEPOSITOS_CONVENIDOS_ESTADISTICA, g, NumeroPersonasSaldoEdadGenero.class);
	        
	        HashMap<String,Object> h = new HashMap<String,Object>();
	        //h.put("SecuenciaSolicitud", entrada.p_SecuenciaSolicitud);
	        List<FilaDepositosAhorroPrevisionalVoluntarioColectivoPorPlan> lstFilaDepositosAhorroPrevisionalVoluntarioColectivoPorPlan = new ArrayList<FilaDepositosAhorroPrevisionalVoluntarioColectivoPorPlan>();
	        lstFilaDepositosAhorroPrevisionalVoluntarioColectivoPorPlan = sql.query(con, SQL_SELECT_DEPOSITOS_AHORRO_PREVISIONAL_VOLUNTARIO_COLECTIVO_PLAN, h, FilaDepositosAhorroPrevisionalVoluntarioColectivoPorPlan.class);
	        
	        HashMap<String,Object> i = new HashMap<String,Object>();
	        //i.put("SecuenciaSolicitud", entrada.p_SecuenciaSolicitud);
	        List<TraspasosDetalle> lstDepositosAPVColectivoTraspasosDetalle = new ArrayList<TraspasosDetalle>();
	        lstDepositosAPVColectivoTraspasosDetalle = sql.query(con, SQL_SELECT_DEPOSITOS_AHORRO_PREVISIONAL_VOLUNTARIO_COLECTIVO_TRASPASOS_DETALLE, i, TraspasosDetalle.class);
	        
	        
	        //******************************************************************************************************
	        //******************************************************************************************************
	        //******************************************************************************************************
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        	factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        	factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        	factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        	factory.setExpandEntityReferences(false);
        	DocumentBuilder builder        = factory.newDocumentBuilder();
        	Document doc                   = builder.newDocument();
        	
        	doc.setXmlVersion("1.0");
        	
        	Element rootElement = doc.createElement("APV");
        	rootElement.setAttribute("xmlns", "http://www.w3.org/2001/XMLSchema");
        	rootElement.setAttribute("xsi:schemaLocation", "http://www.w3.org/2001/XMLSchema ApvInformacion.xsd");
        	rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        	doc.appendChild(rootElement);
        	
        	// PeriodoInformacion
    		Element periodoInformacion = doc.createElement("PeriodoInformacion");
    		rootElement.appendChild(periodoInformacion);
    		
	    		Element mes = doc.createElement("Mes");
	    		mes.appendChild(doc.createTextNode(month.toString()));
	    		periodoInformacion.appendChild(mes);
	    		
	    		Element ano = doc.createElement("Ano");
	    		ano.appendChild(doc.createTextNode(year.toString()));
	    		periodoInformacion.appendChild(ano);
    		
    		// Identificacion
    		Element identificacion = doc.createElement("Identificacion");
    		rootElement.appendChild(identificacion);
    		
	    		Element rut = doc.createElement("Rut");
	    		rut.appendChild(doc.createTextNode(documentoIdentidadEmpresa));
	    		identificacion.appendChild(rut);
	    		
	    		Element dv = doc.createElement("Dv");
	    		dv.appendChild(doc.createTextNode(documentoIdentidadEmpresaDv));
	    		identificacion.appendChild(dv);
		    
	    	//En el caso que no existan planes de APV aprobados, este item no se debe informar.
	    	if (!lstFilaDepositosAhorroPrevisionalVoluntarioPorPlan.isEmpty()){
	    		// DepositosAhorroPrevisionalVoluntario
	    		Element depositosAPV = doc.createElement("DepositosAhorroPrevisionalVoluntario");
	    		rootElement.appendChild(depositosAPV);
		    		
	    		for (FilaDepositosAhorroPrevisionalVoluntarioPorPlan f : lstFilaDepositosAhorroPrevisionalVoluntarioPorPlan) {
	    			// FilaDepositosAhorroPrevisionalVoluntarioPorPlan
	        		Element filaDepositosAPVPorPlan = doc.createElement("FilaDepositosAhorroPrevisionalVoluntarioPorPlan");
	        		depositosAPV.appendChild(filaDepositosAPVPorPlan);
	        		
		        		// NumeroPolizaRunFondoCodigoPlan
		        		Element numeroPolizaRunFondoCodigoPlan = doc.createElement("NumeroPolizaRunFondoCodigoPlan");
		        		numeroPolizaRunFondoCodigoPlan.appendChild(doc.createTextNode(f.NumeroPolizaRunFondoCodigoPlan.toString()));
		        		filaDepositosAPVPorPlan.appendChild(numeroPolizaRunFondoCodigoPlan);
		        		
		        		// Serie
		        		Element serie = doc.createElement("Serie");
		        		serie.appendChild(doc.createTextNode(f.Serie));
		        		filaDepositosAPVPorPlan.appendChild(serie);
		        		
		        		//NumeroCuentasApvVigentes
		        		Element numeroCuentasApvVigentes = doc.createElement("NumeroCuentasApvVigentes");
		        		filaDepositosAPVPorPlan.appendChild(numeroCuentasApvVigentes);
		        		
			        		// NumeroCuentasApvVigentes Letra A
		        			Element numeroCuentasApvVigentesLetraA = doc.createElement("LetraA");
		        			numeroCuentasApvVigentes.appendChild(numeroCuentasApvVigentesLetraA);
			        		
				        		// NumeroCuentasApvVigentes Letra A Masculino
			        			Element numeroCuentasApvVigentesLetraAMasculino = doc.createElement("Masculino");
			        			numeroCuentasApvVigentesLetraAMasculino.appendChild(doc.createTextNode(f.NumeroCuentasApvVigentes_LetraA_Masculino.toString()));
			        			numeroCuentasApvVigentesLetraA.appendChild(numeroCuentasApvVigentesLetraAMasculino);
			        			
			        			// NumeroCuentasApvVigentes Letra A Femenino
			        			Element numeroCuentasApvVigentesLetraAFemenino = doc.createElement("Femenino");
			        			numeroCuentasApvVigentesLetraAFemenino.appendChild(doc.createTextNode(f.NumeroCuentasApvVigentes_LetraA_Femenino.toString()));
			        			numeroCuentasApvVigentesLetraA.appendChild(numeroCuentasApvVigentesLetraAFemenino);
			        			
		        			// NumeroCuentasApvVigentes Total
		        			Element numeroCuentasApvVigentesTotal = doc.createElement("Total");
		        			numeroCuentasApvVigentes.appendChild(numeroCuentasApvVigentesTotal);
			        		
				        		// NumeroCuentasApvVigentes Total Masculino
			        			Element numeroCuentasApvVigentesTotalMasculino = doc.createElement("Masculino");
			        			numeroCuentasApvVigentesTotalMasculino.appendChild(doc.createTextNode(f.NumeroCuentasApvVigentes_Total_Masculino.toString()));
			        			numeroCuentasApvVigentesTotal.appendChild(numeroCuentasApvVigentesTotalMasculino);
			        			
			        			// NumeroCuentasApvVigentes Total Femenino
			        			Element numeroCuentasApvVigentesTotalFemenino = doc.createElement("Femenino");
			        			numeroCuentasApvVigentesTotalFemenino.appendChild(doc.createTextNode(f.NumeroCuentasApvVigentes_Total_Femenino.toString()));
			        			numeroCuentasApvVigentesTotal.appendChild(numeroCuentasApvVigentesTotalFemenino);
			        	
	        			//NumeroAseguradosParticipesAportantesClientesPlan
		        		Element numeroAseguradosParticipesAportantesClientesPlan = doc.createElement("NumeroAseguradosParticipesAportantesClientesPlan");
		        		filaDepositosAPVPorPlan.appendChild(numeroAseguradosParticipesAportantesClientesPlan);
		        		
			        		// NumeroAseguradosParticipesAportantesClientesPlan Letra A
		        			Element numeroAseguradosParticipesAportantesClientesPlanLetraA = doc.createElement("LetraA");
		        			numeroAseguradosParticipesAportantesClientesPlan.appendChild(numeroAseguradosParticipesAportantesClientesPlanLetraA);
			        		
				        		// NumeroAseguradosParticipesAportantesClientesPlan Letra A Masculino
			        			Element numeroAseguradosParticipesAportantesClientesPlanLetraAMasculino = doc.createElement("Masculino");
			        			numeroAseguradosParticipesAportantesClientesPlanLetraAMasculino.appendChild(doc.createTextNode(f.NumeroAseguradosParticipesAportantesClientesPlan_LetraA_Masculino.toString()));
			        			numeroAseguradosParticipesAportantesClientesPlanLetraA.appendChild(numeroAseguradosParticipesAportantesClientesPlanLetraAMasculino);
			        			
			        			// NumeroAseguradosParticipesAportantesClientesPlan Letra A Femenino
			        			Element numeroAseguradosParticipesAportantesClientesPlanLetraAFemenino = doc.createElement("Femenino");
			        			numeroAseguradosParticipesAportantesClientesPlanLetraAFemenino.appendChild(doc.createTextNode(f.NumeroAseguradosParticipesAportantesClientesPlan_LetraA_Femenino.toString()));
			        			numeroAseguradosParticipesAportantesClientesPlanLetraA.appendChild(numeroAseguradosParticipesAportantesClientesPlanLetraAFemenino);
			        			
		        			// NumeroAseguradosParticipesAportantesClientesPlan Total
		        			Element numeroAseguradosParticipesAportantesClientesPlanTotal = doc.createElement("Total");
		        			numeroAseguradosParticipesAportantesClientesPlan.appendChild(numeroAseguradosParticipesAportantesClientesPlanTotal);
			        		
				        		// NumeroAseguradosParticipesAportantesClientesPlan Total Masculino
			        			Element numeroAseguradosParticipesAportantesClientesPlanTotalMasculino = doc.createElement("Masculino");
			        			numeroAseguradosParticipesAportantesClientesPlanTotalMasculino.appendChild(doc.createTextNode(f.NumeroAseguradosParticipesAportantesClientesPlan_Total_Masculino.toString()));
			        			numeroAseguradosParticipesAportantesClientesPlanTotal.appendChild(numeroAseguradosParticipesAportantesClientesPlanTotalMasculino);
			        			
			        			// NumeroAseguradosParticipesAportantesClientesPlan Total Femenino
			        			Element numeroAseguradosParticipesAportantesClientesPlanTotalFemenino = doc.createElement("Femenino");
			        			numeroAseguradosParticipesAportantesClientesPlanTotalFemenino.appendChild(doc.createTextNode(f.NumeroAseguradosParticipesAportantesClientesPlan_Total_Femenino.toString()));
			        			numeroAseguradosParticipesAportantesClientesPlanTotal.appendChild(numeroAseguradosParticipesAportantesClientesPlanTotalFemenino);
			        			
			        			
			        	//SaldoTotalAcumulado
	        			Element saldoTotalAcumulado = doc.createElement("SaldoTotalAcumulado");
		        		filaDepositosAPVPorPlan.appendChild(saldoTotalAcumulado);
		        		
			        		// SaldoTotalAcumulado Letra A
		        			Element saldoTotalAcumuladoLetraA = doc.createElement("LetraA");
		        			saldoTotalAcumulado.appendChild(saldoTotalAcumuladoLetraA);
			        		
				        		// SaldoTotalAcumulado Letra A Masculino
			        			Element saldoTotalAcumuladoLetraAMasculino = doc.createElement("Masculino");
			        			saldoTotalAcumuladoLetraAMasculino.appendChild(doc.createTextNode(f.SaldoTotalAcumulado_LetraA_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
			        			saldoTotalAcumuladoLetraA.appendChild(saldoTotalAcumuladoLetraAMasculino);
			        			
			        			// SaldoTotalAcumulado Letra A Femenino
			        			Element saldoTotalAcumuladoLetraAFemenino = doc.createElement("Femenino");
			        			saldoTotalAcumuladoLetraAFemenino.appendChild(doc.createTextNode(f.SaldoTotalAcumulado_LetraA_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
			        			saldoTotalAcumuladoLetraA.appendChild(saldoTotalAcumuladoLetraAFemenino);
			        			
		        			// SaldoTotalAcumulado Letra B
		        			Element saldoTotalAcumuladoLetraB = doc.createElement("LetraB");
		        			saldoTotalAcumulado.appendChild(saldoTotalAcumuladoLetraB);
			        		
				        		// SaldoTotalAcumulado Letra B Masculino
			        			Element saldoTotalAcumuladoLetraBMasculino = doc.createElement("Masculino");
			        			saldoTotalAcumuladoLetraBMasculino.appendChild(doc.createTextNode(f.SaldoTotalAcumulado_LetraB_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
			        			saldoTotalAcumuladoLetraB.appendChild(saldoTotalAcumuladoLetraBMasculino);
			        			
			        			// NSaldoTotalAcumulado Letra B Femenino
			        			Element saldoTotalAcumuladoLetraBFemenino = doc.createElement("Femenino");
			        			saldoTotalAcumuladoLetraBFemenino.appendChild(doc.createTextNode(f.SaldoTotalAcumulado_LetraB_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
			        			saldoTotalAcumuladoLetraB.appendChild(saldoTotalAcumuladoLetraBFemenino);
	
			        	//Bonificacion
	        			Element bonificacion = doc.createElement("Bonificacion");
		        		filaDepositosAPVPorPlan.appendChild(bonificacion);
		        		
			        		// Bonificacion Letra A
		        			Element bonificacionLetraA = doc.createElement("LetraA");
		        			bonificacion.appendChild(bonificacionLetraA);
			        		
				        		// Bonificacion Letra A Masculino
			        			Element bonificacionLetraAMasculino = doc.createElement("Masculino");
			        			bonificacionLetraAMasculino.appendChild(doc.createTextNode(f.Bonificacion_LetraA_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
			        			bonificacionLetraA.appendChild(bonificacionLetraAMasculino);
			        			
			        			// Bonificacion Letra A Femenino
			        			Element bonificacionAFemenino = doc.createElement("Femenino");
			        			bonificacionAFemenino.appendChild(doc.createTextNode(f.Bonificacion_LetraA_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
			        			bonificacionLetraA.appendChild(bonificacionAFemenino);
			        	
			        	//NumeroDepositos
	        			Element numeroDepositos = doc.createElement("NumeroDepositos");
		        		filaDepositosAPVPorPlan.appendChild(numeroDepositos);
		        		
			        		// NumeroDepositos Letra A
		        			Element numeroDepositosLetraA = doc.createElement("LetraA");
		        			numeroDepositos.appendChild(numeroDepositosLetraA);
			        		
				        		// NumeroDepositos Letra A Masculino
			        			Element numeroDepositosLetraAMasculino = doc.createElement("Masculino");
			        			numeroDepositosLetraAMasculino.appendChild(doc.createTextNode(f.NumeroDepositos_LetraA_Masculino.toString()));
			        			numeroDepositosLetraA.appendChild(numeroDepositosLetraAMasculino);
			        			
			        			// NumeroDepositos Letra A Femenino
			        			Element numeroDepositosLetraAFemenino = doc.createElement("Femenino");
			        			numeroDepositosLetraAFemenino.appendChild(doc.createTextNode(f.NumeroDepositos_LetraA_Femenino.toString()));
			        			numeroDepositosLetraA.appendChild(numeroDepositosLetraAFemenino);
			        			
		        			// NumeroDepositos Letra B
		        			Element numeroDepositosLetraB = doc.createElement("LetraB");
		        			numeroDepositos.appendChild(numeroDepositosLetraB);
			        		
				        		// NumeroDepositos Letra B Masculino
			        			Element numeroDepositosLetraBMasculino = doc.createElement("Masculino");
			        			numeroDepositosLetraBMasculino.appendChild(doc.createTextNode(f.NumeroDepositos_LetraB_Masculino.toString()));
			        			numeroDepositosLetraB.appendChild(numeroDepositosLetraBMasculino);
			        			
			        			// NumeroDepositos Letra B Femenino
			        			Element numeroDepositosLetraBFemenino = doc.createElement("Femenino");
			        			numeroDepositosLetraBFemenino.appendChild(doc.createTextNode(f.NumeroDepositos_LetraB_Femenino.toString()));
			        			numeroDepositosLetraB.appendChild(numeroDepositosLetraBFemenino);
			        			
			        	//MontoTotalDepositos
	        			Element montoTotalDepositos = doc.createElement("MontoTotalDepositos");
		        		filaDepositosAPVPorPlan.appendChild(montoTotalDepositos);
		        		
			        		// MontoTotalDepositos Letra A
		        			Element montoTotalDepositosLetraA = doc.createElement("LetraA");
		        			montoTotalDepositos.appendChild(montoTotalDepositosLetraA);
			        		
				        		// MontoTotalDepositos Letra A Masculino
			        			Element montoTotalDepositosLetraAMasculino = doc.createElement("Masculino");
			        			montoTotalDepositosLetraAMasculino.appendChild(doc.createTextNode(f.MontoTotalDepositos_LetraA_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
			        			montoTotalDepositosLetraA.appendChild(montoTotalDepositosLetraAMasculino);
			        			
			        			// MontoTotalDepositos Letra A Femenino
			        			Element montoTotalDepositosLetraAFemenino = doc.createElement("Femenino");
			        			montoTotalDepositosLetraAFemenino.appendChild(doc.createTextNode(f.MontoTotalDepositos_LetraA_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
			        			montoTotalDepositosLetraA.appendChild(montoTotalDepositosLetraAFemenino);
			        			
		        			// MontoTotalDepositos Letra B
		        			Element montoTotalDepositosLetraB = doc.createElement("LetraB");
		        			montoTotalDepositos.appendChild(montoTotalDepositosLetraB);
			        		
				        		// MontoTotalDepositos Letra B Masculino
			        			Element montoTotalDepositosLetraBMasculino = doc.createElement("Masculino");
			        			montoTotalDepositosLetraBMasculino.appendChild(doc.createTextNode(f.MontoTotalDepositos_LetraB_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
			        			montoTotalDepositosLetraB.appendChild(montoTotalDepositosLetraBMasculino);
			        			
			        			// MontoTotalDepositos Letra B Femenino
			        			Element montoTotalDepositosLetraBFemenino = doc.createElement("Femenino");
			        			montoTotalDepositosLetraBFemenino.appendChild(doc.createTextNode(f.MontoTotalDepositos_LetraB_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
			        			montoTotalDepositosLetraB.appendChild(montoTotalDepositosLetraBFemenino);
			        	
			        			
			        	//NumeroTraspasosRecibidos
	        			Element numeroTraspasosRecibidos = doc.createElement("NumeroTraspasosRecibidos");
		        		filaDepositosAPVPorPlan.appendChild(numeroTraspasosRecibidos);
		        		
			        		// NumeroTraspasosRecibidos Masculino
		        			Element numeroTraspasosRecibidosMasculino = doc.createElement("Masculino");
		        			numeroTraspasosRecibidosMasculino.appendChild(doc.createTextNode(f.NumeroTraspasosRecibidos_Masculino.toString()));
		        			numeroTraspasosRecibidos.appendChild(numeroTraspasosRecibidosMasculino);
		        			
		        			// NumeroTraspasosRecibidos Femenino
		        			Element numeroTraspasosRecibidosFemenino = doc.createElement("Femenino");
		        			numeroTraspasosRecibidosFemenino.appendChild(doc.createTextNode(f.NumeroTraspasosRecibidos_Femenino.toString()));
		        			numeroTraspasosRecibidos.appendChild(numeroTraspasosRecibidosFemenino);
		        			
	        			//MontoTotalTraspasosRecibidos
	        			Element montoTotalTraspasosRecibidos = doc.createElement("MontoTotalTraspasosRecibidos");
		        		filaDepositosAPVPorPlan.appendChild(montoTotalTraspasosRecibidos);
		        		
			        		// MontoTotalTraspasosRecibidos Masculino
		        			Element montoTotalTraspasosRecibidosMasculino = doc.createElement("Masculino");
		        			montoTotalTraspasosRecibidosMasculino.appendChild(doc.createTextNode(f.MontoTotalTraspasosRecibidos_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
		        			montoTotalTraspasosRecibidos.appendChild(montoTotalTraspasosRecibidosMasculino);
		        			
		        			// MontoTotalTraspasosRecibidos Femenino
		        			Element montoTotalTraspasosRecibidosFemenino = doc.createElement("Femenino");
		        			montoTotalTraspasosRecibidosFemenino.appendChild(doc.createTextNode(f.MontoTotalTraspasosRecibidos_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
		        			montoTotalTraspasosRecibidos.appendChild(montoTotalTraspasosRecibidosFemenino);
		        			
		        		//NumeroRetiros
	        			Element numeroRetiros = doc.createElement("NumeroRetiros");
		        		filaDepositosAPVPorPlan.appendChild(numeroRetiros);
		        		
			        		// NumeroRetiros Letra A
		        			Element numeroRetirosLetraA = doc.createElement("LetraA");
		        			numeroRetiros.appendChild(numeroRetirosLetraA);
			        		
				        		// NumeroRetiros Letra A Masculino
			        			Element numeroRetirosLetraAMasculino = doc.createElement("Masculino");
			        			numeroRetirosLetraAMasculino.appendChild(doc.createTextNode(f.NumeroRetiros_LetraA_Masculino.toString()));
			        			numeroRetirosLetraA.appendChild(numeroRetirosLetraAMasculino);
			        			
			        			// NumeroRetiros Letra A Femenino
			        			Element numeroRetirosLetraAFemenino = doc.createElement("Femenino");
			        			numeroRetirosLetraAFemenino.appendChild(doc.createTextNode(f.NumeroRetiros_LetraA_Femenino.toString()));
			        			numeroRetirosLetraA.appendChild(numeroRetirosLetraAFemenino);
			        			
		        			// NumeroRetiros Letra B
		        			Element numeroRetirosLetraB = doc.createElement("LetraB");
		        			numeroRetiros.appendChild(numeroRetirosLetraB);
			        		
				        		// NumeroRetiros Letra B Masculino
			        			Element numeroRetirosLetraBMasculino = doc.createElement("Masculino");
			        			numeroRetirosLetraBMasculino.appendChild(doc.createTextNode(f.NumeroRetiros_LetraB_Masculino.toString()));
			        			numeroRetirosLetraB.appendChild(numeroRetirosLetraBMasculino);
			        			
			        			// NumeroRetiros Letra B Femenino
			        			Element numeroRetirosLetraBFemenino = doc.createElement("Femenino");
			        			numeroRetirosLetraBFemenino.appendChild(doc.createTextNode(f.NumeroRetiros_LetraB_Femenino.toString()));
			        			numeroRetirosLetraB.appendChild(numeroRetirosLetraBFemenino);
			        			
			        	//MontoTotalRetiros
	        			Element montoTotalRetiros = doc.createElement("MontoTotalRetiros");
		        		filaDepositosAPVPorPlan.appendChild(montoTotalRetiros);
		        		
			        		// NumeroRetiros Letra A
		        			Element montoTotalRetirosLetraA = doc.createElement("LetraA");
		        			montoTotalRetiros.appendChild(montoTotalRetirosLetraA);
			        		
				        		// NumeroRetiros Letra A Masculino
			        			Element montoTotalRetirosLetraAMasculino = doc.createElement("Masculino");
			        			montoTotalRetirosLetraAMasculino.appendChild(doc.createTextNode(f.MontoTotalRetiros_LetraA_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
			        			montoTotalRetirosLetraA.appendChild(montoTotalRetirosLetraAMasculino);
			        			
			        			// NumeroRetiros Letra A Femenino
			        			Element montoTotalRetirosLetraAFemenino = doc.createElement("Femenino");
			        			montoTotalRetirosLetraAFemenino.appendChild(doc.createTextNode(f.MontoTotalRetiros_LetraA_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
			        			montoTotalRetirosLetraA.appendChild(montoTotalRetirosLetraAFemenino);
			        			
		        			// NumeroRetiros Letra B
		        			Element montoTotalRetirosLetraB = doc.createElement("LetraB");
		        			montoTotalRetiros.appendChild(montoTotalRetirosLetraB);
			        		
				        		// NumeroRetiros Letra B Masculino
			        			Element montoTotalRetirosLetraBMasculino = doc.createElement("Masculino");
			        			montoTotalRetirosLetraBMasculino.appendChild(doc.createTextNode(f.MontoTotalRetiros_LetraB_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
			        			montoTotalRetirosLetraB.appendChild(montoTotalRetirosLetraBMasculino);
			        			
			        			// NumeroRetiros Letra B Femenino
			        			Element montoTotalRetirosLetraBFemenino = doc.createElement("Femenino");
			        			montoTotalRetirosLetraBFemenino.appendChild(doc.createTextNode(f.MontoTotalRetiros_LetraB_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
			        			montoTotalRetirosLetraB.appendChild(montoTotalRetirosLetraBFemenino);
		        	 
			        	//NumeroTraspasosRealizados
	        			Element numeroTraspasosRealizados = doc.createElement("NumeroTraspasosRealizados");
		        		filaDepositosAPVPorPlan.appendChild(numeroTraspasosRealizados);
		        		
			        		// NumeroTraspasosRealizados Masculino
		        			Element numeroTraspasosRealizadosMasculino = doc.createElement("Masculino");
		        			numeroTraspasosRealizadosMasculino.appendChild(doc.createTextNode(f.NumeroTraspasosRealizados_Masculino.toString()));
		        			numeroTraspasosRealizados.appendChild(numeroTraspasosRealizadosMasculino);
		        			
		        			// NumeroTraspasosRealizados Femenino
		        			Element numeroTraspasosRealizadosFemenino = doc.createElement("Femenino");
		        			numeroTraspasosRealizadosFemenino.appendChild(doc.createTextNode(f.NumeroTraspasosRealizados_Femenino.toString()));
		        			numeroTraspasosRealizados.appendChild(numeroTraspasosRealizadosFemenino);
		        			
		        		//MontoTotalTraspasosRealizados
	        			Element montoTotalTraspasosRealizados = doc.createElement("MontoTotalTraspasosRealizados");
		        		filaDepositosAPVPorPlan.appendChild(montoTotalTraspasosRealizados);
		        		
			        		// MontoTotalTraspasosRealizados Masculino
		        			Element montoTotalTraspasosRealizadosMasculino = doc.createElement("Masculino");
		        			montoTotalTraspasosRealizadosMasculino.appendChild(doc.createTextNode(f.MontoTotalTraspasosRealizados_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
		        			montoTotalTraspasosRealizados.appendChild(montoTotalTraspasosRealizadosMasculino);
		        			
		        			// NumeroTraspasosRealizados Femenino
		        			Element montoTotalTraspasosRealizadosFemenino = doc.createElement("Femenino");
		        			montoTotalTraspasosRealizadosFemenino.appendChild(doc.createTextNode(f.MontoTotalTraspasosRealizados_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
		        			montoTotalTraspasosRealizados.appendChild(montoTotalTraspasosRealizadosFemenino);
	        	};//FilaDepositosAhorroPrevisionalVoluntarioPorPlan
	    		
	    		
	        	
	        	// TraspasosDetalle
	    		Element traspasosDetalle = doc.createElement("TraspasosDetalle");
	    		depositosAPV.appendChild(traspasosDetalle);
	    		
	        	for (TraspasosDetalle t : lstDepositosAhorroPrevisionalVoluntarioTraspasosDetalle) {
	    			// Realizados - Recibidos
	        		Element accion = doc.createElement(t.Accion);
	        		traspasosDetalle.appendChild(accion);
	        		
	        			//Afp
		        		Element afp = doc.createElement("Afp");
		        		accion.appendChild(afp);
		        		
		        			//Afp NumeroTraspasos
			        		Element afpNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		afpNumeroTraspasos.appendChild(doc.createTextNode(t.Afp_NumeroTraspasos.toString()));
			        		afp.appendChild(afpNumeroTraspasos);
			        		
		        			//Afp Miles
			        		Element afpMiles = doc.createElement("Miles");
			        		afpMiles.appendChild(doc.createTextNode(t.Afp_Miles.setScale(3,RoundingMode.HALF_UP).toString()));
			        		afp.appendChild(afpMiles);
			        	
			        	//BancosInstitucionesFinancieras
		        		Element bancosInstitucionesFinancieras = doc.createElement("BancosInstitucionesFinancieras");
		        		accion.appendChild(bancosInstitucionesFinancieras);
		        		
		        			//BancosInstitucionesFinancieras NumeroTraspasos
			        		Element bancosInstitucionesFinancierasNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		bancosInstitucionesFinancierasNumeroTraspasos.appendChild(doc.createTextNode(t.BancosInstitucionesFinancieras_NumeroTraspasos.toString()));
			        		bancosInstitucionesFinancieras.appendChild(bancosInstitucionesFinancierasNumeroTraspasos);
			        		
		        			//BancosInstitucionesFinancieras Miles
			        		Element bancosInstitucionesFinancierasMiles = doc.createElement("Miles");
			        		bancosInstitucionesFinancierasMiles.appendChild(doc.createTextNode(t.BancosInstitucionesFinancieras_Miles.setScale(3,RoundingMode.HALF_UP).toString()));
			        		bancosInstitucionesFinancieras.appendChild(bancosInstitucionesFinancierasMiles);
			        		
		        		//CorredoresBolsa
		        		Element corredoresBolsa = doc.createElement("CorredoresBolsa");
		        		accion.appendChild(corredoresBolsa);
		        		
		        			//CorredoresBolsa NumeroTraspasos
			        		Element corredoresBolsaNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		corredoresBolsaNumeroTraspasos.appendChild(doc.createTextNode(t.CorredoresBolsa_NumeroTraspasos.toString()));
			        		corredoresBolsa.appendChild(corredoresBolsaNumeroTraspasos);
			        		
		        			//CorredoresBolsa Miles
			        		Element corredoresBolsaMiles = doc.createElement("Miles");
			        		corredoresBolsaMiles.appendChild(doc.createTextNode(t.CorredoresBolsa_Miles.setScale(3,RoundingMode.HALF_UP).toString()));
			        		corredoresBolsa.appendChild(corredoresBolsaMiles);
			        		
		        		//AgentesValores
		        		Element agentesValores = doc.createElement("AgentesValores");
		        		accion.appendChild(agentesValores);
		        		
		        			//AgentesValores NumeroTraspasos
			        		Element agentesValoresNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		agentesValoresNumeroTraspasos.appendChild(doc.createTextNode(t.AgentesValores_NumeroTraspasos.toString()));
			        		agentesValores.appendChild(agentesValoresNumeroTraspasos);
			        		
		        			//AgentesValores Miles
			        		Element agentesValoresMiles = doc.createElement("Miles");
			        		agentesValoresMiles.appendChild(doc.createTextNode(t.AgentesValores_Miles.setScale(3,RoundingMode.HALF_UP).toString()));
			        		agentesValores.appendChild(agentesValoresMiles);
			        		
		        		//FondosMutuos
		        		Element fondosMutuos = doc.createElement("FondosMutuos");
		        		accion.appendChild(fondosMutuos);
		        		
		        			//FondosMutuos NumeroTraspasos
			        		Element fondosMutuosNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		fondosMutuosNumeroTraspasos.appendChild(doc.createTextNode(t.FondosMutuos_NumeroTraspasos.toString()));
			        		fondosMutuos.appendChild(fondosMutuosNumeroTraspasos);
			        		
		        			//FondosMutuos Miles
			        		Element fondosMutuosMiles = doc.createElement("Miles");
			        		fondosMutuosMiles.appendChild(doc.createTextNode(t.FondosMutuos_Miles.setScale(3,RoundingMode.HALF_UP).toString()));
			        		fondosMutuos.appendChild(fondosMutuosMiles);
			        		
		        		//FondosInversion
		        		Element fondosInversion = doc.createElement("FondosInversion");
		        		accion.appendChild(fondosInversion);
		        		
		        			//FondosInversion NumeroTraspasos
			        		Element fondosInversionNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		fondosInversionNumeroTraspasos.appendChild(doc.createTextNode(t.FondosInversion_NumeroTraspasos.toString()));
			        		fondosInversion.appendChild(fondosInversionNumeroTraspasos);
			        		
		        			//FondosInversion Miles
			        		Element fondosInversionMiles = doc.createElement("Miles");
			        		fondosInversionMiles.appendChild(doc.createTextNode(t.FondosInversion_Miles.setScale(3,RoundingMode.HALF_UP).toString()));
			        		fondosInversion.appendChild(fondosInversionMiles);
			        		
		        		//CompaniasSeguros
		        		Element companiasSeguros = doc.createElement("CompaniasSeguros");
		        		accion.appendChild(companiasSeguros);
		        		
		        			//CompaniasSeguros NumeroTraspasos
			        		Element companiasSegurosNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		companiasSegurosNumeroTraspasos.appendChild(doc.createTextNode(t.CompaniasSeguros_NumeroTraspasos.toString()));
			        		companiasSeguros.appendChild(companiasSegurosNumeroTraspasos);
			        		
		        			//CompaniasSeguros Miles
			        		Element companiasSegurosMiles = doc.createElement("Miles");
			        		companiasSegurosMiles.appendChild(doc.createTextNode(t.CompaniasSeguros_Miles.setScale(3,RoundingMode.HALF_UP).toString()));
			        		companiasSeguros.appendChild(companiasSegurosMiles);
			        		
		        		//FondosVivienda
		        		Element fondosVivienda = doc.createElement("FondosVivienda");
		        		accion.appendChild(fondosVivienda);
		        		
		        			//FondosVivienda NumeroTraspasos
			        		Element fondosViviendaNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		fondosViviendaNumeroTraspasos.appendChild(doc.createTextNode(t.FondosVivienda_NumeroTraspasos.toString()));
			        		fondosVivienda.appendChild(fondosViviendaNumeroTraspasos);
			        		
		        			//FondosVivienda Miles
			        		Element fondosViviendaMiles = doc.createElement("Miles");
			        		fondosViviendaMiles.appendChild(doc.createTextNode(t.FondosVivienda_Miles.setScale(3,RoundingMode.HALF_UP).toString()));
			        		fondosVivienda.appendChild(fondosViviendaMiles);
			        		
			        	//OtrasEntidadesAutorizadasSvs
		        		Element otrasEntidadesAutorizadasSvs = doc.createElement("OtrasEntidadesAutorizadasSvs");
		        		accion.appendChild(otrasEntidadesAutorizadasSvs);
		        		
		        			//OtrasEntidadesAutorizadasSvs NumeroTraspasos
			        		Element otrasEntidadesAutorizadasSvsNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		otrasEntidadesAutorizadasSvsNumeroTraspasos.appendChild(doc.createTextNode(t.OtrasEntidadesAutorizadasSvs_NumeroTraspasos.toString()));
			        		otrasEntidadesAutorizadasSvs.appendChild(otrasEntidadesAutorizadasSvsNumeroTraspasos);
			        		
		        			//OtrasEntidadesAutorizadasSvs Miles
			        		Element otrasEntidadesAutorizadasSvsMiles = doc.createElement("Miles");
			        		otrasEntidadesAutorizadasSvsMiles.appendChild(doc.createTextNode(t.OtrasEntidadesAutorizadasSvs_Miles.setScale(3,RoundingMode.HALF_UP).toString()));
			        		otrasEntidadesAutorizadasSvs.appendChild(otrasEntidadesAutorizadasSvsMiles);
	        	}; //DepositosAhorroPrevisionalVoluntarioTraspasosDetalle
	        	
	        	
	        	// NumeroPersonasSaldoEdadGenero
	    		Element numeroPersonasSaldoEdadGenero = doc.createElement("NumeroPersonasSaldoEdadGenero");
	    		depositosAPV.appendChild(numeroPersonasSaldoEdadGenero);
	    		
	        	for (NumeroPersonasSaldoEdadGenero n : lstDepositosAhorroPrevisionalVoluntarioNumeroPersonasSaldoEdadGenero) {
	    			// Saldo_0-5,Saldo_5-10
	        		Element saldo = doc.createElement(n.Saldo);
	        		numeroPersonasSaldoEdadGenero.appendChild(saldo);
	        		
		        		// Saldo Edad_20
		        		Element saldo_Edad_20 = doc.createElement("Edad_20");
		        		saldo.appendChild(saldo_Edad_20);
		        		
			        		// Saldo Edad_20 Masculino
			        		Element saldo_Edad_20_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_20_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_20_Masculino.toString()));
			        		saldo_Edad_20.appendChild(saldo_Edad_20_Masculino);
			        		
			        		// Saldo Edad_20 Femenino
			        		Element saldo_Edad_20_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_20_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_20_Femenino.toString()));
			        		saldo_Edad_20.appendChild(saldo_Edad_20_Femenino);
			        		
		        		// Saldo Edad_20_25
		        		Element saldo_Edad_20_25 = doc.createElement("Edad_20-25");
		        		saldo.appendChild(saldo_Edad_20_25);
		        		
			        		// Saldo Edad_20-25 Masculino
			        		Element saldo_Edad_20_25_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_20_25_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_20_25_Masculino.toString()));
			        		saldo_Edad_20_25.appendChild(saldo_Edad_20_25_Masculino);
			        		
			        		// Saldo Edad_20-25 Femenino
			        		Element saldo_Edad_20_25_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_20_25_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_20_25_Femenino.toString()));
			        		saldo_Edad_20_25.appendChild(saldo_Edad_20_25_Femenino);
			        		
		        		// Saldo Edad_25_30
		        		Element saldo_Edad_25_30 = doc.createElement("Edad_25-30");
		        		saldo.appendChild(saldo_Edad_25_30);
		        		
			        		// Saldo Edad_25-30 Masculino
			        		Element saldo_Edad_25_30_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_25_30_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_25_30_Masculino.toString()));
			        		saldo_Edad_25_30.appendChild(saldo_Edad_25_30_Masculino);
			        		
			        		// Saldo Edad_25-30 Femenino
			        		Element saldo_Edad_25_30_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_25_30_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_25_30_Femenino.toString()));
			        		saldo_Edad_25_30.appendChild(saldo_Edad_25_30_Femenino);
			        		
		        		// Saldo Edad_30_35
		        		Element saldo_Edad_30_35 = doc.createElement("Edad_30-35");
		        		saldo.appendChild(saldo_Edad_30_35);
		        		
			        		// Saldo Edad_30-35 Masculino
			        		Element saldo_Edad_30_35_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_30_35_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_30_35_Masculino.toString()));
			        		saldo_Edad_30_35.appendChild(saldo_Edad_30_35_Masculino);
			        		
			        		// Saldo Edad_30-35 Femenino
			        		Element saldo_Edad_30_35_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_30_35_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_30_35_Femenino.toString()));
			        		saldo_Edad_30_35.appendChild(saldo_Edad_30_35_Femenino);
			        		
		        		// Saldo Edad_35_40
		        		Element saldo_Edad_35_40 = doc.createElement("Edad_35-40");
		        		saldo.appendChild(saldo_Edad_35_40);
		        		
			        		// Saldo Edad_35-40 Masculino
			        		Element saldo_Edad_35_40_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_35_40_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_35_40_Masculino.toString()));
			        		saldo_Edad_35_40.appendChild(saldo_Edad_35_40_Masculino);
			        		
			        		// Saldo Edad_35-40 Femenino
			        		Element saldo_Edad_35_40_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_35_40_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_35_40_Femenino.toString()));
			        		saldo_Edad_35_40.appendChild(saldo_Edad_35_40_Femenino);
			        		
		        		// Saldo Edad_40_45
		        		Element saldo_Edad_40_45 = doc.createElement("Edad_40-45");
		        		saldo.appendChild(saldo_Edad_40_45);
		        		
			        		// Saldo Edad_40-45 Masculino
			        		Element saldo_Edad_40_45_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_40_45_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_40_45_Masculino.toString()));
			        		saldo_Edad_40_45.appendChild(saldo_Edad_40_45_Masculino);
			        		
			        		// Saldo Edad_40-45 Femenino
			        		Element saldo_Edad_40_45_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_40_45_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_40_45_Femenino.toString()));
			        		saldo_Edad_40_45.appendChild(saldo_Edad_40_45_Femenino);
			        	
		        		// Saldo Edad_45_50
		        		Element saldo_Edad_45_50 = doc.createElement("Edad_45-50");
		        		saldo.appendChild(saldo_Edad_45_50);
		        		
			        		// Saldo Edad_45-50 Masculino
			        		Element saldo_Edad_45_50_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_45_50_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_45_50_Masculino.toString()));
			        		saldo_Edad_45_50.appendChild(saldo_Edad_45_50_Masculino);
			        		
			        		// Saldo Edad_45-50 Femenino
			        		Element saldo_Edad_45_50_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_45_50_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_45_50_Femenino.toString()));
			        		saldo_Edad_45_50.appendChild(saldo_Edad_45_50_Femenino);
			        		
			        	// Saldo Edad_50_55
		        		Element saldo_Edad_50_55 = doc.createElement("Edad_50-55");
		        		saldo.appendChild(saldo_Edad_50_55);
		        		
			        		// Saldo Edad_50-55 Masculino
			        		Element saldo_Edad_50_55_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_50_55_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_50_55_Masculino.toString()));
			        		saldo_Edad_50_55.appendChild(saldo_Edad_50_55_Masculino);
			        		
			        		// Saldo Edad_50-55 Femenino
			        		Element saldo_Edad_50_55_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_50_55_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_50_55_Femenino.toString()));
			        		saldo_Edad_50_55.appendChild(saldo_Edad_50_55_Femenino);
			        		
		        		// Saldo Edad_55_60
		        		Element saldo_Edad_55_60 = doc.createElement("Edad_55-60");
		        		saldo.appendChild(saldo_Edad_55_60);
		        		
			        		// Saldo Edad_55-60 Masculino
			        		Element saldo_Edad_55_60_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_55_60_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_55_60_Masculino.toString()));
			        		saldo_Edad_55_60.appendChild(saldo_Edad_55_60_Masculino);
			        		
			        		// Saldo Edad_55-60 Femenino
			        		Element saldo_Edad_55_60_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_55_60_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_55_60_Femenino.toString()));
			        		saldo_Edad_55_60.appendChild(saldo_Edad_55_60_Femenino);
			        	
		        		// Saldo Edad_60_65
		        		Element saldo_Edad_60_65 = doc.createElement("Edad_60-65");
		        		saldo.appendChild(saldo_Edad_60_65);
		        		
			        		// Saldo Edad_60-65 Masculino
			        		Element saldo_Edad_60_65_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_60_65_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_60_65_Masculino.toString()));
			        		saldo_Edad_60_65.appendChild(saldo_Edad_60_65_Masculino);
			        		
			        		// Saldo Edad_60-65 Femenino
			        		Element saldo_Edad_60_65_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_60_65_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_60_65_Femenino.toString()));
			        		saldo_Edad_60_65.appendChild(saldo_Edad_60_65_Femenino);
			        		
		        		// Saldo Edad_65_70
		        		Element saldo_Edad_65_70 = doc.createElement("Edad_65-70");
		        		saldo.appendChild(saldo_Edad_65_70);
		        		
			        		// Saldo Edad_65-70 Masculino
			        		Element saldo_Edad_65_70_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_65_70_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_65_70_Masculino.toString()));
			        		saldo_Edad_65_70.appendChild(saldo_Edad_65_70_Masculino);
			        		
			        		// Saldo Edad_65-70 Femenino
			        		Element saldo_Edad_65_70_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_65_70_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_65_70_Femenino.toString()));
			        		saldo_Edad_65_70.appendChild(saldo_Edad_65_70_Femenino);
			        		
			        		
		        		// Saldo Edad_70_Mas
		        		Element saldo_Edad_70_mas = doc.createElement("Edad_70-mas");
		        		saldo.appendChild(saldo_Edad_70_mas);
		        		
			        		// Saldo Edad_70_mas Masculino
			        		Element saldo_Edad_70_mas_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_70_mas_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_70_Mas_Masculino.toString()));
			        		saldo_Edad_70_mas.appendChild(saldo_Edad_70_mas_Masculino);
			        		
			        		// Saldo Edad_70_mas Femenino
			        		Element saldo_Edad_70_mas_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_70_mas_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_70_Mas_Femenino.toString()));
			        		saldo_Edad_70_mas.appendChild(saldo_Edad_70_mas_Femenino);
	        	}; //NumeroPersonasSaldoEdadGenero
	    	};//!lstFilaDepositosAhorroPrevisionalVoluntarioPorPlan.isEmpty()
        	
        	
        	//******************************************************************************************************************
        	//******************************************************************************************************************
        	//******************************************************************************************************************
        	//******************************************************************************************************************
        	//******************************************************************************************************************
        	
	    	//En el caso que no existan depositos convenidos, este item no se debe informar.
	    	if (!lstFilaDepositosConvenidosPorPlan.isEmpty()){
	        	// DepositosConvenidos
	    		Element depositosConvenidos = doc.createElement("DepositosConvenidos");
	    		rootElement.appendChild(depositosConvenidos);
	    		
	    		for (FilaDepositosConvenidosPorPlan f : lstFilaDepositosConvenidosPorPlan) {
	    			// FilaDepositosConvenidosPorPlan
	        		Element filaDepositosConvenidosPorPlan = doc.createElement("FilaDepositosConvenidosPorPlan");
	        		depositosConvenidos.appendChild(filaDepositosConvenidosPorPlan);
	        		
		        		// NumeroPolizaRunFondoCodigoPlan
		        		Element numeroPolizaRunFondoCodigoPlan = doc.createElement("NumeroPolizaRunFondoCodigoPlan");
		        		numeroPolizaRunFondoCodigoPlan.appendChild(doc.createTextNode(f.NumeroPolizaRunFondoCodigoPlan.toString()));
		        		filaDepositosConvenidosPorPlan.appendChild(numeroPolizaRunFondoCodigoPlan);
		 
		        		// Serie
		        		Element serie = doc.createElement("Serie");
		        		serie.appendChild(doc.createTextNode(f.Serie));
		        		filaDepositosConvenidosPorPlan.appendChild(serie);
		        		
		        		//NumeroCuentasApvVigentes
		        		Element numeroCuentasApvVigentes = doc.createElement("NumeroCuentasApvVigentes");
		        		filaDepositosConvenidosPorPlan.appendChild(numeroCuentasApvVigentes);
		        		
		        			//NumeroCuentasApvVigentes Masculino
		        			Element numeroCuentasApvVigentesMasculino = doc.createElement("Masculino");
		        			numeroCuentasApvVigentesMasculino.appendChild(doc.createTextNode(f.NumeroCuentasApvVigentes_Masculino.toString()));
		        			numeroCuentasApvVigentes.appendChild(numeroCuentasApvVigentesMasculino);
		        			
		        			//NumeroCuentasApvVigentes Femenino
		        			Element numeroCuentasApvVigentesFemenino = doc.createElement("Femenino");
		        			numeroCuentasApvVigentesFemenino.appendChild(doc.createTextNode(f.NumeroCuentasApvVigentes_Femenino.toString()));
		        			numeroCuentasApvVigentes.appendChild(numeroCuentasApvVigentesFemenino);
		        			
	        			//NumeroAseguradosParticipesAportantesClientesPlan
		        		Element numeroAseguradosParticipesAportantesClientesPlan = doc.createElement("NumeroAseguradosParticipesAportantesClientesPlan");
		        		filaDepositosConvenidosPorPlan.appendChild(numeroAseguradosParticipesAportantesClientesPlan);
		        		
		        			//NumeroAseguradosParticipesAportantesClientesPlan Masculino
		        			Element numeroAseguradosParticipesAportantesClientesPlanMasculino = doc.createElement("Masculino");
		        			numeroAseguradosParticipesAportantesClientesPlanMasculino.appendChild(doc.createTextNode(f.NumeroAseguradosParticipesAportantesClientesPlan_Masculino.toString()));
		        			numeroAseguradosParticipesAportantesClientesPlan.appendChild(numeroAseguradosParticipesAportantesClientesPlanMasculino);
		        			
		        			//NumeroAseguradosParticipesAportantesClientesPlan Femenino
		        			Element numeroAseguradosParticipesAportantesClientesPlanFemenino = doc.createElement("Femenino");
		        			numeroAseguradosParticipesAportantesClientesPlanFemenino.appendChild(doc.createTextNode(f.NumeroAseguradosParticipesAportantesClientesPlan_Femenino.toString()));
		        			numeroAseguradosParticipesAportantesClientesPlan.appendChild(numeroAseguradosParticipesAportantesClientesPlanFemenino);
		        			
		        			
	        			//SaldoTotalAcumulado
		        		Element saldoTotalAcumulado = doc.createElement("SaldoTotalAcumulado");
		        		filaDepositosConvenidosPorPlan.appendChild(saldoTotalAcumulado);
		        		
		        			//SaldoTotalAcumulado Masculino
		        			Element saldoTotalAcumuladoMasculino = doc.createElement("Masculino");
		        			saldoTotalAcumuladoMasculino.appendChild(doc.createTextNode(f.SaldoTotalAcumulado_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
		        			saldoTotalAcumulado.appendChild(saldoTotalAcumuladoMasculino);
		        			
		        			//SaldoTotalAcumulado Femenino
		        			Element saldoTotalAcumuladoFemenino = doc.createElement("Femenino");
		        			saldoTotalAcumuladoFemenino.appendChild(doc.createTextNode(f.SaldoTotalAcumulado_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
		        			saldoTotalAcumulado.appendChild(saldoTotalAcumuladoFemenino);
		        			
		        			
	        			//NumeroDepositos
		        		Element numeroDepositos = doc.createElement("NumeroDepositos");
		        		filaDepositosConvenidosPorPlan.appendChild(numeroDepositos);
		        		
		        			//NumeroDepositos Masculino
		        			Element numeroDepositosMasculino = doc.createElement("Masculino");
		        			numeroDepositosMasculino.appendChild(doc.createTextNode(f.NumeroDepositos_Masculino.toString()));
		        			numeroDepositos.appendChild(numeroDepositosMasculino);
		        			
		        			//NumeroDepositos Femenino
		        			Element numeroDepositosFemenino = doc.createElement("Femenino");
		        			numeroDepositosFemenino.appendChild(doc.createTextNode(f.NumeroDepositos_Femenino.toString()));
		        			numeroDepositos.appendChild(numeroDepositosFemenino);
	        	
	        			//MontoTotalDepositos
		        		Element montoTotalDepositos = doc.createElement("MontoTotalDepositos");
		        		filaDepositosConvenidosPorPlan.appendChild(montoTotalDepositos);
		        		
		        			//MontoTotalDepositos Masculino
		        			Element montoTotalDepositosMasculino = doc.createElement("Masculino");
		        			montoTotalDepositosMasculino.appendChild(doc.createTextNode(f.MontoTotalDepositos_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
		        			montoTotalDepositos.appendChild(montoTotalDepositosMasculino);
		        			
		        			//MontoTotalDepositos Femenino
		        			Element montoTotalDepositosFemenino = doc.createElement("Femenino");
		        			montoTotalDepositosFemenino.appendChild(doc.createTextNode(f.MontoTotalDepositos_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
		        			montoTotalDepositos.appendChild(montoTotalDepositosFemenino);
		        			
		        			
	        			//NumeroTraspasosRecibidos
		        		Element numeroTraspasosRecibidos = doc.createElement("NumeroTraspasosRecibidos");
		        		filaDepositosConvenidosPorPlan.appendChild(numeroTraspasosRecibidos);
		        		
		        			//NumeroTraspasosRecibidos Masculino
		        			Element numeroTraspasosRecibidosMasculino = doc.createElement("Masculino");
		        			numeroTraspasosRecibidosMasculino.appendChild(doc.createTextNode(f.NumeroTraspasosRecibidos_Masculino.toString()));
		        			numeroTraspasosRecibidos.appendChild(numeroTraspasosRecibidosMasculino);
		        			
		        			//NumeroTraspasosRecibidos Femenino
		        			Element numeroTraspasosRecibidosFemenino = doc.createElement("Femenino");
		        			numeroTraspasosRecibidosFemenino.appendChild(doc.createTextNode(f.NumeroTraspasosRecibidos_Femenino.toString()));
		        			numeroTraspasosRecibidos.appendChild(numeroTraspasosRecibidosFemenino);
		        			
		        			
	        			//MontoTotalTraspasosRecibidos
		        		Element montoTotalTraspasosRecibidos = doc.createElement("MontoTotalTraspasosRecibidos");
		        		filaDepositosConvenidosPorPlan.appendChild(montoTotalTraspasosRecibidos);
		        		
		        			//MontoTotalTraspasosRecibidos Masculino
		        			Element montoTotalTraspasosRecibidosMasculino = doc.createElement("Masculino");
		        			montoTotalTraspasosRecibidosMasculino.appendChild(doc.createTextNode(f.MontoTotalTraspasosRecibidos_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
		        			montoTotalTraspasosRecibidos.appendChild(montoTotalTraspasosRecibidosMasculino);
		        			
		        			//MontoTotalTraspasosRecibidos Femenino
		        			Element montoTotalTraspasosRecibidosFemenino = doc.createElement("Femenino");
		        			montoTotalTraspasosRecibidosFemenino.appendChild(doc.createTextNode(f.MontoTotalTraspasosRecibidos_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
		        			montoTotalTraspasosRecibidos.appendChild(montoTotalTraspasosRecibidosFemenino);
		        			
		        		//NumeroTraspasosRealizados
		        		Element numeroTraspasosRealizados = doc.createElement("NumeroTraspasosRealizados");
		        		filaDepositosConvenidosPorPlan.appendChild(numeroTraspasosRealizados);
		        		
		        			//NumeroTraspasosRealizados Masculino
		        			Element numeroTraspasosRealizadosMasculino = doc.createElement("Masculino");
		        			numeroTraspasosRealizadosMasculino.appendChild(doc.createTextNode(f.NumeroTraspasosRealizados_Masculino.toString()));
		        			numeroTraspasosRealizados.appendChild(numeroTraspasosRealizadosMasculino);
		        			
		        			//NumeroTraspasosRealizados Femenino
		        			Element numeroTraspasosRealizadosFemenino = doc.createElement("Femenino");
		        			numeroTraspasosRealizadosFemenino.appendChild(doc.createTextNode(f.NumeroTraspasosRealizados_Femenino.toString()));
		        			numeroTraspasosRealizados.appendChild(numeroTraspasosRealizadosFemenino);
		        			
		        		//MontoTotalTraspasosRealizados
		        		Element montoTotalTraspasosRealizados = doc.createElement("MontoTotalTraspasosRealizados");
		        		filaDepositosConvenidosPorPlan.appendChild(montoTotalTraspasosRealizados);
		        		
		        			//MontoTotalTraspasosRealizados Masculino
		        			Element montoTotalTraspasosRealizadosMasculino = doc.createElement("Masculino");
		        			montoTotalTraspasosRealizadosMasculino.appendChild(doc.createTextNode(f.MontoTotalTraspasosRealizados_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
		        			montoTotalTraspasosRealizados.appendChild(montoTotalTraspasosRealizadosMasculino);
		        			
		        			//MontoTotalTraspasosRealizados Femenino
		        			Element montoTotalTraspasosRealizadosFemenino = doc.createElement("Femenino");
		        			montoTotalTraspasosRealizadosFemenino.appendChild(doc.createTextNode(f.MontoTotalTraspasosRealizados_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
		        			montoTotalTraspasosRealizados.appendChild(montoTotalTraspasosRealizadosFemenino);
		        			
	    		} //FilaDepositosConvenidosPorPlan
	    		
	    		
	    		// TraspasosDetalle
	    		Element depositosConvenidosTraspasosDetalle = doc.createElement("TraspasosDetalle");
	    		depositosConvenidos.appendChild(depositosConvenidosTraspasosDetalle);
	    		
	        	for (TraspasosDetalle t : lstDepositosConvenidosTraspasosDetalle) {
	    			// Realizados - Recibidos
	        		Element accion = doc.createElement(t.Accion);
	        		depositosConvenidosTraspasosDetalle.appendChild(accion);
	        		
	        			//Afp
		        		Element afp = doc.createElement("Afp");
		        		accion.appendChild(afp);
		        		
		        			//Afp NumeroTraspasos
			        		Element afpNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		afpNumeroTraspasos.appendChild(doc.createTextNode(t.Afp_NumeroTraspasos.toString()));
			        		afp.appendChild(afpNumeroTraspasos);
			        		
		        			//Afp Miles
			        		Element afpMiles = doc.createElement("Miles");
			        		afpMiles.appendChild(doc.createTextNode(t.Afp_Miles.setScale(3,RoundingMode.HALF_UP).toString()));
			        		afp.appendChild(afpMiles);
			        	
			        	//BancosInstitucionesFinancieras
		        		Element bancosInstitucionesFinancieras = doc.createElement("BancosInstitucionesFinancieras");
		        		accion.appendChild(bancosInstitucionesFinancieras);
		        		
		        			//BancosInstitucionesFinancieras NumeroTraspasos
			        		Element bancosInstitucionesFinancierasNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		bancosInstitucionesFinancierasNumeroTraspasos.appendChild(doc.createTextNode(t.BancosInstitucionesFinancieras_NumeroTraspasos.toString()));
			        		bancosInstitucionesFinancieras.appendChild(bancosInstitucionesFinancierasNumeroTraspasos);
			        		
		        			//BancosInstitucionesFinancieras Miles
			        		Element bancosInstitucionesFinancierasMiles = doc.createElement("Miles");
			        		bancosInstitucionesFinancierasMiles.appendChild(doc.createTextNode(t.BancosInstitucionesFinancieras_Miles.setScale(3,RoundingMode.HALF_UP).toString()));
			        		bancosInstitucionesFinancieras.appendChild(bancosInstitucionesFinancierasMiles);
			        		
		        		//CorredoresBolsa
		        		Element corredoresBolsa = doc.createElement("CorredoresBolsa");
		        		accion.appendChild(corredoresBolsa);
		        		
		        			//CorredoresBolsa NumeroTraspasos
			        		Element corredoresBolsaNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		corredoresBolsaNumeroTraspasos.appendChild(doc.createTextNode(t.CorredoresBolsa_NumeroTraspasos.toString()));
			        		corredoresBolsa.appendChild(corredoresBolsaNumeroTraspasos);
			        		
		        			//CorredoresBolsa Miles
			        		Element corredoresBolsaMiles = doc.createElement("Miles");
			        		corredoresBolsaMiles.appendChild(doc.createTextNode(t.CorredoresBolsa_Miles.setScale(3,RoundingMode.HALF_UP).toString()));
			        		corredoresBolsa.appendChild(corredoresBolsaMiles);
			        		
		        		//AgentesValores
		        		Element agentesValores = doc.createElement("AgentesValores");
		        		accion.appendChild(agentesValores);
		        		
		        			//AgentesValores NumeroTraspasos
			        		Element agentesValoresNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		agentesValoresNumeroTraspasos.appendChild(doc.createTextNode(t.AgentesValores_NumeroTraspasos.toString()));
			        		agentesValores.appendChild(agentesValoresNumeroTraspasos);
			        		
		        			//AgentesValores Miles
			        		Element agentesValoresMiles = doc.createElement("Miles");
			        		agentesValoresMiles.appendChild(doc.createTextNode(t.AgentesValores_Miles.setScale(3,RoundingMode.HALF_UP).toString()));
			        		agentesValores.appendChild(agentesValoresMiles);
			        		
		        		//FondosMutuos
		        		Element fondosMutuos = doc.createElement("FondosMutuos");
		        		accion.appendChild(fondosMutuos);
		        		
		        			//FondosMutuos NumeroTraspasos
			        		Element fondosMutuosNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		fondosMutuosNumeroTraspasos.appendChild(doc.createTextNode(t.FondosMutuos_NumeroTraspasos.toString()));
			        		fondosMutuos.appendChild(fondosMutuosNumeroTraspasos);
			        		
		        			//FondosMutuos Miles
			        		Element fondosMutuosMiles = doc.createElement("Miles");
			        		fondosMutuosMiles.appendChild(doc.createTextNode(t.FondosMutuos_Miles.setScale(3,RoundingMode.HALF_UP).toString()));
			        		fondosMutuos.appendChild(fondosMutuosMiles);
			        		
		        		//FondosInversion
		        		Element fondosInversion = doc.createElement("FondosInversion");
		        		accion.appendChild(fondosInversion);
		        		
		        			//FondosInversion NumeroTraspasos
			        		Element fondosInversionNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		fondosInversionNumeroTraspasos.appendChild(doc.createTextNode(t.FondosInversion_NumeroTraspasos.toString()));
			        		fondosInversion.appendChild(fondosInversionNumeroTraspasos);
			        		
		        			//FondosInversion Miles
			        		Element fondosInversionMiles = doc.createElement("Miles");
			        		fondosInversionMiles.appendChild(doc.createTextNode(t.FondosInversion_Miles.setScale(3,RoundingMode.HALF_UP).toString()));
			        		fondosInversion.appendChild(fondosInversionMiles);
			        		
		        		//CompaniasSeguros
		        		Element companiasSeguros = doc.createElement("CompaniasSeguros");
		        		accion.appendChild(companiasSeguros);
		        		
		        			//CompaniasSeguros NumeroTraspasos
			        		Element companiasSegurosNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		companiasSegurosNumeroTraspasos.appendChild(doc.createTextNode(t.CompaniasSeguros_NumeroTraspasos.toString()));
			        		companiasSeguros.appendChild(companiasSegurosNumeroTraspasos);
			        		
		        			//CompaniasSeguros Miles
			        		Element companiasSegurosMiles = doc.createElement("Miles");
			        		companiasSegurosMiles.appendChild(doc.createTextNode(t.CompaniasSeguros_Miles.setScale(3,RoundingMode.HALF_UP).toString()));
			        		companiasSeguros.appendChild(companiasSegurosMiles);
			        		
		        		//FondosVivienda
		        		Element fondosVivienda = doc.createElement("FondosVivienda");
		        		accion.appendChild(fondosVivienda);
		        		
		        			//FondosVivienda NumeroTraspasos
			        		Element fondosViviendaNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		fondosViviendaNumeroTraspasos.appendChild(doc.createTextNode(t.FondosVivienda_NumeroTraspasos.toString()));
			        		fondosVivienda.appendChild(fondosViviendaNumeroTraspasos);
			        		
		        			//FondosVivienda Miles
			        		Element fondosViviendaMiles = doc.createElement("Miles");
			        		fondosViviendaMiles.appendChild(doc.createTextNode(t.FondosVivienda_Miles.setScale(3,RoundingMode.HALF_UP).toString()));
			        		fondosVivienda.appendChild(fondosViviendaMiles);
			        		
			        	//OtrasEntidadesAutorizadasSvs
		        		Element otrasEntidadesAutorizadasSvs = doc.createElement("OtrasEntidadesAutorizadasSvs");
		        		accion.appendChild(otrasEntidadesAutorizadasSvs);
		        		
		        			//OtrasEntidadesAutorizadasSvs NumeroTraspasos
			        		Element otrasEntidadesAutorizadasSvsNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		otrasEntidadesAutorizadasSvsNumeroTraspasos.appendChild(doc.createTextNode(t.OtrasEntidadesAutorizadasSvs_NumeroTraspasos.toString()));
			        		otrasEntidadesAutorizadasSvs.appendChild(otrasEntidadesAutorizadasSvsNumeroTraspasos);
			        		
		        			//OtrasEntidadesAutorizadasSvs Miles
			        		Element otrasEntidadesAutorizadasSvsMiles = doc.createElement("Miles");
			        		otrasEntidadesAutorizadasSvsMiles.appendChild(doc.createTextNode(t.OtrasEntidadesAutorizadasSvs_Miles.setScale(3,RoundingMode.HALF_UP).toString()));
			        		otrasEntidadesAutorizadasSvs.appendChild(otrasEntidadesAutorizadasSvsMiles);
	        	}; //DepositosConvenidosTraspasosDetalle
	 
	    		
	    		
	        	// NumeroPersonasSaldoEdadGenero
	    		Element depositosConvenidosNumeroPersonasSaldoEdadGenero = doc.createElement("NumeroPersonasSaldoEdadGenero");
	    		depositosConvenidos.appendChild(depositosConvenidosNumeroPersonasSaldoEdadGenero);
	    		
	        	for (NumeroPersonasSaldoEdadGenero n : lstDepositosConvenidosNumeroPersonasSaldoEdadGenero) {
	    			// Saldo_0-5,Saldo_5-10
	        		Element saldo = doc.createElement(n.Saldo);
	        		depositosConvenidosNumeroPersonasSaldoEdadGenero.appendChild(saldo);
	        		
		        		// Saldo Edad_20
		        		Element saldo_Edad_20 = doc.createElement("Edad_20");
		        		saldo.appendChild(saldo_Edad_20);
		        		
			        		// Saldo Edad_20 Masculino
			        		Element saldo_Edad_20_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_20_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_20_Masculino.toString()));
			        		saldo_Edad_20.appendChild(saldo_Edad_20_Masculino);
			        		
			        		// Saldo Edad_20 Femenino
			        		Element saldo_Edad_20_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_20_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_20_Femenino.toString()));
			        		saldo_Edad_20.appendChild(saldo_Edad_20_Femenino);
			        		
		        		// Saldo Edad_20_25
		        		Element saldo_Edad_20_25 = doc.createElement("Edad_20-25");
		        		saldo.appendChild(saldo_Edad_20_25);
		        		
			        		// Saldo Edad_20-25 Masculino
			        		Element saldo_Edad_20_25_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_20_25_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_20_25_Masculino.toString()));
			        		saldo_Edad_20_25.appendChild(saldo_Edad_20_25_Masculino);
			        		
			        		// Saldo Edad_20-25 Femenino
			        		Element saldo_Edad_20_25_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_20_25_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_20_25_Femenino.toString()));
			        		saldo_Edad_20_25.appendChild(saldo_Edad_20_25_Femenino);
			        		
		        		// Saldo Edad_25_30
		        		Element saldo_Edad_25_30 = doc.createElement("Edad_25-30");
		        		saldo.appendChild(saldo_Edad_25_30);
		        		
			        		// Saldo Edad_25-30 Masculino
			        		Element saldo_Edad_25_30_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_25_30_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_25_30_Masculino.toString()));
			        		saldo_Edad_25_30.appendChild(saldo_Edad_25_30_Masculino);
			        		
			        		// Saldo Edad_25-30 Femenino
			        		Element saldo_Edad_25_30_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_25_30_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_25_30_Femenino.toString()));
			        		saldo_Edad_25_30.appendChild(saldo_Edad_25_30_Femenino);
			        		
		        		// Saldo Edad_30_35
		        		Element saldo_Edad_30_35 = doc.createElement("Edad_30-35");
		        		saldo.appendChild(saldo_Edad_30_35);
		        		
			        		// Saldo Edad_30-35 Masculino
			        		Element saldo_Edad_30_35_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_30_35_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_30_35_Masculino.toString()));
			        		saldo_Edad_30_35.appendChild(saldo_Edad_30_35_Masculino);
			        		
			        		// Saldo Edad_30-35 Femenino
			        		Element saldo_Edad_30_35_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_30_35_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_30_35_Femenino.toString()));
			        		saldo_Edad_30_35.appendChild(saldo_Edad_30_35_Femenino);
			        		
		        		// Saldo Edad_35_40
		        		Element saldo_Edad_35_40 = doc.createElement("Edad_35-40");
		        		saldo.appendChild(saldo_Edad_35_40);
		        		
			        		// Saldo Edad_35-40 Masculino
			        		Element saldo_Edad_35_40_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_35_40_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_35_40_Masculino.toString()));
			        		saldo_Edad_35_40.appendChild(saldo_Edad_35_40_Masculino);
			        		
			        		// Saldo Edad_35-40 Femenino
			        		Element saldo_Edad_35_40_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_35_40_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_35_40_Femenino.toString()));
			        		saldo_Edad_35_40.appendChild(saldo_Edad_35_40_Femenino);
			        		
		        		// Saldo Edad_40_45
		        		Element saldo_Edad_40_45 = doc.createElement("Edad_40-45");
		        		saldo.appendChild(saldo_Edad_40_45);
		        		
			        		// Saldo Edad_40-45 Masculino
			        		Element saldo_Edad_40_45_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_40_45_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_40_45_Masculino.toString()));
			        		saldo_Edad_40_45.appendChild(saldo_Edad_40_45_Masculino);
			        		
			        		// Saldo Edad_40-45 Femenino
			        		Element saldo_Edad_40_45_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_40_45_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_40_45_Femenino.toString()));
			        		saldo_Edad_40_45.appendChild(saldo_Edad_40_45_Femenino);
			        	
		        		// Saldo Edad_45_50
		        		Element saldo_Edad_45_50 = doc.createElement("Edad_45-50");
		        		saldo.appendChild(saldo_Edad_45_50);
		        		
			        		// Saldo Edad_45-50 Masculino
			        		Element saldo_Edad_45_50_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_45_50_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_45_50_Masculino.toString()));
			        		saldo_Edad_45_50.appendChild(saldo_Edad_45_50_Masculino);
			        		
			        		// Saldo Edad_45-50 Femenino
			        		Element saldo_Edad_45_50_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_45_50_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_45_50_Femenino.toString()));
			        		saldo_Edad_45_50.appendChild(saldo_Edad_45_50_Femenino);
			        		
			        	// Saldo Edad_50_55
		        		Element saldo_Edad_50_55 = doc.createElement("Edad_50-55");
		        		saldo.appendChild(saldo_Edad_50_55);
		        		
			        		// Saldo Edad_50-55 Masculino
			        		Element saldo_Edad_50_55_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_50_55_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_50_55_Masculino.toString()));
			        		saldo_Edad_50_55.appendChild(saldo_Edad_50_55_Masculino);
			        		
			        		// Saldo Edad_50-55 Femenino
			        		Element saldo_Edad_50_55_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_50_55_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_50_55_Femenino.toString()));
			        		saldo_Edad_50_55.appendChild(saldo_Edad_50_55_Femenino);
			        		
		        		// Saldo Edad_55_60
		        		Element saldo_Edad_55_60 = doc.createElement("Edad_55-60");
		        		saldo.appendChild(saldo_Edad_55_60);
		        		
			        		// Saldo Edad_55-60 Masculino
			        		Element saldo_Edad_55_60_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_55_60_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_55_60_Masculino.toString()));
			        		saldo_Edad_55_60.appendChild(saldo_Edad_55_60_Masculino);
			        		
			        		// Saldo Edad_55-60 Femenino
			        		Element saldo_Edad_55_60_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_55_60_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_55_60_Femenino.toString()));
			        		saldo_Edad_55_60.appendChild(saldo_Edad_55_60_Femenino);
			        	
		        		// Saldo Edad_60_65
		        		Element saldo_Edad_60_65 = doc.createElement("Edad_60-65");
		        		saldo.appendChild(saldo_Edad_60_65);
		        		
			        		// Saldo Edad_60-65 Masculino
			        		Element saldo_Edad_60_65_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_60_65_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_60_65_Masculino.toString()));
			        		saldo_Edad_60_65.appendChild(saldo_Edad_60_65_Masculino);
			        		
			        		// Saldo Edad_60-65 Femenino
			        		Element saldo_Edad_60_65_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_60_65_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_60_65_Femenino.toString()));
			        		saldo_Edad_60_65.appendChild(saldo_Edad_60_65_Femenino);
			        		
		        		// Saldo Edad_65_70
		        		Element saldo_Edad_65_70 = doc.createElement("Edad_65-70");
		        		saldo.appendChild(saldo_Edad_65_70);
		        		
			        		// Saldo Edad_65-70 Masculino
			        		Element saldo_Edad_65_70_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_65_70_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_65_70_Masculino.toString()));
			        		saldo_Edad_65_70.appendChild(saldo_Edad_65_70_Masculino);
			        		
			        		// Saldo Edad_65-70 Femenino
			        		Element saldo_Edad_65_70_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_65_70_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_65_70_Femenino.toString()));
			        		saldo_Edad_65_70.appendChild(saldo_Edad_65_70_Femenino);
			        		
			        		
		        		// Saldo Edad_70_Mas
		        		Element saldo_Edad_70_mas = doc.createElement("Edad_70-mas");
		        		saldo.appendChild(saldo_Edad_70_mas);
		        		
			        		// Saldo Edad_70_mas Masculino
			        		Element saldo_Edad_70_mas_Masculino = doc.createElement("Masculino");
			        		saldo_Edad_70_mas_Masculino.appendChild(doc.createTextNode(n.Saldo_Edad_70_Mas_Masculino.toString()));
			        		saldo_Edad_70_mas.appendChild(saldo_Edad_70_mas_Masculino);
			        		
			        		// Saldo Edad_70_mas Femenino
			        		Element saldo_Edad_70_mas_Femenino = doc.createElement("Femenino");
			        		saldo_Edad_70_mas_Femenino.appendChild(doc.createTextNode(n.Saldo_Edad_70_Mas_Femenino.toString()));
			        		saldo_Edad_70_mas.appendChild(saldo_Edad_70_mas_Femenino);
	        	}; //NumeroPersonasSaldoEdadGenero
	    	};//!lstFilaDepositosConvenidosPorPlan.isEmpty()
        	
        	
        	//******************************************************************************************************************
        	//******************************************************************************************************************
        	//******************************************************************************************************************
        	//******************************************************************************************************************
        	//******************************************************************************************************************
        	
	    	//En el caso que no existan planes de APVC aprobados, este item no se debe informar.
	    	if (!lstFilaDepositosAhorroPrevisionalVoluntarioColectivoPorPlan.isEmpty()){
	        	// DepositosAhorroPrevisionalVoluntarioColectivo
	    		Element depositosAhorroPrevisionalVoluntarioColectivo = doc.createElement("DepositosAhorroPrevisionalVoluntarioColectivo");
	    		rootElement.appendChild(depositosAhorroPrevisionalVoluntarioColectivo);
	    		
	    		for (FilaDepositosAhorroPrevisionalVoluntarioColectivoPorPlan f : lstFilaDepositosAhorroPrevisionalVoluntarioColectivoPorPlan) {
	    			// FilaDepositosAhorroPrevisionalVoluntarioColectivoPorPlan
	        		Element filaDepositosAPVColectivoPorPlan = doc.createElement("FilaDepositosAhorroPrevisionalVoluntarioColectivoPorPlan");
	        		depositosAhorroPrevisionalVoluntarioColectivo.appendChild(filaDepositosAPVColectivoPorPlan);
	        		
		        		// NumeroPolizaRunFondoCodigoPlan
		        		Element numeroPolizaRunFondoCodigoPlan = doc.createElement("NumeroPolizaRunFondoCodigoPlan");
		        		numeroPolizaRunFondoCodigoPlan.appendChild(doc.createTextNode(f.NumeroPolizaRunFondoCodigoPlan.toString()));
		        		filaDepositosAPVColectivoPorPlan.appendChild(numeroPolizaRunFondoCodigoPlan);
		 
		        		// Serie
		        		Element serie = doc.createElement("Serie");
		        		serie.appendChild(doc.createTextNode(f.Serie));
		        		filaDepositosAPVColectivoPorPlan.appendChild(serie);
		        		
		        		// NumeroContratosApvc
		        		Element numeroContratosApvc = doc.createElement("NumeroContratosApvc");
		        		numeroContratosApvc.appendChild(doc.createTextNode(f.NumeroContratosApvc.toString()));
		        		filaDepositosAPVColectivoPorPlan.appendChild(numeroContratosApvc);
		        		
		        		// NumeroCuentasIndividualesApvcVigentes
		        		Element numeroCuentasIndividualesApvcVigentes = doc.createElement("NumeroCuentasIndividualesApvcVigentes");
		        		filaDepositosAPVColectivoPorPlan.appendChild(numeroCuentasIndividualesApvcVigentes);
		        		
			        		// NumeroCuentasIndividualesApvcVigentes LetraA
			        		Element numeroCuentasIndividualesApvcVigentesLetraA = doc.createElement("LetraA");
			        		numeroCuentasIndividualesApvcVigentes.appendChild(numeroCuentasIndividualesApvcVigentesLetraA);
			        		
				        		// NumeroCuentasIndividualesApvcVigentes LetraA Masculino
				        		Element numeroCuentasIndividualesApvcVigentesLetraAMasculino = doc.createElement("Masculino");
				        		numeroCuentasIndividualesApvcVigentesLetraAMasculino.appendChild(doc.createTextNode(f.NumeroCuentasIndividualesApvcVigentes_LetraA_Masculino.toString()));
				        		numeroCuentasIndividualesApvcVigentesLetraA.appendChild(numeroCuentasIndividualesApvcVigentesLetraAMasculino);
				        		
				        		// NumeroCuentasIndividualesApvcVigentes LetraA Femenino
				        		Element numeroCuentasIndividualesApvcVigentesLetraAFemenino = doc.createElement("Femenino");
				        		numeroCuentasIndividualesApvcVigentesLetraAFemenino.appendChild(doc.createTextNode(f.NumeroCuentasIndividualesApvcVigentes_LetraA_Femenino.toString()));
				        		numeroCuentasIndividualesApvcVigentesLetraA.appendChild(numeroCuentasIndividualesApvcVigentesLetraAFemenino);
			        		
			        		// NumeroCuentasIndividualesApvcVigentes Total
			        		Element numeroCuentasIndividualesApvcVigentesTotal = doc.createElement("Total");
			        		numeroCuentasIndividualesApvcVigentes.appendChild(numeroCuentasIndividualesApvcVigentesTotal);
			        		
				        		// NumeroCuentasIndividualesApvcVigentes Total Masculino
				        		Element numeroCuentasIndividualesApvcVigentesTotalMasculino = doc.createElement("Masculino");
				        		numeroCuentasIndividualesApvcVigentesTotalMasculino.appendChild(doc.createTextNode(f.NumeroCuentasIndividualesApvcVigentes_Total_Masculino.toString()));
				        		numeroCuentasIndividualesApvcVigentesTotal.appendChild(numeroCuentasIndividualesApvcVigentesTotalMasculino);
				        		
				        		// NumeroCuentasIndividualesApvcVigentes Total Femenino
				        		Element numeroCuentasIndividualesApvcVigentesTotalFemenino = doc.createElement("Femenino");
				        		numeroCuentasIndividualesApvcVigentesTotalFemenino.appendChild(doc.createTextNode(f.NumeroCuentasIndividualesApvcVigentes_Total_Femenino.toString()));
				        		numeroCuentasIndividualesApvcVigentesTotal.appendChild(numeroCuentasIndividualesApvcVigentesTotalFemenino);
				        		
			        	// NumeroAseguradosParticipesAportantesClientesPlan
		        		Element numeroAseguradosParticipesAportantesClientesPlan = doc.createElement("NumeroAseguradosParticipesAportantesClientesPlan");
		        		filaDepositosAPVColectivoPorPlan.appendChild(numeroAseguradosParticipesAportantesClientesPlan);
		        		
			        		// NumeroAseguradosParticipesAportantesClientesPlan LetraA
			        		Element numeroAseguradosParticipesAportantesClientesPlanLetraA = doc.createElement("LetraA");
			        		numeroAseguradosParticipesAportantesClientesPlan.appendChild(numeroAseguradosParticipesAportantesClientesPlanLetraA);
			        		
				        		// NumeroAseguradosParticipesAportantesClientesPlan LetraA Masculino
				        		Element numeroAseguradosParticipesAportantesClientesPlanLetraAMasculino = doc.createElement("Masculino");
				        		numeroAseguradosParticipesAportantesClientesPlanLetraAMasculino.appendChild(doc.createTextNode(f.NumeroAseguradosParticipesAportantesClientesPlan_LetraA_Masculino.toString()));
				        		numeroAseguradosParticipesAportantesClientesPlanLetraA.appendChild(numeroAseguradosParticipesAportantesClientesPlanLetraAMasculino);
				        		
				        		// NumeroAseguradosParticipesAportantesClientesPlan LetraA Femenino
				        		Element numeroAseguradosParticipesAportantesClientesPlanLetraAFemenino = doc.createElement("Femenino");
				        		numeroAseguradosParticipesAportantesClientesPlanLetraAFemenino.appendChild(doc.createTextNode(f.NumeroAseguradosParticipesAportantesClientesPlan_LetraA_Femenino.toString()));
				        		numeroAseguradosParticipesAportantesClientesPlanLetraA.appendChild(numeroAseguradosParticipesAportantesClientesPlanLetraAFemenino);
			        		
			        		// NumeroAseguradosParticipesAportantesClientesPlan Total
			        		Element numeroAseguradosParticipesAportantesClientesPlanTotal = doc.createElement("Total");
			        		numeroAseguradosParticipesAportantesClientesPlan.appendChild(numeroAseguradosParticipesAportantesClientesPlanTotal);
			        		
				        		// NumeroAseguradosParticipesAportantesClientesPlan Total Masculino
				        		Element numeroAseguradosParticipesAportantesClientesPlanTotalMasculino = doc.createElement("Masculino");
				        		numeroAseguradosParticipesAportantesClientesPlanTotalMasculino.appendChild(doc.createTextNode(f.NumeroAseguradosParticipesAportantesClientesPlan_Total_Masculino.toString()));
				        		numeroAseguradosParticipesAportantesClientesPlanTotal.appendChild(numeroAseguradosParticipesAportantesClientesPlanTotalMasculino);
				        		
				        		// NumeroAseguradosParticipesAportantesClientesPlan Total Femenino
				        		Element numeroAseguradosParticipesAportantesClientesPlanTotalFemenino = doc.createElement("Femenino");
				        		numeroAseguradosParticipesAportantesClientesPlanTotalFemenino.appendChild(doc.createTextNode(f.NumeroAseguradosParticipesAportantesClientesPlan_Total_Femenino.toString()));
				        		numeroAseguradosParticipesAportantesClientesPlanTotal.appendChild(numeroAseguradosParticipesAportantesClientesPlanTotalFemenino);
		        		
		        		// SaldoTotalAcumulado
		        		Element saldoTotalAcumulado = doc.createElement("SaldoTotalAcumulado");
		        		filaDepositosAPVColectivoPorPlan.appendChild(saldoTotalAcumulado);
		        		
			        		// SaldoTotalAcumulado Empleador
			        		Element saldoTotalAcumuladoEmpleador = doc.createElement("Empleador");
			        		saldoTotalAcumulado.appendChild(saldoTotalAcumuladoEmpleador);
		        		
				        		// SaldoTotalAcumulado Empleador Masculino
				        		Element saldoTotalAcumuladoEmpleadorMasculino = doc.createElement("Masculino");
				        		saldoTotalAcumuladoEmpleadorMasculino.appendChild(doc.createTextNode(f.SaldoTotalAcumulado_Empleador_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
				        		saldoTotalAcumuladoEmpleador.appendChild(saldoTotalAcumuladoEmpleadorMasculino);
			        		
				        		// SaldoTotalAcumulado Empleador Femenino
				        		Element saldoTotalAcumuladoEmpleadorFemenino = doc.createElement("Femenino");
				        		saldoTotalAcumuladoEmpleadorFemenino.appendChild(doc.createTextNode(f.SaldoTotalAcumulado_Empleador_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
				        		saldoTotalAcumuladoEmpleador.appendChild(saldoTotalAcumuladoEmpleadorFemenino);
			        		
			        		// SaldoTotalAcumulado Trabajador
			        		Element saldoTotalAcumuladoTrabajador = doc.createElement("Trabajador");
			        		saldoTotalAcumulado.appendChild(saldoTotalAcumuladoTrabajador);
			        		
				        		// SaldoTotalAcumulado Trabajador LetraA
				        		Element saldoTotalAcumuladoTrabajadorLetraA = doc.createElement("LetraA");
				        		saldoTotalAcumuladoTrabajador.appendChild(saldoTotalAcumuladoTrabajadorLetraA);
		        		
					        		// SaldoTotalAcumulado Trabajador LetraA Masculino
					        		Element saldoTotalAcumuladoTrabajadorLetraAMasculino = doc.createElement("Masculino");
					        		saldoTotalAcumuladoTrabajadorLetraAMasculino.appendChild(doc.createTextNode(f.SaldoTotalAcumulado_Trabajador_LetraA_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
					        		saldoTotalAcumuladoTrabajadorLetraA.appendChild(saldoTotalAcumuladoTrabajadorLetraAMasculino);
					        		
					        		// SaldoTotalAcumulado Trabajador LetraA Femenino
					        		Element saldoTotalAcumuladoTrabajadorLetraAFemenino = doc.createElement("Femenino");
					        		saldoTotalAcumuladoTrabajadorLetraAFemenino.appendChild(doc.createTextNode(f.SaldoTotalAcumulado_Trabajador_LetraA_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
					        		saldoTotalAcumuladoTrabajadorLetraA.appendChild(saldoTotalAcumuladoTrabajadorLetraAFemenino);
					        		
				        		// SaldoTotalAcumulado Trabajador LetraB
				        		Element saldoTotalAcumuladoTrabajadorLetraB = doc.createElement("LetraB");
				        		saldoTotalAcumuladoTrabajador.appendChild(saldoTotalAcumuladoTrabajadorLetraB);
				        		
					        		// SaldoTotalAcumulado Trabajador LetraB Masculino
					        		Element saldoTotalAcumuladoTrabajadorLetraBMasculino = doc.createElement("Masculino");
					        		saldoTotalAcumuladoTrabajadorLetraBMasculino.appendChild(doc.createTextNode(f.SaldoTotalAcumulado_Trabajador_LetraB_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
					        		saldoTotalAcumuladoTrabajadorLetraB.appendChild(saldoTotalAcumuladoTrabajadorLetraBMasculino);
					        		
					        		// SaldoTotalAcumulado Trabajador LetraB Femenino
					        		Element saldoTotalAcumuladoTrabajadorLetraBFemenino = doc.createElement("Femenino");
					        		saldoTotalAcumuladoTrabajadorLetraBFemenino.appendChild(doc.createTextNode(f.SaldoTotalAcumulado_Trabajador_LetraB_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
					        		saldoTotalAcumuladoTrabajadorLetraB.appendChild(saldoTotalAcumuladoTrabajadorLetraAFemenino);
		        		
					    // Bonificacion
		        		Element bonificacion = doc.createElement("Bonificacion");
		        		filaDepositosAPVColectivoPorPlan.appendChild(bonificacion);
		        		
			        		// Bonificacion LetraA
			        		Element bonificacionLetraA = doc.createElement("LetraA");
			        		bonificacion.appendChild(bonificacionLetraA);
			        		
				        		// Bonificacion LetraA Masculino
				        		Element bonificacionLetraAMasculino = doc.createElement("Masculino");
				        		bonificacionLetraAMasculino.appendChild(doc.createTextNode(f.Bonificacion_LetraA_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
				        		bonificacionLetraA.appendChild(bonificacionLetraAMasculino);
				        		
				        		// Bonificacion LetraA Masculino
				        		Element bonificacionLetraAFemenino = doc.createElement("Femenino");
				        		bonificacionLetraAFemenino.appendChild(doc.createTextNode(f.Bonificacion_LetraA_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
				        		bonificacionLetraA.appendChild(bonificacionLetraAFemenino);
			        	
				        		
		        		// NumeroAportes
		        		Element numeroAportes = doc.createElement("NumeroAportes");
		        		filaDepositosAPVColectivoPorPlan.appendChild(numeroAportes);
		        		
			        		// NumeroAportes Empleador
			        		Element numeroAportesEmpleador = doc.createElement("Empleador");
			        		numeroAportes.appendChild(numeroAportesEmpleador);
		        		
				        		// NumeroAportes Empleador Masculino
				        		Element numeroAportesEmpleadorMasculino = doc.createElement("Masculino");
				        		numeroAportesEmpleadorMasculino.appendChild(doc.createTextNode(f.NumeroAportes_Empleador_Masculino.toString()));
				        		numeroAportesEmpleador.appendChild(numeroAportesEmpleadorMasculino);
			        		
				        		// NumeroAportes Empleador Femenino
				        		Element numeroAportesEmpleadorFemenino = doc.createElement("Femenino");
				        		numeroAportesEmpleadorFemenino.appendChild(doc.createTextNode(f.NumeroAportes_Empleador_Femenino.toString()));
				        		numeroAportesEmpleador.appendChild(numeroAportesEmpleadorFemenino);
			        		
			        		// NumeroAportes Trabajador
			        		Element numeroAportesTrabajador = doc.createElement("Trabajador");
			        		numeroAportes.appendChild(numeroAportesTrabajador);
			        		
				        		// NumeroAportes Trabajador LetraA
				        		Element numeroAportesTrabajadorLetraA = doc.createElement("LetraA");
				        		numeroAportesTrabajador.appendChild(numeroAportesTrabajadorLetraA);
		        		
					        		// NumeroAportes Trabajador LetraA Masculino
					        		Element numeroAportesTrabajadorLetraAMasculino = doc.createElement("Masculino");
					        		numeroAportesTrabajadorLetraAMasculino.appendChild(doc.createTextNode(f.NumeroAportes_Trabajador_LetraA_Masculino.toString()));
					        		numeroAportesTrabajadorLetraA.appendChild(numeroAportesTrabajadorLetraAMasculino);
					        		
					        		// NumeroAportes Trabajador LetraA Femenino
					        		Element numeroAportesTrabajadorLetraAFemenino = doc.createElement("Femenino");
					        		numeroAportesTrabajadorLetraAFemenino.appendChild(doc.createTextNode(f.NumeroAportes_Trabajador_LetraA_Femenino.toString()));
					        		numeroAportesTrabajadorLetraA.appendChild(numeroAportesTrabajadorLetraAFemenino);
					        		
				        		// NumeroAportes Trabajador LetraB
				        		Element numeroAportesTrabajadorLetraB = doc.createElement("LetraB");
				        		numeroAportesTrabajador.appendChild(numeroAportesTrabajadorLetraB);
				        		
					        		// NumeroAportes Trabajador LetraB Masculino
					        		Element numeroAportesTrabajadorLetraBMasculino = doc.createElement("Masculino");
					        		numeroAportesTrabajadorLetraBMasculino.appendChild(doc.createTextNode(f.NumeroAportes_Trabajador_LetraB_Masculino.toString()));
					        		numeroAportesTrabajadorLetraB.appendChild(numeroAportesTrabajadorLetraBMasculino);
					        		
					        		// NumeroAportes Trabajador LetraB Femenino
					        		Element numeroAportesTrabajadorLetraBFemenino = doc.createElement("Femenino");
					        		numeroAportesTrabajadorLetraBFemenino.appendChild(doc.createTextNode(f.NumeroAportes_Trabajador_LetraB_Femenino.toString()));
					        		numeroAportesTrabajadorLetraB.appendChild(numeroAportesTrabajadorLetraBFemenino);
					        		
					    // MontoTotalAportes
		        		Element montoTotalAportes = doc.createElement("MontoTotalAportes");
		        		filaDepositosAPVColectivoPorPlan.appendChild(montoTotalAportes);
		        		
			        		// MontoTotalAportes Empleador
			        		Element montoTotalAportesEmpleador = doc.createElement("Empleador");
			        		montoTotalAportes.appendChild(montoTotalAportesEmpleador);
		        		
				        		// MontoTotalAportes Empleador Masculino
				        		Element montoTotalAportesEmpleadorMasculino = doc.createElement("Masculino");
				        		montoTotalAportesEmpleadorMasculino.appendChild(doc.createTextNode(f.MontoTotalAportes_Empleador_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
				        		montoTotalAportesEmpleador.appendChild(montoTotalAportesEmpleadorMasculino);
			        		
				        		// MontoTotalAportes Empleador Femenino
				        		Element montoTotalAportesEmpleadorFemenino = doc.createElement("Femenino");
				        		montoTotalAportesEmpleadorFemenino.appendChild(doc.createTextNode(f.MontoTotalAportes_Empleador_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
				        		montoTotalAportesEmpleador.appendChild(montoTotalAportesEmpleadorFemenino);
			        		
			        		// MontoTotalAportes Trabajador
			        		Element montoTotalAportesTrabajador = doc.createElement("Trabajador");
			        		montoTotalAportes.appendChild(montoTotalAportesTrabajador);
			        		
				        		// MontoTotalAportes Trabajador LetraA
				        		Element montoTotalAportesTrabajadorLetraA = doc.createElement("LetraA");
				        		montoTotalAportesTrabajador.appendChild(montoTotalAportesTrabajadorLetraA);
		        		
					        		// MontoTotalAportes Trabajador LetraA Masculino
					        		Element montoTotalAportesTrabajadorLetraAMasculino = doc.createElement("Masculino");
					        		montoTotalAportesTrabajadorLetraAMasculino.appendChild(doc.createTextNode(f.MontoTotalAportes_Trabajador_LetraA_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
					        		montoTotalAportesTrabajadorLetraA.appendChild(montoTotalAportesTrabajadorLetraAMasculino);
					        		
					        		// MontoTotalAportes Trabajador LetraA Femenino
					        		Element montoTotalAportesTrabajadorLetraAFemenino = doc.createElement("Femenino");
					        		montoTotalAportesTrabajadorLetraAFemenino.appendChild(doc.createTextNode(f.MontoTotalAportes_Trabajador_LetraA_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
					        		montoTotalAportesTrabajadorLetraA.appendChild(montoTotalAportesTrabajadorLetraAFemenino);
					        		
				        		// MontoTotalAportes Trabajador LetraB
				        		Element montoTotalAportesTrabajadorLetraB = doc.createElement("LetraB");
				        		montoTotalAportesTrabajador.appendChild(montoTotalAportesTrabajadorLetraB);
				        		
					        		// MontoTotalAportes Trabajador LetraB Masculino
					        		Element montoTotalAportesTrabajadorLetraBMasculino = doc.createElement("Masculino");
					        		montoTotalAportesTrabajadorLetraBMasculino.appendChild(doc.createTextNode(f.MontoTotalAportes_Trabajador_LetraB_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
					        		montoTotalAportesTrabajadorLetraB.appendChild(montoTotalAportesTrabajadorLetraBMasculino);
					        		
					        		// MontoTotalAportes Trabajador LetraB Femenino
					        		Element montoTotalAportesTrabajadorLetraBFemenino = doc.createElement("Femenino");
					        		montoTotalAportesTrabajadorLetraBFemenino.appendChild(doc.createTextNode(f.MontoTotalAportes_Trabajador_LetraB_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
					        		montoTotalAportesTrabajadorLetraB.appendChild(montoTotalAportesTrabajadorLetraBFemenino);
	
		        		// NumeroTraspasosRecibidos
		        		Element numeroTraspasosRecibidos = doc.createElement("NumeroTraspasosRecibidos");
		        		filaDepositosAPVColectivoPorPlan.appendChild(numeroTraspasosRecibidos);
		        		
			        		// NumeroTraspasosRecibidos Empleador
			        		Element numeroTraspasosRecibidosEmpleador = doc.createElement("Empleador");
			        		numeroTraspasosRecibidos.appendChild(numeroTraspasosRecibidosEmpleador);
			        		
				        		// NumeroTraspasosRecibidos Empleador Masculino
				        		Element numeroTraspasosRecibidosEmpleadorMasculino = doc.createElement("Masculino");
				        		numeroTraspasosRecibidosEmpleadorMasculino.appendChild(doc.createTextNode(f.NumeroTraspasosRecibidos_Empleador_Masculino.toString()));
				        		numeroTraspasosRecibidosEmpleador.appendChild(numeroTraspasosRecibidosEmpleadorMasculino);
				        		
				        		// NumeroTraspasosRecibidos Empleador Femenino
				        		Element numeroTraspasosRecibidosEmpleadorFemenino = doc.createElement("Femenino");
				        		numeroTraspasosRecibidosEmpleadorFemenino.appendChild(doc.createTextNode(f.NumeroTraspasosRecibidos_Empleador_Femenino.toString()));
				        		numeroTraspasosRecibidosEmpleador.appendChild(numeroTraspasosRecibidosEmpleadorFemenino);
				        		
			        		// NumeroTraspasosRecibidos Trabajador
			        		Element numeroTraspasosRecibidosTrabajador = doc.createElement("Trabajador");
			        		numeroTraspasosRecibidos.appendChild(numeroTraspasosRecibidosTrabajador);
			        		
				        		// NumeroTraspasosRecibidos Trabajador Masculino
				        		Element numeroTraspasosRecibidosTrabajadorMasculino = doc.createElement("Masculino");
				        		numeroTraspasosRecibidosTrabajadorMasculino.appendChild(doc.createTextNode(f.NumeroTraspasosRecibidos_Trabajador_Masculino.toString()));
				        		numeroTraspasosRecibidosTrabajador.appendChild(numeroTraspasosRecibidosTrabajadorMasculino);
				        		
				        		// NumeroTraspasosRecibidos Trabajador Femenino
				        		Element numeroTraspasosRecibidosTrabajadorFemenino = doc.createElement("Femenino");
				        		numeroTraspasosRecibidosTrabajadorFemenino.appendChild(doc.createTextNode(f.NumeroTraspasosRecibidos_Trabajador_Femenino.toString()));
				        		numeroTraspasosRecibidosTrabajador.appendChild(numeroTraspasosRecibidosTrabajadorFemenino);
				        		
				        // MontoTotalTraspasosRecibidos
		        		Element montoTotalTraspasosRecibidos = doc.createElement("MontoTotalTraspasosRecibidos");
		        		filaDepositosAPVColectivoPorPlan.appendChild(montoTotalTraspasosRecibidos);
		        		
			        		// MontoTotalTraspasosRecibidos Empleador
			        		Element montoTotalTraspasosRecibidosEmpleador = doc.createElement("Empleador");
			        		montoTotalTraspasosRecibidos.appendChild(montoTotalTraspasosRecibidosEmpleador);
			        		
				        		// MontoTotalTraspasosRecibidos Empleador Masculino
				        		Element montoTotalTraspasosRecibidosEmpleadorMasculino = doc.createElement("Masculino");
				        		montoTotalTraspasosRecibidosEmpleadorMasculino.appendChild(doc.createTextNode(f.MontoTotalTraspasosRecibidos_Empleador_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
				        		montoTotalTraspasosRecibidosEmpleador.appendChild(montoTotalTraspasosRecibidosEmpleadorMasculino);
				        		
				        		// MontoTotalTraspasosRecibidos Empleador Femenino
				        		Element montoTotalTraspasosRecibidosEmpleadorFemenino = doc.createElement("Femenino");
				        		montoTotalTraspasosRecibidosEmpleadorFemenino.appendChild(doc.createTextNode(f.MontoTotalTraspasosRecibidos_Empleador_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
				        		montoTotalTraspasosRecibidosEmpleador.appendChild(montoTotalTraspasosRecibidosEmpleadorFemenino);
				        		
			        		// MontoTotalTraspasosRecibidos Trabajador
			        		Element montoTotalTraspasosRecibidosTrabajador = doc.createElement("Trabajador");
			        		montoTotalTraspasosRecibidos.appendChild(montoTotalTraspasosRecibidosTrabajador);
			        		
				        		// MontoTotalTraspasosRecibidos Trabajador Masculino
				        		Element montoTotalTraspasosRecibidosTrabajadorMasculino = doc.createElement("Masculino");
				        		montoTotalTraspasosRecibidosTrabajadorMasculino.appendChild(doc.createTextNode(f.MontoTotalTraspasosRecibidos_Trabajador_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
				        		montoTotalTraspasosRecibidosTrabajador.appendChild(montoTotalTraspasosRecibidosTrabajadorMasculino);
				        		
				        		// MontoTotalTraspasosRecibidos Trabajador Femenino
				        		Element montoTotalTraspasosRecibidosTrabajadorFemenino = doc.createElement("Femenino");
				        		montoTotalTraspasosRecibidosTrabajadorFemenino.appendChild(doc.createTextNode(f.MontoTotalTraspasosRecibidos_Trabajador_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
				        		montoTotalTraspasosRecibidosTrabajador.appendChild(montoTotalTraspasosRecibidosTrabajadorFemenino);
				        		
				        		
		        		// NumeroRetiros
		        		Element numeroRetiros = doc.createElement("NumeroRetiros");
		        		filaDepositosAPVColectivoPorPlan.appendChild(numeroRetiros);
		        		
			        		// NumeroRetiros Empleador
			        		Element numeroRetirosEmpleador = doc.createElement("Empleador");
			        		numeroRetiros.appendChild(numeroRetirosEmpleador);
		        		
				        		// NumeroRetiros Empleador Masculino
				        		Element numeroRetirosEmpleadorMasculino = doc.createElement("Masculino");
				        		numeroRetirosEmpleadorMasculino.appendChild(doc.createTextNode(f.NumeroRetiros_Empleador_Masculino.toString()));
				        		numeroRetirosEmpleador.appendChild(numeroRetirosEmpleadorMasculino);
			        		
				        		// NumeroRetiros Empleador Femenino
				        		Element numeroRetirosEmpleadorFemenino = doc.createElement("Femenino");
				        		numeroRetirosEmpleadorFemenino.appendChild(doc.createTextNode(f.NumeroRetiros_Empleador_Femenino.toString()));
				        		numeroRetirosEmpleador.appendChild(numeroRetirosEmpleadorFemenino);
			        		
			        		// NumeroRetiros Trabajador
			        		Element numeroRetirosTrabajador = doc.createElement("Trabajador");
			        		numeroRetiros.appendChild(numeroRetirosTrabajador);
			        		
				        		// NumeroRetiros Trabajador LetraA
				        		Element numeroRetirosTrabajadorLetraA = doc.createElement("LetraA");
				        		numeroRetirosTrabajador.appendChild(numeroRetirosTrabajadorLetraA);
		        		
					        		// NumeroRetiros Trabajador LetraA Masculino
					        		Element numeroRetirosTrabajadorLetraAMasculino = doc.createElement("Masculino");
					        		numeroRetirosTrabajadorLetraAMasculino.appendChild(doc.createTextNode(f.NumeroRetiros_Trabajador_LetraA_Masculino.toString()));
					        		numeroRetirosTrabajadorLetraA.appendChild(numeroRetirosTrabajadorLetraAMasculino);
					        		
					        		// NumeroRetiros Trabajador LetraA Femenino
					        		Element numeroRetirosTrabajadorLetraAFemenino = doc.createElement("Femenino");
					        		numeroRetirosTrabajadorLetraAFemenino.appendChild(doc.createTextNode(f.NumeroRetiros_Trabajador_LetraA_Femenino.toString()));
					        		numeroRetirosTrabajadorLetraA.appendChild(numeroRetirosTrabajadorLetraAFemenino);
					        		
				        		// NumeroRetiros Trabajador LetraB
				        		Element numeroRetirosTrabajadorLetraB = doc.createElement("LetraB");
				        		numeroRetirosTrabajador.appendChild(numeroRetirosTrabajadorLetraB);
				        		
					        		// NumeroRetiros Trabajador LetraB Masculino
					        		Element numeroRetirosTrabajadorLetraBMasculino = doc.createElement("Masculino");
					        		numeroRetirosTrabajadorLetraBMasculino.appendChild(doc.createTextNode(f.NumeroRetiros_Trabajador_LetraB_Masculino.toString()));
					        		numeroRetirosTrabajadorLetraB.appendChild(numeroRetirosTrabajadorLetraBMasculino);
					        		
					        		// NumeroRetiros Trabajador LetraB Femenino
					        		Element numeroRetirosTrabajadorLetraBFemenino = doc.createElement("Femenino");
					        		numeroRetirosTrabajadorLetraBFemenino.appendChild(doc.createTextNode(f.NumeroRetiros_Trabajador_LetraB_Femenino.toString()));
					        		numeroRetirosTrabajadorLetraB.appendChild(numeroRetirosTrabajadorLetraBFemenino);
	
	
		        		// MontoTotalRetiros
		        		Element montoTotalRetiros = doc.createElement("MontoTotalRetiros");
		        		filaDepositosAPVColectivoPorPlan.appendChild(montoTotalRetiros);
		        		
			        		// MontoTotalRetiros Empleador
			        		Element montoTotalRetirosEmpleador = doc.createElement("Empleador");
			        		montoTotalRetiros.appendChild(montoTotalRetirosEmpleador);
		        		
				        		// MontoTotalRetiros Empleador Masculino
				        		Element montoTotalRetirosEmpleadorMasculino = doc.createElement("Masculino");
				        		montoTotalRetirosEmpleadorMasculino.appendChild(doc.createTextNode(f.MontoTotalRetiros_Empleador_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
				        		montoTotalRetirosEmpleador.appendChild(montoTotalRetirosEmpleadorMasculino);
			        		
				        		// MontoTotalRetiros Empleador Femenino
				        		Element montoTotalRetirosEmpleadorFemenino = doc.createElement("Femenino");
				        		montoTotalRetirosEmpleadorFemenino.appendChild(doc.createTextNode(f.MontoTotalRetiros_Empleador_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
				        		montoTotalRetirosEmpleador.appendChild(montoTotalRetirosEmpleadorFemenino);
			        		
			        		// MontoTotalRetiros Trabajador
			        		Element montoTotalRetirosTrabajador = doc.createElement("Trabajador");
			        		montoTotalRetiros.appendChild(montoTotalRetirosTrabajador);
			        		
				        		// MontoTotalRetiros Trabajador LetraA
				        		Element montoTotalRetirosTrabajadorLetraA = doc.createElement("LetraA");
				        		montoTotalRetirosTrabajador.appendChild(montoTotalRetirosTrabajadorLetraA);
		        		
					        		// MontoTotalRetiros Trabajador LetraA Masculino
					        		Element montoTotalRetirosTrabajadorLetraAMasculino = doc.createElement("Masculino");
					        		montoTotalRetirosTrabajadorLetraAMasculino.appendChild(doc.createTextNode(f.MontoTotalRetiros_Trabajador_LetraA_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
					        		montoTotalRetirosTrabajadorLetraA.appendChild(montoTotalRetirosTrabajadorLetraAMasculino);
					        		
					        		// MontoTotalRetiros Trabajador LetraA Femenino
					        		Element montoTotalRetirosTrabajadorLetraAFemenino = doc.createElement("Femenino");
					        		montoTotalRetirosTrabajadorLetraAFemenino.appendChild(doc.createTextNode(f.MontoTotalRetiros_Trabajador_LetraA_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
					        		montoTotalRetirosTrabajadorLetraA.appendChild(montoTotalRetirosTrabajadorLetraAFemenino);
					        		
				        		// MontoTotalRetiros Trabajador LetraB
				        		Element montoTotalRetirosTrabajadorLetraB = doc.createElement("LetraB");
				        		montoTotalRetirosTrabajador.appendChild(montoTotalRetirosTrabajadorLetraB);
				        		
					        		// MontoTotalRetiros Trabajador LetraB Masculino
					        		Element montoTotalRetirosTrabajadorLetraBMasculino = doc.createElement("Masculino");
					        		montoTotalRetirosTrabajadorLetraBMasculino.appendChild(doc.createTextNode(f.MontoTotalRetiros_Trabajador_LetraB_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
					        		montoTotalRetirosTrabajadorLetraB.appendChild(montoTotalRetirosTrabajadorLetraBMasculino);
					        		
					        		// MontoTotalRetiros Trabajador LetraB Femenino
					        		Element montoTotalRetirosTrabajadorLetraBFemenino = doc.createElement("Femenino");
					        		montoTotalRetirosTrabajadorLetraBFemenino.appendChild(doc.createTextNode(f.MontoTotalRetiros_Trabajador_LetraB_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
					        		montoTotalRetirosTrabajadorLetraB.appendChild(montoTotalRetirosTrabajadorLetraBFemenino);
	       		
					        		
					        		
		        		// NumeroTraspasosRealizados
		        		Element numeroTraspasosRealizados = doc.createElement("NumeroTraspasosRealizados");
		        		filaDepositosAPVColectivoPorPlan.appendChild(numeroTraspasosRealizados);
		        		
			        		// NumeroTraspasosRealizados Empleador
			        		Element numeroTraspasosRealizadosEmpleador = doc.createElement("Empleador");
			        		numeroTraspasosRealizados.appendChild(numeroTraspasosRealizadosEmpleador);
			        		
				        		// NumeroTraspasosRealizados Empleador Masculino
				        		Element numeroTraspasosRealizadosEmpleadorMasculino = doc.createElement("Masculino");
				        		numeroTraspasosRealizadosEmpleadorMasculino.appendChild(doc.createTextNode(f.NumeroTraspasosRealizados_Empleador_Masculino.toString()));
				        		numeroTraspasosRealizadosEmpleador.appendChild(numeroTraspasosRealizadosEmpleadorMasculino);
				        		
				        		// NumeroTraspasosRealizados Empleador Femenino
				        		Element numeroTraspasosRealizadosEmpleadorFemenino = doc.createElement("Femenino");
				        		numeroTraspasosRealizadosEmpleadorFemenino.appendChild(doc.createTextNode(f.NumeroTraspasosRealizados_Empleador_Femenino.toString()));
				        		numeroTraspasosRealizadosEmpleador.appendChild(numeroTraspasosRealizadosEmpleadorFemenino);
				        		
			        		// NumeroTraspasosRealizados Trabajador
			        		Element numeroTraspasosRealizadosTrabajador = doc.createElement("Trabajador");
			        		numeroTraspasosRealizados.appendChild(numeroTraspasosRealizadosTrabajador);
			        		
				        		// NumeroTraspasosRealizados Trabajador Masculino
				        		Element numeroTraspasosRealizadosTrabajadorMasculino = doc.createElement("Masculino");
				        		numeroTraspasosRealizadosTrabajadorMasculino.appendChild(doc.createTextNode(f.NumeroTraspasosRealizados_Trabajador_Masculino.toString()));
				        		numeroTraspasosRealizadosTrabajador.appendChild(numeroTraspasosRealizadosTrabajadorMasculino);
				        		
				        		// NumeroTraspasosRealizados Trabajador Femenino
				        		Element numeroTraspasosRealizadosTrabajadorFemenino = doc.createElement("Femenino");
				        		numeroTraspasosRealizadosTrabajadorFemenino.appendChild(doc.createTextNode(f.NumeroTraspasosRealizados_Trabajador_Femenino.toString()));
				        		numeroTraspasosRealizadosTrabajador.appendChild(numeroTraspasosRealizadosTrabajadorFemenino);
	
	
	        		// MontoTotalTraspasosRealizados
	        		Element montoTotalTraspasosRealizados = doc.createElement("MontoTotalTraspasosRealizados");
	        		filaDepositosAPVColectivoPorPlan.appendChild(montoTotalTraspasosRealizados);
	        		
		        		// MontoTotalTraspasosRealizados Empleador
		        		Element montoTotalTraspasosRealizadosEmpleador = doc.createElement("Empleador");
		        		montoTotalTraspasosRealizados.appendChild(montoTotalTraspasosRealizadosEmpleador);
		        		
			        		// MontoTotalTraspasosRealizados Empleador Masculino
			        		Element montoTotalTraspasosRealizadosEmpleadorMasculino = doc.createElement("Masculino");
			        		montoTotalTraspasosRealizadosEmpleadorMasculino.appendChild(doc.createTextNode(f.MontoTotalTraspasosRealizados_Empleador_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
			        		montoTotalTraspasosRealizadosEmpleador.appendChild(montoTotalTraspasosRealizadosEmpleadorMasculino);
			        		
			        		// MontoTotalTraspasosRealizados Empleador Femenino
			        		Element montoTotalTraspasosRealizadosEmpleadorFemenino = doc.createElement("Femenino");
			        		montoTotalTraspasosRealizadosEmpleadorFemenino.appendChild(doc.createTextNode(f.MontoTotalTraspasosRealizados_Empleador_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
			        		montoTotalTraspasosRealizadosEmpleador.appendChild(montoTotalTraspasosRealizadosEmpleadorFemenino);
			        		
		        		// MontoTotalTraspasosRealizados Trabajador
		        		Element montoTotalTraspasosRealizadosTrabajador = doc.createElement("Trabajador");
		        		montoTotalTraspasosRealizados.appendChild(montoTotalTraspasosRealizadosTrabajador);
		        		
			        		// MontoTotalTraspasosRealizados Trabajador Masculino
			        		Element montoTotalTraspasosRealizadosTrabajadorMasculino = doc.createElement("Masculino");
			        		montoTotalTraspasosRealizadosTrabajadorMasculino.appendChild(doc.createTextNode(f.MontoTotalTraspasosRealizados_Trabajador_Masculino.setScale(3,RoundingMode.HALF_UP).toString()));
			        		montoTotalTraspasosRealizadosTrabajador.appendChild(montoTotalTraspasosRealizadosTrabajadorMasculino);
			        		
			        		// MontoTotalTraspasosRealizados Trabajador Femenino
			        		Element montoTotalTraspasosRealizadosTrabajadorFemenino = doc.createElement("Femenino");
			        		montoTotalTraspasosRealizadosTrabajadorFemenino.appendChild(doc.createTextNode(f.MontoTotalTraspasosRealizados_Trabajador_Femenino.setScale(3,RoundingMode.HALF_UP).toString()));
			        		montoTotalTraspasosRealizadosTrabajador.appendChild(montoTotalTraspasosRealizadosTrabajadorFemenino);
	
	    		};//FilaDepositosAhorroPrevisionalVoluntarioColectivoPorPlan
	    		
	    		
	    		// TraspasosDetalle
	    		Element depositosAPVColectivoTraspasosDetalle = doc.createElement("TraspasosDetalle");
	    		depositosAhorroPrevisionalVoluntarioColectivo.appendChild(depositosAPVColectivoTraspasosDetalle);
	    		
	        	for (TraspasosDetalle t : lstDepositosAPVColectivoTraspasosDetalle) {
	    			// Realizados - Recibidos
	        		Element accion = doc.createElement(t.Accion);
	        		depositosAPVColectivoTraspasosDetalle.appendChild(accion);
	        		
	        			//Afp
		        		Element afp = doc.createElement("Afp");
		        		accion.appendChild(afp);
		        		
		        			//Afp NumeroTraspasos
			        		Element afpNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		afpNumeroTraspasos.appendChild(doc.createTextNode(t.Afp_NumeroTraspasos.toString()));
			        		afp.appendChild(afpNumeroTraspasos);
			        		
		        			//Afp Miles
			        		Element afpMiles = doc.createElement("Miles");
			        		afpMiles.appendChild(doc.createTextNode(t.Afp_Miles.toString()));
			        		afp.appendChild(afpMiles);
			        	
			        	//BancosInstitucionesFinancieras
		        		Element bancosInstitucionesFinancieras = doc.createElement("BancosInstitucionesFinancieras");
		        		accion.appendChild(bancosInstitucionesFinancieras);
		        		
		        			//BancosInstitucionesFinancieras NumeroTraspasos
			        		Element bancosInstitucionesFinancierasNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		bancosInstitucionesFinancierasNumeroTraspasos.appendChild(doc.createTextNode(t.BancosInstitucionesFinancieras_NumeroTraspasos.toString()));
			        		bancosInstitucionesFinancieras.appendChild(bancosInstitucionesFinancierasNumeroTraspasos);
			        		
		        			//BancosInstitucionesFinancieras Miles
			        		Element bancosInstitucionesFinancierasMiles = doc.createElement("Miles");
			        		bancosInstitucionesFinancierasMiles.appendChild(doc.createTextNode(t.BancosInstitucionesFinancieras_Miles.toString()));
			        		bancosInstitucionesFinancieras.appendChild(bancosInstitucionesFinancierasMiles);
			        		
		        		//CorredoresBolsa
		        		Element corredoresBolsa = doc.createElement("CorredoresBolsa");
		        		accion.appendChild(corredoresBolsa);
		        		
		        			//CorredoresBolsa NumeroTraspasos
			        		Element corredoresBolsaNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		corredoresBolsaNumeroTraspasos.appendChild(doc.createTextNode(t.CorredoresBolsa_NumeroTraspasos.toString()));
			        		corredoresBolsa.appendChild(corredoresBolsaNumeroTraspasos);
			        		
		        			//CorredoresBolsa Miles
			        		Element corredoresBolsaMiles = doc.createElement("Miles");
			        		corredoresBolsaMiles.appendChild(doc.createTextNode(t.CorredoresBolsa_Miles.toString()));
			        		corredoresBolsa.appendChild(corredoresBolsaMiles);
			        		
		        		//AgentesValores
		        		Element agentesValores = doc.createElement("AgentesValores");
		        		accion.appendChild(agentesValores);
		        		
		        			//AgentesValores NumeroTraspasos
			        		Element agentesValoresNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		agentesValoresNumeroTraspasos.appendChild(doc.createTextNode(t.AgentesValores_NumeroTraspasos.toString()));
			        		agentesValores.appendChild(agentesValoresNumeroTraspasos);
			        		
		        			//AgentesValores Miles
			        		Element agentesValoresMiles = doc.createElement("Miles");
			        		agentesValoresMiles.appendChild(doc.createTextNode(t.AgentesValores_Miles.toString()));
			        		agentesValores.appendChild(agentesValoresMiles);
			        		
		        		//FondosMutuos
		        		Element fondosMutuos = doc.createElement("FondosMutuos");
		        		accion.appendChild(fondosMutuos);
		        		
		        			//FondosMutuos NumeroTraspasos
			        		Element fondosMutuosNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		fondosMutuosNumeroTraspasos.appendChild(doc.createTextNode(t.FondosMutuos_NumeroTraspasos.toString()));
			        		fondosMutuos.appendChild(fondosMutuosNumeroTraspasos);
			        		
		        			//FondosMutuos Miles
			        		Element fondosMutuosMiles = doc.createElement("Miles");
			        		fondosMutuosMiles.appendChild(doc.createTextNode(t.FondosMutuos_Miles.toString()));
			        		fondosMutuos.appendChild(fondosMutuosMiles);
			        		
		        		//FondosInversion
		        		Element fondosInversion = doc.createElement("FondosInversion");
		        		accion.appendChild(fondosInversion);
		        		
		        			//FondosInversion NumeroTraspasos
			        		Element fondosInversionNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		fondosInversionNumeroTraspasos.appendChild(doc.createTextNode(t.FondosInversion_NumeroTraspasos.toString()));
			        		fondosInversion.appendChild(fondosInversionNumeroTraspasos);
			        		
		        			//FondosInversion Miles
			        		Element fondosInversionMiles = doc.createElement("Miles");
			        		fondosInversionMiles.appendChild(doc.createTextNode(t.FondosInversion_Miles.toString()));
			        		fondosInversion.appendChild(fondosInversionMiles);
			        		
		        		//CompaniasSeguros
		        		Element companiasSeguros = doc.createElement("CompaniasSeguros");
		        		accion.appendChild(companiasSeguros);
		        		
		        			//CompaniasSeguros NumeroTraspasos
			        		Element companiasSegurosNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		companiasSegurosNumeroTraspasos.appendChild(doc.createTextNode(t.CompaniasSeguros_NumeroTraspasos.toString()));
			        		companiasSeguros.appendChild(companiasSegurosNumeroTraspasos);
			        		
		        			//CompaniasSeguros Miles
			        		Element companiasSegurosMiles = doc.createElement("Miles");
			        		companiasSegurosMiles.appendChild(doc.createTextNode(t.CompaniasSeguros_Miles.toString()));
			        		companiasSeguros.appendChild(companiasSegurosMiles);
			        		
		        		//FondosVivienda
		        		Element fondosVivienda = doc.createElement("FondosVivienda");
		        		accion.appendChild(fondosVivienda);
		        		
		        			//FondosVivienda NumeroTraspasos
			        		Element fondosViviendaNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		fondosViviendaNumeroTraspasos.appendChild(doc.createTextNode(t.FondosVivienda_NumeroTraspasos.toString()));
			        		fondosVivienda.appendChild(fondosViviendaNumeroTraspasos);
			        		
		        			//FondosVivienda Miles
			        		Element fondosViviendaMiles = doc.createElement("Miles");
			        		fondosViviendaMiles.appendChild(doc.createTextNode(t.FondosVivienda_Miles.toString()));
			        		fondosVivienda.appendChild(fondosViviendaMiles);
			        		
			        	//OtrasEntidadesAutorizadasSvs
		        		Element otrasEntidadesAutorizadasSvs = doc.createElement("OtrasEntidadesAutorizadasSvs");
		        		accion.appendChild(otrasEntidadesAutorizadasSvs);
		        		
		        			//OtrasEntidadesAutorizadasSvs NumeroTraspasos
			        		Element otrasEntidadesAutorizadasSvsNumeroTraspasos = doc.createElement("NumeroTraspasos"+t.Accion);
			        		otrasEntidadesAutorizadasSvsNumeroTraspasos.appendChild(doc.createTextNode(t.OtrasEntidadesAutorizadasSvs_NumeroTraspasos.toString()));
			        		otrasEntidadesAutorizadasSvs.appendChild(otrasEntidadesAutorizadasSvsNumeroTraspasos);
			        		
		        			//OtrasEntidadesAutorizadasSvs Miles
			        		Element otrasEntidadesAutorizadasSvsMiles = doc.createElement("Miles");
			        		otrasEntidadesAutorizadasSvsMiles.appendChild(doc.createTextNode(t.OtrasEntidadesAutorizadasSvs_Miles.toString()));
			        		otrasEntidadesAutorizadasSvs.appendChild(otrasEntidadesAutorizadasSvsMiles);
	        	}; //DepositosAPVColectivoTraspasosDetalle
	    	};//!lstFilaDepositosAhorroPrevisionalVoluntarioColectivoPorPlan.isEmpty()
	    	
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        Transformer transformer = transformerFactory.newTransformer();
	        transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        DOMSource source = new DOMSource(doc);
	        StreamResult result = new StreamResult(new File(entrada.p_Ruta.concat(out.P_OUT_NOMBRE_ARCHIVO)));
	        transformer.transform(source, result);


			LOGGER.info("GenerarAnexo1Circular20: Limpia tabla temporal de SaldosCuentaInversion, SaldosCustodia_paso y SaldosCustodia");
			sql.batch(con, SQL_DELETE_SALDOS_CUENTA_INVERSION, new ArrayList<>(), Object.class);
			sql.batch(con, SQL_DELETE_SALDOS_CUSTODIA_PASO, new ArrayList<>(), Object.class);
			sql.batch(con, SQL_DELETE_SALDOS_CUSTODIA, new ArrayList<>(), Object.class);
			
			LOGGER.info("GenerarAnexo1Circular20: Archivo generado en ruta "+ entrada.p_Ruta.concat(out.P_OUT_NOMBRE_ARCHIVO));
			LOGGER.info("GENERAR ANEXO 1 CIRCULAR 20 :: ruta archivo generado ".concat(entrada.p_Ruta.concat((out.P_OUT_NOMBRE_ARCHIVO))));
		} 
		catch(SQLException e){
			LOGGER.error("GenerarAnexo1Circular20.main(): ERROR AL OBTENER DB CONNECTION", e);
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "GenerarAnexo1Circular20.main(): ERROR AL OBTENER DB CONNECTION";
		}
		catch (TransformerException tfe) {
			LOGGER.error("ERROR TransformerException", tfe);	
			tfe.printStackTrace();
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "ERROR TransformerException";
	    }
		catch(Exception ex){
			LOGGER.error("ERROR AL GENERAR ANEXO 1 CIRCULAR 20", ex);
    		out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "ERROR AL GENERAR ANEXO 1 CIRCULAR 20";
    	}	
		return out;
	}

	public Long validaLongNulo(Long valor) {
		if (valor == null)
			return 0L;
		else
			return valor;
	}
	
	public BigDecimal validaBigDecimalNulo(Double valor) {
		if (valor == null)
			return BigDecimal.valueOf(0);
		else
			return BigDecimal.valueOf(valor);
	}
	
	public static void main(String[] args) throws Exception{
		System.out.println("== TEST ==");
		
		Connection con = PlataformaUtils.getBOLSAConnection();
		
		try {
			GenerarAnexo1Circular20_Entrada entrada = new GenerarAnexo1Circular20_Entrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdModulo = "NOR";
			entrada.p_IdUsuario = 1L;
			entrada.p_FechaSistema = "2020-12-21";
			entrada.p_FechaInicio = "2020-10-01";
			entrada.p_FechaFin = "2020-10-31";
			entrada.p_Ruta = "C:/mnt/optimus/des/empresa1/nor/";
			entrada.p_SecuenciaSolicitud= 944L;
			
			GenerarAnexo1Circular20_Salida Salida = new GenerarAnexo1Circular20_Salida();
			GenerarAnexo1Circular20 sp = new GenerarAnexo1Circular20();
			Salida = sp.logica(con,entrada);
			System.out.println(Salida.P_OUT_MENSAJE);
			con.commit();
		}catch(Exception e){
			con.rollback();
			e.printStackTrace();
		}finally{
			con.close();
		}
		System.out.println("== END ==");
	}
}