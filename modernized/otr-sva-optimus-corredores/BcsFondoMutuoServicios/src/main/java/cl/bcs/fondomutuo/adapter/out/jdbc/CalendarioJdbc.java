package cl.bcs.fondomutuo.adapter.out.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;

/**
 * {@link CalendarioPort}: the reads of the date helpers. The two files are the legacy SQL verbatim; the clock is the legacy
 * {@code FechaDB.ObtenerFechaHoraSistemaLocal} WITHOUT its static one-hour cache (DEV-F2): {@code SELECT CURRENT_UTCTIMESTAMP FROM DUMMY} is read in
 * the call, so there is no state between requests. The date part is {@code PLA.FechaSistema}; the shift is {@code PLA.SegundosZonaHoraria}.
 */
@Component
public class CalendarioJdbc implements CalendarioPort {

    private static final String SQL_FERIADOS = SqlRecursos.kit("ConsultarFeriados.sql");
    private static final String SQL_MONEDA_FORMATO = SqlRecursos.kit("Formato_ConsultarMonedaFormato.sql");

    private final ConexionUnidadDeTrabajo conexion;
    private final ConfiguracionSistemaPort configuracion;

    public CalendarioJdbc(ConexionUnidadDeTrabajo conexion, ConfiguracionSistemaPort configuracion) {
        this.conexion = conexion;
        this.configuracion = configuracion;
    }

    @Override
    public List<Map<String, Object>> feriados(List<Long> idPaises) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdPais", new ArrayList<>(idPaises));
        return conexion.usar(con -> SqlNombrado.filas(con, SQL_FERIADOS, params));
    }

    @Override
    public List<Map<String, Object>> monedaFormato() {
        return conexion.usar(con -> SqlNombrado.filas(con, SQL_MONEDA_FORMATO, new HashMap<>()));
    }

    @Override
    public Timestamp fechaHoraSistemaLocal(Long idEmpresa) {
        String fechaSistema = configuracion.texto(idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        long segundos = configuracion.entero(idEmpresa, "PLATAFORMA", "PLA.SegundosZonaHoraria");
        Timestamp utc = conexion.usar(con -> {
            try (PreparedStatement st = con.prepareStatement("SELECT CURRENT_UTCTIMESTAMP as UTC  FROM DUMMY");
                    ResultSet rs = st.executeQuery()) {
                return rs.next() ? rs.getTimestamp("UTC") : null;
            }
        });
        if (utc == null) {
            throw new IllegalStateException("No se puede obtener cache UTC");
        }
        Timestamp local = new Timestamp(utc.getTime() + segundos * 1000L);
        // legacy: FechaSistema + " " + Fecha.toStringTime(local): the day is the system date, the time of day is the shifted clock
        return Timestamp.valueOf(fechaSistema.substring(0, 10) + " " + local.toLocalDateTime().toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
