SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraCodigos"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraCodigos" AS 
	SELECT 
		COR."IdCorredora",
		COR."CodigoInscripcionRegulador",
		COR."IdEstado",
		COR."NombreCorto",
		COD."IdBolsa",
		COD."CodigoCorredora",
		COD."CodigoCorredoraFix",
		PER."NombreCompleto",
		PER."IdPersona"
		
	FROM "BSCL.Plataforma.BD::INS.Corredora" AS COR
	LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" COD ON COR."IdCorredora" = COD."IdCorredora"
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER on COR."IdPersona" = PER."IdPersona";
                      
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
