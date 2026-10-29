package cl.bcs.fondomutuo.consultas.custodia;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.fechas.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.fondomutuo.fechas.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.fondomutuo.fechas.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.fondomutuo.fechas.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;

/**
 * The next working day as CPF needs it (D-F3A-29): legacy {@code ObtenerDiaSiguiente.logica} for the ONLY mode CPF uses ({@code p_diaHabil = "S"}), but with the date arithmetic of the
 * legacy ON {@code java.sql.Date} + {@code java.util.Calendar} (Julian / Gregorian hybrid calendar of the JVM, zone of the JVM: UTC in the container). The shared port
 * {@code fechas.ObtenerDiaSiguiente} (F1 PROVEN, not modified) uses {@code LocalDate} (proleptic calendar) and differs from the legacy for the dates before 1582, which CPF reaches
 * with the default {@code FechaValorCuota} {@code 0001-01-01} of a balance with no valuation: the legacy asks the parity for {@code 0001-01-03} (0001-01-01 is a Saturday there),
 * the shared port for {@code 0001-01-02}. For every date after 1582 both give the same days. The holidays are read through {@link CalendarioPort} (verbatim
 * {@code ConsultarFeriados.sql}). No state.
 *
 * <p>DUPLICATED ON PURPOSE with {@link cl.bcs.fondomutuo.fechas.ObtenerDiaSiguiente} (D-F3A-29: the shared class is PROVEN and is not modified for CPF): a fix to the day-skipping
 * logic must be considered in BOTH classes. Both carry the same safety stop ({@link #MAX_DIAS_INHABILES_CONSECUTIVOS}) against a calendar with every weekday non-working.
 */
public final class ConsultarCustodiaPortafolioDiaSiguiente {

    /** Architecture review F3 W1 (M2): about 10 years of consecutive non-working days; beyond it the legacy loop would never end, here it fails instead. */
    static final int MAX_DIAS_INHABILES_CONSECUTIVOS = 3700;

    private final CalendarioPort calendario;

    public ConsultarCustodiaPortafolioDiaSiguiente(CalendarioPort calendario) {
        this.calendario = calendario;
    }

    /** Legacy {@code logica} with {@code p_diaHabil = "S"} (the input is mutated as the legacy did: {@code FechasIn.pais}). */
    public OutObtenerDiaSiguiente logica(InObtenerDiaSiguiente entrada) throws Exception {
        OutObtenerDiaSiguiente salida = new OutObtenerDiaSiguiente();
        salida.P_OUT_ERRORESCONTADOR = Long.valueOf(0);
        entrada.p_soloCaeDiaHabil = "S";

        List<Long> paises = new ArrayList<>();
        HashMap<Long, Object> hashFechas = new HashMap<Long, Object>();
        for (FechasIn f : entrada.p_fechas) {
            f.pais = obtenerPaisConsulta(entrada.p_idPais, f.pais);
            if (!hashFechas.containsKey(f.pais)) {
                paises.add(f.pais);
                hashFechas.put(f.pais, new Object());
            }
        }

        HashMap<String, Object> hashDiaFeriado = new HashMap<String, Object>();
        HashMap<String, Object> hashDiaInhabil = new HashMap<String, Object>();
        for (Map<String, Object> fila : calendario.feriados(paises)) {
            String tipo = (String) fila.get("Tipo");
            Object pais = fila.get("IdPais");
            Long idPais = pais == null ? null : (pais instanceof Long l ? l : Long.valueOf(((Number) pais).longValue()));
            if ("FERIADO".equals(tipo)) {
                hashDiaFeriado.put(tipo + "_" + textoFecha(fila.get("Fecha")) + "_" + idPais, fila);
            }
            if ("INHABIL".equals(tipo)) {
                Object dia = fila.get("Dia");
                Integer d = dia == null ? null : Integer.valueOf(((Number) dia).intValue());
                hashDiaInhabil.put(tipo + "_" + d + "_" + idPais, fila);
            }
        }

        for (FechasIn item : entrada.p_fechas) {
            Long idPais = obtenerPaisConsulta(entrada.p_idPais, item.pais);
            FechasOut temp = new FechasOut();
            temp.fechaInicial = item.fechaInicial;
            temp.numeroDiasInicial = item.numeroDias;
            temp.numeroDias = item.numeroDias;
            temp.fechaCalculada = item.fechaInicial;
            temp.IdInterno = item.IdInterno;

            boolean buscar = item.numeroDias == null || item.numeroDias < 1 ? false : true;
            int diasSumados = 0;
            int diasSumadosReal = 0;
            java.sql.Date fPivot = java.sql.Date.valueOf(FechaLegacy.aFecha(temp.fechaInicial));

            while (buscar) {
                fPivot = sumarDias(fPivot, 1);
                diasSumados++;
                diasSumadosReal++;

                Calendar c = Calendar.getInstance();
                boolean esinhabil = true;
                int consecutivos = 0;
                while (esinhabil) {
                    esinhabil = false;
                    c.setTime(fPivot);
                    int day = c.get(Calendar.DAY_OF_WEEK) == 1 ? 6 : c.get(Calendar.DAY_OF_WEEK) - 2;   // java Sunday = 1, optimus Monday = 0
                    if (hashDiaFeriado.get("FERIADO_" + FechaLegacy.aTexto(fPivot.toLocalDate()) + "_" + idPais) != null
                            || hashDiaInhabil.get("INHABIL_" + day + "_" + idPais) != null) {
                        if (++consecutivos > MAX_DIAS_INHABILES_CONSECUTIVOS) {
                            throw new IllegalStateException("No working day found after " + MAX_DIAS_INHABILES_CONSECUTIVOS + " consecutive non-working days (country " + idPais + ")");
                        }
                        esinhabil = true;
                        fPivot = sumarDias(fPivot, 1);
                        diasSumadosReal++;
                    }
                }

                if (diasSumados == item.numeroDias)
                    break;
            }
            temp.fechaCalculada = FechaLegacy.aTexto(fPivot.toLocalDate());
            temp.numeroDias = (long) diasSumadosReal;
            temp.idPais = idPais;
            salida.p_out_fechas.add(temp);
        }
        return salida;
    }

    /** Legacy {@code Fecha.addDays(java.sql.Date, int)}: {@code Calendar.add(DAY_OF_WEEK, days)} on the hybrid calendar of the JVM. */
    private static java.sql.Date sumarDias(java.sql.Date fecha, int dias) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        cal.add(Calendar.DAY_OF_WEEK, dias);
        return new java.sql.Date(cal.getTime().getTime());
    }

    private static String textoFecha(Object fecha) {
        if (fecha instanceof java.sql.Date sql) {
            return sql.toString();
        }
        if (fecha instanceof java.util.Date util) {
            return new java.sql.Date(util.getTime()).toString();
        }
        return String.valueOf(fecha);
    }

    private static Long obtenerPaisConsulta(Long paisEntrada, Long paisFecha) {
        return paisFecha == null && paisEntrada != null ? paisEntrada : paisFecha;
    }
}
