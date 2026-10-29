SELECT TO_DECIMAL(COALESCE("DerechosTasaPactosMenores8Dias",0.0),26,8)  AS "Monto"
FROM "BSCL.Mesa.BD::MRF.ObligacionesDerechosMonedaExtranjera"
WHERE "IdEmpresa" = :p_IdEmpresa
	AND "FechaContable" = TO_DATE(:p_FechaConsulta,'YYYY-MM-DD');