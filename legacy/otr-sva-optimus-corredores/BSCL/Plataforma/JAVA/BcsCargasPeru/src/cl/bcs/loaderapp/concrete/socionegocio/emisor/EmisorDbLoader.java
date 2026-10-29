package cl.bcs.loaderapp.concrete.socionegocio.emisor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.concrete.socionegocio.banco.BancoDbLoader.ContraparteExistente;
import cl.bcs.loaderapp.interfaces.DbLoader;
import cl.bcs.loaderapp.pojo.socionegocio.Emisor;
import cl.bcs.loaderapp.utils.LoaderUtils;
import cl.bcs.plataforma.pla.utils.IdentificarLlaves;
import cl.bcs.plataforma.pla.utils.IdentificarLlavesBuilder;
import cl.bcs.socionegocio.cnt.CrearContraparte;
import cl.bcs.socionegocio.cnt.CrearContraparte.EntradaServicioCrearContraparte;
import cl.bcs.socionegocio.cnt.CrearContraparte.SalidaServicioCrearContraparte;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.socionegocio.soc.ValidarDocumentoIdentidad;
import cl.bcs.socionegocio.soc.ValidarDocumentoIdentidad.SalidaValidarDocumentoIdentidad;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EmisorDbLoader implements DbLoader	{
	
	public String SQL_INGRESAR_EMISOR = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/emisor/Emisor_IngresarEmisor.sql");
	
	public String SQL_CONTRAPARTE_EXISTE = "SELECT \"IdPersona\", \"IdContraparte\" FROM \"BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte\" WHERE \"DocumentoIdentidad\" = :DocumentoIdentidad AND \"Emisor\" = 'N';";
	public String SQL_UPDATE_CONTRAPARTE = "UPDATE \"BSCL.SocioNegocio.BD::CNT.Contraparte\" SET \"Emisor\" = 'S' WHERE \"IdContraparte\" = :IdContraparte;";
	
	public static class EntidadContraparte{
		public Long IdContraparte ;
		public Long IdPersona ;
		public Long IdEmpresa ;
		public String IdEstado ;
		public String CodigoInscripcion;
		public Long IdUsuarioCreador;		
	}
	
	public static class EntidadEmisor{
		public String CodigoEmisor;
		public String CodigoRegulador;
		public String Regulador;
		public String EmiteIRF;
		public Long IdPersona;
		public String IdEmisor;
	}
	
	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral entrada, List<Object> lines) {
		List<String> salida = new ArrayList<String>();
		
		Long idUsuarioDefecto = entrada.p_IdUsuario;
        Long idEmpresaDefecto = entrada.p_IdEmpresa;
		try (Connection con = jsJB.getConnection()){
			IdentificarLlaves llaves = new IdentificarLlavesBuilder(con)
					.setCiudadesUbigeo()
					.setPaises()
					.build();
			String fechaSistema = Utils.getFechaSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
			
			List<Emisor> emisoresCSV = (List<Emisor>) (Object) lines;
			//for(Emisor emisor : emisoresCSV){
			emisoresCSV.parallelStream().forEach((emisor) -> {
				SQL sql = new SQL();
				int linea = emisor.getLinea();
				String mensaje = "";
				LoaderUtils loaderUtil = new LoaderUtils();
				try(Connection conn  = jsJB.getConnection()){
					try {
						Long idPersona = null;
						ValidarDocumentoIdentidad validarDocumentoIdentidad = new ValidarDocumentoIdentidad();
						SalidaValidarDocumentoIdentidad documentoValido = validarDocumentoIdentidad.logica(conn, idEmpresaDefecto, "CONTRAPARTE", emisor.TipoDocumentoIdentidad, emisor.DocumentoIdentidad, emisor.TipoPersona);
						if (documentoValido.Valido.equals("S")) {
							
							//Crear Persona
							CrearContraparte crearContraparte = new CrearContraparte();
							EntradaServicioCrearContraparte entradaCrearContraparte = new EntradaServicioCrearContraparte();
							entradaCrearContraparte.p_IdEmpresa = idEmpresaDefecto;
							entradaCrearContraparte.p_IdModulo = entrada.p_IdModulo;
							entradaCrearContraparte.p_IdUsuario = idUsuarioDefecto;
							
							Enrolamiento enr = new Enrolamiento();
							enr.IdTipoEntidad = "CONTRAPARTE";
							enr.Tipo = emisor.TipoPersona;
							if(enr.Tipo==null) {
								throw new Exception("TipoPersona es obligatorio");
							}
							enr.IdTipoDocumentoIdentidad = emisor.TipoDocumentoIdentidad;
							enr.DocumentoIdentidad = emisor.DocumentoIdentidad;
							if(enr.DocumentoIdentidad==null) {
								throw new Exception("DocumentoIdentidad es obligatorio");
							}
							enr.IdTipoDocumentoIdentidadAsociado = emisor.TipoDocumentoIdentidadAsociado;
							enr.RazonSocial = emisor.RazonSocial;
							enr.IdPaisNacimiento = llaves.getPais(emisor.Pais);
							if(enr.IdPaisNacimiento==null) {
								throw new Exception("Pais " + emisor.Pais + ", no Existe");
							}
							enr.IdCiudad = llaves.getCiudadUbigeo(emisor.Ciudad);
							if(enr.IdCiudad==null) {
								throw new Exception("Ciudad " + emisor.Ciudad + ", no Existe");
							}
							//obligatorios
							enr.IdEmpresa = idEmpresaDefecto;
							enr.IdUsuario = idUsuarioDefecto;
							enr.IdEntidadEnrolada = (long) 0;
							enr.IdTipoDocumentoIdentidadAdicional = "";//?? //son solo para clientes
							enr.DocumentoIdentidadAdicional = "";//??
							enr.Nombres = "";
							enr.ApellidoMaterno="";
							enr.ApellidoPaterno="";
							enr.Sexo ="";
							enr.EstadoCivil = "";
							enr.IdTipoTelefono ="";
							enr.Telefono = "";
							enr.Email ="";
							enr.DireccionCalle = "";
							enr.DireccionNumero = "";
							enr.DireccionDepartamento = "";
							enr.CLI_IdCategoriaInversionista = "";
							enr.POR_IdPerfilComercial ="";
							enr.CLI_IdTipoCliente ="";
							enr.CLI_CodigoAdicionalHolding ="";
							enr.CLI_IdClaseCliente = "";
							enr.IdCarga = entrada.IdCarga;
							
							//datos emisor
							enr.CNT_Bolsa = "N";
							enr.CNT_Boveda = "N";
							enr.CNT_CamaraDeCompensacion = "N";
							enr.CNT_Corredora = "N";
							enr.CNT_Depositos = "N";
							enr.CNT_Emisor ="S";
							enr.CNT_Otros="";
							enr.CNT_Banco="N";
							enr.CNT_CodigoInscripcion = "";
							
							entradaCrearContraparte.p_ConsultarEnrolamiento = Arrays.asList(enr);
							SalidaServicioCrearContraparte salidaCrearContraparte= crearContraparte.logica(conn, entradaCrearContraparte);
							//obtener advertencias de la creacion de persona
							for(AdvertenciaDetalle advertencia : salidaCrearContraparte.P_OUT_ADVERTENCIASDETALLE){
								System.out.println(advertencia.Descripcion);
							}
							if(salidaCrearContraparte.P_OUT_ERRORESCONTADOR==0 || salidaCrearContraparte.P_OUT_REFIDCONTRAPARTE != 0 ) {
								idPersona = salidaCrearContraparte.P_OUT_REFIDPERSONA;
							}else {
								for(ErrorDetalle sd : salidaCrearContraparte.P_OUT_ERRORESDETALLE) {
									mensaje = (linea + " - CONTRAPARTE " + emisor.DocumentoIdentidad + " ERROR: " + sd.Descripcion);
								}
							}		
							
						}else{
							HashMap<String, Object> params = new HashMap<>();
							params.put("DocumentoIdentidad", emisor.DocumentoIdentidad);
							List<ContraparteExistente> contrapartes = sql.query(conn, SQL_CONTRAPARTE_EXISTE, params, ContraparteExistente.class);
							
							if(contrapartes.isEmpty()) {
								throw new Exception(linea + " - EMISOR " + emisor.DocumentoIdentidad + " YA EXISTE");
								
							}else {
								idPersona =contrapartes.get(0).IdPersona;
								sql.batch(conn, SQL_UPDATE_CONTRAPARTE, contrapartes, ContraparteExistente.class);
							}
						}
						
						if(idPersona!=null) {
							//crear emisor
							EntidadEmisor emi = new EntidadEmisor();
							emi.CodigoEmisor = emisor.CodigoEmisor;
							emi.CodigoRegulador = emisor.CodigoRegulador;
							emi.EmiteIRF= emisor.EmiteIRF;
							emi.IdPersona = idPersona;
							emi.IdEmisor = emisor.IdEmisor;
							if(emi.IdEmisor==null) {
								throw new Exception("IdEmisor es obligatorio");
							}
							
							int[] resultado = sql.batch(conn, SQL_INGRESAR_EMISOR, Arrays.asList(emi), EntidadEmisor.class);
							if(resultado!=null && resultado[0]==1){
								mensaje = (linea + " - EMISOR " + emisor.DocumentoIdentidad + " INGRESADO CORRECTAMENTE");
								conn.commit();
							}else{
								mensaje = (linea + " - EMISOR " + emisor.DocumentoIdentidad + " NO FUE INGRESADO");
							}
						}else {
							mensaje = (linea + " - emisor " + emisor.DocumentoIdentidad + " NO SE PUDO CREAR PERSONA");
						}
						
					}catch(Exception ex) {
						mensaje = (linea + " - EMISOR " + emisor.DocumentoIdentidad +" "+ ex.getMessage());
						ex.printStackTrace();
					}
					conn.rollback();
					loaderUtil.log(conn, fechaSistema, "PE", "EMISOR", emisor.Linea, mensaje, (mensaje.contains("CORRECTAMENTE") ? "OK" : "NOK"), emisor.toString());
					conn.commit();
				} catch (SQLException e) {
					mensaje = ("ERROR AL OBTENER CONEXION");	
				}
				salida.add(mensaje);
			});
		} catch (Exception e) {
			salida.add(e.getMessage());
			e.printStackTrace();
		}
		
		
		return salida;
	}

}
