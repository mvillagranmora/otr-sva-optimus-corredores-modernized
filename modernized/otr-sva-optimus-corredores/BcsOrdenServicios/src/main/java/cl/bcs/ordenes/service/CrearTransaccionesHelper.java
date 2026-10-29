package cl.bcs.ordenes.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Constants, POJOs, and date helpers extracted to keep CrearTransaccionesService under 900 lines. */
public final class CrearTransaccionesHelper {

    private CrearTransaccionesHelper() {}

    // ── Constants ────────────────────────────────────────────────────────────────

    static final String CLASIFICACION_RF = "RF";
    static final String CLASIFICACION_RV = "RV";
    static final String CLASIFICACION_IF = "IF";
    static final String CLASIFICACION_RE = "RE";
    static final String CLASIFICACION_DV = "DV";
    static final String TIPO_INSTRUMENTO_SIM = "SIM";
    static final String TIPO_INSTRUMENTO_RRF = "RRF";
    static final String ESTADO_TRANSACCION_INGRESADA = "ING";
    static final String ESTADO_MODULO_CERRADO = "C";
    static final String ESTADO_LIBRO_CERRADO = "CER";
    static final String ID_MODULO_ORDENES = "ORD";
    static final String TIPO_OPERACION_VCRF = "VCRF";
    static final String TIPO_OPERACION_COMSIM = "COMSIM";
    static final String TIPO_OPERACION_VENSIM = "VENSIM";
    static final String TIPO_OPERACION_COMRRV = "COMRRV";
    static final String TIPO_OPERACION_VENRRV = "VENRRV";
    static final String TIPO_OPERACION_COMRRF = "COMRRF";
    static final String TIPO_OPERACION_VENRRF = "VENRRF";
    static final String ACCION_COMPRA = "COMPRA";
    static final String OD = "OD";
    static final String OE = "OE";
    static final String SI = "S";
    static final String NO = "N";

    static final List<String> NEMOS_BOLSON = Arrays.asList("XERO", "CERO", "ZERO");
    static final List<String> BONO_RECONOCIMIENTO = Arrays.asList(
        "BR","AE","A3","B1","B2","B3","BA","BC","BD","BE","BL","BX","CB3","CBR","D3","DE","E1","E2","E3","F3");

    static final Map<String, String> CODIGOS_AGNO_BONO;
    static final Map<String, String> CODIGOS_MES_BONO;
    static {
        CODIGOS_AGNO_BONO = new HashMap<>();
        String[] keys = {"1","2","3","4","5","6","7","8","9","0","A","B","C","D","E","F","G","H","I","J"};
        String[] anos  = {"1981","1982","1983","1984","1985","1986","1987","1988","1989","1990",
                          "1991","1992","1993","1994","1995","1996","1997","1998","1999","2000"};
        for (int i = 0; i < keys.length; i++) CODIGOS_AGNO_BONO.put(keys[i], anos[i]);

        CODIGOS_MES_BONO = new HashMap<>();
        for (int m = 1; m <= 9; m++) CODIGOS_MES_BONO.put(String.valueOf(m), String.format("%02d", m));
        CODIGOS_MES_BONO.put("A","10"); CODIGOS_MES_BONO.put("B","11"); CODIGOS_MES_BONO.put("C","12");
    }

    // ── Date helpers ─────────────────────────────────────────────────────────────

    static Date toDate(String s) {
        if (s == null || s.isBlank() || "0001-01-01".equals(s.substring(0, Math.min(10,s.length()))))
            return Date.valueOf("0001-01-01");
        return Date.valueOf(s.substring(0, 10));
    }

    static Time toTime(String s) {
        if (s == null || s.isBlank()) return Time.valueOf("00:00:00");
        return Time.valueOf(s.trim());
    }

    static String toStringDate(Date d) {
        return d == null ? "0001-01-01" : d.toLocalDate().toString();
    }

    static Timestamp toDateTime(String s) {
        if (s == null || s.isBlank()) return new Timestamp(0);
        try { return Timestamp.valueOf(s); } catch (Exception e) { return new Timestamp(0); }
    }

