SELECT
cta."IdPortafolio"
FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion" cta
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" p on cta."IdPortafolio" = p."IdPortafolio"
GROUP BY cta."IdPortafolio"

