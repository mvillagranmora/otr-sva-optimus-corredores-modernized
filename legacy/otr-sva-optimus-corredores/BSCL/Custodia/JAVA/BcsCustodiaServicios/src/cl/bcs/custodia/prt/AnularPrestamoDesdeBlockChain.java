package cl.bcs.custodia.prt;
import java.sql.Connection;
import java.text.SimpleDateFormat;
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

import cl.bcs.custodia.prt.AnularPrestamo.Prestamo;
import cl.bcs.custodia.prt.AnularPrestamo.Salida;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class AnularPrestamoDesdeBlockChain implements JavaCall
{
	static final Logger logger = LogManager.getLogger(AnularPrestamoDesdeBlockChain.class);
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	private final String SQL_SELECT_PRESTAMO			= loadResourceToString("cl/bcs/custodia/sql/AnularPrestamoDesdeBlockChain_SelectPrestamo.sql");
	private final String SQL_SELECT_PRESTAMISTAS	    = loadResourceToString("cl/bcs/custodia/sql/AnularPrestamo_SelectPrestamistas.sql");	
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class AnularPrestamoDesdeBlockChainEntrada extends BcsPojo {
		public Long			p_IdEmpresa		;
		public Long			p_IdUsuario		;
		public List<String>	p_FoliosBlockChain; 
		public List<String>	p_PrestamistasBlockChain;
		public String 		p_FechaAnulacion;
	}
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class AnularPrestamoDesdeBlockChainSalida extends BcsPojo {
		public Long 				P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public AnularPrestamoDesdeBlockChainSalida() {
			this.P_OUT_ERRORESCONTADOR 	= 0L;
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
		}
	}
	//**************************************************************
    public static class PrestamistaAnu extends BcsPojo {
    	public Long 	IdCartera;
    	public Long 	FolioCartera;
    	public Long 	FolioCarteraRelacionada;
    	public String 	DocumentoIdentidad;
	
    }
  //**************************************************************
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		AnularPrestamoDesdeBlockChainEntrada jin = gson.fromJson(in.toString(), AnularPrestamoDesdeBlockChainEntrada.class);
		AnularPrestamoDesdeBlockChainSalida out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public AnularPrestamoDesdeBlockChainSalida logica(Connection conn, AnularPrestamoDesdeBlockChainEntrada in) throws Exception {
		
		logger.info("INICIO::AnularPrestamoDesdeBlockChain");
		
		AnularPrestamoDesdeBlockChainSalida salida 	= new AnularPrestamoDesdeBlockChainSalida();
		SQL sql 									= new SQL();
		SimpleDateFormat dateFormat 				= new SimpleDateFormat("yyyy-MM-dd");
		HashMap<String,Object> parametros 			= new HashMap<String,Object>();
		HashMap<String,String> mapMsg 				= new HashMap<>();
		List<Prestamo> prestamos 					= null;
		List<Long> prestamosHabilitadosAnulacion	= new ArrayList<>();
		List<String> foliosBlockChainPrestamo		= in.p_FoliosBlockChain.stream()
				.map(folio ->  folio )
				.collect(Collectors.toList());
		
		/*** OBTIENE PRESTAMOS DE FOLIOS BLOCKCHAIN ENTRANTES ***/
		parametros.put("p_IdEmpresa", in.p_IdEmpresa );
		parametros.put("p_FolioSistemaExterno", foliosBlockChainPrestamo);
		prestamos = sql.query(conn, SQL_SELECT_PRESTAMO, parametros, Prestamo.class);
		parametros.clear();
		
		List<Long> foliosPrestamo					= prestamos.stream()
				.map(prestamo ->  prestamo.FolioPrestamo )
				.collect(Collectors.toList());
		
		/** BUSCA PRESTAMISTAS RELACIONADOS **/
		parametros.put("FolioPrestamo", foliosPrestamo);
		parametros.put("IdEmpresa",     in.p_IdEmpresa);				
		List<PrestamistaAnu> prestamistas 			= sql.query(conn, SQL_SELECT_PRESTAMISTAS, parametros, PrestamistaAnu.class);
		parametros.clear();
		
		/** VERIFICA QUE FOLIO BLOCKCHAIN SE ENCUENTRE EN OPTIMUS **/
		for (String folioSistemaExterno : foliosBlockChainPrestamo) {
			if (prestamos.stream()
					.filter(prestamo -> prestamo.FolioSistemaExterno.equals(folioSistemaExterno))
					.collect(Collectors.toList())
					.size() == 0) {
				mapMsg.put("FolioSistemaExterno", folioSistemaExterno);
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_PRESTAMO_BLOCKCHAIN_NO_ENCONTRADO", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR += 1L;
				mapMsg.clear();
			}
		}
		
		/** CONSULTA PAIS SEGUN EMPRESA **/
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
		Long IdPaisEmpresa = empresa.IdPais;
		
		for (Prestamo prestamo : prestamos) {
			
			if ( !prestamo.IdEstado.equalsIgnoreCase("VIG") ) {
				if ( !prestamo.IdEstado.equalsIgnoreCase("ANU") ) {
					salida.P_OUT_MENSAJES.add( new MensajeSalida( 0L, MensajeSalida.MENSAJE_ERROR, "BC_PRESTAMO_ANULADO_DIFERENTE_ESTADO", mapMsg, conn, in.p_IdEmpresa ) );
					salida.P_OUT_ERRORESCONTADOR += 1L;
					mapMsg.clear();
				}
				continue;
			}
			
			int contadorPrestamistasProcesadosOK	= 0;
			int iteracionesPrestamista 				= 0;
			Date fechaInicial   					= prestamo.FechaPrestamo;
			Date fechaFinal 						= dateFormat.parse(in.p_FechaAnulacion);
			int diferenciaDias 						= (int) ((fechaFinal.getTime()-fechaInicial.getTime())/86400000);
			
			if (diferenciaDias != 0) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(prestamo.FolioPrestamo, MensajeSalida.MENSAJE_ERROR, "ANULACION_PRESTAMO_SOLO_DIA_INGRESO", new HashMap<>(), conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR++;
				continue;
			}
			
			List<PrestamistaAnu> lstPrestamistas	= prestamistas.stream()
					.filter(prestamista -> prestamista.FolioCarteraRelacionada.equals(prestamo.FolioPrestamo))
					.collect(Collectors.toList());
			
			if ( lstPrestamistas.size() == 0 ) {
				prestamosHabilitadosAnulacion.add(prestamo.FolioPrestamo);
				continue;
			}
			
			for ( PrestamistaAnu prestamista : lstPrestamistas ) {
				
				AnularEmprestito objAnularPrestamista = new AnularEmprestito();
				AnularEmprestito.Entrada objAnularPrestamista_Entrada = new AnularEmprestito.Entrada();
				objAnularPrestamista_Entrada.p_IdEmpresa 			= in.p_IdEmpresa;
				objAnularPrestamista_Entrada.p_IdUsuario 			= in.p_IdUsuario;
				objAnularPrestamista_Entrada.p_IdModulo 			= "CUS";
				objAnularPrestamista_Entrada.p_IdEmpleadoDigitador 	= in.p_IdUsuario;
				objAnularPrestamista_Entrada.p_IdPais 				= IdPaisEmpresa;
				objAnularPrestamista_Entrada.p_FechaAnulacion 		= in.p_FechaAnulacion; 
				objAnularPrestamista_Entrada.p_FolioEmprestito 		= prestamista.FolioCartera;
				objAnularPrestamista_Entrada.p_VieneDesdeBlockChain = "S";
				AnularEmprestito.Salida objAnularPrestamista_salida = objAnularPrestamista.logica(conn, objAnularPrestamista_Entrada);
				iteracionesPrestamista++;
				if (objAnularPrestamista_salida.P_OUT_ERRORESCONTADOR == 0) {
					if (!prestamosHabilitadosAnulacion.contains(prestamo.FolioPrestamo)) {
						prestamosHabilitadosAnulacion.add(prestamo.FolioPrestamo);
					}
					contadorPrestamistasProcesadosOK++;
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "UI_EMPRESTITO_ANULADO_OK", null, conn, in.p_IdEmpresa));
				} else {
					salida.P_OUT_MENSAJES.addAll(
							objAnularPrestamista_salida.P_OUT_MENSAJES.stream()
							.map(mensaje -> {
								return PlataformaUtils.obtenerMensajeSalida(conn, in.p_IdEmpresa, prestamista.FolioCartera, mensaje.tipo, mensaje.codigo, new HashMap<>());
							})
							.collect(Collectors.toList()));
					salida.P_OUT_ERRORESCONTADOR += 1L;
				}
				// si alguno de los emprestitos no se pudo anular, se quita prestamo de la lista para que no arroje error al anular prestamo
				if (iteracionesPrestamista == lstPrestamistas.size() && contadorPrestamistasProcesadosOK < lstPrestamistas.size()) {
					prestamosHabilitadosAnulacion.remove(prestamo.FolioPrestamo);
					salida.P_OUT_MENSAJES.add(new MensajeSalida(prestamo.FolioPrestamo, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_ANULAR_EMPRESTITO_DE_PRESTAMO", new HashMap<>(), conn, in.p_IdEmpresa));
				}
			}
			
		}
		
		/*** ANULAR PRESTAMO ***/
		for (Long folioPrestamo : prestamosHabilitadosAnulacion) {
			
			AnularPrestamo objAnularPrestamos 					= new AnularPrestamo();
			AnularPrestamo.Entrada objAnularPrestamos_entrada 	= new AnularPrestamo.Entrada();
			objAnularPrestamos_entrada.p_IdEmpresa 				= in.p_IdEmpresa;
			objAnularPrestamos_entrada.p_IdUsuario 				= in.p_IdUsuario;
			objAnularPrestamos_entrada.p_IdModulo 				= "CUS";
			objAnularPrestamos_entrada.p_FechaAnulacion 		= in.p_FechaAnulacion;
			objAnularPrestamos_entrada.p_FolioPrestamo 			= folioPrestamo;
			objAnularPrestamos_entrada.p_VieneDesdeBlockChain 	= "S";  // indica que no vaya a blockchain a realizar anulacion
			
			Salida objAnularPrestamos_salida = objAnularPrestamos.logica(conn, objAnularPrestamos_entrada);
			
			if (objAnularPrestamos_salida.P_OUT_ERRORESCONTADOR > 0) { 
				salida.P_OUT_MENSAJES.addAll(
						objAnularPrestamos_salida.P_OUT_MENSAJES.stream()
						.map(mensaje -> {
							return PlataformaUtils.obtenerMensajeSalida(conn, in.p_IdEmpresa, folioPrestamo, mensaje.tipo, mensaje.codigo, new HashMap<>());
						})
						.collect(Collectors.toList()));
				salida.P_OUT_ERRORESCONTADOR += 1L;
			} else {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "UI_PRESTAMO_ANULADO_OK_OPTIMUS", null, conn, in.p_IdEmpresa));
	
			}
		}
		
		logger.info("FIN::AnularPrestamoDesdeBlockChain");
		
		return salida;
		
	}

		//-------------------------------------------------
		// METODO PARA PRUEBAS LOCALES 
		//-------------------------------------------------
	 
		public static void main(String[] args) throws Exception {
			System.out.println("== INICIO PRUEBA LOCAL ==");
			
			try (Connection con = PlataformaUtils.getDESAConnection()) {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();		

			String json = "{\"p_IdEmpresa\":1, \"p_IdUsuario\":54, \"p_FoliosBlockChain\":[\"61-0000000010435\", \"61-0000000010436\", \"61-0000000010437\"] ,\"p_PrestamistasBlockChain\":[], \"p_FechaAnulacion\": \"2022-11-08\" }";  
			AnularPrestamoDesdeBlockChainEntrada Objin = gson.fromJson(json, AnularPrestamoDesdeBlockChainEntrada.class);
			AnularPrestamoDesdeBlockChain demo = new AnularPrestamoDesdeBlockChain();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try {
 				AnularPrestamoDesdeBlockChainSalida ObjOut = demo.logica(con, Objin);
 				System.out.println("SALIDA:" + ObjOut);
 				//con.commit();
 				con.rollback();
 			} catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}
	    }

			System.out.println("== FIN PRUEBA LOCAL ==");
		}
	}
