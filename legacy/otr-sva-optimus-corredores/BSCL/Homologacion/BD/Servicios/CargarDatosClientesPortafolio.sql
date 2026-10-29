SET SCHEMA "BSCL";
DROP PROCEDURE   "BSCL.Homologacion.BD.Servicios::CargarDatosClientesPortafolio";
CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarDatosClientesPortafolio"(
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
            
--  Autor: Oscar Carreno Tolmo :: OCT
--  Fecha Creacion: 12/09/2017
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
        
        SELECT "BSCL.Homologacion.BD.Secuencias::IdEjecucionCarga".NEXTVAL INTO p_out_IdEjecucionCarga FROM DUMMY;
        
        --0) Se graban inmediatamente los parametros de entrada
        v_PersonaEntrada = SELECT * FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Entrada_Persona";
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
            
            INSERT INTO "BSCL.Homologacion.BD::PAR.CargarClientesPortafolio" (
                "IdCargaEjecucion",
                --------------------------
                "IdPersona","per_id","par_seg_id")
            SELECT
                :p_out_IdEjecucionCarga,
                --------------------------
                "IdPersona","per_id","par_seg_id"
            FROM :v_PersonaEntrada;
            COMMIT;
        END;--AUTONOMOUS TRANSACTION
        
        --1) Se obtienen los valores a procesar en el servicio    
        --Indicadores
        INSERT INTO "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Indicadores"(
            "IdPersona","per_id","IdIndicadoresCliente","IdIndicador","din_id","ind_id","ind_tipo","ind_ambito","ind_nombre","ind_tip_per",
            "ind_expira","ind_duracion_expira","ind_tipo_operador","ind_ind_requisito","reg_id")
        SELECT
            p."IdPersona",p."per_id",ic."IdIndicadoresCliente",i."IdIndicador",ic."din_id",i."ind_id",i."ind_tipo",i."ind_ambito",i."ind_nombre",
            i."ind_tip_per",i."ind_expira",i."ind_duracion_expira",i."ind_tipo_operador",i."ind_ind_requisito",i."reg_id"
        FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Entrada_Persona" AS p
        INNER JOIN "BSCL.Homologacion.BD::GES.IndicadoresCliente" AS ic ON (ic."per_id" = p."per_id"
                                                                        AND ic."IdCarga" = :p_IdCarga)
        INNER JOIN "BSCL.Homologacion.BD::GES.Indicador" AS i ON (i."ind_id" = ic."ind_id"
                                                              AND i."IdCarga" = ic."IdCarga");
        
        --Clientes
		INSERT INTO "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cliente" (
            "IdPersona","per_id","IdClienteCarga","IdEstado","IdSegmentoCliente","IdCategoriaInversionista",
            "FechaBloqueo","ComentarioBloqueo","FechaIngreso","EsEnrolado","IdPersonaEjecutivo","IdTipoCliente",
            --Datos para mensajes problemas
            "est_id","emp_codigo","din_id","par_seg_id",
            --Problemas
            "ExistenProblemas","EsUtilizable",
             --
            "ExisteCliente","EsCliente","ExisteEstadoCliente","PoseeEjecutivo",
            --indicadores
            "PoseeIndicadorCategoriaInversionista","ExisteIndicadorCategoriaInversionista","PoseeIndicadorTipoCliente",
            "ExisteIndicadorTipoCliente","ExisteSegmentoCliente")
        SELECT
            "IdPersona","per_id",COALESCE("IdCliente","IdPersona" * -1),"IdEstado",CASE WHEN "IdSegmentoCliente" = '' THEN '0' ELSE "IdSegmentoCliente" END,"IdCategoriaInversionista",
            "cli_fec_modifica","cli_obs_bloqueo","cli_fec_ingreso","EsEnrolado","IdPersonaEjecutivo","IdTipoCliente",
            --Datos para mensajes problemas
            "est_id",upper("emp_codigo"),"din_id","par_seg_id",
            --Problemas
            CASE WHEN "ExisteCliente" = 'S' THEN 'S'
                 WHEN "EsCliente" = 'N' THEN 'S'
                 WHEN "ExisteEstadoCliente" = 'N' THEN 'S'
                 WHEN "PoseeEjecutivo" = 'N' THEN 'S'
                 WHEN "PoseeIndicadorCategoriaInversionista"  = 'N' THEN 'S'
                 WHEN "ExisteIndicadorCategoriaInversionista"  = 'N' THEN 'S'
                 WHEN "PoseeIndicadorTipoCliente"  = 'N' THEN 'S'
                 WHEN "ExisteIndicadorTipoCliente"  = 'N' THEN 'S'
                 WHEN "ExisteSegmentoCliente" = 'N' THEN 'S'
                 ELSE 'N'
            END,'S',
            "ExisteCliente","EsCliente","ExisteEstadoCliente","PoseeEjecutivo",
            --indicadores
            "PoseeIndicadorCategoriaInversionista","ExisteIndicadorCategoriaInversionista","PoseeIndicadorTipoCliente",
            "ExisteIndicadorTipoCliente","ExisteSegmentoCliente"
        FROM (
            SELECT 
                p."IdPersona",p."per_id",c."IdCliente",he."ValorDestino" AS "IdEstado",c."cli_fec_ingreso",
                CASE WHEN he."ValorDestino" NOT IN ('ACT') THEN c."cli_fec_modifica" ELSE '0001-01-01 00:00:00' END AS "cli_fec_modifica" ,
                CASE WHEN he."ValorDestino" NOT IN ('ACT') THEN c."cli_obs_bloqueo"  ELSE '' END AS "cli_obs_bloqueo" ,
                hci."ValorDestino" AS "IdCategoriaInversionista",COALESCE(htc."ValorDestino",'RETAIL') AS "IdTipoCliente",
                hsc."ValorDestino" AS "IdSegmentoCliente",CASE WHEN c."est_id" = '16' THEN 'N' ELSE 'S' END AS "EsEnrolado", --16, ENROLAMIENTO
                e."IdPersona" AS "IdPersonaEjecutivo",
                --Datos para mensajes problemas
                c."est_id",upper(c."emp_codigo") AS "emp_codigo",ci."din_id",p."par_seg_id",
                --Problemas
                CASE WHEN COALESCE(ec."per_id",-1) != -1 THEN 'S' ELSE 'N' END "ExisteCliente",
                CASE WHEN COALESCE(c."per_id",-1) != -1 THEN 'S' ELSE 'N' END "EsCliente",
                CASE WHEN COALESCE(he."ValorDestino",'-1') = '-1' AND COALESCE(c."per_id",-1) != -1 THEN 'N' ELSE 'S' END AS "ExisteEstadoCliente",
                CASE WHEN COALESCE(e."IdEmpleado",'-1') = '-1' AND COALESCE(c."per_id",-1) != -1 THEN 'N' ELSE 'S' END AS "PoseeEjecutivo",
                --Indicadores
                CASE WHEN COALESCE(ci."per_id",-1) = -1 AND COALESCE(c."per_id",-1) != -1 THEN 'N' ELSE 'S' END "PoseeIndicadorCategoriaInversionista",
                CASE WHEN COALESCE(hci."ValorDestino",'-1') = '-1' AND COALESCE(c."per_id",-1) != -1 THEN 'N' ELSE 'S' END AS "ExisteIndicadorCategoriaInversionista",
                CASE WHEN COALESCE(tc."per_id",-1) = -1 AND COALESCE(c."per_id",-1) != -1 THEN 'N' ELSE 'S' END "PoseeIndicadorTipoCliente",
                CASE WHEN COALESCE(htc."ValorDestino",'-1') = '-1' AND COALESCE(c."per_id",-1) != -1 THEN 'N' ELSE 'S' END AS "ExisteIndicadorTipoCliente",
                CASE WHEN COALESCE(hsc."ValorDestino",'-1') = '-1' AND COALESCE(c."per_id",-1) != -1 THEN 'N' ELSE 'S' END AS "ExisteSegmentoCliente"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Entrada_Persona" AS p
            LEFT JOIN "BSCL.Homologacion.BD::GES.Cliente" AS c ON (c."per_id" = p."per_id"
                                                               AND c."IdCarga" = :p_IdCarga)
            LEFT JOIN (SELECT "per_id" FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Entrada_Persona"
                       INTERSECT
                       SELECT "IdOrigen" FROM "BSCL.SocioNegocio.BD::CLI.Cliente" WHERE "IdEmpresa" = :p_IdEmpresa) AS ec ON (ec."per_id" = p."per_id")
            LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS he ON (he."TipoValor" = 'EstadoCliente' 
                                                                     AND he."Entidad" = 'CLIENTE' 
                                                                     AND he."ValorOrigen" = c."est_id")
            LEFT JOIN "BSCL.Homologacion.BD::HOM.EmpleadoAgente" AS e ON (upper(e."CodigoEmpleado") = upper(c."emp_codigo")
                                                                      AND e."IdCarga" = c."IdCarga")
            --INDICADORES
            --CATEGORÃƒï¿½A DEL INVERSIONISTA
            LEFT JOIN "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Indicadores" AS ci ON (ci."per_id" = p."per_id"
                                                                                                          AND ci."reg_id" = 'CATIG')
            LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hci ON (hci."TipoValor" = 'CategoriaInversionista' 
                                                                      AND hci."Entidad" = 'CLIENTE' 
                                                                      AND hci."ValorOrigen" = ci."din_id")
            --TIPO DE CLIENTE
            LEFT JOIN "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Indicadores" AS tc ON (tc."per_id" = p."per_id"
                                                                                                          AND tc."reg_id" = 'TPCLI')
            LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS htc ON (htc."TipoValor" = 'TipoCliente' 
                                                                      AND htc."Entidad" = 'CLIENTE' 
                                                                      AND htc."ValorOrigen" = tc."din_id")
            --SEGMENTO DEL CLIENTE
            LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hsc ON (hsc."TipoValor" = 'SegmentoCliente' 
                                                                      AND hsc."Entidad" = 'CLIENTE' 
                                                                      AND hsc."ValorOrigen" = p."par_seg_id")
        );
		--Correccion Cliente
        ---Todo cliente del tipo Persona Natural pasa de INSTITUCIONAL a RETAIL
			 UPDATE CLI SET CLI."IdTipoCliente"='RETAIL',CLI."IdSegmentoCliente"=2
			 FROM  BSCL."BSCL.SocioNegocio.BD::CLI.Cliente" CLI
			 INNER JOIN BSCL."BSCL.SocioNegocio.BD::PER.Persona" AS P ON (CLI."IdPersona"=P."IdPersona")
			 WHERE P."IdCarga" = :p_IdCarga
			 AND P."Tipo"='N' AND CLI."IdTipoCliente"='INSTITUCIONAL';
        
        --Portafolios
        INSERT INTO "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cuenta" (
            "IdPersona","per_id","IdClienteCarga","IdCuentaCarga","CodigoPortafolio","Proposito","IdEstado","FechaBloqueo",
            "ComentarioBloqueo","FechaIngreso","DescripcionPortafolio","IdEmpleadoEjecutivo","IdNombreFantasia","Alias","OrdenCuenta",
            "IdPerfilProductos","IdPerfilCobros","IdPerfilComercialBase",
            --Datos para mensajes problemas
            "par_tip_proposito","emp_codigo","rso_id","est_id",
            --Problemas
            "ExistenProblemas","EsUtilizable",
            --------------
            "PoseeCuentas","ExisteCuenta","ExisteProposito","PoseeEjecutivo","PoseeNombreFantasia","ExisteEstadoCuenta",
            "PoseePLantillaCobro","PoseePLantillaProducto")
        SELECT
            c."IdPersona",c."per_id",c."IdClienteCarga",c."IdCuenta",CAST(c."CUENTA" AS NVARCHAR(100)),coalesce(c."Proposito",''),c."IdEstado",c."FechaBloqueo",
            c."ComentarioBloqueo",c."FechaIngreso",c."DescripcionPortafolio",c."IdEmpleadoEjecutivo",c."IdNombreFantasia",c."Alias",c."OrdenCuenta",
            c."PLantillaProducto",c."PLantillaCobro",c."IdPerfilComercialBase",
            --Datos para mensajes problemas
            c."par_tip_proposito",upper(c."emp_codigo") AS emp_codigo,c."rso_id",c."est_id",
            --Problemas
            CASE WHEN c."PoseeCuentas" = 'N' THEN 'S'
                 WHEN COALESCE(ec."IdOrigen",'-1') != '-1' THEN 'S'
                 WHEN c."ExisteProposito" = 'N' THEN 'S'
                 WHEN c."PoseeEjecutivo" = 'N' THEN 'S'
                 WHEN c."PoseeNombreFantasia" = 'N' THEN 'S'
                 WHEN c."ExisteEstadoCuenta" = 'N' THEN 'S'
                 WHEN c."PoseePLantillaCobro" = 'N' THEN 'S'
                 WHEN c."PoseePLantillaProducto" = 'N' THEN 'S'
                 ELSE 'N'
            END,'S',
            --------------
            c."PoseeCuentas",CASE WHEN COALESCE(ec."IdOrigen",'-1') = '-1' THEN 'N' ELSE 'S' END AS "ExisteCuenta",c."ExisteProposito",
            c."PoseeEjecutivo",c."PoseeNombreFantasia",c."ExisteEstadoCuenta",c."PoseePLantillaCobro",c."PoseePLantillaProducto"
        FROM (
            SELECT
                p."IdPersona",p."per_id",p."IdClienteCarga",COALESCE(c."IdCuenta",p."IdClienteCarga") AS "IdCuenta",c."CUENTA",hpr."ValorDestino" AS "Proposito",
                he."ValorDestino" AS "IdEstado",c."cue_fec_bloqueo" AS "FechaBloqueo",c."cue_obs_bloqueo" AS "ComentarioBloqueo",
                c."cue_fec_ingreso" AS "FechaIngreso",c."cue_descripcion" AS "DescripcionPortafolio",e."IdEmpleado" AS "IdEmpleadoEjecutivo",
                f."IdNombreFantasia",c."cue_alias" AS "Alias",DENSE_RANK() over (PARTITION BY c."per_id" ORDER BY c."CUENTA") AS "OrdenCuenta",
                pc."PLantillaCobro",pc."PLantillaProducto",pc."ModeloComercial" AS "IdPerfilComercialBase",
                --Datos para mensajes problemas
                c."par_tip_proposito",upper(c."emp_codigo") AS "emp_codigo",c."rso_id",c."est_id",
                --Problemas
                CASE WHEN COALESCE(c."per_id",-1) = -1 THEN 'N' ELSE 'S' END AS "PoseeCuentas",
                CASE WHEN COALESCE(hpr."ValorDestino",'-1') = '-1' THEN 'N' ELSE 'S' END AS "ExisteProposito",
                CASE WHEN COALESCE(e."IdEmpleado",'-1') = '-1' THEN 'N' ELSE 'S' END AS "PoseeEjecutivo",
                CASE WHEN COALESCE(f."IdNombreFantasia",'-1') = '-1' THEN 'N' ELSE 'S' END AS "PoseeNombreFantasia",
                CASE WHEN COALESCE(he."ValorDestino",'-1') = '-1' THEN 'N' ELSE 'S' END AS "ExisteEstadoCuenta",
                CASE WHEN (COALESCE(pc."per_id",-1) = -1 OR COALESCE(pc."PLantillaCobro",'') = '') THEN 'N' ELSE 'S' END AS "PoseePLantillaCobro",
                CASE WHEN (COALESCE(pc."per_id",-1) = -1 OR COALESCE(pc."PLantillaProducto",'') = '') THEN 'N' ELSE 'S' END AS "PoseePLantillaProducto"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cliente" AS p
            LEFT JOIN "BSCL.Homologacion.BD::GES.Cuenta" AS c ON (c."per_id" = p."per_id"
                                                              AND c."IdCarga" = :p_IdCarga)
            LEFT JOIN "BSCL.Homologacion.BD::HOM.PerfilesPortafolio" AS pc ON (pc."per_id" = c."per_id"
                                                                           AND pc."CUENTA" = c."CUENTA")
            LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hpr ON (hpr."TipoValor" = 'PropositoCuenta' 
                                                                      AND hpr."Entidad" = 'PORTAFOLIO' 
                                                                      AND hpr."ValorOrigen" = c."par_tip_proposito")
                                                                      
            LEFT JOIN (SELECT h.* FROM BSCL."BSCL.SocioNegocio.BD::EJE.Ejecutivo" e INNER JOIN  
 									BSCL."BSCL.Homologacion.BD::HOM.EmpleadoAgente" h ON ( e."IdEmpleadoEjecutivo"=h."IdEmpleado")
			            ) AS e ON (e."CodigoEmpleado" = c."emp_codigo"  AND e."IdCarga" = c."IdCarga")
			            
            --LEFT JOIN "BSCL.Homologacion.BD::HOM.EmpleadoAgente" AS e ON (e."CodigoEmpleado" = upper(c."emp_codigo")
            --                                                          AND e."IdCarga" = c."IdCarga")
            LEFT JOIN "BSCL.SocioNegocio.BD::PER.NombreFantasia" AS f ON (f."IdOrigen" = c."rso_id")
            LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS he ON (he."TipoValor" = 'EstadoPortafolio' 
                                                                     AND he."Entidad" = 'PORTAFOLIO' 
                                                                     AND he."ValorOrigen" = c."est_id")
            WHERE p."EsCliente" = 'S' --NUEVO
        ) AS c
        LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS ec ON (ec."IdOrigen" = c."per_id"
                                                             AND ec."IdCuentaOrigen" = c."CUENTA");
       
                                                             
        -- 2) Si no existen datos disponibles en las tablas de homologacion, indicar error u advertencia
        v_ClienteProblemas =
            SELECT * FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cliente" WHERE "ExistenProblemas" = 'S';
        
        v_CuentaProblemas =
            SELECT * FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cuenta" WHERE "ExistenProblemas" = 'S';
        
            
        --B-->Bloqueante (nivel servicio),E-->Error (nivel tabla),W-->Advertencia    
        INSERT INTO "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Problemas" (
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
            --Cliente
            SELECT
                'CLIENTE' AS "SeccionProblema","per_id",NULL AS "CUENTA","IdClienteCarga" AS "IdOrigenCarga",'B' AS "TipoProblema",
                'CARGA_CLIENTE_EXISTE' AS "CodigoMensaje",'N' AS "RequiereAutorizacion",'per_id' AS "CodigoDato1",
                CAST("per_id" AS NVARCHAR(520)) AS "ValorDato1",'' AS "CodigoDato2",'' AS "ValorDato2"
            FROM :v_ClienteProblemas WHERE "ExisteCliente" = 'S' UNION ALL
            SELECT
                'CLIENTE' AS "SeccionProblema","per_id",NULL AS "CUENTA","IdClienteCarga" AS "IdOrigenCarga",'B' AS "TipoProblema",
                'CARGA_NO_ES_CLIENTE' AS "CodigoMensaje",'N' AS "RequiereAutorizacion",'per_id' AS "CodigoDato1",
                CAST("per_id" AS NVARCHAR(520)) AS "ValorDato1",'' AS "CodigoDato2",'' AS "ValorDato2"
            FROM :v_ClienteProblemas WHERE "EsCliente" = 'N' UNION ALL
            SELECT
                'CLIENTE' AS "SeccionProblema","per_id",NULL AS "CUENTA","IdCuentaCarga" AS "IdOrigenCarga",'B' AS "TipoProblema",
                'CARGA_CLIENTE_SIN_PORTAFOLIO' AS "CodigoMensaje",'N' AS "RequiereAutorizacion",'per_id' AS "CodigoDato1",
                CAST("per_id" AS NVARCHAR(520)) AS "ValorDato1",'' AS "CodigoDato2",'' AS "ValorDato2"
            FROM :v_CuentaProblemas WHERE "PoseeCuentas" = 'N' UNION ALL
            SELECT
                'CLIENTE' AS "SeccionProblema","per_id",NULL AS "CUENTA","IdClienteCarga" AS "IdOrigenCarga",'W' AS "TipoProblema",
                'CARGA_ESTADO_CLIENTE_NO_EXISTE' AS "CodigoMensaje",'N' AS "RequiereAutorizacion",'TipoValor' AS "CodigoDato1",
                'EstadoCliente' AS "ValorDato1",'ValorOrigen' AS "CodigoDato2","est_id" AS "ValorDato2"
            FROM :v_ClienteProblemas WHERE "ExisteEstadoCliente" = 'N' UNION ALL
            SELECT
                'CLIENTE' AS "SeccionProblema","per_id",NULL AS "CUENTA","IdClienteCarga" AS "IdOrigenCarga",'W' AS "TipoProblema",
                'CARGA_CLIENTE_SIN_EJECUTIVO' AS "CodigoMensaje",'N' AS "RequiereAutorizacion",'emp_codigo' AS "CodigoDato1",
                upper("emp_codigo") AS "ValorDato1",'' AS "CodigoDato2",'' AS "ValorDato2"
            FROM :v_ClienteProblemas WHERE "PoseeEjecutivo" = 'N' UNION ALL
            --indicadores
            SELECT
                'CLIENTE' AS "SeccionProblema","per_id",NULL AS "CUENTA","IdClienteCarga" AS "IdOrigenCarga",'W' AS "TipoProblema",
                'CARGA_CLIENTE_SIN_CATEGORIA_INVERSIONISTA' AS "CodigoMensaje",'N' AS "RequiereAutorizacion",
                'per_id' AS "CodigoDato1",CAST("per_id" AS NVARCHAR(520)) AS "ValorDato1",'reg_id' AS "CodigoDato2",'CATIG' AS "ValorDato2"
            FROM :v_ClienteProblemas WHERE "PoseeIndicadorCategoriaInversionista" = 'N' UNION ALL
            SELECT
                'CLIENTE' AS "SeccionProblema","per_id",NULL AS "CUENTA","IdClienteCarga" AS "IdOrigenCarga",'W' AS "TipoProblema",
                'CARGA_NO_EXISTE_CATEGORIA_INVERSIONISTA' AS "CodigoMensaje",'N' AS "RequiereAutorizacion",'TipoValor' AS "CodigoDato1",
                'CategoriaInversionista' AS "ValorDato1",'ValorOrigen' AS "CodigoDato2","din_id" AS "ValorDato2"
            FROM :v_ClienteProblemas WHERE "ExisteIndicadorCategoriaInversionista" = 'N' UNION ALL
            SELECT
                'CLIENTE' AS "SeccionProblema","per_id",NULL AS "CUENTA","IdClienteCarga" AS "IdOrigenCarga",'W' AS "TipoProblema",
                'CARGA_CLIENTE_SIN_TIPO' AS "CodigoMensaje",'N' AS "RequiereAutorizacion",'per_id' AS "CodigoDato1",
                CAST("per_id" AS NVARCHAR(520))  AS "ValorDato1",'reg_id' AS "CodigoDato2",'TPCLI' AS "ValorDato2"
            FROM :v_ClienteProblemas WHERE "PoseeIndicadorTipoCliente" = 'N' UNION ALL
            SELECT
                'CLIENTE' AS "SeccionProblema","per_id",NULL AS "CUENTA","IdClienteCarga" AS "IdOrigenCarga",'W' AS "TipoProblema",
                'CARGA_NO_EXISTE_TIPO_CLIENTE' AS "CodigoMensaje",'N' AS "RequiereAutorizacion",'Entidad' AS "CodigoDato1",
                'CLIENTE' AS "ValorDato1",'ValorOrigen' AS "CodigoDato2","din_id" AS "ValorDato2"
            FROM :v_ClienteProblemas WHERE "ExisteIndicadorTipoCliente" = 'N' UNION ALL
            SELECT
                'CLIENTE' AS "SeccionProblema","per_id",NULL AS "CUENTA","IdClienteCarga" AS "IdOrigenCarga",'W' AS "TipoProblema",
                'CARGA_NO_EXISTE_SEGMENTO_CLIENTE' AS "CodigoMensaje",'N' AS "RequiereAutorizacion",'TipoValor' AS "CodigoDato1",
                'TipoCliente' AS "ValorDato1",'ValorOrigen' AS "CodigoDato2","par_seg_id" AS "ValorDato2"
            FROM :v_ClienteProblemas WHERE "ExisteSegmentoCliente" = 'N' UNION ALL
            --Cuentas
            SELECT
                'CUENTA' AS "SeccionProblema","per_id","CodigoPortafolio","IdCuentaCarga" AS "IdOrigenCarga",'E' AS "TipoProblema",
                'CARGA_PORTAFOLIO_EXISTE' AS "CodigoMensaje",'N' AS "RequiereAutorizacion",'per_id' AS "CodigoDato1",
                CAST("per_id" AS NVARCHAR(520)) AS "ValorDato1",'CUENTA' AS "CodigoDato2","CodigoPortafolio" AS "ValorDato2"
            FROM :v_CuentaProblemas WHERE "ExisteCuenta" = 'S' UNION ALL
            SELECT
                'CUENTA' AS "SeccionProblema","per_id","CodigoPortafolio","IdCuentaCarga" AS "IdOrigenCarga",'W' AS "TipoProblema",
                'CARGA_PORTAFOLIO_SIN_PROPOSITO' AS "CodigoMensaje",'N' AS "RequiereAutorizacion",'TipoValor' AS "CodigoDato1",
                'PropositoCuenta' AS "ValorDato1",'ValorOrigen' AS "CodigoDato2","par_tip_proposito" AS "ValorDato2"
            FROM :v_CuentaProblemas WHERE "ExisteProposito" = 'N' UNION ALL
            SELECT
                'CUENTA' AS "SeccionProblema","per_id","CodigoPortafolio","IdCuentaCarga" AS "IdOrigenCarga",'W' AS "TipoProblema",
                'CARGA_PORTAFOLIO_SIN_EJECUTIVO' AS "CodigoMensaje",'N' AS "RequiereAutorizacion",'emp_codigo' AS "CodigoDato1",
                upper("emp_codigo") AS "ValorDato1",'' AS "CodigoDato2",'' AS "ValorDato2"
            FROM :v_CuentaProblemas WHERE "PoseeEjecutivo" = 'N' UNION ALL
            SELECT
                'CUENTA' AS "SeccionProblema","per_id","CodigoPortafolio","IdCuentaCarga" AS "IdOrigenCarga",'W' AS "TipoProblema",
                'CARGA_PORTAFOLIO_SIN_NOMBRE_FANTASIA' AS "CodigoMensaje",'N' AS "RequiereAutorizacion",'rso_id' AS "CodigoDato1",
                CAST("rso_id" AS NVARCHAR(520)) AS "ValorDato1",'' AS "CodigoDato2",'' AS "ValorDato2"
            FROM :v_CuentaProblemas WHERE "PoseeNombreFantasia" = 'N' UNION ALL
            SELECT
                'CUENTA' AS "SeccionProblema","per_id","CodigoPortafolio","IdCuentaCarga" AS "IdOrigenCarga",'W' AS "TipoProblema",
                'CARGA_ESTADO_PORTAFOLIO_NO_EXISTE' AS "CodigoMensaje",'N' AS "RequiereAutorizacion",'TipoValor' AS "CodigoDato1",
                'EstadoPortafolio' AS "ValorDato1",'ValorOrigen' AS "CodigoDato2","est_id" AS "ValorDato2"
            FROM :v_CuentaProblemas WHERE "ExisteEstadoCuenta" = 'N' UNION ALL
            SELECT
                'CUENTA' AS "SeccionProblema","per_id","CodigoPortafolio","IdCuentaCarga" AS "IdOrigenCarga",'W' AS "TipoProblema",
                'CARGA_PORTAFOLIO_SIN_PLANTILLA_COBRO' AS "CodigoMensaje",'N' AS "RequiereAutorizacion",'per_id' AS "CodigoDato1",
                CAST("per_id" AS NVARCHAR(520)) AS "ValorDato1",'CUENTA' AS "CodigoDato2","CodigoPortafolio" AS "ValorDato2"
            FROM :v_CuentaProblemas WHERE "PoseePLantillaCobro" = 'N' UNION ALL
            SELECT
                'CUENTA' AS "SeccionProblema","per_id","CodigoPortafolio","IdCuentaCarga" AS "IdOrigenCarga",'W' AS "TipoProblema",
                'CARGA_PORTAFOLIO_SIN_PLANTILLA_PRODUCTO' AS "CodigoMensaje",'N' AS "RequiereAutorizacion",'per_id' AS "CodigoDato1",
                CAST("per_id" AS NVARCHAR(520)) AS "ValorDato1",'CUENTA' AS "CodigoDato2","CodigoPortafolio" AS "ValorDato2"
            FROM :v_CuentaProblemas WHERE "PoseePLantillaProducto" = 'N'
        );
        
        -- 3) Se determina si se puede cargar o no
        -- Cliente
        UPDATE c
        SET
            "EsUtilizable" = 'N'
        FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cliente" AS c
        INNER JOIN (SELECT DISTINCT "IdClienteCarga" FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cliente"
                    INTERSECT
                    (
                        SELECT "IdOrigenCarga" AS "IdClienteCarga" FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Problemas"
                        WHERE "SeccionProblema" = 'CLIENTE' 
                          AND "TipoProblema" = 'E'
                        UNION ALL
                        (
                            SELECT
                                "IdClienteCarga"
                            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cliente" AS c
                            INNER JOIN "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Problemas" AS p ON (p."per_id" = c."per_id"
                                                                                                                        AND p."TipoProblema" = 'B'))
                      )  
                   ) AS p ON (p."IdClienteCarga" = c."IdClienteCarga");
        -- Cuenta
        UPDATE c
        SET
            "EsUtilizable" = 'N'
        FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cuenta" AS c
        INNER JOIN (SELECT DISTINCT "IdCuentaCarga" FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cuenta"
                    INTERSECT
                    (
                        SELECT "IdOrigenCarga" AS "IdCuentaCarga" FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Problemas"
                        WHERE "SeccionProblema" = 'CUENTA' 
                          AND "TipoProblema" = 'E'
                        UNION ALL
                        (
                            SELECT
                                "IdCuentaCarga"
                            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cuenta" AS c
                            INNER JOIN "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Problemas" AS p ON (p."per_id" = c."per_id"
                                                                                                                        AND p."TipoProblema" = 'B'))
                      )  
                   ) AS p ON (p."IdCuentaCarga" = c."IdCuentaCarga");
        
        -- 4) Se obtienen los folios de optimus para las tablas, cuando no sea simulacion y no hayan existidos errores
        IF (:p_SimularOperacion = 'N') THEN
            UPDATE c
            SET
                "IdCliente" = "BSCL.SocioNegocio.BD.Secuencias::Cliente".NEXTVAL
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cliente" AS c
            WHERE c."EsUtilizable" = 'S';
            
            UPDATE c
            SET
                "IdPortafolio" = "BSCL.Portafolio.BD.Secuencias::Portafolio".NEXTVAL
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cuenta" AS c
            WHERE c."EsUtilizable" = 'S';
        END IF;
        
        
        --Cliente
        UPDATE c 
        SET
            "IdCliente" = COALESCE(c."IdCliente",c."IdClienteCarga"),
            "IdPortafolioDefecto" = COALESCE(p."IdPortafolio",p."IdCuentaCarga")
        FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cliente" AS c
        INNER JOIN "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cuenta" AS p ON (p."IdClienteCarga" = c."IdClienteCarga"
                                                                                                 AND p."OrdenCuenta" = 1);
        -- Cuenta
        UPDATE p 
        SET
            "IdPortafolio" = COALESCE(p."IdPortafolio",p."IdCuentaCarga"),
            "IdCliente" = c."IdCliente",
            "IdPerfilProductosExtra" = 'EXT_' || COALESCE(p."IdPortafolio",p."IdCuentaCarga"),
            "IdPerfilCobrosExtra" = 'EXT_' || COALESCE(p."IdPortafolio",p."IdCuentaCarga"),
            "IdPerfilLimitesExtra" = 'EXT_' || COALESCE(p."IdPortafolio",p."IdCuentaCarga"),
            "IdPerfilCondicionesOperativasExtra" = 'EXT_' || COALESCE(p."IdPortafolio",p."IdCuentaCarga")
        FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cuenta" AS p 
        INNER JOIN "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cliente" AS c ON (c."IdClienteCarga" = p."IdClienteCarga");
        
        -- 5) Desplegar la informacion hacia fuera si es simulada o insertar en la tabla definitiva de optimus (la informacion debe ser igual)
        IF (:p_SimularOperacion = 'S') THEN
            SELECT
                "IdCliente","IdPersona",:p_IdEmpresa AS "IdEmpresa","IdEstado","IdSegmentoCliente","IdCategoriaInversionista",'' AS "IdTipoBloqueo",
                "FechaBloqueo","ComentarioBloqueo","FechaIngreso",CURRENT_UTCTIMESTAMP AS "FechaCreacion",'0001-01-01' AS "FechaModificacion","EsEnrolado",
                :p_IdCarga AS "IdCarga",:p_IdUsuario AS "IdUsuarioCreador","IdPortafolioDefecto",'N' AS "Borrado",'' AS "IdTipoDesbloqueo",
                '0001-01-01' AS "FechaDesbloqueo",'' AS "ComentarioDesbloqueo","IdPersonaEjecutivo",'' AS "CodigoEjecutivoExterno","IdTipoCliente",
                '' AS "IdPlantillaLimiteExtra",'' AS "CodigoAdicionalHolding",0 AS "IdSucursalPreferencia",0 AS "IdPersonaBrokerExtranjero",
                '' AS "IdClaseCliente",'0001-01-01' AS "FechaBloqueoTemporal",'0001-01-01' AS "FechaDesbloqueoTemporal","per_id" AS "IdOrigen"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cliente"
            WHERE "EsUtilizable" = 'S';
            
            SELECT
                "IdPortafolio","CodigoPortafolio","Proposito","IdCliente","IdEstado","FechaBloqueo","ComentarioBloqueo",'' AS "IdTipoBloqueo",
                "FechaIngreso",CURRENT_UTCTIMESTAMP AS "FechaCreacion",'0001-01-01' AS "FechaModificacion",:p_IdCarga AS "IdCarga",
                "IdPerfilComercialBase" AS "IdPerfilComercialBaseOrigen","IdPerfilProductos","IdPerfilCobros",'ING' AS "IdEstadoAutorizacion","DescripcionPortafolio",
                '' AS "IdPerfilLimites",'' AS "IdPerfilCondicionesOperativas","IdPerfilProductosExtra","IdPerfilCobrosExtra","IdPerfilLimitesExtra",
                "IdPerfilCondicionesOperativasExtra",'' AS "IdTipoDesbloqueo",'0001-01-01' AS "FechaDesbloqueo",'' AS "ComentarioDesbloqueo",
                0 AS "IdDireccionEnvioCorrespondencia",0 AS "IdTelefonoContacto","IdEmpleadoEjecutivo",'N' AS "EsEjecutivoExclusivo",'' AS "NumeroFicha",
                "IdNombreFantasia","Alias",0 AS "IdPersonaBrokerExtranjero",'' AS "IdPerfilCobrosPeru",'' AS "IdPerfilCobrosPeruExtra",
                "per_id" AS "IdOrigen","CodigoPortafolio" AS "IdCuentaOrigen"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cuenta"
            WHERE "EsUtilizable" = 'S';
            
            SELECT
                'BSCL.SocioNegocio.BD::SOC.SocioDeNegocio' AS "Salida",s."IdSocioDeNegocio",s."IdPersona",s."IdEmpresa",c."IdCliente",s."IdEmpleado",
                s."IdCarga",s."IdOrigen"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cliente" AS c
            INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS s ON (s."IdPersona" = c."IdPersona")
            WHERE c."EsUtilizable" = 'S';
        ELSE
        
          
            INSERT INTO "BSCL.SocioNegocio.BD::CLI.Cliente" (
                "IdCliente","IdPersona","IdEmpresa","IdEstado","IdSegmentoCliente","IdCategoriaInversionista","IdTipoBloqueo","FechaBloqueo",
                "ComentarioBloqueo","FechaIngreso","FechaCreacion","FechaModificacion","EsEnrolado","IdCarga","IdUsuarioCreador","IdPortafolioDefecto",
                "Borrado","IdTipoDesbloqueo","FechaDesbloqueo","ComentarioDesbloqueo","IdPersonaEjecutivo","CodigoEjecutivoExterno","IdTipoCliente",
                "IdPlantillaLimiteExtra","CodigoAdicionalHolding","IdSucursalPreferencia","IdPersonaBrokerExtranjero","IdClaseCliente",
                "FechaBloqueoTemporal","FechaDesbloqueoTemporal","IdOrigen")
            SELECT
                "IdCliente","IdPersona",:p_IdEmpresa AS "IdEmpresa",COALESCE("IdEstado",'ACT'),"IdSegmentoCliente",COALESCE("IdCategoriaInversionista",'SIN CATEGORIA'),
                '' AS "IdTipoBloqueo","FechaBloqueo","ComentarioBloqueo","FechaIngreso",CURRENT_UTCTIMESTAMP AS "FechaCreacion",'0001-01-01' AS "FechaModificacion",
                "EsEnrolado",:p_IdCarga AS "IdCarga",:p_IdUsuario AS "IdUsuarioCreador","IdPortafolioDefecto",'N' AS "Borrado",'' AS "IdTipoDesbloqueo",
                '0001-01-01' AS "FechaDesbloqueo",'' AS "ComentarioDesbloqueo",COALESCE("IdPersonaEjecutivo",0),'' AS "CodigoEjecutivoExterno",
                COALESCE("IdTipoCliente",'RETAIL'),'' AS "IdPlantillaLimiteExtra",'' AS "CodigoAdicionalHolding",0 AS "IdSucursalPreferencia",0 AS "IdPersonaBrokerExtranjero",
                '' AS "IdClaseCliente",'0001-01-01' AS "FechaBloqueoTemporal",'0001-01-01' AS "FechaDesbloqueoTemporal","per_id" AS "IdOrigen"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cliente"
            WHERE "EsUtilizable" = 'S';
           
            INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" (
                "IdPortafolio","CodigoPortafolio","Proposito","IdCliente","IdEstado","FechaBloqueo","ComentarioBloqueo","IdTipoBloqueo",
                "FechaIngreso","FechaCreacion","FechaModificacion","IdCarga",
                "IdPerfilComercialBaseOrigen","IdPerfilProductos",
                "IdPerfilCobros","IdEstadoAutorizacion","DescripcionPortafolio","IdPerfilLimites","IdPerfilCondicionesOperativas",
                "IdPerfilProductosExtra","IdPerfilCobrosExtra","IdPerfilLimitesExtra","IdPerfilCondicionesOperativasExtra","IdTipoDesbloqueo",
                "FechaDesbloqueo","ComentarioDesbloqueo","IdDireccionEnvioCorrespondencia","IdTelefonoContacto","IdEmpleadoEjecutivo",
                "EsEjecutivoExclusivo","NumeroFicha","IdNombreFantasia","Alias","IdPersonaBrokerExtranjero","IdPerfilCobrosPeru","IdPerfilCobrosPeruExtra",
                "IdOrigen","IdCuentaOrigen")
            SELECT
                "IdPortafolio","CodigoPortafolio","Proposito","IdCliente",coalesce("IdEstado",'ACT'),"FechaBloqueo","ComentarioBloqueo",'' AS "IdTipoBloqueo",
                "FechaIngreso",CURRENT_UTCTIMESTAMP AS "FechaCreacion",'0001-01-01' AS "FechaModificacion",:p_IdCarga AS "IdCarga",
                coalesce("IdPerfilComercialBase",'') AS "IdPerfilComercialBaseOrigen",upper(COALESCE("IdPerfilProductos",'')),upper(COALESCE("IdPerfilCobros",'')),'ING' AS "IdEstadoAutorizacion",
                "DescripcionPortafolio",'' AS "IdPerfilLimites",'' AS "IdPerfilCondicionesOperativas","IdPerfilProductosExtra","IdPerfilCobrosExtra",
                "IdPerfilLimitesExtra","IdPerfilCondicionesOperativasExtra",'' AS "IdTipoDesbloqueo",'0001-01-01' AS "FechaDesbloqueo",'' AS "ComentarioDesbloqueo",
                0 AS "IdDireccionEnvioCorrespondencia",0 AS "IdTelefonoContacto","IdEmpleadoEjecutivo",'N' AS "EsEjecutivoExclusivo",'' AS "NumeroFicha",
                COALESCE("IdNombreFantasia",0),"Alias",0 AS "IdPersonaBrokerExtranjero",'' AS "IdPerfilCobrosPeru",'' AS "IdPerfilCobrosPeruExtra",
                "per_id" AS "IdOrigen","CodigoPortafolio" AS "IdCuentaOrigen"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cuenta"
            WHERE "EsUtilizable" = 'S';
            
            UPDATE s
            SET
                "IdCliente" = c."IdCliente"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cliente" AS c
            INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS s ON (s."IdPersona" = c."IdPersona")
            WHERE c."EsUtilizable" = 'S';
           
            
            --Setea el ejecutivo a nivel del cliente
             UPDATE  c  
             SET "IdPersonaEjecutivo" = COALESCE((
				             SELECT  min(e."IdPersona") 
					             from "BSCL.Portafolio.BD::POR.Portafolio" AS p INNER JOIN   BSCL."BSCL.SocioNegocio.BD::EMP.Empleado" AS e
					             		ON (e."IdEmpleado"  = p."IdEmpleadoEjecutivo")
					             	WHERE p."IdCliente"  = c."IdCliente"
             		),0)
             FROM   BSCL."BSCL.SocioNegocio.BD::CLI.Cliente" c
             WHERE "IdCarga" = :p_IdCarga; 
             
             
             		
             UPDATE p SET
             	"IdTelefonoContacto" = 
             			COALESCE((SELECT min ("IdTelefono") FROM BSCL."BSCL.SocioNegocio.BD::PER.Telefono" t  WHERE "Predeterminado" = 'S' AND t."IdPersona" = c."IdPersona"),0)
             FROM  "BSCL.Portafolio.BD::POR.Portafolio" p  JOIN  
             			BSCL."BSCL.SocioNegocio.BD::CLI.Cliente"  c
             				ON (p."IdCliente" = c."IdCliente")
             WHERE p."IdCarga" = :p_IdCarga; 
             				

			             
			             		 
			/*Cuentas Custodio Portafolio*/		 
			
			
			
			INSERT INTO BSCL."BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio"
			("IdCuentaCustodioPortafolio", "IdPortafolio", "IdBoveda", "Codigo", "FechaIngreso", "FechaCreacion", "FechaModificacion", "IdUsuarioCreador", "IdEstado", "IdEstadoAutorizacion", "IdModeloCustodia", "IdTipoCustodia", "IdTipoCuentaCustodia", "IdPersonaBancoCustodio", "IdPersonaBancoLiquidador", "LiquidaDividendos", "LiquidaCupones", "LiquidaVencimientos", "LiquidaPrepagos")
			SELECT 
			BSCL."BSCL.Portafolio.BD.Secuencias::CuentaCustodioPortafolio".NEXTVAL AS "IdCuentaCustodioPortafolio"
			,POR."IdPortafolio"
			,'DCV' AS "IdBoveda"
			,CAST (CUE."cue_cta_dcv" AS NVARCHAR(40)) AS  "Codigo"
			,CUE."cue_fec_ingreso" AS "FechaIngreso"
			,CURRENT_UTCTIMESTAMP  AS "FechaCreacion"
			,CURRENT_UTCTIMESTAMP  AS "FechaModificacion"
			,0 AS  "IdUsuarioCreador"
			,'ACT' AS "IdEstado"
			,'AUT' AS "IdEstadoAutorizacion"
			, ''   AS "IdModeloCustodia"
			, ''   AS "IdTipoCustodia"
			, ''   AS "IdTipoCuentaCustodia"
			, 0    AS "IdPersonaBancoCustodio"
			, 0    AS "IdPersonaBancoLiquidador"
			, 'N'  AS "LiquidaDividendos"
			, 'N'  AS "LiquidaCupones"
			, 'N'  AS "LiquidaVencimientos"
			, 'N'  AS "LiquidaPrepagos"
			FROM BSCL."BSCL.Portafolio.BD::POR.Portafolio" POR
			INNER JOIN BSCL."BSCL.Homologacion.BD::GES.Cuenta" CUE ON (CUE."IdCarga"=POR."IdCarga" AND CUE."per_id"=POR."IdOrigen" AND POR."IdCuentaOrigen"=CUE."CUENTA" AND CUE."cue_cta_dcv" NOT IN('0',''))
			WHERE POR."IdCarga"=:p_IdCarga
			AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" P WHERE P."IdPortafolio"=POR."IdPortafolio" AND TRIM(CAST (CUE."cue_cta_dcv" AS NVARCHAR(40))) = TRIM(P."Codigo")); 
			

             
             
             --repaso de la categoria del inversionoista
             /*
             UPDATE cli SET "IdCategoriaInversionista" =  hci."ValorDestino"
             FROM 
             BSCL."BSCL.Homologacion.BD::GES.IndicadoresCliente" AS ic 
             	JOIN  	BSCL."BSCL.Homologacion.BD::GES.Indicador" AS i
             		ON (i."ind_id" = ic."ind_id" AND "reg_id" = 'CATIG') AND i."IdCarga" = :p_IdCarga
             	JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS cli   
             		ON (cli."IdOrigen" = ic."per_id"  )
				JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hci 
					ON (hci."TipoValor" = 'CategoriaInversionista' 
                    	AND hci."Entidad" = 'CLIENTE' 
                    	AND hci."ValorOrigen" = ic."din_id");
             */
             --homologacion de codigos de portafolio apv 
             --/** Se Comenta para no hacer homologacion y cambiar codigoportafolio en tabla portafolio *** NSM
        	 --update p set p."IdMatrizAgrupadorAPV" =  h."ValorOrigen2", p."CodigoPortafolio" = h."ValorDestino"
			 --from "BSCL.Portafolio.BD::POR.Portafolio" p
			 --join "BSCL.Homologacion.BD::HOM.Homologacion" h on (h."ValorOrigen" = p."CodigoPortafolio" and h."Entidad" = 'PORTAFOLIO' and h."TipoValor" = 'CodigoPortafolio');
			 --/**       	
            

			UPDATE p
			SET "IdDireccionEnvioCorrespondencia" =  "IdDireccionFisica"
			FROM 
				BSCL."BSCL.Portafolio.BD::POR.Portafolio"  AS p
						JOIN BSCL."BSCL.Homologacion.BD::GES.uso_direcciones" AS u
							ON (p."IdOrigen" = u."per_id" AND p."IdCuentaOrigen" = u."CUENTA"
								AND u."udi_corresp"  = 'S'						
								)
							JOIN BSCL."BSCL.SocioNegocio.BD::PER.DireccionFisica" AS D
								ON (D."IdOrigen"  = u."dir_id"  );
			
			
								
        END IF;
    
        
        v_Estadisticas =
            SELECT
                'CARGA_TOTAL' AS "Estadistica",
                'CLIENTE' AS "Seccion",
                NULL AS "DatoEstadistica",
                COUNT("IdClienteCarga") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cliente"
            UNION ALL
            SELECT
                'CARGA_ESTADO' AS "Estadistica",
                'CLIENTE' AS "Seccion",
                "IdEstado" AS "DatoEstadistica",
                COUNT("IdClienteCarga") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cliente"
            GROUP BY "IdEstado"
            UNION ALL
            SELECT
                'CARGA_SEGMENTO_CLIENTE' AS "Estadistica",
                'CLIENTE' AS "Seccion",
                CAST("IdSegmentoCliente" AS NVARCHAR(255)) AS "DatoEstadistica",
                COUNT("IdClienteCarga") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cliente"
            GROUP BY "IdSegmentoCliente"
            UNION ALL
            SELECT
                'CARGA_CATEGORIA_INVERSIONISTA' AS "Estadistica",
                'CLIENTE' AS "Seccion",
                "IdCategoriaInversionista" AS "DatoEstadistica",
                COUNT("IdClienteCarga") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cliente"
            GROUP BY "IdCategoriaInversionista"
            UNION ALL
            SELECT
                'CARGA_TIPO_CLIENTE' AS "Estadistica",
                'CLIENTE' AS "Seccion",
                "IdTipoCliente" AS "DatoEstadistica",
                COUNT("IdClienteCarga") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cliente"
            GROUP BY "IdTipoCliente"
            --UNION ALL
            --SELECT
            --    'CARGA_PLANTILLA_LIMITE_EXTRA' AS "Estadistica",
            --    'CLIENTE' AS "Seccion",
            --    "IdPlantillaLimiteExtra" AS "DatoEstadistica",
            --    COUNT("IdClienteCarga") AS "Origen",
            --    COUNT(CASE WHEN "EsUtilizable" = 'S' THEN 0 ELSE NULL END) AS "Destino"
            --FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cliente"
            --GROUP BY "IdPlantillaLimiteExtra"
            UNION ALL
            SELECT
                'CARGA_TOTAL' AS "Estadistica",
                'PORTAFOLIO' AS "Seccion",
                NULL AS "DatoEstadistica",
                COUNT("IdCuentaCarga") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cuenta"
            UNION ALL
            SELECT
                'CARGA_ESTADO' AS "Estadistica",
                'PORTAFOLIO' AS "Seccion",
                "IdEstado" AS "DatoEstadistica",
                COUNT("IdCuentaCarga") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cuenta"
            GROUP BY "IdEstado"
            /*UNION ALL
            SELECT
                'CARGA_PROPOSITO_PORTAFOLIO' AS "Estadistica",
                'PORTAFOLIO' AS "Seccion",
                "Proposito" AS "DatoEstadistica",
                COUNT("IdCuentaCarga") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cuenta"
            GROUP BY "Proposito"
            UNION ALL
            SELECT
                'CARGA_PERFIL_COMERCIAL_BASE' AS "Estadistica",
                'PORTAFOLIO' AS "Seccion",
                "IdPerfilComercialBaseOrigen" AS "DatoEstadistica",
                COUNT("IdCuentaCarga") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cuenta"
            GROUP BY "IdPerfilComercialBaseOrigen"*/
            UNION ALL
            SELECT
                'CARGA_PERFIL_PRODUCTOS' AS "Estadistica",
                'PORTAFOLIO' AS "Seccion",
                "IdPerfilProductos" AS "DatoEstadistica",
                COUNT("IdCuentaCarga") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cuenta"
            GROUP BY "IdPerfilProductos"
            UNION ALL
            SELECT
                'CARGA_PERFIL_COBROS' AS "Estadistica",
                'PORTAFOLIO' AS "Seccion",
                "IdPerfilCobros" AS "DatoEstadistica",
                COUNT("IdCuentaCarga") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cuenta"
            GROUP BY "IdPerfilCobros"
            /*UNION ALL
            SELECT
                'CARGA_PERFIL_LIMITES' AS "Estadistica",
                'PORTAFOLIO' AS "Seccion",
                "IdPerfilLimites" AS "DatoEstadistica",
                COUNT("IdCuentaCarga") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cuenta"
            GROUP BY "IdPerfilLimites"
            UNION ALL
            SELECT
                'CARGA_PERFIL_CONDICIONES_OPERATIVAS' AS "Estadistica",
                'PORTAFOLIO' AS "Seccion",
                "IdPerfilCondicionesOperativas" AS "DatoEstadistica",
                COUNT("IdCuentaCarga") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cuenta"
            GROUP BY "IdPerfilCondicionesOperativas"
            UNION ALL
            SELECT
                'CARGA_PERFIL_CONDICIONES_OPERATIVAS_EXTRAS' AS "Estadistica",
                'PORTAFOLIO' AS "Seccion",
                "IdPerfilCondicionesOperativasExtra" AS "DatoEstadistica",
                COUNT("IdCuentaCarga") AS "Origen",
                COUNT(CASE WHEN "EsUtilizable" = 'S' THEN 0 ELSE NULL END) AS "Destino"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Cuenta"
            GROUP BY "IdPerfilCondicionesOperativasExtra"*/;

            
        v_Diccionario =
            SELECT
                "IdInterno","CodigoMensaje" AS "Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2"
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Problemas";
        
        IF (NOT IS_EMPTY(:v_Diccionario)) THEN
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (
                P_TablaCodigosMensajes  => :v_Diccionario,
                p_out_TablaMensajes     => v_TablaMensajes);
            
            UPDATE p
            SET
                "Descripcion" = COALESCE(m."Descripcion",'')
            FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Problemas" p
            INNER JOIN :v_TablaMensajes AS m ON (m."IdInterno" = p."IdInterno");
        END IF;--IF (NOT IS_EMPTY(:v_Diccionario)) THEN
        
        SELECT count(1) into p_out_ErroresContador FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Problemas" WHERE "TipoProblema" IN ('B','E');
        SELECT count(1) into p_out_AdvertenciasContador FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Problemas" WHERE "TipoProblema" = 'W';
        v_Log = SELECT * FROM "BSCL.Homologacion.BD::GBL.CargarDatosClientesPortafolio_Proceso_Problemas";
                   

        --Siempre Grabar
        BEGIN AUTONOMOUS TRANSACTION
            INSERT INTO "BSCL.Homologacion.BD::HOM.CargaLog" (
                "IdCargaLog","IdCarga","IdCargaEjecucion","FechaCarga",
                -----------------------------------------------------------------
                "SeccionProblema","per_id","CUENTA","IdOrigenCarga",
                -----------------------------------------------------------------   
                --CAMPOS DE ERRORES
                "OrigenProblema","TipoProblema","CodigoMensaje","RequiereAutorizacion","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","Descripcion")
            SELECT
                "BSCL.Homologacion.BD.Secuencias::IdCargaLog".NEXTVAL,:p_IdCarga,:p_out_IdEjecucionCarga,CURRENT_UTCTIMESTAMP,
                -----------------------------------------------------------------
                "SeccionProblema","per_id","CUENTA","IdOrigenCarga",
                -----------------------------------------------------------------   
                --CAMPOS DE ERRORES
                ::CURRENT_OBJECT_NAME,"TipoProblema","CodigoMensaje","RequiereAutorizacion","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","Descripcion"
            FROM :v_Log;
            
            INSERT INTO "BSCL.Homologacion.BD::HOM.CargaEstadisticas" (
                "IdCargaEstadisticas","IdCarga","IdCargaEjecucion","OrigenEstadistica","Estadistica","Seccion","DatoEstadistica","Origen","Destino")
            SELECT
                "BSCL.Homologacion.BD.Secuencias::IdCargaEstadisticas".NEXTVAL,:p_IdCarga,:p_out_IdEjecucionCarga,::CURRENT_OBJECT_NAME,"Estadistica",
                "Seccion","DatoEstadistica","Origen","Destino"
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
