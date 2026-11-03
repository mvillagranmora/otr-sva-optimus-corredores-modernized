package cl.bcs.plataforma.login.dialecto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fixtures of the LOGIN-PG round 2 tests (src/test/resources/loginpg2/): the column oracle of the 7 views (from the Postgres QA catalog, NOT from the xlsx:
 * the xlsx covers tables only) and the signatures of the Postgres routines and composite types. A missing fixture is a FAILURE ("FIXTURE MISSING"), never a skip.
 */
public final class LoginPg2Recursos {

    private LoginPg2Recursos() {
    }

    public static final String DIR = "/loginpg2/";

    public static String texto(String archivo) {
        return LoginPgRecursos.texto(DIR + archivo);
    }

    // ------------------------------------------------------------------------------------------------ the view columns oracle

    public record ColumnaVista(String vistaHana, String vistaPg, String columnaHana, String columnaPg, String origen) {
    }

    public static final class OraculoVistas {
        /** HANA view -> (HANA column -> Postgres column), in the order of the catalog */
        public final Map<String, Map<String, String>> columnas = new LinkedHashMap<>();
        public final Map<String, String> vistaPg = new LinkedHashMap<>();
        public final List<ColumnaVista> filas = new ArrayList<>();
        /** Postgres view -> (Postgres column -> Postgres type), straight from the catalog rows */
        public final Map<String, Map<String, String>> tiposPg = new LinkedHashMap<>();

        public static OraculoVistas cargar() {
            OraculoVistas o = new OraculoVistas();
            List<List<String>> csv = LoginPgRecursos.csv(texto("oraculo-vistas-columnas.csv"));
            for (List<String> f : csv.subList(1, csv.size())) {
                o.filas.add(new ColumnaVista(f.get(0), f.get(1), f.get(2), f.get(3), f.get(4)));
                o.columnas.computeIfAbsent(f.get(0), k -> new LinkedHashMap<>()).put(f.get(2), f.get(3));
                o.vistaPg.put(f.get(0), f.get(1));
            }
            for (String linea : texto("vistas-columnas-pg-qa-20261103.tsv").split("\n")) {
                if (linea.isBlank()) continue;
                String[] p = linea.split("\t");
                o.tiposPg.computeIfAbsent(p[0], k -> new LinkedHashMap<>()).put(p[1], p[2]);
            }
            return o;
        }
    }

    /** columns of a table (xlsx oracle) or of a view (catalog oracle): HANA column -> Postgres column */
    public static Map<String, String> columnasDe(String objetoHana, LoginPgRecursos.Oraculo xlsx, OraculoVistas vistas) {
        Map<String, String> c = xlsx.columnas.get(objetoHana);
        if (c == null) c = vistas.columnas.get(objetoHana);
        if (c == null) throw new AssertionError("no column oracle for " + objetoHana);
        return c;
    }

    // ------------------------------------------------------------------------------------------------ routines and composite types

    public record Parametro(int posicion, String modo, String nombre, String tipo, boolean conDefault) {
        public boolean esEntrada() {
            return modo.equals("IN") || modo.equals("INOUT");
        }

        public boolean esSalida() {
            return modo.equals("OUT") || modo.equals("INOUT");
        }
    }

    public record Atributo(String nombre, String tipo) {
    }

    public record Rutina(String nombre, String clase, List<Parametro> parametros, List<Atributo> columnasRetorno) {
        public Parametro parametro(String nombre) {
            return parametros.stream().filter(p -> p.nombre().equals(nombre)).findFirst().orElseThrow(() -> new AssertionError("no parameter " + nombre + " in " + this.nombre));
        }

        public int indice(String nombre) {
            return parametro(nombre).posicion();
        }

        public boolean esFuncion() {
            return clase.equals("f");
        }
    }

    public static final class Rutinas {
        public final Map<String, Rutina> rutinas = new LinkedHashMap<>();
        public final Map<String, List<Atributo>> tipos = new LinkedHashMap<>();

