package cl.bcs.tesoreria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.pojo.ConstantesTesoreria;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class IngresarMandatoPAC implements JavaCall{

	private final Log log = new Log("TES.InsertarPAC");
	private final static Logger logger = LogManager.getLogger(IngresarMandatoPAC.class);
	private final ConstantesTesoreria _c = new ConstantesTesoreria();

	private final String SQL_INSERT_MANDATO = loadResourceToString("cl/bcs/tesoreria/sql/IngresoPAC_InsertMandato.sql");
	
	private final String SQL_SELECT_FOLIO_CSC = loadResourceToString("cl/bcs/tesoreria/sql/IngresarMandatoPAC_SelectFolioPACCSC.sql");
	private final String SQL_SELECT_FOLIO_SAAPV = loadResourceToString("cl/bcs/tesoreria/sql/IngresarMandatoPAC_SelectFolioPACSAAPV.sql");
	private final String SQL_SELECT_CTA_CORRIENTE = loadResourceToString("cl/bcs/tesoreria/sql/IngresarMandatoPAC_SelectCtaCorriente.sql");
	private final String SQL_SELECT_DOC_IDENTIDAD = loadResourceToString("cl/bcs/tesoreria/sql/IngresarMandatoPAC_SelectDocIdentidad.sql");
	
	private final String SQL_CREATE_TABLE = loadResourceToString("cl/bcs/tesoreria/sql/ModificarPersonaCuentaBancaria_CreateTable.sql");
	private final String SQL_DROP_TABLE = loadResourceToString("cl/bcs/tesoreria/sql/ModificarPersonaCuentaBancaria_DropTable.sql");
	private final String SQL_INSERT_TABLE = loadResourceToString("cl/bcs/tesoreria/sql/ModificarPersonaCuentaBancaria_InsertTable.sql");
	private final String SQL_CALL_SP = loadResourceToString("cl/bcs/tesoreria/sql/ModificarPersonaCuentaBancaria_CallSp.sql");
	
	private final String SQL_SELECT_CONTRATO_BY_ID = loadResourceToString("cl/bcs/fondomutuo/sql/CrearIngresoContratoCSC_SelectContratoById.sql");
	//private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_UPDATE_BANCO = loadResourceToString("cl/bcs/tesoreria/sql/IngresoPAC_ActualizarBanco.sql");
	
	private final String SQL_SELECT_DIA_DESCUENTO_CONTRATOS = loadResourceToString("cl/bcs/tesoreria/sql/IngresarMandatoPAC_SelectDiaDescuentoContratos.sql");
	private final String SQL_SELECT_MANDATOS_PAC = loadResourceToString("cl/bcs/tesoreria/sql/IngresarMandatoPAC_SelectMandatosPAC.sql");
	
	public static class EntradaIngresoMandatoPAC{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public List<InIngresoPAC> p_IngresoPAC;
		
		public EntradaIngresoMandatoPAC() {
			this.p_IngresoPAC = new ArrayList<InIngresoPAC>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InIngresoPAC{
		public Long IdCliente = 0L;
		public Long IdPortafolio;
		public String FolioCSC;
		public String CuentaCorriente;
		public String FolioSAAPV;
		public Long FolioPACMandatoAgrupador;
		public String Receptor;
		public Date Fecha;
		public Long NumeroMandato;
		public String Firma;
		public String IdentServicio;
		public String Estado;
		public String EstadoCargo;
		public Long IdEmpresa;
		public Long IdCuentaBancaria;
		public String Canal;
		public String TipoPago;
		public String DocumentoIdentidad;
		public Long DiaDescuento;
		public String EstadoContrato;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ErroresDetalle{
		public Long IdInterno;
		public String Codigo;
		public String Descripcion;
	}
	
	public static class AdvertenciasDetalle{
		public Long IdInterno;
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}
	
	public static class SalidaCrearIngresoPAC{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoIngresoPAC> P_OUT_RESULTADO;

		public SalidaCrearIngresoPAC(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoIngresoPAC>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoIngresoPAC{
		public Long IdMandatoPAC;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MandatoPAC{
		public Long IdMandatoPAC;
		public Long IdCliente;
		public Long IdPortafolio;
		public String FolioCSC;
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
		public Long IdCuentaBancaria;
		public String EstadoPorAutorizar;
		public Long DiaDescuentoPorAutorizar;
		public String EstadoContratoPorAutorizar;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoNumeroCuentaBancaria{
		
		public String NumeroCuentaBancaria;
		public String CodigoBanco;
		public Long IdBanco;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoDocumentoIdentidad{
		
		public String DocumentoIdentidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	public class Modulo11 {
		public String invertirCadena(String cadena) {
	        String cadenaInvertida = "";
	        for (int x = cadena.length() - 1; x >= 0; x--) {
	            cadenaInvertida = cadenaInvertida + cadena.charAt(x);
	        }
	        return cadenaInvertida;
	    }
	 
	    public int obtenerSumaPorDigitos(String cadena) {
	        int pivote = 2;
	        int longitudCadena = cadena.length();
	        int cantidadTotal = 0;
	        int b = 1;
	        for (int i = 0; i < longitudCadena; i++) {
	            if (pivote == 8) {
	                pivote = 2;
	            }
	            int temporal = Integer.parseInt("" + cadena.substring(i, b));
	            b++;
	            temporal *= pivote;
	            pivote++;
	            cantidadTotal += temporal;
	        }
	        cantidadTotal = 11 - cantidadTotal % 11;
	        return cantidadTotal;
	    }
	}
	
	public class CalculoModulo11 {
		public String  Calculo (Long cuentaCorriente, String codigoBanco) {
			Modulo11 a = new Modulo11();
			String temporal_agrupador = null;
			int modulo_11 = 0;
			int diferencia = 0;
			String ceros = "0";
			String ident_servicio = null;
			String final_ident_servicio = null;
			
			temporal_agrupador = cuentaCorriente+codigoBanco;
			
			if (temporal_agrupador.length() < 17) {
				modulo_11 = a.obtenerSumaPorDigitos(a.invertirCadena(temporal_agrupador));
				if (modulo_11 == 10) {
					modulo_11 = 1;
				}
				if (modulo_11 == 11) {
					modulo_11 = 0;
				}
				temporal_agrupador += modulo_11;
				diferencia = 17 - temporal_agrupador.length();
				
				for(int i = 0; i < diferencia-1; i++) {
					ceros += "0";
					
				}
				ident_servicio = ceros+temporal_agrupador;
				final_ident_servicio = ident_servicio;
			} else {
				final_ident_servicio = temporal_agrupador;
			}
			return final_ident_servicio;
	    }
	}
	
	public static class ActualizarBanco{
		public Long IdBanco;
		public String FolioCSC;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DiaDescuentoContrato extends BcsPojo{
		public String FolioContrato;
		public Long DiaDescuento;
		public String TipoContrato;
		public String Hash;
	}
	
	private void updateBanco(ResultadoNumeroCuentaBancaria cuentaBancaria, MandatoPAC mandatoPAC, Connection conn) throws SQLException {
		if(mandatoPAC.FolioCSC.equals("0") == false) {
			SQL sql = new SQL();		
			List<ActualizarBanco> actualizarBancos = new ArrayList<ActualizarBanco>();
			ActualizarBanco actualizarBanco = new ActualizarBanco();			
			actualizarBanco.IdBanco = cuentaBancaria.IdBanco;
			actualizarBanco.FolioCSC = mandatoPAC.FolioCSC;
			actualizarBancos.add(actualizarBanco);
			sql.batch(conn, SQL_UPDATE_BANCO, actualizarBancos, ActualizarBanco.class);
		}
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaIngresoMandatoPAC jin = gson.fromJson(in.toString(), EntradaIngresoMandatoPAC.class);
		SalidaCrearIngresoPAC out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	//map
	@SuppressWarnings("rawtypes")
	public SalidaCrearIngresoPAC logica (Connection con, EntradaIngresoMandatoPAC entrada) throws Exception{
		//TODO DEBO OPTIMIZAR...
		log.write("INICIO CrearIngresoPAC");
		SQL sql=new SQL();
		SalidaCrearIngresoPAC out = new SalidaCrearIngresoPAC();
		
		try {
			/***** Validaciones de entrada *****/
			if(entrada.p_IngresoPAC.size() == 0){
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES_DE_TRASPASO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				return out;
			}
			
			/***** Variables *****/
			String fechaSistema 					= 	Utils.getFechaSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
			
			ArrayList<String> correlativos_CSC 		= 	new ArrayList<String>();
			HashMap<String,String> codigos_banco = new HashMap<String,String>();
			ArrayList<String> correlativos_SAAPV 		= 	new ArrayList<String>();
			List<InIngresoPAC> solicitudInvalida 	= 	new ArrayList<InIngresoPAC>();
			List<InIngresoPAC> solicitudProcesar 	= 	new ArrayList<InIngresoPAC>();
			List<String> correlativos_IdentServicio = 	new ArrayList<String>();
			
			ResultadoNumeroCuentaBancaria cuentaBancaria = new ResultadoNumeroCuentaBancaria();
			
			for (InIngresoPAC mov: entrada.p_IngresoPAC) {
				Boolean Valido = true; 
				
				//reemplazar los valores que no tengan numeros (IdentServicio es el Folio Mandato Agrupador)
				mov.IdentServicio = (mov.IdentServicio != null && !mov.IdentServicio.isEmpty()) ? mov.IdentServicio.replaceAll("[^0-9]+","") : "0";
				
				correlativos_CSC.add(mov.FolioCSC);
				correlativos_SAAPV.add(mov.FolioSAAPV);
				correlativos_IdentServicio.add(mov.IdentServicio);
				Long cuenta=mov.IdCuentaBancaria;
				String doc_ident=mov.DocumentoIdentidad;
				String identServicio= mov.IdentServicio;

				HashMap<String,Object> params_CTA_CTE 							= 	new HashMap<String,Object>();
				params_CTA_CTE.put("IdCuentaBancaria", cuenta);
				List<ResultadoNumeroCuentaBancaria> mandatosExistentesCTACTE 	= 	sql.query(con, SQL_SELECT_CTA_CORRIENTE, params_CTA_CTE, ResultadoNumeroCuentaBancaria.class);
				
				// si IdCliente es == 0, viene del WS
				if (mov.IdCliente == 0L ) {  	
					HashMap<String,Object> params_DocumentoIdentidad = new HashMap<String,Object>();
					params_DocumentoIdentidad.put("DocumentoIdentidad", doc_ident);
					List<ResultadoDocumentoIdentidad> mandatosExistentesDocumentoIdentidad = sql.query(con, SQL_SELECT_DOC_IDENTIDAD, params_DocumentoIdentidad, ResultadoDocumentoIdentidad.class);
					
					if (mandatosExistentesDocumentoIdentidad.size() == 0 ) {
						//out.P_OUT_MENSAJES++;
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"DOCUMENTO_IDENTIDAD_NO_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
						Valido = false;
					}
				}
				if (mandatosExistentesCTACTE.size() == 0) {
					//out.P_OUT_MENSAJES++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"CUENTA_CORRIENTE_NO_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					Valido = false;
				} else {
					if (!codigos_banco.containsKey(mandatosExistentesCTACTE.get(0).NumeroCuentaBancaria)) codigos_banco.put(mandatosExistentesCTACTE.get(0).NumeroCuentaBancaria, mandatosExistentesCTACTE.get(0).CodigoBanco);
					cuentaBancaria = mandatosExistentesCTACTE.get(0);
				} 
				
				if(!Valido){	
					solicitudInvalida.add(mov);
				}else {
					solicitudProcesar.add(mov);//encontrado
				};
			}
			
			HashMap<String,Object> params_CSC = new HashMap<String,Object>();
			params_CSC.put("FolioCSC", correlativos_CSC);
			List<MandatoPAC> mandatosExistentesCSC = sql.query(con, SQL_SELECT_FOLIO_CSC, params_CSC, MandatoPAC.class);
			HashMap<Long,Object> hMandatosExistentesCSC = Utils.List2Hash(mandatosExistentesCSC, "FolioCSC");	
			
			HashMap<String,Object> params_SAAPV = new HashMap<String,Object>();
			params_SAAPV.put("FolioSAAPV", correlativos_SAAPV);
			List<MandatoPAC> mandatosExistentesSAAPV = sql.query(con, SQL_SELECT_FOLIO_SAAPV, params_SAAPV, MandatoPAC.class);
			HashMap<Long,Object> hMandatosExistentesSAAPV = Utils.List2Hash(mandatosExistentesSAAPV, "FolioSAAPV");
			
			List<MandatoPAC> mandatosExistentesConMismoIdentServicio = obtenerMandatosPACConMismoIdentServicio(con, sql, correlativos_IdentServicio);
			
			List<DiaDescuentoContrato> diaDescuentoContratos = obtenerDiaDescuentoContrato(con, sql, correlativos_SAAPV, correlativos_CSC);
			HashMap<Long,Object> hDiaDescuentoContratos = Utils.List2Hash(diaDescuentoContratos, "Hash");
			/***** Insert Mandato PAC *****/
			List<Long> secuenciaPAC = Utils.getSecuencia(con, "BSCL.Tesoreria.BD.Secuencias::IdMandatoPAC", new Long(entrada.p_IngresoPAC.size()));
			int i=0;
			
			List<MandatoPAC> listMandatoPAC = new ArrayList<MandatoPAC>();
			
			List<MandatoPAC> mandatosPorAnular = new ArrayList<>();
			
			for (InIngresoPAC ingresos : solicitudProcesar){
				MandatoPAC existenteCSC 	= 	(MandatoPAC) hMandatosExistentesCSC.get((ingresos.FolioCSC).toString());
				MandatoPAC existenteSAAPV 	= 	(MandatoPAC) hMandatosExistentesSAAPV.get(ingresos.FolioSAAPV);
	
				if(existenteCSC != null) {
					//out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"FOLIO_CSC_YA_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					continue;
				}
				if(existenteSAAPV != null) {
					//out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"FOLIO_SAAPV_YA_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					continue;
				}
				
				if(ingresos.DiaDescuento == null) {
					if(ingresos.FolioCSC.equalsIgnoreCase("0") && !ingresos.FolioSAAPV.equalsIgnoreCase("0")) {
						DiaDescuentoContrato existeDiaDescuento = (DiaDescuentoContrato) hDiaDescuentoContratos.get(ingresos.FolioSAAPV + "_" + "SAAPV");
						ingresos.DiaDescuento = existeDiaDescuento != null ? existeDiaDescuento.DiaDescuento:0L;
					}
					if(!ingresos.FolioCSC.equalsIgnoreCase("0") && ingresos.FolioSAAPV.equalsIgnoreCase("0")) {
						DiaDescuentoContrato existeDiaDescuento = (DiaDescuentoContrato) hDiaDescuentoContratos.get(ingresos.FolioCSC + "_" + "CSC");
						ingresos.DiaDescuento = existeDiaDescuento != null ? existeDiaDescuento.DiaDescuento:0L;
					}
				}
				
				if(!ingresos.FolioSAAPV.equalsIgnoreCase("0")) {
					mandatosPorAnular.addAll(mandatosExistentesConMismoIdentServicio.stream()
							.filter(Mandato-> Mandato.IdentServicio.equalsIgnoreCase(ingresos.IdentServicio) && Mandato.DiaDescuentoPorAutorizar.compareTo(ingresos.DiaDescuento) == 0 && Mandato.IdCliente.compareTo(ingresos.IdCliente) == 0 && (!Mandato.FolioSAAPV.equalsIgnoreCase("0")))
							.collect(Collectors.toList()));
				}
				if(!ingresos.FolioCSC.equalsIgnoreCase("0")) {
					mandatosPorAnular.addAll(mandatosExistentesConMismoIdentServicio.stream()
							.filter(Mandato-> Mandato.IdentServicio.equalsIgnoreCase(ingresos.IdentServicio) && Mandato.DiaDescuentoPorAutorizar.compareTo(ingresos.DiaDescuento) == 0 && Mandato.IdCliente.compareTo(ingresos.IdCliente) == 0 && (!Mandato.FolioCSC.equalsIgnoreCase("0")))
							.collect(Collectors.toList()));
				}
				
				MandatoPAC mandatoPAC 		 = new MandatoPAC();
				mandatoPAC.IdMandatoPAC 	 = secuenciaPAC.get(i);
				mandatoPAC.IdCliente 		 = ingresos.IdCliente;
				mandatoPAC.IdPortafolio 	 = ingresos.IdPortafolio;
				mandatoPAC.FolioCSC 		 = ingresos.FolioCSC;
				mandatoPAC.CuentaCorriente 	 = ingresos.CuentaCorriente;
				mandatoPAC.FolioSAAPV 		 = ingresos.FolioSAAPV;
				mandatoPAC.Receptor 		 = ingresos.Receptor;
				mandatoPAC.Fecha 			 = ingresos.Fecha;
				mandatoPAC.NumeroMandato 	 = ingresos.NumeroMandato;
				mandatoPAC.Firma 			 = ingresos.Firma;
				mandatoPAC.IdentServicio 	 = ingresos.IdentServicio;
				mandatoPAC.IdEmpresa 		 = ingresos.IdEmpresa;
				mandatoPAC.Canal 			 = ingresos.Canal;
				mandatoPAC.IdCuentaBancaria  = ingresos.IdCuentaBancaria;
				mandatoPAC.EstadoPorAutorizar = "ING";
				mandatoPAC.DiaDescuentoPorAutorizar	= ingresos.DiaDescuento;
				mandatoPAC.EstadoContratoPorAutorizar = ingresos.EstadoContrato;
				mandatoPAC.Estado = "PAU";
				
				//MODIFICAR CTA CORRIENTE
				if (ingresos.TipoPago.equals("N")){
					try{
						SQL.exec(con, SQL_CREATE_TABLE);
						SQL.exec(con, SQL_INSERT_TABLE);
						
						HashMap<String,Object> parmsIn =new HashMap<String,Object>();
						parmsIn.put("p_IdEmpresa", entrada.p_IdEmpresa);
						parmsIn.put("p_IdUsuario", entrada.p_IdUsuario);
						parmsIn.put("p_IdModulo", entrada.p_IdModulo);
						parmsIn.put("p_UsaReglasNegocio", entrada.p_UsaReglasNegocio);
						parmsIn.put("p_IdCuentaBancaria", ingresos.IdCuentaBancaria);
						
						HashMap<String,Class> parmsOut = new HashMap<>();
						parmsOut.put("p_out_RefId", Long.class);
						parmsOut.put("p_out_AdvertenciasContador", Long.class);
						parmsOut.put("p_out_AdvertenciasDetalle", AdvertenciasDetalle.class);
						parmsOut.put("p_out_ErroresContador", Long.class);
						parmsOut.put("p_out_ErroresDetalle", ErroresDetalle.class);
						
						sql.call(con, SQL_CALL_SP, parmsIn, parmsOut);
						
					}catch(Exception e){
						log.write("ERROR",e);
						throw e;
					}finally{
						SQL.exec(con, SQL_DROP_TABLE);
					}	
				}
				
				//Autorizacion
				CrearEventoAutorizacionMasivo evtAutorizacion = new CrearEventoAutorizacionMasivo();
				
				CrearEventoAutorizacionMasivo.ServiceEntrada entradaEvtAutorizacion = new CrearEventoAutorizacionMasivo.ServiceEntrada();
				entradaEvtAutorizacion.P_IDEMPRESA = entrada.p_IdEmpresa;
				entradaEvtAutorizacion.P_IDMODULO = entrada.p_IdModulo;
				entradaEvtAutorizacion.P_IDUSUARIO = entrada.p_IdUsuario;
				entradaEvtAutorizacion.P_SIMULAROPERACION = "N";
				entradaEvtAutorizacion.P_USAREGLASNEGOCIO = "N";
				
				CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
				
				evento.IdTipoEvento = "NUEPAC";
				evento.IdModuloSolicita = entrada.p_IdModulo;
				evento.IdUsuarioSolicita = entrada.p_IdUsuario;
				evento.Fecha = fechaSistema;
				evento.IdConcepto = "NUEPAC";
				
				evento.FechaOperacionConcepto = fechaSistema;
				evento.IdCliente = (long) 1;
			
				evento.Glosa = "Ingreso Contrato PAC"+" "+"IdMandato:"+mandatoPAC.IdMandatoPAC;
				evento.NumeroOperacionConcepto = Long.toString(mandatoPAC.IdMandatoPAC);
							
				entradaEvtAutorizacion.P_TABLAEVENTOS = Arrays.asList(evento);
				
				CrearEventoAutorizacionMasivo.ServiceSalida salidaAut = evtAutorizacion.logica(con, entradaEvtAutorizacion);
				
				for(CrearEventoAutorizacionMasivo.ObjADVERTENCIASDETALLE advertencia : salidaAut.P_OUT_ADVERTENCIASDETALLE){
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,advertencia.Descripcion,new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				
				}
				for (CrearEventoAutorizacionMasivo.ObjERRORESDETALLE error : salidaAut.P_OUT_ERRORESDETALLE) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,error.Descripcion,new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				}
								
				listMandatoPAC.add(mandatoPAC);
				i++;
				
				ResultadoIngresoPAC resultadoIngresoPAC = new ResultadoIngresoPAC();
				resultadoIngresoPAC.IdMandatoPAC = mandatoPAC.IdMandatoPAC;
				out.P_OUT_RESULTADO.add(resultadoIngresoPAC);
			}
			
			if(mandatosPorAnular.size() > 0) {
				anularMandatosPAC(con, entrada, mandatosPorAnular);
			}
				
			if(listMandatoPAC.size() > 0) {
				sql.batch(con, SQL_INSERT_MANDATO, listMandatoPAC, MandatoPAC.class);
				for (MandatoPAC salida : listMandatoPAC){
					this.updateBanco(cuentaBancaria, salida, con);
					MensajeSalida msg = new MensajeSalida();
					msg.id = salida.IdMandatoPAC;
					msg.tipo = MensajeSalida.MENSAJE_AUTORIZACION;
					msg.codigo = "CREACION_MANDATO_PAC_OK, FOLIO: " + salida.IdMandatoPAC;
					msg.descripcion = "Creacion de Mandato PAC exitosa, FOLIO: " + salida.IdMandatoPAC;
					out.P_OUT_MENSAJES.add(msg);
				}
			}
			
			/******** AGREGAR LOG ************************/
			agregarLogSistema(con, entrada, fechaSistema);
			/*********************************************/
			
			
			return out;
			
		}catch (Exception error) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write(error);
			throw error;
		}			
	}
	
	private void agregarLogSistema(Connection con, EntradaIngresoMandatoPAC entrada, String fechaSistema) throws Exception {
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
		log.IdCabeceraLog = 0L;
		log.IdTipoLog = "INGRESO MANDATO PAC";
		log.IdModulo = _c.ModuloTesoreria; 
		log.IdUsuario = entrada.p_IdUsuario;
		log.IdEmpresa = entrada.p_IdEmpresa;
		log.FechaSistemaLog = fechaSistema;
		log.IdUsuarioAtendido = entrada.p_IdUsuario;
		log.IdCliente = 0L;
		log.EstadoCliente = "";
		log.NombreCliente = "";
		log.IdTipoDocumentoIdentidad = "";
		log.IdDocumentoIdentidad = "";
		log.IdPortafolio = 0L;
		log.EstadoPortafolio = "";
		log.IdTipoRegistro = "INGRESO_MANDATO_PAC";
		log.EstadoRegistro = _c.EstadoIngresado;
		log.IdTipoOperacion = "";
		log.Folio = 0L;
		log.Cantidad = BigDecimal.ZERO;
		log.PrecioTasa = BigDecimal.ZERO;
		log.Monto = BigDecimal.ZERO;
		log.IdMonedaOrigen = "";
		log.IdMonedaLiquidacion = "";
		log.TipoCambio = "";
		log.Nemo = "";
		log.IdProceso = 0L;
		log.EstadoProceso = "";
		log.NombreProceso = "";						
		entradaAgregarLog.p_Log.add(log);
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
	}
	
	private List<MandatoPAC> obtenerMandatosPACConMismoIdentServicio(Connection con, SQL sql,List<String> correlativos_IdentServicio) throws SQLException{
		logger.debug("INICIO::IngresarMandatoPAC.obtenerMandatosPACConMismoIdentServicio");
		long inicio = System.currentTimeMillis();
		
		List<MandatoPAC> mandatosExistentesConMismoIdentServicio = new ArrayList<>();
		
		HashMap<String,Object> params_IdentServicio = new HashMap<String,Object>();
		params_IdentServicio.put("IdentServicio", correlativos_IdentServicio);
		mandatosExistentesConMismoIdentServicio = sql.query(con, SQL_SELECT_MANDATOS_PAC, params_IdentServicio, MandatoPAC.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::IngresarMandatoPAC.obtenerMandatosPACConMismoIdentServicio: tiempo " + (fin - inicio));
		
		return mandatosExistentesConMismoIdentServicio;
	}
	
	private List<DiaDescuentoContrato> obtenerDiaDescuentoContrato(Connection con, SQL sql,List<String> correlativos_SAAPV, List<String> correlativos_CSC) throws SQLException{
		logger.debug("INICIO::IngresarMandatoPAC.obtenerDiaDescuentoContrato");
		long inicio = System.currentTimeMillis();
		
		List<DiaDescuentoContrato> diaDescuentoContratos = new ArrayList<>();
		
		HashMap<String,Object> params_DiaDescuentoContrato = new HashMap<String,Object>();
		params_DiaDescuentoContrato.put("FolioContratoSAAPV", correlativos_SAAPV);
		params_DiaDescuentoContrato.put("FolioCSC", correlativos_CSC);
		diaDescuentoContratos = sql.query(con, SQL_SELECT_DIA_DESCUENTO_CONTRATOS, params_DiaDescuentoContrato, DiaDescuentoContrato.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::IngresarMandatoPAC.obtenerDiaDescuentoContrato: tiempo " + (fin - inicio));
		
		return diaDescuentoContratos;
	}
	
	private void anularMandatosPAC(Connection con,EntradaIngresoMandatoPAC entrada,List<MandatoPAC> mandatosPorAnular) throws Exception {
		logger.trace("INICIO::IngresarMandatoPAC.anularMandatosPAC");
		long inicio = System.currentTimeMillis();
		
		AnulacionMandatoPAC anularMandato = new AnulacionMandatoPAC();
		AnulacionMandatoPAC.EntradaAnulacionContrato entradaAnularMandato = new AnulacionMandatoPAC.EntradaAnulacionContrato();
		
		entradaAnularMandato.p_Contrato.addAll(mandatosPorAnular.stream()
				.map(temp -> {AnulacionMandatoPAC.InContrato contrato = new AnulacionMandatoPAC.InContrato();
				contrato.IdContrato = temp.IdMandatoPAC;
				contrato.Estado = "ANU";
				return contrato;})
				.collect(Collectors.toList())
				);
						
		entradaAnularMandato.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaAnularMandato.p_IdUsuario = entrada.p_IdUsuario;
		entradaAnularMandato.p_IdModulo = entrada.p_IdModulo;
		anularMandato.logica(con, entradaAnularMandato);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::IngresarMandatoPAC.anularMandatosPAC: tiempo " + (fin - inicio));
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			String json15 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"TES\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_IngresoPAC\":[{\"IdCliente\":524891,\"IdPortafolio\":1450068,\"FolioCSC\":131325,\"CuentaCorriente\":\"11111111111111\",\"FolioSAAPV\":0,\"FolioPACMandatoAgrupador\":1,\"Receptor\":\"\",\"Fecha\":\"1900-01-01\",\"NumeroMandato\":0,\"Firma\":\"Si\",\"IdentServicio\":\"0000000111111111111117\",\"IdEmpresa\":1,\"IdCuentaBancaria\":260,\"Canal\":\"OPTIMUS\",\"TipoPago\":\"S\"}]}";
			  
			EntradaIngresoMandatoPAC  Objin = gson.fromJson(json15, EntradaIngresoMandatoPAC.class);
			IngresarMandatoPAC demo = new IngresarMandatoPAC();
			try {
				SalidaCrearIngresoPAC ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println(ObjOut);
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}			
		}
		System.out.println("== END ==");
	}
}
