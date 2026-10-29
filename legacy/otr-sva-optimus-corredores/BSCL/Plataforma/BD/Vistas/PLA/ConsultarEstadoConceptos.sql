SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoConceptos"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoConceptos"  
AS
SELECT 
	  "IdEstado",
	  "Nombre"
FROM "BSCL.Plataforma.BD::PLA.Estado"
WHERE "IdEstado" IN ('VIG','INA');
GO   

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoConceptos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
