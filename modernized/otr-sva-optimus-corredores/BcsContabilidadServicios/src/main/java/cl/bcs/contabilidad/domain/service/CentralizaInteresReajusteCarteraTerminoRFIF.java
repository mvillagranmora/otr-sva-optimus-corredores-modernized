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
 * Centralization type CentralizaInteresReajusteCarteraTerminoRFIF (legacy CentralizaInteresReajusteCarteraTerminoRFIF.java). Characterization: CONT-F-001..CONT-F-011.
 *
 * <p>Runs the type's own legacy SQL (legacy lines :81, :89, :98) and turns each data row into the
 * TabDatosMovimiento entries of legacy lines 101-133.
 */
@Service("centralizaInteresReajusteCarteraTerminoRFIF")
public class CentralizaInteresReajusteCarteraTerminoRFIF extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaInteresReajusteCarteraTerminoRFIF_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaInteresReajusteCarteraTerminoRFIF_SelectEventos.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaInteresReajusteCarteraTerminoRFIF_SelectDatosMovimiento.sql";

    /** Legacy CentralizaInteresReajusteCarteraTerminoRFIF.java:101-133, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            valorDe("IdMovimiento"),
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
            texto("Relacion"),
            texto("Bolsa"),
            fecha("FechaLiquidacion"),
            texto("TipoPersona"),
            fecha("FechaOperacion"),
            texto("IdConcepto"),
            texto("DocumentoIdentidadCAVALI"),
            texto("TipoEmisor"),
            texto("Mesa"),
            valorDe("FolioOperacion"));

    public CentralizaInteresReajusteCarteraTerminoRFIF(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "p_IdEmpresa", ctx.idEmpresa(), "p_FechaContable", ctx.fechaContable(),
                        "p_IdTipoCentralizacion", ctx.idTipo(),
                        "p_DocumentoIdentidadCorredora", ctx.documentoCorredora())))
                .eventos(eventos(SELECT_EVENTOS, parametros(
                        "p_IdEmpresa", ctx.idEmpresa(), "p_FechaContable", ctx.fechaContable(),
                        "p_IdTipoCentralizacion", ctx.idTipo(),
                        "p_DocumentoIdentidadCorredora", ctx.documentoCorredora())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO, parametros(
                        "p_IdEmpresa", ctx.idEmpresa(), "p_FechaContable", ctx.fechaContable(),
                        "p_DocumentoIdentidadCorredora", ctx.documentoCorredora(), "LocSistema", ctx.localizacion())), CAMPOS));
    }
}
