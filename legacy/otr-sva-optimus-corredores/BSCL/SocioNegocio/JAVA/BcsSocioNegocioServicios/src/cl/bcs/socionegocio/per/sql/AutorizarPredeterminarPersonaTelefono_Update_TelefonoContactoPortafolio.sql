UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET "IdTelefonoContacto" = (
    SELECT TEL."IdTelefono"
    FROM "BSCL.SocioNegocio.BD::PER.Telefono" TEL
    WHERE TEL."IdPersona" = :IdPersona
      AND TEL."Predeterminado" = 'S'
    LIMIT 1
)
WHERE "IdCliente" IN (
    SELECT CLI."IdCliente"
    FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
    WHERE CLI."IdPersona" = :IdPersona
)
