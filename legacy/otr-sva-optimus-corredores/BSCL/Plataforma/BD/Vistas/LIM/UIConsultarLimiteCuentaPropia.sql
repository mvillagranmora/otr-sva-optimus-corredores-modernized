
SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::LIM.UIConsultarLimiteCuentaPropia"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.UIConsultarLimiteCuentaPropia"  
AS
WITH
	CteLimiteCtaPropia AS (
		SELECT
			Limite."IdLimite"
			, Limite."IdTipoLimite"
			, CASE Limite."IdTipoInstrumento" WHEN '0' THEN 'TODOS' ELSE Limite."TipoInstrumento" END AS "TipoInstrumento"
			, CASE Limite."IdInstrumento" WHEN 0 THEN 'TODOS' ELSE COALESCE(Limite."RazonSocialInstrumento", '') END AS "RazonSocialInstrumento"
			, CASE Limite."IdTipoOperacion" WHEN '0' THEN 'TODOS' ELSE COALESCE(TipoOperacion."Nombre", '') END AS "TipoOperacion"
			, CASE Limite."IdCanalIngreso" WHEN '0' THEN 'TODOS' ELSE COALESCE(TipoCanal."Nombre", '') END AS "CanalIngreso"
			, CASE Limite."CodigoOperador" WHEN '0' THEN 'TODOS' ELSE COALESCE(Limite."CodigoOperador", '') END AS "CodigoOperador"
			, CASE Limite."IdMoneda" WHEN '0' THEN 'TODOS' ELSE COALESCE(Moneda."NombreLargo",' -') END AS "Moneda"
			, COALESCE(Limite."MontoIntraday", 0) AS "MontoIntraday"
			, COALESCE(Limite."MontoOvernight", 0) AS "MontoOvernight"
			, COALESCE(Limite."MontoTotal", 0) AS "MontoTotal"
			, COALESCE(Limite."UsaPlazo", '') AS "UsaPlazo"
			, COALESCE(Limite."UnidadPlazo", '') AS "UnidadPlazo"
			, COALESCE(Limite."Plazo", 0) AS "Plazo"
			, COALESCE(Limite."IdEmisor", '') AS "IdEmisor"
			, COALESCE(Limite."Porcentaje", 0) AS "Porcentaje"
			, COALESCE(ClasificacionRiesgo."IdEntidadClasificadoraRiesgo", '') AS "IdEntidadClasificadoraRiesgo"
			, COALESCE(ClasificacionRiesgo."ClasificacionRiesgo", '') AS "ClasificacionRiesgo"
			, COALESCE(Limite."MargenInferior", 0) AS "MargenInferior"
			, COALESCE(Limite."MargenSuperior", 0) AS "MargenSuperior"
			, Limite."IdTipoComportamiento" AS "IdTipoComportamiento"
			, Limite."TipoComportamiento" AS "TipoComportamiento"
			, Limite."IdEstadoConfigurable" AS "IdEstadoConfigurable"
			, CASE Limite."IdEstadoAutorizacion" WHEN 'ING' THEN 'AUT' ELSE Limite."IdEstadoAutorizacion" END AS "IdEstadoAutorizacion"
		FROM
			"BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteCtaPropia" Limite
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" Instrumento ON (Instrumento."IdInstrumento" = Limite."IdInstrumento")
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TipoOperacion ON (TipoOperacion."IdTipoOperacion" = Limite."IdTipoOperacion")
			LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" TipoCanal ON (TipoCanal."IdTipoCanalIngreso" = Limite."IdCanalIngreso")
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" Moneda ON (Moneda."IdMoneda" = Limite."IdMoneda")
			LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ClasificacionRiesgo ON (ClasificacionRiesgo."IdClasificacionRiesgo" = Limite."IdClasificacionRiesgo")
	)
