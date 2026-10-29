package cl.bcs.fondomutuo.valorcuota;

import java.sql.Connection;
import java.util.List;

import com.google.gson.JsonArray;

import cl.bcs.fondomutuo.adapter.out.jdbc.CalendarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConfiguracionSistemaJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.DiccionarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.RevisarRequisitosBasicosJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.SeriesFondoJdbc;
import cl.bcs.fondomutuo.cierre3b.Guion;
import cl.bcs.fondomutuo.cierre3b.SecuenciaH2;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;

/**
 * The NEW CargarValorCuota over the REAL JDBC adapters on the H2 of the case (kit adapters of the F1 closure, the series adapter and the two new adapters of this package).
 * The only collaborator outside the module, Plataforma {@code AgregarLog} (HTTP write port {@link LogPort}), is answered by the case script ({@link Guion}), exactly as the
 * oracle's registered double. The ids of the historico come from the sequence port ({@link SecuenciaH2}: the H2 sequence of the same name). Nothing here has state except
 * the test hooks below. Uses the assumed API of {@code API_ASSUMED.md}.
 */
final class SujetoCvc {

    /** Test hook (log failure tests): replaces the HTTP log port. Null = the case script. */
    static volatile LogPort logPrueba;
    /** Test hook (order tests): when not null, every call to the write port, the sequence port and the log port appends its name here, in the order they happen. */
    static volatile List<String> orden;

    private SujetoCvc() {
    }

    static CargarValorCuotaPort crear(ConexionUnidadDeTrabajo conexion, Connection con, Guion guion, JsonArray secuencias) {
        CargarValorCuotaFabrica.Dependencias d = new CargarValorCuotaFabrica.Dependencias();
        ConfiguracionSistemaJdbc configuracion = new ConfiguracionSistemaJdbc(conexion);
        DiccionarioJdbc diccionario = new DiccionarioJdbc(conexion);
        d.configuracion = configuracion;
        d.diccionario = diccionario;
        d.basicos = new RevisarRequisitosBasicosJdbc(conexion, diccionario);
        d.calendario = new CalendarioJdbc(conexion, configuracion);
        d.series = new SeriesFondoJdbc(conexion);
        d.datos = new CargarValorCuotaDatosJdbc(conexion);
        CargarValorCuotaEscritura escritura = new CargarValorCuotaEscrituraJdbc(conexion);
        SecuenciaPort sec = new SecuenciaH2(con, secuencias);
        LogPort log = logPrueba != null ? logPrueba : e -> guion.responder("AgregarLog", e);
        List<String> o = orden;
        if (o != null) {
            d.escritura = new CargarValorCuotaEscritura() {
                @Override
                public void actualizarUltimaCarga(List<CargarValorCuotaContrato.Resultado> filas) {
                    o.add("actualizarUltimaCarga:" + filas.size());
                    escritura.actualizarUltimaCarga(filas);
                }

                @Override
                public void insertarHistorico(List<CargarValorCuotaContrato.Resultado> filas, List<Long> ids) {
                    o.add("insertarHistorico:" + filas.size() + ":ids=" + ids);
                    escritura.insertarHistorico(filas, ids);
                }

                @Override
                public void eliminarVigentes(List<CargarValorCuotaContrato.Resultado> filas) {
                    o.add("eliminarVigentes:" + filas.size());
                    escritura.eliminarVigentes(filas);
                }

                @Override
                public void insertarVigentes(List<CargarValorCuotaContrato.Resultado> filas) {
                    o.add("insertarVigentes:" + filas.size());
                    escritura.insertarVigentes(filas);
                }
            };
            d.secuencias = (nombre, cantidad) -> {
                o.add("secuencia:" + nombre + ":" + cantidad);
                return sec.siguientes(nombre, cantidad);
            };
            d.log = e -> {
                o.add("log");
                return log.agregar(e);
            };
        } else {
            d.escritura = escritura;
            d.secuencias = sec;
            d.log = log;
        }
        return CargarValorCuotaFabrica.crear(d);
    }
}
