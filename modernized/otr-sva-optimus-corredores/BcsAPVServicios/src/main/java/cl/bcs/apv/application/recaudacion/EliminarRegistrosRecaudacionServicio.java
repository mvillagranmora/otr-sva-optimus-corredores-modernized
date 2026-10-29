package cl.bcs.apv.application.recaudacion;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CambiarEstadoRecaudacion;
import cl.bcs.apv.api.casouso.EliminarRegistrosRecaudacion;
import cl.bcs.apv.api.dto.cambiarestadorecaudacion.CambiarEstadoRecaudacionEntrada;
import cl.bcs.apv.api.dto.cambiarestadorecaudacion.ParametrosRecaudacionEstado;
import cl.bcs.apv.api.dto.eliminarregistrosrecaudacion.EliminarRegistrosRecaudacionEntrada;
import cl.bcs.apv.api.dto.eliminarregistrosrecaudacion.EliminarRegistrosRecaudacionResultado;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code EliminarRegistrosRecaudacion} (family C): deletes the client or employer detail of a deposit and puts the deposit back to ING.
 * Legacy {@code cl.bcs.APV.EliminarRegistrosRecaudacion} (call and logica); the legacy commits at the end of logica.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class EliminarRegistrosRecaudacionServicio implements EliminarRegistrosRecaudacion {

    private static final String SQL_ELIMINA_REGISTRO_CLIENTE = SqlLegacy.recurso("EliminaRegistroRecaudacionCliente");
    private static final String SQL_ELIMINA_REGISTRO_EMPLEADOR = SqlLegacy.recurso("EliminaRegistroRecaudacionEmpleador");

    private final NamedParameterJdbcTemplate jdbc;
    private final SqlLegacy sql;
    private final CambiarEstadoRecaudacion cambiarEstadoRecaudacion;

    public EliminarRegistrosRecaudacionServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores, CambiarEstadoRecaudacion cambiarEstadoRecaudacion) {
        this.jdbc = jdbc;
        this.sql = new SqlLegacy(jdbc);
        this.cambiarEstadoRecaudacion = cambiarEstadoRecaudacion;
    }

    /** Parameter row of the delete statements (legacy {@code EliminarRegistrosRecaudacion.ParametrosEntradaServicio}). */
    public static class EliminarFila {
        public Long IdEmpresa;
        public Long IdRecaudacion;
        public Long IdRecaudacionEmpleador;
        public Long IdRecaudacionCliente;
        public String TipoPersonaEliminar;
    }

    @Override
    public EliminarRegistrosRecaudacionResultado ejecutar(EliminarRegistrosRecaudacionEntrada in) {
        try {
            List<MensajeLegacy> mensajes = logica(in);
            return new EliminarRegistrosRecaudacionResultado(MensajeLegacy.aMensajes(mensajes), MensajeLegacy.contarErrores(mensajes));
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    private List<MensajeLegacy> logica(EliminarRegistrosRecaudacionEntrada in) throws Exception {
        List<MensajeLegacy> out = new ArrayList<>();
        EliminarFila inS = new EliminarFila();
        List<EliminarFila> listIn = new ArrayList<>();
        inS.IdEmpresa = in.idEmpresa();
        inS.IdRecaudacion = in.idRecaudacion();
        inS.IdRecaudacionEmpleador = in.idRecaudacionEmpleador();
        inS.IdRecaudacionCliente = in.idRecaudacionCliente();
        inS.TipoPersonaEliminar = in.tipoPersonaEliminar();
        listIn.add(inS);

        int[] resEmpleador;
        int[] resCliente;
        Long idRecaudacionEmpleador = 0L;

        if (inS.TipoPersonaEliminar.equals("E")) {
            resEmpleador = LoteConteo.ejecutar(jdbc, SQL_ELIMINA_REGISTRO_EMPLEADOR, listIn);
            if (resEmpleador != null && resEmpleador.length > 0 && resEmpleador[0] == 1) {
                out.add(new MensajeLegacy(1L, "OK", "UI_DETALLE_EMPLEADOR_ELIMINADO", "UI_DETALLE_EMPLEADOR_ELIMINADO"));
            }
            resCliente = LoteConteo.ejecutar(jdbc, SQL_ELIMINA_REGISTRO_CLIENTE, listIn);
            if (resCliente != null && resCliente.length > 0 && resCliente[0] == 1) {
                out.add(new MensajeLegacy(1L, "OK", "UI_DETALLE_CLIENTE_ASOCIADO_ELIMINADO", "UI_DETALLE_CLIENTE_ASOCIADO_ELIMINADO"));
            }
        } else {
            resCliente = LoteConteo.ejecutar(jdbc, SQL_ELIMINA_REGISTRO_CLIENTE, listIn);
            if (resCliente != null && resCliente.length > 0 && resCliente[0] == 1) {
                out.add(new MensajeLegacy(1L, "OK", "UI_DETALLE_CLIENTE_ELIMINADO", "UI_DETALLE_CLIENTE_ELIMINADO"));
                // LEGACY DEFECT kept (see README APV-C-450..458): the employer is not reset when no client was deleted
                idRecaudacionEmpleador = in.idRecaudacionEmpleador();
            }
        }

        List<ParametrosRecaudacionEstado> paramsCambioEstado = new ArrayList<>();
        paramsCambioEstado.add(new ParametrosRecaudacionEstado(in.idRecaudacion(), idRecaudacionEmpleador, 0L, "ING"));
        // the legacy ignores the answer of the state change
        cambiarEstadoRecaudacion.ejecutar(new CambiarEstadoRecaudacionEntrada(in.idEmpresa(), 0L, "APV", paramsCambioEstado));
        // legacy con.commit(): mid-flow commit on the connection of the call
        sql.commit();
        return out;
    }
}
