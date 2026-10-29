SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::ConsultarCoberturaGarantia"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ConsultarCoberturaGarantia"  
AS

SELECT
	"Cobertura"."IdCoberturaGarantia",
    "Cobertura"."IdTipoInstrumentoExigeGarantia",
    CASE WHEN "TipoInstrumentoExigeGarantia"."Codigo"!='DER' THEN "TipoInstrumentoExigeGarantia"."Nombre" ELSE "TipoInstrumentoExigeGarantia"."Descripcion" END AS "NombreTipoInstrumentoExigeGarantia",
	"Cobertura"."IdInstrumentoExigeGarantia",
	CASE WHEN "Cobertura"."IdInstrumentoExigeGarantia" != 0 THEN "InstrumentoExigeGarantia"."Nemotecnico" ELSE "Concepto"."Nombre" END AS "NombreInstrumentoExigeGarantia",
	"Cobertura"."IdTipoInstrumentoComoGarantia",
    "TipoInstrumentoComoGarantia"."Nombre" AS "NombreTipoInstrumentoComoGarantia",
    "Cobertura"."IdTipoClasificacionRiesgo",
    "ClasificacionRiesgo"."ClasificacionRiesgo" AS "NombreClasificacionRiesgo",
	"Cobertura"."PlazoTope",
	"Cobertura"."UnidadPlazo",
	CASE "Cobertura"."UnidadPlazo"
		WHEN 'D' THEN 'Días'
		WHEN 'M' THEN 'Meses'
		WHEN 'A' THEN 'Años'
	END AS "NombreUnidadPlazo",	
	"Cobertura"."CoberturaEntidad",
	"Cobertura"."CoberturaCorredora",
	"Cobertura"."IdEstado",
	"Estado"."Nombre" AS "NombreEstado",
	"EstadoCustodia"."IdEstado" AS "IdEstadoGarantia",
	"ClasificacionRiesgo"."IdEntidadClasificadoraRiesgo" AS "IdEntidadClasificadoraRiesgo",
	"ClasificacionRiesgo"."IdClasificacionRiesgo" AS "IdClasificacionRiesgo",
	COALESCE("EntidadDestinoGarantia"."IdEntidadDestinoGarantia",0) AS "IdEntidadDestinoGarantia", -- Puede ser una Bolsa/Camara Compensación/etc
	COALESCE("EntidadDestinoGarantia"."IdContraparte",0) AS "IdContraparteDestinoGarantia",
	COALESCE("EntidadDestinoGarantia"."IdPersona",0) AS "IdPersonaDestinoGarantia",
	"EntidadDestinoGarantia"."NombreEntidad" AS "NombreEntidadDestinoGarantia"
	
FROM "BSCL.Plataforma.BD::GTA.CoberturaGarantia" AS "Cobertura"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumentoExigeGarantia" ON ("TipoInstrumentoExigeGarantia"."IdTipoInstrumento" = "Cobertura"."IdTipoInstrumentoExigeGarantia")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumentoComoGarantia" ON ("TipoInstrumentoComoGarantia"."IdTipoInstrumento" = "Cobertura"."IdTipoInstrumentoComoGarantia")
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "InstrumentoExigeGarantia"  ON ("InstrumentoExigeGarantia"."IdInstrumento" = "Cobertura"."IdInstrumentoExigeGarantia")
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" AS "ClasificacionRiesgo" ON("ClasificacionRiesgo"."ClasificacionRiesgo" = "Cobertura"."ClasificacionRiesgo"
AND "ClasificacionRiesgo"."IdEntidadClasificadoraRiesgo" = "Cobertura"."IdEntidadClasificadoraRiesgo"
AND "ClasificacionRiesgo"."IdTipoClasificacionRiesgo" = "Cobertura"."IdTipoClasificacionRiesgo")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado" ON ("Estado"."IdEstado" = "Cobertura"."IdEstado")
LEFT JOIN "BSCL.Custodia.BD::CUS.EstadoCustodia" AS "EstadoCustodia" ON ("Estado"."IdEstado" = "EstadoCustodia"."IdEstado" AND "EstadoCustodia"."NombreEntidad"='Garantia.Cobertura')
LEFT JOIN "BSCL.Custodia.BD.Vistas::GTA.ConsultarEntidadDestinoGarantia" AS "EntidadDestinoGarantia" ON ("EntidadDestinoGarantia"."IdEntidadDestinoGarantia" = "Cobertura"."IdEntidadDestinoGarantia")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS "Concepto" ON "Concepto"."IdConcepto"= "Cobertura"."IdConcepto";


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ConsultarCoberturaGarantia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
