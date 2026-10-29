SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarEstadoGrupoPortafolio"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarEstadoGrupoPortafolio"  
AS SELECT 
	"ESTORD"."IdEstado",
	"ESTADO"."Nombre" as "NombreEstado"

FROM "BSCL.Orden.BD::ORD.EstadoOrden" "ESTORD"
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "ESTADO" ON "ESTORD"."IdEstado" = "ESTADO"."IdEstado"
where "ESTORD"."NombreEntidad" = 'GRUPOPORTAFOLIO';

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarEstadoGrupoPortafolio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


