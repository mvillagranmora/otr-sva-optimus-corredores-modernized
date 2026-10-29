SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarEstadoLibroOrdenes"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarEstadoLibroOrdenes"  
AS SELECT 
	"ESTORD"."IdEstado",
	"ESTADO"."Nombre" as "NombreEstado"

FROM "BSCL.Orden.BD::ORD.EstadoOrden" "ESTORD"
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "ESTADO" ON "ESTORD"."IdEstado" = "ESTADO"."IdEstado"
WHERE "ESTORD"."NombreEntidad" = UPPER('Libro.Ordenes');
	
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarEstadoLibroOrdenes" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
