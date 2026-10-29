SELECT
    CCP."IdBoveda",
    CCP."Codigo",
    CCP."IdTipoCuentaCustodia",
    CCP."IdTipoCustodia",
    CCP."IdModeloCustodia",
    CCP."IdPersonaBancoCustodio",
    CCP."IdPersonaBancoLiquidador",
    CCP."IdEstado",
    CCP."LiquidaVencimientos",
    CCP."LiquidaCupones",
    CCP."LiquidaPrepagos",
    CCP."LiquidaDividendos"
FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCP
WHERE CCP."IdPortafolio" = :IdPortafolio
AND CCP."IdEstado" = 'ACT'
