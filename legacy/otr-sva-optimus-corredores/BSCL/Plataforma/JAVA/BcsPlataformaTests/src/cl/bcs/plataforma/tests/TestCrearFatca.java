package cl.bcs.plataforma.tests;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.junit.jupiter.api.Test;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.CrearFatca;
import cl.bcs.socionegocio.per.CrearFatca.EntradaCrearFatca;
import cl.bcs.socionegocio.per.CrearFatca.SalidaCrearFatca;
import cl.bcs.socionegocio.per.pojo.Fatca;
import cl.bcs.utils.SQL;


class TestCrearFatca {	
	
	private static final SimpleDateFormat FORMATO_FECHA = new SimpleDateFormat("yyyy-MM-dd");	
	String sql = "SELECT top 1 \"IdPersona\" FROM \"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad\" WHERE \"IdPersona\" NOT IN (SELECT \"IdPersona\" FROM \"BSCL.SocioNegocio.BD::PER.Fatca\")";
	
	public static class SelectIdPersona{
		public Long IdPersona;
	} 
	
	@Test
    void testCrearFatca() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		
    		SQL _sql = new SQL();
    		List<SelectIdPersona> c = _sql.query(conn, sql, new HashMap<>(), SelectIdPersona.class);
    		
    		CrearFatca demo = new CrearFatca(); 
    		List<Fatca> fatcas = new ArrayList<Fatca>();
    		Fatca fatca = new Fatca();
    		fatca.IdInterno =  0L;
    		fatca.IdPersona = c.get(0).IdPersona;
    		fatca.IdEstado = "ACT";
    		fatca.NombreCompleto = "";
    		fatca.IdClasificacionFatca = "PENDIENTE";
    		fatca.IdClasificacionEmpresaFatca = "";
    		fatca.NumeroGIIN = "";
    		fatca.FechaFirmaFormulario = new java.sql.Timestamp(FORMATO_FECHA.parse("0001-01-01 00:00:00.000").getTime());
    		fatca.AutorizacionInformarOrganismo = "N";
    		fatca.ExcepcionResidente = "N";
    		fatca.ExcepcionNacimiento = "N";
    		fatca.ExcepcionDomicilio = "N";
    		fatca.ExcepcionTelefono = "N";
    		fatca.ExcepcionOrdenPermanente = "N";
    		fatca.ExcepcionFirma = "N";
    		fatca.ParticipacionSociedadesUSA = "N";
    		fatca.OrdenPermanente = "N";
    		fatca.NACEEUU = "";
    		fatca.NACEEUUExplicacion = "";
    		fatca.EntidadFinanciera = "";
    		fatca.TipoInstitucionFinanciera = "";
    		fatca.EntidadGestionada = "";
    		fatca.IngresosPasivos = "";
    		fatca.GenerarIngresosPasivos = "";
    		fatca.CategoriaEntidad = "";
    		fatca.GIINEntidad = "";
    		fatca.RazonSocial = "";
    		fatca.FechaLiquidacion = new java.sql.Date(FORMATO_FECHA.parse("0001-01-01 00:00:00.000").getTime());
    		fatca.NombreMercado = "";
    		fatca.FechaConstitucion = new java.sql.Date(FORMATO_FECHA.parse("0001-01-01 00:00:00.000").getTime());
    		fatca.ObligacionesFiscales = "";
    		fatca.ObligacionesFiscalesExplicacion = "";
    		fatca.IndividuoControl = "";
    		fatca.Borrado = "N";
    		fatca.IdUsuarioCreador = 0L;
    		fatca.IdUsuarioModificador = 0L;
    		fatca.IdCarga = 0L;
    		fatcas.add(fatca);
    		
    		EntradaCrearFatca entradaCrearFacta = new EntradaCrearFatca();
    		entradaCrearFacta.p_IdEmpresa = 1L;
    		entradaCrearFacta.p_IdUsuario = 0L;
    		entradaCrearFacta.p_Fatcas = fatcas;
    		
    		
    		SalidaCrearFatca salidaCrearFacta = demo.logica(conn, entradaCrearFacta);
					
			assertTrue(salidaCrearFacta.P_OUT_REFID.get(0).IdEntidad > 0);
    		conn.rollback();
    	}
    }
    	
}