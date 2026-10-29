package cl.bcs.apv.application.recaudacion;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CambiarEstadoRecaudacion;
import cl.bcs.apv.api.dto.cambiarestadorecaudacion.CambiarEstadoRecaudacionEntrada;
import cl.bcs.apv.api.dto.cambiarestadorecaudacion.CambiarEstadoRecaudacionResultado;
import cl.bcs.apv.api.dto.cambiarestadorecaudacion.ParametrosRecaudacionEstado;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code CambiarEstadoRecaudacion} (family C): moves the state of the deposit, employer and client rows of each requested line.
 * Legacy {@code cl.bcs.APV.CambiarEstadoRecaudacion} (call and logica).
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CambiarEstadoRecaudacionServicio implements CambiarEstadoRecaudacion {

    private static final String SQL_CAMBIA_ESTADO_RECAUDACION = SqlLegacy.recurso("Deposito_Update");
    private static final String SQL_CAMBIA_ESTADO_EMPLEADOR = SqlLegacy.recurso("DetalleEmpleador_CambiaEstado");
    private static final String SQL_CAMBIA_ESTADO_CLIENTE = SqlLegacy.recurso("DetalleCliente_CambiaEstado");

    private final SqlLegacy sql;

    public CambiarEstadoRecaudacionServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores) {
        this.sql = new SqlLegacy(jdbc);
    }

    @Override
    public CambiarEstadoRecaudacionResultado ejecutar(CambiarEstadoRecaudacionEntrada in) {
        try {
            List<MensajeLegacy> mensajes = logica(in);
            return new CambiarEstadoRecaudacionResultado(MensajeLegacy.aMensajes(mensajes), MensajeLegacy.contarErrores(mensajes));
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    private List<MensajeLegacy> logica(CambiarEstadoRecaudacionEntrada in) throws Exception {
        List<MensajeLegacy> salida = new ArrayList<>();
        for (ParametrosRecaudacionEstado fila : in.parametros()) {
            // Estado Recaudacion
            if (fila.folioRecaudacion() > 0) {
                CambiaEstadoFilas.Recaudacion rec = new CambiaEstadoFilas.Recaudacion();
                rec.IdRecaudacion = fila.folioRecaudacion();
                rec.IdEstado = fila.idEstado();
                rec.IdEmpresa = in.idEmpresa();
                sql.lote(SQL_CAMBIA_ESTADO_RECAUDACION, List.of(rec));
            }
            // Estado detalle empleador
            if (fila.idRecaudacionEmpleador() > 0) {
                CambiaEstadoFilas.DetalleEmpleador emp = new CambiaEstadoFilas.DetalleEmpleador();
                emp.IdRecaudacionEmpleador = fila.idRecaudacionEmpleador();
                emp.IdEstado = fila.idEstado();
                emp.IdEmpresa = in.idEmpresa();
                sql.lote(SQL_CAMBIA_ESTADO_EMPLEADOR, List.of(emp));
            }
            // Estado detalle cliente
            if (fila.idRecaudacionCliente() > 0) {
                CambiaEstadoFilas.DetalleCliente cli = new CambiaEstadoFilas.DetalleCliente();
                cli.IdRecaudacionCliente = fila.idRecaudacionCliente();
                cli.IdEstado = fila.idEstado();
                cli.IdEmpresa = in.idEmpresa();
                sql.lote(SQL_CAMBIA_ESTADO_CLIENTE, List.of(cli));
            }
        }
        salida.add(new MensajeLegacy(1L, MensajeLegacy.OK, "OK", "Proceso ejecutado correctamente"));
        return salida;
    }
}
