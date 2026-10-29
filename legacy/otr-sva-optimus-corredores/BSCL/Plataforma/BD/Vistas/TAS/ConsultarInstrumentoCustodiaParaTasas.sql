SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarInstrumentoCustodiaParaTasas";
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarInstrumentoCustodiaParaTasas" AS

SELECT
	 DISTINCT(CASE WHEN COALESCE(TAS."Nemotecnico",'') = '' THEN INS."Nemotecnico" ELSE TAS."Nemotecnico" END) AS "Nemotecnico"
	,TAS."NemotecnicoAux"
	,CASE WHEN COALESCE(TAS."IdEmisor",'') = '' THEN INS."IdEmisor" ELSE TAS."IdEmisor" END AS "IdEmisor"
	,TAS."IdClasificacionTipoInstrumento"
	,TAS."IdTipoInstrumento"
	--,TAS."IdMonedaCustodia"
	--,INS."IdMoneda" AS "IdMonedaInstrumento"
	,CASE WHEN COALESCE(TAS."IdMonedaCustodia",'') = '' THEN COALESCE(INS."IdMoneda",'') ELSE TAS."IdMonedaCustodia" END AS "IdMoneda"
	,TAS."IdEmpresa"
	,EMP."IdPais"
	,EMP."IdSocioDeNegocio"
	,SOC."IdCliente"
	--,TAS."OrigenInstrumento"
FROM(
	-- Movimiento En Transito
	SELECT
		 DISTINCT(CASE WHEN MOVTRA."IdClasificacionTipoInstrumento" = 'IF' THEN MOVTRA."NemoSVS" ELSE MOVTRA."Nemotecnico" END) AS "Nemotecnico"
		,MOVTRA."Nemotecnico" AS "NemotecnicoAux"
		,MOVTRA."IdEmisor"
		,MOVTRA."IdClasificacionTipoInstrumento"
		,MOVTRA."IdTipoInstrumento"
		,CASE 
			WHEN MOVTRA."IdClasificacionTipoInstrumento" = 'IF' THEN MOVTRA."IdTipoReajuste"
			WHEN COALESCE(MOVTRA."IdMonedaInstrumento",'') = '' THEN MOVTRA."IdTipoReajuste"
			ELSE MOVTRA."IdMonedaInstrumento" END AS "IdMonedaCustodia"
		,MOVTRA."IdEmpresa"
		,'MOVIMIENTOENTRANSITO' AS "OrigenInstrumento"
	FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoEnTransitoLite" MOVTRA
	WHERE MOVTRA."IdClasificacionTipoInstrumento" IN ('RF','IF')
	  AND MOVTRA."IdEstado" = 'VIG'
	UNION ALL
	
	-- Subyacente
	SELECT
		 DISTINCT(CASE WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' THEN SUB."NemoSVS" ELSE SUB."Nemotecnico" END) AS "Nemotecnico"
		,SUB."Nemotecnico" AS "NemotecnicoAux"
		,SUB."IdEmisor"
		,SUB."IdClasificacionTipoInstrumento"
		,SUB."IdTipoInstrumento"
		,CASE 
			WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' THEN SUB."IdTipoReajuste"
			WHEN COALESCE(SUB."IdMonedaInstrumento",'') = '' THEN SUB."IdTipoReajuste"
			ELSE SUB."IdMonedaInstrumento" END AS "IdMonedaCustodia"
		,SUB."IdEmpresa"
		,'SUBYACENTE' AS "OrigenInstrumento"
	FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarSubyacenteLite" SUB
	WHERE SUB."IdClasificacionTipoInstrumento" IN ('RF','IF')
	  AND SUB."IdEstado" = 'VIG'
	UNION ALL
	
	-- Cartera
	SELECT
		 DISTINCT(CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN CAR."NemoSVS" ELSE CAR."Nemotecnico" END) AS "Nemotecnico"
		,CAR."Nemotecnico" AS "NemotecnicoAux"
		,CAR."IdEmisor"
		,CAR."IdClasificacionTipoInstrumento"
		,CAR."IdTipoInstrumento"
		,CASE 
			WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN CAR."IdTipoReajuste"
			WHEN COALESCE(CAR."IdMonedaInstrumento",'') = '' THEN CAR."IdTipoReajuste"
			ELSE CAR."IdMonedaInstrumento" END AS "IdMonedaCustodia"
		,CAR."IdEmpresa"
		,'CARTERA' AS "OrigenInstrumento"
	FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite" CAR
	WHERE CAR."IdClasificacionTipoInstrumento" IN ('RF','IF')
	  AND CAR."IdEstado" = 'VIG'
	UNION ALL
	
	-- Garantia
	SELECT
		 DISTINCT(CASE WHEN GTA."IdClasificacionTipoInstrumento" = 'IF' THEN GTA."NemoSVS" ELSE GTA."Nemotecnico" END) AS "Nemotecnico"
		,GTA."Nemotecnico" AS "NemotecnicoAux"
		,GTA."IdEmisor"
		,GTA."IdClasificacionTipoInstrumento"
		,GTA."IdTipoInstrumento"
		,CASE 
			WHEN GTA."IdClasificacionTipoInstrumento" = 'IF' THEN GTA."IdTipoReajuste"
			WHEN COALESCE(GTA."IdMonedaInstrumento",'') = '' THEN GTA."IdTipoReajuste"
			ELSE GTA."IdMonedaInstrumento" END AS "IdMonedaCustodia"
		,GTA."IdEmpresa"
		,'GARANTIA' AS "OrigenInstrumento"
	FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiaLite" GTA
	WHERE GTA."IdClasificacionTipoInstrumento" IN ('RF','IF')
	  AND GTA."IdEstado" = 'VIG'
	) TAS
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS
	ON (TAS."Nemotecnico" = INS."Nemotecnico" AND INS."IdEstado" = 'ACT')
INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP
	ON (TAS."IdEmpresa" = EMP."IdEmpresa" AND EMP."IdEstado" = 'ACT')
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC
	ON (EMP."IdEmpresa" = SOC."IdEmpresa" AND EMP."IdSocioDeNegocio" = SOC."IdSocioDeNegocio" AND SOC."Borrado" = 'N')
WHERE (CASE WHEN COALESCE(TAS."IdEmisor",'') = '' THEN INS."IdEmisor" ELSE TAS."IdEmisor" END) <> ''
  AND (CASE WHEN COALESCE(TAS."IdMonedaCustodia",'') = '' THEN COALESCE(INS."IdMoneda",'') ELSE TAS."IdMonedaCustodia" END) <> ''
;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TAS.ConsultarInstrumentoCustodiaParaTasas" TO BCS_DESARROLLADOR WITH GRANT OPTION; 