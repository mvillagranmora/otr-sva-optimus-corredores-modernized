SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::MER.ConsultarDeterioroEmisor";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::MER.ConsultarDeterioroEmisor" AS
SELECT 
	FLG."IdDeterioroEmisor",
	FLG."Emisor",
	FLG."FlagDeterioro",
	FLG."PorcentajeDeterioro",
	FLG."Fase"
FROM "BSCL.Plataforma.BD::MER.DeterioroEmisor" FLG
WHERE FLG."IdEstado" = 'ACT';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::MER.ConsultarDeterioroEmisor" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
