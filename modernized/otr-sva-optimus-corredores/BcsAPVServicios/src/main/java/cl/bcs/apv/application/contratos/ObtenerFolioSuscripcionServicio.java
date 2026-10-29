package cl.bcs.apv.application.contratos;

import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.ObtenerFolioSuscripcion;
import cl.bcs.apv.api.dto.obtenerfoliosuscripcion.MensajesErrores;
import cl.bcs.apv.api.dto.obtenerfoliosuscripcion.ObtenerFolioSuscripcionEntrada;
import cl.bcs.apv.api.dto.obtenerfoliosuscripcion.ObtenerFolioSuscripcionResultado;
import cl.bcs.apv.application.comun.Json;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.contratos.FolioSuscripcionRepositorio;
import cl.bcs.apv.domain.port.out.contratos.FolioSuscripcionRepositorio.FolioFila;

/** Use case {@code ObtenerFolioSuscripcion} (legacy {@code cl.bcs.APV.ObtenerFolioSuscripcion}). */
@Transactional(rollbackFor = Exception.class)
public class ObtenerFolioSuscripcionServicio implements ObtenerFolioSuscripcion {

    static final String PROCEDIMIENTO = "BSCL.APV.BD.Servicios::ActualizarFolioSuscripcion";

    private final FolioSuscripcionRepositorio folios;
    private final Colaboradores colaboradores;

    public ObtenerFolioSuscripcionServicio(FolioSuscripcionRepositorio folios, Colaboradores colaboradores) {
        this.folios = folios;
        this.colaboradores = colaboradores;
    }

    @Override
    public ObtenerFolioSuscripcionResultado ejecutar(ObtenerFolioSuscripcionEntrada in) {
        // the legacy NPE on a null channel (in.p_Canal.isEmpty()) is kept
        if (!in.canal().isEmpty()) {
            java.util.List<FolioFila> filas = folios.seleccionarPorCanal(in.canal());
            if (!filas.isEmpty()) {
                FolioFila f = filas.get(0);
                colaboradores.invocar("StoredProcedure", Json.objeto().poner("procedimiento", PROCEDIMIENTO)
                        .poner("entrada", Json.objeto().poner("p_Canal", f.Canal()).poner("p_Folio", f.Folio() + 1L).construir()).construir());
                return new ObtenerFolioSuscripcionResultado(f.Canal(), f.Folio() + 1L, f.Canal() + (f.Folio() + 1L), 0L, null);
            }
            folios.insertar(in.canal(), 1L);
            return new ObtenerFolioSuscripcionResultado(in.canal(), 1L, in.canal() + 1, 0L, null);
        }
        return new ObtenerFolioSuscripcionResultado("", 0L, "", 1L,
                new MensajesErrores(0L, Mensajes.MENSAJE_ERROR, "NO_SE_INGRESO_UN_CANAL", "NO_SE_INGRESO_UN_CANAL"));
    }
}
