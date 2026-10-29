package cl.bcs.fondomutuo.consultas.custodiaffmm;

import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;

public final class ConsultarCustodiaPortafolioFfmmFabrica {

    private ConsultarCustodiaPortafolioFfmmFabrica() {
    }

    public static final class Dependencias {
        public ConfiguracionSistemaPort configuracion;
        public EmpresaPort empresas;
        public ConsultarCustodiaPortafolioFfmmDatos datos;
    }

    public static ConsultarCustodiaPortafolioFfmmPort crear(Dependencias d) {
        return new ConsultarCustodiaPortafolioFfmmService(d.configuracion, d.empresas, d.datos);
    }
}
