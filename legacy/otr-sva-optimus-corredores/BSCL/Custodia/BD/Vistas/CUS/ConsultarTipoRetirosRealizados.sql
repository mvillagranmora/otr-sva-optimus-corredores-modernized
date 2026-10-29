SET SCHEMA "BSCL";
DROP VIEW   "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoRetirosRealizados";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoRetirosRealizados" AS 
SELECT 
	 cast(0 as BIGINT) as "IdGenerarMovimientoCustodia"
	,cast(0 as BIGINT) as "IdMovimientoCarteraTransito"
	,cast(0 as BIGINT) as "IdCartera"
	,cast(0 as BIGINT) as "FolioCartera"
	,cast(0 as BIGINT) as "IdCarteraRelacionada"
	,cast(0 as BIGINT) as "FolioCarteraRelacionada"
	,cast(0 as BIGINT) as "IdSubyacente"
	,cast(0 as BIGINT) as "FolioSubyacente"
	,cast(0 as BIGINT) as "IdSubyacenteRelacionado"
	,cast(0 as BIGINT) as "FolioSubyacenteRelacionado"
	,cast(0 as BIGINT) as "IdGarantia"
	,cast(0 as BIGINT) as "FolioGarantia"
FROM dummy;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoRetirosRealizados"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;