package cl.bcs.ordenjobs.domain;

import java.math.BigDecimal;

public record TransaccionRV(
    String tradeID,
    String tradeTime,
    String codTicker,
    String action,
    String instrument,
    String market,
    String settlementCondition,
    String orderType,
    String indivisible,
    BigDecimal quantity,
    BigDecimal price,
    BigDecimal amount,
    BigDecimal closingPrice,
    String buyerBroker,
    String buyEnteringFirm,
    String buyEnteringTrader,
    String sellerBroker,
    String sellEnteringFirm,
    String sellEnteringTrader,
    BigDecimal priceBuyingQTY,
    BigDecimal priceBuyingPrice,
    BigDecimal priceSellingQty,
    BigDecimal priceSellingPrice,
    String codIsin,
    String securityType,
    String timeInForce,
    String tradingSessionID,
    String entryType,
    String priceExchange,
    String priceTime
) {}
