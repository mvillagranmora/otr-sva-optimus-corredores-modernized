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
 * Centralization type CentralizaLiquidacionFacturas (legacy CentralizaLiquidacionFacturas.java). Characterization: CONT-F-023..CONT-F-032.
 *
 * <p>Runs the type's own legacy SQL (legacy lines :81, :89, :94) and turns each data row into the
 * TabDatosMovimiento entries of legacy lines 98-129.
 */
@Service("centralizaLiquidacionFacturas")
public class CentralizaLiquidacionFacturas extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaLiquidacionFacturas_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaLiquidacionFacturas_SelectEventos.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaLiquidacionFacturas_SelectDatosMovimiento.sql";

    /** Legacy CentralizaLiquidacionFacturas.java:98-129, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            texto("TipoDocumento"),
            valorDe("Folio"),
            texto("FechaDocumento"),
            texto("CompraVenta"),
            texto("Operacion"),
            texto("Producto"),
            texto("ClaseInstrumento"),
            texto("TipoInstrumento"),
            texto("MonedaComprobante"),
            texto("Bolsa"),
            texto("DocumentoIdentidad"),
            texto("NombreCliente"),
            texto("Portafolio"),
            texto("Concepto"),
            texto("NombreConcepto"),
            texto("Agente"),
            texto("NombreAgente"),
            texto("SucursalAgente"),
            texto("NombreSucursalAgente"),
            texto("CentroCostoAgente"),
            texto("EstadoSUNAT"),
            texto("DocumentoIdentidadCAVALI"),
            texto("EsExento"),
            texto("Moneda"),
            texto("MonedaLiquidacion"),
            texto("EsContraparte"),
            texto("Relacion"),
            texto("ClaseEmisor"),
            texto("Emisor"),
            texto("Nemotecnico"),
            texto("CondicionLiquidacion"),
            texto("CodigoIFRS"));

    public CentralizaLiquidacionFacturas(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo())))
                .eventos(eventos(SELECT_EVENTOS, parametros(
                        "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo(),
                        "Sistema", ctx.localizacion())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO, parametros(
                        "FechaContable", ctx.fechaContable())), CAMPOS));
    }
}
