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
 * Centralization type CentralizaCompraVentaPactos (legacy CentralizaCompraVentaPactos.java). Characterization: CONT-E-152..CONT-E-163.
 *
 * <p>Runs the type's own legacy SQL. MESA / TASARECOMPRAVENTACONPACTO (default "N") is passed as
 * IncluirSpreadVentaPacto to the underlying-events SQL (:86-90, :138-148); operation events come first.
 */
@Service("centralizaCompraVentaPactos")
public class CentralizaCompraVentaPactos extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaCompraVentaPactos_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS_OPERACION = "cl/bcs/contabilidad/sql/CentralizaCompraVentaPactos_SelectEventosOperacion.sql";
    private static final String SELECT_EVENTOS_SUBYACENTE = "cl/bcs/contabilidad/sql/CentralizaCompraVentaPactos_SelectEventosSubyacente.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaCompraVentaPactos_SelectDatosMovimiento.sql";

    /** Legacy CentralizaCompraVentaPactos.java:99-129, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            texto("DocumentoIdentidad"),
            texto("NombreCliente"),
            texto("CompraVenta"),
            texto("Moneda"),
            texto("Producto"),
            texto("ClaseInstrumento"),
            texto("TipoInstrumento"),
            texto("Nemo"),
            texto("Boveda"),
            valorDe("IdCartera"),
            texto("Emisor"),
            texto("TipoReajuste"),
            texto("CodigoIFRS"),
            texto("TipoCartera"),
            texto("ClaseEmisor"),
            texto("Agente"),
            texto("NombreAgente"),
            texto("SucursalAgente"),
            texto("NombreSucursalAgente"),
            texto("CentroCostoAgente"),
            valorDe("Relacion"),
            texto("Bolsa"),
            texto("TipoPersona"),
            texto("Concepto"),
            fecha("FechaLiquidacion"),
            fecha("FechaOperacion"),
            texto("TipoEmisor"),
            texto("Mesa"),
            valorDe("FolioOperacion"));

    public CentralizaCompraVentaPactos(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        // MESA / TASARECOMPRAVENTACONPACTO, "N" when absent (legacy obtenerParametroUsoTasaRecompraVentaConPacto, :138-148)
        String incluirSpreadVentaPacto = ctx.parametro("MESA", "TASARECOMPRAVENTACONPACTO", "N");
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo())))
                .eventos(eventos(SELECT_EVENTOS_OPERACION, parametros(
                        "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo())))
                .eventos(eventos(SELECT_EVENTOS_SUBYACENTE, parametros(
                        "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo(),
                        "IncluirSpreadVentaPacto", incluirSpreadVentaPacto)))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO, parametros(
                        "FechaContable", ctx.fechaContable())), CAMPOS));
    }
}
