package cl.bcs.custodia.flujo.kit;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Port of Plataforma {@code ObtenerDiaSiguiente.logica}, bug for bug, as ConsultarFlujoCustodia uses it (CUS-FLU-06): adds days to a date skipping
 * the holidays and non-working weekdays of ONE country per date when {@code p_diaHabil = "S"}. The holiday table comes from
 * {@link PlataformaLecturas#feriados}; dates are {@link LocalDate} (legacy: {@code java.sql.Date} + {@code Calendar}, the same days under UTC).
 * The input is mutated as the legacy did ({@code p_soloCaeDiaHabil}, {@code FechasIn.pais}).
 */
public final class DiaSiguiente {

    private DiaSiguiente() {
    }

    public static class Entrada {
        public Long p_idPais;
        public String p_diaHabil;
        public String p_soloCaeDiaHabil;
        public String p_consideraHabilAnterior = "N";
        public List<FechaEntrada> p_fechas = new ArrayList<>();
    }

    public static class FechaEntrada {
        public String fechaInicial;
        public Long numeroDias;
        public Long pais;
        public Long IdInterno = 0L;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR;
        public List<FechaSalida> p_out_fechas = new ArrayList<>();
    }

    public static class FechaSalida {
        public String fechaInicial;
        public Long numeroDiasInicial;
        public Long numeroDias;
        public String fechaCalculada;
        public Long idPais;
        public Long IdInterno;
    }

    public static Salida logica(Connection con, Entrada entrada) throws Exception {
        Salida salida = new Salida();
        salida.P_OUT_ERRORESCONTADOR = 0L;

        entrada.p_soloCaeDiaHabil = entrada.p_diaHabil.equalsIgnoreCase("S") ? "S" : entrada.p_soloCaeDiaHabil == null ? "N" : entrada.p_soloCaeDiaHabil;

        List<Long> paises = new ArrayList<>();
        HashMap<Long, Object> hashFechas = new HashMap<>();
        for (FechaEntrada f : entrada.p_fechas) {
            f.pais = paisConsulta(entrada.p_idPais, f.pais);
            if (!hashFechas.containsKey(f.pais)) {
                paises.add(f.pais);
                hashFechas.put(f.pais, new Object());
            }
        }

        HashMap<String, Object> hashDiaFeriado = new HashMap<>();
        HashMap<String, Object> hashDiaInhabil = new HashMap<>();
        for (Map<String, Object> fila : PlataformaLecturas.feriados(con, paises)) {
            String tipo = (String) fila.get("Tipo");
            LocalDate fecha = fecha(fila.get("Fecha"));
            Object dia = fila.get("Dia");
            Integer diaInt = dia == null ? null : Integer.valueOf(((Number) dia).intValue());
            Object pais = fila.get("IdPais");
            Long idPais = pais == null ? null : Long.valueOf(((Number) pais).longValue());
            if ("FERIADO".equals(tipo) && !hashDiaFeriado.containsKey(tipo + "_" + fecha)) {
                hashDiaFeriado.put(tipo + "_" + fecha + "_" + idPais, fila);
            }
            if ("INHABIL".equals(tipo) && !hashDiaInhabil.containsKey(tipo + "_" + diaInt)) {
                hashDiaInhabil.put(tipo + "_" + diaInt + "_" + idPais, fila);
            }
        }

        for (FechaEntrada item : entrada.p_fechas) {
            Long idPais = paisConsulta(entrada.p_idPais, item.pais);
            FechaSalida temp = new FechaSalida();
            temp.fechaInicial = item.fechaInicial;
            temp.numeroDiasInicial = item.numeroDias;
            temp.numeroDias = item.numeroDias;
            temp.fechaCalculada = item.fechaInicial;
            temp.IdInterno = item.IdInterno;

            boolean buscar = !(item.numeroDias == null || item.numeroDias < 1);
            int diasSumados = 0;
            int diasSumadosReal = 0;
            LocalDate pivote = FechaLegacy.aFecha(temp.fechaInicial);

            while (buscar) {
                pivote = pivote.plusDays(1);
                diasSumados++;
                diasSumadosReal++;

                if (entrada.p_diaHabil.equals("S")) {
                    boolean inhabil = true;
                    while (inhabil) {
                        inhabil = false;
                        int dia = FechaLegacy.diaOptimus(pivote);
                        if (esInhabil(hashDiaFeriado, hashDiaInhabil, FechaLegacy.aTexto(pivote), dia, idPais)) {
                            inhabil = true;
                            pivote = pivote.plusDays(1);
                            diasSumadosReal++;
                        }
                    }
                }
                if (diasSumados == item.numeroDias) {
                    break;
                }
            }
            temp.fechaCalculada = FechaLegacy.aTexto(pivote);
            temp.numeroDias = (long) diasSumadosReal;
            temp.idPais = idPais;
            salida.p_out_fechas.add(temp);
        }

        if (entrada.p_diaHabil.equalsIgnoreCase("N") && entrada.p_soloCaeDiaHabil.equalsIgnoreCase("S")) {
            for (FechaSalida out : salida.p_out_fechas) {
                boolean inhabil = true;
                LocalDate pivote = FechaLegacy.aFecha(out.fechaCalculada);
                while (inhabil) {
                    inhabil = false;
                    int dia = FechaLegacy.diaOptimus(pivote);
                    if (esInhabil(hashDiaFeriado, hashDiaInhabil, FechaLegacy.aTexto(pivote), dia, out.idPais)
                            && entrada.p_consideraHabilAnterior.equalsIgnoreCase("S")) {
                        inhabil = true;
                        pivote = pivote.plusDays(-1);
                        out.numeroDias--;
                    } else if (esInhabil(hashDiaFeriado, hashDiaInhabil, FechaLegacy.aTexto(pivote), dia, out.idPais)) {
                        inhabil = true;
                        pivote = pivote.plusDays(1);
                        out.numeroDias++;
                    }
                }
                out.fechaCalculada = FechaLegacy.aTexto(pivote);
            }
        }
        return salida;
    }

    private static Long paisConsulta(Long paisEntrada, Long paisFecha) {
        return paisFecha == null && paisEntrada != null ? paisEntrada : paisFecha;
    }

    private static LocalDate fecha(Object valor) {
        if (valor instanceof java.sql.Date sql) {
            return sql.toLocalDate();
        }
        if (valor instanceof java.util.Date util) {
            return new java.sql.Date(util.getTime()).toLocalDate();
        }
        return null;
    }

    private static boolean esInhabil(Map<String, Object> feriados, Map<String, Object> inhabiles, String fecha, int dia, Long idPais) {
        return feriados.get("FERIADO_" + fecha + "_" + idPais) != null || inhabiles.get("INHABIL_" + dia + "_" + idPais) != null;
    }
}
