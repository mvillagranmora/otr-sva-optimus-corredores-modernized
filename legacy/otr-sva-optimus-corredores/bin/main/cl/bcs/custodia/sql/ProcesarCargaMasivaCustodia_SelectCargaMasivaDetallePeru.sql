SELECT 
	CM."IdTipoCargaMasiva",
	CMD."IdCargaMasiva",
	CMD."IdCargaMasivaDetalle",
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
	CP."IdPortafolio",
	CMD."Fungible",
	CMD."Bolsa",
	CMD."CodigoPortafolio"
FROM "BSCL.Custodia.BD::CUS.CargaMasivaDetalle" 							AS CMD 
	INNER JOIN "BSCL.Custodia.BD::CUS.CargaMasiva"		 					AS CM
		ON (CMD."IdCargaMasiva" = CM."IdCargaMasiva")
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"			AS CC
		ON "DocumentoIdentidadAdicional" = CMD."DocumentoIdentidad"
	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"				AS CP
		ON CP."IdCliente" = CC."IdCliente"
	   AND CP."IdEstado" = 'ACT'
	   AND CP."CodigoPortafolio" = CMD."CodigoPortafolio"
	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" AS CCCP
		ON CCCP."IdPortafolio" = CP."IdPortafolio"
	   AND CCCP."Codigo" = CMD."CuentaCustodio"
	INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion" AS CL
		ON CMD."IdCondicionLiquidacion" = CL."IdCondicionDeLiquidacion"
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
										ELSE
											'INGTIT'
									END
WHERE CM."IdEmpresa" = :IdEmpresa 
  AND CMD."IdCargaMasiva" = :IdCargaMasiva
  AND CP."IdEstado" = 'ACT';
