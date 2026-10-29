package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.ObtenerFolioSuscripcion;
import cl.bcs.APV.ObtenerFolioSuscripcion.ObtenerFolioSuscripcion_Entrada;
import cl.bcs.APV.ObtenerFolioSuscripcion.ObtenerFolioSuscripcion_Salida;
import cl.bcs.fondomutuo.AnulacionContratoCSC.EntradaAnulacionContrato;
import cl.bcs.fondomutuo.AnulacionContratoCSC.InContrato;
import cl.bcs.fondomutuo.CrearIngresoCSC.ContratoSuscripcionCuotas;
import cl.bcs.fondomutuo.CrearIngresoCSC.INMandatoPAC;
import cl.bcs.fondomutuo.CrearIngresoCSC.MandatoPAC;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.AnulacionMandatoPAC;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarCSC implements JavaCall{

	private final Log log = new Log("FM.ModificarCSC");

	private final String SQL_INSERT_CONTRATO = loadResourceToString("cl/bcs/fondomutuo/sql/CrearIngresoCSC_InsertContrato.sql");
	private final String SQL_INSERT_CONTRATO_DETALLE = loadResourceToString("cl/bcs/fondomutuo/sql/CrearIngresoCSC_InsertContratoDetalle.sql");
	private final String SQL_SELECT_CONTRATO_BY_ID = loadResourceToString("cl/bcs/fondomutuo/sql/CrearIngresoContratoCSC_SelectContratoById.sql");
	private final String SQL_SELECT_FONDO_BY_ID = loadResourceToString("cl/bcs/fondomutuo/sql/CrearIngresoContratoCSC_SelectFondoById.sql");
	private final String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ModificarCSC_ConsultarPortafolio.sql");
	private final String SQL_SELECT_MANDATO_PAC = loadResourceToString("cl/bcs/fondomutuo/sql/ModificarCSC_ConsultarMandatoPAC.sql");
	private final String SQL_INSERT_MANDATO_PAC = loadResourceToString("cl/bcs/fondomutuo/sql/ModificarCSC_InsertarMandatoPAC.sql");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaModificarCSC{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public List<InModificarCSC> p_ModificarCSC;	

		public EntradaModificarCSC() {
			this.p_ModificarCSC = new ArrayList<InModificarCSC>();	
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InModificarCSC{
		public Long IdFolioCSC;
		public String FolioCSC;
		public Long IdCliente;
		public Long IdPortafolio;
		public String Hora;
		public Date Fecha;
		public BigDecimal Monto;		
		public BigDecimal MontoPesos;
		public BigDecimal MontoUF;		
		public String Moneda;
		public String TipoPago;
		public Long IdBanco;
		public String DomicilioSucursal;
		public String CodigoSucursal;
		public String NombreFuncionario;
		public String DocumentoIdentificacionFuncionario;
		public String FirmaFuncionario;
		public Long DiaDescuento;
		public String AceptoCondiciones;
		public String Estado;
		public String Canal;
		public String IdFolioEdit;
		public String IdEstado;
		public String FolioRelacionado;
		public String MLT;
		
		public List<InModificarCSCDetalle> p_ModificarCSCDetalle;
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	public static class InModificarCSCDetalle{
		public String Serie;
		public BigDecimal Monto;
		public BigDecimal Porcentaje;
		public String Regimen;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaModificarCSC{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoModificarCSC> P_OUT_RESULTADO;

		public SalidaModificarCSC(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoModificarCSC>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoModificarCSC{
		public String FolioCSC;
		public Long IdFolioCSC;
	}
	
	public static class ContratoSuscripcionCuotas{
		public Long IdFolioCSC;
		public BigDecimal MontoPesos;
		public BigDecimal MontoUF;
		public String FolioCSC;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ContratoSuscripcionCuotasDetalle{
		public Long IdCSC;
		public String Serie;
		public BigDecimal Monto;
		public BigDecimal Porcentaje;
		public String Regimen;
		public Long IdFolioCSC;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class FondoIdSerie{
		public String IdSerieFondo;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ContratoSuscripcionCuotasOptimus{
		public Long IdFolioCSC;
		public Long IdCliente;
		public Long IdPortafolio;
		public String Hora;
		public Date Fecha;
		public BigDecimal MontoPesos;
		public BigDecimal MontoUF;
		public BigDecimal MontoUSD;
		public String TipoPago;
		public Long IdBanco;
		public String DomicilioSucursal;
		public String CodigoSucursal;
		public String NombreFuncionario;
		public String DocumentoIdentificacionFuncionario;
		public String FirmaFuncionario;
		public Long DiaDescuento;
		public String AceptoCondiciones;
		public String Estado;
		public String FolioCSC;
		public Long IdInterno;
		public String Canal;
		public String FolioRelacionado;
		public String MLT;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ContratoSuscripcionCuotasDetalleOptimus{
		public Long IdCSC;
		public String Serie;
		public BigDecimal Monto;
		public BigDecimal Porcentaje;
		public String Regimen;
		public Long IdFolioCSC;
				
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoId{
		
		public Long ResultadoId;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class PortafolioProposito {
		public String FolioCSC;
		public Long IdPortafolio;
		public String Proposito;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class MandatoPAC{
		public Long IdMandatoPAC;
		public Long IdCliente;
		public Long IdPortafolio;
		public String FolioCSC;
		public Long FolioPAC;
		public String CuentaCorriente;
		public String FolioSAAPV;
		public String Receptor;
		public Date Fecha;
		public Long NumeroMandato;
		public String Firma;
		public String IdentServicio;
		public String Estado;
		public String EstadoCargo;
		public Long IdEmpresa;
		public String Canal;
		public String EstadoRendicion;
		public String EstadoCargoBanco;
		public Long IdCarga;
		public Long IdCuentaBancaria;
		public String EstadoPorAutorizar;
		public String EstadoContratoPorAutorizar;
		public Long DiaDescuentoPorAutorizar;
		public String Compuesto;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class INMandatoPAC{
		public Long IdCliente;
		public Long IdPortafolio;
		public String FolioCSC;
		public Long FolioPAC;
		public String CuentaCorriente;
		public String FolioSAAPV;
		public String Receptor;
		public Date Fecha;
		public Long NumeroMandato;
		public String Firma;
		public String IdentServicio;
		public String Estado;
		public String EstadoCargo;
		public Long IdEmpresa;
		public String Canal;
		public String EstadoRendicion;
		public String EstadoCargoBanco;
		public Long IdCarga;
		public Long IdCuentaBancaria;
		public String EstadoPorAutorizar;
		public String EstadoContratoPorAutorizar;
		public Long DiaDescuentoPorAutorizar;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaModificarCSC jin = gson.fromJson(in.toString(), EntradaModificarCSC.class);
		SalidaModificarCSC out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	//map
	public SalidaModificarCSC logica (Connection con, EntradaModificarCSC entrada) throws Exception{		
		log.write("INICIO ModificarCSC");
		
		SQL sql = new SQL();
		SalidaModificarCSC out 							= 	new SalidaModificarCSC();
		RevisarRequisitosBasicos basicos 				= 	new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos 	= 	basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		AnulacionContratoCSC anularContrato 			= 	new AnulacionContratoCSC();
		ArrayList<String> correlativos 					= 	new ArrayList<String>();
		AnulacionMandatoPAC anularMandato = new AnulacionMandatoPAC();
		AnulacionMandatoPAC.EntradaAnulacionContrato entradaAnularMandato = new AnulacionMandatoPAC.EntradaAnulacionContrato();
		
		List<ContratoSuscripcionCuotasOptimus> listContratoSuscripcionCuotasOptimus 				= 	new ArrayList<ContratoSuscripcionCuotasOptimus>();
		List<ContratoSuscripcionCuotasDetalleOptimus> listContratoSuscripcionCuotasDetalleOptimus 	= 	new ArrayList<ContratoSuscripcionCuotasDetalleOptimus>();
		
		ObtenerFolioSuscripcion  obtenerFolioSuscripcion											= 	new ObtenerFolioSuscripcion();
		List<INMandatoPAC> listMandatoPAC = new ArrayList<INMandatoPAC>();
		EntradaAnulacionContrato entradaAnulacionContrato 											= 	new EntradaAnulacionContrato();
		entradaAnulacionContrato.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaAnulacionContrato.p_IdModulo = entrada.p_IdModulo;
		entradaAnulacionContrato.p_IdUsuario = entrada.p_IdUsuario;
		HashMap<String,Object> params = new HashMap<String,Object>();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String horaSistema = FechaDB.ObtenerHoraSistemaLocal(con, entrada.p_IdEmpresa);
		
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			return out;
		}
		
		if(entrada.p_ModificarCSC.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES_DE_CONTRATO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		
		
		for (InModificarCSC mov: entrada.p_ModificarCSC) {

			
			if(mov.Canal.equalsIgnoreCase("OPT") ) {
				
				if(mov.IdEstado.equals("ANU")) {
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CONTRATO_ANULADO_NO_EDITABLE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					return out;
				}
				if(mov.IdEstado.equals("RVC")){
                    out.P_OUT_ERRORESCONTADOR = 1L;
                    out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CONTRATO_REVOCADO_NO_EDITABLE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
                    return out;
                }
				if(mov.IdFolioEdit.equals("0")){
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ID_INTERNO_NO_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					return out;
				}
				if (correlativos.contains(mov.FolioCSC)) {
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					return out;
				} else {
					correlativos.add(mov.FolioCSC);
				}
			}else {
				params.put("FolioCSC", mov.FolioCSC);
                List<ContratoSuscripcionCuotasOptimus> contratosExistentes = sql.query(con, SQL_SELECT_CONTRATO_BY_ID, params, ContratoSuscripcionCuotasOptimus.class);
                params.clear();
                if(contratosExistentes.size() > 0) {
                        mov.IdEstado = contratosExistentes.get(0).Estado;
                }
                
                if(mov.IdEstado.equals("ANU")){
                        out.P_OUT_ERRORESCONTADOR = 1L;
                        out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CONTRATO_ANULADO_NO_EDITABLE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
                        return out;
                }
                
                if(mov.IdEstado.equals("RVC")){
                        out.P_OUT_ERRORESCONTADOR = 1L;
                        out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CONTRATO_REVOCADO_NO_EDITABLE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
                        return out;
                }
				if(mov.FolioCSC.equals("0")) {
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ID_INTERNO_NO_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					return out;
				}
				
				if (correlativos.contains(mov.FolioCSC)) {
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					return out;
				} else {
					correlativos.add(mov.FolioCSC);
				}
			}
			
			if (mov.p_ModificarCSCDetalle.size() == 0 && mov.Monto == null) {
				correlativos.remove(mov.FolioCSC);
				out.P_OUT_MENSAJES.add(new MensajeSalida(mov.IdFolioCSC,MensajeSalida.MENSAJE_ADVERTENCIA,"NO_VIENE_MONTO_NI_DETALLE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				continue;				
			}

			for (InModificarCSCDetalle det: mov.p_ModificarCSCDetalle) {
				params.put("IdSerieFondo", det.Serie);
				List<FondoIdSerie> detFondo = sql.query(con, SQL_SELECT_FONDO_BY_ID, params, FondoIdSerie.class);
				params.clear();
				if (detFondo.size() == 0) {
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ID_SERIE_FONDO_NO_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					return out;
				}
			}
		}
			
		
		
		
		params.put("FolioCSC", correlativos);
		List<ContratoSuscripcionCuotasOptimus> contratosExistentes = sql.query(con, SQL_SELECT_CONTRATO_BY_ID, params, ContratoSuscripcionCuotasOptimus.class);
		List<PortafolioProposito> portafolios = sql.query(con, SQL_SELECT_PORTAFOLIO, params, PortafolioProposito.class);
		HashMap<Long,Object> hPortafolio = Utils.List2Hash(portafolios, "FolioCSC");
		params.clear();
		if(contratosExistentes.isEmpty()){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_EXISTE_CONTRATO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		for(ContratoSuscripcionCuotasOptimus contrato: contratosExistentes){
			if(("ANU").equals(contrato.Estado)){
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CONTRATO_ANULADO_NO_EDITABLE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			if(("RVD").equals(contrato.Estado)){
                out.P_OUT_ERRORESCONTADOR = 1L;
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CONTRATO_REVOCADO_NO_EDITABLE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
                return out;
            }
            params.put("IdsCliente", contrato.IdCliente);
			params.put("IdsPortafolio", contrato.IdPortafolio);
			params.put("FolioCSC", correlativos);
		}
		List<MandatoPAC> mandatosExistentes = sql.query(con, SQL_SELECT_MANDATO_PAC, params, MandatoPAC.class);
		HashMap<String,Object> hMandatosExistentes = Utils.List2Hash(mandatosExistentes, "Compuesto");
		HashMap<String,Object> hContratosExistentes = Utils.List2Hash(contratosExistentes, "FolioCSC");
					
		int i =	0;
		long incremental = 0l;
		List<Long> secuenciaCSC = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::IdCSC",  new Long(entrada.p_ModificarCSC.size()));
		for (InModificarCSC modificacion : entrada.p_ModificarCSC) {
			
			PortafolioProposito portafolio = (PortafolioProposito)hPortafolio.get(modificacion.FolioCSC);
			if(portafolio == null) {
				out.P_OUT_MENSAJES.add(new MensajeSalida( Long.parseLong(i+""), MensajeSalida.MENSAJE_ERROR, "PORTAFOLIO_NO_EXISTE" ,new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				ResultadoModificarCSC resultado = new ResultadoModificarCSC();
				resultado.IdFolioCSC = 0L;
				out.P_OUT_RESULTADO.add(resultado);
				continue;
			}
			
			if(!_c.PROPOSITO_FFMM.equals(portafolio.Proposito)) {
				out.P_OUT_MENSAJES.add(new MensajeSalida( Long.parseLong(i+""), MensajeSalida.MENSAJE_ERROR, "PROPOSITO_PORTAFOLIO_NO_ES_CORRECTO" ,new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				ResultadoModificarCSC resultado = new ResultadoModificarCSC();
				resultado.IdFolioCSC = 0L;
				out.P_OUT_RESULTADO.add(resultado);
				continue;
			}
			
			if (!modificacion.Canal.equalsIgnoreCase("OPT")) {	
				
				if (modificacion.p_ModificarCSCDetalle.size() == 0 && modificacion.Monto == null){
					correlativos.remove(modificacion.FolioCSC);
					out.P_OUT_MENSAJES.add(new MensajeSalida( incremental, MensajeSalida.MENSAJE_ERROR, "NO_VIENE_MONTO_NI_DETALLE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					continue;					
				}
				
				ContratoSuscripcionCuotasOptimus contratoSuscripcionCuotas 	= 	(ContratoSuscripcionCuotasOptimus) hContratosExistentes.get(modificacion.FolioCSC);
				
				InContrato contratoAnulado = new InContrato();
				
				contratoAnulado.IdContrato = contratoSuscripcionCuotas.IdFolioCSC;
				entradaAnulacionContrato.p_Contrato.add(contratoAnulado);
				
				ObtenerFolioSuscripcion_Entrada in = new ObtenerFolioSuscripcion_Entrada();
				ObtenerFolioSuscripcion_Salida salidaFolio = new ObtenerFolioSuscripcion_Salida();
				in.p_Canal = "W";
				in.p_IdEmpresa = entrada.p_IdEmpresa;
				in.p_IdUsuario = entrada.p_IdUsuario;
				
				contratoSuscripcionCuotas.Canal = modificacion.Canal;
				
				salidaFolio = obtenerFolioSuscripcion.logica(con, in);
				
				contratoSuscripcionCuotas.FolioCSC		= 	salidaFolio.p_Out_FolioSuscripcion;
				contratoSuscripcionCuotas.IdFolioCSC 	= 	secuenciaCSC.get(i);
				contratoSuscripcionCuotas.Estado  		= 	"ING";
				contratoSuscripcionCuotas.FolioRelacionado = modificacion.FolioCSC;
				contratoSuscripcionCuotas.MLT			= modificacion.MLT;	
				//Fecha Actual
				Date fechaHrsActual = new Date();
		        SimpleDateFormat fecAct = new SimpleDateFormat("yyyy-MM-dd");
				contratoSuscripcionCuotas.Fecha = Fecha.toDate(fecAct.format(fechaHrsActual));
				//Hora Actual
		        SimpleDateFormat hrAct = new SimpleDateFormat("HH:mm:ss");
				contratoSuscripcionCuotas.Hora = hrAct.format(fechaHrsActual);				
				
				if (modificacion.Moneda.equals("CLP")) {
					contratoSuscripcionCuotas.MontoPesos = modificacion.Monto;
					contratoSuscripcionCuotas.MontoUF= new BigDecimal(0);
					contratoSuscripcionCuotas.MontoUSD= new BigDecimal(0);
					
				}else if (modificacion.Moneda.equals("UF")) {
					contratoSuscripcionCuotas.MontoUF = modificacion.Monto;
					contratoSuscripcionCuotas.MontoPesos= new BigDecimal(0);
					contratoSuscripcionCuotas.MontoUSD= new BigDecimal(0);
				}else if (modificacion.Moneda.equals("USD")) {
					contratoSuscripcionCuotas.MontoUSD = modificacion.Monto;
					contratoSuscripcionCuotas.MontoUF= new BigDecimal(0);
					contratoSuscripcionCuotas.MontoPesos= new BigDecimal(0);
				}
					
				listContratoSuscripcionCuotasOptimus.add(contratoSuscripcionCuotas);
				
				if (contratoSuscripcionCuotas.TipoPago.equalsIgnoreCase("PAC") && hMandatosExistentes.get(contratoSuscripcionCuotas.IdCliente + "_" + contratoSuscripcionCuotas.IdPortafolio) != null){
					MandatoPAC mandato = (MandatoPAC) hMandatosExistentes.get(contratoSuscripcionCuotas.IdCliente + "_" + contratoSuscripcionCuotas.IdPortafolio);
					INMandatoPAC ingresoPAC = new INMandatoPAC();
					AnulacionMandatoPAC.InContrato contrato = new AnulacionMandatoPAC.InContrato();
					ingresoPAC.IdCliente = mandato.IdCliente;
					ingresoPAC.IdPortafolio = mandato.IdPortafolio;
					ingresoPAC.FolioCSC = contratoSuscripcionCuotas.FolioCSC;
					ingresoPAC.FolioPAC = mandato.FolioPAC;
					ingresoPAC.CuentaCorriente = mandato.CuentaCorriente;
					ingresoPAC.FolioSAAPV = mandato.FolioSAAPV;
					ingresoPAC.Receptor = mandato.Receptor;
					ingresoPAC.Fecha = mandato.Fecha;
					ingresoPAC.NumeroMandato = mandato.NumeroMandato;
					ingresoPAC.Firma = mandato.Firma;
					ingresoPAC.IdentServicio = mandato.IdentServicio;
					ingresoPAC.Estado = mandato.Estado;
					ingresoPAC.EstadoCargo = mandato.EstadoCargo;
					ingresoPAC.IdEmpresa = mandato.IdEmpresa;
					ingresoPAC.Canal = mandato.Canal;
					ingresoPAC.EstadoRendicion = mandato.EstadoRendicion;
					ingresoPAC.EstadoCargoBanco = mandato.EstadoCargoBanco;
					ingresoPAC.IdCarga = mandato.IdCarga;
					ingresoPAC.IdCuentaBancaria = mandato.IdCuentaBancaria;
					ingresoPAC.EstadoPorAutorizar = mandato.EstadoPorAutorizar;
					ingresoPAC.EstadoContratoPorAutorizar = mandato.EstadoContratoPorAutorizar;
					ingresoPAC.DiaDescuentoPorAutorizar = mandato.DiaDescuentoPorAutorizar;
					contrato.IdContrato = mandato.IdMandatoPAC;
					contrato.Estado = "ANU";
					entradaAnularMandato.p_Contrato.add(contrato);
					listMandatoPAC.add(ingresoPAC);
				}
				
				for (InModificarCSCDetalle modificacionDetalle : modificacion.p_ModificarCSCDetalle){
					ContratoSuscripcionCuotasDetalleOptimus contratoSuscripcionCuotasDetalle = new ContratoSuscripcionCuotasDetalleOptimus();
					BigDecimal monto_final = modificacion.Monto.multiply(modificacionDetalle.Porcentaje);
					contratoSuscripcionCuotasDetalle.IdFolioCSC = contratoSuscripcionCuotas.IdFolioCSC;
					contratoSuscripcionCuotasDetalle.Serie = modificacionDetalle.Serie;
					contratoSuscripcionCuotasDetalle.Monto =   monto_final.divide(new BigDecimal(100));;
					contratoSuscripcionCuotasDetalle.Porcentaje = modificacionDetalle.Porcentaje;
					contratoSuscripcionCuotasDetalle.Regimen = modificacionDetalle.Regimen;
					
					ResultadoModificarCSC resultado = new ResultadoModificarCSC();
					resultado.IdFolioCSC = contratoSuscripcionCuotas.IdFolioCSC;
					resultado.FolioCSC = salidaFolio.p_Out_FolioSuscripcion;
					out.P_OUT_RESULTADO.add(resultado);
					listContratoSuscripcionCuotasDetalleOptimus.add(contratoSuscripcionCuotasDetalle);
				}
				i++;
			} else {
				ContratoSuscripcionCuotasOptimus contratoSuscripcionCuotas 	= 	(ContratoSuscripcionCuotasOptimus) hContratosExistentes.get(modificacion.IdFolioEdit);
				if(contratoSuscripcionCuotas != null) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FOLIO_YA_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					continue;
				}
				InContrato contratoAnulado = new InContrato();
				
				contratoAnulado.IdContrato = modificacion.IdFolioCSC;
				entradaAnulacionContrato.p_Contrato.add(contratoAnulado);
				
				ContratoSuscripcionCuotasOptimus contratoSuscripcionCuotasOptimus = new ContratoSuscripcionCuotasOptimus();
				contratoSuscripcionCuotasOptimus.IdFolioCSC = secuenciaCSC.get(i);
				
				contratoSuscripcionCuotasOptimus.FolioRelacionado = modificacion.FolioCSC;
				contratoSuscripcionCuotasOptimus.FolioCSC = modificacion.IdFolioEdit;
				contratoSuscripcionCuotasOptimus.IdCliente = modificacion.IdCliente;
				contratoSuscripcionCuotasOptimus.IdPortafolio = modificacion.IdPortafolio;
				contratoSuscripcionCuotasOptimus.Hora = modificacion.Hora;
				contratoSuscripcionCuotasOptimus.Fecha = modificacion.Fecha;
				contratoSuscripcionCuotasOptimus.MontoPesos = modificacion.MontoPesos;
				contratoSuscripcionCuotasOptimus.MontoUF =  modificacion.MontoUF;
				contratoSuscripcionCuotasOptimus.TipoPago =  modificacion.TipoPago;
				contratoSuscripcionCuotasOptimus.IdBanco = modificacion.IdBanco;
				contratoSuscripcionCuotasOptimus.NombreFuncionario = modificacion.NombreFuncionario;
				contratoSuscripcionCuotasOptimus.DomicilioSucursal = modificacion.DomicilioSucursal;
				contratoSuscripcionCuotasOptimus.CodigoSucursal = modificacion.CodigoSucursal;
				contratoSuscripcionCuotasOptimus.DocumentoIdentificacionFuncionario = modificacion.DocumentoIdentificacionFuncionario;
				contratoSuscripcionCuotasOptimus.FirmaFuncionario = modificacion.FirmaFuncionario;
				contratoSuscripcionCuotasOptimus.DiaDescuento = modificacion.DiaDescuento;
				contratoSuscripcionCuotasOptimus.AceptoCondiciones = modificacion.AceptoCondiciones;
				contratoSuscripcionCuotasOptimus.Canal = modificacion.Canal;
				contratoSuscripcionCuotasOptimus.Estado  = "ING";
				contratoSuscripcionCuotasOptimus.MLT	= modificacion.MLT;
				
				listContratoSuscripcionCuotasOptimus.add(contratoSuscripcionCuotasOptimus);
				
				for (InModificarCSCDetalle modificaciondetalle : modificacion.p_ModificarCSCDetalle){
					ContratoSuscripcionCuotasDetalleOptimus contratoSuscripcionCuotasDetalleOptimus = new ContratoSuscripcionCuotasDetalleOptimus();
					contratoSuscripcionCuotasDetalleOptimus.Serie = modificaciondetalle.Serie;
					contratoSuscripcionCuotasDetalleOptimus.Monto = modificaciondetalle.Monto;
					contratoSuscripcionCuotasDetalleOptimus.Porcentaje = modificaciondetalle.Porcentaje;
					contratoSuscripcionCuotasDetalleOptimus.Regimen = modificaciondetalle.Regimen;
					contratoSuscripcionCuotasDetalleOptimus.IdFolioCSC = contratoSuscripcionCuotasOptimus.IdFolioCSC;
					
					listContratoSuscripcionCuotasDetalleOptimus.add(contratoSuscripcionCuotasDetalleOptimus);
					ResultadoModificarCSC resultado = new ResultadoModificarCSC();
					resultado.IdFolioCSC = contratoSuscripcionCuotasOptimus.IdFolioCSC;
					resultado.FolioCSC = modificacion.IdFolioEdit;
					out.P_OUT_RESULTADO.add(resultado);
				}
				
				i++;
			}	
		}
		
		if(entradaAnulacionContrato.p_Contrato.size()>0) {
			anularContrato.logica(con, entradaAnulacionContrato);
		}
		
		
			
		if (listContratoSuscripcionCuotasOptimus.size() > 0) {
			sql.batch(con, SQL_INSERT_CONTRATO, listContratoSuscripcionCuotasOptimus, ContratoSuscripcionCuotasOptimus.class);
			
			long iterador = 0L;
			for (ContratoSuscripcionCuotasOptimus salida : listContratoSuscripcionCuotasOptimus){
				out.P_OUT_MENSAJES.add(new MensajeSalida(iterador,MensajeSalida.MENSAJE_OK,"Contrato Creado.",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				iterador ++;
			}
		}
		
		if (listContratoSuscripcionCuotasDetalleOptimus.size() > 0) {
			sql.batch(con, SQL_INSERT_CONTRATO_DETALLE, listContratoSuscripcionCuotasDetalleOptimus, ContratoSuscripcionCuotasDetalleOptimus.class);
		}
		
		for (InModificarCSC ingresos : entrada.p_ModificarCSC){
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"Contrato Anulado.",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		if (listMandatoPAC.size() > 0){
			sql.batch(con, SQL_INSERT_MANDATO_PAC, listMandatoPAC, INMandatoPAC.class);
		}
		
		if (entradaAnularMandato.p_Contrato.size() > 0){
			entradaAnularMandato.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaAnularMandato.p_IdUsuario = entrada.p_IdUsuario;
			entradaAnularMandato.p_IdModulo = entrada.p_IdModulo;
			entradaAnularMandato.p_DesdeCSC = "S";
			anularMandato.logica(con, entradaAnularMandato);
		}
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);

		return out;

	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa01", "connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
			
//			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_ModificarCSC\":[{\"IdFolioCSC\":2256,\"FolioCSC\":\"1569\",\"IdCliente\":249194,\"IdPortafolio\":1450263,\"Hora\":\"12:00:00\",\"Fecha\":\"2018-10-19T03:00:00.000Z\",\"MontoPesos\":0,\"MontoUF\":300,\"TipoPago\":\"CHE\",\"IdBanco\":0,\"DomicilioSucursal\":\"313\",\"CodigoSucursal\":\"31231\",\"NombreFuncionario\":\"EQWE\",\"DocumentoIdentificacionFuncionario\":\"22222222-2\",\"FirmaFuncionario\":true,\"DiaDescuento\":0,\"AceptoCondiciones\":true,\"Canal\":\"OPT\",\"p_ModificarCSCDetalle\":[{\"Serie\":null,\"Monto\":300,\"Porcentaje\":100,\"Regimen\":\"18L\"}]}]}";	
			//String json2 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"FMM\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_ModificarCSC\":[{\"IdFolioCSC\":2630,\"FolioCSC\":\"101112\",\"IdCliente\":1423967,\"IdFolioEdit\":100100,\"FolioRelacionado\":\"\",\"IdPortafolio\":1451456,\"Hora\":\"12:00:00\",\"Fecha\":\"2018-10-22T03:00:00.000Z\",\"MontoPesos\":25000,\"MontoUF\":0,\"TipoPago\":\"PAC\",\"IdBanco\":8,\"DomicilioSucursal\":\"SUC12\",\"CodigoSucursal\":\"1212\",\"NombreFuncionario\":\"FUN\",\"DocumentoIdentificacionFuncionario\":\"11111111-1\",\"FirmaFuncionario\":true,\"DiaDescuento\":5,\"AceptoCondiciones\":true,\"Canal\":\"OPT\",\"IdEstado\":\"ING\",\"p_ModificarCSCDetalle\":[{\"Serie\":\"FM_APC1_A\",\"Monto\":25000,\"Porcentaje\":100,\"Regimen\":\"54BIS\"}]}]}";
			//String json3 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"FMM\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_ModificarCSC\":[{\"IdFolioCSC\":2826,\"FolioCSC\":\"100002\",\"IdCliente\":1423967,\"IdFolioEdit\":100002,\"FolioRelacionado\":null,\"IdPortafolio\":1451456,\"Hora\":\"12:00:00\",\"Fecha\":\"2018-10-23T03:00:00.000Z\",\"MontoPesos\":105,\"MontoUF\":0,\"TipoPago\":\"PAC\",\"IdBanco\":31,\"DomicilioSucursal\":\"DADSADA\",\"CodigoSucursal\":\"ADA\",\"NombreFuncionario\":\"DASD\",\"DocumentoIdentificacionFuncionario\":\"11111111-1\",\"FirmaFuncionario\":true,\"DiaDescuento\":5,\"AceptoCondiciones\":true,\"Canal\":\"OPT\",\"IdEstado\":\"ING\",\"p_ModificarCSCDetalle\":[{\"Serie\":\"FM_57BIS_A\",\"Monto\":100,\"Porcentaje\":100,\"Regimen\":\"57BIS\"}]}]}";
			//String jsonWS = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_ModificarCSC\":[{\"FolioCSC\":\"100100\",\"Monto\":30000,\"Moneda\":\"CLP\",\"p_ModificarCSCDetalle\":[{\"Serie\":\"FM_APC1_A\",\"Porcentaje\":100,\"Regimen\":\"54BIS\"}]}]}";
//			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_ModificarCSC\":[{\"FolioCSC\":\"W7\",\"Monto\":200,\"Moneda\":\"CLP\",\"p_ModificarCSCDetalle\":[{\"Serie\":\"FM_54BIS_A\",\"Porcentaje\":100,\"Regimen\":\"54BIS\"}]}]}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_ModificarCSC\":[{\"FolioCSC\":\"W7\",\"Monto\":200,\"Moneda\":\"CLP\",\"p_ModificarCSCDetalle\":[{\"Serie\":\"FM_54BIS_A\",\"Porcentaje\":100,\"Regimen\":\"54BIS\"}]}]}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_ModificarCSC\":[{\"IdFolioCSC\":3004,\"FolioCSC\":\"24789\",\"IdCliente\":524891,\"IdFolioEdit\":40506089,\"FolioRelacionado\":\"24789\",\"IdPortafolio\":1450068,\"Hora\":\"12:00:00\",\"Fecha\":\"2019-05-29T04:00:00.000Z\",\"MontoPesos\":20000,\"MontoUF\":0,\"TipoPago\":\"CHE\",\"IdBanco\":0,\"DomicilioSucursal\":\"ADSADASDA\",\"CodigoSucursal\":\"ASDASDASD\",\"NombreFuncionario\":\"DSASDA\",\"DocumentoIdentificacionFuncionario\":\"1-9\",\"FirmaFuncionario\":true,\"DiaDescuento\":0,\"AceptoCondiciones\":true,\"Canal\":\"OPT\",\"IdEstado\":\"ING\",\"p_ModificarCSCDetalle\":[{\"Serie\":\"CFICCLAT-B\",\"Monto\":20000,\"Porcentaje\":100,\"Regimen\":\"NINGUNO\"}]}]}";
			EntradaModificarCSC  Objin = gson.fromJson(json, EntradaModificarCSC.class);
			ModificarCSC  demo = new ModificarCSC();
			
			try {
				SalidaModificarCSC ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println(ObjOut);
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}			
		}
	}
}

