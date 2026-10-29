package cl.bcs.APV;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.CargaMasivaArchivo.CargaArchivo;
import cl.bcs.APV.CargaMasivaBanco.DepositosPorCodigoArchivo;
import cl.bcs.APV.ConciliarDeposito.Recaudacion;
import cl.bcs.APV.CrearRecaudacionCliente.Cliente;
import cl.bcs.APV.CrearRecaudacionCliente.ParametrosEntradaServicio;
import cl.bcs.APV.CrearRecaudacionDeposito;
import cl.bcs.APV.CrearRecaudacionEmpleador.Empleador;
import cl.bcs.APV.CrearRecaudacionEmpleador.ParametrosSalidaServicio;
import cl.bcs.APV.ProcesarDistribucion.ParametrosRecaudacion;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.CrearCliente;
import cl.bcs.socionegocio.cli.CrearCliente.EntradaServicioCrearCliente;
import cl.bcs.socionegocio.cli.CrearCliente.SalidaServicioCrearCliente;
import cl.bcs.socionegocio.per.CrearPersona;
import cl.bcs.socionegocio.per.CrearPersona.EntradaServicioCrearPersona;
import cl.bcs.socionegocio.per.CrearPersona.SalidaServicioCrearPersona;
import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.socionegocio.pojo.ErrorDetalle;

