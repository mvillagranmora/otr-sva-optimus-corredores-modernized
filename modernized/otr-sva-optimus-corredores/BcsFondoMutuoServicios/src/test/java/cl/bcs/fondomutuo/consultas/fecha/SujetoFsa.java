package cl.bcs.fondomutuo.consultas.fecha;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;

import cl.bcs.fondomutuo.adapter.out.jdbc.CalendarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConfiguracionSistemaJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.DiccionarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.FechasJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.PerfilFondoMutuoJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.RevisarRequisitosBasicosJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.SeriesFondoJdbc;
import cl.bcs.fondomutuo.cierre3b.Guion;
import cl.bcs.fondomutuo.kit.InformacionEmpresa;

/**
 * The NEW ConsultarFechaSolicitudAporteRescate over the REAL JDBC adapters on the H2 of the case (the same adapters the closure of F1 already ships:
 * kit, perfil, fechas, series); the only collaborator outside the module, Plataforma {@code ConsultarEmpresa}, is answered by the case script ({@link Guion}).
 * Nothing here has state. Uses the assumed API of {@code API_ASSUMED.md}.
 */
final class SujetoFsa {

    private SujetoFsa() {
    }

    static ConsultarFechaPort crear(ConexionUnidadDeTrabajo conexion, Guion guion) {
        ConsultarFechaFabrica.Dependencias d = new ConsultarFechaFabrica.Dependencias();
        ConfiguracionSistemaJdbc configuracion = new ConfiguracionSistemaJdbc(conexion);
        DiccionarioJdbc diccionario = new DiccionarioJdbc(conexion);
        d.configuracion = configuracion;
        d.diccionario = diccionario;
        d.basicos = new RevisarRequisitosBasicosJdbc(conexion, diccionario);
        d.calendario = new CalendarioJdbc(conexion, configuracion);
        d.empresas = id -> new Gson().fromJson(guion.responder("ConsultarEmpresa", new JsonPrimitive(id)), InformacionEmpresa.class);
        d.series = new SeriesFondoJdbc(conexion);
        d.fechasPort = new FechasJdbc(conexion);
        d.perfilPort = new PerfilFondoMutuoJdbc(conexion);
        d.datos = new ConsultarFechaJdbc(conexion);
        return ConsultarFechaFabrica.crear(d);
    }
}
