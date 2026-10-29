
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
			
			 c."Folio"
			,c."IdEmpresa"
			,c."Esquema"
			,c."Version"
			,c."Anio"
			,c."Periodo"
			
			,e."RazonSocial" AS "RazonSocialEmpresa"
			,e."DocumentoIdentidad" AS "DocumentoIdentidadEmpresa"
			,e."DireccionFisicaPrincipalCalle" || ' ' || e."DireccionFisicaPrincipalNumero" || ',' || e."DireccionFisicaPrincipalSector" || ' ' || e."DireccionFisicaPrincipalNombreCiudad" AS "DireccionEmpresa"
			,e."RazonSocial" AS "NombreEntidadRecaudadora"
			
			,c."IdCliente"
			,COALESCE(cli."NombreCompleto", 'Pendiente de enrolar') AS "NombreCompletoCliente"
			,cli."DocumentoIdentidad" AS "DocumentoIdentidadCliente"
			,e."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:Fechacertificado,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha"
			,CASE WHEN m."Mes" = 1 THEN m."DescripcionMes" || ' ' || c."Version" ELSE m."DescripcionMes" END AS "DescripcionMes"
			,m."Mes" 
			,CASE WHEN d."TipoAhorro" = 1 THEN d."MontoRetiro" ELSE 0 END AS "MontoRetiroNominal"
			,CASE WHEN d."TipoAhorro" = 1 THEN d."RentaPositiva" ELSE 0 END AS "RentaPorRetiroPositiva"
			,CASE WHEN d."TipoAhorro" = 1 THEN d."RentaNegativa" ELSE 0 END AS "RentaPorRetiroNegativa"
			,CASE WHEN d."TipoAhorro" = 2 THEN d."MontoRetiro" ELSE 0 END AS "MontoRetiroNominal42"
			,CASE WHEN d."TipoAhorro" = 2 THEN d."RentaPositiva" ELSE 0 END AS "RentaPorRetiroPositiva42"
			,CASE WHEN d."TipoAhorro" = 2 THEN d."RentaNegativa" ELSE 0 END AS "RentaPorRetiroNegativa42"
			,COALESCE(d."Factor",0) AS "FactorActualizacion"
 			,d."RentaActualizadaPositiva"
 			,d."RentaActualizadaNegativa"
			
			FROM  --AQUI la tabla que filtra los ruts
			
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
			ORDER BY m."Mes" ASC