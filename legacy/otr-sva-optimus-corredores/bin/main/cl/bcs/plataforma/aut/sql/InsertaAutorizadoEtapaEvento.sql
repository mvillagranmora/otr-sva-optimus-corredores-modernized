INSERT INTO "BSCL.Plataforma.BD::AUT.AutorizadoEtapaEvento"(
	"IdAutorizadoEtapaEvento",
	"IdEtapaEvento",
	"IdUsuario",
	"IdRol",
	"Observaciones",
	"FechaHoraCambioEstado",
	"IdEstado"
)
VALUES (
    "BSCL.Plataforma.BD.Secuencias::AUTSecuenciasGenerales".NEXTVAL,   
	:IdEtapaEvento,
	COALESCE(:IdUsuario, 0),
	COALESCE(:IdRol, 0),
	:Observaciones,
	:FechaHoraCambioEstado,
	:IdEstado
)
