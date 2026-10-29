WITH v_TabEventosPaso AS(
    SELECT
         "IdCartera"                                        AS  "IdMovimiento"
        ,TO_BIGINT(:p_IdTipoCentralizacion)                  AS "IdTipoCentralizacion"
        ,CASE WHEN ("INGMontoATasaMercadoActual" - "INGMontoATasaMercadoAnterior") > 0 THEN ("INGMontoATasaMercadoActual" - "INGMontoATasaMercadoAnterior") ELSE 0 END  AS "MayorMontoMercado"
        ,CASE WHEN ("INGMontoATasaMercadoActual" - "INGMontoATasaMercadoAnterior") < 0 THEN ABS(("INGMontoATasaMercadoActual" - "INGMontoATasaMercadoAnterior")) ELSE 0 END AS "MenorMontoMercado"              
        ,CASE WHEN ("INGMontoATasaMercadoActual" - "INGMontoATasaCompraActual") > 0 THEN ("INGMontoATasaMercadoActual" - "INGMontoATasaCompraActual") ELSE 0 END AS "MayorMontoCompra"
        ,CASE WHEN ("INGMontoATasaMercadoActual" - "INGMontoATasaCompraActual") < 0 THEN ABS(("INGMontoATasaMercadoActual" - "INGMontoATasaCompraActual")) ELSE 0 END AS "MenorMontoCompra"
        
        ,CASE WHEN ("INGMontoMercadoActualMonedaInstrumento" - "INGMontoMercadoAnteriorMonedaInstrumento") > 0 THEN ("INGMontoMercadoActualMonedaInstrumento" - "INGMontoMercadoAnteriorMonedaInstrumento") ELSE 0 END  AS "MayorValorMercado"
        ,CASE WHEN ("INGMontoMercadoActualMonedaInstrumento" - "INGMontoMercadoAnteriorMonedaInstrumento") < 0 THEN ABS(("INGMontoMercadoActualMonedaInstrumento" - "INGMontoMercadoAnteriorMonedaInstrumento")) ELSE 0 END AS "MenorValorMercado"              
        ,CASE WHEN ("INGMontoMercadoActualMonedaInstrumento" - "INGMontoCompraActualMonedaInstrumento") > 0 THEN ("INGMontoMercadoActualMonedaInstrumento" - "INGMontoCompraActualMonedaInstrumento") ELSE 0 END AS "MayorValorCompra"
        ,CASE WHEN ("INGMontoMercadoActualMonedaInstrumento" - "INGMontoCompraActualMonedaInstrumento") < 0 THEN ABS(("INGMontoMercadoActualMonedaInstrumento" - "INGMontoCompraActualMonedaInstrumento")) ELSE 0 END AS "MenorValorCompra"
        ,"IdMonedaSistema"
        ,"IdMonedaInstrumento"
    FROM "BSCL.Custodia.BD::HIS.Cartera" ConsultarCartera
    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS cli ON cli."IdCliente" = ConsultarCartera."IdCliente"
    WHERE ConsultarCartera."IdEmpresa" = :p_IdEmpresa
    AND ConsultarCartera."FechaOperacion" <= :p_FechaContable
    AND ConsultarCartera."FechaRespaldo" = :p_FechaContable
    AND ConsultarCartera."EGRCantidad" = 0
    AND ConsultarCartera."IdClasificacionTipoInstrumento" = 'RV'
    AND (ConsultarCartera."IdTipoInstrumento" NOT IN ('PRT','EMP','PAC', 'FWD','RRV','RRF') OR (ConsultarCartera."IdTipoInstrumento" = 'SIM' AND ConsultarCartera."IdTipoOperacion" = 'VENSIM'))
    AND cli."DocumentoIdentidad" = :p_DocumentoIdentidadCorredora
    AND ConsultarCartera."IdEstado" = 'VIG'
    UNION ALL
    SELECT
         "IdGarantia"                                       AS  "IdMovimiento"
        ,TO_BIGINT(:p_IdTipoCentralizacion)                  AS "IdTipoCentralizacion"
        ,CASE WHEN ("INGMontoATasaMercadoActual" - "INGMontoATasaMercadoAnterior") > 0 THEN ("INGMontoATasaMercadoActual" - "INGMontoATasaMercadoAnterior") ELSE 0 END AS "MayorMontoMercado"
        ,CASE WHEN ("INGMontoATasaMercadoActual" - "INGMontoATasaMercadoAnterior") < 0 THEN ABS(("INGMontoATasaMercadoActual" - "INGMontoATasaMercadoAnterior")) ELSE 0 END AS "MenorMontoMercado"
        ,CASE WHEN ("INGMontoATasaMercadoActual" - "INGMontoATasaCompraActual") > 0 THEN ("INGMontoATasaMercadoActual" - "INGMontoATasaCompraActual") ELSE 0 END AS "MayorMontoCompra"
        ,CASE WHEN ("INGMontoATasaMercadoActual" - "INGMontoATasaCompraActual") < 0 THEN ABS(("INGMontoATasaMercadoActual" - "INGMontoATasaCompraActual")) ELSE 0 END AS "MenorMontoCompra"
        
        ,CASE WHEN ("INGMontoMercadoActualMonedaInstrumento" - "INGMontoMercadoAnteriorMonedaInstrumento") > 0 THEN ("INGMontoMercadoActualMonedaInstrumento" - "INGMontoMercadoAnteriorMonedaInstrumento") ELSE 0 END  AS "MayorValorMercado"
        ,CASE WHEN ("INGMontoMercadoActualMonedaInstrumento" - "INGMontoMercadoAnteriorMonedaInstrumento") < 0 THEN ABS(("INGMontoMercadoActualMonedaInstrumento" - "INGMontoMercadoAnteriorMonedaInstrumento")) ELSE 0 END AS "MenorValorMercado"              
        ,CASE WHEN ("INGMontoMercadoActualMonedaInstrumento" - "INGMontoCompraActualMonedaInstrumento") > 0 THEN ("INGMontoMercadoActualMonedaInstrumento" - "INGMontoCompraActualMonedaInstrumento") ELSE 0 END AS "MayorValorCompra"
        ,CASE WHEN ("INGMontoMercadoActualMonedaInstrumento" - "INGMontoCompraActualMonedaInstrumento") < 0 THEN ABS(("INGMontoMercadoActualMonedaInstrumento" - "INGMontoCompraActualMonedaInstrumento")) ELSE 0 END AS "MenorValorCompra"
        ,"IdMonedaSistema"
        ,"IdMonedaInstrumento"
    FROM "BSCL.Custodia.BD.Vistas::HIS.ConsultarGarantias" ConsultarGarantias
    WHERE "IdEmpresa" = :p_IdEmpresa
AND "FechaOperacion" <= :p_FechaContable
AND ConsultarGarantias."IdClasificacionTipoInstrumento" = 'RV'
AND "FechaRespaldo" = :p_FechaContable
AND "EGRCantidad" = 0
AND "DocumentoIdentidad" = :p_DocumentoIdentidadCorredora
AND "NemoGarantizado" != 'SIMULTANEA VENTA'
AND ConsultarGarantias."IdEstado" = 'VIG'
)
SELECT "IdMovimiento","IdTipoCentralizacion",'MAYOR_VALOR_MERCADO' AS "IdEvento"	,'Mayor Valor Mercado'	AS "Nombre","MayorValorMercado"	AS "MontoEvento","IdMonedaInstrumento" AS "CodigoMoneda" 	FROM v_TabEventosPaso UNION ALL
SELECT "IdMovimiento","IdTipoCentralizacion",'MENOR_VALOR_MERCADO' AS "IdEvento" 	,'Menor Valor Mercado'	AS "Nombre","MenorValorMercado"	AS "MontoEvento","IdMonedaInstrumento" AS "CodigoMoneda"	FROM v_TabEventosPaso UNION ALL
SELECT "IdMovimiento","IdTipoCentralizacion",'MAYOR_VALOR_COMPRA'  AS "IdEvento" 	,'Mayor Valor Compra'	AS "Nombre","MayorValorCompra"	AS "MontoEvento","IdMonedaInstrumento" AS "CodigoMoneda"	FROM v_TabEventosPaso UNION ALL
SELECT "IdMovimiento","IdTipoCentralizacion",'MENOR_VALOR_COMPRA'  AS "IdEvento"	,'Menor Valor Compra'	AS "Nombre","MenorValorCompra"	AS "MontoEvento","IdMonedaInstrumento" AS "CodigoMoneda"	FROM v_TabEventosPaso UNION ALL

