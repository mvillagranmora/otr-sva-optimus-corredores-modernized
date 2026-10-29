package cl.bcs.apv.application.traspasos;

import java.math.BigDecimal;

import cl.bcs.apv.api.dto.calcularmontosdesinversion.Custodia;

/** Mutable working copy of the legacy {@code CalculoMontosDesinversion.Custodia} (the legacy logic mutates the input objects and returns them in the output). Fields keep the legacy names. */
final class CustodiaDesinversion {

    Long IdRegistro;
    String NombreProductoAgrupador;
    String Regimen;
    String IdClasificacionTipoInstrumento;
    String IdTipoInstrumento;
    String Nemotecnico;
    BigDecimal CantidadCustodia;
    BigDecimal PPPCustodia;
    BigDecimal ValorPPPCustodia;
    BigDecimal MontoPPPCustodia;
    BigDecimal CantidadADesinvertir;
    BigDecimal MontoADesinvertir;
    BigDecimal MontoADesinvertirCLP;
    Long IdPortafolio;
    String IdEmisor;
    Long IdInstrumento;
    String IdMonedaInstrReajuste;
    String DocumentoIdentidad;
    String DocumentoIdentidadRutCavali;
    String NombreCliente;
    String Relacion;
    String ISIN;
    String ClasificacionRiesgoInstrumento;
    String EsFungible;
    String EsPagaderoEn;
    String IdBoveda;
    String NumeroCuentaBoveda;
    BigDecimal TotalPosicion;
    BigDecimal TotalPosicionPresunta;
    BigDecimal Rentabilidad;
    BigDecimal PrecioMercadoCustodia;
    BigDecimal ValorMercadoCustodia;
    BigDecimal MontoMercadoCustodia;
    BigDecimal DiferenciaMercadoCustodia;
    BigDecimal CantidadGarantia;
    BigDecimal PrecioMercadoGarantia;
    BigDecimal ValorMercadoGarantia;
    BigDecimal DiferenciaMercadoGarantia;
    BigDecimal CantidadSimulCom;
    BigDecimal PrecioMercadoSimulCom;
    BigDecimal ValorMercadoSimulCom;
    BigDecimal DiferenciaMercadoSimulCom;
    BigDecimal CantidadSimulVen;
    BigDecimal PrecioMercadoSimulVen;
    BigDecimal ValorMercadoSimulVen;
    BigDecimal DiferenciaMercadoSimulVen;
    BigDecimal CantidadPrestamo;
    BigDecimal PrecioMercadoPrestamo;
    BigDecimal ValorMercadoPrestamo;
    BigDecimal DiferenciaMercadoPrestamo;
    BigDecimal CantidadEmpresti;
    BigDecimal PrecioMercadoEmpresti;
    BigDecimal ValorMercadoEmpresti;
    BigDecimal DiferenciaMercadoEmpresti;
    BigDecimal CantidadPactosV;
    BigDecimal PrecioMercadoPactosV;
    BigDecimal ValorMercadoPactosV;
    BigDecimal DiferenciaMercadoPactosV;
    BigDecimal CantidadPactosC;
    BigDecimal PrecioMercadoPactosC;
    BigDecimal ValorMercadoPactosC;
    BigDecimal DiferenciaMercadoPactosC;
    BigDecimal CantidadReporteCompra;
    BigDecimal PrecioMercadoReporteCompra;
    BigDecimal ValorMercadoReporteCompra;
    BigDecimal DiferenciaMercadoReporteCompra;
    BigDecimal CantidadReporteVenta;
    BigDecimal PrecioMercadoReporteVenta;
    BigDecimal ValorMercadoReporteVenta;
    BigDecimal DiferenciaMercadoReporteVenta;
    BigDecimal CantidadTransIng;
    BigDecimal CantidadTransEgr;
    Long IdInstrumentoCustodia;
    String IdMonedaInstrumento;
    String FechaOperacion;

