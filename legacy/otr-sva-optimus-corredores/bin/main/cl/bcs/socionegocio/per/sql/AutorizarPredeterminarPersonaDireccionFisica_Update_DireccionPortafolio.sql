UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET "IdDireccionEnvioCorrespondencia" = :IdDireccionFisica
WHERE "IdCliente" IN (
    SELECT "IdCliente"
    FROM "BSCL.SocioNegocio.BD::CLI.Cliente"
    WHERE "IdPersona" = :IdPersona
)
