package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import cl.bcs.fondomutuo.CrearIngresoCSC.InIngresoCSC;
import cl.bcs.fondomutuo.CrearIngresoCSC.InIngresoCSCDetalle;
import cl.bcs.fondomutuo.CrearIngresoCSC.ResultadoIngresoCSC;
import cl.bcs.fondomutuo.CrearIngresoCSC.ResultadoOperacion;
import cl.bcs.fondomutuo.CrearIngresoCSC.EntradaCrearIngresoCSC;
import cl.bcs.fondomutuo.CrearIngresoCSC.SalidaCrearIngresoCSC;
import cl.bcs.tesoreria.IngresarMandatoPAC;
import cl.bcs.tesoreria.IngresarMandatoPAC.EntradaIngresoMandatoPAC;
import cl.bcs.tesoreria.IngresarMandatoPAC.InIngresoPAC;
import cl.bcs.tesoreria.IngresarMandatoPAC.SalidaCrearIngresoPAC;




public class CrearIngresoCSCManejador implements JavaCall{
	private final Log log = new Log("FM.InsertarCSCManejador");
	private final String SQL_SELECT_REQUISITOS_CREAR_MANDATO= loadResourceToString("cl/bcs/fondomutuo/sql/IngresarMandatoPAC_ConsultaRequisitosCrearMandato.sql");
	private final String SQL_SELECT_BANCO= loadResourceToString("cl/bcs/fondomutuo/sql/CrearIngresoCSCManejador_SelectBanco.sql");
	private final String SQL_SELECT_CUENTA_BANCARIA_PREDETERMINADA= loadResourceToString("cl/bcs/fondomutuo/sql/CrearIngresoCSCManejador_CuentaBancariaPredeterminada.sql");
	
	public static class RequisitosCrearMandato{
		public String CodigoBanco;
		public Long IdBanco;
		public Long IdCuentaBancaria;
		public String NumeroCuentaBancaria;
		public String NombreBanco;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public class SalidaCrearIngresoCSC_Mandato extends SalidaCrearIngresoCSC{
		EntidadContrato EntidadContrato;
	}


