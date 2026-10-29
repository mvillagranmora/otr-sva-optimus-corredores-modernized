SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarEstadoTipoCliente"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarEstadoTipoCliente" AS
SELECT
	"IdEstado",	"Nombre"
FROM "BSCL.Plataforma.BD::PLA.Estado"
WHERE "IdEstado" IN  ('ACT','BLO');

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarEstadoTipoCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 