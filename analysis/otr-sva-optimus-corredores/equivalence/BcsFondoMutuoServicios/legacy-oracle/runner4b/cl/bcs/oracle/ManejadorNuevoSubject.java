package cl.bcs.oracle;

import java.sql.Connection;
import java.util.EnumMap;
import java.util.Map;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.manejador.ManejadorJavaCallHandler;
import cl.bcs.fondomutuo.manejador.ManejadorSolicitudAporteRescateService;
import cl.bcs.fondomutuo.manejador.ManejadorSolicitudAporteRescateService.Lista;
import cl.bcs.fondomutuo.manejador.TipoSolicitudPort;

/**
 * Step 4B subject: the NEW {@code ManejadorJavaCallHandler} (real class, real gate {@code Compuertas.g0ListasConFilas}) over the oracle's H2 session.
 * Isolation from step 4A: the Normal destination is the UNMODIFIED LEGACY {@code CrearSolicitudAporteRescateNormal} (adapter {@link LegacyNormalPort}) and
 * the seven other destinations are the oracle's registered doubles (adapter {@link LegacyDestinoPort}), so the dump compares the dispatch logic of the
 * new facade against the legacy facade on the same destinations. -Doracle.listas=NORMAL,PROGRAMADO,... = lists the gate serves (default: all eight,
 * the F1 gate is tested in JUnit). Only the service "ManejadorSolicitudAporteRescate".
 */
public class ManejadorNuevoSubject implements Subject {

    @Override
    public JsonObject call(String servicio, Connection con, JsonObject jin) throws Exception {
        if (!"ManejadorSolicitudAporteRescate".equals(servicio)) throw new IllegalArgumentException("this subject only runs the Manejador, not " + servicio);
        String listas = System.getProperty("oracle.listas", "NORMAL,PROGRAMADO,OVERNIGHT,REINVERSION_APORTE,CAMBIO_SERIE,RESCATE_REINVERSION,TRANSFERENCIA,TRASPASO");
        Compuertas compuertas = new Compuertas(listas, false, false, false, false, false);
        Map<Lista, TipoSolicitudPort> destinos = new EnumMap<>(Lista.class);
        destinos.put(Lista.PROGRAMADO, new LegacyDestinoPort(con, "CrearSolicitudAporteRescateProgramado"));
        destinos.put(Lista.OVERNIGHT, new LegacyDestinoPort(con, "CrearSolicitudAporteOvernight"));
        destinos.put(Lista.REINVERSION_APORTE, new LegacyDestinoPort(con, "CrearSolicitudAporteReinversion"));
        destinos.put(Lista.CAMBIO_SERIE, new LegacyDestinoPort(con, "CrearSolicitudCambioSerie"));
        destinos.put(Lista.RESCATE_REINVERSION, new LegacyDestinoPort(con, "CrearSolicitudRescateReinversion"));
        destinos.put(Lista.TRANSFERENCIA, new LegacyDestinoPort(con, "CrearSolicitudRescateTransferencia"));
        destinos.put(Lista.TRASPASO, new LegacyDestinoPort(con, "CrearSolicitudRescateTraspaso"));
        ManejadorSolicitudAporteRescateService servicioNuevo = new ManejadorSolicitudAporteRescateService(new LegacyNormalPort(con), destinos,
                compuertas::g0ListasConFilas);
        return new ManejadorJavaCallHandler(servicioNuevo).call(con, jin);
    }
}
