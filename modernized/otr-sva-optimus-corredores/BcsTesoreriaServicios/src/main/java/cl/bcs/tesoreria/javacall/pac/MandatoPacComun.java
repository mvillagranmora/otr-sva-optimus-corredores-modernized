package cl.bcs.tesoreria.javacall.pac;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.Plataforma;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.javacall.kit.colaborador.AgregarLog;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearEventoAutorizacionMasivo;
import cl.bcs.tesoreria.javacall.kit.colaborador.ProcedimientoAlmacenado;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;

/**
 * Steps shared by IngresarMandatoPAC and ModificarPAC (TES-B-97..134, 330): account / identity document lookups, the HANA procedure that changes the bank
 * account of a non-PAC payment (type 'N'), the NUEPAC authorization event, the audit log entry and the bank update of the CSC contract.
 * Their collaborators go through the {@link Colaboradores} port.
 */
@Component
public class MandatoPacComun {

    private static final String SQL_SELECT_CTA_CORRIENTE = SqlNombrado.recurso("IngresarMandatoPAC_SelectCtaCorriente");
    private static final String SQL_SELECT_DOC_IDENTIDAD = SqlNombrado.recurso("IngresarMandatoPAC_SelectDocIdentidad");
    private static final String SQL_SELECT_MANDATOS_PAC = SqlNombrado.recurso("IngresarMandatoPAC_SelectMandatosPAC");
    private static final String SQL_UPDATE_BANCO = SqlNombrado.recurso("IngresoPAC_ActualizarBanco");
    private static final String SQL_CREATE_TABLE = SqlNombrado.recurso("ModificarPersonaCuentaBancaria_CreateTable");
    private static final String SQL_DROP_TABLE = SqlNombrado.recurso("ModificarPersonaCuentaBancaria_DropTable");
    private static final String SQL_INSERT_TABLE = SqlNombrado.recurso("ModificarPersonaCuentaBancaria_InsertTable");
    private static final String SQL_CALL_SP = SqlNombrado.recurso("ModificarPersonaCuentaBancaria_CallSp");

    /** Bank account of a client (legacy {@code ResultadoNumeroCuentaBancaria}). */
    public static class CuentaBancaria {
        public String NumeroCuentaBancaria;
        public String CodigoBanco;
        public Long IdBanco;
    }

    /** Row of the identity-document lookup (legacy {@code ResultadoDocumentoIdentidad}). */
    public static class DocumentoIdentidad {
        public String DocumentoIdentidad;
    }

    /** Parameters of the CSC bank update (legacy {@code ActualizarBanco}). */
    public static class ActualizarBanco {
        public Long IdBanco;
        public String FolioCSC;
    }

    private final SqlNombrado sql;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;

    public MandatoPacComun(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario, Colaboradores colaboradores) {
        this.sql = new SqlNombrado(jdbc);
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
    }

    /** The bank account (at most one row: the query is {@code TOP 1}); empty when the id is unknown. */
    public List<CuentaBancaria> cuentaBancaria(Long idCuentaBancaria) throws SQLException {
        Map<String, Object> params = new HashMap<>();
        params.put("IdCuentaBancaria", idCuentaBancaria);
        return sql.consultar(SQL_SELECT_CTA_CORRIENTE, params, CuentaBancaria.class);
    }

    public boolean existeDocumentoIdentidad(String documento) throws SQLException {
        Map<String, Object> params = new HashMap<>();
        params.put("DocumentoIdentidad", documento);
        return !sql.consultar(SQL_SELECT_DOC_IDENTIDAD, params, DocumentoIdentidad.class).isEmpty();
    }

    /** Active mandates (state ING, or PAU pending an ING authorization) of the given grouping folios. */
    public List<MandatoPac> mandatosConMismoIdentServicio(List<String> identServicios) throws SQLException {
        return sql.consultar(SQL_SELECT_MANDATOS_PAC, Map.of("IdentServicio", identServicios), MandatoPac.class);
    }

    /**
     * Payment type 'N': changes the bank account through the HANA procedure {@code PER.ModificarPersonaCuentaBancaria}, with a temporary table that is
     * always dropped. The procedure's outputs are not read (the legacy declared them and ignored them).
     */
    public void modificarCuentaBancaria(EntradaPac entrada, Long idCuentaBancaria) throws SQLException {
        try {
            sql.ejecutar(SQL_CREATE_TABLE);
            sql.ejecutar(SQL_INSERT_TABLE);

            Map<String, Object> parmsIn = new HashMap<>();
            parmsIn.put("p_IdEmpresa", entrada.p_IdEmpresa);
            parmsIn.put("p_IdUsuario", entrada.p_IdUsuario);
            parmsIn.put("p_IdModulo", entrada.p_IdModulo);
            parmsIn.put("p_UsaReglasNegocio", entrada.p_UsaReglasNegocio);
            parmsIn.put("p_IdCuentaBancaria", idCuentaBancaria);
            ProcedimientoAlmacenado.llamar(colaboradores, SQL_CALL_SP, parmsIn);
        } finally {
            sql.ejecutar(SQL_DROP_TABLE);
        }
    }

