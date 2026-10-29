SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"  
AS
SELECT 
	INS."IdInstrumento",
	INS."Nemotecnico",
	INS."ISIN",
	INS."CorteMinimo",
	INS."EstaInscrito",
	INS."EsDivisible",
	INS."RazonSocial",
	INS."FechaEmision",
	INS."FechaVencimiento",
	INS."IdInstrumentoRelacionado",
	INS."IdEmisor",
	INS."IdTipoReajuste",
	INS."Reajustable",
	INS."IdMoneda",
	INS."IdEstado",
	INS."DecimalesPrecio",
	TINS."IdTipoInstrumento" AS "IdTipoInstrumento",
	TINS."Codigo" AS "CodigoTipoInstrumento",
	TINS."Nombre" AS "NombreTipoInstrumento",
	CTINS."IdClasificacionTipoInstrumento",
	CTINS."Codigo"  AS "CodigoClasificacionTipoInstrumento",
	CTINS."Nombre" AS "NombreClasificacionTipoInstrumento",
	EMI."DocumentoIdentidad" AS "DocumentoIdentidadEmisor", --RutEmisor
	EMI."NombreCompleto" as "NombreCompletoEmisor", --RazonSocialEmisor
	TOTI."IdTipoOperacion",
	OPER."Accion",
	OPER."Nombre" as "NombreTipoOperacion",
	INS."Tamano",
	TICR."IdTipoClasificacionRiesgo",
	ICR."IdEntidadClasificadoraRiesgo",
	ICR."ValorClasificacionRiesgo",	
	INS."TipoValorizacion",
	INS."Tasa",
	INS."Precio",
	INS."ValorizaPor",
	INS."TieneCupon",
	INS."NumeroCupones",
	INS."PrecioStrike",
	INS."IndicadorBolson",
	TiPre."IdTipoPrestamo",
	INS."IdMonedaEmision",
	INS."EsPutEsCall",
	INS."ModalidadLiquidacion",	
	INS."ModalidadEjercicio",
	INS."EstiloOpcion",
	INS."AfectoArt107",
	INS."AfectoArt104",
	INS."EsAutorizado",
	INS."CobroComisionesCustodia",
	INS."IdInstrumentoRaiz",
	INS."EsInstrumentoCalificado",
	CASE 
		WHEN TINS."IdTipoInstrumento" <> 'SIM' AND COUNT(INS."Nemotecnico") OVER(PARTITION BY INS."Nemotecnico") > 1 THEN INS."Nemotecnico" || ' - ' || TINS."IdClasificacionTipoInstrumento"
		ELSE INS."Nemotecnico"
	END AS "NemotecnicoUI"
FROM  "BSCL.Plataforma.BD::INS.Instrumento" AS INS 
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" AS TICR ON TICR."IdTipoInstrumento" = INS."IdTipoInstrumento" AND TICR."EntidadClasificadoraRiesgoPrincipal" = 'S'
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = INS."IdInstrumento" AND ICR."IdEntidadClasificadoraRiesgo" = TICR."IdEntidadClasificadoraRiesgo"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS TOTI ON TOTI."IdInstrumento" = INS."IdInstrumento" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS OPER ON OPER."IdTipoOperacion" = TOTI."IdTipoOperacion" 
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS EMI ON EMI."IdEmisor" = INS."IdEmisor"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CTINS ON CTINS."IdClasificacionTipoInstrumento" = TINS."IdClasificacionTipoInstrumento"
LEFT OUTER JOIN "BSCL.Custodia.BD::PRT.TipoPrestamo" AS TiPre ON TiPre."IdInstrumentoPrestamo" = INS."IdInstrumento"  
WITH READ ONLY;
GO


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
