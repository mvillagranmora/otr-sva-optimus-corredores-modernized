SELECT
    POR."IdPortafolio" AS "IdPortafolio",
    CLI."IdTipoCliente" AS "IdTipoCliente",
    TI."IdTipoIndicador" AS "IdTipoIndicador",
    DIRF."IdDireccionFisica" AS "IdDireccionFisica"
FROM "BSCL.Portafolio.BD::POR.Portafolio" POR 
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI 
    ON CLI."IdCliente" = POR."IdCliente"
INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TI 
    ON TI."CodigoTipoIndicador" IN ('GEN_CODDIR', 'FAC_DIRFIS')
INNER JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" DIRF 
    ON DIRF."IdPersona" = CLI."IdPersona"
WHERE CLI."IdPersona" = :IdPersona
    AND DIRF."Predeterminado" = 'S'
