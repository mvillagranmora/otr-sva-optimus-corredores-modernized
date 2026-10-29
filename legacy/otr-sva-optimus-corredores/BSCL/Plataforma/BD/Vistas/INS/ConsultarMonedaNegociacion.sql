SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarMonedaNegociacion"; 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarMonedaNegociacion" AS
SELECT
	DISTINCT insRaiz."IdInstrumento" AS "IdInstrumento",
	insRaiz."IdTipoInstrumento" AS "IdTipoInstrumento",
	insRaiz."Nemotecnico" AS "NemotecnicoRaiz",
	insNeg."IdMoneda" AS "IdMoneda"
FROM
	"BSCL.Plataforma.BD::INS.Instrumento" insRaiz
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" insNeg ON
	( insNeg."IdInstrumentoRaiz" = insRaiz."IdInstrumento"
		OR insNeg."IdInstrumento" = insRaiz."IdInstrumento" )
	AND insNeg."IdEstado" = 'ACT'
WHERE
	insRaiz."IdEstado" = 'ACT'
	AND (insRaiz."IdInstrumentoRaiz" = 0
		OR insRaiz."IdInstrumentoRaiz" IS NULL);

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarMonedaNegociacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;   