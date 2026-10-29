SELECT 
	CB ."IdCuentaBancaria" ,
	CB ."IdBanco" ,
	CB ."IdTipoCuentaBancaria"    AS "CodigoTipoCuentaBancaria",
	CB."NumeroCuentaBancaria"     AS "NumeroCuentaBancaria",
	CB."IdMoneda"                 AS "IdMoneda",
	CB."IdPlaza"                  AS "CodigoPlaza",
	CB."EsPac"                    AS "PAC",
	CB."Predeterminado"			  AS "Predeterminado"
FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CB 
WHERE CB ."IdPersona"  IN (:IdPersona);