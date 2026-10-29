package cl.bcs.fondomutuo.rescatereinversion;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.fondomutuo.cierre.ListaHash;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.comisionnn.ComisionNnDatos;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesAporte;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesRescate;

/** The keys of the request, the support reads and their hashes (FM-REI-05/06, RR:333-432) and the type of reinversion (FM-REI-07, RR:441-463). */
final class RreLectura {

    private final SeriesFondoPort series;
    private final RequisitosDatos requisitos;
    private final ComisionNnDatos comunes;
    private final RreDatos datos;

    RreLectura(SeriesFondoPort series, RequisitosDatos requisitos, ComisionNnDatos comunes, RreDatos datos) {
        this.series = series;
        this.requisitos = requisitos;
        this.comunes = comunes;
        this.datos = datos;
    }

    /** RR:333-361: batch count and the distinct series, portfolios and regimes (contributions add series and regimes only). */
    static void claves(RreContexto c) {
        List<Long> clientes = new ArrayList<>();
        for (InSolicitudesRescate r : c.entrada.p_SolicitudesRescate) {
            if (RreConstantes.SI.equalsIgnoreCase(r.SolicitudBatch)) c.countTransaccionesBatch++;
            if (!c.series.contains(r.IdSerieFondo)) c.series.add(r.IdSerieFondo);
            if (!clientes.contains(r.IdCliente)) clientes.add(r.IdCliente);
            if (!c.portafolios.contains(r.IdPortafolio)) c.portafolios.add(r.IdPortafolio);
            if (!c.regimen.contains(r.IdRegimenTributario)) c.regimen.add(r.IdRegimenTributario);
        }
        for (InSolicitudesAporte a : c.entrada.p_SolicitudesAporte) {
            if (!c.series.contains(a.IdSerieFondo)) c.series.add(a.IdSerieFondo);
            if (!c.regimen.contains(a.IdRegimenTributario)) c.regimen.add(a.IdRegimenTributario);
        }
    }

    /** The eight reads in the legacy order, then the nine {@code List2Hash} (a repeated key: "key field is not unique", D-NOR-9). */
    void leer(RreContexto c) throws Exception {
        Long idEmpresa = c.entrada.p_IdEmpresa;
        List<String> idTipoMovimientos = new ArrayList<>();
        idTipoMovimientos.add(RreConstantes.REINVERSION);
        idTipoMovimientos.add(RreConstantes.APORTE_REINVERSION);
        List<SerieFondoSolicitudes> seriesSolicitudes = series.series(idEmpresa, c.series);
        List<RequisitosDatos.ClientePortafolioSolicitudes> clientesSolicitudes = requisitos.clientesPortafolio(c.portafolios);
        List<ComisionNnDatos.TipoMovimientoFondoMutuo> tipoMovimientos = comunes.tiposMovimiento(idTipoMovimientos);
        List<RreDatos.RegimenSerieDeFondo> regimenesSeries = datos.regimenes(idEmpresa, c.series, c.regimen);
        c.regimenMandato = datos.regimenesMandato(idEmpresa, c.series);
        List<RreDatos.ConfiguracionRegimenFondoMutuo> configuracionesRegimen = datos.configuracionRegimen(c.regimen);
        List<RreDatos.ConfiguracionPropositoPortafolio> configuracionesProposito = datos.configuracionProposito(c.regimen);
        List<String> monedas = new ArrayList<>();
        for (SerieFondoSolicitudes serie : seriesSolicitudes) {
            if (!monedas.contains(serie.IdMoneda)) monedas.add(serie.IdMoneda);
        }
        List<ComisionNnDatos.Moneda> monedasFondo = comunes.monedas(monedas);
        c.hseries = ListaHash.aHash(seriesSolicitudes, s -> s.IdSerieFondo);
        c.hclientes = ListaHash.aHash(clientesSolicitudes, x -> x.IdPortafolio);
        c.htipoMovimiento = ListaHash.aHash(tipoMovimientos, t -> t.IdTipoMovimientoFondoMutuo);
        c.hseriesRegimen = ListaHash.aHash(regimenesSeries, r -> r.IdRegimenFondoMutuoSerieFondo);
        c.hrescates = ListaHash.aHash(c.entrada.p_SolicitudesRescate, r -> r.IdInterno);
        c.haportes = ListaHash.aHash(c.entrada.p_SolicitudesAporte, a -> a.IdInterno);
        c.hmonedasFondo = ListaHash.aHash(monedasFondo, m -> m.IdMoneda);
        c.hregimenConfiguraciones = ListaHash.aHash(configuracionesRegimen, x -> x.Id);
        c.hconfRegimenPortafolio = ListaHash.aHash(configuracionesProposito, x -> x.Id);
    }

    /** FM-REI-07: per rescue and per contribution with the same IdInterno; also sets the contribution's IdSerieFondoOrigen (it mutates the input). */
    static void tipoReinversion(RreContexto c) {
        for (InSolicitudesRescate rescate : c.entrada.p_SolicitudesRescate) {
            c.hseriesMonedasDiferentes.put(rescate.IdInterno, RreConstantes.NO);
            for (InSolicitudesAporte aporte : c.entrada.p_SolicitudesAporte) {
                SerieFondoSolicitudes serieRescate = c.hseries.get(rescate.IdSerieFondo);
                SerieFondoSolicitudes serieAporte = c.hseries.get(aporte.IdSerieFondo);
                if (aporte.IdInterno.compareTo(rescate.IdInterno) == 0) {
                    String monedas = RreReglas.tipoReinversionRescate(rescate, serieRescate, serieAporte);
                    if (monedas != null) c.hseriesMonedasDiferentes.put(rescate.IdInterno, monedas);
                    aporte.IdSerieFondoOrigen = rescate.IdSerieFondo;
                }
            }
        }
    }
}
