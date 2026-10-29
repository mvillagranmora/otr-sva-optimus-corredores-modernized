UPDATE EVT 
SET EVT."IdEstado"                  = :IdEstado,
	EVT."IdUsuarioCambioEstado" 	= :IdUsuarioCambioEstado,
	EVT."FechaHoraCambioEstado" 	= :FechaHoraCambioEstado                     
FROM "BSCL.Plataforma.BD::AUT.Evento" EVT
WHERE  "IdEvento" = :IdEvento
