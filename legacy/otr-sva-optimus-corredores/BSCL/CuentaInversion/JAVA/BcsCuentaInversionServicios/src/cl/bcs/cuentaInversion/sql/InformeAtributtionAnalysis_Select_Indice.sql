WITH
    v_p as (
    
		SELECT 	
		ROW_NUMBER() OVER (PARTITION by IND."IdIndiceFinanciero" ORDER BY IND."IdIndiceFinanciero" , IND."Fecha" ASC)   AS idRow,
        ROW_NUMBER() OVER (PARTITION by IND."IdIndiceFinanciero" ORDER BY IND."IdIndiceFinanciero" , IND."Fecha" DESC)  AS idRow2,
                IND."Valor" , IND."IdIndiceFinanciero"
            FROM "BSCL.Plataforma.BD::MER.IndiceFinanciero" AS IND
            WHERE IND."Fecha" BETWEEN  :FechaDesde AND :FechaHasta
    )
    SELECT DISTINCT
    a."Valor" AS "PrecioInicial",    
    b."Valor" AS "PrecioFinal",
    CASE WHEN ARB."Indice" != '' THEN  ARB."Indice" ELSE 'ND' END  AS  "Indice",
    ARB."IdArbolClasificacionActivo",
    ARB."Padre",
    ARB."Descripcion"
    FROM  "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos" AS ARB
        LEFT JOIN "BSCL.Plataforma.BD::MER.IndiceFinanciero" AS INDFIN ON (INDFIN."IdIndiceFinanciero" = ARB."Indice")
        LEFT JOIN v_p AS a ON  (
            a."IdIndiceFinanciero" = INDFIN."IdIndiceFinanciero"  AND a.idRow = 1
        )
        LEFT JOIN v_p AS b ON  (
            b."IdIndiceFinanciero" = INDFIN."IdIndiceFinanciero"  AND b.idRow2 = 1
        )
    	INNER JOIN  #InArbolClasificacion as TEMP on (TEMP."IdArbolClasificacionActivo" = ARB."IdArbolClasificacionActivo")