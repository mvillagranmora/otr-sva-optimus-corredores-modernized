package cl.bcs.contabilidad.domain.service;

import cl.bcs.contabilidad.domain.model.EntradaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.soporte.Contexto;
import cl.bcs.contabilidad.domain.service.soporte.Fila;
import cl.bcs.contabilidad.domain.service.soporte.Tablas;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.List;
import java.util.Map;

/**
 * Base of the Java centralization types that read their tables with the type's own legacy SQL files
 * (legacy {@code BcsCentralizacion} implementations: {@code sql.query(con, SQL_SELECT_X, params, Pojo.class)}).
 *
 * <p>Every statement runs through {@link ContabilidadRepository#queryDatosMovimientoRows}, the port's single
 * "run this named legacy SQL with these named parameters" method; the rows are mapped here to movements
 * (pojo/TabMovimientos), events (pojo/TabEventos) or data rows ({@link Fila}).
 *
 * <p>A real run ({@code simulaOperacion = "N"}) hands the produced tables to the persistence port once, as
 * CentralizaMultiSeleccion.java:347-357,411-417 did through CentralizaGenerico; a simulation never writes.
 */
public abstract class CentralizadorSql implements BcsCentralizacionService {

    protected final PlataformaPort plataforma;
    protected final ContabilidadRepository repository;

    protected CentralizadorSql(PlataformaPort plataforma, ContabilidadRepository repository) {
        this.plataforma = plataforma;
        this.repository = repository;
    }

    @Override
    public final SalidaCentralizacion centralizar(EntradaCentralizacion entrada) {
        SalidaCentralizacion salida = calcular(new Contexto(entrada, plataforma)).salida();
        if ("N".equals(entrada.simulaOperacion())) {
            repository.persistirCentralizacion(entrada.idTipoCentralizacion(), entrada.fechaContable(),
                    entrada.idEmpresa(), entrada.ejercicio(), String.valueOf(entrada.idUsuario()),
                    salida.tabMovimientos(), salida.tabEventos(), salida.tabDatosMovimiento(), salida.tabDatosEvento());
        }
        return salida;
    }

    /** The type-specific queries and mapping. */
    protected abstract Tablas calcular(Contexto ctx);

    // ------------------------------------------------------------------ queries

    /** Rows of a legacy *_SelectMovimientos*.sql, shaped like pojo/TabMovimientos. */
    protected List<TabMovimientoRef> movimientos(String sql, MapSqlParameterSource params) {
        return filas(sql, params).stream().map(CentralizadorSql::movimiento).toList();
    }

    /** Rows of a legacy *_SelectEventos*.sql, shaped like pojo/TabEventos. */
    protected List<TabEventoRef> eventos(String sql, MapSqlParameterSource params) {
        return filas(sql, params).stream().map(CentralizadorSql::evento).toList();
    }

    protected List<Fila> filas(String sql, MapSqlParameterSource params) {
        List<Map<String, Object>> rows = repository.queryDatosMovimientoRows(sql, params);
        return rows == null ? List.of() : rows.stream().map(Fila::de).toList();
    }

    /**
     * {@code clasificacion.get(0).ClasificacionTipoInstrumento} of a *_SelectClasificacionTipoInstrumento.sql.
     * Legacy crashed (IndexOutOfBounds) without a row; here it is a controlled error.
     */
    protected String clasificacionTipoInstrumento(String sql, Contexto ctx) {
        return filas(sql, parametros("IdTipoCentralizacion", ctx.idTipo())).stream().findFirst()
                .map(f -> f.texto("ClasificacionTipoInstrumento"))
                .orElseThrow(() -> new IllegalStateException(
                        "Tipo de centralizacion " + ctx.idTipo() + " sin ClasificacionTipoInstrumento (" + sql + ")"));
    }

    /** Named parameters in legacy {@code params.put} order: name, value, name, value... */
    protected static MapSqlParameterSource parametros(Object... nombreValor) {
        if (nombreValor.length % 2 != 0) throw new IllegalArgumentException("parametros: pares nombre/valor");
        MapSqlParameterSource p = new MapSqlParameterSource();
        for (int i = 0; i < nombreValor.length; i += 2) p.addValue((String) nombreValor[i], nombreValor[i + 1]);
        return p;
    }

    static TabMovimientoRef movimiento(Fila f) {
        return new TabMovimientoRef(f.entero("IdTipoCentralizacion"), f.entero("IdMovimiento"),
                f.texto("IdTipoMovimiento"), f.entero("IdEmpresa"), f.entero("Numero"), f.entero("Linea"),
                f.texto("Fecha"), f.texto("DocumentoIdentidad"), f.entero("IdPortafolio"), f.entero("Ejercicio"),
                f.texto("Periodo"), f.decimal("Monto"), f.texto("IdMoneda"), f.texto("Estado"));
    }

    static TabEventoRef evento(Fila f) {
        return new TabEventoRef(f.entero("IdTipoCentralizacion"), f.entero("IdMovimiento"), f.texto("IdEvento"),
                f.texto("Nombre"), f.decimal("MontoEvento"), f.texto("CodigoMoneda"));
    }
}
