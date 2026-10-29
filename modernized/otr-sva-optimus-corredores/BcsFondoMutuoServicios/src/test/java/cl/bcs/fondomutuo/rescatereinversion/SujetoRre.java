package cl.bcs.fondomutuo.rescatereinversion;

import java.sql.Connection;
import java.util.EnumMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import cl.bcs.fondomutuo.adapter.out.jdbc.CalendarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConfiguracionSistemaJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.CuotasJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.DiccionarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.FechasJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.PerfilFondoMutuoJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.RevisarRequisitosBasicosJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.SeriesFondoJdbc;
import cl.bcs.fondomutuo.agf.AgfJdbc;
import cl.bcs.fondomutuo.cierre3b.Guion;
import cl.bcs.fondomutuo.cierre3b.SecuenciaH2;
import cl.bcs.fondomutuo.comisionnn.ComisionNnJdbc;
import cl.bcs.fondomutuo.custodia.FlujoCustodiaFmJdbc;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.InformacionEmpresa;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.manejador.ManejadorSolicitudAporteRescateService;
import cl.bcs.fondomutuo.manejador.ManejadorSolicitudAporteRescateService.Lista;
import cl.bcs.fondomutuo.manejador.TipoNoServido;
import cl.bcs.fondomutuo.manejador.TipoSolicitudPort;
import cl.bcs.fondomutuo.patrimonio.PatrimonioEmisorJdbc;
import cl.bcs.fondomutuo.requisitos.RequisitosJdbc;

/** The NEW RescateReinversion over the REAL JDBC adapters on the H2 of the case; collaborators outside the closure answered by the case script ({@link Guion}). */
final class SujetoRre {

    /** Test hook (G5 / failure tests): replaces the HTTP write ports. Null = the case script. */
    static volatile cl.bcs.fondomutuo.port.out.http.LogPort logPrueba;
    static volatile cl.bcs.fondomutuo.port.out.http.BatchPort batchPrueba;

    private SujetoRre() {
    }

    static RescateReinversionPort crear(ConexionUnidadDeTrabajo conexion, Connection con, Guion guion, JsonArray secuencias, Compuertas compuertas) {
        RescateReinversionFabrica.Dependencias d = new RescateReinversionFabrica.Dependencias();
        ConfiguracionSistemaJdbc configuracion = new ConfiguracionSistemaJdbc(conexion);
        DiccionarioJdbc diccionario = new DiccionarioJdbc(conexion);
        d.configuracion = configuracion;
        d.diccionario = diccionario;
        d.basicos = new RevisarRequisitosBasicosJdbc(conexion, diccionario);
        d.calendario = new CalendarioJdbc(conexion, configuracion);
        d.empresas = id -> new Gson().fromJson(guion.responder("ConsultarEmpresa", new JsonPrimitive(id)), InformacionEmpresa.class);
        d.series = new SeriesFondoJdbc(conexion);
        d.requisitosDatos = new RequisitosJdbc(conexion);
        d.comisionNnDatos = new ComisionNnJdbc(conexion);
        d.datos = new RreJdbc(conexion);
        d.bloqueo = new BloqueoReinversionJdbc(conexion);
        d.perfilPort = new PerfilFondoMutuoJdbc(conexion);
        d.fechasPort = new FechasJdbc(conexion);
        d.cuotasPort = new CuotasJdbc(conexion);
        d.patrimonioPort = new PatrimonioEmisorJdbc(conexion);
        d.flujoCustodiaDatos = new FlujoCustodiaFmJdbc(conexion);
        d.agfDatos = new AgfJdbc(conexion);
        d.secuencias = new SecuenciaH2(con, secuencias);
        d.paridad = e -> guion.responder("CalcularParidad", e);
        d.precios = e -> guion.responder("EntregarPreciosTasas", e);
        d.custodia = e -> guion.responder("ConsultarFlujoCustodia", e);
        d.log = logPrueba != null ? logPrueba : e -> guion.responder("AgregarLog", e);
        d.batch = batchPrueba != null ? batchPrueba : e -> guion.responder("InscribirProcesoBatch", e);
        d.productos = e -> guion.responder("ConsultarProductosPortafolio", e);
        d.compuertas = compuertas;
        return RescateReinversionFabrica.crear(d);
    }

    /** The real Manejador facade with the RescateReinversion destination (the other destinations are not served; Normal is not part of these cases). */
    static ManejadorSolicitudAporteRescateService manejador(RescateReinversionPort rre, Compuertas compuertas) {
        Map<Lista, TipoSolicitudPort> destinos = new EnumMap<>(Lista.class);
        for (Lista l : Lista.values()) {
            if (l != Lista.NORMAL) destinos.put(l, new TipoNoServido(l.name()));
        }
        destinos.put(Lista.RESCATE_REINVERSION, new RescateReinversionDestino(rre));
        return new ManejadorSolicitudAporteRescateService(in -> {
            throw new ServicioNoServidoException("G0", "Normal is not part of the FMM-RRE cases");
        }, destinos, compuertas::g0ListasConFilas);
    }
}
