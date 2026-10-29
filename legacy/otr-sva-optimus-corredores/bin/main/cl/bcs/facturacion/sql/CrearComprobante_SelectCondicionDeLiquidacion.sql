SELECT
	"IdCondicionDeLiquidacion",
	"Codigo",
	"Nombre",
	TO_BIGINT("PlazoEnDias") AS "PlazoEnDias"
FROM
	"BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion"
WHERE
	"IdCondicionDeLiquidacion" = :p_IdCondicionDeLiquidacion;