SELECT
	SAR."IdSolicitudAporteRescate",
	SFO."IdSerieFondo" AS "IdSerieFondoOrigen",
	CASE WHEN FO."FondoExtranjero" = 'N' AND FO."IdMoneda" = CONM."Valor" THEN 'S' ELSE 'N' END AS "FondoOrigenNacionalDolar",
	CASE WHEN FO."FondoExtranjero" = 'S' AND FO."IdMoneda" = CONM."Valor" THEN 'S' ELSE 'N' END AS "FondoOrigenInternacionalDolar",
	SFD."IdSerieFondo" AS "IdSerieFondoDestino",
	CASE WHEN FD."FondoExtranjero" = 'N' AND FD."IdMoneda" = CONM."Valor" THEN 'S' ELSE 'N' END AS "FondoDestinoNacionalDolar",
	CASE WHEN FD."FondoExtranjero" = 'S' AND FD."IdMoneda" = CONM."Valor" THEN 'S' ELSE 'N' END AS "FondoDestinoInternacionalDolar"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS SAR
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SFO ON ( SAR."IdSerieFondo" = SFO."IdSerieFondo" AND SFO."IdEmpresa" = :IdEmpresa)
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SFD ON ( SAR."IdSerieFondoDestino" = SFD."IdSerieFondo" AND SFD."IdEmpresa" = :IdEmpresa)
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FO ON (FO."IdFondo" = SFO."IdFondo" AND FO."IdEmpresa" = :IdEmpresa)
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FD ON (FD."IdFondo" = SFD."IdFondo" AND FD."IdEmpresa" = :IdEmpresa)
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS CONM ON (CONM."IdEmpresa" = :IdEmpresa AND CONM."CodigoParametro" = 'PLA.MonedaAdicional')
WHERE SAR."IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate);