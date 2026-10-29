package cl.bcs.apv.application.recaudacion;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.ModificarAnotacionRecaudacion;
import cl.bcs.apv.api.dto.modificaranotacionrecaudacion.ModificarAnotacionRecaudacionEntrada;
import cl.bcs.apv.api.dto.modificaranotacionrecaudacion.ModificarAnotacionRecaudacionResultado;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code ModificarAnotacionRecaudacion} (family C): updates one annotation of a deposit. Legacy {@code cl.bcs.APV.ModificarAnotacionRecaudacion}.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ModificarAnotacionRecaudacionServicio implements ModificarAnotacionRecaudacion {

    private static final String SQL_UPDATE_ANOTACION = SqlLegacy.recurso("Anotacion_Update");

    private final NamedParameterJdbcTemplate jdbc;
    private final Secuencias secuencias;

    public ModificarAnotacionRecaudacionServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.secuencias = secuencias;
    }

    /** Parameter row of the update statement (legacy {@code ModificarAnotacionRecaudacion.Anotacion_Update}). */
    public static class AnotacionUpdate {
        public Long IdAnotacion;
        public String IdMotivo;
        public String Titulo;
        public String Detalle;
        public String IdRelevancia;
        public String IdAmbito;
        public String FechaDesde;
        public String FechaHasta;
    }

    @Override
    public ModificarAnotacionRecaudacionResultado ejecutar(ModificarAnotacionRecaudacionEntrada in) {
        try {
            // LEGACY DEFECT kept (see README APV-C-440..443): the REFID of the modification is a NEW sequence value
            List<Long> ids = Colab.secuencia(secuencias, "BSCL.APV.BD.Secuencias::REC.IdAnotacion", 1L);

            AnotacionUpdate an = new AnotacionUpdate();
            an.IdAnotacion = in.idAnotacion();
            // LEGACY DEFECT kept (see README APV-C-440..443): IdMotivo takes p_IdAmbito
            an.IdMotivo = in.idAmbito();
            an.Titulo = in.titulo();
            an.Detalle = in.detalle();
            an.IdRelevancia = in.idRelevancia();
            an.IdAmbito = in.idAmbito();
            an.FechaDesde = in.fechaDesde();
            an.FechaHasta = in.fechaHasta();
            List<AnotacionUpdate> lista = new ArrayList<>();
            lista.add(an);
            LoteConteo.ejecutar(jdbc, SQL_UPDATE_ANOTACION, lista);

            List<MensajeLegacy> mensajes = new ArrayList<>();
            mensajes.add(new MensajeLegacy(1L, MensajeLegacy.OK, "OK", "Proceso ejecutado correctamente"));
            return new ModificarAnotacionRecaudacionResultado(MensajeLegacy.aMensajes(mensajes), MensajeLegacy.contarErrores(mensajes), ids.get(0));
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }
}