    /** Combines a local-date string with a UTC-time into a UTC Timestamp. */
    static Timestamp fechaSistemaHoraUTC(String fechaCreacion, Time horaUTC) {
        try {
            LocalDate ld = LocalDate.parse(fechaCreacion.substring(0, 10));
            LocalTime lt = horaUTC.toLocalTime();
            return Timestamp.from(ld.atTime(lt).toInstant(ZoneOffset.UTC));
        } catch (Exception e) {
            return new Timestamp(System.currentTimeMillis());
        }
    }

    static boolean eq(String a, String b) {
        return a != null && a.equalsIgnoreCase(b);
    }

    // ── MensajeSalida ────────────────────────────────────────────────────────────

    static final String TIPO_ERROR = "ERROR";
    static final String TIPO_INFO  = "INFORMACION";
    static final String TIPO_WARN  = "ADVERTENCIA";
    static final String TIPO_AUTH  = "AUTORIZACION";

    static class MensajeSalida {
        Long   IdInterno;
        String tipo;
        String codigo;
        String descripcion;

        MensajeSalida(Long id, String tipo, String codigo, String desc) {
            this.IdInterno = id; this.tipo = tipo; this.codigo = codigo; this.descripcion = desc;
        }
        MensajeSalida(Long id, String tipo, String codigo) { this(id, tipo, codigo, codigo); }

        static long contarErrores(List<MensajeSalida> list) {
            return list.stream().filter(m -> TIPO_ERROR.equals(m.tipo)).count();
        }
    }

    // ── Input/Output POJOs ───────────────────────────────────────────────────────

    static class Entrada {
        Long   p_IdEmpresa;
        Long   p_IdUsuario;
        String p_IdModulo;
        String p_simularOperacion = "N";
        String p_UsaReglasNegocio;
        String p_Reproceso = "N";
        List<Transaccion_Entrada> p_Transacciones = new ArrayList<>();
    }

    static class Salida {
        long P_OUT_ERRORESCONTADOR = 0L;
        List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        List<ResultadoSalida> P_OUT_RESULTADO = new ArrayList<>();
    }

    static class ResultadoSalida {
        Long   IdInterno;
        Long   IdTransaccion;
        String IdTipoOperacion;
        String EsAPlazo;
    }

    // ── Transaccion_Entrada ───────────────────────────────────────────────────────

    static class Transaccion_Entrada {
        Long   IdInterno;
        String IdMoneda;
        String IdMonedaEmision;
        String FolioTransaccion;
        String FechaCreacion;
        String HoraCreacion;
        String FechaEmision;
        String FechaVencimiento;
        Long   Plazo;
        String EsCuentaPropia;
        String EsFungible;
        String EsVentaCorta;
        String CodigoOperadorContraparte;
        String CodigoOperadorCorredor;
        String IdSistemaBursatil;
        String IdEmisor;
        BigDecimal Cantidad;
        BigDecimal Monto;
        BigDecimal Precio;
        BigDecimal Tasa;
        BigDecimal MontoPlazo;
        BigDecimal PrecioPlazo;
        BigDecimal ValorRescate;
        String IdTipoReajuste;
        Long   IdEmpleado;
        String IdTipoOperacion;
        String IdCondicionDeLiquidacionContado;
        Long   IdInstrumento;
        Long   IdCorredoraContraparte;
        String IdBolsa;
        String IdBoveda;
        Long   IdLibroOrdenes;
        Long   IdInstrumentoSubyacente;
        String EsFueraRueda;
        String IndicadorDCV;
        String EsBilateral;
        String MarcaNegociacion;
        String MarcaNegociacionContraparte = "";
        BigDecimal TipoCambioCorresponsal;
        Long   IdCorredoraCorresponsal;
        BigDecimal MontoLiquidacionCorresponsal;
        String IdMonedaLiquidacionCorresponsal;
        String FolioTransaccionPlazo;
        BigDecimal MejorPrecioCompra;
        BigDecimal MejorPrecioVenta;
        String NemotecnicoEspecificoBolson;
        String IndicadorOrdenDirecta = "";
        String NemotecnicoSVS = "";
        BigDecimal Intereses;
        BigDecimal Reajustes;
        BigDecimal GastosCorresponsal;
        String EsRuteo = "N";
        String FlujoOrigen = "INGRESOORDEN";
        String EsModificacionMarketData = "N";
        String EstadoModificacion;
        Long   IdTransaccionHistoricoAnulacion = 0L;
    }

