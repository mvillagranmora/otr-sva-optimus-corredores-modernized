SELECT  
	g."IdGarantia"
	,g."INGCantidad" AS "Cantidad"
	,g."INGValorRescate" AS "ValorRescate"
	,g."INGMontoCompraActualMonedaInstrumento" AS "MontoMonedaInstrumento"
	,g."IdCliente"
	,g."IdPortafolio"
	,g."IdGarantia" AS "IdCustodia"
	,c."IdCartera"
	,c."FolioCartera"
	,c."IdOperacion"
	,g."IdConcepto"
	,g."IdInstrumento"
	,g."IdOrigenGarantia"
	,c."FechaVencimientoInstrumento"
	,g."EsFungible"
	,g."IdTipoCanalIngreso" AS "IdTipoCanalIngreso"
	,c."INGIdTipoOperacion" AS "IdTipoOperacion"
	,g."IdMonedaInstrumento" AS "IdMonedaInstrumento"
	,g."INGTasaGarantia" AS "INGTasaGarantia"
	,g."NemoSVS" AS "NemoSVS"

FROM "BSCL.Custodia.BD::GTA.Garantia" AS g
  INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" c 
    ON c."IdCliente" = g."IdCliente"
   AND c."IdPortafolio" = g."IdPortafolio"
   AND c."IdEmisor" = g."IdEmisor"
   AND c."IdEmpresa" = :IdEmpresa  
   AND c."FechaVencimientoOperacion" > '0001-01-01' 
   AND c."FechaVencimientoOperacion" = :Fecha
   AND c."IdEstado" = 'VIG'
   AND c."IdClasificacionTipoInstrumento" = 'RV'
   AND c."IdTipoInstrumento" = 'SIM'
   AND c."IdTipoOperacion" = 'VENSIM'
WHERE g."IdEstado" = 'VIG'
  AND g."IdConcepto" = 'IGRV';
  