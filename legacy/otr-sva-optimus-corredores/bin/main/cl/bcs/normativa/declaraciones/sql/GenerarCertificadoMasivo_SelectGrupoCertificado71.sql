SELECT DISTINCT	"Rut"	
FROM
(
	SELECT  CER."RutCliente" AS "Rut"
		 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
		 INNER JOIN #GenerarCertificado71Masivo_RUT AS RUT ON RTRIM(RUT."Rut") = RTRIM(CER."RutCliente")
		 WHERE CER."IdEmpresa"=:IdEmpresa AND CER."Version"=:Version AND CER."IdTipoCertificado"=:IdTipoDocumento
UNION ALL 
SELECT  CER."RutCliente" AS "Rut"  
		 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
		 WHERE CER."IdEmpresa"=:IdEmpresa AND CER."Version"=:Version AND CER."IdTipoCertificado"=:IdTipoDocumento
		 AND EXISTS 
		 ( 
		 
		   SELECT 1 FROM 
			(SELECT COUNT(1) AS "CANT" FROM #GenerarCertificado71Masivo_RUT WHERE LENGTH(LTRIM(RTRIM("Rut")) )>0 ) AS C WHERE C."CANT"=0

		  )
) ;