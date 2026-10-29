SELECT "IdCuentaCustodioPortafolio","CodigoBancoCustodio","CodigoBancoLiquidador","IdPortafolio"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio"
WHERE "CodigoBancoCustodio" IN (:BancoCustodio) AND "CodigoBancoLiquidador" IN (:BancoLiquida) AND "IdPortafolio" IN (:IdPortafolio)