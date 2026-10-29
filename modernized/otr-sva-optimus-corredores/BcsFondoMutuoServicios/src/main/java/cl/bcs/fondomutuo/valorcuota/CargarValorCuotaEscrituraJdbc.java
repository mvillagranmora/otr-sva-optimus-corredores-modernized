package cl.bcs.fondomutuo.valorcuota;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;
import cl.bcs.fondomutuo.kit.EscrituraConfirmada;
import cl.bcs.fondomutuo.requisitos.SqlLote;

/**
 * The four writes of {@code CargarValorCuota}, verbatim SQL, through the legacy {@code SQL.batch} binder rules ({@link SqlLote}: every {@code BigDecimal} is bound at
 * scale 16 HALF_DOWN, D-F3A-23). They join the ambient transaction and NEVER commit or roll back. The history INSERT carries the legacy inline {@code NEXTVAL}; the file
 * stays byte for byte (a test pins its SHA-256) and here, in memory, that single token becomes {@code :IdValorCuotaHistorico}, bound with the id the service took from
 * the sequence port.
 */
@Component
public class CargarValorCuotaEscrituraJdbc implements CargarValorCuotaEscritura {

    private static final String NEXTVAL = "\"BSCL.FondoMutuo.BD.Secuencias::ValorCuotaHistorico\".NEXTVAL";

    private static final String SQL_UPDATE_ULTIMA_CARGA = SqlRecursos.modulo("CargarValorCuota_UpdateValorCuotaUltimaCarga.sql");
    private static final String SQL_INSERT_HISTORICO = reemplazarSecuencia(SqlRecursos.modulo("CargarValorCuota_InsertValorCuotaHistorico.sql"));
    private static final String SQL_DELETE_VALOR_CUOTA = SqlRecursos.modulo("CargarValorCuota_DeleteValorCuota.sql");
    private static final String SQL_INSERT_VALOR_CUOTA = SqlRecursos.modulo("CargarValorCuota_InsertValorCuota.sql");

    /** Row of the history INSERT: the fields the statement names, with the id of the sequence. */
    public static class FilaHistorico {
        public Long IdValorCuotaHistorico;
        public String IdSerieFondo;
        public java.sql.Date FechaVigencia;
        public BigDecimal ValorCuota;
        public String IdMoneda;
        public String Observacion;
        public java.sql.Date FechaCalculo;
        public Long IdEmpresa;
    }

    private final ConexionUnidadDeTrabajo conexion;
    private final boolean exigirTransaccion;

    /** Without the guard of {@link #exigirTransaccion}: the equivalence harness runs on a raw connection with no Spring transaction. */
    public CargarValorCuotaEscrituraJdbc(ConexionUnidadDeTrabajo conexion) {
        this(conexion, false);
    }

    /**
     * M-2 (architecture review F3 W2): with {@code fondomutuo.valorcuota.exigir-transaccion} (default true in the application) every write requires a Spring transaction
     * that is active and NOT read only; otherwise the writes would autocommit one by one and an error counter &gt; 0 could not roll them back.
     */
    @Autowired
    public CargarValorCuotaEscrituraJdbc(ConexionUnidadDeTrabajo conexion, @Value("${fondomutuo.valorcuota.exigir-transaccion:true}") boolean exigirTransaccion) {
        this.conexion = conexion;
        this.exigirTransaccion = exigirTransaccion;
    }

    private void exigirTransaccionDeEscritura() {
        if (exigirTransaccion && !(TransactionSynchronizationManager.isActualTransactionActive() && !TransactionSynchronizationManager.isCurrentTransactionReadOnly())) {
            throw new IllegalStateException("CargarValorCuota writes need an active read-write transaction (the host decides commit or rollback)");
        }
    }

    static String reemplazarSecuencia(String sql) {
        if (!sql.contains(NEXTVAL)) {
            throw new IllegalStateException("CargarValorCuota_InsertValorCuotaHistorico.sql no longer has the NEXTVAL token");
        }
        return sql.replace(NEXTVAL, ":IdValorCuotaHistorico");
    }

    @Override
    public void actualizarUltimaCarga(List<CargarValorCuotaContrato.Resultado> filas) {
        exigirTransaccionDeEscritura();
        EscrituraConfirmada.escrituraLocalPendiente();   // M3: like Normal / RRE (a later DDL commit on HANA); this class has none, the mark is inert
        conexion.usar(con -> SqlLote.ejecutar(con, SQL_UPDATE_ULTIMA_CARGA, filas));
    }

    @Override
    public void insertarHistorico(List<CargarValorCuotaContrato.Resultado> filas, List<Long> ids) {
        List<FilaHistorico> lote = new ArrayList<>();
        for (int i = 0; i < filas.size(); i++) {
            CargarValorCuotaContrato.Resultado r = filas.get(i);
            FilaHistorico f = new FilaHistorico();
            f.IdValorCuotaHistorico = ids.get(i);
            f.IdSerieFondo = r.IdSerieFondo;
            f.FechaVigencia = r.FechaVigencia;
            f.ValorCuota = r.ValorCuota;
            f.IdMoneda = r.IdMoneda;
            f.Observacion = r.Observacion;
            f.FechaCalculo = r.FechaCalculo;
            f.IdEmpresa = r.IdEmpresa;
            lote.add(f);
        }
        exigirTransaccionDeEscritura();
        EscrituraConfirmada.escrituraLocalPendiente();
        conexion.usar(con -> SqlLote.ejecutar(con, SQL_INSERT_HISTORICO, lote));
    }

    @Override
    public void eliminarVigentes(List<CargarValorCuotaContrato.Resultado> filas) {
        exigirTransaccionDeEscritura();
        EscrituraConfirmada.escrituraLocalPendiente();
        conexion.usar(con -> SqlLote.ejecutar(con, SQL_DELETE_VALOR_CUOTA, filas));
    }

    @Override
    public void insertarVigentes(List<CargarValorCuotaContrato.Resultado> filas) {
        exigirTransaccionDeEscritura();
        EscrituraConfirmada.escrituraLocalPendiente();
        conexion.usar(con -> SqlLote.ejecutar(con, SQL_INSERT_VALOR_CUOTA, filas));
    }
}
