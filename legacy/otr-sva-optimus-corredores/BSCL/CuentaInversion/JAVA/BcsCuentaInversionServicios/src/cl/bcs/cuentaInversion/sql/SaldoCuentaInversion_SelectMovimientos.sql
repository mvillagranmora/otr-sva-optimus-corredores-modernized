SELECT   
c."IdCuentaInversion"
,COALESCE(c."FechaSistema",TO_DATE(:FechaHasta)) as "FechaSistema"
,COALESCE(c."FechaCertificacion",TO_TIMESTAMP(:FechaHasta)) as "FechaCertificacion"
,COALESCE(c."FechaIngresoRegistro",TO_TIMESTAMP(:FechaHasta)) as "FechaIngresoRegistro"
,COALESCE(c."FechaEfectivaMovimiento",TO_DATE(:FechaHasta)) as "FechaEfectivaMovimiento"
,COALESCE(c."IdCargoAbonoMovimiento",0) as "IdCargoAbonoMovimiento"
,c."IdTipoMovimiento"
,c."IdEstado"
,c."Saldo"
,c."SaldoPorFechaMovimiento"
,CASE WHEN c."IdEstado" = 'CRT' THEN (CASE WHEN c."IdTipoMovimiento" = 'MOVCAR' THEN  -1*c."Monto" ELSE c."Monto" END) ELSE 0 END AS "Monto"
,CASE WHEN c."IdEstado" = 'PEN' AND c."IdTipoMovimiento" = 'MOVCAR' THEN c."Monto" ELSE 0 END AS "MontoCargoPendiente"
,CASE WHEN c."IdEstado" = 'PEN' AND c."IdTipoMovimiento" = 'MOVABN' THEN c."Monto" ELSE 0 END AS "MontoAbonoPendiente"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as c
INNER JOIN "BSCL.CuentaInversion.BD::GBL.SaldoCuentaInversion_Proceso_Paso" p ON p."IdCuentaInversion" = c."IdCuentaInversion"
WHERE c."IdEstado" IN ('CRT','PEN') 
AND ((COALESCE(c."FechaEfectivaMovimiento",TO_DATE(:FechaHasta)) BETWEEN :FechaDesde AND :FechaHasta) OR (COALESCE(c."FechaSistema",TO_DATE(:FechaHasta)) BETWEEN :FechaDesde AND :FechaHasta))
AND p."IdTrabajo" = :IdTrabajo;