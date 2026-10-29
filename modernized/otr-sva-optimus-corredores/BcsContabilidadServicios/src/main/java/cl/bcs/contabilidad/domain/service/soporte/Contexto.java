package cl.bcs.contabilidad.domain.service.soporte;

import cl.bcs.contabilidad.domain.model.EmpresaInfo;
import cl.bcs.contabilidad.domain.model.EntradaCentralizacion;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * What one centralization run knows besides its input: the legacy {@code EntradaCentralizacionInfoAdicional}
 * (company, system date, localization, system currency, filled by CentralizaMultiSeleccion.java:183-193) plus the
 * previous business day and configuration parameters. Every value is read from the platform at most once, and only
 * when the type actually needs it.
 */
public final class Contexto {

    private final EntradaCentralizacion entrada;
    private final PlataformaPort plataforma;
    private final Supplier<EmpresaInfo> empresa;
    private final Supplier<String> localizacion;
    private final Supplier<String> monedaSistema;
    private final Supplier<String> fechaSistema;
    private final Supplier<String> diaAnterior;

    public Contexto(EntradaCentralizacion entrada, PlataformaPort plataforma) {
        this.entrada = entrada;
        this.plataforma = plataforma;
        this.empresa = memo(() -> plataforma.consultarEmpresa(entrada.idEmpresa()));
        this.localizacion = memo(() -> plataforma.obtenerLocalizacionSistema(entrada.idEmpresa()));
        this.monedaSistema = memo(() -> plataforma.obtenerMonedaSistema(entrada.idEmpresa()));
        this.fechaSistema = memo(() -> plataforma.obtenerFechaSistema(entrada.idEmpresa()));
        // ObtenerDiaAnterior(idPais of the company, fechaContable, 1 business day)
        this.diaAnterior = memo(() -> plataforma.obtenerDiaAnterior(empresa().idPais(), entrada.fechaContable()));
    }

    public EntradaCentralizacion entrada() { return entrada; }
    public Long idEmpresa() { return entrada.idEmpresa(); }
    public String fechaContable() { return entrada.fechaContable(); }
    public Long idTipo() { return entrada.idTipoCentralizacion(); }

    public EmpresaInfo empresa() { return empresa.get(); }
    /** Legacy {@code empresa.DocumentoIdentidad}: the broker's own RUT. */
    public String documentoCorredora() { return empresa().documentoIdentidad(); }
    public String localizacion() { return localizacion.get(); }
    public String monedaSistema() { return monedaSistema.get(); }
    public String fechaSistema() { return fechaSistema.get(); }
    public String diaAnterior() { return diaAnterior.get(); }

    /** True when the run is for the current system date (otherwise the historic queries apply). */
    public boolean esFechaSistema() {
        return Objects.equals(fechaContable(), fechaSistema());
    }

    /** Legacy {@code ConfiguracionSistema.getParametroConfiguracionString} guarded by a try/catch with a default. */
    public String parametro(String modulo, String parametro, String porDefecto) {
        String v = plataforma.obtenerParametroConfiguracion(idEmpresa(), modulo, parametro, porDefecto);
        return v == null ? porDefecto : v;
    }

    private static <T> Supplier<T> memo(Supplier<T> s) {
        return new Supplier<>() {
            private boolean listo;
            private T valor;

            @Override
            public T get() {
                if (!listo) {
                    valor = s.get();
                    listo = true;
                }
                return valor;
            }
        };
    }
}
