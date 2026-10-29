package cl.bcs.apv.application.recaudacion;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.ReplicarPortafolio;
import cl.bcs.apv.api.dto.replicarportafolio.ReplicarPortafolioEntrada;
import cl.bcs.apv.api.dto.replicarportafolio.ReplicarPortafolioResultado;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code ReplicarPortafolio} (family C): copies an authorized person, indicators, custody accounts or payment preferences of one APV portfolio
 * to the other APV portfolios of the same client. Legacy {@code cl.bcs.APV.ReplicaPortafolioAPV}.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ReplicarPortafolioServicio implements ReplicarPortafolio {

    private static final String SQL_SELECT_INFORMACION_PORTAFOLIO_APV = SqlLegacy.recurso("ReplicaPortafolioAPV_SelectInformacionPortafolioAPV");
    private static final String SQL_SELECT_PORTAFOLIOS_APV_ASOCIADOS = SqlLegacy.recurso("ReplicaPortafolioAPV_SelectPortafoliosAPVAsociados");
    private static final String SQL_SELECT_INDICADOR_PORTAFOLIO_ORIGEN = SqlLegacy.recurso("ReplicaPortafolioAPV_SelectIndicadorPortafolioOrigen");
    private static final String SQL_DELETE_INDICADOR_REPLICA = SqlLegacy.recurso("ReplicaPortafolioAPV_DeleteIndicadorReplica");
    private static final String SQL_INSERT_INDICADOR_REPLICA = SqlLegacy.recurso("ReplicaPortafolioAPV_InsertIndicadorReplica");
    private static final String SQL_SELECT_CUENTA_CUSTODIO_PORTAFOLIO_ORIGEN = SqlLegacy.recurso("ReplicaPortafolioAPV_SelectCuentaCustodioPortafolioOrigen");
    private static final String SQL_INSERT_CUENTA_CUSTODIO_REPLICA = SqlLegacy.recurso("ReplicaPortafolioAPV_InsertCuentaCustodioReplica");
    private static final String SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO_ORIGEN = SqlLegacy.recurso("ReplicaPortafolioAPV_SelectPreferenciaPagoPortafolioOrigen");
    private static final String SQL_INSERT_PREFERENCIA_PAGO_PORTAFOLIO_REPLICA = SqlLegacy.recurso("ReplicaPortafolioAPV_InsertPreferenciaPagoPortafolioOrigen");
    private static final String SQL_UPDATE_PREFERENCIA_PAGO_PORTAFOLIO_REPLICA_NO_DEFECTO = SqlLegacy.recurso("ReplicaPortafolioAPV_UpdatePreferenciaPagoPortafolioOrigenNoDefecto");
    private static final String SQL_SELECT_AUTORIZADO_ORIGEN = SqlLegacy.recurso("ReplicaPortafolioAPV_SelectAutorizadoOrigen");
    private static final String SQL_INSERT_AUTORIZADO_REPLICA = SqlLegacy.recurso("ReplicaPortafolioAPV_InsertAutorizadoReplica");

    public static class PortafolioAPV {
        public Long IdPortafolio;
        public Long IdCliente;
        public java.sql.Timestamp FechaIngreso;
        public String IdPerfilLimitesExtra;
        public Integer IdProductoAgrupador;
    }

    public static class Indicador {
        public Long IdIndicador;
        public Long IdEntidad;
        public String IdTipoEntidad;
        public Long IdTipoIndicador;
        public String ModeloCliente;
        public Long IdEmpresa;
        public String ValorIndicador;
        public java.sql.Timestamp FechaIngreso;
        public java.sql.Timestamp FechaCreacion;
        public java.sql.Timestamp FechaModificacion;
        public String IdEstadoAutorizacion;
    }

    public static class Autorizado {
        public Long IdAutorizado;
        public Long IdPortafolio;
        public Long IdPersona;
        public String IdTipoAutorizado;
        public String IdEstado;
        public String IdEstadoAutorizacion;
        public java.sql.Timestamp FechaIngreso;
        public java.sql.Timestamp FechaCreacion;
        public java.sql.Timestamp FechaModificacion;
        public Long IdUsuarioCreador;
        public String NumeroRegistroPublico;
        public String Asiento;
        public String FichaPartida;
        public String TipoFirma;
    }

    /** Legacy {@code cl.bcs.socionegocio.por.pojo.CuentaCustodio} (with its constructor defaults). */
    public static class CuentaCustodio {
        public Long IdInterno;
        public Long IdCuentaCustodioPortafolio;
        public Long IdPortafolio;
        public String IdBoveda;
        public String Codigo;
        public java.sql.Timestamp FechaIngreso;
        public java.sql.Timestamp FechaCreacion;
        public java.sql.Timestamp FechaModificacion;
        public Long IdUsuarioCreador;
        public String IdEstado;
        public String IdEstadoAutorizacion;
        public String IdTipoCuentaCustodia = "";
        public String IdTipoCustodia = "";
        public String IdModeloCustodia = "";
        public Long IdPersonaBancoCustodio = 0L;
        public Long IdPersonaBancoLiquidador = 0L;
        public String LiquidaDividendos = "N";
        public String LiquidaCupones = "N";
        public String LiquidaVencimientos = "N";
        public String LiquidaPrepagos = "N";
    }

    /** Legacy {@code cl.bcs.socionegocio.por.pojo.PreferenciaPagoPortafolio}. */
    public static class PreferenciaPagoPortafolio {
        public Long IdInterno;
        public Long IdPreferenciaPagoPortafolio;
        public Long IdPortafolio;
        public String IdPreferenciaPago;
        public Long IdPersonaRelacionada;
        public Long IdCuentaBancaria;
        public java.sql.Timestamp FechaCreacion;
        public java.sql.Timestamp FechaModificacion;
        public Long IdUsuarioCreador;
        public String IdEstado;
        public Long IdEmpresa;
        public Long IdTarjetaBancaria;
        public java.sql.Timestamp FechaIngreso;
        public String IdEstadoAutorizacion;
        public String PorDefecto;
        public Long IdCarga;
    }

    private final SqlLegacy sql;
    private final Secuencias secuencias;

    public ReplicarPortafolioServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores) {
        this.sql = new SqlLegacy(jdbc);
        this.secuencias = secuencias;
    }

    @Override
    public ReplicarPortafolioResultado ejecutar(ReplicarPortafolioEntrada entrada) {
        try {
            List<MensajeLegacy> mensajes = new ArrayList<>();
            try {
                logica(entrada, mensajes);
            } catch (SQLException e) {
                throw SqlExceptionPlana.de(e);
            }
            // the legacy call never recomputes the counter: it stays 0 even when an ERROR message is returned
            return new ReplicarPortafolioResultado(0L, MensajeLegacy.aMensajes(mensajes));
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    private void logica(ReplicarPortafolioEntrada entrada, List<MensajeLegacy> out) throws Exception {
        PortafolioAPV portafolioAPV = obtenerInformacionPortafolioAPV(entrada.portafolio());
        if (portafolioAPV == null) {
            return;
        }
        List<PortafolioAPV> portafoliosAPV = obtenerPortafoliosAPVAsociados(portafolioAPV);
        if (portafoliosAPV.isEmpty()) {
            out.add(new MensajeLegacy(0L, MensajeLegacy.ADVERTENCIA, "", "No se encontraron otros portafolios relacionados."));
        }
        Long numeroEntidadesReplicadas = 0L;
        numeroEntidadesReplicadas += replicarAutorizados(portafoliosAPV, entrada);
        numeroEntidadesReplicadas += replicarIndicadores(portafoliosAPV, entrada);
        numeroEntidadesReplicadas += replicarCuentasCustodio(portafoliosAPV, entrada);
        numeroEntidadesReplicadas += replicarPreferenciasPagoPortafolio(portafoliosAPV, entrada);
        if (numeroEntidadesReplicadas == 0) {
            out.add(new MensajeLegacy(0L, MensajeLegacy.ERROR, "", "El TipoEntidad y el TipoOperacion no coinciden con las opciones disponibles."));
        }
    }

    private PortafolioAPV obtenerInformacionPortafolioAPV(Long idPortafolio) throws Exception {
        HashMap<String, Object> parametros = new HashMap<>();
        parametros.put("IdPortafolio", idPortafolio);
        List<PortafolioAPV> lista = sql.consultar(SQL_SELECT_INFORMACION_PORTAFOLIO_APV, parametros, PortafolioAPV.class);
        return lista.stream().findFirst().orElse(null);
    }

    private List<PortafolioAPV> obtenerPortafoliosAPVAsociados(PortafolioAPV portafolioAPV) throws Exception {
        HashMap<String, Object> parametros = new HashMap<>();
        parametros.put("IdCliente", portafolioAPV.IdCliente);
        parametros.put("IdPortafolio", portafolioAPV.IdPortafolio);
        parametros.put("IdProductoAgrupador", portafolioAPV.IdProductoAgrupador);
        return sql.consultar(SQL_SELECT_PORTAFOLIOS_APV_ASOCIADOS, parametros, PortafolioAPV.class);
    }

    /**
     * Legacy {@code SQL.batch} prepares the statement even when the list is empty: a missing table fails there. The kit skips an empty parameterised
     * batch, so the statement is prepared (not executed) here to reproduce the failure.
     */
    private <T> void lotePreparando(String texto, List<T> filas) throws Exception {
        if (filas.isEmpty()) {
            String limpio = SqlLegacy.sinComentarios(texto);
            String jdbcSql = NamedParameterUtils.substituteNamedParameters(NamedParameterUtils.parseSqlStatement(limpio), null);
            try {
                sql.jdbc().getJdbcOperations().execute((ConnectionCallback<Void>) c -> {
                    c.prepareStatement(jdbcSql).close();
                    return null;
                });
            } catch (org.springframework.dao.DataAccessException e) {
                Throwable causa = e.getMostSpecificCause();
                throw causa instanceof SQLException s ? s : new SQLException(e.getMessage(), e);
            }
            return;
        }
        sql.lote(texto, filas);
    }

    private Long replicarIndicadores(List<PortafolioAPV> portafoliosAPV, ReplicarPortafolioEntrada entrada) throws Exception {
        if ("INDICADORES".equals(entrada.tipoEntidad()) && "U".equals(entrada.tipoOperacion())) {
            HashMap<String, Object> parametros = new HashMap<>();
            parametros.put("IdSolicitudCambio", entrada.idEntidad());
            parametros.put("IdPortafolio", entrada.portafolio());
            List<Indicador> indicadores = sql.consultar(SQL_SELECT_INDICADOR_PORTAFOLIO_ORIGEN, parametros, Indicador.class);
            List<Indicador> indicadoresReplica = new ArrayList<>();
            for (PortafolioAPV portafolioAPV : portafoliosAPV) {
                for (Indicador indicador : indicadores) {
                    Indicador r = new Indicador();
                    r.IdEntidad = portafolioAPV.IdPortafolio;
                    r.IdTipoEntidad = indicador.IdTipoEntidad;
                    r.IdTipoIndicador = indicador.IdTipoIndicador;
                    r.ModeloCliente = indicador.ModeloCliente;
                    r.IdEmpresa = indicador.IdEmpresa;
                    r.ValorIndicador = indicador.ValorIndicador;
                    r.FechaIngreso = indicador.FechaIngreso;
                    r.FechaCreacion = indicador.FechaCreacion;
                    r.FechaModificacion = indicador.FechaModificacion;
                    r.IdEstadoAutorizacion = indicador.IdEstadoAutorizacion;
                    indicadoresReplica.add(r);
                }
            }
            // LEGACY DEFECT kept (see README APV-C-374): the delete filters IND.Indicador by IdPortafolio, a column the table does not have; the select can never match
            lotePreparando(SQL_DELETE_INDICADOR_REPLICA, indicadoresReplica);
            lotePreparando(SQL_INSERT_INDICADOR_REPLICA, indicadoresReplica);
            return 1L;
        }
        return 0L;
    }

    private Long replicarCuentasCustodio(List<PortafolioAPV> portafoliosAPV, ReplicarPortafolioEntrada entrada) throws Exception {
        if ("CUENTACUSTODIO".equals(entrada.tipoEntidad()) && "I".equals(entrada.tipoOperacion())) {
            HashMap<String, Object> parametros = new HashMap<>();
            parametros.put("IdCuentaCustodioPortafolio", entrada.idEntidad());
            List<CuentaCustodio> cuentas = sql.consultar(SQL_SELECT_CUENTA_CUSTODIO_PORTAFOLIO_ORIGEN, parametros, CuentaCustodio.class);
            List<CuentaCustodio> replicas = new ArrayList<>();
            for (PortafolioAPV portafolioAPV : portafoliosAPV) {
                for (CuentaCustodio c : cuentas) {
                    CuentaCustodio r = new CuentaCustodio();
                    r.IdPortafolio = portafolioAPV.IdPortafolio;
                    r.IdBoveda = c.IdBoveda;
                    r.Codigo = c.Codigo;
                    r.IdTipoCuentaCustodia = c.IdTipoCuentaCustodia;
                    r.IdTipoCustodia = c.IdTipoCustodia;
                    r.IdModeloCustodia = c.IdModeloCustodia;
                    r.IdPersonaBancoCustodio = c.IdPersonaBancoCustodio;
                    r.IdPersonaBancoLiquidador = c.IdPersonaBancoLiquidador;
                    r.IdEstado = c.IdEstado;
                    r.LiquidaPrepagos = c.LiquidaPrepagos;
                    r.LiquidaVencimientos = c.LiquidaVencimientos;
                    r.LiquidaCupones = c.LiquidaCupones;
                    r.LiquidaDividendos = c.LiquidaDividendos;
                    r.FechaIngreso = c.FechaIngreso;
                    r.FechaCreacion = c.FechaCreacion;
                    r.FechaModificacion = c.FechaModificacion;
                    r.IdUsuarioCreador = c.IdUsuarioCreador;
                    r.IdEstadoAutorizacion = c.IdEstadoAutorizacion;
                    replicas.add(r);
                }
            }
            lotePreparando(SQL_INSERT_CUENTA_CUSTODIO_REPLICA, replicas);
            return 1L;
        }
        return 0L;
    }

    @SuppressWarnings("StringEquality")
    private Long replicarPreferenciasPagoPortafolio(List<PortafolioAPV> portafoliosAPV, ReplicarPortafolioEntrada entrada) throws Exception {
        if ("PREFERENCIASDEPAGO".equals(entrada.tipoEntidad()) && ("I".equals(entrada.tipoOperacion()) || "U".equals(entrada.tipoOperacion()))) {
            HashMap<String, Object> parametros = new HashMap<>();
            parametros.put("IdEmpresa", entrada.idEmpresa());
            parametros.put("IdPreferenciaPagoPortafolio", entrada.idEntidad());
            List<PreferenciaPagoPortafolio> preferencias = sql.consultar(SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO_ORIGEN, parametros, PreferenciaPagoPortafolio.class);
            List<PreferenciaPagoPortafolio> replicas = new ArrayList<>();
            List<PreferenciaPagoPortafolio> replicasPorDefecto = new ArrayList<>();
            for (PortafolioAPV portafolioAPV : portafoliosAPV) {
                for (PreferenciaPagoPortafolio p : preferencias) {
                    PreferenciaPagoPortafolio r = new PreferenciaPagoPortafolio();
                    r.IdPreferenciaPagoPortafolio = p.IdPreferenciaPagoPortafolio;
                    r.IdPortafolio = portafolioAPV.IdPortafolio;
                    r.IdPreferenciaPago = p.IdPreferenciaPago;
                    r.IdPersonaRelacionada = p.IdPersonaRelacionada;
                    r.FechaCreacion = p.FechaCreacion;
                    r.FechaModificacion = p.FechaModificacion;
                    r.IdUsuarioCreador = p.IdUsuarioCreador;
                    r.IdEstado = p.IdEstado;
                    r.IdEmpresa = p.IdEmpresa;
                    r.IdTarjetaBancaria = p.IdTarjetaBancaria;
                    r.FechaIngreso = p.FechaIngreso;
                    r.IdEstadoAutorizacion = p.IdEstadoAutorizacion;
                    r.PorDefecto = p.PorDefecto;
                    r.IdCarga = p.IdCarga;
                    // LEGACY DEFECT kept (see README APV-C-372): String compared by reference, never true for a value read from the database
                    if (p.PorDefecto == "S") {
                        replicasPorDefecto.add(r);
                    }
                    replicas.add(r);
                }
                // LEGACY DEFECT kept (see README APV-C-372/373): insert into "BSCLPortafolioBD::POR..." (dots missing): the statement cannot be prepared
                lotePreparando(SQL_INSERT_PREFERENCIA_PAGO_PORTAFOLIO_REPLICA, replicas);
                lotePreparando(SQL_UPDATE_PREFERENCIA_PAGO_PORTAFOLIO_REPLICA_NO_DEFECTO, replicasPorDefecto);
            }
            return 1L;
        }
        return 0L;
    }

    private Long replicarAutorizados(List<PortafolioAPV> portafoliosAPV, ReplicarPortafolioEntrada entrada) throws Exception {
        if ("AUTORIZACION".equals(entrada.tipoEntidad()) && "I".equals(entrada.tipoOperacion())) {
            HashMap<String, Object> parametros = new HashMap<>();
            parametros.put("IdAutorizado", entrada.idEntidad());
            List<Autorizado> autorizados = sql.consultar(SQL_SELECT_AUTORIZADO_ORIGEN, parametros, Autorizado.class);
            if (!autorizados.isEmpty()) {
                Autorizado principal = autorizados.get(0);
                List<Autorizado> replicas = new ArrayList<>();
                List<Long> nuevos = Colab.secuencia(secuencias, "BSCL.Portafolio.BD.Secuencias::Autorizado", Long.valueOf(portafoliosAPV.size()));
                int index = 0;
                for (PortafolioAPV portafolioAPV : portafoliosAPV) {
                    // the source portfolio itself is not replicated
                    if (!portafolioAPV.IdPortafolio.equals(principal.IdPortafolio)) {
                        Autorizado r = new Autorizado();
                        r.IdAutorizado = nuevos.get(index);
                        r.IdPortafolio = portafolioAPV.IdPortafolio;
                        r.IdPersona = principal.IdPersona;
                        r.IdTipoAutorizado = principal.IdTipoAutorizado;
                        r.IdEstado = principal.IdEstado;
                        r.IdEstadoAutorizacion = principal.IdEstadoAutorizacion;
                        r.FechaIngreso = principal.FechaIngreso;
                        r.FechaCreacion = principal.FechaCreacion;
                        r.FechaModificacion = principal.FechaModificacion;
                        r.IdUsuarioCreador = principal.IdUsuarioCreador;
                        r.NumeroRegistroPublico = principal.NumeroRegistroPublico;
                        r.Asiento = principal.Asiento;
                        r.FichaPartida = principal.FichaPartida;
                        r.TipoFirma = principal.TipoFirma;
                        replicas.add(r);
                        index++;
                    }
                }
                if (!replicas.isEmpty()) {
                    sql.lote(SQL_INSERT_AUTORIZADO_REPLICA, replicas);
                }
                return 1L;
            }
        }
        return 0L;
    }
}
