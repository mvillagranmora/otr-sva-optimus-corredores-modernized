SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarEstadoVigenciaOrden"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarEstadoVigenciaOrden"  
AS SELECT 
	"ESTORD"."IdEstado",
	"ESTADO"."Nombre" as "NombreEstado"

FROM "BSCL.Orden.BD::ORD.EstadoOrden" "ESTORD"
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "ESTADO" ON "ESTORD"."IdEstado" = "ESTADO"."IdEstado"
where upper("ESTORD"."NombreEntidad") = upper('Orden.Vigencia');
	
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarEstadoVigenciaOrden" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
