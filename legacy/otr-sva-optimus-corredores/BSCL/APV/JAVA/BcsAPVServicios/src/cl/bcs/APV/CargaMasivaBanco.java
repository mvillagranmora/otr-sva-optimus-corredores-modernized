package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.APV.CrearRecaudacionDeposito.Recaudacion;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaMasivaBanco implements JavaCall {

	//private final Log log = new Log("CargaMasivaBanco");

	private String SQL_DEPOSITOS_POR_CODIGO_ARCHIVO = loadResourceToString("cl/bcs/APV/sql/ConsultarCodigoArchivoRecaudacion.sql");
	private final String SQL_INSERT_CARGA_ARCHIVO = loadResourceToString("cl/bcs/APV/sql/CargaMasivaArchivo_Insert.sql");
	private String SQL_SUCURSAL_POR_ID = loadResourceToString("cl/bcs/APV/sql/SucursalPorId.sql");
	private String SQL_CODIGOBANCO_PREVIRED_POR_IDBANCO = loadResourceToString("cl/bcs/APV/sql/CodigoBancoPreviredPorId.sql");
	private String SQL_EXISTEN_EMPLEADORES = loadResourceToString("cl/bcs/APV/sql/ConsultaExistenEmpleadores.sql");
	private String SQL_DEPOSITOS_PREVIRED_POR_NOMBRE_COMPLETO_ARCHIVO = loadResourceToString("cl/bcs/APV/sql/ConsultarRecaudacionPorNombreCompletoArchivo.sql");
	
	// -- JAVA --//
	CrearRecaudacionDeposito crd = new CrearRecaudacionDeposito();
	CrearRecaudacionEmpleador cre = new CrearRecaudacionEmpleador();
	CrearRecaudacionCliente crc = new CrearRecaudacionCliente();
	CargaMasivaArchivo cma = new CargaMasivaArchivo();

	Long VarIdRecaudacion;

	/********************
	 * Clases IN/OUT *
	 ********************/
	public static class archivoBanco {

		public Long TipoFila;
		public Long Folio;
		public String FechaOperacion;
		public String HoraOperacion;
		public String IdBanco;
		public String TipoInstitucion;
		public String Institucion;
		public String FechaAbono;
		public String HoraAbono;
		public String PeriodoPago;
		public String PeriodoPagoDesde;
		public String PeriodoPagoHasta;
		public String RutPagador;
		public String CuentaCorrientePagador;
		public String NumeroAfiliado;
		public String NumeroContrato;
		public String MontoAbono;
		public String CuentaCorrienteInstitucion;
		public String MENSAJEVALIDACION;
	}

	public static class CargaArchivo {
		public Long IdEmpresa;
		public Long IdCargaMasiva;
		public String NombreCargaMasiva;
		public Long CantidadRegistros;
		public Long IdUsuario;
		public int IdTipoCarga;
		public String Descripcion;
		public String FechaCarga;
		public String IdEstado;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// SERVICIO ENTRADA
	public static class Parametros_Entrada {

		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_NombreCarga;
		public String p_Origen;
		public List<CargaArchivo> p_Archivo;
		public List<archivoBanco> p_Parametros;

		public Parametros_Entrada() {
			p_Parametros = new ArrayList<archivoBanco>();
			p_Archivo = new ArrayList<CargaArchivo>();
		}

	}

	public static class ExisteEmpleador {
		public String DocumentoIdentidad;
		public Long IdPersona;
	};

	public static class DepositosPorCodigoArchivo {
		public Long Cantidad;
	};
	
	public static class Sucursal {
		public Long IdSucursal;
		public String NombreSucursal;
		public Long IdBanco;
		public String IdEstado;
	}
	
	public static class CodigoBanco {
		public Long IdBanco;
		public String CodigoBanco;
	}
	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;

		public String toString() {
			return Utils.printJson(this, false);
		}

		public ParametrosSalidaServicio() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	};

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();
		Parametros_Entrada Objin = gson.fromJson(in.toString(), Parametros_Entrada.class);
		ParametrosSalidaServicio Objout = logica(conn, Objin);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(Objout)).getAsJsonObject();

		return dataout;
	};

	/************************
	 * Metodos Basicos *
	 ************************/
	public ParametrosSalidaServicio logica(Connection con, Parametros_Entrada in) throws Exception {
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		ParametrosSalidaServicio salida = new ParametrosSalidaServicio();
		SQL sql = new SQL();
		HashMap<String, Object> param;
		MensajeSalida msg = new MensajeSalida();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date FechaActual = new java.util.Date();
		df.format(FechaActual);
		List<Long> idsCargaArchivo = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencia::APV.IdCargaMasiva", (long) in.p_Archivo.size());
		
		if(!in.p_NombreCarga.matches("[a-zA-Z0-9]+-[a-zA-Z0-9]+_[a-zA-Z0-9]+_[a-zA-Z0-9]+.[a-zA-Z0-9]+")) {
			msg.id = 1L;
			msg.tipo = "ERROR";
			msg.codigo = "FORMATO_NOMBRE_INVALIDO";
			msg.descripcion = "Error nombre del archivo no es valido.";
			salida.P_OUT_MENSAJES.add(msg);
			
			return salida;
		}
		
		param = new HashMap<>();


		param.put("NombreCargaArchivo", in.p_NombreCarga);
		List<Recaudacion> DepositosPorNombreCargaArchivo = sql.query(con, SQL_DEPOSITOS_PREVIRED_POR_NOMBRE_COMPLETO_ARCHIVO, param, Recaudacion.class);
		
		if (DepositosPorNombreCargaArchivo.size() == 0) {
			
			param = new HashMap<>();
			//IdSucursal 7 es el Id de la sucursal banca en linea.
			param.put("p_IdSucursal", 7);

			List<Sucursal> Sucursal = sql.query(con, SQL_SUCURSAL_POR_ID, param, Sucursal.class);
			param.clear();
			if (Sucursal.size() == 0) {
				msg.id = 1L;
				msg.tipo = "ERROR";
				msg.codigo = "SUCURSAL_ERROR";
				msg.descripcion = "No se encontro la Sucursal Banca en Linea.";
				salida.P_OUT_MENSAJES.add(msg);
				
				return salida;
			}
			List<CrearRecaudacionDeposito.Recaudacion> ParamsRecaudacion = new ArrayList<CrearRecaudacionDeposito.Recaudacion>();	

			
			Long numeroReferencial = 0L;

			HashMap<String,Object> params = new HashMap<String,Object>();
			List<String> rutsEmpleadores = new ArrayList<String>();
			List<ExisteEmpleador> empleadoresExistentes = new ArrayList<ExisteEmpleador>();
			for (CargaMasivaBanco.archivoBanco fila : in.p_Parametros) {
				if("OK".equalsIgnoreCase(fila.MENSAJEVALIDACION)) {
					Long rutLongEmpleador = Long.parseLong(fila.RutPagador.substring(0, fila.RutPagador.length() - 1));
					String rutEmpleador = rutLongEmpleador.toString() + "-" + fila.RutPagador.substring(fila.RutPagador.length() -1, fila.RutPagador.length());
					rutsEmpleadores.add(rutEmpleador);
				}
			}
			
			if(rutsEmpleadores.size() > 0) {
				params.put("p_DoctosIdentidad",rutsEmpleadores);
				empleadoresExistentes = sql.query(con, SQL_EXISTEN_EMPLEADORES, params, ExisteEmpleador.class);				
				params.clear();
			}
			if(empleadoresExistentes.size() == 0) {
				MensajeSalida msg1 = new MensajeSalida();
				msg1.id = (long) numeroReferencial;
				msg1.tipo = "ERROR";
				msg1.codigo = "ERROR";
				msg1.descripcion = "Los Empleadores del archivo no existen en el sistema.";
				salida.P_OUT_MENSAJES.add(msg1);
				salida.P_OUT_ERRORESCONTADOR++;
				return salida;
			} else {
				if(empleadoresExistentes.size() != rutsEmpleadores.size()) {
					List<String> strRutsExts = new ArrayList<String>();
					for(ExisteEmpleador emp : empleadoresExistentes) {
						strRutsExts.add(emp.DocumentoIdentidad);
					}
					for(String rutEmpleador : rutsEmpleadores) {
						if (!strRutsExts.contains(rutEmpleador) ) {
							numeroReferencial++;
							MensajeSalida msg1 = new MensajeSalida();
							msg1.id = (long) numeroReferencial;
							msg1.tipo = "ERROR";
							msg1.codigo = "ERROR";
							msg1.descripcion = "El Empleador RUT : "+rutEmpleador+" no existe en el sistema.";
							salida.P_OUT_MENSAJES.add(msg1);
							salida.P_OUT_ERRORESCONTADOR++;
						}
					}
					if (salida.P_OUT_ERRORESCONTADOR > 0) {
						return salida;
					}
				}
			}
			
			
			for (CargaMasivaBanco.archivoBanco fila : in.p_Parametros) {
				numeroReferencial++;
				Recaudacion PR = new Recaudacion();
	
				param = new HashMap<>();
				param.put("p_CodigoBanco", fila.IdBanco.trim());

				List<CodigoBanco> CodigoBanco = sql.query(con, SQL_CODIGOBANCO_PREVIRED_POR_IDBANCO, param, CodigoBanco.class);
				param.clear();
				if (CodigoBanco.size() == 0) {
					msg.id = 1L;
					msg.tipo = "ERROR";
					msg.codigo = "CODIGOBANCO_PREVIRED_ERROR";
					msg.descripcion = "El Banco: " + fila.IdBanco +" no esta registrado en el sistema";
					salida.P_OUT_MENSAJES.add(msg);
					
					return salida;
				}
				
				// PARAMETROS RECAUDACION
				PR.IdRecaudacion = 0L;
				PR.IdEmpresa = in.p_IdEmpresa;
				PR.CodigoArchivo = in.p_NombreCarga.split("_")[2].split("\\.")[0];
				PR.FolioCartola =fila.Folio;
				PR.LineaCartola = 1L;
				PR.Sucursal = String.valueOf(Sucursal.get(0).IdSucursal);
				PR.FechaDeposito = fila.FechaAbono;
				PR.CuentaCorriente = fila.CuentaCorrienteInstitucion.replaceFirst("^0+", "").replaceAll("\\s+$", "");
				PR.MontoDeposito = BigDecimal.valueOf(Long.parseLong(fila.MontoAbono));
				PR.ConceptoRecaudacion = "";
				PR.Origen = in.p_Origen;
				PR.Observacion = "";
				PR.NumeroDocumento = 1L;
				PR.IdTipoDeposito = "TRA";
				PR.IdBanco = CodigoBanco.get(0).IdBanco;
				PR.IdRegimenTributario = 0L;
				PR.IdCarga = idsCargaArchivo.get(0);
				PR.IdEstado = "ING";
				ParamsRecaudacion.add(PR);
				// FOLIO CARTOLA EXISTE
			}

			CrearRecaudacionDeposito.ParametrosEntradaServicio ObjinRec = new CrearRecaudacionDeposito.ParametrosEntradaServicio();		
			ObjinRec.p_IdEmpresa = in.p_IdEmpresa;
			ObjinRec.p_IdUsuario = in.p_IdUsuario;
			ObjinRec.p_IdModulo = "APV";
			ObjinRec.p_Tipo = "I";
			ObjinRec.p_Origen = in.p_Origen;
			ObjinRec.p_Parametros = ParamsRecaudacion;
			try {
				CrearRecaudacionDeposito.ParametrosSalidaServicio ObjOutDep = crd.logica(con, ObjinRec);
				salida.P_OUT_MENSAJES.addAll(ObjOutDep.P_OUT_MENSAJES);
				salida.P_OUT_ERRORESCONTADOR = ObjOutDep.P_OUT_ERRORESCONTADOR;
				if(salida.P_OUT_ERRORESCONTADOR > 0) return salida;
				System.out.println(ObjOutDep.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			
			System.out.println(idsCargaArchivo.get(0));
			System.out.println(in.p_Archivo.get(0));
			
			in.p_Archivo.get(0).IdCargaMasiva = idsCargaArchivo.get(0);
			in.p_Archivo.get(0).FechaCarga = fechaSistema;
	
			sql.batch(con, SQL_INSERT_CARGA_ARCHIVO, in.p_Archivo, CargaArchivo.class);
		}else{
			msg.id = 1L;
			msg.tipo = "ERROR";
			msg.codigo = "ERROR";
			msg.descripcion = "El archivo " + in.p_NombreCarga + " ya fue cargado previamente.";
			salida.P_OUT_MENSAJES.add(msg);
		}
		return salida;

	}


	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			CargaMasivaBanco CMB = new CargaMasivaBanco();
			try {
				//Parametros_Entrada Objin = new Parametros_Entrada();

				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":15,\"p_IdModulo\":\"APV\",\"p_NombreCarga\":\"Bnaco- AC07BAA_20191212_1648Z.TXT\",\"p_Origen\":\"BANCO\",\"p_Archivo\":[{\"IdEmpresa\":1,\"IdCargaMasiva\":0,\"NombreCargaMasiva\":\"Bnaco-AC07BAA_20191212_1648Z.TXT\",\"CantidadRegistros\":2,\"IdUsuario\":15,\"IdTipoCarga\":3,\"Descripcion\":\"Carga Banco\",\"FechaCarga\":\"2019-12-16T03:00:00.000Z\",\"IdEstado\":\"VIG\"}],\"p_Parametros\":[{\"TipoFila\":2,\"Folio\":2069201912003132,\"FechaOperacion\":\"20191010\",\"HoraOperacion\":\"123036\",\"IdBanco\":\"Banco Security  \",\"TipoInstitucion\":\"IAAPV     \",\"Institucion\":\"Corredores de Bolsa SURA S.A. \",\"FechaAbono\":\"20191212\",\"HoraAbono\":\"090000\",\"PeriodoPago\":\"201912\",\"PeriodoPagoDesde\":\"00000000\",\"PeriodoPagoHasta\":\"00000000\",\"RutPagador\":\"0763090973\",\"CuentaCorrientePagador\":\"165369501                     \",\"NumeroAfiliado\":\"00001\",\"NumeroContrato\":\"12\",\"MontoAbono\":\"000000841456\",\"CuentaCorrienteInstitucion\":\"000004360602                  \",\"MENSAJEVALIDACION\":\"OK\"},{\"TipoFila\":2,\"Folio\":2069201912001131,\"FechaOperacion\":\"20191010\",\"HoraOperacion\":\"121550\",\"IdBanco\":\"Banco Security  \",\"TipoInstitucion\":\"IAAPV     \",\"Institucion\":\"Corredores de Bolsa SURA S.A. \",\"FechaAbono\":\"20191212\",\"HoraAbono\":\"090000\",\"PeriodoPago\":\"201912\",\"PeriodoPagoDesde\":\"00000000\",\"PeriodoPagoHasta\":\"00000000\",\"RutPagador\":\"0088506100\",\"CuentaCorrientePagador\":\"163376301                     \",\"NumeroAfiliado\":\"00001\",\"NumeroContrato\":\"12\",\"MontoAbono\":\"000000025000\",\"CuentaCorrienteInstitucion\":\"000004360602                  \",\"MENSAJEVALIDACION\":\"OK\"}]}";
				
				Parametros_Entrada Objin2 = gson.fromJson(json, Parametros_Entrada.class);
				/*CargaArchivo CA = new CargaArchivo();
				List<CargaArchivo> paramsCargaMasiva = new ArrayList<CargaArchivo>();
				archivoBanco AB = new archivoBanco();
				archivoBanco AB2 = new archivoBanco();

				List<archivoBanco> paramsBanco = new ArrayList<archivoBanco>();
				String periodoString = "2017-12";
				String fechaString = "2017-12-06";
				String horaString = "00:00:00";

				CA.IdEmpresa = 1L;
				CA.IdCargaMasiva = null;
				CA.NombreCargaMasiva = "Banco-AC07BAA_20170710_2034M.txt";
				CA.CantidadRegistros = 1L;
				CA.IdUsuario = 10L;
				CA.IdTipoCarga = 1;
				CA.Descripcion = "Prueba Carga Banco";
				CA.FechaCarga = fechaString;
				CA.IdEstado = "VIG";

				paramsCargaMasiva.add(CA);
				
				AB.TipoFila = 2L;
				AB.Folio = 1111L;
				AB.FechaOperacion = fechaString;
				AB.HoraOperacion = horaString;
				AB.IdBanco = "BancoChile";
				AB.TipoInstitucion = "IAAPV";
				AB.Institucion = "Corredores de Bolsa SURA S.A.";
				AB.FechaAbono = fechaString;
				AB.HoraAbono = horaString;
				AB.PeriodoPago = periodoString;
				AB.PeriodoPagoDesde = periodoString;
				AB.PeriodoPagoHasta = periodoString;
				AB.RutPagador = "0764287908";
				AB.CuentaCorrientePagador = "000004360602";
				AB.NumeroAfiliado = "1";
				AB.NumeroContrato = "08";
				AB.MontoAbono = "200000";
				AB.CuentaCorrienteInstitucion ="209299200";

				paramsBanco.add(AB);
				
				AB2.TipoFila = 2L;
				AB2.Folio = 3333L;
				AB2.FechaOperacion = fechaString;
				AB2.HoraOperacion = horaString;
				AB2.IdBanco = "BancoChile";
				AB2.TipoInstitucion = "IAAPV";
				AB2.Institucion = "Corredores de Bolsa SURA S.A.";
				AB2.FechaAbono = fechaString;
				AB2.HoraAbono = horaString;
				AB2.PeriodoPago = periodoString;
				AB2.PeriodoPagoDesde = periodoString;
				AB2.PeriodoPagoHasta = periodoString;
				AB2.RutPagador = "0764287908";
				AB2.CuentaCorrientePagador = "000004360602";
				AB2.NumeroAfiliado = "1";
				AB2.NumeroContrato = "08";
				AB2.MontoAbono = "200000";
				AB2.CuentaCorrienteInstitucion = "209299200";				
				
				paramsBanco.add(AB2);
				
				Objin.p_IdEmpresa = (long) 1;
				Objin.p_IdUsuario = 10L;
				Objin.p_IdModulo = "APV";
				Objin.p_NombreCarga = "Banco-AC07BAA_20170710_2034M.txt";
				Objin.p_Origen		= "BANCO";
				Objin.p_Archivo = paramsCargaMasiva;
				Objin.p_Parametros = paramsBanco;*/

				try {

					ParametrosSalidaServicio ObjOut = CMB.logica(con, Objin2);
					con.rollback();
					System.out.println(ObjOut.toString());
				} catch (Exception er) {
					er.printStackTrace();
					con.rollback();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("== END ==");
		}
		;
	}

}