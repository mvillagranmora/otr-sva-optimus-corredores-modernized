SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarCorredoraInstruccion";

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarCorredoraInstruccion" AS
SELECT 
	COR."IdCorredora",
	COR."CodigoInscripcionRegulador",
	COR."IdEstado",
	COR."NombreCorto",
	COD."IdBolsa",
	CAST(COD."CodigoCorredora" AS int) AS "CodigoCorredora",
	COD."CodigoCorredoraFix",
	PER."NombreCompleto",
	PER."IdPersona"
FROM "BSCL.Plataforma.BD::INS.Corredora" AS COR
LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" COD ON COR."IdCorredora" = COD."IdCorredora"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER on COR."IdPersona" = PER."IdPersona";

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarCorredoraInstruccion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;