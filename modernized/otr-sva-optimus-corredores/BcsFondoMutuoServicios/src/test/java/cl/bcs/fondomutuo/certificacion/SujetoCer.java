package cl.bcs.fondomutuo.certificacion;

import java.sql.Connection;

import com.google.gson.JsonArray;

import cl.bcs.fondomutuo.adapter.out.jdbc.CalendarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConfiguracionSistemaJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.CuotasJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.DiccionarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.RevisarRequisitosBasicosJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.SeriesFondoJdbc;
import cl.bcs.fondomutuo.cierre.FormatoMoneda;
import cl.bcs.fondomutuo.cierre3b.Guion;
import cl.bcs.fondomutuo.cierre3b.SecuenciaH2;

/**
 * The NEW CertificarSolicitudAporteRescate (fronts F-A and F-B) over the REAL JDBC adapters on the H2 of the case: the kit adapters of the F1 closure (configuration, dictionary,
 * basic requirements, calendar / currency formats), the series and quota adapters of the closure of {@code CalcularMontoNumeroCuota}, and the two adapters of this package
 * ({@link CertificarAporteRescateDatosJdbc}, {@link CertificarAporteRescateEscrituraJdbc}). The collaborators outside the module that T1 reaches, Plataforma
 * {@code EntregarPreciosTasas} and {@code CalcularParidad} (HTTP read ports), are answered by the case script ({@link Guion}), exactly as the oracle's registered doubles; the ids of
 * the log come from the sequence port ({@link SecuenciaH2}). The only thing after F-B, the seam {@link CertificarAporteRescateFasesPosteriores}, is given by the caller. No state
 * but what its arguments carry. Uses the assumed API of {@code API_ASSUMED.md}.
 */
final class SujetoCer {

    /** Test hook: wraps the write port (the early-exit log) so that a test can count and read what the facade writes. Null = the real adapter. */
    static volatile java.util.function.UnaryOperator<CertificarAporteRescateEscritura> envolverEscritura;

    private SujetoCer() {
    }

    static CertificarAporteRescatePort crear(ConexionUnidadDeTrabajo conexion, Connection con, Guion guion, JsonArray secuencias, CertificarAporteRescateFasesPosteriores posteriores) {
        CertificarAporteRescateFabrica.Dependencias d = new CertificarAporteRescateFabrica.Dependencias();
        ConfiguracionSistemaJdbc configuracion = new ConfiguracionSistemaJdbc(conexion);
        DiccionarioJdbc diccionario = new DiccionarioJdbc(conexion);
        d.configuracion = configuracion;
        d.diccionario = diccionario;
        d.basicos = new RevisarRequisitosBasicosJdbc(conexion, diccionario);
        d.calendario = new CalendarioJdbc(conexion, configuracion);
        d.series = new SeriesFondoJdbc(conexion);
        d.cuotas = new CuotasJdbc(conexion);
        d.precios = e -> guion.responder("EntregarPreciosTasas", e);
        d.paridad = e -> guion.responder("CalcularParidad", e);
        d.datos = new CertificarAporteRescateDatosJdbc(conexion);
        CertificarAporteRescateEscritura escritura = new CertificarAporteRescateEscrituraJdbc(conexion);
        d.escritura = envolverEscritura != null ? envolverEscritura.apply(escritura) : escritura;
        d.secuencias = new SecuenciaH2(con, secuencias);
        d.posteriores = posteriores;
        return CertificarAporteRescateFabrica.crear(d);
    }

    /** The currency formatter the later fronts will use (FM-CER-23), on the same H2: the test applies it to a COPY of what the seam received, to compute what the legacy wrote. */
    static FormatoMoneda formato(ConexionUnidadDeTrabajo conexion) {
        return new FormatoMoneda(new CalendarioJdbc(conexion, new ConfiguracionSistemaJdbc(conexion)));
    }
}
