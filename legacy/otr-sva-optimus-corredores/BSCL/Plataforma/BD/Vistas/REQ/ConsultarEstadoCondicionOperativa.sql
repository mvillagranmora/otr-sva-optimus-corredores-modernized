SET SCHEMA "BSCL";
DROP VIEW "BSCL.Plataforma.BD.Vistas::REQ.ConsultarEstadoCondicionOperativa";
CREATE VIEW "BSCL.Plataforma.BD.Vistas::REQ.ConsultarEstadoCondicionOperativa" AS
	
	SELECT
		ECO."IdEstadoCondicionOperativa",
 		ECO."IdEstado",
		EST."Nombre"
	FROM "BSCL.Plataforma.BD::REQ.EstadoCondicionOperativa" as ECO
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as EST ON(EST."IdEstado" = ECO."IdEstado");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REQ.ConsultarEstadoCondicionOperativa" TO BCS_DESARROLLADOR WITH GRANT OPTION;