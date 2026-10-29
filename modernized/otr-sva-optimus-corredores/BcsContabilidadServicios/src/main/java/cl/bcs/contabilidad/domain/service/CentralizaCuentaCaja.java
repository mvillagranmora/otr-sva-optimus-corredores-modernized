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
 * Centralization type CentralizaCuentaCaja (legacy CentralizaCuentaCaja.java). Characterization: CONT-E-182..CONT-E-189.
 *
 * <p>Runs the type's own legacy SQL (legacy lines :71, :77, :84) and turns each data row into the
 * TabDatosMovimiento entries of legacy lines 89-114.
 */
@Service("centralizaCuentaCaja")
public class CentralizaCuentaCaja extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaCuentaCaja_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaCuentaCaja_SelectEventos.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaCuentaCaja_SelectDatosMovimiento.sql";

    /** Legacy CentralizaCuentaCaja.java:89-114, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            texto("TipoDocumento"),
            valorDe("Folio"),
            texto("FechaDocumento"),
            valorDe("IdEmpresa"),
            texto("Fecha"),
            texto("DocumentoIdentidad"),
            texto("Portafolio"),
            texto("Concepto"),
            texto("NombreConcepto"),
            texto("NombreCliente"),
            texto("Moneda"),
            texto("Agente"),
            texto("NombreAgente"),
            valorDe("SucursalAgente"),
            texto("NombreSucursalAgente"),
            texto("CentroCostoAgente"),
            texto("Producto"),
            texto("Relacion"),
            texto("Bolsa"),
            texto("TipoPersona"),
            texto("Nemo"),
            texto("DocumentoIdentidadCAVALI"),
            valorDe("Dias"),
            texto("TipoCmbte"),
            texto("ClienteBancario"));

    public CentralizaCuentaCaja(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa(),
                        "IdTipoCentralizacion", ctx.idTipo())))
                .eventos(eventos(SELECT_EVENTOS, parametros(
                        "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO, parametros(
                        "FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa(),
                        "LocSistema", ctx.localizacion())), CAMPOS));
    }
}
