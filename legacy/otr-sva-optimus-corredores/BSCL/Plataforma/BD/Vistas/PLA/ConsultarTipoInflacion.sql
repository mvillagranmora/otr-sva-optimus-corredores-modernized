SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoInflacion"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoInflacion"  
AS
SELECT 
	"IdTipoInflacion",
	"Codigo",
	"Nombre"
FROM "BSCL.Plataforma.BD::PLA.TipoInflacion";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoInflacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