    // ── InformacionTransaccion (from SQL_SELECT_TRANSACCIONES) ───────────────────

    static class InformacionTransaccion {
        String IdMonedaEmisionInstrumento;
        Long   IdInterno;
        Long   IdTransaccion;
        Date   FechaEmisionInstrumento;
        Date   FechaVencimientoInstrumento;
        String NemotecnicoInstrumento;
        String IdClasificacionTipoInstrumento;
        String IdTipoInstrumento;
        String AccionOrden;
        String IdEmisorInstrumento;
        String IdTipoReajusteInstrumento;
        String IdMonedaInstrumento;
        Long   IdPaisBolsa;
        String NemotecnicoInstrumentoSubyacente;
        Long   IdInstrumentoSubyacente;
        String IdMonedaInstrumentoSubyacente;
        String IdEmisorInstrumentoSubyacente;
        BigDecimal CorteMinimo;
        Long   IdCorredoraCorresponsal;
        String InstrumentoPermiteVentaCorta;
        String IdEstadoLibroOrdenes;
        Integer DiasLiquidacionContado;
        String NemotecnicoRelacionado;
        Long   IdInstrumentoRelacionado;
        String IdTipoOperacionRelacionada;
        String BolsaEsExtranjera;
        String IdTipoInstrumentoSubyacente;
    }

    // ── Transaccion_Proceso ───────────────────────────────────────────────────────

    static class Transaccion_Proceso {
        int ok, advertencias, autorizaciones, errores;
        String AccionOrden;
        Long   BaseTasa;
        BigDecimal CorteMinimo;
        BigDecimal Cantidad;
        String CodigoOperadorCorredor = "";
        String CodigoOperadorContraparte = "";
        Long   DiasLiquidacionContado;
        String EsFueraRueda;
        String EsBilateral;
        String EsVentaCorta;
        String EsVentaCortaOD = "N";
        String EsAPlazo;
        String EsFungible;
        String EsCuentaPropia;
        Date   FechaLiquidacion;
        Date   FechaTipoCambio;
        String FolioTransaccionPlazo;
        Timestamp FechaIngresoOferta;
        Date   FechaVencimiento;
        Date   FechaEmision;
        Date   FechaSistemaCreacion;
        String Folio;
        Timestamp FechaOperacion;
        Timestamp FechaModificacionOperacion;
        Date   FechaParidad;
        BigDecimal GastosCorresponsal;
        Time   HoraSistemaCreacion;
        String IdTipoOperacionRelacionada;
        Long   IdInstrumentoRelacionado;
        String IdEstadoLibroOrdenes;
        String InstrumentoPermiteVentaCorta;
        String IdMonedaLiquidacionCorresponsal;
        Long   IdCorredoraCorresponsal;
        String IndicadorDCV;
        Long   IdProgramaProcesaAutomatico = 0L;
        String IdBolsa;
        String IdBoveda;
        Long   IdCorredoraLocal;
        Long   IdCorredoraContraparte;
        String IdMonedaInstrumento;
        String IdMonedaEmision;
        Long   IdEmpleado;
        String IdTipoOperacion;
        String IdCondicionDeLiquidacion;
        String IdTipoReajuste;
        String IdClasificacionTipoInstrumento;
        String IdTipoInstrumento;
        Long   IdTransaccionOd = 0L;
        Long   IdTransaccionPlazo = 0L;
        String IdEstado;
        Long   IdInterno;
        Long   IdTransaccion;
        BigDecimal Intereses;
        Long   IdPaisBolsa;
        Long   IdLibroOrdenes;
        Long   IdEmpresa;
        String IdMonedaSubyacente;
        Long   IdInstrumentoSubyacente;
        Long   IdInstrumento;
        String IndicadorOrdenDirecta = "";
        String IdEmisor;
        String IdSistemaBursatil;
        String MarcaNegociacion = "";
        String MarcaNegociacionContraparte = "";
        BigDecimal MejorPrecioVenta;
        BigDecimal MontoLiquidacionCorresponsal;
        BigDecimal MejorPrecioCompra;
        BigDecimal MontoPlazo;
        BigDecimal Monto;
        String NemotecnicoSVS = "";
        String Nemotecnico;
        String NemotecnicoSubyacente;
        String NemotecnicoRelacionado;
        String ProcesaAutomatico = "N";
        Long   Plazo;
        BigDecimal PrecioPlazo;
        BigDecimal Precio;
        BigDecimal Reajustes;
        BigDecimal Tasa;
        String TipoInteres;
        BigDecimal TipoCambioCorresponsal;
        BigDecimal ValorTipoCambio;
        BigDecimal ValorParidad;
        BigDecimal ValorRescate;
        String IdCamara = "";
        String EsRuteo = "N";
        Long   IdUsuarioReserva = 0L;
        String Reservado = "N";
        String BolsaEsExtranjera = "N";
        String FlujoOrigen = "INGRESOORDEN";
        BigDecimal PrecioRaiz;
        BigDecimal MontoRaiz;
        String IdMonedaRaiz;
        Long   IdInstrumentoNegociacion = 0L;
        String TipoMensaje;
        String EsAsignacionDirecta = "N";
        String MarcaNegociacionCorrecta = "N";
        String EsModificacionMarketData = "N";
        String EstadoModificacion;
        Long   IdTransaccionHistoricoAnulacion = 0L;
        String MotivoReserva = "";
    }

