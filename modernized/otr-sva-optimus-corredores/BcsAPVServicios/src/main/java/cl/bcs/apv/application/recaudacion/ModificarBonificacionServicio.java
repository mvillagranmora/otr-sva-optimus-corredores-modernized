package cl.bcs.apv.application.recaudacion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.ModificarBonificacion;
import cl.bcs.apv.api.dto.modificarbonificacion.Bonificacion;
import cl.bcs.apv.api.dto.modificarbonificacion.ModificarBonificacionEntrada;
import cl.bcs.apv.api.dto.modificarbonificacion.ModificarBonificacionResultado;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code ModificarBonificacion} (family C): sets the amount to bonify of each bonificacion.
 * Legacy {@code cl.bcs.APV.ModificarBonificacion} (call and logica).
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ModificarBonificacionServicio implements ModificarBonificacion {

    private static final String SQL_UPDATE_BONIFICACION = SqlLegacy.recurso("ModificarBonificacion");

    private final SqlLegacy sql;

    public ModificarBonificacionServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores) {
        this.sql = new SqlLegacy(jdbc);
    }

    /** Row of the update statement (public fields are the SQL parameter names). */
    public static class Fila {
        public Long IdBonificacion;
        public BigDecimal MontoABonificar;
    }

    @Override
    public ModificarBonificacionResultado ejecutar(ModificarBonificacionEntrada in) {
        try {
            List<MensajeLegacy> mensajes = new ArrayList<>();
            Long contadorErrores = null; // legacy call() does NOT recount: the counter is what the last row left (null with no rows)
            // LEGACY DEFECT kept (see README APV-C-352): ONE message instance is shared by every list entry, so all entries show the LAST row's result
            MensajeLegacy msg = new MensajeLegacy();
            for (Bonificacion fila : in.parametros()) {
                Fila bon = new Fila();
                bon.IdBonificacion = fila.idBonificacion();
                bon.MontoABonificar = fila.montoABonificar();
                int[] res = BonificacionLote.lote(sql, SQL_UPDATE_BONIFICACION, List.of(bon));
                if (res != null && res.length > 0 && res[0] == 1) {
                    msg.id = 1L;
                    msg.tipo = "APV";
                    msg.codigo = "OK";
                    msg.descripcion = "Bonificación Modificada";
                    mensajes.add(msg);
                    contadorErrores = 0L;
                } else {
                    msg.id = 1L;
                    msg.tipo = "APV";
                    msg.codigo = "ERROR";
                    msg.descripcion = "Error al Modificar Bonificación";
                    mensajes.add(msg);
                    contadorErrores = 1L;
                }
            }
            return new ModificarBonificacionResultado(MensajeLegacy.aMensajes(mensajes), contadorErrores);
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }
}
