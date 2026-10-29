SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoCliente"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoCliente" AS
SELECT
	"TIPCLI"."IdTipoCliente",
	"TIPCLI"."Nombre",
	"TIPCLI"."IdUsuarioCreador",
	"TIPCLI"."FechaCreacion",
	"TIPCLI"."FechaModificacion",
	"TIPCLI"."IdEmpresa",
	"TIPCLI"."IdEstado",
	"TIPCLI"."TipoEnrolamiento",
	"TIPCLI"."PorDefecto",
	"ESTADO"."Nombre" AS "NombreEstado"
FROM "BSCL.SocioNegocio.BD::CLI.TipoCliente" AS "TIPCLI"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "ESTADO" ON "TIPCLI"."IdEstado" = "ESTADO"."IdEstado"
WHERE "TIPCLI"."IdEstado" = 'ACT';

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 