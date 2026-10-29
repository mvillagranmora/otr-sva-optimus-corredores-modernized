package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

/**
 * Migrated from legacy cl.bcs.plataforma.pla.ObtenerDiaSiguiente.
 * Calculates next business day considering holidays and non-working days.
 */
@Service
public class ObtenerDiaSiguienteService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ObtenerDiaSiguienteService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_CONSULTAR_FERIADOS;

    public ObtenerDiaSiguienteService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_CONSULTAR_FERIADOS = loadSql("cl/bcs/plataforma/pla/sql/ConsultarFeriados.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.ObtenerDiaSiguiente"; }

    @Override
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ObtenerDiaSiguiente");
        InObtenerDiaSiguiente in = GSON.fromJson(params, InObtenerDiaSiguiente.class);

        if (in.p_diaHabil == null) in.p_diaHabil = "N";
        if (in.p_consideraHabilAnterior == null) in.p_consideraHabilAnterior = "N";
        in.p_soloCaeDiaHabil = "S".equalsIgnoreCase(in.p_diaHabil) ? "S"
            : (in.p_soloCaeDiaHabil == null ? "N" : in.p_soloCaeDiaHabil);

        OutObtenerDiaSiguiente out = logica(in);
        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public OutObtenerDiaSiguiente logica(InObtenerDiaSiguiente entrada) {
        OutObtenerDiaSiguiente salida = new OutObtenerDiaSiguiente();
        salida.P_OUT_ERRORESCONTADOR = 0L;

        // Collect all paises needed
        List<Long> paises = new ArrayList<>();
        for (FechasIn f : entrada.p_fechas) {
            f.pais = obtenerPaisConsulta(entrada.p_idPais, f.pais);
            if (!paises.contains(f.pais)) paises.add(f.pais);
        }

        // Load holiday data
        Map<String, Object> parmsFeriado = new HashMap<>();
        parmsFeriado.put("IdPais", paises);
        List<DiaFeriado> listaDiaFeriado = jdbc.query(SQL_CONSULTAR_FERIADOS, parmsFeriado,
            BeanPropertyRowMapper.newInstance(DiaFeriado.class));

        Map<String, DiaFeriado> hashDiaFeriado = new HashMap<>();
        Map<String, DiaFeriado> hashDiaInhabil = new HashMap<>();
        for (DiaFeriado df : listaDiaFeriado) {
            if ("FERIADO".equals(df.Tipo)) {
                String key = "FERIADO_" + df.Fecha + "_" + df.IdPais;
                if (!hashDiaFeriado.containsKey(key)) hashDiaFeriado.put(key, df);
            } else if ("INHABIL".equals(df.Tipo)) {
                String key = "INHABIL_" + df.Dia + "_" + df.IdPais;
                if (!hashDiaInhabil.containsKey(key)) hashDiaInhabil.put(key, df);
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
            temp.idPais = idPais;

            boolean buscar = item.numeroDias != null && item.numeroDias >= 1;
            int diasSumados = 0;
            int diasSumadosReal = 0;
            LocalDate fPivot = LocalDate.parse(temp.fechaInicial);

            while (buscar) {
                fPivot = fPivot.plusDays(1);
                diasSumados++;
                diasSumadosReal++;

                if ("S".equals(entrada.p_diaHabil)) {
                    boolean esinhabil = true;
                    while (esinhabil) {
                        esinhabil = false;
                        int day = javaDayToOptimus(fPivot.getDayOfWeek().getValue());
                        if (esInhabil(hashDiaFeriado, hashDiaInhabil, fPivot.toString(), day, idPais)) {
                            esinhabil = true;
                            fPivot = fPivot.plusDays(1);
                            diasSumadosReal++;
                        }
                    }
                }

                if (diasSumados == item.numeroDias) break;
            }

            temp.fechaCalculada = fPivot.toString();
            temp.numeroDias = (long) diasSumadosReal;
            salida.p_out_fechas.add(temp);
        }

        // If soloCaeDiaHabil = S and diaHabil = N → adjust result to be a working day
        if ("N".equalsIgnoreCase(entrada.p_diaHabil) && "S".equalsIgnoreCase(entrada.p_soloCaeDiaHabil)) {
            for (FechasOut out : salida.p_out_fechas) {
                boolean esinhabil = true;
                LocalDate fPivot = LocalDate.parse(out.fechaCalculada);
                while (esinhabil) {
                    esinhabil = false;
                    int day = javaDayToOptimus(fPivot.getDayOfWeek().getValue());
                    if (esInhabil(hashDiaFeriado, hashDiaInhabil, fPivot.toString(), day, out.idPais)) {
                        esinhabil = true;
                        if ("S".equalsIgnoreCase(entrada.p_consideraHabilAnterior)) {
                            fPivot = fPivot.minusDays(1);
                            out.numeroDias--;
                        } else {
                            fPivot = fPivot.plusDays(1);
                            out.numeroDias++;
                        }
                    }
                }
                out.fechaCalculada = fPivot.toString();
            }
        }

        log.debug("FIN::ObtenerDiaSiguiente");
        return salida;
    }

    /** Java DayOfWeek (Mon=1..Sun=7) → Optimus (Mon=0..Sun=6) */
    private int javaDayToOptimus(int javaDow) {
        return javaDow == 7 ? 6 : javaDow - 1;
    }

    private Long obtenerPaisConsulta(Long paisEntrada, Long paisFecha) {
        return (paisFecha == null && paisEntrada != null) ? paisEntrada : paisFecha;
    }

    private boolean esInhabil(Map<String, DiaFeriado> hashFeriado, Map<String, DiaFeriado> hashInhabil,
                               String fecha, int dia, Long idPais) {
        return hashFeriado.get("FERIADO_" + fecha + "_" + idPais) != null
            || hashInhabil.get("INHABIL_" + dia + "_" + idPais) != null;
    }

    // ---- DTOs ----
    public static class InObtenerDiaSiguiente {
        public Long p_idPais;
        public String p_diaHabil;
        public String p_soloCaeDiaHabil;
        public String p_consideraHabilAnterior;
        public List<FechasIn> p_fechas = new ArrayList<>();
    }

    public static class FechasIn {
        public String fechaInicial;
        public Long numeroDias;
        public Long pais;
        public Long IdInterno = 0L;
    }

    public static class OutObtenerDiaSiguiente {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<FechasOut> p_out_fechas = new ArrayList<>();
    }

    public static class FechasOut {
        public String fechaInicial;
        public Long numeroDiasInicial;
        public Long numeroDias;
        public String fechaCalculada;
        public Long idPais;
        public Long IdInterno;
    }

    public static class DiaFeriado {
        public Long IdFeriado;
        public String Tipo;
        public Date Fecha;
        public Integer Dia;
        public String DescripcionDia;
        public Integer AgnoCarga;
        public Long IdPais;
    }

    private static String loadSql(String path) {
        try (InputStream is = ObtenerDiaSiguienteService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