	public static int obtenerSumaPorDigitos(String cadenaInvertida) {
		int pivote = 2;
		int longitudCadena = cadenaInvertida.length();
		int cantidadTotal = 0;
		int b = 1;
		for(int i = 0; i<longitudCadena;i++ ) {
			if(pivote ==8)
				pivote = 2;
			int temporal = Integer.parseInt(cadenaInvertida.substring(i,b));
			b++;
	        temporal *= pivote;
	        pivote++;
	        cantidadTotal += temporal;
		}
	    cantidadTotal = 11 - cantidadTotal % 11;
		return cantidadTotal;
	}
	
	
	public static String invertirCadena(String cadena) {
		String cadenaInvertida = "";
		for(int x = cadena.length()-1;x>=0;x--) {
			cadenaInvertida = cadenaInvertida + cadena.charAt(x);
		}
		return cadenaInvertida;
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
	
	public static class EntradaCrearIngresoCSCManejadorCSC{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public List<InIngresoCSC> p_IngresoCSC;
		
		public EntradaCrearIngresoCSCManejadorCSC() {
			this.p_IngresoCSC = new ArrayList<InIngresoCSC>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCrearIngresoCSCManejador{
		public Long P_OUT_ERRORESCONTADOR;
		public List<EntidadContrato> CONTRATO;

		public SalidaCrearIngresoCSCManejador(){
			CONTRATO = new ArrayList<EntidadContrato>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntidadContrato{
		public String Folio;
		public Long	IdFolioCSC;
		public String MandatoAgrupador;
		public List<MensajesErrores> Mensajes;
        
		public EntidadContrato(){
			Mensajes = new ArrayList<MensajesErrores>();
		}
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class MensajesErrores{
		public Long IdInterno;
		public String Tipo;
		public String Codigo;
		public String Descripcion;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}


	public static class ResultadoIngresoCSC_Mandato{
		public SalidaCrearIngresoCSC salidaCrearIngresoCSC;
		public EntidadContrato salidaEntidadContrato;
	}
	
	public static class Banco{
		public Long IdBanco;
		public String NombreBanco;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static String generadorMAgrupador(String NumeroCuentaBancaria, String CodigoBanco) {
		//String temporal_agrupador = Integer.toString(NumeroCuentaBancaria)+ Integer.toString(CodigoBanco);
		String temporal_agrupador = NumeroCuentaBancaria+ CodigoBanco;
		int modulo_11 = 0;
		int diferencia = 0;
		String ceros = "0";
		String ident_servicio = "";
		String final_ident_servicio = "";
		if(temporal_agrupador.length()<17) {
			modulo_11 = obtenerSumaPorDigitos(invertirCadena(temporal_agrupador));
			if(modulo_11==10) 
				modulo_11 = 1;
			if (modulo_11 == 11) 
				modulo_11 = 0;
			temporal_agrupador += modulo_11;
			diferencia = 17 - temporal_agrupador.length();
			for(int i = 0; i < diferencia-1; i++)
				ceros += "0";
			ident_servicio = ceros+temporal_agrupador;
			final_ident_servicio = ident_servicio;
		}
		else if (temporal_agrupador.length() > 17){
			diferencia = temporal_agrupador.length() - 17;
			temporal_agrupador = temporal_agrupador.substring(diferencia,temporal_agrupador.length());
			final_ident_servicio = temporal_agrupador;
		}
		else 
			final_ident_servicio = temporal_agrupador;
		
		
		return final_ident_servicio;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCrearIngresoCSCManejadorCSC jin = gson.fromJson(in.toString(), EntradaCrearIngresoCSCManejadorCSC.class);
		SalidaCrearIngresoCSCManejador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaCrearIngresoCSCManejador logica (Connection con, EntradaCrearIngresoCSCManejadorCSC entrada) throws Exception{
		log.write("INICIO CrearIngresoCSCManejador");
		SalidaCrearIngresoCSCManejador out = new SalidaCrearIngresoCSCManejador();
		List<SalidaCrearIngresoCSC> salidas = new ArrayList<SalidaCrearIngresoCSC>();
		List<ResultadoIngresoCSC_Mandato> salidas_2 = new ArrayList<ResultadoIngresoCSC_Mandato>();
		ResultadoIngresoCSC_Mandato salidaCrearIngresoCSC_Mandato = new ResultadoIngresoCSC_Mandato();
		EntidadContrato entidadMandato = new EntidadContrato();
		try {
			for (InIngresoCSC mov : entrada.p_IngresoCSC) {
				
				//REQUISITOS_CREAR_MANDATO
				HashMap<String,Object> params = new HashMap<String,Object>();
				HashMap<String,String> params2 = new HashMap<String,String>();
				params.put("IdCliente", mov.IdCliente);
				params.put("IdBanco", mov.IdBanco);
				SQL _sql = new SQL();
				String identServicio = new String();
				Long idCuentaBancaria = 0L;
				String numeroCuentaBancaria= new String();
				
				salidaCrearIngresoCSC_Mandato = new ResultadoIngresoCSC_Mandato();
				SalidaCrearIngresoCSC salidaCrearIngreso = new SalidaCrearIngresoCSC();
				EntidadContrato salidaEntidadContrato = new EntidadContrato(); 
				
				List<RequisitosCrearMandato> listRequerimientos=_sql.query(con,SQL_SELECT_REQUISITOS_CREAR_MANDATO,params,RequisitosCrearMandato.class);
				if(listRequerimientos.isEmpty()) {
					HashMap<String,Object> paramsBanco = new HashMap<String,Object>();
					paramsBanco.put("IdBanco", mov.IdBanco);
					List<Banco> Banco=_sql.query(con,SQL_SELECT_BANCO,paramsBanco,Banco.class);
					MensajeSalida mensajeSalida = new MensajeSalida();
					if(!Banco.isEmpty()) {
						params2.put("NombreBanco", Banco.get(0).NombreBanco);
						mensajeSalida = new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"CUENTA_BANCARIA_NO_EXISTE_A_BANCO",params2,con,entrada.p_IdEmpresa);
					} else {
						params2.put("IdBanco", mov.IdBanco.toString());
						mensajeSalida = new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"BANCO_NO_EXISTE",params2,con,entrada.p_IdEmpresa);
					}
 					List<MensajeSalida> mensajeSalidaListaMensajeSalidas = new ArrayList<MensajeSalida>();
 					mensajeSalidaListaMensajeSalidas.add(mensajeSalida);
					salidaCrearIngreso.P_OUT_MENSAJES =  mensajeSalidaListaMensajeSalidas;
					salidaCrearIngreso.P_OUT_RESULTADO = new ArrayList<ResultadoIngresoCSC>();
					salidaEntidadContrato.Folio = mov.FolioCSC;
					salidaEntidadContrato.IdFolioCSC = 0L;
					salidaEntidadContrato.MandatoAgrupador = "";
									
					salidaCrearIngresoCSC_Mandato.salidaEntidadContrato = salidaEntidadContrato;
					salidaCrearIngresoCSC_Mandato.salidaCrearIngresoCSC = salidaCrearIngreso;
					
					salidas_2.add(salidaCrearIngresoCSC_Mandato);
				}
				else if(listRequerimientos.get(0).IdBanco == null) {
					params2.put("IdBanco", mov.IdBanco.toString());
					MensajeSalida mensajeSalida = new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"BANCO_NO_EXISTE",params2,con,entrada.p_IdEmpresa);
					List<MensajeSalida> mensajeSalidaListaMensajeSalidas = new ArrayList<MensajeSalida>();
					mensajeSalidaListaMensajeSalidas.add(mensajeSalida);
					salidaCrearIngreso.P_OUT_MENSAJES =  mensajeSalidaListaMensajeSalidas;
					salidaCrearIngreso.P_OUT_RESULTADO = new ArrayList<ResultadoIngresoCSC>();
					salidaEntidadContrato.Folio = mov.FolioCSC;
					salidaEntidadContrato.IdFolioCSC = 0L;
					salidaEntidadContrato.MandatoAgrupador = "";
									
					salidaCrearIngresoCSC_Mandato.salidaEntidadContrato = salidaEntidadContrato;
					salidaCrearIngresoCSC_Mandato.salidaCrearIngresoCSC = salidaCrearIngreso;
					
					salidas_2.add(salidaCrearIngresoCSC_Mandato);
				}
				params.clear();
				params.put("IdCliente", mov.IdCliente);
				List<RequisitosCrearMandato> ListCuentaBancariaPredeterminada = _sql.query(con,SQL_SELECT_CUENTA_BANCARIA_PREDETERMINADA,params,RequisitosCrearMandato.class);
				//Si Cuenta Bancaria enviada y predeterminada no existen, marca error
				if(ListCuentaBancariaPredeterminada.isEmpty()) {					
					salidaCrearIngresoCSC_Mandato = new ResultadoIngresoCSC_Mandato();
					salidaCrearIngreso = new SalidaCrearIngresoCSC();
					salidaEntidadContrato = new EntidadContrato(); 
					
					salidaCrearIngreso.P_OUT_ERRORESCONTADOR = 1L;
					MensajeSalida mensajeSalida = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CUENTA_BANCARIA_PREDETERMINADA_NO_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa);
					List<MensajeSalida> mensajeSalidaListaMensajeSalidas = new ArrayList<MensajeSalida>();
 					mensajeSalidaListaMensajeSalidas.add(mensajeSalida);
					salidaCrearIngreso.P_OUT_MENSAJES =  mensajeSalidaListaMensajeSalidas;
					salidaCrearIngreso.P_OUT_RESULTADO = new ArrayList<ResultadoIngresoCSC>();
					salidaEntidadContrato.Folio = mov.FolioCSC;
					salidaEntidadContrato.IdFolioCSC = 0L;
					salidaEntidadContrato.MandatoAgrupador = "";
									
					salidaCrearIngresoCSC_Mandato.salidaEntidadContrato = salidaEntidadContrato;
					salidaCrearIngresoCSC_Mandato.salidaCrearIngresoCSC = salidaCrearIngreso;
					
					salidas_2.add(salidaCrearIngresoCSC_Mandato);
				}
				else {
					//Si Cuenta Bancaria enviada no existe, toma predeterminada
					if(listRequerimientos.isEmpty()) {
						salidaCrearIngresoCSC_Mandato = new ResultadoIngresoCSC_Mandato();
						salidaCrearIngreso = new SalidaCrearIngresoCSC();
						salidaEntidadContrato = new EntidadContrato(); 
						
						params2.clear();
						params2.put("NombreBanco", ListCuentaBancariaPredeterminada.get(0).NombreBanco);
						MensajeSalida mensajeSalida = new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"CUENTA_BANCARIA_PREDETERMINADA_SELECCIONADA",params2,con,entrada.p_IdEmpresa);							
						List<MensajeSalida> mensajeSalidaListaMensajeSalidas = new ArrayList<MensajeSalida>();
						mensajeSalidaListaMensajeSalidas.add(mensajeSalida);
						salidaCrearIngreso.P_OUT_MENSAJES =  mensajeSalidaListaMensajeSalidas;
						salidaCrearIngreso.P_OUT_RESULTADO = new ArrayList<ResultadoIngresoCSC>();
						salidaEntidadContrato.Folio = mov.FolioCSC;
						salidaEntidadContrato.IdFolioCSC = 0L;
						salidaEntidadContrato.MandatoAgrupador = "";
										
						salidaCrearIngresoCSC_Mandato.salidaEntidadContrato = salidaEntidadContrato;
						salidaCrearIngresoCSC_Mandato.salidaCrearIngresoCSC = salidaCrearIngreso;
						
						salidas_2.add(salidaCrearIngresoCSC_Mandato);
						
						listRequerimientos = ListCuentaBancariaPredeterminada;
					}
					
					identServicio=generadorMAgrupador(listRequerimientos.get(0).NumeroCuentaBancaria.replaceAll("[^0-9]+",""),listRequerimientos.get(0).CodigoBanco);
					numeroCuentaBancaria = listRequerimientos.get(0).NumeroCuentaBancaria;
					idCuentaBancaria = listRequerimientos.get(0).IdCuentaBancaria;
					
					InIngresoCSC inContrato = new InIngresoCSC();
					EntradaCrearIngresoCSC entradaCrearIngresoCSC = new EntradaCrearIngresoCSC();
					
					inContrato.FolioCSC = mov.FolioCSC;
					inContrato.IdCliente = mov.IdCliente;
					inContrato.IdPortafolio = mov.IdPortafolio;
					inContrato.Hora = mov.Hora;
					inContrato.Fecha = mov.Fecha;
					inContrato.MontoPesos= mov.MontoPesos;
					inContrato.MontoUF = mov.MontoUF;
					inContrato.MontoUSD = mov.MontoUSD;
					inContrato.TipoPago = mov.TipoPago;
					inContrato.IdBanco = listRequerimientos.get(0).IdBanco;
					inContrato.NombreFuncionario = mov.NombreFuncionario;
					inContrato.DomicilioSucursal = mov.DomicilioSucursal;
					inContrato.CodigoSucursal = mov.CodigoSucursal;
					inContrato.DocumentoIdentificacionFuncionario = mov.DocumentoIdentificacionFuncionario;
					inContrato.FirmaFuncionario = mov.FirmaFuncionario;
					inContrato.DiaDescuento = mov.DiaDescuento;
					inContrato.AceptoCondiciones = mov.AceptoCondiciones;
					inContrato.Canal = mov.Canal;
					inContrato.MLT = mov.MLT;
					
					for (InIngresoCSCDetalle det : mov.p_IngresoCSCDetalle) {
						InIngresoCSCDetalle inContratoDetalle = new InIngresoCSCDetalle();
						inContratoDetalle.Serie= det.Serie;
						inContratoDetalle.Monto = det.Monto;
						inContratoDetalle.Porcentaje = det.Porcentaje;
						inContratoDetalle.Regimen = det.Regimen;
						inContrato.p_IngresoCSCDetalle.add(inContratoDetalle);			
					}
				
					entradaCrearIngresoCSC.p_IngresoCSC.add(inContrato);
					
					entradaCrearIngresoCSC.p_IdEmpresa = entrada.p_IdEmpresa;
					entradaCrearIngresoCSC.p_IdModulo = entrada.p_IdModulo;
					entradaCrearIngresoCSC.p_IdUsuario = entrada.p_IdUsuario;
					entradaCrearIngresoCSC.p_SimularOperacion = entrada.p_SimularOperacion;
					entradaCrearIngresoCSC.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
					
					CrearIngresoCSC crearIngresoCSC = new CrearIngresoCSC();
					SalidaCrearIngresoCSC salidaCrearIngresoCSC = crearIngresoCSC.logica(con, entradaCrearIngresoCSC);
					
					salidaCrearIngresoCSC_Mandato = new ResultadoIngresoCSC_Mandato();
					salidaCrearIngresoCSC_Mandato.salidaCrearIngresoCSC = salidaCrearIngresoCSC;
					
					entidadMandato.MandatoAgrupador = "";
					entidadMandato.Folio= salidaCrearIngresoCSC.P_OUT_RESULTADO.size() > 0 ? salidaCrearIngresoCSC.P_OUT_RESULTADO.get(0).FolioCSC : "";
					entidadMandato.IdFolioCSC = salidaCrearIngresoCSC.P_OUT_RESULTADO.size() > 0 ? salidaCrearIngresoCSC.P_OUT_RESULTADO.get(0).IdFolioCSC : 0;
					
					
					if(mov.TipoPago.equals("PAC") && salidaCrearIngresoCSC.P_OUT_ERRORESCONTADOR==0) {
						List<InIngresoPAC> solicitudesProcesar = new ArrayList<InIngresoPAC>();
						InIngresoPAC ingresoMandatoPAC = new InIngresoPAC ();
						
						ingresoMandatoPAC.IdCliente = mov.IdCliente;
						ingresoMandatoPAC.IdPortafolio =  mov.IdPortafolio;
						ingresoMandatoPAC.FolioCSC =  mov.FolioCSC;
						ingresoMandatoPAC.CuentaCorriente = numeroCuentaBancaria;
						ingresoMandatoPAC.FolioSAAPV =  "0";
						ingresoMandatoPAC.FolioPACMandatoAgrupador = (long) 1;//Uso Exclusivo del Banco
						ingresoMandatoPAC.Receptor =  ""; //Uso Exclusivo del Banco
						ingresoMandatoPAC.Fecha =  Fecha.toDate("1900-01-01");//Uso Exclusivo del Banco
						ingresoMandatoPAC.NumeroMandato =  (long) 0;
						ingresoMandatoPAC.Firma = "SI";//Uso Exclusivo del Banco
						ingresoMandatoPAC.IdentServicio = identServicio;
						ingresoMandatoPAC.IdEmpresa = (long) 1;
						ingresoMandatoPAC.IdCuentaBancaria = idCuentaBancaria;
						ingresoMandatoPAC.Canal = mov.Canal;
						ingresoMandatoPAC.TipoPago = mov.TipoPago;
						solicitudesProcesar.add(ingresoMandatoPAC )	;
						
						//Entrada Ingreso Mandato PAC
						EntradaIngresoMandatoPAC entradaMandatoPAC = new EntradaIngresoMandatoPAC();
						entradaMandatoPAC.p_IdEmpresa = entrada.p_IdEmpresa;
						entradaMandatoPAC.p_IdUsuario = entrada.p_IdUsuario;
						entradaMandatoPAC.p_IdModulo = entrada.p_IdModulo;
						entradaMandatoPAC.p_SimularOperacion = entrada.p_SimularOperacion;
						entradaMandatoPAC.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
						entradaMandatoPAC.p_IngresoPAC = solicitudesProcesar;
						
						IngresarMandatoPAC crearMandatoPAC = new IngresarMandatoPAC();
						SalidaCrearIngresoPAC salidaMandato = crearMandatoPAC.logica(con,entradaMandatoPAC);
						
						entidadMandato = new EntidadContrato();
						salidaCrearIngresoCSC_Mandato.salidaEntidadContrato = entidadMandato;
						entidadMandato.MandatoAgrupador = identServicio;
						salidaCrearIngresoCSC_Mandato.salidaCrearIngresoCSC.P_OUT_MENSAJES.addAll(salidaMandato.P_OUT_MENSAJES);
						entidadMandato.Folio=  salidaCrearIngresoCSC.P_OUT_RESULTADO.get(0).FolioCSC;
						entidadMandato.IdFolioCSC = salidaCrearIngresoCSC.P_OUT_RESULTADO.get(0).IdFolioCSC ;
						if(salidaMandato.P_OUT_MENSAJES!=null && salidaMandato.P_OUT_MENSAJES.get(0).codigo.contains("FOLIO_CSC_YA_EXISTE")) {
							entidadMandato.Folio= salidaCrearIngresoCSC.P_OUT_RESULTADO.size() > 0 ? salidaCrearIngresoCSC.P_OUT_RESULTADO.get(0).FolioCSC : "";
							entidadMandato.IdFolioCSC = salidaCrearIngresoCSC.P_OUT_RESULTADO.size() > 0 ? salidaCrearIngresoCSC.P_OUT_RESULTADO.get(0).IdFolioCSC : 0;
						}
					}
					salidaCrearIngresoCSC_Mandato.salidaEntidadContrato = entidadMandato;
					salidas_2.add(salidaCrearIngresoCSC_Mandato);
					}
			}
			
			for(ResultadoIngresoCSC_Mandato salida : salidas_2){
				Long i = 0L;
				entidadMandato = new EntidadContrato();
				List<MensajesErrores> listMensajesErrores = new ArrayList<MensajesErrores>();
				
				for (MensajeSalida mensajeSalida : salida.salidaCrearIngresoCSC.P_OUT_MENSAJES){
					MensajesErrores mensajesErrores = new MensajesErrores();
					mensajesErrores.IdInterno = i;
					mensajesErrores.Tipo = mensajeSalida.tipo;
					mensajesErrores.Codigo = mensajeSalida.codigo;
					mensajesErrores.Descripcion = mensajeSalida.descripcion;
					listMensajesErrores.add(mensajesErrores);
					entidadMandato.Mensajes = listMensajesErrores;
				}
				entidadMandato.MandatoAgrupador = salida.salidaEntidadContrato.MandatoAgrupador;
				entidadMandato.Folio= salida.salidaEntidadContrato.Folio;
				entidadMandato.IdFolioCSC = salida.salidaEntidadContrato.IdFolioCSC;
				
				out.CONTRATO.add(entidadMandato);
				
				if(salida.salidaCrearIngresoCSC.P_OUT_ERRORESCONTADOR > 0) {
					out.P_OUT_ERRORESCONTADOR++;
				}
				i++;
			}
			
			return out;
			
		}catch (Exception error) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write(error);
			throw error;
		}
	}

	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			String json5 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"1\",\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_IngresoCSC\":[{\"FolioCSC\":\"0000443320\",\"IdCliente\":61625,\"IdPortafolio\":206349,\"Hora\":\"12:25:56\",\"Fecha\":\"2025-04-21\",\"MontoPesos\":2489232,\"MontoUF\":0,\"MontoUSD\":0,\"TipoPago\":\"TRA\",\"IdBanco\":20,\"DomicilioSucursal\":\"\",\"CodigoSucursal\":\"\",\"NombreFuncionario\":\"\",\"DocumentoIdentificacionFuncionario\":\"\",\"FirmaFuncionario\":\"Si\",\"AceptoCondiciones\":\"Si\",\"Estado\":\"\",\"Canal\":\"EJE\",\"MLT\":\"S\",\"p_IngresoCSCDetalle\":[{\"Serie\":\"00382ACLP\",\"Porcentaje\":100,\"Regimen\":\"18Q\"}]}]}";
			EntradaCrearIngresoCSCManejadorCSC  Objin = gson.fromJson(json5, EntradaCrearIngresoCSCManejadorCSC.class);
			CrearIngresoCSCManejador  demo = new CrearIngresoCSCManejador();
			try {
				SalidaCrearIngresoCSCManejador  ObjOut = demo.logica(con, Objin);
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