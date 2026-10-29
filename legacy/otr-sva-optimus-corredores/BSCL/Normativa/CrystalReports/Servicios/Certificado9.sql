SET SCHEMA "BSCL";
--GO 
DROP PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::Certificado9";
--GO
CREATE PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::Certificado9" (
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
        ----------------------------------------------
        "RazonSocialEmpresa"            NVARCHAR(100),
        "DocumentoIdentidadEmpresa"     NVARCHAR(40),
        "DireccionEmpresa"              NVARCHAR(343),
        "NombreEntidadRecaudadora"      NVARCHAR(100),
        "IdCliente"                     BIGINT,
        "NombreCompletoCliente"         NVARCHAR(302),
        "DocumentoIdentidadCliente"     NVARCHAR(40),
        "CiudadFecha"                   NVARCHAR(5000),
        "Mes"                           BIGINT,
        "DescripcionMes"                NVARCHAR(21),
        "MontoRetiroNominal"            DECIMAL(26,8),
        "RentaPorRetiroPositiva"        DECIMAL(26,8),
        "RentaPorRetiroNegativa"        DECIMAL(26,8),
        "MontoRetiroNominal42"          DECIMAL(26,8),
        "RentaPorRetiroPositiva42"      DECIMAL(26,8),
        "RentaPorRetiroNegativa42"      DECIMAL(26,8),
        "FactorActualizacion"           DECIMAL(26,8),
        "RentaActualizadaPositiva"      DECIMAL(26,8),
        "RentaActualizadaNegativa"      DECIMAL(26,8),
        "Certificacion"                 NVARCHAR(3),
        "EstadoCertificado"             NVARCHAR(3),
        -----------------------------------------	
        "RazonSocialCorredora"          NVARCHAR(100),
        "DireccionCorredora"            NVARCHAR(300),
        "DocumentoIdentidadCorredora"   NVARCHAR(10),
        "rut_representante"				NVARCHAR(10),
        "nombre_representante"			NVARCHAR(100),
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
		DECLARE v_count BIGINT:=0;DECLARE v_FechaSistema  NVARCHAR(10);
        DECLARE v_DefineFechaCertificado NVARCHAR(10);
	    DECLARE v_FechaCertificado NVARCHAR(10);

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
				SELECT CER."IdCertificadoCliente", CER."RutCliente", CER."IdEmpresa", CER."Folio", CER."Esquema", CER."Version", CER."Anio", CER."Periodo",CER."IdCliente"
			 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
			 INNER JOIN :p_Rut AS RUT ON RUT."Rut" = CER."RutCliente"
			 WHERE CER."IdEmpresa"=:p_IdEmpresa AND CER."Version"=:p_Version AND CER."IdTipoCertificado"='CERT9'
			UNION ALL 
			SELECT CER."IdCertificadoCliente", CER."RutCliente", CER."IdEmpresa", CER."Folio", CER."Esquema", CER."Version", CER."Anio", CER."Periodo",CER."IdCliente"
					 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
					 WHERE CER."IdEmpresa"=:p_IdEmpresa AND CER."Version"=:p_Version AND CER."IdTipoCertificado"='CERT9'
					 AND EXISTS 
					 ( 
					 
					   SELECT 1 FROM 
						(SELECT COUNT(1) AS "CANT" FROM :p_Rut WHERE LENGTH(LTRIM(RTRIM("Rut")) )>0 ) AS C WHERE C."CANT"=0
			
					  );
			
		v_datos_representante=
            SELECT "DocumentoIdentidadRepresentante" AS "rut_representante", PERSONA."NombreCompleto" AS "nombre_representante" FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMPRESA
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERSONA ON (PERSONA."DocumentoIdentidad" = EMPRESA."DocumentoIdentidadRepresentante" AND EMPRESA."IdEmpresa"=:p_IdEmpresa);
		
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

		--zona especifica ceritificado
		
		SELECT DOCDJ."DefineFechaCertificado" INTO v_DefineFechaCertificado FROM "BSCL.Normativa.BD::DOC.Documento" DOCDJ
                        WHERE "Esquema" = 'DJ1889' AND "Version" = p_Version AND "IdTipoDocumento" = 'DECLACORR';
                       
        SELECT SUBSTRING(TO_DATE(DOCDJ."FechaCertificado"),1,10) INTO v_FechaCertificado FROM "BSCL.Normativa.BD::DOC.Documento" DOCDJ
                        WHERE "Esquema" = 'DJ1889' AND "Version" = p_Version AND "IdTipoDocumento" = 'DECLACORR';
        
        IF v_DefineFechaCertificado = 'S'
        THEN
            v_FechaSistema := v_FechaCertificado;
        ELSE
		    CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
        END IF;
		
        v_Resultado =
            SELECT 
                c."Folio",--Certificado numero
                c."IdEmpresa",
                c."Esquema",
                c."Version",              
                c."Anio",
                c."Periodo",
                --Institución Autorizada
                e."RazonSocial" AS "RazonSocialEmpresa", --razon social de la administradora
                e."DocumentoIdentidad" AS "DocumentoIdentidadEmpresa", --rut de la administradora
                e."DireccionFisicaPrincipalCalle" || ' ' || e."DireccionFisicaPrincipalNumero" || ',' || e."DireccionFisicaPrincipalSector" || ' ' || e."DireccionFisicaPrincipalNombreCiudad" AS "DireccionEmpresa", --diraccion de la administradora
                --La Administradora de Fondos de Pensiones...
                e."RazonSocial" AS "NombreEntidadRecaudadora",
                --trabajador
                c."IdCliente",--contribuyente               
                COALESCE(cli."NombreCompleto", 'Pendiente de enrolar') AS "NombreCompletoCliente",--nombre del contribuyente
                c."RutCliente" AS "DocumentoIdentidadCliente",--rut del contribuyente
                --
                e."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:v_FechaSistema,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha", --ciudad y fecha
                m."Mes",
                CASE WHEN m."Mes" = 1 THEN m."DescripcionMes" || ' ' || d."Version" ELSE m."DescripcionMes" END AS "DescripcionMes",

                CASE WHEN d."TipoAhorro" = 1 THEN d."MontoRetiro" ELSE 0 END AS "MontoRetiroNominal",
                CASE WHEN d."TipoAhorro" = 1 THEN d."RentaPositiva" ELSE 0 END AS "RentaPorRetiroPositiva",
                CASE WHEN d."TipoAhorro" = 1 THEN d."RentaNegativa" ELSE 0 END AS "RentaPorRetiroNegativa",

                CASE WHEN d."TipoAhorro" = 2 THEN d."MontoRetiro" ELSE 0 END AS "MontoRetiroNominal42",
                CASE WHEN d."TipoAhorro" = 2 THEN d."RentaPositiva" ELSE 0 END AS "RentaPorRetiroPositiva42",
                CASE WHEN d."TipoAhorro" = 2 THEN d."RentaNegativa" ELSE 0 END AS "RentaPorRetiroNegativa42",

                COALESCE(d."Factor",0) AS "FactorActualizacion",
                d."RentaActualizadaPositiva",
                d."RentaActualizadaNegativa"
            FROM :v_cerificados c
            LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON (TRIM(cli."DocumentoIdentidad") = TRIM(c."RutCliente"))
            INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1889N9" d  ON (d."IdCertificadoCliente" = c."IdCertificadoCliente")
            INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" e ON (e."IdEmpresa" = c."IdEmpresa")
            INNER JOIN :v_meses AS m ON (m."Mes" = d."Mes")
            WHERE c."Version" = :p_Version;	

        v_datos =
            SELECT
                DISTINCT
                    "Folio",
                    "IdEmpresa",
                    "Esquema",
                    "Version",
                    "Anio",
                    "Periodo", 
                    --Institución Autorizada
                    "RazonSocialEmpresa",
                    "DocumentoIdentidadEmpresa",
                    "DireccionEmpresa",
                    --La Administradora de Fondos de Pensiones...
                    "NombreEntidadRecaudadora",
                    --trabajador
                    "IdCliente",
                    "NombreCompletoCliente",
                    "DocumentoIdentidadCliente",
                    "CiudadFecha"
            FROM :v_Resultado;

        v_meses_sin_datos =
            SELECT
                c."Folio",
                c."IdEmpresa",
                c."Esquema",
                c."Version",
                c."Anio",
                c."Periodo", 
                --Institución Autorizada
                c."RazonSocialEmpresa", --razon social de la administradora
                c."DocumentoIdentidadEmpresa", --rut de la administradora
                c."DireccionEmpresa", --diraccion de la administradora
                --La Administradora de Fondos de Pensiones...
                c."NombreEntidadRecaudadora",--??
                --trabajador
                c."IdCliente",--contribuyente               
                c."NombreCompletoCliente",--nombre del contribuyente
                c."DocumentoIdentidadCliente",--rut del contribuyente
                --
                c."CiudadFecha", --ciudad y fecha
                m."Mes",
                CASE WHEN m."Mes" = 1 THEN m."DescripcionMes" || ' ' || c."Version" ELSE m."DescripcionMes" END AS "DescripcionMes",
                0 AS "MontoRetiroNominal",
                0 AS "RentaPorRetiroPositiva",
                0 AS "RentaPorRetiroNegativa",

                0 AS "MontoRetiroNominal42",
                0 AS "RentaPorRetiroPositiva42",
                0 AS "RentaPorRetiroNegativa42",

                0 AS "FactorActualizacion",
                0 AS "RentaActualizadaPositiva",
                0 AS "RentaActualizadaNegativa"
            FROM :v_datos AS c,:v_meses AS m
            WHERE NOT EXISTS (SELECT 1 FROM :v_Resultado r WHERE r."Folio" = c."Folio" AND m."Mes" = r."Mes");

        v_Resultado1 =
            SELECT
                d."Folio",
                d."IdEmpresa",
                d."Esquema",
                d."Version",
                d."Anio",
                d."Periodo", 
                --Institución Autorizada
                d."RazonSocialEmpresa",
                d."DocumentoIdentidadEmpresa",
                d."DireccionEmpresa",
                --La Administradora de Fondos de Pensiones...
                d."NombreEntidadRecaudadora",
                --trabajador
                d."IdCliente",
                d."NombreCompletoCliente",
                d."DocumentoIdentidadCliente",
                d."CiudadFecha",
                d."Mes",
                d."DescripcionMes",
                d."MontoRetiroNominal",
                d."RentaPorRetiroPositiva",
                d."RentaPorRetiroNegativa",
                d."MontoRetiroNominal42",
                d."RentaPorRetiroPositiva42",
                d."RentaPorRetiroNegativa42",
                d."FactorActualizacion",
                d."RentaActualizadaPositiva",
                d."RentaActualizadaNegativa",
                COALESCE(c."Certificacion",'NGE') AS "Certificacion",
                COALESCE(c."EstadoCertificado",'NGE') AS "EstadoCertificado"
            FROM
            (SELECT * FROM :v_Resultado UNION ALL
                SELECT * FROM :v_meses_sin_datos) AS d
            LEFT JOIN "BSCL.Normativa.BD::TRI.ControlProceso" c ON (c."Esquema" = d."Esquema"
                                                                AND c."Version" = d."Version")
            ORDER BY d."Folio",d."Mes";

		p_Salida = SELECT
            "Detalle"."Folio"
            ,"Detalle"."IdEmpresa"
            ,"Detalle"."Esquema"
            ,"Detalle"."Version"
            ,"Detalle"."Anio"
            ,"Detalle"."Periodo"
            ---
            ,"Detalle"."RazonSocialEmpresa"
            ,"Detalle"."DocumentoIdentidadEmpresa"
            ,"Detalle"."DireccionEmpresa"
            ,"Detalle"."NombreEntidadRecaudadora"
            ,"Detalle"."IdCliente"
            ,"Detalle"."NombreCompletoCliente"
            ,"Detalle"."DocumentoIdentidadCliente"
            ,"Detalle"."CiudadFecha"
            ,"Detalle"."Mes"
            ,"Detalle"."DescripcionMes"
            ,"Detalle"."MontoRetiroNominal"
            ,"Detalle"."RentaPorRetiroPositiva"
            ,"Detalle"."RentaPorRetiroNegativa"
            ,"Detalle"."MontoRetiroNominal42"
            ,"Detalle"."RentaPorRetiroPositiva42"
            ,"Detalle"."RentaPorRetiroNegativa42"
            ,"Detalle"."FactorActualizacion"
            ,"Detalle"."RentaActualizadaPositiva"
            ,"Detalle"."RentaActualizadaNegativa"
            ,"Detalle"."Certificacion"
            ,"Detalle"."EstadoCertificado"
            -------------------------
            ,"Corredor"."RazonSocial" AS "RazonSocialCorredora"
            ,"Corredor"."Direccion" AS "DireccionCorredora"
            ,"Corredor"."DocumentoIdentidad" AS "DocumentoIdentidadCorredora"
            ,"Rep"."rut_representante"
            ,"Rep"."nombre_representante"
            ,"Detalle"."DocumentoIdentidadCliente" AS "RutCertificado"
            ,"Logo"."LogoCorredora"
            ,"Firma"."FirmaCorredora"
                FROM :v_Resultado1 AS "Detalle"
                LEFT JOIN :v_DatosCorredor AS "Corredor" ON "Corredor"."IdEmpresa"=:p_IdEmpresa
                LEFT JOIN :v_Logo AS "Logo" ON "Logo"."LogoCorredora" IS NOT NULL
                LEFT JOIN :v_Firma AS "Firma" ON "Firma"."FirmaCorredora" IS NOT NULL
                LEFT JOIN :v_datos_representante AS "Rep" ON "Rep"."rut_representante" IS NOT NULL
            ORDER BY "Detalle"."Folio","Detalle"."Mes";				 

END;
