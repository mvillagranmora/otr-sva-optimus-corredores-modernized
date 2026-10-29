SET SCHEMA BSCL;

--DJ1949N71
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1949.GenerarCertificado71";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1949.GenerarCertificado71"(
    -- ID empresa del login.
    IN p_IdEmpresa  BIGINT       ,
    -- ID usuario del login.
    IN p_IdUsuario  BIGINT       ,
    -- ID modulo invocador.
    IN p_IdModulo   NVARCHAR(3)  ,
    IN p_Version    NVARCHAR(10) ,
    IN p_Todos      NVARCHAR(1) DEFAULT 'S',
    IN p_Clientes   TABLE (
                        "IdCliente" BIGINT
                    ),
    OUT p_out_AdvertenciasContador      INT,
    OUT p_out_AdvertenciasDetalle       TABLE ("IdInterno"  BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
    OUT p_out_ErroresContador           INT,
    OUT p_out_ErroresDetalle            TABLE ("IdInterno"  BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
)
    LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
    --------------------
    --DECLARE Manejo de Errores
    --------------------
    DECLARE v_out_AdvertenciasDetalle   TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
    DECLARE v_out_ErroresDetalle        TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
    BEGIN
        v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
        v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
    END;
    BEGIN
        DECLARE SALIDA_CONTROLADA CONDITION;
        DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA  
        BEGIN
            select count(1) into p_out_ErroresContador 
            from :v_out_ErroresDetalle;
            select count(1) into p_out_AdvertenciasContador 
            from :v_out_AdvertenciasDetalle;
            
            v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
                                    "NombreParametro" as "CodigoDato",
                                    "ValorParametro" as "ValorDato" 
                                    FROM :v_out_ErroresDetalle
                                    UNION ALL
                                    SELECT "Codigo" AS "CodigoMensaje",
                                    "NombreParametro" as "CodigoDato",
                                    "ValorParametro" as "ValorDato" 
                                    FROM :v_out_AdvertenciasDetalle;
                                    
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
            
            p_out_ErroresDetalle = SELECT   "errores"."IdInterno",
                                            "errores"."Codigo",
                                            "msj"."Descripcion"
                                    FROM :v_out_ErroresDetalle AS "errores"
                                    INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "errores"."Codigo" = "msj"."Codigo";
            
            p_out_AdvertenciasDetalle = SELECT  "adv"."IdInterno",
                                                "adv"."Codigo",
                                                "adv"."RequiereAutorizacion",
                                                "msj"."Descripcion"
                                        FROM :v_out_AdvertenciasDetalle AS "adv"
                                        INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "adv"."Codigo" = "msj"."Codigo"; 
        END;
        
        p_out_AdvertenciasContador  := 0;
        p_out_ErroresContador       := 0;
        
        BEGIN
            DECLARE v_resultadoRegla    NVARCHAR(40);
            DECLARE v_FechaSistema      NVARCHAR(10);
            DECLARE v_count_folio       BIGINT;
            DECLARE v_count_cli		    BIGINT=0;
            DECLARE V_ultimo_folio      BIGINT;
            DECLARE v_max_folio         BIGINT;
            DECLARE count_certificados  BIGINT;
            CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1949.GenerarCertificado71','INICIO','INICIO');
                
            CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
            
            IF :v_resultadoRegla = 'RECH' THEN
                v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
                SIGNAL SALIDA_CONTROLADA;   
            END IF;
           
           /*rut cliente*/
       		SELECT COUNT(1)  INTO v_count_cli  FROM :p_Clientes;      
       	     IF :p_Todos = 'N' AND v_count_cli>0 THEN
       	     	v_cliente = SELECT cl."DocumentoIdentidad" AS "RutCliente", cl."IdCliente"   FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS cl 
						               INNER JOIN :p_Clientes parcli ON (parcli."IdCliente" = cl."IdCliente");
       	     ELSE
       	     
       	    	 v_cliente = 
       	    	 SELECT DISTINCT DAT."RutCliente",max(COALESCE(cl."IdCliente",0)) AS "IdCliente"
       	    	 FROM
       	    	 (
       	    			 SELECT trim(UPPER(dj."RutCliente")) AS "RutCliente"  
       	    	 				FROM "BSCL.Normativa.BD::TRI.DatosDJ1949" AS dj
				                INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" AS d ON (d."IdDocumento" = dj."IdDocumento")
						        WHERE d."Version" = :p_Version  
						 UNION ALL
						 
						 SELECT trim(UPPER(dj."RutCliente")) AS "RutCliente"  
       	    	 				FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1949N71" AS dj
				                INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" AS d ON (d."IdDocumento" = dj."IdDocumento")
						        WHERE d."Version" = :p_Version  
						     
				)DAT  
				LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS cl ON (trim(DAT."RutCliente") = trim(cl."DocumentoIdentidad"))
				GROUP BY DAT."RutCliente" ;	              
						              
						              
       	     END IF; 
           
            
            CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
            
            SELECT COUNT(1) 
              INTO v_count_folio
              FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
             WHERE "Esquema" = 'DJ1949' 
               AND "Version" = :p_Version 
               AND "CodigoCertificado" = 'CERTIFICADO_71';
            
            IF (:v_count_folio = 0) THEN
                INSERT INTO "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
                ("Esquema","Version","UltimoFolio","CodigoCertificado","NombreCertificado","CodigoPrograma")
                VALUES('DJ1949',:p_Version,0,'CERTIFICADO_71','Certificado 71','GenerarCertificado71');
            END IF;
            
            SELECT "UltimoFolio" 
              INTO v_ultimo_folio
              FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
             WHERE "Esquema" = 'DJ1949' 
               AND "Version" = :p_Version 
               AND "CodigoCertificado" = 'CERTIFICADO_71';
            
            v_datos =
                SELECT
                    dj."ID",
                    dj."IdDocumento",
                    d."Version" AS "Version",
                    d."IdTipoDocumento",
                    :p_IdEmpresa AS "IdEmpresa",
                   	COALESCE(cl."IdCliente",0)  AS "IdCliente",
                    dj."FecMovto" AS "FechaDividendo",
                    dj."RutSociedad" AS "RutSocPagadora",
                    dj."TipoSociedad",
                    dj."RutCliente" AS "RutReceptor",
                    dj."Monto" AS "MontoF6",
					dj."MontoActualizado" AS "MontoActualizadoF8",
                    --Montos Retiros reajustados
                    dj."MtoDivReaAfecGCCCIDPC_C6" AS  "ConCreditoIDPCDesed2017C6F9",
                    dj."MtoDivReaAfecGCCCIDPCAL311216_C7" AS "ConCreditoIDPCHasta2017C7F10",
                    dj."MtoDivReaAfecGCCDIDPCVol_C8" AS "ConDerechoIDPCVoluntarioC8F11",
                    dj."MtoDivReaAfecGCSinDer_C9" AS "SinDerechoCreditoC9F12",
					--Rentas con tributaci�n cumplida
					dj."MtoDivReaREXTribCumpRentasRap_C10" AS "MtoDivReaREXTribCumpRentasRap_C10F13",
					dj."MtoDivReaREXTribCumpOtrasRentas_C11" AS "MtoDivReaREXTribCumpOtrasRentas_C11F14",
					dj."MtoDivReaREXTribCumpExcDist_C12" AS "MtoDivReaREXTribCumpExcDist_C12F15",
					dj."MtoDivReaREXTribCumpUtiISFUT_C13" AS "MtoDivReaREXTribCumpUtiISFUT_C13F16",
					dj."MtoDivReaREXTribCumpRentasISFUT_C14" AS "MtoDivReaREXTribCumpRentasISFUT_C14F17",
					--Rentas Excentas
					dj."MtoDivReaREXRentExIGCAfecIAart11_C15" AS "MtoDivReaREXRentExIGCAfecIAart11_C15F18",
					dj."MtoDivReaREXRentExIGCIA_C16" AS "RentasExcentasIGCC16F19",
					dj."MtoDivReaREXNoConstRenta_C17" AS "MtoDivReaREXNoConstRenta_C17F20",
					--Resto
					dj."CIGCDesde01012017RentAfecNoRestSDD_C18" AS "CIGCDesde01012017RentAfecNoRestSDD_C18F21",
					dj."CIGCDesde01012017RentAfecNoRestCDD_C19"	AS "CIGCDesde01012017RentAfecNoRestCDD_C19F22",
					dj."CIGCDesde01012017RentAfecNoRestSDD_C20" AS "CIGCDesde01012017RentAfecNoRestSDD_C20F23",
					dj."CIGCDesde01012017RentAfecNoRestCDD_C21" AS "CIGCDesde01012017RentAfecNoRestCDD_C21F24",
					dj."CIGCDesde01012017RentAfecRestSDD_C22" AS "CIGCDesde01012017RentAfecRestSDD_C22F25",
					dj."CIGCDesde01012017RentAfecRestCDD_C23" AS "CIGCDesde01012017RentAfecRestCDD_C23F26",
					dj."CIGCDesde01012017RentExenRestSDD_C24" AS "CIGCDesde01012017RentExenRestSDD_C24F27",
					dj."CIGCDesde01012017RentExenRestCDD_C25" AS "CIGCDesde01012017RentExenRestCDD_C25F28",
					dj."CIGCDesde01012017CredIPE_C26" AS "CIGCDesde01012017CredIPE_C26F29",
					dj."CIGCHasta31122016RentAfecSDD_C27" AS "CIGCHasta31122016RentAfecSDD_C27F30",
					dj."CIGCHasta31122016RentAfecCDD_C28" AS "CIGCHasta31122016RentAfecCDD_C28F31",
					dj."CIGCHasta31122016RentExenSDD_C29" AS "CIGCHasta31122016RentExenSDD_C29F32",
					dj."CIGCHasta31122016RentExenCDD_C30" AS "CIGCHasta31122016RentExenCDD_C30F33",
					dj."CIGCHasta31122016CredIPE_C31" AS "CIGCHasta31122016CredIPE_C31F34",
					dj."CIGCImptoTasaAdicional_C32" AS	"CIGCImptoTasaAdicional_C32F35",
					dj."DevCapArt17N7LIR_C33" AS "DevCapArt17N7LIR_C33F36",
					dj."TasaEfectCredFUT",
					dj."TasaEfectCredFUNT",
					0 AS "NroCertificadoF5",
					round((IPCACT."Valor"/IPCINV."Valor"),3)  AS "FactorF7",
					0 AS "NroDividendoF2",
                    TRIM(UPPER(dj."RutCliente")) AS "RutCliente",
                    '0' AS "CodigoPortafolio"
                FROM "BSCL.Normativa.BD::TRI.DatosDJ1949" AS dj
                INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" AS d ON (d."IdDocumento" = dj."IdDocumento")
                LEFT OUTER JOIN :v_cliente AS cl ON (trim(UPPER(dj."RutCliente")) = cl."RutCliente")
                --LEFT OUTER JOIN :p_Clientes parcli ON (parcli."IdCliente" = cl."IdCliente")
                LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCINV  ON (substring(ADD_MONTHS (TO_DATE (dj."FecMovto", 'YYYY-MM-DD'), -1),0,7) = IPCINV."AgnoMes")  AND IPCINV."IdParametroFinanciero" = 'IPC'
				LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCACT  ON (SUBSTRING(dj."FecMovto",0,4) || '-11' = IPCACT."AgnoMes")  AND IPCACT."IdParametroFinanciero" = 'IPC'
                WHERE d."Version" = :p_Version;
                --  AND (:p_Todos = 'S' OR (:p_Todos = 'N' AND COALESCE(parcli."IdCliente",0) != 0 AND trim(dj."RutCliente") = cl."DocumentoIdentidad" ));

            v_datos_agrupados =
                SELECT
                	max("IdCliente") AS "IdCliente",
                    "IdDocumento",
                    "Version",
                    "IdEmpresa",
                    "FechaDividendo",
                    "IdTipoDocumento",
                    "RutSocPagadora",
                    "TipoSociedad",
                    "RutReceptor",
                    --DatosDividendo
					"NroCertificadoF5",
					"FactorF7",
					"NroDividendoF2",
					"MontoF6",
					"MontoActualizadoF8",
                    --Montos Retiros Reajustados
                    SUM("ConCreditoIDPCDesed2017C6F9") AS "ConCreditoIDPCDesed2017C6F9",
                    SUM("ConCreditoIDPCHasta2017C7F10") AS "ConCreditoIDPCHasta2017C7F10",
                    SUM("ConDerechoIDPCVoluntarioC8F11") AS "ConDerechoIDPCVoluntarioC8F11",
                    SUM("SinDerechoCreditoC9F12") AS "SinDerechoCreditoC9F12",
                    --Rentas con tributaci�n cumplida
					SUM("MtoDivReaREXTribCumpRentasRap_C10F13") AS "MtoDivReaREXTribCumpRentasRap_C10F13",
					SUM("MtoDivReaREXTribCumpOtrasRentas_C11F14") AS "MtoDivReaREXTribCumpOtrasRentas_C11F14",
					SUM("MtoDivReaREXTribCumpExcDist_C12F15") AS "MtoDivReaREXTribCumpExcDist_C12F15",
					SUM("MtoDivReaREXTribCumpUtiISFUT_C13F16") AS "MtoDivReaREXTribCumpUtiISFUT_C13F16",
					SUM("MtoDivReaREXTribCumpRentasISFUT_C14F17") AS "MtoDivReaREXTribCumpRentasISFUT_C14F17",
					--Rentas Excentas
					SUM("MtoDivReaREXRentExIGCAfecIAart11_C15F18") AS "MtoDivReaREXRentExIGCAfecIAart11_C15F18",
					SUM("RentasExcentasIGCC16F19") AS "RentasExcentasIGCC16F19",
					SUM("MtoDivReaREXNoConstRenta_C17F20") AS "MtoDivReaREXNoConstRenta_C17F20",
					--Resto
					SUM("CIGCDesde01012017RentAfecNoRestSDD_C18F21") AS "CIGCDesde01012017RentAfecNoRestSDD_C18F21",
					SUM("CIGCDesde01012017RentAfecNoRestCDD_C19F22") AS "CIGCDesde01012017RentAfecNoRestCDD_C19F22",
					SUM("CIGCDesde01012017RentAfecNoRestSDD_C20F23") AS "CIGCDesde01012017RentAfecNoRestSDD_C20F23",
					SUM("CIGCDesde01012017RentAfecNoRestCDD_C21F24") AS "CIGCDesde01012017RentAfecNoRestCDD_C21F24",
					SUM("CIGCDesde01012017RentAfecRestSDD_C22F25") AS "CIGCDesde01012017RentAfecRestSDD_C22F25",
					SUM("CIGCDesde01012017RentAfecRestCDD_C23F26") AS "CIGCDesde01012017RentAfecRestCDD_C23F26",
					SUM("CIGCDesde01012017RentExenRestSDD_C24F27") AS "CIGCDesde01012017RentExenRestSDD_C24F27",
					SUM("CIGCDesde01012017RentExenRestCDD_C25F28") AS "CIGCDesde01012017RentExenRestCDD_C25F28",
					SUM("CIGCDesde01012017CredIPE_C26F29") AS "CIGCDesde01012017CredIPE_C26F29",
					SUM("CIGCHasta31122016RentAfecSDD_C27F30") AS "CIGCHasta31122016RentAfecSDD_C27F30",
					SUM("CIGCHasta31122016RentAfecCDD_C28F31") AS "CIGCHasta31122016RentAfecCDD_C28F31",
					SUM("CIGCHasta31122016RentExenSDD_C29F32") AS "CIGCHasta31122016RentExenSDD_C29F32",
					SUM("CIGCHasta31122016RentExenCDD_C30F33") AS "CIGCHasta31122016RentExenCDD_C30F33",
					SUM("CIGCHasta31122016CredIPE_C31F34") AS "CIGCHasta31122016CredIPE_C31F34",
					SUM("CIGCImptoTasaAdicional_C32F35") AS "CIGCImptoTasaAdicional_C32F35",
					SUM("DevCapArt17N7LIR_C33F36") AS "DevCapArt17N7LIR_C33F36",
					"TasaEfectCredFUT",
					"TasaEfectCredFUNT",
                    "RutCliente",
                    "CodigoPortafolio"
                FROM :v_datos
                GROUP BY "TasaEfectCredFUT", "TasaEfectCredFUNT", "MontoF6", "MontoActualizadoF8", "IdTipoDocumento", "FechaDividendo", "IdDocumento", "NroCertificadoF5", "FactorF7", "NroDividendoF2", "Version","IdEmpresa", "FechaDividendo", "RutSocPagadora", "TipoSociedad", "RutReceptor","RutCliente","CodigoPortafolio";


            v_datos_certificado = 
                SELECT DISTINCT "IdCliente", "Version","IdDocumento","IdEmpresa", "IdTipoDocumento","RutCliente","CodigoPortafolio" FROM :v_datos_agrupados;

            v_datos_sin_certificado =
                SELECT
                    "BSCL.Normativa.BD.Secuencias::TRI.IdCertificadoCliente".NEXTVAL AS "IdCertificadoCliente",
                    :v_ultimo_folio + "Numero" AS "Folio",
                    "RutCliente",
					"CodigoPortafolio",
                    "IdTipoDocumento",
                    "IdEmpresa",
                    "Version"
                FROM (
                        SELECT
                        	ROW_NUMBER() OVER(ORDER BY d."CodigoPortafolio") AS "Numero",
                            d."RutCliente",
                            d."CodigoPortafolio",
                            d."IdTipoDocumento",
                            d."IdEmpresa",
                            d."Version"
                        FROM :v_datos_certificado d
                        WHERE NOT EXISTS (  
                                           SELECT 1 FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" c
                                           WHERE c."RutCliente" = d."RutCliente"
                                             AND c."CodigoPortafolio" = d."CodigoPortafolio"
                                             AND c."Esquema" = 'DJ1949'
                                             AND c."Version" = d."Version"
                                             AND c."IdEmpresa" = d."IdEmpresa"
                                             AND c."IdTipoCertificado" = 'CERT71')
                );
            
            v_detalle_sin_certificado =
                SELECT
                    "BSCL.Normativa.BD.Secuencias::TRI.IdDetalleCertificadoClienteDJ1949N71".NEXTVAL AS "IdDetalleCertificadoClienteDJ1949N71",
                    d."IdDocumento",
                    d."Version",
                    d."IdEmpresa",
                    d."IdCliente",
                    d."IdTipoDocumento",
                    d."FechaDividendo",
                    d."RutSocPagadora",
                    d."TipoSociedad",
                    d."RutReceptor",
                    --Montos Retiros Reajustados
                    d."ConCreditoIDPCDesed2017C6F9",
                    d."ConCreditoIDPCHasta2017C7F10",
                    d."ConDerechoIDPCVoluntarioC8F11",
                    d."SinDerechoCreditoC9F12",
					--Rentas con tributaci�n cumplida
					d."MtoDivReaREXTribCumpRentasRap_C10F13",
					d."MtoDivReaREXTribCumpOtrasRentas_C11F14",
					d."MtoDivReaREXTribCumpExcDist_C12F15",
					d."MtoDivReaREXTribCumpUtiISFUT_C13F16",
					d."MtoDivReaREXTribCumpRentasISFUT_C14F17",
					--Rentas Excentas
					d."MtoDivReaREXRentExIGCAfecIAart11_C15F18",
					d."RentasExcentasIGCC16F19",
					d."MtoDivReaREXNoConstRenta_C17F20",
                    c."IdCertificadoCliente",
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
					--Datos Dividendo
					d."NroCertificadoF5",
					d."FactorF7",
					d."NroDividendoF2",
					d."MontoF6",
					d."MontoActualizadoF8",   
                    d."RutCliente",
                    d."CodigoPortafolio"
                FROM :v_datos_agrupados d
                INNER JOIN :v_datos_sin_certificado c ON (c."RutCliente" = d."RutCliente"
                                                      AND c."CodigoPortafolio" = d."CodigoPortafolio"
                                                      AND c."IdEmpresa" = d."IdEmpresa"
                                                      AND c."Version" = d."Version"
                                                      AND c."IdTipoDocumento" = d."IdTipoDocumento");
            --Se mantiene
            v_datos_con_certificado =
                SELECT
                    c."IdCertificadoCliente",
                    c."Folio",
                    d."RutCliente",
					d."CodigoPortafolio",
                    d."IdTipoDocumento",
                    d."IdEmpresa",
                    d."Version"
                FROM :v_datos_certificado d
                INNER JOIN "BSCL.Normativa.BD::TRI.CertificadoCliente" c    ON (c."RutCliente" = d."RutCliente"
                                                                            AND c."CodigoPortafolio" = d."CodigoPortafolio"
                                                                            AND c."Esquema" = 'DJ1949'
                                                                            AND c."Version" = d."Version"
                                                                            AND c."IdEmpresa" = d."IdEmpresa"
                                                                            AND c."IdTipoCertificado" = 'CERT71'); 
               -- WHERE EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1949N71" de WHERE de."IdCertificadoCliente" = c."IdCertificadoCliente");
            

            v_detalle_con_certificado =
                SELECT
                    "BSCL.Normativa.BD.Secuencias::TRI.IdDetalleCertificadoClienteDJ1949N71".NEXTVAL AS "IdDetalleCertificadoClienteDJ1949N71",
                    d."IdCliente",
                    d."IdDocumento",
                    d."Version",
                    d."IdEmpresa",
                    d."IdTipoDocumento",
                    d."FechaDividendo",
                    d."RutSocPagadora",
                    d."TipoSociedad",
                    d."RutReceptor",
                    --Montos Retiros Reajustados
                    d."ConCreditoIDPCDesed2017C6F9",
                    d."ConCreditoIDPCHasta2017C7F10",
                    d."ConDerechoIDPCVoluntarioC8F11",
                    d."SinDerechoCreditoC9F12",
					--Rentas con tributaci�n cumplida
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
                    c."IdCertificadoCliente",
                    --Datos Dividendo
					d."NroCertificadoF5",
					d."FactorF7",
					d."NroDividendoF2",
					d."MontoF6",
					d."MontoActualizadoF8",
					d."TasaEfectCredFUT", 
					d."TasaEfectCredFUNT",
                    d."RutCliente",
                    d."CodigoPortafolio"
                FROM :v_datos_agrupados d
                INNER JOIN :v_datos_con_certificado c   ON (c."RutCliente" = d."RutCliente"
                                                        AND c."CodigoPortafolio" = d."CodigoPortafolio"
                                                        AND c."IdEmpresa" = d."IdEmpresa"
                                                        AND c."Version" = d."Version"
                                                        AND c."IdTipoDocumento" = d."IdTipoDocumento");
			

            INSERT INTO "BSCL.Normativa.BD::TRI.CertificadoCliente"(
                "IdCertificadoCliente", "IdTipoDocumento", "IdEmpresa", 
                "Esquema", "Version", "Anio", "Periodo", 
                "IdCliente", "IdPortafolio", "Folio", 
                "IdTipoCertificado", "RutCliente", "CodigoPortafolio")
            SELECT 
                "IdCertificadoCliente","IdTipoDocumento","IdEmpresa",
                'DJ1949',"Version","Version","Version",
                0 AS "IdCliente", 0 AS "IdPortafolio","Folio",
                'CERT71',TRIM(UPPER("RutCliente")),"CodigoPortafolio"
            FROM :v_datos_sin_certificado;
            
              --Se actualizan los folios en la tabla de datos en la dj
            UPDATE Datos
                SET "NumCertificado_C34" = Cert."Folio"
                FROM "BSCL.Normativa.BD::TRI.DatosDJ1949" Datos
              	INNER JOIN "BSCL.Normativa.BD::TRI.CertificadoCliente" Cert 
              	ON Cert."Esquema" = 'DJ1949'
			    AND TRIM(UPPER(Datos."RutCliente")) = Cert."RutCliente";
			   
			  
			DELETE  FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1949N71"  
			WHERE "Version"=:p_Version
		    AND  TRIM(UPPER("RutCliente")) IN ( SELECT "RutCliente" FROM  :v_cliente);
   
			  
              						
            INSERT INTO "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1949N71"(
            "IdDetalleCertificadoClienteDJ1949N71", 
            "IdCertificadoCliente", 
            "IdTipoDocumento",
            "Version",
            "IdCliente",
            --Montos Retiros Reajustados
            "ConCreditoIDPCDesed2017C6F9",
            "ConCreditoIDPCHasta2017C7F10",
            "ConDerechoIDPCVoluntarioC8F11",
            "SinDerechoCreditoC9F12",
            --Rentas con tributaci�n cumplida
			"MtoDivReaREXTribCumpRentasRap_C10F13",
			"MtoDivReaREXTribCumpOtrasRentas_C11F14",
			"MtoDivReaREXTribCumpExcDist_C12F15",
			"MtoDivReaREXTribCumpUtiISFUT_C13F16",
			"MtoDivReaREXTribCumpRentasISFUT_C14F17",
			--Rentas Excentas
			"MtoDivReaREXRentExIGCAfecIAart11_C15F18",
			"RentasExcentasIGCC16F19",
			"MtoDivReaREXNoConstRenta_C17F20",
			--Resto
            "CIGCDesde01012017RentAfecNoRestSDD_C18F21",
			"CIGCDesde01012017RentAfecNoRestCDD_C19F22",
			"CIGCDesde01012017RentAfecNoRestSDD_C20F23",
			"CIGCDesde01012017RentAfecNoRestCDD_C21F24",
			"CIGCDesde01012017RentAfecRestSDD_C22F25",
			"CIGCDesde01012017RentAfecRestCDD_C23F26",
			"CIGCDesde01012017RentExenRestSDD_C24F27",
			"CIGCDesde01012017RentExenRestCDD_C25F28",
			"CIGCDesde01012017CredIPE_C26F29",
			"CIGCHasta31122016RentAfecSDD_C27F30",
			"CIGCHasta31122016RentAfecCDD_C28F31",
			"CIGCHasta31122016RentExenSDD_C29F32",
			"CIGCHasta31122016RentExenCDD_C30F33",
			"CIGCHasta31122016CredIPE_C31F34",
			"CIGCImptoTasaAdicional_C32F35",
			"DevCapArt17N7LIR_C33F36",
			--
			"IdDocumento",
			"IdEmpresa",
			"FechaDividendo",
            "RutSocPagadora",
            "TipoSociedad",
            "RutReceptor",
            --Datos Dividendo
			"NroCertificadoF5",
			"FactorF7",
			"NroDividendoF2",
			"MontoF6",
			"MontoActualizadoF8",
			"TasaEfectCredFUT", 
			"TasaEfectCredFUNT",
            "RutCliente"
            )
            SELECT
                "IdDetalleCertificadoClienteDJ1949N71",
                "IdCertificadoCliente",
                "IdTipoDocumento",
                "Version",
                "IdCliente",
				"ConCreditoIDPCDesed2017C6F9",
            	"ConCreditoIDPCHasta2017C7F10",
            	"ConDerechoIDPCVoluntarioC8F11",
            	"SinDerechoCreditoC9F12",
            	--Rentas con tributaci�n cumplida
			"MtoDivReaREXTribCumpRentasRap_C10F13",
			"MtoDivReaREXTribCumpOtrasRentas_C11F14",
			"MtoDivReaREXTribCumpExcDist_C12F15",
			"MtoDivReaREXTribCumpUtiISFUT_C13F16",
			"MtoDivReaREXTribCumpRentasISFUT_C14F17",
			--Rentas Excentas
			"MtoDivReaREXRentExIGCAfecIAart11_C15F18",
			"RentasExcentasIGCC16F19",
			"MtoDivReaREXNoConstRenta_C17F20",
			--Resto
            "CIGCDesde01012017RentAfecNoRestSDD_C18F21",
			"CIGCDesde01012017RentAfecNoRestCDD_C19F22",
			"CIGCDesde01012017RentAfecNoRestSDD_C20F23",
			"CIGCDesde01012017RentAfecNoRestCDD_C21F24",
			"CIGCDesde01012017RentAfecRestSDD_C22F25",
			"CIGCDesde01012017RentAfecRestCDD_C23F26",
			"CIGCDesde01012017RentExenRestSDD_C24F27",
			"CIGCDesde01012017RentExenRestCDD_C25F28",
			"CIGCDesde01012017CredIPE_C26F29",
			"CIGCHasta31122016RentAfecSDD_C27F30",
			"CIGCHasta31122016RentAfecCDD_C28F31",
			"CIGCHasta31122016RentExenSDD_C29F32",
			"CIGCHasta31122016RentExenCDD_C30F33",
			"CIGCHasta31122016CredIPE_C31F34",
			"CIGCImptoTasaAdicional_C32F35",
			"DevCapArt17N7LIR_C33F36",
			--
			"IdDocumento",
			"IdEmpresa",
			"FechaDividendo",
            "RutSocPagadora",
           	"TipoSociedad",
            "RutReceptor",
            --Datos Dividendo
			"NroCertificadoF5",
			"FactorF7",
			"NroDividendoF2",
			"MontoF6",
			"MontoActualizadoF8",
			"TasaEfectCredFUT", 
			"TasaEfectCredFUNT",
            TRIM(UPPER("RutCliente"))
            FROM :v_detalle_sin_certificado;

           
			
            
            INSERT INTO "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1949N71"(
            "IdDetalleCertificadoClienteDJ1949N71", 
            "IdCertificadoCliente", 
            "IdTipoDocumento",
            "IdDocumento",
			"Version",
			"ConCreditoIDPCDesed2017C6F9",
            "ConCreditoIDPCHasta2017C7F10",
            "ConDerechoIDPCVoluntarioC8F11",
            "SinDerechoCreditoC9F12",
            --Rentas con tributaci�n cumplida
			"MtoDivReaREXTribCumpRentasRap_C10F13",
			"MtoDivReaREXTribCumpOtrasRentas_C11F14",
			"MtoDivReaREXTribCumpExcDist_C12F15",
			"MtoDivReaREXTribCumpUtiISFUT_C13F16",
			"MtoDivReaREXTribCumpRentasISFUT_C14F17",
			--Rentas Excentas
			"MtoDivReaREXRentExIGCAfecIAart11_C15F18",
			"RentasExcentasIGCC16F19",
			"MtoDivReaREXNoConstRenta_C17F20",
			--Resto
            "CIGCDesde01012017RentAfecNoRestSDD_C18F21",
			"CIGCDesde01012017RentAfecNoRestCDD_C19F22",
			"CIGCDesde01012017RentAfecNoRestSDD_C20F23",
			"CIGCDesde01012017RentAfecNoRestCDD_C21F24",
			"CIGCDesde01012017RentAfecRestSDD_C22F25",
			"CIGCDesde01012017RentAfecRestCDD_C23F26",
			"CIGCDesde01012017RentExenRestSDD_C24F27",
			"CIGCDesde01012017RentExenRestCDD_C25F28",
			"CIGCDesde01012017CredIPE_C26F29",
			"CIGCHasta31122016RentAfecSDD_C27F30",
			"CIGCHasta31122016RentAfecCDD_C28F31",
			"CIGCHasta31122016RentExenSDD_C29F32",
			"CIGCHasta31122016RentExenCDD_C30F33",
			"CIGCHasta31122016CredIPE_C31F34",
			"CIGCImptoTasaAdicional_C32F35",
			"DevCapArt17N7LIR_C33F36",
			--
			"FechaDividendo",
            "RutSocPagadora",
            "TipoSociedad",
            "RutReceptor",
			"IdEmpresa",
			--Datos Dividendo
			"NroCertificadoF5",
			"FactorF7",
			"NroDividendoF2",
			"MontoF6",
			"MontoActualizadoF8",
			"TasaEfectCredFUT", 
			"TasaEfectCredFUNT",
            "RutCliente"
            )
            SELECT
            "IdDetalleCertificadoClienteDJ1949N71",
            "IdCertificadoCliente",
            "IdTipoDocumento",
            "IdDocumento",
            "Version",
            "ConCreditoIDPCDesed2017C6F9",
            "ConCreditoIDPCHasta2017C7F10",
           	"ConDerechoIDPCVoluntarioC8F11",
            "SinDerechoCreditoC9F12",
            	--Rentas con tributaci�n cumplida
			"MtoDivReaREXTribCumpRentasRap_C10F13",
			"MtoDivReaREXTribCumpOtrasRentas_C11F14",
			"MtoDivReaREXTribCumpExcDist_C12F15",
			"MtoDivReaREXTribCumpUtiISFUT_C13F16",
			"MtoDivReaREXTribCumpRentasISFUT_C14F17",
			--Rentas Excentas
			"MtoDivReaREXRentExIGCAfecIAart11_C15F18",
			"RentasExcentasIGCC16F19",
			"MtoDivReaREXNoConstRenta_C17F20",
						--Resto
            "CIGCDesde01012017RentAfecNoRestSDD_C18F21",
			"CIGCDesde01012017RentAfecNoRestCDD_C19F22",
			"CIGCDesde01012017RentAfecNoRestSDD_C20F23",
			"CIGCDesde01012017RentAfecNoRestCDD_C21F24",
			"CIGCDesde01012017RentAfecRestSDD_C22F25",
			"CIGCDesde01012017RentAfecRestCDD_C23F26",
			"CIGCDesde01012017RentExenRestSDD_C24F27",
			"CIGCDesde01012017RentExenRestCDD_C25F28",
			"CIGCDesde01012017CredIPE_C26F29",
			"CIGCHasta31122016RentAfecSDD_C27F30",
			"CIGCHasta31122016RentAfecCDD_C28F31",
			"CIGCHasta31122016RentExenSDD_C29F32",
			"CIGCHasta31122016RentExenCDD_C30F33",
			"CIGCHasta31122016CredIPE_C31F34",
			"CIGCImptoTasaAdicional_C32F35",
			"DevCapArt17N7LIR_C33F36",
			--
            "FechaDividendo",
            "RutSocPagadora",
            "TipoSociedad",
            "RutReceptor",
            "IdEmpresa",
            --Datos Dividendo
			"NroCertificadoF5",
			"FactorF7",
			"NroDividendoF2",
			"MontoF6",
			"MontoActualizadoF8",
			"TasaEfectCredFUT", 
			"TasaEfectCredFUNT",
            TRIM(UPPER("RutCliente"))
            FROM :v_detalle_con_certificado;

            SELECT COUNT(1) into count_certificados
            FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" c
            INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1949N71" d ON (d."IdCertificadoCliente" = c."IdCertificadoCliente")  
            WHERE c."Esquema" = 'DJ1949'
              AND c."Version" = :p_Version
              AND c."IdTipoCertificado" ='CERT71';
            
            IF (:count_certificados > 0) THEN
                SELECT
                    MAX("Folio") into v_max_folio  
                FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" c
                INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1949N71" d ON (d."IdCertificadoCliente" = c."IdCertificadoCliente")  
                WHERE c."Esquema" = 'DJ1949'
                  AND c."Version" = :p_Version
                  AND c."IdTipoCertificado" ='CERT71';
            
            
                UPDATE "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
                SET
                    "UltimoFolio" = :v_max_folio
                WHERE "Esquema" = 'DJ1949'
                  AND "Version" = :p_Version
                  AND "CodigoCertificado" = 'CERTIFICADO_71';
            END IF;
            
            
            UPDATE "BSCL.Normativa.BD::TRI.ControlProceso" 
            SET
                "Carga" = 'S', "Validacion" = 'S',"PreDeclaracion" = 'S',"Certificacion" = 'S', "EstadoCertificado" = 'GEN'
            WHERE "Esquema"= 'DJ1949'
              AND "Version" = :p_Version;
            
            UPDATE C SET C."IdEstado"='ACT'
			FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" C
			WHERE C."IdTipoCertificado"='CERT71' AND C."Version"=:p_Version
			AND EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1949N71" D WHERE D."IdCertificadoCliente"=C."IdCertificadoCliente");
            
            v_log =
                SELECT 
                    0 AS "IdCabeceraLog",
                    'INGRESO' AS "IdTipoLog",
                    'NOR' AS "IdModulo",
                    :p_IdUsuario AS "IdUsuario",
                    :p_IdEmpresa AS "IdEmpresa",
                    :v_FechaSistema AS "FechaSistemaLog",
                    :p_IdUsuario AS "IdUsuarioAtendido",
                    0 AS "IdPersona",
                    0 AS "IdCliente",
                    '' AS "EstadoCliente",
                    '' AS "NombreCliente",
                    '' AS "IdTipoDocumentoIdentidad",
                    '' AS "IdDocumentoIdentidad",
                    0 AS "IdPortafolio",
                    '' AS "EstadoPortafolio",
                    'CERTIFICADO_71' AS "IdTipoRegistro",
                    :p_Version AS "IdRegistro",
                    'ACT' AS "EstadoRegistro",
                    '' AS "IdTipoOperacion",
                    0 AS "Folio",
                    0 AS "Cantidad",
                    0 AS "PrecioTasa",
                    0 AS "Monto",
                    '' AS "IdMonedaOrigen",
                    '' AS "IdMonedaLiquidacion",
                    '' AS "TipoCambio",
                    '' AS "Nemo",
                    0 AS "IdProceso",
                    '' AS "EstadoProceso",
                    '' AS "NombreProceso"
                FROM DUMMY;
            
            v_DETALLELOG =
                SELECT
                    0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo"
                FROM dummy
                WHERE 1=2;
        
            CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log, p_LogDetalle => :v_DETALLELOG);
        
            CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1949.GenerarCertificado71','FIN','FIN');

        	COMMIT;

            SIGNAL SALIDA_CONTROLADA;
        
        END;
    END;    
END;