package cl.bcs.custodia.prt;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.prt.CrearEmprestitoDesdeBlockChain.CrearEmprestitoDesdeBlockChain_Entrada;
import cl.bcs.custodia.prt.CrearEmprestitoDesdeBlockChain.CrearEmprestitoDesdeBlockChain_Salida;
import cl.bcs.custodia.prt.CrearPrestamoDesdeBlockChain.CrearPrestamoDesdeBlockChain_Entrada;
import cl.bcs.custodia.prt.CrearPrestamoDesdeBlockChain.CrearPrestamoDesdeBlockChain_Salida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.Folio;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.PrestamosBlockChain;
import cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamoAsignado;
import cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamoCreado;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pla.utils.Diccionario;


public class ReprocesarPrestamos implements JavaCall {
	private final String SQL_UPDATE_ESTADO_PRESTAMO 	= loadResourceToString("cl/bcs/custodia/sql/ReprocesarPrestamo_UpdateEstadoPrestamo.sql");
	private final String SQL_SELECT_PRESTAMO_PENDIENTE	= loadResourceToString("cl/bcs/custodia/sql/ReprocesarPrestamo_SelectPrestamosPendientes.sql");
	
	//private final String SQL_SELECT_PRESTAMO_ORIGINAL = loadResourceToString("cl/bcs/custodia/sql/ReprocesarPrestamo_SelectPrestamoOriginal.sql");
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class ReprocesarPrestamos_Entrada {
		public Long 			p_IdEmpresa;
	    public Long				p_IdUsuario;
	    public String			p_IdModulo;
	    public Long				p_IdPrestamosPendientes;
	    public String			p_Funcion;
	    public List<Reprocesar>	IDPRESTAMOS;
		
	    public ReprocesarPrestamos_Entrada() {
			this.IDPRESTAMOS = new ArrayList<Reprocesar>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	} 
	 
	public static class Reprocesar {
		public Long			IdPrestamosPendientes;
		public Long			Nemotecnico;
	    public String		Mercado;
		public String		DocumentoIdentidad;
		public String		TipoDocIdentidad;
		public String		Cantidad;
		public String		Tasa;
		public String		CondicionLiquidacion;
		public String		FechaLiquidacion;
		public String		CodigoCorredora;
		public String		FolioSistemaExterno;
		public String		Origen;
		public String		Mensaje;
		public String		Estado;
		public String		TipoRegistro;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Indicador {
		public Long IdCliente;
		public Long IdPersona;
		public String IdTipoCliente;
		public Long IdPortafolio;
		public String CodigoTipoIndicador;
		public String ValorIndicador;
		
		public String toString() {
			return Utils.printJson(this, false);
		} 
	}
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class ReprocesarPrestamos_Salida {	
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public List<SalidaRenovacion>		P_OUT_SALIDA_RENOVACION;
		
		public ReprocesarPrestamos_Salida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES	 =  new ArrayList<MensajeSalida>();
			this.P_OUT_SALIDA_RENOVACION = new ArrayList<SalidaRenovacion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaRenovacion {
		public Long				p_NewIdCartera;
		//public Long				p_NewIdSubyacente;
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	


	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------	
	public ReprocesarPrestamos_Salida logica(Connection conn, ReprocesarPrestamos_Entrada entrada) throws Exception {
		
		SQL _sql 							= new SQL();
		ReprocesarPrestamos_Salida salida 	= new ReprocesarPrestamos_Salida();
		HashMap<String,Object> hparam 		= new HashMap<String,Object>();
		
		hparam.put( "p_IdPrestamosPendientes", entrada.IDPRESTAMOS.stream().map( prestamo_pendiente -> prestamo_pendiente.IdPrestamosPendientes ).collect( Collectors.toList() ) );
		List<Reprocesar> prestamosReprocesar = _sql.query( conn, SQL_SELECT_PRESTAMO_PENDIENTE, hparam, Reprocesar.class );
		hparam.clear();
		
		if ( prestamosReprocesar.size() == 0 ) return salida;
		
		if ( !entrada.p_Funcion.equalsIgnoreCase("REPROCESAR") ) return salida;
		
		ConsultaListaPorFolioBlockChain consultaPrestamoPorLAID 												= new ConsultaListaPorFolioBlockChain();
		ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Entrada consultaPrestamoPorLAIDEntrada 	= new ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Entrada();
		
		List<Folio>	folios = prestamosReprocesar.stream()
				.map( prestamo -> new Folio( prestamo.FolioSistemaExterno ) )
				.collect( Collectors.toList() );
		
		consultaPrestamoPorLAIDEntrada.p_IdEmpresa 		= entrada.p_IdEmpresa;
		consultaPrestamoPorLAIDEntrada.p_FolioExterno	= folios;
		ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Salida consultaPrestamoPorLAIDSalida = consultaPrestamoPorLAID.logica(conn, consultaPrestamoPorLAIDEntrada);
		
		List<Reprocesar> prestamosPendientesActualizar 	= new ArrayList<>();
		List<PrestamoCreado> prestamosPorCrear 			= new ArrayList<>();
		List<PrestamoAsignado> prestamosAsignados		= new ArrayList<>();
		
		for ( Reprocesar reprocesar : prestamosReprocesar ) {
			
			List<PrestamosBlockChain> prestamoBlockChain = consultaPrestamoPorLAIDSalida.p_lista_prestamos.stream()
					.filter( prestamo_bck -> prestamo_bck.laDetails.LAID.equals( reprocesar.FolioSistemaExterno ) )
					.collect( Collectors.toList() );
					
			if ( prestamoBlockChain.size() == 0) continue;
					
			if ( reprocesar.TipoRegistro.equals( "PRT" ) ) {
				PrestamoCreado prestamoCreado 	= new PrestamoCreado();
				prestamoCreado.laDetails 		= prestamoBlockChain.get( 0 ).laDetails;
				prestamoCreado.borrowingClients	= prestamoBlockChain.get( 0 ).borrowingClients;
				prestamosPorCrear.add( prestamoCreado );
			}
			
			if ( reprocesar.TipoRegistro.equals( "EMP" ) ) {
				PrestamoAsignado prestamoAsignado 	= new PrestamoAsignado();
				prestamoAsignado.laDetails 			= prestamoBlockChain.get( 0 ).laDetails;
				prestamoAsignado.borrowingClients	= prestamoBlockChain.get( 0 ).borrowingClients;
				prestamoAsignado.lendingClients		= prestamoBlockChain.get( 0 ).lendingClients;
				prestamosAsignados.add( prestamoAsignado );
			}
		}
		
		if ( prestamosPorCrear.size() > 0 ) {
			
			CrearPrestamoDesdeBlockChain objCrearPrestamoDesdeBlockChain 					= new CrearPrestamoDesdeBlockChain();
			CrearPrestamoDesdeBlockChain_Entrada objCrearPrestamoDesdeBlockChain_Entrada 	= new CrearPrestamoDesdeBlockChain_Entrada();
			objCrearPrestamoDesdeBlockChain_Entrada.p_IdEmpresa			= entrada.p_IdEmpresa;
			objCrearPrestamoDesdeBlockChain_Entrada.p_IdUsuario			= entrada.p_IdUsuario;
			objCrearPrestamoDesdeBlockChain_Entrada.p_IdModulo			= "CUS";
			objCrearPrestamoDesdeBlockChain_Entrada.p_PrestamosCreado	= prestamosPorCrear;
			CrearPrestamoDesdeBlockChain_Salida objCrearPrestamoDesdeBlockChain_Salida = objCrearPrestamoDesdeBlockChain.logica( conn, objCrearPrestamoDesdeBlockChain_Entrada );
			
			if ( objCrearPrestamoDesdeBlockChain_Salida.P_OUT_ERRORESCONTADOR > 0 ) {
				
				for ( MensajeSalida mensajeSalidaError : objCrearPrestamoDesdeBlockChain_Salida.P_OUT_MENSAJES.stream()
						.filter( mensaje -> mensaje.tipo.equals( MensajeSalida.MENSAJE_ERROR ) )
						.collect( Collectors.toList() )) {
					
					List<String> foliosBlockChain = consultaPrestamoPorLAIDSalida.p_lista_prestamos.stream()
					.filter( prestamo_bck -> prestamo_bck.laDetails.LegacyLAID.equals( mensajeSalidaError.id.toString() ) )
					.map( prestamo_bck -> prestamo_bck.laDetails.LAID )
					.collect( Collectors.toList() );
					
					List<Reprocesar> reprocesos = prestamosReprocesar.stream()
					.filter( prestamo_reprocesar -> prestamo_reprocesar.FolioSistemaExterno.equals( foliosBlockChain.get( 0 ) ) )
					.collect( Collectors.toList() );
					
					HashMap<String, String> valores = new HashMap<String, String>();
					valores.put( "IdPrestamosPendientes", reprocesos.get( 0 ).IdPrestamosPendientes.toString() );
					salida.P_OUT_ERRORESCONTADOR += 1L;
					salida.P_OUT_MENSAJES.add( new MensajeSalida( mensajeSalidaError.id, mensajeSalidaError.tipo, "UI_ERROR_REPROCESA_PRESTAMOS", valores, conn, entrada.p_IdEmpresa  ) );
					
				}
			}
			
			prestamosPendientesActualizar = prestamosReprocesar.stream()
					.filter( prestamo_reprocesar -> objCrearPrestamoDesdeBlockChain_Salida.P_OUT_RESULTADO.stream().anyMatch( resultado -> resultado.equals( prestamo_reprocesar.FolioSistemaExterno ) ) )
					.collect( Collectors.toList() );
		}
		
		if ( prestamosAsignados.size() > 0 ) {
			CrearEmprestitoDesdeBlockChain objCrearEmprestitoDesdeBlockChain 					= new CrearEmprestitoDesdeBlockChain();
			CrearEmprestitoDesdeBlockChain_Entrada objCrearEmprestitoDesdeBlockChain_Entrada 	= new CrearEmprestitoDesdeBlockChain_Entrada();
			objCrearEmprestitoDesdeBlockChain_Entrada.p_IdEmpresa			= entrada.p_IdEmpresa;
			objCrearEmprestitoDesdeBlockChain_Entrada.p_IdUsuario			= entrada.p_IdUsuario;
			objCrearEmprestitoDesdeBlockChain_Entrada.p_IdModulo			= "CUS";
			objCrearEmprestitoDesdeBlockChain_Entrada.p_FoliosBlockChain	= prestamosAsignados;
			
			CrearEmprestitoDesdeBlockChain_Salida objCrearEmprestitoDesdeBlockChain_Salida = objCrearEmprestitoDesdeBlockChain.logica( conn, objCrearEmprestitoDesdeBlockChain_Entrada );
			
			if ( objCrearEmprestitoDesdeBlockChain_Salida.P_OUT_ERRORESCONTADOR > 0 ) {
				
				for ( MensajeSalida mensajeSalidaError : objCrearEmprestitoDesdeBlockChain_Salida.P_OUT_MENSAJES.stream()
						.filter( mensaje -> mensaje.tipo.equals( MensajeSalida.MENSAJE_ERROR ) )
						.collect( Collectors.toList() )) {
					
					List<String> foliosBlockChain = consultaPrestamoPorLAIDSalida.p_lista_prestamos.stream()
					.filter( prestamo_bck -> prestamo_bck.laDetails.LegacyLAID.equals( mensajeSalidaError.id.toString() ) )
					.map( prestamo_bck -> prestamo_bck.laDetails.LAID )
					.collect( Collectors.toList() );
					
					List<Reprocesar> reprocesos = prestamosReprocesar.stream()
					.filter( prestamo_reprocesar -> prestamo_reprocesar.FolioSistemaExterno.equals( foliosBlockChain.get( 0 ) ) )
					.collect( Collectors.toList() );
					
					HashMap<String, String> valores = new HashMap<String, String>();
					valores.put( "IdPrestamosPendientes", reprocesos.get( 0 ).IdPrestamosPendientes.toString() );
					salida.P_OUT_ERRORESCONTADOR += 1L;
					salida.P_OUT_MENSAJES.add( new MensajeSalida( mensajeSalidaError.id, mensajeSalidaError.tipo, "UI_ERROR_REPROCESA_PRESTAMISTAS", valores, conn, entrada.p_IdEmpresa  ) );
					
				}
			}
			
			prestamosPendientesActualizar = prestamosReprocesar.stream()
					.filter( prestamo_reprocesar -> objCrearEmprestitoDesdeBlockChain_Salida.P_OUT_RESULTADO.stream().anyMatch( resultado -> resultado.equals( prestamo_reprocesar.FolioSistemaExterno ) ) )
					.collect( Collectors.toList() );
		}
		
		if ( prestamosPendientesActualizar.size() > 0 ) {
			_sql.batch(conn, SQL_UPDATE_ESTADO_PRESTAMO, prestamosPendientesActualizar, Reprocesar.class);
		}
				
		return salida;
	}
	
	
	private BigDecimal BigDecimal(int i) {
		// TODO Auto-generated method stub
		return null;
	}


	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		ReprocesarPrestamos_Entrada in = gson.fromJson(jin.toString(), ReprocesarPrestamos_Entrada.class);
		ReprocesarPrestamos_Salida out = logica(con,in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
 
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();		
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"CUS\",\"p_Funcion\":\"REPROCESAR\",\"IDPRESTAMOS\":[{\"IdPrestamosPendientes\":276},{\"IdPrestamosPendientes\":277},{\"IdPrestamosPendientes\":278},{\"IdPrestamosPendientes\":279},{\"IdPrestamosPendientes\":280},{\"IdPrestamosPendientes\":281}]}";
			                         
			ReprocesarPrestamos_Entrada Objin = gson.fromJson(json, ReprocesarPrestamos_Entrada.class);
			ReprocesarPrestamos demo = new ReprocesarPrestamos();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try {
 				ReprocesarPrestamos_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println("SALIDA:" + ObjOut);
 				con.commit();
 				
 			} catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}

	    }

		System.out.println("== FIN PRUEBA LOCAL ==");
	}

}
