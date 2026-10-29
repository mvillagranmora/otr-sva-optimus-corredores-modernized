SET SCHEMA "BSCL"; 
GO

DROP VIEW "BSCL.Orden.BD.Vistas.ConsultarConfiguracionShortName"; 
GO
CREATE VIEW "BSCL.Orden.BD.Vistas.ConsultarConfiguracionShortName" AS
SELECT
    CSN."ShortName",
    CSN."IdTipoInstrumento",
	TINS."Nombre" as "NombreTipoInstrumento",
	CSN."AsignacionAuto",
	CSN."MovimientoFacturarAuto",
	CSN."ComprobanteAuto"
FROM "BSCL.Orden.BD::ORD.ConfiguracionShortName" AS CSN
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = CSN."IdTipoInstrumento";
GO

GRANT SELECT ON "BSCL.Orden.BD.Vistas.ConsultarConfiguracionShortName" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO