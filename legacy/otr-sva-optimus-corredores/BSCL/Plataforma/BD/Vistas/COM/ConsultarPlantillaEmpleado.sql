SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarPlantillaEmpleado"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarPlantillaEmpleado"
AS

SELECT 
	PEMP.*
FROM
	"BSCL.Plataforma.BD::COM.PlantillaEmpleado" as PEMP;
	
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarPlantillaEmpleado" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
