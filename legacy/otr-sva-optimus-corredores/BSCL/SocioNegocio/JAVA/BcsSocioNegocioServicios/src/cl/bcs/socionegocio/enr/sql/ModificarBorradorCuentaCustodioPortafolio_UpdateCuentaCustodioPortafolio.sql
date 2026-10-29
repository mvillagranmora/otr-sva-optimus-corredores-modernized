UPDATE "BSCL.SocioNegocio.BD::ENR.CuentaCustodioPortafolio"
SET 
"IdBoveda" = :IdBoveda
,"Codigo" = :Codigo
,"IdModeloCustodia" = :IdModeloCustodia
,"IdTipoCustodia" = :IdTipoCustodia
,"IdTipoCuentaCustodia" = :IdTipoCuentaCustodia
,"LiquidaDividendos" = :LiquidaDividendos
,"LiquidaCupones" = :LiquidaCupones
,"LiquidaPrepagos" = :LiquidaPrepagos
,"IdPersonaBancoCustodio" = :IdPersonaBancoCustodio
,"IdPersonaBancoLiquidador" = :IdPersonaBancoLiquidador
,"LiquidaVencimientos" = :LiquidaVencimientos
WHERE "IdCuentaCustodioPortafolio" = :IdCuentaCustodioPortafolio