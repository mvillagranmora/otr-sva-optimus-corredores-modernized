SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMensajesVigentes";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMensajesVigentes" AS
SELECT 
	"MSJ"."IdMensaje",
	"MSJ"."IdTipoMensaje",
	"MSJ"."NombreTipoMensaje",
	"MSJ"."IdTipoEntidadDestinatario", 
	"MSJ"."NombreTipoSocioDeNegocio",
	"MSJ"."Titulo", 
	"MSJ"."MensajeCorto",
	"MSJ"."MensajeLargo",
	"MSJ"."FechaVigenciaDesde", 
	"MSJ"."FechaVigenciaDesdeString", 
	"MSJ"."FechaVigenciaHasta",
	"MSJ"."FechaVigenciaHastaString",
	"MSJ"."Habilitado",
	"MSJ"."MuestraMensajeCorto",
	"MSJ"."UrlAviso",
	"MSJ"."IdTipoCanal"
FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMensaje" AS "MSJ" 
WHERE "MSJ"."Habilitado" = 'S'
AND CURRENT_DATE BETWEEN "MSJ"."FechaVigenciaDesde" AND "MSJ"."FechaVigenciaHasta";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMensajesVigentes" TO  BCS_DESARROLLADOR WITH GRANT OPTION;