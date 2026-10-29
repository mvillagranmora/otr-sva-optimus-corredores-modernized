SELECT COUNT(1) AS "Contador"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioDatosPlantilla"
WHERE "IdCliente" = :IdCliente
AND "IdPortafolio" <> :IdPortafolio
AND "PorDefecto" = 'S'
