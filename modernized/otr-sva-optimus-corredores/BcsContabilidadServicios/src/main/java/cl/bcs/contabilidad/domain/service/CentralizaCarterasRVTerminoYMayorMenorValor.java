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
 * Centralization type CentralizaCarterasRVTerminoYMayorMenorValor (legacy CentralizaCarterasRVTerminoYMayorMenorValor.java). Characterization: CONT-E-129..CONT-E-140.
 *
 * <p>Runs the type's own legacy SQL. When FechaContable is not PLA.FechaSistema the *Historico SQL run instead
 * (:86-117).
 */
@Service("centralizaCarterasRVTerminoYMayorMenorValor")
public class CentralizaCarterasRVTerminoYMayorMenorValor extends CentralizadorSql {

    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaCarterasRVTerminoYMayorMenorValor_SelectMovimientos.sql";
    private static final String SELECT_MOVIMIENTOS_HISTORICO = "cl/bcs/contabilidad/sql/CentralizaCarterasRVTerminoYMayorMenorValor_SelectMovimientosHistorico.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaCarterasRVTerminoYMayorMenorValor_SelectEventos.sql";
    private static final String SELECT_EVENTOS_HISTORICO = "cl/bcs/contabilidad/sql/CentralizaCarterasRVTerminoYMayorMenorValor_SelectEventosHistorico.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaCarterasRVTerminoYMayorMenorValor_SelectDatosMovimiento.sql";
    private static final String SELECT_DATOS_MOVIMIENTO_HISTORICO = "cl/bcs/contabilidad/sql/CentralizaCarterasRVTerminoYMayorMenorValor_SelectDatosMovimientoHistorico.sql";

    /** Legacy CentralizaCarterasRVTerminoYMayorMenorValor.java:120-157, one entry per legacy add(...), in order. */
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
            decimal("PrecioMercadoAnterior"),
            decimal("PrecioMercadoActual"),
            decimal("TipoCambio"),
            fecha("FechaAnterior"),
            fecha("FechaActual"),
            texto("Relacion"),
            texto("Bolsa"),
            fecha("FechaLiquidacion"),
            texto("TipoPersona"),
            fecha("FechaOperacion"),
            texto("Concepto", "IdConcepto"),
            texto("DocumentoIdentidadCAVALI"),
            texto("CostingCode", "CentroCostoAgente"),
            texto("CostingCode2", "Producto"),
            texto("CostingCode3", "Relacion"),
            texto("CostingCode4", "Bolsa"),
            vacio("CostingCode5"));

    public CentralizaCarterasRVTerminoYMayorMenorValor(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        boolean actual = ctx.esFechaSistema(); // otherwise the historic queries apply
        return new Tablas()
                .movimientos(movimientos(actual ? SELECT_MOVIMIENTOS : SELECT_MOVIMIENTOS_HISTORICO, parametros(
                        "p_IdEmpresa", ctx.idEmpresa(), "p_FechaContable", ctx.fechaContable(),
                        "p_IdTipoCentralizacion", ctx.idTipo(),
                        "p_DocumentoIdentidadCorredora", ctx.documentoCorredora())))
                .eventos(eventos(actual ? SELECT_EVENTOS : SELECT_EVENTOS_HISTORICO, parametros(
                        "p_IdEmpresa", ctx.idEmpresa(), "p_FechaContable", ctx.fechaContable(),
                        "p_IdTipoCentralizacion", ctx.idTipo(),
                        "p_DocumentoIdentidadCorredora", ctx.documentoCorredora())))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(actual ? SELECT_DATOS_MOVIMIENTO : SELECT_DATOS_MOVIMIENTO_HISTORICO, parametros(
                        "p_IdEmpresa", ctx.idEmpresa(), "p_FechaContable", ctx.fechaContable(),
                        "p_DocumentoIdentidadCorredora", ctx.documentoCorredora())), CAMPOS));
    }
}
