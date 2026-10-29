package cl.bcs.apv.application.recaudacion;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import cl.bcs.apv.domain.port.out.DiccionarioMensajes;

/**
 * Legacy {@code cl.bcs.APV.ValidarBancoPrevired} (its {@code logica} methods; the JavaCall {@code call} answers null and is not an operation).
 * Shared by CrearBancoPrevired, ModificarBancoPrevired, EliminarBancoPrevired (and CargaMasivaBanco in the legacy).
 */
public final class BancoPreviredValidador {

    static final String MENSAJE_BANCO_PREVIRED_YA_EXISTE = "UI_ERROR_CODIGO_BANCO_PREVIRED_YA_EXISTE";
    static final String MENSAJE_NO_SE_INGRESARON_BANCOS_PREVIRED = "UI_ERROR_NO_SE_INGRESARON_BANCOS_PREVIRED";
    static final String MENSAJE_ID_INTERNO_REPETIDO_ENTRADA = "UI_ERROR_ID_INTERNO_REPETIDO_ENTRADA";
    static final String MENSAJE_CODIGO_BANCO_PREVIRED_REPETIDO_ENTRADA = "UI_ERROR_CODIGO_BANCO_PREVIRED_REPETIDO_ENTRADA";
    static final String MENSAJE_NO_EXISTE_BANCO_PREVIRED = "UI_ERROR_NO_EXISTE_BANCO_PREVIRED";
    static final String MENSAJE_CUENTA_BANCO_NO_EXISTE = "UI_CUENTA_BANCO_NO_EXISTE";

    private static final String SQL_SELECT_BANCOS_PREVIRED = SqlLegacy.recurso("ValidarBancoPrevired_SelectBancosPrevired");
    private static final String SQL_SELECT_CUENTAS_BANCO = SqlLegacy.recurso("ValidarBancoPrevired_SelectCuentasBanco");

    /** Legacy {@code BancoPreviredEntrada}: public fields are the SQL parameter names of the insert/update/delete statements. */
    public static class BancoPreviredFila {
        public Long IdInterno;
        public Long IdCuentaBanco;
        public String CodigoBancoPrevired;

        public BancoPreviredFila() {
        }

        public BancoPreviredFila(Long idInterno, Long idCuentaBanco, String codigoBancoPrevired) {
            this.IdInterno = idInterno;
            this.IdCuentaBanco = idCuentaBanco;
            this.CodigoBancoPrevired = codigoBancoPrevired;
        }
    }

    public static class BancoPreviredExistente {
        public Long IdCuentaBanco;
        public String CodigoBancoPrevired;
    }

    public static class CuentaBancoExistente {
        public Long IdCuentaBanco;
        public Long IdBanco;
    }

    /** Legacy {@code ValidarBancoPrevired.ResultadoValidacion}. */
    public static final class ResultadoValidacion {
        public final List<BancoPreviredFila> bancosPreviredCorrectos = new ArrayList<>();
        public final List<BancoPreviredFila> bancosPreviredIncorrectos = new ArrayList<>();
        public final List<MensajeLegacy> mensajesErrores = new ArrayList<>();
    }

    private final SqlLegacy sql;
    private final DiccionarioMensajes diccionario;

    BancoPreviredValidador(SqlLegacy sql, DiccionarioMensajes diccionario) {
        this.sql = sql;
        this.diccionario = diccionario;
    }

    private MensajeLegacy error(Long id, String codigo, Map<String, String> parametros, Long idEmpresa) {
        return Plataforma.mensaje(diccionario, idEmpresa, id, MensajeLegacy.ERROR, codigo, parametros);
    }

    ResultadoValidacion validarCreacion(List<BancoPreviredFila> bancos, Long idEmpresa) throws Exception {
        Map<String, BancoPreviredExistente> existentes = bancosPorCodigoExistentes(bancos);
        Map<Long, CuentaBancoExistente> cuentas = cuentasPorIdExistentes(bancos);
        ResultadoValidacion r = validarReglasGenerales(bancos, idEmpresa);
        if (r.mensajesErrores.size() == 0) {
            for (BancoPreviredFila b : bancos) {
                boolean esCorrecto = true;
                if (existentes.get(b.CodigoBancoPrevired) != null) {
                    esCorrecto = false;
                    r.mensajesErrores.add(error(b.IdInterno, MENSAJE_BANCO_PREVIRED_YA_EXISTE, parametros(b), idEmpresa));
                }
                if (cuentas.get(b.IdCuentaBanco) == null) {
                    esCorrecto = false;
                    r.mensajesErrores.add(error(b.IdInterno, MENSAJE_CUENTA_BANCO_NO_EXISTE, parametros(b), idEmpresa));
                }
                (esCorrecto ? r.bancosPreviredCorrectos : r.bancosPreviredIncorrectos).add(b);
            }
        }
        return r;
    }

