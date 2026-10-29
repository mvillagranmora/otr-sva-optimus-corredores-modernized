SELECT
	"IdSubyacente"
   ,"IdEstado"
FROM
	"BSCL.Custodia.BD::CUS.Subyacente"
WHERE
	"IdSubyacente" = :p_IdSubyacente
AND "IdEstado" = 'VIG';