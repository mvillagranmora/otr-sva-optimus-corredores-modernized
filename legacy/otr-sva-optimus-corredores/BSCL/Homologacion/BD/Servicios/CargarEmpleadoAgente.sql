SET SCHEMA "BSCL";
DROP PROCEDURE   "BSCL.Homologacion.BD.Servicios::CargarEmpleadoAgente";
CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarEmpleadoAgente"(
-- 
--  Proposito: Cargar informacion desde sebra para la migracion a optimus,
-- *************tablas del proceso************
-- Los parametros de entrada, datos, log y estadisticas son guardados bajo el Id de Ejecucion de carga entregado por el servicio.
-- "BSCL.Homologacion.BD::PAR.CargarDatosParametrosEntrada" --> Parametros de entrada del servicio (son grabados inmediatamente)
-- "BSCL.Homologacion.BD::GBL.CargarDatosPersona_Entrada_Persona" --> "BSCL.Homologacion.BD::PAR.CargarDatosPersona" --> Tabla de entrada del servicio, con las personas enviadas como datos de entrada. (es grabada inmediatamente)
-- "BSCL.Homologacion.BD::HOM.CargaLog" --> Problemas presentados en el proceso, (Bloqueantes,Advertencias, Errores)
-- "BSCL.Homologacion.BD::HOM.CargaEstadisticas" --> Estadisticas generadas por el servicio, datos cargados, cantidad de hombres, mujeres, etc.
-- tablas origen --> destino
-- "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" --> "BSCL.SocioNegocio.BD::EMP.Empleado"
-- "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" --> "BSCL.SocioNegocio.BD::EJE.Supervisor"
-- "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" --> "BSCL.SocioNegocio.BD::EJE.Ejecutivo"
-- "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" --> "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"
-- "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" --> "BSCL.Homologacion.BD::HOM.EmpleadoAgente"
            
--  Autor: Oscar Carreno Tolmo :: OCT
--  Fecha Creacion: 08/09/2017
     -- Parametros entrada comunes en todos los servicios publicos
     IN p_IdEmpresa     BIGINT,
     IN p_IdPais        BIGINT DEFAULT 44,
     IN p_IdUsuario     BIGINT,
     IN p_IdCarga       BIGINT,
     IN p_SimularOperacion NVARCHAR(1)     DEFAULT 'N',    --No graba los registros en la tabla de persona real
     OUT p_out_IdEjecucionCarga BIGINT,
     OUT p_out_AdvertenciasContador INT,
     OUT p_out_ErroresContador       INT
)
    LANGUAGE SQLSCRIPT
    SQL SECURITY INVOKER 
    DEFAULT SCHEMA "BSCL"
        ---READS SQL DATA 
