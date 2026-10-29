SELECT SOC."IdSocioDeNegocio", PER."IdPersona", PER."IdDireccionElectronicaPrincipal"
FROM "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (SOC."IdPersona" = PER."IdPersona")
WHERE PER."DocumentoIdentidad" = :DocumentoIdentidad
AND PER."TipoDocumentoIdentidad" = :IdTipoDocumentoIdentidad
AND SOC."IdEmpresa" = :IdEmpresa
