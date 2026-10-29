SET SCHEMA "BSCL";  
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente";
  
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" AS

	SELECT "FolioCarteraRelacionada", MAX("IdSubyacente") as "IdSubyacente"
	FROM "BSCL.Custodia.BD::CUS.Subyacente" 
	where "IdEstado" <> 'ANU'
	GROUP BY "FolioCarteraRelacionada";
	
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;