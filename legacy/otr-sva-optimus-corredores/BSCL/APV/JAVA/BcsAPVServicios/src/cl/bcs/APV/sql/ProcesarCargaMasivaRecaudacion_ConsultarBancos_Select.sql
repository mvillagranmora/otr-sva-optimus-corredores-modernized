
SELECT 
 "IdBanco" 
,TRIM("NombreBanco") AS "NombreBanco"
,TRIM("NumeroCuentaBancaria") AS "NumeroCuentaBancaria"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias"
WHERE TRIM("NombreBanco") IN(:NombreBanco)
AND   TRIM("NumeroCuentaBancaria") IN(:NumeroCuenta)