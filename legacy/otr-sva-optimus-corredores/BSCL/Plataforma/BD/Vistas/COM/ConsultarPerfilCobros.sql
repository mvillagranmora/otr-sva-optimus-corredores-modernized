SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilCobros"; 
 		
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilCobros"  
AS

SELECT 
	PC."IdPerfilCobros",
	PC."DescripcionPerfilCobros",
	PC."IdPerfilCobros" || ' - ' || PC."DescripcionPerfilCobros" AS "PerfilCobros",
	PC."IdEmpresa",
	PC."IdEstado" as "EstadoPerfilCobros",
	ESTPERF."Nombre" as "NombreEstadoPerfilCobros",
	PC."EsPerfilPersonalizado"
FROM 
	"BSCL.Plataforma.BD::COM.PerfilCobros" as PC
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as ESTPERF
		ON (ESTPERF."IdEstado" = PC."IdEstado");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilCobros" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
