SELECT
	<<SELECT_CLAUSE>>
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
	INNER JOIN "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" as CMC
		ON CMC."IdTipoOperacion" = CAR."IdTipoOperacion"
			AND CMC."PoseeInstrumentoSubyacente" = 'N'
	INNER JOIN "BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso" as MZU ON
		<<BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso>>
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS
		ON INS."IdInstrumento" = CAR."IdInstrumento"
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPER
		ON TOPER."IdTipoOperacion" = CAR."IdTipoOperacion"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS TOTI
		ON TOTI."IdTipoOperacion" = MZU."UsoTipoOperacion"
			AND TOTI."IdTipoInstrumento" = CAR."IdTipoInstrumento"
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" RFM
		ON CAR."IdRegimenFondoMutuo" = RFM."IdRegimenFondoMutuo"
			AND RFM."IdEstado" = 'ACT'
WHERE CAR."IdEstado" = 'VIG'
	AND CAR."IdEmpresa" = :IdEmpresa
	AND CAR."INGFechaLiquidacion" <= :FechaMaxima
	AND MZU."IdUsoFlujoCustodia" IS NOT NULL
	AND MZU."IdTrabajo" = :IdTrabajo;

