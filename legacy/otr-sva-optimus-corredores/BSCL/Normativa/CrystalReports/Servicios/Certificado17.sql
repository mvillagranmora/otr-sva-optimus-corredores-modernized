SET SCHEMA "BSCL";
--GO 
DROP PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::Certificado17";
--GO
CREATE PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::Certificado17" (
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
        "NombreEntidadRecaudadora"      NVARCHAR(300),
        "IdCliente"                     BIGINT,
        "NombreCompletoCliente"         NVARCHAR(300),
        "DocumentoIdentidadCliente"     NVARCHAR(40),
        "CiudadFecha"                   NVARCHAR(5000),
        "NombreDelGiro"                 NVARCHAR(500),
        "SalAhorroNetoEjerNEGT1"        DECIMAL(26,8),
        "SalAhorroNetoEjerT2"           DECIMAL(26,8),
        "SalAhorroNetoEjeNegT3"         DECIMAL(26,8),
        "MtoRenRetFFMMPOS"              DECIMAL(26,8),
        "MtoRenRetFFMMNEG"              DECIMAL(26,8),
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
			 WHERE CER."IdEmpresa"=:p_IdEmpresa AND CER."Version"=:p_Version AND CER."IdTipoCertificado"='CERT17'
			UNION ALL 
			SELECT CER."IdCertificadoCliente", CER."RutCliente", CER."IdEmpresa", CER."Folio", CER."Esquema", CER."Version", CER."Anio", CER."Periodo",CER."IdCliente" 
					 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
					 WHERE CER."IdEmpresa"=:p_IdEmpresa AND CER."Version"=:p_Version AND CER."IdTipoCertificado"='CERT17'
					 AND EXISTS 
					 ( 
					 
					   SELECT 1 FROM 
						(SELECT COUNT(1) AS "CANT" FROM :p_Rut WHERE LENGTH(LTRIM(RTRIM("Rut")) )>0 ) AS C WHERE C."CANT"=0
			
					  );

			
		v_datos_representante=
            SELECT "DocumentoIdentidadRepresentante" AS "rut_representante", PERSONA."NombreCompleto" AS "nombre_representante" FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMPRESA
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERSONA ON (PERSONA."DocumentoIdentidad" = EMPRESA."DocumentoIdentidadRepresentante" AND EMPRESA."IdEmpresa"=:p_IdEmpresa);
		
		--zona especifica ceritificado
		
        SELECT DOCDJ."DefineFechaCertificado" INTO v_DefineFechaCertificado FROM "BSCL.Normativa.BD::DOC.Documento" DOCDJ
                        WHERE "Esquema" = 'DJ1944' AND "Version" = p_Version AND "IdTipoDocumento" = 'DECLACORR';
                       
        SELECT SUBSTRING(TO_DATE(DOCDJ."FechaCertificado"),1,10) INTO v_FechaCertificado FROM "BSCL.Normativa.BD::DOC.Documento" DOCDJ
                        WHERE "Esquema" = 'DJ1944' AND "Version" = p_Version AND "IdTipoDocumento" = 'DECLACORR';
        
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
                e."RazonSocial" AS "RazonSocialEmpresa", --razon social de la administradora
                e."DocumentoIdentidad" AS "DocumentoIdentidadEmpresa", --rut de la administradora
                e."DireccionFisicaPrincipalCalle" || ' ' || e."DireccionFisicaPrincipalNumero" || ',' || e."DireccionFisicaPrincipalSector" || ' ' || e."DireccionFisicaPrincipalNombreCiudad" AS "DireccionEmpresa", --diraccion de la administradora
                e."RazonSocial" AS "NombreEntidadRecaudadora",
                c."IdCliente",--contribuyente               
                COALESCE(cli."NombreCompleto", 'Pendiente de enrolar') AS "NombreCompletoCliente",--nombre del contribuyente
                cli."DocumentoIdentidad" AS "DocumentoIdentidadCliente",--rut del contribuyente
                e."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:v_FechaSistema,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha", --ciudad y fecha
                G."Nombre" AS "NombreDelGiro", 
                d."SalAhorroNetoEjerNEGT1",
                d."SalAhorroNetoEjerT2",
                d."SalAhorroNetoEjeNegT3",
                d."MtoRenRetFFMMPOS",
                d."MtoRenRetFFMMNEG"
            FROM :v_cerificados c
            INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1944N17" d  ON (d."IdCertificadoCliente" = c."IdCertificadoCliente")
            INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON (TRIM(cli."DocumentoIdentidad") = TRIM(c."RutCliente"))
            INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" e ON (e."IdEmpresa" = c."IdEmpresa")
            INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS S ON e."IdSocioDeNegocio" = S."IdSocioDeNegocio"
            INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS P ON S."IdPersona"=P."IdPersona"
            LEFT JOIN "BSCL.Plataforma.BD::REG.Giro" AS G ON P."IdGiroPrincipal"=G."IdGiro"
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
                        "RazonSocialEmpresa",
                        "DocumentoIdentidadEmpresa",
                        "DireccionEmpresa",
                        "NombreEntidadRecaudadora",
                        "IdCliente",
                        "NombreCompletoCliente",
                        "DocumentoIdentidadCliente",
                        "CiudadFecha",
                        "NombreDelGiro"
                FROM :v_Resultado;

            v_Resultado1 =
                SELECT
                    d."Folio",
                    d."IdEmpresa",
                    d."Esquema",
                    d."Version",
                    d."Anio",
                    d."Periodo", 
                    d."RazonSocialEmpresa",
                    d."DocumentoIdentidadEmpresa",
                    d."DireccionEmpresa",
                    d."NombreEntidadRecaudadora",
                    d."IdCliente",
                    d."NombreCompletoCliente",
                    d."DocumentoIdentidadCliente",
                    d."CiudadFecha",
                    d."NombreDelGiro",
                    d."SalAhorroNetoEjerNEGT1",
                    d."SalAhorroNetoEjerT2",
                    d."SalAhorroNetoEjeNegT3",
                    d."MtoRenRetFFMMPOS",
                    d."MtoRenRetFFMMNEG",
                    COALESCE(c."Certificacion",'NGE') AS "Certificacion",
                    COALESCE(c."EstadoCertificado",'NGE') AS "EstadoCertificado"
                FROM
                (SELECT * FROM :v_Resultado) AS d
                LEFT JOIN "BSCL.Normativa.BD::TRI.ControlProceso" c ON (c."Esquema" = d."Esquema"
                                                                    AND c."Version" = d."Version")
                ORDER BY d."Folio";

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
            ,"Detalle"."NombreDelGiro"
            ,"Detalle"."SalAhorroNetoEjerNEGT1"
            ,"Detalle"."SalAhorroNetoEjerT2"
            ,"Detalle"."SalAhorroNetoEjeNegT3"
            ,"Detalle"."MtoRenRetFFMMPOS"
            ,"Detalle"."MtoRenRetFFMMNEG"
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
            ORDER BY "Detalle"."Folio";--,"Detalle"."Mes";				 

END;
