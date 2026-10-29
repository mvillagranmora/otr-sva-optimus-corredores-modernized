SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.WS.ConsultarInstrumento"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.WS.ConsultarInstrumento"  
AS
SELECT 
	INS."IdInstrumento",
	INS."Nemotecnico",
	EMI."NombreCompleto" as "NombreCompletoEmisor",
	INS."RazonSocial",
	CTINS."IdClasificacionTipoInstrumento",
	INS."IdTipoInstrumento",
	INS."IdEstado",
	INS."Rentabilidad7Dias", 
	INS."Rentabilidad30Dias",
	INS."Rentabilidad52Sem",
	INS."EstaInscrito",
	INS."EsAutorizado",
	INS."EsInstrumentoCalificado",
	INS."IdTipoReajuste" AS "MonedaDeReajuste",
	INS."IdMoneda" AS "MonedaDePago" 
FROM  "BSCL.Plataforma.BD::INS.Instrumento" AS INS 
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CTINS ON CTINS."IdClasificacionTipoInstrumento" = TINS."IdClasificacionTipoInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS EMI ON EMI."IdEmisor" = INS."IdEmisor"
WITH READ ONLY;
GO


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.WS.ConsultarInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO