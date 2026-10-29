SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarTipoEstado";

CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarTipoEstado" AS
SELECT
	 TES."IdTipoEstado"
	,TES."Codigo"
	,TES."Nombre"  		
	,TES."IdEstado"
FROM "BSCL.Mesa.BD::MES.TipoEstado" TES;

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarTipoEstado" TO BCS_DESARROLLADOR WITH GRANT OPTION;