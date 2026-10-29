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
 * Centralization type CentralizaEmprestitoInicio (legacy CentralizaEmprestitoInicio.java). Characterization: CONT-E-243..CONT-E-254.
 *
 * <p>Runs the type's own legacy SQL. Reads its instrument classification first (:72-75); when FechaContable is not
 * PLA.FechaSistema the *Historicos SQL run instead (:82-111).
 */
@Service("centralizaEmprestitoInicio")
public class CentralizaEmprestitoInicio extends CentralizadorSql {

    private static final String SELECT_CLASIFICACION_TIPO_INSTRUMENTO = "cl/bcs/contabilidad/sql/CentralizaEmprestitoInicio_SelectClasificacionTipoInstrumento.sql";
    private static final String SELECT_MOVIMIENTOS = "cl/bcs/contabilidad/sql/CentralizaEmprestitoInicio_SelectMovimientos.sql";
    private static final String SELECT_MOVIMIENTOS_HISTORICOS = "cl/bcs/contabilidad/sql/CentralizaEmprestitoInicio_SelectMovimientosHistoricos.sql";
    private static final String SELECT_EVENTOS = "cl/bcs/contabilidad/sql/CentralizaEmprestitoInicio_SelectEventos.sql";
    private static final String SELECT_EVENTOS_HISTORICOS = "cl/bcs/contabilidad/sql/CentralizaEmprestitoInicio_SelectEventosHistoricos.sql";
    private static final String SELECT_DATOS_MOVIMIENTO = "cl/bcs/contabilidad/sql/CentralizaEmprestitoInicio_SelectDatosMovimiento.sql";
    private static final String SELECT_DATOS_MOVIMIENTO_HISTORICO = "cl/bcs/contabilidad/sql/CentralizaEmprestitoInicio_SelectDatosMovimientoHistorico.sql";

    /** Legacy CentralizaEmprestitoInicio.java:116-138, one entry per legacy add(...), in order. */
    private static final List<Campo> CAMPOS = List.of(
            texto("DocumentoIdentidad"),
            texto("NombreCliente"),
            texto("Portafolio"),
            texto("Moneda"),
            texto("EsRelacionado"),
            texto("EsCuentaPropia"),
            texto("Producto"),
            texto("Nemo"),
            texto("Boveda"),
            texto("IdCartera"),
            texto("Emisor"),
            texto("TipoReajuste"),
            texto("CodigoIFRS"),
            texto("Agente"),
            texto("NombreAgente"),
            texto("SucursalAgente"),
            texto("NombreSucursalAgente"),
            texto("CentroCostoAgente"),
            texto("TipoPrestamo"),
            texto("TipoPrestatario"),
            texto("CondicionLiquidacion"),
            texto("FechaLiquidacion"),
            texto("FechaOperacion"));

    public CentralizaEmprestitoInicio(PlataformaPort plataforma, ContabilidadRepository repository) {
        super(plataforma, repository);
    }

    @Override
    protected Tablas calcular(Contexto ctx) {
        boolean actual = ctx.esFechaSistema(); // otherwise the historic queries apply
        String clasificacion = clasificacionTipoInstrumento(SELECT_CLASIFICACION_TIPO_INSTRUMENTO, ctx);
        return new Tablas()
                .movimientos(movimientos(actual ? SELECT_MOVIMIENTOS : SELECT_MOVIMIENTOS_HISTORICOS, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable(),
                        "IdTipoCentralizacion", ctx.idTipo(), "IdClasificacionTipoInstrumento", clasificacion)))
                .eventos(eventos(actual ? SELECT_EVENTOS : SELECT_EVENTOS_HISTORICOS, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable(),
                        "IdTipoCentralizacion", ctx.idTipo(), "IdClasificacionTipoInstrumento", clasificacion)))
                .datos(DatosMovimiento.de(ctx.idTipo(), filas(actual ? SELECT_DATOS_MOVIMIENTO : SELECT_DATOS_MOVIMIENTO_HISTORICO, parametros(
                        "IdEmpresa", ctx.idEmpresa(), "FechaContable", ctx.fechaContable(),
                        "LocSistema", ctx.localizacion(), "IdClasificacionTipoInstrumento", clasificacion)), CAMPOS));
    }
}
