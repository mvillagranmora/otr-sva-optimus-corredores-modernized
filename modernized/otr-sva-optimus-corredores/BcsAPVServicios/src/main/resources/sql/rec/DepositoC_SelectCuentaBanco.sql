 SELECT "IdCuentaBanco", "IdBanco", "NumeroCuentaBancaria" from "BSCL.Tesoreria.BD::TES.CuentaBanco" WHERE "IdBanco" IN (:IdsBanco) AND "NumeroCuentaBancaria" IN (:NumerosCuentaBancaria)
