
SELECT DISTINCT "RutCliente" as "Rut","Cantidad"   FROM 
(

   --los grupos son x cliente, equivale a N hojas dentro de 1 PDF (CONTADOR X CLIENTE PDF (N HOJAS))
   --SELECT detalle."RutCliente",detalle."Folio",detalle."ID",total."Cantidad"
	SELECT detalle."RutCliente",detalle."ID",total."Cantidad"
	FROM
		(
			--SELECT C."RutCliente",C."Folio",row_number() over (order by C."RutCliente","Folio") AS "ID"
			SELECT C."RutCliente",row_number() over (order by C."RutCliente") AS "ID"
			FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" C
			WHERE C."IdTipoCertificado"=:IdTipoDocumento AND C."Version"=:VersionArchivo
			AND EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1944N17" D WHERE D."IdCertificadoCliente"=C."IdCertificadoCliente")
			AND C."IdEstado" NOT IN ('INA','GEN')
			--ORDER BY  C."RutCliente",C."Folio"
			GROUP BY C."RutCliente"
			ORDER BY  C."RutCliente"
		) detalle,
		(
			SELECT count(1)  AS "Cantidad"
			FROM (
					SELECT C."RutCliente",row_number() over (order by C."RutCliente") AS "ID"
					FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" C
					WHERE C."IdTipoCertificado"=:IdTipoDocumento  AND C."Version"=:VersionArchivo
					AND EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1944N17" D WHERE D."IdCertificadoCliente"=C."IdCertificadoCliente")
					AND C."IdEstado" NOT IN ('INA','GEN')
					GROUP BY C."RutCliente"
					ORDER BY  C."RutCliente"
				 )
		) total
		
) WHERE ID <=:cantidadPDF;