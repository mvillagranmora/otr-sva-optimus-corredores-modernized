package cl.bcs.contabilidad;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.sap.pojo.OACT;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EnvioContabilidad implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(EnvioContabilidad.class);
	
	public final String CLASE_CUENTA_VENTAS = "I";
	public final String CLASE_CUENTA_SALIDA = "E";
	
	private String SQL_SELECT_PRECOMPROBANTE = loadResourceToString("cl/bcs/contabilidad/sql/EnvioContabilidad_SelectPreComprobante.sql");
	private String SQL_SELECT_HOMOLOGACION_CAMPOS = loadResourceToString("cl/bcs/contabilidad/sql/EnvioContabilidad_SelectHomologacionCampos.sql");
	private String SQL_SELECT_DEFINICION_ASIENTO_DETALLADO = loadResourceToString("cl/bcs/contabilidad/sql/EnvioContabilidad_SelectDefinicionAsientoDetallado.sql");
	private String SQL_SELECT_DEFINICION_ASIENTO_RESUMIDO = loadResourceToString("cl/bcs/contabilidad/sql/EnvioContabilidad_SelectDefinicionAsientoResumido.sql");
	private String SQL_SELECT_ASIENTO_PROCESO_DETALLADO = loadResourceToString("cl/bcs/contabilidad/sql/EnvioContabilidad_SelectAsientoProcesoDetallado.sql");
	private String SQL_SELECT_ASIENTO_PROCESO_RESUMIDO = loadResourceToString("cl/bcs/contabilidad/sql/EnvioContabilidad_SelectAsientoProcesoResumido.sql");
	private String SQL_SELECT_PRECOMPROBANTE_CREACION = loadResourceToString("cl/bcs/contabilidad/sql/EnvioContabilidad_SelectPrecomprobanteCreacion.sql");
	private String SQL_SELECT_MOVIMIENTO_DATO = loadResourceToString("cl/bcs/contabilidad/sql/EnvioContabilidad_SelectMovimientoDato.sql");
	private String SQL_INSERT_PRECOMPROBANTE_ENVIO_CONTABILIDAD = loadResourceToString("cl/bcs/contabilidad/sql/EnvioContabilidad_InsertPreComprobanteEnvioContabilidad.sql");
	private String SQL_INSERT_PRECOMPROBANTE_ASIENTO_ENVIO_CONTABILIDAD = loadResourceToString("cl/bcs/contabilidad/sql/EnvioContabilidad_InsertPreComprobanteAsientoEnvioContabilidad.sql");
	private String SQL_INSERT_PRECOMPROBANTE_CENTRALIZACION_TIPO = loadResourceToString("cl/bcs/contabilidad/sql/EnvioContabilidad_InsertPreComprobanteCentralizacionTipo.sql");
	
	//CUENTAS SAP
	public String SQL_SELECT_OACT = loadResourceToString("cl/bcs/contabilidad/sql/EnvioContabilidad_SelectOACT.sql");
	
	public static void main (String [] args) throws Exception {
		EntradaEnvioContabilidad e = new EntradaEnvioContabilidad();
		e.p_Destino = "B";
		e.p_IdEmpresa = 1L;
		e.p_IdModulo = "CON";
		e.p_IdUsuario = 21L;
		Secuencia secuencia = new Secuencia();
		secuencia.SecuenciaComprobante = 398L;
		e.p_SecCmbte.add(secuencia);
		e.p_UsaReglasNegocio = "N";
		
		EnvioContabilidad ee = new EnvioContabilidad();
		ee.logica(PlataformaUtils.getDESAConnection(), e);
	}

	public static class EntradaEnvioContabilidad {
		
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Destino;
		public String p_UsaReglasNegocio;
	    public List<Secuencia> p_SecCmbte;
		
		public EntradaEnvioContabilidad() {
			this.p_SecCmbte = new ArrayList<Secuencia>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class Secuencia {
		public Long SecuenciaComprobante;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaEnvioContabilidad {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public List<PreComprobanteSalida> P_OUT_PRECOMPROBANTE;
		public List<PreComprobanteAsientoSalida> P_OUT_ASIENTOS;
		public List<Long> P_OUT_TIPOCENTRALIZACION;
		
		public SalidaEnvioContabilidad() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_PRECOMPROBANTE = new ArrayList<PreComprobanteSalida>();
			this.P_OUT_ASIENTOS = new ArrayList<PreComprobanteAsientoSalida>();
			this.P_OUT_TIPOCENTRALIZACION = new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class PreComprobanteSalida {
		public Long JdtNum;
		public String ReferenceDate;
		public String Memo;
		public String DueDate;
		public String TaxDate;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PreComprobanteAsientoSalida {
		public Long IdTipoContabilidad;
		public Long ParentKey;
		public BigDecimal Debit;
		public BigDecimal Credit;
		public String U_glosa;
		public String LineMemo;
		public String ProfitCode;
		public Long BPLId;
		public String AccountCode;
		public String DueDate;
		public String Formato;
		public String TieneProject;
		public String Project;
		public String ProductoMovimiento;
		public String RelacionCorredora;
		public String CodSecuencia;
		public Long IdMovimiento;
		public String Reference1;
		public String Reference2;
		public String AdditionalReference;
		public BigDecimal FCDebit;
		public BigDecimal FCCredit;
		public String FCCurrency;
		public String ShortName;
		public String TipoSocioNegocio;
		public String ReferenceDate1;
		public String TaxDate;
		public String CostingCode;
		public String CostingCode2;
		public String CostingCode3;
		public String CostingCode4;
		public String CostingCode5;
		public String U_BCS_TipoPersona;
		public String U_BCS_Rut;
		public String U_BCS_Portafolio;
		public String U_BCS_Nombre;
		public String U_BCS_CtaDCV;
		public String U_BCS_EmitidoA;
		public String U_BCS_Comprobante;
		public String U_BCS_Folio;
		public String U_BCS_NDcto;
		public String U_BCS_Concepto;
		public String U_BCS_CV;
		public String U_BCS_Nemo;
		public String U_BCS_IFRSCtaPropia;
		public String U_BCS_CodigoProducto;
		public String U_BCS_Bolsa;
		public String U_BCS_Relacion;
		public String U_RelacionCodigoSap;
		public String U_BCS_FolioDTE;
		
		public PreComprobanteAsientoSalida(){
			this.TieneProject = "";
			this.Project = "";
			this.ProductoMovimiento = "";
			this.RelacionCorredora = "";
			this.CodSecuencia = "";
			this.AdditionalReference = "";
			this.DueDate = "";
			this.ShortName = "";
			this.TipoSocioNegocio = "";
			this.TaxDate = "";
			this.Reference1 = "";
			this.Reference2 = "";
			this.CostingCode = "";
			this.CostingCode2 = "";
			this.CostingCode3 = "";
			this.CostingCode4 = "";
			this.CostingCode5 = "";
			this.U_BCS_TipoPersona = "";
			this.U_BCS_Rut = "";
			this.U_BCS_Portafolio = "";
			this.U_BCS_Nombre = "";
			this.U_BCS_CtaDCV = "";
			this.U_BCS_EmitidoA = "";
			this.U_BCS_Comprobante = "";
			this.U_BCS_Folio = "";
			this.U_BCS_NDcto = "";
			this.U_BCS_Concepto = "";
			this.U_BCS_CV = "";
			this.U_BCS_Nemo = "";
			this.U_BCS_IFRSCtaPropia = "";
			this.U_BCS_CodigoProducto = "";
			this.U_BCS_Bolsa = "";
			this.U_BCS_Relacion = "";
			this.U_RelacionCodigoSap = "";
			this.U_BCS_FolioDTE = "";
		}
		
		public Long getIdMovimiento(){
			return this.IdMovimiento;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PreComprobanteProceso {
		public Long IdPreComprobante;
		public Long IdTipoContabilidad;
		public String FechaContable;
		public String Estado;
		public Long IdTipoCentralizacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DefinicionAsientoProceso {
		
		public String IdCuentaContable;
		public String Formato;
		public Long Suma;
		public String ConRut;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CampoHomologadoSap {
		
		public Long IdTipoCentralizacion;
		public String CampoSAP;
		public String CampoTipoCentralizacion;
		public String IdCampoSAP;
		public String IdCampoTipoCentralizacion;
		public String toString() {
			return Utils.printJson(this, false);
		}
	
	}
	
	public static class MovimientoDato {
		
		public Long IdMovimiento;
		public String IdTipoDatoEvento;
		public String ValorVarchar;
		public BigDecimal ValorDecimal;
		public String Estado;
		public String TipoEvento;
		public String TipoDato;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class AsientoXProceso {
		public Long IdEvento;
		public Long IdMovimiento;
		public Long IdTipoContabilidad;
		public Long IdTipoCentralizacion;
		public Long IdPreComprobante;
		public String CodigoMoneda;
		public BigDecimal Monto;
		public BigDecimal MontoDebe;
		public BigDecimal MontoHaber;
		public String Glosa;
		public String GlosaDetalle;
		public String CentroCosto;
		public String DocumentoIdentidad;
		public Long Cuenta;
		public String FechaContable;
		public String CuentaContabilidad;
		public String CodigoContabilidad;
		public String IdCuentaContable;
		public String NombreCuenta;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CamposHomologadosSAP{
		public String CampoTipoCentralizacion;
		public String CampoSAP;
		public Long IdTipoCentralizacion;
		public String Id;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PreComprobanteCentralizacionTipo{
		public Long IdPreComprobante;
		public Long IdTipoCentralizacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SalidaEnvioContabilidad logica(Connection con, EntradaEnvioContabilidad entrada) throws Exception {
		long init = System.currentTimeMillis();
		logger.info("INICIO EnvioContabilidad");
		SalidaEnvioContabilidad out = new SalidaEnvioContabilidad();
		
		SQL _sql = new SQL();
		String usaCuentasSYS = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "CONTABILIDAD", "UsaCuentasSYS");
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		String prefijoCliente = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "CONTABILIDAD", "PrefijoClienteSAP");
		String prefijoContraparte = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "CONTABILIDAD", "PrefijoContraparteSAP");
		String prefijoProveedor = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "CONTABILIDAD", "PrefijoProveedorSAP");
		String usaCentroCosto = "N";
		try {
			usaCentroCosto =  ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "CONTABILIDAD", "UsaCentroCosto");
		}catch(Exception e) {
			logger.warn("Error al encontrar parametro UsaCentroCosto", e);
		}
		
		String calculaParidadCLPCentralizacionTesoreria;
		try
		{
			calculaParidadCLPCentralizacionTesoreria = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "CONTABILIDAD", "CalculaParidadCLPCentralizacionTesoreria");
		}	
		catch (Exception e)
		{
			calculaParidadCLPCentralizacionTesoreria = "N"; // valor pod defecto en caso que no exista parametro
		}
		
		
		Properties prop = new Properties();
		try {
        	String rutaArchivosConfiguracion = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.RutaArchivosConfiguracion");
        	InputStream input = new FileInputStream(rutaArchivosConfiguracion + "jobm/interfazSAP.properties");        
			prop.load(input);
		} catch (FileNotFoundException e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ARCHIVO_CONFIGURACION_NO_ENCONTRADO","Archivo de configuraci�n no encontrado"));
			logger.error(e);
			return out;
		} catch (IOException e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ARCHIVO_CONFIGURACION_PROBLEMA_LECTURA","Problemas al leer archivo de configuraci�n no encontrado"));
			logger.error(e);
			return out;
		}
        
		List<OACT> cuentas = new ArrayList<>();
		try(Connection sapConnection = Utils.getSAPConnection(prop.getProperty("HANA_SERVER"),prop.getProperty("HANA_PORT"),prop.getProperty("B1_COMPANY"),prop.getProperty("HANA_USER"),prop.getProperty("HANA_PASS"));){
			cuentas = _sql.query(sapConnection, SQL_SELECT_OACT.replace(":EMPRESA", prop.getProperty("B1_COMPANY")), new HashMap<>(), OACT.class);	
		}
        
		Map<String,OACT> hashCuentas = cuentas.stream().collect(Collectors.toMap(cuenta->cuenta.AcctCode, cuenta->cuenta));
		
		List<Long> secuencias = new ArrayList<Long>();
		
		for(Secuencia secuencia : entrada.p_SecCmbte){
			if(!secuencias.contains(secuencia.SecuenciaComprobante)) secuencias.add(secuencia.SecuenciaComprobante);
		}
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdPrecomprobante", secuencias);
		List<PreComprobanteProceso> listaPre = _sql.query(con, SQL_SELECT_PRECOMPROBANTE, params , PreComprobanteProceso.class);
		
		List<PreComprobanteCentralizacionTipo> precomprobantesCentralizacionTipo = new ArrayList<PreComprobanteCentralizacionTipo>();
		List<Long> secuenciasProceso = new ArrayList<Long>();
		for(Secuencia secuencia : entrada.p_SecCmbte){
			PreComprobanteProceso precomprobanteProceso = null;
			for(PreComprobanteProceso pre : listaPre){
				if(pre.IdPreComprobante.equals(secuencia.SecuenciaComprobante)){
					precomprobanteProceso = pre;
					if(!out.P_OUT_TIPOCENTRALIZACION.contains(precomprobanteProceso.IdTipoCentralizacion)) out.P_OUT_TIPOCENTRALIZACION.add(pre.IdTipoCentralizacion);
				}
			}
			
			if(precomprobanteProceso == null){
				HashMap<String,String> valores = new HashMap<String,String>();
				valores.put("NumeroPreComprobante", String.valueOf(secuencia.SecuenciaComprobante));
				out.P_OUT_MENSAJES.add(new MensajeSalida(secuencia.SecuenciaComprobante,MensajeSalida.MENSAJE_ADVERTENCIA,"NO_EXISTE_PRE_COMPROBANTE",valores,con,entrada.p_IdEmpresa));
			}else{
				if("C".equals(precomprobanteProceso.Estado) || "A".equals(precomprobanteProceso.Estado)){
					HashMap<String,String> valores = new HashMap<String,String>();
					valores.put("NumeroPreComprobante", String.valueOf(secuencia.SecuenciaComprobante));
					String mensaje = "D".equals(precomprobanteProceso.Estado) ? "PRE_COMPROBANTE_DESCUADRADO" : "PRE_COMPROBANTE_CONTABILIZADO_O_ANULADO";
					out.P_OUT_MENSAJES.add(new MensajeSalida(secuencia.SecuenciaComprobante,MensajeSalida.MENSAJE_ADVERTENCIA,mensaje,valores,con,entrada.p_IdEmpresa));
				}else{
					if(!secuenciasProceso.contains(secuencia.SecuenciaComprobante)) secuenciasProceso.add(secuencia.SecuenciaComprobante);
					PreComprobanteCentralizacionTipo precomprobanteCentralizacionTipo = new PreComprobanteCentralizacionTipo();
					precomprobanteCentralizacionTipo.IdPreComprobante = precomprobanteProceso.IdPreComprobante;
					precomprobanteCentralizacionTipo.IdTipoCentralizacion = precomprobanteProceso.IdTipoCentralizacion;
					precomprobantesCentralizacionTipo.add(precomprobanteCentralizacionTipo);
				}
			}
		}
		
		params.clear();
		params.put("IdPrecomprobante", secuenciasProceso);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		List<DefinicionAsientoProceso> definicionesDetallado = _sql.query(con, SQL_SELECT_DEFINICION_ASIENTO_DETALLADO, params , DefinicionAsientoProceso.class);
		HashMap<String,Object> hDefinicionesDetallado = Utils.List2Hash(definicionesDetallado, "IdCuentaContable");
		params.clear();
		params.put("IdPrecomprobante", secuenciasProceso);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		List<DefinicionAsientoProceso>	definicionesResumido = _sql.query(con, SQL_SELECT_DEFINICION_ASIENTO_RESUMIDO, params , DefinicionAsientoProceso.class);
		HashMap<String,Object>	hDefinicionesResumido = Utils.List2Hash(definicionesResumido, "IdCuentaContable"); 
		params.clear();
		params.put("IdTipoCentralizacion", out.P_OUT_TIPOCENTRALIZACION);
		List<CampoHomologadoSap> campos = _sql.query(con, SQL_SELECT_HOMOLOGACION_CAMPOS, params, CampoHomologadoSap.class);
		
		params.clear();
		params.put("IdPrecomprobante", secuenciasProceso);
		out.P_OUT_PRECOMPROBANTE.addAll(_sql.query(con, SQL_SELECT_PRECOMPROBANTE_CREACION, params, PreComprobanteSalida.class));
		
		// DETALLADO
		if (definicionesDetallado.size() > 0) {
		    params.clear();
		    params.put("IdPrecomprobante", secuenciasProceso);
		    List<AsientoXProceso> asientosProceso = _sql.query(con, SQL_SELECT_ASIENTO_PROCESO_DETALLADO, params, AsientoXProceso.class);
		    List<MovimientoDato> movimientosDato = _sql.query(con, SQL_SELECT_MOVIMIENTO_DATO, params, MovimientoDato.class);
		    final Map<Long, List<MovimientoDato>> movimientosPorIdMovimiento = 	movimientosDato.stream()
                    															.filter(m -> m.IdMovimiento != null)
            																	.collect(Collectors.groupingBy(m -> m.IdMovimiento));		            
		    final Map<Long, List<CampoHomologadoSap>> camposPorCentralizacion = campos.stream()
                    															.filter(c -> c.IdTipoCentralizacion != null)
                																.collect(Collectors.groupingBy(c -> c.IdTipoCentralizacion));
		    for (AsientoXProceso asiento : asientosProceso) {

		        DefinicionAsientoProceso cuenta = (DefinicionAsientoProceso) hDefinicionesDetallado.get(asiento.IdCuentaContable);

		        PreComprobanteAsientoSalida precomprobanteAsientoSalida = new PreComprobanteAsientoSalida();
		        precomprobanteAsientoSalida.IdTipoContabilidad = asiento.IdTipoContabilidad;
		        precomprobanteAsientoSalida.ParentKey = asiento.IdPreComprobante;

		        precomprobanteAsientoSalida.TieneProject = cuenta != null ? cuenta.ConRut : "";
		        if (cuenta != null && "S".equals(cuenta.ConRut)) {
		            precomprobanteAsientoSalida.Project = asiento.DocumentoIdentidad;
		        } else {
		            precomprobanteAsientoSalida.Project = "";
		        }

		        precomprobanteAsientoSalida.U_glosa = asiento.Glosa;
		        precomprobanteAsientoSalida.LineMemo = asiento.GlosaDetalle.length() > 50 ? asiento.GlosaDetalle.substring(0, 50) : asiento.GlosaDetalle;
		        precomprobanteAsientoSalida.ProfitCode = asiento.CentroCosto;
		        precomprobanteAsientoSalida.AccountCode = "S".equals(usaCuentasSYS) ? asiento.CodigoContabilidad : asiento.CuentaContabilidad;
		        precomprobanteAsientoSalida.Formato = "Detallado";
		        precomprobanteAsientoSalida.BPLId = 4L;
		        precomprobanteAsientoSalida.Reference1 = asiento.DocumentoIdentidad;
		        precomprobanteAsientoSalida.Reference2 = String.valueOf(asiento.Cuenta);
		        precomprobanteAsientoSalida.ReferenceDate1 = asiento.FechaContable;
		        precomprobanteAsientoSalida.IdMovimiento = asiento.IdMovimiento;

		        OACT oACT = hashCuentas.get(precomprobanteAsientoSalida.AccountCode);
		        boolean esCuentaAnalitica = oACT != null && "Y".equals(oACT.LocManTran);
		        boolean esCuentaCostingCode = oACT != null &&
		                (CLASE_CUENTA_SALIDA.equalsIgnoreCase(oACT.ActType) || CLASE_CUENTA_VENTAS.equalsIgnoreCase(oACT.ActType));

		        List<CampoHomologadoSap> camposAsiento = camposPorCentralizacion.getOrDefault(asiento.IdTipoCentralizacion, Collections.emptyList());
		        List<MovimientoDato> datosMovimiento = movimientosPorIdMovimiento.getOrDefault(asiento.IdMovimiento, Collections.emptyList());		                
		        BigDecimal monto = BigDecimal.ZERO;

		        for (MovimientoDato mvto : datosMovimiento) {

		            for (CampoHomologadoSap campo : camposAsiento) {
		                if (campo.CampoTipoCentralizacion != null && campo.CampoTipoCentralizacion.equals(mvto.IdTipoDatoEvento)) {

		                    // CASO MONTO MOVIMIENTO
		                    if ("MontoMonedaSistema".equals(campo.CampoSAP)) {
		                        monto = "N".equals(mvto.TipoDato) ? mvto.ValorDecimal : new BigDecimal(mvto.ValorVarchar);
		                        monto = monto.abs(MathContext.DECIMAL128);
		                        continue;
		                    }

		                    try {
		                        Field f = precomprobanteAsientoSalida.getClass().getDeclaredField(campo.CampoSAP);
		                        if (f != null) {
		                            String value = "N".equals(mvto.TipoDato) ? mvto.ValorDecimal.toPlainString() : mvto.ValorVarchar;
		                            f.set(precomprobanteAsientoSalida, value);
		                        }
		                    } catch (Exception e) {
		                        logger.warn("No usa campo:" + campo.CampoSAP, e);
		                    }
		                }
		            }

		            // CASOS ESPECIALES
		            switch (mvto.IdTipoDatoEvento) {
		                case "ProductoMovimiento": {
		                    precomprobanteAsientoSalida.ProductoMovimiento = mvto.ValorVarchar;
		                    break;
		                }
		                case "RelacionCorredora": {
		                    precomprobanteAsientoSalida.RelacionCorredora = mvto.ValorVarchar;
		                    break;
		                }
		                case "CodSecuencia": {
		                    precomprobanteAsientoSalida.CodSecuencia = mvto.ValorVarchar;
		                    break;
		                }
		                case "EsContraparte": {
		                    if ("".equals(precomprobanteAsientoSalida.ShortName)) {
		                        if ("SI".equals(mvto.ValorVarchar)) {
		                            precomprobanteAsientoSalida.ShortName = esCuentaAnalitica ? prefijoContraparte + asiento.DocumentoIdentidad : "";
		                            precomprobanteAsientoSalida.TipoSocioNegocio = "CONTRAPARTE";
		                        } else {
		                            precomprobanteAsientoSalida.ShortName = esCuentaAnalitica ? prefijoCliente + asiento.DocumentoIdentidad : "";
		                            precomprobanteAsientoSalida.TipoSocioNegocio = "CLIENTE";
		                        }
		                    }
		                    break;
		                }
		                case "EsProveedor": {
		                    if ("SI".equals(mvto.ValorVarchar)) {
		                        precomprobanteAsientoSalida.ShortName = esCuentaAnalitica ? prefijoProveedor + asiento.DocumentoIdentidad : "";
		                        precomprobanteAsientoSalida.TipoSocioNegocio = "PROVEEDOR";
		                    }
		                    break;
		                }
						case "FolioDTE":{
							precomprobanteAsientoSalida.U_BCS_FolioDTE = mvto.ValorVarchar;
							if(!"0".equals(mvto.ValorVarchar)){
								String sufijo = "/" + mvto.ValorVarchar;
								int maxBaseLength = 50 - sufijo.length();

								String baseMemo = precomprobanteAsientoSalida.LineMemo;
								if (baseMemo.length() > maxBaseLength) {
								    baseMemo = baseMemo.substring(0, maxBaseLength);
								}

								precomprobanteAsientoSalida.LineMemo = baseMemo + sufijo;
							}
							break;
						}
		            }

		            // CENTRO COSTOS
		            if ("S".equals(usaCentroCosto) && esCuentaCostingCode) {
		                switch (mvto.IdTipoDatoEvento) {
		                    case "CostingCode": {
		                        precomprobanteAsientoSalida.CostingCode = mvto.ValorVarchar;
		                        break;
		                    }
		                    case "CostingCode2": {
		                        precomprobanteAsientoSalida.CostingCode2 = mvto.ValorVarchar;
		                        break;
		                    }
		                    case "CostingCode3": {
		                        precomprobanteAsientoSalida.CostingCode3 = mvto.ValorVarchar;
		                        break;
		                    }
		                    case "CostingCode4": {
		                        precomprobanteAsientoSalida.CostingCode4 = mvto.ValorVarchar;
		                        break;
		                    }
		                    case "CostingCode5": {
		                        precomprobanteAsientoSalida.CostingCode5 = mvto.ValorVarchar;
		                        break;
		                    }
		                }
		            }
		        }

		        if (precomprobanteAsientoSalida.ShortName.isEmpty() && esCuentaAnalitica) {
		            precomprobanteAsientoSalida.ShortName = "C" + asiento.DocumentoIdentidad;
		            precomprobanteAsientoSalida.TipoSocioNegocio = "CLIENTE";
		        }


		        if (asiento.MontoDebe.compareTo(BigDecimal.ZERO) == 1) {
		            if (monedaSistema.equals(asiento.CodigoMoneda)
		                    || (calculaParidadCLPCentralizacionTesoreria.equalsIgnoreCase("S") && asiento.IdTipoCentralizacion.compareTo(210L) == 0)) {
		                precomprobanteAsientoSalida.Debit = asiento.Monto.abs(MathContext.DECIMAL128);
		                precomprobanteAsientoSalida.FCDebit = BigDecimal.ZERO;
		                precomprobanteAsientoSalida.FCCurrency = "";
		            } else {
		                precomprobanteAsientoSalida.FCDebit = asiento.Monto.abs(MathContext.DECIMAL128);
		                precomprobanteAsientoSalida.Debit = monto;
		                precomprobanteAsientoSalida.FCCurrency = asiento.CodigoMoneda;
		            }
		        } else {
		            precomprobanteAsientoSalida.Debit = BigDecimal.ZERO;
		            precomprobanteAsientoSalida.FCDebit = BigDecimal.ZERO;
		        }

		        if (asiento.MontoHaber.compareTo(BigDecimal.ZERO) == 1) {
		            if (monedaSistema.equals(asiento.CodigoMoneda)
		                    || (calculaParidadCLPCentralizacionTesoreria.equalsIgnoreCase("S") && asiento.IdTipoCentralizacion.compareTo(210L) == 0)) {
		                precomprobanteAsientoSalida.Credit = asiento.Monto.abs(MathContext.DECIMAL128);
		                precomprobanteAsientoSalida.FCCredit = BigDecimal.ZERO;
		                precomprobanteAsientoSalida.FCCurrency = "";
		            } else {
		                precomprobanteAsientoSalida.FCCredit = asiento.Monto.abs(MathContext.DECIMAL128);
		                precomprobanteAsientoSalida.Credit = monto;
		                precomprobanteAsientoSalida.FCCurrency = asiento.CodigoMoneda;
		            }
		        } else {
		            precomprobanteAsientoSalida.Credit = BigDecimal.ZERO;
		            precomprobanteAsientoSalida.FCCredit = BigDecimal.ZERO;
		        }

		        out.P_OUT_ASIENTOS.add(precomprobanteAsientoSalida);
		    }
		}

		
		//RESUMIDO
		if(definicionesResumido.size() > 0){
			params.clear();
			params.put("IdPrecomprobante", secuenciasProceso);
			List<AsientoXProceso> asientosProceso = _sql.query(con, SQL_SELECT_ASIENTO_PROCESO_RESUMIDO, params, AsientoXProceso.class);
			for(AsientoXProceso asiento : asientosProceso){
				DefinicionAsientoProceso cuenta = (DefinicionAsientoProceso) hDefinicionesResumido.get(asiento.IdCuentaContable);
				
					PreComprobanteAsientoSalida precomprobanteAsientoSalida = new PreComprobanteAsientoSalida();
					precomprobanteAsientoSalida.IdTipoContabilidad = asiento.IdTipoContabilidad;
					precomprobanteAsientoSalida.ParentKey = asiento.IdPreComprobante;
					precomprobanteAsientoSalida.Debit = asiento.MontoDebe;
					precomprobanteAsientoSalida.Credit = asiento.MontoHaber;
					precomprobanteAsientoSalida.FCDebit = BigDecimal.ZERO;
					precomprobanteAsientoSalida.FCCredit = BigDecimal.ZERO;
					precomprobanteAsientoSalida.FCCurrency = "";
					precomprobanteAsientoSalida.U_glosa = asiento.Glosa;
					precomprobanteAsientoSalida.LineMemo = asiento.GlosaDetalle.length() > 50 ? asiento.GlosaDetalle.substring(0, 50) : asiento.GlosaDetalle;
					precomprobanteAsientoSalida.ProfitCode = asiento.CentroCosto;
					precomprobanteAsientoSalida.BPLId = 4L;
					precomprobanteAsientoSalida.AccountCode = "S".equals(usaCuentasSYS) ? asiento.CodigoContabilidad : asiento.CuentaContabilidad;
					precomprobanteAsientoSalida.Formato = "Agrupado";
					precomprobanteAsientoSalida.DueDate = asiento.FechaContable;
					precomprobanteAsientoSalida.TieneProject = "N";
					precomprobanteAsientoSalida.Project = "";
					precomprobanteAsientoSalida.U_glosa = asiento.Glosa;
					precomprobanteAsientoSalida.LineMemo = asiento.GlosaDetalle;
					precomprobanteAsientoSalida.ReferenceDate1 = "";
					precomprobanteAsientoSalida.IdMovimiento = 0L;
					out.P_OUT_ASIENTOS.add(precomprobanteAsientoSalida);
				
			}
		}
		List<PreComprobanteSalida> preComprobanteInsert = new ArrayList<>();
		if(out.P_OUT_PRECOMPROBANTE.size() > 0) {
			    preComprobanteInsert = out.P_OUT_PRECOMPROBANTE.parallelStream().map(id -> {
				PreComprobanteSalida preComprobanteSalidaInsert = new PreComprobanteSalida();
				preComprobanteSalidaInsert.JdtNum = id.JdtNum;
				preComprobanteSalidaInsert.ReferenceDate = id.ReferenceDate;
				preComprobanteSalidaInsert.DueDate = id.DueDate;
				preComprobanteSalidaInsert.Memo = id.Memo.length() > 50 ? id.Memo.substring(0, 50) : id.Memo;
				preComprobanteSalidaInsert.TaxDate = id.TaxDate;
				return preComprobanteSalidaInsert;
			}).collect(Collectors.toList());
		}
		_sql.batch(con, SQL_INSERT_PRECOMPROBANTE_ENVIO_CONTABILIDAD, preComprobanteInsert, PreComprobanteSalida.class);
		logger.info("Termino Query: SQL_INSERT_PRECOMPROBANTE_ENVIO_CONTABILIDAD");
		_sql.batch(con, SQL_INSERT_PRECOMPROBANTE_ASIENTO_ENVIO_CONTABILIDAD, out.P_OUT_ASIENTOS, PreComprobanteAsientoSalida.class);
		logger.info("Termino Query: SQL_INSERT_PRECOMPROBANTE_ASIENTO_ENVIO_CONTABILIDAD");
		_sql.batch(con, SQL_INSERT_PRECOMPROBANTE_CENTRALIZACION_TIPO, precomprobantesCentralizacionTipo, PreComprobanteCentralizacionTipo.class);
		logger.info("Termino Query: SQL_INSERT_PRECOMPROBANTE_CENTRALIZACION_TIPO");
		logger.info("FIN EnvioContabilidad(ms): {}", System.currentTimeMillis() - init);
		return out;
		
	}
	
	 

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaEnvioContabilidad in = gson.fromJson(jin.toString(), EntradaEnvioContabilidad.class);
		SalidaEnvioContabilidad out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	
}
