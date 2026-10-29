SELECT "IdCuentaInversion","SaldoAnterior","FechaAnterior","SaldoCargoPendienteAnterior","SaldoAbonoPendienteAnterior"
FROM (SELECT

   
    row_number() OVER (partition by B."IdCuentaInversion" order BY B."FechaSistema" DESC, B."IdCargoAbonoMovimiento" DESC) AS "Row"
	,B."IdCuentaInversion"
	,B."SaldoPorFechaMovimiento" AS "SaldoAnterior"
	,R."FechaAnterior"
	,R."MontoCargoPendiente" AS "SaldoCargoPendienteAnterior"
	,R."MontoAbonoPendiente" AS "SaldoAbonoPendienteAnterior"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" B
INNER JOIN (SELECT   
c."IdCuentaInversion"
,MAX(CASE WHEN c."IdEstado" = 'CRT' THEN c."FechaSistema" END) as "FechaAnterior"
,SUM(CASE WHEN c."IdEstado" = 'PEN' AND c."IdTipoMovimiento" = 'MOVCAR' THEN c."Monto" ELSE 0 END) AS "MontoCargoPendiente"
,SUM(CASE WHEN c."IdEstado" = 'PEN' AND c."IdTipoMovimiento" = 'MOVABN' THEN c."Monto" ELSE 0 END) AS "MontoAbonoPendiente"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as c 
INNER JOIN "BSCL.CuentaInversion.BD::GBL.SaldoCuentaInversion_Proceso_Paso" p ON p."IdCuentaInversion" = c."IdCuentaInversion"
WHERE c."IdEstado" IN ('CRT','PEN') 
AND COALESCE(c."FechaSistema",TO_DATE(:FechaHasta)) < :FechaDesde
AND p."IdTrabajo" = :IdTrabajo
GROUP BY c."IdCuentaInversion") AS R ON 
(
	B."FechaSistema" = R."FechaAnterior"
	AND B."IdCuentaInversion" = R."IdCuentaInversion"
)
WHERE B."IdEstado" = 'CRT') 
WHERE "Row" = 1;