SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarClaseCobro"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarClaseCobro"  
AS

SELECT 
	"IdClaseCobro",
	"IdClaseCobroBase",
	"NombreClaseCobro"
	FROM "BSCL.Plataforma.BD::COM_2.ClaseCobro";


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarClaseCobro" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

