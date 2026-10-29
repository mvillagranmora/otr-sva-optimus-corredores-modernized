SELECT DISTINCT CIN."IdPortafolio" AS "Valor5"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarMovimientosCuentaInversion" CIN
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR 
ON CIN."IdPortafolio" = POR."IdPortafolio"
WHERE POR."IdPortafolio" IS NULL;