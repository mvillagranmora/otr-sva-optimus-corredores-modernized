SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventoEntidad"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventoEntidad"  
AS

SELECT 
evtper."IdConfiguracionEvento",
CASE 
	WHEN (evtper."IdEstado" IS NULL) THEN 'ACT'
	ELSE evtper."IdEstado"  
END as "EstadoConfiguracionPortafolio",
evtper."IdEntidad",
evtper."TipoEntidad",
CASE 
	WHEN (evtper."SuspensionTemporal" IS NULL) THEN 'N'
	ELSE   evtper."SuspensionTemporal"
END as "SuspensionTemporal",
CASE 
	WHEN (evtper."FechaInicioSuspension" IS NULL) THEN '0001-01-01'
	ELSE   evtper."FechaInicioSuspension"
END as "FechaInicioSuspension",
CASE 
	WHEN (evtper."FechaTerminoSuspension" IS NULL) THEN '0001-01-01'
	ELSE   evtper."FechaTerminoSuspension"
END as "FechaTerminoSuspension"
FROM "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" evtper;
 

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventoEntidad" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

 
