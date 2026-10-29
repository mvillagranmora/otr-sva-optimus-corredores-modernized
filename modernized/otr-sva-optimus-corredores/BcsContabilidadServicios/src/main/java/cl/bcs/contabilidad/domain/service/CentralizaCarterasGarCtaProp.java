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
 * Centralization type CentralizaCarterasGarCtaProp (legacy CentralizaCarterasGarCtaProp.java). Characterization: CONT-E-111..CONT-E-119.
 *
 * <p>Runs the type's own legacy SQL (legacy lines :90, :98, :106) and turns each data row into the
 * TabDatosMovimiento entries of legacy lines 110-146.
 */
@Service("centralizaCarterasGarCtaProp")
public class CentralizaCarterasGarCtaProp extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS_GAR_CTA_PROP = "cl/bcs/contabilidad/sql/CentralizaCarteras_SelectMovimientosGarCtaProp.sql";
    private static final String SELECT_EVENTOS_GAR_CTA_PROP = "cl/bcs/contabilidad/sql/CentralizaCarteras_SelectEventosGarCtaProp.sql";
    private static final String SELECT_DATOS_MOVIMIENTO_GAR_CTA_PROP = "cl/bcs/contabilidad/sql/CentralizaCarteras_SelectDatosMovimientoGarCtaProp.sql";

    /** Legacy CentralizaCarterasGarCtaProp.java:110-146, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            texto("DocumentoIdentidad"),
            texto("NombreCliente"),
            texto("Portafolio"),
            valorDe("Folio"),
            texto("DocumentoIdentidadCAVALI"),
            texto("Producto"),
            texto("ClaseInstrumento"),
            texto("TipoInstrumento"),
            texto("IdTipoInstrumentoGarantizado"),
            valorDe("IdOrigenGarantia"),
            texto("Nemo"),
            texto("Boveda"),
            valorDe("IdGarantia"),
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
            texto("Concepto"),
            texto("NombreConcepto"),
            texto("TipoMovimiento"),
            texto("Moneda"),
            fecha("FechaActual"),
            decimal("TasaCambio"),
            valorDe("Relacion"),
            texto("Bolsa"),
            fecha("FechaLiquidacion"),
            texto("TipoPersona"),
            fecha("FechaOperacion"));

    public CentralizaCarterasGarCtaProp(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS_GAR_CTA_PROP, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable(),
                        "IdTipoCentralizacion", ctx.idTipo(), "DocumentoIdentidad", ctx.documentoCorredora())))
                .eventos(eventos(SELECT_EVENTOS_GAR_CTA_PROP, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable(),
                        "IdTipoCentralizacion", ctx.idTipo(), "DocumentoIdentidad", ctx.documentoCorredora())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO_GAR_CTA_PROP, parametros(
                        "FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa(),
                        "DocumentoIdentidad", ctx.documentoCorredora(), "LocSistema", ctx.localizacion())), CAMPOS));
    }
}
