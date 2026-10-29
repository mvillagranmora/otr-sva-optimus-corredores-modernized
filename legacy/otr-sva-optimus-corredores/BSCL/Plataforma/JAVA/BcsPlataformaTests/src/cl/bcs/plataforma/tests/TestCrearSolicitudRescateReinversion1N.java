/**
 * 
 */
package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cl.bcs.fondomutuo.CrearIngresoRetiroTituloFondoMutuo.EntradaCrearIngresoRetiroTituloFondoMutuo;
import cl.bcs.fondomutuo.CrearIngresoRetiroTituloFondoMutuo.SalidaCrearIngresoRetiroTituloFondoMutuo;
import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion1N.EntradaCrearSolicitudRescateReinversion1N;
import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion1N.SalidaCrearSolicitudRescateReinversion1N;
import cl.bcs.fondomutuo.TestCrearSolicituAporteRescateNormal.ConsultaCliente;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

/**
 * @author saltamirano
 *
 */
class TestCrearSolicitudRescateReinversion1N {
	
	public static SalidaCrearIngresoRetiroTituloFondoMutuo ObjOut_SalidaCrearIngresoRetiroTituloFondoMutuo;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void BeforeAll() throws Exception {
		//CREAR UN INGRESO DE TITULO A PROBAR 
		try(Connection con = PlataformaUtils.getPropertiesConnection("master01", "connections.properties")){
			crearIngresoTitulo(con);
			con.commit();
		}
	}

	@Test
	void testCrearSolicitud() {
	    System.out.println("== TEST ==");
	    try (Connection con = PlataformaUtils.getPropertiesConnection("master01", "connections.properties")) {
	        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

	        // Generar el JSON de manera dinámica
	        Long idCliente = 61625L; // Ejemplo de ID de cliente
	        Long idPortafolio = 206349L; // Ejemplo de ID de portafolio
	        String json = JsonCrearSolicitudRescateReinversion1NGenerator.generateJsonCrearSolicitudRescateReinversion1N(idCliente, idPortafolio);

	        EntradaCrearSolicitudRescateReinversion1N Objin = gson.fromJson(json, EntradaCrearSolicitudRescateReinversion1N.class);
	        CrearSolicitudRescateReinversion1N demo = new CrearSolicitudRescateReinversion1N();

	        try {
	            SalidaCrearSolicitudRescateReinversion1N ObjOut = demo.logica(con, Objin);
	            con.commit();

	            // Assertions
	            assertNotNull(ObjOut, "La salida no debería ser nula");
	            assertEquals(0, ObjOut.P_OUT_ERRORESCONTADOR.longValue(), "No debería haber errores en la ejecución");
	            System.out.println("=======================\n");
	            System.out.println(ObjOut.toString());
	        } catch (Exception er) {
	            er.printStackTrace();
	            con.rollback();
	            fail("Se produjo una excepción durante la ejecución del test");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        fail("Se produjo una excepción al establecer la conexión");
	    }
	    System.out.println("== END ==");
	}
	
	private static void crearIngresoTitulo(Connection con) throws Exception {
	    CrearIngresoRetiroTituloFondoMutuo ingresoTitulo = new CrearIngresoRetiroTituloFondoMutuo();
	    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

        Long idCliente = 61625L; // Ejemplo de ID de cliente
        Long idPortafolio = 206349L; // Ejemplo de ID de portafolio
        
	    String jsonIngresoTitulo = JsonCrearSolicitudRescateReinversion1NGenerator.generateJsonIngresoTitulo(idCliente, idPortafolio);

	    EntradaCrearIngresoRetiroTituloFondoMutuo objIn = gson.fromJson(jsonIngresoTitulo, EntradaCrearIngresoRetiroTituloFondoMutuo.class);
	    System.out.println(objIn.toString());
	    ObjOut_SalidaCrearIngresoRetiroTituloFondoMutuo = ingresoTitulo.logica(con, objIn);

	    System.out.println("=========INGRESO TÍTULO========");
	    System.out.println(ObjOut_SalidaCrearIngresoRetiroTituloFondoMutuo.toString());
	    System.out.println("=================");
	    con.commit();
	}

}

class JsonCrearSolicitudRescateReinversion1NGenerator {
	
    public static String generateJsonIngresoTitulo(Long idCliente, Long idPortafolio) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("p_IdEmpresa", 1);
        jsonMap.put("p_IdUsuario", 0);
        jsonMap.put("p_IdModulo", "FMM");
        jsonMap.put("p_simularOperacion", "N");
        jsonMap.put("p_UsaReglasNegocio", "S");

