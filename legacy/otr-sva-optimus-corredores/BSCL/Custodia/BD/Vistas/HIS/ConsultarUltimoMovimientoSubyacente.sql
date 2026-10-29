SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::HIS.ConsultarUltimoMovimientoSubyacente";
GO
  
CREATE VIEW "BSCL.Custodia.BD.Vistas::HIS.ConsultarUltimoMovimientoSubyacente" AS

	SELECT "FolioCarteraRelacionada", "FechaRespaldo", MAX("IdSubyacente") as "IdSubyacente"
	FROM "BSCL.Custodia.BD::HIS.Subyacente" 
	where "IdEstado" <> 'ANU'
	GROUP BY "FolioCarteraRelacionada", "FechaRespaldo"
WITH READ ONLY;
GO

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::HIS.ConsultarUltimoMovimientoSubyacente"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
