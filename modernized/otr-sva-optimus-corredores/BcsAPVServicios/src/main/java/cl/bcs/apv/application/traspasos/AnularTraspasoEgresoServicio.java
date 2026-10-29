package cl.bcs.apv.application.traspasos;

import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.apv.api.casouso.AnularTraspasoEgreso;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.anulartraspasoegreso.AnularTraspasoEgresoEntrada;
import cl.bcs.apv.api.dto.anulartraspasoegreso.AnularTraspasoEgresoResultado;
import cl.bcs.apv.application.comun.Json;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.application.comun.Parametros;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.traspasos.AnularTraspasoEgresoRepositorio;

/** Use case {@code AnularTraspasoEgreso}: legacy {@code AnularTraspasoEgreso} (marks the valid egress traspasos as ANU, reactivates their TRA contracts, logs). */
@Transactional(rollbackFor = Exception.class)
public class AnularTraspasoEgresoServicio implements AnularTraspasoEgreso {

    private final ConfiguracionSistema configuracion;
    private final Colaboradores colaboradores;
    private final AnularTraspasoEgresoRepositorio repositorio;

    public AnularTraspasoEgresoServicio(ConfiguracionSistema configuracion, Colaboradores colaboradores, AnularTraspasoEgresoRepositorio repositorio) {
        this.configuracion = configuracion;
        this.colaboradores = colaboradores;
        this.repositorio = repositorio;
    }

    @Override
    public AnularTraspasoEgresoResultado ejecutar(AnularTraspasoEgresoEntrada in) {
        List<Mensaje> mensajes = new ArrayList<>();
        // null list: NullPointerException, as the legacy
        if (in.traspasosValidos().size() > 0) {
            Long idTrabajo = Double.valueOf(Math.random() * 10000000000L).longValue();
            List<Long> ids = new ArrayList<>();
            for (Integer p : in.traspasosValidos()) {
                ids.add(Long.parseLong(p + ""));
            }

            repositorio.anularTraspasos(ids, idTrabajo);
            repositorio.anularDetalleTraspasos(ids, idTrabajo);
            try {
                repositorio.reactivarContratos(ids, idTrabajo);
            } catch (Exception e) {
                // LEGACY DEFECT kept (see README APV-B-28): a failure of the contract reactivation is only printed, the traspasos stay annulled
                System.out.println(e.toString());
            }
            // legacy logs the invalid list here: a null list is a NullPointerException before the audit log
            in.traspasosInValidos().toString();
        } else {
            mensajes.add(Mensajes.de(0L, Mensajes.MENSAJE_ERROR, "", "No existen traspasos validos para anular."));
            agregarLogs(in);
            return new AnularTraspasoEgresoResultado(mensajes, Mensajes.numeroErrores(mensajes));
        }
        agregarLogs(in);
        return new AnularTraspasoEgresoResultado(mensajes, Mensajes.numeroErrores(mensajes));
    }

    private void agregarLogs(AnularTraspasoEgresoEntrada in) {
        String fechaSistema = Parametros.texto(configuracion, in.idEmpresa(), Parametros.PLATAFORMA, "PLA.FechaSistema");
        JsonArray logs = new JsonArray();
        for (Integer ord : in.traspasosValidos()) {
            logs.add(log(in, fechaSistema, ord, "TRASPASOEGRESOVALIDO"));
        }
        for (Integer ord : in.traspasosInValidos()) {
            logs.add(log(in, fechaSistema, ord, "TRASPASOEGRESOINVALIDO"));
        }
        colaboradores.invocar("AgregarLog", Json.objeto()
                .poner("p_IdEmpresa", null)
                .poner("p_IdUsuario", null)
                .poner("p_IdModulo", null)
                .poner("p_UsaReglasNegocio", null)
                .poner("p_IdEstadoAutorizacion", null)
                .poner("p_IdTrabajo", 0L)
                .poner("p_Log", logs)
                .poner("p_LogDetalle", new JsonArray())
                .construir());
    }

    private static JsonObject log(AnularTraspasoEgresoEntrada in, String fechaSistema, Integer ord, String tipoRegistro) {
        return Json.objeto()
                .poner("IdTrabajo", 0L)
                .poner("IdCabeceraLog", ord.longValue())
                .poner("IdTipoLog", "EGRESO")
                .poner("IdModulo", "APV")
                .poner("IdUsuario", in.idUsuario())
                .poner("IdEmpresa", in.idEmpresa())
                .poner("FechaSistemaLog", fechaSistema)
                .poner("IdUsuarioAtendido", in.idUsuario())
                .poner("IdCliente", 0L)
                .poner("EstadoCliente", "")
                .poner("NombreCliente", "")
                .poner("IdTipoDocumentoIdentidad", "")
                .poner("IdDocumentoIdentidad", "")
                .poner("IdPortafolio", 0L)
                .poner("EstadoPortafolio", "")
                .poner("IdTipoRegistro", tipoRegistro)
                .poner("IdRegistro", ord.longValue() + "")
                .poner("EstadoRegistro", "")
                .poner("IdTipoOperacion", "")
                .poner("Folio", 0L)
                .poner("Cantidad", 0L)
                .poner("PrecioTasa", 0L)
                .poner("Monto", 0L)
                .poner("IdMonedaOrigen", "")
                .poner("IdMonedaLiquidacion", "")
                .poner("TipoCambio", "")
                .poner("Nemo", "")
                .poner("IdProceso", 0L)
                .poner("EstadoProceso", "")
                .poner("NombreProceso", "")
                .poner("IdPersona", 0L)
                .construir();
    }
}
