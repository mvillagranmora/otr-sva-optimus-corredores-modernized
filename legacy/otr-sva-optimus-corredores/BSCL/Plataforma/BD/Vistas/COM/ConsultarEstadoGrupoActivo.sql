SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarEstadoGrupoActivo"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarEstadoGrupoActivo"  
AS
SELECT 
	E."IdEstado",
	E."Nombre"
FROM "BSCL.Plataforma.BD::PLA.Estado" as E
WHERE "IdEstado" = 'ACT' OR "IdEstado" = 'INA';	

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarEstadoGrupoActivo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
