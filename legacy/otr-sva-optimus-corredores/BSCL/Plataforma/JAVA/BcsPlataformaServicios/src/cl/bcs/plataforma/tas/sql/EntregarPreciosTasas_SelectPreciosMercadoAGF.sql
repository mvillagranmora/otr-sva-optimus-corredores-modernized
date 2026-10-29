SELECT "IdInstrumento","ValorCuota","FechaEncontrada","Fecha" FROM (
	SELECT ff."IdInstrumento",ff."Fecha",VH."IdSerieFondo",MAX(VH."FechaCierre" ) AS "FechaEncontrada"	
	FROM  "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_IN_InstrumentoFecha" ff
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("SerieFondo"."IdInstrumento" = ff."IdInstrumento")
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico" AS VH ON ( "SerieFondo"."IdSerieFondo" = VH."IdSerieFondo" AND VH."EsUltimaCargaDiaria" = 'S' AND VH."FechaCierre" <= ff."Fecha")
	WHERE "Correlativo" = :Correlativo
	GROUP BY ff."IdInstrumento",ff."Fecha",VH."IdSerieFondo"
) AS V1
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico" V2 ON V2."FechaCierre" = V1."FechaEncontrada" and V1."IdSerieFondo" = V2."IdSerieFondo"	AND V2."EsUltimaCargaDiaria" = 'S'
WHERE V2."ValorCuota" IS NOT NULL;