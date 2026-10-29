package cl.bcs.APV;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaMasivaRecaudacion implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CargaMasivaRecaudacion.class);
	
	private final String SQL_INSERT_CARGA_ARCHIVO = loadResourceToString("cl/bcs/APV/sql/CargaMasivaRecaudacion_CargaMasivaArchivo_Insert.sql");
	private final String SQL_INSERT_CARGA_DETALLE = loadResourceToString("cl/bcs/APV/sql/CargaMasivaRecaudacion_CargaMasivaArchivoDetalle_Insert.sql");		
	private final String SQL_SELECT_CARGA_DETALLE = loadResourceToString("cl/bcs/APV/sql/CargaMasivaRecaudacion_CargaMasivaArchivoDetalle_Select.sql");
	private final String SQL_CREATE_TABLA_TEMPORAL_CARGA_MASIVA  = loadResourceToString("cl/bcs/APV/sql/CargaMasivaRecaudacion_CargaMasivaRecaudacion_CargaArchivo_CreateTable.sql");
	private final String SQL_INSERT_TABLA_TEMPORAL_CARGA_MASIVA = loadResourceToString("cl/bcs/APV/sql/CargaMasivaRecaudacion_CargaMasivaRecaudacion_CargaArchivo_Insert.sql");
	private final String SQL_DROP_TABLA_TEMPORAL_CARGA_MASIVA   = loadResourceToString("cl/bcs/APV/sql/CargaMasivaRecaudacion_CargaMasivaRecaudacion_CargaArchivo_Drop.sql");
	
	
	// SERVICIO ENTRADA
	public static class EntradaCargaMasivaRecaudacion extends BcsPojo{

		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_NombreCarga;
		public String p_Origen;
		public List<CargaArchivo> p_Archivo;
		public List<ArchivoRecaudacion> p_Parametros;

		public EntradaCargaMasivaRecaudacion() {
			p_Parametros = new ArrayList<ArchivoRecaudacion>();
			p_Archivo = new ArrayList<CargaArchivo>();
		}

	}
	
	// SERVICIO SALIDA
	public static class SalidaCargaMasivaRecaudacion extends BcsPojo{
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;

		public SalidaCargaMasivaRecaudacion() {
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
	};

	
	public static class ArchivoRecaudacion extends BcsPojo{
		public Long IdCargaMasiva;
		public String Fecha ;
		public String TipoDeposito ;
		public Long FolioCartola ;
		public Long FolioLineaCartola;
		public String Banco ;
		public String Cuenta;
		public Long Monto ;
		public String PeriodoPago;
		public String TipoPago;
		public String RUTEmpleador;
		public String RUTCliente;
		public String TipodeAfiliado;
		public String Regimen;
		public String EstadoRendicion;
		public String EstadoProceso;
	
		
		public ArchivoRecaudacion() {
			this.IdCargaMasiva=0L;
			this.EstadoProceso="";
			this.EstadoRendicion="";
		}
		
	}
	
	public static class DetalleRecaudacion extends BcsPojo{	
		public Long IdCargaMasiva;
		public Long FolioCartola ;
		public Long FolioLineaCartola ;
						
	}
	
	public static class CargaArchivo extends BcsPojo {
		public Long IdEmpresa;
		public Long IdCargaMasiva;
		public String NombreCargaMasiva;
		public Long CantidadRegistros;
		public Long IdUsuario;
		public int IdTipoCarga;
		public String Descripcion;
		public String FechaCarga;
		public String IdEstado;

	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCargaMasivaRecaudacion jin = gson.fromJson(in.toString(), EntradaCargaMasivaRecaudacion.class);
		SalidaCargaMasivaRecaudacion out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCargaMasivaRecaudacion logica(Connection con, EntradaCargaMasivaRecaudacion in) throws Exception {
		logger.info("INICIO::CargaMasivaRecaudacion ");
		long inicioServicio = System.currentTimeMillis();
		SalidaCargaMasivaRecaudacion salida = new SalidaCargaMasivaRecaudacion();
		
		
		//CARGA ARCHIVO TABLAS TEMPORALES
		this.insertTablaTemporal(in, salida, con);	
				
		//VALIDA QUE NO HAYA CARGA IGUAKL FOLIO - FOLIO LINEA
		if(validaCarga(con, in,salida)) {
			this.guardarCargaMasiva(in, salida, con);				
		}	
		//BORRA TABLA TEMPORAL
		SQL.exec(con, SQL_DROP_TABLA_TEMPORAL_CARGA_MASIVA);
		
		if(salida.P_OUT_ERRORESCONTADOR.compareTo(0L)==0) {
			MensajeSalida mensajeSalida = new MensajeSalida(1L, MensajeSalida.MENSAJE_OK, "CARGA_RECAUDACION_EXITOSA","Carga Completada Exitosamente");					
			salida.P_OUT_MENSAJES.add(mensajeSalida);
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CargaMasivaRecaudacion ", finServicio - inicioServicio);
		return salida;
	}
	
	public void insertTablaTemporal (EntradaCargaMasivaRecaudacion in ,SalidaCargaMasivaRecaudacion out , Connection con) throws Exception{
		logger.debug("INICIO::CargaMasivaRecaudacion.insertTablaTemporal");
		long inicioMetodo = System.currentTimeMillis();
		SQL sql = new SQL();
		
		List<DetalleRecaudacion> listaArchivo = in.p_Parametros.stream().map(det -> {
			DetalleRecaudacion detalle= new DetalleRecaudacion();
			detalle.FolioCartola = det.FolioCartola;
			detalle.FolioLineaCartola = det.FolioLineaCartola;
			return detalle;
		}).collect(Collectors.toList());
		
		SQL.exec( con, SQL_CREATE_TABLA_TEMPORAL_CARGA_MASIVA);		
		sql.batch(con, SQL_INSERT_TABLA_TEMPORAL_CARGA_MASIVA ,listaArchivo, DetalleRecaudacion.class);
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("INICIO::CargaMasivaRecaudacion.insertTablaTemporal" , finMetodo- inicioMetodo);		
	}
	
	
	public boolean validaCarga (Connection con ,EntradaCargaMasivaRecaudacion in ,SalidaCargaMasivaRecaudacion out ) throws Exception {
		logger.debug("INICIO::CargaMasivaRecaudacion.validaCarga");
		long inicioMetodo = System.currentTimeMillis();
		List<DetalleRecaudacion> listaDetalles = new ArrayList<DetalleRecaudacion>();
		SQL sql = new SQL();
		
		listaDetalles = sql.query(con, SQL_SELECT_CARGA_DETALLE, new HashMap<String,Object>(), DetalleRecaudacion.class);
		
		if(!listaDetalles.isEmpty()) {
			listaDetalles.forEach( detalle ->{
				MensajeSalida mensajeSalida = new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "CARGA_RECAUDACION_EXITE_CARGA","Folio Cartola "+detalle.FolioCartola+" y Folio Linea Cartola existen "+detalle.FolioLineaCartola);					
				out.P_OUT_MENSAJES.add(mensajeSalida);
			});
		}
		
		if(!out.P_OUT_MENSAJES.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR=1L;
		}
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::CargaMasivaRecaudacion.validaCarga :" , finMetodo- inicioMetodo);		
		
		return out.P_OUT_MENSAJES.isEmpty();
	}
	
	public void guardarCargaMasiva (EntradaCargaMasivaRecaudacion in ,SalidaCargaMasivaRecaudacion out , Connection con) throws Exception {
		logger.debug("INICIO::CargaMasivaRecaudacion.trace");
		long inicioMetodo = System.currentTimeMillis();
		SQL sql = new SQL();		
		List<Long> idsCargaAchivo = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencia::APV.IdCargaMasiva",(long) in.p_Archivo.size());
		Long idCargaMasiva = idsCargaAchivo.get(0);
		
		
		for (CargaArchivo archivo : in.p_Archivo) {
				archivo.FechaCarga= archivo.FechaCarga.substring(0,10);
				archivo.IdCargaMasiva =idCargaMasiva ;
		}
		
		for (ArchivoRecaudacion detalle : in.p_Parametros) {			
			detalle.IdCargaMasiva= idCargaMasiva;
	    }
		
		
		sql.batch(con, SQL_INSERT_CARGA_ARCHIVO, in.p_Archivo, CargaArchivo.class);
		sql.batch(con, SQL_INSERT_CARGA_DETALLE, in.p_Parametros, ArchivoRecaudacion.class);
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::CargaMasivaRecaudacion.trace", finMetodo- inicioMetodo);		
	}
	
	
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			CargaMasivaRecaudacion CMR = new CargaMasivaRecaudacion();
			try {
				EntradaCargaMasivaRecaudacion Objin = new EntradaCargaMasivaRecaudacion();

				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
				
				String json ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"APV\",\"p_NombreCarga\":\"Carga_4.txt\",\"p_Origen\":\"RECAUDA\",\"p_IdEmpleado\":7889,\"p_Archivo\":[{\"IdEmpresa\":1,\"IdCargaMasiva\":0,\"NombreCargaMasiva\":\"Carga_4.txt\",\"CantidadRegistros\":4,\"IdUsuario\":21,\"IdTipoCarga\":2,\"Descripcion\":\"Carga Recaudacion\",\"FechaCarga\":\"2021-12-07T03:00:00.000Z\",\"IdEstado\":\"VIG\"}],\"p_Parametros\":[{\"Fecha\":\"2021-12-07\",\"TipoDeposito\":\"TRA\",\"FolioCartola\":1011,\"FolioLineaCartola\":115,\"Banco\":\"CHILE\",\"Cuenta\":\"111111\",\"Monto\":1500,\"PeriodoPago\":\"2021-07\",\"TipoPago\":\"NORMAL\",\"RUTEmpleador\":\"76129263-3\",\"RUTCliente\":\"9499210-9\",\"TipodeAfiliado\":\"DEP\",\"Regimen\":\"APV-B\",\"MENSAJEVALIDACION\":\"OK\"},{\"Fecha\":\"2021-12-07\",\"TipoDeposito\":\"TRA\",\"FolioCartola\":1011,\"FolioLineaCartola\":115,\"Banco\":\"CHILE\",\"Cuenta\":\"111111\",\"Monto\":52500,\"PeriodoPago\":\"2021-07\",\"TipoPago\":\"NORMAL\",\"RUTEmpleador\":\"76129263-3\",\"RUTCliente\":\"12969673-7\",\"TipodeAfiliado\":\"DEP\",\"Regimen\":\"APV-B\",\"MENSAJEVALIDACION\":\"OK\"},{\"Fecha\":\"2021-12-07\",\"TipoDeposito\":\"TRA\",\"FolioCartola\":1011,\"FolioLineaCartola\":115,\"Banco\":\"CHILE\",\"Cuenta\":\"111111\",\"Monto\":8500,\"PeriodoPago\":\"2021-07\",\"TipoPago\":\"NORMAL\",\"RUTEmpleador\":\"76129263-3\",\"RUTCliente\":\"20231097-4\",\"TipodeAfiliado\":\"DEP\",\"Regimen\":\"APV-B\",\"MENSAJEVALIDACION\":\"OK\"},{\"Fecha\":\"2021-12-07\",\"TipoDeposito\":\"TRA\",\"FolioCartola\":118,\"FolioLineaCartola\":119,\"Banco\":\"CHILE\",\"Cuenta\":\"111111\",\"Monto\":21500,\"PeriodoPago\":\"2021-07\",\"TipoPago\":\"NORMAL\",\"RUTEmpleador\":\"76129263-3\",\"RUTCliente\":\"20231097-4\",\"TipodeAfiliado\":\"DEP\",\"Regimen\":\"APV-A\",\"MENSAJEVALIDACION\":\"OK\"}]}"; 
				
				Objin = gson.fromJson(json, EntradaCargaMasivaRecaudacion.class);
				

				try {

					SalidaCargaMasivaRecaudacion ObjOut = CMR.logica(con, Objin);
					System.out.println(ObjOut);
					//con.rollback(); 
					con.commit();
				} catch (Exception er) {
					er.printStackTrace();
					con.rollback(); 
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("== END ==");
		};
	}

}
