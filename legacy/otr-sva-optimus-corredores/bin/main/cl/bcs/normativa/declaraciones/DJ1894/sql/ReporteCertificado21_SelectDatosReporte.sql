
WITH Meses AS (	
			SELECT 1 "Mes",'ENERO' 		AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 2 "Mes",'FEBRERO' 	AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 3 "Mes",'MARZO'		AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 4 "Mes",'ABRIL' 		AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 5 "Mes",'MAYO' 		AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 6 "Mes",'JUNIO' 		AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 7 "Mes",'JULIO' 		AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 8 "Mes",'AGOSTO'		AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 9 "Mes",'SEPTIEMBRE' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 10 "Mes",'OCTUBRE' 	AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 11 "Mes",'NOVIEMBRE' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 12 "Mes",'DICIEMBRE' AS "DescripcionMes" FROM DUMMY
)

   SELECT
			
			m."DescripcionMes"
			,m."Mes" 
   
			,c."Folio"
			,c."IdEmpresa"
			,c."Esquema"
			,c."Version"
			,c."Anio"
			,c."Periodo"
			,d."IdFondo"
			--montos certificado
			,d."MontoRescate"
			,d."MontoHistorico"
			,d."MayorValor"
			,d."MenorValor"
			,d."Factor"
			,d."MayorValorActualizado"
			,d."MenorValorActualizado"
			,COALESCE(cli."NombreCompleto", 'Pendiente de enrolar') AS "NombreCompletoCliente"--nombre del contribuyente
			,cli."DocumentoIdentidad" AS "DocumentoIdentidadCliente"
			--empresa
			,e."RazonSocial" AS "RazonSocialEmpresa"
			,e."RazonSocial" AS "NombreEntidadRecaudadora"
			,e."DocumentoIdentidad" AS "DocumentoIdentidadEmpresa"
			,e."DireccionFisicaPrincipalCalle" || ' ' || e."DireccionFisicaPrincipalNumero" || ',' || e."DireccionFisicaPrincipalSector" || ' ' || e."DireccionFisicaPrincipalNombreCiudad" AS "DireccionEmpresa"
			,e."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:Fechacertificado,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha"
			--emisor
			,COALESCE(em."NombreCompleto", '') AS "NombreCompletoEmisor"
			,(SELECT "NombreSerieFondo" FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" fm WHERE fm."IdSerieFondo" = d."IdFondo") AS "NombreFondo"
			FROM  --AQUI la tabla que filtra los ruts
			
			(
			
				SELECT CER."IdCertificadoCliente", CER."RutCliente", CER."IdEmpresa", CER."Folio", CER."Esquema", CER."Version", CER."Anio", CER."Periodo"
				 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
				 WHERE CER."IdEmpresa"=:IdEmpresa AND CER."Version"=:Version AND CER."IdTipoCertificado"='CERT21'
				 AND CER."IdCertificadoCliente" in (:IdCertificadoCliente)
				
			) AS c
			INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N21" 	d 	ON (c."IdCertificadoCliente" = d."IdCertificadoCliente")
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 			cli ON (TRIM(cli."DocumentoIdentidad") = TRIM(c."RutCliente"))
			INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 			e 	ON (e."IdEmpresa" = c."IdEmpresa")
			LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" 		em 	ON (em."IdEmisor" = d."IdEmisor")
			INNER JOIN 	(SELECT * FROM Meses)
			m ON (m."Mes" = d."DescMes")
			
			WHERE c."Version" = :Version 