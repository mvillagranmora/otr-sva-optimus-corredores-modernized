SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1944.GenerarDocumentoCertificado17";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1944.GenerarDocumentoCertificado17"(
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
            
            CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1944.GenerarCertificado17','INICIO','INICIO');
                
            CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
            
            IF :v_resultadoRegla = 'RECH' THEN
                v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
            END IF;
            
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
                    e."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:v_FechaSistema,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha", --ciudad y fecha
                    G."Nombre" AS "NombreDelGiro", 
                    d."SalAhorroNetoEjerNEGT1",
                    d."SalAhorroNetoEjerT2",
                    d."SalAhorroNetoEjeNegT3",
                    d."MtoRenRetFFMMPOS",
                    d."MtoRenRetFFMMNEG"
                FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" c
                INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1944N17" d  ON (d."IdCertificadoCliente" = c."IdCertificadoCliente")
                INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON (TRIM(cli."DocumentoIdentidad") = TRIM(c."RutCliente"))
                INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" e ON (e."IdEmpresa" = c."IdEmpresa")
                INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS S ON e."IdSocioDeNegocio" = S."IdSocioDeNegocio"
				INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS P ON S."IdPersona"=P."IdPersona"
				INNER JOIN "BSCL.Plataforma.BD::REG.Giro" AS G ON P."IdGiroPrincipal"=G."IdGiro"
                WHERE :p_Todos = 'S' OR (c."RutCliente" = :p_RutCliente AND c."CodigoPortafolio" = :p_CodigoPortafolio AND c."Folio" = :p_Folio)
                  AND c."Version" = :p_Version;

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
                        "CiudadFecha",
                        "NombreDelGiro"
                FROM :v_Resultado;

            CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1944.GenerarCertificado17','FIN','FIN');
            
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
			
            v_datos_corredor=
            SELECT "DocumentoIdentidadRepresentante" AS "rut_representante", PERSONA."NombreCompleto" AS "nombre_representante" FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMPRESA
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERSONA ON (PERSONA."DocumentoIdentidad" = EMPRESA."DocumentoIdentidadRepresentante");

                
            SELECT * FROM :v_Resultado1
            CROSS JOIN :v_logo
            CROSS JOIN :v_firma
            CROSS JOIN :v_datos_corredor
            ORDER BY "Folio";      
        END;
    END;                                
END;