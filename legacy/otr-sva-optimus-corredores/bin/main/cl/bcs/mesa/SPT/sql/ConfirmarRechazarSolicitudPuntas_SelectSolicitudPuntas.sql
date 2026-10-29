SELECT
	"IdSolicitudPuntas",
	"IdEmpresa",
	"FechaRegistro",
	"FechaVigencia",
	"IdOperacion",
	"IdTipoOperacion",
	"IdCliente",
	"TipoCambioCostoSolicitado",
	"TipoCambioCostoAprobado",
	"MontoMonedaPrincipal",
	"IdMonedaPrincipal",
	"IdUsuarioSolicitud",
	"IdUsuarioAutorizador",
	"ObservacionSolicitud",
	"IdEstado"
FROM "BSCL.Mesa.BD::SPT.SolicitudesPuntas"
WHERE "IdSolicitudPuntas" = :IdSolicitudPuntas