SELECT
	EventoDetalle."IdEventoDetalle" AS "IdEventoDetalle"
	, EventoDetalle."IdEvento" AS "IdEvento"
	, EventoDetalle."NombreCampo" AS "NombreCampo"
	, EventoDetalle."Valor" AS "Valor"
FROM
	"BSCL.Plataforma.BD::AUT.EventoDetalle" EventoDetalle
WHERE
	EventoDetalle."IdEvento" IN (:EventoIds);
