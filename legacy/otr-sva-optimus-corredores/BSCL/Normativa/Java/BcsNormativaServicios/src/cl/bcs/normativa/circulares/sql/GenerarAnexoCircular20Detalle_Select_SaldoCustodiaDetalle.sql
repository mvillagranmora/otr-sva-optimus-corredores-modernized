SELECT 
	"IdPlanTipoInversion",
	"CodigoPortafolio",
	"IdCliente",
	"DocumentoIdentidad",
	"IdPortafolio",
	"SaldoCustodia",
	"Nemotecnico"
FROM "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20Detalle_SaldoCustodiaDetalle"
WHERE "SecuenciaSolicitud" = :SecuenciaSolicitud;