SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoCartera";
GO 
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoCartera" AS

	SELECT 	"IdEmpresa", 
			"FolioCartera", 
			MAX("IdCartera") as "IdCartera"		
    FROM "BSCL.Custodia.BD::CUS.Cartera"
	GROUP BY "IdEmpresa","FolioCartera"
	WITH READ ONLY;
GO	
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoCartera"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO