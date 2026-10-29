package cl.bcs.apv.application.recaudacion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.AsignarDepositoEmpleador;
import cl.bcs.apv.api.dto.asignardepositoempleador.AsignarDepositoEmpleadorEntrada;
import cl.bcs.apv.api.dto.asignardepositoempleador.AsignarDepositoEmpleadorResultado;
import cl.bcs.apv.api.dto.asignardepositoempleador.DepositoEmpleador;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code AsignarDepositoEmpleador} (family C): assigns an employer detail to a deposit while the deposit amount allows it.
 * Legacy {@code cl.bcs.APV.AsignarDepositoEmpleador} (call and logica).
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AsignarDepositoEmpleadorServicio implements AsignarDepositoEmpleador {

    private static final String SQL_ASIGNAR_DEPOSITO_EMPLEADOR = SqlLegacy.recurso("AsignarDepositoEmpleador");
    private static final String SQL_TOTAL_DEPOSITO_EMPLEADOR = SqlLegacy.recurso("TotalDepositoEmpleador");

    private final NamedParameterJdbcTemplate jdbc;
    private final SqlLegacy sql;

    public AsignarDepositoEmpleadorServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.sql = new SqlLegacy(jdbc);
    }

    /** Parameter row of the assignment statement (legacy {@code AsignarDepositoEmpleador.DepositoEmpleador}). */
    public static class DepositoEmpleadorFila {
        public Long IdRecaudacion;
        public double MontoDeposito;
        public Long IdRecaudacionEmpleador;
        public double MontoEmpleador;
    }

    /** Row of the employer total query. */
    public static class TotalDepositoEmpleador {
        public BigDecimal Total;
    }

    @Override
    public AsignarDepositoEmpleadorResultado ejecutar(AsignarDepositoEmpleadorEntrada in) {
        try {
            List<MensajeLegacy> mensajes = logica(in);
            return new AsignarDepositoEmpleadorResultado(MensajeLegacy.aMensajes(mensajes), MensajeLegacy.contarErrores(mensajes));
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    private List<MensajeLegacy> logica(AsignarDepositoEmpleadorEntrada in) throws Exception {
        List<MensajeLegacy> salida = new ArrayList<>();
        // LEGACY DEFECT kept (see README APV-C-460..467): one MensajeSalida instance (type "APV") is shared by every entry, so the batch shows the LAST row's result
        MensajeLegacy msg = new MensajeLegacy();

        for (DepositoEmpleador fila : in.parametros()) {
            HashMap<String, Object> paramsDepositoEmpleador = new HashMap<>();
            paramsDepositoEmpleador.put("IdRecaudacion", fila.idRecaudacion());
            List<TotalDepositoEmpleador> totalDepositoEmpleador = sql.consultar(SQL_TOTAL_DEPOSITO_EMPLEADOR, paramsDepositoEmpleador,
                    TotalDepositoEmpleador.class);
            BigDecimal total = totalDepositoEmpleador.get(0).Total;
            double totalDouble = total.doubleValue();

            if (totalDouble >= 0) {
                if (fila.montoDeposito() - totalDouble > 0) {
                    if (totalDouble + fila.montoEmpleador() <= fila.montoDeposito()) {
                        DepositoEmpleadorFila de = new DepositoEmpleadorFila();
                        de.IdRecaudacion = fila.idRecaudacion();
                        de.IdRecaudacionEmpleador = fila.idRecaudacionEmpleador();
                        de.MontoDeposito = fila.montoDeposito();
                        de.MontoEmpleador = fila.montoEmpleador();
                        List<DepositoEmpleadorFila> paramsFila = new ArrayList<>();
                        paramsFila.add(de);
                        int[] res = LoteConteo.ejecutar(jdbc, SQL_ASIGNAR_DEPOSITO_EMPLEADOR, paramsFila);
                        if (res != null && res.length > 0 && res[0] == 1) {
                            msg.id = 1L;
                            msg.tipo = "APV";
                            msg.codigo = "OK";
                            msg.descripcion = "Asignación exitosa";
                            salida.add(msg);
                        } else {
                            msg.id = 2L;
                            msg.tipo = "APV";
                            msg.codigo = "ERROR";
                            msg.descripcion = "Problemas al Conciliar";
                            salida.add(msg);
                        }
                    } else {
                        msg.id = 3L;
                        msg.tipo = "APV";
                        msg.codigo = "ERROR";
                        msg.descripcion = "Monto Empleador Excede el Monto Depósito Disponible";
                        salida.add(msg);
                    }
                } else {
                    msg.id = 4L;
                    msg.tipo = "APV";
                    msg.codigo = "ERROR";
                    msg.descripcion = "Monto Empleador Excede el Monto Depósito Disponible";
                    salida.add(msg);
                }
            }
        }
        return salida;
    }
}
