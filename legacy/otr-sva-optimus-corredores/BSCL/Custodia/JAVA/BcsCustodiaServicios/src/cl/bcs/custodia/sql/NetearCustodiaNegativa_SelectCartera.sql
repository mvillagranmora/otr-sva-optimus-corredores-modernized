SELECT
CARTERA."IdCartera"
,CARTERA."FolioCartera"
,CARTERA."IdPortafolio"
,CARTERA."IdInstrumento"
,CARTERA."IdClasificacionTipoInstrumento"
,CARTERA."Nemotecnico"
,CARTERA."NemoSVS"
,CARTERA."IdBoveda"
,CARTERA."FechaLiquidacion"
,CARTERA."INGCantidad" - COALESCE(RESERVA_TOTAL."INGCantidad", 0) AS "Cantidad"
,CARTERA."INGValorRescate" - COALESCE(RESERVA_TOTAL."INGValorRescate", 0) AS "ValorRescate"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CARTERA
LEFT JOIN (
    SELECT RESERVA."FolioCarteraOrigen"
        ,SUM(RESERVA."IngCantidad") AS "INGCantidad"
        ,SUM(RESERVA."IngValorRescate") AS "INGValorRescate"
    FROM "BSCL.Custodia.BD::CUS.Reserva" AS RESERVA 
    WHERE RESERVA."IdEstado"='VIG' AND RESERVA."EgrCantidad" = 0
    GROUP BY RESERVA."FolioCarteraOrigen"
) AS RESERVA_TOTAL ON RESERVA_TOTAL."FolioCarteraOrigen" = CARTERA."FolioCartera" 
WHERE 
CARTERA."IdPortafolio" IN (:IdPortafolio) 
AND CARTERA."IdInstrumento" IN (:IdInstrumento)
AND CARTERA."IdEstado" = 'VIG'
ORDER BY CARTERA."FechaLiquidacion" ASC, CARTERA."FolioCartera" ASC