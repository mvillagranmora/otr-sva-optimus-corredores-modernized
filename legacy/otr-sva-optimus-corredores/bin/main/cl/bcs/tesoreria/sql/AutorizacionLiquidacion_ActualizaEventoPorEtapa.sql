UPDATE EVT 
SET EVT."IdEstado" =  'RZD',	  
EVT."IdUsuarioCambioEstado" 	= :IdUsuarioCambioEstado,
EVT."FechaHoraCambioEstado" 	= :FechaHoraCambioEstado                     
FROM "BSCL.Plataforma.BD::AUT.Evento" EVT
WHERE  EVT."IdEvento" = :IdEvento  AND EVT."IdEstado" IN ('VIG','AUT')