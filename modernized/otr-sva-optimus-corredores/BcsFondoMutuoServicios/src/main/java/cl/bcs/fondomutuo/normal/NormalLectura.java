package cl.bcs.fondomutuo.normal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.comisionnn.ComisionNnDatos;
import cl.bcs.fondomutuo.normal.NormalContrato.InSolicitud;
import cl.bcs.fondomutuo.normal.NormalDatos.SalidaTraspasoTotal;
import cl.bcs.fondomutuo.normal.NormalDatos.TraspasoAPV;
import cl.bcs.fondomutuo.requisitos.Hashes;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos.ClientePortafolioSolicitudes;

/** The support reads of {@code CrearSolicitudAporteRescateNormal} (legacy :392-532), in the legacy order, into the request context. */
final class NormalLectura {

    private final NormalDatos datos;
    private final SeriesFondoPort series;
    private final RequisitosDatos requisitos;
    private final ComisionNnDatos comunes;

    NormalLectura(NormalDatos datos, SeriesFondoPort series, RequisitosDatos requisitos, ComisionNnDatos comunes) {
        this.datos = datos;
        this.series = series;
        this.requisitos = requisitos;
        this.comunes = comunes;
    }

    /** What the loop over the request collects (legacy :403-437), also fixing the payment currency when it is empty (it mutates the input, as the legacy). */
    static final class Claves {
        int countTransaccionesBatch = 0;
        final List<String> series = new ArrayList<>();
        final List<Long> portafolios = new ArrayList<>();
        final List<String> regimen = new ArrayList<>();
        final List<Long> foliosContrato = new ArrayList<>();
        boolean existeTraspasoTotal = false;
    }

    static Claves claves(NormalContexto c) {
        Claves k = new Claves();
        List<Long> clientes = new ArrayList<>();
        for (InSolicitud mov : c.entrada.p_SolicitudesAporteRescate) {
            if (mov.IdMonedaPago == null || mov.IdMonedaPago.isEmpty()) {
                mov.IdMonedaPago = mov.IdMoneda;
            }
            if (NormalConstantes.SI.equalsIgnoreCase(mov.SolicitudBatch)) {
                k.countTransaccionesBatch++;
            }
            if (!k.series.contains(mov.IdSerieFondo)) k.series.add(mov.IdSerieFondo);
            if (!clientes.contains(mov.IdCliente)) clientes.add(mov.IdCliente);
            if (!k.portafolios.contains(mov.IdPortafolio)) k.portafolios.add(mov.IdPortafolio);
            if (!k.regimen.contains(mov.IdRegimenTributario)) k.regimen.add(mov.IdRegimenTributario);
            if (!k.foliosContrato.contains(mov.FolioContrato)) k.foliosContrato.add(mov.FolioContrato);
            if (NormalConstantes.CALCULO_POR_TOTAL.equals(mov.CalculoPorMontoCuota)) k.existeTraspasoTotal = true;
        }
        return k;
    }

    /** The twelve reads and the hashes of the legacy (:453-532). */
    void leer(NormalContexto c, Claves k) throws Exception {
        Long idEmpresa = c.entrada.p_IdEmpresa;
        List<String> idTipoMovimientos = new ArrayList<>();
        idTipoMovimientos.add(NormalConstantes.APORTE_FONDO_MUTUO);
        idTipoMovimientos.add(NormalConstantes.RESCATE_FONDO_MUTUO);
        idTipoMovimientos.add(NormalConstantes.RESCATE_TRASPASO);
        idTipoMovimientos.add(NormalConstantes.APORTE_TRASPASO);
        List<SerieFondoSolicitudes> seriesSolicitudes = series.series(idEmpresa, k.series);
        List<ClientePortafolioSolicitudes> clientesSolicitudes = requisitos.clientesPortafolio(k.portafolios);
        var tipoMovimientos = comunes.tiposMovimiento(idTipoMovimientos);
        var regimenesSeries = datos.regimenes(idEmpresa, k.series, k.regimen);
        var indicadoresPortafolio = datos.indicadoresPortafolio(k.portafolios, NormalConstantes.USA_CUENTA_INVERSION);
        var portafolioAPV = datos.portafoliosApv(k.portafolios);
        var configuracionesRegimenPortafolio = datos.configuracionProposito(k.regimen);
        List<TraspasoAPV> traspasosAPV = datos.traspasosApv(k.foliosContrato);
        List<SalidaTraspasoTotal> custodiasDC = new ArrayList<>();
        if (k.existeTraspasoTotal) {
            custodiasDC = datos.custodiaTraspasoTotal(k.series, k.portafolios);
        }
        List<String> monedas = new ArrayList<>();
        for (SerieFondoSolicitudes serie : seriesSolicitudes) {
            if (!monedas.contains(serie.IdMoneda)) monedas.add(serie.IdMoneda);
        }
        var monedasFondo = comunes.monedas(monedas);

        c.hseries = Hashes.aHash(seriesSolicitudes, s -> s.IdSerieFondo);
        c.hclientes = Hashes.aHash(clientesSolicitudes, x -> x.IdPortafolio);
        c.htipoMovimiento = Hashes.aHash(tipoMovimientos, t -> t.IdTipoMovimientoFondoMutuo);
        c.hseriesRegimen = Hashes.aHash(regimenesSeries, r -> r.IdRegimenFondoMutuoSerieFondo);
        c.hindicadoresPortafolio = Hashes.aHash(indicadoresPortafolio, i -> i.IdPortafolio);
        c.hmonedasFondo = Hashes.aHash(monedasFondo, m -> m.IdMoneda);
        c.hportafolioAPV = Hashes.aHash(portafolioAPV, p -> p.IdPortafolio);
        c.hconfRegimenPortafolio = Hashes.aHash(configuracionesRegimenPortafolio, x -> x.Id);
        c.hcustodiasDC = Hashes.aHash(custodiasDC, x -> x.Id);
        // legacy: Collectors.toMap(IdTraspaso): a repeated id raises IllegalStateException (D-NOR-9); a null key goes to HashMap.merge (NPE on a null value)
        Map<Long, TraspasoAPV> traspasos = traspasosAPV.stream().collect(Collectors.toMap(t -> t.IdTraspaso, t -> t));
        c.hTraspasoAPV = traspasos;
    }
}
