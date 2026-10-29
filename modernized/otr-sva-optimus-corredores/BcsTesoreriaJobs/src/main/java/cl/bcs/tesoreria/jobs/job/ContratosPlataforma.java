package cl.bcs.tesoreria.jobs.job;

import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.jobs.job.port.ServiciosExternos;
import cl.bcs.tesoreria.port.out.Colaboradores;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON contracts of the Plataforma services the mass-load job calls through {@link Colaboradores} (names in
 * {@link ServiciosExternos}). The nested classes keep the legacy names and field names (they ARE the contract) and the legacy
 * constructor defaults: an absent field of the answer keeps them, as the legacy typed objects did.
 */
final class ContratosPlataforma {

    private ContratosPlataforma() {
    }

    /** Legacy {@code cl.bcs.plataforma.cre.ConsultarEstadoModulo} (Plataforma/.../cre/ConsultarEstadoModulo.java:19-60). */
    static final class ConsultarEstadoModulo {

        static final class Modulo {
            public String IdEstadoModulo;
            public String NombreModulo;
        }

        static final class In {
            public Long p_IdEmpresa;
            public String p_IdModulo;
            public Long p_IdUsuario;
        }

        static final class Out {
            public List<Modulo> p_out_ListaModulos;
        }

        static Out logica(Colaboradores colaboradores, In entrada) {
            return JsonLegacy.de(colaboradores.invocar(ServiciosExternos.CONSULTAR_ESTADO_MODULO,
                    JsonLegacy.aJson(entrada)), Out.class);
        }
    }

    /** Legacy {@code cl.bcs.plataforma.pla.ObtenerDiaAnterior} (Plataforma/.../pla/ObtenerDiaAnterior.java:39-76). */
    static final class ObtenerDiaAnterior {

        static final class FechasIn {
            public String fechaInicial;
            public Long numeroDias;
        }

        static final class In {
            public Long idPais;
            public String diaHabil;
            public String soloCaeDiaHabil;
            public boolean excluirConsultarDiaFeriado = false;
            public List<FechasIn> fechas = new ArrayList<>();
        }

        static final class FechasOut {
            public String fechaInicial;
            public Long numeroDiasInicial;
            public Long numeroDias;
            public String fechaCalculada;
        }

        static final class Out {
            public Long erroresContador = 0L;
            public List<FechasOut> fechas = new ArrayList<>();
        }

        static Out logica(Colaboradores colaboradores, In entrada) {
            return JsonLegacy.de(colaboradores.invocar(ServiciosExternos.OBTENER_DIA_ANTERIOR,
                    JsonLegacy.aJson(entrada)), Out.class);
        }
    }
}
