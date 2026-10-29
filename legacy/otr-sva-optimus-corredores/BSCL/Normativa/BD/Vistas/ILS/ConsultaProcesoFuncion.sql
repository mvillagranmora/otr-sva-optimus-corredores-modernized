SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaProcesoFuncion";

CREATE VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaProcesoFuncion" 
AS

SELECT "IdProcesoFuncion" 
	, "Nombre"
	, "Nemo"
	, "Orden"
	, "Estado"
FROM "BSCL.Normativa.BD::ILS.ProcesoFuncion";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::ILS.ConsultaProcesoFuncion" TO BCS_DESARROLLADOR WITH GRANT OPTION;