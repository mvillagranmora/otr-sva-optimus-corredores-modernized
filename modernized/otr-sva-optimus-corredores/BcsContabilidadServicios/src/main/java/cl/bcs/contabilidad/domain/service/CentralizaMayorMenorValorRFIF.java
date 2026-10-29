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
 * Centralization type CentralizaMayorMenorValorRFIF (legacy CentralizaMayorMenorValorRFIF.java). Characterization: CONT-F-033..CONT-F-043.
 *
 * <p>Runs the type's own legacy SQL (legacy lines :67, :74, :81) and turns each data row into the
 * TabDatosMovimiento entries of legacy lines 85-104.
 */
@Service("centralizaMayorMenorValorRFIF")
public class CentralizaMayorMenorValorRFIF extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaMayorMenorValorRFIF_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaMayorMenorValorRFIF_SelectEventos.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaMayorMenorValorRFIF_SelectDatosMovimiento.sql";

    /** Legacy CentralizaMayorMenorValorRFIF.java:85-104, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            texto("DocumentoIdentidad"),
            texto("Portafolio"),
            valorDe("EsCuentaPropia"),
            valorDe("EsRelacionado"),
            texto("TipoCartera"),
            fecha("FechaOperacion"),
            obligatorio("FechaVencimientoOperacion"),
            fecha("FechaLiquidacion"),
            valorDe("FolioOperacion"),
            texto("ClaseInstrumento"),
            texto("TipoInstrumento"),
            texto("Nemotecnico", "Nemo"),
            texto("Boveda"),
            texto("Emisor"),
            texto("TipoEmisor"),
            texto("IdTipoReajuste", "TipoReajuste"),
            obligatorio("FechaVencimientoInstrumento"),
            texto("CodigoIFRS"),
            texto("Moneda"),
            obligatorio("ClaseEmisor"));

    public CentralizaMayorMenorValorRFIF(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        return new Tablas()
                .movimientos(movimientos(SELECT_MOVIMIENTOS, parametros(
                        "p_IdEmpresa", ctx.idEmpresa(), "p_FechaContable", ctx.fechaContable(),
                        "p_IdTipoCentralizacion", ctx.idTipo(), "p_IdInforme", ctx.entrada().idInforme())))
                .eventos(eventos(SELECT_EVENTOS, parametros(
                        "p_IdEmpresa", ctx.idEmpresa(), "p_IdTipoCentralizacion", ctx.idTipo(),
                        "p_IdInforme", ctx.entrada().idInforme())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(SELECT_DATOS_MOVIMIENTO, parametros(
                        "p_IdEmpresa", ctx.idEmpresa(), "p_DocumentoIdentidad", ctx.documentoCorredora(),
                        "p_IdInforme", ctx.entrada().idInforme())), CAMPOS));
    }
}
