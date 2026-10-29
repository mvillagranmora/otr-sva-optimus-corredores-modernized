SELECT 
CB."IdTipoCuentaBancaria" AS "IdTipoCuentaBancariaSocio"
,CB."NumeroCuentaBancaria" AS "NumeroCuentaBancariaSocio"
,CB."IdCuentaBancaria" AS "IdCuentaBancariaSocio"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" AS CB
INNER JOIN "BSCL.APV.BD::APV.OrigenDestinoTransicion" AS ODT 
ON ODT."Operacion" = 'DIS' AND ODT."SocioNegocioCtaBco" = CB."IdCuentaBancaria"
AND ODT."IdEmpresa"=:IdEmpresa;
