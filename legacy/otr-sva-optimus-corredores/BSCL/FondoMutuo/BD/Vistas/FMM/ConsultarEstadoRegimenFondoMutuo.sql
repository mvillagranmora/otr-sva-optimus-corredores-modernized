SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarEstadoRegimenFondoMutuo"; 
 
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarEstadoRegimenFondoMutuo"  
AS SELECT 
	"ESTFM"."IdEstado",
	"ESTADO"."Nombre" as "NombreEstado"
FROM "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" "ESTFM"
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS "ESTADO" ON "ESTFM"."IdEstado" = "ESTADO"."IdEstado"
WHERE "ESTFM"."NombreEntidad" = UPPER('REGIMEN');
	
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarEstadoRegimenFondoMutuo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
