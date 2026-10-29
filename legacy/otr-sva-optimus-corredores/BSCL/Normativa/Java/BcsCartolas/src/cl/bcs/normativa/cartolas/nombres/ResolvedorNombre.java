package cl.bcs.normativa.cartolas.nombres;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;

public abstract class ResolvedorNombre {

    private static final Map<String, ResolvedorNombre> resolvedorMap;

    static {
        Map<String, ResolvedorNombre> aMap = new HashMap();
        aMap.put("YYYYMMDD", new ResolvedorFecha());
        aMap.put("YYYYMMDDHHMMSS", new ResolvedorFechaHora());
        aMap.put("NombreEmpresa", new ResolvedorNombreEmpresa());
        aMap.put("DocumentoCliente", new ResolvedorDocumentoCliente());
        aMap.put("CodigoPortafolio", new ResolvedorCodigoPortafolio());
        aMap.put("IdPortafolio", new ResolvedorIdPortafolio());
        aMap.put("IdCliente", new ResolvedorIdCliente());
        resolvedorMap = Collections.unmodifiableMap(aMap);
    }

    public static Set<String> getKeys(
            final String patron) {
        final Set<String> set = new HashSet();
        int x = 0;
        do {
            x = patron.indexOf('{', x);
            if (x < 0) {
                break;
            }

            int y = patron.indexOf('}', x);
            if (y > x) {
                final String key = patron.substring(x + 1, y);
                set.add(key);
                x = y + 1;
            } else {
                x++;
            }
        } while (x < patron.length());

        return set;
    }

    public static String getNombreArchivoCartola(
            final ContextoCartolas contexto,
            final Cartola cartola)
                    throws SQLException {
        final String patron = contexto.getConfigs().getFormatoNombreArchivoCartola();
        return ResolvedorNombre.traducir(contexto, patron, cartola) + ".pdf";
    }

    public static String getNombreArchivoNomina(
            ContextoCartolas contexto)
                    throws SQLException {
        final String patron = contexto.getConfigs().getFormatoNombreArchivoNomina();
        return ResolvedorNombre.traducir(contexto, patron, null) + ".txt";
    }

    public static String getNombreArchivoZip(
            final ContextoCartolas contexto)
                    throws SQLException {
        final String patron = contexto.getConfigs().getFormatoNombreArchivoZip();
        return ResolvedorNombre.traducir(contexto, patron, null) + ".zip";
    }

    public static final String traducir(
            final ContextoCartolas contexto,
            final String patron,
            final Cartola cartola)
                    throws SQLException {
        String ret = patron;
        for (final String key : getKeys(patron)) {
            ResolvedorNombre resolvedor = resolvedorMap.get(key);
            if (resolvedor != null) {
                final String texto = resolvedor.resolver(contexto, cartola);
                ret = ret.replaceAll("\\{" + key + "\\}", texto);
            }
        }

        return ret;
    }

    public abstract String resolver(
            final ContextoCartolas contexto,
            final Cartola cartola)
                    throws SQLException;

}
