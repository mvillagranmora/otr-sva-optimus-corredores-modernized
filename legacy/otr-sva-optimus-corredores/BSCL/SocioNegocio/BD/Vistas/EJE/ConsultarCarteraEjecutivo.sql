SET SCHEMA "BSCL"; 
GO

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarCarteraEjecutivo"; 
GO

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarCarteraEjecutivo"  
AS
SELECT 
	ci."NombreCarteraEjecutivo",
	ci."IdEmpresa",
	ci."Descripcion",
	ci."IdEstado"
FROM  "BSCL.SocioNegocio.BD::EJE.CarteraEjecutivo" ci;
GO

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarCarteraEjecutivo" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
