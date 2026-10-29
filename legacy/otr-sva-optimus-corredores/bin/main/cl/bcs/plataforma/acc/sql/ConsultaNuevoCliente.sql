
SELECT TOP 1 CLI."IdPersona" AS IdPersona,
--SELECT TOP 1 COALESCE(CLI."IdPersona",EMP."IdPersona")AS IdPersona,
PER."IdDireccionElectronicaPrincipal",
PER."Nombres",
PER."ApellidoPaterno",
PER."ApellidoMaterno",
PER."RazonSocial",
PER."TipoDocumentoIdentidad",
PER."DocumentoIdentidad",
TEL."Numero",
CLI."IdEmpresa",
MAIL."Direccion",
DTOS."RazonSocial" AS corredora

FROM "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" CLI
--LEFT OUTER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" EMP ON USU."TipoEntidad" = 'EMP' 
--AND USU."IdEntidad" = EMP."IdEmpleado"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON PER."IdPersona" = CLI."IdPersona"
--OR PER."IdPersona" = EMP."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" TEL ON TEL."IdPersona" = CLI."IdPersona"
--OR TEL."IdPersona" = EMP."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" MAIL ON MAIL."IdPersona" = CLI."IdPersona"
--OR MAIl."IdPersona" = EMP."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" DTOS ON DTOS."IdEmpresa" = CLI."IdEmpresa"
--OR DTOS."IdEmpresa" = CLI."IdEmpresa"
WHERE CLI."IdCliente" = :p_IdCliente