        Map<String, Object> solicitud1 = new HashMap<>();
        solicitud1.put("IdInterno", 1);
        solicitud1.put("IdCliente", idCliente);
        solicitud1.put("IdPortafolio", idPortafolio);
        solicitud1.put("IdTipoMovimientoFondoMutuo", "INGTIT");
        solicitud1.put("IdSerieFondo", "00039ACLP");
        solicitud1.put("NumeroCuotas", 200);
        solicitud1.put("SolicitudBatch", "N");
        solicitud1.put("OrigenOperacion", "M");
        solicitud1.put("IdRegimenTributario", "18Q");
        solicitud1.put("Glosa", "PRUEBA EJECUTADO POR JAVA UNITARIA TEST");
        solicitud1.put("IdAutorizadoOrigen", 0);

        Map<String, Object> solicitud2 = new HashMap<>();
        solicitud2.put("IdInterno", 1);
        solicitud2.put("IdCliente", 61625L); // Ejemplo de otro cliente
        solicitud2.put("IdPortafolio", 206349L); // Ejemplo de otro portafolio
        solicitud2.put("IdTipoMovimientoFondoMutuo", "INGTIT");
        solicitud2.put("IdSerieFondo", "00041CCLP");
        solicitud2.put("NumeroCuotas", 100);
        solicitud2.put("SolicitudBatch", "N");
        solicitud2.put("OrigenOperacion", "M");
        solicitud2.put("IdRegimenTributario", "APV-A");
        solicitud2.put("Glosa", "");
        solicitud2.put("IdAutorizadoOrigen", 0);

        jsonMap.put("p_SolicitudesTitulo", new Map[]{solicitud1});

        Gson gson = new Gson();
        return gson.toJson(jsonMap);
    }

    public static String generateJsonCrearSolicitudRescateReinversion1N(Long idCliente, Long idPortafolio) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("p_IdEmpresa", 1);
        jsonMap.put("p_IdUsuario", 0);
        jsonMap.put("p_IdModulo", "FMM");
        jsonMap.put("p_simularOperacion", "N");
        jsonMap.put("p_UsaReglasNegocio", "S");

        // Solicitud de Rescate
        Map<String, Object> solicitudRescate = new HashMap<>();
        solicitudRescate.put("IdInterno", 1L);
        solicitudRescate.put("IdCliente", idCliente);
        solicitudRescate.put("IdPortafolio", idPortafolio);
        solicitudRescate.put("IdSerieFondo", "00039ACLP");
        solicitudRescate.put("CalculoPorMontoCuota", "C");
        solicitudRescate.put("Monto", 397474);
        solicitudRescate.put("NumeroCuotas", 193.2191);
        solicitudRescate.put("IdMoneda", "CLP");
        solicitudRescate.put("IdRegimenTributario", "18Q");
        solicitudRescate.put("IdMedioDePago", "CIN");
        solicitudRescate.put("SolicitudBatch", "N");
        solicitudRescate.put("OrigenOperacion", "M");
        solicitudRescate.put("Glosa", "REINVERSION 1 A N");
        solicitudRescate.put("IdAutorizadoOrigen", 0);
        solicitudRescate.put("FolioContrato", 0);
        solicitudRescate.put("IdAgrupador", 1);
        solicitudRescate.put("Porcentaje", 100);

        // Solicitud de Aporte
        Map<String, Object> solicitudAporte = new HashMap<>();
        solicitudAporte.put("IdInterno", 1);
        solicitudAporte.put("IdCliente", idCliente);
        solicitudAporte.put("IdPortafolio", idPortafolio);
        solicitudAporte.put("IdSerieFondo", "00067ACLP");
        solicitudAporte.put("IdEmisor", "SURA");
        solicitudAporte.put("CalculoPorMontoCuota", "M");
        solicitudAporte.put("Monto", 397474);
        solicitudAporte.put("NumeroCuotas", 297.3909);
        solicitudAporte.put("IndicadorMLT", "S");
        solicitudAporte.put("IdMoneda", "CLP");
        solicitudAporte.put("IdMedioDePago", "CIN");
        solicitudAporte.put("IdRegimenTributario", "18Q");
        solicitudAporte.put("Glosa", 100);
        solicitudAporte.put("Porcentaje", 100);
        solicitudAporte.put("FolioContrato", 0);
        solicitudAporte.put("IdAgrupador", 1);

        jsonMap.put("p_SolicitudesRescate", new Map[]{solicitudRescate});
        jsonMap.put("p_SolicitudesAporte", new Map[]{solicitudAporte});

        Gson gson = new Gson();
        return gson.toJson(jsonMap);
    }
    
}
