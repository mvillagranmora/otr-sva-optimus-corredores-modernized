SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoBolsa"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoBolsa"  
AS
SELECT 
"Instrumento"."IdInstrumento" 
, "Instrumento"."Nemotecnico" 
, "Instrumento"."ISIN" 
, "Instrumento"."CorteMinimo" 
, "Instrumento"."EstaInscrito" 
, "Instrumento"."EsDivisible" 
, "Instrumento"."RazonSocial" 
, ifnull("Instrumento"."FechaEmision", cast('0001-01-01' as date)) as "FechaEmision"
, ifnull("Instrumento"."FechaVencimiento", cast('0001-01-01' as date)) as "FechaVencimiento"
, "Instrumento"."IdInstrumentoRelacionado" 
, "Instrumento"."IdEmisor" 
, "Instrumento"."IdTipoReajuste" 
, "Instrumento"."Reajustable" 
, "Instrumento"."IdMoneda" 
, "Instrumento"."IdEstado" 
, "Instrumento"."DecimalesPrecio" 
, "TipoInstrumento"."IdTipoInstrumento" AS "IdTipoInstrumento" 
, "TipoInstrumento"."Codigo" AS "CodigoTipoInstrumento" 
, "TipoInstrumento"."Nombre" AS "NombreTipoInstrumento" 
, "ClasificacionTipoInstrumento"."IdClasificacionTipoInstrumento" 
, "ClasificacionTipoInstrumento"."Nombre" AS "NombreClasificacionTipoInstrumento" 
, "ConsultarEmisor"."NombreCompleto" AS "NombreCompletoEmisor"
, "TipoOperacionTipoInstrumento"."IdTipoOperacion"
, "TipoOperacion"."Accion"
, "TipoOperacion"."Nombre" as "NombreTipoOperacion"
, "Instrumento"."Tamano"
, "BolsaTipoInstrumento"."ExigeGarantia" 
, "BolsaTipoInstrumento"."IdBolsa" 
,COALESCE("EntidadDestinoGarantia"."IdContraparte",0) AS "IdContraparteDestinoGarantia"		
,COALESCE("EntidadDestinoGarantia"."IdEntidadDestinoGarantia",0) AS "IdEntidadDestinoGarantia"

FROM  "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento" 
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS "ConsultarEmisor" ON  ( "ConsultarEmisor"."IdEmisor" = "Instrumento"."IdEmisor" )
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento" ON  ( "TipoInstrumento"."IdTipoInstrumento" = "Instrumento"."IdTipoInstrumento" )
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" AS "BolsaTipoInstrumento" ON  ("BolsaTipoInstrumento"."IdTipoInstrumento" = "TipoInstrumento"."IdTipoInstrumento" )
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS "ClasificacionTipoInstrumento" ON  ( "ClasificacionTipoInstrumento"."IdClasificacionTipoInstrumento" = "TipoInstrumento"."IdClasificacionTipoInstrumento" )
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS "TipoOperacionTipoInstrumento" ON  ( "Instrumento"."IdInstrumento" = "TipoOperacionTipoInstrumento"."IdInstrumento" )
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion" ON  ( "TipoOperacionTipoInstrumento"."IdTipoOperacion" = "TipoOperacion"."IdTipoOperacion" )
LEFT OUTER JOIN "BSCL.Custodia.BD::GTA.EntidadDestinoGarantia" AS "EntidadDestinoGarantia" ON ("EntidadDestinoGarantia"."IdEntidadDestinoGarantia" = "BolsaTipoInstrumento"."IdEntidadDestinoGarantia");
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoBolsa" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO