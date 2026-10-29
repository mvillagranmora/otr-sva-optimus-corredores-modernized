INSERT INTO "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20Detalle_SaldoCustodiaDetalle"
( 
	"SecuenciaSolicitud",
	"IdPlanTipoInversion",
	"CodigoPortafolio",
	"IdCliente",
	"DocumentoIdentidad",
	"IdPortafolio",
	"SaldoCustodia",
	"Nemotecnico"
)(
Select 	    :SecuenciaSolicitud AS "SecuenciaSolicitud",
			A."IdPlanTipoInversion",
			A."CodigoPortafolio",
			A."IdCliente",
			DocumentoIdentidad."DocumentoIdentidad",
			A."IdPortafolio",
			(CASE WHEN A."IdMoneda" != 'CLP' THEN (A."CantidadCustodia" * A."PrecioMercadoCustodia" * :ValorDolar)
								ELSE (A."CantidadCustodia" * A."PrecioMercadoCustodia") END 
							) AS "SaldoCustodia",
			A."Nemotecnico"
	FROM "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20_SaldosCustodia_Paso" AS A
	LEFT JOIN  "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" =  A."IdCliente"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad ON DocumentoIdentidad."IdPersona" = CLI."IdPersona" AND DocumentoIdentidad."Predeterminado" = 'S'
	WHERE
		A."CodigoPortafolio" IN ('APV-A','BF','APV-AN','APV-B','DC-R','DC-DL','DC-AL') AND
		A."SecuenciaSolicitud" = :SecuenciaSolicitud
)