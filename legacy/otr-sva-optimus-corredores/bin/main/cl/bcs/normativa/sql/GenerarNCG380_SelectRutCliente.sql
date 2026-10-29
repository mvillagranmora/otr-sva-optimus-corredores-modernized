SELECT TOP 1 "IdPersona" FROM "BSCL.SocioNegocio.BD.Vistas.UI::ConsultarSocioDeNegocio"
WHERE UPPER("DocumentoIdentidadDefecto") = UPPER(:DocumentoIdentidad);