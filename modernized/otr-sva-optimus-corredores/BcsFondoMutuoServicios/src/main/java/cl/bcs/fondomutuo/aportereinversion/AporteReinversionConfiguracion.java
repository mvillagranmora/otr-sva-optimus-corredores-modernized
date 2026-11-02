package cl.bcs.fondomutuo.aportereinversion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.bcs.fondomutuo.agf.AgfDatos;
import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.certificacion.CrearAporteReinversionPort;
import cl.bcs.fondomutuo.comisionnn.ComisionNnDatos;
import cl.bcs.fondomutuo.cuotas.CuotasPort;
import cl.bcs.fondomutuo.custodia.FlujoCustodiaFmDatos;
import cl.bcs.fondomutuo.fechas.FechasPort;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.normal.ProcesosExternos;
import cl.bcs.fondomutuo.patrimonio.PatrimonioEmisorPort;
import cl.bcs.fondomutuo.perfil.PerfilFondoMutuoPort;
import cl.bcs.fondomutuo.port.out.http.CustodiaPort;
import cl.bcs.fondomutuo.port.out.http.FlujoCajaPort;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.http.PreciosTasasPort;
import cl.bcs.fondomutuo.port.out.http.ProductosPortafolioPort;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;
import cl.bcs.fondomutuo.port.out.kit.RevisarRequisitosBasicosPort;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;

/**
 * Spring wiring of {@link AporteReinversionPort} (legacy {@code CrearSolicitudAporteReinversion}) and of its JSON adapter for Certificar (F-I). The AR has NO JavaCall
 * handler and its FQN is not in the allow-list: it is reached only in process, from Certificar, which is itself closed by default (its list, G-CUS and the gates of
 * its own fronts).
 */
@Configuration
public class AporteReinversionConfiguracion {

    @Bean
    public AporteReinversionPort aporteReinversionPort(RevisarRequisitosBasicosPort basicos, ConfiguracionSistemaPort configuracion, EmpresaPort empresas,
            DiccionarioMensajesPort diccionario, CalendarioPort calendario, SeriesFondoPort series, RequisitosDatos requisitosDatos,
            ComisionNnDatos comisionNnDatos, AporteReinversionJdbc datos, PerfilFondoMutuoPort perfilPort, FechasPort fechasPort, CuotasPort cuotasPort,
            PatrimonioEmisorPort patrimonioPort, FlujoCustodiaFmDatos flujoCustodiaDatos, AgfDatos agfDatos, SecuenciaPort secuencias, ParidadPort paridad,
            PreciosTasasPort precios, CustodiaPort custodia, FlujoCajaPort flujoCaja, LogPort log, ProductosPortafolioPort productos,
            ProcesosExternos.TesoreriaPort tesoreria, ProcesosExternos.SpotPort spot, Compuertas compuertas) {
        AporteReinversionFabrica.Dependencias d = new AporteReinversionFabrica.Dependencias();
        d.basicos = basicos;
        d.configuracion = configuracion;
        d.empresas = empresas;
        d.diccionario = diccionario;
        d.calendario = calendario;
        d.series = series;
        d.requisitosDatos = requisitosDatos;
        d.comisionNnDatos = comisionNnDatos;
        d.datos = datos;
        d.perfilPort = perfilPort;
        d.fechasPort = fechasPort;
        d.cuotasPort = cuotasPort;
        d.patrimonioPort = patrimonioPort;
        d.flujoCustodiaDatos = flujoCustodiaDatos;
        d.agfDatos = agfDatos;
        d.secuencias = secuencias;
        d.paridad = paridad;
        d.precios = precios;
        d.custodia = custodia;
        d.flujoCaja = flujoCaja;
        d.log = log;
        d.productos = productos;
        d.tesoreria = tesoreria;
        d.spot = spot;
        d.compuertas = compuertas;
        return AporteReinversionFabrica.crear(d);
    }

    @Bean
    public CrearAporteReinversionPort crearAporteReinversionPort(AporteReinversionPort aporteReinversion) {
        return new AporteReinversionJsonAdapter(aporteReinversion);
    }
}
