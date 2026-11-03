import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/** Solo lectura. Uso: java -cp jars EstadoSesiones.java <.env> hana|postgres <CODIGO>. Cuenta filas del usuario; nunca imprime tokens, claves ni datos personales. */
public class EstadoSesiones {

    public static void main(String[] a) throws Exception {
        Map<String, String> e = new HashMap<>();
        for (String l : Files.readAllLines(Path.of(a[0]), StandardCharsets.UTF_8)) {
            l = l.strip();
            if (l.isEmpty() || l.startsWith("#") || !l.contains("=")) continue;
            int i = l.indexOf('=');
            e.put(l.substring(0, i).strip(), l.substring(i + 1).strip());
        }
        String motor = a[1];
        String codigo = a[2].toUpperCase();
        DriverManager.setLoginTimeout(25);
        Properties p = new Properties();
        String url;
        String sqlUsuario, sqlSesiones, sqlLogs;
        if (motor.equals("hana")) {
            p.setProperty("user", e.get("qa01.saphana.dbuser"));
            p.setProperty("password", e.get("qa01.saphana.dbpass"));
            String prop = e.get("qa01.saphana.dbprop");
            if (prop != null) for (String kv : prop.split(";")) if (kv.contains("=")) p.setProperty(kv.substring(0, kv.indexOf('=')).strip(), kv.substring(kv.indexOf('=') + 1).strip());
            Class.forName(e.getOrDefault("qa01.saphana.dbdrv", "com.sap.db.jdbc.Driver"));
            url = e.get("qa01.saphana.dbURL");
            sqlUsuario = "select \"IdUsuario\", \"IntentosConexion\" from \"BSCL.Plataforma.BD::ACC.Usuario\" where upper(\"CodigoUsuario\") = ?";
            sqlSesiones = "select count(*), ifnull(sum(case when \"TokenValido\" = 'S' then 1 else 0 end), 0) from \"BSCL.Plataforma.BD::ACC.Sesion\" where \"IdUsuario\" = ?";
            sqlLogs = "select count(*) from \"BSCL.Plataforma.BD::PLA.Log\" where \"IdUsuario\" = ?";
        } else {
            p.setProperty("user", e.get("qa01.postgres.dbuser"));
            p.setProperty("password", e.get("qa01.postgres.dbpass"));
            url = e.get("qa01.postgres.dbURL");
            sqlUsuario = "select id_usuario, intentos_conexion from t_pla_acc_usuario where upper(codigo_usuario) = ?";
            sqlSesiones = "select count(*), coalesce(sum(case when token_valido = 'S' then 1 else 0 end), 0) from t_pla_acc_sesion where id_usuario = ?";
            sqlLogs = "select count(*) from t_pla_pla_log where id_usuario = ?";
        }
        try (Connection c = DriverManager.getConnection(url, p)) {
            c.setReadOnly(true);
            long id;
            String intentos;
            try (PreparedStatement ps = c.prepareStatement(sqlUsuario)) {
                ps.setString(1, codigo);
                try (ResultSet r = ps.executeQuery()) {
                    if (!r.next()) { System.out.println(motor + " usuario=no existe"); return; }
                    id = r.getLong(1);
                    intentos = r.getString(2);
                }
            }
            long totales, validas, logs;
            try (PreparedStatement ps = c.prepareStatement(sqlSesiones)) {
                ps.setLong(1, id);
                try (ResultSet r = ps.executeQuery()) { r.next(); totales = r.getLong(1); validas = r.getLong(2); }
            }
            try (PreparedStatement ps = c.prepareStatement(sqlLogs)) {
                ps.setLong(1, id);
                try (ResultSet r = ps.executeQuery()) { r.next(); logs = r.getLong(1); }
            }
            System.out.println(motor + " intentos=" + intentos + " sesiones_totales=" + totales + " sesiones_validas=" + validas + " logs_del_usuario=" + logs);
        } catch (SQLException ex) {
            System.out.println(motor + " ERROR estado=" + ex.getSQLState());
            System.exit(2);
        }
    }
}
