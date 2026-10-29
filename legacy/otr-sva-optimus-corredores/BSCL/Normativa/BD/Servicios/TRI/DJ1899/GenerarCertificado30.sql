SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1899.GenerarCertificado30";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1899.GenerarCertificado30"(
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
            DECLARE v_ultimo_folio      BIGINT;
            DECLARE v_max_folio         BIGINT;
            DECLARE v_count_certificados  BIGINT;
            DECLARE v_count_IPC         BIGINT;
            DECLARE v_IndiceMesActual   DECIMAL(26,8) := 0;
            
            CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1899.GenerarCertificado30','INICIO','INICIO');
                
            CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
            
            IF :v_resultadoRegla = 'RECH' THEN
                v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
                SIGNAL SALIDA_CONTROLADA;   
            END IF;
            
            CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
            
            SELECT COUNT(1) into v_count_folio
            FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
            WHERE "Esquema" = 'DJ1899' AND "Version" = :p_Version AND "CodigoCertificado" = 'CERTIFICADO_30';
            
            IF (:v_count_folio = 0) THEN
                INSERT INTO "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
                ("Esquema","Version","UltimoFolio","CodigoCertificado","NombreCertificado","CodigoPrograma")
                VALUES('DJ1899',:p_Version,0,'CERTIFICADO_30','Certificado 30','GenerarCertificado30');
            END IF;
            
            SELECT "UltimoFolio" INTO v_ultimo_folio
            FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
            WHERE "Esquema" = 'DJ1899' AND "Version" = :p_Version AND "CodigoCertificado" = 'CERTIFICADO_30';
            
            SELECT COUNT(1) into v_count_IPC
            FROM "BSCL.Normativa.BD.Vistas::TRI.ConsultarFactorIpc"
            WHERE "AgnoMes" = :p_Version || '-11';
            
            IF (:v_count_IPC > 0) THEN
                SELECT "ValorIpc" INTO v_IndiceMesActual
                FROM "BSCL.Normativa.BD.Vistas::TRI.ConsultarFactorIpc"
                WHERE "AgnoMes" = :p_Version || '-11';
            END IF;
            --Todo, una vez que se determine cuales seran los campos a utilizar, se deben ocupar estos, y sacar la logica para
            --RetiroEmpleador y MontoRetencion y utilizar el java /Normativa/Java/BcsNormativaServicios/src/cl/bcs/normativa/declaraciones/DJ1899/ReajustarValorIPC.java
            v_datos =
                SELECT
                    d."ID",
                    d."IdPersona",
                    d."RutEmpleador",
                    d."FechaOperacion",
                    d."Mes",
                    d."IdTipoDocumento",
                    d."AporteEmpleador", --Aporte Empleador en $
                    d."TraspasoTrabajador", --Traspaso Trabajador en $
                    (((:v_IndiceMesActual / COALESCE(i."ValorIpc",1)) * 100) - 100) AS "VariacionPorcentual",
                    (((d."RetiroEmpleador" * (((:v_IndiceMesActual / COALESCE(i."ValorIpc",1)) * 100) - 100)) /100) + d."RetiroEmpleador") AS "RetiroEmpleador",--Retiro Empleador Actualizado en $
                    (((d."MontoRetencion" * (((:v_IndiceMesActual / COALESCE(i."ValorIpc",1)) * 100) - 100)) /100) + d."MontoRetencion") AS "MontoRetencion", --Monto Retencion 15 % actualizdad en $
                    :p_IdEmpresa AS "IdEmpresa",
                    :p_Version AS "Version"
                FROM (
                    SELECT
                        dj."ID",
                        p."IdPersona",
                        dj."RutEmpleador",
                        dj."FechaOperacion",
                        MONTH (dj."FechaOperacion") AS "Mes",
                        TO_CHAR(ADD_MONTHS(dj."FechaOperacion",-1),'YYYY-MM') AS "AgnoMes",
                        d."IdTipoDocumento",
                        0 AS "AporteEmpleador", --Aporte Empleador en $
                        0 AS "TraspasoTrabajador", --Traspaso Trabajador en $
                        1000000 AS "RetiroEmpleador", --Retiro Empleador Actualizado en $
                        (1000000 * 0.15) AS "MontoRetencion" --Monto Retencion 15 % actualizdad en $
                    FROM "BSCL.Normativa.BD::TRI.DatosDJ1899" AS dj
                    INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" AS d ON (d."IdDocumento" = dj."IdDocumento")
                    INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad" AS p ON (p."DocumentoIdentidad" = dj."RutEmpleador")
                    --LEFT OUTER JOIN :p_Clientes AS c ON (c."IdCliente" = dj."IdCliente") 
                    WHERE d."Version" = :p_Version
                      --AND (:p_Todos = 'S' OR (:p_Todos = 'N' AND COALESCE(c."IdCliente",0) != 0))
                      --dj."EjecutadoComplementoMontos" = 'S'
                ) AS d
                LEFT JOIN "BSCL.Normativa.BD.Vistas::TRI.ConsultarFactorIpc" AS i ON (i."AgnoMes" = d."AgnoMes");
            
            v_datos_agrupados =
                SELECT
                    "IdPersona",
                    "Mes",
                    "IdTipoDocumento",
                    SUM("AporteEmpleador") AS "AporteEmpleador", --Aporte Empleador en $
                    SUM("TraspasoTrabajador") AS "TraspasoTrabajador", --Traspaso Trabajador en $
                    SUM("RetiroEmpleador") AS "RetiroEmpleador", --Retiro Empleador Actualizado en $
                    SUM("MontoRetencion") AS "MontoRetencion", --Monto Retencion 15 % actualizdad en $
                    "IdEmpresa",
                    "Version"
                FROM :v_datos 
                GROUP BY "IdPersona","Mes","IdTipoDocumento","IdEmpresa","Version";

                    
            v_datos_certificado = 
                SELECT DISTINCT "IdPersona","IdEmpresa","Version","IdTipoDocumento" FROM :v_datos_agrupados;
                                    
            v_datos_sin_certificado =
                SELECT
                    "BSCL.Normativa.BD.Secuencias::TRI.IdCertificadoCliente".NEXTVAL AS "IdCertificadoCliente",
                    :v_ultimo_folio + "Numero" AS "Folio",
                    "IdPersona",
                    "IdTipoDocumento",
                    "IdEmpresa",
                    "Version"
                FROM (
                        SELECT
                            ROW_NUMBER() OVER(ORDER BY d."IdPersona") AS "Numero",
                            d."IdPersona",
                            d."IdTipoDocumento",
                            d."IdEmpresa",
                            d."Version"
                        FROM :v_datos_certificado d
                        WHERE NOT EXISTS (  
                                           SELECT 1 FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" c
                                           WHERE c."IdCliente" = d."IdPersona"
                                             AND c."Esquema" = 'DJ1899'
                                             AND c."Version" = d."Version"
                                             AND c."IdEmpresa" = d."IdEmpresa"
                                             AND c."IdTipoCertificado" = 'CERT30')
                );
            
            v_detalle_sin_certificado =
                SELECT
                    "BSCL.Normativa.BD.Secuencias::TRI.IdDetalleCertificadoClienteDJ1899N30".NEXTVAL AS "IdDetalleCertificadoClienteDJ1899N30",
                    c."IdCertificadoCliente",
                    d."IdPersona",
                    d."Mes",
                    d."IdTipoDocumento",
                    d."AporteEmpleador", --Aporte Empleador en $
                    d."TraspasoTrabajador", --Traspaso Trabajador en $
                    d."RetiroEmpleador", --Retiro Empleador Actualizado en $
                    d."MontoRetencion", --Monto Retencion 15 % actualizdad en $
                    d."IdEmpresa",
                    d."Version"
                FROM :v_datos_agrupados d
                INNER JOIN :v_datos_sin_certificado c ON (c."IdPersona" = d."IdPersona"
                                                      AND c."IdEmpresa" = d."IdEmpresa"
                                                      AND c."Version" = d."Version"
                                                      AND c."IdTipoDocumento" = d."IdTipoDocumento");
            
            v_datos_con_certificado =
                SELECT
                    c."IdCertificadoCliente",
                    c."Folio",
                    d."IdPersona",
                    d."IdTipoDocumento",
                    d."IdEmpresa",
                    d."Version"
                FROM :v_datos_certificado d
                INNER JOIN "BSCL.Normativa.BD::TRI.CertificadoCliente" c ON (c."IdCliente" = d."IdPersona"
                                                                         AND c."Esquema" = 'DJ1899'
                                                                         AND c."Version" = d."Version"
                                                                         AND c."IdEmpresa" = d."IdEmpresa"
                                                                         AND c."IdTipoCertificado" = 'CERT30') 
                WHERE EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N30" de WHERE de."IdCertificadoCliente" = c."IdCertificadoCliente");
            
            v_detalle_con_certificado =
                SELECT
                    "BSCL.Normativa.BD.Secuencias::TRI.IdDetalleCertificadoClienteDJ1899N30".NEXTVAL AS "IdDetalleCertificadoClienteDJ1899N30",
                    c."IdCertificadoCliente",
                    d."IdPersona",
                    d."Mes",
                    d."IdTipoDocumento",
                    d."AporteEmpleador", --Aporte Empleador en $
                    d."TraspasoTrabajador", --Traspaso Trabajador en $
                    d."RetiroEmpleador", --Retiro Empleador Actualizado en $
                    d."MontoRetencion", --Monto Retencion 15 % actualizdad en $
                    d."IdEmpresa",
                    d."Version"
                FROM :v_datos_agrupados d
                INNER JOIN :v_datos_con_certificado c  ON (c."IdPersona" = d."IdPersona"
                                                       AND c."IdEmpresa" = d."IdEmpresa"
                                                       AND c."Version" = d."Version"
                                                       AND c."IdTipoDocumento" = d."IdTipoDocumento");

                                                                                                
            INSERT INTO "BSCL.Normativa.BD::TRI.CertificadoCliente"(
                "IdCertificadoCliente","IdTipoDocumento","IdEmpresa","Esquema","Version","Anio","Periodo","IdCliente","IdPortafolio",
                "Folio","IdTipoCertificado")
            SELECT 
                "IdCertificadoCliente","IdTipoDocumento","IdEmpresa",'DJ1899',"Version","Version","Version","IdPersona",0,"Folio",
                'CERT30'
            FROM :v_datos_sin_certificado;
            
            --Se actualizan los folios en la tabla de datos en la dj
            UPDATE d
            SET
                "NumeroCertificadoEmpleador" = c."Folio"
            FROM "BSCL.Normativa.BD::TRI.DatosDJ1899" AS d
            INNER JOIN :v_Datos AS v ON (v."ID" = d."ID")
            INNER JOIN :v_datos_sin_certificado c ON (c."IdPersona" = v."IdPersona");

            INSERT INTO "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N30"(
                "IdDetalleCertificadoClienteDJ1899N30","IdCertificadoCliente","IdPersona",
                "Mes","IdTipoDocumento",
                --
                "AporteEmpleador","TraspasoTrabajador","RetiroEmpleador","MontoRetencion",
                --
                "IdEmpresa","Version"
            )
            SELECT
                "IdDetalleCertificadoClienteDJ1899N30","IdCertificadoCliente","IdPersona",
                "Mes","IdTipoDocumento",
                --
                "AporteEmpleador","TraspasoTrabajador","RetiroEmpleador","MontoRetencion",
                --
                "IdEmpresa","Version"
            FROM :v_detalle_sin_certificado;
            
            DELETE FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N30" d
            WHERE EXISTS(SELECT 1 FROM :v_detalle_con_certificado c WHERE c."IdCertificadoCliente" = d."IdCertificadoCliente");
            
            INSERT INTO "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N30"(
                "IdDetalleCertificadoClienteDJ1899N30","IdCertificadoCliente","IdPersona",
                "Mes","IdTipoDocumento",
                --
                "AporteEmpleador","TraspasoTrabajador","RetiroEmpleador","MontoRetencion",
                --
                "IdEmpresa","Version")
            SELECT
                "IdDetalleCertificadoClienteDJ1899N30","IdCertificadoCliente","IdPersona",
                "Mes","IdTipoDocumento",
                --
                "AporteEmpleador","TraspasoTrabajador","RetiroEmpleador","MontoRetencion",
                --
                "IdEmpresa","Version"
            FROM :v_detalle_con_certificado;
            
            
            SELECT COUNT(1) into v_count_certificados
            FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" c
            INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N30" d ON (d."IdCertificadoCliente" = c."IdCertificadoCliente")  
            WHERE c."Esquema" = 'DJ1899'
              AND c."Version" = :p_Version
              AND c."IdTipoCertificado" ='CERT30';
            
            IF (:v_count_certificados > 0) THEN
                SELECT
                    MAX("Folio") into v_max_folio  
                FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" c
                INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N30" d ON (d."IdCertificadoCliente" = c."IdCertificadoCliente")  
                WHERE c."Esquema" = 'DJ1899'
                  AND c."Version" = :p_Version
                  AND c."IdTipoCertificado" ='CERT30';
            
            
                UPDATE "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
                SET
                    "UltimoFolio" = :v_max_folio
                WHERE "Esquema" = 'DJ1899'
                  AND "Version" = :p_Version
                  AND "CodigoCertificado" = 'CERTIFICADO_30';
            END IF;
            
            UPDATE "BSCL.Normativa.BD::TRI.ControlProceso" 
            SET
                "Carga" = 'S', "Validacion" = 'S',"PreDeclaracion" = 'S',"Certificacion" = 'S', "EstadoCertificado" = 'GEN'
            WHERE "Esquema"= 'DJ1899'
              AND "Version" = :p_Version;
            
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
                    'CERTIFICADO_30' AS "IdTipoRegistro",
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
        
            CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1899.GenerarCertificado30','FIN','FIN');
        
            SIGNAL SALIDA_CONTROLADA;
        
        END;
    END;    
END;