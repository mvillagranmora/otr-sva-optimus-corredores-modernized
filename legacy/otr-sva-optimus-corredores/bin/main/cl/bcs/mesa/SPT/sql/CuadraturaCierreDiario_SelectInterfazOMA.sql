SELECT 
"IdLogInforme"
,0 AS "FolioOperacion"
,'' AS "RazonSocial"
,TO_DECIMAL(0) AS "MontoOperacion"
,TO_DECIMAL(0) AS "MontoOperacionMoneda"
,'' AS "IdMoneda"
,'' AS "IdMonedaLiquidacion"
,TO_BIGINT(0) AS "IdMesaOrigen"
,'VIG' AS "IdEstado"
FROM "BSCL.Mesa.BD::SPT.LogInforme"
WHERE TO_VARCHAR("FechaEjecucion",'YYYY-MM-DD') = :Fecha 
AND "IdEmpresa" = :IdEmpresa