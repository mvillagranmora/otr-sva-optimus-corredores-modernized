SELECT S."IdPortafolio" AS "Valor5"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarSaldoFechaEfectivaAnalitica" S
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON S."IdPortafolio" = POR."IdPortafolio"
WHERE POR."IdPortafolio" IS NULL