SELECT 
    DPL."IdLimite"
    , DPL."IdTipoLimite"
    , DPL."NombreTipoLimite"
    , DPL."IdCanalIngreso"
    , Limite."CanalIngreso"
    , DPL."IdTipoInstrumento"
    , DPL."IdClasificacionTipoInstrumento"
    , DPL."IdInstrumento"
    , DPL."IdTipoOperacion"
    , DPL."IdMoneda"
    , "BSCL.Plataforma.BD.Utilitarios::ObtenerEstadoAutorizacion"('PLANTILLALIMITEDETALLE',DPL."IdEstado",DPL."IdEstadoAutorizacion") AS "IdEstado"
    , DPL."IdEstadoConfigurable"
    , DPL."IdEmpresa"
    , DPL."IdClasificacionRiesgo"
    , DPL."IdTipoComportamiento"
    , DPL."TipoComportamiento"
    , CASE
    	WHEN Limite."IdTipoLimite" IN ('LIMCTPSPOT', 'LIMCTPFORWARD') THEN Limite."RazonSocialInstrumento"
    	ELSE Limite."TipoInstrumento"
	END AS "TipoInstrumento"
	--, CASE WHEN (DPL."IdTipoOperacion" = '0') OR (DPL."IdInstrumento" = '0') THEN 'TODOS' ELSE COALESCE(INS."Nemotecnico",' -') END AS "Nemotecnico"
    --, CASE WHEN (DPL."IdInstrumento" = 0) THEN 'TODOS' ELSE COALESCE(INS."RazonSocial", ' -') END AS "RazonSocial"
    , CASE
		WHEN Limite."IdTipoLimite" IN ('LIMCTPEXCLUIREMISOR', 'LIMCTPCLASIFRIESGO', 'LIMCTPMARGENTASAS', 'LIMCTPMONEDA') THEN Limite."TipoOperacion"
		ELSE 'UI_NO_APLICA'
	END "TipoOperacion"
    , CASE Limite."IdTipoLimite"
    	WHEN 'LIMCTPFORWARD' THEN Limite."UnidadPlazo"
    	ELSE 'UI_NO_APLICA'
	END AS "UnidadPlazo"
    , Limite."Plazo"
    , CASE
    	WHEN Limite."IdTipoLimite" IN ('LIMCTPSPOT', 'LIMCTPFORWARD') THEN Limite."CodigoOperador"
    	ELSE 'UI_NO_APLICA'
	END AS "CodigoOperador"
    , CASE
    	WHEN Limite."IdTipoLimite" IN ('LIMCTPFORWARD', 'LIMCTPCLASIFRIESGO') THEN Limite."MontoTotal"
    	ELSE 0
	END AS "MontoTotal"
	, CASE Limite."IdTipoLimite"
    	WHEN 'LIMCTPSPOT' THEN Limite."MontoIntraday"
    	ELSE 0
	END AS "MontoIntraday"
	, CASE Limite."IdTipoLimite"
    	WHEN 'LIMCTPSPOT' THEN Limite."MontoOvernight"
    	ELSE 0
	END AS "MontoOvernight"
	, CASE
    	WHEN Limite."IdTipoLimite" IN ('LIMCTPSPOT', 'LIMCTPFORWARD', 'LIMCTPPLAZO', 'LIMCTPCLASIFRIESGO', 'LIMCTPMONEDA') THEN Limite."Moneda"
    	ELSE 'UI_NO_APLICA'
	END AS "Moneda"
	, CASE WHEN DPL."IdEstadoAutorizacion" = 'ING' THEN 'AUT' ELSE DPL."IdEstadoAutorizacion" END AS "IdEstadoAutorizacion",
	CASE Limite."IdTipoLimite"
    	WHEN 'LIMCTPCLASIFRIESGO' THEN
    	  (
    		CASE DPL."IdClasificacionRiesgo" 
    		WHEN 0 THEN 'TODOS'
    		ELSE Limite."ClasificacionRiesgo"
    		END
    	  )
    	ELSE 'UI_NO_APLICA'
	END AS "ClasificacionRiesgo"
	, CASE WHEN DPL."IdTipoLimite"='LIMCTPGRUPOEMISOR' THEN DPL."IdGrupoEmisor" ELSE ' -' END AS "IdGrupoEmisor"
	, CASE Limite."IdTipoLimite"
    	WHEN 'LIMCTPEXCLUIREMISOR' THEN Limite."IdEmisor"
    	ELSE 'UI_NO_APLICA'
	END AS "IdEmisor"
	, CASE WHEN DPL."IdEmisor" = '0' THEN 'TODOS' ELSE DPL."IdEmisor" END AS "NombreEmisor"
	, CASE Limite."IdTipoLimite"
    	WHEN 'LIMCTPFORWARD' THEN Limite."UsaPlazo"
    	ELSE 'UI_NO_APLICA'
	END AS "UsaPlazo"
	, CASE Limite."IdTipoLimite"
    	WHEN 'LIMCTPCLASIFRIESGO' THEN Limite."IdEntidadClasificadoraRiesgo"
    	ELSE 'UI_NO_APLICA'
	END AS "IdEntidadClasificadoraRiesgo"
	, CASE Limite."IdTipoLimite"
    	WHEN 'LIMCTPMARGENTASAS' THEN Limite."MargenInferior"
    	ELSE 0
	END AS "MargenInferior"
	, CASE Limite."IdTipoLimite"
    	WHEN 'LIMCTPMARGENTASAS' THEN Limite."MargenSuperior"
    	ELSE 0
	END AS "MargenSuperior"
	, CASE
    	WHEN Limite."IdTipoLimite" IN ('LIMCTPEXCLUIREMISOR', 'LIMCTPMONEDA') THEN Limite."Porcentaje"
    	ELSE 0
	END AS "Porcentaje"
FROM
	CteLimiteCtaPropia Limite
	INNER JOIN  "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteCtaPropia" AS DPL ON (DPL."IdLimite" = Limite."IdLimite")
ORDER BY
 	"IdTipoLimite" ASC
	, "IdClasificacionTipoInstrumento" ASC
	, "IdTipoInstrumento" ASC;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.UIConsultarLimiteCuentaPropia" TO BCS_DESARROLLADOR WITH GRANT OPTION;