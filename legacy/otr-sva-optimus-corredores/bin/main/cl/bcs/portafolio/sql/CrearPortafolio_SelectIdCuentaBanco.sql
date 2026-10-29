SELECT TOP 1 "IdCuentaBanco"
FROM "BSCL.Tesoreria.BD::TES.CuentaBanco"
WHERE "Default" = 'S' AND "IdMoneda" = :monedaSistema
ORDER BY "APV" DESC;