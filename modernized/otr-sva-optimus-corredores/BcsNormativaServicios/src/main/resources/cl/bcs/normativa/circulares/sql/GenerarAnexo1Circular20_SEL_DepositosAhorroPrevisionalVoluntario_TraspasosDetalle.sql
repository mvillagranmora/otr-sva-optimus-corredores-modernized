--falta la daterminacion de "IdTipoEntidadRecaudadora"
WITH t AS (
    SELECT
        t."IdTraspaso",d."TipoMovimiento",
        CASE WHEN d."TipoMovimiento" = 'INGRESO' THEN 2 ELSE 1 END AS "IdAccion",
        CASE WHEN d."TipoMovimiento" = 'INGRESO' THEN 'Recibidos' ELSE 'Realizados' END AS "Accion",
        COALESCE(t."Monto",0) AS "Monto",(COALESCE(t."Monto",0)/1000) AS "MontoMiles",
        CASE WHEN te."Nombre" IS NULL THEN 'SIN'--Sin entidad recaudadora
             WHEN te."Nombre" like '%AFP%' THEN 'AFP'--Afp
             WHEN te."Nombre" like '%CORREDORAS%' THEN 'COR'--CorredoresBolsa
             WHEN te."Nombre" like '%ASEGURADORA%' THEN 'CSE'--CompaniasSeguros
             WHEN te."Nombre" like '%AGENTE DE VALORES%' THEN 'AGE'--AgentesValores
             WHEN te."Nombre" like '%INVERSION%' THEN 'FIN'--FondosInversion
             WHEN te."Nombre" like '%ADMINISTRADORA DE FONDOS%' THEN 'FFM'--FondosMutuos
             WHEN te."Nombre" like '%ADMINISTRADORA DE VIVIENDA Y FFMM%' THEN 'FVI'--FondosVivienda
             WHEN te."Nombre" like '%BANCOS%' THEN 'BIF'--BancosInstitucionesFinancieras
            ELSE 'OTR' --OtrasEntidadesAutorizadasSvs
        END AS "TipoEntidad"
    FROM 
    ( 
       SELECT 2 AS "IdAccion",'INGRESO' AS "TipoMovimiento" FROM DUMMY UNION ALL
       SELECT 1 AS "IdAccion",'EGRESO' AS "TipoMovimiento" FROM DUMMY
    ) AS d    
    LEFT JOIN  (
        SELECT 
            cir."IdTraspaso",
            cir."TipoMovimiento",
            cir."IdPersona",
            SUM(COALESCE(cir."Monto",0)) AS "Monto"
        FROM "BSCL.Normativa.BD::CIR.DatosEstadisticaCircular1981" cir
        INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" por ON por."IdPortafolio" = cir."IdPortafolio" AND ( por."IdEstado" = 'ACT' OR (por."IdEstado" = 'BLO'AND por."IdTipoBloqueo" ='BMUERT'))
        WHERE cir."SecuenciaSolicitud" = :SecuenciaSolicitud
          AND cir."EsTraspaso" = 'S'
          AND (cir."Codigo" = 'APV-A' OR cir."Codigo" = 'APV-B')
        GROUP BY cir."IdTraspaso",cir."TipoMovimiento",cir."IdPersona"
    ) t ON (t."TipoMovimiento" = d."TipoMovimiento")
    LEFT JOIN "BSCL.APV.BD::SCS.EntidadRecaudadora" AS e ON (e."IdPersona" = t."IdPersona")
    LEFT JOIN "BSCL.APV.BD::SCS.TipoEntidadRecaudadora" AS te ON (te."IdTipoEntidadRecaudadora" = e."IdTipoEntidadRecaudadora")
)
SELECT 
    "IdAccion","Accion",
    --Afp
    to_bigint(COUNT(CASE WHEN "TipoEntidad" = 'AFP' THEN 1 ELSE NULL END)) AS "Afp_NumeroTraspasos",
    to_decimal(SUM(CASE WHEN "TipoEntidad" = 'AFP' THEN "MontoMiles" ELSE 0 END),26,8) AS "Afp_Miles",
    --BancosInstitucionesFinancieras
    to_bigint(COUNT(CASE WHEN "TipoEntidad" = 'BIF' THEN 1 ELSE NULL END)) AS "BancosInstitucionesFinancieras_NumeroTraspasos",
    to_decimal(SUM(CASE WHEN "TipoEntidad" = 'BIF' THEN "MontoMiles" ELSE 0 END),26,8) AS "BancosInstitucionesFinancieras_Miles",
    --CorredoresBolsa
    to_bigint(COUNT(CASE WHEN "TipoEntidad" = 'COR' THEN 1 ELSE NULL END)) AS "CorredoresBolsa_NumeroTraspasos",
    to_decimal(SUM(CASE WHEN "TipoEntidad" = 'COR' THEN "MontoMiles" ELSE 0 END),26,8) AS "CorredoresBolsa_Miles",
    --AgentesValores
    to_bigint(COUNT(CASE WHEN "TipoEntidad" = 'AGE' THEN 1 ELSE NULL END)) AS "AgentesValores_NumeroTraspasos",
    to_decimal(SUM(CASE WHEN "TipoEntidad" = 'AGE' THEN "MontoMiles" ELSE 0 END),26,8) AS "AgentesValores_Miles",
    --FondosMutuos
    to_bigint(COUNT(CASE WHEN "TipoEntidad" = 'FFM' THEN 1 ELSE NULL END)) AS "FondosMutuos_NumeroTraspasos",
    to_decimal(SUM(CASE WHEN "TipoEntidad" = 'FFM' THEN "MontoMiles" ELSE 0 END),26,8) AS "FondosMutuos_Miles",
    --FondosInversion
    to_bigint(COUNT(CASE WHEN "TipoEntidad" = 'FIN' THEN 1 ELSE NULL END)) AS "FondosInversion_NumeroTraspasos",
    to_decimal(SUM(CASE WHEN "TipoEntidad" = 'FIN' THEN "MontoMiles" ELSE 0 END),26,8) AS "FondosInversion_Miles",
    --CompaniasSeguros
    to_bigint(COUNT(CASE WHEN "TipoEntidad" = 'CSE' THEN 1 ELSE NULL END)) AS "CompaniasSeguros_NumeroTraspasos",
    to_decimal(SUM(CASE WHEN "TipoEntidad" = 'CSE' THEN "MontoMiles" ELSE 0 END),26,8) AS "CompaniasSeguros_Miles",
    --FondosVivienda
    to_bigint(COUNT(CASE WHEN "TipoEntidad" = 'FVI' THEN 1 ELSE NULL END)) AS "FondosVivienda_NumeroTraspasos",
    to_decimal(SUM(CASE WHEN "TipoEntidad" = 'FVI' THEN "MontoMiles" ELSE 0 END),26,8) AS "FondosVivienda_Miles",
    --OtrasEntidadesAutorizadasSvs
    to_bigint(COUNT(CASE WHEN "TipoEntidad" = 'OTR' THEN 1 ELSE NULL END)) AS "OtrasEntidadesAutorizadasSvs_NumeroTraspasos",
    to_decimal(SUM(CASE WHEN "TipoEntidad" = 'OTR' THEN "MontoMiles" ELSE 0 END),26,8) AS "OtrasEntidadesAutorizadasSvs_Miles"    
FROM t 
GROUP BY "IdAccion","Accion"
ORDER BY "IdAccion";
