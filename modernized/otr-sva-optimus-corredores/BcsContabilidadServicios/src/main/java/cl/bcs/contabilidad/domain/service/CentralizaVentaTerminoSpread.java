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
 * Centralization type CentralizaVentaTerminoSpread (legacy CentralizaVentaTerminoSpread.java). Characterization: CONT-F-234..CONT-F-244.
 *
 * <p>Runs the type's own legacy SQL (legacy lines :81, :89, :97) and turns each data row into the
 * TabDatosMovimiento entries of legacy lines 101-131.
 */
@Service("centralizaVentaTerminoSpread")
public class CentralizaVentaTerminoSpread extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaVentaTerminoSpread_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaVentaTerminoSpread_SelectEventos.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaVentaTerminoSpread_SelectDatosMovimiento.sql";

    /** Legacy CentralizaVentaTerminoSpread.java:101-131, one entry per legacy add(...), in order. */
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
            texto("IdCartera"),
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
            texto("TipoPersona"),
            texto("TipoEmisor"),
            texto("Mesa"),
            valorDe("FolioOperacion"),
            texto("ClienteBancario"));

    public CentralizaVentaTerminoSpread(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable(),
                        "IdTipoCentralizacion", ctx.idTipo())))
                .eventos(eventos(SELECT_EVENTOS, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable(),
                        "IdTipoCentralizacion", ctx.idTipo(), "DocumentoIdentidad", ctx.documentoCorredora())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO, parametros(
                        "FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa(),
                        "DocumentoIdentidad", ctx.documentoCorredora(), "LocSistema", ctx.localizacion())), CAMPOS));
    }
}
