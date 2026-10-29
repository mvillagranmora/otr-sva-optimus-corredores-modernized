SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::Certificado21";
GO
CREATE PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::Certificado21" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 								BIGINT,
    IN p_IdUsuario 								BIGINT,
    IN p_IdModulo 								NVARCHAR(3),
    IN p_Version								NVARCHAR(10) ,
	IN p_Rut           							TABLE ( "Rut" NVARCHAR(40) ),
	
	OUT p_Salida   TABLE (

	 		"Folio"							BIGINT,	
			"IdEmpresa"						BIGINT,	
			"Esquema"						NVARCHAR(10),
			"Version"						NVARCHAR(10),
			"Anio"							NVARCHAR(4),
			"Periodo"						NVARCHAR(20),
			"IdFondo"						NVARCHAR(40),
			"Factor"						DECIMAL(26,8),	  
			"MontoRescate"					DECIMAL(26,8),	  
			"MontoHistorico"				DECIMAL(26,8),	  
			"MayorValor"					DECIMAL(26,8),	
			"MenorValor"					DECIMAL(26,8),	
			"MayorValorActualizado"			DECIMAL(26,8),	
			"MenorValorActualizado"			DECIMAL(26,8),	
			"Mes"							BIGINT,	
			"DescripcionMes"				NVARCHAR(15),
			--"IdCliente"						BIGINT,
			--"IdPortafolio"					BIGINT,
			"NombreCompletoCliente"			NVARCHAR(400),	
			"DocumentoIdentidadCliente"		NVARCHAR(40),	
			"RazonSocialEmpresa"			NVARCHAR(400),
			"NombreEntidadRecaudadora"		NVARCHAR(100),
			"DocumentoIdentidadEmpresa"		NVARCHAR(40),
			"DireccionEmpresa"				NVARCHAR(200),
			"CiudadFecha"					NVARCHAR(250),
			"NombreCompletoEmisor"			NVARCHAR(400),
			"Certificacion"					NVARCHAR(1),
			"EstadoCertificado"				NVARCHAR(3),
			"NombreFondo"					NVARCHAR(100),
			-----------------------------------------	
			"RazonSocialCorredora"          NVARCHAR(100),
			"DireccionCorredora"            NVARCHAR(300),
			"DocumentoIdentidadCorredora"   NVARCHAR(40),
			"rut_representante"				NVARCHAR(40),
			"nombre_representante"			NVARCHAR(300),
			-------------------------------------------
			"RutCertificado"				NVARCHAR(40),
			
			"LogoCorredora"                 BLOB,
			"FirmaCorredora"                BLOB
	
	)
	
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
		DECLARE v_Logo  TABLE("LogoCorredora" BLOB);
		DECLARE v_Firma TABLE("FirmaCorredora" BLOB);
		DECLARE v_count BIGINT:=0;
		DECLARE v_FechaSistema  NVARCHAR(10);
		DECLARE v_DefineFechaCertificado NVARCHAR(10);
	    DECLARE v_FechaCertificado NVARCHAR(10);
	
		SELECT COUNT(1) INTO v_count FROM :p_Rut;

		v_Logo =
			SELECT TOP 1 "Binario" AS "LogoCorredora"
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
			WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
		
		v_Firma =
			SELECT TOP 1 "Binario" AS "FirmaCorredora"
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
			WHERE "NombreImagen" = 'FIRMA_CORREDORA' ORDER BY "Version" DESC;
	
		v_DatosCorredor = 
			SELECT 
			  PER."RazonSocial" 
			 ,EMP."IdEmpresa" 
			 ,CONCAT(DIR."Calle" ,CONCAT(' ',CONCAT(DIR."Numero",CONCAT(' , ',CIU."NombreCiudad")))) AS "Direccion"
			 ,DOC."DocumentoIdentidad" 
			FROM "BSCL.Plataforma.BD::PLA.Empresa" AS EMP
			INNER JOIN  "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"
			INNER JOIN  "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = SOC."IdPersona" 
			INNER JOIN  "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DIR ON DIR."IdPersona" = PER."IdPersona" AND DIR."Predeterminado" ='S'
			INNER JOIN  "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdPersona" = PER."IdPersona" AND DOC."Predeterminado" ='S'
			INNER JOIN  "BSCL.Plataforma.BD::REG.Ciudad" AS CIU ON CIU."IdCiudad" = DIR."IdCiudad" 
			WHERE EMP."IdEmpresa" = :p_IdEmpresa;
		
			---zona de filtros
													
			v_cerificados = 	
			SELECT CER."IdCertificadoCliente", CER."RutCliente", CER."IdEmpresa", CER."Folio", CER."Esquema", CER."Version", CER."Anio", CER."Periodo"
			 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
			 INNER JOIN :p_Rut AS RUT ON RUT."Rut" = CER."RutCliente"
			 WHERE CER."IdEmpresa"=:p_IdEmpresa AND CER."Version"=:p_Version AND CER."IdTipoCertificado"='CERT21'
			UNION ALL 
			SELECT CER."IdCertificadoCliente", CER."RutCliente", CER."IdEmpresa", CER."Folio", CER."Esquema", CER."Version", CER."Anio", CER."Periodo"
					 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
					 WHERE CER."IdEmpresa"=:p_IdEmpresa AND CER."Version"=:p_Version AND CER."IdTipoCertificado"='CERT21'
					 AND EXISTS 
					 ( 
					 
					   SELECT 1 FROM 
						(SELECT COUNT(1) AS "CANT" FROM :p_Rut WHERE LENGTH(LTRIM(RTRIM("Rut")) )>0 ) AS C WHERE C."CANT"=0
			
					  );
				
			
		--zona especifica ceritificado
		
		SELECT DOCDJ."DefineFechaCertificado" INTO v_DefineFechaCertificado FROM "BSCL.Normativa.BD::DOC.Documento" DOCDJ
                        WHERE "Esquema" = 'DJ1894' AND "Version" = p_Version AND "IdTipoDocumento" = 'DECLACORR';
                       
        SELECT SUBSTRING(TO_DATE(DOCDJ."FechaCertificado"),1,10) INTO v_FechaCertificado FROM "BSCL.Normativa.BD::DOC.Documento" DOCDJ
                        WHERE "Esquema" = 'DJ1894' AND "Version" = p_Version AND "IdTipoDocumento" = 'DECLACORR';
        
        IF v_DefineFechaCertificado = 'S'
        THEN
            v_FechaSistema := v_FechaCertificado;
        ELSE
		    CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
        END IF;
	
		v_meses =
			SELECT 1 "Mes",'ENERO' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 2 "Mes",'FEBRERO' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 3 "Mes",'MARZO' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 4 "Mes",'ABRIL' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 5 "Mes",'MAYO' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 6 "Mes",'JUNIO' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 7 "Mes",'JULIO' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 8 "Mes",'AGOSTO' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 9 "Mes",'SEPTIEMBRE' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 10 "Mes",'OCTUBRE' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 11 "Mes",'NOVIEMBRE' AS "DescripcionMes" FROM DUMMY UNION ALL
			SELECT 12 "Mes",'DICIEMBRE' AS "DescripcionMes" FROM DUMMY;

		v_datos_representante=
            SELECT "DocumentoIdentidadRepresentante" AS "rut_representante", PERSONA."NombreCompleto" AS "nombre_representante" FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMPRESA
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERSONA ON (PERSONA."DocumentoIdentidad" = EMPRESA."DocumentoIdentidadRepresentante" AND EMPRESA."IdEmpresa"=:p_IdEmpresa);
		
		v_Resultado =
			SELECT
			c."Folio"
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
			-- fecha
			,m."DescripcionMes"
			,m."Mes" --,m."NUMERO" AS "Numero"
			--cliente
			--c."IdCliente"
			--,c."IdPortafolio"
			,COALESCE(cli."NombreCompleto", 'Pendiente de enrolar') AS "NombreCompletoCliente"--nombre del contribuyente
			,cli."DocumentoIdentidad" AS "DocumentoIdentidadCliente"
			--empresa
			,e."RazonSocial" AS "RazonSocialEmpresa"
			,e."RazonSocial" AS "NombreEntidadRecaudadora"
			,e."DocumentoIdentidad" AS "DocumentoIdentidadEmpresa"
			,e."DireccionFisicaPrincipalCalle" || ' ' || e."DireccionFisicaPrincipalNumero" || ',' || e."DireccionFisicaPrincipalSector" || ' ' || e."DireccionFisicaPrincipalNombreCiudad" AS "DireccionEmpresa"
			,e."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:v_FechaSistema,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha"
			--emisor
			,COALESCE(em."NombreCompleto", '') AS "NombreCompletoEmisor"
			,(SELECT "NombreSerieFondo" FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" fm WHERE fm."IdSerieFondo" = d."IdFondo") AS "NombreFondo"
			FROM :v_cerificados c
			INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1894N21" d ON (c."IdCertificadoCliente" = d."IdCertificadoCliente")
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON (TRIM(cli."DocumentoIdentidad") = TRIM(c."RutCliente"))
			INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" e ON (e."IdEmpresa" = c."IdEmpresa")
			LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" em ON (em."IdEmisor" = d."IdEmisor")
			INNER JOIN :v_meses m ON (m."Mes" = d."DescMes")
			WHERE c."Version" = :p_Version
			ORDER BY c."Folio";	
			
			
			v_datos =
			SELECT DISTINCT
			"Folio",
			"IdEmpresa",
			"Esquema",
			"Version",
			"Anio",
			"Periodo",
			"Mes",
			"RazonSocialEmpresa",
			"DocumentoIdentidadEmpresa",
			"DireccionEmpresa",
			"NombreEntidadRecaudadora",
			"IdFondo",
			"NombreCompletoCliente",
			"DocumentoIdentidadCliente",
			"NombreCompletoEmisor",
			"CiudadFecha",
			"NombreFondo"
			FROM :v_Resultado;

		v_meses_sin_datos =
			SELECT DISTINCT 
			c."Folio"
			,c."IdEmpresa"
			,c."Esquema"
			,c."Version"
			,c."Anio"
			,c."Periodo"
			,c."IdFondo"
			--valores
			,0 AS "Factor"
			,0 AS "MontoRescate"
			,0 AS "MontoHistorico"
			,0 AS "MayorValor"
			,0 AS "MenorValor"
			,0 AS "MayorValorActualizado"
			,0 AS "MenorValorActualizado"
			--fecha
			,m."DescripcionMes"
			,m."Mes"
			--cliente
			--,c."IdCliente"
			--,c."IdPortafolio"
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
			-- nombre fondo
			,c."NombreFondo"
			FROM :v_datos AS c, :v_meses AS m
			WHERE NOT EXISTS (	SELECT 1
						FROM :v_Resultado r
						WHERE r."Folio" = c."Folio" AND m."Mes" = r."Mes");
			
			
			
			
		--Zona comun	
						
		v_DatosDetalle =
		SELECT
			d."Folio"
			,d."IdEmpresa"
			,d."Esquema"
			,d."Version"
			,d."Anio"
			,d."Periodo"
			,d."IdFondo"
			,d."Factor"
			,d."MontoRescate"
			,d."MontoHistorico"
			,d."MayorValor"
			,d."MenorValor"
			,d."MayorValorActualizado"
			,d."MenorValorActualizado"
			,d."Mes"
			,d."DescripcionMes"
			--,d."IdCliente"
			--,d."IdPortafolio"
			,d."NombreCompletoCliente"
			,d."DocumentoIdentidadCliente"
			,d."RazonSocialEmpresa"
			,d."NombreEntidadRecaudadora"
			,d."DocumentoIdentidadEmpresa"
			,d."DireccionEmpresa"
			,d."CiudadFecha"
			,d."NombreCompletoEmisor"
			,COALESCE(c."Certificacion",'N') AS "Certificacion"
			,COALESCE(c."EstadoCertificado",'NGE') AS "EstadoCertificado"
			,d."NombreFondo"
			FROM
			(SELECT * FROM :v_Resultado UNION ALL
			SELECT * FROM :v_meses_sin_datos) AS d
			LEFT JOIN "BSCL.Normativa.BD::TRI.ControlProceso" c ON (c."Esquema" = d."Esquema"
			AND c."Version" = d."Version");			
			
						
						
					
		p_Salida = SELECT
						 
				 "Detalle"."Folio"
				,"Detalle"."IdEmpresa"
				,"Detalle"."Esquema"
				,"Detalle"."Version"
				,"Detalle"."Anio"
				,"Detalle"."Periodo"
				,"Detalle"."IdFondo"
				,"Detalle"."Factor"
				,"Detalle"."MontoRescate"
				,"Detalle"."MontoHistorico"
				,"Detalle"."MayorValor"
				,"Detalle"."MenorValor"
				,"Detalle"."MayorValorActualizado"
				,"Detalle"."MenorValorActualizado"
				,"Detalle"."Mes"
				,"Detalle"."DescripcionMes"
				--,"Detalle"."IdCliente"
				--,"Detalle"."IdPortafolio"
				,"Detalle"."NombreCompletoCliente"
				,"Detalle"."DocumentoIdentidadCliente"
				,"Detalle"."RazonSocialEmpresa"
				,"Detalle"."NombreEntidadRecaudadora"
				,"Detalle"."DocumentoIdentidadEmpresa"
				,"Detalle"."DireccionEmpresa"
				,"Detalle"."CiudadFecha"
				,"Detalle"."NombreCompletoEmisor"
				,"Detalle"."Certificacion"
				,"Detalle"."EstadoCertificado"
				,"Detalle"."NombreFondo"
				-------------------------
				,"Corredor"."RazonSocial" AS "RazonSocialCorredora"
				,"Corredor"."Direccion" AS "DireccionCorredora"
				,"Corredor"."DocumentoIdentidad" AS "DocumentoIdentidadCorredora"
				,"Rep"."rut_representante"
				,"Rep"."nombre_representante"
				
				,"Detalle"."DocumentoIdentidadCliente" AS "RutCertificado"
				,CASE when v_count = 1 then "Logo"."LogoCorredora" else '' end as "LogoCorredora"
				,CASE when v_count = 1 then "Firma"."FirmaCorredora" else '' end as "FirmaCorredora"
 
					 FROM :v_DatosDetalle AS "Detalle"
					 LEFT JOIN :v_DatosCorredor AS "Corredor" ON "Corredor"."IdEmpresa"=:p_IdEmpresa
			         LEFT JOIN :v_Logo AS "Logo" ON "Logo"."LogoCorredora" IS NOT NULL
					 LEFT JOIN :v_Firma AS "Firma" ON "Firma"."FirmaCorredora" IS NOT NULL
					 LEFT JOIN :v_datos_representante AS "Rep" ON "Rep"."rut_representante" IS NOT NULL
				ORDER BY "Detalle"."Folio","Detalle"."Mes";
END;
