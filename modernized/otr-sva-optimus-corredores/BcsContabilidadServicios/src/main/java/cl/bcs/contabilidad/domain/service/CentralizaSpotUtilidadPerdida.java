package cl.bcs.contabilidad.domain.service;

import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.soporte.Campo;
import cl.bcs.contabilidad.domain.service.soporte.Contexto;
import cl.bcs.contabilidad.domain.service.soporte.DatosMovimiento;
import cl.bcs.contabilidad.domain.service.soporte.Fila;
import cl.bcs.contabilidad.domain.service.soporte.Tablas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static cl.bcs.contabilidad.domain.service.soporte.Campo.*;

/**
 * Centralization type CentralizaSpotUtilidadPerdida (legacy CentralizaSpotUtilidadPerdida.java). Characterization: CONT-F-155..CONT-F-166.
 *
 * <p>Runs the type's own legacy SQL. Takes the lock "<IdTipoCentralizacion>_<FechaContable>" first: an existing lock
 * rejects the run with 1 error UI_CENTRALIZACION_CONTABILIZADA_ANULAR (:98-108); otherwise the lock is created, the
 * tables read, and the lock released at the end (:111-121, :190-195). Any failure in between is logged and
 * swallowed, keeping the tables read so far and no P_OUT_RESULTADO (:187-189).
 */
@Service("centralizaSpotUtilidadPerdida")
public class CentralizaSpotUtilidadPerdida extends CentralizadorSql {

    private static final Logger log = LoggerFactory.getLogger(CentralizaSpotUtilidadPerdida.class);

    private static final String CONSULTA_TABLA_BLOQUEO = "cl/bcs/contabilidad/sql/CentralizaSpotUtilidadPerdida_ConsultaTablaBloqueo.sql";
    private static final String SELECT_TIPO_CENTRALIZACION = "cl/bcs/contabilidad/sql/CentralizaGenerico_SelectTipoCentralizacion.sql";
    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaSpotUtilidadPerdida_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaSpotUtilidadPerdida_SelectEventos.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaSpotUtilidadPerdida_SelectDatosMovimiento.sql";

    /** Legacy CentralizaSpotUtilidadPerdida.java:155-181, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            texto("DocumentoIdentidad"),
            texto("NombreCliente"),
            texto("Portafolio"),
            texto("Moneda"),
            texto("Producto"),
            texto("ClaseInstrumento"),
            texto("TipoInstrumento"),
            texto("Nemo"),
            texto("Boveda"),
            texto("IdCartera", "Boveda"),
            texto("Emisor"),
            texto("TipoReajuste"),
            texto("CodigoIFRS"),
            texto("Agente"),
            texto("NombreAgente"),
            texto("SucursalAgente"),
            texto("NombreSucursalAgente"),
            texto("Canal"),
            texto("Relacion"),
            texto("Bolsa"),
            texto("CentroCostoAgente"),
            texto("TipoComprobante"),
            texto("CompraVenta"),
            texto("TipoPersona"),
            texto("FechaOperacion"),
            texto("ClienteBancario"),
            texto("OperacionNoFacturable"));

    public CentralizaSpotUtilidadPerdida(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        Tablas t = new Tablas();
        // one run per type and date at a time: lock id "<IdTipoCentralizacion>_<FechaContable>" (:88-96)
        String idBloqueo = ctx.idTipo() + "_" + ctx.fechaContable();
        boolean rechazado = false;
        try {
            if (!filas(CONSULTA_TABLA_BLOQUEO, parametros("IdBloqueoCentralizacion", idBloqueo)).isEmpty()) {
                // already running or centralized (:99-108); the lock belongs to the other run and is kept
                String nombre = filas(SELECT_TIPO_CENTRALIZACION, parametros("IdTipoCentralizacion", ctx.idTipo()))
                        .stream().findFirst().map(f -> f.texto("Nombre")).orElseThrow();
                rechazado = true;
                return t.error("UI_CENTRALIZACION_CONTABILIZADA_ANULAR", Map.of("Centralizacion", String.valueOf(nombre)));
            }
            repository.crearBloqueoCentralizacion(idBloqueo, ctx.idTipo(), ctx.fechaContable());

            String moneda = ctx.monedaSistema();
            t.movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                    "FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa(),
                    "IdTipoCentralizacion", ctx.idTipo(), "MonedaSistema", moneda)));
            t.eventos(eventos(SELECT_EVENTOS, parametros(
                    "FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa(),
                    "IdTipoCentralizacion", ctx.idTipo(), "MonedaSistema", moneda)));
            for (Fila f : filas(SELECT_DATOS_MOVIMIENTO, parametros(
                    "FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa(), "MonedaSistema", moneda))) {
                t.datos(DatosMovimiento.de(ctx.idTipo(), f.entero("IdMovimiento"), f, CAMPOS));
            }
            return t;
        } catch (RuntimeException e) {
            // legacy :187-189 logs and swallows any failure: the tables filled so far are kept, no P_OUT_RESULTADO
            log.error("CentralizaSpotUtilidadPerdida: tipo {} fecha {} failed", ctx.idTipo(), ctx.fechaContable(), e);
            return t.sinResultados();
        } finally {
            if (!rechazado) {
                repository.eliminarBloqueoCentralizacion(idBloqueo, ctx.idTipo(), ctx.fechaContable()); // :190-195
            }
        }
    }
}
