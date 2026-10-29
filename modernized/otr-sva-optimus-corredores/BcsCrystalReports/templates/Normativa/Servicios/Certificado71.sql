SET SCHEMA "BSCL";
--GO 
DROP PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::Certificado71";
--GO
CREATE PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::Certificado71" (
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
        "NroCertificadoF5"			                    DECIMAL(26,8),
        "FactorF7"		                                DECIMAL(26,8),
        "NroDividendoF2"				                DECIMAL(26,8),
        "MontoF6"		                                DECIMAL(26,8),
        "MontoActualizadoF8"                            DECIMAL(26,8),
        "ConCreditoIDPCDesed2017C6F9"					DECIMAL(26,8),
        "ConCreditoIDPCHasta2017C7F10"			        DECIMAL(26,8),	
        "ConDerechoIDPCVoluntarioC8F11"		            DECIMAL(26,8),	
        "SinDerechoCreditoC9F12"			            DECIMAL(26,8),
        "MtoDivReaREXTribCumpRentasRap_C10F13"		    DECIMAL(26,8),
        "MtoDivReaREXTribCumpOtrasRentas_C11F14"		DECIMAL(26,8),
        "MtoDivReaREXTribCumpExcDist_C12F15"			DECIMAL(26,8),
        "MtoDivReaREXTribCumpUtiISFUT_C13F16"			DECIMAL(26,8),	
        "MtoDivReaREXTribCumpRentasISFUT_C14F17"		DECIMAL(26,8),
        "MtoDivReaREXRentExIGCAfecIAart11_C15F18"       DECIMAL(26,8),
        "RentasExcentasIGCC16F19"                       DECIMAL(26,8),
        "MtoDivReaREXNoConstRenta_C17F20"               DECIMAL(26,8),
        "CIGCDesde01012017RentAfecNoRestSDD_C18F21"     DECIMAL(26,8),
        "CIGCDesde01012017RentAfecNoRestCDD_C19F22"     DECIMAL(26,8),
        "CIGCDesde01012017RentAfecNoRestSDD_C20F23"     DECIMAL(26,8),
        "CIGCDesde01012017RentAfecNoRestCDD_C21F24"     DECIMAL(26,8),
        "CIGCDesde01012017RentAfecRestSDD_C22F25"       DECIMAL(26,8),
        "CIGCDesde01012017RentAfecRestCDD_C23F26"       DECIMAL(26,8),
        "CIGCDesde01012017RentExenRestSDD_C24F27"       DECIMAL(26,8),
        "CIGCDesde01012017RentExenRestCDD_C25F28"       DECIMAL(26,8),
        "CIGCDesde01012017CredIPE_C26F29"               DECIMAL(26,8),
        "CIGCHasta31122016RentAfecSDD_C27F30"           DECIMAL(26,8),
        "CIGCHasta31122016RentAfecCDD_C28F31"           DECIMAL(26,8),
        "CIGCHasta31122016RentExenSDD_C29F32"           DECIMAL(26,8),
        "CIGCHasta31122016RentExenCDD_C30F33"           DECIMAL(26,8),
        "CIGCHasta31122016CredIPE_C31F34"               DECIMAL(26,8),
        "CIGCImptoTasaAdicional_C32F35"                 DECIMAL(26,8),
        "DevCapArt17N7LIR_C33F36"                       DECIMAL(26,8),
        "TasaEfectCredFUT"                              DECIMAL(26,8),
        "TasaEfectCredFUNT"                             DECIMAL(26,8),
        "FechaDividendo"                               	NVARCHAR(300),
        "RutSocPagadora"                                NVARCHAR(30),
        "RazonSocialSocPagadora"                        NVARCHAR(300),
        "TipoSociedad"                                  NVARCHAR(300),
        "RutReceptor"                                   NVARCHAR(40),
        "RazonSocialEmpresa"                            NVARCHAR(300),
        "DocumentoIdentidadEmpresa"                     NVARCHAR(300),
        "DireccionEmpresa"                              NVARCHAR(300),
        "NombreEntidadRecaudadora"                      NVARCHAR(300),
        "IdCliente"                                     BIGINT,
        "NombreCompletoCliente"                         NVARCHAR(300),
        "DocumentoIdentidadCliente"                     NVARCHAR(300),
        "CiudadFecha"                                   NVARCHAR(300),
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
/*cflores*/
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
			 WHERE CER."IdEmpresa"=:p_IdEmpresa AND CER."Version"=:p_Version AND CER."IdTipoCertificado"='CERT71'
			UNION ALL 
			SELECT CER."IdCertificadoCliente", CER."RutCliente", CER."IdEmpresa", CER."Folio", CER."Esquema", CER."Version", CER."Anio", CER."Periodo",CER."IdCliente"
					 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
					 WHERE CER."IdEmpresa"=:p_IdEmpresa AND CER."Version"=:p_Version AND CER."IdTipoCertificado"='CERT71'
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
                        WHERE "Esquema" = 'DJ1949' AND "Version" = p_Version AND "IdTipoDocumento" = 'DECLACORR';
                       
        SELECT SUBSTRING(TO_DATE(DOCDJ."FechaCertificado"),1,10) INTO v_FechaCertificado FROM "BSCL.Normativa.BD::DOC.Documento" DOCDJ
                        WHERE "Esquema" = 'DJ1949' AND "Version" = p_Version AND "IdTipoDocumento" = 'DECLACORR';
        
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
                TO_NVARCHAR(TO_INT(c."Periodo") + 1) AS "Periodo",
                ---
                d."NroCertificadoF5",
                d."FactorF7",
                d."NroDividendoF2",
                d."MontoF6",
                d."MontoActualizadoF8",
                --Montos Retiros Reajustados
                d."ConCreditoIDPCDesed2017C6F9",
                d."ConCreditoIDPCHasta2017C7F10",
                d."ConDerechoIDPCVoluntarioC8F11",
                d."SinDerechoCreditoC9F12",
                --Rentas con tributación cumplida
                d."MtoDivReaREXTribCumpRentasRap_C10F13",
                d."MtoDivReaREXTribCumpOtrasRentas_C11F14",
                d."MtoDivReaREXTribCumpExcDist_C12F15",
                d."MtoDivReaREXTribCumpUtiISFUT_C13F16",
                d."MtoDivReaREXTribCumpRentasISFUT_C14F17",
                --Rentas Excentas
                d."MtoDivReaREXRentExIGCAfecIAart11_C15F18",
                d."RentasExcentasIGCC16F19",
                d."MtoDivReaREXNoConstRenta_C17F20",
                --Resto
                d."CIGCDesde01012017RentAfecNoRestSDD_C18F21",
                d."CIGCDesde01012017RentAfecNoRestCDD_C19F22",
                d."CIGCDesde01012017RentAfecNoRestSDD_C20F23",
                d."CIGCDesde01012017RentAfecNoRestCDD_C21F24",

                d."CIGCDesde01012017RentAfecRestSDD_C22F25",
                d."CIGCDesde01012017RentAfecRestCDD_C23F26",
                d."CIGCDesde01012017RentExenRestSDD_C24F27",
                d."CIGCDesde01012017RentExenRestCDD_C25F28",

                d."CIGCDesde01012017CredIPE_C26F29",
                d."CIGCHasta31122016RentAfecSDD_C27F30",
                d."CIGCHasta31122016RentAfecCDD_C28F31",
                d."CIGCHasta31122016RentExenSDD_C29F32",
                d."CIGCHasta31122016RentExenCDD_C30F33",
                
                d."CIGCHasta31122016CredIPE_C31F34",
                d."CIGCImptoTasaAdicional_C32F35",
                d."DevCapArt17N7LIR_C33F36",
                d."TasaEfectCredFUT", 
                d."TasaEfectCredFUNT",
                --
                TO_CHAR(TO_DATE(d."FechaDividendo",'YYYY-MM-DD'),'DD-MM-YYYY') AS "FechaDividendo",
                d."RutSocPagadora",
                CASE WHEN LTRIM(RTRIM(COALESCE(soc."RazonSocial",''))) = '' THEN COALESCE(soc."NombreCompleto",'') ELSE COALESCE(soc."RazonSocial",'') END AS "RazonSocialSocPagadora",
                d."TipoSociedad",
                d."RutReceptor",
                --Institución Autorizada
                e."RazonSocial" AS "RazonSocialEmpresa", --razon social de la administradora
                e."DocumentoIdentidad" AS "DocumentoIdentidadEmpresa", --rut de la administradora
                e."DireccionFisicaPrincipalCalle" || ' ' || e."DireccionFisicaPrincipalNumero" || ',' || e."DireccionFisicaPrincipalSector" || ' ' || e."DireccionFisicaPrincipalNombreCiudad" AS "DireccionEmpresa", --diraccion de la administradora
                --La Administradora de Fondos de Pensiones...
                e."RazonSocial" AS "NombreEntidadRecaudadora",
                --trabajador
                c."IdCliente",--contribuyente               
                COALESCE(cli."NombreCompleto", 'Pendiente de enrolar') AS "NombreCompletoCliente",--nombre del contribuyente
                COALESCE(cli."DocumentoIdentidad",'0') AS "DocumentoIdentidadCliente",--rut del contribuyente
                --
                e."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:v_FechaSistema,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha" --ciudad y fecha
            FROM :v_cerificados c
            INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1949N71" d  ON (d."IdCertificadoCliente" = c."IdCertificadoCliente")
            LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON (TRIM(cli."DocumentoIdentidad") = TRIM(c."RutCliente"))
            LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" soc ON (TRIM(soc."DocumentoIdentidad") = TRIM(d."RutSocPagadora")) 
            INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" e ON (e."IdEmpresa" = c."IdEmpresa")
            WHERE c."Version" = :p_Version;	


		p_Salida = SELECT
				 "Detalle"."Folio"
				,"Detalle"."IdEmpresa"
				,"Detalle"."Esquema"
				,"Detalle"."Version"
				,"Detalle"."Anio"
				,"Detalle"."Periodo"
                ---
				,"Detalle"."NroCertificadoF5"
				,"Detalle"."FactorF7"
				,"Detalle"."NroDividendoF2"
				,"Detalle"."MontoF6"
                ,"Detalle"."MontoActualizadoF8"
				,"Detalle"."ConCreditoIDPCDesed2017C6F9"
                
                ,"Detalle"."ConCreditoIDPCHasta2017C7F10"
                ,"Detalle"."ConDerechoIDPCVoluntarioC8F11"
                ,"Detalle"."SinDerechoCreditoC9F12"
                ,"Detalle"."MtoDivReaREXTribCumpRentasRap_C10F13"
                ,"Detalle"."MtoDivReaREXTribCumpOtrasRentas_C11F14"

                ,"Detalle"."MtoDivReaREXTribCumpExcDist_C12F15"
                ,"Detalle"."MtoDivReaREXTribCumpUtiISFUT_C13F16"
                ,"Detalle"."MtoDivReaREXTribCumpRentasISFUT_C14F17"
                ,"Detalle"."MtoDivReaREXRentExIGCAfecIAart11_C15F18"
                ,"Detalle"."RentasExcentasIGCC16F19"
                
                ,"Detalle"."MtoDivReaREXNoConstRenta_C17F20"
                ,"Detalle"."CIGCDesde01012017RentAfecNoRestSDD_C18F21"
                ,"Detalle"."CIGCDesde01012017RentAfecNoRestCDD_C19F22"
                ,"Detalle"."CIGCDesde01012017RentAfecNoRestSDD_C20F23"
                ,"Detalle"."CIGCDesde01012017RentAfecNoRestCDD_C21F24"
                
                ,"Detalle"."CIGCDesde01012017RentAfecRestSDD_C22F25"
                ,"Detalle"."CIGCDesde01012017RentAfecRestCDD_C23F26"
                ,"Detalle"."CIGCDesde01012017RentExenRestSDD_C24F27"
                ,"Detalle"."CIGCDesde01012017RentExenRestCDD_C25F28"
                ,"Detalle"."CIGCDesde01012017CredIPE_C26F29"
                
                ,"Detalle"."CIGCHasta31122016RentAfecSDD_C27F30"
                ,"Detalle"."CIGCHasta31122016RentAfecCDD_C28F31"
                ,"Detalle"."CIGCHasta31122016RentExenSDD_C29F32"
                ,"Detalle"."CIGCHasta31122016RentExenCDD_C30F33"
                ,"Detalle"."CIGCHasta31122016CredIPE_C31F34"
                
                ,"Detalle"."CIGCImptoTasaAdicional_C32F35"
                ,"Detalle"."DevCapArt17N7LIR_C33F36"
                ,"Detalle"."TasaEfectCredFUT"
                ,"Detalle"."TasaEfectCredFUNT"
                ,"Detalle"."FechaDividendo"
                ,"Detalle"."RutSocPagadora"
                ,"Detalle"."RazonSocialSocPagadora"
                ,"Detalle"."TipoSociedad"
                ,"Detalle"."RutReceptor"
                ,"Detalle"."RazonSocialEmpresa"
                ,"Detalle"."DocumentoIdentidadEmpresa"
                
                ,"Detalle"."DireccionEmpresa"
                ,"Detalle"."NombreEntidadRecaudadora"
                ,"Detalle"."IdCliente"
                ,"Detalle"."NombreCompletoCliente"
                ,"Detalle"."DocumentoIdentidadCliente"
                ,"Detalle"."CiudadFecha"
				-------------------------
				,"Corredor"."RazonSocial" AS "RazonSocialCorredora"
				,"Corredor"."Direccion" AS "DireccionCorredora"
				,"Corredor"."DocumentoIdentidad" AS "DocumentoIdentidadCorredora"
				,"Rep"."rut_representante"
				,"Rep"."nombre_representante"
				,"Detalle"."DocumentoIdentidadCliente" AS "RutCertificado"
				,"Logo"."LogoCorredora"
				,"Firma"."FirmaCorredora"
                FROM :v_Resultado AS "Detalle"
                LEFT JOIN :v_DatosCorredor AS "Corredor" ON "Corredor"."IdEmpresa"=:p_IdEmpresa
                LEFT JOIN :v_Logo AS "Logo" ON "Logo"."LogoCorredora" IS NOT NULL
                LEFT JOIN :v_Firma AS "Firma" ON "Firma"."FirmaCorredora" IS NOT NULL
                LEFT JOIN :v_datos_representante AS "Rep" ON "Rep"."rut_representante" IS NOT NULL
            ORDER BY "Detalle"."Folio";--,"Detalle"."Mes";				 

END;
