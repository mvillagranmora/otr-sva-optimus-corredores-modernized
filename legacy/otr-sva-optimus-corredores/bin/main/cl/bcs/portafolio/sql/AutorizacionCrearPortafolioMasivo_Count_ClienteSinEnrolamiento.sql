-- Contar clientes sin enrolamiento autorizado
SELECT COUNT(1) AS "Contador"
FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente"
WHERE POR."IdPortafolio" IN (:IdPortafolios)
  AND CLI."EsEnrolado" != 'S'
