UPDATE "BSCL.Custodia.BD::CUS.Cartera"
SET "IdEstado"				= 'LIQ',
	"EGRFolioComprobante" = coalesce(:IdIngresoEgreso,:IdCargoAbono),
	"EGRIdTipoComprobante" = (CASE :IdTipoMovimiento WHEN  'MOVING' THEN  'TES'
														 WHEN  'MOVEGR' THEN  'TES'
	                                            ELSE 'CTA' END),
	"EGRIdMonedaLiquidacion"= :IdMonedaLiquidacion,
	"EGRMontoLiquidacion"	= (CASE WHEN "EGRMontoLiquidacion" < 0 THEN (:MontoOperacion*-1) 
									ELSE :MontoOperacion END),
	"FechaFixing"= :FechaFixing,
	"HoraFixing"= :HoraFixing
WHERE "IdCartera" = :IdCartera AND "FolioCartera" = :FolioCartera AND "IdOperacion" = :IdOperacion