SET SCHEMA "BSCL";
GO 

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarEntidadClasificadoraRiesgo";
GO 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarEntidadClasificadoraRiesgo" AS 

SELECT distinct 	ecr."IdEntidadClasificadoraRiesgo",ecr."Nombre" as "NombreEntidadClasificadoraRiesgo", tcr."IdClasificacionTipoInstrumento"	
FROM "BSCL.Plataforma.BD::INS.EntidadClasificadoraRiesgo" ecr
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" cr ON (ecr."IdEntidadClasificadoraRiesgo" = cr."IdEntidadClasificadoraRiesgo") 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo" tcr ON (tcr."IdTipoClasificacionRiesgo" = cr."IdTipoClasificacionRiesgo")
WHERE cr."IdEstado" = 'ACT';

GO                      
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarEntidadClasificadoraRiesgo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


