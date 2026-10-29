SELECT
	 MIN("IdPortafolio") AS "IdPortafolio" 
FROM
	"BSCL.Portafolio.BD::POR.Portafolio"
WHERE
	"IdCliente" = :p_IdCliente;