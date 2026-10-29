package cl.bcs.loaderapp.concrete.socionegocio.cliente;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.DbLoader;
import cl.bcs.loaderapp.pojo.socionegocio.Cliente;
import cl.bcs.loaderapp.utils.BreakException;
import cl.bcs.loaderapp.utils.LoaderUtils;
import cl.bcs.plataforma.ind.ConsultarIndicadores;
import cl.bcs.plataforma.ind.ConsultarIndicadores.Indicador;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarValoresPorDefecto;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.IdentificarLlaves;
import cl.bcs.plataforma.pla.utils.IdentificarLlavesBuilder;
import cl.bcs.plataforma.pla.utils.ConsultarValoresPorDefecto.EntradaConsultarValoresPorDefecto;
import cl.bcs.socionegocio.cli.CrearCliente.Contador;
import cl.bcs.socionegocio.cli.CrearCliente.EntidadCrearClienteUpdate;
import cl.bcs.socionegocio.per.CrearDireccionElectronica;
import cl.bcs.socionegocio.per.CrearDireccionElectronica.EntradaCrearDireccionElectronica;
import cl.bcs.socionegocio.per.CrearDireccionElectronica.SalidaCrearDireccionElectronica;
import cl.bcs.socionegocio.per.CrearDireccionFisica;
import cl.bcs.socionegocio.per.CrearDireccionFisica.EntradaCrearDireccionFisica;
import cl.bcs.socionegocio.per.CrearDireccionFisica.SalidaCrearDireccionFisica;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidad;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidad.EntradaCrearDocumentoIdentidad;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidad.SalidaCrearDocumentoIdentidad;
import cl.bcs.socionegocio.per.CrearPersona.ConsultaExistePersona;
import cl.bcs.socionegocio.per.CrearPersona.EntidadCrearPersonaUpdate;
import cl.bcs.socionegocio.per.CrearPersona.EntidadCrearPersonaUpdateSocioNegocio;
import cl.bcs.socionegocio.per.CrearTelefono;
import cl.bcs.socionegocio.per.CrearTelefono.EntradaCrearTelefono;
import cl.bcs.socionegocio.per.CrearTelefono.SalidaCrearTelefono;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.socionegocio.soc.ConsultarEnrolamiento;
import cl.bcs.socionegocio.soc.ConsultarEnrolamiento.EntradaConsultaEnrolamiento;
import cl.bcs.socionegocio.soc.ConsultarEnrolamiento.SalidaConsultaEnrolamiento;
import cl.bcs.socionegocio.soc.ValidarDocumentoIdentidad;
import cl.bcs.socionegocio.soc.ValidarDocumentoIdentidad.SalidaValidarDocumentoIdentidad;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ClienteDbLoader implements DbLoader {
	
	public String SQL_INSERT_ENROLAMIENTO = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/cliente/Cliente_IngresarEnrolamiento.sql");
	public String SQL_CREAR_PERSONA = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/cliente/Cliente_IngresarPersona.sql");
	public String SQL_CREAR_SOCIO_NEGOCIO = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/cliente/Cliente_IngresarSocioNegocio.sql");
	public String SQL_CREAR_FATCA= loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/cliente/Cliente_IngresarPersonaFatca.sql");
	
	public String SQL_CREAR_PERSONA_UPDATE = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/cliente/Cliente_ActualizarPersona.sql");
	public String SQL_CREAR_PERSONA_ENROLAMIENTO_UPDATE = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/cliente/Cliente_ActualizarEnrolamiento.sql");
	public String SQL_CREAR_CLIENTE = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/cliente/Cliente_IngresarCliente.sql");
	public String SQL_CREAR_PORTAFOLIO_Call = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/cliente/Cliente_CearPortafolio.sql");
	public String SQL_ACTUALIZAR_SOCIO_NEGOCIO = "UPDATE \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" SET \"IdCliente\" = :IdCliente WHERE \"IdSocioDeNegocio\" = :IdSocioDeNegocio;";
	public String SQL_ACTUALIZAR_ENROLAMIENTO = "UPDATE \"BSCL.SocioNegocio.BD::SOC.Enrolamiento\" SET \"IdEntidadEnrolada\" = :IdCliente WHERE \"IdEnrolamiento\" = :IdEnrolamiento;";
	public String SQL_ACTUALIZAR_DOCUMENTO_ADICIONAL_PERSONA = "UPDATE \"BSCL.SocioNegocio.BD::PER.Persona\" SET \"IdDocumentoIdentidadAdicional\" = :p_IdDocumentoIdentidadAdicional WHERE \"IdPersona\" = :p_IdPersona;";
	public String SQL_CREAR_CUENTA_CUSTODIA_Call = loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_CrearCuentaCustodia_Call.sql");
	
	public String SQL_ConsultarExistenciaPersona = "SELECT CP.\"IdPersona\", SOC.\"IdSocioDeNegocio\" FROM \"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona\" CP INNER JOIN \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" SOC ON SOC.\"IdPersona\" = CP.\"IdPersona\" WHERE CP.\"DocumentoIdentidad\" = :DocumentoIdentidad AND CP.\"TipoDocumentoIdentidad\" = :TipoDocumentoIdentidad AND CP.\"Tipo\" = (CASE WHEN :CodigoPais='PE' THEN CP.\"Tipo\" ELSE :Tipo END)";
	public String SQL_ConsultarIdPaisCiudad= "SELECT \"IdPais\" AS \"Id\" FROM \"BSCL.Plataforma.BD.Vistas::REG.ConsultarCiudad\" CIU WHERE CIU.\"IdCiudad\" = :IdCiudad ";
	public String SQL_ConsultarIdPersonaEjecutivo = "SELECT \"IdPersonaEjecutivo\" AS \"Id\" FROM \"BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo\" WHERE \"CodigoEmpleado\" LIKE :CodigoEmpleado;";
//	public String SQL_ConsultarIdEmpleadoUsuario = "SELECT \"IdEntidad\" AS \"Id\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"IdUsuario\" = :IdUsuario";
	public String SQL_ConsultarExistenciaDocumentoAdicional = "SELECT COUNT(1) AS \"Count\" FROM \"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad\" WHERE \"DocumentoIdentidad\" = :DocumentoIdentidadAdicional AND \"IdTipoDocumentoIdentidad\" = :TipoDocumentoIdentidad AND \"IdEmpresa\" = :IdEmpresa";
	
	
	public static class EnrolamientoCliente{
		public Long IdEnrolamiento;
		public Long IdEmpresa;
		public Long IdUsuario;
		public String IdTipoEntidad;
		public Long IdEntidadEnrolada;
		public String IdTipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		public String Tipo;
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public String RazonSocial;
		public Long IdPaisNacimiento;
		public String FechaNacimiento;
		public String Sexo;
		public String EstadoCivil;
		public Long IdCiudad;
		public String IdTipoTelefono;
		public String Telefono;
		public String Email;
		public String DireccionCalle;
		public String FechaCreacion;
		public String FechaModificacion;
		public String CLI_IdCategoriaInversionista;
		public String POR_IdPerfilComercial;
		public String CLI_IdTipoCliente;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EnrolamientoPersona{
		public Long IdPersona;
		public String IdEstado;
		
		public String Tipo;
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public String RazonSocial;
		public Long IdPaisNacimiento;
		public Long IdPaisNacionalidadPrincipal;
		public Long IdPaisResidencia;
		public String FechaNacimiento;
		public String Sexo;
		public String EstadoCivil;
		
		public String FechaIngreso;
		
		public String EsEnrolado;
		public Long IdUsuarioCreador;
		public Long IdCiudad;
		public Long IdSocioNegocio;
		public Long IdEmpresa;
		public String FechaHoraSistema;
		public Long Distrito;
		public Long IdCarga;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntidadCliente{
		public Long IdCliente;
		public Long IdPersona;
		public Long IdEmpresa;
		public String IdEstado;
		public String IdSegmentoCliente;
		public String IdCategoriaInversionista;
		public Long IdUsuarioCreador;
		public String FechaIngreso;
		public String FechaCreacion;
		public String FechaModificacion;
		public String EsEnrolado;
		public String IdTipoCliente;
		public String IdPlantillaLimiteExtra;
		public Long IdPersonaEjecutivo;
		public String CodigoAdicionalHolding;
		public Long IdPersonaUsuario;
		public Long IdCarga;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class Ids{
		public Long Id;
	}
	
	public static class EntidadActualizarSocioNegocio{
		public long IdSocioDeNegocio;
		public long IdCliente;
	}
	
	public static class EntidadActualizarEnrolamiento{
		public long IdEnrolamiento;
		public long IdCliente;
	}
	
	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral in, List<Object> lines) {
		List<String> salida = new ArrayList<String>();
		
		Long idUsuarioDefecto = in.p_IdUsuario;
        Long idEmpresaDefecto = in.p_IdEmpresa;
        
        try (Connection con = jsJB.getConnection()){
            
            IdentificarLlaves identificarLlaves = new IdentificarLlavesBuilder(con, (long) 1, idEmpresaDefecto)
                    .setCategoriasInversionista()
                    .setCiudades()
                    .setCiudadesUbigeo()
                    .setPaises()
                    .setEstadoCivil()
                    .build();
            
            String fechaHoraSistema = Utils.getFechaHoraSistema(con, Utils.getSchema(), idEmpresaDefecto);
			String fechaSistema = Utils.getFechaSistema(con, Utils.getSchema(), idEmpresaDefecto);
			String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresaDefecto, "PLATAFORMA", "PLA.LocalizacionSistema");
			String codigoPais = Utils.getConfiguracion(con, Utils.getSchema(), idEmpresaDefecto, "PLA.LocalizacionSistema");
			
            List<Cliente> clientesCSV = (List<Cliente>) (Object) lines;
            long idCarga = in.IdCarga;
            System.out.println("PROCESANDO CARGA N°: " + idCarga);

			clientesCSV.parallelStream().forEach((cli) -> {
				try(Connection conn = jsJB.getConnection()){
					SQL sql = new SQL();
					sql.exec(conn, "SET SCHEMA BSCL");
					
					int linea = cli.Linea;
	            	String mensaje = "";
	            	boolean estado = false;
	            	StringBuilder errores = new StringBuilder();
	            	try {
		            	errores.append(validarDatosBasicos(conn, idEmpresaDefecto, cli, identificarLlaves, localizacionSistema));
		            	if(!errores.toString().equals("")) {
		            		throw new Exception(errores.toString());
		            	}
		            	
		            	ValidarDocumentoIdentidad validarDocumentoIdentidad = new ValidarDocumentoIdentidad();
						SalidaValidarDocumentoIdentidad documentoValido = validarDocumentoIdentidad.logica(conn, idEmpresaDefecto, in.p_Entidad, cli.TipoDocumentoIdentidad, cli.DocumentoIdentidad, cli.TipoPersona);
						if (documentoValido.Valido.equals("S")) {
							//persona no existe, enrolar.
							
							ConsultarEnrolamiento consultarEnrolamiento = new ConsultarEnrolamiento();
							EntradaConsultaEnrolamiento entradaConsultaEnrolamiento = new EntradaConsultaEnrolamiento();
							entradaConsultaEnrolamiento.codigoPais = documentoValido.CodigoPais;
							entradaConsultaEnrolamiento.enrolamiento = new Enrolamiento();
							entradaConsultaEnrolamiento.enrolamiento.IdTipoEntidad = "CLIENTE";
							entradaConsultaEnrolamiento.enrolamiento.IdTipoDocumentoIdentidad = cli.TipoDocumentoIdentidad;
							entradaConsultaEnrolamiento.enrolamiento.DocumentoIdentidad = cli.DocumentoIdentidad;
							entradaConsultaEnrolamiento.enrolamiento.Tipo = cli.TipoPersona;
							
							SalidaConsultaEnrolamiento salidaConsultaEnrolamiento = consultarEnrolamiento.logica(conn, entradaConsultaEnrolamiento);
							
							Long idEnrolamiento = null;
							Long idSocioNegocio = null;
							Long idPersona = null;
							Long idDocumentoIdentidad = null;
							
							Long idDireccionFisica = null;
							Long idDireccionElectronica = null;
							Long idTelefono = null;
							
							if(salidaConsultaEnrolamiento.enrolamientos.isEmpty()){
								// no existe, enrolar.
								idEnrolamiento = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::Cliente", (long) 1).get(0);
								
								EnrolamientoCliente enrCliente = new EnrolamientoCliente();
								enrCliente.IdEnrolamiento = idEnrolamiento;
								enrCliente.IdEmpresa = idEmpresaDefecto;
								enrCliente.IdUsuario = idUsuarioDefecto;
								enrCliente.IdTipoEntidad = "CLIENTE";
								enrCliente.IdEntidadEnrolada = new Long(0);
								enrCliente.IdTipoDocumentoIdentidad = cli.TipoDocumentoIdentidad;
								enrCliente.DocumentoIdentidad = cli.DocumentoIdentidad;
								enrCliente.Tipo = cli.TipoPersona;
								enrCliente.Nombres = cli.Nombres;
								enrCliente.ApellidoMaterno = cli.ApellidoMaterno;
								enrCliente.ApellidoPaterno = cli.ApellidoPaterno;
								enrCliente.RazonSocial = cli.RazonSocial;
								enrCliente.IdPaisNacimiento = identificarLlaves.getPais(cli.PaisNacimientoConstitucion);
								enrCliente.FechaNacimiento = cli.FechaNacimientoConstitucion;
								enrCliente.Sexo = cli.Genero;
								if(enrCliente.Sexo==null || enrCliente.Sexo.equalsIgnoreCase("null") ) {
									enrCliente.Sexo="";
								}
								enrCliente.EstadoCivil = identificarLlaves.getEstadoCivil(cli.TipoEstadoCivil);
								if(enrCliente.EstadoCivil==null) {
									enrCliente.EstadoCivil="";
								}
								if(enrCliente.Tipo.equals("N") && enrCliente.EstadoCivil.equals("")) {
									errores.append(" ESTADO CIVIL: " + cli.TipoEstadoCivil + ", NO EXISTE");
								}
	
								enrCliente.IdCiudad = identificarLlaves.getCiudadUbigeo(cli.CiudadDistrito);
								if(enrCliente.IdCiudad==null) {
									errores.append(" CIUDAD: " + cli.CiudadDistrito+ ", NO EXISTE");
								}
								enrCliente.IdTipoTelefono = "FIJO";
								enrCliente.Telefono = cli.Telefono;
								enrCliente.Email = cli.Email;
								enrCliente.DireccionCalle = cli.Calle;
								enrCliente.CLI_IdCategoriaInversionista = identificarLlaves.getIdCategoriaInversionista(cli.NombreCortoCategoriaInversionista);
								enrCliente.POR_IdPerfilComercial = "ARRIESGADO";
								enrCliente.CLI_IdTipoCliente = cli.TipoCliente;
																
								try {
									if(errores.toString().length()==0) {
										int[] resultado = sql.batch(conn, SQL_INSERT_ENROLAMIENTO, Arrays.asList(enrCliente), EnrolamientoCliente.class);
									}else {
										throw new BreakException(errores.toString());
									}
								}catch(Exception e) {
									throw new BreakException("ERROR AL ENROLAR CLIENTE " +e.getMessage());
								}
							}else{
								idEnrolamiento = salidaConsultaEnrolamiento.enrolamientos.get(0).IdEnrolamiento;
							}
							
							HashMap<String, Object> params = new HashMap<>();
							params.put("DocumentoIdentidad", cli.DocumentoIdentidad);
							params.put("TipoDocumentoIdentidad", cli.TipoDocumentoIdentidad);
							params.put("Tipo", cli.TipoPersona);
							params.put("CodigoPais", documentoValido.CodigoPais);
							List<ConsultaExistePersona> contadorPersona = sql.query(conn, SQL_ConsultarExistenciaPersona, params, ConsultaExistePersona.class);
							boolean existePersona = (contadorPersona.isEmpty()) ? false : true;
							if(existePersona){
								idPersona = contadorPersona.get(0).IdPersona;
								idSocioNegocio = contadorPersona.get(0).IdSocioDeNegocio;
								//throw new BreakException("PERSONA " + cli.DocumentoIdentidad + " YA EXISTE");
							}else{
								
								idPersona = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::Persona", (long) 1).get(0);
		
								EnrolamientoPersona persona = new EnrolamientoPersona();
								persona.IdPersona = idPersona;
								persona.IdEstado = "ACT";
								
								persona.Tipo = cli.TipoPersona;
								persona.Nombres = cli.Nombres;
								persona.ApellidoPaterno = cli.ApellidoPaterno;
								persona.ApellidoMaterno = cli.ApellidoMaterno;
								persona.RazonSocial = cli.RazonSocial;
								persona.IdPaisNacimiento = identificarLlaves.getPais(cli.PaisNacimientoConstitucion);
								persona.IdPaisNacionalidadPrincipal = identificarLlaves.getPais(cli.PaisNacionalidadPrincipalCasaMatriz);
								persona.IdPaisResidencia = identificarLlaves.getPais(cli.PaisResidenciaPrincipal);
								persona.FechaNacimiento = cli.FechaNacimientoConstitucion;
								persona.Sexo = cli.Genero;
								if(persona.Sexo==null || persona.Sexo.equalsIgnoreCase("null")) {
									persona.Sexo="";
								}
								persona.EstadoCivil = identificarLlaves.getEstadoCivil(cli.TipoEstadoCivil);
								if(persona.EstadoCivil==null) {
									persona.EstadoCivil="";
								}
								persona.FechaIngreso = fechaSistema;
								
								persona.EsEnrolado = "S";
								persona.IdUsuarioCreador = idUsuarioDefecto;
								persona.IdCiudad = identificarLlaves.getCiudadUbigeo(cli.CiudadDistrito);
								persona.IdCarga = idCarga;
								
								int[] res = null;
								try {
									res = sql.batch(conn, SQL_CREAR_PERSONA, Arrays.asList(persona), EnrolamientoPersona.class);
								}catch(Exception e) {
									throw new BreakException(" ERROR AL CREAR PERSONA; "+e.getMessage());
								}
								if(res!=null && res.length>0 && res[0]==1){
									//System.out.println("RES PER: " + idPersona);
									idSocioNegocio = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::SocioDeNegocio", (long) 1).get(0);
									//se creo la persona, crear socio de negocio
									persona.IdSocioNegocio = idSocioNegocio;
									persona.IdEmpresa = idEmpresaDefecto;
									try {
									res = sql.batch(conn, SQL_CREAR_SOCIO_NEGOCIO, Arrays.asList(persona), EnrolamientoPersona.class);
									}catch(Exception e) {
										errores.append(" ERROR AL CREAR SOCIO NEGOCIO; "+e.getMessage());
									}
									System.out.println("RES SOC: " + ((res!=null && res.length>0 && res[0]==1) ? idSocioNegocio : false ));
									
									//crear fatca
									persona.FechaHoraSistema = fechaHoraSistema;
									try {
										res = sql.batch(conn, SQL_CREAR_FATCA, Arrays.asList(persona), EnrolamientoPersona.class);
									}catch(Exception e) {
										throw new BreakException(" ERROR AL CREAR FATCA; "+e.getMessage());
									}
									System.out.println("RES FAT: " + (res!=null && res.length>0 && res[0]==1));
									
									//ingresar documento identidad
									CrearDocumentoIdentidad crearDocumentoIdentidad = new CrearDocumentoIdentidad();
									EntradaCrearDocumentoIdentidad entradaDocumentoIdentidad = new EntradaCrearDocumentoIdentidad();
									entradaDocumentoIdentidad.p_IdUsuario = idUsuarioDefecto;
									entradaDocumentoIdentidad.p_IdEmpresa = idEmpresaDefecto;
									entradaDocumentoIdentidad.p_IdModulo = in.p_IdModulo;
									entradaDocumentoIdentidad.p_UsaReglasNegocio = "N";
									
									entradaDocumentoIdentidad.p_IdPersona = idPersona;
									
									entradaDocumentoIdentidad.p_IdTipoDocumentoIdentidad = cli.TipoDocumentoIdentidad;
									entradaDocumentoIdentidad.p_DocumentoIdentidad = cli.DocumentoIdentidad;
									
									entradaDocumentoIdentidad.p_IdTipoDocumentoIdentidadAsociado = cli.TipoDocumentoIdentidadAsociado;
									
									entradaDocumentoIdentidad.p_TipoPersona = persona.Tipo;
									SalidaCrearDocumentoIdentidad salidaDocumentoIdentidad = crearDocumentoIdentidad.logica(conn, entradaDocumentoIdentidad);
									if(salidaDocumentoIdentidad.P_OUT_ERRORESCONTADOR==0){
										idDocumentoIdentidad = salidaDocumentoIdentidad.p_out_RefId;
										System.out.println("RES DOC: " + idDocumentoIdentidad);
									}else{
										for (ErrorDetalle error : salidaDocumentoIdentidad.P_OUT_ERRORESDETALLE) {
											System.out.println(error.Descripcion);
										}
										throw new BreakException(" ERROR AL CREAR DOCUMENTO IDENTIDAD");
									}
									
									
									//ingresar direccion fisica
									persona.Distrito =  identificarLlaves.getCiudad(cli.Distrito);
									if(persona.Distrito!=null && persona.Distrito !=0 && persona.IdCiudad!=null && persona.IdCiudad!=0){
										CrearDireccionFisica crearDireccionFisica = new CrearDireccionFisica();
										EntradaCrearDireccionFisica entradaDireccionFisica = new EntradaCrearDireccionFisica();
										entradaDireccionFisica.p_IdUsuario = idUsuarioDefecto;
										entradaDireccionFisica.p_IdEmpresa = idEmpresaDefecto;
										entradaDireccionFisica.p_IdModulo = in.p_IdModulo;
										entradaDireccionFisica.p_UsaReglasNegocio = "N";
										
										entradaDireccionFisica.p_Calle = cli.Calle;
										entradaDireccionFisica.p_Numero = cli.Numero;
										entradaDireccionFisica.p_Departamento = cli.Complemento;
										entradaDireccionFisica.p_Sector = "";
										entradaDireccionFisica.p_CodigoPostal = "";
										entradaDireccionFisica.p_IdCiudad = persona.IdCiudad;
										entradaDireccionFisica.p_IdDivisionPais = persona.Distrito;
										entradaDireccionFisica.p_Tipo = persona.Tipo;
										entradaDireccionFisica.p_Comentario = "";
										entradaDireccionFisica.p_IdEstado = "ACT";
										entradaDireccionFisica.p_IdPersona = idPersona;
										
										SalidaCrearDireccionFisica salidaDireccionFisica = crearDireccionFisica.logica(conn, entradaDireccionFisica);
										if(salidaDireccionFisica.P_OUT_ADVERTENCIASCONTADOR==0 && salidaDireccionFisica.P_OUT_ERRORESCONTADOR==0){
											idDireccionFisica = salidaDireccionFisica.p_out_RefId;
											System.out.println("RES DIR FIS: " + idDireccionFisica);
										}else{
											for (ErrorDetalle error : salidaDireccionFisica.P_OUT_ERRORESDETALLE) {
												System.out.println(error.Descripcion);
											}
											throw new BreakException(" ERROR AL CREAR DIRECCION FISICA");
										}
									}
		
									//ingresar telefono
									if(cli.Telefono!=null && !cli.Telefono.equals("")){
										params = new HashMap<>();
										params.put("IdCiudad", persona.IdCiudad);
										List<Ids> idsPaisCiudad = sql.query(conn, SQL_ConsultarIdPaisCiudad, params, Ids.class);
										if(!idsPaisCiudad.isEmpty()){
											CrearTelefono crearTelefono = new CrearTelefono();
											EntradaCrearTelefono entradadCrearTelefono = new EntradaCrearTelefono();
											entradadCrearTelefono.p_IdUsuario = idUsuarioDefecto;
											entradadCrearTelefono.p_IdEmpresa = idEmpresaDefecto;
											entradadCrearTelefono.p_IdModulo = in.p_IdModulo;
											entradadCrearTelefono.p_UsaReglasNegocio = "N";
											
											entradadCrearTelefono.p_IdPais = idsPaisCiudad.get(0).Id;
											entradadCrearTelefono.p_IdTipoTelefono = "FIJO";
											entradadCrearTelefono.p_Numero = cli.Telefono;
											entradadCrearTelefono.p_Anexo = "";
											entradadCrearTelefono.p_Tipo = persona.Tipo;
											entradadCrearTelefono.p_IdPersona = idPersona;
											
											SalidaCrearTelefono salidaCrearTelefono = crearTelefono.logica(conn, entradadCrearTelefono);
											if(salidaCrearTelefono.P_OUT_ADVERTENCIASCONTADOR==0 && salidaCrearTelefono.P_OUT_ERRORESCONTADOR==0){
												idTelefono = salidaCrearTelefono.p_out_RefId;
												System.out.println("RES TEL: " + idTelefono);
											}else{
												for (ErrorDetalle error : salidaCrearTelefono.P_OUT_ERRORESDETALLE) {
													System.out.println(error.Descripcion);
												}
												throw new BreakException(" ERROR AL CREAR TELEFONO");
											}
										}
									}
									
									//ingresar direccion electronica
									if(cli.Email!=null && !cli.Email.equals("")){
										CrearDireccionElectronica crearDireccionelectronica = new CrearDireccionElectronica();
										EntradaCrearDireccionElectronica entradaDireccionElectronica = new EntradaCrearDireccionElectronica();
										entradaDireccionElectronica.p_IdUsuario = idUsuarioDefecto;
										entradaDireccionElectronica.p_IdEmpresa = idEmpresaDefecto;
										entradaDireccionElectronica.p_IdModulo = in.p_IdModulo;
										entradaDireccionElectronica.p_UsaReglasNegocio = "N";
										
										entradaDireccionElectronica.p_Comentario = "";
										entradaDireccionElectronica.p_Direccion = cli.Email;
										entradaDireccionElectronica.p_IdPersona = idPersona;
										
										SalidaCrearDireccionElectronica salidaCrearDireccionElectronica = crearDireccionelectronica.logica(conn, entradaDireccionElectronica);
										if(salidaCrearDireccionElectronica.P_OUT_ADVERTENCIASCONTADOR==0 && salidaCrearDireccionElectronica.P_OUT_ERRORESCONTADOR==0){
											idDireccionElectronica = salidaCrearDireccionElectronica.p_out_RefId;
											System.out.println("RES DIR ELE: " + idDireccionElectronica);
										}else{
											for (ErrorDetalle error : salidaCrearDireccionElectronica.P_OUT_ERRORESDETALLE) {
												System.out.println(error.Descripcion);
											}
											throw new BreakException(" ERROR AL CREAR DIRECCION ELECTRONICA");
										}
									}
									
									//actualizar los ids de la persona con cada entidad que fue ingresada
									EntidadCrearPersonaUpdate updatePersona = new EntidadCrearPersonaUpdate();
									updatePersona.p_IdDireccionElectronicaPrincipal = idDireccionElectronica;
									updatePersona.p_IdDireccionFisicaPrincipal = idDireccionFisica;
									updatePersona.p_IdDocumentoIdentidad = idDocumentoIdentidad;
									updatePersona.p_IdPersona = idPersona;
									updatePersona.p_IdTelefonoPrincipal = idTelefono;
									try {
										int[] resUpdatePersona = sql.batch(conn, SQL_CREAR_PERSONA_UPDATE, Arrays.asList(updatePersona), EntidadCrearPersonaUpdate.class);
										if(resUpdatePersona==null || resUpdatePersona.length==0 || resUpdatePersona[0]==0){
											errores.append("UI_ERROR_CREAR_PERSONA");
										}
									}catch(Exception e) {
										throw new BreakException(" ERROR AL ACTUALIZAR IDS RELACIONES PERSONA; "+e.getMessage());
									}
									
									//actualizar idEntidadEnrolada en tabla enrolamiento
									EntidadCrearPersonaUpdateSocioNegocio updateEnrolamiento = new EntidadCrearPersonaUpdateSocioNegocio();
									updateEnrolamiento.p_IdEnrolamiento = idEnrolamiento;
									updateEnrolamiento.p_IdPersona = idPersona;
									try {
										int[] resUpdateSocioNegocio = sql.batch(conn, SQL_CREAR_PERSONA_ENROLAMIENTO_UPDATE, Arrays.asList(updateEnrolamiento), EntidadCrearPersonaUpdateSocioNegocio.class);									
										if(resUpdateSocioNegocio==null || resUpdateSocioNegocio.length==0 || resUpdateSocioNegocio[0]==0){
											throw new BreakException("UI_ERROR_ACTUALIZAR_ENROLAMIENTO");
										}
									}catch(Exception e) {
										throw new BreakException(" ERROR AL ACTUALIZAR IDS RELACIONES ENROLAMIENTO; "+e.getMessage());
									}
									
								}
							}
							//buscar el idPersona del ejecutivo asumiendo que el documento de identidad es DNI
							
							params = new HashMap<String, Object>();
							//params.put("DocumentoIdentidadAsesor", cli.DocumentoIdentidadAsesor);
							//viene el codigo del asesor, no el documento de identidad. problema de nombres solamente
							params.put("CodigoEmpleado", cli.getDocumentoIdentidadAsesor());//02000054610000//TODO Obtener del archivo
							
							List<Ids> idPersonasEjecutivos = sql.query(conn, SQL_ConsultarIdPersonaEjecutivo, params, Ids.class);
							Long idPersonaEjecutivo = null;
							if(idPersonasEjecutivos!=null && idPersonasEjecutivos.size()==1){
								idPersonaEjecutivo = idPersonasEjecutivos.get(0).Id;
							}else{
								throw new BreakException(" NO EXISTE CODIGO ASESOR: " + cli.getDocumentoIdentidadAsesor());
							}
							
							//crear cliente
							long idCliente = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::Cliente", (long) 1).get(0);
							
							EntidadCliente entidadCliente = new EntidadCliente();
							entidadCliente.IdCliente = idCliente;
							entidadCliente.IdPersona = idPersona;
							entidadCliente.IdEmpresa = idEmpresaDefecto;
							entidadCliente.IdEstado = "ACT";
							entidadCliente.IdSegmentoCliente = null;
							entidadCliente.IdCategoriaInversionista = identificarLlaves.getIdCategoriaInversionista(cli.NombreCortoCategoriaInversionista);//DE
							if(entidadCliente.IdCategoriaInversionista==null) {
								errores.append("; NO EXISTE CATEGORIA INVERSIONISTA: " + cli.NombreCortoCategoriaInversionista);
							}
							entidadCliente.IdUsuarioCreador = idUsuarioDefecto; 
							entidadCliente.EsEnrolado = "S";
							entidadCliente.IdTipoCliente = cli.TipoCliente;
							if(entidadCliente.IdTipoCliente==null) {
								errores.append("; NO EXISTE TIPO CLIENTE: " + cli.TipoCliente);
							}
							entidadCliente.IdPlantillaLimiteExtra = "CLI_" + idCliente;
							entidadCliente.IdPersonaEjecutivo = idPersonaEjecutivo;
							entidadCliente.CodigoAdicionalHolding = "";
							entidadCliente.IdPersonaUsuario = idPersonaEjecutivo;
							entidadCliente.IdCarga = idCarga;
							try {
								if(errores.toString().length()==0) {
									int[] resCliente = sql.batch(conn, SQL_CREAR_CLIENTE, Arrays.asList(entidadCliente), EntidadCliente.class);	
								}else {
									throw new BreakException(errores.toString());
								}
								
							}catch(Exception e) {
								throw new BreakException(e.getMessage());
							}
		
							//crear portafolio cliente.
							HashMap<String,Object>  paramsIn = new HashMap<String,Object>();
							paramsIn.put("IdEmpresa", idEmpresaDefecto);
							paramsIn.put("IdUsuario", idUsuarioDefecto);
							paramsIn.put("IdModulo", in.p_IdModulo);
							
							paramsIn.put("IdCliente", idCliente);
							paramsIn.put("IdPerfilComercial", "RETAIL");//identificarLlaves.getIdCategoriaInversionista(cli.NombreCortoCategoriaInversionista));
								
							HashMap<String,Class>  paramsOut = new HashMap<String,Class>();
							paramsOut.put("p_out_RefId", Long.class);
							paramsOut.put("p_out_AdvertenciasContador", Long.class);
							paramsOut.put("p_out_ErroresContador", Long.class);
							paramsOut.put("p_out_ErroresDetalle", ErrorDetalle.class);
							paramsOut.put("p_out_AdvertenciasDetalle", AdvertenciaDetalle.class);
							Long idPortafolio = null;
							try {
								ErrorDetalle asd = new ErrorDetalle();
								HashMap<String,Object> resultSP = sql.call(conn, SQL_CREAR_PORTAFOLIO_Call, paramsIn, paramsOut);
								if(((Long) resultSP.get("p_out_ErroresContador")).equals(new Long(0))){
									idPortafolio= (Long)resultSP.get("p_out_RefId");
								}else{
									for(ErrorDetalle errorDetalle : (List<ErrorDetalle>) resultSP.get("p_out_ErroresDetalle")){
										throw new BreakException(errorDetalle.Descripcion);
									}
									for(AdvertenciaDetalle advertenciaDetalle : (List<AdvertenciaDetalle>) resultSP.get("p_out_AdvertenciasDetalle")){
										throw new BreakException(advertenciaDetalle.Descripcion);
									}
								}
							}catch(Exception e) {
								throw new BreakException(" ERROR AL CREAR PORTAFOLIO; "+e.getMessage());
							}
							
							//actualizar socio de negocio con nuevo idCliente
							EntidadActualizarSocioNegocio updateSocioNegocio = new EntidadActualizarSocioNegocio();
							updateSocioNegocio.IdSocioDeNegocio = idSocioNegocio;
							updateSocioNegocio.IdCliente = idCliente;
							try {
								int[] resUpdateSocioNegocio = sql.batch(conn, SQL_ACTUALIZAR_SOCIO_NEGOCIO, Arrays.asList(updateSocioNegocio), EntidadActualizarSocioNegocio.class);									
								if(resUpdateSocioNegocio==null || (resUpdateSocioNegocio.length>0 && resUpdateSocioNegocio[0]==0)){
									throw new BreakException("UI_ERROR_ACTUALIZAR_SOCIONEGOCIO");
								}
							}catch(Exception e) {
								throw new BreakException("ERROR AL ACTUALIZAR SOCIO NEGOCIO; "+e.getMessage());
							}
							
							//Actualizar enrolamiento con IdEntidadEnrolada 
							EntidadActualizarEnrolamiento updateEnrolamiento = new EntidadActualizarEnrolamiento();
							updateEnrolamiento.IdCliente = idCliente;
							updateEnrolamiento.IdEnrolamiento = idEnrolamiento;
							try {
								int[] resUpdateEnrolamiento = sql.batch(conn, SQL_ACTUALIZAR_ENROLAMIENTO, Arrays.asList(updateEnrolamiento), EntidadActualizarEnrolamiento	.class);									
								if(resUpdateEnrolamiento==null || (resUpdateEnrolamiento.length>0 && resUpdateEnrolamiento[0]==0)){
									throw new BreakException("UI_ERROR_ACTUALIZAR_ENROLAMIENTO");
								}
							}catch(Exception e) {
								throw new BreakException(" ERROR AL ACTUALIZAR ENROLAMIENTO; "+e.getMessage());
							}
							
							
							if(documentoValido.CodigoPais.equals("PE") && cli.TipoCliente != null){
								//obtener preferencias de negocio del portafolio
								ConsultarIndicadores consultarIndicadores = new ConsultarIndicadores();
								ConsultarIndicadores.EntradaServicioIndicadores entradaConsultarIndicadores = new ConsultarIndicadores.EntradaServicioIndicadores(); 
								entradaConsultarIndicadores.p_IdEmpresa = in.p_IdEmpresa;
								entradaConsultarIndicadores.p_ModeloCliente = cli.TipoCliente;
								entradaConsultarIndicadores.p_IdTipoEntidad = "PORTAFOLIO";
								ConsultarIndicadores.SalidaServicioIndicadores salidaConsultarIndicadores = consultarIndicadores.logica(conn, entradaConsultarIndicadores);
								HashMap<String, Indicador> indicadores = new HashMap<>();
								for(Indicador indicador : salidaConsultarIndicadores.p_out_Indicadores) {
									indicadores.put(indicador.CodigoTipoIndicador, indicador);
								}
								
								boolean existeAlgunIndicador = false;
								String tipoCustodia = "";
								String tipoCuenta = "";
								//obtener valores por defecto
								ConsultarValoresPorDefecto consultarValoresDefecto = new ConsultarValoresPorDefecto();
								EntradaConsultarValoresPorDefecto entradaValoresDefecto = new EntradaConsultarValoresPorDefecto();
								entradaValoresDefecto.p_IdEmpresa = in.p_IdEmpresa;
								entradaValoresDefecto.p_TipoEntidad = "ENROLAMIENTO";
								entradaValoresDefecto.p_IdTipoCanalIngreso = "SIS";
								HashMap<String, String> valoresDefecto = consultarValoresDefecto.logica(conn, entradaValoresDefecto);
								
								//obtener indicadores ORDOCUSTRV y ORDOCUSTRF
								if( (indicadores.get("ORDOCUSTRV")!=null && indicadores.get("ORDOCUSTRV").ValorIndicador.equals("S")) ||  
										(indicadores.get("ORDOCUSTRF")!=null && indicadores.get("ORDOCUSTRF").ValorIndicador.equals("S"))) {
									//obtener las llaves de los valores por defecto
									tipoCustodia = "IdTipoCustodia";
									tipoCuenta = "IdTipoCuentaCustodia";
									existeAlgunIndicador=true;
								}
								
								//obtener indicadores ORDCUENTAPROPIA y ORDCUENTABANCO
								if( (indicadores.get("ORDCUENTAPROPIA")!=null && indicadores.get("ORDCUENTAPROPIA").ValorIndicador.equals("S")) ||  
										(indicadores.get("ORDCUENTABANCO")!=null && indicadores.get("ORDCUENTABANCO").ValorIndicador.equals("S"))) {
									
									tipoCustodia = (indicadores.get("ORDCUENTAPROPIA").ValorIndicador.equals("S")) ? "IdTipoCustodiaPropia" : "IdTipoCustodiaBanco";
									tipoCuenta = (indicadores.get("ORDCUENTAPROPIA").ValorIndicador.equals("S")) ? "IdTipoCustodiaMandante" : "IdTipoCustodiaDepositante";
									existeAlgunIndicador = true;
								}
								
								if(existeAlgunIndicador) {
									//crear cuenta custodia 
																	
									paramsIn = new HashMap<String,Object>();
									paramsIn.put("IdEmpresa", in.p_IdEmpresa);
									paramsIn.put("IdUsuario", in.p_IdUsuario);
									paramsIn.put("IdModulo", in.p_IdModulo);
									
									paramsIn.put("p_IdPortafolio", idPortafolio);
									paramsIn.put("p_IdBoveda", valoresDefecto.get("IdBovedaCuentaCustodia"));
									paramsIn.put("p_Codigo", cli.RUT);
									paramsIn.put("p_IdTipoCuentaCustodia", valoresDefecto.get(tipoCuenta));
									
									paramsIn.put("p_IdTipoCustodia", valoresDefecto.get(tipoCustodia));
									paramsIn.put("p_IdModeloCustodia", valoresDefecto.get("IdModeloCustodia"));
									
									//Salida HashMap
									paramsOut = new HashMap<String,Class>();
									paramsOut.put("p_out_RefId", Long.class);
									paramsOut.put("p_out_AdvertenciasContador", Long.class);
									paramsOut.put("p_out_ErroresContador", Long.class);
									paramsOut.put("p_out_ErroresDetalle", ErrorDetalle.class);
									paramsOut.put("p_out_AdvertenciasDetalle", AdvertenciaDetalle.class);
									paramsOut.put("p_out_idAuth", Long.class);
									
									HashMap<String,Object> resultSP = sql.call(conn, SQL_CREAR_CUENTA_CUSTODIA_Call, paramsIn, paramsOut);
									if(!((Long) resultSP.get("p_out_ErroresContador")).equals(new Long(0))){
										for(ErrorDetalle errorDetalle : (List<ErrorDetalle>) resultSP.get("p_out_ErroresDetalle")){
											throw new BreakException(errorDetalle.Descripcion);
										}
									}
								}else {
									throw new BreakException("El Tipo de Cliente SAB, se encuentra mal configurado. Debe operar con tipo cuenta custodio, propio o banco.");
								}
								
							}
							
							//identificar si existe un documento de identidad adicional (PERU)
							Long idDocumentoIdentidadAdicional = null;
							EntradaCrearDocumentoIdentidad entradaDocumentoIdentidad = new EntradaCrearDocumentoIdentidad();
							
							try {
								if(documentoValido.CodigoPais.equals("PE") && !cli.RUT.isEmpty() && !cli.RUT.equals("")){
									params = new HashMap<>();
									params.put("DocumentoIdentidadAdicional", cli.RUT);
									params.put("TipoDocumentoIdentidad", "RUT");
									params.put("IdEmpresa", idEmpresaDefecto);
									List<Contador> contadorDocumentoAdicioanl = sql.query(conn, SQL_ConsultarExistenciaDocumentoAdicional, params, Contador.class);
									if(!contadorDocumentoAdicioanl.isEmpty() && contadorDocumentoAdicioanl.get(0).Count==1){
										throw new BreakException(Diccionario.getMensaje(conn, idEmpresaDefecto, "EXISTE_DOCUMENTO_ADICIONAL", new HashMap<>()));
									}else{
										
										//ingresar documento identidad adicional.
										idDocumentoIdentidadAdicional = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad", (long) 1).get(0);
										CrearDocumentoIdentidad crearDocumentoIdentidad = new CrearDocumentoIdentidad();
										entradaDocumentoIdentidad = new EntradaCrearDocumentoIdentidad();
										entradaDocumentoIdentidad.p_IdUsuario = idUsuarioDefecto;
										entradaDocumentoIdentidad.p_IdEmpresa = idEmpresaDefecto;
										entradaDocumentoIdentidad.p_IdModulo = in.p_IdModulo;
										entradaDocumentoIdentidad.p_UsaReglasNegocio = "N";
										
										entradaDocumentoIdentidad.p_IdPersona = idPersona;
										entradaDocumentoIdentidad.p_IdTipoDocumentoIdentidad = "RUT";
										entradaDocumentoIdentidad.p_IdTipoDocumentoIdentidadAsociado = "RUT";
										entradaDocumentoIdentidad.p_DocumentoIdentidad = cli.RUT;
										entradaDocumentoIdentidad.p_TipoPersona = cli.TipoPersona;
										SalidaCrearDocumentoIdentidad salidaDocumentoIdentidad = crearDocumentoIdentidad.logica(conn, entradaDocumentoIdentidad);
										if(salidaDocumentoIdentidad.P_OUT_ERRORESCONTADOR==0){
											idDocumentoIdentidadAdicional = salidaDocumentoIdentidad.p_out_RefId;
											System.out.println("RES DOC ADI: " + idDocumentoIdentidadAdicional);
											
											//actualizar los ids de la persona con cada entidad que fue ingresada
											EntidadCrearClienteUpdate updatePersona = new EntidadCrearClienteUpdate();
											updatePersona.p_IdDocumentoIdentidadAdicional = (idDocumentoIdentidadAdicional!=null) ? idDocumentoIdentidadAdicional : 0;
											updatePersona.p_IdPersona = idPersona;
											sql.batch(conn, SQL_ACTUALIZAR_DOCUMENTO_ADICIONAL_PERSONA, Arrays.asList(updatePersona), EntidadCrearClienteUpdate.class);
											
										}else{
											for (ErrorDetalle error : salidaDocumentoIdentidad.P_OUT_ERRORESDETALLE) {
												throw new BreakException(error.Descripcion);
											}
										}
										
									}
								}
							}catch(Exception e) {
								throw new BreakException(" ERROR AL CREAR DOCUMENTO IDENTIDAD ADICIONAL; "+e.getMessage());
							}
							
							//fin
							if(errores.toString().equals("")) {
								mensaje = (linea + " - CLIENTE " + cli.DocumentoIdentidad + " INGRESADO CORRECTAMENTE");
								estado = true;	
							}
						}else{
							mensaje = (linea + " - CLIENTE " + cli.DocumentoIdentidad + " YA EXISTE");
						}
	            	}catch(Exception e) {
	            		mensaje = (linea + " - CLIENTE " + cli.DocumentoIdentidad + " ERROR GRAVE; "+e.getMessage());
	            	}
	            	
	            	LoaderUtils loader = new LoaderUtils();
	            	loader.log(conn, fechaSistema, codigoPais, "CLIENTE", linea, mensaje, (estado)?"OK":"NOK", cli.toString());
	            	if(estado) {
	            		conn.commit();
	            	}else {
	            		conn.rollback();
	            	}
	            	salida.add(mensaje);
				} catch (SQLException e) {
					salida.add(cli.Linea + " - ERROR AL REALIZAR COMMIT");
					e.printStackTrace();
				}
        	});
        }catch (Exception e) {
			e.printStackTrace();
			salida.add("ERROR AL INGRESAR LOS CLIENTES");
		}
		return salida;
	}

	private String validarDatosBasicos(Connection conn, Long idEmpresa, Cliente cli, IdentificarLlaves identificarLlaves, String codigoPais) throws Exception {
		StringBuffer salida =  new StringBuffer();
		salida.append("");
		
		if(cli.DocumentoIdentidad==null || cli.DocumentoIdentidad.length()==0) {
			salida.append("DocumentoIdentidad es obligatorio;");
		}
		if(cli.TipoDocumentoIdentidad==null || cli.TipoDocumentoIdentidad.length()==0) {
			salida.append("TipoDocumentoIdentidad es obligatorio;");
		}
		if(cli.TipoPersona==null || cli.TipoPersona.length()==0) {
			salida.append("TipoPersona es obligatorio;");
		}
		if(identificarLlaves.getPais(cli.PaisNacimientoConstitucion)==null || cli.DocumentoIdentidad.length()==0) {
			salida.append("PaisNacimientoConstitucion es invalido: " + cli.PaisNacimientoConstitucion+";");
		}
		if((cli.TipoEstadoCivil==null || cli.TipoEstadoCivil.length()==0) && cli.TipoPersona.equalsIgnoreCase("N")) {
			salida.append("TipoEstadoCivil es invalido: " + cli.TipoEstadoCivil+";");
		}
		if(cli.NombreCortoCategoriaInversionista==null || cli.NombreCortoCategoriaInversionista.length()==0) {
			salida.append("NombreCortoCategoriaInversionista es invalido: " + cli.NombreCortoCategoriaInversionista+";");
		}
		if(cli.getDocumentoIdentidadAsesor()==null || cli.getDocumentoIdentidadAsesor().length()==0) {
			salida.append("IUP_Ejecutivo es invalido: " + cli.getDocumentoIdentidadAsesor()+";");
		}
		if(cli.TipoCliente==null || cli.TipoCliente.length()==0 || cli.TipoCliente.equals("NULL")) {
			salida.append("TipoCliente es obligatorio"+";");
		}
		
		//validar que existan preferencias para el cliente
		if(codigoPais.equals("PE") && cli.TipoCliente != null){
			//obtener preferencias de negocio del portafolio
			ConsultarIndicadores consultarIndicadores = new ConsultarIndicadores();
			ConsultarIndicadores.EntradaServicioIndicadores entradaConsultarIndicadores = new ConsultarIndicadores.EntradaServicioIndicadores(); 
			entradaConsultarIndicadores.p_IdEmpresa = idEmpresa;
			entradaConsultarIndicadores.p_ModeloCliente = cli.TipoCliente;
			entradaConsultarIndicadores.p_IdTipoEntidad = "PORTAFOLIO";
			ConsultarIndicadores.SalidaServicioIndicadores salidaConsultarIndicadores = consultarIndicadores.logica(conn, entradaConsultarIndicadores);
			HashMap<String, Indicador> indicadores = new HashMap<>();
			for(Indicador indicador : salidaConsultarIndicadores.p_out_Indicadores) {
				indicadores.put(indicador.CodigoTipoIndicador, indicador);
			}
			
			boolean existeAlgunIndicador = false;
			String tipoCustodia = "";
			String tipoCuenta = "";
			//obtener valores por defecto
			ConsultarValoresPorDefecto consultarValoresDefecto = new ConsultarValoresPorDefecto();EntradaConsultarValoresPorDefecto entradaValoresDefecto = new EntradaConsultarValoresPorDefecto();
			entradaValoresDefecto.p_IdEmpresa = idEmpresa;
			entradaValoresDefecto.p_TipoEntidad = "ENROLAMIENTO";
			entradaValoresDefecto.p_IdTipoCanalIngreso = "SIS";
			HashMap<String, String> valoresDefecto = consultarValoresDefecto.logica(conn, entradaValoresDefecto);
			
			//obtener indicadores ORDOCUSTRV y ORDOCUSTRF
			if( (indicadores.get("ORDOCUSTRV")!=null && indicadores.get("ORDOCUSTRV").ValorIndicador.equals("S")) ||  
					(indicadores.get("ORDOCUSTRF")!=null && indicadores.get("ORDOCUSTRF").ValorIndicador.equals("S"))) {
				//obtener las llaves de los valores por defecto
				tipoCustodia = "IdTipoCustodia";
				tipoCuenta = "IdTipoCuentaCustodia";
				existeAlgunIndicador=true;
			}
			
			//obtener indicadores ORDCUENTAPROPIA y ORDCUENTABANCO
			if( (indicadores.get("ORDCUENTAPROPIA")!=null && indicadores.get("ORDCUENTAPROPIA").ValorIndicador.equals("S")) ||  
					(indicadores.get("ORDCUENTABANCO")!=null && indicadores.get("ORDCUENTABANCO").ValorIndicador.equals("S"))) {
				
				tipoCustodia = (indicadores.get("ORDCUENTAPROPIA").ValorIndicador.equals("S")) ? "IdTipoCustodiaPropia" : "IdTipoCustodiaBanco";
				tipoCuenta = (indicadores.get("ORDCUENTAPROPIA").ValorIndicador.equals("S")) ? "IdTipoCustodiaMandante" : "IdTipoCustodiaDepositante";
				existeAlgunIndicador = true;
			}
			
			if(!existeAlgunIndicador) {
				salida.append("El Tipo de Cliente SAB, se encuentra mal configurado. Debe operar con tipo cuenta custodio, propio o banco."+";");
			}
		}
		return salida.toString();
	}

}

