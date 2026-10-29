UPDATE "BSCL.Plataforma.BD::AUT.AutorizadoEtapaEvento"
  SET
	"Observaciones" =  :Observaciones ,
	"FechaHoraCambioEstado" = :FechaHoraCambioEstado ,
	"IdRol" =  COALESCE(:IdRol, 0),
	"IdUsuario" = COALESCE(:IdUsuario, 0),
	"IdEstado" = :IdEstado
WHERE   "IdAutorizadoEtapaEvento" =  :IdAutorizadoEtapaEvento

