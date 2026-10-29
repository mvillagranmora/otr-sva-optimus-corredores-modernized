SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarEstadoCorredorCorresponsal";
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarEstadoCorredorCorresponsal"
AS SELECT 
	e1."IdEstado",
	e2."Nombre" as "NombreEstado"
FROM "BSCL.Orden.BD::ORD.EstadoOrden" e1
left JOIN "BSCL.Plataforma.BD::PLA.Estado" e2 on e1."IdEstado" = e2."IdEstado" 
WHERE e1."NombreEntidad" = 'CORREDORCORRESPONSAL';

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarEstadoCorredorCorresponsal" TO  BCS_DESARROLLADOR WITH GRANT OPTION;