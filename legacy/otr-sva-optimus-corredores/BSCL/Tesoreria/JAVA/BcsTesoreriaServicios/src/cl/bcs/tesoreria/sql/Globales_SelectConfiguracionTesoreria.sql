SELECT 
	"CodigoParametro", 
	COALESCE("Valor", "ValorDefecto", '') AS "Valor", 
	"Descripcion"
FROM 
	(
		SELECT "CodigoParametro", "Valor", "ValorDefecto", "Descripcion"
		FROM "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"
		WHERE "IdEmpresa" = :IdEmpresa
	)
WHERE "CodigoParametro" IN (:CodigoParametros);
