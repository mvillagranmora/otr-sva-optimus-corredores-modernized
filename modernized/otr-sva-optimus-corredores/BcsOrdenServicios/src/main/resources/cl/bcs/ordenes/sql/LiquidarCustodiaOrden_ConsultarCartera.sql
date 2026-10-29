
SELECT 
	CAR."IdCartera",
	CAR."IdTipoOperacion",
	CAR."IdBolsa",
	CAR."IdBoveda",
	CAR."IdMonedaInstrumento",
	CAR."IdCuentaCustodioPortafolio",
	CAR."IdEmisor",
	MOVTRA."EsNominal",
	CAR."INGCantidad",
    CAR."INGPrecioCompra",
    CAR."INGTasaCompra",
    CAR."PlazoEnDiasOperacion",
    CAR."TasaCompra", 
    CAR."PrecioCompra",
    CAR."PrecioAPlazo"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOVTRA ON (MOVTRA."IdMovimientoCarteraEnTransito" = CAR."IdMovimientoCarteraEnTransito")
WHERE CAR."IdCartera" IN (:IdCarteras)
AND   CAR."IdEstado" = 'VIG';