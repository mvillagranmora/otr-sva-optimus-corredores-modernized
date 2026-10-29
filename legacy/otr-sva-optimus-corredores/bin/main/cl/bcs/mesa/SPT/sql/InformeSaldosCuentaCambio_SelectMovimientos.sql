SELECT 
    "RutInstitucion",
    "FechaPosicion",
    "Concepto",
    "Moneda",
    COALESCE("Pariedad",0) AS "Pariedad",
    COALESCE(SUM(ROUND("MontoMonedaPrincipal",2)),0) AS "MontoMonedaPrincipal",
    COALESCE(SUM(ROUND("MontoDolar",2)),0) AS "MontoDolar"
FROM (
    SELECT
    EMP."DocumentoIdentidad" AS "RutInstitucion"
    ,TO_NVARCHAR(:Periodo,'YYYY-MM-DD') AS "FechaPosicion"
    ,1 AS "Concepto"    
    ,MON."CodigoNumerico" AS "Moneda"
    ,CASE 
        WHEN SPT."IdMonedaPrincipal" <> 'CLP'THEN NULLIF(PAR_MON."ParidadMercado",0)
        WHEN SPT."IdMonedaPrincipal" = 'CLP' THEN NULLIF(PAR_USD."TipoCambioMercado",0)
        ELSE 0
    END  AS "Pariedad"
    ,SPT."MontoMonedaPrincipal" AS "MontoMonedaPrincipal"    
    ,CASE WHEN MON."IdTipoMoneda" = 'FUE' AND SPT."IdMonedaPrincipal" <> 'CLP' THEN COALESCE(SPT."MontoMonedaPrincipal" * 
         NULLIF(PAR_MON."ParidadMercado",0), 0)
      WHEN MON."IdTipoMoneda" <> 'FUE' AND SPT."IdMonedaPrincipal" <> 'CLP' THEN COALESCE(SPT."MontoMonedaPrincipal" / 
         NULLIF(PAR_MON."ParidadMercado",0), 0) 
      WHEN SPT."IdMonedaPrincipal" = 'CLP' THEN COALESCE(SPT."MontoMonedaPrincipal" / 
        NULLIF(PAR_USD."TipoCambioMercado",0), 0)
     ELSE 0
    END AS "MontoDolar"
    FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
    INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente 
        ON Cliente."IdCliente" = SPT."IdCliente"
    INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP
        ON (EMP."IdEmpresa" = :IdEmpresa) 
    LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MON   
        ON (SPT."IdMonedaPrincipal" = MON."IdMoneda")    
    LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR_MON
        ON (SPT."IdMonedaPrincipal" = PAR_MON."IdMoneda" AND :Periodo = TO_CHAR(PAR_MON."Fecha",'YYYY-MM-DD'))
    LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR_USD
        ON ('USD' = PAR_USD."IdMoneda" AND :Periodo = TO_CHAR(PAR_USD."Fecha",'YYYY-MM-DD'))    
    WHERE SPT."OperacionFicticia" = 'N'
    AND SPT."Estado" = 'VIG'
    AND TO_CHAR(SPT."FechaOperacion",'YYYY-MM') = TO_CHAR(:Periodo,'YYYY-MM'))
group by "RutInstitucion","FechaPosicion","Concepto","Moneda","Pariedad"    

UNION ALL	
	
SELECT 
    "RutInstitucion",
    "FechaPosicion",
    "Concepto",
    "Moneda",
    "Pariedad",
    ROUND("MontoMonedaPrincipal", 2) AS "MontoMonedaPrincipal",
    COALESCE(SUM(ROUND("MontoDolar", 2)),0) AS "MontoDolar"
