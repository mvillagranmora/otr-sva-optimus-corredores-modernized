package cl.bcs.APV;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.CrearRecaudacionDeposito.Recaudacion;
import cl.bcs.mesa.FWD.CrearOperacionForward.OperacionForward;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import java.math.BigDecimal;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.Log;

public class CargaMasivaPrevired implements JavaCall {
	private final Log log = new Log("APV.CargaMasivaPrevired");

	private String SQL_DEPOSITOS_POR_CODIGO_ARCHIVO = loadResourceToString("cl/bcs/APV/sql/ConsultarCodigoArchivoRecaudacion.sql");
	private String SQL_EMPLEADORES_POR_CODIGO_ARCHIVO = loadResourceToString("cl/bcs/APV/sql/ConsultarCodigoArchivoEmpleador.sql");

	private String SQL_EXISTE_EMPLEADOR = loadResourceToString("cl/bcs/APV/sql/ConsultaExisteEmpleador.sql");
	private String SQL_EXISTE_EMPLEADOR_RECAUDACION = loadResourceToString("cl/bcs/APV/sql/ConsultarExisteEmpleadorRecaudacion.sql");
	private String SQL_EXISTE_CLIENTE = loadResourceToString("cl/bcs/APV/sql/ConsultarExisteCliente.sql");
	private final String SQL_INSERT_CARGA_ARCHIVO = loadResourceToString("cl/bcs/APV/sql/CargaMasivaArchivo_Insert.sql");
	private String SQL_CONSULTAR_CARGA_ARCHIVO_POR_NOMBRE_COMPLETO_ARCHIVO = loadResourceToString("cl/bcs/APV/sql/ConsultarRecaudacionesEmpleadorPorNombreCompletoArchivo.sql");
	
	
	// -- JAVA --//
	CrearRecaudacionDeposito crd = new CrearRecaudacionDeposito();
	CrearRecaudacionEmpleador cre = new CrearRecaudacionEmpleador();
	CrearRecaudacionCliente crc = new CrearRecaudacionCliente();
	CargaMasivaArchivo cma = new CargaMasivaArchivo();

	Long VarIdRecaudacion;

	/********************
	 * Clases IN/OUT *
	 ********************/
	public static class archivoPrevired {

		public Long fila1;
		public Long Folio;
		public Long Tipo_Pag;
		public Long FechaOperacion;
		public String HoraOperacion;
		public Long INS_APV;
		public String Rut_Empleador;
		public String Tipo_Empleador;
		public String Email_Empleador;
		// ---
		public Long fila2;
		public String RazonSocial_Empleador;
		public String Rut_Empleador1;
		public Long Act_Econ_Empleador;
		public String Direccion_Empleador;
		public String Nro_Direccion_Empleador;
		public String Departamento_Empleador;
		public String Comuna_Empleador;
		public String Ciudad_Empleador;
		public String Region_Empleador;
		public String Telefono_Empleador;
		public String Nom_Rep_Legal_Empleador;
		public String Rut_Rep_Legal_Empleador;
		// --
		public Long fila3;
		public String Rut_Trabajador;
		public String Ape_Pat_Trabajador;
		public String Ape_Mat_Trabajador;
		public String Nom_Trabajador;
		public String Sexo_Trabajador;
		public Long Renta_Imponible;
		public Long Monto_APV;
		public Long Monto_DC;
		public Long Monto_Total;
		public String Periodo_Pago;
		// --
		public Long fila4;
		public Long Monto_Total_APV;
		public Long Monto_Total_DC;
		public Long Monto_Total1;
		public Long Periodo_Pago1;
		public Long Num_Afiliados;
		// --
		public String MENSAJEVALIDACION;
		public Long NroRegistro;
	}

	public static class Empleador {

		public Long IdRecaudacionEmpleador;
		public Long IdRecaudacion;
		public Long IdEmpresa;
		public Long IdPersona;
		public String FechaIngreso;
		public String PeriodoPago;
		public Long NumeroHojas;
		public String TipoAfiliado;
		public String TipoPago;
		public String Insistencias;
		public Long MontoDepositoConvenido;
		public Long MontoDepositoAPV;
		public Long Reajustes;
		public Long Intereses;
		public String Origen;
		public String Observaciones;
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
		public List<archivoPrevired> p_Parametros;

