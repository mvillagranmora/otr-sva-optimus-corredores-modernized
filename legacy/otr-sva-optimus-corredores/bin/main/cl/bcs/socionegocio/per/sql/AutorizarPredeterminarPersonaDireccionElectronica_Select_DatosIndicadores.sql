SELECT
    POR."IdPortafolio" AS "IdPortafolio",
    CLI."IdTipoCliente" AS "IdTipoCliente",
    TI."IdTipoIndicador" AS "IdTipoIndicador",
    DIRE."IdDireccionElectronica" AS "IdDireccionElectronica"
FROM "BSCL.Portafolio.BD::POR.Portafolio" POR 
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI 
    ON CLI."IdCliente" = POR."IdCliente"
INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TI 
    ON TI."CodigoTipoIndicador" IN ('ORDMAILNTF', 'CAR_DIRELE', 'FAC_DIRELE')
INNER JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DIRE 
    ON DIRE."IdPersona" = CLI."IdPersona"
WHERE CLI."IdPersona" = :IdPersona
    AND DIRE."Predeterminado" = 'S'
