WITH a AS (
    SELECT 
        cir."EsTraspaso",
        cir."IdContratoSAAPV",
        CASE WHEN cir."EsTraspaso" = 'N' THEN 0
             WHEN cir."IdContratoSAAPV" = 0 THEN cir."IdTraspaso"
             ELSE 0
        END AS "IdTraspaso",
        cir."IdPortafolioContratoSAAPV",
        cir."IdCliente",
        cir."IdPortafolio",
        cir."IdCargoAbonoMovimiento",
        cir."IdTipoMovimiento",
        CASE WHEN cir."EsTraspaso" = 'N' THEN cir."IdConcepto"
             ELSE ''
        END AS "IdConcepto",
        CASE WHEN cir."IdTipoMovimiento" = 'MOVABN' THEN cir."Monto" ELSE COALESCE(cir."Monto" * -1, 0) END AS "Monto",
        CASE WHEN cir."IdTipoMovimiento" = 'MOVCAR' THEN cir."Monto" ELSE 0 END AS "Cargo",
        CASE WHEN cir."IdTipoMovimiento" = 'MOVABN' THEN cir."Monto" ELSE 0 END AS "Abono",
        (CASE WHEN cir."IdTipoMovimiento" = 'MOVABN' THEN cir."Monto" ELSE COALESCE(cir."Monto" * -1, 0) END) / 1000 AS "MontoMiles",
        CASE WHEN cir."IdTipoMovimiento" = 'MOVCAR' THEN cir."Monto" / 1000 ELSE 0 END AS "CargoMiles",
        CASE WHEN cir."IdTipoMovimiento" = 'MOVABN' THEN cir."Monto" / 1000 ELSE 0 END AS "AbonoMiles",
        cir."IdMatrizAgrupadorAPV",
        cir."IdPlanTipoInversion",
        CASE WHEN cir."NombrePlan" = 'A. ABIERTO' THEN 'ARQUITECTURA ABIERTA'
             WHEN cir."NombrePlan" = 'I. ACTIVO' THEN 'INVERSION ACTIVA' END AS "NombrePlan",
        cir."Sexo",
        cir."Codigo"
     FROM "BSCL.Normativa.BD::CIR.DatosEstadisticaCircular1981" cir
     INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" por ON por."IdPortafolio" = cir."IdPortafolio" AND ( por."IdEstado" = 'ACT' OR (por."IdEstado" = 'BLO'AND por."IdTipoBloqueo" ='BMUERT'))
     WHERE cir."SecuenciaSolicitud" = :SecuenciaSolicitud
       AND cir."Codigo" IN ('APV-A','APV-B','BF')
)
(SELECT
    to_bigint(a."IdPlanTipoInversion") AS "NumeroPolizaRunFondoCodigoPlan",
    a."NombrePlan" AS "Serie",
    --NumeroCuentasApvVigentes
    to_bigint(:CantCuentas_A_M_1) AS "NumeroCuentasApvVigentes_LetraA_Masculino",
    to_bigint(:CantCuentas_A_F_1) AS "NumeroCuentasApvVigentes_LetraA_Femenino",
    to_bigint(:CantCuentas_T_M_1) AS "NumeroCuentasApvVigentes_Total_Masculino",
    to_bigint(:CantCuentas_T_F_1) AS "NumeroCuentasApvVigentes_Total_Femenino",
    --NumeroAseguradosParticipesAportantesClientesPlan
    to_bigint(:CantClientes_A_M_1) AS "NumeroAseguradosParticipesAportantesClientesPlan_LetraA_Masculino",
    to_bigint(:CantClientes_A_F_1) AS "NumeroAseguradosParticipesAportantesClientesPlan_LetraA_Femenino",
    to_bigint(:CantClientes_T_M_1) AS "NumeroAseguradosParticipesAportantesClientesPlan_Total_Masculino",
    to_bigint(:CantClientes_T_F_1) AS "NumeroAseguradosParticipesAportantesClientesPlan_Total_Femenino",
    --SaldoTotalAcumulado
    (TO_DECIMAL(:SaldoAcumulado_A_M_1,26,8) / 1000) AS "SaldoTotalAcumulado_LetraA_Masculino",
    (TO_DECIMAL(:SaldoAcumulado_A_F_1,26,8) / 1000) AS "SaldoTotalAcumulado_LetraA_Femenino",
    (TO_DECIMAL(:SaldoAcumulado_B_M_1,26,8) / 1000) AS "SaldoTotalAcumulado_LetraB_Masculino",
    (TO_DECIMAL(:SaldoAcumulado_B_F_1,26,8) / 1000) AS "SaldoTotalAcumulado_LetraB_Femenino",
    --Bonificacion
    (TO_DECIMAL(:SaldoAcumulado_BF_M_1,26,8) / 1000) AS "Bonificacion_LetraA_Masculino",
    (TO_DECIMAL(:SaldoAcumulado_BF_F_1,26,8) / 1000) AS "Bonificacion_LetraA_Femenino",
    --NumeroDepositos
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" = 'APV-A' AND "Sexo" = 'M' THEN 1 ELSE NULL END)) AS "NumeroDepositos_LetraA_Masculino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" = 'APV-A' AND "Sexo" = 'F' THEN 1 ELSE NULL END)) AS "NumeroDepositos_LetraA_Femenino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" = 'APV-B' AND "Sexo" = 'M' THEN 1 ELSE NULL END)) AS "NumeroDepositos_LetraB_Masculino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" = 'APV-B' AND "Sexo" = 'F' THEN 1 ELSE NULL END)) AS "NumeroDepositos_LetraB_Femenino",
    --MontoTotalDepositos
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" = 'APV-A' AND "Sexo" = 'M' THEN "AbonoMiles" ELSE 0 END),26,8) AS "MontoTotalDepositos_LetraA_Masculino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" = 'APV-A' AND "Sexo" = 'F' THEN "AbonoMiles" ELSE 0 END),26,8) AS "MontoTotalDepositos_LetraA_Femenino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" = 'APV-B' AND "Sexo" = 'M' THEN "AbonoMiles" ELSE 0 END),26,8) AS "MontoTotalDepositos_LetraB_Masculino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" = 'APV-B' AND "Sexo" = 'F' THEN "AbonoMiles" ELSE 0 END),26,8) AS "MontoTotalDepositos_LetraB_Femenino",
    --NumeroTraspasosRecibidos
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" IN ('APV-A','APV-B') AND "Sexo" = 'M' THEN 1 ELSE NULL END)) AS "NumeroTraspasosRecibidos_Masculino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" IN ('APV-A','APV-B') AND "Sexo" = 'F' THEN 1 ELSE NULL END)) AS "NumeroTraspasosRecibidos_Femenino",
    --MontoTotalTraspasosRecibidos
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" IN ('APV-A','APV-B') AND "Sexo" = 'M' THEN "AbonoMiles" ELSE 0 END),26,8) AS "MontoTotalTraspasosRecibidos_Masculino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" IN ('APV-A','APV-B') AND "Sexo" = 'F' THEN "AbonoMiles" ELSE 0 END),26,8) AS "MontoTotalTraspasosRecibidos_Femenino",
    --NumeroRetiros
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" = 'APV-A' AND "Sexo" = 'M' THEN 1 ELSE NULL END)) AS "NumeroRetiros_LetraA_Masculino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" = 'APV-A' AND "Sexo" = 'F' THEN 1 ELSE NULL END)) AS "NumeroRetiros_LetraA_Femenino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" = 'APV-B' AND "Sexo" = 'M' THEN 1 ELSE NULL END)) AS "NumeroRetiros_LetraB_Masculino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" = 'APV-B' AND "Sexo" = 'F' THEN 1 ELSE NULL END)) AS "NumeroRetiros_LetraB_Femenino",
    --MontoTotalRetiros
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" = 'APV-A' AND "Sexo" = 'M' THEN "CargoMiles" ELSE 0 END),26,8) AS "MontoTotalRetiros_LetraA_Masculino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" = 'APV-A' AND "Sexo" = 'F' THEN "CargoMiles" ELSE 0 END),26,8) AS "MontoTotalRetiros_LetraA_Femenino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" = 'APV-B' AND "Sexo" = 'M' THEN "CargoMiles" ELSE 0 END),26,8) AS "MontoTotalRetiros_LetraB_Masculino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" = 'APV-B' AND "Sexo" = 'F' THEN "CargoMiles" ELSE 0 END),26,8) AS "MontoTotalRetiros_LetraB_Femenino",
    --NumeroTraspasosRealizados
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" IN ('APV-A','APV-B') AND "Sexo" = 'M' THEN 1 ELSE NULL END)) AS "NumeroTraspasosRealizados_Masculino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" IN ('APV-A','APV-B') AND "Sexo" = 'F' THEN 1 ELSE NULL END)) AS "NumeroTraspasosRealizados_Femenino",
    --MontoTotalTraspasosRealizados
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" IN ('APV-A','APV-B') AND "Sexo" = 'M' THEN "CargoMiles" ELSE 0 END),26,8) AS "MontoTotalTraspasosRealizados_Masculino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" IN ('APV-A','APV-B') AND "Sexo" = 'F' THEN "CargoMiles" ELSE 0 END),26,8) AS "MontoTotalTraspasosRealizados_Femenino"
    FROM a
    WHERE a."IdPlanTipoInversion" = 1
    GROUP BY a."IdPlanTipoInversion", a."NombrePlan") 
