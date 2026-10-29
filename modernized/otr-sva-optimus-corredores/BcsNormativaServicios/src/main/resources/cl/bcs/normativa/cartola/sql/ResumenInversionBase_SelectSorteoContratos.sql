SELECT
	CLI."IdCliente",
	CLI."IdPortafolio",
	SUM(SOR."PerdidaGanancia") AS "PerdidaGanancia"
FROM 
	"BSCL.Custodia.BD.Vistas::DER.ConsultaSorteoContratos"				SOR
	INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio"	CLI ON (CLI."IdPortafolio" = SOR."IdPortafolio")
WHERE 
	SOR."Fecha" = :p_Fecha AND CLI."IdPortafolio" = :p_IdPortafolio
GROUP BY CLI."IdCliente",CLI."IdPortafolio";