SELECT
	"IdCartera"
   ,"IdEstado"
FROM
	"BSCL.Custodia.BD::CUS.Cartera"
WHERE
	"IdCartera" = :p_IdCartera
AND "IdEstado" = 'VIG';