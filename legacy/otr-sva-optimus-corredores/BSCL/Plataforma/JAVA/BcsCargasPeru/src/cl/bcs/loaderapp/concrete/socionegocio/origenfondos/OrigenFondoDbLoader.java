package cl.bcs.loaderapp.concrete.socionegocio.origenfondos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.DbLoader;
import cl.bcs.loaderapp.pojo.socionegocio.OrigenFondo;
import cl.bcs.loaderapp.servicios.ConsultarDocumentoIdentidadCliente;
import cl.bcs.loaderapp.servicios.ConsultarDocumentoIdentidadCliente.SalidaSocioNegocio;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class OrigenFondoDbLoader implements DbLoader {
	public String SQL_INGRESAR_ORIGEN_FONDO = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/origenfondo/OrigenFondo_IngresarOrigen.sql");
	public String SQL_INGRESAR_DETALLE_ORIGEN_FONDO = loadResourceToString("cl/bcs/loaderapp/sql/socionegocio/origenfondo/OrigenFondo_IngresarDetalleOrigen.sql");
	
	public static class EntidadOrigenFondo{
		public Long IdDeclaracionOrigenFondos;
		public Long IdCliente;
		public String IdTipoOperacionOrigenFondos;
		public String FechaDeclaracion;
		public String Comentario;
		public Long IdUsuarioCreador;
		public Long IdEmpresa;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntidadDetalleOrigenFondo{
		public String IdTipoOrigenFondo;
		public Long IdDeclaracionOrigenFondos;
		public BigDecimal Valor;
		public Long IdUsuarioCreador;
		public String Otros;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral entrada, List<Object> lines) {
		List<String> salida = new ArrayList<String>();
		Long idUsuarioDefecto = entrada.p_IdUsuario;
        Long idEmpresaDefecto = entrada.p_IdEmpresa;
		SQL sql = new SQL();
		
		int linea = 1;
		List<OrigenFondo> origenesFondoCSV = (List<OrigenFondo>) (Object) lines;
		try(Connection conn = jsJB.getConnection()){
			for(OrigenFondo origen : origenesFondoCSV){
				try {
					//Identificar IdCLiente mediante el documento de identidad.
					ConsultarDocumentoIdentidadCliente consDocCli = new ConsultarDocumentoIdentidadCliente();
					SalidaSocioNegocio idsSocioNegocio = consDocCli.logica(conn, origen.getDocumentoIdentidad(), origen.getTipoDocumentoIdentidad(), origen.getTipoPersona(), idEmpresaDefecto);
					if(idsSocioNegocio.IdCliente!=null) {
						//crear origen fondos
						Long IdDeclaracionOrigenFondos = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::DeclaracionOrigenFondos", (long) 1).get(0);
						
						EntidadOrigenFondo org = new EntidadOrigenFondo();
						org.IdDeclaracionOrigenFondos = IdDeclaracionOrigenFondos;
						org.IdCliente = idsSocioNegocio.IdCliente;
						org.IdTipoOperacionOrigenFondos = origen.IdTipoOperacionOrigenFondo;
						SimpleDateFormat parser = new SimpleDateFormat("dd-MM-yyyy");
				        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				        Date fecha = parser.parse(origen.FechaDeclaracion);
				        org.FechaDeclaracion = formatter.format(fecha);
				        
						org.Comentario = origen.Comentario;
						org.IdUsuarioCreador = idUsuarioDefecto;
						org.IdEmpresa = idEmpresaDefecto;
						
						int[] resultado = sql.batch(conn, SQL_INGRESAR_ORIGEN_FONDO, Arrays.asList(org), EntidadOrigenFondo.class);
					
						if(resultado[0]==1){
							//ingresar detalle origen fondo
							List<EntidadDetalleOrigenFondo> detalles = new ArrayList<EntidadDetalleOrigenFondo>();
							//remuneracion
							EntidadDetalleOrigenFondo detalleOrigen = new EntidadDetalleOrigenFondo();
							detalleOrigen.IdDeclaracionOrigenFondos = IdDeclaracionOrigenFondos;
							detalleOrigen.IdTipoOrigenFondo = "REM";
							detalleOrigen.Valor = new BigDecimal(origen.Remuneraciones);
							detalleOrigen.IdUsuarioCreador = idUsuarioDefecto;
							detalleOrigen.Otros = "";
							detalles.add(detalleOrigen);
							//ahorro
							detalleOrigen = new EntidadDetalleOrigenFondo();
							detalleOrigen.IdDeclaracionOrigenFondos = IdDeclaracionOrigenFondos;
							detalleOrigen.IdTipoOrigenFondo = "AHR";
							detalleOrigen.Valor = new BigDecimal(origen.Ahorros);
							detalleOrigen.IdUsuarioCreador = idUsuarioDefecto;
							detalleOrigen.Otros = "";
							detalles.add(detalleOrigen);
							//Desembolso
							detalleOrigen = new EntidadDetalleOrigenFondo();
							detalleOrigen.IdDeclaracionOrigenFondos = IdDeclaracionOrigenFondos;
							detalleOrigen.IdTipoOrigenFondo = "DES";
							detalleOrigen.Valor = new BigDecimal(origen.DesembolsoPrestamo);
							detalleOrigen.IdUsuarioCreador = idUsuarioDefecto;
							detalleOrigen.Otros = "";
							detalles.add(detalleOrigen);
							//Utilidad
							detalleOrigen = new EntidadDetalleOrigenFondo();
							detalleOrigen.IdDeclaracionOrigenFondos = IdDeclaracionOrigenFondos;
							detalleOrigen.IdTipoOrigenFondo = "UTI";
							detalleOrigen.Valor = new BigDecimal(origen.UtilidadesDividendosEmpresa);
							detalleOrigen.IdUsuarioCreador = idUsuarioDefecto;
							detalleOrigen.Otros = "";
							detalles.add(detalleOrigen);
							//Venta de mis acciones
							detalleOrigen = new EntidadDetalleOrigenFondo();
							detalleOrigen.IdDeclaracionOrigenFondos = IdDeclaracionOrigenFondos;
							detalleOrigen.IdTipoOrigenFondo = "VAC";
							detalleOrigen.Valor = new BigDecimal(origen.VentaMisAcciones);
							detalleOrigen.IdUsuarioCreador = idUsuarioDefecto;
							detalleOrigen.Otros = "";
							detalles.add(detalleOrigen);
							//Venta de un inmueble
							detalleOrigen = new EntidadDetalleOrigenFondo();
							detalleOrigen.IdDeclaracionOrigenFondos = IdDeclaracionOrigenFondos;
							detalleOrigen.IdTipoOrigenFondo = "VIN";
							detalleOrigen.Valor = new BigDecimal(origen.VentaInmueble);
							detalleOrigen.IdUsuarioCreador = idUsuarioDefecto;
							detalleOrigen.Otros = "";
							detalles.add(detalleOrigen);
							//Otros
							detalleOrigen = new EntidadDetalleOrigenFondo();
							detalleOrigen.IdDeclaracionOrigenFondos = IdDeclaracionOrigenFondos;
							detalleOrigen.IdTipoOrigenFondo = "OTR";
							detalleOrigen.Valor = new BigDecimal(origen.Otros);
							detalleOrigen.IdUsuarioCreador = idUsuarioDefecto;
							detalleOrigen.Otros = (origen.DescripcionOtros!=null && origen.DescripcionOtros.equalsIgnoreCase("null")) ? origen.DescripcionOtros : "";
							detalles.add(detalleOrigen);
							
							int[] resultado2 = sql.batch(conn, SQL_INGRESAR_DETALLE_ORIGEN_FONDO, detalles, EntidadDetalleOrigenFondo.class);
							
							if(resultado2[0]==1){
								salida.add(linea + " - ORIGEN FONDO CON DOCUMENTO IDENTIDAD " + origen.getDocumentoIdentidad() + " INGRESADO CORRECTAMENTE");
								conn.commit();
							}else{
								salida.add(linea + " - ORIGEN FONDO CON DOCUMENTO IDENTIDAD " + origen.getDocumentoIdentidad() + " NO INGRESADO");
							}
							
						}else{
							salida.add(linea + " - ORIGEN FONDO CON DOCUMENTO IDENTIDAD " + origen.getDocumentoIdentidad() + " NO INGRESADO");
						}
					}else {
						salida.add(linea + " - CLIENTE " + origen.getDocumentoIdentidad() + " NO EXISTE");
					}
				} catch (Exception e) {
					//el documento no esta asociado a una persona.
					salida.add(linea + " - ORIGEN FONDO CON DOCUMENTO IDENTIDAD " + origen.getDocumentoIdentidad() + " NO INGRESADO");
					e.printStackTrace();
				}
				conn.rollback();
				linea++;
			}
		}catch(Exception e) {
			salida.add("ERROR AL OBTENER CONEXION");
		}
		System.out.println(lines);
		return salida;
	}

}
