SELECT DOC."IdCliente", ANX."IdAnexo", ANX."IdEstado", TPO."BloquearAlVencer", CFUMC."FechaUltimaModificacion" AS "FechaModificacion" FROM "BSCL.Plataforma.BD::DOC.TipoDocumento" TPO
INNER JOIN "BSCL.Plataforma.BD::DOC.Documento" DOC ON TPO."IdTipoDocumento" = DOC."IdTipoDocumento"
INNER JOIN "BSCL.Plataforma.BD::DOC.Anexo" ANX ON ANX."IdDocumento" = DOC."IdDocumento" AND ANX."IdAnexo" = DOC."IdUltimoAnexo" AND ANX."IdEstado" != 'VCD' AND ANX."IdEstado" NOT IN ('VCD','PEN')
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = DOC."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = CLI."IdPortafolioDefecto"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Area" AREA ON AREA."Codigo" = POR."AreaCliente"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarFechaUltimaModificacionCliente" CFUMC ON CLI."IdCliente" = CFUMC."IdCliente"
WHERE AREA."BloquearAlVencer" = 'S'