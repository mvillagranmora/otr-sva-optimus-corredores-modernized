package cl.bcs.normativa.cartola.jobs;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Deflater;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.JsonObject;

import cl.bcs.normativa.cartola.GenerarCartola;
import cl.bcs.normativa.cartola.GestionarClientes;
import cl.bcs.normativa.cartola.GestionarClientes.EntradaGestionarClientes;
import cl.bcs.normativa.cartola.GestionarClientes.SalidaGestionarClientes;
import cl.bcs.normativa.cartola.GestionarNomina;
import cl.bcs.plataforma.evt.AgregarEmail;
import cl.bcs.plataforma.evt.AgregarEmail.Destinatario;
import cl.bcs.plataforma.evt.AgregarEmail.Email;
import cl.bcs.plataforma.evt.AgregarEmail.ServiceEntrada;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.EntradaEjecutarTareaJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.ParametroJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.SalidaEjecutarTareaJenkins;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public final class GenerarCartolasJob extends BcsJob implements JavaCall {
	AtomicBoolean running = new AtomicBoolean(false);

	//Portafolios por invocacion a GenerarCartola.logica(). El retenedor dominante no es el PDF sino
	//ServiceResultSet (todas las filas de todos los reportes del lote), por eso el valor es bajo.
	//Si una nomina vuelve a agotar el heap, este es el numero a bajar.
	private static final int TAMANO_LOTE_CARTOLAS         = 1000;
	//Filas por executeBatch al persistir cartolas: acota el buffer de blobs del driver JDBC.
	private static final int TAMANO_CHUNK_UPDATE_CARTOLA  = 100;

	private final Log log = new Log("NOR.GenerarCartolasJob");

	private final String SQL_SelectNomina = loadResourceToString("cl/bcs/normativa/cartola/jobs/sql/GenerarCartolasJob_SEL_Nomina.sql");
	private final String SQL_SelectPortafolioNomina = loadResourceToString("cl/bcs/normativa/cartola/jobs/sql/GenerarCartolasJob_SEL_PortafolioNomina.sql");
	private final String SQL_InsertCartola = loadResourceToString("cl/bcs/normativa/cartola/jobs/sql/GenerarCartolasJob_INS_IngresarCartola.sql");
	private final String SQL_UpdateCartola = loadResourceToString("cl/bcs/normativa/cartola/jobs/sql/GenerarCartolasJob_UPD_ActualizarCartola.sql");
	private final String SQL_SelectInformacionNotificaciones = loadResourceToString("cl/bcs/normativa/cartola/jobs/sql/GenerarCartolasJob_SEL_NotificacionCliente.sql");
	private final String SQL_Select_InfoCliente = loadResourceToString("cl/bcs/normativa/cartola/jobs/sql/GenerarCartolasJob_SelectInfoClientes.sql");
	private final String SQL_Update_CampoArchivoTxtNomina = loadResourceToString("cl/bcs/normativa/cartola/jobs/sql/GenerarCartolasJob_UpdateCampoClienteNomina.sql");
	private final String SQL_Select_ListaCorreos = loadResourceToString("cl/bcs/normativa/cartola/jobs/sql/GenerarCartolasJob_ListaCorreos.sql");
	private final String SQL_Select_PlantillaCorreo = loadResourceToString("cl/bcs/normativa/cartola/jobs/sql/GenerarCartolasJob_PlantillaCorreo.sql");

	public static class GenerarColumnaArchivoTxt {
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public String NombreCompleto;
		public String CodigoEjecutivo;
		public String Correo;
		public String Direccion;
		public String Comuna;
		public String Ciudad;
		public String Region;
		public String NombrePais;
	}
	
	public static class UpdateColumnaArchivoTxt {
		public Long IdNomina;
		public String ArchivoTxt;
	}
	
	public static class ObjNomina {

		public Long					IdNomina;
		public Long					IdEmpresa;
		public Long					IdUsuario;
		public Long					IdPlantilla;
		public String 				CartolaConsolidada;
		
		public java.sql.Date		FechaDesde;
		public java.sql.Date		FechaHasta;
		public java.sql.Timestamp	FechaProgramadaProceso;
		public String				Periodo;
		public String				TipoProceso;
		public String				Comentario;
		
		public String				Certificada;
		public Long					IdUsuarioCertificador;
		public String				NombreUsuarioCertificador; 		
		public String				NotaCertificacion;		
		public java.sql.Date		FechaCertificacion;
		public java.sql.Timestamp	FechaInicioProceso;		
		public java.sql.Timestamp	FechaTerminoProceso;
		public String				EstadoProceso;
		public String				MensajeProceso;
		
		public String Ano;
	    public String Mes;
	    public String IncluirPortafoliosConSaldo;
	    public String IncluirPortafoliosConCustodia;
	    public String IncluirPortafoliosConMovimientoCuentaInversion;
	    public String IncluirCategoriaInversionista;
	    public String CategoriaInversionista;
	    public String IncluirTipoPersona;
	    public String TipoPersona;
	    public String IncluirTipoCliente;
	    public String TipoCliente;
	    public String IncluirPortafoliosExcluidos;
	    public String EnvioAutomatico;
	    public String ExcluirPortafoliosIncluidosOtraNomina;
	    public String ExcluirOperadoresDirectos;
	    public String OpcionProceso;
		public Long   IdNominaPadre;
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	};
	
	public static class ObjNotificacion {
		public Long					IdSecuencia;
		public Long					IdPersona;
		public Long					IdCliente;
		public Long					IdPortafolio;
		public String				NombreCompleto;
		public String				IdTipoCliente;
		public String				DI;
		public String				TipoDoc;
		public String				TelCliente;
		public Long					IdDireccionElectronica;
		public String				Correo;
		public String				RazonCorredora;
		public String				DireccionCorredora;
		public String				RutCorredora;
		public String				TelefonoCorredora;
		public String				Agente;
				
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjPortafoliosValidos {
		public Long	IdCliente;
		public Long	IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjCartola {
		public Long		IdNomina;
		public Long		IdCartola;
		public Long		IdCliente;
		public Long		IdPortafolio;
		public String 	MensajeProceso;
		public String 	EstadoProceso;
		public String 	Notificar;
		public String 	Reprocesar;
		public byte[] 	CartolaEncoded;
		public String	NombreArchivo;
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjCartolaGenerada {
		public Long		IdNomina;
		public Long		IdCartola;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjConnectJenkins {
		public String server;
		public String username;
		public String userToken;
	};
	
	public static class ListaCorreos {
		public String ListaCorreos;
		public String TipoProceso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class PlantillaCorreo {
		public String Plantilla;
		public String Asunto;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica (con,false);
	}
	
	public boolean logica(Connection con,boolean debugMode) throws Exception {
		running.set(true);
		SQL sql = new SQL();	
		try {
			HashMap<String,Object> parms =new HashMap<String,Object>();
			//00 RECUPERAR PARAMETROS
			
			if (!debugMode) {
				parms.put("IdNomina", this.getParam("IdNomina"));
			}
			else {
				parms.put("IdNomina", 913034L);
			}	
			//01 RECUPERAR INFORMACION NOMINAS
			HashMap<String,Object> paramsNomina = new HashMap<String,Object>();			
			paramsNomina.put("IdNomina", parms.get("IdNomina"));
			//cambios en la consulta de nominas
			List<ObjNomina> sqlNomina = sql.query(con, SQL_SelectNomina, paramsNomina, ObjNomina.class);
			
			ObjNomina infoNomina = new ObjNomina();
			
			if(sqlNomina.size() > 0){
				infoNomina = sqlNomina.get(0);
			}
			//02 EJECUTAR PROCESO
			if (!"ING".equals(infoNomina.EstadoProceso) ||  sqlNomina.size() == 0){
				return false;
			}
			if ("NORMAL".equals(infoNomina.TipoProceso)){
				List<ObjPortafoliosValidos> PortafoliosValidos = IngresarCartola(infoNomina,con);
				GenerarCartola (infoNomina, PortafoliosValidos, con);
			}
			if ("RECURRENTE".equals(infoNomina.TipoProceso)){
				List<ObjPortafoliosValidos> PortafoliosValidos = IngresarCartola(infoNomina,con);
				GenerarCartola (infoNomina, PortafoliosValidos, con);
			}
			if ("PROGRAMADO".equals(infoNomina.TipoProceso)){
				List<ObjPortafoliosValidos> PortafoliosValidos = IngresarCartola(infoNomina,con);
				GenerarCartola (infoNomina, PortafoliosValidos, con);
			}
			if("REPROCESO".equals(infoNomina.TipoProceso)){
				HashMap<String,Object> params = new HashMap<String,Object>();
				params.put("IdNomina", infoNomina.IdNomina);
				List<ObjPortafoliosValidos> PortafoliosValidos = sql.query(con, SQL_SelectPortafolioNomina, params, ObjPortafoliosValidos.class);
				GenerarCartola (infoNomina, PortafoliosValidos, con);
			}
			
			
		}
		catch (Exception e) {
			log.write(e);
			throw e;
		}
		return true;
	}
	
	public List<ObjPortafoliosValidos> IngresarCartola (ObjNomina nomina, Connection con) throws Exception{
		List<ObjPortafoliosValidos> portafoliosValidos = new ArrayList<ObjPortafoliosValidos>();
		SQL sql = new SQL();
		List<ObjCartola> paramsInsert = new ArrayList<ObjCartola>();
		try {
			GestionarClientes gestionarClientes = new GestionarClientes();
			EntradaGestionarClientes entradaGestionarClientes = new EntradaGestionarClientes();
			entradaGestionarClientes.p_IdEmpresa = nomina.IdEmpresa;
			entradaGestionarClientes.p_IncluirTipoPersona =  nomina.IncluirTipoPersona;
			entradaGestionarClientes.p_TipoPersona =  nomina.TipoPersona;
			entradaGestionarClientes.p_IncluirCategoriaInversionista =  nomina.IncluirCategoriaInversionista;
			entradaGestionarClientes.p_CategoriaInversionista =  nomina.CategoriaInversionista;
			entradaGestionarClientes.p_IncluirTipoCliente =  nomina.IncluirTipoCliente;
			entradaGestionarClientes.p_TipoCliente =  nomina.TipoCliente;
			entradaGestionarClientes.p_IncluirPortafoliosConSaldo = nomina.IncluirPortafoliosConSaldo;
			entradaGestionarClientes.p_IncluirPortafoliosConCustodia = nomina.IncluirPortafoliosConCustodia;
			entradaGestionarClientes.p_IncluirPortafoliosConMovimientoCuentaInversion = nomina.IncluirPortafoliosConMovimientoCuentaInversion;
			entradaGestionarClientes.p_ExcluirOperadoresDirectos = nomina.ExcluirOperadoresDirectos;
			entradaGestionarClientes.p_ExcluirPortafoliosIncluidosOtraNomina = nomina.ExcluirPortafoliosIncluidosOtraNomina;
			entradaGestionarClientes.p_IncluirPortafoliosExcluidos = nomina.IncluirPortafoliosExcluidos;
			entradaGestionarClientes.p_FechaDesde = Fecha.stringToSqlDate(nomina.FechaDesde);
			entradaGestionarClientes.p_FechaHasta = Fecha.stringToSqlDate(nomina.FechaHasta);
			entradaGestionarClientes.p_Periodo = nomina.Periodo;
			entradaGestionarClientes.p_IdUsuario = nomina.IdUsuario;
			entradaGestionarClientes.p_IdModulo = "NOR";
			
			SalidaGestionarClientes salidaGestionarClientes = gestionarClientes.logica(con, entradaGestionarClientes);
			
			for (GestionarClientes.PortafolioValido portafolioValido : salidaGestionarClientes.P_OUT_PORTAFOLIOS){
				ObjCartola cartola = new ObjCartola();
				cartola.IdNomina		= nomina.IdNomina;
				cartola.IdCliente		= portafolioValido.IdCliente;
				cartola.IdPortafolio 	= portafolioValido.IdPortafolio;
				cartola.EstadoProceso	= "EPR";
				cartola.MensajeProceso 	= "";
				paramsInsert.add(cartola);
				
				
				
				ObjPortafoliosValidos portValido = new ObjPortafoliosValidos();
				portValido = Utils.copyPojo(portafolioValido, portValido);
				portafoliosValidos.add(portValido);
			}		
			sql.batch(con, SQL_InsertCartola, paramsInsert, ObjCartola.class);
			
		} 
		catch(Exception e){
			log.write(e);
			throw e;
		};
		
		return portafoliosValidos;
	};
	
	public List<ObjPortafoliosValidos> ActualizarCartola (List<ObjCartola> Cartola, Connection con) throws Exception{
		List<ObjPortafoliosValidos> sqlPortafoliosValidos = new ArrayList<ObjPortafoliosValidos>();
		SQL sql = new SQL();
		//SQL.batch no trocea internamente: hace addBatch() de todas las filas y recien al final
		//executeBatch(), asi que el driver bufferiza todos los blobs de PDF a la vez.
		
		try {
			for (int desde = 0; desde < Cartola.size(); desde += TAMANO_CHUNK_UPDATE_CARTOLA) {
				int hasta = Math.min(desde + TAMANO_CHUNK_UPDATE_CARTOLA, Cartola.size());
				sql.batch(con, SQL_UpdateCartola, Cartola.subList(desde, hasta), ObjCartola.class);
				con.commit();
			}
		}
		catch(Exception e){
			log.write(e);
			throw e;
		};

		return sqlPortafoliosValidos;
	};
	
	private byte[] comprimirData (byte[] data) throws IOException {
		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
		try {
			deflater.setInput(data);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
			deflater.finish();

			byte[] buffer = new byte[1024];
			while (!deflater.finished()) {
				int count = deflater.deflate(buffer);
				outputStream.write(buffer, 0, count);
			}
			outputStream.close();

			return outputStream.toByteArray();
		} finally {
			// Sin end() los buffers nativos de zlib no se liberan hasta que el GC finalice el
			// Deflater: una fuga de memoria off-heap por cada cartola comprimida.
			deflater.end();
		}
	}

	/**
	 * Parte la nomina en lotes de a lo mas tamanoLote portafolios, cortando siempre en frontera de
	 * cliente: GenerarCartola deduplica las cartolas consolidadas con un registro local a cada
	 * invocacion (clientesProcesados), asi que un cliente repartido entre dos lotes recibiria su
	 * cartola consolidada dos veces. Un cliente con mas portafolios que tamanoLote queda solo en
	 * su propio lote, aunque lo exceda.
	 */
	private List<List<GenerarCartola.GenerarCartolaEntrada.objDatosEntrada>> dividirEnLotesPorCliente(
			List<GenerarCartola.GenerarCartolaEntrada.objDatosEntrada> datosEntrada, int tamanoLote) {

		// Agrupa por cliente preservando el orden de aparicion original.
		LinkedHashMap<Long, List<GenerarCartola.GenerarCartolaEntrada.objDatosEntrada>> porCliente = new LinkedHashMap<>();
		for (GenerarCartola.GenerarCartolaEntrada.objDatosEntrada datos : datosEntrada) {
			List<GenerarCartola.GenerarCartolaEntrada.objDatosEntrada> delCliente = porCliente.get(datos.IdCliente);
			if (delCliente == null) {
				delCliente = new ArrayList<>();
				porCliente.put(datos.IdCliente, delCliente);
			}
			delCliente.add(datos);
		}

		List<List<GenerarCartola.GenerarCartolaEntrada.objDatosEntrada>> lotes = new ArrayList<>();
		List<GenerarCartola.GenerarCartolaEntrada.objDatosEntrada> loteActual = new ArrayList<>();
		for (List<GenerarCartola.GenerarCartolaEntrada.objDatosEntrada> delCliente : porCliente.values()) {
			if (!loteActual.isEmpty() && loteActual.size() + delCliente.size() > tamanoLote) {
				lotes.add(loteActual);
				loteActual = new ArrayList<>();
			}
			loteActual.addAll(delCliente);
		}
		if (!loteActual.isEmpty())
			lotes.add(loteActual);

		return lotes;
	}


	public boolean GenerarCartola (ObjNomina InfoNomina, List<ObjPortafoliosValidos> PortafoliosValidos, Connection con) throws Exception{
		SQL sql = new SQL();
		List<Long> idPortafolios = new ArrayList<>();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, InfoNomina.IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		List<GenerarCartola.GenerarCartolaEntrada.objDatosEntrada> datosEntrada = new ArrayList<GenerarCartola.GenerarCartolaEntrada.objDatosEntrada>();
		
		for (ObjPortafoliosValidos portafolioValido : PortafoliosValidos){
			GenerarCartola.GenerarCartolaEntrada.objDatosEntrada datos = new GenerarCartola.GenerarCartolaEntrada.objDatosEntrada();
			Utils.copyPojo(portafolioValido, datos);		
			datosEntrada.add(datos);
		};
		
		GestionarNomina gestionarNomina = new GestionarNomina();
		String fechaHastaFix = gestionarNomina.validarFecha(con,InfoNomina.FechaHasta, InfoNomina.IdEmpresa);
		
		
		//SE INICIA PROCESO
		{
			//Si existe nomina, se cambia el estado de ingresado a "en proceso"
			GestionarNomina.GestionarNominaEntrada GNT = new GestionarNomina.GestionarNominaEntrada();
			GNT.p_IdEmpresa		= InfoNomina.IdEmpresa;
			GNT.p_IdUsuario		= InfoNomina.IdUsuario;
			GNT.p_IdModulo		= "NOR";
			GNT.p_AccionNomina	= "INICIAR";
			GNT.p_IdNomina		= InfoNomina.IdNomina;
			GNT.p_TipoProceso	= InfoNomina.TipoProceso;			
			GNT.p_FechaDesde	= InfoNomina.FechaDesde.toString();
			GNT.p_FechaHasta	= fechaHastaFix;
			GNT.p_Comentario	= "";			
			GestionarNomina GNOM = new GestionarNomina();
			GNOM.logica(con, GNT);
		}
		
	
		// La nomina se genera por lotes en vez de una sola invocacion con todos los portafolios.
		// GenerarCartola.logica() retiene en heap todo lo que produce hasta su return (ServiceResultSet,
		// PDFs, salidas), asi que el pico de memoria escalaba con el tamano de la nomina y reventaba
		// el heap. Segmentando, el pico pasa a depender del lote y no de la nomina.
		final List<List<GenerarCartola.GenerarCartolaEntrada.objDatosEntrada>> lotes =
				dividirEnLotesPorCliente(datosEntrada, TAMANO_LOTE_CARTOLAS);

		log.write("Nomina " + InfoNomina.IdNomina + ": " + datosEntrada.size()
				+ " portafolios en " + lotes.size() + " lote(s) de hasta " + TAMANO_LOTE_CARTOLAS);

		long erroresTotales = 0L;
		List<PlataformaUtils.MensajeSalida> mensajesError = new ArrayList<PlataformaUtils.MensajeSalida>();

		for (int nroLote = 0; nroLote < lotes.size(); nroLote++) {
			List<GenerarCartola.GenerarCartolaEntrada.objDatosEntrada> lote = lotes.get(nroLote);

			GenerarCartola.GenerarCartolaEntrada VIE = new GenerarCartola.GenerarCartolaEntrada();
			VIE.p_IdEmpresa		= InfoNomina.IdEmpresa;
			VIE.p_IdUsuario		= InfoNomina.IdUsuario;
			VIE.p_IdModulo		= "NOR";
			VIE.p_FechaDesde	= InfoNomina.FechaDesde.toString();
			VIE.p_FechaHasta	= fechaHastaFix;
			VIE.p_IdNomina		= InfoNomina.IdNomina;
			VIE.p_CartolaConsolidada	= InfoNomina.CartolaConsolidada;
			VIE.p_Clientes		= lote;
			GenerarCartola GCE = new GenerarCartola();
			GenerarCartola.GenerarCartolaSalida VIS = GCE.logica(con, VIE);

			List<ObjCartola> cartolas = new ArrayList<ObjCartola>();
			if(VIS.P_OUT_ERRORESCONTADOR.equals(0L)){
				for(GenerarCartola.ObjCartolaSalida outCartola : VIS.P_OUT_CARTOLAS){
					idPortafolios.add(outCartola.IdPortafolio);
					ObjCartola cartola = new ObjCartola();
					// Se considera generada solo si vienen los bytes del PDF: es lo unico que
					// comprimirData puede persistir. CartolaEncoded (Base64) ya no se puebla
					// fuera de ONLINE porque pesa 1.33x el PDF y aca no se usa.
					if(outCartola.CartolaEncodedByte != null)
					{
						cartola.EstadoProceso = "TER";
						// almacena bytes comprimidos sin cifrar con Base64
						// el metodo OFFLINE de GestionarNomina.java se encarga de descomprimir
						// y cifrar en Base64 la salida de CartolaEncoded
						cartola.CartolaEncoded = comprimirData(outCartola.CartolaEncodedByte);
						cartola.NombreArchivo = outCartola.NombreArchivo;
					}else{
						cartola.EstadoProceso = "ERR";
					}
					cartola.IdNomina 		= InfoNomina.IdNomina;
					cartola.IdCliente		= outCartola.IdCliente;
					cartola.IdPortafolio	= outCartola.IdPortafolio;
					cartola.MensajeProceso	= "";
					cartola.Notificar		= "";
					cartola.Reprocesar		= "";
					cartolas.add(cartola);
					// Suelta el PDF de este registro: ya esta comprimido dentro de 'cartola'.
					outCartola.CartolaEncodedByte = null;
					outCartola.CartolaEncoded = null;
				}
			} else {
				erroresTotales += VIS.P_OUT_ERRORESCONTADOR;
				mensajesError.addAll(VIS.P_OUT_MENSAJES);
			}

			try {
				if (!cartolas.isEmpty())
					ActualizarCartola(cartolas, con);
				cartolas.clear();
			}catch(Exception e) {
				throw e;

			}

			// Suelta la salida completa del lote antes de pedir el siguiente.
			VIS = null;
			GCE = null;
			VIE = null;

			log.write("Nomina " + InfoNomina.IdNomina + ": lote " + (nroLote + 1) + "/" + lotes.size()
					+ " listo (" + lote.size() + " portafolios, acumulado " + idPortafolios.size()
					+ "), heap libre " + (Runtime.getRuntime().freeMemory() / (1024 * 1024)) + " MB");

			if (erroresTotales > 0L)
				break;
		}

		// Fuera del loop de lotes: sobrescribe la columna ArchivoTxt completa de la nomina, asi que
		// llamarlo por lote haria que cada uno pisara al anterior.
		if ( erroresTotales == 0L ){
			this.updateCampoArchivoTxtNomina(con, idPortafolios, InfoNomina.IdNomina);
		}

		if ( erroresTotales == 0L ){
			GestionarNomina.GestionarNominaEntrada GNT = new GestionarNomina.GestionarNominaEntrada();
			GNT.p_IdEmpresa		= InfoNomina.IdEmpresa;
			GNT.p_IdUsuario		= InfoNomina.IdUsuario;
			GNT.p_IdModulo		= "NOR";
			GNT.p_AccionNomina	= "TERMINAR";
			GNT.p_IdNomina		= InfoNomina.IdNomina;
			GNT.p_TipoProceso	= InfoNomina.TipoProceso;			
			GNT.p_FechaDesde	= InfoNomina.FechaDesde.toString();
			GNT.p_FechaHasta	= fechaHastaFix;
			GNT.p_Comentario	= "";			
			GestionarNomina GNOM = new GestionarNomina();			
			GNOM.logica(con, GNT);
			//NOTIFICACIONES
			//La consulta de notificaciones y la lista 'IN' que la acumulaba se eliminaron: su unico
			//consumidor es el bloque AgregarEvento comentado mas abajo, asi que se leia toda la nomina
			//desde BD para retenerla en memoria y descartarla. Si se reactiva ese bloque, hay que
			//recuperar la consulta cargandola por lote junto a la generacion, no de una sola vez.

			// Envio Automatico
			if (InfoNomina.EnvioAutomatico.equals("S")) {
				List<ListaCorreos> ListaC = new ArrayList<>();
				HashMap<String,Object> params =new HashMap<String,Object>();
				params.put("TipoProceso","GENERAR_CARTOLA_ENVIO_AUTOMATICO");
				params.put("NombreLista","NOTIFICAR");
				ListaC = sql.query(con, SQL_Select_ListaCorreos, params, ListaCorreos.class);
				if (ListaC.size()>0)
					enviarFTP(con,InfoNomina,ListaC.get(0).ListaCorreos,"GENERAR_CARTOLA_ENVIO_AUTOMATICO");
			}
			
			/*AgregarEvento.ServiceEntrada notifIn = new AgregarEvento.ServiceEntrada();
			notifIn.P_IDEMPRESA = InfoNomina.IdEmpresa;
			notifIn.P_IDUSUARIO = InfoNomina.IdUsuario;
			notifIn.P_IDMODULO 	= "NOR";
			
			for (GenerarCartola.ObjCartolaSalida infoCartola : VIS.P_OUT_CARTOLAS){
				ObjNotificacion InfoNotificacion = (ObjNotificacion) InformacionNotificacion.get( (infoCartola.IdCliente + infoCartola.IdPortafolio) );
				if (InfoNotificacion == null) continue;
				
				ObjEVENTOS evento = new ObjEVENTOS();
				evento.CodigoTipoEvento 		= "GENERACION_CARTOLA";
				evento.FechaCreacionSistema 	= fechaSistema;
				evento.IdPersonaDestinatario 	= InfoNotificacion.IdPersona;
				evento.IdPersonaRemitente 		= 0L;
				evento.IdPortafolio 			= infoCartola.IdPortafolio;
				evento.IdSecuencia 				= InfoNotificacion.IdSecuencia;
				notifIn.P_EVENTOS.add(evento);
				
				
				ObjIDDIRECCIONES dir = new ObjIDDIRECCIONES();
				dir.IdDireccion 		= InfoNotificacion.IdDireccionElectronica;
				dir.IdSecuencia 		= InfoNotificacion.IdSecuencia;
				dir.TipoDestinatario 	= "TO";
				notifIn.P_IDDIRECCIONES.add(dir);
				
				notifIn.addValor(InfoNotificacion.IdSecuencia, "IdSecuencia", InfoNotificacion.IdSecuencia.toString())
						//cabecera
				   		.addValor( InfoNotificacion.IdSecuencia, "CLIENTE", InfoNotificacion.NombreCompleto)
				   		.addValor( InfoNotificacion.IdSecuencia, "DOCUMENTOIDENTIDAD", InfoNotificacion.DI)
				   		.addValor( InfoNotificacion.IdSecuencia, "TIPODOCUMENTOIDENTIDAD", InfoNotificacion.TipoDoc)
				   		.addValor( InfoNotificacion.IdSecuencia, "CORREO",InfoNotificacion.Correo)
				   		.addValor( InfoNotificacion.IdSecuencia, "TELEFONO", InfoNotificacion.TelCliente)
				   		.addValor( InfoNotificacion.IdSecuencia, "AGENTE", InfoNotificacion.Agente)
				   		//detalle
				   		.addValor( InfoNotificacion.IdSecuencia, "TIPOCLIENTE", InfoNotificacion.IdTipoCliente)
				   		.addValor( InfoNotificacion.IdSecuencia, "PERIODO", "")
				   		//info corredora
				   		.addValor( InfoNotificacion.IdSecuencia, "DIRECCIONCORREDORA", InfoNotificacion.DireccionCorredora)
				   		.addValor( InfoNotificacion.IdSecuencia, "RUTCORREDORA", InfoNotificacion.RutCorredora)
				   		.addValor( InfoNotificacion.IdSecuencia, "CORREDORA", InfoNotificacion.RazonCorredora)
				   		.addValor( InfoNotificacion.IdSecuencia, "ATENCIONCLIENTE", InfoNotificacion.TelefonoCorredora);
				
			};
			
			if (notifIn.P_EVENTOS.size() > 0) {
			    AgregarEvento notif = new AgregarEvento();
			    notif.logica(con, notifIn);
			}*/
			
		} else {
			GestionarNomina.GestionarNominaEntrada GNE = new GestionarNomina.GestionarNominaEntrada();
			GNE.p_IdEmpresa		= InfoNomina.IdEmpresa;
			GNE.p_IdUsuario		= InfoNomina.IdUsuario;
			GNE.p_IdModulo		= "NOR";
			GNE.p_AccionNomina	= "ERROR";
			GNE.p_IdNomina		= InfoNomina.IdNomina;
			GNE.p_TipoProceso	= InfoNomina.TipoProceso;			
			GNE.p_FechaDesde	= InfoNomina.FechaDesde.toString();
			GNE.p_FechaHasta	= fechaHastaFix;
			GNE.p_Comentario	= "";			
			GestionarNomina GNOM = new GestionarNomina();
			GNOM.logica(con, GNE);
			
			if (InfoNomina.EnvioAutomatico.equals("S"))
				notificarError(con, "GENERACION_AUTOMATICA_CARTOLA", mensajesError.toString());
		};
		
		return true;
	};
	
	private void notificarError(Connection con, String proceso ,String mensaje) throws Exception {
		SQL sql = new SQL();
		HashMap<String,Object> params =new HashMap<String,Object>();
		List<ListaCorreos> listaCorreo = new ArrayList<>();
		listaCorreo = sql.query(con, SQL_Select_ListaCorreos, params, ListaCorreos.class);
		List<String> para = Arrays.asList(listaCorreo.get(0).ListaCorreos.toLowerCase().split(";"));
		
		params.put("plantilla", "NOTIFICAR_GENERACION_ENVIO_AUT_CARTOLA");
		List<PlantillaCorreo> plantillas = sql.query(con, SQL_Select_PlantillaCorreo, params, PlantillaCorreo.class);
		
		String plantilla = plantillas.get(0).Plantilla
				.replace("${data.NOTIFICAR_GENERACION_ENVIO_AUT_CARTOLA.NOMBRE_PROCESO!}", proceso)
				.replace("${data.NOTIFICAR_GENERACION_ENVIO_AUT_CARTOLA.MENSAJE_CONTENIDO!}", mensaje);
		String asunto = plantillas.get(0).Asunto;
		
	    List<Email> emails = new ArrayList<Email>();
	    Email email = new Email();
	    email.IdEnvio = 0L;
	    email.Asunto = "[ERROR] " + asunto;
	    email.Remitente = "emisor";
	    email.MensajeCuerpo = plantilla;
	    emails.add(email);
	    
	    List<Destinatario> destinatarios = new ArrayList<Destinatario>();
	    for (String destinatario : para) {
	      Destinatario dest = new Destinatario();
	      dest.IdEnvio = 0L;
	      dest.Destinatario = destinatario;
	      dest.TipoDestinatario = "TO";
	      destinatarios.add(dest);
	    }
	    
		AgregarEmail agregarEmail = new AgregarEmail();
		ServiceEntrada entradaAgregarEmail = new ServiceEntrada();
	    entradaAgregarEmail.emails = emails;
	    entradaAgregarEmail.destinatarios = destinatarios;
	    agregarEmail.logica(con, entradaAgregarEmail);
	}
	
	private void enviarFTP(Connection con, ObjNomina InfoNomina, String Lista, String tarea) throws Exception {
		
		EntradaEjecutarTareaJenkins inJenkins = new EntradaEjecutarTareaJenkins();
		inJenkins.p_IdModulo = "PLA";
		inJenkins.p_IdUsuario = 0L;
		inJenkins.p_IdEmpresa = InfoNomina.IdEmpresa;
		inJenkins.p_TipoTarea = tarea;
		inJenkins.p_Parametros.add(new ParametroJenkins("SecuenciaCartola",InfoNomina.IdNomina.toString()));
		inJenkins.p_Parametros.add(new ParametroJenkins("GeneraTxt","false"));
		inJenkins.p_Parametros.add(new ParametroJenkins("ListaCorreosNotificacion",Lista));
		
		EjecutarTareaJenkins jenkins = new EjecutarTareaJenkins();
		SalidaEjecutarTareaJenkins outJenkins = jenkins.logica(con, inJenkins);
		
		if (outJenkins.P_OUT_ERRORESCONTADOR>0) {
			notificarError(con,"ENVIO_AUTOMATICO_CARTOLA","Id: "+InfoNomina.IdNomina+", Mensaje: "+outJenkins.P_OUT_MENSAJES);
		}
		else if (outJenkins.CodigoHttp == 401) {
			String mnsj = Diccionario.getMensaje(con, InfoNomina.IdEmpresa, "UI_ERROR_401").msg();
			notificarError(con,"ENVIO_AUTOMATICO_CARTOLA","Id: "+InfoNomina.IdNomina+", HTTP Error "+outJenkins.CodigoHttp+" "+outJenkins.MensajeHttp+", Mensaje: "+mnsj);
		}
		else if (outJenkins.CodigoHttp == 403) {
			String mnsj = Diccionario.getMensaje(con, InfoNomina.IdEmpresa, "UI_ERROR_403").msg();
			notificarError(con,"ENVIO_AUTOMATICO_CARTOLA","Id: "+InfoNomina.IdNomina+", HTTP Error "+outJenkins.CodigoHttp+" "+outJenkins.MensajeHttp+", Mensaje: "+mnsj);
		}
		else if (outJenkins.CodigoHttp == 404) {
			String mnsj = Diccionario.getMensaje(con, InfoNomina.IdEmpresa, "UI_ERROR_404").msg();
			notificarError(con,"ENVIO_AUTOMATICO_CARTOLA","Id: "+InfoNomina.IdNomina+", HTTP Error "+outJenkins.CodigoHttp+" "+outJenkins.MensajeHttp+", Mensaje: "+mnsj);
		}
		else if(outJenkins.CodigoHttp != 201){
			notificarError(con,"ENVIO_AUTOMATICO_CARTOLA","Id: "+InfoNomina.IdNomina+", HTTP Error "+outJenkins.CodigoHttp+" "+outJenkins.MensajeHttp);
		}
	}
	
	private void updateCampoArchivoTxtNomina(Connection con, List<Long> idPortafolios, Long idNomina) throws SQLException {
		
		SQL sql 								= new SQL();
		HashMap<String,Object> paramsPortafolio = new HashMap<>(1);
		StringBuilder archivoTxt 				= new StringBuilder();
		
		paramsPortafolio.put("IdPortafolio", idPortafolios);
		List<GenerarColumnaArchivoTxt> clientes = sql.query(con, SQL_Select_InfoCliente,paramsPortafolio,GenerarColumnaArchivoTxt.class);
		clientes.forEach(d -> {
			archivoTxt.append(d.DocumentoIdentidad.trim() + ";");
			archivoTxt.append(d.CodigoPortafolio.trim() + ";");
			archivoTxt.append(d.NombreCompleto.trim() + ";");
			archivoTxt.append(d.CodigoEjecutivo.trim() + ";");
			archivoTxt.append("S;");
			archivoTxt.append("Procesado OK;");
			archivoTxt.append(d.Correo.trim() + ";");
			archivoTxt.append(d.Direccion.trim() + ";");
			archivoTxt.append(d.Comuna.trim() + ";");
			archivoTxt.append(d.Ciudad.trim() + ";");
			archivoTxt.append(d.Region.trim() + ";");
			archivoTxt.append(d.NombrePais.trim() + ";\r\n");
		});
		
		List<UpdateColumnaArchivoTxt> paramsNomina 	= new ArrayList<UpdateColumnaArchivoTxt>(1);
		UpdateColumnaArchivoTxt ColumnaArchivoTxt 	= new UpdateColumnaArchivoTxt();
		ColumnaArchivoTxt.IdNomina 					= idNomina;
		ColumnaArchivoTxt.ArchivoTxt 				= archivoTxt.toString();
		paramsNomina.add(ColumnaArchivoTxt);
		
		sql.batch(con, SQL_Update_CampoArchivoTxtNomina, paramsNomina, UpdateColumnaArchivoTxt.class);

	}
	
	public static void main(String[] args) {
    	GenerarCartolasJob p1 = new GenerarCartolasJob();
		try {
			//Connection con = PlataformaUtils.getDESAConnection();
			Connection con = PlataformaUtils.getPropertiesConnection("mbi","connections.properties");
			p1.logica( con, true);
			//con.commit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	@Override
	public JsonObject call(Connection con, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onError(Throwable t) {
		System.out.println(" ****** ERROR  *******");  
        t.printStackTrace();	
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub		
	}

};