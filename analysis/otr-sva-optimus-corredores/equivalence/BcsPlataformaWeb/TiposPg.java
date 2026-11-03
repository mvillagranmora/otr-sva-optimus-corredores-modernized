import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/** Solo lectura de catalogo: firmas completas de las rutinas del login y atributos de los tipos compuestos que reciben. */
public class TiposPg {
    public static void main(String[] a) throws Exception {
        Map<String, String> e = new HashMap<>();
        for (String l : Files.readAllLines(Path.of(a[0]), StandardCharsets.UTF_8)) {
            l = l.strip();
            if (l.isEmpty() || l.startsWith("#") || !l.contains("=")) continue;
            int i = l.indexOf('=');
            e.put(l.substring(0, i).strip(), l.substring(i + 1).strip());
        }
        Properties p = new Properties();
        p.setProperty("user", e.get("qa01.postgres.dbuser"));
        p.setProperty("password", e.get("qa01.postgres.dbpass"));
        DriverManager.setLoginTimeout(25);
        try (Connection c = DriverManager.getConnection(e.get("qa01.postgres.dbURL"), p)) {
            c.setReadOnly(true);
            try (Statement s = c.createStatement()) { s.execute("set statement_timeout = '15s'"); }
            String[] rutinas = {"sp_pla_acc_consultar_usuario_admin", "fn_pla_consultar_formatos", "sp_pla_obtener_configuracion", "sp_pla_obtener_fechas",
                    "sp_pla_acc_recuperar_clave_usuario", "sp_pla_acc_confirmar_cambio_clave_usuario", "sp_pla_evt_agregar_evento", "sp_pla_obtener_fecha_hora_sistema"};
            for (String r : rutinas) {
                try (PreparedStatement ps = c.prepareStatement("select p.prokind::text, pg_get_function_arguments(p.oid), coalesce(pg_get_function_result(p.oid),'') from pg_proc p join pg_namespace n on n.oid=p.pronamespace where n.nspname='bscl' and p.proname = ?")) {
                    ps.setString(1, r);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) System.out.println("RUTINA " + r + " | " + rs.getString(1) + " | args: " + rs.getString(2) + " | retorna: " + rs.getString(3));
                    }
                }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "select t.typname, a.attnum, a.attname, format_type(a.atttypid, a.atttypmod) from pg_type t join pg_namespace n on n.oid=t.typnamespace join pg_class k on k.oid=t.typrelid join pg_attribute a on a.attrelid=k.oid and a.attnum>0 and not a.attisdropped where n.nspname='bscl' and t.typname like 'ty\\_pla\\_%' order by 1, 2")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) System.out.println("TIPO " + rs.getString(1) + " | " + rs.getInt(2) + " " + rs.getString(3) + " " + rs.getString(4));
                }
            }
        } catch (SQLException ex) {
            System.out.println("ERROR estado=" + ex.getSQLState() + " " + String.valueOf(ex.getMessage()).replace('\n', ' '));
        }
    }
}