FROM (
    SELECT
    EMP."DocumentoIdentidad" AS "RutInstitucion"
    ,TO_NVARCHAR(:Periodo,'YYYY-MM-DD') AS "FechaPosicion"
    ,CASE WHEN SPT."IdTipoOperacion"='COMSPT'AND  RTRIM(LTRIM(Cliente."DocumentoIdentidad")) IN (SELECT RTRIM(LTRIM("DocumentoIdentidad")) FROM "BSCL.Mesa.BD::SPT.EntMercadoCambiario") THEN 2
          WHEN SPT."IdTipoOperacion"='COMSPT' AND RTRIM(LTRIM(Cliente."DocumentoIdentidad")) NOT  IN (SELECT RTRIM(LTRIM("DocumentoIdentidad")) FROM "BSCL.Mesa.BD::SPT.EntMercadoCambiario")THEN 3
          WHEN SPT."IdTipoOperacion"='VENSPT' AND RTRIM(LTRIM(Cliente."DocumentoIdentidad")) NOT  IN (SELECT RTRIM(LTRIM("DocumentoIdentidad")) FROM "BSCL.Mesa.BD::SPT.EntMercadoCambiario")THEN 4
    END AS "Concepto"
    ,0 AS "Moneda"
    ,0.0 AS "Pariedad"
    ,0.0 AS "MontoMonedaPrincipal"
    ,CASE 
        WHEN SPT."IdMonedaPrincipal"='USD' THEN SPT."MontoMonedaPrincipal"
        WHEN  SPT."IdMonedaSecundaria"='USD'  THEN SPT."MontoMonedaSecundaria"
        WHEN MON."IdTipoMoneda" = 'FUE' AND SPT."IdMonedaPrincipal" <> 'CLP' THEN COALESCE(SPT."MontoMonedaPrincipal" * 
            (CASE WHEN SPT."ParidadCierre" <> 0 THEN SPT."ParidadCierre" ELSE NULLIF(PAR_MON."ParidadMercado",0) END), 0)
        WHEN MON."IdTipoMoneda" <> 'FUE' AND SPT."IdMonedaPrincipal" <> 'CLP' THEN COALESCE(SPT."MontoMonedaPrincipal" / 
            (CASE WHEN SPT."ParidadCierre" <> 0 THEN SPT."ParidadCierre" ELSE NULLIF(PAR_MON."ParidadMercado",0) END), 0) 
        WHEN SPT."IdMonedaPrincipal" = 'CLP' THEN COALESCE(SPT."MontoMonedaPrincipal" / 
            (CASE WHEN SPT."TipoCambioCierre" <> 0 THEN SPT."TipoCambioCierre" ELSE NULLIF(PAR_USD."TipoCambioMercado",0) END), 0)
     ELSE 0
    END AS "MontoDolar"    
    FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
    INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente 
        ON Cliente."IdCliente" = SPT."IdCliente"
    INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP
        ON (EMP."IdEmpresa" = :IdEmpresa)
    LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MON   
        ON (SPT."IdMonedaPrincipal" = MON."IdMoneda")
    LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR_MON
        ON (SPT."IdMonedaPrincipal" = PAR_MON."IdMoneda" AND TO_CHAR(SPT."FechaOperacion",'YYYY-MM-DD') = TO_CHAR(PAR_MON."Fecha",'YYYY-MM-DD'))
    LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR_USD
        ON ('USD' = PAR_USD."IdMoneda" AND TO_CHAR(SPT."FechaOperacion",'YYYY-MM-DD') = TO_CHAR(PAR_USD."Fecha",'YYYY-MM-DD'))
    LEFT JOIN "BSCL.SocioNegocio.BD::PRO.Proveedor" PRO
        ON Cliente."IdPersona"=PRO."IdPersona" 
    WHERE SPT."OperacionFicticia" = 'N'
    AND SPT."Estado"  = 'VIG'
    AND TO_CHAR(SPT."FechaOperacion",'YYYY-MM') = TO_CHAR(:Periodo,'YYYY-MM')
)
WHERE  "Concepto" IS NOT NULL
GROUP BY "RutInstitucion","FechaPosicion","Concepto","Moneda","Pariedad","MontoMonedaPrincipal"
ORDER BY "Concepto" ASC