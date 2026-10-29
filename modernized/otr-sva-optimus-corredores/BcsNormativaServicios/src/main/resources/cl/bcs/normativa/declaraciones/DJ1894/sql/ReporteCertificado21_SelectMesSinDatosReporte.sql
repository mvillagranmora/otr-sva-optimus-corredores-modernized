
WITH Meses AS (	
			SELECT 1 "Mes",'ENERO' 		AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 2 "Mes",'FEBRERO' 	AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 3 "Mes",'MARZO'		AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 4 "Mes",'ABRIL' 		AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 5 "Mes",'MAYO' 		AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 6 "Mes",'JUNIO' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 7 "Mes",'JULIO' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 8 "Mes",'AGOSTO' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 9 "Mes",'SEPTIEMBRE' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 10 "Mes",'OCTUBRE' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 11 "Mes",'NOVIEMBRE' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 12 "Mes",'DICIEMBRE' AS "DescripcionMes" FROM DUMMY
)

SELECT DISTINCT -- meses sin datos  
m."DescripcionMes"
			
   
			,c."Folio"
			,c."IdEmpresa"
			,c."Esquema"
			,c."Version"
			,c."Anio"
			,c."Periodo"
			,c."IdFondo"
			--valores
			,to_decimal(0) AS "Factor"
			,to_decimal(0) AS "MontoRescate"
			,to_decimal(0) AS "MontoHistorico"
			,to_decimal(0) AS "MayorValor"
			,to_decimal(0) AS "MenorValor"
			,to_decimal(0) AS "MayorValorActualizado"
			,to_decimal(0) AS "MenorValorActualizado"
			,m."Mes" 
			,m."DescripcionMes"
			,c."NombreCompletoCliente"
			,c."DocumentoIdentidadCliente"
			--empresa
			,c."RazonSocialEmpresa"
			,c."NombreEntidadRecaudadora"
			,c."DocumentoIdentidadEmpresa"
			,c."DireccionEmpresa"
			,c."CiudadFecha"
			--emisor
			,c."NombreCompletoEmisor"
			,c."NombreFondo"

           FROM (
						SELECT DISTINCT  --datos del certificado
									c."Folio"
									,c."IdEmpresa"
									,c."Esquema"
									,c."Version"
									,c."Anio"
									,c."Periodo"
									
									,m."Mes"
									,e."RazonSocial" AS "RazonSocialEmpresa"
									,e."DocumentoIdentidad" AS "DocumentoIdentidadEmpresa"
									,e."DireccionFisicaPrincipalCalle" || ' ' || e."DireccionFisicaPrincipalNumero" || ',' || e."DireccionFisicaPrincipalSector" || ' ' || e."DireccionFisicaPrincipalNombreCiudad" AS  "DireccionEmpresa"
									,e."RazonSocial" AS "NombreEntidadRecaudadora"
									,d."IdFondo"
									,COALESCE(cli."NombreCompleto", 'Pendiente de enrolar') AS "NombreCompletoCliente"
									,cli."DocumentoIdentidad" AS "DocumentoIdentidadCliente"
									,COALESCE(em."NombreCompleto", '') AS "NombreCompletoEmisor"
									,e."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:Fechacertificado,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha"
									,(SELECT "NombreSerieFondo" FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" fm WHERE fm."IdSerieFondo" = d."IdFondo") AS "NombreFondo"
						
							FROM 	
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
									
									
					)AS c, (
					
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
							) AS m
					WHERE NOT EXISTS (	SELECT 1
						FROM (
						                SELECT c."Folio",d."DescMes" AS "Mes"
						 
									      FROM 	
											(
											
												SELECT CER."IdCertificadoCliente", CER."RutCliente", CER."IdEmpresa", CER."Folio", CER."Esquema", CER."Version", CER."Anio", CER."Periodo"
												 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
												 WHERE CER."IdEmpresa"=:IdEmpresa AND CER."Version"=:Version AND CER."IdTipoCertificado"='CERT21'
												 AND CER."IdCertificadoCliente" in (:IdCertificadoCliente)
												
											) AS c
											INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N21" 	d 	ON (c."IdCertificadoCliente" = d."IdCertificadoCliente")
											INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 			cli ON (TRIM(cli."DocumentoIdentidad") = TRIM(c."RutCliente"))
											INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 			e 	ON (e."IdEmpresa" = c."IdEmpresa")
											WHERE c."Version" = :Version 
						
						
						
							  ) AS     r
						WHERE r."Folio" = c."Folio" AND m."Mes" = r."Mes");
			
					
					