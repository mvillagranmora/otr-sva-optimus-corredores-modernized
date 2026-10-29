UPDATE "BSCL.Mesa.BD::SPT.SolicitudesPuntas" 
SET	"IdEstado" = :p_IdEstado,
	"TipoCambioCostoAprobado" = :p_TipoCambioCostoAprobado, 
	"ObservacionAutorizador" = :p_ObservacionAutorizador,
	"IdUsuarioAutorizador" = :p_IdUsuarioAutorizador
WHERE "IdSolicitudPuntas" = :p_IdSolicitudPuntas