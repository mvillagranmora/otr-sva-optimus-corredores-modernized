SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1949.GenerarDocumentoCertificado71";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1949.GenerarDocumentoCertificado71"(
    -- ID empresa del login.
    IN p_IdEmpresa                      BIGINT       ,
    -- ID usuario del login.
    IN p_IdUsuario                      BIGINT       ,
    -- ID modulo invocador.
    IN p_IdModulo                       NVARCHAR(3)  ,
    
    IN p_Version                        NVARCHAR(10) ,
    
    IN p_Todos                          NVARCHAR(1) DEFAULT 'S',
    
    --IN p_IdCliente                      BIGINT      ,
    
    --IN p_IdPortafolio                   BIGINT      DEFAULT 0,

    IN p_RutCliente						NVARCHAR(40)	DEFAULT '',

    IN p_CodigoPortafolio		 		NVARCHAR(100)	DEFAULT '',

    IN p_Folio                          BIGINT      
    
)
    LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
    --DECLARE Manejo de Errores
    --------------------
    DECLARE v_out_AdvertenciasDetalle   TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
    DECLARE v_out_ErroresDetalle        TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
    BEGIN
        v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
        v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
    END;
    BEGIN

        BEGIN
            DECLARE v_resultadoRegla    NVARCHAR(40);
            DECLARE v_FechaSistema      NVARCHAR(10);
            DECLARE v_DefineFechaCertificado NVARCHAR(10);
	        DECLARE v_FechaCertificado NVARCHAR(10);
            
            CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1949.GenerarCertificado71','INICIO','INICIO');
                
            CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
            
            IF :v_resultadoRegla = 'RECH' THEN
                v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
            END IF;
            
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
					d."NroCertificadoF5",
					d."FactorF7",
					d."NroDividendoF2",
					d."MontoF6",
					d."MontoActualizadoF8",
                    c."IdEmpresa",
                    c."Esquema",
                    c."Version",              
                    c."Anio",
                    TO_NVARCHAR(TO_INT(c."Periodo") + 1) AS "Periodo",
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
                    d."FechaDividendo",
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
                    cli."DocumentoIdentidad" AS "DocumentoIdentidadCliente",--rut del contribuyente
                    --
                    e."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:v_FechaSistema,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha" --ciudad y fecha

                FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" c
                INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1949N71" d  ON (d."IdCertificadoCliente" = c."IdCertificadoCliente")
                LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON (TRIM(cli."DocumentoIdentidad") = TRIM(c."RutCliente"))
                LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" soc ON (TRIM(soc."DocumentoIdentidad") = TRIM(d."RutSocPagadora")) 
                INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" e ON (e."IdEmpresa" = c."IdEmpresa")
                WHERE :p_Todos = 'S' OR (c."RutCliente" = :p_RutCliente AND c."CodigoPortafolio" = :p_CodigoPortafolio AND c."Folio" = :p_Folio)
                  AND c."Version" = :p_Version AND c."IdTipoCertificado" = 'CERT71';

            CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1949.GenerarCertificado71','FIN','FIN');
            
            v_logo =
                SELECT TOP 1
                    "Binario" AS "LogoCorredora"
                FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
                WHERE "NombreImagen" = 'LOGO_CORREDOR'
                  AND "IdEmpresa" = :p_IdEmpresa 
                ORDER BY "Version" DESC;
            
            v_firma =
                SELECT TOP 1
                    "Binario" AS "FirmaCorredora"
                FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
                WHERE "NombreImagen" = 'FIRMA_CORREDORA'
                ORDER BY "Version" DESC;

            
            v_datos_corredor=
            SELECT "DocumentoIdentidadRepresentante" AS "rut_representante", PERSONA."NombreCompleto" AS "nombre_representante" FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMPRESA
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERSONA ON (PERSONA."DocumentoIdentidad" = EMPRESA."DocumentoIdentidadRepresentante");
    
            
            SELECT * FROM :v_Resultado
            CROSS JOIN :v_logo
            CROSS JOIN :v_firma
            CROSS JOIN :v_datos_corredor
            ORDER BY "Folio";      
        END;
    END;                                
END;