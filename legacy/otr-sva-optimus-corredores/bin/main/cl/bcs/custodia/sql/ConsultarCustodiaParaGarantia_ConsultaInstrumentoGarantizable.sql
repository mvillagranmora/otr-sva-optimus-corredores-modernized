/********************************************************** 
Proposito	: Script para obtener informacion del instrumento
autor		: 
fecha		: Diciembre-2017
***********************************************************/

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
	ICR."IdEntidadClasificadoraRiesgo",
	ICR."ValorClasificacionRiesgo",	
	INS."TipoValorizacion",
	INS."Tasa",
	INS."Precio",
	INS."ValorizaPor",
	INS."TieneCupon",
	INS."NumeroCupones",
	INS."IndicadorBolson",
	INSTSUB."EsGarantizable",
	INS."Nemotecnico" AS "Instrumento"
FROM  "BSCL.Plataforma.BD::INS.Instrumento" AS INS 
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" AS TICR ON TICR."IdTipoInstrumento" = INS."IdTipoInstrumento" AND TICR."EntidadClasificadoraRiesgoPrincipal" = 'S'
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = INS."IdInstrumento" AND ICR."IdEntidadClasificadoraRiesgo" = TICR."IdEntidadClasificadoraRiesgo"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS TOTI ON TOTI."IdInstrumento" = INS."IdInstrumento" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS OPER ON OPER."IdTipoOperacion" = TOTI."IdTipoOperacion" 
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS EMI ON EMI."IdEmisor" = INS."IdEmisor"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CTINS ON CTINS."IdClasificacionTipoInstrumento" = TINS."IdClasificacionTipoInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.InstrumentoSubyacente" AS INSTSUB ON INSTSUB."IdInstrumento" = INS."IdInstrumento"

WHERE INS."IdInstrumento" IN (:IdInstrumento)
AND (:IdTipoInstrumento IS NULL OR INSTSUB."IdTipoInstrumento" = :IdTipoInstrumento)
AND (:IdBolsa IS NULL OR INSTSUB."IdBolsa" = :IdBolsa)
