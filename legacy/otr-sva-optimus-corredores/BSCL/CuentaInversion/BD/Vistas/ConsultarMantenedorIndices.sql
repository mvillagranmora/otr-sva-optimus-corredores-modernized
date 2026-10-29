SET SCHEMA "BSCL"; 
GO

DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarMantenedorIndices"; 
GO
CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarMantenedorIndices"  
AS
	SELECT "IdIndiceFinanciero", "Periodo", "Nombre", "Descripcion", "IdMonedaParametro", "IdMonedaValor"
	FROM "BSCL.Plataforma.BD::MER.DefinicionIndiceFinanciero";

GO
GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarMantenedorIndices" TO  BCS_DESARROLLADOR WITH GRANT OPTION;