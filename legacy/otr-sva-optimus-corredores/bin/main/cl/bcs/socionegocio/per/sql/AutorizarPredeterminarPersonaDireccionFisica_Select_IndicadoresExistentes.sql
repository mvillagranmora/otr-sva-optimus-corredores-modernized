SELECT 
    COUNT(IND."IdIndicador") AS "Contador"
FROM "BSCL.Plataforma.BD::IND.Indicador" IND
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR 
    ON IND."IdEntidad" = POR."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI 
    ON CLI."IdCliente" = POR."IdCliente"
WHERE CLI."IdPersona" = :IdPersona
    AND IND."IdTipoIndicador" IN (
        SELECT "IdTipoIndicador"
        FROM "BSCL.Plataforma.BD::IND.TipoIndicador"
        WHERE "CodigoTipoIndicador" IN ('GEN_CODDIR', 'FAC_DIRFIS')
    )
