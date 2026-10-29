SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarEstadoProcesoCargaSolicitudAporteRescate";
 
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarEstadoProcesoCargaSolicitudAporteRescate"
AS 
SELECT "ESTFM"."IdEstado",
	"ESTADO"."Nombre" as "NombreEstado"

FROM "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" "ESTFM"
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS "ESTADO" ON "ESTFM"."IdEstado" = "ESTADO"."IdEstado"
WHERE "ESTFM"."NombreEntidad" = UPPER('PROCESOCARGASOLICITUDAPORTERESCATE');
	
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarEstadoProcesoCargaSolicitudAporteRescate" TO  BCS_DESARROLLADOR WITH GRANT OPTION;