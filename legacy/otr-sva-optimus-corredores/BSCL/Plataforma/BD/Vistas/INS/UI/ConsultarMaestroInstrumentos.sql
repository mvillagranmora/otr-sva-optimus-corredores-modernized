--VISTA PARA EL MANTENEDOR DE INSTRUMENTOS, NO USAR EN SPS!! usar ConsultarInstrumento.sql

SET SCHEMA "BSCL"; 
GO

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.UI.ConsultarMaestroInstrumentos"; 
GO

CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.UI.ConsultarMaestroInstrumentos"  
AS
SELECT 
"Instrumento"."IdInstrumento",
"Instrumento"."Nemotecnico",
"Instrumento"."ISIN",
"Instrumento"."CorteMinimo",
"Instrumento"."EstaInscrito",
"Instrumento"."RazonSocial",
"Instrumento"."FechaEmision",
"Instrumento"."FechaVencimiento",
"Instrumento"."IdEmisor",
"Instrumento"."IdTipoReajuste",
"Instrumento"."IdMoneda",
"Instrumento"."IdEstado",
"TipoInstrumento"."IdTipoInstrumento" AS "IdTipoInstrumento",
"TipoInstrumento"."Codigo" AS "CodigoTipoInstrumento",
"TipoInstrumento"."Nombre" AS "NombreTipoInstrumento",
"ClasificacionTipoInstrumento"."IdClasificacionTipoInstrumento",
"ClasificacionTipoInstrumento"."Codigo"  AS "CodigoClasificacionTipoInstrumento",
"ClasificacionTipoInstrumento"."Nombre" AS "NombreClasificacionTipoInstrumento",
"ConsultarEmisor"."NombreCompleto",
"ConsultarEmisor"."DocumentoIdentidad",
"Instrumento"."Tamano",
ivcr."IdEntidadClasificadoraRiesgo",
ivcr."ValorClasificacionRiesgo",
"Instrumento"."Tasa",
"Instrumento"."Precio",
"Instrumento"."PrecioReferencia",
"Instrumento"."FechaReferencia",
"Instrumento"."NumeroCupones",
"Instrumento"."FrecuenciaCupon",
"Instrumento"."InstrumentoAcreditado",
"Instrumento"."TipoBonoIRF",
"Instrumento"."OrigenInstrumento",
"Instrumento"."TipoRenta",
"Instrumento"."FormadorMercado",
"Instrumento"."PactoAndino",
"Instrumento"."RelacionadoGrupo",
"Instrumento"."FactorExposicion",
"Instrumento"."LargoPlazo",
"InstrumentoRelacionado"."Nemotecnico" AS "NemotecnicoRelacionado",
COALESCE("Instrumento"."EsPutEsCall",'') AS "TipoOpcion",
"Instrumento"."PorcentajeCastigo",
"Instrumento"."FechaHastaAcreditado",
"Instrumento"."PrecioStrike",
"Instrumento"."AfectoArt107",
"Instrumento"."AfectoArt104",
"Instrumento"."DividendoOtraMoneda",
"Instrumento"."EsAutorizado",
"Instrumento"."CobroComisionesCustodia",
"Instrumento"."IdInstrumentoRaiz",
CASE 
 	WHEN "Instrumento"."IdInstrumentoRaiz" = 0 THEN ''
 		ELSE "InstrumentoRaiz"."Nemotecnico"
 	END  AS "NemotecnicoRaiz",
"Instrumento"."IdCondicionLiquidacion" AS "IdCondicionDeLiquidacion",
"Instrumento"."EsInstrumentoCalificado"
FROM  "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento" 
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS "ConsultarEmisor" ON "ConsultarEmisor"."IdEmisor" = "Instrumento"."IdEmisor"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento" ON "TipoInstrumento"."IdTipoInstrumento" = "Instrumento"."IdTipoInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS "ClasificacionTipoInstrumento" ON	"ClasificacionTipoInstrumento"."IdClasificacionTipoInstrumento" = "TipoInstrumento"."IdClasificacionTipoInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ivcr ON 
	ivcr."IdInstrumento" = "Instrumento"."IdInstrumento" /*AND
	ivcr."IdEntidadClasificadoraRiesgo" = "Instrumento"."IdEntidadClasificadoraRiesgo"*/
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "InstrumentoRelacionado" ON "InstrumentoRelacionado"."IdInstrumento" = "Instrumento"."IdInstrumentoRelacionado"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "InstrumentoRaiz"  ON "InstrumentoRaiz"."IdInstrumento" = "Instrumento"."IdInstrumentoRaiz"
WITH READ ONLY;
GO


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.UI.ConsultarMaestroInstrumentos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO

