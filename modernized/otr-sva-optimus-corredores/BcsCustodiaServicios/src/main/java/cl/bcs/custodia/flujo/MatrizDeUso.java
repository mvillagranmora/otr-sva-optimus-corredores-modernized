package cl.bcs.custodia.flujo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cl.bcs.custodia.flujo.FlujoCustodiaModelo.ConfiguracionUso;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.FiltroBusqueda;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.MatrizUso;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.Uso;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.UsoFiltro;

/**
 * The usage matrix (CUS-FLU-08..13): the requested uses crossed with the requested filters, matched to the configured uses by similarity, expanded
 * with the configured items into the base table the flow is built on. No I/O. The iteration order of the winners, of the sources and (later) of the
 * totalised lines follows {@link HashMap} / {@link HashSet} of the legacy key strings (D-CUS-05): the same key texts are built on purpose.
 */
final class MatrizDeUso {

    /** Winners of each requested key (in {@code HashMap} order, every one carrying the work id), as written to the work table. */
    final List<MatrizUso> ganadores = new ArrayList<>();
    /** The base table: one row per (winner, configured item of its use), without duplicates. */
    final List<MatrizUso> tablaBase = new ArrayList<>();
    /** Winner keys, in the same map the iteration order comes from (used to detect the capital-rights search). */
    final Set<String> clavesGanadores;
    /** (use id # item) to configured item, any use (the last one wins). */
    final Map<String, ConfiguracionUso> itemsPorUsoYCalculo = new HashMap<>();
    /** item to origin, across every winner's uses (the last one wins). */
    final Map<String, String> origenPorItem = new HashMap<>();
    /** The distinct sources the items map to (CUS-FLU-13), in {@code HashSet} order. */
    final Set<String> fuentes = new HashSet<>();
    /** Configured uses that were requested (CUS-FLU-09), in query order. */
    final List<ConfiguracionUso> usosConfigurados;

    private MatrizDeUso(List<ConfiguracionUso> usosConfigurados, Set<String> clavesGanadores) {
        this.usosConfigurados = usosConfigurados;
        this.clavesGanadores = clavesGanadores;
    }

    static MatrizDeUso construir(List<Uso> usosPedidos, List<FiltroBusqueda> filtrosPedidos, List<ConfiguracionUso> usosConfig,
            List<ConfiguracionUso> itemsConfig, Long idEmpresa, long idTrabajo) {
        // CUS-FLU-08: distinct (by text form), then the full cross
        List<Uso> usos = usosPedidos.stream().distinct().collect(Collectors.toList());
        List<FiltroBusqueda> filtros = filtrosPedidos.stream().distinct().collect(Collectors.toList());

        List<UsoFiltro> usoFiltros = new ArrayList<>();
        List<String> tiposOperacion = new ArrayList<>();
        Map<String, Boolean> tiposVistos = new HashMap<>();
        for (Uso fu : usos) {
            for (FiltroBusqueda ffb : filtros) {
                UsoFiltro uf = cruzar(fu, ffb);
                usoFiltros.add(uf);
                if (!tiposVistos.containsKey(uf.UsoTipoOperacion)) {
                    tiposVistos.put(uf.UsoTipoOperacion, true);
                    tiposOperacion.add(uf.UsoTipoOperacion);
                }
            }
        }

        // CUS-FLU-09: only the configured uses whose operation type was requested
        List<ConfiguracionUso> usosFiltrados = usosConfig.stream().filter(u -> tiposOperacion.contains(u.UsoTipoOperacion)).collect(Collectors.toList());

        // CUS-FLU-10/11: per requested key the configured use of the highest score wins; a tie keeps the first
        HashMap<String, MatrizUso> ganadoresPorClave = new HashMap<>();
        for (UsoFiltro u : usoFiltros) {
            for (ConfiguracionUso ufc : usosFiltrados) {
                if ((ufc.UsoTipoOperacion + "").equals(u.UsoTipoOperacion + "")) {
                    MatrizUso ma = aMatriz(u);
                    ma.IdUsoFlujoCustodia = ufc.IdUsoFlujoCustodia;
                    ma.IdEstado = "ACT";
                    ma.IdEmpresa = idEmpresa;
                    ma.ItemCalculo = ufc.ItemCalculo;
                    ma.ItemOrigen = ufc.ItemOrigen;

                    int puntaje = FlujoCustodiaReglas.similitud(u, ufc);
                    ma.MayorSimilitud = puntaje;
                    String clave = u.IdClasificacionTipoInstrumento + "||" + u.IdTipoInstrumento + "||" + u.IdInstrumento + "||" + u.UsoTipoOperacion + "||"
                            + u.IdCliente + "||" + u.IdPortafolio + "||" + u.BusquedaEspecifica + "||" + u.IdTipoInstrumentoEspecifico + "" + "||"
                            + u.IdRegimenFondoMutuo + "" + "||" + u.IdEmisor + "";

                    MatrizUso actual = ganadoresPorClave.get(clave);
                    if (actual == null || actual.MayorSimilitud < puntaje) {
                        ganadoresPorClave.put(clave, ma);
                    }
                }
            }
        }

        MatrizDeUso m = new MatrizDeUso(usosFiltrados, ganadoresPorClave.keySet());

        // CUS-FLU-12: each winner expanded with every configured item of its use
        Map<String, MatrizUso> vistos = new HashMap<>();
        for (Map.Entry<String, MatrizUso> entrada : ganadoresPorClave.entrySet()) {
            MatrizUso mu = entrada.getValue();
            mu.IdTrabajo = idTrabajo;
            m.ganadores.add(mu);

            for (ConfiguracionUso ifc : itemsConfig) {
                m.itemsPorUsoYCalculo.put(ifc.IdUsoFlujoCustodia + "#" + ifc.ItemCalculo, ifc);

                if (mu.IdUsoFlujoCustodia.equals(ifc.IdUsoFlujoCustodia)) {
                    MatrizUso tb = copiar(mu);
                    tb.ItemCalculo = ifc.ItemCalculo;
                    tb.ItemOrigen = ifc.ItemOrigen;
                    tb.FuncionOperador = ifc.FuncionOperador;

                    String clave = tb.IdCliente + "#" + tb.IdPortafolio + "#" + tb.UsoTipoOperacion + "#" + tb.IdClasificacionTipoInstrumento + "#"
                            + tb.IdTipoInstrumento + "#" + tb.IdInstrumento + "#" + tb.IdEmisor + "#" + tb.FechaVencimiento + "#" + tb.IdTipoReajuste + "#"
                            + tb.EsFungible + "#" + tb.IdBoveda + "#" + tb.IdTipoInstrumentoEspecifico + "#" + tb.IdUsoFlujoCustodia + "#"
                            + tb.BusquedaEspecifica + "#" + tb.IdRegimenFondoMutuo + "#" + tb.IdEstado + "#" + tb.IdEmpresa + "#" + tb.ItemCalculo + "#"
                            + tb.ItemOrigen + "#" + tb.FuncionOperador + "#" + tb.MayorSimilitud;
                    if (!vistos.containsKey(clave)) {
                        vistos.put(clave, tb);
                        m.tablaBase.add(tb);
                    }

                    m.origenPorItem.put(ifc.ItemCalculo, ifc.ItemOrigen);
                    String fuente = FlujoCustodiaReglas.fuenteDeItem(ifc.ItemCalculo);
                    if (fuente != null) {
                        m.fuentes.add(fuente);
                    }
                }
            }
        }
        return m;
    }

    private static UsoFiltro cruzar(Uso fu, FiltroBusqueda ffb) {
        UsoFiltro f = new UsoFiltro();
        f.UsoTipoOperacion = fu.UsoTipoOperacion;
        f.IdCliente = fu.IdCliente;
        f.IdPortafolio = fu.IdPortafolio;
        f.BusquedaEspecifica = fu.BusquedaEspecifica;
        f.IdTipoInstrumentoEspecifico = fu.IdTipoInstrumentoEspecifico;
        f.IdClasificacionTipoInstrumento = ffb.IdClasificacionTipoInstrumento;
        f.IdTipoInstrumento = ffb.IdTipoInstrumento;
        f.IdInstrumento = ffb.IdInstrumento;
        f.IdEmisor = ffb.IdEmisor;
        f.FechaVencimiento = ffb.FechaVencimiento;
        f.IdTipoReajuste = ffb.IdTipoReajuste;
        f.EsFungible = ffb.EsFungible;
        f.IdBoveda = ffb.IdBoveda;
        f.IdRegimenFondoMutuo = ffb.IdRegimenFondoMutuo;
        return f;
    }

    private static MatrizUso aMatriz(UsoFiltro u) {
        MatrizUso ma = new MatrizUso();
        ma.IdCliente = u.IdCliente;
        ma.IdPortafolio = u.IdPortafolio;
        ma.UsoTipoOperacion = u.UsoTipoOperacion;
        ma.IdClasificacionTipoInstrumento = u.IdClasificacionTipoInstrumento;
        ma.IdTipoInstrumento = u.IdTipoInstrumento;
        ma.IdInstrumento = u.IdInstrumento;
        ma.IdEmisor = u.IdEmisor;
        ma.FechaVencimiento = u.FechaVencimiento;
        ma.IdTipoReajuste = u.IdTipoReajuste;
        ma.EsFungible = u.EsFungible;
        ma.IdBoveda = u.IdBoveda;
        ma.IdTipoInstrumentoEspecifico = u.IdTipoInstrumentoEspecifico;
        ma.BusquedaEspecifica = u.BusquedaEspecifica;
        ma.IdRegimenFondoMutuo = u.IdRegimenFondoMutuo;
        return ma;
    }

    private static MatrizUso copiar(MatrizUso mu) {
        MatrizUso tb = new MatrizUso();
        tb.IdCliente = mu.IdCliente;
        tb.IdPortafolio = mu.IdPortafolio;
        tb.UsoTipoOperacion = mu.UsoTipoOperacion;
        tb.IdClasificacionTipoInstrumento = mu.IdClasificacionTipoInstrumento;
        tb.IdTipoInstrumento = mu.IdTipoInstrumento;
        tb.IdInstrumento = mu.IdInstrumento;
        tb.IdEmisor = mu.IdEmisor;
        tb.FechaVencimiento = mu.FechaVencimiento;
        tb.IdTipoReajuste = mu.IdTipoReajuste;
        tb.EsFungible = mu.EsFungible;
        tb.IdBoveda = mu.IdBoveda;
        tb.IdTipoInstrumentoEspecifico = mu.IdTipoInstrumentoEspecifico;
        tb.IdUsoFlujoCustodia = mu.IdUsoFlujoCustodia;
        tb.BusquedaEspecifica = mu.BusquedaEspecifica;
        tb.IdRegimenFondoMutuo = mu.IdRegimenFondoMutuo;
        tb.IdEstado = mu.IdEstado;
        tb.IdEmpresa = mu.IdEmpresa;
        tb.ItemCalculo = mu.ItemCalculo;
        tb.ItemOrigen = mu.ItemOrigen;
        tb.FuncionOperador = mu.FuncionOperador;
        tb.MayorSimilitud = mu.MayorSimilitud;
        return tb;
    }
}
