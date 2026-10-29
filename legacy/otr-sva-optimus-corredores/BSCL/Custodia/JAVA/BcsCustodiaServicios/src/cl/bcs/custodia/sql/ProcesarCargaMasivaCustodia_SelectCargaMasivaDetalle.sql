SELECT
	CM."IdTipoCargaMasiva",
	CMD."IdCargaMasiva",
	CMD."IdCargaMasivaDetalle",
	TCM."Codigo" AS "TipoCargaMasiva",
	CMD."DocumentoIdentidad",
	CMD."IdCondicionLiquidacion",
	CAST(COALESCE(CL."PlazoEnDias",0) AS BIGINT) AS "PlazoEnDias", --NULL AS "PlazoEnDias",--CL."PlazoEnDias",
	CMD."IdBoveda",
	CMD."CuentaCustodio",
	CMD."IdConcepto",
	CMD."Nemotecnico",
	CMD."Emisor",
	CMD."NemoGarantizado",
	CMD."IdTipoReajuste",
	CAST(CMD."FechaVencimiento" AS VARCHAR(10)) AS "FechaVencimiento",
	CMD."Moneda",
	CMD."Cantidad",
	CMD."Precio",
	CMD."Tasa",
	CMD."Monto",
	CMD."ValorRescate",
	CMD."Glosa",
	CP."IdCliente",
--	CP."IdPersona",
	CP."IdPortafolio",
	CMD."Fungible",
	CMD."Bolsa",
	CMD."CodigoPortafolio",
	CMD."FolioCartera",
	CMD."TipoEvento",
	CMD."NumeroDividendo",
	CAST(CMD."FechaLimite" AS VARCHAR(10)) AS "FechaLimite",
	CAST(CMD."FechaPago" AS VARCHAR(10)) AS "FechaPago",
	CMD."Descripcion",
	CMD."Factor",
	CMD."Cuotas",
	CP."IdEstado" AS "IdEstadoPortafolio"
FROM "BSCL.Custodia.BD::CUS.CargaMasivaDetalle" 							AS CMD 
	INNER JOIN "BSCL.Custodia.BD::CUS.CargaMasiva"		 					AS CM
		ON (CMD."IdCargaMasiva" = CM."IdCargaMasiva")
	LEFT JOIN "BSCL.Custodia.BD::CUS.TipoCargaMasiva" 						AS TCM
		ON (CM."IdTipoCargaMasiva" = TCM."IdTipoCargaMasiva")
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" 	AS CP 
		ON (CP."DocumentoIdentidad" = CMD."DocumentoIdentidad" 
		AND CP."CodigoPortafolio" = CMD."CodigoPortafolio")
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion" AS CL
		ON (CMD."IdCondicionLiquidacion" = CL."IdCondicionDeLiquidacion" OR CMD."IdCondicionLiquidacion" = CL."Codigo")
	   AND CL."IdTipoOperacion" = 	CASE 
										WHEN CM."IdTipoCargaMasiva" = 1
											THEN 'INGTIT'
										WHEN CM."IdTipoCargaMasiva" = 2
											THEN 'INGTIT'
										WHEN CM."IdTipoCargaMasiva" = 3
											THEN 'INGTIT'
										WHEN CM."IdTipoCargaMasiva" = 4
											THEN 'RETTIT'
										WHEN CM."IdTipoCargaMasiva" = 5
											THEN 'RETTIT'
										WHEN CM."IdTipoCargaMasiva" = 6
											THEN 'RETTIT'
										WHEN CM."IdTipoCargaMasiva" = 7
											THEN 'COMSIM'
										WHEN CM."IdTipoCargaMasiva" = 8
											THEN 'COMSIM'
										WHEN CM."IdTipoCargaMasiva" = 9
											THEN 'VENSIM'
										WHEN CM."IdTipoCargaMasiva" = 10
											THEN 'VENSIM'
										WHEN CM."IdTipoCargaMasiva" = 11
											THEN 'COMSIM'
										WHEN CM."IdTipoCargaMasiva" = 12
											THEN 'VENSIM'
										--Inicio GCMBISEGCU-1 Carga de registros Custodia Externa 
										WHEN CM."IdTipoCargaMasiva" = 13
											THEN 'INGTIT'
										WHEN CM."IdTipoCargaMasiva" = 14
											THEN 'RETTIT'
										--Fin GCMBISEGCU-1
										ELSE
											'INGTIT'
									END
WHERE CM."IdEmpresa" = :IdEmpresa 
  AND CMD."IdCargaMasiva" = :IdCargaMasiva;
