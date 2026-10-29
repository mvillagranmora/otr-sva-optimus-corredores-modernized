SET SCHEMA BSCL;  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1899.GenerarCertificado36";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1899.GenerarCertificado36"(
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

            CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1899.GenerarCertificado36','INICIO','INICIO');

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
       	    	 				FROM "BSCL.Normativa.BD::TRI.DatosDJ1899" AS dj
				                INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" AS d ON (d."IdDocumento" = dj."IdDocumento")
						        WHERE d."Version" = :p_Version  
						 UNION ALL
						 
						  SELECT trim(UPPER(dj."RutCliente")) AS "RutCliente"  
       	    	 				FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N36" AS dj
				                --INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" AS d ON (d."IdDocumento" = dj."IdDocumento")
						        WHERE dj."Version" = :p_Version  
							        
				) DAT  
				LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS cl ON (trim(DAT."RutCliente") = trim(cl."DocumentoIdentidad"))
				GROUP BY DAT."RutCliente" ;	    
            END IF; 

        CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
 
        SELECT COUNT(1) into v_count_folio
            FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
            WHERE "Esquema" = 'DJ1899' AND "Version" = :p_Version AND "CodigoCertificado" = 'CERTIFICADO_36';

        IF (:v_count_folio = 0) THEN
            INSERT INTO "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
            ("Esquema","Version","UltimoFolio","CodigoCertificado","NombreCertificado","CodigoPrograma")
            VALUES('DJ1899',:p_Version,0,'CERTIFICADO_36','Certificado 36','GenerarCertificado36');
        END IF;

        SELECT "UltimoFolio" into v_ultimo_folio
            FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
            WHERE "Esquema" = 'DJ1899' AND "Version" = :p_Version AND "CodigoCertificado" = 'CERTIFICADO_36';

        v_datos =
                SELECT
                dj."ID",
                COALESCE(c."IdCliente",0) AS "IdCliente",
                dj."RutEmpleador",
                dj."FechaOperacion",
                MONTH (dj."FechaOperacion") AS "Mes",
                d."IdTipoDocumento",
                --	MSO 
                -- coalesce(dj."DepositoEmpleadorMonedaDJ",dj."DepositoEmpleador") AS "MontoDepositoConvenido",
                dj."DepositoEmpleador" AS "MontoDepositoConvenido",
                --	MSO
                :p_IdEmpresa AS "IdEmpresa",
                :p_Version AS "Version",
                TRIM(UPPER(dj."RutCliente")) AS "RutCliente",
                '0' AS "CodigoPortafolio"
                FROM "BSCL.Normativa.BD::TRI.DatosDJ1899" AS dj
                    INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" AS d ON (d."IdDocumento" = dj."IdDocumento")
                    LEFT OUTER JOIN :v_cliente AS c ON (c."RutCliente" = TRIM(UPPER(dj."RutCliente")))
                WHERE d."Version" = :p_Version
                    --AND (:p_Todos = 'S' OR (:p_Todos = 'N' AND COALESCE(c."IdCliente",0) != 0))
                    --dj."EjecutadoComplementoMontos" = 'S'
                    --	MSO
                    -- AND ((dj."DepositoEmpleadorMonedaDJ" + dj."DepositoEmpleador") > 0);
                    AND dj."DepositoEmpleador" > 0;
                    
                --	MSO
                v_datos_agrupados =
                            SELECT
                            max("IdCliente") as "IdCliente",
                            "RutEmpleador",--??duda
                            "Mes",
                            "IdTipoDocumento",
                            SUM("MontoDepositoConvenido") AS "MontoDepositoConvenido",
                            "IdEmpresa",
                            "Version",
                            "RutCliente",
                            "CodigoPortafolio"
                            FROM :v_datos
                            GROUP BY "RutEmpleador","Mes","IdTipoDocumento","IdEmpresa","Version","RutCliente","CodigoPortafolio";

                v_datos_certificado =
                SELECT DISTINCT "IdEmpresa","Version","IdTipoDocumento","RutCliente","CodigoPortafolio" FROM :v_datos_agrupados;

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
                                            WHERE c."RutCliente" = d."RutCliente" -- IdCliente x RutCliente
                                                    AND c."CodigoPortafolio" = d."CodigoPortafolio" --
                                                    AND c."Esquema" = 'DJ1899'
                                                    AND c."Version" = d."Version"
                                                    AND c."IdEmpresa" = d."IdEmpresa"
                                                    AND c."IdTipoCertificado" = 'CERT36')
                        );

                v_detalle_sin_certificado =
                            SELECT
                                "BSCL.Normativa.BD.Secuencias::TRI.IdDetalleCertificadoClienteDJ1899N36".NEXTVAL AS "IdDetalleCertificadoClienteDJ1899N36",
                                c."IdCertificadoCliente",
                                d."IdCliente",
                                d."RutEmpleador",
                                d."Mes",
                                d."IdTipoDocumento",
                                d."MontoDepositoConvenido",
                                d."IdEmpresa",
                                d."Version",
                                d."RutCliente",
                                d."CodigoPortafolio"
                            FROM :v_datos_agrupados d
                                INNER JOIN :v_datos_sin_certificado c ON (c."RutCliente" = d."RutCliente" --
                                AND c."CodigoPortafolio" = d."CodigoPortafolio" --
                                AND c."IdEmpresa" = d."IdEmpresa"
                                AND c."Version" = d."Version"
                                AND c."IdTipoDocumento" = d."IdTipoDocumento");

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
                                    INNER JOIN "BSCL.Normativa.BD::TRI.CertificadoCliente" c ON (c."RutCliente" = d."RutCliente" --
                                    AND c."CodigoPortafolio" = d."CodigoPortafolio" --
                                    AND c."Esquema" = 'DJ1899'
                                    AND c."Version" = d."Version"
                                    AND c."IdEmpresa" = d."IdEmpresa"
                                    AND c."IdTipoCertificado" = 'CERT36');
                                --WHERE EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N36" de WHERE de."IdCertificadoCliente" = c."IdCertificadoCliente");

                v_detalle_con_certificado =
                                SELECT
                                    "BSCL.Normativa.BD.Secuencias::TRI.IdDetalleCertificadoClienteDJ1899N36".NEXTVAL AS "IdDetalleCertificadoClienteDJ1899N36",
                                    c."IdCertificadoCliente",
                                    d."IdCliente",
                                    d."RutEmpleador",
                                    d."Mes",
                                    d."IdTipoDocumento",
                                    d."MontoDepositoConvenido",
                                    d."IdEmpresa",
                                    d."Version",
                                    d."RutCliente",
                                    d."CodigoPortafolio"
                                FROM :v_datos_agrupados d
                                INNER JOIN :v_datos_con_certificado c  ON (c."RutCliente" = d."RutCliente"
                                    AND c."CodigoPortafolio" = d."CodigoPortafolio"
                                    AND c."IdEmpresa" = d."IdEmpresa"
                                    AND c."Version" = d."Version"
                                    AND c."IdTipoDocumento" = d."IdTipoDocumento");

                    --Se actualizan los folios en la tabla de datos en la dj
                        UPDATE Datos
                            SET "NumeroCertificadoEmpleador" = Cert."Folio"
                            FROM "BSCL.Normativa.BD::TRI.DatosDJ1899" Datos
                            INNER JOIN "BSCL.Normativa.BD::TRI.CertificadoCliente" Cert
                                ON Cert."Esquema" = 'DJ1899'
                                --si es por carga externa usara el RutCliente
                                AND Cert."IdCliente" = Datos."IdCliente" OR (TRIM(UPPER(Datos."RutCliente")) = Cert."RutCliente")
                                AND Cert."IdTipoCertificado" = 'CERT36';

                        DELETE FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N36" d
                        WHERE d."Version"= :p_Version
			                    AND d."RutCliente" IN ( SELECT "RutCliente" FROM :v_cliente);

                        INSERT INTO "BSCL.Normativa.BD::TRI.CertificadoCliente"(
                            "IdCertificadoCliente", "IdTipoDocumento", "IdEmpresa",
                            "Esquema", "Version", "Anio", "Periodo",
                            "IdCliente", "IdPortafolio", "Folio",
                            "IdTipoCertificado", "RutCliente", "CodigoPortafolio")
                        SELECT
                            "IdCertificadoCliente","IdTipoDocumento","IdEmpresa",
                            'DJ1899',"Version","Version","Version",
                            0 AS "IdCliente", 0 AS "IdPortafolio","Folio",
                            'CERT36',TRIM(UPPER("RutCliente")),"CodigoPortafolio"
                        FROM :v_datos_sin_certificado;

                        INSERT INTO "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N36"(
                            "IdDetalleCertificadoClienteDJ1899N36","IdCertificadoCliente","IdCliente",
                            "RutEmpleador","Mes","IdTipoDocumento","MontoDepositoConvenido","IdEmpresa","Version","RutCliente"
                            )
                        SELECT
                            "IdDetalleCertificadoClienteDJ1899N36","IdCertificadoCliente","IdCliente",
                            "RutEmpleador","Mes","IdTipoDocumento","MontoDepositoConvenido","IdEmpresa","Version","RutCliente"
                            FROM :v_detalle_sin_certificado;
 

                        INSERT INTO "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N36"(
                            "IdDetalleCertificadoClienteDJ1899N36","IdCertificadoCliente","IdCliente",
                            "RutEmpleador","Mes","IdTipoDocumento","MontoDepositoConvenido","IdEmpresa","Version","RutCliente")
                        SELECT
                            "IdDetalleCertificadoClienteDJ1899N36","IdCertificadoCliente","IdCliente",
                            "RutEmpleador","Mes","IdTipoDocumento","MontoDepositoConvenido","IdEmpresa","Version","RutCliente"
                        FROM :v_detalle_con_certificado;

                        SELECT COUNT(1) into count_certificados
                        FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" c
                            INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N36" d ON (d."IdCertificadoCliente" = c."IdCertificadoCliente")
                        WHERE c."Esquema" = 'DJ1899'
                            AND c."Version" = :p_Version
                            AND c."IdTipoCertificado" ='CERT36';

                        IF (:count_certificados > 0) THEN
                            SELECT
                                MAX("Folio") into v_max_folio
                            FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" c
                                INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N36" d ON (d."IdCertificadoCliente" = c."IdCertificadoCliente")
                            WHERE c."Esquema" = 'DJ1899'
                                AND c."Version" = :p_Version
                                AND c."IdTipoCertificado" ='CERT36';

                            UPDATE "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
                                SET
                                    "UltimoFolio" = :v_max_folio
                                WHERE "Esquema" = 'DJ1899'
                                AND "Version" = :p_Version
                                AND "CodigoCertificado" = 'CERTIFICADO_36';
                        END IF;

                        UPDATE "BSCL.Normativa.BD::TRI.ControlProceso"
                            SET
                            "Carga" = 'S', "Validacion" = 'S',"PreDeclaracion" = 'S',"Certificacion" = 'S', "EstadoCertificado" = 'GEN'
                            WHERE "Esquema"= 'DJ1899'
                            AND "Version" = :p_Version;

                    UPDATE C SET C."IdEstado"='ACT'
                    FROM "BSCL.Normativa.BD::TRI.CertificadoCliente" C
                    WHERE C."IdTipoCertificado"='CERT36' AND C."Version"=:p_Version
                    AND EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1899N36" D WHERE D."IdCertificadoCliente"=C."IdCertificadoCliente");

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
                            'CERTIFICADO_36' AS "IdTipoRegistro",
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
                    CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1899.GenerarCertificado36','FIN','FIN');

                SIGNAL SALIDA_CONTROLADA;

        END;
    END;
 END;