package cl.bcs.oracle;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import com.google.gson.Gson;

import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.EntradaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.InFormaPagoSpot;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate;
import cl.bcs.fondomutuo.normal.NormalContrato;
import cl.bcs.fondomutuo.normal.NormalPort;

/**
 * {@link NormalPort} over the UNMODIFIED LEGACY Normal of the oracle (isolation from step 4A). As in the legacy Manejador: ONE Normal instance and ONE
 * legacy input object for both passes, and the same request objects in both passes (an identity map keeps the legacy twin of every new request object,
 * so whatever Normal mutates in the simulation is seen by the real pass). The fields of the input that the Manejador never sets keep the legacy defaults.
 */
class LegacyNormalPort implements NormalPort {
    private static final Gson GSON = new Gson();
    private final Connection con;
    private final CrearSolicitudAporteRescateNormal servicio = new CrearSolicitudAporteRescateNormal();
    private final EntradaCrearSolicitudAporteRescateNormal in = new EntradaCrearSolicitudAporteRescateNormal();
    private final IdentityHashMap<NormalContrato.InSolicitud, InSolicitudesAporteRescate> solicitudes = new IdentityHashMap<>();
    private final IdentityHashMap<NormalContrato.InFormaPagoSpot, InFormaPagoSpot> formas = new IdentityHashMap<>();

    LegacyNormalPort(Connection con) {
        this.con = con;
    }

    @Override
    public NormalContrato.Salida logica(NormalContrato.Entrada e) throws Exception {
        in.p_IdEmpresa = e.p_IdEmpresa;
        in.p_IdUsuario = e.p_IdUsuario;
        in.p_IdModulo = e.p_IdModulo;
        in.p_SimularOperacion = e.p_SimularOperacion;
        in.p_UsaReglasNegocio = e.p_UsaReglasNegocio;
        in.p_IdTipoCanalIngreso = e.p_IdTipoCanalIngreso;
        if (e.p_SolicitudesAporteRescate == null) {
            in.p_SolicitudesAporteRescate = null;
        } else {
            List<InSolicitudesAporteRescate> l = new ArrayList<>();
            for (NormalContrato.InSolicitud s : e.p_SolicitudesAporteRescate) {
                l.add(solicitudes.computeIfAbsent(s, k -> GSON.fromJson(GSON.toJson(k), InSolicitudesAporteRescate.class)));
            }
            in.p_SolicitudesAporteRescate = l;
        }
        if (e.p_FormaPagoSpot == null) {
            in.p_FormaPagoSpot = null;
        } else {
            List<InFormaPagoSpot> l = new ArrayList<>();
            for (NormalContrato.InFormaPagoSpot f : e.p_FormaPagoSpot) {
                l.add(formas.computeIfAbsent(f, k -> GSON.fromJson(GSON.toJson(k), InFormaPagoSpot.class)));
            }
            in.p_FormaPagoSpot = l;
        }
        return GSON.fromJson(GSON.toJson(servicio.logica(con, in)), NormalContrato.Salida.class);
    }
}
