SELECT Bancaria."IdCuentaBancaria" AS "IdCuentaBancaria" 
FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS Bancaria
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente ON Cliente."IdPersona" = Bancaria."IdPersona"
WHERE Cliente."IdCliente" = :IdCliente AND Bancaria."IdCuentaBancaria" = :IdCuentaBanco
