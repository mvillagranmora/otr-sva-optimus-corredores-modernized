package cl.bcs.ordenjobs.service;

import cl.bcs.ordenjobs.domain.TransaccionRV;
import org.springframework.stereotype.Service;

@Service
public class ControlMercadoService {

    // B-ORD-06: constantes pinned — valores exactos del legacy
    public static final String COMPRA = "COM";
    public static final String VENTA  = "VEN";

    // B-ORD-07
    public static final String FLUJO_MODIFICACION_AUTO = "MODIFICACION_AUTO";

    // B-ORD-08: espacio, no guion bajo
    public static final String MOTIVO_ANULACION = "ANULACION POR SISTEMA BURSATIL";

    // B-ORD-05: llave de trade — tradeID + fechaSistema + tipo
    public static String keyCompra(String tradeID, String fechaSistema) {
        return tradeID + fechaSistema + COMPRA;
    }

    public static String keyVenta(String tradeID, String fechaSistema) {
        return tradeID + fechaSistema + VENTA;
    }

    // B-ORD-09: igualdad de transacciones usa BigDecimal.compareTo == 0 (no .equals)
    // BigDecimal.compareTo ignora escala; .equals no → oracle pinned
    public static boolean sonTransaccionesIguales(TransaccionRV a, TransaccionRV b) {
        return a.codTicker().equalsIgnoreCase(b.codTicker())
            && a.action().equalsIgnoreCase(b.action())
            && a.tradeTime().equalsIgnoreCase(b.tradeTime())
            && a.instrument().equalsIgnoreCase(b.instrument())
            && a.market().equalsIgnoreCase(b.market())
            && a.settlementCondition().equalsIgnoreCase(b.settlementCondition())
            && a.orderType().equalsIgnoreCase(b.orderType())
            && a.indivisible().equalsIgnoreCase(b.indivisible())
            && a.quantity().compareTo(b.quantity()) == 0
            && a.price().compareTo(b.price()) == 0
            && a.amount().compareTo(b.amount()) == 0
            && a.closingPrice().compareTo(b.closingPrice()) == 0
            && a.buyerBroker().equalsIgnoreCase(b.buyerBroker())
            && a.buyEnteringFirm().equalsIgnoreCase(b.buyEnteringFirm())
            && a.buyEnteringTrader().equalsIgnoreCase(b.buyEnteringTrader())
            && a.sellerBroker().equalsIgnoreCase(b.sellerBroker())
            && a.sellEnteringFirm().equalsIgnoreCase(b.sellEnteringFirm())
            && a.sellEnteringTrader().equalsIgnoreCase(b.sellEnteringTrader())
            && a.priceBuyingQTY().compareTo(b.priceBuyingQTY()) == 0
            && a.priceBuyingPrice().compareTo(b.priceBuyingPrice()) == 0
            && a.priceSellingQty().compareTo(b.priceSellingQty()) == 0
            && a.priceSellingPrice().compareTo(b.priceSellingPrice()) == 0
            && a.codIsin().equalsIgnoreCase(b.codIsin())
            && a.securityType().equalsIgnoreCase(b.securityType())
            && a.timeInForce().equalsIgnoreCase(b.timeInForce())
            && a.tradingSessionID().equalsIgnoreCase(b.tradingSessionID())
            && a.entryType().equalsIgnoreCase(b.entryType())
            && a.priceExchange().equalsIgnoreCase(b.priceExchange())
            && a.priceTime().equalsIgnoreCase(b.priceTime());
    }
}
