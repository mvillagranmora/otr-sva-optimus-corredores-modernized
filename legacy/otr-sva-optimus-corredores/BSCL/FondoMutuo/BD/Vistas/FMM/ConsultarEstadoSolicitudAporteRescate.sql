SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarEstadoSolicitudAporteRescate"; 
 
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarEstadoSolicitudAporteRescate"  
AS SELECT 
	"ESTFM"."IdEstado",
	"ESTADO"."Nombre" as "NombreEstado"

FROM "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" "ESTFM"
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS "ESTADO" ON "ESTFM"."IdEstado" = "ESTADO"."IdEstado"
WHERE "ESTFM"."NombreEntidad" = UPPER('SolicitudAporteRescate');
	
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarEstadoSolicitudAporteRescate" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
