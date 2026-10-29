SELECT "IdCliente","IdPortafolio","NombreArchivo","CertificadoEncoded" AS "CertificadoBytes" FROM "BSCL.Normativa.BD::TRI.CertificadoClienteOffline"
where "IdEmpresa" = :IdEmpresa and "IdCliente" = :IdCliente and "IdPortafolio" = :IdPortafolio and "IdTipoCertificado" = :IdTipoCertificado
