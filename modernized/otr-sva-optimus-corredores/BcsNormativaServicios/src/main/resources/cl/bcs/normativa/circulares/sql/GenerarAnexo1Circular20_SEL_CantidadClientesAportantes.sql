(
	SELECT to_bigint(COUNT (DISTINCT A."IdPortafolio")) AS "CantidadCuentas", to_bigint(COUNT (DISTINCT A."IdCliente")) AS "CantidadClientes" ,
	'APV-A' AS "Codigo",to_bigint(A."IdPlanTipoInversion") AS "IdPlanTipoInversion",A."Sexo"
	FROM "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20_SaldosCustodia" AS A
	WHERE
	A."CodigoPortafolio" IN ('APV-A')
	GROUP BY A."IdPlanTipoInversion",A."Sexo"
	
UNION

	SELECT to_bigint(COUNT (DISTINCT A."IdPortafolio")) AS "CantidadCuentas", to_bigint(COUNT (DISTINCT A."IdCliente")) AS "CantidadClientes" ,
	'TOTAL' AS "Codigo",to_bigint(A."IdPlanTipoInversion") AS "IdPlanTipoInversion",A."Sexo"
	FROM "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20_SaldosCustodia" AS A
	WHERE
	A."CodigoPortafolio" IN ('APV-A','APV-AN','APV-B')
	GROUP BY A."IdPlanTipoInversion",A."Sexo"
	
UNION

	SELECT to_bigint(COUNT (DISTINCT A."IdPortafolio")) AS "CantidadCuentas", to_bigint(COUNT (DISTINCT A."IdCliente")) AS "CantidadClientes" ,
	'DC' AS "Codigo",to_bigint(A."IdPlanTipoInversion") AS "IdPlanTipoInversion",A."Sexo"
	FROM "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20_SaldosCustodia" AS A
	WHERE
	A."CodigoPortafolio" IN ('DC-R','DC-DL','DC-AL')
	GROUP BY A."IdPlanTipoInversion",A."Sexo"
);