
INSERT INTO "BSCL.Plataforma.BD::GBL.AgregarEvento_Evento"
(
	"IdEvento",
	"CodigoTipoEvento",
	"IdSecuencia",
	"FechaCreacionSistema",
	"IdPersonaRemitente",
	"IdPersonaDestinatario",
	"IdPortafolio"
)
VALUES
(
	:IdEvento,
	:CodigoTipoEvento,
	:IdSecuencia,
	:FechaCreacionSistema,
	:IdPersonaRemitente,
	:IdPersonaDestinatario,
	:IdPortafolio
);