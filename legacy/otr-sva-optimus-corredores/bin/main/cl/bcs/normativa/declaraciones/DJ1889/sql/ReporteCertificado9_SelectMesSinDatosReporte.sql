
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
   
			 c."Folio"
			,c."IdEmpresa"
			,c."Esquema"
			,c."Version"
			,c."Anio"
			,c."Periodo"
			
			,c."RazonSocialEmpresa"
			,c."DocumentoIdentidadEmpresa"
			,c."DireccionEmpresa"
			,c."NombreEntidadRecaudadora"
			
			,c."IdCliente"
			,c."NombreCompletoCliente"
			,c."DocumentoIdentidadCliente"
			,c."CiudadFecha"
			,CASE WHEN m."Mes" = 1 THEN m."DescripcionMes" || ' ' || :Version ELSE m."DescripcionMes" END AS "DescripcionMes"
			,m."Mes"
			,to_decimal(0) AS "MontoRetiroNominal"
			,to_decimal(0) AS "RentaPorRetiroPositiva"
			,to_decimal(0) AS "RentaPorRetiroNegativa"
			,to_decimal(0) AS "MontoRetiroNominal42"
			,to_decimal(0) AS "RentaPorRetiroPositiva42"
			,to_decimal(0) AS "RentaPorRetiroNegativa42"
			,to_decimal(0) AS "FactorActualizacion"
			,to_decimal(0) AS "RentaActualizadaPositiva"
			,to_decimal(0) AS "RentaActualizadaNegativa"

           FROM (
						SELECT DISTINCT  --datos del certificado
									c."Folio"
									,c."IdEmpresa"
									,c."Esquema"
									,c."Version"
									,c."Anio"
									,c."Periodo"
									,c."IdCliente"
									
									
									,m."Mes"
									,m."DescripcionMes"
									,e."RazonSocial" AS "RazonSocialEmpresa"
									,e."DocumentoIdentidad" AS "DocumentoIdentidadEmpresa"
									,e."DireccionFisicaPrincipalCalle" || ' ' || e."DireccionFisicaPrincipalNumero" || ',' || e."DireccionFisicaPrincipalSector" || ' ' || e."DireccionFisicaPrincipalNombreCiudad" AS  "DireccionEmpresa"
									,e."RazonSocial" AS "NombreEntidadRecaudadora"
									,COALESCE(cli."NombreCompleto", 'Pendiente de enrolar') AS "NombreCompletoCliente"
									,cli."DocumentoIdentidad" AS "DocumentoIdentidadCliente"
									,e."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:Fechacertificado,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha"
						
							FROM 	
									(
									
										SELECT CER."IdCertificadoCliente", CER."RutCliente", CER."IdEmpresa", CER."Folio", CER."Esquema", CER."Version", CER."Anio", CER."Periodo", CER."IdCliente"
										 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
										 WHERE CER."IdEmpresa"=:IdEmpresa AND CER."Version"=:Version AND CER."IdTipoCertificado"='CERT9'
										 AND CER."IdCertificadoCliente" in (:IdCertificadoCliente)
										
									) AS c
									INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1889N9" 	d 	ON (c."IdCertificadoCliente" = d."IdCertificadoCliente")
									INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 			cli ON (TRIM(cli."DocumentoIdentidad") = TRIM(c."RutCliente"))
									INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 			e 	ON (e."IdEmpresa" = c."IdEmpresa")
									INNER JOIN 	(SELECT * FROM Meses)
									m ON (m."Mes" = d."Mes")
									
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
						                SELECT c."Folio",d."Mes" AS "Mes"
						 
									      FROM 	
											(
											
												SELECT CER."IdCertificadoCliente", CER."RutCliente", CER."IdEmpresa", CER."Folio", CER."Esquema", CER."Version", CER."Anio", CER."Periodo"
												 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
												 WHERE CER."IdEmpresa"=:IdEmpresa AND CER."Version"=:Version AND CER."IdTipoCertificado"='CERT9'
												 AND CER."IdCertificadoCliente" in (:IdCertificadoCliente)
												
											) AS c
											INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1889N9" 	d 	ON (c."IdCertificadoCliente" = d."IdCertificadoCliente")
											INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 			cli ON (TRIM(cli."DocumentoIdentidad") = TRIM(c."RutCliente"))
											INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 			e 	ON (e."IdEmpresa" = c."IdEmpresa")
											WHERE c."Version" = :Version 
						
						
						
							  ) AS     r
						WHERE r."Folio" = c."Folio" AND m."Mes" = r."Mes");
			
					
					