SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora"; 
 GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" AS 
	SELECT 
		COR."IdCorredora",
		COR."CodigoInscripcionRegulador",
		COR."IdEstado",
		COR."NombreCorto",
		PER."NombreCompleto",
		PER."IdPersona",
		PER."TipoDocumentoIdentidad",
		PER."DocumentoIdentidad"
		
	FROM "BSCL.Plataforma.BD::INS.Corredora" AS COR
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER on COR."IdPersona" = PER."IdPersona";
   GO                   
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
