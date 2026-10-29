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
        CASE WHEN cir."IdTipoMovimiento" = 'MOVABN' THEN cir."Monto" ELSE COALESCE(cir."Monto" * -1,0) END AS "Monto",
        CASE WHEN cir."IdTipoMovimiento" = 'MOVCAR' THEN cir."Monto" ELSE 0 END AS "Cargo",
        CASE WHEN cir."IdTipoMovimiento" = 'MOVABN' THEN cir."Monto" ELSE 0 END AS "Abono",
        (CASE WHEN cir."IdTipoMovimiento" = 'MOVABN' THEN cir."Monto" ELSE COALESCE(cir."Monto" * -1,0) END) / 1000 AS "MontoMiles",
        CASE WHEN cir."IdTipoMovimiento" = 'MOVCAR' THEN cir."Monto" / 1000 ELSE 0 END AS "CargoMiles",
        CASE WHEN cir."IdTipoMovimiento" = 'MOVABN' THEN cir."Monto" / 1000 ELSE 0 END AS "AbonoMiles",
        cir."IdMatrizAgrupadorAPV",cir."IdPlanTipoInversion",
         CASE WHEN cir."NombrePlan" = 'A. ABIERTO' THEN 'ARQUITECTURA ABIERTA'
        	  WHEN cir."NombrePlan" = 'I. ACTIVO' THEN 'INVERSION ACTIVA' END AS "NombrePlan",
        cir."Sexo",cir."Codigo"
     FROM "BSCL.Normativa.BD::CIR.DatosEstadisticaCircular1981" cir
     INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" por ON por."IdPortafolio" = cir."IdPortafolio" AND ( por."IdEstado" = 'ACT' OR (por."IdEstado" = 'BLO'AND por."IdTipoBloqueo" ='BMUERT'))
     WHERE cir."SecuenciaSolicitud" = :SecuenciaSolicitud
     AND cir."Codigo" = 'DC'
)
(SELECT
    to_bigint("IdPlanTipoInversion") AS "NumeroPolizaRunFondoCodigoPlan","NombrePlan" AS "Serie",
    --NumeroCuentasApvVigentes
    to_bigint(:CantCuentas_DC_M_1) AS "NumeroCuentasApvVigentes_Masculino",
    to_bigint(:CantCuentas_DC_F_1) AS "NumeroCuentasApvVigentes_Femenino",
    --NumeroAseguradosParticipesAportantesClientesPlan
    to_bigint(:CantClientes_DC_M_1) AS "NumeroAseguradosParticipesAportantesClientesPlan_Masculino",
    to_bigint(:CantClientes_DC_F_1) AS "NumeroAseguradosParticipesAportantesClientesPlan_Femenino",
    --SaldoTotalAcumulado
    TO_DECIMAL(:SaldoAcumulado_DC_M_1 / 1000,26,8) AS "SaldoTotalAcumulado_Masculino",
    TO_DECIMAL(:SaldoAcumulado_DC_F_1 / 1000,26,8) AS "SaldoTotalAcumulado_Femenino",
    --NumeroDepositos
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Sexo" = 'M' THEN 1 ELSE NULL END)) AS "NumeroDepositos_Masculino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Sexo" = 'F' THEN 1 ELSE NULL END)) AS "NumeroDepositos_Femenino",
    --MontoTotalDepositos
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Sexo" = 'M' THEN "AbonoMiles" ELSE 0 END),26,8) AS "MontoTotalDepositos_Masculino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Sexo" = 'F' THEN "AbonoMiles" ELSE 0 END),26,8) AS "MontoTotalDepositos_Femenino",
    --NumeroTraspasosRecibidos
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVABN' AND "Sexo" = 'M' THEN 1 ELSE NULL END)) AS "NumeroTraspasosRecibidos_Masculino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVABN' AND "Sexo" = 'F' THEN 1 ELSE NULL END)) AS "NumeroTraspasosRecibidos_Femenino",
    --MontoTotalTraspasosRecibidos
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVABN' AND "Sexo" = 'M' THEN "AbonoMiles" ELSE 0 END),26,8) AS "MontoTotalTraspasosRecibidos_Masculino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVABN' AND "Sexo" = 'F' THEN "AbonoMiles" ELSE 0 END),26,8) AS "MontoTotalTraspasosRecibidos_Femenino",
    --NumeroTraspasosRealizados
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVCAR' AND "Sexo" = 'M' THEN 1 ELSE NULL END)) AS "NumeroTraspasosRealizados_Masculino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVCAR' AND "Sexo" = 'F' THEN 1 ELSE NULL END)) AS "NumeroTraspasosRealizados_Femenino",
    --MontoTotalTraspasosRealizados
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVCAR' AND "Sexo" = 'M' THEN "CargoMiles" ELSE 0 END),26,8) AS "MontoTotalTraspasosRealizados_Masculino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVCAR' AND "Sexo" = 'F' THEN "CargoMiles" ELSE 0 END),26,8) AS "MontoTotalTraspasosRealizados_Femenino"

    FROM a WHERE "IdPlanTipoInversion"=1
    GROUP BY "IdPlanTipoInversion","NombrePlan") UNION ALL 