    /**
     * Creates the NUEPAC authorization event of one mandate and returns the messages the legacy added to the output: every warning and every error of the
     * authorization service becomes an ERROR message whose code is its description.
     */
    public List<MensajeSalida> crearEventoAutorizacion(EntradaPac entrada, String fechaSistema, String glosa, String numeroOperacion) {
        CrearEventoAutorizacionMasivo.ServiceEntrada evento = new CrearEventoAutorizacionMasivo.ServiceEntrada();
        evento.P_IDEMPRESA = entrada.p_IdEmpresa;
        evento.P_IDMODULO = entrada.p_IdModulo;
        evento.P_IDUSUARIO = entrada.p_IdUsuario;
        evento.P_SIMULAROPERACION = "N";
        evento.P_USAREGLASNEGOCIO = "N";

        CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS fila = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
        fila.IdTipoEvento = "NUEPAC";
        fila.IdModuloSolicita = entrada.p_IdModulo;
        fila.IdUsuarioSolicita = entrada.p_IdUsuario;
        fila.Fecha = fechaSistema;
        fila.IdConcepto = "NUEPAC";
        fila.FechaOperacionConcepto = fechaSistema;
        fila.IdCliente = 1L;
        fila.Glosa = glosa;
        fila.NumeroOperacionConcepto = numeroOperacion;
        evento.P_TABLAEVENTOS = List.of(fila);

        CrearEventoAutorizacionMasivo.ServiceSalida salida = CrearEventoAutorizacionMasivo.invocar(colaboradores, evento);

        List<MensajeSalida> mensajes = new ArrayList<>();
        for (CrearEventoAutorizacionMasivo.ObjADVERTENCIASDETALLE advertencia : salida.P_OUT_ADVERTENCIASDETALLE) {
            mensajes.add(Plataforma.mensaje(diccionario, entrada.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_ERROR, advertencia.Descripcion, new HashMap<>()));
        }
        for (CrearEventoAutorizacionMasivo.ObjERRORESDETALLE error : salida.P_OUT_ERRORESDETALLE) {
            mensajes.add(Plataforma.mensaje(diccionario, entrada.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_ERROR, error.Descripcion, new HashMap<>()));
        }
        return mensajes;
    }

    /** Audit log entry of the operation ({@code tipoLog} e.g. "INGRESO MANDATO PAC", {@code tipoRegistro} e.g. "INGRESO_MANDATO_PAC"). */
    public void agregarLog(EntradaPac entrada, String fechaSistema, String tipoLog, String tipoRegistro) {
        AgregarLog.InAgregarLog log = new AgregarLog.InAgregarLog();
        AgregarLog.InAgregarLog.Log registro = new AgregarLog.InAgregarLog.Log();
        registro.IdCabeceraLog = 0L;
        registro.IdTipoLog = tipoLog;
        registro.IdModulo = "TES";
        registro.IdUsuario = entrada.p_IdUsuario;
        registro.IdEmpresa = entrada.p_IdEmpresa;
        registro.FechaSistemaLog = fechaSistema;
        registro.IdUsuarioAtendido = entrada.p_IdUsuario;
        registro.IdCliente = 0L;
        registro.EstadoCliente = "";
        registro.NombreCliente = "";
        registro.IdTipoDocumentoIdentidad = "";
        registro.IdDocumentoIdentidad = "";
        registro.IdPortafolio = 0L;
        registro.EstadoPortafolio = "";
        registro.IdTipoRegistro = tipoRegistro;
        registro.EstadoRegistro = "ING";
        registro.IdTipoOperacion = "";
        registro.Folio = 0L;
        registro.Cantidad = BigDecimal.ZERO;
        registro.PrecioTasa = BigDecimal.ZERO;
        registro.Monto = BigDecimal.ZERO;
        registro.IdMonedaOrigen = "";
        registro.IdMonedaLiquidacion = "";
        registro.TipoCambio = "";
        registro.Nemo = "";
        registro.IdProceso = 0L;
        registro.EstadoProceso = "";
        registro.NombreProceso = "";
        log.p_Log.add(registro);
        AgregarLog.invocar(colaboradores, log);
    }

    /** Sets the bank of the CSC contract of a mandate. */
    public void actualizarBanco(Long idBanco, String folioCsc) throws SQLException {
        ActualizarBanco fila = new ActualizarBanco();
        fila.IdBanco = idBanco;
        fila.FolioCSC = folioCsc;
        sql.lote(SQL_UPDATE_BANCO, List.of(fila));
    }
}
