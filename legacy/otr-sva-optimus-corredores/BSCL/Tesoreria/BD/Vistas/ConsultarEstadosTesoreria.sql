SET SCHEMA "BSCL";
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarEstadosTesoreria";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarEstadosTesoreria" AS 	 
SELECT
	"Estado"."IdEstado",
	"Estado"."Nombre" AS "NombreEstado",
	"EstadoTesoreria"."NombreEntidad"
FROM   "BSCL.Tesoreria.BD::TES.EstadoTesoreria" AS "EstadoTesoreria" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado" ON "Estado"."IdEstado" = "EstadoTesoreria"."IdEstado"
WITH READ ONLY;

  
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarEstadosTesoreria" TO  BCS_DESARROLLADOR WITH GRANT OPTION;