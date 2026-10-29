package cl.bcs.apv.application.recaudacion;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CrearAnotacionRecaudacion;
import cl.bcs.apv.api.dto.crearanotacionrecaudacion.CrearAnotacionRecaudacionEntrada;
import cl.bcs.apv.api.dto.crearanotacionrecaudacion.CrearAnotacionRecaudacionResultado;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code CrearAnotacionRecaudacion} (family C): inserts one annotation of a deposit. Legacy {@code cl.bcs.APV.CrearAnotacionRecaudacion}.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CrearAnotacionRecaudacionServicio implements CrearAnotacionRecaudacion {

    private static final String SQL_INSERT_ANOTACION = SqlLegacy.recurso("Anotacion_Insert");

    private final NamedParameterJdbcTemplate jdbc;
    private final Secuencias secuencias;
    private final Reloj reloj;

    public CrearAnotacionRecaudacionServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.secuencias = secuencias;
        this.reloj = reloj;
    }

    /** Parameter row of the insert statement (legacy {@code CrearAnotacionRecaudacion.Anotacion_Insert}). */
    public static class AnotacionInsert {
        public Long IdAnotacion;
        public Long IdRecaudacion;
        public String FechaAnotacion;
        public String IdMotivo;
        public String Titulo;
        public String Detalle;
        public String FechaIngreso;
        public Long IdUsuarioCreador;
        public String IdRelevancia;
        public String IdAmbito;
        public String FechaDesde;
        public String FechaHasta;
    }

    @Override
    public CrearAnotacionRecaudacionResultado ejecutar(CrearAnotacionRecaudacionEntrada in) {
        try {
            java.sql.Date fechaSistema = new java.sql.Date(reloj.ahora().toEpochMilli());
            List<Long> ids = Colab.secuencia(secuencias, "BSCL.APV.BD.Secuencias::REC.IdAnotacion", 1L);

            AnotacionInsert an = new AnotacionInsert();
            an.IdAnotacion = ids.get(0);
            an.IdRecaudacion = in.idRecaudacion();
            an.FechaAnotacion = fechaSistema.toString();
            // LEGACY DEFECT kept (see README APV-C-430..433): IdMotivo takes p_IdAmbito
            an.IdMotivo = in.idAmbito();
            an.Titulo = in.titulo();
            an.Detalle = in.detalle();
            an.FechaIngreso = fechaSistema.toString();
            an.IdUsuarioCreador = in.idUsuario();
            an.IdRelevancia = in.idRelevancia();
            an.IdAmbito = in.idAmbito();
            an.FechaDesde = in.fechaDesde();
            an.FechaHasta = in.fechaHasta();
            List<AnotacionInsert> lista = new ArrayList<>();
            lista.add(an);
            LoteConteo.ejecutar(jdbc, SQL_INSERT_ANOTACION, lista);

            List<MensajeLegacy> mensajes = new ArrayList<>();
            mensajes.add(new MensajeLegacy(1L, MensajeLegacy.OK, "OK", "Proceso ejecutado correctamente"));
            return new CrearAnotacionRecaudacionResultado(MensajeLegacy.aMensajes(mensajes), MensajeLegacy.contarErrores(mensajes), ids.get(0));
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }
}
