package cl.bcs.contabilidad.domain.service;

import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.soporte.Campo;
import cl.bcs.contabilidad.domain.service.soporte.Contexto;
import cl.bcs.contabilidad.domain.service.soporte.DatosMovimiento;
import cl.bcs.contabilidad.domain.service.soporte.Tablas;
import org.springframework.stereotype.Service;

import java.util.List;

import static cl.bcs.contabilidad.domain.service.soporte.Campo.*;

/**
 * Centralization type CentralizaVencimientoPactos (legacy CentralizaVencimientoPactos.java). Characterization: CONT-F-223..CONT-F-233.
 *
 * <p>Runs the type's own legacy SQL. CONTABILIDAD / DetalleEventoCentralizacionVP = "S" (any case, default "N")
 * switches the underlying-events SQL to its Detalle variant (:94-112); FechaAnterior is the previous business day
 * of the company country (:69-83).
 */
@Service("centralizaVencimientoPactos")
public class CentralizaVencimientoPactos extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaVencimientoPactos_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS_SUBYACENTE_DETALLE = "cl/bcs/contabilidad/sql/CentralizaVencimientoPactos_SelectEventosSubyacenteDetalle.sql";
    private static final String SELECT_EVENTOS_SUBYACENTE = "cl/bcs/contabilidad/sql/CentralizaVencimientoPactos_SelectEventosSubyacente.sql";
    private static final String SELECT_EVENTOS_OPERACION = "cl/bcs/contabilidad/sql/CentralizaVencimientoPactos_SelectEventosOperacion.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaVencimientoPactos_SelectDatosMovimiento.sql";

    /** Legacy CentralizaVencimientoPactos.java:124-148, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            valorDe("IdMovimiento"),
            texto("DocumentoIdentidad"),
            texto("NombreCliente"),
            texto("PortafolioCliente", "Portafolio"),
            texto("Moneda"),
            texto("Producto"),
            texto("ClaseInstrumento"),
            texto("TipoInstrumento"),
            texto("Nemo"),
            texto("Boveda"),
            texto("IdCartera"),
            texto("Emisor"),
            texto("IdTipoReajuste", "TipoReajuste"),
            texto("TipoEmisor"),
            texto("CodigoIFRS"),
            texto("TipoCartera"),
            texto("ClaseEmisor"),
            texto("Agente"),
            texto("NombreAgente"),
            texto("SucursalAgente"),
            texto("NombreSucursalAgente"),
            texto("CentroCostoAgente"),
            texto("Relacion"),
            texto("CompraVenta"),
            texto("Mesa"));

    public CentralizaVencimientoPactos(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        boolean detalle = "S".equalsIgnoreCase(ctx.parametro("CONTABILIDAD", "DetalleEventoCentralizacionVP", "N"));
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "p_IdEmpresa", ctx.idEmpresa(), "p_FechaContable", ctx.fechaContable(),
                        "p_IdTipoCentralizacion", ctx.idTipo())))
                .eventos(eventos(detalle ? SELECT_EVENTOS_SUBYACENTE_DETALLE : SELECT_EVENTOS_SUBYACENTE, parametros(
                        "p_IdEmpresa", ctx.idEmpresa(), "p_FechaContable", ctx.fechaContable(),
                        "p_IdTipoCentralizacion", ctx.idTipo(), "p_FechaAnterior", ctx.diaAnterior())))
                .eventos(eventos(SELECT_EVENTOS_OPERACION, parametros(
                        "p_IdEmpresa", ctx.idEmpresa(), "p_FechaContable", ctx.fechaContable(),
                        "p_IdTipoCentralizacion", ctx.idTipo())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO, parametros(
                        "p_IdEmpresa", ctx.idEmpresa(), "p_FechaContable", ctx.fechaContable())), CAMPOS));
    }
}
