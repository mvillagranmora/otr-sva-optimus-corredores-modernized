package cl.bcs.fondomutuo.port.out.kit;

import cl.bcs.fondomutuo.kit.InformacionEmpresa;

/** Kit port K: legacy {@code ConsultarEmpresa.logica(con, idEmpresa)}; raises {@code Exception("Empresa no existe")} (as unchecked) when there is no row. */
public interface EmpresaPort {

    InformacionEmpresa informacion(Long idEmpresa);
}
