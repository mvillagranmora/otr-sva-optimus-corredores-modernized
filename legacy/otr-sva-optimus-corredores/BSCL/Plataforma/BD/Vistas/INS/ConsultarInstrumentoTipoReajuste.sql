SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste"
AS
SELECT 
	 inst."IdInstrumento"
	,inst."Nemotecnico"
	,inst."IdTipoInstrumento"
	,inst."FechaEmision"
	,tipoins."IdClasificacionTipoInstrumento"
	,	CASE  
			WHEN tipoins."IdClasificacionTipoInstrumento" = 'RF' THEN inst."IdTipoReajuste" 
			WHEN tipoins."IdClasificacionTipoInstrumento" = 'IF' THEN ITI."IdTipoReajuste"
			ELSE inst."IdTipoReajuste" 
		END AS "IdTipoReajuste"
	,TR."Nombre" AS "NombreTipoReajuste"
	,	CASE  
			WHEN tipoins."IdClasificacionTipoInstrumento" = 'RF' THEN inst."IdMoneda" 
			WHEN tipoins."IdClasificacionTipoInstrumento" = 'IF' THEN ITI."IdMoneda"
			ELSE inst."IdMoneda" 
		END AS "IdMoneda"
	, 	CASE  
			WHEN tipoins."IdClasificacionTipoInstrumento" = 'RF' THEN inst."CorteMinimo" 
			WHEN tipoins."IdClasificacionTipoInstrumento" = 'IF' THEN ITI."CorteMinimo"
			ELSE inst."CorteMinimo" 
		END AS "CorteMinimo"
	,TR."Codigo"
	,TR."Reajustable"
	,TR."IdEstado"
FROM "BSCL.Plataforma.BD::INS.Instrumento" inst 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoins ON tipoins."IdTipoInstrumento" = inst."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste" as ITI on ITI."IdInstrumento" = inst."IdInstrumento"
iNNER JOIN "BSCL.Plataforma.BD::PLA.TipoReajuste" as TR on (TR."IdTipoReajuste" = case when tipoins."IdClasificacionTipoInstrumento" = 'IF' then ITI."IdTipoReajuste" else inst."IdTipoReajuste" end)
WITH READ ONLY;
GO

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO