SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMensaje"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMensaje" AS
SELECT
	"MSJ"."IdMensaje" AS "IdMensaje",
	"MSJ"."Titulo" AS "Titulo",
	"MSJ"."IdTipoMensaje"AS "IdTipoMensaje",
	"TIPO"."NombreTipoMensaje" AS "NombreTipoMensaje",
	"MSJ"."IdTipoEntidadDestinatario" AS "IdTipoEntidadDestinatario",
	"SOC"."NombreTipoSocioDeNegocio" AS "NombreTipoSocioDeNegocio",
	"MSJ"."MensajeCorto" AS "MensajeCorto",
	"MSJ"."MensajeLargo" AS "MensajeLargo",
	"MSJ"."FechaVigenciaDesde" AS "FechaVigenciaDesde",
	"MSJ"."FechaVigenciaHasta" AS "FechaVigenciaHasta",
	TO_NVARCHAR("MSJ"."FechaVigenciaDesde", 'YYYY-MM-DD') AS "FechaVigenciaDesdeString",
	TO_NVARCHAR("MSJ"."FechaVigenciaHasta", 'YYYY-MM-DD') AS "FechaVigenciaHastaString",
	"MSJ"."Habilitado" AS "Habilitado",
	"MSJ"."MuestraMensajeCorto" AS "MuestraMensajeCorto",
	"MSJ"."UrlAviso" AS "UrlAviso",
	"MSJ"."IdTipoCanal" AS "IdTipoCanal"
FROM "BSCL.Plataforma.BD::EVT.Mensaje" AS "MSJ"
INNER JOIN "BSCL.Plataforma.BD::EVT.TipoMensaje" AS "TIPO" ON "MSJ"."IdTipoMensaje" = "TIPO"."IdTipoMensaje"
INNER JOIN "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio" AS "SOC" ON "MSJ"."IdTipoEntidadDestinatario" = "SOC"."IdTipoSocioDeNegocio";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMensaje" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

 
