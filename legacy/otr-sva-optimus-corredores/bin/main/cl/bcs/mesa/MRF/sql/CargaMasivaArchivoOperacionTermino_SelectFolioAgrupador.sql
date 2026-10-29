SELECT
"FolioAgrupadorCartera"
FROM "BSCL.Mesa.BD::CAR.CargaMasivaDetalle"
WHERE 
"FolioAgrupadorCartera" IN (:FolioAgrupadorCartera)
AND "IdEstado" != 'INA'
GROUP BY "FolioAgrupadorCartera";