-- Busca las garantias asociadas a la simultanea
SELECT
     GTA."IdGarantia"
    ,GTA."FolioGarantia"
    ,GTA."FolioCarteraOrigen"
    ,GTA."IdCarteraOrigen"
    ,GTA."INGCantidad"
    ,GTA."INGValorRescate"
    ,GTA."INGMontoATasaGarantiaActual"
    ,GTA."PrecioCompraOperacionOrigen"
    ,GTA."TasaCompraOperacionOrigen"
    ,CAR."IdTipoOperacion" AS "IdTipoOperacionOrigen"
    ,GTA."IdCliente" 
    ,GTA."IdPortafolio"
    ,GTA."IdInstrumento"
    ,GTA."IdEmisor"
    ,GTA."IdOrigenGarantia"
    ,GTA."IdConcepto"
    ,CAR."IdBoveda"
	,GTA."IdTipoCanalIngreso"    
    ,CAR."IdMonedaInstrumento"
    
FROM "BSCL.Custodia.BD::GTA.Garantia" AS GTA
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON (CAR."FolioComprobante" = GTA."FolioComprobante" AND CAR."IdEstado" = 'VIG')
WHERE CAR."FolioCartera" IN (:FolioCarteras)
AND   GTA."IdEstado" = 'VIG';