WITH RelacionPersona AS (
	SELECT  
	"IdPersonaDestino"
	,ROW_NUMBER() OVER (PARTITION BY "IdPersonaDestino" ORDER BY "IdPersonaDestino") AS "nroIdPersona"
	FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona"
	WHERE "IdPersonaOrigen" = :v_IdPersonaCuentaPropia
	AND "IdEmpresa" = :p_IdEmpresa
	AND "IdEstado" = 'ACT'
)
SELECT
    MOV."IdClasificacionTipoInstrumento"
   ,MOV."IdTipoInstrumento"
   ,MOV."IdInstrumento"
   ,case when "IdClasificacionTipoInstrumento" = 'IF' then MOV."NemoSVS" else MOV."Nemotecnico" end as "Nemotecnico"
   ,MOV."IdEmisor"
   ,case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."EsFungible" else 'N' end as "EsFungible"
   ,MOV."IdCliente"
   ,MOV."IdPortafolio"
   ,coalesce(MOV."IdCuentaCustodioPortafolio",0) as "IdCuentaCustodioPortafolio"
   ,CASE
        WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
        WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
        ELSE 'TER'                                                  -- Tercero
    END AS "Relacion"
    ,SUM(CASE WHEN  TipoOperacion."AccionCustodia"= 'RETIRO' THEN cast(IFNULL(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) * -1 as DECIMAL(26,8)) ELSE 0 END) as "CantidadTransEgr"
    , SUM(CASE WHEN  "AccionCustodia"= 'INGRESO' THEN case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" END ELSE 0 END)  AS "CantidadTransIng"
 
   ,CASE
        WHEN ifnull(MOV."IdMonedaInstrumento",'') <> '' then MOV."IdMonedaInstrumento"
        WHEN ifnull(MOV."IdTipoReajuste",'') <> '' then MOV."IdTipoReajuste"
        WHEN ifnull(MOV."IdMonedaSistema",'') <> '' then MOV."IdMonedaSistema"
        ELSE ''
    END AS "IdMonedaReajuste"
     ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
 			  ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
  ,COALESCE (REG."IdRegimenFondoMutuo",'') AS "IdRegimenTributario"
  ,COALESCE (REG."Descripcion",'') AS "RegimenTributario"
   ,inst4."RazonSocial" AS "RazonSocialInstrumento"
	FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = MOV."IdEmpresa"  AND CLI."IdCliente" = MOV."IdCliente")
	LEFT JOIN RelacionPersona AS REL  ON REL."IdPersonaDestino" = CLI."IdPersona" AND REL."nroIdPersona" = 1
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = MOV."IdRegimenFondoMutuo"
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" =MOV."IdInstrumento"
	WHERE
	    MOV."IdEmpresa" = :p_IdEmpresa 
	AND MOV."IdCliente" = case when :p_IdCliente > 0 then :p_IdCliente else MOV."IdCliente" END
	AND MOV."IdPortafolio" = case when :p_IdPortafolio > 0 then :p_IdPortafolio else MOV."IdPortafolio" END
	AND MOV."IdEstado" = 'PEN'
	AND MOV."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(:p_IdClasificacionTipoInstrumento,'') <> '' THEN :p_IdClasificacionTipoInstrumento ELSE "IdClasificacionTipoInstrumento" END
	AND MOV."IdTipoInstrumento" = CASE WHEN IFNULL(:p_IdTipoInstrumento,'') <> '' THEN :p_IdTipoInstrumento ELSE MOV."IdTipoInstrumento" END
	AND MOV."IdInstrumento" = CASE WHEN IFNULL(:p_IdInstrumento,0) > 0 THEN :p_IdInstrumento ELSE MOV."IdInstrumento" END
	AND MOV."Nemotecnico" = CASE WHEN IFNULL(:p_Nemotecnico,'') <> '' THEN :p_Nemotecnico ELSE MOV."Nemotecnico" END
	AND 
	(
	    (:v_FechaConsulta >= MOV."FechaOperacion" AND :v_FechaConsulta <= MOV."FechaCertificacionRegistro" AND "FechaCertificacionRegistro" >= '0001-01-02')
	    OR  
	    (:v_FechaConsulta >= MOV."FechaOperacion" AND MOV."FechaCertificacionRegistro" = '0001-01-01' )
	)
	AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
	GROUP BY
	 MOV."IdClasificacionTipoInstrumento"
	,MOV."IdTipoInstrumento"
    ,MOV."IdInstrumento"
	,MOV."NemoSVS"
	,MOV."Nemotecnico"
	,MOV."IdEmisor"
	,MOV."EsFungible"
	,MOV."IdCliente"
	,MOV."IdPortafolio"
	,"IdCuentaCustodioPortafolio"
	,MOV."IdMonedaInstrumento"
	,MOV."IdTipoReajuste"
	,MOV."IdMonedaSistema"
	,CLI."IdCliente"
	,REL."IdPersonaDestino"
	,MAT_NOAPV."IdMatrizAgrupadorNOAPV"
	,REG."IdRegimenFondoMutuo"
	,REG."Descripcion"
	,Portafolio."IdMatrizAgrupadorAPV"
	,MAT_NOAPV."EsADC"
	,MatrizAgrupador."EsADC"
	 ,inst4."RazonSocial";
