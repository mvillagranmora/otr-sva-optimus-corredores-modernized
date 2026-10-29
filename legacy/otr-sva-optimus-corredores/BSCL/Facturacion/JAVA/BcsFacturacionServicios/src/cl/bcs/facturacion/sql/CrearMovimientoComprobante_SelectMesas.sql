SELECT
	"Prioridad",
	"IdCliente", 
	"IdPortafolio", 
	"IdMesa",
	"PorDefecto"
FROM (
	SELECT
		RANK() OVER (PARTITION BY "IdCliente","IdPortafolio" ORDER BY "PorDefecto" DESC, "IdMesa" ASC) AS "Prioridad",
		COALESCE("IdCliente",0) AS "IdCliente",
		COALESCE("IdPortafolio",0) AS "IdPortafolio",
		COALESCE("IdMesa",0) AS "IdMesa",
		"PorDefecto"
	FROM "BSCL.Mesa.BD::MES.Mesa"
	WHERE "IdEmpresa" = :idEmpresa
	  AND "IdTipoProducto" = 'MERF'
	  AND "IdEstadoRegistro" = 'ACT'
	  AND (:filtroIdPortafolio = 0 OR "IdPortafolio" IN (:IdPortafolio))
)
ORDER BY "Prioridad" ASC;