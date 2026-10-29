/********************************************************** 
Proposito	: Script busca la mesa x defecto para asignarla a ingresos a la cta.propia
autor		: ACC
fecha		: 05-09-2018
***********************************************************/

SELECT "IdMesa", "IdPortafolio"
FROM (
	SELECT
		COALESCE("IdMesa",0) AS "IdMesa",
		COALESCE("IdPortafolio",0) AS "IdPortafolio",
		RANK() OVER (PARTITION BY "IdCliente","IdPortafolio" ORDER BY "PorDefecto" DESC, "IdMesa" ASC) AS "Prioridad"
	FROM "BSCL.Mesa.BD::MES.Mesa"
	WHERE "IdEmpresa" = :IdEmpresa
	  AND "IdTipoProducto" = 'MERF'
	  AND "IdEstadoRegistro" = 'ACT'
	  AND (:filtroIdPortafolio = 0 OR "IdPortafolio" IN (:IdPortafolio))
)
ORDER BY "Prioridad" ASC;