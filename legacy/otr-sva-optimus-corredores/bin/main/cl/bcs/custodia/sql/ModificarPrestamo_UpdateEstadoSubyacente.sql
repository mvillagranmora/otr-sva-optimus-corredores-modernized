UPDATE
	"BSCL.Custodia.BD::CUS.Subyacente"
SET
	"IdEstado" = 'MOD', 
	"FechaModificacionRegistro" = CURRENT_TIMESTAMP
WHERE
	"IdSubyacente" = :p_IdSubyacente
AND "IdEstado" = 'VIG';