UNION ALL 
(SELECT
    to_bigint(a."IdPlanTipoInversion") AS "NumeroPolizaRunFondoCodigoPlan",
    a."NombrePlan" AS "Serie",
     --NumeroCuentasApvVigentes
    to_bigint(:CantCuentas_A_M_2) AS "NumeroCuentasApvVigentes_LetraA_Masculino",
    to_bigint(:CantCuentas_A_F_2) AS "NumeroCuentasApvVigentes_LetraA_Femenino",
    to_bigint(:CantCuentas_T_M_2) AS "NumeroCuentasApvVigentes_Total_Masculino",
    to_bigint(:CantCuentas_T_F_2) AS "NumeroCuentasApvVigentes_Total_Femenino",
    --NumeroAseguradosParticipesAportantesClientesPlan
    to_bigint(:CantClientes_A_M_2) AS "NumeroAseguradosParticipesAportantesClientesPlan_LetraA_Masculino",
    to_bigint(:CantClientes_A_F_2) AS "NumeroAseguradosParticipesAportantesClientesPlan_LetraA_Femenino",
    to_bigint(:CantClientes_T_M_2) AS "NumeroAseguradosParticipesAportantesClientesPlan_Total_Masculino",
    to_bigint(:CantClientes_T_F_2) AS "NumeroAseguradosParticipesAportantesClientesPlan_Total_Femenino",
    --SaldoTotalAcumulado
    (TO_DECIMAL(:SaldoAcumulado_A_M_2,26,8) / 1000) AS "SaldoTotalAcumulado_LetraA_Masculino",
    (TO_DECIMAL(:SaldoAcumulado_A_F_2,26,8) / 1000) AS "SaldoTotalAcumulado_LetraA_Femenino",
    (TO_DECIMAL(:SaldoAcumulado_B_M_2,26,8) / 1000) AS "SaldoTotalAcumulado_LetraB_Masculino",
    (TO_DECIMAL(:SaldoAcumulado_B_F_2,26,8) / 1000) AS "SaldoTotalAcumulado_LetraB_Femenino",
    --Bonificacion
    (TO_DECIMAL(:SaldoAcumulado_BF_M_2,26,8) / 1000) AS "Bonificacion_LetraA_Masculino",
    (TO_DECIMAL(:SaldoAcumulado_BF_F_2,26,8) / 1000) AS "Bonificacion_LetraA_Femenino",
    --NumeroDepositos
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" = 'APV-A' AND "Sexo" = 'M' THEN 1 ELSE NULL END)) AS "NumeroDepositos_LetraA_Masculino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" = 'APV-A' AND "Sexo" = 'F' THEN 1 ELSE NULL END)) AS "NumeroDepositos_LetraA_Femenino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" = 'APV-B' AND "Sexo" = 'M' THEN 1 ELSE NULL END)) AS "NumeroDepositos_LetraB_Masculino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" = 'APV-B' AND "Sexo" = 'F' THEN 1 ELSE NULL END)) AS "NumeroDepositos_LetraB_Femenino",
    --MontoTotalDepositos
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" = 'APV-A' AND "Sexo" = 'M' THEN "AbonoMiles" ELSE 0 END),26,8) AS "MontoTotalDepositos_LetraA_Masculino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" = 'APV-A' AND "Sexo" = 'F' THEN "AbonoMiles" ELSE 0 END),26,8) AS "MontoTotalDepositos_LetraA_Femenino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" = 'APV-B' AND "Sexo" = 'M' THEN "AbonoMiles" ELSE 0 END),26,8) AS "MontoTotalDepositos_LetraB_Masculino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" = 'APV-B' AND "Sexo" = 'F' THEN "AbonoMiles" ELSE 0 END),26,8) AS "MontoTotalDepositos_LetraB_Femenino",
    --NumeroTraspasosRecibidos
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" IN ('APV-A','APV-B') AND "Sexo" = 'M' THEN 1 ELSE NULL END)) AS "NumeroTraspasosRecibidos_Masculino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" IN ('APV-A','APV-B') AND "Sexo" = 'F' THEN 1 ELSE NULL END)) AS "NumeroTraspasosRecibidos_Femenino",
    --MontoTotalTraspasosRecibidos
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" IN ('APV-A','APV-B') AND "Sexo" = 'M' THEN "AbonoMiles" ELSE 0 END),26,8) AS "MontoTotalTraspasosRecibidos_Masculino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVABN' AND "Codigo" IN ('APV-A','APV-B') AND "Sexo" = 'F' THEN "AbonoMiles" ELSE 0 END),26,8) AS "MontoTotalTraspasosRecibidos_Femenino",
    --NumeroRetiros
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" = 'APV-A' AND "Sexo" = 'M' THEN 1 ELSE NULL END)) AS "NumeroRetiros_LetraA_Masculino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" = 'APV-A' AND "Sexo" = 'F' THEN 1 ELSE NULL END)) AS "NumeroRetiros_LetraA_Femenino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" = 'APV-B' AND "Sexo" = 'M' THEN 1 ELSE NULL END)) AS "NumeroRetiros_LetraB_Masculino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" = 'APV-B' AND "Sexo" = 'F' THEN 1 ELSE NULL END)) AS "NumeroRetiros_LetraB_Femenino",
    --MontoTotalRetiros
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" = 'APV-A' AND "Sexo" = 'M' THEN "CargoMiles" ELSE 0 END),26,8) AS "MontoTotalRetiros_LetraA_Masculino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" = 'APV-A' AND "Sexo" = 'F' THEN "CargoMiles" ELSE 0 END),26,8) AS "MontoTotalRetiros_LetraA_Femenino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" = 'APV-B' AND "Sexo" = 'M' THEN "CargoMiles" ELSE 0 END),26,8) AS "MontoTotalRetiros_LetraB_Masculino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" = 'APV-B' AND "Sexo" = 'F' THEN "CargoMiles" ELSE 0 END),26,8) AS "MontoTotalRetiros_LetraB_Femenino",
    --NumeroTraspasosRealizados
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" IN ('APV-A','APV-B') AND "Sexo" = 'M' THEN 1 ELSE NULL END)) AS "NumeroTraspasosRealizados_Masculino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" IN ('APV-A','APV-B') AND "Sexo" = 'F' THEN 1 ELSE NULL END)) AS "NumeroTraspasosRealizados_Femenino",
    --MontoTotalTraspasosRealizados
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" IN ('APV-A','APV-B') AND "Sexo" = 'M' THEN "CargoMiles" ELSE 0 END),26,8) AS "MontoTotalTraspasosRealizados_Masculino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVCAR' AND "Codigo" IN ('APV-A','APV-B') AND "Sexo" = 'F' THEN "CargoMiles" ELSE 0 END),26,8) AS "MontoTotalTraspasosRealizados_Femenino"
    FROM a
    WHERE a."IdPlanTipoInversion" = 2
    GROUP BY a."IdPlanTipoInversion", a."NombrePlan");