        public static Rutinas cargar() {
            Rutinas r = new Rutinas();
            for (String linea : texto("rutinas-y-tipos-postgres-qa-20261103.txt").split("\n")) {
                if (linea.isBlank()) continue;
                if (linea.startsWith("RUTINA ")) {
                    String[] p = linea.substring(7).split(" \\| ", -1);
                    String nombre = p[0].strip();
                    String clase = p[1].strip();
                    String args = p[2].strip().substring("args:".length()).strip();
                    String retorna = p.length > 3 ? p[3].strip().substring("retorna:".length()).strip() : "";
                    List<Parametro> ps = new ArrayList<>();
                    if (!args.isEmpty()) {
                        int pos = 1;
                        for (String a : args.split(", ")) {
                            String[] t = a.strip().split(" ", 3);
                            String resto = t[2];
                            boolean def = resto.contains(" DEFAULT ");
                            String tipo = def ? resto.substring(0, resto.indexOf(" DEFAULT ")) : resto;
                            ps.add(new Parametro(pos++, t[0], t[1], tipo, def));
                        }
                    }
                    List<Atributo> cols = new ArrayList<>();
                    if (retorna.startsWith("TABLE(")) {
                        String cuerpo = retorna.substring(6, retorna.length() - 1);
                        for (String c : cuerpo.split(", ")) {
                            int i = c.indexOf(' ');
                            cols.add(new Atributo(c.substring(0, i), c.substring(i + 1)));
                        }
                    }
                    r.rutinas.put(nombre, new Rutina(nombre, clase, ps, cols));
                } else if (linea.startsWith("TIPO ")) {
                    String[] p = linea.substring(5).split(" \\| ");
                    String[] t = p[1].split(" ", 3);
                    r.tipos.computeIfAbsent(p[0].strip(), k -> new ArrayList<>()).add(new Atributo(t[1], t[2]));
                }
            }
            return r;
        }

        public Rutina rutina(String nombre) {
            Rutina x = rutinas.get(nombre);
            if (x == null) throw new AssertionError("no routine " + nombre + " in the signatures fixture");
            return x;
        }
    }

    // ------------------------------------------------------------------------------------------------ the naming rules of the answer

    /** id_uso_moneda_formato -> IdUsoMonedaFormato (the HANA name of a column of the formats function and of a field of an event row) */
    public static String pascal(String snake) {
        StringBuilder sb = new StringBuilder();
        for (String p : snake.split("_")) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }

    /** p_out_es_admin -> P_OUT_ESADMIN, p_status_code -> P_STATUSCODE: the legacy OUT name (upper case, no underscore after the prefix) */
    public static String nombreLegadoDeSalida(String pg) {
        String prefijo = pg.startsWith("p_out_") ? "p_out_" : "p_";
        return prefijo.toUpperCase(Locale.ROOT) + pg.substring(prefijo.length()).replace("_", "").toUpperCase(Locale.ROOT);
    }

    /** the key form under which a legacy JSON key and a Postgres name are the same parameter: lower case without underscores */
    public static String normalizar(String nombre) {
        return nombre.replace("_", "").toLowerCase(Locale.ROOT);
    }

    // ------------------------------------------------------------------------------------------------ the eight procedures of the login

    /** constant of the login class -> full HANA name, as in the goldens */
    public static final Map<String, String> HANA = new LinkedHashMap<>();

    static {
        HANA.put("BootstrapServicio.SP_USUARIO_ADMIN", "BSCL.Plataforma.BD.Servicios::ACC.ConsultarUsuarioAdmin");
        HANA.put("BootstrapServicio.SP_OBTENER_CONFIGURACION", "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion");
        HANA.put("BootstrapServicio.SP_FORMATOS", "BSCL.Plataforma.BD.Servicios::ConsultarFormatos");
        HANA.put("BootstrapServicio.SP_FECHAS", "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas");
        HANA.put("GestionClaveServicio.SP_RECUPERAR", "BSCL.Plataforma.BD.Servicios::ACC.RecuperarClaveUsuario");
        HANA.put("GestionClaveServicio.SP_CONFIRMAR", "BSCL.Plataforma.BD.Servicios::ACC.ConfirmarCambioClaveUsuario");
        HANA.put("GestionClaveServicio.SP_AGREGAR_EVENTO", "BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento");
        HANA.put("GestionClaveServicio.SP_FECHA_HORA", "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema");
    }

    public static String hana(String constante) {
        return HANA.get("BootstrapServicio." + constante) != null ? HANA.get("BootstrapServicio." + constante) : HANA.get("GestionClaveServicio." + constante);
    }

    public static final String ADMIN = HANA.get("BootstrapServicio.SP_USUARIO_ADMIN");
    public static final String CONFIG = HANA.get("BootstrapServicio.SP_OBTENER_CONFIGURACION");
    public static final String FORMATOS = HANA.get("BootstrapServicio.SP_FORMATOS");
    public static final String FECHAS = HANA.get("BootstrapServicio.SP_FECHAS");
    public static final String RECUPERAR = HANA.get("GestionClaveServicio.SP_RECUPERAR");
    public static final String CONFIRMAR = HANA.get("GestionClaveServicio.SP_CONFIRMAR");
    public static final String EVENTO = HANA.get("GestionClaveServicio.SP_AGREGAR_EVENTO");
    public static final String FECHA_HORA = HANA.get("GestionClaveServicio.SP_FECHA_HORA");
    /** a procedure that exists in the catalog and in Postgres but is NOT one of the eight of the login */
    public static final String OTRO = "BSCL.Plataforma.BD.Servicios::AgregarLog";
}
