SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarTipoRangoCobro"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarTipoRangoCobro"  
AS

SELECT 
	"IdTipoRangoCobro","NombreTipoRangoCobro"
	FROM "BSCL.Plataforma.BD::COM_2.TipoRangoCobro";


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarTipoRangoCobro" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
