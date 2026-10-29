INSERT INTO "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20_SaldosCustodia"
( 
	"CodigoPortafolio",
	"FechaNacimiento",
	"IdCliente",
	"IdPlanTipoInversion",
	"IdPortafolio",
	"SaldoCuentaInversion",
	"SaldoCustodia",
	"SecuenciaSolicitud",
	"Sexo"
)
(
	Select 	A."CodigoPortafolio",
			A."FechaNacimiento",
			A."IdCliente",
			A."IdPlanTipoInversion",
			A."IdPortafolio",
			COALESCE(B."Saldo",0) AS "SaldoCuentaInversion",
			TO_DECIMAL(SUM(CASE WHEN A."IdMoneda" != 'CLP' THEN (A."CantidadCustodia" * A."PrecioMercadoCustodia" * :ValorDolar)
								ELSE (A."CantidadCustodia" * A."PrecioMercadoCustodia") END 
							),26,8) AS "SaldoCustodia",
			:SecuenciaSolicitud AS "SecuenciaSolicitud",
			A."Sexo"
	FROM "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20_SaldosCustodia_Paso" AS A
	LEFT JOIN "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20_SaldosCuentaInversion" AS B ON (B."CodigoPortafolio" IN ('APV-A','BF') AND 
																							A."IdCliente" = B."IdCliente" AND 
																							A."CodigoPortafolio" = B."CodigoPortafolio" AND
																							A."IdPortafolio" = B."IdPortafolio")
	WHERE
		A."CodigoPortafolio" IN ('APV-A','BF') AND
		A."SecuenciaSolicitud" = :SecuenciaSolicitud
	GROUP BY A."CodigoPortafolio",A."FechaNacimiento",A."IdCliente",A."IdPlanTipoInversion",A."IdPortafolio",B."Saldo",A."Sexo"
UNION
	Select 	A."CodigoPortafolio",
			A."FechaNacimiento",
			A."IdCliente",
			A."IdPlanTipoInversion",
			A."IdPortafolio",
			COALESCE(B."Saldo",0) AS "SaldoCuentaInversion",
			TO_DECIMAL(SUM(CASE WHEN A."IdMoneda" != 'CLP' THEN (A."CantidadCustodia" * A."PrecioMercadoCustodia" * :ValorDolar)
								ELSE (A."CantidadCustodia" * A."PrecioMercadoCustodia") END 
							),26,8) AS "SaldoCustodia",
			:SecuenciaSolicitud AS "SecuenciaSolicitud",
			A."Sexo"
	FROM "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20_SaldosCustodia_Paso" AS A
	LEFT JOIN "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20_SaldosCuentaInversion" AS B ON (B."CodigoPortafolio" IN ('APV-AN','APV-B') AND 
																							A."IdCliente" = B."IdCliente" AND 
																							A."CodigoPortafolio" = B."CodigoPortafolio" AND
																							A."IdPortafolio" = B."IdPortafolio")
	WHERE
		A."CodigoPortafolio" IN ('APV-AN','APV-B') AND
		A."SecuenciaSolicitud" = :SecuenciaSolicitud
	GROUP BY A."CodigoPortafolio",A."FechaNacimiento",A."IdCliente",A."IdPlanTipoInversion",A."IdPortafolio",B."Saldo",A."Sexo"
UNION
	Select 	A."CodigoPortafolio",
			A."FechaNacimiento",
			A."IdCliente",
			A."IdPlanTipoInversion",
			A."IdPortafolio",
			COALESCE(B."Saldo",0) AS "SaldoCuentaInversion",
			TO_DECIMAL(SUM(CASE WHEN A."IdMoneda" != 'CLP' THEN (A."CantidadCustodia" * A."PrecioMercadoCustodia" * :ValorDolar)
								ELSE (A."CantidadCustodia" * A."PrecioMercadoCustodia") END 
							),26,8) AS "SaldoCustodia",
			:SecuenciaSolicitud AS "SecuenciaSolicitud",
			A."Sexo"
	FROM "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20_SaldosCustodia_Paso" AS A
	LEFT JOIN "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20_SaldosCuentaInversion" AS B ON (B."CodigoPortafolio" IN ('DC-R','DC-DL','DC-AL') AND 
																							A."IdCliente" = B."IdCliente" AND 
																							A."CodigoPortafolio" = B."CodigoPortafolio" AND
																							A."IdPortafolio" = B."IdPortafolio")
	WHERE
		A."CodigoPortafolio" IN ('DC-R','DC-DL','DC-AL') AND
		A."SecuenciaSolicitud" = :SecuenciaSolicitud
	GROUP BY A."CodigoPortafolio",A."FechaNacimiento",A."IdCliente",A."IdPlanTipoInversion",A."IdPortafolio",B."Saldo",A."Sexo"
);