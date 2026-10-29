package cl.bcs.plataforma.ins;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaArchivoVectorPrecio implements JavaCall {

	
	private final String SQL_UPSERT_CARGA_VECTOR_PRECIO = loadResourceToString("cl/bcs/plataforma/ins/sql/CargaArchivoVectorPrecio_UPSERT.sql");
	private final String SQL_SELECT_CLASIFICACION_RIESGO = "SELECT COUNT(*) AS \"Count\" FROM BSCL.\"BSCL.Plataforma.BD::INS.ClasificacionRiesgo\" WHERE \"ClasificacionRiesgo\" = :ClasificacionRiesgo AND \"IdEntidadClasificadoraRiesgo\" = :IdEntidadClasificadoraRiesgo;";
	private final String SQL_SELECT_INSTRUMENO = "SELECT TOP 1 \"IdInstrumento\" FROM BSCL.\"BSCL.Plataforma.BD::INS.Instrumento\" WHERE \"Nemotecnico\" = :Nemotecnico";
	private final String SQL_SELECT_INSTRUMENO_ISIN = "SELECT TOP 1 \"IdInstrumento\" FROM \"BSCL.Plataforma.BD::INS.Instrumento\" WHERE \"ISIN\" = :ISIN;";
	private final String SQL_UPSERT_VALOR_CLASIFICACION_RIESGO = "UPSERT \"BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo\"(\"IdInstrumento\", \"IdEntidadClasificadoraRiesgo\",\"ValorClasificacionRiesgo\") VALUES(:IdInstrumento, :IdEntidadClasificadoraRiesgo, :ValorClasificacionRiesgo) WHERE \"IdInstrumento\" = :IdInstrumento AND \"IdEntidadClasificadoraRiesgo\" = :IdEntidadClasificadoraRiesgo;";
	//ARCHIVO BVL
	private static final String ENTIDAD_BVL = "BVL";
	private static final String TIPO_ENTIDAD_BVL_PROMEDIO = "PRECIOPROMEDIO";
	private static final String TIPO_ENTIDAD_BVL_APERTURA = "PRECIOAPERTURA";
	private static final String TIPO_ENTIDAD_BVL_CIERRE = "PRECIOCIERRE";
	private static final String TIPO_ENTIDAD_BVL_REPORTE = "PRECIOREPORTE";
	//ARCHIVO TASAS RIESGO
	private static final String ENTIDAD_TASA_RIESGO = "TASARIESGO";
	private static final String TIPO_ENTIDAD_TASA_RIESGO = "PRECIOMERCADO";
	//ARCHIVO CORREDORA
	private static final String ENTIDAD_CORREDORA = "CORREDORA";
	private static final String TIPO_ENTIDAD_CORREDORA_ANUAL = "TASAANUAL";
	
	public static class Instrumento{
		public Long IdInstrumento;
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
	
	public static class ValorClasificacion{
		public Long IdInstrumento;
		public String ValorClasificacionRiesgo;
		public String IdEntidadClasificadoraRiesgo;
	}
	
	
	public class EntradaCargaArchivoVectorPrecio{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_CodigoArchivo;
		public String p_Archivo;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class ArchivoVectorPrecio{
		public String FECHA;
		public String NEMOTECNICO;
		public String ENTIDAD;
		public String TIPOREGISTRO;
		public Double VALORREGISTRO;		
		public String RATING;
		public String MENSAJEVALIDACION;
		public String CODISIN;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class ArchivoVectorPreciosBVLRV{
		public String FECHAHORA;
		public String FECHA;
		public String MODALIDAD;
		public String MERCADO;
		public String LIBRO;
		public String SEGMENTO;
		public String CATEGORIA;
		public String ISIN;
		public String NEMOTECNICO;
		public String MONEDAINSTRUMENTO;
		public String TIPOCAMBIO;
		public String CIERREPREVIO;
		public String FECHACIERREPREVIO;
		public String ULTIMASIG;
		public String HORAULTIMASIG;
		public String VARULTSIG;
		public String ULTIMA;
		public String QULTIMA;
		public String HORAULTIMA;
		public String VARIACIONULTVSPREVIO;
		public String VARULT;
		public String PCOMPRA;
		public String PVENTA;
		public String QCOMPRA;
		public String QVENTA;
		public String PCOMPRAMAX;
		public String PVENTAMAX;
		public String DIFCOMPRAULTIMA;
		public String DIFVENTAULTIMA;
		public String PCOMPRAAYER;
		public String PVENTAAYER;
		public String PAPERTURA;
		public String QAPERTURA;
		public String PCIERRE;
		public String PMAXIMOSIG;
		public String PMINIMOSIG;
		public String PPROMEDIO;
		public String FUENTEAPERTURA;
		public String FUENTECIERRE;
		public String PMAXIMO;
		public String PMINIMO;
		public String QNEGOCIADA;
		public String MONTOEFECTIVO;
		public String MONTOEFECTIVOPEN;
		public String NUMOPERACIONES;
		public String PREFERENCIABP;
		public String PLIMITEMINBP;
		public String PLIMITEMAXBP;
		public String PREFCBESTATICO;
		public String PREFCBDINAMICO;
		public String PREFREPORTE;
		public String PREFERENCIAPRESTAMO;
		public String PINDICATIVOSUBASTA;
		public String QNEGOCIABLESUBASTA;
		public String QDESEQUILIBRIOSUB;
		public String DIRDESEQUILIBRIOSUB;
		public String MARCADOREXDERECHO;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		
	}
	
	public class ArchivoVectorPreciosBVLRF{
		public String FECHAHORA;
		public String FECHADERUEDA;
		public String MODALIDAD;
		public String MERCADO;
		public String LIBRO;
		public String SEGMENTO;
		public String CATEGORIA;
		public String ISIN;
		public String NEMOTECNICO;
		public String MONEDA;
		public String TIPODECAMBIO;
		public String VNACTUAL;
		public String CIERREPREVIO;
		public String TREANTERIOR;
		public String FECHACIERREPREVIO;
		public String PULTIMASIG;
		public String TREULTIMASIG;
		public String HORAULTIMASIG;
		public String PULTIMA;
		public String TREULTIMA;
		public String QULTIMA;
		public String HORAULTIMA;
		public String ULTVSPRECIO;
		public String PCOMPRA;
		public String PVENTA;
		public String QCOMPRA;
		public String QVENTA;
		public String MNCOMPRA;
		public String MNVENTA;
		public String PCOMPRAMAX;
		public String PVENTAMIN;
		public String DIFCOMPRAULTIMA;
		public String DIFVENTAULTIMA;
		public String PCOMPRAAYER;
		public String PVENTAAYER;
		public String PAPERTURA;
		public String TREAPERTURA;
		public String QAPERTURA;
		public String PCIERRE;
		public String TCIERRE;
		public String PMAXIMOSIG;
		public String PMINIMOSIG;
		public String PPROMEDIO;
		public String FUENTEAPERTURA;
		public String FUENTECIERRE;
		public String PMAXIMO;
		public String PMINIMO;
		public String TREMAXIMO;
		public String TREMINIMO;
		public String QNEGOCIADO;
		public String MNNEGOCIADO;
		public String MONTOEFECTIVO;
		public String MONTOEFECTIVOPEN;
		public String NUMOPERACIONES;
		public String PINDICATIVOSUBASTA;
		public String QNEGOCIABLESUBASTA;
		public String QDESEQUILIBRIOSUB;
		public String DIRDESEQUILIBRIOSUB;
		public String TREINDICATIVASUB;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class ArchivoVectorPreciosTasaRiesgoRF{
		public String FECHA;
		public String CODISIN;
		public String NEMONICO;
		public String EMISOR;
		public String MONEDA;
		public String TIPOTASA;
		public String TASACUPON;
		public String FECVCTO;
		public String RATING;
		public String NOMINAL;
		public String TIRMCDO;
		public String PRECIOLIMPIO;
		public String INTCORRIDO;
		public String REAJUSTEVAC;
		public String PRECIOMCDO;
		public String DURNORMAL;
		public String FUENTE;
		public String PRECIODIAANT;
		public String PRECIOMESANT;
		public String FECPROXCUPON;
		public String ANIOS;
		public String FACTOR;
		public String DEUDASOBERANA;
		public String MENSAJEVALIDACION;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class ArchivoVectorPreciosTasaRiesgoRV{
		public String CODISIN;
		public String TICKER;
		public String EMISOR;
		public String MONEDA;
		public String PRECIO;
		public String FUENTE;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public class ArchivoVectorCorredoraRV{
		public String FECHAHORA;
		public String FECHADERUEDA;
		public String ISIN;
		public String NEMONICO;
		public String ESTADO;
		public String CLASE;
		public String EMISOR;
		public String MERCADO;
		public String SEGMENTO;
		public String CATEGOREA;
		public String TRADINGPARAMETER;
		public String POSTTRADEPARAMETER;
		public String FIRSTTRADINGDATE;
		public String LASTTRADINGDATE;
		public String BASEDISCOUNT;
		public String ADDITIONALCOLLATERAL;
		public String BASEINSTRUMENT;
		public String FOREIGNCURRENCY;
		public String NORMALORDERBOOK;
		public String EARLYSETTLEMENTORDERBOOK;
		public String AUCTIONORDERBOOK;
		public String BLOCKTRADEORDERBOOK;
		public String REPOORDERBOOK;
		public String AONORDERBOOK;
		public String ODDLOTORDERBOOK;
		public String BULLETINORDERBOOK;
		public String OFFBOOK;
		public String MILAALLOWED;
		public String VALORNOMINAL;
		public String MONTOMENIMOCOTIZACION;
		public String TOTACCSCIRC;
		public String ACCSCIRCDISP;
		public String ACCIONESLIBERADAS;
		public String UDNCDAL;
		public String ENTREGAAL;
		public String DIVIDENDOSENEFECTIVO;
		public String UDNCDDE;
		public String ENTREGADE;
		public String SUSCRIPCION;
		public String UDNCDS;
		public String ENTREGAS;
		public String INFORMACION;
		public String PM;
		public String UNIDADDENEGOCIACION;
		public String CIIU;
		public String CLASIFICACIONDERIESGO;
		public String MONEDA;
		public String FECHADEREGISTRO;
		public String DEASMENIMOS;
		public String LEMITE;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class ArchivoVectorCorredoraRF{
		public String FECHAHORA;
		public String FECHARUEDA;
		public String ISIN;
		public String NEMONICO;
		public String ESTADO;
		public String CLASE;
		public String EMISOR;
		public String MERCADO;
		public String EMISION;
		public String SERIE;
		public String INFORMACION;
		public String SEGMENTO;
		public String CATEGORIA;
		public String TRADINGPARAMETER;
		public String POSTTRADEPARAMETER;
		public String FIRSTTRADINGDATE;
		public String LASTTRADINGDATE;
		public String BASEDISCOUNT;
		public String ADDITIONALCOLLATERAL;
		public String BASEINSTRUMENT;
		public String FIRSTCOUPONDATE;
		public String PENULTIMATECOUPONDATE;
		public String NORMALORDERBOOK;
		public String EARLYSETTLEMENTORDERBOOK;
		public String AUCTIONORDERBOOK;
		public String BLOCKTRADEORDERXBOOK;
		public String REPOORDERBOOK;
		public String AONORDERBOOK;
		public String ODDLOTORDERBOOK;
		public String BULLETINORDERBOOK;
		public String OFFBOOK;
		public String TASAANUAL;
		public String TIPOTASA;
		public String PERIODODEPAGO;
		public String MONEMIS;
		public String MONPAGO;
		public String EXPDELVN;
		public String VNAJUS;
		public String TASAINTERESAJUSTADA;
		public String BASEv;
		public String VALCONY;
		public String FECHAEMISION;
		public String FECHADEVENCIMIENTO;
		public String FECHADEANTIC;
		public String DISPLEGAL;
		public String TOTALAUTORIZADO;
		public String TOTALEMITIDO;
		public String TOTALCOLOCADO;
		public String REPRESENTANTEDEOBLIGACIONES;
		public String CLASDERIESGO;
		public String PM;
		public String FECHADECOLOCACION;
		public String CANASTA;
		public String VALORNOMINALINICIAL;
		public String VALORNOMINALACTUAL;
		public String MONTOMINESTABCOT;
		public String COUPONTYPE;
		public String BONDTYPE;
		public String CAVALICUPON;
		public String INDICEBASEIPC;
		public String LARGO;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class SalidaCargaArchivoVectorPrecio extends SalidaEstandar{
		public List<ArchivoVectorPrecio> p_Resultado;
		
		public SalidaCargaArchivoVectorPrecio(){
			super();
			this.P_OUT_ADVERTENCIASCONTADOR=0;
			this.P_OUT_ERRORESCONTADOR=0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<AdvertenciaDetalle>();
			this.P_OUT_ERRORESDETALLE = new ArrayList<ErrorDetalle>();
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

	private SalidaCargaArchivoVectorPrecio logica(Connection conn, EntradaCargaArchivoVectorPrecio in) throws Exception {
		SalidaCargaArchivoVectorPrecio salida = new SalidaCargaArchivoVectorPrecio();
		SQL sql = new SQL();
		String CLASIFICACION_RIESGO = "FITCH"; //validar que exista en EntidadClasificadoraRiesgo
		
		List<ArchivoVectorPrecio> vectorPrecios = new ArrayList<ArchivoVectorPrecio>();
		try {
			if(in.p_CodigoArchivo.equals("VECTOR_PRECIOS_BVL_RV")) {
				vectorPrecios = parseArchivoBvlRV(in.p_Archivo);
				
			}else if(in.p_CodigoArchivo.equals("VECTOR_PRECIOS_BVL_RF")) {
				vectorPrecios = parseArchivoBvlRF(in.p_Archivo);
							
			}else if(in.p_CodigoArchivo.equals("VECTOR_PRECIOS_TASA_RIESGO_RV")) {
				String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
				vectorPrecios = parseArchivoTasaRiesgoRV(in.p_Archivo, fechaSistema);//obtener fecha de sistema!!
				
			}else if(in.p_CodigoArchivo.equals("VECTOR_PRECIOS_TASA_RIESGO_RF")) {
				vectorPrecios = parseArchivoTasaRiesgoRF(in.p_Archivo);
				
			}else if(in.p_CodigoArchivo.equals("VECTOR_PRECIOS_CORREDORA_RV")) {
				vectorPrecios = parseArchivoCorredoraRV(in.p_Archivo);
				
			}else if(in.p_CodigoArchivo.equals("VECTOR_PRECIOS_CORREDORA_RF")) {
				vectorPrecios = parseArchivoCorredoraRF(in.p_Archivo);
				
			}else {
				salida.addError("ERROR_ARCHIVO_INVALIDO", "EL ARCHIVO QUE ESTA CARGANDO NO EXISTE ");
				return salida;
			}
		} catch (ParseException e) {
			salida.addError("ERROR_ARCHIVO_INVALIDO", "EL ARCHIVO QUE ESTA CARGANDO CONTIENE FECHAS O VALORES CON FORMATO INCORRECTO");
			return salida;
		}
		for(ArchivoVectorPrecio fila : vectorPrecios) {
			try {

				HashMap<String, Object> params = new HashMap<>();
				Long idInstrumento = null;
				//obtener IdInstrumento
				if(in.p_CodigoArchivo.equals("VECTOR_PRECIOS_TASA_RIESGO_RV")) {
					params.put("ISIN", fila.CODISIN);
					List<Instrumento>  instrumentos = sql.query(conn, SQL_SELECT_INSTRUMENO_ISIN, params, Instrumento.class);
					if(instrumentos!=null && instrumentos.size()>0) {
						idInstrumento = instrumentos.get(0).IdInstrumento;
					}else {
						fila.MENSAJEVALIDACION = "NO EXISTE ISIN";
						System.out.println(fila.MENSAJEVALIDACION);
						continue;
					}
				}else {
					params.put("Nemotecnico", fila.NEMOTECNICO);
					List<Instrumento> instrumentos = sql.query(conn, SQL_SELECT_INSTRUMENO, params, Instrumento.class);
					if(instrumentos!=null && instrumentos.size()>0) {
						idInstrumento = instrumentos.get(0).IdInstrumento;
					}else {
						fila.MENSAJEVALIDACION = "NO EXISTE NEMOTECNICO";
						System.out.println(fila.MENSAJEVALIDACION);
						continue;	
					}
				}
								
				int[] res = sql.batch(conn, SQL_UPSERT_CARGA_VECTOR_PRECIO, Arrays.asList(fila), ArchivoVectorPrecio.class);
				fila.MENSAJEVALIDACION = (res[0]==1) ? "OK" : "ERROR AL INGRESAR PRECIO";
				
				//obtener clasificacion de riesgo validar
				params = new HashMap<>();
				params.put("ClasificacionRiesgo", fila.RATING);
				params.put("IdEntidadClasificadoraRiesgo", CLASIFICACION_RIESGO);
				List<Count> existeClasificacionRiesgo = sql.query(conn, SQL_SELECT_CLASIFICACION_RIESGO, params, Count.class);
				if(existeClasificacionRiesgo!=null && existeClasificacionRiesgo.size()>0 && existeClasificacionRiesgo.get(0).Count.equals(new Long(1))) {
					//actualizar valor clasificacion riesgo
					ValorClasificacion valor = new ValorClasificacion();
					valor.IdInstrumento = idInstrumento;
					valor.ValorClasificacionRiesgo = fila.RATING;
					valor.IdEntidadClasificadoraRiesgo = CLASIFICACION_RIESGO;
					int[] ress = sql.batch(conn, SQL_UPSERT_VALOR_CLASIFICACION_RIESGO, Arrays.asList(valor), ValorClasificacion.class);
				}
				System.out.println(fila);
			} catch (SQLException e) {
				fila.MENSAJEVALIDACION = "ERROR AL INGRESAR PRECIO";
				e.printStackTrace();
			}
		}
		salida.p_Resultado = vectorPrecios;
		
		return salida;
	}
	
	private List<ArchivoVectorPrecio> parseArchivoCorredoraRV(String archivo) throws ParseException {
		List<ArchivoVectorPrecio> salida = new ArrayList<ArchivoVectorPrecio>();
		Gson gson = new Gson();	
		SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        
		TypeToken<List<ArchivoVectorCorredoraRV>> token = new TypeToken<List<ArchivoVectorCorredoraRV>>(){};
		List<ArchivoVectorCorredoraRV> corredorasRV = gson.fromJson(archivo, token.getType());
		for (ArchivoVectorCorredoraRV corredora : corredorasRV) {
			//PRECIO MERCADO
			ArchivoVectorPrecio vector = new ArchivoVectorPrecio();
			vector.ENTIDAD = ENTIDAD_CORREDORA;
			Date fecha = parser.parse(corredora.FECHADERUEDA);
			vector.FECHA = formatter.format(fecha);
			vector.NEMOTECNICO = ((corredora.NEMONICO!=null)?corredora.NEMONICO : "");
			vector.RATING = "";
			vector.TIPOREGISTRO = TIPO_ENTIDAD_CORREDORA_ANUAL;
			vector.VALORREGISTRO = Double.parseDouble((corredora.VALORNOMINAL!=null)? corredora.VALORNOMINAL : "0" );//TODO NO SE SABE CUAL ES
			salida.add(vector);
		}
		return salida;
	}
	
	private List<ArchivoVectorPrecio> parseArchivoCorredoraRF(String archivo) throws ParseException {
		List<ArchivoVectorPrecio> salida = new ArrayList<ArchivoVectorPrecio>();
		Gson gson = new Gson();	
		SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        
		TypeToken<List<ArchivoVectorCorredoraRF>> token = new TypeToken<List<ArchivoVectorCorredoraRF>>(){};
		List<ArchivoVectorCorredoraRF> corredorasRF = gson.fromJson(archivo, token.getType());
		for (ArchivoVectorCorredoraRF corredora : corredorasRF) {
			//PRECIO MERCADO
			ArchivoVectorPrecio vector = new ArchivoVectorPrecio();
			vector.ENTIDAD = ENTIDAD_CORREDORA;
			Date fecha = parser.parse(corredora.FECHARUEDA);
			vector.FECHA = formatter.format(fecha);
			vector.NEMOTECNICO = ((corredora.NEMONICO!=null)?corredora.NEMONICO : "");
			vector.RATING = "";
			vector.TIPOREGISTRO = TIPO_ENTIDAD_CORREDORA_ANUAL;
			vector.VALORREGISTRO =  (corredora.TASAANUAL!=null)? formatDouble(corredora.TASAANUAL) : 0.0;
			salida.add(vector);
		}
		return salida;
	}

	private List<ArchivoVectorPrecio> parseArchivoTasaRiesgoRF(String archivo) throws ParseException {
		List<ArchivoVectorPrecio> salida = new ArrayList<ArchivoVectorPrecio>();
		
		Gson gson = new Gson();	
		SimpleDateFormat parser = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        
		TypeToken<List<ArchivoVectorPreciosTasaRiesgoRF>> token = new TypeToken<List<ArchivoVectorPreciosTasaRiesgoRF>>(){};
		List<ArchivoVectorPreciosTasaRiesgoRF> tasasRiesgo= gson.fromJson(archivo, token.getType());
		for (ArchivoVectorPreciosTasaRiesgoRF tasaRiesgo : tasasRiesgo) {
			//PRECIO MERCADO
			ArchivoVectorPrecio vector = new ArchivoVectorPrecio();
			vector.ENTIDAD = ENTIDAD_TASA_RIESGO;
			Date fecha = parser.parse(tasaRiesgo.FECHA);
			vector.FECHA = formatter.format(fecha);
			vector.NEMOTECNICO = ((tasaRiesgo.NEMONICO!=null)?tasaRiesgo.NEMONICO : "");
			vector.RATING = (tasaRiesgo.RATING!=null) ? tasaRiesgo.RATING : "";
			vector.TIPOREGISTRO = TIPO_ENTIDAD_TASA_RIESGO;
			vector.VALORREGISTRO = Double.parseDouble((tasaRiesgo.PRECIOLIMPIO!=null)? tasaRiesgo.PRECIOLIMPIO : "0" );
			salida.add(vector);
		}
		return salida;
	}

	private List<ArchivoVectorPrecio> parseArchivoTasaRiesgoRV(String archivo, String fechaSistema) throws ParseException {
		List<ArchivoVectorPrecio> salida = new ArrayList<ArchivoVectorPrecio>();
		
		Gson gson = new Gson();	
		TypeToken<List<ArchivoVectorPreciosTasaRiesgoRV>> token = new TypeToken<List<ArchivoVectorPreciosTasaRiesgoRV>>(){};
		List<ArchivoVectorPreciosTasaRiesgoRV> tasasRiesgo= gson.fromJson(archivo, token.getType());
		for (ArchivoVectorPreciosTasaRiesgoRV tasaRiesgo : tasasRiesgo) {
			//PRECIO MERCADO
			ArchivoVectorPrecio vector = new ArchivoVectorPrecio();
			vector.ENTIDAD = ENTIDAD_TASA_RIESGO;
			vector.FECHA = fechaSistema;
			//vector.NEMOTECNICO = ((tasaRiesgo.CODISIN!=null)?tasaRiesgo.CODISIN : "");
			vector.CODISIN = ((tasaRiesgo.CODISIN!=null)?tasaRiesgo.CODISIN : "");
			vector.NEMOTECNICO = ((tasaRiesgo.TICKER!=null)?tasaRiesgo.TICKER : "");
			vector.RATING = "";
			vector.TIPOREGISTRO = TIPO_ENTIDAD_TASA_RIESGO;
			vector.VALORREGISTRO = (tasaRiesgo.PRECIO!=null)? formatDouble(tasaRiesgo.PRECIO) : 0.0;
			salida.add(vector);
		}
		return salida;
	}
	
	private List<ArchivoVectorPrecio> parseArchivoBvlRV(String archivo) throws ParseException {
		List<ArchivoVectorPrecio> salida = new ArrayList<ArchivoVectorPrecio>();
		
		Gson gson = new Gson();
		SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        
		TypeToken<List<ArchivoVectorPreciosBVLRV>> token = new TypeToken<List<ArchivoVectorPreciosBVLRV>>(){};
		List<ArchivoVectorPreciosBVLRV> preciosBvl = gson.fromJson(archivo, token.getType());
		
		for (ArchivoVectorPreciosBVLRV archivoBVL : preciosBvl) {
			//PRECIO APERTURA
			ArchivoVectorPrecio vector = new ArchivoVectorPrecio();
			vector.ENTIDAD = ENTIDAD_BVL;
			Date fecha = parser.parse(archivoBVL.FECHA);
			vector.FECHA = formatter.format(fecha);
			vector.NEMOTECNICO = ((archivoBVL.NEMOTECNICO!=null)?archivoBVL.NEMOTECNICO : "").trim();
			vector.RATING = "";
			vector.TIPOREGISTRO = TIPO_ENTIDAD_BVL_APERTURA;
			vector.VALORREGISTRO = Double.parseDouble((archivoBVL.PAPERTURA!=null)? archivoBVL.PAPERTURA : "0" );
			salida.add(vector);
			//PRECIO CIERRE
			vector = new ArchivoVectorPrecio();
			vector.ENTIDAD = ENTIDAD_BVL;
			fecha = parser.parse(archivoBVL.FECHA);
			vector.FECHA = formatter.format(fecha);
			vector.NEMOTECNICO = ((archivoBVL.NEMOTECNICO!=null)?archivoBVL.NEMOTECNICO : "").trim();
			vector.RATING = "";
			vector.TIPOREGISTRO = TIPO_ENTIDAD_BVL_CIERRE;
			vector.VALORREGISTRO = Double.parseDouble((archivoBVL.PCIERRE!=null)? archivoBVL.PCIERRE : "0");
			salida.add(vector);
			//PRECIO PROMEDIO
			vector = new ArchivoVectorPrecio();
			vector.ENTIDAD = ENTIDAD_BVL;
			fecha = parser.parse(archivoBVL.FECHA);
			vector.FECHA = formatter.format(fecha);
			vector.NEMOTECNICO = ((archivoBVL.NEMOTECNICO!=null)?archivoBVL.NEMOTECNICO : "").trim();
			vector.RATING = "";
			vector.TIPOREGISTRO = TIPO_ENTIDAD_BVL_PROMEDIO;
			vector.VALORREGISTRO = Double.parseDouble((archivoBVL.PPROMEDIO!=null)? archivoBVL.PPROMEDIO : "0");
			salida.add(vector);
			//PRECIO REPORTE
			vector = new ArchivoVectorPrecio();
			vector.ENTIDAD = ENTIDAD_BVL;
			fecha = parser.parse(archivoBVL.FECHA);
			vector.FECHA = formatter.format(fecha);
			vector.NEMOTECNICO = ((archivoBVL.NEMOTECNICO!=null)?archivoBVL.NEMOTECNICO : "").trim();
			vector.RATING = "";
			vector.TIPOREGISTRO = TIPO_ENTIDAD_BVL_REPORTE;
			vector.VALORREGISTRO = Double.parseDouble((archivoBVL.PREFREPORTE!=null) ? archivoBVL.PREFREPORTE : "0");
			salida.add(vector);

		}
		
		return salida;
	}

	private List<ArchivoVectorPrecio> parseArchivoBvlRF(String archivo) throws ParseException {
		List<ArchivoVectorPrecio> salida = new ArrayList<ArchivoVectorPrecio>();
		
		Gson gson = new Gson();	
		SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        
		TypeToken<List<ArchivoVectorPreciosBVLRF>> token = new TypeToken<List<ArchivoVectorPreciosBVLRF>>(){};
		List<ArchivoVectorPreciosBVLRF> preciosBvlRF = gson.fromJson(archivo, token.getType());
		for (ArchivoVectorPreciosBVLRF bvlRf : preciosBvlRF) {
			//PRECIO MERCADO
			ArchivoVectorPrecio vector = new ArchivoVectorPrecio();
			vector.ENTIDAD = ENTIDAD_BVL;
			Date fecha = parser.parse(bvlRf.FECHADERUEDA);
			vector.FECHA = formatter.format(fecha);
			vector.NEMOTECNICO = ((bvlRf.NEMOTECNICO!=null)?bvlRf.NEMOTECNICO : "").trim();
			vector.RATING = "";
			vector.TIPOREGISTRO = TIPO_ENTIDAD_BVL_CIERRE;
			vector.VALORREGISTRO = Double.parseDouble((bvlRf.PCIERRE!=null)? bvlRf.PCIERRE : "0" );
			salida.add(vector);
		}
		
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
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getQAPEConnection()){
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			//ARCHIVO TASA RIESGO
			String json_BVL_RF ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":102,\"p_IdModulo\":\"PLA\",\"p_CodigoArchivo\":\"VECTOR_PRECIOS_BVL_RF\",\"p_Archivo\":\"[{\\\"FECHAHORA\\\":\\\"21-02-2017 16:31:00\\\",\\\"FECHADERUEDA\\\":\\\"21/02/2017\\\",\\\"MODALIDAD\\\":\\\"            \\\",\\\"MERCADO\\\":\\\"FIXED INCOME                  \\\",\\\"LIBRO\\\":\\\"NORMAL     \\\",\\\"SEGMENTO\\\":\\\"RF1            \\\",\\\"CATEGORIA\\\":\\\"               \\\",\\\"ISIN\\\":\\\"PEP36100M154\\\",\\\"NEMOTECNICO\\\":\\\"GLOR1BC5A                 \\\",\\\"MONEDA\\\":\\\"PEN\\\",\\\"TIPODECAMBIO\\\":3.251,\\\"VNACTUAL\\\":0.000001,\\\"CIERREPREVIO\\\":0.0001019022,\\\"TREANTERIOR\\\":0.0000057047,\\\"FECHACIERREPREVIO\\\":\\\"23/01/2017\\\",\\\"PULTIMASIG\\\":0,\\\"TREULTIMASIG\\\":0,\\\"HORAULTIMASIG\\\":\\\"               \\\",\\\"PULTIMA\\\":0,\\\"TREULTIMA\\\":0,\\\"QULTIMA\\\":\\\"00000000\\\",\\\"HORAULTIMA\\\":\\\"               \\\",\\\"ULTVSPRECIO\\\":0,\\\"PCOMPRA\\\":0,\\\"PVENTA\\\":0,\\\"QCOMPRA\\\":\\\"00000000\\\",\\\"QVENTA\\\":\\\"00000000\\\",\\\"MNCOMPRA\\\":0,\\\"MNVENTA\\\":0,\\\"PCOMPRAMAX\\\":0,\\\"PVENTAMIN\\\":0,\\\"DIFCOMPRAULTIMA\\\":0,\\\"DIFVENTAULTIMA\\\":0,\\\"PCOMPRAAYER\\\":0,\\\"PVENTAAYER\\\":0,\\\"PAPERTURA\\\":0,\\\"TREAPERTURA\\\":\\\"00000000\\\",\\\"QAPERTURA\\\":0,\\\"PCIERRE\\\":101.9022,\\\"TCIERRE\\\":0.057047,\\\"PMAXIMOSIG\\\":0,\\\"PMINIMOSIG\\\":0,\\\"PPROMEDIO\\\":0,\\\"FUENTEAPERTURA\\\":\\\" \\\",\\\"FUENTECIERRE\\\":\\\"C\\\",\\\"PMAXIMO\\\":0,\\\"PMINIMO\\\":0,\\\"TREMAXIMO\\\":0,\\\"TREMINIMO\\\":0,\\\"QNEGOCIADO\\\":\\\"000000000000000000\\\",\\\"MNNEGOCIADO\\\":0,\\\"MONTOEFECTIVO\\\":0,\\\"MONTOEFECTIVOPEN\\\":0,\\\"NUMOPERACIONES\\\":\\\"000000000000000000\\\",\\\"PINDICATIVOSUBASTA\\\":0,\\\"QNEGOCIABLESUBASTA\\\":\\\"000000000000000000\\\",\\\"QDESEQUILIBRIOSUB\\\":0,\\\"DIRDESEQUILIBRIOSUB\\\":\\\" \\\",\\\"TREINDICATIVASUB\\\":0,\\\"MENSAJEVALIDACION\\\":\\\"OK\\\"},{\\\"FECHAHORA\\\":\\\"21-02-2017 16:31:00\\\",\\\"FECHADERUEDA\\\":\\\"21/02/2017\\\",\\\"MODALIDAD\\\":\\\"            \\\",\\\"MERCADO\\\":\\\"FIXED INCOME                  \\\",\\\"LIBRO\\\":\\\"NORMAL     \\\",\\\"SEGMENTO\\\":\\\"RF1            \\\",\\\"CATEGORIA\\\":\\\"               \\\",\\\"ISIN\\\":\\\"PEP66450D031\\\",\\\"NEMOTECNICO\\\":\\\"INTSE2BS2U                \\\",\\\"MONEDA\\\":\\\"USD\\\",\\\"TIPODECAMBIO\\\":3.251,\\\"VNACTUAL\\\":0.000001,\\\"CIERREPREVIO\\\":0.0001106022,\\\"TREANTERIOR\\\":0.0000057047,\\\"FECHACIERREPREVIO\\\":\\\"23/01/2017\\\",\\\"PULTIMASIG\\\":0,\\\"TREULTIMASIG\\\":0,\\\"HORAULTIMASIG\\\":\\\"               \\\",\\\"PULTIMA\\\":0,\\\"TREULTIMA\\\":0,\\\"QULTIMA\\\":\\\"00000000\\\",\\\"HORAULTIMA\\\":\\\"               \\\",\\\"ULTVSPRECIO\\\":0,\\\"PCOMPRA\\\":0,\\\"PVENTA\\\":0,\\\"QCOMPRA\\\":\\\"00000000\\\",\\\"QVENTA\\\":\\\"00000000\\\",\\\"MNCOMPRA\\\":0,\\\"MNVENTA\\\":0,\\\"PCOMPRAMAX\\\":0,\\\"PVENTAMIN\\\":0,\\\"DIFCOMPRAULTIMA\\\":0,\\\"DIFVENTAULTIMA\\\":0,\\\"PCOMPRAAYER\\\":0,\\\"PVENTAAYER\\\":0,\\\"PAPERTURA\\\":0,\\\"TREAPERTURA\\\":\\\"00000000\\\",\\\"QAPERTURA\\\":0,\\\"PCIERRE\\\":101.9022,\\\"TCIERRE\\\":0.057047,\\\"PMAXIMOSIG\\\":0,\\\"PMINIMOSIG\\\":0,\\\"PPROMEDIO\\\":0,\\\"FUENTEAPERTURA\\\":\\\" \\\",\\\"FUENTECIERRE\\\":\\\"C\\\",\\\"PMAXIMO\\\":0,\\\"PMINIMO\\\":0,\\\"TREMAXIMO\\\":0,\\\"TREMINIMO\\\":0,\\\"QNEGOCIADO\\\":\\\"000000000000000000\\\",\\\"MNNEGOCIADO\\\":0,\\\"MONTOEFECTIVO\\\":0,\\\"MONTOEFECTIVOPEN\\\":0,\\\"NUMOPERACIONES\\\":\\\"000000000000000000\\\",\\\"PINDICATIVOSUBASTA\\\":0,\\\"QNEGOCIABLESUBASTA\\\":\\\"000000000000000000\\\",\\\"QDESEQUILIBRIOSUB\\\":0,\\\"DIRDESEQUILIBRIOSUB\\\":\\\" \\\",\\\"TREINDICATIVASUB\\\":0,\\\"MENSAJEVALIDACION\\\":\\\"OK\\\"},{\\\"FECHAHORA\\\":\\\"21-02-2017 16:31:00\\\",\\\"FECHADERUEDA\\\":\\\"21/02/2017\\\",\\\"MODALIDAD\\\":\\\"            \\\",\\\"MERCADO\\\":\\\"FIXED INCOME                  \\\",\\\"LIBRO\\\":\\\"NORMAL     \\\",\\\"SEGMENTO\\\":\\\"RF1            \\\",\\\"CATEGORIA\\\":\\\"               \\\",\\\"ISIN\\\":\\\"PEP70310M123\\\",\\\"NEMOTECNICO\\\":\\\"REP2BC20A                 \\\",\\\"MONEDA\\\":\\\"USD\\\",\\\"TIPODECAMBIO\\\":3.251,\\\"VNACTUAL\\\":0.000001,\\\"CIERREPREVIO\\\":0.0001120122,\\\"TREANTERIOR\\\":0.0000057047,\\\"FECHACIERREPREVIO\\\":\\\"23/01/2017\\\",\\\"PULTIMASIG\\\":0,\\\"TREULTIMASIG\\\":0,\\\"HORAULTIMASIG\\\":\\\"               \\\",\\\"PULTIMA\\\":0,\\\"TREULTIMA\\\":0,\\\"QULTIMA\\\":\\\"00000000\\\",\\\"HORAULTIMA\\\":\\\"               \\\",\\\"ULTVSPRECIO\\\":0,\\\"PCOMPRA\\\":0,\\\"PVENTA\\\":0,\\\"QCOMPRA\\\":\\\"00000000\\\",\\\"QVENTA\\\":\\\"00000000\\\",\\\"MNCOMPRA\\\":0,\\\"MNVENTA\\\":0,\\\"PCOMPRAMAX\\\":0,\\\"PVENTAMIN\\\":0,\\\"DIFCOMPRAULTIMA\\\":0,\\\"DIFVENTAULTIMA\\\":0,\\\"PCOMPRAAYER\\\":0,\\\"PVENTAAYER\\\":0,\\\"PAPERTURA\\\":0,\\\"TREAPERTURA\\\":\\\"00000000\\\",\\\"QAPERTURA\\\":0,\\\"PCIERRE\\\":101.9022,\\\"TCIERRE\\\":0.057047,\\\"PMAXIMOSIG\\\":0,\\\"PMINIMOSIG\\\":0,\\\"PPROMEDIO\\\":0,\\\"FUENTEAPERTURA\\\":\\\" \\\",\\\"FUENTECIERRE\\\":\\\"C\\\",\\\"PMAXIMO\\\":0,\\\"PMINIMO\\\":0,\\\"TREMAXIMO\\\":0,\\\"TREMINIMO\\\":0,\\\"QNEGOCIADO\\\":\\\"000000000000000000\\\",\\\"MNNEGOCIADO\\\":0,\\\"MONTOEFECTIVO\\\":0,\\\"MONTOEFECTIVOPEN\\\":0,\\\"NUMOPERACIONES\\\":\\\"000000000000000000\\\",\\\"PINDICATIVOSUBASTA\\\":0,\\\"QNEGOCIABLESUBASTA\\\":\\\"000000000000000000\\\",\\\"QDESEQUILIBRIOSUB\\\":0,\\\"DIRDESEQUILIBRIOSUB\\\":\\\" \\\",\\\"TREINDICATIVASUB\\\":0,\\\"MENSAJEVALIDACION\\\":\\\"OK\\\"}]\"}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":102,\"p_IdModulo\":\"PLA\",\"p_CodigoArchivo\":\"VECTOR_PRECIOS_TASA_RIESGO_RV\",\"p_Archivo\":\"[{\\\"CODISIN\\\":\\\"AU000000MNC7\\\",\\\"TICKER\\\":\\\"MNC AU\\\",\\\"EMISOR\\\":\\\"METMINCO LTD\\\",\\\"MONEDA\\\":\\\"AUD\\\",\\\"PRECIO\\\":\\\"0.004\\\",\\\"FUENTE\\\":\\\"BLG\\\",\\\"MENSAJEVALIDACION\\\":\\\"OK\\\"},{\\\"CODISIN\\\":\\\"CA5503443031\\\",\\\"TICKER\\\":\\\"LGC CN\\\",\\\"EMISOR\\\":\\\"LUNA GOLD CORP\\\",\\\"MONEDA\\\":\\\"CAD\\\",\\\"PRECIO\\\":\\\"0.195\\\",\\\"FUENTE\\\":\\\"BLG\\\",\\\"MENSAJEVALIDACION\\\":\\\"OK\\\"},{\\\"CODISIN\\\":\\\"COC04PA00016\\\",\\\"TICKER\\\":\\\"ECOPETL CB\\\",\\\"EMISOR\\\":\\\"ECOPETROL S.A.\\\",\\\"MONEDA\\\":\\\"COP\\\",\\\"PRECIO\\\":\\\"1445\\\",\\\"FUENTE\\\":\\\"BLG\\\",\\\"MENSAJEVALIDACION\\\":\\\"OK\\\"},{\\\"CODISIN\\\":\\\"US4642851053\\\",\\\"TICKER\\\":\\\"IAU US\\\",\\\"EMISOR\\\":\\\"ISHARES GOLD TRUST\\\",\\\"MONEDA\\\":\\\"USD\\\",\\\"PRECIO\\\":\\\"11.99\\\",\\\"FUENTE\\\":\\\"BLG\\\",\\\"MENSAJEVALIDACION\\\":\\\"OK\\\"},{\\\"CODISIN\\\":\\\"US78462F1030\\\",\\\"TICKER\\\":\\\"SPY US\\\",\\\"EMISOR\\\":\\\"STANDARD & POORS DEPOSITARY RECEIPTS SPDR TRUST SERIES 1\\\",\\\"MONEDA\\\":\\\"USD\\\",\\\"PRECIO\\\":\\\"208.9192\\\",\\\"FUENTE\\\":\\\"BLG\\\",\\\"MENSAJEVALIDACION\\\":\\\"OK\\\"}]\"}";
			
			
			EntradaCargaArchivoVectorPrecio Objin = gson.fromJson(json, EntradaCargaArchivoVectorPrecio.class);

			CargaArchivoVectorPrecio demo = new CargaArchivoVectorPrecio();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SalidaCargaArchivoVectorPrecio ObjOut = demo.logica(con, Objin);
				//con.commit();
				System.out.println(ObjOut.toString());
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			//con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

}
