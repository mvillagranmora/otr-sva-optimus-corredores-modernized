UPDATE
	"BSCL.Custodia.BD::CUS.Cartera"
SET
	"IdEstado" = 'MOD',
	"FechaModificacionRegistro" = CURRENT_TIMESTAMP
WHERE
	"IdCartera" = :p_IdCartera
AND "IdEstado" = 'VIG';