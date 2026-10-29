SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarBaseTasaCobro"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarBaseTasaCobro"  
AS

SELECT 
	"IdBaseTasaCobro","NombreBaseTasaCobro","ValorBaseTasaCobro"
	FROM "BSCL.Plataforma.BD::COM_2.BaseTasaCobro";


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarBaseTasaCobro" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