AS
BEGIN
    BEGIN
        DECLARE v_Diccionario TABLE (
            "IdInterno" BIGINT,"Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520));
        
        DECLARE v_TablaMensajes TABLE (
            "IdInterno" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000));
        
        DECLARE v_Estadisticas TABLE (
            "Estadistica" NVARCHAR(20),"Seccion" NVARCHAR(20),"DatoEstadistica" NVARCHAR(255),"Origen" DECIMAL(26,8),"Destino" DECIMAL(26,8));
            
        DECLARE v_IdSUper, v_CountIdSUper BIGINT=0;
        SELECT "BSCL.Homologacion.BD.Secuencias::IdEjecucionCarga".NEXTVAL INTO p_out_IdEjecucionCarga FROM DUMMY;
        
        --0) Se graban inmediatamente los parametros de entrada
        v_PersonaEntrada = SELECT * FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Entrada_Persona";
        BEGIN AUTONOMOUS TRANSACTION
            INSERT INTO "BSCL.Homologacion.BD::PAR.CargarDatosParametrosEntrada" (
                "IdCargaEjecucion","NombreCarga","FechaInicioProceso","FechaFinProceso","ProcesoConProblemas",
                --PARAMETROS DE ENTRADA OBLIGATORIOS
                "IdEmpresa","IdPais","IdUsuario","IdCarga","SimularOperacion")
            SELECT
                :p_out_IdEjecucionCarga,::CURRENT_OBJECT_NAME,CURRENT_UTCTIMESTAMP,NULL,'N',
                --PARAMETROS DE ENTRADA OBLIGATORIOS
                :p_IdEmpresa,:p_IdPais,:p_IdUsuario,:p_IdCarga,:p_SimularOperacion
            FROM DUMMY;
            
            INSERT INTO "BSCL.Homologacion.BD::PAR.CargarEmpleadoAgente" (
                "IdCargaEjecucion",
                --------------------------
                "IdPersona","per_id")
            SELECT
                :p_out_IdEjecucionCarga,
                --------------------------
                "IdPersona","per_id"
            FROM :v_PersonaEntrada;
            COMMIT;
        END;--AUTONOMOUS TRANSACTION
        
        INSERT INTO "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_EsSupervisor" (
            "IdEmpleadoCorredora","emp_codigo")
        SELECT
            c."IdEmpleadoCorredora",c."emp_codigo"
        FROM (
            SELECT
                DISTINCT "emp_cod_super"
            FROM "BSCL.Homologacion.BD::GES.EmpleadoFilial"
            WHERE "IdCarga" = :p_IdCarga) AS f
        INNER JOIN "BSCL.Homologacion.BD::GES.EmpleadoCorredora" AS c ON (c."emp_codigo" = f."emp_cod_super"
                                                                      AND c."IdCarga" = :p_IdCarga);
            
            
        INSERT INTO "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" (
            "IdEmpleado","IdPersona","per_id","IdEmpleadoCorredora","IdEmpleadoFilial","CodigoEmpleadoPrincipal","CantidadCodigos","CodigoEmpleado","CodigoSupervisor",
            "IdSucursal","FechaInicioCargo","FechaTerminoCargo","FechaIngreso","IdEstado","CodigoTipoCargoEmpleado","RestringeCarteraCliente",
            "RestringeOrdenCliente","EsAgente","EsSupervisor","UtilizaPlantillaEmpleado","IdPlantillaEmpleado","CargoIndefinido","EsEnrolado",
            "IdEmpleadoSupervisorCarga",
            --Datos para mensajes problemas
            "emp_cod_super","est_id","par_tif_id","par_tpa_id","suc_id",
            --Problemas
            "ExistenProblemas","EsUtilizable",
            -------,
            "ExisteEmpleado","EsEmpleado","ExisteEstadoEmpleado","ExisteTipoCargoEmpleado","ExistePlantillaEmpleado","ExisteSucursal")
        SELECT
            "BSCL.SocioNegocio.BD.Secuencias::Empleado".NEXTVAL, "IdPersona","per_id",COALESCE("IdEmpleadoCorredora",("per_id"*-1)),COALESCE("IdEmpleadoFilial",("per_id"*-1)),
            DENSE_RANK() over (PARTITION BY "per_id" ORDER BY "IdEstado","EsSupervisor" DESC,"FechaInicioCargo" DESC,"emp_fec_modifica" desc),
            COUNT("per_id") over (PARTITION BY "per_id"),"CodigoEmpleado","CodigoSupervisor","IdSucursal","FechaInicioCargo","FechaTerminoCargo",
            "FechaIngreso","IdEstado","CodigoTipoCargoEmpleado","RestringeCarteraCliente",
            "RestringeOrdenCliente","EsAgente","EsSupervisor",CASE WHEN "EsAgente" = 'S' THEN 'S' ELSE 'N' END,"IdPlantillaEmpleado",
            CASE WHEN "FechaTerminoCargo" = '0001-01-01' THEN 'S' ELSE 'N' END,"EsEnrolado",
            "IdEmpleadoSupervisorCarga",
            --Datos para mensajes problemas
            "emp_cod_super","est_id","par_tif_id","par_tpa_id","suc_id",
            --Problemas
            CASE WHEN "ExisteEmpleado" = 'S' THEN 'S'
                 WHEN "EsEmpleado" = 'N' THEN 'S'
                 WHEN "ExisteEstadoEmpleado" = 'N' THEN 'S'
                 WHEN "ExisteTipoCargoEmpleado" = 'N' THEN 'S'
                 WHEN "ExistePlantillaEmpleado" = 'N' THEN 'S'
                 WHEN "ExisteSucursal" = 'N' THEN 'S'
            END,'S',
            ----
            "ExisteEmpleado","EsEmpleado","ExisteEstadoEmpleado","ExisteTipoCargoEmpleado","ExistePlantillaEmpleado","ExisteSucursal"
        FROM (
            SELECT
                p."IdPersona",p."per_id",c."IdEmpleadoCorredora",f."IdEmpleadoFilial",c."emp_codigo" AS "CodigoEmpleado",
                f."emp_cod_super" AS "CodigoSupervisor",s."IdSucursal" AS "IdSucursal",c."emp_fec_modifica",
                CASE WHEN c."fec_inicio_funciones" = '1900-01-01' THEN '0001-01-01' ELSE c."fec_inicio_funciones" END AS "FechaInicioCargo",
                CASE WHEN c."fec_temino_funciones" = '1900-01-01' THEN '0001-01-01' ELSE c."fec_temino_funciones" END AS "FechaTerminoCargo",
                CASE WHEN c."emp_fec_ingreso" = '1900-01-01' THEN '0001-01-01' ELSE c."emp_fec_ingreso" END AS "FechaIngreso",
                e."ValorDestino" AS "IdEstado",te."ValorDestino" AS "CodigoTipoCargoEmpleado",
                f."emp_ind_vercli" AS "RestringeCarteraCliente",f."emp_ind_verord" AS "RestringeOrdenCliente",
                CASE WHEN c."par_ten_id" = 'TENTIAGE' THEN 'S' ELSE 'N' END AS "EsAgente",
                CASE WHEN COALESCE(es."IdEmpleadoCorredora",-1) != -1  THEN 'S' ELSE 'N' END AS "EsSupervisor",
                pe."ValorDestino" AS "IdPlantillaEmpleado",CASE WHEN c."est_id" = 16 THEN 'N' ELSE 'S' END AS "EsEnrolado", --16, ENROLAMIENTO
                sp."IdEmpleadoCorredora" AS "IdEmpleadoSupervisorCarga",
                --Datos para mensajes problemas
                f."emp_cod_super",c."est_id",c."par_tif_id",c."par_tpa_id",f."suc_id",
                --Problemas
                CASE WHEN COALESCE(em."per_id",-1) != -1 THEN 'S' ELSE 'N' END "ExisteEmpleado",
                CASE WHEN COALESCE(c."per_id",-1) != -1 THEN 'S' ELSE 'N' END "EsEmpleado",
                CASE WHEN COALESCE(e."ValorDestino",'-1') = '-1' AND COALESCE(c."per_id",-1) != -1 THEN 'N' ELSE 'S' END AS "ExisteEstadoEmpleado",
                CASE WHEN COALESCE(te."ValorDestino",'-1') = '-1' AND COALESCE(c."per_id",-1) != -1 THEN 'N' ELSE 'S' END AS "ExisteTipoCargoEmpleado",
                CASE WHEN COALESCE(pe."ValorDestino",'-1') = '-1' AND COALESCE(c."per_id",-1) != -1 THEN 'N' ELSE 'S' END AS "ExistePlantillaEmpleado",
                CASE WHEN COALESCE(s."IdSucursal",'-1') = '-1' AND COALESCE(c."per_id",-1) != -1 THEN 'N' ELSE 'S' END AS "ExisteSucursal"
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Entrada_Persona" AS p
            LEFT JOIN "BSCL.Homologacion.BD::GES.EmpleadoCorredora" AS c ON (c."per_id" = p."per_id"
                                                                         AND c."IdCarga" = :p_IdCarga)                                                                         
            LEFT JOIN "BSCL.Homologacion.BD::GES.EmpleadoFilial" AS f ON (f."emp_codigo" = c."emp_codigo" 
                                                                      AND f."IdCarga" = :p_IdCarga)
            LEFT JOIN "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_EsSupervisor" AS es ON (es."emp_codigo" = c."emp_codigo")
            LEFT JOIN "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_EsSupervisor" AS sp ON (sp."emp_codigo" = f."emp_cod_super")
            LEFT JOIN (SELECT "per_id" FROM "BSCL.Homologacion.BD::GBL.CargarDatosPersona_Entrada_Persona"
                       INTERSECT
                       SELECT "IdOrigen" FROM "BSCL.SocioNegocio.BD::EMP.Empleado" WHERE "IdEmpresa" = :p_IdEmpresa) AS em ON (em."per_id" = p."per_id")
            LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS e  ON (e."TipoValor" = 'EstadoEmpleado'
                                                                     AND e."Entidad" = 'EMPLEADO'
                                                                     AND e."ValorOrigen" = c."est_id")
            LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS te  ON (te."TipoValor" = 'TipoEmpleado'
                                                                      AND te."Entidad" = 'EMPLEADO'
                                                                      AND te."ValorOrigen" = c."par_tif_id")
            LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS pe  ON (pe."TipoValor" = 'PlantillaEmpleado'
                                                                      AND pe."Entidad" = 'EMPLEADO'
                                                                      AND pe."ValorOrigen" = c."par_tpa_id")                                                                  
            LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS s ON (s."CodigoSucursal" = f."suc_id"
                                                              AND s."IdEmpresa" = :p_IdEmpresa) 
        );
	   
   

        
        --SE ACTUALIZA EL CODIGO DE EMPLEADO, DEJANDO EL CODIGO DE EMPLEADO PRINCIPAL.
       /* UPDATE e
        SET
            "CodigoEmpleadoCorregido" = cp."CodigoEmpleado",
            "IdEmpleadoCorredoraCorregido" = cp."IdEmpleadoCorredora"
        FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS e
        INNER JOIN "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS cp ON (cp."IdPersona" = e."IdPersona"
                                                                                           AND cp."CodigoEmpleadoPrincipal" = 1)
        WHERE e."CantidadCodigos" > 1;
        */
        
        UPDATE e
        SET
            "CodigoEmpleadoCorregido" = e."CodigoEmpleado",
            "IdEmpleadoCorredoraCorregido" = e."IdEmpleadoCorredora"
        FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS e;
        --WHERE e."CantidadCodigos" > 1;
        
        
        --SE ACTUALIZA EL CODIGO SUPERVISORIOR, DEJANDO EL CODIGO DEL EMPLEADO PRINCIPAL
        UPDATE e
        SET
            "CodigoSupervisorCorregido" = cs."CodigoEmpleadoCorregido",
            "IdEmpleadoSupervisorCarga" = cs."IdEmpleadoCorredoraCorregido"
        FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS cs
        INNER JOIN "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS e ON (e."CodigoSupervisor" = cs."CodigoEmpleado")
        WHERE cs."CantidadCodigos" > 1;
      
        --SE CREAN LOS SUPERVISORES  --AKI
        INSERT INTO "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Supervisor" (
            "IdPersona","per_id","IdEmpleadoCorredora","IdEstado",
            --Problemas
            "ExistenProblemas","EsUtilizable",
            -------
            "ExisteSupervisor")
        SELECT
            e."IdPersona",e."per_id",e."IdEmpleadoCorredora",e."IdEstado",
            --Problemas
            CASE WHEN COALESCE(s."per_id",-1) != -1 THEN 'S' ELSE 'N' END "ExistenProblemas",'S',
            -------
            CASE WHEN COALESCE(s."per_id",-1) != -1 THEN 'S' ELSE 'N' END "ExisteSupervisor"  
        FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS e
        LEFT JOIN (SELECT "per_id" FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado"
                   INTERSECT
                   SELECT "IdOrigen" FROM "BSCL.SocioNegocio.BD::EJE.Supervisor") AS s ON (s."per_id" = e."per_id")
        WHERE e."EsSupervisor" = 'S'
         /* AND e."CodigoEmpleadoPrincipal" = 1*/;
          
        
        --SE CREAN LOS EJECUTIVOS
        INSERT INTO "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Ejecutivo" (
            "IdPersona","per_id","IdEmpleadoCorredora","IdEmpleadoSupervisorCarga","IdEstado",
            --Problemas
            "ExistenProblemas","EsUtilizable",
            -------
            "ExisteEjecutivo")
        SELECT
            e."IdPersona",e."per_id",e."IdEmpleadoCorredora",e."IdEmpleadoSupervisorCarga",e."IdEstado",
            --Problemas
            CASE WHEN COALESCE(ej."per_id",-1) != -1 THEN 'S' ELSE 'N' END "ExistenProblemas",'S',
            -------
            CASE WHEN COALESCE(ej."per_id",-1) != -1 THEN 'S' ELSE 'N' END "ExisteEjecutivo"  
        FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS e
        LEFT JOIN (SELECT "per_id" FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado"
                   INTERSECT
                   SELECT "IdOrigen" FROM "BSCL.SocioNegocio.BD::EJE.Ejecutivo") AS ej ON (ej."per_id" = e."per_id")
        WHERE e."EsAgente" = 'S'
          /*AND e."CodigoEmpleadoPrincipal" = 1*/;
      
        -- 3) Si no existen datos disponibles en las tablas de homologacion, indicar error u advertencia
        v_EmpleadoProblemas =
            SELECT * FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" WHERE "ExistenProblemas" = 'S' /*AND "CodigoEmpleadoPrincipal" = 1*/;
            
        v_SupervisorProblemas =
            SELECT * FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Supervisor" WHERE "ExistenProblemas" = 'S';

        v_EjecutivoProblemas =
            SELECT * FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Ejecutivo" WHERE "ExistenProblemas" = 'S';
            

        --B-->Bloqueante (nivel servicio),E-->Error (nivel tabla),W-->Advertencia    
        INSERT INTO "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Problemas" (
            "IdInterno","SeccionProblema","per_id","IdOrigenCarga",
            -----------------------------------------------------------------
            --CAMPOS DE ERRORES
            "TipoProblema","CodigoMensaje","RequiereAutorizacion","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2")
        SELECT
            ROW_NUMBER() over (ORDER BY "IdOrigenCarga"),"SeccionProblema","per_id","IdOrigenCarga",
            -----------------------------------------------------------------
            --CAMPOS DE ERRORES
            "TipoProblema","CodigoMensaje","RequiereAutorizacion","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2"
        FROM (
            --Empleado
            SELECT
                'EMPLEADO' AS "SeccionProblema","per_id","IdEmpleadoCorredora" AS "IdOrigenCarga",'B' AS "TipoProblema",'CARGA_EMPLEADO_EXISTE' AS "CodigoMensaje",
                'N' AS "RequiereAutorizacion",'per_id' AS "CodigoDato1",CAST ("per_id" AS NVARCHAR(520)) AS "ValorDato1",
                '' AS "CodigoDato2",'' AS "ValorDato2"
            FROM :v_EmpleadoProblemas WHERE "ExisteEmpleado" = 'S' UNION ALL
            SELECT
                'EMPLEADO' AS "SeccionProblema","per_id","IdEmpleadoCorredora" AS "IdOrigenCarga",'B' AS "TipoProblema",'CARGA_NO_ES_EMPLEADO' AS "CodigoMensaje",
                'N' AS "RequiereAutorizacion",'per_id' AS "CodigoDato1",CAST ("per_id" AS NVARCHAR(520)) AS "ValorDato1",
                '' AS "CodigoDato2",'' AS "ValorDato2"
            FROM :v_EmpleadoProblemas WHERE "EsEmpleado" = 'N' UNION ALL
            SELECT
                'EMPLEADO' AS "SeccionProblema","per_id","IdEmpleadoCorredora" AS "IdOrigenCarga",'W' AS "TipoProblema",'CARGA_ESTADO_EMPLEADO_NO_EXISTE' AS "CodigoMensaje",
                'N' AS "RequiereAutorizacion",'TipoValor' AS "CodigoDato1",'EstadoEmpleado' AS "ValorDato1",'ValorOrigen' AS "CodigoDato2",
                "est_id" AS "ValorDato2"
            FROM :v_EmpleadoProblemas WHERE "ExisteEstadoEmpleado" = 'N' UNION ALL
            SELECT
                'EMPLEADO' AS "SeccionProblema","per_id","IdEmpleadoCorredora" AS "IdOrigenCarga",'W' AS "TipoProblema",'CARGA_TIPO_CARGO_EMPLEADO_NO_EXISTE' AS "CodigoMensaje",
                'N' AS "RequiereAutorizacion",'TipoValor' AS "CodigoDato1",'EMPLEADO' AS "ValorDato1",'ValorOrigen' AS "CodigoDato2",
                "par_tif_id" AS "ValorDato2"
            FROM :v_EmpleadoProblemas WHERE "ExisteTipoCargoEmpleado" = 'N' UNION ALL
            SELECT
                'EMPLEADO' AS "SeccionProblema","per_id","IdEmpleadoCorredora" AS "IdOrigenCarga",'W' AS "TipoProblema",'CARGA_PLANTILLA_EMPLEADO_NO_EXISTE' AS "CodigoMensaje",
                'N' AS "RequiereAutorizacion",'TipoValor' AS "CodigoDato1",'PlantillaEmpleado' AS "ValorDato1",'ValorOrigen' AS "CodigoDato2",
                "par_tpa_id" AS "ValorDato2"
            FROM :v_EmpleadoProblemas WHERE "ExistePlantillaEmpleado" = 'N' UNION ALL
            SELECT
                'EMPLEADO' AS "SeccionProblema","per_id","IdEmpleadoCorredora" AS "IdOrigenCarga",'W' AS "TipoProblema",'CARGA_SUCURSAL_EMPLEADO_NO_EXISTE' AS "CodigoMensaje",
                'N' AS "RequiereAutorizacion",'CodigoSucursal' AS "CodigoDato1","suc_id" AS "ValorDato1",'IdEmpresa' AS "CodigoDato2",
                CAST(:p_IdEmpresa AS NVARCHAR(520)) AS "ValorDato2"
            FROM :v_EmpleadoProblemas WHERE "ExisteSucursal" = 'N' UNION ALL
            --Supervisor
            SELECT
                'SUPERVISOR' AS "SeccionProblema","per_id","IdEmpleadoCorredora" AS "IdOrigenCarga",'E' AS "TipoProblema",'CARGA_SUPERVISOR_EXISTE' AS "CodigoMensaje",
                'N' AS "RequiereAutorizacion",'per_id' AS "CodigoDato1",CAST ("per_id" AS NVARCHAR(520)) AS "ValorDato1",
                '' AS "CodigoDato2",'' AS "ValorDato2"
            FROM :v_SupervisorProblemas WHERE "ExisteSupervisor" = 'S' UNION ALL
            --Ejecutivo
            SELECT
                'EJECUTIVO' AS "SeccionProblema","per_id","IdEmpleadoCorredora" AS "IdOrigenCarga",'E' AS "TipoProblema",'CARGA_EJECUTIVO_EXISTE' AS "CodigoMensaje",
                'N' AS "RequiereAutorizacion",'per_id' AS "CodigoDato1",CAST ("per_id" AS NVARCHAR(520)) AS "ValorDato1",
                '' AS "CodigoDato2",'' AS "ValorDato2"
            FROM :v_EjecutivoProblemas WHERE "ExisteEjecutivo" = 'S'
        );     
        
        -- 4) Se determina si se puede cargar o no
        -- Empleado
        UPDATE e
        SET
            "EsUtilizable" = 'N'
        FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS e
        INNER JOIN (SELECT DISTINCT "IdEmpleadoCorredora" FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado"
                    INTERSECT
                    (
                        SELECT "IdOrigenCarga" AS "IdEmpleadoCorredora" FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Problemas"
                        WHERE "SeccionProblema" = 'EMPLEADO' 
                          AND "TipoProblema" = 'E'
                        UNION ALL
                        (
                            SELECT
                                "IdEmpleadoCorredora"
                            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS e
                            INNER JOIN "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Problemas" AS p ON (p."per_id" = e."per_id"
                                                                                                               AND p."TipoProblema" = 'B'))
                      )  
                   ) AS p ON (p."IdEmpleadoCorredora" = e."IdEmpleadoCorredora");
        
        --Supervisor
        UPDATE s
        SET
            "EsUtilizable" = 'N'
        FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Supervisor" AS s
        INNER JOIN (SELECT DISTINCT "IdEmpleadoCorredora" FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Supervisor"
                    INTERSECT
                    (
                        SELECT "IdOrigenCarga" AS "IdEmpleadoCorredora" FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Problemas"
                        WHERE "SeccionProblema" = 'SUPERVISOR' 
                          AND "TipoProblema" = 'E'
                        UNION ALL
                        (
                            SELECT
                                "IdEmpleadoCorredora"
                            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Supervisor" AS s
                            INNER JOIN "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Problemas" AS p ON (p."per_id" = s."per_id"
                                                                                                               AND p."TipoProblema" = 'B'))
                      )  
                   ) AS p ON (p."IdEmpleadoCorredora" = s."IdEmpleadoCorredora");
                   
       -- Ejecutivo
        UPDATE e
        SET
            "EsUtilizable" = 'N'
        FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Ejecutivo" AS e
        INNER JOIN (SELECT DISTINCT "IdEmpleadoCorredora" FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Ejecutivo"
                    INTERSECT
                    (
                        SELECT "IdOrigenCarga" AS "IdEmpleadoCorredora" FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Problemas"
                        WHERE "SeccionProblema" = 'EJECUTIVO' 
                          AND "TipoProblema" = 'E'
                        UNION ALL
                        (
                            SELECT
                                "IdEmpleadoCorredora"
                            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Ejecutivo" AS e
                            INNER JOIN "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Problemas" AS p ON (p."per_id" = e."per_id"
                                                                                                               AND p."TipoProblema" = 'B'))
                      )  
                   ) AS p ON (p."IdEmpleadoCorredora" = e."IdEmpleadoCorredora");
       
                
        -- 5) Se obtienen los folios de optimus para las tablas, cuando no sea simulacion y no hayan existidos errores
        --IF (:p_SimularOperacion = 'N') THEN
          --  UPDATE e
           -- SET
             --   "IdEmpleado" = "BSCL.SocioNegocio.BD.Secuencias::Empleado".NEXTVAL
            --FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS e
            --WHERE e."EsUtilizable" = 'S'
              /*AND e."CodigoEmpleadoPrincipal" = 1*/
        --END IF;
       
        -- 6) Se corrigen folios
        UPDATE e
        SET 
            "IdEmpleado" = COALESCE(e."IdEmpleado",ie."IdEmpleado",ie."IdEmpleadoCorredoraCorregido",e."IdEmpleadoCorredora")
        FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS e
        LEFT JOIN "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS ie ON (ie."IdEmpleadoCorredora" = e."IdEmpleadoCorredoraCorregido");
        
        UPDATE s
        SET 
            "IdEmpleadoSupervisor" = COALESCE(ie."IdEmpleado",ie."IdEmpleadoSupervisorCarga")
        FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS s
        INNER JOIN "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS ie ON (ie."IdEmpleadoCorredora" = s."IdEmpleadoSupervisorCarga");
        
        UPDATE e
        SET
            "IdDireccionElectronica" = d."IdDireccionElectronica"
        FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS e
        INNER JOIN (
                    SELECT
                        DENSE_RANK() over (PARTITION BY d."IdPersona" ORDER BY d."IdEstado",d."Predeterminado" DESC,d."IdDireccionElectronica" DESC) AS "DireccionPrincipal",
                        e."IdEmpleado",d."IdDireccionElectronica"
                    FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS e
                    INNER JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS d ON (d."IdPersona" = e."IdPersona"
                                                                                     AND d."CodigoProposito" = 'COME')
                    WHERE "EsUtilizable" = 'S'
                    ) AS d ON (d."IdEmpleado" = e."IdEmpleado"
                           AND d."DireccionPrincipal" = 1);
               
        UPDATE s
        SET 
            "IdEmpleadoSupervisor" = ie."IdEmpleado"
        FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Supervisor" AS s
        INNER JOIN "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS ie ON (ie."IdEmpleadoCorredora" = s."IdEmpleadoCorredora" AND ie."EsUtilizable" = 'S' /* AND ie."CodigoEmpleadoPrincipal" = 1*/);
       
        UPDATE e
        SET 
            "IdEmpleadoEjecutivo" = ie."IdEmpleado",
            "IdEmpleadoSupervisor" = s."IdEmpleadoSupervisor"
        FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Ejecutivo" AS e
        INNER JOIN "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS ie ON (ie."IdEmpleadoCorredora" = e."IdEmpleadoCorredora" AND ie."EsUtilizable" = 'S'/*  AND ie."CodigoEmpleadoPrincipal" = 1*/)
        LEFT JOIN "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Supervisor" AS s ON (s."IdEmpleadoCorredora" = e."IdEmpleadoSupervisorCarga" AND s."EsUtilizable" = 'S'  );
        
        -- 7) Desplegar la informacion hacia fuera si es simulada o insertar en la tabla definitiva de optimus (la informacion debe ser igual)
        IF (:p_SimularOperacion = 'S') THEN
            SELECT
                "IdEmpleado","IdPersona",COALESCE("CodigoEmpleadoCorregido","CodigoEmpleado") AS "CodigoEmpleado",:p_IdEmpresa AS "IdEmpresa",
                "IdEmpleadoSupervisor","IdSucursal","FechaInicioCargo","FechaTerminoCargo","FechaIngreso",CURRENT_UTCTIMESTAMP AS "FechaCreacion",
                '0001-01-01' AS "FechaModificacion",:p_IdCarga AS "IdCarga","per_id" AS "IdOrigen","IdEstado","CodigoEmpleado" AS "CodigoAgenteMigracion",
                "CodigoSupervisorCorregido" AS "CodigoSupervisorAgenteMigracion",'N' AS "Borrado","EsEnrolado",:p_IdUsuario AS "IdUsuarioCreador",
                "CodigoTipoCargoEmpleado","UtilizaPlantillaEmpleado","IdPlantillaEmpleado",'' AS "IdTipoBloqueo",'0001-01-01' AS "FechaBloqueo",
                '' AS "ComentarioBloqueo",'' AS "IdTipoDesbloqueo",'0001-01-01'"FechaDesbloqueo",'' AS "ComentarioDesbloqueo",'' AS "DetalleCargo",
                "IdDireccionElectronica","CargoIndefinido","RestringeCarteraCliente","RestringeOrdenCliente"
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS e
            WHERE e."EsUtilizable" = 'S'
              /*AND e."CodigoEmpleadoPrincipal" = 1*/;

            SELECT
                "IdEmpleadoSupervisor",CURRENT_UTCTIMESTAMP AS "FechaCreacion",:p_IdUsuario AS "IdUsuarioCreacion","IdEstado",:p_IdCarga AS "IdCarga",
                "per_id" AS "IdOrigen"
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Supervisor"
            WHERE "EsUtilizable" = 'S';
            
            SELECT
                "IdEmpleadoEjecutivo","IdEmpleadoSupervisor",CURRENT_UTCTIMESTAMP AS "FechaCreacion","IdEstado",NULL AS "IdEmpleadoReemplaza",
                '0001-01-01' AS "FechaInicioReemplazo",'0001-01-01' AS "FechaFinReemplazo",:p_IdUsuario AS "IdUsuarioCreacion",:p_IdCarga AS "IdCarga",
                "per_id" AS "IdOrigen"
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Ejecutivo"
            WHERE "EsUtilizable" = 'S';
            
            SELECT
                'BSCL.SocioNegocio.BD::SOC.SocioDeNegocio' AS "Salida",s."IdSocioDeNegocio",s."IdPersona",s."IdEmpresa",s."IdCliente",e."IdEmpleado",
                s."IdCarga",s."IdOrigen"
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS e
            INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS s ON (s."IdPersona" = e."IdPersona")
            WHERE e."EsUtilizable" = 'S'
              /*AND e."CodigoEmpleadoPrincipal" = 1*/;
            
            SELECT
                'BSCL.Homologacion.BD::HOM.EmpleadoAgente' AS "Salida",:p_IdCarga AS "IdCarga",:p_out_IdEjecucionCarga AS "IdCargaEjecucion","IdEmpleado",
                "IdPersona","per_id","IdEmpleadoCorredora","IdEmpleadoCorredoraCorregido","IdEmpleadoFilial","CodigoEmpleadoPrincipal","CantidadCodigos",
                "CodigoEmpleado","CodigoSupervisor","CodigoEmpleadoCorregido","CodigoSupervisorCorregido","IdEmpleadoSupervisor","IdEmpleadoSupervisorCarga"
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado"
            WHERE "EsUtilizable" = 'S';
        ELSE
        
         
            INSERT INTO "BSCL.SocioNegocio.BD::EMP.Empleado" (
                "IdEmpleado","IdPersona","CodigoEmpleado","IdEmpresa","IdEmpleadoSupervisor","IdSucursal","FechaInicioCargo","FechaTerminoCargo",
                "FechaIngreso","FechaCreacion","FechaModificacion","IdCarga","IdOrigen","IdEstado","CodigoAgenteMigracion","CodigoSupervisorAgenteMigracion",
                "Borrado","EsEnrolado","IdUsuarioCreador","CodigoTipoCargoEmpleado","UtilizaPlantillaEmpleado","IdPlantillaEmpleado","IdTipoBloqueo",
                "FechaBloqueo","ComentarioBloqueo","IdTipoDesbloqueo","FechaDesbloqueo","ComentarioDesbloqueo","DetalleCargo","IdDireccionElectronica",
                "CargoIndefinido","RestringeCarteraCliente","RestringeOrdenCliente")
            SELECT
                "IdEmpleado","IdPersona",COALESCE("CodigoEmpleadoCorregido","CodigoEmpleado") AS "CodigoEmpleado",:p_IdEmpresa AS "IdEmpresa",
                "IdEmpleadoSupervisor","IdSucursal","FechaInicioCargo","FechaTerminoCargo","FechaIngreso",CURRENT_UTCTIMESTAMP AS "FechaCreacion",
                '0001-01-01' AS "FechaModificacion",:p_IdCarga AS "IdCarga","per_id" AS "IdOrigen","IdEstado","CodigoEmpleado" AS "CodigoAgenteMigracion",
                "CodigoSupervisorCorregido" AS "CodigoSupervisorAgenteMigracion",'N' AS "Borrado","EsEnrolado",:p_IdUsuario AS "IdUsuarioCreador",
                "CodigoTipoCargoEmpleado","UtilizaPlantillaEmpleado","IdPlantillaEmpleado",'' AS "IdTipoBloqueo",'0001-01-01' AS "FechaBloqueo",
                '' AS "ComentarioBloqueo",'' AS "IdTipoDesbloqueo",'0001-01-01'"FechaDesbloqueo",'' AS "ComentarioDesbloqueo",'' AS "DetalleCargo",
                 COALESCE("IdDireccionElectronica",0),"CargoIndefinido",'N' AS "RestringeCarteraCliente",'N' AS "RestringeOrdenCliente"
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS e
            WHERE e."EsUtilizable" = 'S'
                AND e."CodigoEmpleado" NOT IN (SELECT "CodigoEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado")
              /*AND e."CodigoEmpleadoPrincipal" = 1*/;
              
            INSERT INTO "BSCL.SocioNegocio.BD::EJE.Supervisor" (
                "IdEmpleadoSupervisor","FechaCreacion","IdUsuarioCreacion","IdEstado","IdCarga","IdOrigen")
            SELECT
                "IdEmpleadoSupervisor",CURRENT_UTCTIMESTAMP AS "FechaCreacion",:p_IdUsuario AS "IdUsuarioCreacion","IdEstado",:p_IdCarga AS "IdCarga",
                "per_id" AS "IdOrigen"
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Supervisor"
            WHERE "EsUtilizable" = 'S' AND EXISTS (SELECT 1 FROM  "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" e 
            WHERE "IdEmpleado" ="IdEmpleadoSupervisor" AND e."EsUtilizable" = 'S'
            /*  AND e."CodigoEmpleadoPrincipal" = 1*/) ;
            
            
            SELECT Count(*) "IdEmpleadoSupervisor" INTO v_CountIdSUper FROM "BSCL.SocioNegocio.BD::EJE.Supervisor"  WHERE "IdCarga"= :p_IdCarga;
           
            IF (v_CountIdSUper > 0) THEN
              SELECT TOP 1 "IdEmpleadoSupervisor" INTO v_IdSUper FROM "BSCL.SocioNegocio.BD::EJE.Supervisor"  WHERE "IdCarga"= :p_IdCarga;
              
              INSERT INTO "BSCL.SocioNegocio.BD::EJE.Ejecutivo" (
                "IdEmpleadoEjecutivo","IdEmpleadoSupervisor","FechaCreacion","IdEstado","IdEmpleadoReemplaza","FechaInicioReemplazo","FechaFinReemplazo",
                "IdUsuarioCreacion","IdCarga","IdOrigen")
              SELECT
               "IdEmpleadoEjecutivo",coalesce("IdEmpleadoSupervisor",v_IdSUper),CURRENT_UTCTIMESTAMP AS "FechaCreacion","IdEstado",NULL AS "IdEmpleadoReemplaza",
               -- coalesce("IdEmpleadoEjecutivo",0),NULL AS "IdEmpleadoSupervisor",CURRENT_UTCTIMESTAMP AS "FechaCreacion","IdEstado",NULL AS "IdEmpleadoReemplaza",
                '0001-01-01' AS "FechaInicioReemplazo",'0001-01-01' AS "FechaFinReemplazo",:p_IdUsuario AS "IdUsuarioCreacion",:p_IdCarga AS "IdCarga",
                "per_id" AS "IdOrigen"
              FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Ejecutivo"
              WHERE "EsUtilizable" = 'S'; 
            
            END IF;  

              /* No acepta nulos para la foranea IdSupervisor se asume id default */
              
            
            
            /*
            AND EXISTS (SELECT 1 FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" e WHERE "IdEmpleado" ="IdEmpleadoSupervisor" AND e."EsUtilizable" = 'S'
              AND e."CodigoEmpleadoPrincipal" = 1)
            AND EXISTS (SELECT 1 FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" e WHERE "IdEmpleado" ="IdEmpleadoSupervisor" AND e."EsUtilizable" = 'S'
              AND e."CodigoEmpleadoPrincipal" = 1)
            AND EXISTS (SELECT 1 FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" e WHERE "IdEmpleado" ="IdEmpleadoSupervisor" AND e."EsUtilizable" = 'S'
              AND e."CodigoEmpleadoPrincipal" = 1);
              */
           
            UPDATE s
            SET
                "IdEmpleado" = e."IdEmpleado"
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado" AS e
            INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS s ON (s."IdPersona" = e."IdPersona")
            WHERE e."EsUtilizable" = 'S'
          /*    AND e."CodigoEmpleadoPrincipal" = 1*/;
                          
            INSERT INTO "BSCL.Homologacion.BD::HOM.EmpleadoAgente" (
                "IdCarga","IdCargaEjecucion","IdEmpleado","IdPersona","per_id","IdEmpleadoCorredora","IdEmpleadoCorredoraCorregido","IdEmpleadoFilial",
                "CodigoEmpleadoPrincipal","CantidadCodigos","CodigoEmpleado","CodigoSupervisor","CodigoEmpleadoCorregido","CodigoSupervisorCorregido",
                "IdEmpleadoSupervisor","IdEmpleadoSupervisorCarga")
            SELECT
                :p_IdCarga AS "IdCarga",:p_out_IdEjecucionCarga AS "IdCargaEjecucion","IdEmpleado","IdPersona","per_id","IdEmpleadoCorredora",
                "IdEmpleadoCorredoraCorregido","IdEmpleadoFilial","CodigoEmpleadoPrincipal","CantidadCodigos","CodigoEmpleado","CodigoSupervisor",
                "CodigoEmpleadoCorregido","CodigoSupervisorCorregido","IdEmpleadoSupervisor","IdEmpleadoSupervisorCarga"
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado"
            WHERE "EsUtilizable" = 'S';
            
           
             UPDATE  e
             SET "RestringeCarteraCliente" = CASE WHEN "emp_ind_vercli" = 'S' OR "emp_ind_verord" = 'S' THEN 'N' ELSE 'S' END   ,
             	  "RestringeOrdenCliente"  = CASE WHEN "emp_ind_vercli" = 'S' OR "emp_ind_verord" = 'S' THEN 'N' ELSE 'S' END   
             FROM "BSCL.SocioNegocio.BD::EMP.Empleado" AS e LEFT JOIN "BSCL.Homologacion.BD::GES.EmpleadoFilial" f
             			ON (f."emp_codigo"  = e."CodigoEmpleado"  AND e."IdCarga" = f."IdCarga") 
             WHERE e."IdCarga" = :p_IdCarga
             AND "IdOrigen"  IN (
            		SELECT "IdOrigen" FROM "BSCL.SocioNegocio.BD::EJE.Ejecutivo" a WHERE   a."IdCarga" = :p_IdCarga	
             	);
  
 
             
             	
          
            
        END IF;
         
        v_Estadisticas =
            SELECT
                'CARGA_TOTAL' AS "Estadistica",
                'EMPLEADO' AS "Seccion",
                '' AS "DatoEstadistica",
                COUNT("IdEmpleadoCorredora") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S'/* AND "CodigoEmpleadoPrincipal" = 1*/ THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado"
            UNION ALL
            SELECT
                'CARGA_CODIGOS_REPETIDOS' AS "Estadistica",
                'EMPLEADO' AS "Seccion",
                CAST("per_id" AS NVARCHAR(255)) AS "DatoEstadistica",
                COUNT("IdEmpleadoCorredora") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado"
            WHERE "CodigoEmpleadoPrincipal" = 1
              AND "CantidadCodigos" > 1
            GROUP BY "per_id"
            UNION ALL
            SELECT
                'CARGA_ESTADO' AS "Estadistica",
                'EMPLEADO' AS "Seccion",
                "IdEstado" AS "DatoEstadistica",
                COUNT("IdEmpleadoCorredora") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' /*AND "CodigoEmpleadoPrincipal" = 1 */THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado"
            GROUP BY "IdEstado"
            UNION ALL
            SELECT
                'CARGA_SUCURSAL' AS "Estadistica",
                'EMPLEADO' AS "Seccion",
                CAST("IdSucursal" AS NVARCHAR(255)) AS "DatoEstadistica",
                COUNT("IdEmpleadoCorredora") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' /*AND "CodigoEmpleadoPrincipal" = 1 */THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado"
            GROUP BY "IdSucursal"
            UNION ALL
            SELECT
                'CARGA_TIPO_CARGO_EMPLEADO' AS "Estadistica",
                'EMPLEADO' AS "Seccion",
                "CodigoTipoCargoEmpleado" AS "DatoEstadistica",
                COUNT("IdEmpleadoCorredora") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' /*AND "CodigoEmpleadoPrincipal" = 1 */THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado"
            GROUP BY "CodigoTipoCargoEmpleado"
            UNION ALL
            SELECT
                'CARGA_PLANTILLA_EMPLEADO' AS "Estadistica",
                'EMPLEADO' AS "Seccion",
                CAST("IdPlantillaEmpleado" AS NVARCHAR(255)) AS "DatoEstadistica",
                COUNT("IdEmpleadoCorredora") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' /*AND "CodigoEmpleadoPrincipal" = 1 */THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Empleado"
            GROUP BY "IdPlantillaEmpleado"
            UNION ALL
            SELECT
                'CARGA_TOTAL' AS "Estadistica",
                'SUPERVISOR' AS "Seccion",
                NULL AS "DatoEstadistica",
                COUNT("IdEmpleadoCorredora") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Supervisor"
            UNION ALL
            SELECT
                'CARGA_ESTADO' AS "Estadistica",
                'SUPERVISOR' AS "Seccion",
                "IdEstado" AS "DatoEstadistica",
                COUNT("IdEmpleadoCorredora") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Supervisor"
            GROUP BY "IdEstado"
            UNION ALL
            SELECT
                'CARGA_TOTAL' AS "Estadistica",
                'EJECUTIVO' AS "Seccion",
                NULL AS "DatoEstadistica",
                COUNT("IdEmpleadoCorredora") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Ejecutivo"
            UNION ALL
            SELECT
                'CARGA_ESTADO' AS "Estadistica",
                'EJECUTIVO' AS "Seccion",
                "IdEstado" AS "DatoEstadistica",
                COUNT("IdEmpleadoCorredora") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Ejecutivo"
            GROUP BY "IdEstado";
        --
        
        v_Diccionario =
            SELECT
                "IdInterno","CodigoMensaje" AS "Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2"
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Problemas";
        
        IF (NOT IS_EMPTY(:v_Diccionario)) THEN
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (
                P_TablaCodigosMensajes  => :v_Diccionario,
                p_out_TablaMensajes     => v_TablaMensajes);
            
            UPDATE p
            SET
                "Descripcion" = COALESCE(m."Descripcion",'')
            FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Problemas" p
            INNER JOIN :v_TablaMensajes AS m ON (m."IdInterno" = p."IdInterno");
        END IF;--IF (NOT IS_EMPTY(:v_Diccionario)) THEN
        
        SELECT count(1) into p_out_ErroresContador FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Problemas" WHERE "TipoProblema" IN ('B','E');
        SELECT count(1) into p_out_AdvertenciasContador FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Problemas" WHERE "TipoProblema" = 'W';
        v_Log = SELECT * FROM "BSCL.Homologacion.BD::GBL.CargarEmpleadoAgente_Proceso_Problemas";
                   

        --Siempre Grabar
        BEGIN AUTONOMOUS TRANSACTION
            INSERT INTO "BSCL.Homologacion.BD::HOM.CargaLog" (
                "IdCargaLog","IdCarga","IdCargaEjecucion","FechaCarga",
                -----------------------------------------------------------------
                "SeccionProblema","per_id","IdOrigenCarga",
                -----------------------------------------------------------------   
                --CAMPOS DE ERRORES
                "OrigenProblema","TipoProblema","CodigoMensaje","RequiereAutorizacion","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","Descripcion")
            SELECT
                "BSCL.Homologacion.BD.Secuencias::IdCargaLog".NEXTVAL,:p_IdCarga,:p_out_IdEjecucionCarga,CURRENT_UTCTIMESTAMP,
                -----------------------------------------------------------------
                "SeccionProblema","per_id","IdOrigenCarga",
                -----------------------------------------------------------------   
                --CAMPOS DE ERRORES
                ::CURRENT_OBJECT_NAME,"TipoProblema","CodigoMensaje","RequiereAutorizacion","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","Descripcion"
            FROM :v_Log;
            
            INSERT INTO "BSCL.Homologacion.BD::HOM.CargaEstadisticas" (
                "IdCargaEstadisticas","IdCarga","IdCargaEjecucion","OrigenEstadistica","Estadistica","Seccion","DatoEstadistica","Origen","Destino")
            SELECT
                "BSCL.Homologacion.BD.Secuencias::IdCargaEstadisticas".NEXTVAL,:p_IdCarga,:p_out_IdEjecucionCarga,::CURRENT_OBJECT_NAME,"Estadistica","Seccion",
                "DatoEstadistica","Origen","Destino"
            FROM :v_Estadisticas;
            
            UPDATE p
            SET
                "FechaFinProceso" = CURRENT_UTCTIMESTAMP,
                "ProcesoConProblemas" = CASE WHEN :p_out_ErroresContador > 0 THEN 'S'
                                             WHEN :p_out_AdvertenciasContador > 0 THEN 'S'
                                             ELSE 'N'
                                        END,
                "Diferencia" = SECONDS_BETWEEN ("FechaInicioProceso",CURRENT_UTCTIMESTAMP)
            FROM "BSCL.Homologacion.BD::PAR.CargarDatosParametrosEntrada" p
            WHERE "IdCargaEjecucion" = :p_out_IdEjecucionCarga;
                
            COMMIT;
        END;--AUTONOMOUS TRANSACTION
        
        SELECT 'BSCL.Homologacion.BD::HOM.CargaLog',* FROM :v_Log;
        SELECT 'BSCL.Homologacion.BD::HOM.CargaEstadisticas',* FROM :v_Estadisticas;
	END;	
END;
