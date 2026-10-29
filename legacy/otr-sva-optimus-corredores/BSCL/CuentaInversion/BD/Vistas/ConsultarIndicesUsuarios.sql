SET SCHEMA "BSCL"; 
GO

DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarIndicesUsuarios"; 
GO
CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarIndicesUsuarios"  
AS
	SELECT "IdIndiceFinanciero","Periodo","Nombre","Descripcion" 
	FROM "BSCL.Plataforma.BD::MER.DefinicionIndiceFinanciero"
    ORDER BY  "IdIndiceFinanciero";
GO
GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarIndicesUsuarios" TO  BCS_DESARROLLADOR WITH GRANT OPTION;