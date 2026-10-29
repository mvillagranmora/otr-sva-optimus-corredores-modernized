SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarEstadoFamiliaFondoMutuo"; 
 
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarEstadoFamiliaFondoMutuo"  
AS SELECT 
	"ESTFM"."IdEstado",
	"ESTADO"."Nombre" as "NombreEstado"

FROM "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" "ESTFM"
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS "ESTADO" ON "ESTFM"."IdEstado" = "ESTADO"."IdEstado"
WHERE "ESTFM"."NombreEntidad" = UPPER('FamiliaFondo');
	
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarEstadoFamiliaFondoMutuo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
