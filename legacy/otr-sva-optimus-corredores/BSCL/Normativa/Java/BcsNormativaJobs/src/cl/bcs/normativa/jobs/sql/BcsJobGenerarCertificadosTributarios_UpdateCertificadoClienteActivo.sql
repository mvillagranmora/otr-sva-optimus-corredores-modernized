
	UPDATE C SET C."IdEstado"=:IdEstado
	FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" C
	WHERE C."IdTipoCertificado"=:IdTipoDocumento AND C."Version"=:VersionArchivo;
	


