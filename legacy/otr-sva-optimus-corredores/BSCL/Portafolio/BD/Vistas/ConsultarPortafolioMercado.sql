SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMercado";
GO

CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMercado" 
AS
	SELECT DISTINCT
		   Portafolio."IdPortafolio"
	     , Portafolio."IdCliente"
	     , Cartera."IdClasificacionTipoInstrumento" 
	FROM "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS Cartera
			ON Cartera."IdPortafolio" = Portafolio."IdPortafolio"
	WHERE Cartera."IdEstado" = 'VIG'
	WITH READ ONLY;
GO

GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMercado"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO