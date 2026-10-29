SELECT
     	TO_VARCHAR(MONTH(OPE."FechaOperacion")) 		AS "MesOperacion",
		OPE."IdOperacion"				AS "NumeroOperacion",
		OPE."RazonSocial" 					AS "NombreCliente",
		OPE."DocumentoIdentidad" 			AS "RutCLiente",
		'0' 								AS "SecRutCLiente",
		OPE."MontoOperacionInicial" 		AS "MontoInicial",
		OPE."Tasa",
		OPE."PlazoEnDias" 					AS "Plazo",
		0.0 								AS "TotalInteres",
		OPE."MontoOperacionFinal" 			AS "MontoFinal",
		OPE."FechaOperacion"				AS "FechaInicio",
		OPE."FechaVencimiento"			AS "FechaFinal",
		OPE."FechaLiquidacion" AS "FechaLiquidacion",
		COALESCE(OPE."CostoFondo",0.0)					AS "CostoFondo",
		COALESCE(OPE."CostoFondo",0.0) 			AS "CostoFondoCDB",
		0.0 									AS "Utilidad",
		OPE."IdMoneda" 						AS "Moneda",
		OPE."MontoOperacionInicialMoneda"	AS "MontoMonedaOrigen",
		OPE."UtilidadPerdida"					AS "UtilidadPerdida",
		OPE."UtilidadFinal"						AS "UtilidadFinal"
FROM   "BSCL.Mesa.BD.Vistas::ConsultarOperaciones"   AS OPE 
where OPE."IdEstado"= 'VIG' AND OPE."FechaOperacion" BETWEEN :FechaDesde AND :FechaHasta;