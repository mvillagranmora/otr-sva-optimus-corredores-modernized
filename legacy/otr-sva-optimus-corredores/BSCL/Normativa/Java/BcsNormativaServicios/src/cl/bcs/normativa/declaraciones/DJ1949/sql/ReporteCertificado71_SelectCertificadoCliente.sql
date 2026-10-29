	
SELECT CER."IdCertificadoCliente"
	 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
	 INNER JOIN #GenerarCertificado71Masivo_RUT AS RUT ON RUT."Rut" = CER."RutCliente"
	 WHERE CER."IdEmpresa"=:IdEmpresa AND CER."Version"=:Version AND CER."IdTipoCertificado"='CERT71'
	UNION ALL 
	SELECT CER."IdCertificadoCliente"
			 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
			 WHERE CER."IdEmpresa"=:IdEmpresa AND CER."Version"=:Version AND CER."IdTipoCertificado"='CERT71'
			 AND EXISTS 
			 ( 
			 
			   SELECT 1 FROM 
				(SELECT COUNT(1) AS "CANT" FROM #GenerarCertificado71Masivo_RUT WHERE LENGTH(LTRIM(RTRIM("Rut")) )>0 ) AS C WHERE C."CANT"=0
	
			  )