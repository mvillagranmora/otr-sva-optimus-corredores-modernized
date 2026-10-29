package cl.bcs.loaderapp.concrete.socionegocio.banco;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.dbcp.BasicDataSource;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.DbLoader;
import cl.bcs.plataforma.pla.utils.IdentificarLlaves;
import cl.bcs.plataforma.pla.utils.IdentificarLlavesBuilder;
import cl.bcs.socionegocio.cnt.CrearContraparte;
import cl.bcs.socionegocio.cnt.CrearContraparte.EntradaServicioCrearContraparte;
import cl.bcs.socionegocio.cnt.CrearContraparte.SalidaServicioCrearContraparte;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.socionegocio.soc.ValidarDocumentoIdentidad;
import cl.bcs.socionegocio.soc.ValidarDocumentoIdentidad.SalidaValidarDocumentoIdentidad;
import cl.bcs.socionegocio.utils.IUP;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class BancoDbLoader implements DbLoader{
	public String SQL_CREAR_BANCO = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/banco/CrearBanco_Insert.sql");
	public String SQL_CONTRAPARTE_EXISTE = "SELECT \"IdPersona\", \"IdContraparte\" FROM \"BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte\" WHERE \"DocumentoIdentidad\" = :DocumentoIdentidad AND \"Banco\" = 'N';";
	public String SQL_UPDATE_CONTRAPARTE = "UPDATE \"BSCL.SocioNegocio.BD::CNT.Contraparte\" SET \"Banco\" = 'S' WHERE \"IdContraparte\" = :IdContraparte;";
	
	public static class ContraparteExistente{
		public Long IdPersona;
		public Long IdContraparte;
	}
	
	public class Banco{
		public String IDESTADO;
		public String IDPAISOPERACION;
		public String IDTIPOBANCO;
		public String CODIGOSWIFT;
		public String TIPOPERSONA;
		public String TIPODOCUMENTOIDENTIDAD;
		public String DOCUMENTOIDENTIDAD;
		public String RAZONSOCIAL;
		public String CIUDAD;
		public String IDEMPRESA;
		public int LINEA;
		public String TIPODOCUMENTOIDENTIDADASOCIADO;
		public String DOCUMENTOIDENTIDADASOCIADO;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class EntidadBanco{
		public Long IdBanco;
		public String NombreBanco;
		public String CodigoBanco;
		public Long IdPersona;
		public Long IdPaisOperacion;
		public String IdTipoBanco;
		public Long IdEmpresa;
		public String IdEstado;
	}
	
	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral in, List<Object> lines) {
		List<String> salida = new ArrayList<String>();
		Long idUsuarioDefecto = in.p_IdUsuario;
        Long idEmpresaDefecto = in.p_IdEmpresa;
		SQL sql = new SQL();
		try (Connection con = jsJB.getConnection()){
			ValidarDocumentoIdentidad validarDocumentoIdentidad = new ValidarDocumentoIdentidad();
			IdentificarLlaves llaves = new IdentificarLlavesBuilder(con)
					.setCiudadesUbigeo()
					.setPaises()
					.build();
			long idCarga = in.IdCarga;
			
			List<CSVRecord> recordsCSV = (List<CSVRecord>) (Object) lines;
			List<Banco> bancos = new ArrayList<Banco>();
			int inlinea = 0;
			for (CSVRecord csvRecord : recordsCSV) {
				Banco banco = new Banco();
				banco.IDESTADO = csvRecord.get("IDESTADO");
				banco.IDPAISOPERACION = csvRecord.get("IDPAISOPERACION");
				banco.IDTIPOBANCO = csvRecord.get("IDTIPOBANCO");
				banco.CODIGOSWIFT = csvRecord.get("CODIGOSWIFT");
				banco.TIPOPERSONA = csvRecord.get("TIPOPERSONA");
				banco.TIPODOCUMENTOIDENTIDADASOCIADO = csvRecord.get("TIPODOCUMENTOIDENTIDAD");
				banco.DOCUMENTOIDENTIDADASOCIADO = csvRecord.get("DOCUMENTOIDENTIDAD");
				banco.TIPODOCUMENTOIDENTIDAD = "IUP";
				banco.DOCUMENTOIDENTIDAD = IUP.generar(csvRecord.get("DOCUMENTOIDENTIDAD"), csvRecord.get("TIPODOCUMENTOIDENTIDAD"),csvRecord.get("TIPOPERSONA"));
				banco.RAZONSOCIAL = csvRecord.get("RAZONSOCIAL");
				banco.CIUDAD = csvRecord.get("CIUDAD");
				banco.IDEMPRESA = csvRecord.get("IDEMPRESA");
				banco.LINEA = inlinea++;
				bancos.add(banco);
			}
			
//			bancos.parallelStream().forEach((banco) -> {
			bancos.forEach((banco) -> {
				String mensaje = "";
				int linea = banco.LINEA;
				try (Connection conn = jsJB.getConnection()){
					try {
						Long idPersona = null;
						SalidaValidarDocumentoIdentidad documentoValido = validarDocumentoIdentidad.logica(conn, idEmpresaDefecto, "CONTRAPARTE", banco.TIPODOCUMENTOIDENTIDAD, banco.DOCUMENTOIDENTIDAD, banco.TIPOPERSONA);
						if (documentoValido.Valido.equals("S")) {
							
							//Crear Persona
							CrearContraparte crearContraparte = new CrearContraparte();
							EntradaServicioCrearContraparte entradaCrearContraparte = new EntradaServicioCrearContraparte();
							entradaCrearContraparte.p_IdEmpresa = idEmpresaDefecto;
							entradaCrearContraparte.p_IdModulo = in.p_IdModulo;
							entradaCrearContraparte.p_IdUsuario = idUsuarioDefecto;
							
							Enrolamiento enr = new Enrolamiento();
							enr.IdTipoEntidad = "CONTRAPARTE";
							enr.Tipo = banco.TIPOPERSONA;
							if(enr.Tipo==null) {
								throw new Exception("TipoPersona es obligatorio");
							}
							enr.IdTipoDocumentoIdentidad = banco.TIPODOCUMENTOIDENTIDAD;
							enr.DocumentoIdentidad = banco.DOCUMENTOIDENTIDAD;
							if(enr.DocumentoIdentidad==null) {
								throw new Exception("DocumentoIdentidad es obligatorio");
							}
							enr.IdTipoDocumentoIdentidadAsociado = banco.TIPODOCUMENTOIDENTIDADASOCIADO;
							enr.RazonSocial = banco.RAZONSOCIAL;
							enr.IdPaisNacimiento = llaves.getPais(banco.IDPAISOPERACION);
							if(enr.IdPaisNacimiento==null) {
								throw new Exception("Pais " + banco.IDPAISOPERACION + ", no Existe");
							}
							enr.IdCiudad = llaves.getCiudadUbigeo(banco.CIUDAD);
							if(enr.IdCiudad==null) {
								throw new Exception("Ciudad " + banco.CIUDAD + ", no Existe");
							}
							enr.CNT_Bolsa = "N";
							enr.CNT_Boveda = "N";
							enr.CNT_CamaraDeCompensacion = "N";
							enr.CNT_Corredora = "N";
							enr.CNT_Depositos = "N";
							enr.CNT_Emisor ="N";
							enr.CNT_Otros="";
							
							enr.CNT_Banco="S";
							enr.CNT_CodigoInscripcion = (banco.CODIGOSWIFT!=null) ? banco.CODIGOSWIFT : "";
							
							//obligatorios
							enr.IdEmpresa = idEmpresaDefecto;
							enr.IdUsuario = idUsuarioDefecto;
							enr.IdEntidadEnrolada = (long) 0;
							enr.IdTipoDocumentoIdentidadAdicional = "";
							enr.DocumentoIdentidadAdicional = "";
							enr.Nombres = "";
							enr.ApellidoMaterno="";
							enr.ApellidoPaterno="";
							enr.Sexo ="";
							enr.EstadoCivil = "";
							enr.IdTipoTelefono ="";
							enr.Telefono = "";
							enr.Email = "";
							enr.DireccionCalle = "";
							enr.DireccionNumero = "";
							enr.DireccionDepartamento = "";
							enr.CLI_IdCategoriaInversionista = "";
							enr.POR_IdPerfilComercial ="";
							enr.CLI_IdTipoCliente ="";
							enr.CLI_CodigoAdicionalHolding ="";
							enr.CLI_IdClaseCliente = "";
							enr.IdCarga = idCarga;
							
							entradaCrearContraparte.p_ConsultarEnrolamiento = Arrays.asList(enr);
							SalidaServicioCrearContraparte salidaCrearContraparte = crearContraparte.logica(conn, entradaCrearContraparte);
							//obtener advertencias de la creacion de contraparte
							for(AdvertenciaDetalle advertencia : salidaCrearContraparte.P_OUT_ADVERTENCIASDETALLE){
								System.out.println(advertencia.Descripcion);
							}
							if(salidaCrearContraparte.P_OUT_ERRORESCONTADOR==0 || salidaCrearContraparte.P_OUT_REFIDCONTRAPARTE != 0 ){
								idPersona = salidaCrearContraparte.P_OUT_REFIDPERSONA;
							}
						}else{
							HashMap<String, Object> params = new HashMap<>();
							params.put("DocumentoIdentidad", banco.DOCUMENTOIDENTIDAD);
							List<ContraparteExistente> contrapartes = sql.query(conn, SQL_CONTRAPARTE_EXISTE, params, ContraparteExistente.class);
							
							if(contrapartes.isEmpty()) {
								throw new Exception(linea + " - BANCO " + banco.DOCUMENTOIDENTIDADASOCIADO + " YA EXISTE");
								
							}else {
								idPersona =contrapartes.get(0).IdPersona;
								sql.batch(conn, SQL_UPDATE_CONTRAPARTE, contrapartes, ContraparteExistente.class);
							}
						}	
						
						if(idPersona!=null){									
							long idBanco = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::BAN.Banco", (long) 1).get(0);
							
							EntidadBanco enBanco = new EntidadBanco();
							enBanco.CodigoBanco = banco.CODIGOSWIFT;
							enBanco.IdBanco = idBanco;
							enBanco.IdEmpresa = (banco.IDEMPRESA!=null && !banco.IDEMPRESA.equals("NULL")&& !banco.IDEMPRESA.equals(""))? Long.parseLong(banco.IDEMPRESA) : idEmpresaDefecto;
							enBanco.IdEstado = banco.IDESTADO;
							enBanco.IdPaisOperacion = llaves.getPais(banco.IDPAISOPERACION);
							enBanco.IdPersona = idPersona;
							enBanco.IdTipoBanco = banco.IDTIPOBANCO;
							enBanco.NombreBanco = banco.RAZONSOCIAL;
							
							sql.batch(conn, SQL_CREAR_BANCO, Arrays.asList(enBanco), EntidadBanco.class);
															
							mensaje = (linea + " - BANCO " + banco.DOCUMENTOIDENTIDADASOCIADO + " INGRESADO CORRECTAMENTE");
							conn.commit();
						}else {
							mensaje = (linea + " - BANCO " + banco.DOCUMENTOIDENTIDADASOCIADO + " NO SE PUDO CREAR PERSONA");
						}
					}catch(Exception ex) {
						mensaje = (linea + " - BANCO " + banco.DOCUMENTOIDENTIDADASOCIADO +" "+ ex.getMessage());
					}
					conn.rollback();
				} catch (SQLException e) {
					mensaje = (linea + " - BANCO " + banco.DOCUMENTOIDENTIDADASOCIADO +" "+ e.getMessage());
				}
				System.out.println(mensaje);
				salida.add(mensaje);
			});
		} catch (Exception e) {
			salida.add(e.getMessage());
			e.printStackTrace();
		}
		
		return salida;
	}

}
