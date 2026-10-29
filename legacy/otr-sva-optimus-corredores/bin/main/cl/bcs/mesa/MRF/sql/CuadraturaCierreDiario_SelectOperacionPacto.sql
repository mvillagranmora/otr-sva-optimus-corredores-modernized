SELECT
 CC."IdOperacion" AS "IdOperacion"
,CC."FolioOperacion" AS "FolioOperacion"
,CC."RazonSocial"
,CC."MontoOperacion" AS "MontoOperacion"
,CC."MontoOperacion" AS "MontoOperacionMoneda"
,CC."IdMonedaLiquidacion" AS "IdMoneda"
,CC."IdMonedaLiquidacion" AS "IdMonedaLiquidacion"
,CC."IdMesaOrigen" AS "IdMesaOrigen"
,'' AS "IdEstado"
------
,CC."ValorOperacion"
,CC."MontoCompromiso"
,CC."ValorCompromiso"
,COALESCE(CC."ValorInteresCartera",TO_BIGINT (0)) AS "Intereses"
FROM "BSCL.Mesa.BD.Vistas::ConsultarOperacionesDiariasPactos" CC
WHERE CC."IdEmpresa" = :IdEmpresa AND CC."FechaOperacion"=:Fecha
AND CC."IdEstado"='VIG';