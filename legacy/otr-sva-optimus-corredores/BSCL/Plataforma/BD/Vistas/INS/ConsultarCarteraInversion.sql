SET SCHEMA "BSCL"; 
GO

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarCarteraInversion"; 
GO

CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarCarteraInversion"  
AS
SELECT 
	ci."NombreCarteraInversion",
	ci."IdEmpresa",
	ci."Descripcion",
	ci."IdEstado"
FROM  "BSCL.Plataforma.BD::INS.CarteraInversion" ci;
GO

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarCarteraInversion" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
