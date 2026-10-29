SELECT 
	"SecuenciaSolicitud",
	"IdPlanTipoInversion",
	"CodigoPortafolio",
	"DocumentoIdentidad",
	"IdPortafolio",
	"SaldoAcumulado",
	"SaldoAcumuladoCuentaInversion",
	"SaldoAcumuladoCustodia",
	"Sexo"
	FROM "BSCL.Normativa.BD::CIR.GenerarAnexo1Circular20Detalle"
	WHERE "SecuenciaSolicitud"= :SecuenciaSolicitud
	ORDER BY "IdPlanTipoInversion";