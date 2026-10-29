package cl.bcs.socionegocio.cli;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.socionegocio.cli.pojo.Enrolamiento;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CrearBorradorCliente implements JavaCall {

	static final Logger logger = LogManager.getLogger(CrearBorradorCliente.class);
	private final String SQL_SELECT_ENROLAMIENTO_BORRADOR = loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearBorradorCliente_SelectEnrolamientoBorrador.sql");
	private final String SQL_INSERT_ENROLAMIENTO_BORRADOR = loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearBorradorCliente_InsertEnrolamientoBorrador.sql");
	private final String SQL_UPDATE_ENROLAMIENTO_BORRADOR = loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearBorradorCliente_UpdateEnrolamientoBorrador.sql");
	
	public static class ConsultarEnrolamientoObj{
		public Long IdEnrolamiento;
		public Long IdEmpresa;
		public Long IdUsuario;
		public String IdTipoEntidad;
		public Long IdEntidadEnrolada;
		public String IdTipoDocumentoIdentidad;
		public String IdTipoDocumentoIdentidadAsociado;
		public String DocumentoIdentidad;
		public String IdTipoDocumentoIdentidadAdicional;
		public String DocumentoIdentidadAdicional;
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
		public Long IdDivisionPais;
		public String IdTipoTelefono;
		public String Telefono;
		public String Email;
		public String DireccionCalle;
		public String DireccionNumero;
		public String DireccionDepartamento;
		public String FechaCreacion;
		public String FechaModificacion;
		public Long IdPersonaConyuge = 0L;
		public String RegimenConyugal;
		public String CLI_IdCategoriaInversionista;
		public String CLI_IdTipoCliente;
		public Long CLI_IdTipoClienteCMF;
		public Long CLI_IdEmpleadoEjecutivo;
		public String CLI_CodigoAdicionalHolding;
		public String CLI_IdClaseCliente;
		public String POR_IdPerfilComercial;
		public String EMP_CodigoEmpleado;
		public String EMP_CodigoTipoCargoEmpleado;
		public Long EMP_IdEmpleadoSupervisor;
		public Long EMP_IdSucursal;
		public String EMP_FechaInicioCargo;
		public String EMP_FechaTerminoCargo;
		public String CNT_CodigoInscripcion;
		public String CNT_Emisor;
		public String CNT_Depositos;
		public String CNT_Bolsa;
		public String CNT_Corredora;
		public String CNT_CamaraDeCompensacion;
		public String CNT_Banco;
		public String CNT_Boveda;
		public String CNT_Otros;
		public Long PRO_IdAreaProveedor;
		public String EMP_CargoIndefinido;
		public String TipoEnrolamiento;
		
		public ConsultarEnrolamientoObj() {
			this.TipoEnrolamiento = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class CrearBorradorClienteEntrada extends BcsPojo{
		public Long p_IdUsuario;
		public Long p_IdEmpresa;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<ConsultarEnrolamientoObj> p_ConsultarEnrolamiento;
		
	}
	
	public static class CrearBorradorClienteSalida {
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public Long							P_OUT_REFID;
		
		public CrearBorradorClienteSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class BorradorEnrolamientoObj {
		public Long	IdEnrolamiento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CrearBorradorClienteEntrada in = gson.fromJson(jin.toString(), CrearBorradorClienteEntrada.class);
		CrearBorradorClienteSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public CrearBorradorClienteSalida logica(Connection conn, CrearBorradorClienteEntrada in) throws Exception {
		logger.info("INICIO CrearBorradorClienteSalida");
		CrearBorradorClienteSalida out = new CrearBorradorClienteSalida();	
		try {
			SQL _sql = new SQL();
			HashMap<String, Object> params = new HashMap<>();
			params.put("p_llave_IdTipoEntidad", in.p_ConsultarEnrolamiento.get(0).IdTipoEntidad);
			params.put("p_llave_IdTipoDocumentoIdentidad", in.p_ConsultarEnrolamiento.get(0).IdTipoDocumentoIdentidad);
			params.put("p_llave_DocumentoIdentidad", in.p_ConsultarEnrolamiento.get(0).DocumentoIdentidad);
					
			List<BorradorEnrolamientoObj> lstEnrolamientoBorrador = _sql.query(conn, SQL_SELECT_ENROLAMIENTO_BORRADOR, params, BorradorEnrolamientoObj.class);
			List<Enrolamiento> lstEnrola = new ArrayList<Enrolamiento>();
			
			if (lstEnrolamientoBorrador.size() > 0) {
				lstEnrola.add(crearEnrolamientoBorrador(in.p_ConsultarEnrolamiento.get(0), lstEnrolamientoBorrador.get(0).IdEnrolamiento));
				_sql.batch(conn, SQL_UPDATE_ENROLAMIENTO_BORRADOR, lstEnrola, Enrolamiento.class);
			} else {
				List<Long> secuenciaBorrador = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::Enrolamiento", 1L);
				lstEnrola.add(crearEnrolamientoBorrador(in.p_ConsultarEnrolamiento.get(0),secuenciaBorrador.get(0)));
				_sql.batch(conn, SQL_INSERT_ENROLAMIENTO_BORRADOR, lstEnrola, Enrolamiento.class);
			}
			out.P_OUT_REFID = lstEnrola.get(0).IdEnrolamiento;
			
		} catch (Exception ex) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_AL_CREAR_BORRADOR_CLIENTE",new HashMap<String,String>(),conn,in.p_IdEmpresa));
			logger.error(ex);
			throw ex;
		}
		logger.info("TERMINO CrearBorradorClienteSalida");
		return out;
	}
	
	public Enrolamiento crearEnrolamientoBorrador(ConsultarEnrolamientoObj enrol, Long idEnrolamiento) {
		Enrolamiento enrola = new Enrolamiento();
		enrola.IdEnrolamiento = idEnrolamiento;
		enrola.IdEmpresa = enrol.IdEmpresa;
		enrola.IdUsuario = enrol.IdUsuario;
		enrola.IdTipoEntidad = enrol.IdTipoEntidad;
		enrola.IdEntidadEnrolada = enrol.IdEntidadEnrolada;
		enrola.IdTipoDocumentoIdentidad = enrol.IdTipoDocumentoIdentidad;
		enrola.IdTipoDocumentoIdentidadAsociado = enrol.IdTipoDocumentoIdentidadAsociado;
		enrola.DocumentoIdentidad = enrol.DocumentoIdentidad;
		enrola.IdTipoDocumentoIdentidadAdicional = enrol.IdTipoDocumentoIdentidadAdicional;
		enrola.DocumentoIdentidadAdicional = enrol.DocumentoIdentidadAdicional;
		enrola.Tipo = enrol.Tipo;
		enrola.Nombres = enrol.Nombres;
		enrola.ApellidoPaterno = enrol.ApellidoPaterno;
		enrola.ApellidoMaterno = enrol.ApellidoMaterno;
		enrola.RazonSocial = enrol.RazonSocial;
		enrola.IdPaisNacimiento = enrol.IdPaisNacimiento;
		logger.info(enrol.FechaNacimiento);
		enrola.FechaNacimiento = Fecha.toDate(enrol.FechaNacimiento);
		enrola.Sexo = enrol.Sexo;
		enrola.EstadoCivil = enrol.EstadoCivil;
		enrola.IdCiudad = enrol.IdCiudad;
		enrola.IdDivisionPais = enrol.IdDivisionPais;
		enrola.IdTipoTelefono = enrol.IdTipoTelefono;
		enrola.Telefono = enrol.Telefono;
		enrola.Email = enrol.Email;
		enrola.DireccionCalle = enrol.DireccionCalle;
		enrola.DireccionNumero = enrol.DireccionNumero;
		enrola.DireccionDepartamento = enrol.DireccionDepartamento;
		enrola.IdPersonaConyuge = enrol.IdPersonaConyuge;
		enrola.RegimenConyugal = enrol.RegimenConyugal;
		enrola.CLI_IdCategoriaInversionista = enrol.CLI_IdCategoriaInversionista;
		enrola.CLI_IdTipoCliente = enrol.CLI_IdTipoCliente;
		enrola.CLI_IdTipoClienteCMF = enrol.CLI_IdTipoClienteCMF;
		enrola.CLI_IdEmpleadoEjecutivo = enrol.CLI_IdEmpleadoEjecutivo;
		enrola.CLI_CodigoAdicionalHolding = enrol.CLI_CodigoAdicionalHolding;
		enrola.CLI_IdClaseCliente = enrol.CLI_IdClaseCliente;
		enrola.POR_IdPerfilComercial = enrol.POR_IdPerfilComercial;
		enrola.EMP_CodigoEmpleado = enrol.EMP_CodigoEmpleado;
		enrola.EMP_CodigoTipoCargoEmpleado = enrol.EMP_CodigoTipoCargoEmpleado;
		enrola.EMP_IdEmpleadoSupervisor = enrol.EMP_IdEmpleadoSupervisor;
		enrola.EMP_IdSucursal = enrol.EMP_IdSucursal;
		enrola.EMP_FechaInicioCargo = Fecha.toDate(enrol.EMP_FechaInicioCargo);
		enrola.EMP_FechaTerminoCargo = Fecha.toDate(enrol.EMP_FechaTerminoCargo);
		enrola.CNT_CodigoInscripcion = enrol.CNT_CodigoInscripcion;
		enrola.CNT_Emisor = enrol.CNT_Emisor;
		enrola.CNT_Depositos = enrol.CNT_Depositos;
		enrola.CNT_Bolsa = enrol.CNT_Bolsa;
		enrola.CNT_Corredora = enrol.CNT_Corredora;
		enrola.CNT_CamaraDeCompensacion = enrol.CNT_CamaraDeCompensacion;
		enrola.CNT_Banco = enrol.CNT_Banco;
		enrola.CNT_Boveda = enrol.CNT_Boveda;
		enrola.CNT_Otros = enrol.CNT_Otros;
		enrola.PRO_IdAreaProveedor = enrol.PRO_IdAreaProveedor;
		enrola.EMP_CargoIndefinido = enrol.EMP_CargoIndefinido;
		enrola.TipoEnrolamiento = enrol.TipoEnrolamiento;
		return enrola;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_ConsultarEnrolamiento\":[{\"IdEnrolamiento\":0,\"IdEmpresa\":1,\"IdUsuario\":21,\"IdTipoEntidad\":\"CLIENTE\",\"IdEntidadEnrolada\":0,\"IdTipoDocumentoIdentidad\":\"R\",\"IdTipoDocumentoIdentidadAsociado\":\"R\",\"DocumentoIdentidad\":\"16210559-0\",\"IdTipoDocumentoIdentidadAdicional\":\"R\",\"DocumentoIdentidadAdicional\":\"\",\"Tipo\":\"N\",\"Nombres\":\"ASDF\",\"ApellidoPaterno\":\"ASD\",\"ApellidoMaterno\":\"ASD\",\"RazonSocial\":\"\",\"IdPaisNacimiento\":44,\"FechaNacimiento\":\"2020-06-02\",\"Sexo\":\"M\",\"EstadoCivil\":\"SO\",\"IdCiudad\":788,\"IdDivisionPais\":344,\"IdTipoTelefono\":\"OTRO\",\"Telefono\":\"+56912345678\",\"Email\":\"ASDF@ASDF.CL\",\"DireccionCalle\":\"ASD\",\"DireccionNumero\":\"123\",\"DireccionDepartamento\":\"123\",\"FechaCreacion\":\"2021-01-07T14:32:33.116Z\",\"FechaModificacion\":\"2021-01-07T14:32:33.116Z\",\"IdPersonaConyuge\":0,\"RegimenConyugal\":\"\",\"CLI_IdCategoriaInversionista\":\"ARRIESGADO\",\"CLI_IdTipoCliente\":\"PRUEBA_1\",\"CLI_IdTipoClienteCMF\":805,\"CLI_IdClaseCliente\":\"\",\"CLI_IdEmpleadoEjecutivo\":11496,\"CLI_CodigoAdicionalHolding\":\"\",\"POR_IdPerfilComercial\":\"ASDAWDAWDOP\",\"EMP_CodigoEmpleado\":\"\",\"EMP_CodigoTipoCargoEmpleado\":\"\",\"EMP_IdEmpleadoSupervisor\":0,\"EMP_IdSucursal\":0,\"EMP_FechaInicioCargo\":\"0001-01-01\",\"EMP_FechaTerminoCargo\":\"0001-01-01\",\"CNT_CodigoInscripcion\":\"\",\"CNT_Emisor\":\"\",\"CNT_Depositos\":\"\",\"CNT_Bolsa\":\"\",\"CNT_Corredora\":\"\",\"CNT_CamaraDeCompensacion\":\"\",\"CNT_Banco\":\"\",\"CNT_Boveda\":\"\",\"CNT_Otros\":\"\",\"PRO_IdAreaProveedor\":0,\"EMP_CargoIndefinido\":\"\",\"TipoEnrolamiento\":\"FULL\"}],\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\"}";
			CrearBorradorClienteEntrada Objin = gson.fromJson(json, CrearBorradorClienteEntrada.class);
			
			CrearBorradorCliente demo=new CrearBorradorCliente();				
 			try {
 				CrearBorradorClienteSalida ObjOut=demo.logica(con, Objin);
 				con.commit();
 				//con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				con.rollback();
			}
	    }
		System.out.println("== END ==");
	}
}