    // ── Other POJOs ──────────────────────────────────────────────────────────────

    static class Empresa {
        Long   IdEmpresa;
        Long   IdCorredora;
        Long   IdPais;
    }

    static class LibrosOrdenes {
        Long   IdEmpresa;
        Long   IdLibroOrdenes;
        String CodigoOperador;
        String IdEstado;
    }

    static class LibrosOrdenesCambiar {
        Long IdLibroOrdenes;
        Long IdTransaccion;
    }

    static class RutPortafolio {
        String DocumentoIdentidad;
        String CodigoPortafolio;
        Long   IdLibroOrdenes;
        String NombreEstado;
    }

    static class FolioExistente {
        Long   IdTransaccion;
        String FolioFecha;
        FolioExistente() {}
        FolioExistente(Long id, String ff) { this.IdTransaccion = id; this.FolioFecha = ff; }
        @Override public boolean equals(Object o) {
            return o instanceof FolioExistente && FolioFecha != null && FolioFecha.equalsIgnoreCase(((FolioExistente)o).FolioFecha);
        }
        @Override public int hashCode() { return FolioFecha == null ? 0 : FolioFecha.toUpperCase().hashCode(); }
    }

    static class InformacionOperador {
        String CodigoOperador;
        String IdTipoInstrumento;
        String ReservaTransaccion;
        String AsignacionAuto;
        Long   IdPortafolio;
        String UtilizaPortafolio;
    }

    static class ConfiguracionAsignacion {
        String IdTipoInstrumento;
        String AsignacionAuto;
    }

    static class ConfiguracionShortName {
        String ShortName;
        String IdTipoInstrumento;
        String AsignacionAuto;
        Long   IdLibroOrdenes;
    }

    static class InstrumentoMonedaValidar {
        Long   IdInstrumento;
        String MonedaEmisionValida;
    }

    static class EstadoModulo {
        String IdEstadoModulo;
    }

    // ── claveMapa helper ──────────────────────────────────────────────────────────

    static String claveMapa(String... valores) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) sb.append("-");
            sb.append(valores[i] == null ? "" : valores[i].trim().toUpperCase());
        }
        return sb.toString();
    }

    static long toLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number) return ((Number)v).longValue();
        try { return Long.parseLong(v.toString().trim()); } catch (Exception e) { return 0L; }
    }

    static String safeStr(String s) { return s == null ? "" : s; }
}
