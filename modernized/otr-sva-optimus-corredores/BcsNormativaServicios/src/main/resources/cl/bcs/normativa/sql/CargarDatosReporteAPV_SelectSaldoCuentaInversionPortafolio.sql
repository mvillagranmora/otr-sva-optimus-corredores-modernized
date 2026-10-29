WITH PortafolioFiltrado AS (
	SELECT 
		 POR."IdPortafolio"
		,POR."IdCliente"
		,CLI."DocumentoIdentidad"
        ,CLI."NombreCompleto" AS "NombreCliente"
	    ,CASE WHEN REGTRIB."Codigo" = 'BF' THEN 'APV' ELSE REGTRIB."CodigoRegimenTipoAhorro" END AS "GrupoRegimenTributario"
		,REGTRIB."IdRegimenTributario"
		,REGTRIB."Codigo" AS "CodigoRegimenTributario"
		,PLAN."IdPlanTipoInversion"
		,PLAN."NombrePlanTipoInversion"
    FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
    INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente" = POR."IdCliente" AND CLI."IdEmpresa" = :IdEmpresa
    INNER JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" MAT ON MAT."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV"
    INNER JOIN "BSCL.APV.BD::APV.ProductoAgrupador" MADOR ON MADOR."IdProductoAgrupador" = MAT."IdProductoAgrupador" AND MADOR."IdEstado" = 'VIG'
    INNER JOIN "BSCL.APV.BD::SCS.PlanTipoInversion" PLAN ON PLAN."IdPlanTipoInversion" = MADOR."IdPlanTipoInversion"
    INNER JOIN "BSCL.APV.BD::REC.RegimenTributario" REGTRIB ON REGTRIB."IdRegimenTributario" = MAT."IdRegimenTributario"
    WHERE POR."Borrado" = 'N' 
    AND POR."Proposito" = 'PROPOSITOAPV'
	AND POR."IdMatrizAgrupadorAPV" <> 0
)
,CuentaInversion AS (
	SELECT    
		 CIN."IdCuentaInversion"
        ,CIN."IdPortafolio"
        ,CIN."IdCliente"
        ,CIN."IdMoneda"
        ,POR."DocumentoIdentidad"
        ,POR."NombreCliente"
        ,POR."GrupoRegimenTributario"
        ,POR."IdRegimenTributario"
        ,POR."CodigoRegimenTributario"
        ,POR."IdPlanTipoInversion"
        ,POR."NombrePlanTipoInversion"
	FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion" CIN
	INNER JOIN PortafolioFiltrado POR ON POR."IdPortafolio" = CIN."IdPortafolio"
	WHERE 
		CIN."IdEmpresa" = :IdEmpresa
		AND CIN."IdMoneda" IN ('USD','CLP')
)
,Disponible AS (
	SELECT *
	FROM (
        SELECT ROW_NUMBER() OVER (PARTITION BY MOV."IdCuentaInversion" ORDER BY MOV."FechaCertificacion" DESC, MOV."IdCargoAbonoMovimiento" DESC) AS "Row",
               MOV."IdCuentaInversion", CIN."IdMoneda",CIN."IdCliente",
               CIN."IdPortafolio", CIN."DocumentoIdentidad", CIN."NombreCliente", 
               CIN."GrupoRegimenTributario", CIN."IdRegimenTributario", CIN."CodigoRegimenTributario", 
               CIN."IdPlanTipoInversion", CIN."NombrePlanTipoInversion",
               COALESCE(MOV."Saldo", 0) AS "SaldoDisponible"
        FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" MOV
        INNER JOIN CuentaInversion CIN ON CIN."IdCuentaInversion" = MOV."IdCuentaInversion"
        INNER JOIN (
            SELECT M."IdCuentaInversion",
                   MAX(CASE WHEN M."IdEstado" = 'CRT' THEN M."FechaEfectivaMovimiento" END) AS "FechaAnterior"
            FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" M
            INNER JOIN CuentaInversion C ON C."IdCuentaInversion" = M."IdCuentaInversion"
            WHERE M."IdEstado" IN ('CRT', 'PEN')
              AND COALESCE(M."FechaEfectivaMovimiento", TO_DATE(:FechaCierre)) <= TO_DATE(:FechaCierre)
            GROUP BY M."IdCuentaInversion"
        ) R ON R."IdCuentaInversion" = MOV."IdCuentaInversion"
           AND R."FechaAnterior" = MOV."FechaEfectivaMovimiento"
        WHERE MOV."IdEstado" = 'CRT'
    )
    WHERE "Row" = 1
)
,Retenido AS (
    SELECT CIN."IdCuentaInversion", CIN."IdMoneda", CIN."IdPortafolio", CIN."IdCliente",
           CIN."DocumentoIdentidad", CIN."NombreCliente", CIN."GrupoRegimenTributario",
           CIN."IdRegimenTributario", CIN."CodigoRegimenTributario",
           CIN."IdPlanTipoInversion", CIN."NombrePlanTipoInversion",
           CAST(SUM(CASE WHEN MOV."IdTipoMovimiento" = 'MOVABN' THEN MOV."Monto" ELSE -MOV."Monto" END) AS DECIMAL(26,8)) AS "SaldoRetenido"
    FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" MOV
    INNER JOIN CuentaInversion CIN ON CIN."IdCuentaInversion" = MOV."IdCuentaInversion"
    WHERE MOV."IdEstado" = 'CRT' AND MOV."FechaSistema" <= :FechaCierre AND MOV."FechaEfectivaMovimiento" > :FechaCierre
    GROUP BY CIN."IdCuentaInversion", CIN."IdMoneda", CIN."IdPortafolio", CIN."IdCliente",
             CIN."DocumentoIdentidad", CIN."NombreCliente", CIN."GrupoRegimenTributario",
             CIN."IdRegimenTributario", CIN."CodigoRegimenTributario",
             CIN."IdPlanTipoInversion", CIN."NombrePlanTipoInversion"
)
,Garantia AS (
    SELECT 
        RES_GAR."IdCuentaInversion", RES_GAR."IdMoneda", RES_GAR."IdPortafolio", RES_GAR."IdCliente",
		RES_GAR."DocumentoIdentidad", RES_GAR."NombreCliente", RES_GAR."GrupoRegimenTributario",
		RES_GAR."IdRegimenTributario", RES_GAR."CodigoRegimenTributario",
		RES_GAR."IdPlanTipoInversion", RES_GAR."NombrePlanTipoInversion",
        CAST(SUM(COALESCE(RES_GAR."INGCantidad", 0)) AS DECIMAL(26,8)) AS "SaldoGarantia"
    FROM (SELECT
            CIN."IdCuentaInversion", CIN."IdMoneda", CIN."IdPortafolio", CIN."IdCliente",
           	CIN."DocumentoIdentidad", CIN."NombreCliente", CIN."GrupoRegimenTributario",
           	CIN."IdRegimenTributario", CIN."CodigoRegimenTributario",
           	CIN."IdPlanTipoInversion", CIN."NombrePlanTipoInversion",
            GAR."INGCantidad"
        FROM CuentaInversion CIN
        LEFT JOIN "BSCL.Custodia.BD::GTA.Garantia" GAR ON CIN."IdPortafolio" = GAR."IdPortafolio" AND CIN."IdMoneda" = GAR."IdMonedaInstrumento"
        WHERE GAR."IdEstado" = 'VIG'
          AND GAR."IdOrigenGarantia" = 'CTA'
          AND GAR."IdEmpresa" = :IdEmpresa
          AND GAR."FechaOperacion" <= :FechaCierre
    ) RES_GAR
    GROUP BY RES_GAR."IdCuentaInversion", RES_GAR."IdMoneda", RES_GAR."IdPortafolio", RES_GAR."IdCliente",
		RES_GAR."DocumentoIdentidad", RES_GAR."NombreCliente", RES_GAR."GrupoRegimenTributario",
		RES_GAR."IdRegimenTributario", RES_GAR."CodigoRegimenTributario",
		RES_GAR."IdPlanTipoInversion", RES_GAR."NombrePlanTipoInversion"
)
,SaldoBase AS (
    SELECT COALESCE(DISP."IdCuentaInversion", RET."IdCuentaInversion") AS "IdCuentaInversion",
           COALESCE(DISP."IdMoneda", RET."IdMoneda") AS "IdMoneda",
           COALESCE(DISP."IdPortafolio", RET."IdPortafolio") AS "IdPortafolio",
           COALESCE(DISP."IdCliente", RET."IdCliente") AS "IdCliente",
           COALESCE(DISP."DocumentoIdentidad", RET."DocumentoIdentidad") AS "DocumentoIdentidad",
           COALESCE(DISP."NombreCliente", RET."NombreCliente") AS "NombreCliente",
           COALESCE(DISP."GrupoRegimenTributario", RET."GrupoRegimenTributario") AS "GrupoRegimenTributario",
           COALESCE(DISP."IdRegimenTributario", RET."IdRegimenTributario") AS "IdRegimenTributario",
           COALESCE(DISP."CodigoRegimenTributario", RET."CodigoRegimenTributario") AS "CodigoRegimenTributario",
           COALESCE(DISP."IdPlanTipoInversion", RET."IdPlanTipoInversion") AS "IdPlanTipoInversion",
           COALESCE(DISP."NombrePlanTipoInversion", RET."NombrePlanTipoInversion") AS "NombrePlanTipoInversion",
           GREATEST((COALESCE(DISP."SaldoDisponible",0) + COALESCE(GAR."SaldoGarantia", 0)) + COALESCE(RET."SaldoRetenido",0), 0) AS "SaldoBase"
    FROM Disponible DISP
    FULL OUTER JOIN Retenido RET ON RET."IdCuentaInversion" = DISP."IdCuentaInversion" AND RET."IdMoneda" = DISP."IdMoneda"
    LEFT JOIN Garantia GAR ON GAR."IdCuentaInversion" = COALESCE(DISP."IdCuentaInversion", RET."IdCuentaInversion") AND GAR."IdMoneda" = COALESCE(DISP."IdMoneda", RET."IdMoneda")
)
,TipoCambio AS (
    SELECT MAX("Valor") AS "Valor"
    FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero"
    WHERE "IdParametroFinanciero" = 'DO'
      AND "Fecha" = :FechaCierre
)
,SaldoCalculado AS (
    SELECT
         SB."IdPortafolio"
        ,SB."IdCliente"
        ,SB."DocumentoIdentidad"
        ,SB."NombreCliente"
        ,SB."GrupoRegimenTributario"
        ,SB."IdRegimenTributario"
        ,SB."CodigoRegimenTributario"
        ,SB."IdPlanTipoInversion"
        ,SB."NombrePlanTipoInversion"
        ,CASE
            WHEN SB."IdMoneda" = 'USD'
                THEN "BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(
                        SB."SaldoBase" * COALESCE(TC."Valor", 1),
                        MF."MontoDecimales",
                        MF."MontoTrucaRedondea"
                     )
            ELSE ROUND(SB."SaldoBase", 0, ROUND_DOWN)
         END AS "SaldoCalculado"
    FROM SaldoBase SB
    INNER JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF
        ON MF."IdMoneda" = 'CLP'
       AND MF."IdUsoMonedaFormato" = 'GEN'
       AND MF."IdTipoInstrumento" = ''
       AND MF."IdTipoOperacion" = ''
    CROSS JOIN TipoCambio TC
)
SELECT
     SC."IdPortafolio"
    ,SC."IdCliente"
    ,SC."DocumentoIdentidad"
    ,SC."NombreCliente"
    ,SC."GrupoRegimenTributario"
    ,SC."IdRegimenTributario"
    ,SC."CodigoRegimenTributario"
    ,SC."IdPlanTipoInversion"
    ,SC."NombrePlanTipoInversion"
    ,SUM(SC."SaldoCalculado") AS "SaldoTotal"
FROM SaldoCalculado SC
GROUP BY
     SC."IdPortafolio"
    ,SC."IdCliente"
    ,SC."DocumentoIdentidad"
    ,SC."NombreCliente"
    ,SC."GrupoRegimenTributario"
    ,SC."IdRegimenTributario"
    ,SC."CodigoRegimenTributario"
    ,SC."IdPlanTipoInversion"
    ,SC."NombrePlanTipoInversion"
HAVING SUM(SC."SaldoCalculado") <> 0;