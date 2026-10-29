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
 * Centralization type CentralizaRFIFVencimientos (legacy CentralizaRFIFVencimientos.java). Characterization: CONT-F-112..CONT-F-122.
 *
 * <p>Runs the type's own legacy SQL (legacy lines :111, :119, :127) and turns each data row into the
 * TabDatosMovimiento entries of legacy lines 132-191.
 */
@Service("centralizaRFIFVencimientos")
public class CentralizaRFIFVencimientos extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaRFIFVencimientos_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaRFIFVencimientos_SelectEventos.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaRFIFVencimientos_SelectDatosMovimiento.sql";

    /** Legacy CentralizaRFIFVencimientos.java:132-191, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            texto("DocumentoIdentidad"),
            texto("Portafolio"),
            texto("CentroCosto"),
            texto("CodigoAgente"),
            texto("CodigoBolsa"),
            texto("CodigoIFRS"),
            texto("ClaseInstrumento"),
            texto("TipoInstrumento"),
            texto("CodigoSucursal"),
            texto("CodigoTipoOperacion"),
            vacio("CodSecuencia"),
            texto("DocumentoIdentidadCAVALI"),
            vacio("DiasLiquidacion"),
            vacio("DiasVencido"),
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
            texto("FamiliaTipoInstrumento"),
            texto("Relacion"),
            texto("ClaseEmisor"),
            texto("TipoCartera"),
            texto("TipoFamilia"),
            texto("TipoReajuste"),
            vacio("IdCartera"),
            texto("TipoDocumento"),
            texto("VentaCorta"),
            texto("TipoCmbteCont"),
            texto("FolioCmbteCont"),
            texto("Concepto"),
            texto("Glosa"),
            fecha("FechaDeVencimiento"),
            fecha("FechaAnterior"),
            vacio("TasaMercadoActual"),
            vacio("PrecioMercadoActual"),
            vacio("TasaMercadoAnterior"),
            vacio("PrecioMercadoAnterior"),
            decimal("TasaCompra"),
            decimal("PrecioCompra"),
            texto("Moneda"),
            decimal("TasaCambio"),
            fecha("FechaLiquidacion"),
            texto("TipoPersona"),
            fecha("FechaOperacion"),
            texto("NombreCliente"),
            texto("OpCentros"),
            texto("TipoEmisor"),
            texto("Mesa"),
            texto("ClienteBancario"));

    public CentralizaRFIFVencimientos(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo())))
                .eventos(eventos(SELECT_EVENTOS, parametros(
                        "FechaContable", ctx.fechaContable(), "IdTipoCentralizacion", ctx.idTipo())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable(),
                        "DocumentoIdentidad", ctx.documentoCorredora(), "LocSistema", ctx.localizacion())), CAMPOS));
    }
}
