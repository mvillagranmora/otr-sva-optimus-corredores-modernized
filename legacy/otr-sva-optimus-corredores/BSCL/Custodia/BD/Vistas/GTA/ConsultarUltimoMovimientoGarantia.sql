SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarUltimoMovimientoGarantia";

CREATE VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarUltimoMovimientoGarantia" 
AS 

	SELECT 
		 "IdEmpresa"
		,"FolioGarantia"
--		,"IdEstado"
--		,"EGRCantidad"
		,MAX("IdGarantia") as "IdGarantia"
	FROM "BSCL.Custodia.BD::GTA.Garantia" 
	GROUP BY "IdEmpresa","FolioGarantia"

WITH READ ONLY;
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::GTA.ConsultarUltimoMovimientoGarantia"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
