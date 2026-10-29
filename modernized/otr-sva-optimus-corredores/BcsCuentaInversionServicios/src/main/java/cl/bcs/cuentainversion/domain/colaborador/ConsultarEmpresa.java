package cl.bcs.cuentainversion.domain.colaborador;

/** Contract of {@code cl.bcs.plataforma.pla.utils.ConsultarEmpresa}: input is the company id ({@code {"valor": id}}), output the company data. */
public final class ConsultarEmpresa {

    private ConsultarEmpresa() {
    }

    public static final String CLASE = "cl.bcs.plataforma.pla.utils.ConsultarEmpresa";

    public static class InformacionEmpresa {
        public Long IdEmpresa;
        public Long IdPais;
    }
}
