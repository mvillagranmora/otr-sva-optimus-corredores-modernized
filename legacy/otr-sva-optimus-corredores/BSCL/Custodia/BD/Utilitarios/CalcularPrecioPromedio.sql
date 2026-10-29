SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.BD.Utilitarios::CalcularPrecioPromedio";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Utilitarios::CalcularPrecioPromedio"(
-- 
--  Proposito: Calcular el precio promedio de los registros enviados
--             minimo debe haber un registro con valores null y la fecha para obtener la paridad
--             insert into "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Entrada_Entrada" (    
--             "IdCliente","IdPortafolio","IdInstrumento","FechaParidad")
--             SELECT 1,NULL,NULL,NULL,"2017-01-05" FROM DUMMY;
--             CALL "BSCL.Custodia.BD.Utilitarios::CalcularPrecioPromedio"();
--             
--  Autor: Oscar Carreno Tolmo - OCT
--  Fecha Creacion: 04/01/2017
--------------------------------MODIFICACIONES---------------------------------------------------------------
--  Proposito: Redondear o Truncar los montos a precio promedio
--  Autor: Oscar Carreno Tolmo - OCT
--  Fecha Creacion: 29/03/2017
-------------------------------------------------------------------------------------------------------------
--  Proposito: Se incluye tabla de configuraciones, en la cual se indican los registros exlcuidos paara ser
--             considerados en el calculo de precio promedio
--  Autor: Oscar Carreno Tolmo - OCT
--  Fecha Creacion: 12/04/2017
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        IF (IS_EMPTY("BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Entrada_Parametros")) THEN
            INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
                "IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
            SELECT
                "IdEmpresa",CURRENT_UTCTIMESTAMP,"IdUsuario",NULL,"ServicioAgrupador",::CURRENT_OBJECT_NAME,
                'Error',::SQL_ERROR_CODE || ': ' || ::SQL_ERROR_MESSAGE
            FROM "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Entrada_Parametros";
        
            CALL "BSCL.Plataforma.BD.Servicios::DepuracionMM"();
        ELSE
            UPDATE "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Entrada_Parametros"
            SET "ProcesoConProblemas" = 'S',"FechaFinProceso" = CURRENT_UTCTIMESTAMP;

            INSERT INTO "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Salida_Log"(
                "FechaProcesoMovimientoCustodia",
                --CAMPOS DE ERRORES
                "OrigenError","TipoProblema","CodigoMensaje","CodigoDato1","ValorDato1")
            SELECT
                CURRENT_UTCTIMESTAMP,
                --CAMPOS DE ERRORES
                ::CURRENT_OBJECT_NAME,'E','ERROR_INESPERADO',::SQL_ERROR_CODE,::SQL_ERROR_MESSAGE
            FROM DUMMY;
            
            INSERT INTO "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Salida_ErroresDetalle"(
                "IdGenerarMovimientoCustodia","IdGrupoOperacion","Codigo","Descripcion")
            SELECT
                "IdGenerarMovimientoCustodia","IdGrupoOperacion","CodigoMensaje","Descripcion"
            FROM "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Salida_Log"
            WHERE "TipoProblema" = 'E'
            ORDER BY "IdGenerarMovimientoCustodia";
        
            CALL "BSCL.Custodia.BD.Utilitarios::GrabarParametrosMovimientoCustodia"();
        END IF;
        RESIGNAL;
    END;

    BEGIN
        --VARIABLES DE ENTRADA
        DECLARE v_TicketAtencion,v_IdEmpresa,v_IdUsuario,v_IdServicioAgrupador BIGINT :=0;
        DECLARE v_IdProceso BIGINT :=1;
        DECLARE v_EsDemo,v_GrabaLog NVARCHAR(1) :='N';
        DECLARE v_ServicioAgrupador NVARCHAR(255) := '';
        DECLARE v_IdTrabajoParidad BIGINT :=0;
        DECLARE v_FechaParidad   DATE := '0001-01-01';
        DECLARE v_IdMonedaSistema,v_IdMonedaAdicional NVARCHAR(3) := '';
        DECLARE v_EsGenerarMovimientoCustodia NVARCHAR(1) :='S';
        
        DECLARE v_RegistrosUnicos TABLE (   
            "IdCliente" BIGINT,"IdPortafolio" BIGINT,"IdInstrumento" BIGINT);
        
        DELETE FROM "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Proceso_Registros";
        DELETE FROM "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Proceso_RegistrosValidos";
        DELETE FROM "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Salida_ConfiguracionPrecioPromedio";
        DELETE FROM "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Salida_Salida";
            
        IF (IS_EMPTY("BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Entrada_Parametros")) THEN
            SELECT
            NULL,"EsDemo","IdEmpresa","IdUsuario","IdServicioAgrupador","ServicioAgrupador","GrabaLog","FechaParidad","IdMonedaSistema","IdMonedaAdicional",'N'
            INTO v_TicketAtencion,v_EsDemo,v_IdEmpresa,v_IdUsuario,v_IdServicioAgrupador,v_ServicioAgrupador,v_GrabaLog,v_FechaParidad,v_IdMonedaSistema,
            v_IdMonedaAdicional,v_EsGenerarMovimientoCustodia
            FROM "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Entrada_Parametros";
        ELSE    
            SELECT COALESCE(MAX("IdProceso"),0) + 1 INTO v_IdProceso FROM "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Salida_Depuracion" ;
            
            SELECT "IdProcesoMovimientoCustodia","EsDemo","IdEmpresa","IdUsuario","IdServicioAgrupador","ServicioAgrupador",'N',"FechaSistema","IdMonedaSistema",
            "IdMonedaAdicional",'S'
            INTO v_TicketAtencion,v_EsDemo,v_IdEmpresa,v_IdUsuario,v_IdServicioAgrupador,v_ServicioAgrupador,v_GrabaLog,v_FechaParidad,v_IdMonedaSistema,
            v_IdMonedaAdicional,v_EsGenerarMovimientoCustodia
            FROM "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Entrada_Parametros";
        END IF;
        
        IF (:v_EsGenerarMovimientoCustodia = 'S') THEN 
            INSERT INTO "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Salida_Depuracion" ("IdProceso","NombreProceso","FechaInicioProceso")
            SELECT :v_IdProceso,::CURRENT_OBJECT_NAME,CURRENT_UTCTIMESTAMP FROM DUMMY;
        END IF;
        
        INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
            "IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
        SELECT
            :v_IdEmpresa,CURRENT_UTCTIMESTAMP,:v_IdUsuario,:v_IdServicioAgrupador,:v_ServicioAgrupador,::CURRENT_OBJECT_NAME,
            'Inicio','Inicio Utilitario'
        FROM DUMMY;
        
        SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
        
        INSERT INTO "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Salida_ConfiguracionPrecioPromedio"(
            "IdConfiguracionPrecioPromedio","IdTipoProducto","IdTipoOperacion","IdClasificacionTipoInstrumento","IdTipoInstrumento",
            "IdInstrumento","IdOrigenInstrumento")
        SELECT
            "IdConfiguracionPrecioPromedio",
            CASE WHEN "IdTipoProducto" = '' THEN NULL ELSE "IdTipoProducto" END,
            CASE WHEN "IdTipoOperacion" = '' THEN NULL ELSE "IdTipoOperacion" END,
            CASE WHEN "IdClasificacionTipoInstrumento" = '' THEN NULL ELSE "IdClasificacionTipoInstrumento" END,
            CASE WHEN "IdTipoInstrumento" = '' THEN NULL ELSE "IdTipoInstrumento" END,
            CASE WHEN "IdInstrumento" = 0 THEN NULL ELSE "IdInstrumento" END,
            CASE WHEN "IdOrigenInstrumento" = '' THEN NULL ELSE "IdOrigenInstrumento" END
        FROM "BSCL.Custodia.BD::CUS.ConfiguracionPrecioPromedio"
        WHERE "IdEstado" = 'ACT';
        
        v_RegistrosUnicos =
            SELECT
                "IdCliente","IdPortafolio","IdInstrumento"
            FROM "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Entrada_Entrada"
            GROUP BY "IdCliente","IdPortafolio","IdInstrumento";
        
        INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
            "IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
        SELECT
            :v_IdEmpresa,CURRENT_UTCTIMESTAMP,:v_IdUsuario,:v_IdServicioAgrupador,:v_ServicioAgrupador,::CURRENT_OBJECT_NAME,
            'Fin','Fin registros unicos'
        FROM DUMMY;
        
        INSERT INTO "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Proceso_Registros"(
            "TablaAfectada","IdCustodia","FolioCustodia","FechaParidad","IdMonedaSistema","IdCliente","IdPortafolio","IdTipoProducto",
            "IdTipoOperacion","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento",
            "IdOrigenInstrumento","INGPrecioCompra","INGCantidad","IdMonedaAdicional")
        SELECT
            "TablaAfectada","IdCustodia","FolioCustodia","FechaParidad","IdMonedaSistema","IdCliente","IdPortafolio","IdTipoProducto",
            "IdTipoOperacion","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento",
            "IdOrigenInstrumento","INGPrecioCompra","INGCantidad","IdMonedaAdicional"
        FROM
        (
        SELECT
            'CAR' AS "TablaAfectada",c."IdCartera" AS "IdCustodia",c."FolioCartera" AS "FolioCustodia",
            COALESCE(:v_IdMonedaSistema,c."IdMonedaSistema") AS "IdMonedaSistema",c."IdCliente",c."IdPortafolio",c."IdTipoProducto",
            c."IdTipoOperacion",c."IdClasificacionTipoInstrumento",c."IdTipoInstrumento",c."IdInstrumento",c."Nemotecnico",c."IdMonedaInstrumento",
            '' AS "IdOrigenInstrumento",c."INGPrecioCompra",c."INGCantidad",:v_FechaParidad AS "FechaParidad",
            COALESCE(:v_IdMonedaAdicional,c."IdMonedaAdicional") AS "IdMonedaAdicional"
        FROM :v_RegistrosUnicos AS rc
        INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS c ON ((rc."IdCliente" IS NULL OR c."IdCliente" = rc."IdCliente")
                                                        AND (rc."IdPortafolio" IS NULL OR c."IdPortafolio" = rc."IdPortafolio")
                                                        AND (rc."IdInstrumento" IS NULL OR c."IdInstrumento" = rc."IdInstrumento"))
        WHERE c."IdEstado" = 'VIG'
        UNION ALL
        SELECT
            'SUB' AS "TablaAfectada",s."IdSubyacente" AS "IdCustodia",s."FolioSubyacente" AS "FolioCustodia",
            COALESCE(:v_IdMonedaSistema,c."IdMonedaSistema") AS "IdMonedaSistema",c."IdCliente",c."IdPortafolio",c."IdTipoProducto",c."IdTipoOperacion",
            s."IdClasificacionTipoInstrumento",s."IdTipoInstrumento",s."IdInstrumento",s."Nemotecnico",s."IdMonedaInstrumento",'' AS "IdOrigenInstrumento",
            s."PrecioCompraOperacionRelacionada" AS "INGPrecioCompra",s."INGCantidad",
            :v_FechaParidad AS "FechaParidad",COALESCE(:v_IdMonedaAdicional,c."IdMonedaAdicional") AS "IdMonedaAdicional"
        FROM :v_RegistrosUnicos AS rs
        INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS c ON  ((rs."IdCliente" IS NULL OR c."IdCliente" = rs."IdCliente")
                                                         AND (rs."IdPortafolio" IS NULL OR c."IdPortafolio" = rs."IdPortafolio"))
        INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" AS s ON (s."FolioCarteraRelacionada" = c."FolioCartera"
                                                           AND (rs."IdInstrumento" IS NULL OR s."IdInstrumento" = rs."IdInstrumento")   
                                                           AND s."IdEstado" = 'VIG')
        WHERE c."IdEstado" = 'VIG'
        UNION ALL
        SELECT
            'GAR' AS "TablaAfectada",g."IdGarantia" AS "IdCustodia",g."FolioGarantia" AS "FolioCustodia",
            COALESCE(:v_IdMonedaSistema,g."IdMonedaSistema") AS "IdMonedaSistema",g."IdCliente",g."IdPortafolio",
            c."IdTipoProducto",c."IdTipoOperacion",g."IdClasificacionTipoInstrumento",g."IdTipoInstrumento",g."IdInstrumento",g."Nemotecnico",
            g."IdMonedaInstrumento",g."IdOrigenGarantia" AS "IdOrigenInstrumento",g."PrecioCompraOperacionOrigen" AS "INGPrecioCompra",g."INGCantidad",
            :v_FechaParidad AS "FechaParidad",COALESCE(:v_IdMonedaAdicional,g."IdMonedaAdicional") AS "IdMonedaAdicional"
        FROM :v_RegistrosUnicos AS rg
        INNER JOIN "BSCL.Custodia.BD::GTA.Garantia" AS g ON ((rg."IdCliente" IS NULL OR g."IdCliente" = rg."IdCliente")
                                                         AND (rg."IdPortafolio" IS NULL OR g."IdPortafolio" = rg."IdPortafolio")
                                                         AND (rg."IdInstrumento" IS NULL OR g."IdInstrumento" = rg."IdInstrumento"))
        INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS c ON (c."IdCartera" = g."IdCarteraOrigen")
        WHERE g."IdEstado" = 'VIG');
        
        --TODO-- tengo duda sobre si en la tabla suyacente hay un if que viene de un pacto, y no estan permitidos los pactos, no debe ser considerado
        --o es sobre la cartera origen y no la operacion origen.. lo mismo con la tabla garantias.
        
        INSERT INTO "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Proceso_RegistrosValidos"(
            "IdInterno","TablaAfectada","IdCustodia","FolioCustodia","FechaParidad","IdMonedaSistema","IdCliente","IdPortafolio","IdTipoProducto","IdTipoOperacion",
            "IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","IdOrigenInstrumento","INGPrecioCompra",
            "INGCantidad","SumINGPrecioCompra","CantidadRegistros","IdMonedaAdicional","PrecioPromedioCompra","MontoPrecioPromedioCompra")
        SELECT
            ROW_NUMBER() over (ORDER BY "IdCustodia"),
            "TablaAfectada","IdCustodia","FolioCustodia","FechaParidad","IdMonedaSistema","IdCliente","IdPortafolio","IdTipoProducto","IdTipoOperacion",
            "IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","IdOrigenInstrumento","INGPrecioCompra",
            "INGCantidad","SumINGPrecioCompra","CantidadRegistros","IdMonedaAdicional",("SumINGPrecioCompra" / "CantidadRegistros"),
            ("INGCantidad" * ("SumINGPrecioCompra" / "CantidadRegistros"))
        FROM(
            SELECT
                r."TablaAfectada",r."IdCustodia",r."FolioCustodia",r."FechaParidad",r."IdMonedaSistema",r."IdCliente",r."IdPortafolio",r."IdTipoProducto",
                r."IdTipoOperacion",r."IdClasificacionTipoInstrumento",r."IdTipoInstrumento",r."IdInstrumento",r."Nemotecnico",r."IdMonedaInstrumento",
                r."IdOrigenInstrumento",r."INGPrecioCompra",r."INGCantidad",r."IdMonedaAdicional",
                SUM(r."INGPrecioCompra") over (partition by r."IdCliente",r."IdPortafolio",r."IdInstrumento") AS "SumINGPrecioCompra",
                COUNT(r."IdCustodia") over (partition by r."IdCliente",r."IdPortafolio",r."IdInstrumento") AS "CantidadRegistros"
            FROM "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Proceso_Registros" r
            INNER JOIN (
                        SELECT
                            rc."TablaAfectada",rc."IdCustodia",MAX("DatoRestringido") AS "DatoRestringido"
                        FROM
                        (
                            SELECT
                                "TablaAfectada","IdCustodia","IdConfiguracionPrecioPromedio",
                                CASE WHEN "IdTipoProductoRestringido" = 'S' THEN 'S'
                                     WHEN "IdTipoOperacionRestringido" = 'S' THEN 'S'
                                     WHEN "IdClasificacionTipoInstrumentoRestringido" = 'S' THEN 'S'
                                     WHEN "IdTipoInstrumentoRestringido" = 'S' THEN 'S'
                                     WHEN "IdInstrumentoRestringido" = 'S' THEN 'S'
                                     WHEN "IdOrigenInstrumentoRestringido" = 'S' THEN 'S'
                                     ELSE 'N'
                                END AS "DatoRestringido"
                            FROM
                            (
                                SELECT
                                    r."TablaAfectada",r."IdCustodia",c."IdConfiguracionPrecioPromedio",
                                    CASE WHEN c."IdTipoProducto" IS NULL THEN 'N'
                                         WHEN c."IdTipoProducto" =  r."IdTipoProducto" THEN 'S'
                                         ELSE 'N'
                                    END AS "IdTipoProductoRestringido",
                                    CASE WHEN c."IdTipoOperacion" IS NULL THEN 'N'
                                         WHEN c."IdTipoOperacion" =  r."IdTipoOperacion" THEN 'S'
                                         ELSE 'N'
                                    END AS "IdTipoOperacionRestringido",
                                    CASE WHEN c."IdClasificacionTipoInstrumento" IS NULL THEN 'N'
                                         WHEN c."IdClasificacionTipoInstrumento" =  r."IdClasificacionTipoInstrumento" THEN 'S'
                                         ELSE 'N'
                                    END AS "IdClasificacionTipoInstrumentoRestringido",
                                    CASE WHEN c."IdTipoInstrumento" IS NULL THEN 'N'
                                         WHEN c."IdTipoInstrumento" =  r."IdTipoInstrumento" THEN 'S'
                                         ELSE 'N'
                                    END AS "IdTipoInstrumentoRestringido",
                                    CASE WHEN c."IdInstrumento" IS NULL THEN 'N'
                                         WHEN c."IdInstrumento" =  r."IdInstrumento" THEN 'S'
                                         ELSE 'N'
                                    END AS "IdInstrumentoRestringido",
                                    CASE WHEN c."IdOrigenInstrumento" IS NULL THEN 'N'
                                         WHEN c."IdOrigenInstrumento" =  r."IdOrigenInstrumento" THEN 'S'
                                         ELSE 'N'
                                    END AS "IdOrigenInstrumentoRestringido"
                                FROM "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Proceso_Registros" r,
                                     "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Salida_ConfiguracionPrecioPromedio" c)
                            ) AS rc
                        GROUP BY rc."TablaAfectada",rc."IdCustodia") AS rv
            ON (rv."TablaAfectada" = r."TablaAfectada"
            AND rv."IdCustodia" = r."IdCustodia"
            AND rv."DatoRestringido" = 'N')
        );
        
        INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
            "IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
        SELECT
            :v_IdEmpresa,CURRENT_UTCTIMESTAMP,:v_IdUsuario,:v_IdServicioAgrupador,:v_ServicioAgrupador,::CURRENT_OBJECT_NAME,
            'Fin','Fin obtención de custodia para registros enviados'
        FROM DUMMY;
        
        INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"(
            "IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
        SELECT
            :v_IdTrabajoParidad,"IdInterno","FechaParidad","IdMonedaSistema","IdMonedaInstrumento",0,"MontoPrecioPromedioCompra",'CARTERA'
        FROM "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Proceso_RegistrosValidos";
        
        --a moneda adicional
        INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"(
            "IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
        SELECT
            :v_IdTrabajoParidad,"IdInterno" * -1,"FechaParidad","IdMonedaSistema","IdMonedaAdicional",0,"MontoPrecioPromedioCompra",'CARTERA'
        FROM "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Proceso_RegistrosValidos"
        WHERE "IdMonedaAdicional" != '';
        
        IF (NOT IS_EMPTY("BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada")) THEN
            CALL "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(p_IdEmpresa => :v_IdEmpresa,
                                                                                p_IdTrabajo => :v_IdTrabajoParidad);
        END IF;
        
        INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
            "IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
        SELECT
            :v_IdEmpresa,CURRENT_UTCTIMESTAMP,:v_IdUsuario,:v_IdServicioAgrupador,:v_ServicioAgrupador,::CURRENT_OBJECT_NAME,
            'Fin','Fin llamada servicio BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio'
        FROM DUMMY;
                
        INSERT INTO "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Salida_Salida" (
            "FechaParidad","IdMonedaSistema","TablaAfectada","IdCustodia","FolioCustodia","IdCliente","IdPortafolio","IdTipoOperacion","IdTipoInstrumento",
            "IdInstrumento","Nemotecnico","IdMonedaInstrumento","INGPrecioCompra","INGCantidad","SumINGPrecioCompra","CantidadRegistros","PrecioPromedioCompra",
            "MontoPrecioPromedioCompra","MontoPrecioPromedioMonedaInstrumento","MontoPrecioPromedioMonedaAdicional")
        SELECT DISTINCT
            c."FechaParidad",c."IdMonedaSistema",c."TablaAfectada",c."IdCustodia",c."FolioCustodia",c."IdCliente",c."IdPortafolio",
            c."IdTipoOperacion",c."IdTipoInstrumento",c."IdInstrumento",c."Nemotecnico",c."IdMonedaInstrumento",c."INGPrecioCompra",
            c."INGCantidad",c."SumINGPrecioCompra",c."CantidadRegistros",c."PrecioPromedioCompra",
            COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero" (c."MontoPrecioPromedioCompra",fs."MontoDecimales",fs."MontoTrucaRedondea"),c."MontoPrecioPromedioCompra"),
            COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero" (COALESCE(p."MontoDestino",0),fi."MontoDecimales",fi."MontoTrucaRedondea"),COALESCE(p."MontoDestino",0)),
            COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero" (COALESCE(a."MontoDestino",0),fa."MontoDecimales",fa."MontoTrucaRedondea"),COALESCE(a."MontoDestino",0))
        FROM "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Proceso_RegistrosValidos" AS c
        INNER JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" p ON (p."IdInterno" = c."IdInterno"
                                                                                        AND p."IdTrabajo" = :v_IdTrabajoParidad)
        LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" a ON (a."IdInterno" = c."IdInterno" * -1
                                                                                        AND a."IdTrabajo" = :v_IdTrabajoParidad)
        LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" fi ON (fi."IdMoneda" = c."IdMonedaInstrumento"
                                                             AND fi."IdTipoInstrumento" = c."IdTipoInstrumento"
                                                             AND fi."IdUsoMonedaFormato" = 'CUS')
        LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" fs ON (fs."IdMoneda" = c."IdMonedaSistema"
                                                             AND fs."IdTipoInstrumento" = c."IdTipoInstrumento"
                                                             AND fs."IdUsoMonedaFormato" = 'CUS')
        LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" fa ON (fa."IdMoneda" = c."IdMonedaAdicional"
                                                             AND fa."IdTipoInstrumento" = c."IdTipoInstrumento"
                                                             AND fa."IdUsoMonedaFormato" = 'CUS');
        
        IF (:v_EsGenerarMovimientoCustodia = 'S') THEN 
            INSERT INTO "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Entrada_Log"(
                "FechaProcesoMovimientoCustodia",
                --CAMPOS DE ERRORES
                "OrigenError","TipoProblema","CodigoMensaje")
            SELECT DISTINCT
                CURRENT_UTCTIMESTAMP,
                --CAMPOS DE ERRORES
                ::CURRENT_OBJECT_NAME,'W',"CodigoMensaje"
            FROM "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida"
            WHERE "IdTrabajo" = :v_IdTrabajoParidad
              AND "Resultado" = 'RECHAZO';
        END IF;
        
        IF (:v_EsDemo = 'S') THEN
            SELECT 'Salida' AS "v_RegistrosUnicos",* FROM :v_RegistrosUnicos;
            SELECT 'Salida' AS "CalcularPrecioPromedio_Salida_ConfiguracionPrecioPromedio",* FROM "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Salida_ConfiguracionPrecioPromedio";
            SELECT 'Salida' AS "CalcularPrecioPromedio_Proceso_Registros",* FROM "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Proceso_Registros";
            SELECT 'Salida' AS "CalcularPrecioPromedio_Proceso_RegistrosValidos",* FROM "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Proceso_RegistrosValidos";
            SELECT 'Salida' AS "CalcularParidadYTipoCambio_Entrada_Entrada",* FROM "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada";
            SELECT 'Salida' AS "CalcularParidadYTipoCambio_Salida_Salida",* FROM "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida";
            SELECT 'Salida' AS "CalcularPrecioPromedio_Salida_Salida",* FROM "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Salida_Salida";
        END IF;
        
        INSERT INTO "BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion" (
            "IdEmpresa","Fecha","IdUsuario","IdServicioAgrupador","ServicioAgrupador","Servicio","Bandera","Detalle") 
        SELECT
            :v_IdEmpresa,CURRENT_UTCTIMESTAMP,:v_IdUsuario,:v_IdServicioAgrupador,:v_ServicioAgrupador,::CURRENT_OBJECT_NAME,
            'Fin','Fin utilitario'
        FROM DUMMY;
        
        IF (:v_GrabaLog = 'S') THEN
            CALL "BSCL.Plataforma.BD.Servicios::DepuracionMM"();
        END IF;
        
        IF (:v_EsGenerarMovimientoCustodia = 'S') THEN 
            UPDATE "BSCL.Custodia.BD::GBL.GenerarMovimientoCustodia_Salida_Depuracion"  SET "FechaFinProceso" = CURRENT_UTCTIMESTAMP,
                   "Diferencia" = SECONDS_BETWEEN ("FechaInicioProceso",CURRENT_UTCTIMESTAMP)
            WHERE "IdProceso" = :v_IdProceso;
        END IF;
        
        DELETE FROM "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Entrada_Parametros";
        DELETE FROM "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Entrada_Entrada";
        
        
        
        
    END;
END; 
GO