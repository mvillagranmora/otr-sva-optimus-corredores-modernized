-- Actualizar Cliente
-- Cambia Estado de Cliente solo si esta pendiente ('P' o 'N')
UPDATE "BSCL.SocioNegocio.BD::CLI.Cliente"
SET "EsEnrolado" = :EsEnrolado
WHERE "IdCliente" = :IdCliente
AND "EsEnrolado" IN ('P', 'N')
