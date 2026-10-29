SET SCHEMA "BSCL";  
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoCarteraPorEstado";
  
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoCarteraPorEstado" AS

	SELECT "FolioCartera", "IdEstado", MAX("IdCartera") as "IdCartera"
	FROM "BSCL.Custodia.BD::CUS.Cartera" 
	GROUP BY "FolioCartera", "IdEstado";
	
	
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoCarteraPorEstado"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;