SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarEstadoCliente"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarEstadoCliente" AS
SELECT estSoc."IdEstado",estPla."Nombre"
	
FROM "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" estSoc
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" estPla on estSoc."IdEstado" = estPla."IdEstado"
WHERE "NombreEntidad" = 'CLIENTE';
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarEstadoCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
