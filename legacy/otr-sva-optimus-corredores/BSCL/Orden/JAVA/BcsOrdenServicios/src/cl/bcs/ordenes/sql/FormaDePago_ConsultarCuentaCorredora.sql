SELECT "IdCuentaBanco" AS "IdCuentaBancariaCorredora", "NombreCuentaBancaria"
FROM BSCL."BSCL.Tesoreria.BD::TES.CuentaBanco"
WHERE "NombreCuentaBancaria" IN (:banco);