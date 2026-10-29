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
 * Centralization type CentralizaCarterasRFCompraTermino (legacy CentralizaCarterasRFCompraTermino.java). Characterization: CONT-E-120..CONT-E-128.
 *
 * <p>Runs the type's own legacy SQL (legacy lines :83, :91, :98) and turns each data row into the
 * TabDatosMovimiento entries of legacy lines 102-133.
 */
@Service("centralizaCarterasRFCompraTermino")
public class CentralizaCarterasRFCompraTermino extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS_RFCOMPRA_TERMINO = "cl/bcs/contabilidad/sql/CentralizaCarteras_SelectMovimientosRFCompraTermino.sql";
    private static final String SELECT_EVENTOS_RFCOMPRA_TERMINO = "cl/bcs/contabilidad/sql/CentralizaCarteras_SelectEventosRFCompraTermino.sql";
    private static final String SELECT_DATOS_MOVIMIENTO_RFCOMPRA_TERMINO = "cl/bcs/contabilidad/sql/CentralizaCarteras_SelectDatosMovimientoRFCompraTermino.sql";

    /** Legacy CentralizaCarterasRFCompraTermino.java:102-133, one entry per legacy add(...), in order. */
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
            fecha("FechaLiquidacion"),
            fecha("FechaOperacion"),
            texto("Concepto"),
            texto("TipoPersona"),
            texto("TipoEmisor"),
            texto("Mesa"),
            valorDe("FolioOperacion"),
            texto("ClienteBancario"));

    public CentralizaCarterasRFCompraTermino(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS_RFCOMPRA_TERMINO, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable(),
                        "IdTipoCentralizacion", ctx.idTipo(), "DocumentoIdentidad", ctx.documentoCorredora())))
                .eventos(eventos(SELECT_EVENTOS_RFCOMPRA_TERMINO, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable(),
                        "IdTipoCentralizacion", ctx.idTipo(), "DocumentoIdentidad", ctx.documentoCorredora())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO_RFCOMPRA_TERMINO, parametros(
                        "FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa(),
                        "DocumentoIdentidad", ctx.documentoCorredora())), CAMPOS));
    }
}