		public Parametros_Entrada() {
			p_Parametros = new ArrayList<archivoPrevired>();
			p_Archivo = new ArrayList<CargaArchivo>();
		}

	}

	public static class ExistePersona {
		public Long IdRecaudacionEmpleador;
		public String DocumentoIdentidad;
		public Long IdPersona;
	};
	
	public static class ExisteDetalleEmpleador {
		public Long IdRecaudacionEmpleador;
		public Long IdPersona;
	};
	
	public static class ExisteEmpleador {
		public Long IdPersona;
		public String PersonaBusqueda;
	};
	
	public static class ExisteCliente {
		public Long IdCliente;
	};

	public static class ConsultaFolio {
		public Long FolioCartola;
	};
	
	public static class ConsultaCargaArchivoExiste {
		public Long IdCargaMasiva;
		public String NombreCargaArchivo;
		public Long CantidadRegistros;
		public Long IdUsuario;
		public Long IdTipoCarga;
		public String Descripcion;
		public String FechaCarga;
		public String IdEstado;
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
			P_OUT_ERRORESCONTADOR = 0L;
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
		ParametrosSalidaServicio salida = new ParametrosSalidaServicio();
		SQL sql = new SQL();
		HashMap<String, Object> param;
        Long IdRecaudacionEmpleador = 0L;
		param = new HashMap<>();
		
		String CodigoArchivo = "";

		String [] CodigoArchivoExtencion = in.p_NombreCarga.split("_");
		log.write("INICIO :: APV.CargaMasivaPrevired");

		if(CodigoArchivoExtencion.length > 2){
			String [] CodigoArchivoList = CodigoArchivoExtencion[2].split("\\.");
			if(CodigoArchivoList.length > 0){
				CodigoArchivo = CodigoArchivoList[0];
			}else{
				MensajeSalida msg = new MensajeSalida();
				msg.id = 1L;
				msg.tipo = "ERROR";
				msg.codigo = "ERROR";
				msg.descripcion = "Formato Nombre de Archivo Incorrecto";
				salida.P_OUT_MENSAJES.add(msg);
				salida.P_OUT_ERRORESCONTADOR++;
				return salida;
			}
		}else{
			MensajeSalida msg = new MensajeSalida();
			msg.id = 1L;
			msg.tipo = "ERROR";
			msg.codigo = "ERROR";
			msg.descripcion = "Formato Nombre de Archivo Incorrecto";
			salida.P_OUT_MENSAJES.add(msg);
			salida.P_OUT_ERRORESCONTADOR++;
			return salida;
		}
		
		param.put("CodigoArchivo", CodigoArchivo);
		List<Recaudacion> DepositosPorCodigo = sql.query(con, SQL_DEPOSITOS_POR_CODIGO_ARCHIVO, param, Recaudacion.class);

