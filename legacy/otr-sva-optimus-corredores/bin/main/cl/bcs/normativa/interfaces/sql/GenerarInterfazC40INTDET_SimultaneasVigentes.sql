SELECT 
	'Simultanea' AS "IdTipoOperacion" , 
	'PAGARE NR' AS "Nemotecnico" ,
	CAR."FolioCartera",
	'NR' AS "IdTipoReajuste",
	COALESCE(CAR."INGCantidad",0) AS "CantidadNominal",
	0 AS "CantidadTramiteCom",
	COALESCE (TO_VARCHAR(CAR."FechaVencimientoOperacion", 'YYYY-MM-DD'),'' )  AS "FechaVencimiento",
	COALESCE (TO_VARCHAR(CAR."FechaOperacion", 'YYYY-MM-DD'),'' )  AS "FechaOperacion",
	CAR."PlazoEnDiasOperacion" AS "PlazoEnDias",
    '' AS "Banda",
 	TO_DECIMAL(1) AS "Capital",
    CAR."INGMontoMercadoActualMonedaInstrumento", -- Validar si corresponde a valor presente (este valor se usa para calcular Interes Deve)
    TO_DECIMAL(CAR."IdOperacion",26,0) AS "IdOperacion",
    CAR."TasaCompra",
    CAR."INGMontoMercadoActualMonedaInstrumento" AS "ValorPresente",
    COALESCE(CAR."INGMontoInteresMercadoProximo"+ CAR."INGMontoReajusteMercadoProximo",0) as "ReajusteDev",
    COALESCE(CAR."INGMontoReajusteCorridoCompraActual",0) as "ReajusteProy",
	COALESCE(CAR."INGMontoInteresMercadoProximo",0) AS "InteresProy",
	COALESCE(CAR."INGMontoInteresCorridoMercadoActual",0) AS "InteresDeve"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
WHERE CAR."IdEmpresa"	= :IdEmpresa
  	  AND CAR."IdEstado" = 'VIG'
  	  AND CAR."IdTipoInstrumento" = 'SIM'
  	  AND CAR."FechaOperacion" <= :Fecha