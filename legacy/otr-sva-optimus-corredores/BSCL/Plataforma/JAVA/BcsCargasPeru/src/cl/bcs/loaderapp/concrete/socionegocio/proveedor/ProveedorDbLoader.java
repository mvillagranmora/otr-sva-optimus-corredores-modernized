package cl.bcs.loaderapp.concrete.socionegocio.proveedor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.DbLoader;
import cl.bcs.loaderapp.pojo.socionegocio.Proveedor;
import cl.bcs.loaderapp.utils.LoaderUtils;
import cl.bcs.plataforma.pla.utils.IdentificarLlaves;
import cl.bcs.plataforma.pla.utils.IdentificarLlavesBuilder;
import cl.bcs.socionegocio.per.CrearPersona;
import cl.bcs.socionegocio.per.CrearPersona.EntradaServicioCrearPersona;
import cl.bcs.socionegocio.per.CrearPersona.SalidaServicioCrearPersona;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.socionegocio.soc.ValidarDocumentoIdentidad;
import cl.bcs.socionegocio.soc.ValidarDocumentoIdentidad.SalidaValidarDocumentoIdentidad;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProveedorDbLoader implements DbLoader{
	public String SQL_CREAR_PROVEEDOR = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/proveedor/CrearProveedor_INSERT.sql");
	public String SQL_CREAR_PROVEEDOR_SOCIONEGOCIO_UPDATE = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/proveedor/CrearProveedor_SocioNegocio_Update.sql");
	public String SQL_CREAR_PROVEEDOR_ENROLAMIENTO_UPDATE = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/proveedor/CrearProveedor_Enrolamiento_Update.sql");
	
	public class EntidadProveedor{
		public Long IdProveedor;
		public Long IdPersona;
		public String IdEstado;
		public Long PRO_IdAreaProveedor;
		public Long IdUsuario;
		public Object IdEmpresa;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public class EntidadCrearProveedorUpdateSocioNegocio{

		public Long IdSocioDeNegocio;
		public Long IdProveedor;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class EntidadCrearProveedorUpdateEnrolamiento{

		public Long IdProveedor;
		public Long IdEnrolamiento;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}	
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
					.setCiudades()
					.setCiudadesUbigeo()
					.setPaises()
					.build();
			String fechaSistema = Utils.getFechaSistema(con, Utils.getSchema(), in.p_IdEmpresa);
			long idCarga = in.IdCarga;
			
			List<Proveedor> proveedoresCSV = (List<Proveedor>) (Object) lines;
			
			proveedoresCSV.parallelStream().forEach((proveedor) -> {
				LoaderUtils loaderUtils = new LoaderUtils();
				int linea = proveedor.Linea;
				String mensaje = "";
				try (Connection conn = jsJB.getConnection()){
					try {	
						SalidaValidarDocumentoIdentidad documentoValido = validarDocumentoIdentidad.logica(conn, idEmpresaDefecto, "PROVEEDOR", proveedor.TIPODOCUMENTOIDENTIDAD, proveedor.DOCUMENTOIDENTIDAD, proveedor.TIPOPERSONA);
						if (documentoValido.Valido.equals("S")) {
							
							//Crear Persona
							CrearPersona crearPersona = new CrearPersona();
							EntradaServicioCrearPersona entradaCrearPersona = new EntradaServicioCrearPersona();
							entradaCrearPersona.p_IdEmpresa = idEmpresaDefecto;
							entradaCrearPersona.p_IdModulo = in.p_IdModulo;
							entradaCrearPersona.p_IdUsuario = idUsuarioDefecto;
							
							Enrolamiento enr = new Enrolamiento();
							enr.IdTipoEntidad = "PROVEEDOR";
							enr.Tipo = proveedor.TIPOPERSONA;
							if(enr.Tipo==null) {
								throw new Exception("TipoPersona es obligatorio");
							}
							enr.IdTipoDocumentoIdentidad = proveedor.TIPODOCUMENTOIDENTIDAD;
							enr.DocumentoIdentidad = proveedor.DOCUMENTOIDENTIDAD;
							if(enr.DocumentoIdentidad==null) {
								throw new Exception("DocumentoIdentidad es obligatorio");
							}
							enr.IdTipoDocumentoIdentidadAsociado = proveedor.TIPODOCUMENTOIDENTIDADASOCIADO;
							enr.RazonSocial = (proveedor.RAZONSOCIAL!=null) ? proveedor.RAZONSOCIAL : "";
							enr.IdPaisNacimiento = llaves.getPais(proveedor.PAISNACIMIENTOCONSTITUCION);
							if(enr.IdPaisNacimiento==null) {
								throw new Exception("Pais " + proveedor.PAISNACIMIENTOCONSTITUCION + ", no Existe");
							}
							enr.IdCiudad = llaves.getCiudadUbigeo(proveedor.CIUDAD);
							if(enr.IdCiudad==null) {
								throw new Exception("Ciudad " + proveedor.CIUDAD + ", no Existe");
							}
							enr.PRO_IdAreaProveedor = Long.parseLong((proveedor.AREAPROVEEDOR!=null && !proveedor.AREAPROVEEDOR.equals("")) ? proveedor.AREAPROVEEDOR : "0");
							
							//obligatorios
							enr.IdEmpresa = idEmpresaDefecto;
							enr.IdUsuario = idUsuarioDefecto;
							enr.IdEntidadEnrolada = (long) 0;
							enr.IdTipoDocumentoIdentidadAdicional = "";
							enr.DocumentoIdentidadAdicional = "";
							enr.Nombres = (proveedor.NOMBRES!=null) ? proveedor.NOMBRES : "";
							enr.ApellidoMaterno= (proveedor.APELLIDOMATERNO!=null) ? proveedor.APELLIDOMATERNO : "";
							enr.ApellidoPaterno= (proveedor.APELLIDOPATERNO!=null) ? proveedor.APELLIDOPATERNO : "";
							enr.Sexo = "";
							enr.EstadoCivil = "";
							enr.IdTipoTelefono ="";
							enr.Telefono = (proveedor.TELEFONO!=null)?proveedor.TELEFONO:"";
							enr.Email = (proveedor.EMAIL!=null)?proveedor.EMAIL:"";
							enr.DireccionCalle = (proveedor.CALLE!=null)?proveedor.CALLE:"";
							enr.DireccionNumero = (proveedor.NUMERO!=null)?proveedor.NUMERO:"";
							enr.DireccionDepartamento = (proveedor.COMPLEMENTO!=null)?proveedor.COMPLEMENTO:"";
							enr.CLI_IdCategoriaInversionista = "";
							enr.POR_IdPerfilComercial ="";
							enr.CLI_IdTipoCliente ="";
							enr.CLI_CodigoAdicionalHolding ="";
							enr.CLI_IdClaseCliente = "";
							enr.IdCarga = idCarga;
							
							entradaCrearPersona.p_ConsultarEnrolamiento = Arrays.asList(enr);
							SalidaServicioCrearPersona salidaCrearPersona = crearPersona.logica(conn, entradaCrearPersona);
							//obtener advertencias de la creacion de persona
							for(AdvertenciaDetalle advertencia : salidaCrearPersona.P_OUT_ADVERTENCIASDETALLE){
								System.out.println(advertencia.Descripcion);
							}
							
							if(salidaCrearPersona.P_OUT_ERRORESCONTADOR==0 || salidaCrearPersona.P_OUT_REFID != 0 ){
								long idPersona = salidaCrearPersona.P_OUT_REFID;
								long idSocioNegocio = salidaCrearPersona.P_OUT_IDSOCIONEGOCIO;												
								long idProveedor = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::Proveedor", (long) 1).get(0);
								
								EntidadProveedor pro = new EntidadProveedor();
								pro.IdPersona = idPersona;
								pro.IdEstado ="ACT";
								pro.IdEmpresa = idEmpresaDefecto;
								pro.IdProveedor = idProveedor;
								pro.IdUsuario = idUsuarioDefecto;
								pro.PRO_IdAreaProveedor = enr.PRO_IdAreaProveedor;
								
								int[] resproveedor = sql.batch(conn, SQL_CREAR_PROVEEDOR, Arrays.asList(pro), EntidadProveedor.class);
								if(resproveedor!=null && resproveedor.length>0 && resproveedor[0]==1){
									
									//Actualizar socio de negocio con IdContraparte
									EntidadCrearProveedorUpdateSocioNegocio updateSocioNegocio = new EntidadCrearProveedorUpdateSocioNegocio();
									updateSocioNegocio.IdSocioDeNegocio = idSocioNegocio;
									updateSocioNegocio.IdProveedor = idProveedor;
									int[] resUpdateSocioNegocio = sql.batch(conn, SQL_CREAR_PROVEEDOR_SOCIONEGOCIO_UPDATE, Arrays.asList(updateSocioNegocio), EntidadCrearProveedorUpdateSocioNegocio.class);									
									if(resUpdateSocioNegocio==null || (resUpdateSocioNegocio.length>0 && resUpdateSocioNegocio[0]==0)){
										throw new Exception("ERROR_ACTUALIZAR_SOCIONEGOCIO");
									}
									
									//Actualizar enrolamiento con IdEntidadEnrolada 
									EntidadCrearProveedorUpdateEnrolamiento updateEnrolamiento = new EntidadCrearProveedorUpdateEnrolamiento();
									updateEnrolamiento.IdProveedor = idProveedor;
									updateEnrolamiento.IdEnrolamiento = salidaCrearPersona.P_OUT_IDENROLAMIENTO;
									int[] resUpdateEnrolamiento = sql.batch(conn, SQL_CREAR_PROVEEDOR_ENROLAMIENTO_UPDATE, Arrays.asList(updateEnrolamiento), EntidadCrearProveedorUpdateEnrolamiento.class);									
									if(resUpdateEnrolamiento==null || (resUpdateEnrolamiento.length>0 && resUpdateEnrolamiento[0]==0)){
										throw new Exception("ERROR_ACTUALIZAR_ENROLAMIENTO");
									}
									
									mensaje = (linea + " - PROVEEDOR " + proveedor.DOCUMENTOIDENTIDADASOCIADO + " INGRESADO CORRECTAMENTE");
									conn.commit();
								}else{
									mensaje = ("PROVEEDOR NO PUEDE SER INGRESADO");
								}
							}else {
								mensaje = (linea + " - PROVEEDOR " + proveedor.DOCUMENTOIDENTIDADASOCIADO + " ERROR AL CREAR U OPBTENER PERSONA");
							}
						}else{
							mensaje = (linea + " - PROVEEDOR " + proveedor.DOCUMENTOIDENTIDADASOCIADO + " YA EXISTE");
							conn.rollback();
						}
						conn.rollback();
					}catch(Exception ex) {
						mensaje = (linea + " - PROVEEDOR " + proveedor.DOCUMENTOIDENTIDADASOCIADO +" "+ ex.getMessage());
						ex.printStackTrace();
						conn.rollback();
					}
				} catch (SQLException e) {
					mensaje = (linea + " - PROVEEDOR " + proveedor.DOCUMENTOIDENTIDADASOCIADO +" "+ e.getMessage());
				}
				salida.add(mensaje);
				loaderUtils.log(con, fechaSistema, "PE", "PROVEEDOR", proveedor.Linea, mensaje, (mensaje.contains("CORRECTAMENTE")?"OK":"NOK"), proveedor.toString());
			});
		} catch (Exception e) {
			salida.add(e.getMessage());
			e.printStackTrace();
		}
		
		return salida;
	}

}