    static CustodiaDesinversion de(Custodia r) {
        if (r == null) {
            return null;
        }
        CustodiaDesinversion c = new CustodiaDesinversion();
        c.IdRegistro = r.idRegistro();
        c.NombreProductoAgrupador = r.nombreProductoAgrupador();
        c.Regimen = r.regimen();
        c.IdClasificacionTipoInstrumento = r.idClasificacionTipoInstrumento();
        c.IdTipoInstrumento = r.idTipoInstrumento();
        c.Nemotecnico = r.nemotecnico();
        c.CantidadCustodia = r.cantidadCustodia();
        c.PPPCustodia = r.pppCustodia();
        c.ValorPPPCustodia = r.valorPPPCustodia();
        c.MontoPPPCustodia = r.montoPPPCustodia();
        c.CantidadADesinvertir = r.cantidadADesinvertir();
        c.MontoADesinvertir = r.montoADesinvertir();
        c.MontoADesinvertirCLP = r.montoADesinvertirCLP();
        c.IdPortafolio = r.idPortafolio();
        c.IdEmisor = r.idEmisor();
        c.IdInstrumento = r.idInstrumento();
        c.IdMonedaInstrReajuste = r.idMonedaInstrReajuste();
        c.DocumentoIdentidad = r.documentoIdentidad();
        c.DocumentoIdentidadRutCavali = r.documentoIdentidadRutCavali();
        c.NombreCliente = r.nombreCliente();
        c.Relacion = r.relacion();
        c.ISIN = r.isin();
        c.ClasificacionRiesgoInstrumento = r.clasificacionRiesgoInstrumento();
        c.EsFungible = r.esFungible();
        c.EsPagaderoEn = r.esPagaderoEn();
        c.IdBoveda = r.idBoveda();
        c.NumeroCuentaBoveda = r.numeroCuentaBoveda();
        c.TotalPosicion = r.totalPosicion();
        c.TotalPosicionPresunta = r.totalPosicionPresunta();
        c.Rentabilidad = r.rentabilidad();
        c.PrecioMercadoCustodia = r.precioMercadoCustodia();
        c.ValorMercadoCustodia = r.valorMercadoCustodia();
        c.MontoMercadoCustodia = r.montoMercadoCustodia();
        c.DiferenciaMercadoCustodia = r.diferenciaMercadoCustodia();
        c.CantidadGarantia = r.cantidadGarantia();
        c.PrecioMercadoGarantia = r.precioMercadoGarantia();
        c.ValorMercadoGarantia = r.valorMercadoGarantia();
        c.DiferenciaMercadoGarantia = r.diferenciaMercadoGarantia();
        c.CantidadSimulCom = r.cantidadSimulCom();
        c.PrecioMercadoSimulCom = r.precioMercadoSimulCom();
        c.ValorMercadoSimulCom = r.valorMercadoSimulCom();
        c.DiferenciaMercadoSimulCom = r.diferenciaMercadoSimulCom();
        c.CantidadSimulVen = r.cantidadSimulVen();
        c.PrecioMercadoSimulVen = r.precioMercadoSimulVen();
        c.ValorMercadoSimulVen = r.valorMercadoSimulVen();
        c.DiferenciaMercadoSimulVen = r.diferenciaMercadoSimulVen();
        c.CantidadPrestamo = r.cantidadPrestamo();
        c.PrecioMercadoPrestamo = r.precioMercadoPrestamo();
        c.ValorMercadoPrestamo = r.valorMercadoPrestamo();
        c.DiferenciaMercadoPrestamo = r.diferenciaMercadoPrestamo();
        c.CantidadEmpresti = r.cantidadEmpresti();
        c.PrecioMercadoEmpresti = r.precioMercadoEmpresti();
        c.ValorMercadoEmpresti = r.valorMercadoEmpresti();
        c.DiferenciaMercadoEmpresti = r.diferenciaMercadoEmpresti();
        c.CantidadPactosV = r.cantidadPactosV();
        c.PrecioMercadoPactosV = r.precioMercadoPactosV();
        c.ValorMercadoPactosV = r.valorMercadoPactosV();
        c.DiferenciaMercadoPactosV = r.diferenciaMercadoPactosV();
        c.CantidadPactosC = r.cantidadPactosC();
        c.PrecioMercadoPactosC = r.precioMercadoPactosC();
        c.ValorMercadoPactosC = r.valorMercadoPactosC();
        c.DiferenciaMercadoPactosC = r.diferenciaMercadoPactosC();
        c.CantidadReporteCompra = r.cantidadReporteCompra();
        c.PrecioMercadoReporteCompra = r.precioMercadoReporteCompra();
        c.ValorMercadoReporteCompra = r.valorMercadoReporteCompra();
        c.DiferenciaMercadoReporteCompra = r.diferenciaMercadoReporteCompra();
        c.CantidadReporteVenta = r.cantidadReporteVenta();
        c.PrecioMercadoReporteVenta = r.precioMercadoReporteVenta();
        c.ValorMercadoReporteVenta = r.valorMercadoReporteVenta();
        c.DiferenciaMercadoReporteVenta = r.diferenciaMercadoReporteVenta();
        c.CantidadTransIng = r.cantidadTransIng();
        c.CantidadTransEgr = r.cantidadTransEgr();
        c.IdInstrumentoCustodia = r.idInstrumentoCustodia();
        c.IdMonedaInstrumento = r.idMonedaInstrumento();
        c.FechaOperacion = r.fechaOperacion();
        return c;
    }

