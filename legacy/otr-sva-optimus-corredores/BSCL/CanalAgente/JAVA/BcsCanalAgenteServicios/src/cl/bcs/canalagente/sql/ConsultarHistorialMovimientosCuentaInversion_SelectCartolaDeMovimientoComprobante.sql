SELECT 
chm."IdCargoAbonoMovimiento",
TO_VARCHAR(fac."FolioComprobante") AS "FolioComprobante",
por."CodigoPortafolio",
fac."IdRespuestaDTE",
fac."RespuestaPdfDTE",
ident."DocumentoIdentidad"
FROM "BSCL.CanalAgente.BD::ConsultarHistorialMovimientosCuentaInversion_Proceso" chm
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" por ON por."IdPortafolio" = chm."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" cli ON cli."IdCliente" = por."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS per ON per."IdPersona" = cli."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as ident ON per."IdDocumentoIdentidad" = ident."IdDocumentoIdentidad"
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS fac ON fac."IdComprobante" = chm."IdComprobante";