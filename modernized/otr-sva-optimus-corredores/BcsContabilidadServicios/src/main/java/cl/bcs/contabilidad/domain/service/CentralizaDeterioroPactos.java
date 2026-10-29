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
 * Centralization type CentralizaDeterioroPactos (legacy CentralizaDeterioroPactos.java). Characterization: CONT-E-199..CONT-E-207.
 *
 * <p>Runs the type's own legacy SQL (legacy lines :74, :76, :80) and turns each data row into the
 * TabDatosMovimiento entries of legacy lines 85-116.
 */
@Service("centralizaDeterioroPactos")
public class CentralizaDeterioroPactos extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaDeterioroPactos_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS_OPERACION = "cl/bcs/contabilidad/sql/CentralizaDeterioroPactos_SelectEventosOperacion.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaDeterioroPactos_SelectDatosMovimiento.sql";

    /** Legacy CentralizaDeterioroPactos.java:85-116, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            valorDe("IdMovimiento"),
            texto("DocumentoIdentidad"),
            texto("NombreCliente"),
            texto("PortafolioCliente", "Portafolio"),
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
            valorDe("FolioOperacion"),
            valorDe("Fase"));

    public CentralizaDeterioroPactos(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo())))
                .eventos(eventos(SELECT_EVENTOS_OPERACION, parametros(
                        "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO, parametros(
                        "FechaContable", ctx.fechaContable())), CAMPOS));
    }
}