		if (DepositosPorCodigo.size() > 0) {
			param = new HashMap<>();

			param.put("NombreCargaArchivo", in.p_NombreCarga);
			List<ConsultaCargaArchivoExiste> DepositosPorCodigoEmpleador = sql.query(con, SQL_CONSULTAR_CARGA_ARCHIVO_POR_NOMBRE_COMPLETO_ARCHIVO, param, ConsultaCargaArchivoExiste.class);
			
			if (DepositosPorCodigoEmpleador.size() == 0) {
			
				String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA","PLA.FechaSistema");
				List<Long> idsCargaArchivo = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencia::APV.IdCargaMasiva",
						(long) in.p_Archivo.size());
				
				String DocumentoIdentidadAnterior = "";
				Long IdRecaudacionEmpleadorAnterior = 0L;
				Long IdPersonaEmpleadorAnterior = 0L;
				Long FolioRecaudacioAnterior = 0L;
				
				Long IdPersona = 0L;					
				Long IdRecaudacionDeposito = 0L;
				
				CrearRecaudacionCliente.ParametrosEntradaServicio ObjinCli = new CrearRecaudacionCliente.ParametrosEntradaServicio();
				List<CrearRecaudacionCliente.Cliente> ParamsCliente = new ArrayList<CrearRecaudacionCliente.Cliente>();
				
				Long EmpIngresados = 0L;
				Long EmpRechazados = 0L;
				Long CliIngresados = 0L;
				Long CliRechazados = 0L;
				
				for (CargaMasivaPrevired.archivoPrevired fila : in.p_Parametros) {
					//EMPLEADOR
					Long rutLong = Long.parseLong(fila.Rut_Empleador.substring(0, fila.Rut_Empleador.length() - 1));
					String rutPer = rutLong.toString() + "-" + fila.Rut_Empleador.substring(fila.Rut_Empleador.length() - 1, fila.Rut_Empleador.length());
					
					if(DocumentoIdentidadAnterior.equals(rutPer) == false){
						param = new HashMap<>();
						
						param.put("p_DoctoIdentidad", rutPer);
						
						List<ExisteEmpleador> ExisteEmpleador = sql.query(con, SQL_EXISTE_EMPLEADOR, param,ExisteEmpleador.class);
						
						if(ExisteEmpleador.size() == 0){
							MensajeSalida msg1 = new MensajeSalida();
							msg1.id = salida.P_OUT_MENSAJES.size()+1L;
							msg1.tipo = "ADVERTENCIA"; //filaMsg.tipo;
							msg1.codigo = "ADVERTENCIA"; //filaMsg.codigo;
							msg1.descripcion = "El Empleador con Documento Identidad: " + rutPer + " no es un Empleador valido. Detalle Empleador y Detalle Cliente no ingresados.";
							salida.P_OUT_MENSAJES.add(msg1);	
							DocumentoIdentidadAnterior = rutPer;
							IdRecaudacionEmpleadorAnterior = 0L;
							IdPersonaEmpleadorAnterior = 0L;
							EmpRechazados++;
							CliRechazados++;
							
							continue;
						} 
						if (ExisteEmpleador.size() == 1) {
							IdPersona = ExisteEmpleador.get(0).IdPersona;
						} else {
							for (ExisteEmpleador emp : ExisteEmpleador) {
								if (emp.PersonaBusqueda.equalsIgnoreCase(rutPer.trim() + " " + fila.RazonSocial_Empleador.trim())) {
									IdPersona = emp.IdPersona;
								}
							}
						}
						
						param = new HashMap<>();
						param.put("p_IdEmpresa", in.p_IdEmpresa);
						param.put("p_DoctoIdentidad", rutPer);
						List<ExistePersona> ExisteDetalleEmpleador = sql.query(con, SQL_EXISTE_EMPLEADOR_RECAUDACION, param,ExistePersona.class);
						
						//Asignamos a IdPersona el identificador de la persona juridica
						
						IdRecaudacionDeposito = 0L;
						
						IdRecaudacionDeposito = obtenerIdRecaudacionDeposito(DepositosPorCodigo, IdRecaudacionDeposito,
								fila);
						
						// PARAMETROS EMPLEADOR
						CrearRecaudacionEmpleador.Empleador EMP = new CrearRecaudacionEmpleador.Empleador();
						CrearRecaudacionEmpleador.ParametrosEntradaServicio ObjinEmp = new CrearRecaudacionEmpleador.ParametrosEntradaServicio();
						EMP.IdRecaudacionEmpleador = 0L;
						EMP.IdRecaudacion = IdRecaudacionDeposito;
						EMP.CodigoArchivo = in.p_NombreCarga.split("_")[2].split("\\.")[0];
						EMP.IdEmpresa = in.p_IdEmpresa;
						EMP.IdPersona = ExisteDetalleEmpleador.size() == 0 ? IdPersona : ExisteDetalleEmpleador.get(0).IdPersona ;
						EMP.FechaIngreso = fechaSistema;
						EMP.PeriodoPago = fila.Periodo_Pago.substring(0, 4) + "-" + fila.Periodo_Pago.substring(4, 6);
						EMP.NumeroHojas = 1L;
						EMP.TipoAfiliado = "DEP";
						EMP.TipoPago = "Normal";
						EMP.Insistencias = "";
						EMP.MontoDepositoConvenido = fila.Monto_Total_DC;
						EMP.MontoDepositoAPV = fila.Monto_Total_APV;
						EMP.Reajustes = 0L;
						EMP.Intereses = 0L;
						EMP.Origen = "CARGA";
						EMP.Observaciones = "";
						EMP.IdEstado = "ING";
						List<CrearRecaudacionEmpleador.Empleador> ParamsEmpleador = new ArrayList<CrearRecaudacionEmpleador.Empleador>();
						ParamsEmpleador.add(EMP);
						ObjinEmp.p_IdEmpresa = in.p_IdEmpresa;
						ObjinEmp.p_IdUsuario = in.p_IdUsuario;
						ObjinEmp.p_IdModulo = "APV";
						ObjinEmp.p_Tipo = "I";
						ObjinEmp.p_Origen = "CARGA";
						ObjinEmp.p_Parametros = ParamsEmpleador;
			
						try {
							CrearRecaudacionEmpleador.ParametrosSalidaServicio ObjOutEmp = cre.logica(con, ObjinEmp);
							System.out.println(ObjOutEmp.toString());

							for (MensajeSalida filaMsg : ObjOutEmp.P_OUT_MENSAJES) {
								MensajeSalida msg1 = new MensajeSalida();
								msg1.id = salida.P_OUT_MENSAJES.size()+1L;
								msg1.tipo = "INFORMACION"; //filaMsg.tipo;
								msg1.codigo = "INFORMACION"; //filaMsg.codigo;
								msg1.descripcion = filaMsg.descripcion;
								salida.P_OUT_MENSAJES.add(msg1);	
							}
							
							IdRecaudacionEmpleador = ObjOutEmp.P_OUT_IDRECEMP;
							DocumentoIdentidadAnterior = rutPer;
							IdRecaudacionEmpleadorAnterior = ObjOutEmp.P_OUT_IDRECEMP;
							IdPersonaEmpleadorAnterior = EMP.IdPersona;
							FolioRecaudacioAnterior = fila.Folio;
							EmpIngresados++;
							
						} catch (Exception er) {
							er.printStackTrace();
							MensajeSalida msg1 = new MensajeSalida();
							msg1.id = salida.P_OUT_MENSAJES.size()+1L;
							msg1.tipo = "ERROR";
							msg1.codigo = "ERROR";
							msg1.descripcion = "Error al crear empleador";
							salida.P_OUT_MENSAJES.add(msg1);	
							salida.P_OUT_ERRORESCONTADOR++;
							DocumentoIdentidadAnterior = rutPer;
							IdRecaudacionEmpleadorAnterior = 0L;
							IdPersonaEmpleadorAnterior = 0L;
						}
						
					}else{
						if(IdRecaudacionEmpleadorAnterior == 0){
							MensajeSalida msg1 = new MensajeSalida();
							msg1.id = salida.P_OUT_MENSAJES.size()+1L;
							msg1.tipo = "ADVERTENCIA"; //filaMsg.tipo;
							msg1.codigo = "ADVERTENCIA"; //filaMsg.codigo;
							msg1.descripcion = "El Empleador con Documento Identidad: " + rutPer + " no es un Empleador valido. Detalle Empleador y Detalle Cliente no ingresados.";
							salida.P_OUT_MENSAJES.add(msg1);	
							CliRechazados++;
							
							continue;
						}else{
							//Si el Folio Deposito coincide, usamos el IdRecaudacionEmpleador anterior sino creamos uno nuevo
							if(FolioRecaudacioAnterior.equals(fila.Folio)) {
								IdRecaudacionEmpleador = IdRecaudacionEmpleadorAnterior;
							} else {
								// PARAMETROS EMPLEADOR
								IdRecaudacionDeposito = obtenerIdRecaudacionDeposito(DepositosPorCodigo, IdRecaudacionDeposito,
										fila);
								CrearRecaudacionEmpleador.Empleador EMP = new CrearRecaudacionEmpleador.Empleador();
								CrearRecaudacionEmpleador.ParametrosEntradaServicio ObjinEmp = new CrearRecaudacionEmpleador.ParametrosEntradaServicio();
								EMP.IdRecaudacionEmpleador = 0L;
								EMP.IdRecaudacion = IdRecaudacionDeposito;
								EMP.CodigoArchivo = in.p_NombreCarga.split("_")[2].split("\\.")[0];
								EMP.IdEmpresa = in.p_IdEmpresa;
								EMP.IdPersona = IdPersonaEmpleadorAnterior;
								EMP.FechaIngreso = fechaSistema;
								EMP.PeriodoPago = fila.Periodo_Pago.substring(0, 4) + "-" + fila.Periodo_Pago.substring(4, 6);
								EMP.NumeroHojas = 1L;
								EMP.TipoAfiliado = "DEP";
								EMP.TipoPago = "Normal";
								EMP.Insistencias = "";
								EMP.MontoDepositoConvenido = fila.Monto_Total_DC;
								EMP.MontoDepositoAPV = fila.Monto_Total_APV;
								EMP.Reajustes = 0L;
								EMP.Intereses = 0L;
								EMP.Origen = "CARGA";
								EMP.Observaciones = "";
								EMP.IdEstado = "ING";
								List<CrearRecaudacionEmpleador.Empleador> ParamsEmpleador = new ArrayList<CrearRecaudacionEmpleador.Empleador>();
								ParamsEmpleador.add(EMP);
								ObjinEmp.p_IdEmpresa = in.p_IdEmpresa;
								ObjinEmp.p_IdUsuario = in.p_IdUsuario;
								ObjinEmp.p_IdModulo = "APV";
								ObjinEmp.p_Tipo = "I";
								ObjinEmp.p_Origen = "CARGA";
								ObjinEmp.p_Parametros = ParamsEmpleador;
					
								try {
									CrearRecaudacionEmpleador.ParametrosSalidaServicio ObjOutEmp = cre.logica(con, ObjinEmp);
									System.out.println(ObjOutEmp.toString());

									for (MensajeSalida filaMsg : ObjOutEmp.P_OUT_MENSAJES) {
										MensajeSalida msg1 = new MensajeSalida();
										msg1.id = salida.P_OUT_MENSAJES.size()+1L;
										msg1.tipo = "INFORMACION"; //filaMsg.tipo;
										msg1.codigo = "INFORMACION"; //filaMsg.codigo;
										msg1.descripcion = filaMsg.descripcion;
										salida.P_OUT_MENSAJES.add(msg1);	
									}
									
									IdRecaudacionEmpleador = ObjOutEmp.P_OUT_IDRECEMP;
									DocumentoIdentidadAnterior = rutPer;
									IdRecaudacionEmpleadorAnterior = ObjOutEmp.P_OUT_IDRECEMP;
									FolioRecaudacioAnterior = fila.Folio;
									IdPersonaEmpleadorAnterior = EMP.IdPersona;
									EmpIngresados++;
									
								} catch (Exception er) {
									er.printStackTrace();
									MensajeSalida msg1 = new MensajeSalida();
									msg1.id = salida.P_OUT_MENSAJES.size()+1L;
									msg1.tipo = "ERROR";
									msg1.codigo = "ERROR";
									msg1.descripcion = "Error al crear empleador";
									salida.P_OUT_MENSAJES.add(msg1);	
									salida.P_OUT_ERRORESCONTADOR++;
									DocumentoIdentidadAnterior = rutPer;
									IdRecaudacionEmpleadorAnterior = 0L;
								}
							}
						}
					}					
					
					//CLIENTE

					//Valida si existe Cliente.
					IdPersona = 0L;
					Long rutCliLong = Long.parseLong(fila.Rut_Trabajador.substring(0, fila.Rut_Trabajador.length() - 1));
					String ruttrab = rutCliLong + "-" + fila.Rut_Trabajador.substring(fila.Rut_Trabajador.length() - 1, fila.Rut_Trabajador.length());

					param = new HashMap<>();
					
					param.put("DocumentoIdentidad", ruttrab);
	
					List<ExisteCliente> ExisteCliente = sql.query(con, SQL_EXISTE_CLIENTE, param, ExisteCliente.class);
					param.clear();
					if (ExisteCliente.size() > 0) {
						
							// PARAMETROS CLIENTE
							if(BigDecimal.valueOf(fila.Monto_APV) != BigDecimal.valueOf(0) && BigDecimal.valueOf(fila.Monto_DC) != BigDecimal.valueOf(0)) {
								
								CrearRecaudacionCliente.Cliente CLI = new CrearRecaudacionCliente.Cliente();

								CLI.IdRecaudacionCliente = 0L;
								CLI.IdRecaudacion = IdRecaudacionDeposito;
								CLI.IdRecaudacionEmpleador = IdRecaudacionEmpleador;
								CLI.IdEmpresa = in.p_IdEmpresa;
								CLI.IdCliente = ExisteCliente.get(0).IdCliente ;
								CLI.FechaIngreso = fechaSistema;
								CLI.TipoAfiliado = "DEP";
								CLI.IdRegimenTriburario = "APV";
								CLI.MontoDepositoConvenido = BigDecimal.valueOf(0);
								CLI.MontoDepositoAPV = BigDecimal.valueOf(fila.Monto_APV);
								CLI.Origen = "CARGA";
								CLI.IdEstado = "ING";
								ParamsCliente.add(CLI);
								
								CrearRecaudacionCliente.Cliente CLI2 = new CrearRecaudacionCliente.Cliente();

								CLI2.IdRecaudacionCliente = 0L;
								CLI2.IdRecaudacion = IdRecaudacionDeposito;
								CLI2.IdRecaudacionEmpleador = IdRecaudacionEmpleador;
								CLI2.IdEmpresa = in.p_IdEmpresa;
								CLI2.IdCliente = ExisteCliente.get(0).IdCliente ;
								CLI2.FechaIngreso = fechaSistema;
								CLI2.TipoAfiliado = "DEP";
								CLI2.IdRegimenTriburario = "DC";
								CLI2.MontoDepositoConvenido = BigDecimal.valueOf(fila.Monto_DC);
								CLI2.MontoDepositoAPV = BigDecimal.valueOf(0);
								CLI2.Origen = "CARGA";
								CLI2.IdEstado = "ING";
								ParamsCliente.add(CLI2);
							} else {
								CrearRecaudacionCliente.Cliente CLI = new CrearRecaudacionCliente.Cliente();

								CLI.IdRecaudacionCliente = 0L;
								CLI.IdRecaudacion = IdRecaudacionDeposito;
								CLI.IdRecaudacionEmpleador = IdRecaudacionEmpleador;
								CLI.IdEmpresa = in.p_IdEmpresa;
								CLI.IdCliente = ExisteCliente.get(0).IdCliente ;
								CLI.FechaIngreso = fechaSistema;
								CLI.TipoAfiliado = "DEP";

								if(BigDecimal.valueOf(fila.Monto_APV).compareTo(BigDecimal.valueOf(0)) > 0) {
								CLI.IdRegimenTriburario = "APV";
								} else if (BigDecimal.valueOf(fila.Monto_DC).compareTo(BigDecimal.valueOf(0)) > 0) {
									CLI.IdRegimenTriburario = "DC";
								} else {
									CLI.IdRegimenTriburario = "";
								}
								CLI.MontoDepositoConvenido = BigDecimal.valueOf(fila.Monto_DC);
								CLI.MontoDepositoAPV = BigDecimal.valueOf(fila.Monto_APV);
								CLI.Origen = "CARGA";
								CLI.IdEstado = "ING";
								ParamsCliente.add(CLI);
							}	
						
					}else{
						MensajeSalida msg = new MensajeSalida();
						msg.id = salida.P_OUT_MENSAJES.size()+1L;
						msg.tipo = "ERROR";
						msg.codigo = "ERROR";
						msg.descripcion = "El Cliente con Documento Identidad: " + ruttrab + " no existe.";
						salida.P_OUT_MENSAJES.add(msg);
						CliRechazados++;
					}	
				}
				
				ObjinCli.p_IdEmpresa = in.p_IdEmpresa;
				ObjinCli.p_IdUsuario = in.p_IdUsuario;
				ObjinCli.p_IdModulo = "APV";
				ObjinCli.p_Tipo = "I";
				ObjinCli.p_Origen = "CARGA";
				ObjinCli.p_Parametros = ParamsCliente;

				try {
					CrearRecaudacionCliente.ParametrosSalidaServicio ObjOutCLi = crc.logica(con, ObjinCli);
					System.out.println(ObjOutCLi.toString());
					for (MensajeSalida filaMsg : ObjOutCLi.P_OUT_MENSAJES) {
						MensajeSalida msg1 = new MensajeSalida();
						msg1.id = salida.P_OUT_MENSAJES.size()+1L;
						msg1.tipo = "INFORMACION"; //filaMsg.tipo;
						msg1.codigo = "INFORMACION"; //filaMsg.codigo;
						msg1.descripcion = filaMsg.descripcion;
						salida.P_OUT_MENSAJES.add(msg1);	
						if(("CREACION_CLIENTE_OK").equals(filaMsg.codigo)) {
							CliIngresados++;
						}
					}
				} catch (Exception er) {
					er.printStackTrace();
					MensajeSalida msg1 = new MensajeSalida();
					msg1.id = salida.P_OUT_MENSAJES.size()+1L;
					msg1.tipo = "ERROR";
					msg1.codigo = "ERROR";
					msg1.descripcion = "Error al crear cliente";
					salida.P_OUT_ERRORESCONTADOR++;
					salida.P_OUT_MENSAJES.add(msg1);
					CliRechazados++;
				}	
		
				in.p_Archivo.get(0).IdCargaMasiva = idsCargaArchivo.get(0);
				in.p_Archivo.get(0).FechaCarga = fechaSistema;
		
				int[] res = sql.batch(con, SQL_INSERT_CARGA_ARCHIVO, in.p_Archivo, CargaArchivo.class);
				if (res != null && res.length > 0 && res[0] == 1) {
					MensajeSalida msg = new MensajeSalida();
					msg.id = salida.P_OUT_MENSAJES.size()+1L;
					msg.tipo = "INFORMACION";
					msg.codigo = "INFORMACION";
					msg.descripcion = "Total Detalle Empleadores. Ingresados: " + EmpIngresados + " Rechazados: " + EmpRechazados + ".";
					salida.P_OUT_MENSAJES.add(msg);
					
					MensajeSalida msg1 = new MensajeSalida();
					msg1.id = salida.P_OUT_MENSAJES.size()+1L;
					msg1.tipo = "INFORMACION";
					msg1.codigo = "INFORMACION";
					msg1.descripcion = "Total Detalle Clientes. Ingresados: " + CliIngresados + " Rechazados: " + CliRechazados + ".";
					salida.P_OUT_MENSAJES.add(msg1);
				}
			}else{
				MensajeSalida msg = new MensajeSalida();
				msg.id = salida.P_OUT_MENSAJES.size()+1L;
				msg.tipo = "ERROR";
				msg.codigo = "ERROR";
				msg.descripcion = "El archivo ya fue cargado previamente.";
				salida.P_OUT_ERRORESCONTADOR++;
				salida.P_OUT_MENSAJES.add(msg);
			}
		}else{
			MensajeSalida msg = new MensajeSalida();
			msg.id = salida.P_OUT_MENSAJES.size()+1L; 
			msg.tipo = "ERROR";
			msg.codigo = "ERROR";
			msg.descripcion = "Antes de cargar el Detalle debe realizar la carga del archivo Banco.";
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(msg);
		}
		
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			AgregarLog.InAgregarLog.Log inLog = new AgregarLog.InAgregarLog.Log();
			inLog.IdCabeceraLog = 0L;
			inLog.IdTipoLog = "INGRESO";
			inLog.IdModulo = in.p_IdModulo;
			inLog.IdUsuario = in.p_IdUsuario;
			inLog.IdEmpresa = in.p_IdEmpresa;
			inLog.IdUsuarioAtendido = in.p_IdUsuario;
			inLog.IdCliente = 0L;
			inLog.EstadoCliente = "";
			inLog.NombreCliente = "";
			inLog.IdTipoDocumentoIdentidad = "";
			inLog.IdDocumentoIdentidad = "";
			inLog.IdPortafolio = 0L;
			inLog.EstadoPortafolio = "";
			inLog.IdTipoRegistro = "PREVIRED";
			inLog.EstadoRegistro = "";
			inLog.IdTipoOperacion = "";
			inLog.Folio = 0L;
			inLog.Cantidad = BigDecimal.ZERO;
			inLog.PrecioTasa = BigDecimal.ZERO;
			inLog.IdMonedaOrigen = "";
			inLog.IdMonedaLiquidacion = "";
			inLog.TipoCambio = "";
			inLog.Nemo = "";
			inLog.IdProceso = 0L;
			inLog.EstadoProceso = "VIG";
			inLog.NombreProceso = "CargaMasivaPrevired";						
			entradaAgregarLog.p_Log.add(inLog);
		
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		log.write("FIN :: APV.CargaMasivaPrevired");
		return salida;

	}

	private Long obtenerIdRecaudacionDeposito(List<Recaudacion> DepositosPorCodigo, Long IdRecaudacionDeposito,
			CargaMasivaPrevired.archivoPrevired fila) {
		for(Recaudacion deposito : DepositosPorCodigo){
			if(deposito.FolioCartola.equals(fila.Folio) == true){
				IdRecaudacionDeposito = deposito.IdRecaudacion;
				break;
			}
		};
		return IdRecaudacionDeposito;
	}
	// CARGA MASIVA ARCHIVO

	/********************
	 * Metodo Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getBOLSAConnection()) {
			CargaMasivaPrevired CMP = new CargaMasivaPrevired();
			try {
				Parametros_Entrada Objin = new Parametros_Entrada();

				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":74352,\"p_IdModulo\":\"APV\",\"p_NombreCarga\":\"PL03BCH_20231002_O01I.txt\",\"p_Origen\":\"PREVRED\",\"p_IdEmpleado\":845,\"p_Archivo\":[{\"IdEmpresa\":1,\"IdCargaMasiva\":0,\"NombreCargaMasiva\":\"PL03BCH_20231002_O01I.txt\",\"CantidadRegistros\":1,\"IdUsuario\":1006,\"IdTipoCarga\":1,\"Descripcion\":\"Carga Previred\",\"FechaCarga\":\"2020-09-30T03:00:00.000Z\",\"IdEstado\":\"VIG\"}],"
						+ "\"p_Parametros\":[{\"fila1\":1,\"Folio\":2069202305000228,\"Tipo_Pag\":1,\"FechaOperacion\":20200330,\"HoraOperacion\":194151,\"INS_APV\":227,\"Rut_Empleador\":\"0111849315\",\"Tipo_Empleador\":\"3\",\"Email_Empleador\":\"claudiamariele@gmail.cl                           \",\"fila2\":2,\"RazonSocial_Empleador\":\"CLAUDIA MARIELE REYES MURARO                      \",\"Ruta_Empleador1\":\"0111849315\",\"Act_Econ_Empleador\":\"1     \",\"Direccion_Empleador\":\"VASCO DE GAMA                                     \",\"Nro_Direccion_Empleador\":\"4610      \",\"Departamento_Empleador\":\"71                  \",\"Comuna_Empleador\":\"LAS CONDES                    \",\"Ciudad_Empleador\":\"SANTIAGO                      \",\"Region_Empleador\":\"13\",\"Telefono_Empleador\":\"4538610             \",\"Nom_Rep_Legal_Empleador\":\"CLAUDIA MARIELE REYES MURARO                      \",\"Rut_Rep_Legal_Empleador\":\"0111849315\",\"fila3\":3,\"Rut_Trabajador\":\"0111849315\",\"Ape_Pat_Trabajador\":\"REYES                         \",\"Ape_Mat_Trabajador\":\"MURARO                        \",\"Nom_Trabajador\":\"CLAUDIA MARIELE               \",\"Sexo_Trabajador\":\"F\",\"Renta_Imponible\":301000,\"Monto_APV\":20000,\"Monto_DC\":0,\"Monto_Total\":20000,\"Periodo_Pago\":\"202002\",\"fila4\":4,\"Monto_Total_APV\":20000,\"Monto_Total_DC\":0,\"Monto_Total1\":20000,\"Periodo_Pago1\":202002,\"Num_Afiliados\":1,\"NroRegistro\":1,\"MENSAJEVALIDACION\":\"OK\"}]}";
				
				Objin = gson.fromJson(json, Parametros_Entrada.class);
				

				try {

					ParametrosSalidaServicio ObjOut = CMP.logica(con, Objin);
					System.out.println(ObjOut.toString());
					con.commit();
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