(SELECT
    to_bigint("IdPlanTipoInversion") AS "NumeroPolizaRunFondoCodigoPlan","NombrePlan" AS "Serie",
    --NumeroCuentasApvVigentes
    to_bigint(:CantCuentas_DC_M_2) AS "NumeroCuentasApvVigentes_Masculino",
    to_bigint(:CantCuentas_DC_F_2) AS "NumeroCuentasApvVigentes_Femenino",
    --NumeroAseguradosParticipesAportantesClientesPlan
    to_bigint(:CantClientes_DC_M_2) AS "NumeroAseguradosParticipesAportantesClientesPlan_Masculino",
    to_bigint(:CantClientes_DC_F_2) AS "NumeroAseguradosParticipesAportantesClientesPlan_Femenino",
    --SaldoTotalAcumulado
    TO_DECIMAL(:SaldoAcumulado_DC_M_2 / 1000,26,8) AS "SaldoTotalAcumulado_Masculino",
    TO_DECIMAL(:SaldoAcumulado_DC_F_2 / 1000,26,8) AS "SaldoTotalAcumulado_Femenino",
    --NumeroDepositos
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Sexo" = 'M' THEN 1 ELSE NULL END)) AS "NumeroDepositos_Masculino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Sexo" = 'F' THEN 1 ELSE NULL END)) AS "NumeroDepositos_Femenino",
    --MontoTotalDepositos
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Sexo" = 'M' THEN "AbonoMiles" ELSE 0 END),26,8) AS "MontoTotalDepositos_Masculino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'N' AND "IdTipoMovimiento" = 'MOVABN' AND "Sexo" = 'F' THEN "AbonoMiles" ELSE 0 END),26,8) AS "MontoTotalDepositos_Femenino",
    --NumeroTraspasosRecibidos
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVABN' AND "Sexo" = 'M' THEN 1 ELSE NULL END)) AS "NumeroTraspasosRecibidos_Masculino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVABN' AND "Sexo" = 'F' THEN 1 ELSE NULL END)) AS "NumeroTraspasosRecibidos_Femenino",
    --MontoTotalTraspasosRecibidos
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVABN' AND "Sexo" = 'M' THEN "AbonoMiles" ELSE 0 END),26,8) AS "MontoTotalTraspasosRecibidos_Masculino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVABN' AND "Sexo" = 'F' THEN "AbonoMiles" ELSE 0 END),26,8) AS "MontoTotalTraspasosRecibidos_Femenino",
    --NumeroTraspasosRealizados
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVCAR' AND "Sexo" = 'M' THEN 1 ELSE NULL END)) AS "NumeroTraspasosRealizados_Masculino",
    to_bigint(COUNT(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVCAR' AND "Sexo" = 'F' THEN 1 ELSE NULL END)) AS "NumeroTraspasosRealizados_Femenino",
    --MontoTotalTraspasosRealizados
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVCAR' AND "Sexo" = 'M' THEN "CargoMiles" ELSE 0 END),26,8) AS "MontoTotalTraspasosRealizados_Masculino",
    TO_DECIMAL(SUM(CASE WHEN "EsTraspaso" = 'S' AND "IdTipoMovimiento" = 'MOVCAR' AND "Sexo" = 'F' THEN "CargoMiles" ELSE 0 END),26,8) AS "MontoTotalTraspasosRealizados_Femenino"

    FROM a WHERE "IdPlanTipoInversion"=2
    GROUP BY "IdPlanTipoInversion","NombrePlan") ;