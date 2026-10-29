SELECT
 CC."IdCartera" AS "IdOperacion"
,CC."FolioCartera" AS "FolioOperacion"
,CC."RazonSocial"
,CC."INGMontoATasaCompraActual" AS "MontoOperacion"
,CC."INGMontoCompraActualMonedaInstrumento" AS "MontoOperacionMoneda"
,CC."IdMonedaInstrReajuste" AS "IdMoneda"
,CC."IdMonedaInstrReajuste" AS "IdMonedaLiquidacion"
,CC."IdMesa" AS "IdMesaOrigen"
,CC."IdEstado"
------
,CC."IdClasificacionTipoInstrumento"
,CASE WHEN CC."IdClasificacionTipoInstrumento"='IF' THEN CC."INGValorRescate" ELSE CC."INGCantidad"  END AS "INGCantidad"
,CC."INGValorRescate"
,CC."INGMontoATasaCompraActual"
,CC."INGMontoCompraActualMonedaInstrumento"
,CC."INGMontoATasaMercadoActual"
,CC."INGMontoMercadoActualMonedaInstrumento"
,CC."ReajusteCorridoMonedaInstrumento"

,CC."IdTipoInstrumento"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraUI" CC
WHERE CC."IdEstado"  NOT IN ('ANU','RPA') AND CC."IdTipoInstrumento" NOT IN ('ACN','SIM','PRT','EMP','PAC', 'FWD','RRV','RRF')
AND "IdClasificacionTipoInstrumento" IN ('IF','RF')
AND CC."IdEmpresa" = :IdEmpresa 
AND CC."FechaOperacion" < :Fecha AND CC."EGRCantidad" = 0 AND CC."IdEstado" = 'VIG';