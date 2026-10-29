package cl.bcs.apv.application.recaudacion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.ProcesarBonificacion;
import cl.bcs.apv.api.dto.procesarbonificacion.ProcesarBonificacionEntrada;
import cl.bcs.apv.api.dto.procesarbonificacion.ProcesarBonificacionResultado;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code ProcesarBonificacion} (family C): computes, per bonificacion of the saving year, the amount to bonify after the withdrawals and transfers
 * of the portfolios. Legacy {@code cl.bcs.APV.ProcesarBonificacion} (call and logica).
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProcesarBonificacionServicio implements ProcesarBonificacion {

    private static final String SQL_SELECT_BONIFICACION_A_PROCESAR = SqlLegacy.recurso("ConsultarBonificacionAProcesar");
    private static final String SQL_UPDATE_PROCESAR_BONIFICACION = SqlLegacy.recurso("ProcesarBonificacion");
    private static final String SQL_SELECT_CARTERAS_RETIRADAS = SqlLegacy.recurso("ConsultarCarterasRetiradas");
    private static final String SQL_SELECT_CARTERAS_TRASPASOS = SqlLegacy.recurso("ConsultarCarterasTraspaso");
    private static final String SQL_SELECT_CARTERAS_TRASPASOS_ANIO_AHORRO = SqlLegacy.recurso("ConsultarCarterasTraspasoAhorro");

    private final SqlLegacy sql;
    private final ConfiguracionSistema configuracion;

    public ProcesarBonificacionServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores) {
        this.sql = new SqlLegacy(jdbc);
        this.configuracion = configuracion;
    }

    public static class BonificacionAProcesar {
        public Long IdCliente;
        public Long AnoAhorro;
        public BigDecimal AhorrosDJ;
        public BigDecimal RetirosDJ;
        public Long IdBonificacion;
        public BigDecimal MontoBonificacionAfiliadoNeto;
        public BigDecimal MontoReajustadoPagadoAfiliado;
        public String IdEstado;
    }

    public static class BonificacionUpdate {
        public BigDecimal MontoCalculado;
        public BigDecimal MontoTraspasoEgreso;
        public BigDecimal MontoEgreso;
        public BigDecimal PorcentajeTraspasoEgreso;
        public BigDecimal PorcentajeEgreso;
        public BigDecimal MontoTraspasoDevolucion;
        public BigDecimal MontoEgresoDevolucion;
        public Long IdBonificacion;
        public Long IdEmpresa;
    }

    public static class Cartera {
        public BigDecimal Cantidad;
        public BigDecimal PrecioCompra;
        public Long IdCliente;
    }

    @Override
    public ProcesarBonificacionResultado ejecutar(ProcesarBonificacionEntrada in) {
        try {
            List<MensajeLegacy> mensajes = logica(in);
            return new ProcesarBonificacionResultado(MensajeLegacy.aMensajes(mensajes), MensajeLegacy.contarErrores(mensajes));
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    private List<MensajeLegacy> logica(ProcesarBonificacionEntrada in) throws Exception {
        List<MensajeLegacy> salida = new ArrayList<>();
        String fechaSistema = Plataforma.fechaSistema(configuracion, in.idEmpresa());

        Map<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", in.idEmpresa());
        params.put("AnoAhorro", in.anoAhorro());
        List<BonificacionAProcesar> listaBonificacion = sql.consultar(SQL_SELECT_BONIFICACION_A_PROCESAR, params, BonificacionAProcesar.class);

        List<Long> idClientes = new ArrayList<>();
        for (BonificacionAProcesar fila : listaBonificacion) {
            if (!idClientes.contains(fila.IdCliente)) {
                idClientes.add(fila.IdCliente);
            }
        }

        params.clear();
        params.put("IdCliente", idClientes);
        params.put("FechaDesdeAhorro", in.anoAhorro() + "-01-01");
        params.put("FechaHastaAhorro", in.anoAhorro() + "-12-31");
        List<Cartera> listaCarterasRetiradasTraspasoAnioAhorro = sql.consultar(SQL_SELECT_CARTERAS_TRASPASOS_ANIO_AHORRO, params, Cartera.class);
        params.put("FechaHasta", fechaSistema);
        List<Cartera> listaCarterasRetiradasAnioProceso = sql.consultar(SQL_SELECT_CARTERAS_RETIRADAS, params, Cartera.class);
        params.put("FechaDesde", (in.anoAhorro() + 1) + "-01-01");
        List<Cartera> listaCarterasRetiradasTraspasoAnioProceso = sql.consultar(SQL_SELECT_CARTERAS_TRASPASOS, params, Cartera.class);

        List<BonificacionUpdate> listBonUpdate = new ArrayList<>();
        for (BonificacionAProcesar fila : listaBonificacion) {
            BigDecimal montoRetirada = BigDecimal.ZERO;
            BigDecimal montoRetiradasTraspaso = BigDecimal.ZERO;
            BigDecimal montoAhorroNeto = fila.AhorrosDJ.subtract(fila.RetirosDJ, MathContext.DECIMAL128);

            for (Cartera cartera : listaCarterasRetiradasAnioProceso) {
                if (fila.IdCliente.equals(cartera.IdCliente)) {
                    montoRetirada = montoRetirada.add(cartera.Cantidad.multiply(cartera.PrecioCompra, MathContext.DECIMAL128), MathContext.DECIMAL128);
                }
            }
            for (Cartera cartera : listaCarterasRetiradasTraspasoAnioProceso) {
                if (fila.IdCliente.equals(cartera.IdCliente)) {
                    montoRetiradasTraspaso = montoRetiradasTraspaso.add(cartera.Cantidad.multiply(cartera.PrecioCompra, MathContext.DECIMAL128), MathContext.DECIMAL128);
                }
            }
            for (Cartera cartera : listaCarterasRetiradasTraspasoAnioAhorro) {
                if (fila.IdCliente.equals(cartera.IdCliente)) {
                    montoRetiradasTraspaso = montoRetiradasTraspaso.add(cartera.Cantidad.multiply(cartera.PrecioCompra, MathContext.DECIMAL128), MathContext.DECIMAL128);
                }
            }

            // LEGACY DEFECT kept (see README APV-C-347): net savings AhorrosDJ - RetirosDJ = 0 divides by zero (ArithmeticException)
            BigDecimal montoRestarTraspaso = montoRetiradasTraspaso.multiply(fila.MontoReajustadoPagadoAfiliado).divide(montoAhorroNeto, MathContext.DECIMAL128);
            BigDecimal montoRestarRetiro = montoRetirada.multiply(fila.MontoReajustadoPagadoAfiliado).divide(montoAhorroNeto, MathContext.DECIMAL128);
            BigDecimal montoARestar = montoRestarTraspaso.add(montoRestarRetiro, MathContext.DECIMAL128);

            BonificacionUpdate bonUpdate = new BonificacionUpdate();
            bonUpdate.IdBonificacion = fila.IdBonificacion;
            bonUpdate.PorcentajeEgreso = montoRetirada.multiply(new BigDecimal("100"), MathContext.DECIMAL128).divide(montoAhorroNeto, MathContext.DECIMAL128);
            bonUpdate.PorcentajeTraspasoEgreso = montoRetiradasTraspaso.multiply(new BigDecimal("100"), MathContext.DECIMAL128).divide(montoAhorroNeto, MathContext.DECIMAL128);
            bonUpdate.MontoEgreso = montoRetirada;
            bonUpdate.MontoTraspasoEgreso = montoRetiradasTraspaso;
            bonUpdate.MontoTraspasoDevolucion = montoRestarTraspaso;
            bonUpdate.MontoEgresoDevolucion = montoRestarRetiro;
            bonUpdate.IdEmpresa = in.idEmpresa();
            // LEGACY DEFECT kept (see README APV-C-348): MontoCalculado can be negative when the withdrawals exceed the reajustado amount
            bonUpdate.MontoCalculado = fila.MontoReajustadoPagadoAfiliado.subtract(montoARestar, MathContext.DECIMAL128);
            listBonUpdate.add(bonUpdate);
        }

        int[] resUpdate = BonificacionLote.lote(sql, SQL_UPDATE_PROCESAR_BONIFICACION, listBonUpdate);

        if (resUpdate != null && resUpdate.length > 0 && resUpdate[0] == 1) {
            salida.add(new MensajeLegacy(1L, MensajeLegacy.OK, "UI_PROCESO_EXITOSO", "UI_PROCESO_EXITOSO"));
        } else {
            salida.add(new MensajeLegacy(1L, MensajeLegacy.ERROR, "NO_SE_ENCONTRARON_REGISTROS_PARA_PROCESAR",
                    "No se encontraron registros que cumplan con las condiciones para procesar"));
        }
        return salida;
    }
}
