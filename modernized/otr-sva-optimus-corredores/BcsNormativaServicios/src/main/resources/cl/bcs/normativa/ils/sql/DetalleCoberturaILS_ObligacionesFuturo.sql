SELECT  
	''																AS "Titulo"
	, 'Obligaciones por Op. Futuro < 7 dias (Compromiso Compra)' 	AS "Categoria"
	, 0.0																AS "MontoGarantia"
	, 0.0																AS "MontoBancosyEmpresas"
	, 0.0																AS "FactorA"
	, 0.0																AS "FactorB"	
	,TO_DECIMAL(COALESCE("ObligacionesTasaPactosMenores8Dias",0.0),26,8) AS "MontoCobertura"
	, 0.0																AS "MontoEntidadFinanciera"
	,:FechaContable												AS "Fecha" 
	,TO_BIGINT(:IdEmpresa)													AS "IdEmpresa"
FROM "BSCL.Mesa.BD::MRF.ObligacionesDerechosMonedaExtranjera"
WHERE "FechaContable" = TO_DATE(:FechaContable,'YYYY-MM-DD') AND "IdEmpresa" = :IdEmpresa