SELECT   
TP."IdCuentaInversion"
,COALESCE(c."FechaSistema",TO_DATE(:p_FechaConsulta)) as "FechaSistema"
,COALESCE(c."FechaEfectivaMovimiento",TO_DATE(:p_FechaConsulta)) as "FechaEfectivaMovimiento"
,SUM(CASE WHEN c."IdEstado" = 'CRT' THEN (CASE WHEN c."IdTipoMovimiento" = 'MOVCAR' THEN  -1*c."Monto" ELSE c."Monto" END) ELSE 0 END) AS "Monto"
,TP."IdPortafolio"
,TP."IdMoneda"
FROM #PatrimonioCaja_CobroComisionTablaPaso AS TP
left join "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as c on c."IdCuentaInversion" = TP."IdCuentaInversion"
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CobroComisionExclusionConcepto" AS CCEC ON CCEC."IdConcepto" = c."IdConcepto"
WHERE c."IdEstado"  = 'CRT'  AND COALESCE(CCEC."IdExclusionConcepto", 0) = 0 
AND ( c."FechaEfectivaMovimiento" = :p_FechaConsulta OR c."FechaSistema" = :p_FechaConsulta )
group  by TP."IdCuentaInversion", c."FechaSistema",c."FechaEfectivaMovimiento", TP."IdPortafolio",TP."IdMoneda";