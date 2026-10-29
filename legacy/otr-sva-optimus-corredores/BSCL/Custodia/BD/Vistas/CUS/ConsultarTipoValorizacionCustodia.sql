SET SCHEMA "BSCL";
DROP VIEW   "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoValorizacionCustodia";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoValorizacionCustodia" AS 
SELECT 
	 cast('' as NVARCHAR(1)) as "EsCartera"
	,cast('' as NVARCHAR(1)) as "EsSubyacente"
	,cast('' as NVARCHAR(1)) as "EsGarantia"
	,cast(0 as BIGINT) as "IdCartera"
	,cast(0 as BIGINT) as "FolioCartera"
	,cast(0 as BIGINT) as "FolioCarteraRelacionada"
	,cast (0 as BIGINT) as "IdCliente"
	,cast (0 as BIGINT) as "IdPortafolio"
	,cast(0 as BIGINT) as "IdInstrumento"
	,cast ('' as NVARCHAR(40)) as "Nemotecnico"
	,cast ('' as NVARCHAR(3)) as "IdMonedaInstrumento"
	,cast ('' as NVARCHAR(3)) as "IdTipoInstrumento"
	,cast ('' as NVARCHAR(6)) as "IdTipoOperacion"
	,cast ('' as NVARCHAR(3)) as "IdBoveda"
	,cast ('' as NVARCHAR(3)) as "IdBolsa"
	,cast (0 as BIGINT) as "IdMesa"
	,cast ('' as NVARCHAR(3)) as "IdClasificacionTipoInstrumento"
	,cast ('' as NVARCHAR(40)) as "IdEmisor"
	,cast ('' as NVARCHAR(3)) as "IdTipoReajuste"
	,cast ('' as NVARCHAR(40)) as "NemoSVS"
	,cast ('' as NVARCHAR(1)) as "EsFungible"
	,cast ('0001-01-01' as DATE) as "FechaEmisionInstrumento"
	,cast ('0001-01-01' as DATE) as "FechaVencimientoInstrumento"
	,cast (0 as DECIMAL(26,8)) as "Cantidad" 						
	,cast (0 as DECIMAL(26,8)) as "ValorRescate" 					
	,cast (0 as DECIMAL(26,8)) as "Precio"							
	,cast (0 as DECIMAL(26,8)) as "Tasa" 							
FROM dummy;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoValorizacionCustodia"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;