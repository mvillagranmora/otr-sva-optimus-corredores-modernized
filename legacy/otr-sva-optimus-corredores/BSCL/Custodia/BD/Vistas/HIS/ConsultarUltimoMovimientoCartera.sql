SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::HIS.ConsultarUltimoMovimientoCartera";
GO
  
CREATE VIEW "BSCL.Custodia.BD.Vistas::HIS.ConsultarUltimoMovimientoCartera" AS

	SELECT 
			"IdEmpresa", 
			"FolioCartera", 
			"FechaRespaldo",
			MAX("IdCartera") as "IdCartera"
			
	FROM "BSCL.Custodia.BD::HIS.Cartera"
	GROUP BY "IdEmpresa","FolioCartera", "FechaRespaldo"
	WITH READ ONLY;
GO
	
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::HIS.ConsultarUltimoMovimientoCartera"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
