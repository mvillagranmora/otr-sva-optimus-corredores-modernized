
INSERT INTO  "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20Detalle"
( 
	"SecuenciaSolicitud",
	"IdPlanTipoInversion",
	"CodigoPortafolio",
	"DocumentoIdentidad",
	"IdPortafolio",
	"SaldoAcumulado",
	"SaldoAcumuladoCuentaInversion",
	"SaldoAcumuladoCustodia", 
	"Sexo"
) SELECT 
    :SecuenciaSolicitud,
	"IdPlanTipoInversion",
	"CodigoPortafolio",
	"DocumentoIdentidad",
	"IdPortafolio",
	"SaldoAcumulado",
	"SaldoAcumuladoCuentaInversion",
	"SaldoAcumuladoCustodia",
	"Sexo"
FROM (SELECT  to_bigint(A."IdPlanTipoInversion") AS "IdPlanTipoInversion", A."CodigoPortafolio", B."DocumentoIdentidad", B."IdPortafolio", A."Sexo"
			,SUM("SaldoCustodia"+"SaldoCuentaInversion") AS "SaldoAcumulado"
			,SUM("SaldoCuentaInversion") AS "SaldoAcumuladoCuentaInversion"
			,SUM("SaldoCustodia") AS "SaldoAcumuladoCustodia"
			FROM "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20_SaldosCustodia" A
			LEFT JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarPortafolio" B ON A."IdPortafolio" = B."IdPortafolio"
			WHERE
			A."CodigoPortafolio" IN ('APV-A','BF')
			GROUP BY A."IdPlanTipoInversion", A."CodigoPortafolio", B."DocumentoIdentidad", B."IdPortafolio", A."Sexo"
			
		UNION 
		
		SELECT  to_bigint(A."IdPlanTipoInversion") AS "IdPlanTipoInversion", A."CodigoPortafolio", B."DocumentoIdentidad", B."IdPortafolio", A."Sexo"
			,SUM("SaldoCustodia"+"SaldoCuentaInversion") AS "SaldoAcumulado"
			,SUM("SaldoCuentaInversion") AS "SaldoAcumuladoCuentaInversion"
			,SUM("SaldoCustodia") AS "SaldoAcumuladoCustodia"
			FROM "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20_SaldosCustodia" A
			LEFT JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarPortafolio" B ON A."IdPortafolio" = B."IdPortafolio"
			WHERE
			A."CodigoPortafolio" IN ('APV-AN','APV-B')
			GROUP BY A."IdPlanTipoInversion", A."CodigoPortafolio", B."DocumentoIdentidad", B."IdPortafolio", A."Sexo"
		UNION 
		
		SELECT  to_bigint(A."IdPlanTipoInversion") AS "IdPlanTipoInversion", A."CodigoPortafolio", B."DocumentoIdentidad", B."IdPortafolio", A."Sexo"
			,SUM("SaldoCustodia"+"SaldoCuentaInversion") AS "SaldoAcumulado"
			,SUM("SaldoCuentaInversion") AS "SaldoAcumuladoCuentaInversion"
			,SUM("SaldoCustodia") AS "SaldoAcumuladoCustodia"
			FROM "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20_SaldosCustodia" A
			LEFT JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarPortafolio" B ON A."IdPortafolio" = B."IdPortafolio"
			WHERE
			A."CodigoPortafolio" IN ('DC-R','DC-DL','DC-AL')
			GROUP BY A."IdPlanTipoInversion", A."CodigoPortafolio", B."DocumentoIdentidad", B."IdPortafolio", A."Sexo"
		);