SELECT "IdMovimiento","IdTipoCentralizacion",'MAYOR_MONTO_MERCADO' AS "IdEvento"	,'Mayor Monto Mercado'	AS "Nombre","MayorMontoMercado"	AS "MontoEvento","IdMonedaSistema" AS "CodigoMoneda" 	FROM v_TabEventosPaso UNION ALL
SELECT "IdMovimiento","IdTipoCentralizacion",'MENOR_MONTO_MERCADO' AS "IdEvento" 	,'Menor Monto Mercado'	AS "Nombre","MenorMontoMercado"	AS "MontoEvento","IdMonedaSistema" AS "CodigoMoneda"	FROM v_TabEventosPaso UNION ALL
SELECT "IdMovimiento","IdTipoCentralizacion",'MAYOR_MONTO_COMPRA'  AS "IdEvento" 	,'Mayor Monto Compra'	AS "Nombre","MayorMontoCompra"	AS "MontoEvento","IdMonedaSistema" AS "CodigoMoneda"	FROM v_TabEventosPaso UNION ALL
SELECT "IdMovimiento","IdTipoCentralizacion",'MENOR_MONTO_COMPRA'  AS "IdEvento"	,'Menor Monto Compra'	AS "Nombre","MenorMontoCompra"	AS "MontoEvento","IdMonedaSistema" AS "CodigoMoneda"	FROM v_TabEventosPaso; 
   