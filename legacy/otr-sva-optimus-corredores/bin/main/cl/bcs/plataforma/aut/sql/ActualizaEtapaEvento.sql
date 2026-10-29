UPDATE "BSCL.Plataforma.BD::AUT.EtapaEvento"
SET "IdEstado" = :IdEstado,
	"FechaHoraCambioEstado" = :FechaHoraCambioEstado,
	"IdUsuarioCambioEstado" = :IdUsuarioCambioEstado
WHERE "IdEtapaEvento" = :IdEtapaEvento;						