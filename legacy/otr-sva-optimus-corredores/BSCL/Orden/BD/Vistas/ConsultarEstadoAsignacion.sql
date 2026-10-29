SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarEstadoAsignacion"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarEstadoAsignacion"  
AS SELECT 
	"ESTORD"."IdEstado",
	"ESTADO"."Nombre" as "NombreEstado"

FROM "BSCL.Orden.BD::ORD.EstadoOrden" "ESTORD"
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "ESTADO" ON "ESTORD"."IdEstado" = "ESTADO"."IdEstado"
WHERE "ESTORD"."NombreEntidad" = UPPER('Asignacion') AND "ESTORD"."IdEstado" != 'ANU';
	
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarEstadoAsignacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
