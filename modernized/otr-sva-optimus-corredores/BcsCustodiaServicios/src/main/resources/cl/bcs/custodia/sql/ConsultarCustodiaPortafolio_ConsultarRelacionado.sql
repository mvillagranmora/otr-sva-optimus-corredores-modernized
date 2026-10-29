SELECT CLI."IdPersona" AS "IdPersonaDestino" 
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND ON IND."IdEntidad" = CLI."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TPO ON IND."IdTipoIndicador" = TPO."IdTipoIndicador"
WHERE TPO."IdTipoEntidad" = 'CLIENTE' AND TPO."CodigoTipoIndicador"  = 'RELCORR' AND IND."ValorIndicador" = 'S';