SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::MER.ConsultarTipoRegistroParidad"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::MER.ConsultarTipoRegistroParidad" AS
	SELECT 	
		"IdTipoRegistro",
		"Nombre" as "NombreTipoRegistro"
		
	FROM "BSCL.Plataforma.BD::MER.TipoRegistoParidad";
			
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::MER.ConsultarTipoRegistroParidad" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
