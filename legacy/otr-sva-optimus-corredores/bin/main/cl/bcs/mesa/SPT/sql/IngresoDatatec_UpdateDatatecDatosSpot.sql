UPDATE DAT
SET
	 DAT."IdEstadoRegistro" = :IdEstadoRegistro
	,DAT."GlosaProceso" = :GlosaProceso
	,DAT."IdOperacionSpot" = :IdOperacionSpot
	,DAT."IdMonedaLocal" = COALESCE(SPT."IdMonedaLocal",'')
	,DAT."IdFormaPagoRecibo" = COALESCE(FPR."IdFormaDePago",'')
	,DAT."FechaValutaRecibo" = COALESCE(FPR."FechaLiquidacionComprobantePagoOperacion",'')
	,DAT."IdFormaPagoEntrego" = COALESCE(FPE."IdFormaDePago",'')
	,DAT."FechaValutaEntrego" = COALESCE(FPE."FechaLiquidacionComprobantePagoOperacion",'')
FROM "BSCL.Mesa.BD::SPT.OperacionSpotDatatec" DAT
LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
	ON (SPT."IdOperacionSpot" IN (:IdOperacionSpot) AND
		DAT."IdEmpresa" = SPT."IdEmpresa")
LEFT JOIN "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" FPR
	ON (SPT."IdOperacionSpot" = FPR."IdOperacion" AND
		FPR."IdEmpresa" = SPT."IdEmpresa" AND
		FPR."RecibePaga" = 'R' AND
		FPR."IdTipoProducto" = 'MEME')
LEFT JOIN "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" FPE
	ON (SPT."IdOperacionSpot" = FPE."IdOperacion" AND
		FPE."IdEmpresa" = SPT."IdEmpresa" AND
		FPE."RecibePaga" = 'P' AND
		FPE."IdTipoProducto" = 'MEME')
WHERE DAT."IdUnicoDatatec" IN (:IdUnicoDatatec)
  --AND "IdEstadoRegistro" = 'LEI'
;