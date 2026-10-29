SELECT
	"IdParametro",
	"ValorParametro"
FROM "BSCL.Mesa.BD::MES.Parametro"
WHERE "NombreTipoParametro" = :NombreTipoParametro AND "ValorParametro" = :ValorParametro AND "IdEmpresa" = :IdEmpresa AND "IdEstado" = 'VIG';