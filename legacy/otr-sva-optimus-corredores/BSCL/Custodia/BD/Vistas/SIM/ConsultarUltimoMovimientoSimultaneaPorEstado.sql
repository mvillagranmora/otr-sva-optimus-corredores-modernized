SET SCHEMA "BSCL";  
DROP VIEW "BSCL.Custodia.BD.Vistas::SIM.ConsultarUltimoMovimientoSimultaneaPorEstado";
  
CREATE VIEW "BSCL.Custodia.BD.Vistas::SIM.ConsultarUltimoMovimientoSimultaneaPorEstado" AS

	SELECT "FolioCartera", "IdEstado", MAX("IdCartera") as "IdCartera"
	FROM "BSCL.Custodia.BD::CUS.Cartera" 
	Where "IdTipoInstrumento"  IN ('SIM', 'RRV', 'RRF')
	GROUP BY "FolioCartera", "IdEstado"

WITH READ ONLY;
	
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::SIM.ConsultarUltimoMovimientoSimultaneaPorEstado"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
