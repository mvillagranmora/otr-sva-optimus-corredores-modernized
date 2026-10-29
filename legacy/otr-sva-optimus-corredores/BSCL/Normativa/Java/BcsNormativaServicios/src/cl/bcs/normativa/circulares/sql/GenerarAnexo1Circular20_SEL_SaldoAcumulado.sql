(
	SELECT to_bigint(A."IdPlanTipoInversion") AS "IdPlanTipoInversion",A."Sexo",A."CodigoPortafolio" AS "Codigo",
	SUM("SaldoCustodia"+"SaldoCuentaInversion") AS "SaldoAcumulado"
	FROM "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20_SaldosCustodia" AS A
	WHERE
	A."CodigoPortafolio" IN ('APV-A','BF')
	GROUP BY A."IdPlanTipoInversion",A."Sexo",A."CodigoPortafolio"
	
UNION

	SELECT to_bigint(A."IdPlanTipoInversion") AS "IdPlanTipoInversion",A."Sexo",'APV-B' AS "Codigo",
	SUM("SaldoCustodia"+"SaldoCuentaInversion") AS "SaldoAcumulado"
	FROM "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20_SaldosCustodia" AS A
	WHERE
	A."CodigoPortafolio" IN ('APV-AN','APV-B')
	GROUP BY A."IdPlanTipoInversion",A."Sexo"
	
UNION

	SELECT to_bigint(A."IdPlanTipoInversion") AS "IdPlanTipoInversion",A."Sexo",'DC' AS "Codigo",
	SUM("SaldoCustodia"+"SaldoCuentaInversion") AS "SaldoAcumulado"
	FROM "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20_SaldosCustodia" AS A
	WHERE
	A."CodigoPortafolio" IN ('DC-R','DC-DL','DC-AL')
	GROUP BY A."IdPlanTipoInversion",A."Sexo"
);