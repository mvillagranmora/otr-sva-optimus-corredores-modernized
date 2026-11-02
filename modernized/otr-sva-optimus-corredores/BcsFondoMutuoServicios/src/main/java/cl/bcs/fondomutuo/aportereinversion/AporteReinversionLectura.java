package cl.bcs.fondomutuo.aportereinversion;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.fondomutuo.aportereinversion.AporteReinversionContrato.InSolicitudesAporte;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.comisionnn.ComisionNnDatos;
import cl.bcs.fondomutuo.requisitos.Hashes;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos.ClientePortafolioSolicitudes;

/** The support reads of one lot of {@code CrearSolicitudAporteReinversion} (legacy :489-567), in the legacy order, and the classification of each reinversion (:603-621). */
final class AporteReinversionLectura {

    private final AporteReinversionDatos datos;
    private final SeriesFondoPort series;
    private final RequisitosDatos requisitos;
    private final ComisionNnDatos comunes;

    AporteReinversionLectura(AporteReinversionDatos datos, SeriesFondoPort series, RequisitosDatos requisitos, ComisionNnDatos comunes) {
        this.datos = datos;
        this.series = series;
        this.requisitos = requisitos;
        this.comunes = comunes;
    }

    /** What the loop over the lot collects (legacy :489-506). */
    static final class Claves {
        int countTransaccionesBatch = 0;
        final List<String> series = new ArrayList<>();
        final List<String> seriesOrigenDestino = new ArrayList<>();
        final List<Long> clientes = new ArrayList<>();
        final List<Long> portafolios = new ArrayList<>();
    }

    static Claves claves(AporteReinversionContexto c) {
        Claves k = new Claves();
        for (InSolicitudesAporte mov : c.entrada.p_SolicitudesAporte) {
            if (AporteReinversionConstantes.SI.equalsIgnoreCase(mov.SolicitudBatch)) {
                k.countTransaccionesBatch++;
            }
            if (!k.series.contains(mov.IdSerieFondo)) k.series.add(mov.IdSerieFondo);
            if (!k.seriesOrigenDestino.contains(mov.IdSerieFondoOrigen)) {
                k.seriesOrigenDestino.add(mov.IdSerieFondoOrigen);
            }
            if (!k.seriesOrigenDestino.contains(mov.IdSerieFondo)) {
                k.seriesOrigenDestino.add(mov.IdSerieFondo);
            }
            if (!k.clientes.contains(mov.IdCliente)) k.clientes.add(mov.IdCliente);
            if (!k.portafolios.contains(mov.IdPortafolio)) k.portafolios.add(mov.IdPortafolio);
            if (!c.regimen.contains(mov.IdRegimenTributario)) c.regimen.add(mov.IdRegimenTributario);
        }
        return k;
    }

    /** The nine reads and the hashes of the legacy, in its order. */
    void leer(AporteReinversionContexto c, Claves k) throws Exception {
        Long idEmpresa = c.entrada.p_IdEmpresa;
        List<String> idTipoMovimientos = new ArrayList<>();
        idTipoMovimientos.add(AporteReinversionConstantes.APORTE_REINVERSION);
        List<SerieFondoSolicitudes> seriesSolicitudes = series.series(idEmpresa, k.series);
        List<SerieFondoSolicitudes> seriesSolicitudesOrigenDestino = series.series(idEmpresa, k.seriesOrigenDestino);
        List<ClientePortafolioSolicitudes> clientesSolicitudes = requisitos.clientesPortafolio(k.portafolios);
        var tipoMovimientos = comunes.tiposMovimiento(idTipoMovimientos);
        var regimenesSeries = datos.regimenes(idEmpresa, k.series, c.regimen);
        var indicadoresPortafolio = datos.indicadoresPortafolio(k.portafolios, AporteReinversionConstantes.USA_CUENTA_INVERSION);
        var indicadoresPortafolioFFMM = datos.indicadoresPortafolioFondoMutuo(k.portafolios);
        var configuracionesRegimenPortafolio = datos.configuracionProposito(c.regimen);
        List<String> monedas = new ArrayList<>();
        for (SerieFondoSolicitudes serie : seriesSolicitudes) {
            if (!monedas.contains(serie.IdMoneda)) monedas.add(serie.IdMoneda);
        }
        var monedasFondo = comunes.monedas(monedas);

        c.hseries = Hashes.aHash(seriesSolicitudes, s -> s.IdSerieFondo);
        c.hseriesOrigenDestino = Hashes.aHash(seriesSolicitudesOrigenDestino, s -> s.IdSerieFondo);
        c.hclientes = Hashes.aHash(clientesSolicitudes, x -> x.IdPortafolio);
        c.htipoMovimiento = Hashes.aHash(tipoMovimientos, t -> t.IdTipoMovimientoFondoMutuo);
        c.hseriesRegimen = Hashes.aHash(regimenesSeries, r -> r.IdRegimenFondoMutuoSerieFondo);
        c.hindicadoresPortafolio = Hashes.aHash(indicadoresPortafolio, i -> i.IdPortafolio);
        c.hindicadoresPortafolioFFMM = Hashes.aHash(indicadoresPortafolioFFMM, i -> i.IdPortafolio);
        c.hmonedasFondo = Hashes.aHash(monedasFondo, m -> m.IdMoneda);
        c.hconfRegimenPortafolio = Hashes.aHash(configuracionesRegimenPortafolio, x -> x.Id);
    }

    /**
     * The type of each reinversion from the origin and destination series (legacy :603-621; the later rule wins, so a null series or flag raises the NullPointerException of the
     * legacy: every access is kept as written). SWITCH: foreign funds of the same issuer; INTERNACIONAL: foreign funds of different issuers; NACIONAL: to a national fund or from one;
     * VALUTASNACUSD: between national dollar and national peso issuers, or international dollar to national dollar.
     */
    static void clasificar(AporteReinversionContexto c) {
        for (InSolicitudesAporte solicitud : c.entrada.p_SolicitudesAporte) {
            SerieFondoSolicitudes serieOrigen = c.hseriesOrigenDestino.get(solicitud.IdSerieFondoOrigen);
            SerieFondoSolicitudes serieDestino = c.hseriesOrigenDestino.get(solicitud.IdSerieFondo);
            if (serieOrigen.FondoExtranjero.equalsIgnoreCase("S") && serieDestino.FondoExtranjero.equalsIgnoreCase("S") && serieOrigen.IdEmisor.equalsIgnoreCase(serieDestino.IdEmisor))
                solicitud.TipoReinversion = "SWITCH";
            if (serieOrigen.FondoExtranjero.equalsIgnoreCase("S") && serieDestino.FondoExtranjero.equalsIgnoreCase("S") && !serieOrigen.IdEmisor.equalsIgnoreCase(serieDestino.IdEmisor))
                solicitud.TipoReinversion = "INTERNACIONAL";
            if (serieOrigen.FondoExtranjero.equalsIgnoreCase("S") && serieDestino.FondoExtranjero.equalsIgnoreCase("N"))
                solicitud.TipoReinversion = "NACIONAL";
            if (serieOrigen.FondoExtranjero.equalsIgnoreCase("N"))
                solicitud.TipoReinversion = "NACIONAL";
            if (serieOrigen.EmisorFondoNacionalDolar.equalsIgnoreCase("S") && serieDestino.EmisorFondoNacionalPesos.equalsIgnoreCase("S"))
                solicitud.TipoReinversion = "VALUTASNACUSD";
            if (serieOrigen.EmisorFondoNacionalPesos.equalsIgnoreCase("S") && serieDestino.EmisorFondoNacionalDolar.equalsIgnoreCase("S"))
                solicitud.TipoReinversion = "VALUTASNACUSD";
            if (serieOrigen.EmisorFondoInternacionalDolar.equalsIgnoreCase("S") && serieDestino.EmisorFondoNacionalDolar.equalsIgnoreCase("S"))
                solicitud.TipoReinversion = "VALUTASNACUSD";
        }
    }
}
