SELECT
 CLI."IdCliente",
 DOCI."DocumentoIdentidad",
 SOC."IdSocioDeNegocio",
 POR."CodigoPortafolio",
 POR."IdPortafolio",
 TPO."CodigoTipoIndicador",
 IFNULL(IND."ValorIndicador",TPO."ValorIndicadorDefecto") as "ValorIndicador"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCI
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = DOCI."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdPersona" = PER."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON SOC."IdPersona" = PER."IdPersona"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS IND ON IND."IdEntidad" = POR."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" AS TPO ON TPO."IdTipoIndicador" = IND."IdTipoIndicador"
WHERE DOCI."DocumentoIdentidad" = :DocumentoIdentidad
AND TPO."IdTipoEntidad" = 'PORTAFOLIO'
AND TPO."IdUso" = 'PRE'
AND TPO."IdEstado" = 'ACT'
AND TPO."IdEmpresa" = 1
AND TPO."CodigoTipoIndicador" = 'GEN_OCCINV'
AND "ValorIndicador" = 'S'
AND DOCI."Predeterminado" = 'S'
ORDER BY POR."CodigoPortafolio" ASC;