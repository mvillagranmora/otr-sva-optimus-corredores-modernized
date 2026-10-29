package cl.bcs.plataforma.mer;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import cl.bcs.fondomutuo.CargarValorCuota;
import cl.bcs.fondomutuo.CargarValorCuota.ResultadoCargaValorCuota;
import cl.bcs.plataforma.mer.CargaArchivoVectorPrecio_mer.ArchivoVectorPrecio;
import cl.bcs.plataforma.mer.CargaArchivoVectorPrecio_mer.Instrumento;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaArchivoVectorPrecio_mer implements JavaCall {
	static final Logger logger = LogManager.getLogger(CargarValorCuota.class);
	static int contador_insertados =0;
	private final String SQL_UPSERT_CARGA_VECTOR_PRECIO = loadResourceToString("cl/bcs/plataforma/mer/sql/CargaArchivoVectorPrecio_UPSERT_MER.sql");
	private final String SQL_SELECT_INSTRUMENO = loadResourceToString("cl/bcs/plataforma/mer/sql/CargaArchivoVectorPrecio_SelectInstrumento.sql");

	
	public static class Instrumento{
		public String Nemotecnico;
		public Long IdInstrumento;
		public String Fecha;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Count{
		public Long Count;
		@Override
		public String toString() {
			return String.format("Count [Count=%s]", Count);
		}
	}
	public static class EntradaCargaArchivoVectorPrecio{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_CodigoArchivo;
		public String p_Archivo;
		public String P_Simular;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public class ArchivoVectorPrecio{
		public String INSTRUMENTO;
		public String FECHA;
		public String VALOR;
		public String MENSAJEVALIDACION;
		public String ENTIDAD;
		public String TIPOREGISTRO;
		private boolean existeInstrumentoBD = false;
		private boolean existeFechaCarga = false;
		public String ESTADOVALIDACION;
		public int idResp;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public class SalidaCargaArchivoVectorPrecio extends SalidaEstandar{
		public List<ArchivoVectorPrecio> p_Resultado;
		
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCargaArchivoVectorPrecio(){
			super();
			this.P_OUT_ADVERTENCIASCONTADOR=0;
			this.P_OUT_ERRORESCONTADOR=0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<AdvertenciaDetalle>();
			this.P_OUT_ERRORESDETALLE = new ArrayList<ErrorDetalle>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		EntradaCargaArchivoVectorPrecio in =gson.fromJson(jin.toString() ,EntradaCargaArchivoVectorPrecio.class);
		SalidaCargaArchivoVectorPrecio out=logica(conn,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	 
	public SalidaCargaArchivoVectorPrecio logica(Connection conn, EntradaCargaArchivoVectorPrecio in) throws Exception {
		SalidaCargaArchivoVectorPrecio salida = new SalidaCargaArchivoVectorPrecio();
		
		SQL sql = new SQL();
		long contadorErrores =0L;
		List<ArchivoVectorPrecio> vectorPrecios = new ArrayList<ArchivoVectorPrecio>();
		try {
			
			if(in.p_CodigoArchivo.equals("CARGA_VECTOR_PRECIOS_PLA")) {
				
				System.out.println("Entro a validar el archivo");
				
				vectorPrecios = parseArchivo(in.p_Archivo);
				
			}else if(!in.p_CodigoArchivo.equals("VECTOR_PRECIOS_PLA"))
			{
				//AQUI va EL DE NAV
				salida.addError("ERROR_ARCHIVO_INVALIDO", "EL ARCHIVO QUE ESTA CARGANDO NO EXISTE");
				return salida;
			}
			else if(!in.P_Simular.equals("S") && !in.P_Simular.equals("S") ) {
				salida.addError("ERROR_PARAMETRO_INVALIDO", "FALTA EL PARAMETRO DE SIMULACION.");
				return salida;
			}
		} catch (ParseException e) {
			salida.addError("ERROR_ARCHIVO_INVALIDO", "EL ARCHIVO QUE ESTA CARGANDO CONTIENE FECHAS O VALORES CON FORMATO INCORRECTO");
			return salida;
		}

		List<String> VectorInstrumentos = vectorPrecios.stream().map(vector -> vector.INSTRUMENTO).distinct().collect(Collectors.toList());
		List<java.sql.Date> VectorFechas = vectorPrecios.stream().map(vector -> java.sql.Date.valueOf(vector.FECHA)  ).distinct().collect(Collectors.toList());
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("Instrumentos", VectorInstrumentos);
		params.put("Fechas", VectorFechas);
		List<Instrumento> instrumentos = new ArrayList<CargaArchivoVectorPrecio_mer.Instrumento>() ;
		int[] res = new int[1];
		List<ArchivoVectorPrecio> vectoresConIntrumentoValido = new ArrayList<CargaArchivoVectorPrecio_mer.ArchivoVectorPrecio>();
		try {
			instrumentos = sql.query(conn, SQL_SELECT_INSTRUMENO, params, Instrumento.class);
			
			if (instrumentos!=null && instrumentos.size()>0) {
				for (ArchivoVectorPrecio elementoVector : vectorPrecios) {
					
					
						elementoVector.existeInstrumentoBD=instrumentos.stream().anyMatch(elem -> elem.Nemotecnico.equalsIgnoreCase(elementoVector.INSTRUMENTO));
						elementoVector.existeFechaCarga=instrumentos.stream().anyMatch(elem -> elem.Nemotecnico.equalsIgnoreCase(elementoVector.INSTRUMENTO) && elem.Fecha.equalsIgnoreCase(elementoVector.FECHA));
				}
			}else {
				salida.addError("ERROR_ARCHIVO_INVALIDO", "NO EXISTE NEMOTECNICO VALIDO");
				contadorErrores++;
			}
			
			vectoresConIntrumentoValido = vectorPrecios.stream().filter(vector-> vector.existeInstrumentoBD).collect(Collectors.toList());
			if (vectoresConIntrumentoValido.size()>0 && in.P_Simular.equalsIgnoreCase("N")) {
				res = sql.batch(conn, SQL_UPSERT_CARGA_VECTOR_PRECIO, vectoresConIntrumentoValido, ArchivoVectorPrecio.class);
				if(res.length<vectoresConIntrumentoValido.size()) {
					salida.addError("ERROR_AL_INSERTAR_O_ACTUALIZAR_VECTOR", "ERROR AL REGISTRAR O ACTUALIZAR LOS VECTORES");
					contadorErrores++;
				}
				else if(res.length==vectoresConIntrumentoValido.size()){
					int puntero = 0;
					for (ArchivoVectorPrecio VectorPrecioValido : vectoresConIntrumentoValido) {
						VectorPrecioValido.idResp = res[puntero];
						puntero++;
					}
				}
			}
			
			
			
		} catch (SQLException ex) {
			// TODO Auto-generated catch block
			salida.addError("ERROR_AL_INSERTAR_O_ACTUALIZAR_VECTOR", "ERROR AL REGISTRAR O ACTUALIZAR LOS VECTORES");
			contadorErrores++;
			ex.printStackTrace();
		}
		if(in.p_CodigoArchivo.equals("CARGA_VECTOR_PRECIOS_PLA")) {
			for (ArchivoVectorPrecio vecPrecSinFiltro : vectorPrecios) {
				String instrumento = vecPrecSinFiltro.INSTRUMENTO;
				ArchivoVectorPrecio aux = vectoresConIntrumentoValido.stream().filter((vector) -> vector.INSTRUMENTO.equals(instrumento)).findFirst().orElse(null);
				
				if(aux!=null && aux.existeInstrumentoBD && in.P_Simular.equalsIgnoreCase("S") && vecPrecSinFiltro.existeFechaCarga) {
					vecPrecSinFiltro.ESTADOVALIDACION = "ADV";
					vecPrecSinFiltro.MENSAJEVALIDACION= Diccionario.getMensaje(conn,in.p_IdEmpresa, "VALOR_EXISTE_FECHA",new HashMap<String,String>() );

				}
				else if(aux!=null && aux.existeInstrumentoBD  && in.P_Simular.equalsIgnoreCase("S")){
					vecPrecSinFiltro.existeInstrumentoBD=aux.existeInstrumentoBD;
					vecPrecSinFiltro.ESTADOVALIDACION= "OK";
					vecPrecSinFiltro.MENSAJEVALIDACION= Diccionario.getMensaje(conn,in.p_IdEmpresa, "ES_VALIDO_GUARDAR",new HashMap<String,String>() );

				}
				else if(aux!=null && aux.existeInstrumentoBD && aux.idResp==1 && in.P_Simular.equalsIgnoreCase("N")){
					vecPrecSinFiltro.existeInstrumentoBD=aux.existeInstrumentoBD;
					vecPrecSinFiltro.ESTADOVALIDACION= "OK";
					vecPrecSinFiltro.MENSAJEVALIDACION= Diccionario.getMensaje(conn,in.p_IdEmpresa, "UI_DATOS_GUARDADOS",new HashMap<String,String>() );

				}
				
				else if (aux==null && vecPrecSinFiltro!=null) {
					vecPrecSinFiltro.ESTADOVALIDACION = "ERR";
					HashMap<String,String> Valor = new HashMap<String,String>();
					Valor.put("INSTRUMENTO", vecPrecSinFiltro.INSTRUMENTO);
					vecPrecSinFiltro.MENSAJEVALIDACION= Diccionario.getMensaje(conn,in.p_IdEmpresa, "INSTRUMENTO_INVALIDO_NO_EXISTE", Valor );
					contadorErrores++;//no guardados por id invalido
				}
				else
				{
					vecPrecSinFiltro.ESTADOVALIDACION = "ERR";
					vecPrecSinFiltro.MENSAJEVALIDACION= Diccionario.getMensaje(conn,in.p_IdEmpresa, "NO_VALIDO",new HashMap<String,String>() );

				}
			}
		}				
		
		salida.P_OUT_MENSAJES = obtenerMensajesSalida(conn,vectorPrecios,in.p_IdEmpresa);
		salida.P_OUT_ERRORESCONTADOR = contadorErrores;
		salida.p_Resultado = vectorPrecios;
		
		return salida;
	}
	private List<MensajeSalida> obtenerMensajesSalida(Connection con, List<ArchivoVectorPrecio> resultados,long IdEmpresa) throws Exception {
		logger.trace("INICIO::CargarArchivoVectorPrecio.ObtenerMensajesSalida");
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		for(ArchivoVectorPrecio resultado : resultados) {
			if(resultado.ESTADOVALIDACION.equalsIgnoreCase("ERR")) {
				
				
				mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR",resultado.MENSAJEVALIDACION));
				
			}
			else if (resultado.ESTADOVALIDACION.equalsIgnoreCase("ADV")) {
				
				mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"ADV",resultado.MENSAJEVALIDACION));
			}
			else {
				mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"OK",resultado.MENSAJEVALIDACION));
			}
		}
		logger.trace("FIN::CargarArchivoVectorPrecio.ObtenerMensajesSalida");
		return mensajes;
	}
	private List<ArchivoVectorPrecio> parseArchivo(String archivo) throws ParseException {
		
		List<ArchivoVectorPrecio> salida = new ArrayList<ArchivoVectorPrecio>();
		
		Gson gson = new Gson();
		SimpleDateFormat parser = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        
		TypeToken<List<ArchivoVectorPrecio>> token = new TypeToken<List<ArchivoVectorPrecio>>(){};
		List<ArchivoVectorPrecio> ArchivoIN = gson.fromJson(archivo, token.getType());

		//ArchivoVectorPrecio vector = new ArchivoVectorPrecio();

			for (ArchivoVectorPrecio archivoVEC : ArchivoIN)
			{
					archivoVEC.INSTRUMENTO = ((archivoVEC.INSTRUMENTO!=null)?archivoVEC.INSTRUMENTO : "").trim();
					Date fecha = parser.parse(archivoVEC.FECHA);
					archivoVEC.FECHA = formatter.format(fecha);
					//String VV = archivoVEC.VALOR.replace(",", ".");
					archivoVEC.VALOR=archivoVEC.VALOR;
					archivoVEC.MENSAJEVALIDACION ="OK";
					archivoVEC.ENTIDAD=archivoVEC.ENTIDAD;
					archivoVEC.TIPOREGISTRO=archivoVEC.TIPOREGISTRO;
					
					salida.add(archivoVEC);
			}
			System.out.println(salida);
		return salida;
	}

	protected Double formatDouble(String valor) throws ParseException {
		DecimalFormat df = new DecimalFormat();
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator(',');
		df.setDecimalFormatSymbols(dfs);
		try {
			Double parsedValor = df.parse(valor).doubleValue();
			return parsedValor;
		} catch (ParseException e) {
			throw new ParseException("ERROR AL PARSEAR VALOR: " + valor, 0);
		}
	}
	public static void main(String[] args) {

		System.out.println("====== TEST ======");
		 
		
		try (Connection con = PlataformaUtils.getDESAConnection()){	
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			EntradaCargaArchivoVectorPrecio JSN = new EntradaCargaArchivoVectorPrecio();
			
			String JSON =JSN.p_Archivo; //json de fuera
			
			
			//JSON A MANO
     
			String json2="{\"p_IdEmpresa\":1,\"p_IdUsuario\":109,\"p_IdModulo\":\"PLA\",\"p_CodigoArchivo\":\"CARGA_VECTOR_PRECIOS_PLA\",\"p_Archivo\":\"[{\\\"INSTRUMENTO\\\":\\\"IVV\\\",\\\"FECHA\\\":\\\"30-05-2018\\\",\\\"VALOR\\\":\\\"000000186960000\\\",\\\"MENSAJEVALIDACION\\\":\\\"OK\\\",\\\"ENTIDAD\\\":\\\"BLOOMBERG\\\",\\\"TIPOREGISTRO\\\":\\\"BLOOMBERG PRECIO\\\"},{\\\"INSTRUMENTO\\\":\\\"AES GENER\\\",\\\"FECHA\\\":\\\"30-05-2018\\\",\\\"VALOR\\\":\\\"000000001075700\\\",\\\"MENSAJEVALIDACION\\\":\\\"OK\\\",\\\"ENTIDAD\\\":\\\"BLOOMBERG\\\",\\\"TIPOREGISTRO\\\":\\\"BLOOMBERG PRECIO\\\"},{\\\"INSTRUMENTO\\\":\\\"AAPL\\\",\\\"FECHA\\\":\\\"30-05-2018\\\",\\\"VALOR\\\":\\\"000000001075700\\\",\\\"MENSAJEVALIDACION\\\":\\\"OK\\\",\\\"ENTIDAD\\\":\\\"BLOOMBERG\\\",\\\"TIPOREGISTRO\\\":\\\"BLOOMBERG PRECIO\\\"},{\\\"INSTRUMENTO\\\":\\\"FCX\\\",\\\"FECHA\\\":\\\"30-05-2018\\\",\\\"VALOR\\\":\\\"000000016690000\\\",\\\"MENSAJEVALIDACION\\\":\\\"OK\\\",\\\"ENTIDAD\\\":\\\"BLOOMBERG\\\",\\\"TIPOREGISTRO\\\":\\\"BLOOMBERG PRECIO\\\"}]\"}";
			EntradaCargaArchivoVectorPrecio Objin = gson.fromJson(JSON, EntradaCargaArchivoVectorPrecio.class);
			CargaArchivoVectorPrecio_mer demo = new CargaArchivoVectorPrecio_mer();
			System.out.println("====== ENTRADA ======");
			
			//System.out.println(Objin.toString());
			
			try {
				SalidaCargaArchivoVectorPrecio ObjOut = demo.logica(con, Objin);
				con.commit();
				//System.out.println(ObjOut.toString());
				System.out.println("====== SALIDA ======");
			} catch (Exception er) {
				er.printStackTrace();
				//con.rollback(); //no bd
			}
			//con.rollback(); //------>
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
