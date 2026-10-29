SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarEstadoOrden";

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarEstadoOrden" AS
SELECT
	EMES."IdEstadoOrden",
	EMES."IdEstado",
	EMES."NombreEntidad",
	EST."Nombre"
FROM "BSCL.Orden.BD::ORD.EstadoOrden" EMES
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON EMES."IdEstado" = EST."IdEstado";
  
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarEstadoOrden" TO BCS_DESARROLLADOR WITH GRANT OPTION;
