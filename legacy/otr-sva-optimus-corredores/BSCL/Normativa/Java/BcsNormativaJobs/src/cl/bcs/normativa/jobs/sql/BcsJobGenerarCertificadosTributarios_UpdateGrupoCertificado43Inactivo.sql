UPDATE C SET C."IdEstado"=:IdEstado
FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" C
WHERE C."IdTipoCertificado"=:IdTipoDocumento AND C."Version"=:VersionArchivo
AND NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1922N43" D WHERE D."IdCertificadoCliente"=C."IdCertificadoCliente")