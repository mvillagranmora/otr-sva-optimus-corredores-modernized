SELECT   
p."IdPortafolio"
,COUNT(1) AS "CantidadMovimientos"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as c 
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" p ON p."IdCuentaInversion" = c."IdCuentaInversion"
WHERE p."IdPortafolio" IN (:IdPortafolio) AND c."IdEstado" IN ('CRT','PEN') 
AND ((COALESCE(c."FechaSistema",TO_DATE(:FechaHasta)) BETWEEN :FechaDesde AND :FechaHasta))
GROUP BY p."IdPortafolio";