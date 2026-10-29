SELECT 
	"RutInstitucion",
	"FechaPosicion",
	"Concepto",
	"Moneda",
	"Paridad",
	"MontoMonedaPrincipal",
	"MontoDolar"
FROM "BSCL.Normativa.BD::TRI.SaldoDJ999" 
WHERE "Periodo" = :Periodo
AND "IdEmpresa" = :IdEmpresa;