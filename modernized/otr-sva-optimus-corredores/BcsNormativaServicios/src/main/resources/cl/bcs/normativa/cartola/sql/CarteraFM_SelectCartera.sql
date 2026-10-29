select 
		CAR."IdCliente" 				as "IdCliente",
		CAR."IdPortafolio" 				as "IdPortafolio",
		'' 								as "alert",
		CAR."Nemotecnico" 				as "nemo",
		FON."IdEmisor" 					as "emisor",
		SER."CodigoSerie" 				as "serie",
		SER."NombreSerieFondo"			as "RAZON_SOCIAL",
		SUM(CAR."INGCantidad")			as "CAN_CUSTODIA",
		TO_DECIMAL(0,26,8)				as "garantia",
		IFNULL(CAR."INGPrecioPromedioCompra",TO_DECIMAL(0,26,8)) as "valorCuotaAporte",
		IFNULL(VAL."ValorCuota",TO_DECIMAL(0,26,8))	as "valorCuotaActual",
		''								as "COD_IMP_RES",
		SER."Diferido"					as "IND_DIFERIDO",
		CAR."Nemotecnico" 				as "NEMO_ASOC",
		TO_DECIMAL(0,26,8)				as "TASA_CAMBIO",
		TO_DECIMAL(0,26,8)			 	as "BLOQUEO_FONDO",
		TO_DECIMAL(0,26,8)			 	as "TIR_CONTAB",
		COALESCE(FAM."NombreFamilia",'') as "TIPO_FAM",
		''		 						as "categorizacion",
		SUM(ROUND(CAR."INGMontoATasaMercadoActual")) AS "MONTO_VALORIZ",
		ifnull(VAL."ValorCuota",TO_DECIMAL(0,26,8))	as "valorActual",
		TO_DECIMAL(0,26,8)				as "RENTABILIDAD",
		CAR."IdInstrumento"			 	as "ID_INSTRUMENTO", --GCMBIMECAR-1 Mejoramiento de Cartolas
		CAR."IdMonedaInstrumento"		as "ID_MONEDA_INSTRUMENTO", --GCMBIMECAR-1 Mejoramiento de Cartolas
		CAR."IdBoveda"
	FROM 
		"BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos" CAR
		INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SER ON CAR."IdInstrumento" = SER."IdInstrumento"
		LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.ValorCuota" VAL ON VAL."IdSerieFondo" = SER."IdSerieFondo"
		INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" FON ON SER."IdFondo" = FON."IdFondo"
		LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.FamiliaFondo" AS FAM ON (SER."IdFamiliaFondo" = FAM."IdFamiliaFondo")
	WHERE CAR."IdPortafolio" IN (:IdPortafolio) AND CAR."IdEstado" = 'VIG' 
	GROUP BY 
	CAR."IdCliente"
	, CAR."IdPortafolio"
	, CAR."Nemotecnico"
	, FON."IdEmisor"
	, SER."CodigoSerie"
	, SER."NombreSerieFondo"
	, CAR."INGPrecioPromedioCompra"
	, VAL."ValorCuota"
	, SER."Diferido"
	, FAM."NombreFamilia"
	, CAR."IdInstrumento"
	, CAR."IdMonedaInstrumento"
	, CAR."IdBoveda"