    ResultadoValidacion validarModificacion(List<BancoPreviredFila> bancos, Long idEmpresa) throws Exception {
        Map<String, BancoPreviredExistente> existentes = bancosPorCodigoExistentes(bancos);
        Map<Long, CuentaBancoExistente> cuentas = cuentasPorIdExistentes(bancos);
        ResultadoValidacion r = validarReglasGenerales(bancos, idEmpresa);
        if (r.mensajesErrores.size() == 0) {
            for (BancoPreviredFila b : bancos) {
                boolean esCorrecto = true;
                if (existentes.get(b.CodigoBancoPrevired) == null) {
                    esCorrecto = false;
                    r.mensajesErrores.add(error(b.IdInterno, MENSAJE_NO_EXISTE_BANCO_PREVIRED, parametros(b), idEmpresa));
                }
                if (cuentas.get(b.IdCuentaBanco) == null) {
                    esCorrecto = false;
                    r.mensajesErrores.add(error(b.IdInterno, MENSAJE_CUENTA_BANCO_NO_EXISTE, parametros(b), idEmpresa));
                }
                (esCorrecto ? r.bancosPreviredCorrectos : r.bancosPreviredIncorrectos).add(b);
            }
        }
        return r;
    }

    ResultadoValidacion validarEliminacion(List<BancoPreviredFila> bancos, Long idEmpresa) throws Exception {
        Map<String, BancoPreviredExistente> existentes = bancosPorCodigoExistentes(bancos);
        ResultadoValidacion r = validarReglasGenerales(bancos, idEmpresa);
        if (r.mensajesErrores.size() == 0) {
            for (BancoPreviredFila b : bancos) {
                if (existentes.get(b.CodigoBancoPrevired) == null) {
                    r.bancosPreviredIncorrectos.add(b);
                    r.mensajesErrores.add(error(b.IdInterno, MENSAJE_NO_EXISTE_BANCO_PREVIRED, parametros(b), idEmpresa));
                } else {
                    r.bancosPreviredCorrectos.add(b);
                }
            }
        }
        return r;
    }

    private Map<String, BancoPreviredExistente> bancosPorCodigoExistentes(List<BancoPreviredFila> bancos) throws SQLException {
        List<String> codigos = bancos.parallelStream().map(b -> b.CodigoBancoPrevired).distinct().collect(Collectors.toList());
        HashMap<String, Object> parametros = new HashMap<>();
        parametros.put("CodigoBancoPrevired", codigos);
        List<BancoPreviredExistente> existentes = sql.consultar(SQL_SELECT_BANCOS_PREVIRED, parametros, BancoPreviredExistente.class);
        // duplicate codes in the table make toMap throw IllegalStateException, as in the legacy
        return existentes.parallelStream().collect(Collectors.toMap(b -> b.CodigoBancoPrevired, b -> b));
    }

    private Map<Long, CuentaBancoExistente> cuentasPorIdExistentes(List<BancoPreviredFila> bancos) throws SQLException {
        List<Long> ids = bancos.parallelStream().map(b -> b.IdCuentaBanco).distinct().collect(Collectors.toList());
        HashMap<String, Object> parametros = new HashMap<>();
        parametros.put("IdCuentaBanco", ids);
        List<CuentaBancoExistente> existentes = sql.consultar(SQL_SELECT_CUENTAS_BANCO, parametros, CuentaBancoExistente.class);
        return existentes.parallelStream().collect(Collectors.toMap(c -> c.IdCuentaBanco, c -> c));
    }

    private ResultadoValidacion validarReglasGenerales(List<BancoPreviredFila> bancos, Long idEmpresa) {
        ResultadoValidacion r = new ResultadoValidacion();
        if (bancos.size() == 0) {
            r.mensajesErrores.add(error(0L, MENSAJE_NO_SE_INGRESARON_BANCOS_PREVIRED, new HashMap<String, String>(), idEmpresa));
        }
        Map<Long, List<BancoPreviredFila>> porIdInterno = bancos.parallelStream().collect(Collectors.groupingBy(b -> b.IdInterno));
        Map<String, List<BancoPreviredFila>> porCodigo = bancos.parallelStream().collect(Collectors.groupingBy(b -> b.CodigoBancoPrevired));
        for (Entry<Long, List<BancoPreviredFila>> agrupacion : porIdInterno.entrySet()) {
            if (agrupacion.getValue().size() > 1) {
                r.bancosPreviredIncorrectos.addAll(bancos);
                HashMap<String, String> parametros = new HashMap<>();
                parametros.put("IdInterno", String.valueOf(agrupacion.getKey()));
                r.mensajesErrores.add(error(0L, MENSAJE_ID_INTERNO_REPETIDO_ENTRADA, parametros, idEmpresa));
            }
        }
        for (Entry<String, List<BancoPreviredFila>> agrupacion : porCodigo.entrySet()) {
            if (agrupacion.getValue().size() > 1) {
                r.bancosPreviredIncorrectos.addAll(bancos);
                // LEGACY DEFECT kept (see README APV-C-410): the repeated-code message is built with an EMPTY value map (the code is computed and not used)
                r.mensajesErrores.add(error(0L, MENSAJE_CODIGO_BANCO_PREVIRED_REPETIDO_ENTRADA, new HashMap<String, String>(), idEmpresa));
            }
        }
        return r;
    }

    private static HashMap<String, String> parametros(BancoPreviredFila b) {
        HashMap<String, String> p = new HashMap<>();
        p.put("IdInterno", String.valueOf(b.IdInterno));
        p.put("CodigoBancoPrevired", b.CodigoBancoPrevired);
        p.put("IdCuentaBanco", String.valueOf(b.IdCuentaBanco));
        return p;
    }
}
