package cl.bcs.socionegocio.cli;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.IdentificarLlaves;
import cl.bcs.plataforma.pla.utils.IdentificarLlavesBuilder;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.cli.CrearCliente.SalidaServicioCrearCliente;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.socionegocio.utils.IUP;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearClienteNominaPeru implements JavaCall {
	
	public String SQL_CONSULTAR_BROKERS = loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearClienteNominaPeru_SelectBroker.sql");
	public String SQL_CONSULTAR_INDICADORES_BROKER = loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearClienteNominaPeru_SelectIndicadoresPortafolioBroker.sql");
	public String SQL_INSERT_INDICADORES_BROKER = loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearClienteNominaPeru_InsertIndicadoresPortafolioBroker.sql");
	
	public static class EntradaServicioCrearClienteNominaPeru{
		public long p_IdEmpresa;
		public long p_IdUsuario;
		public String p_IdModulo;
		
		public String p_ClienteBloqueado;
		public String p_IdCategoriaInversionistaDefecto;
		public String p_IdPerfilComercialDefecto;
		public String p_IdTipoClienteDefecto;
		public String p_IdEmpleadoEjecutivoDefecto;
		public String p_PermiteParametrosDefault;
		public List<ArchivoNominaPeru> p_Archivo;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ArchivoNominaPeru{
		public String TIPODOCUMENTO;
		public String DOCUMENTOIDENTIDAD;
		public String TIPOPERSONA;
		public String NOMBRES;
		public String APELLIDOPATERNO;
		public String APELLIDOMATERNO;
		public String PAISNACIMIENTO;
		public String FECHANACIMIENTO;
		public String EDOCIVIL;
		public String GENERO;
		public String RAZONSOCIAL;
		public String CALLE;
		public String NUMERO;
		public String COMPLEMENTO;
		public String PAISRESIDENCIA;
		public String DISTRITO;
		public String TIPOTELEFONO;
		public String TELEFONO;
		public String EMAIL;
		public String EJECUTIVO;
		public String CATINV;
		public String PERCOMER;
		public String TIPOCLIENTE;
		public String CLASECLIENTE;
		public Long SEGMENTOCLIENTE;
		
		public String CODIGORUT;
		public String BROKER;		
		public String MENSAJEVALIDACION;
		public Long CIUDAD;
		public Long DIVISIONPAIS;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class Broker{
		public Long IDPERSONA;
		public String PAISNACIMIENTO;
		public String EJECUTIVO;
		public String CATINV;
		public String PERCOMER;
		public String TIPOCLIENTE;
		public String CODIGORUT;
		public String CLASECLIENTE;
		public Long SEGMENTOCLIENTE;
		public String FECHANACIMIENTO;
		
		public String CALLE;
		public String NUMERO;
		public Long CIUDAD;
		public Long DIVISIONPAIS;
		public String DIRECCIONELECTRONICA;
		public String TIPOTELEFONO;
		public String TELEFONO;
		public String BROKER;		
		public String MENSAJEVALIDACION;

		public String getBROKER() {
			return BROKER;
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class SalidaCrearClienteNominaPeru{
		public Long IDPERSONA;
		public Long IDCLIENTE;
		public String TIPODOCUMENTO;
		public String DOCUMENTOIDENTIDAD;
		public String CODIGORUT;
		public String NOMBRES;
		public String APELLIDOPATERNO;
		public String APELLIDOMATERNO;
		public String RAZONSOCIAL;
		public String MENSAJE;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public SalidaCrearClienteNominaPeru(){
			
		}
		public SalidaCrearClienteNominaPeru(ArchivoNominaPeru cliente) {
			this.TIPODOCUMENTO = cliente.TIPODOCUMENTO;
			this.DOCUMENTOIDENTIDAD = cliente.DOCUMENTOIDENTIDAD;
			this.CODIGORUT = cliente.CODIGORUT;
			this.NOMBRES = cliente.NOMBRES;
			this.APELLIDOPATERNO = cliente.APELLIDOPATERNO;
			this.APELLIDOMATERNO = cliente.APELLIDOMATERNO;
			this.RAZONSOCIAL = cliente.RAZONSOCIAL;
			this.MENSAJE = cliente.MENSAJEVALIDACION;
		}
	}
	
	public static class SalidaServicioCrearClienteNominaPeru extends SalidaEstandar{
		public List<SalidaCrearClienteNominaPeru> P_OUT_MENSAJE;
		public SalidaServicioCrearClienteNominaPeru(){
			this.P_OUT_MENSAJE = new ArrayList<>(); 
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
	
	public static class IndicadoresBroker{
		public String CodigoTipoIndicador;
		public Long IdIndicador;
		public Long IdEntidad;
		public String IdTipoEntidad;
		public Long IdTipoIndicador;
		public String ModeloCliente;
		public Long IdEmpresa;
		public String ValorIndicador;
		public String IdEstadoAutorizacion;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaServicioCrearClienteNominaPeru in = gson.fromJson(jin.toString(), EntradaServicioCrearClienteNominaPeru.class);
		SalidaServicioCrearClienteNominaPeru out = logica(conn, in);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	private SalidaServicioCrearClienteNominaPeru logica(Connection conn, EntradaServicioCrearClienteNominaPeru in) {
		SalidaServicioCrearClienteNominaPeru salida = new SalidaServicioCrearClienteNominaPeru();
		try {
			//obtener informacion referencial
			IdentificarLlaves llaves = new IdentificarLlavesBuilder(conn)
			        .setPaises()
			        .setCiudadesUbigeo()
			        .setIupEjecutivo()
			        .build();
			
			SQL sql = new SQL();
			Set<String> documentosIdentidad = new HashSet<>();
			Set<String> documentosIdentidadAdicional = new HashSet<>();
			
			//obtener informacion de los brokers y crear map
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			Map<String, Broker> brokers = sql.query(conn, SQL_CONSULTAR_BROKERS, params, Broker.class)
					.stream()
					.filter(Objects::nonNull)
					.collect(Collectors.toMap(Broker::getBROKER, Function.identity()) );
			
			//validar clientes
			List<ArchivoNominaPeru> clientes = in.p_Archivo.stream()
					.map( cliente -> {
						
						// 1) evaluar si permite parametros default para clientes
						if(in.p_PermiteParametrosDefault!=null && in.p_PermiteParametrosDefault.equals("S")) {
							
							if(in.p_IdCategoriaInversionistaDefecto.equals("") && cliente.CATINV.isEmpty()) {
								cliente.MENSAJEVALIDACION = "CATEGORIA INVERSIONISTA INVALIDA";
							}else if(!in.p_IdCategoriaInversionistaDefecto.equals("") && cliente.CATINV.isEmpty()){
								cliente.CATINV = in.p_IdCategoriaInversionistaDefecto;
							}else if(!in.p_IdCategoriaInversionistaDefecto.equals("") && !cliente.CATINV.isEmpty()){
								cliente.CATINV = in.p_IdCategoriaInversionistaDefecto; 
							}
							
							if(in.p_IdPerfilComercialDefecto.equals("") && cliente.PERCOMER.isEmpty()) {
								cliente.MENSAJEVALIDACION = "PERFIL COMERCIAL INVALIDO";
							}else if(!in.p_IdPerfilComercialDefecto.equals("") && cliente.PERCOMER.isEmpty()){
								cliente.PERCOMER = in.p_IdPerfilComercialDefecto;
							}else if(!in.p_IdPerfilComercialDefecto.equals("") && !cliente.PERCOMER.isEmpty()){
								cliente.PERCOMER = in.p_IdPerfilComercialDefecto; 
							}
							
							if(in.p_IdTipoClienteDefecto.equals("") && cliente.TIPOCLIENTE.isEmpty()) {
								cliente.MENSAJEVALIDACION = "TIPO CLIENTE INVALIDO";
							}else if(!in.p_IdTipoClienteDefecto.equals("") && cliente.TIPOCLIENTE.isEmpty()){
								cliente.TIPOCLIENTE = in.p_IdTipoClienteDefecto;
							}else if(!in.p_IdTipoClienteDefecto.equals("") && !cliente.TIPOCLIENTE.isEmpty()){
								cliente.TIPOCLIENTE = in.p_IdTipoClienteDefecto; 
							}
							
						}
						// 2) evaluar si los clientes tienen codigo broker para clientes finales
						if(!Optional.ofNullable(cliente.BROKER).orElse("").isEmpty()) {
							Broker broker = brokers.get(cliente.BROKER);
							if(broker!=null) {
								//el cliente debe heredar los datos del broker
								if(Optional.ofNullable(cliente.TIPODOCUMENTO).orElse("").isEmpty()) {
									cliente.TIPODOCUMENTO="FIC";
								}
								if(Optional.ofNullable(cliente.DOCUMENTOIDENTIDAD).orElse("").isEmpty()) {
									try {
										cliente.DOCUMENTOIDENTIDAD =  Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::DocumentoFicticio", (long) 1).get(0).toString();
									} catch (SQLException e) {
										cliente.MENSAJEVALIDACION = "ERROR AL GENERAR DOCUMENTO FICTICIO";
									}
								}
								if(Optional.ofNullable(cliente.PAISNACIMIENTO).orElse("").isEmpty()) {
									cliente.PAISNACIMIENTO = broker.PAISNACIMIENTO;
								}
								
								if (Optional.ofNullable(cliente.EJECUTIVO).orElse("").isEmpty()) {
									cliente.EJECUTIVO = broker.EJECUTIVO;
								}
								
								if (Optional.ofNullable(cliente.CATINV).orElse("").isEmpty() ||  !cliente.BROKER.equals("")) {//obliga a tomar datos del broker
									cliente.CATINV = broker.CATINV;
								}
								
								if (Optional.ofNullable(cliente.PERCOMER).orElse("").isEmpty() || !cliente.BROKER.equals("")) {//obliga a tomar datos del broker
									cliente.PERCOMER = broker.PERCOMER;
								}
								
								if (Optional.ofNullable(cliente.TIPOCLIENTE).orElse("").isEmpty() || !cliente.BROKER.equals("")) {//obliga a tomar datos del broker
									cliente.TIPOCLIENTE = broker.TIPOCLIENTE;
								}
								
								//DATOS ADICIONALES
								if (Optional.ofNullable(cliente.CALLE).orElse("").isEmpty()) {
									cliente.CALLE = broker.CALLE;
									cliente.NUMERO =broker.NUMERO;
									cliente.CIUDAD = broker.CIUDAD;
									cliente.DIVISIONPAIS = broker.DIVISIONPAIS;
								}
								if (Optional.ofNullable(cliente.TELEFONO).orElse("").isEmpty()) {
									cliente.TELEFONO = broker.TELEFONO;
									cliente.TIPOTELEFONO = broker.TIPOTELEFONO;
								}
								if (Optional.ofNullable(cliente.EMAIL).orElse("").isEmpty()) {
									cliente.EMAIL = broker.DIRECCIONELECTRONICA;
								}
								if (Optional.ofNullable(cliente.CLASECLIENTE).orElse("").isEmpty()) {
									cliente.CLASECLIENTE = broker.CLASECLIENTE;
								}
								if (Optional.ofNullable(cliente.SEGMENTOCLIENTE).orElse(0L).equals(0L)) {
									cliente.SEGMENTOCLIENTE = broker.SEGMENTOCLIENTE;
								}
								if (Optional.ofNullable(cliente.FECHANACIMIENTO).orElse("").equals("")) {
									cliente.FECHANACIMIENTO = broker.FECHANACIMIENTO;
								}

							}else {
								cliente.MENSAJEVALIDACION = "BROKER NO EXISTE";
								
							}
						}
						
						// 3) generar IUP
						if(!Optional.ofNullable(cliente.DOCUMENTOIDENTIDAD).orElse("").isEmpty()) {
							cliente.DOCUMENTOIDENTIDAD = IUP.generar(cliente.DOCUMENTOIDENTIDAD, cliente.TIPODOCUMENTO, cliente.TIPOPERSONA);
						}else {
							cliente.MENSAJEVALIDACION = "DOCUMENTO IDENTIDAD INVALIDO";
						}
						
						if(!documentosIdentidad.add(cliente.DOCUMENTOIDENTIDAD)) {
							cliente.MENSAJEVALIDACION = "DOCUMENTO IDENTIDAD "+cliente.DOCUMENTOIDENTIDAD+" REPETIDO EN EL ARCHIVO";
						}
						
						if(!documentosIdentidadAdicional.add(cliente.CODIGORUT)) {
							cliente.MENSAJEVALIDACION = "DOCUMENTO IDENTIDAD ADICIONAL " +cliente.CODIGORUT+" REPETIDO EN EL ARCHIVO";
						}
						
						return cliente;
					
					}).filter(cliente -> {
						//obtener solo los clientes que se encuentren con estado OK
						if(cliente.MENSAJEVALIDACION.equalsIgnoreCase("OK")) {
							return true;
						}else {
							salida.P_OUT_MENSAJE.add(new SalidaCrearClienteNominaPeru(cliente));
							return false;
						}
					}).collect(Collectors.toList());
			
			//Llamar al servicio CrearClientes
			clientes.forEach(cliente -> {
				CrearCliente crearCliente = new CrearCliente();
				CrearCliente.EntradaServicioCrearCliente entradaCrearCliente = new CrearCliente.EntradaServicioCrearCliente();
				entradaCrearCliente.p_IdEmpresa = in.p_IdEmpresa;
				entradaCrearCliente.p_IdModulo = in.p_IdModulo;
				entradaCrearCliente.p_IdUsuario = in.p_IdUsuario;
				
				Enrolamiento enr = new Enrolamiento();
				enr.IdTipoDocumentoIdentidad = "IUP";
				enr.DocumentoIdentidad = cliente.DOCUMENTOIDENTIDAD;
				enr.IdTipoDocumentoIdentidadAsociado = cliente.TIPODOCUMENTO;
				enr.IdTipoDocumentoIdentidadAdicional = "RUT";
				
				int numrut =Integer.parseInt(cliente.CODIGORUT);
				String rut = "" + String.format("%08d", numrut);
				enr.DocumentoIdentidadAdicional = rut;
				enr.Tipo = cliente.TIPOPERSONA;
				enr.Nombres = cliente.NOMBRES;
				enr.ApellidoPaterno = cliente.APELLIDOPATERNO;
				enr.ApellidoMaterno = cliente.APELLIDOMATERNO;
				enr.IdPaisNacimiento = llaves.getPais(cliente.PAISNACIMIENTO);
				//vaidar fecha de megda
				try {
					enr.FechaNacimiento =  Fecha.toDate(cliente.FECHANACIMIENTO);
				} catch (DateTimeParseException e1) {
					cliente.MENSAJEVALIDACION = "FECHA NACIMIENTO / CONSTITUCION, INVALIDA. FORMATO: YYYY-MM-DD";
				}
				enr.EstadoCivil = Optional.ofNullable(cliente.EDOCIVIL).orElse("");
				enr.Sexo = Optional.ofNullable(cliente.GENERO).orElse("");
				enr.RazonSocial = cliente.RAZONSOCIAL;
				enr.DireccionCalle= cliente.CALLE;
				enr.DireccionNumero = cliente.NUMERO;
				enr.DireccionDepartamento = cliente.COMPLEMENTO;
				//enr.IdPaiOIsNacimiento = cliente.PAISRESIDENCIA;
				if(cliente.BROKER == null || cliente.BROKER.equals("")){
					enr.IdDivisionPais = llaves.getCiudadUbigeo(cliente.DISTRITO);
				}else{
					enr.IdDivisionPais = cliente.DIVISIONPAIS;
				}
				enr.IdTipoTelefono = cliente.TIPOTELEFONO;
				enr.Telefono = cliente.TELEFONO;
				enr.Email = cliente.EMAIL;
				if(cliente.BROKER == null || cliente.BROKER.equals("")){
					enr.IdCiudad = llaves.getCiudadUbigeo( (cliente.DISTRITO!=null && !cliente.DISTRITO.isEmpty()) ? cliente.DISTRITO : "NININI" );
					enr.IdCiudad = (enr.IdCiudad==null) ? 1 : enr.IdCiudad;//
				}else{
					enr.IdCiudad =  cliente.CIUDAD;
					
				}
				//obligatorios extra
				enr.IdTipoEntidad = "CLIENTE";
				enr.IdEmpresa = in.p_IdEmpresa;
				enr.IdUsuario = in.p_IdUsuario;
				enr.IdEntidadEnrolada = new Long(0);
				enr.CLI_ClienteBloqueado = in.p_ClienteBloqueado;
				enr.CLI_CodigoAdicionalHolding = "";
				enr.CLI_IdClaseCliente = (cliente.CLASECLIENTE!=null && !cliente.CLASECLIENTE.isEmpty()) ? cliente.CLASECLIENTE : "";
				
				Optional<Broker> broker = Optional.ofNullable(brokers.get(cliente.BROKER));
				enr.POR_IdPersonaBrokerExtranjero = (broker.isPresent() && broker.get().IDPERSONA!=null) ? broker.get().IDPERSONA : 0;
				
				enr.CLI_IdCategoriaInversionista = cliente.CATINV;
				enr.CLI_IdTipoCliente = cliente.TIPOCLIENTE;
				enr.CLI_ClienteFinal = ( cliente.BROKER == null || cliente.BROKER.equals("")) ? "N" : "S";
				if(cliente.BROKER == null || cliente.BROKER.equals("")){
					enr.CLI_IdEmpleadoEjecutivo = (cliente.EJECUTIVO!=null && !cliente.EJECUTIVO.isEmpty()) ? llaves.getIUPEjecutivo(cliente.EJECUTIVO) : null;
				}else{
					enr.CLI_IdEmpleadoEjecutivo = llaves.getIUPEjecutivo(cliente.EJECUTIVO);
				}
				enr.POR_IdPerfilComercial = cliente.PERCOMER;

				//construir objeto de salida
				SalidaCrearClienteNominaPeru resultado = new SalidaCrearClienteNominaPeru(cliente);				
				try {
					//crear cliente	
					entradaCrearCliente.p_ConsultarEnrolamiento = Arrays.asList(enr);
					SalidaServicioCrearCliente salidaCrearCliente = crearCliente.logica(conn, entradaCrearCliente);
					if(salidaCrearCliente.P_OUT_ERRORESCONTADOR==0) {
						//cliente creado
						resultado.IDCLIENTE = salidaCrearCliente.P_OUT_REFIDCLIENTE;
						
						//evaluar si es broker, en cuyo caso actualizar las preferencias del portafolio del cliente con las del portafolio del broker por defecto.
						if(broker.isPresent()) {
							HashMap<String, Object> paramsIndocadores = new HashMap<String,Object>();
							paramsIndocadores.put("IdPersonaBroker", enr.POR_IdPersonaBrokerExtranjero);
							paramsIndocadores.put("IdCliente", resultado.IDCLIENTE);
							List<IndicadoresBroker> indicadoresBroker = sql.query(conn, SQL_CONSULTAR_INDICADORES_BROKER, paramsIndocadores, IndicadoresBroker.class);
							if(!indicadoresBroker.isEmpty()) {
								sql.batch(conn, SQL_INSERT_INDICADORES_BROKER, indicadoresBroker, IndicadoresBroker.class);
							}
						}
						conn.commit();
					}else {
						resultado.MENSAJE = salidaCrearCliente.P_OUT_ERRORESDETALLE.stream().map(e -> e.Descripcion).collect(Collectors.joining(", "));
						conn.rollback();
					}
				} catch (Exception e) {
					resultado.MENSAJE = "ERROR AL CREAR EL CLIENTE";
					e.printStackTrace();
				}
				salida.P_OUT_MENSAJE.add(resultado);
			}); 
			
			System.out.println(in.p_Archivo);
			
		}catch(Exception e) {
			e.printStackTrace();
			salida.addError("UI_ERROR_GRAVE", e.getMessage());
		}
		
		return salida;
	}
	
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getQAPEConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":102,\"p_IdModulo\":\"CLI\",\"p_ClienteBloqueado\":\"S\",\"p_IdCategoriaInversionistaDefecto\":\"\",\"p_IdPerfilComercialDefecto\":\"\",\"p_IdTipoClienteDefecto\":\"\",\"p_IdEmpleadoEjecutivoDefecto\":\"0\",\"p_PermiteParametrosDefault\":\"N\",\"p_Archivo\":[{\"TIPODOCUMENTO\":\"\",\"DOCUMENTOIDENTIDAD\":\"\",\"CODIGORUT\":\"146673\",\"TIPOPERSONA\":\"J\",\"RAZONSOCIAL\":\"BROKER TEST 1 S.A\",\"NOMBRES\":\"\",\"APELLIDOPATERNO\":\"\",\"APELLIDOMATERNO\":\"\",\"PAISNACIMIENTO\":\"\",\"FECHANACIMIENTO\":\"1900-01-01\",\"EDOCIVIL\":\"\",\"GENERO\":\"\",\"PAISRESIDENCIA\":\"\",\"DISTRITO\":\"\",\"CALLE\":\"\",\"NUMERO\":\"\",\"COMPLEMENTO\":\"\",\"TIPOTELEFONO\":\"\",\"TELEFONO\":\"\",\"EMAIL\":\"\",\"CATINV\":\"\",\"PERCOMER\":\"\",\"TIPOCLIENTE\":\"CLIENTE MESA\",\"EJECUTIVO\":\"\",\"BROKER\":\"2525\",\"MENSAJEVALIDACION\":\"OK\"},{\"TIPODOCUMENTO\":\"\",\"DOCUMENTOIDENTIDAD\":\"\",\"CODIGORUT\":\"146673\",\"TIPOPERSONA\":\"J\",\"RAZONSOCIAL\":\"BROKER TEST 1 S.A\",\"NOMBRES\":\"\",\"APELLIDOPATERNO\":\"\",\"APELLIDOMATERNO\":\"\",\"PAISNACIMIENTO\":\"\",\"FECHANACIMIENTO\":\"1900-01-01\",\"EDOCIVIL\":\"\",\"GENERO\":\"\",\"PAISRESIDENCIA\":\"\",\"DISTRITO\":\"\",\"CALLE\":\"\",\"NUMERO\":\"\",\"COMPLEMENTO\":\"\",\"TIPOTELEFONO\":\"\",\"TELEFONO\":\"\",\"EMAIL\":\"\",\"CATINV\":\"\",\"PERCOMER\":\"\",\"TIPOCLIENTE\":\"CLIENTE MESA\",\"EJECUTIVO\":\"\",\"BROKER\":\"2529\",\"MENSAJEVALIDACION\":\"OK\"},{\"TIPODOCUMENTO\":\"DNI\",\"DOCUMENTOIDENTIDAD\":\"12548526\",\"CODIGORUT\":\"146674\",\"TIPOPERSONA\":\"N\",\"RAZONSOCIAL\":\"\",\"NOMBRES\":\"TEST\",\"APELLIDOPATERNO\":\"1\",\"APELLIDOMATERNO\":\"BROKER\",\"PAISNACIMIENTO\":\"CL\",\"FECHANACIMIENTO\":\"2010-01-01\",\"EDOCIVIL\":\"SO\",\"GENERO\":\"M\",\"PAISRESIDENCIA\":\"PE\",\"DISTRITO\":\"010101\",\"CALLE\":\"CALLE1\",\"NUMERO\":\"123\",\"COMPLEMENTO\":\"B\",\"TIPOTELEFONO\":\"FIJO\",\"TELEFONO\":\"56847585956\",\"EMAIL\":\"TEST@TEST.CL\",\"CATINV\":\"AGRESIVO\",\"PERCOMER\":\"MODELO AGRESIVO 01\",\"TIPOCLIENTE\":\"CLIENTE MESA\",\"EJECUTIVO\":\"2525\",\"BROKER\":\"\",\"MENSAJEVALIDACION\":\"OK\"},{\"TIPODOCUMENTO\":\"RUC\",\"DOCUMENTOIDENTIDAD\":\"85749265\",\"CODIGORUT\":\"146675\",\"TIPOPERSONA\":\"J\",\"RAZONSOCIAL\":\"BROKER TEST 3 S.A\",\"NOMBRES\":\"\",\"APELLIDOPATERNO\":\"\",\"APELLIDOMATERNO\":\"\",\"PAISNACIMIENTO\":\"PE\",\"FECHANACIMIENTO\":\"2000-01-01\",\"EDOCIVIL\":\"\",\"GENERO\":\"\",\"PAISRESIDENCIA\":\"PE\",\"DISTRITO\":\"010101\",\"CALLE\":\"\",\"NUMERO\":\"321\",\"COMPLEMENTO\":\"A\",\"TIPOTELEFONO\":\"FIJO\",\"TELEFONO\":\"5678452563\",\"EMAIL\":\"TEST1@TEST.CL\",\"CATINV\":\"AGRESIVO\",\"PERCOMER\":\"MODELO AGRESIVO 01\",\"TIPOCLIENTE\":\"CLIENTE MESA\",\"EJECUTIVO\":\"2525\",\"BROKER\":\"\",\"MENSAJEVALIDACION\":\"OK\"}]}";
			String jsonUn = "{   \"p_IdEmpresa\": 1,   \"p_IdUsuario\": 102,   \"p_IdModulo\": \"CLI\",   \"p_ClienteBloqueado\": \"S\",   \"p_IdCategoriaInversionistaDefecto\": \"\",   \"p_IdPerfilComercialDefecto\": \"\",   \"p_IdTipoClienteDefecto\": \"\",   \"p_IdEmpleadoEjecutivoDefecto\": \"0\",   \"p_PermiteParametrosDefault\": \"N\",   \"p_Archivo\": [     {       \"TIPODOCUMENTO\": \"\",       \"DOCUMENTOIDENTIDAD\": \"\",       \"TIPOPERSONA\": \"J\",       \"NOMBRES\": \"\",       \"APELLIDOPATERNO\": \"\",       \"APELLIDOMATERNO\": \"\",       \"PAISNACIMIENTO\": \"\",       \"FECHANACIMIENTO\": \"\",       \"EDOCIVIL\": \"\",       \"GENERO\": \"\",       \"RAZONSOCIAL\": \"BROKER TEST 1 S.A\",       \"CALLE\": \"\",       \"NUMERO\": \"\",       \"COMPLEMENTO\": \"\",       \"PAISRESIDENCIA\": \"\",       \"DISTRITO\": \"\",       \"TIPOTELEFONO\": \"\",       \"TELEFONO\": \"\",       \"EMAIL\": \"\",       \"EJECUTIVO\": \"\",       \"CATINV\": \"\",       \"PERCOMER\": \"\",       \"TIPOCLIENTE\": \"CLIENTE MESA\",       \"CLASECLIENTE\": null,       \"SEGMENTOCLIENTE\": null,       \"CODIGORUT\": \"146682\",       \"BROKER\": \"2525\",       \"MENSAJEVALIDACION\": \"OK\",       \"CIUDAD\": null,       \"DIVISIONPAIS\": null     }   ] }";
			String jsonQA = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CLI\",\"p_ClienteBloqueado\":\"S\",\"p_IdCategoriaInversionistaDefecto\":\"\",\"p_IdPerfilComercialDefecto\":\"\",\"p_IdTipoClienteDefecto\":\"\",\"p_IdEmpleadoEjecutivoDefecto\":\"0\",\"p_PermiteParametrosDefault\":\"N\",\"p_Archivo\":[{\"TIPODOCUMENTO\":\"\",\"DOCUMENTOIDENTIDAD\":\"\",\"CODIGORUT\":\"146702\",\"TIPOPERSONA\":\"J\",\"RAZONSOCIAL\":\"BROKER TEST S.A\",\"NOMBRES\":\"\",\"APELLIDOPATERNO\":\"\",\"APELLIDOMATERNO\":\"\",\"PAISNACIMIENTO\":\"\",\"FECHANACIMIENTO\":\"\",\"EDOCIVIL\":\"\",\"GENERO\":\"\",\"PAISRESIDENCIA\":\"\",\"DISTRITO\":\"\",\"CALLE\":\"\",\"NUMERO\":\"\",\"COMPLEMENTO\":\"\",\"TIPOTELEFONO\":\"\",\"TELEFONO\":\"\",\"EMAIL\":\"\",\"CATINV\":\"\",\"PERCOMER\":\"\",\"TIPOCLIENTE\":\"\",\"EJECUTIVO\":\"\",\"BROKER\":\"BRO00004\",\"MENSAJEVALIDACION\":\"OK\"}]}";
			
			EntradaServicioCrearClienteNominaPeru Objin = gson.fromJson(jsonQA, EntradaServicioCrearClienteNominaPeru.class);

			CrearClienteNominaPeru demo = new CrearClienteNominaPeru();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SalidaServicioCrearClienteNominaPeru ObjOut = demo.logica(con, Objin);
				//con.commit();
				System.out.println(ObjOut.toString());
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

}
