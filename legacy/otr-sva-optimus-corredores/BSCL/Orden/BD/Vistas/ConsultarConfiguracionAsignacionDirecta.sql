SET SCHEMA "BSCL"; 
GO

DROP VIEW "BSCL.Orden.BD.Vistas.ConsultarConfiguracionAsignacionDirecta"; 
GO

CREATE VIEW "BSCL.Orden.BD.Vistas.ConsultarConfiguracionAsignacionDirecta"
AS SELECT 
    CAD."IdTipoInstrumento", 
    TINS."Nombre" as "NombreTipoInstrumento", 
    CAD."IdEstado",
    CAD."AsignacionAuto",
	CAD."MovimientoFacturarAuto",
	CAD."ComprobanteAuto",
	CAD."IdEmpresa"
FROM "BSCL.Orden.BD::ORD.ConfiguracionAsignacionDirecta" as CAD 
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TINS 
ON (TINS."IdTipoInstrumento" = CAD."IdTipoInstrumento");

GO

GRANT SELECT ON "BSCL.Orden.BD.Vistas.ConsultarConfiguracionAsignacionDirecta" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO