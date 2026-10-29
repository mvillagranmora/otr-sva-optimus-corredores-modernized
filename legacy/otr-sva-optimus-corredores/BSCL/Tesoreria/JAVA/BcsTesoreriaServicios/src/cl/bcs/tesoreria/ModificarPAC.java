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
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarPAC implements JavaCall{

	private final Log log = new Log("TES.ModificarPAC");
	private final static Logger logger = LogManager.getLogger(ModificarPAC.class);
	private final ConstantesTesoreria _c = new ConstantesTesoreria();

	private final String SQL_SELECT_FOLIO_CSC = loadResourceToString("cl/bcs/tesoreria/sql/ModificarPAC_SelectFolioPACCSC.sql");
	private final String SQL_UPDATE_MANDATO = loadResourceToString("cl/bcs/tesoreria/sql/IngresoPAC_Update.sql");	

	private final String SQL_SELECT_CTA_CORRIENTE = loadResourceToString("cl/bcs/tesoreria/sql/IngresarMandatoPAC_SelectCtaCorriente.sql");
	private final String SQL_SELECT_DOC_IDENTIDAD = loadResourceToString("cl/bcs/tesoreria/sql/IngresarMandatoPAC_SelectDocIdentidad.sql");
	
	private final String SQL_CREATE_TABLE = loadResourceToString("cl/bcs/tesoreria/sql/ModificarPersonaCuentaBancaria_CreateTable.sql");
	private final String SQL_DROP_TABLE = loadResourceToString("cl/bcs/tesoreria/sql/ModificarPersonaCuentaBancaria_DropTable.sql");
	private final String SQL_INSERT_TABLE = loadResourceToString("cl/bcs/tesoreria/sql/ModificarPersonaCuentaBancaria_InsertTable.sql");
	private final String SQL_CALL_SP = loadResourceToString("cl/bcs/tesoreria/sql/ModificarPersonaCuentaBancaria_CallSp.sql");
	
	private final String SQL_UPDATE_BANCO = loadResourceToString("cl/bcs/tesoreria/sql/IngresoPAC_ActualizarBanco.sql");	
	
	private final String SQL_SELECT_MANDATOS_PAC = loadResourceToString("cl/bcs/tesoreria/sql/IngresarMandatoPAC_SelectMandatosPAC.sql");
	
	//private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
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
		public Long IdMandatoPACBase;
		public Long DiaDescuento;
		public String EstadoContrato;
		public String EstadoMandato;

		
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
		public Long FolioPAC;
		
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
		//public Long FolioPACMandatoAgrupador;
		public String Receptor;
		public Date Fecha;
		public Long NumeroMandato;
		public String Firma;
		public String IdentServicio;
		public String Estado;
		public String EstadoCargo;
		public Long IdEmpresa;
		public String Canal;
		public Long IdMandatoPACBase;
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
	
	private void updateBanco(ResultadoNumeroCuentaBancaria cuentaBancaria, MandatoPAC mandatoPAC, Connection conn) throws SQLException {
		if(mandatoPAC.FolioCSC.equals(0L) == false) {
			
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
		log.write("INICIO Modificar Mandato PAC");
		
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
			String fechaSistema = Utils.getFechaSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
			
			ArrayList<String> correlativos_CSC = new ArrayList<String>();
			ArrayList<String> correlativos_SAAPV = new ArrayList<String>();
			HashMap<String,String> codigos_banco = new HashMap<String,String>();
			String correlativos_CTA_CTE = "";
			List<InIngresoPAC> solicitudInvalida = new ArrayList<InIngresoPAC>();
			List<InIngresoPAC> solicitudProcesar = new ArrayList<InIngresoPAC>();
			List<String> correlativos_IdentServicio = 	new ArrayList<String>();
			
			ResultadoNumeroCuentaBancaria cuentaBancaria = new ResultadoNumeroCuentaBancaria();
			
			for (InIngresoPAC mov: entrada.p_IngresoPAC) {
				Boolean Valido = true; 
				
				correlativos_CSC.add(mov.FolioCSC);
				correlativos_SAAPV.add(mov.FolioSAAPV);
				correlativos_IdentServicio.add(mov.IdentServicio);
				Long cuenta=mov.IdCuentaBancaria;
				String doc_ident=mov.DocumentoIdentidad;

				HashMap<String,Object> params_CTA_CTE = new HashMap<String,Object>();
				params_CTA_CTE.put("IdCuentaBancaria", cuenta);
				List<ResultadoNumeroCuentaBancaria> mandatosExistentesCTACTE = sql.query(con, SQL_SELECT_CTA_CORRIENTE, params_CTA_CTE, ResultadoNumeroCuentaBancaria.class);
				
				
						
				// si IdCliente es == 0, viene del WS
				if (mov.IdCliente == 0L ) {  
					
					HashMap<String,Object> params_DocumentoIdentidad = new HashMap<String,Object>();
					params_DocumentoIdentidad.put("DocumentoIdentidad", doc_ident);
					List<ResultadoDocumentoIdentidad> mandatosExistentesDocumentoIdentidad = sql.query(con, SQL_SELECT_DOC_IDENTIDAD, params_DocumentoIdentidad, ResultadoDocumentoIdentidad.class);
					
					
					if (mandatosExistentesDocumentoIdentidad.size() == 0 ) {
						//out.P_OUT_MENSAJES++;
						out.P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMandatoPACBase,MensajeSalida.MENSAJE_ADVERTENCIA,"DOCUMENTO_IDENTIDAD_NO_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
						Valido = false;
					}
				}
				if (mandatosExistentesCTACTE.size() == 0) {
					//out.P_OUT_MENSAJES++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMandatoPACBase,MensajeSalida.MENSAJE_ADVERTENCIA,"CUENTA_CORRIENTE_NO_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					Valido = false;
				} else {
					if (!codigos_banco.containsKey(mandatosExistentesCTACTE.get(0).NumeroCuentaBancaria)) codigos_banco.put(mandatosExistentesCTACTE.get(0).NumeroCuentaBancaria, mandatosExistentesCTACTE.get(0).CodigoBanco);
					cuentaBancaria = mandatosExistentesCTACTE.get(0);
				} 

				if(Valido == false){	

				solicitudInvalida.add(mov);
				}else {
					solicitudProcesar.add(mov);//encontrado
				};
				
		

			}
			
			HashMap<String,Object> params_CSC = new HashMap<String,Object>();
			params_CSC.put("FolioCSC", correlativos_CSC);
			List<MandatoPAC> mandatosExistentesCSC = sql.query(con, SQL_SELECT_FOLIO_CSC, params_CSC, MandatoPAC.class);
			
			List<MandatoPAC> mandatosExistentesConMismoIdentServicio = obtenerMandatosPACConMismoIdentServicio(con, sql, correlativos_IdentServicio);
			
			List<MandatoPAC> listMandatoPAC = new ArrayList<MandatoPAC>();
						
			for (InIngresoPAC ingresos : solicitudProcesar){
				
				List<MandatoPAC> existenteCSC = new ArrayList<>();
				
				if(mandatosExistentesCSC.size() > 0) {
					existenteCSC.addAll(mandatosExistentesCSC.stream()
							.filter(Mandato -> Mandato.FolioCSC.equalsIgnoreCase(ingresos.FolioCSC) && Mandato.IdMandatoPAC.compareTo(ingresos.IdMandatoPACBase) != 0)
							.collect(Collectors.toList()));
				}
							
				List<MandatoPAC> mandatosConIgualesCaracteristicas = new ArrayList<>();
				String tipoContrato = "";
				
				if(existenteCSC.size() > 0 && ingresos.EstadoMandato.equalsIgnoreCase("ING")) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"FOLIO_CSC_YA_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					continue;
				}
				
				if(!ingresos.FolioSAAPV.equalsIgnoreCase("0")) {
					mandatosConIgualesCaracteristicas.addAll(mandatosExistentesConMismoIdentServicio.stream()
							.filter(Mandato-> Mandato.IdentServicio.equalsIgnoreCase(ingresos.IdentServicio) && Mandato.DiaDescuentoPorAutorizar.compareTo(ingresos.DiaDescuento) == 0 && Mandato.IdCliente.compareTo(ingresos.IdCliente) == 0 && (!Mandato.FolioSAAPV.equalsIgnoreCase("0")) && Mandato.IdMandatoPAC.compareTo(ingresos.IdMandatoPACBase) != 0)
							.collect(Collectors.toList()));
					tipoContrato = "SAAPV";
				}
				if(!ingresos.FolioCSC.equalsIgnoreCase("0")) {
					mandatosConIgualesCaracteristicas.addAll(mandatosExistentesConMismoIdentServicio.stream()
							.filter(Mandato-> Mandato.IdentServicio.equalsIgnoreCase(ingresos.IdentServicio) && Mandato.DiaDescuentoPorAutorizar.compareTo(ingresos.DiaDescuento) == 0 && Mandato.IdCliente.compareTo(ingresos.IdCliente) == 0 && (!Mandato.FolioCSC.equalsIgnoreCase("0")) && Mandato.IdMandatoPAC.compareTo(ingresos.IdMandatoPACBase) != 0)
							.collect(Collectors.toList()));
					tipoContrato = "CSC";
				}
				
				if(mandatosConIgualesCaracteristicas.size() > 0) {
					HashMap<String,String> mje = new HashMap<String,String>();
					mje.put("TipoContrato", tipoContrato);
					mje.put("FolioAgrupador", mandatosConIgualesCaracteristicas.get(0).IdentServicio);
					mje.put("DiasDescuento", mandatosConIgualesCaracteristicas.get(0).DiaDescuentoPorAutorizar.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"MANDATOS_PAC_CON_IGUAL_FOLIO_AGRUPADOR_Y_DIAS_DESCUENTO",mje,con,entrada.p_IdEmpresa));
					continue;
				}
											
				MandatoPAC mandatoPAC 		= new MandatoPAC();
				mandatoPAC.IdCliente 		= ingresos.IdCliente;
				mandatoPAC.IdPortafolio 	= ingresos.IdPortafolio;
				mandatoPAC.FolioCSC 		= ingresos.FolioCSC;
				mandatoPAC.CuentaCorriente 	= ingresos.CuentaCorriente;
				mandatoPAC.FolioSAAPV 		= ingresos.FolioSAAPV;
				mandatoPAC.Receptor 		= ingresos.Receptor;
				mandatoPAC.Fecha 			= ingresos.Fecha;
				mandatoPAC.NumeroMandato 	= ingresos.NumeroMandato;
				mandatoPAC.Firma 			= ingresos.Firma;
				mandatoPAC.IdentServicio 	= ingresos.IdentServicio;
				mandatoPAC.IdEmpresa 		= ingresos.IdEmpresa;
				mandatoPAC.Canal 			= ingresos.Canal;
				mandatoPAC.IdMandatoPACBase	= ingresos.IdMandatoPACBase;
				mandatoPAC.IdCuentaBancaria	= ingresos.IdCuentaBancaria;
				mandatoPAC.EstadoPorAutorizar = ingresos.EstadoMandato;
				mandatoPAC.DiaDescuentoPorAutorizar	= ingresos.DiaDescuento;
				mandatoPAC.EstadoContratoPorAutorizar = ingresos.EstadoContrato;
				mandatoPAC.Estado = "PAU";
			
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
				
				evento.FechaOperacionConcepto = fechaSistema;//"2018-06-06";
				evento.IdCliente = (long) 1;
			
				evento.Glosa = "Modificacion Contrato PAC"+" "+"IdMandato:"+mandatoPAC.IdMandatoPACBase;
				evento.NumeroOperacionConcepto = Long.toString(mandatoPAC.IdMandatoPACBase);
			
				
				entradaEvtAutorizacion.P_TABLAEVENTOS = Arrays.asList(evento);
				
				CrearEventoAutorizacionMasivo.ServiceSalida salidaAut = evtAutorizacion.logica(con, entradaEvtAutorizacion);
				
				for(CrearEventoAutorizacionMasivo.ObjADVERTENCIASDETALLE advertencia : salidaAut.P_OUT_ADVERTENCIASDETALLE){
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,advertencia.Descripcion,new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				
				}
				for (CrearEventoAutorizacionMasivo.ObjERRORESDETALLE error : salidaAut.P_OUT_ERRORESDETALLE) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,error.Descripcion,new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				}
				

				
				listMandatoPAC.add(mandatoPAC);
				
				ResultadoIngresoPAC resultadoIngresoPAC = new ResultadoIngresoPAC();
				resultadoIngresoPAC.IdMandatoPAC = mandatoPAC.IdMandatoPACBase;
				resultadoIngresoPAC.FolioPAC = mandatoPAC.IdMandatoPACBase;
				out.P_OUT_RESULTADO.add(resultadoIngresoPAC);
			}
			
			
			if(listMandatoPAC.size() > 0) {
				sql.batch(con, SQL_UPDATE_MANDATO, listMandatoPAC, MandatoPAC.class);				
				for (MandatoPAC salida : listMandatoPAC){					
					this.updateBanco(cuentaBancaria, salida, con);					
					MensajeSalida msg = new MensajeSalida();
					msg.id = salida.IdMandatoPACBase;
					msg.tipo = MensajeSalida.MENSAJE_AUTORIZACION;
					msg.codigo = "MODIFICACION_MANDATO_PAC_OK, FOLIO: " + salida.IdMandatoPACBase;
					msg.descripcion = "Modificacion de Mandato PAC exitosa, FOLIO: " + salida.IdMandatoPACBase;
					out.P_OUT_MENSAJES.add(msg);
				}
			}
			
			/******** AGREGAR LOG ************************/
			agregarLogSistema(con, entrada, fechaSistema);
			/*********************************************/
			
		}
		catch (Exception error) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write(error);
			throw error;
		}
		
		return out;
	}
	
	private void agregarLogSistema(Connection con, EntradaIngresoMandatoPAC entrada, String fechaSistema) throws Exception {
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
		log.IdCabeceraLog = 0L;
		log.IdTipoLog = "MODIFICACION MANDATO PAC";
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
		log.IdTipoRegistro = "MODIFICACION_MANDATO_PAC";
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
		logger.debug("INICIO::ModificarPAC.obtenerMandatosPACConMismoIdentServicio");
		long inicio = System.currentTimeMillis();
		
		List<MandatoPAC> mandatosExistentesConMismoIdentServicio = new ArrayList<>();
		
		HashMap<String,Object> params_IdentServicio = new HashMap<String,Object>();
		params_IdentServicio.put("IdentServicio", correlativos_IdentServicio);
		mandatosExistentesConMismoIdentServicio = sql.query(con, SQL_SELECT_MANDATOS_PAC, params_IdentServicio, MandatoPAC.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPAC.obtenerMandatosPACConMismoIdentServicio: tiempo " + (fin - inicio));
		
		return mandatosExistentesConMismoIdentServicio;
	}
	
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		//try (Connection con = PlataformaUtils.getSURAConnection()) {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				
			String json3 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"TES\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_IngresoPAC\":[{\"IdCliente\":1,\"IdPortafolio\":0,\"FolioCSC\":0,\"FolioPAC\":4003,\"CuentaCorriente\":\"454545\",\"FolioSAAPV\":\"766\",\"FolioPACMandatoAgrupador\":1,\"Receptor\":\"GABRIEL\",\"Fecha\":\"2019-03-06T03:00:00.000Z\",\"NumeroMandato\":20,\"Firma\":\"Si\",\"IdentServicio\":\"0000000000000454545281\",\"IdEmpresa\":1,\"IdCuentaBancaria\":208,\"Canal\":\"OPTIMUS\",\"TipoPago\":\"S\",\"FolioPACIn\":4002}]}";
			
			//String json4 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"TES\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_IngresoPAC\":[{\"IdCliente\":524891,\"IdPortafolio\":1450068,\"FolioCSC\":131325,\"CuentaCorriente\":\"11111111111111\",\"FolioSAAPV\":0,\"FolioPACMandatoAgrupador\":1,\"Receptor\":\"\",\"Fecha\":\"1900-01-01\",\"NumeroMandato\":0,\"Firma\":\"Si\",\"IdentServicio\":\"0000000111111111111117\",\"IdEmpresa\":1,\"IdCuentaBancaria\":260,\"Canal\":\"OPTIMUS\",\"TipoPago\":\"S\"}]}";

			EntradaIngresoMandatoPAC  Objin_json = gson.fromJson(json3, EntradaIngresoMandatoPAC.class);
			
			ModificarPAC demo = new ModificarPAC();
			System.out.println("ENTRADA");
 			System.out.println(Objin_json.toString());
 			System.out.println("SALIDA");
 			try { 
 				SalidaCrearIngresoPAC  ObjOut = demo.logica(con, Objin_json);
 				con.commit();
 				System.out.println(ObjOut.toString());
 			} 
 			catch(Exception er) {
				er.printStackTrace();	
			}
			finally {
				con.rollback();
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}
		System.out.println("== END ==");
	}
}