import java.math.BigDecimal;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaMasivaPAC implements JavaCall {

	private String SQL_DEPOSITOS_POR_CODIGO_ARCHIVO = loadResourceToString("cl/bcs/APV/sql/ConsultarCodigoArchivoRecaudacion.sql");
	private String SQL_BANCO_POR_CODIGO = loadResourceToString("cl/bcs/APV/sql/ConsultaBancoPorCodigo.sql");
	private String SQL_CUENTA_BANCO_POR_CODIGO = loadResourceToString("cl/bcs/APV/sql/ConsultaCuentaBancoPorCodigo.sql");
	
	private String SQL_EXISTE_FOLIO_PREVIRED = loadResourceToString("cl/bcs/APV/sql/ConsultarRecaudacion.sql");
	private String SQL_EXISTE_CLIENTE = loadResourceToString("cl/bcs/APV/sql/ConsultarExisteCliente.sql");
	private String SQL_EXISTE_CLIENTE_RECAUDACION = loadResourceToString("cl/bcs/APV/sql/ConsultarExisteClienteRecaudacion.sql");
	private String SQL_EXISTE_PERSONA = loadResourceToString("cl/bcs/APV/sql/ConsultarPersona.sql");
	private final String SQL_INSERT_CARGA_ARCHIVO = loadResourceToString("cl/bcs/APV/sql/CargaMasivaArchivo_Insert.sql");
	
	private String SQL_CLIENTES_POR_RECAUDACION = loadResourceToString("cl/bcs/APV/sql/ConsultarClientesPorRecaudacion.sql");
	private String SQL_CUENTA_POR_CODIGOBANCO = loadResourceToString("cl/bcs/APV/sql/ConsultaCuentaBancoPorCodigo.sql");
	private String SQL_IDCLIENTE_POR_CUENTABANCARIA = loadResourceToString("cl/bcs/APV/sql/ConsultarIdClientePorCuentaBancaria.sql");
	private String SQL_SUCURSAL_POR_ID = loadResourceToString("cl/bcs/APV/sql/SucursalPorId.sql");
	

	
	// -- JAVA --//
	CrearRecaudacionDeposito crd = new CrearRecaudacionDeposito();
	CrearRecaudacionCliente crc = new CrearRecaudacionCliente();
	CargaMasivaArchivo cma = new CargaMasivaArchivo();
	ConciliarDeposito cd = new ConciliarDeposito();
	ProcesarDistribucion pd = new ProcesarDistribucion();

	Long VarIdRecaudacion;

	/********************
	 * Clases IN/OUT *
	 ********************/
	public static class ArchivoPAC {

		public String 	CodigoBanco;
		public String 	CodigoEmpresa;
		public String 	CodigoConvenio;
		public String 	TipoRegistro;
		public String 	IdServicioEmpresa;
		public String 	InformacionAdicional;
		public Long		MontoCargo;
		public String 	FechaFacturacion;
		public String 	FechaVencimiento;
	}

	public static class TotalPAC {

		public String CodigoBanco;
		public String CodigoEmpresa;
		public String CodigoConvenio;
		public String TipoRegistro;
		public String MontoTotalCargo;
		public String NumeroRegistros;
	}
	
	public static class Sucursal {
		public Long IdSucursal;
		public String NombreSucursal;
		public Long IdBanco;
		public String IdEstado;
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
		public Long p_IdEmpleado;
		public List<CargaArchivo> p_Archivo;
		public List<ArchivoPAC> p_ParametrosArchivo;
		public List<TotalPAC> p_ParametrosTotalArchivo;

		public Parametros_Entrada() {
			p_ParametrosTotalArchivo = new ArrayList<TotalPAC>();
			p_ParametrosArchivo = new ArrayList<ArchivoPAC>();
			p_Archivo = new ArrayList<CargaArchivo>();
		}
	}

	public static class ExistePersona {
		public Long IdRecaudacionEmpleador;
		public String DocumentoIdentidad;
		public Long IdPersona;
	};
	
	public static class Banco {
		public Long IdBanco;
		public String NumeroCuentaBancaria;
	};
	
	public static class ClienteCuenta {
		public Long IdCliente;
	};
	
	public static class ExisteEmpleador {
		public Long IdRecaudacionEmpleador;
		public Long IdPersona;
	};
	
	public static class ExisteCliente {
		public Long IdCliente;
	};

	public static class ConsultaFolio {
		public Long FolioCartola;
	};
	
	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;

		public String toString() {
			return Utils.printJson(this, false);
		}

		public ParametrosSalidaServicio() {
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
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
	 * Métodos Básicos *
	 ************************/
	@SuppressWarnings("unused")
	public ParametrosSalidaServicio logica(Connection con, Parametros_Entrada in) throws Exception {
		ParametrosSalidaServicio salida = new ParametrosSalidaServicio();
		SQL sql = new SQL();
		HashMap<String, Object> param;
        Date Fecha = Date.valueOf("1900-01-01");
        Long time = System.currentTimeMillis();
		java.sql.Date FechaSistema = new java.sql.Date(time);
		Timestamp Fechatime	= new java.sql.Timestamp(FechaSistema.getTime());
        Long IdRecaudacionEmpleador = 0L;
		param = new HashMap<>();
		MensajeSalida msg = new MensajeSalida();
				
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA","PLA.FechaSistema");
		List<Long> idsCargaArchivo = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencia::APV.IdCargaMasiva", (long) in.p_Archivo.size());
		java.sql.Date FechaDeposito = new java.sql.Date(time);	
		DateFormat FormatoFecha = new SimpleDateFormat("yyyy-MM-dd");			
		
		
		String CodigoArchivo = "";

		String [] CodigoArchivoExtencion = in.p_NombreCarga.split("_");
		if(CodigoArchivoExtencion.length > 5){
			String [] CodigoArchivoList = CodigoArchivoExtencion[CodigoArchivoExtencion.length - 1].split("\\.");
			if(CodigoArchivoList.length > 0){
				CodigoArchivo = CodigoArchivoList[0];
			}else{
				msg.id = 1L;
				msg.tipo = "ERROR";
				msg.codigo = "ERROR";
				msg.descripcion = "Formato Nombre de Archivo Incorrecto";
				salida.P_OUT_MENSAJES.add(msg);
				
				return salida;
			}
		}else{
			msg.id = 1L;
			msg.tipo = "ERROR";
			msg.codigo = "ERROR";
			msg.descripcion = "Formato Nombre de Archivo Incorrecto";
			salida.P_OUT_MENSAJES.add(msg);
			
			return salida;
		}
		
		param.put("CodigoArchivo", CodigoArchivo);
		List<Recaudacion> DepositosPorCodigo = sql.query(con, SQL_DEPOSITOS_POR_CODIGO_ARCHIVO, param, Recaudacion.class);

		if (DepositosPorCodigo.size() > 0) {
			msg.id = 1L;
			msg.tipo = "ERROR";
			msg.codigo = "ERROR";
			msg.descripcion = "El archivo ya fue cargado previamente.";
			salida.P_OUT_MENSAJES.add(msg);

			return salida;
		}
		//DEPOSITO

		Long IdRecaudacion = 0L;
		Long ContadorMSG = 0L;
				
		for (TotalPAC fila : in.p_ParametrosTotalArchivo) {

			ContadorMSG = ContadorMSG+1;
			CrearRecaudacionDeposito.ParametrosEntradaServicio EntradaDeposito 	= new CrearRecaudacionDeposito.ParametrosEntradaServicio();
			CrearRecaudacionDeposito.Recaudacion dep = new CrearRecaudacionDeposito.Recaudacion();
			CrearRecaudacionDeposito.ParametrosSalidaServicio DepOut = new CrearRecaudacionDeposito.ParametrosSalidaServicio();
			CrearRecaudacionDeposito rec = new CrearRecaudacionDeposito ();
			
			List<Long> idsRecaudacion = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencia::REC.IdRecaudacion", (long)in.p_ParametrosTotalArchivo.size());

			param = new HashMap<>();
			param.put("p_CodigoBanco", fila.CodigoBanco);

			List<Banco> Banco = sql.query(con, SQL_CUENTA_POR_CODIGOBANCO, param, Banco.class);
						
			if (Banco.size() == 0) {
				msg.id = ContadorMSG;//IdRecaudacion;
				msg.tipo = "ERROR";
				msg.codigo = "CUENTA_BANCO_ERROR";
				msg.descripcion = "No se encontro una cuenta asociada al código de banco ingresado.";
				salida.P_OUT_MENSAJES.add(msg);
				
				return salida;
			}
			
			param = new HashMap<>();
			//IdSucursal 7 es el Id de la sucursal banca en linea.
			param.put("p_IdSucursal", 7);

			List<Sucursal> Sucursal = sql.query(con, SQL_SUCURSAL_POR_ID, param, Sucursal.class);
			
			if (Sucursal.size() == 0) {
				msg.id = ContadorMSG;//IdRecaudacion;
				msg.tipo = "ERROR";
				msg.codigo = "SUCURSAL_ERROR";
				msg.descripcion = "No se encontro la Sucursal Banca en Línea.";
				salida.P_OUT_MENSAJES.add(msg);
				
				return salida;
			}
			
			dep.IdRecaudacion = idsRecaudacion.get(0); 
			dep.IdEmpresa = in.p_IdEmpresa;
			dep.CodigoArchivo = CodigoArchivo;
			dep.FolioCartola = 0L; 			
			dep.LineaCartola = 0L; 						
			dep.Sucursal = String.valueOf(Sucursal.get(0).IdSucursal); 					
			dep.FechaDeposito = fechaSistema;				
			dep.CuentaCorriente = Banco.get(0).NumeroCuentaBancaria;							
			dep.MontoDeposito = BigDecimal.valueOf(Long.parseLong(fila.MontoTotalCargo));					
			dep.ConceptoRecaudacion = "";							
			dep.Origen = in.p_Origen; 							
			dep.Observacion = "";							
			dep.NumeroDocumento = 0L; 					
			dep.IdTipoDeposito = "TRA";						
			dep.IdBanco = Banco.get(0).IdBanco;
			dep.IdRegimenTributario = 0L;						
			dep.IdCarga = idsCargaArchivo.get(0);
			dep.IdEstado = "ING";
				
			List<CrearRecaudacionDeposito.Recaudacion> listaDepositos = new ArrayList<CrearRecaudacionDeposito.Recaudacion>();
			listaDepositos.add(dep);
			
			EntradaDeposito.p_IdEmpresa = in.p_IdEmpresa;
			EntradaDeposito.p_IdUsuario = in.p_IdUsuario;
			EntradaDeposito.p_IdModulo = "APV";
			EntradaDeposito.p_Tipo = "I";
			EntradaDeposito.p_Origen = "CARGA";
			EntradaDeposito.p_Parametros = listaDepositos;
				
			CrearRecaudacionDeposito depo = new CrearRecaudacionDeposito ();
							
			try {
				DepOut = depo.logica(con, EntradaDeposito);
				System.out.println(DepOut.toString());
				con.commit();
			} catch (Exception er) {
					er.printStackTrace();
					con.rollback();
					
					msg = new MensajeSalida();
					
					msg.id = ContadorMSG;//1L;
					msg.tipo = "ERROR";
					msg.codigo = "ERROR";
					msg.descripcion = er.getMessage();
					salida.P_OUT_MENSAJES.add(msg);
					
					return salida;
			}	
			

			if(DepOut.P_OUT_ERRORESCONTADOR == 0){

				for(MensajeSalida msgDep : DepOut.P_OUT_MENSAJES){
					msg = new MensajeSalida();
					msg.id = ContadorMSG;
					msg.tipo = msgDep.tipo;
					msg.codigo = msgDep.codigo;
					msg.descripcion = msgDep.descripcion;
					salida.P_OUT_MENSAJES.add(msg);
				}
				
				//salida.P_OUT_MENSAJES.addAll(DepOut.P_OUT_MENSAJES);
			    IdRecaudacion = DepOut.P_OUT_IDREC;
				/*
				msg.id = IdRecaudacion;
				msg.tipo = "OK";
				msg.codigo = "CREACION_DEPOSITO_OK";
				msg.descripcion = "Creacion de depósito exitosa";
				salida.P_OUT_MENSAJES.add(msg);*/
			}else{
				for (PlataformaUtils.MensajeSalida mensaje : DepOut.P_OUT_MENSAJES) {
					msg = new MensajeSalida();
					msg.id = ContadorMSG;//mensaje.id;
					msg.tipo = mensaje.tipo;
					msg.codigo = mensaje.codigo;
					msg.descripcion = mensaje.descripcion;
					
					salida.P_OUT_MENSAJES.add(msg);
				}	
				
				return salida;
			}
		}
		
		//CLIENTE

		Long CliIngresados = 0L;
		Long CliRechazados = 0L;
		
		for (ArchivoPAC fila : in.p_ParametrosArchivo) {

			String CuentaCorriente 	= fila.IdServicioEmpresa.substring(0, 14);
			String CodigoBanco		= fila.IdServicioEmpresa.substring(14, 17);
			
			param = new HashMap<>();
			param.put("p_CodigoBanco", CodigoBanco);
			param.put("p_NumeroCuentaBancaria", CuentaCorriente); 

			List<ClienteCuenta> Cliente = sql.query(con, SQL_IDCLIENTE_POR_CUENTABANCARIA, param, ClienteCuenta.class);
			
			if (Cliente.size() > 0) {
				
				// PARAMETROS CLIENTE
				CrearRecaudacionCliente.Cliente CLI = new CrearRecaudacionCliente.Cliente();
				CrearRecaudacionCliente.ParametrosEntradaServicio ObjinCli = new CrearRecaudacionCliente.ParametrosEntradaServicio();
				CLI.IdRecaudacionCliente = 0L;
				CLI.IdRecaudacion = IdRecaudacion;
				CLI.IdRecaudacionEmpleador = null;
				CLI.IdEmpresa = in.p_IdEmpresa;
				
				//Con Cuenta Corriente y banco buscar IdPersona
				//si hay mas de 1 validar cual tiene contrato sappv activo.
				// Buscar el IdCliente con el IdPersona, sino hay, no agregar cliente y continuar.
				
				CLI.IdCliente = Cliente.get(0).IdCliente;
				CLI.FechaIngreso = fechaSistema;
				CLI.TipoAfiliado = "IND";
				CLI.IdRegimenTriburario = "";
				CLI.MontoDepositoConvenido = BigDecimal.valueOf(0L);
				CLI.MontoDepositoAPV = BigDecimal.valueOf(fila.MontoCargo);
				CLI.Origen = "CARGAPAC";
				CLI.IdEstado = "ING";
				List<CrearRecaudacionCliente.Cliente> ParamsCliente = new ArrayList<CrearRecaudacionCliente.Cliente>();
				ParamsCliente.add(CLI);
				ObjinCli.p_IdEmpresa = in.p_IdEmpresa;
				ObjinCli.p_IdUsuario = in.p_IdUsuario;
				ObjinCli.p_IdModulo = "APV";
				ObjinCli.p_Tipo = "I";
				ObjinCli.p_Origen = "CARGA";
					ObjinCli.p_Parametros = ParamsCliente;
				try {
					CrearRecaudacionCliente.ParametrosSalidaServicio ObjOutCLi = crc.logica(con, ObjinCli);
					
					if(ObjOutCLi.P_OUT_ERRORESCONTADOR == 0){
						msg = new MensajeSalida();

						msg.id = ContadorMSG++;//IdRecaudacion;
						msg.tipo = "OK";
						msg.codigo = "CREACION_CLIENTE_OK";
						msg.descripcion = "Detalle Cliente Ingresado";
						salida.P_OUT_MENSAJES.add(msg);
						CliIngresados++;
						
					}else{
						for (PlataformaUtils.MensajeSalida mensaje : ObjOutCLi.P_OUT_MENSAJES) {

							msg = new MensajeSalida();
							
							msg.id = ContadorMSG++;//mensaje.id;
							msg.tipo = mensaje.tipo;
							msg.codigo = mensaje.codigo;
							msg.descripcion = mensaje.descripcion;
							salida.P_OUT_MENSAJES.add(msg);
							CliRechazados++;
						}	
						
						return salida;
					}
					
					con.commit();
					System.out.println(ObjOutCLi.toString());
				} catch (Exception er) {
						er.printStackTrace();
						con.rollback();
						
						msg = new MensajeSalida();
						
						msg.id = ContadorMSG++;//1L;
						msg.tipo = "ERROR";
						msg.codigo = "ERROR";
						msg.descripcion = er.getMessage();
						salida.P_OUT_MENSAJES.add(msg);
				}		
			}else{
				msg = new MensajeSalida();

				msg.id = ContadorMSG++;//1L;
				msg.tipo = "ERROR";
				msg.codigo = "ERROR";
				msg.descripcion = "No existe un Cliente con Codigo Banco " + CodigoBanco + " y Numero de Cuenta " + CuentaCorriente ;
				salida.P_OUT_MENSAJES.add(msg);
				CliRechazados++;
			}
		}
		
		in.p_Archivo.get(0).IdCargaMasiva = idsCargaArchivo.get(0);
		in.p_Archivo.get(0).FechaCarga = fechaSistema;
		
		int[] res = sql.batch(con, SQL_INSERT_CARGA_ARCHIVO, in.p_Archivo, CargaArchivo.class);
		ContadorMSG = ContadorMSG+1;
		if (res != null && res.length > 0 && res[0] == 1) {
			msg = new MensajeSalida();

			msg.id = ContadorMSG;
			msg.tipo = "INFORMACION";
			msg.codigo = "INFORMACION";
			msg.descripcion = "Total Detalle Clientes. Ingresados: " + CliIngresados + " Rechazados: " + CliRechazados + ".";
			salida.P_OUT_MENSAJES.add(msg);
			
			//CONCILIAR;
			
			ConciliarDeposito.Recaudacion IdRec = new ConciliarDeposito.Recaudacion();
			ConciliarDeposito.ParametrosEntradaServicio ObjinCon = new ConciliarDeposito.ParametrosEntradaServicio();
			
			IdRec.IdRecaudacion = IdRecaudacion;

			List<ConciliarDeposito.Recaudacion> ParamsConci = new ArrayList<ConciliarDeposito.Recaudacion>();
			ParamsConci.add(IdRec);
			ObjinCon.p_IdEmpresa = in.p_IdEmpresa;
			ObjinCon.p_IdUsuario = in.p_IdUsuario;
			ObjinCon.p_IdModulo = "APV";
			ObjinCon.p_Parametros = ParamsConci;
						
			ConciliarDeposito.ParametrosSalidaServicio ConciliarDeposito= cd.logica(con, ObjinCon);
			
			for (MensajeSalida fila : ConciliarDeposito.P_OUT_MENSAJES) {
				msg = new MensajeSalida();

				msg.id = ContadorMSG;//fila.id;
				msg.tipo = fila.tipo;
				msg.codigo = fila.codigo;
				msg.descripcion = fila.descripcion;
				salida.P_OUT_MENSAJES.add(msg);	
				
				if(fila.codigo =="CONCILIADO_OK"){
					//DISTRIBUIR.		
								
					ParametrosRecaudacion pRec = new ParametrosRecaudacion();
					List<ParametrosRecaudacion> ListRec = new ArrayList<ParametrosRecaudacion>();
					ProcesarDistribucion.ParametrosEntradaServicio paramDis = new ProcesarDistribucion.ParametrosEntradaServicio();

					pRec.IdRecaudacion = IdRecaudacion;
							
					ListRec.add(pRec);
						
					paramDis.p_IdEmpresa = in.p_IdEmpresa;
					paramDis.p_IdModulo	= in.p_IdModulo;
					paramDis.p_IdUsuario =in.p_IdUsuario;
					paramDis.p_Opcion = "PAC";
					paramDis.p_Parametros = ListRec;
				
					ProcesarDistribucion.ParametrosSalidaServicio ProcesarDistibucion = pd.logica(con, paramDis);
						
					for (MensajeSalida row : ProcesarDistibucion.P_OUT_MENSAJES) {
						msg = new MensajeSalida();

						msg.id = ContadorMSG;
						msg.tipo = row.tipo;
						msg.codigo = row.codigo;
						msg.descripcion = row.descripcion;
						salida.P_OUT_MENSAJES.add(msg);	
						
						if(row.codigo == "OK"){
							return salida;
						}
					}
				}
			}
		}
		con.commit();
		return salida;
	}

	/********************
	 * Método Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getQAConnection()) {
			CargaMasivaPAC CMP = new CargaMasivaPAC();
			try {
				Parametros_Entrada Objin = new Parametros_Entrada();

				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":17,\"p_IdModulo\":\"APV\",\"p_NombreCarga\":\"ARCHIVO_CARGO_10_4_2018_133504.txt\",\"p_Origen\":\"PAC\",\"p_IdEmpleado\":7312,\"p_Archivo\":[{\"IdEmpresa\":1,\"IdCargaMasiva\":0,\"NombreCargaMasiva\":\"ARCHIVO_CARGO_10_4_2018_133504.txt\",\"CantidadRegistros\":3,\"IdUsuario\":17,\"IdTipoCarga\":1,\"Descripcion\":\"Carga PAC\",\"FechaCarga\":\"2018-05-20T04:00:00.000Z\",\"IdEstado\":\"VIG\"}],\"p_ParametrosArchivo\":[{\"CodigoBanco\":\"016\",\"CodigoEmpresa\":\"006\",\"CodigoConvenio\":\"023\",\"TipoRegistro\":\"D\",\"IdServicioEmpresa\":\"000022222233400160    \",\"InformacionAdicional\":\"          \",\"MontoCargo\":600000,\"FechaFacturacion\":\"20180508\",\"FechaVencimiento\":\"20180508\",\"NroRegistro\":1,\"MENSAJEVALIDACION\":\"OK\"},{\"CodigoBanco\":\"012\",\"CodigoEmpresa\":\"006\",\"CodigoConvenio\":\"023\",\"TipoRegistro\":\"D\",\"IdServicioEmpresa\":\"000000001111110126    \",\"InformacionAdicional\":\"          \",\"MontoCargo\":500000,\"FechaFacturacion\":\"20180508\",\"FechaVencimiento\":\"20180508\",\"NroRegistro\":2,\"MENSAJEVALIDACION\":\"OK\"},{\"CodigoBanco\":\"016\",\"CodigoEmpresa\":\"006\",\"CodigoConvenio\":\"023\",\"TipoRegistro\":\"D\",\"IdServicioEmpresa\":\"000006666663340169    \",\"InformacionAdicional\":\"          \",\"MontoCargo\":700000,\"FechaFacturacion\":\"20180508\",\"FechaVencimiento\":\"20180508\",\"NroRegistro\":3,\"MENSAJEVALIDACION\":\"OK\"}],\"p_ParametrosTotalArchivo\":[{\"CodigoBanco\":\"001\",\"CodigoEmpresa\":\"006\",\"CodigoConvenio\":\"023\",\"TipoRegistro\":\"T\",\"MontoTotalCargo\":1800000,\"NumeroDeRegistros\":\"000001\",\"NroRegistro\":1,\"MENSAJEVALIDACION\":\"OK\"}]}";
				//Previred-PL07BAA_20170710_2034C.TXT.
				Parametros_Entrada Objin2 = gson.fromJson(json, Parametros_Entrada.class);
				CargaArchivo CA = new CargaArchivo();
				List<CargaArchivo> paramsCargaMasiva = new ArrayList<CargaArchivo>();
				String fechaString = "2017-11-30";

				CA.IdEmpresa = 1L;
				CA.IdCargaMasiva = null;
				CA.NombreCargaMasiva = "Previred-PL07BAA_20170710_2034C.txt";
				CA.CantidadRegistros = 1L;
				CA.IdUsuario = 10L;
				CA.IdTipoCarga = 1;
				CA.Descripcion = "Prueba Carga Previred";
				CA.FechaCarga = fechaString;
				CA.IdEstado = "VIG";

				try {
					ParametrosSalidaServicio ObjOut = CMP.logica(con, Objin2);
					con.commit();
					//System.out.println(ObjOut.toString());
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