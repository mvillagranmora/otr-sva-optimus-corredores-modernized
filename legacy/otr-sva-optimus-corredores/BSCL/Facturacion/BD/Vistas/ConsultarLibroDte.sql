SET SCHEMA "BSCL"; 
GO

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarLibroDte";
GO

CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarLibroDte" As 
SELECT
        LibDte."IdLibro",
        LibDte."Tipo", 
        LibDte."PeriodoContable", 
        LibDte."DocumentoIdentidadEmpresa", 
        LibDte."IdEstado", 
        Est."Nombre" AS "Estado",
        LibDte."FechaIngresoRegistro"        
FROM "BSCL.Facturacion.BD::FAC.LibroDte" As LibDte
  INNER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS Estfac
    ON Estfac."IdEstado" = LibDte."IdEstado"
  INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS Est
    ON Est."IdEstado" = Estfac."IdEstado"
WHERE (Estfac."NombreEntidad" = 'FAC.LibroDte');     
GO

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarLibroDte" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