    Custodia aRecord() {
        return new Custodia(
                IdRegistro,
                NombreProductoAgrupador,
                Regimen,
                IdClasificacionTipoInstrumento,
                IdTipoInstrumento,
                Nemotecnico,
                CantidadCustodia,
                PPPCustodia,
                ValorPPPCustodia,
                MontoPPPCustodia,
                CantidadADesinvertir,
                MontoADesinvertir,
                MontoADesinvertirCLP,
                IdPortafolio,
                IdEmisor,
                IdInstrumento,
                IdMonedaInstrReajuste,
                DocumentoIdentidad,
                DocumentoIdentidadRutCavali,
                NombreCliente,
                Relacion,
                ISIN,
                ClasificacionRiesgoInstrumento,
                EsFungible,
                EsPagaderoEn,
                IdBoveda,
                NumeroCuentaBoveda,
                TotalPosicion,
                TotalPosicionPresunta,
                Rentabilidad,
                PrecioMercadoCustodia,
                ValorMercadoCustodia,
                MontoMercadoCustodia,
                DiferenciaMercadoCustodia,
                CantidadGarantia,
                PrecioMercadoGarantia,
                ValorMercadoGarantia,
                DiferenciaMercadoGarantia,
                CantidadSimulCom,
                PrecioMercadoSimulCom,
                ValorMercadoSimulCom,
                DiferenciaMercadoSimulCom,
                CantidadSimulVen,
                PrecioMercadoSimulVen,
                ValorMercadoSimulVen,
                DiferenciaMercadoSimulVen,
                CantidadPrestamo,
                PrecioMercadoPrestamo,
                ValorMercadoPrestamo,
                DiferenciaMercadoPrestamo,
                CantidadEmpresti,
                PrecioMercadoEmpresti,
                ValorMercadoEmpresti,
                DiferenciaMercadoEmpresti,
                CantidadPactosV,
                PrecioMercadoPactosV,
                ValorMercadoPactosV,
                DiferenciaMercadoPactosV,
                CantidadPactosC,
                PrecioMercadoPactosC,
                ValorMercadoPactosC,
                DiferenciaMercadoPactosC,
                CantidadReporteCompra,
                PrecioMercadoReporteCompra,
                ValorMercadoReporteCompra,
                DiferenciaMercadoReporteCompra,
                CantidadReporteVenta,
                PrecioMercadoReporteVenta,
                ValorMercadoReporteVenta,
                DiferenciaMercadoReporteVenta,
                CantidadTransIng,
                CantidadTransEgr,
                IdInstrumentoCustodia,
                IdMonedaInstrumento,
                FechaOperacion);
    }
}
