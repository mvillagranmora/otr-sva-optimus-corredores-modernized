SELECT PER."IdPersona", DI."DocumentoIdentidad" FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SN ON EMP."IdSocioDeNegocio" = SN."IdSocioDeNegocio" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON SN."IdPersona" = PER."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON DI."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad" 
WHERE EMP."IdEmpresa" = :IdEmpresa