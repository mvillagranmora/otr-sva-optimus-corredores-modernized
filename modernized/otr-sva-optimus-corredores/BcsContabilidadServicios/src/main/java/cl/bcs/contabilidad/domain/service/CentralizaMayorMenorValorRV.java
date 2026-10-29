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
 * Centralization type CentralizaMayorMenorValorRV (legacy CentralizaMayorMenorValorRV.java). Characterization: CONT-F-044..CONT-F-054.
 *
 * <p>Runs the type's own legacy SQL (legacy lines :69, :76, :83) and turns each data row into the
 * TabDatosMovimiento entries of legacy lines 87-106.
 */
@Service("centralizaMayorMenorValorRV")
public class CentralizaMayorMenorValorRV extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaMayorMenorValorRV_SelectMovimientos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaMayorMenorValorRV_SelectEventos.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaMayorMenorValorRV_SelectDatosMovimiento.sql";

    /** Legacy CentralizaMayorMenorValorRV.java:87-106, one entry per legacy add(...), in order. */
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

    public CentralizaMayorMenorValorRV(PlataformaPort plataforma, ContabilidadRepository repository) {
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
