INSERT INTO "BSCL.Plataforma.BD::PLA.MensajeWebsocket"(
	"IdEmpresa", 
	"FechaRegistro",
	"Evento",
	"DATA",
	"EsProcesado" 
) VALUES (
	:IdEmpresa,
   CURRENT_UTCTIMESTAMP,
   'AVISO',
   'El modulo ' || :NombreModulo || ' ha sido cerrado por el usuario ' || UPPER(:CodigoUsuario),
   'N'
);