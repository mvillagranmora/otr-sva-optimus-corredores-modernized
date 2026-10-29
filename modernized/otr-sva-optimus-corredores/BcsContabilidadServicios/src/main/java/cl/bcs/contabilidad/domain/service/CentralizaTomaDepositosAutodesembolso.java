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
 * Centralization type CentralizaTomaDepositosAutodesembolso (legacy CentralizaTomaDepositosAutodesembolso.java). Characterization: CONT-F-211..CONT-F-222.
 *
 * <p>Runs the type's own legacy SQL (legacy lines :100, :107, :114) and turns each data row into the
 * TabDatosMovimiento entries of legacy lines 118-173.
 */
@Service("centralizaTomaDepositosAutodesembolso")
public class CentralizaTomaDepositosAutodesembolso extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaTomaDepositosAutodesembolso_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaTomaDepositosAutodesembolso_SelectEventos.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaTomaDepositosAutodesembolso_SelectDatosMovimiento.sql";

    /** Legacy CentralizaTomaDepositosAutodesembolso.java:118-173, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            texto("DocumentoIdentidad"),
            texto("Portafolio"),
            texto("CentroCosto"),
            texto("CodigoAgente"),
            texto("CodigoBolsa"),
            texto("CodigoIFRS"),
            texto("TipoInstrumento"),
            texto("CodigoSucursal"),
            texto("CodigoTipoOperacion"),
            vacio("CodSecuencia"),
            texto("DiasLiquidacion"),
            texto("DiasVencido"),
            vacio("DiasVencimiento"),
            texto("EmisorInstrumento"),
            texto("EstadoMovimiento"),
            texto("FamiliaInstrumento"),
            fecha("FechaEmisionDocumento"),
            fecha("FechaLiquidacionDocumento"),
            texto("IndicadorLiquidacion"),
            texto("IndicadorSimultanea"),
            texto("Nemo"),
            texto("ProductoMovimiento"),
            texto("RelacionadoEmpresa"),
            texto("RelacionCorredora"),
            texto("TipoEmisor"),
            texto("FamiliaTipoInstrumento"),
            texto("Relacion"),
            texto("ClaseEmisor"),
            texto("TipoCartera"),
            texto("TipoFamilia"),
            texto("TipoReajuste"),
            vacio("IdCartera"),
            texto("TipoDocumento"),
            texto("Concepto"),
            texto("Glosa"),
            fecha("FechaDeVencimiento"),
            fecha("FechaAnterior"),
            fechaHora("FechaActual"),
            texto("Moneda"),
            fecha("FechaLiquidacion"),
            texto("TipoPersona"),
            fecha("FechaOperacion"),
            texto("NombreCliente"),
            texto("DocumentoIdentidadCAVALI"),
            decimal("TasaMercadoActual"),
            decimal("PrecioMercadoActual"),
            decimal("TasaMercadoAnterior"),
            decimal("PrecioMercadoAnterior"),
            texto("TasaEmision"),
            decimal("TasaCompra"),
            decimal("PrecioCompra"),
            decimal("TasaCambio"));

    public CentralizaTomaDepositosAutodesembolso(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa(),
                        "IdTipoCentralizacion", ctx.idTipo())))
                .eventos(eventos(SELECT_EVENTOS, parametros(
                        "FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa(),
                        "IdTipoCentralizacion", ctx.idTipo())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO, parametros(
                        "FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa())), CAMPOS));
    }
}
