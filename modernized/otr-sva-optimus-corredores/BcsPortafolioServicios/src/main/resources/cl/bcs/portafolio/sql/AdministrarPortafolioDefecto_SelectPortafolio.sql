SELECT "IdPortafolioConfiguracionDefecto", "CodigoPortafolio", "Proposito"
FROM BSCL."BSCL.Portafolio.BD::POR.PortafolioConfiguracionDefecto"
WHERE "CodigoPortafolio" IN (:CodigosPortafolio)
