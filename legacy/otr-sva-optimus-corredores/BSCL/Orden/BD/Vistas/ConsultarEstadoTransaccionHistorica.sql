SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarEstadoTransaccionHistorica"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarEstadoTransaccionHistorica"  
AS SELECT 
	"ESTORD"."IdEstado",
	"ESTADO"."Nombre" as "NombreEstado"

FROM "BSCL.Orden.BD::ORD.EstadoOrden" "ESTORD"
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "ESTADO" ON "ESTORD"."IdEstado" = "ESTADO"."IdEstado"
WHERE "ESTORD"."NombreEntidad" = UPPER('TransaccionHistorica');
	
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarEstadoTransaccionHistorica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
