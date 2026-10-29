SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClaseCliente"; 
GO
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClaseCliente" AS
SELECT
	"TIPCLI"."IdClaseCliente",
	"TIPCLI"."Nombre",
	"TIPCLI"."IdUsuarioCreador",
	"TIPCLI"."FechaCreacion",
	"TIPCLI"."FechaModificacion",
	"TIPCLI"."IdEmpresa",
	"TIPCLI"."IdEstado"
FROM "BSCL.SocioNegocio.BD::CLI.ClaseCliente" AS "TIPCLI";
GO
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClaseCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 