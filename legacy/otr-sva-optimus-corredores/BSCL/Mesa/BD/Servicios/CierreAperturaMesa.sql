SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::CierreAperturaMesa";

-- 
--  Proposito: Cierre y Apertura Mesa RF
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 06/09/2016  
-- 

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::CierreAperturaMesa"
(
    -- Parametros de Validacion                                                                                            
    IN  p_IdEmpresa                 BIGINT,                                                                            
    IN  p_IdUsuario                 BIGINT,                                                                            
    IN  p_IdModulo                  NVARCHAR(3),                                                                       
    IN  p_simularOperacion          NVARCHAR(1),    -- S/N se usa para simular operacion (algunos casos)                  
    IN  p_UsaReglasNegocio          NVARCHAR(1),    -- S/N se usa en caso de servicios anidados.
    IN  p_CierreMesa                NVARCHAR(1) DEFAULT 'N', -- S/N se usa para cerrar Mesa Spot con operaciones pendiente.
 
    -- Parametros entrada
    IN p_DatosMesas                 TABLE(  "IdMesa"        BIGINT,
                                            "NombreMesa"    NVARCHAR(30),
                                            "IdEstadoMesa"  NVARCHAR(3)),
    
    -- Salidas
    OUT p_out_ResultadoProceso          TABLE("IdMesa" BIGINT, "Resultado" NVARCHAR(250)),
    OUT p_out_AdvertenciasContador      INT,
    OUT p_out_AdvertenciasDetalle       TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),    
    OUT p_out_ErroresContador           INT,   
    OUT p_out_ErroresDetalle            TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS

BEGIN

    --------------------
    --DECLARE Manejo de Errores
    --------------------    
    DECLARE SALIDA_CONTROLADA_NO_DATA   CONDITION;
    DECLARE SALIDA_CONTROLADA   CONDITION; 
    DECLARE v_errorcuenta       INT DEFAULT 0;  
    DECLARE v_errorhashmsg      TABLE ("Codigo" NVARCHAR(100),"Valor" NVARCHAR(5000));
    DECLARE v_erroroutmsg       NVARCHAR(520);              
    DECLARE v_ErrorNoData       NVARCHAR(100);
    DECLARE v_ErrorNoDataGls    NVARCHAR(520);              
    
    DECLARE v_NroMesaSpot       INT;
    DECLARE v_NroMesaForward    INT;
    
    
    DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA   
    BEGIN
        --salida/error/advertencia controlado por este servicio
        SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
        SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;                                
    END;    
    
    DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
    BEGIN
        v_hash= SELECT '' AS "Codigo",'' AS "Valor" FROM DUMMY; 
        --CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
        p_out_ErroresDetalle =  
            SELECT :v_ErrorNoData AS "Codigo", v_ErrorNoDataGls AS "Descripcion" FROM dummy UNION ALL
            SELECT * FROM :p_out_ErroresDetalle;
        SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
    END;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        DECLARE v_errorExt NVARCHAR(40);
        IF ::SQL_ERROR_CODE > 10000 THEN
            --error pseudo-controlado pero desde un Sub-SP
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);          
            p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
                                   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;                
            SELECT COUNT(1) INTO p_out_ErroresContador 
            FROM :p_out_ErroresDetalle;                                     
        ELSE
            --Error interno no manejado  (El log será captado por la capa de XS control)        
            RESIGNAL; 
        END IF;     
    END;    

    p_out_AdvertenciasContador  := 0;
    p_out_ErroresContador       := 0;
    p_out_AdvertenciasDetalle   = SELECT '' AS "Codigo",'' AS "RequiereAutorizacion",'' AS "Descripcion" FROM dummy where 1=2;
    p_out_ErroresDetalle        = SELECT '' AS "Codigo",'' AS "Descripcion" FROM dummy where 1=2;
    
    BEGIN --LOGICA
        --------------------
        -- DECLARE Variables
        --------------------
        DECLARE v_resultadoRegla    NVARCHAR(40);
        DECLARE v_Count             BIGINT:=0;
        DECLARE v_mensajeError      NVARCHAR(40);
        
        DECLARE v_FechaHoraSistema      SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
        DECLARE v_FechaSistemaUTC       NVARCHAR(40);
        DECLARE v_FechaSistemaLOCAL     NVARCHAR(40);
        DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
        DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
        DECLARE v_FechaHoraUTC          NVARCHAR(40);
        DECLARE v_FechaHoraLOCAL        NVARCHAR(40);
        DECLARE v_CodigoUsuario         NVARCHAR(40);
        DECLARE v_ProcesoLiquidacion    NVARCHAR(40);
        DECLARE v_CountParametro        BIGINT:=0;
        DECLARE v_CountPendientesIng    BIGINT:=0;
        DECLARE v_CountPendientesAut    BIGINT:=0;
        DECLARE v_CountPendientesConf   BIGINT:=0;
        DECLARE v_IdModalidadEntregaFisica  BIGINT:=0;
        DECLARE v_IdModalidadCompensada     BIGINT:=0;
        DECLARE p_DetalleLog            TABLE ( "SubRegistro" NVARCHAR(5000), 
                                                "CampoRegistro" NVARCHAR(40), 
                                                "ValorAntiguo" NVARCHAR(5000), 
                                                "ValorNuevo" NVARCHAR(5000));
        
        DECLARE varMonedaEquivalente NVARCHAR(3);
        declare v_texto_resultado    NVARCHAR(40);
		
        DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
        BEGIN
            v_ErrorNoData := 'DATA_NO_EXISTE';
            SIGNAL SALIDA_CONTROLADA_NO_DATA;
        END;

        v_ProcesoLiquidacion = 'OPERACIONES_FWD_POR_LIQUIDAR';      
        p_out_ResultadoProceso  = SELECT 0  AS "IdMesa", '' AS "Resultado" FROM dummy where 1=2;
        
        CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaHoraSistema);
        
        -- Se obtiene la misma FechaHoraSistema que se muestra en la UI
        CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
            p_IdEmpresa                 => p_IdEmpresa,
            p_out_FechaSistemaUTC       => v_FechaSistemaUTC,
            p_out_FechaSistemaLOCAL     => v_FechaSistemaLOCAL,
            p_out_FechaHoraSistemaUTC   => v_FechaHoraSistemaUTC,
            p_out_FechaHoraSistemaLOCAL => v_FechaHoraSistemaLOCAL,
            p_out_FechaHoraUTC          => v_FechaHoraUTC,
            p_out_FechaHoraLOCAL        => v_FechaHoraLOCAL
        );
        
        
        v_ErrorNoDataGls = 'CODIGO DE USUARIO NO ENCONTRADO';
        SELECT  IFNULL(USR."CodigoUsuario",' - ') 
        INTO    v_CodigoUsuario
        FROM    "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" USR
        WHERE   USR."IdUsuario" = p_IdUsuario AND  USR."IdEmpresa" = p_IdEmpresa;
        
        
        SELECT "Valor" into varMonedaEquivalente  
        FROM "BSCL.Mesa.BD::MES.ConfiguracionMesa"
        WHERE "IdTipoProducto" = 'MEME'
          AND "CodigoParametro" = 'IDMONEDAEQUIVALENTE'
          AND "IdEmpresa" = :p_IdEmpresa;
      

        --------------------------------------------
        -- VALIDACIONES
        -------------------------------------------- 

        IF (p_UsaReglasNegocio = 'S') THEN
            BEGIN
                -- Validación básica de Empresa, Usuario, Módulo
                CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle,:v_resultadoRegla);
                IF :v_resultadoRegla = 'RECH' THEN
                    v_ErrorNoDataGls = 'SERVICIO DE CUMPLIMIENTO SIN DATOS';
                    SIGNAL SALIDA_CONTROLADA;
                END IF; 
            END;
        END IF;
        

        -- Solo fue una simulacion
        IF p_simularOperacion = 'S' THEN
            SIGNAL SALIDA_CONTROLADA;
        END IF;

        -- validacion existen Mesas
        v_ErrorNoDataGls = 'MESA NO ENCONTRADA';
        SELECT COUNT(1) INTO v_Count
        FROM :p_DatosMesas DM
        INNER JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON (DM."IdMesa" = MES."IdMesa" AND MES."IdEmpresa" = p_IdEmpresa);
        
        IF v_Count = 0 THEN
            p_out_ErroresDetalle = SELECT 'UI_MESAS_NO_EXISTEN' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
            SIGNAL SALIDA_CONTROLADA;
        END IF;
        
        SELECT COUNT(1) INTO v_CountParametro 
        FROM "BSCL.Mesa.BD::MES.Parametro" AS PRM_EF
        WHERE PRM_EF."NombreTipoParametro" = 'ModalidadLiquidacion' 
        AND PRM_EF."ValorParametro" = 'ENTREGA_FISICA' AND PRM_EF."IdEmpresa" = p_IdEmpresa;
        
        IF  v_CountParametro > 0 THEN
            SELECT TOP 1 PRM_EF."IdParametro" INTO v_IdModalidadEntregaFisica 
            FROM "BSCL.Mesa.BD::MES.Parametro" AS PRM_EF
            WHERE PRM_EF."NombreTipoParametro" = 'ModalidadLiquidacion' 
            AND PRM_EF."ValorParametro" = 'ENTREGA_FISICA' AND PRM_EF."IdEmpresa" = p_IdEmpresa;
        END IF;
        
        SELECT COUNT(1) INTO v_CountParametro 
        FROM "BSCL.Mesa.BD::MES.Parametro" AS PRM_COMP
        WHERE PRM_COMP."NombreTipoParametro" = 'ModalidadLiquidacion' 
        AND PRM_COMP."ValorParametro" = 'COMPENSADA' AND PRM_COMP."IdEmpresa" = p_IdEmpresa;
        
        IF  v_CountParametro > 0 THEN
            SELECT TOP 1 PRM_COMP."IdParametro" INTO v_IdModalidadCompensada 
            FROM "BSCL.Mesa.BD::MES.Parametro" AS PRM_COMP
            WHERE PRM_COMP."NombreTipoParametro" = 'ModalidadLiquidacion' 
            AND PRM_COMP."ValorParametro" = 'COMPENSADA' AND PRM_COMP."IdEmpresa" = p_IdEmpresa;
        END IF;
                
        v_Check = 
            SELECT  MES."IdMesa",
                    CASE MES."IdTipoProducto" 
                         WHEN 'MEME' THEN COUNT(SPT."Estado") 
                         WHEN 'MEFW' THEN COUNT(FWD_PEN."IdEstado") 
                         WHEN 'MERF' THEN COUNT(MRF."IdEstado")                          
                    END AS "Pendientes",
                    CASE MES."IdTipoProducto" 
                         WHEN 'MEME' THEN COUNT(SPT_CNF."Estado") 
                         ELSE 0 
                    END AS "PendientesSpot_PorConfirmar", 
                    --* Pendientes de liquidacion Entrega Fisica ---------------------------------------------- 
                    COUNT(CASE WHEN FWD_EF_COMP."IdModalidadLiquidacion" = v_IdModalidadEntregaFisica AND FWD_EF_COMP."IdEstado" IN ('VIG','ANT') THEN FWD_EF_COMP."IdEstado" ELSE NULL END) AS "PorLiquidar_EF",
                    --* Pendientes de liquidacion Compensado --- ----------------------------------------------  
                    COUNT(CASE WHEN FWD_EF_COMP."IdModalidadLiquidacion" = v_IdModalidadCompensada AND FWD_EF_COMP."IdEstado" IN ('VIG','ANT','VCD') THEN FWD_EF_COMP."IdEstado" ELSE NULL END) AS "PorLiquidar_COMP",
                    -- Operaciones Pendientes no facturables
                    CASE MES."IdTipoProducto" 
                        WHEN 'MEME' THEN COUNT(SPT_NF."Estado") 
                        ELSE 0 
                    END AS "PendientesSpot_NoFacturables"                            
            FROM    "BSCL.Mesa.BD::MES.Mesa" MES
                    --* ---------------------------------------------------------------------------------------
                    LEFT JOIN :p_DatosMesas PAR
                    ON (PAR."IdMesa" = MES."IdMesa" AND MES."IdEmpresa" = p_IdEmpresa)
                    --* Pendientes Spot -----------------------------------------------------------------------
                    LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
                    ON (MES."IdMesa" = SPT."IdMesa" AND SPT."IdEmpresa" = p_IdEmpresa 
                        AND SPT."Estado" = 'PEN' 
                        AND SPT."OperacionFicticia" = 'N'
                        AND TO_VARCHAR(SPT."FechaOperacion",'YYYY-MM-DD') = TO_VARCHAR(v_FechaHoraSistema,'YYYY-MM-DD')) 
                    --* Pendientes Spot No Facturables
                    -----------------------------------------------------------------------
                    LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" SPT_NF
                    ON (MES."IdMesa" = SPT_NF."IdMesa" AND SPT_NF."IdEmpresa" = p_IdEmpresa 
                        AND (SPT_NF."Estado" = 'VIG' AND SPT_NF."EstadoConfirmacion" = 'PEN')
                        AND SPT_NF."OperacionFicticia" = 'S'
                        AND TO_VARCHAR(SPT_NF."FechaOperacion",'YYYY-MM-DD') = TO_VARCHAR(v_FechaHoraSistema,'YYYY-MM-DD')) 
                    --* Pendientes Spot Tesoreria--------------------------------------------------------------
                    LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" SPT_CNF
                    ON (MES."IdMesa" = SPT_CNF."IdMesa" AND SPT_CNF."IdEmpresa" = p_IdEmpresa 
                        AND SPT_CNF."Estado" = 'VIG' AND SPT_CNF."EstadoConfirmacion" = 'PEN' 
                        AND TO_VARCHAR(SPT_CNF."FechaOperacion",'YYYY-MM-DD') = TO_VARCHAR(v_FechaHoraSistema,'YYYY-MM-DD') ) 
                    --* Pendientes Forward--------------------------------------------------------------------   
                    LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" FWD_PEN
                    ON (MES."IdMesa" = FWD_PEN."IdMesaOrigen" AND FWD_PEN."IdTipoProducto" = 'MEFW' 
                        AND FWD_PEN."IdEstado" = 'PEN' ) 
                    LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" FWD_EF_COMP
                    ON ( MES."IdMesa" = FWD_EF_COMP."IdMesaOrigen" AND FWD_EF_COMP."IdTipoProducto" = 'MEFW' 
                        AND  FWD_EF_COMP."IdEstado" IN ('VIG','ANT','VCD') --NOT IN ('VCD','LIQ') 
                        AND TO_VARCHAR(FWD_EF_COMP."FechaVencimiento",'YYYY-MM-DD') = TO_VARCHAR(v_FechaHoraSistema,'YYYY-MM-DD') )                         
                    --* Pendientes MRF-------------------------------------------------------------------------
                    LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" MRF
                    ON (MES."IdMesa" = MRF."IdMesaOrigen" AND MRF."IdEstado" = 'PEN' 
                        AND MRF."IdTipoProducto" = 'MERF' 
                        AND TO_VARCHAR(MRF."FechaOperacion",'YYYY-MM-DD') = TO_VARCHAR(v_FechaHoraSistema,'YYYY-MM-DD') ) 
                    --* ---------------------------------------------------------------------------------------         
            GROUP BY MES."IdMesa",
                    MES."IdTipoProducto";
        
        select 'v_Check', * from :v_Check;

        v_Mesas =
            SELECT  MES."IdMesa",
                    MES."Nombre" AS "NombreMesa",
                    MES."IdTipoProducto",
                    CASE MES."IdTipoProducto" 
                        WHEN 'MEME' THEN 'MSP'
                        WHEN 'MEFW' THEN 'MFW'
                        WHEN 'MERF' THEN 'MRF'
                    END AS "CodigoModulo",
                    CASE MES."IdTipoProducto" 
                        WHEN 'MEME' THEN 'OPERACIONES_MSP_PENDIENTES'
                        WHEN 'MEFW' THEN 'OPERACIONES_MFW_PENDIENTES'
                        WHEN 'MERF' THEN 'OPERACIONES_MRF_PENDIENTES'                       
                        ELSE ''
                    END AS "ProcesoPendientes",   
                    Chk."PendientesSpot_NoFacturables",          
                    CASE MES."IdTipoProducto" 
                        WHEN 'MEME' THEN 'CIERRE_MSP'
                        WHEN 'MEFW' THEN 'CIERRE_MFW'
                        WHEN 'MERF' THEN 'CIERRE_MRF'
                    END AS "ProcesoCierre",                 
                    COALESCE(PAR."IdEstadoMesa", MES."IdEstadoMesa") AS "IdEstadoNuevo",
                    MES."IdEstadoMesa" AS "IdEstadoActual",
                    Chk."Pendientes",
                    Chk."PendientesSpot_PorConfirmar",    
                    Chk."PorLiquidar_EF",               
                    Chk."PorLiquidar_COMP"                                  
            FROM    :v_Check Chk
                    --* ---------------------------------------------------------------------------------------
                    LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES 
                    ON (Chk."IdMesa" = MES."IdMesa" AND MES."IdEmpresa" = p_IdEmpresa)
                    --* ---------------------------------------------------------------------------------------
                    LEFT JOIN :p_DatosMesas PAR 
                    ON (PAR."IdMesa" = MES."IdMesa" AND MES."IdEmpresa" = p_IdEmpresa)
            WHERE   MES."IdEstadoMesa" <> COALESCE(PAR."IdEstadoMesa", MES."IdEstadoMesa");
        
        select 'v_Mesas', * from :v_Mesas;
                    
        --* -----------------------------------------------
        --* ESTA ABRIENDO PERO EL MODULO ESTA CERRADO
        --* -----------------------------------------------
        SELECT  COUNT(1) INTO v_Count
        FROM    :v_Mesas PAR
                INNER JOIN "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulosDetalle" MDL 
                ON MDL."CodigoModulo" = PAR."CodigoModulo" AND MDL."IdEstado" = 'CER' AND MDL."IdEmpresa" = :p_IdEmpresa
        WHERE   PAR."IdEstadoNuevo" = 'A';

        IF v_Count > 0 THEN     
            p_out_ErroresDetalle = SELECT 'UI_MODULO_CERRADO' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM DUMMY;
            p_out_ResultadoProceso =
                SELECT  PAR."IdMesa", PAR."NombreMesa" ||' MODULO CERRADO' AS "Resultado"
                FROM    :v_Mesas PAR
                        INNER JOIN "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulosDetalle" MDL 
                        ON MDL."CodigoModulo" = PAR."CodigoModulo" AND MDL."IdEstado" = 'CER' AND MDL."IdEmpresa" = :p_IdEmpresa
                WHERE   PAR."IdEstadoNuevo" = 'A';
            SIGNAL SALIDA_CONTROLADA;
        END IF;
        
        
       --*	--------------------------------------
       	--* RF: OPERACIONES PENDIENTES AL CIERRE ??
        --*	--------------------------------------
        
       	SELECT  SUM(1) INTO v_Count 
        FROM    :v_Mesas 
        WHERE   "IdEstadoNuevo" = 'C' AND "Pendientes" > 0 AND "IdEstadoNuevo" <> "IdEstadoActual" AND "IdTipoProducto" = 'MERF';
        
       	IF v_Count > 0 THEN 
        
       		SELECT COUNT(1) INTO v_Count FROM :v_Mesas DM;
            IF v_Count > 1 THEN
                v_mensajeError := 'UI_MESAS_CON_OPERACIONES_PENDIENTES';
            ELSE
                v_mensajeError := 'UI_MESA_CON_OPERACIONES_PENDIENTES';
            END IF;

           	p_out_ResultadoProceso =
                SELECT  RF."IdMesa", RF."NombreMesa"||' tiene operaciones pendientes :' || '   ' || RF."Pendientes" AS "Resultado"
                FROM    :v_Mesas RF
                WHERE   "Pendientes" > 0 AND "IdEstadoNuevo" = 'C' AND "IdEstadoNuevo" <> "IdEstadoActual" AND "IdTipoProducto" = 'MERF';
            
           	p_out_ErroresDetalle = SELECT :v_mensajeError AS "Codigo",(SELECT "Resultado" FROM :p_out_ResultadoProceso) AS "Descripcion" FROM dummy; 
           
           
       		SIGNAL SALIDA_CONTROLADA;
       	
       END IF;
        
        
        
        
        --* -----------------------------------------------
        --* SPOT: OPERACIONES PENDIENTES AL CIERRE ??
        --* -----------------------------------------------
        SELECT  SUM(1) INTO v_Count 
        FROM    :v_Mesas 
        WHERE   "IdEstadoNuevo" = 'C' AND ("Pendientes" > 0 OR "PendientesSpot_PorConfirmar" > 0) AND "IdEstadoNuevo" <> "IdEstadoActual" AND "IdTipoProducto" = 'MEME'; 
        
        --* SE AGREGA PARAMETRO PARA CERRAR MESA SIN VALIDAR PENDIENTE       
        IF  p_CierreMesa = 'N' THEN
        
	        IF v_Count > 0 THEN
	            SELECT COUNT(1) INTO v_Count FROM :v_Mesas DM;
	            IF v_Count > 1 THEN
	                v_mensajeError := 'UI_MESAS_CON_OPERACIONES_PENDIENTES';
	            ELSE
	                v_mensajeError := 'UI_MESA_CON_OPERACIONES_PENDIENTES';
	            END IF;
	        
	            -- Cantidad Pendientes
	            SELECT COUNT("IdOperacion") INTO v_CountPendientesIng FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" SPT_PEN 
	            WHERE SPT_PEN."Estado" = 'PEN' AND TO_VARCHAR(SPT_PEN."FechaOperacion",'YYYY-MM-DD') = TO_VARCHAR(v_FechaHoraSistema,'YYYY-MM-DD');
	            -- Cantidad Pendientes por Autorizar
	            SELECT COUNT("IdOperacion") INTO v_CountPendientesAut FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" SPT_PAU 
	            WHERE SPT_PAU."Estado" = 'PAU' AND TO_VARCHAR(SPT_PAU."FechaOperacion",'YYYY-MM-DD') = TO_VARCHAR(v_FechaHoraSistema,'YYYY-MM-DD');
	            -- Cantidad Pendientes por Confirmar
	            SELECT COUNT("IdOperacion") INTO v_CountPendientesConf FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" SPT_CNF
	            WHERE SPT_CNF."Estado" = 'VIG' AND SPT_CNF."EstadoConfirmacion" = 'PEN' 
	            AND TO_VARCHAR(SPT_CNF."FechaOperacion",'YYYY-MM-DD') = TO_VARCHAR(v_FechaHoraSistema,'YYYY-MM-DD');
	                               
	            p_out_ResultadoProceso =
	                SELECT  PAR."IdMesa", PAR."NombreMesa"||' tiene operaciones pendientes: ' || 'Ingreso:' || :v_CountPendientesIng || ', Autorización:' || :v_CountPendientesAut || ', Confirmación:' || :v_CountPendientesConf AS "Resultado"
	                FROM    :v_Mesas PAR
	                WHERE   ("Pendientes" > 0 OR "PendientesSpot_PorConfirmar" > 0) AND "IdEstadoNuevo" = 'C' AND "IdEstadoNuevo" <> "IdEstadoActual" AND "IdTipoProducto" = 'MEME';
	
	            p_out_ErroresDetalle = SELECT :v_mensajeError AS "Codigo",(SELECT "Resultado" FROM :p_out_ResultadoProceso) AS "Descripcion" FROM dummy;
	            
	            SIGNAL SALIDA_CONTROLADA;
	    
	        END IF;
	        
	    END IF;
        
        --* -----------------------------------------------
        --* FWD : OPERACIONES PENDIENTES AL CIERRE ??
        --* -----------------------------------------------
        SELECT  SUM(1) INTO v_Count 
        FROM    :v_Mesas 
        WHERE   "IdEstadoNuevo" = 'C' AND "Pendientes" > 0 AND "IdEstadoNuevo" <> "IdEstadoActual" AND "IdTipoProducto" = 'MEFW'; 
        
        IF v_Count > 0 THEN
            SELECT COUNT(1) INTO v_Count FROM :v_Mesas DM;
            IF v_Count > 1 THEN
                v_mensajeError := 'UI_MESAS_CON_OPERACIONES_PENDIENTES';
            ELSE
                v_mensajeError := 'UI_MESA_CON_OPERACIONES_PENDIENTES';
            END IF;
            
            p_out_ResultadoProceso =
                SELECT  PAR."IdMesa", PAR."NombreMesa"||' tiene operaciones pendientes' AS "Resultado"
                FROM    :v_Mesas PAR
                WHERE   "Pendientes" > 0 AND "IdEstadoNuevo" = 'C' AND "IdEstadoNuevo" <> "IdEstadoActual" AND "IdTipoProducto" = 'MEFW';
            
            p_out_ErroresDetalle = SELECT :v_mensajeError AS "Codigo",(SELECT "Resultado" FROM :p_out_ResultadoProceso) AS "Descripcion" FROM dummy;    
         
            SIGNAL SALIDA_CONTROLADA;
    
        END IF;     

        --* -----------------------------------------------
        --* SPOT: OPERACIONES NO FACTURABLES PENDIENTES AL CIERRE ??
        --* -----------------------------------------------
        SELECT  SUM(1) INTO v_Count 
        FROM    :v_Mesas 
        WHERE   "IdEstadoNuevo" = 'C' AND "PendientesSpot_NoFacturables" > 0 AND "IdEstadoNuevo" <> "IdEstadoActual" AND "IdTipoProducto" = 'MEME'; 

        IF v_Count > 0 THEN
            SELECT COUNT(1) INTO v_Count FROM :v_Mesas DM;
            IF v_Count > 1 THEN
                v_mensajeError := 'UI_MESA_SPOT_PENDIENTES_NO_FACTURABLE';
            ELSE
                v_mensajeError := 'UI_MESA_SPOT_PENDIENTES_NO_FACTURABLE';
            END IF;

            p_out_ResultadoProceso =
                SELECT  PAR."IdMesa", PAR."NombreMesa"||' tiene operaciones no facturables pendientes: ' || PAR."PendientesSpot_NoFacturables" AS "Resultado"
                FROM    :v_Mesas PAR
                WHERE   "PendientesSpot_NoFacturables" > 0 AND "IdEstadoNuevo" = 'C' AND "IdEstadoNuevo" <> "IdEstadoActual" AND "IdTipoProducto" = 'MEME';

            p_out_ErroresDetalle = SELECT :v_mensajeError AS "Codigo",(SELECT "Resultado" FROM :p_out_ResultadoProceso) AS "Descripcion" FROM dummy;
            
            SIGNAL SALIDA_CONTROLADA;

        END IF;
        
        
        --* -----------------------------------------------
        --* OPERACIONES FWD POR LIQUIDAR ENT. FISICA
        --* -----------------------------------------------
        SELECT  SUM(1) INTO v_Count 
        FROM    :v_Mesas 
        WHERE   "IdEstadoNuevo" = 'C' AND "PorLiquidar_EF" > 0 AND "IdEstadoNuevo" <> "IdEstadoActual"; 
        
        IF v_Count > 0 THEN

            p_out_ResultadoProceso =
                SELECT  PAR."IdMesa", PAR."NombreMesa"||' TIENE OPERACIONES POR LIQUIDAR (ENTREGA FISICA)' AS "Resultado"
                FROM    :v_Mesas PAR
                WHERE   "PorLiquidar_EF" > 0 AND "IdEstadoNuevo" = 'C' AND "IdEstadoNuevo" <> "IdEstadoActual";
        
            p_out_ErroresDetalle = SELECT 'UI_OPERACIONES_POR_LIQUIDAR' AS "Codigo",(SELECT "Resultado" FROM :p_out_ResultadoProceso) AS "Descripcion" FROM dummy;         
            
            SIGNAL SALIDA_CONTROLADA;                       
        END IF;


        --* -----------------------------------------------
        --* OPERACIONES FWD POR LIQUIDAR COMPEMSADO
        --* -----------------------------------------------
        SELECT  SUM(1) INTO v_Count 
        FROM    :v_Mesas 
        WHERE   "IdEstadoNuevo" = 'C' AND "PorLiquidar_COMP" > 0 AND "IdEstadoNuevo" <> "IdEstadoActual";   
        
        IF v_Count > 0 THEN
            
            p_out_ResultadoProceso =
                SELECT  PAR."IdMesa", PAR."NombreMesa"||' TIENE OPERACIONES POR LIQUIDAR (COMPENSACION)' AS "Resultado"
                FROM    :v_Mesas PAR
                WHERE   "PorLiquidar_COMP" > 0 AND "IdEstadoNuevo" = 'C' AND "IdEstadoNuevo" <> "IdEstadoActual";
             
            p_out_ErroresDetalle = SELECT 'UI_OPERACIONES_POR_LIQUIDAR' AS "Codigo",(SELECT "Resultado" FROM :p_out_ResultadoProceso) AS "Descripcion" FROM dummy;          
            
            SIGNAL SALIDA_CONTROLADA;                       
        END IF;
        
        --* -----------------------------------------------
        --* BORRA TODOS LOS LOGS DE LAS MESAS QUE SE DEBEN REGISTRAR, ASI QUEDAN PENDIENTES MIENTRAS NO ESTE OK
        --* -----------------------------------------------
        v_Procesos = 
            SELECT 'MEME' AS "Producto", 'MSP' AS "Modulo", 'OPERACIONES_MSP_PENDIENTES' AS "Proceso" FROM DUMMY UNION  
            SELECT 'MEME' AS "Producto", 'MSP' AS "Modulo", 'OPERACIONES_PENDIENTES_NO_FACTURABLES' AS "Proceso" FROM DUMMY UNION
            SELECT 'MEFW' AS "Producto", 'MFW' AS "Modulo", 'OPERACIONES_MFW_PENDIENTES' AS "Proceso" FROM DUMMY UNION
            SELECT 'MERF' AS "Producto", 'MRF' AS "Modulo", 'OPERACIONES_MRF_PENDIENTES' AS "Proceso" FROM DUMMY UNION  
            SELECT 'MEME' AS "Producto", 'MSP' AS "Modulo", 'CIERRE_MSP' AS "Proceso" FROM DUMMY UNION  
            SELECT 'MEFW' AS "Producto", 'MFW' AS "Modulo", 'CIERRE_MFW' AS "Proceso" FROM DUMMY UNION  
            SELECT 'MERF' AS "Producto", 'MRF' AS "Modulo", 'CIERRE_MRF' AS "Proceso" FROM DUMMY UNION      
            SELECT 'MEME' AS "Producto", 'MSP' AS "Modulo", 'OPERACIONES_MSP_TESORERIA' AS "Proceso" FROM DUMMY UNION   
            SELECT 'MEFW' AS "Producto", 'MFW' AS "Modulo", 'VALORIZACION_FWD' AS "Proceso" FROM DUMMY UNION                            
            SELECT 'MEFW' AS "Producto", 'MFW' AS "Modulo", 'OPERACIONES_FWD_COMPENSADO' AS "Proceso" FROM DUMMY UNION              
            SELECT 'MEFW' AS "Producto", 'MFW' AS "Modulo", 'OPERACIONES_FWD_ENTREGA_FISICA' AS "Proceso" FROM DUMMY;   

        DELETE  FROM "BSCL.Plataforma.BD::PAU.ControlProceso"
        WHERE   ( "IdEmpresa", "CodigoModulo", "CodigoProceso", TO_VARCHAR("FechaSistema",'YYYY-MM-DD') ) IN
                (   SELECT  p_IdEmpresa AS "IdEmpresa",                         
                            PROC."Modulo",
                            PROC."Proceso",                     
                            TO_VARCHAR(v_FechaHoraSistema,'YYYY-MM-DD') AS "FechaSistema"
                    FROM    :v_Procesos PROC    
                            INNER JOIN :v_Mesas PARAM
                            ON ( PARAM."CodigoModulo" = PROC."Modulo" AND "IdEstadoNuevo" <> "IdEstadoActual") 
                );      


                                    
        --* -----------------------------------------------
        --* CAMBIO DE ESTADO DE LAS MESAS       
        --* -----------------------------------------------
        UPDATE  MES
        SET     MES."IdEstadoMesa" = PAR."IdEstadoMesa",
                MES."IdUsuarioModificacion" = p_IdUsuario,
                MES."FechaModificacion" = v_FechaHoraSistemaLOCAL
        FROM    "BSCL.Mesa.BD::MES.Mesa" MES
        INNER JOIN :p_DatosMesas PAR
            ON (MES."IdMesa" = PAR."IdMesa" AND
                MES."IdEstadoMesa" <> PAR."IdEstadoMesa");
  
        
        p_out_ResultadoProceso =
            SELECT  MES."IdMesa", 'ESTADO '||MES."NombreMesa"||' '||MES."EstadoMesa" AS "Resultado"
            FROM    "BSCL.Mesa.BD.Vistas::ConsultarCierreMesa" MES
                    INNER JOIN :p_DatosMesas PAR
                    ON (MES."IdMesa" = PAR."IdMesa" AND MES."IdEstadoMesa" = PAR."IdEstadoMesa");
        
        
        --IMPCREDOPT-2549 para las operacioes Spot ingresadas con fecha p_Fecha, el campo ResultadoMonedaLocalTCCierreMesa se debe actualizar una vez cerrada la mesa
        v_RecalcularResultadoMonedaLocalTCCierreMesa =
            SELECT
                 SPT."IdOperacionSpot"
                ,CASE
                    -- Compra && Existe PosicionCierre
                    WHEN SPT."IdTipoOperacion" = 'COMSPT' AND COALESCE(PosMonPri."CierreIngresado",0) != 0 THEN
                        (COALESCE(PosMonPri."CierreIngresado",0) - SPT."TipoCambioCierre") * SPT."MontoMonedaPrincipal"
                    
                    -- Venta && Existe PosicionCierre
                    WHEN SPT."IdTipoOperacion" = 'VENSPT' AND COALESCE(PosMonPri."CierreIngresado",0) != 0 THEN
                        (SPT."TipoCambioCierre" - COALESCE(PosMonPri."CierreIngresado",0)) * SPT."MontoMonedaPrincipal"
                    
                    -- Compra && NO existe PosicionCierre para la monedaExtranjera
                    WHEN SPT."IdTipoOperacion" = 'COMSPT' AND COALESCE(PosMonPri."CierreIngresado",0) = 0 AND COALESCE(PosMonEqui."CierreIngresado",0) != 0 THEN
                        CASE WHEN COALESCE(PAR."ParidadBID",0) = 0 THEN 0
                             WHEN SPT."IdMonedaPrincipal" = varMonedaEquivalente THEN
                                (COALESCE(PosMonEqui."CierreIngresado",0) - (SPT."TipoCambioCierre" / PAR."ParidadBID")) * SPT."MontoMonedaPrincipal"
                             ELSE (COALESCE(PosMonEqui."CierreIngresado",0) - (SPT."TipoCambioCierre" / PAR."ParidadBID")) * SPT."MontoMonedaPrincipal" * COALESCE(PAR."ParidadMercado",0) END
                    
                    -- Venta && NO existe PosicionCierre para la monedaExtranjera
                    WHEN SPT."IdTipoOperacion" = 'VENSPT' AND COALESCE(PosMonPri."CierreIngresado",0) = 0 AND COALESCE(PosMonEqui."CierreIngresado",0) != 0 THEN
                        CASE WHEN COALESCE(PAR."ParidadASK",0) = 0 THEN 0
                             WHEN SPT."IdMonedaPrincipal" = varMonedaEquivalente THEN
                                ((SPT."TipoCambioCierre" / PAR."ParidadASK") - COALESCE(PosMonEqui."CierreIngresado",0)) * SPT."MontoMonedaPrincipal"
                             ELSE ((SPT."TipoCambioCierre" / PAR."ParidadASK") - COALESCE(PosMonEqui."CierreIngresado",0)) * SPT."MontoMonedaPrincipal" * COALESCE(PAR."ParidadMercado",0) END
                    ELSE 0 END as "ResultadoMonedaLocalTCCierreMesa"
            FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
            INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS
                ON (SPT."IdInstrumento" = INS."IdInstrumento")
            INNER JOIN :p_DatosMesas MES
                ON (SPT."IdMesa" = MES."IdMesa")
            LEFT JOIN "BSCL.Mesa.BD.Vistas::SPT.ConsultarPosicionInicial" as PosMonPri
                ON (SPT."IdMonedaPrincipal" = PosMonPri."IdMoneda" AND
                    TO_CHAR(PosMonPri."Fecha", 'yyyyMMdd') = TO_CHAR(v_FechaHoraSistema, 'yyyyMMdd') AND
                    PosMonPri."Estado" = 'ACT')
            LEFT JOIN "BSCL.Mesa.BD.Vistas::SPT.ConsultarPosicionInicial" as PosMonEqui
                ON (PosMonEqui."IdMoneda" = varMonedaEquivalente AND
                    TO_CHAR(PosMonEqui."Fecha", 'yyyyMMdd') = TO_CHAR(v_FechaHoraSistema, 'yyyyMMdd') AND
                    PosMonEqui."Estado" = 'ACT')
            LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR
                ON (PAR."IdMoneda" = SPT."IdMonedaPrincipal" AND
                    PAR."Fecha" = SPT."FechaOperacion")
            WHERE INS."Nemotecnico" = 'INTERB'
              AND TO_VARCHAR(SPT."FechaOperacion",'YYYY-MM-DD') = TO_VARCHAR(:v_FechaHoraSistema,'YYYY-MM-DD')
              AND SPT."IdEmpresa" = :p_IdEmpresa
              AND MES."IdEstadoMesa" = 'C';
            
        
        UPDATE SPT
        SET SPT."ResultadoMonedaLocalTCCierreMesa" = REC."ResultadoMonedaLocalTCCierreMesa"
        FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
        INNER JOIN :v_RecalcularResultadoMonedaLocalTCCierreMesa REC
            ON (SPT."IdOperacionSpot" = REC."IdOperacionSpot");
            
                          
        --* -----------------------------------------------
        --* LOG DE AUDITORIA
        --* -----------------------------------------------
        p_DetalleLog = 
            SELECT  "NombreMesa"  AS "SubRegistro",
                    'IdEstadoMesa' AS "CampoRegistro",
                    "IdEstadoActual" AS "ValorAntiguo",
                    "IdEstadoNuevo" AS "ValorNuevo"
            FROM    :v_Mesas
            WHERE   "IdEstadoActual" <> "IdEstadoNuevo";
        
        CALL "BSCL.Plataforma.BD.Servicios::AgregarLog"(
             p_IdTipoLog         => 'MODIFICACION'
            ,p_IdModulo          => :p_IdModulo
            ,p_IdUsuario         => :p_IdUsuario
            ,p_IdEmpresa         => :p_IdEmpresa
            ,p_FechaSistemaLog   => TO_NVARCHAR(:v_FechaHoraSistema,'YYYY-MM-DD')
            ,p_IdUsuarioAtendido => :p_IdUsuario
            ,p_IdTipoRegistro    => 'APERTURACIERREMESA'
            ,p_NombreProceso     => 'MODIFICACION APERTURA CIERRE MESA'
            ,p_DetalleLog        => :p_DetalleLog
        );


        --* -----------------------------------------------
        --* TODO OK => REGISTRAS PROCESOS DE CIERRE EJECUTADOS PARA LOS PRODUCTOS QUE SE ESTAN CERRANDO
        --* ESTO SOLO SE HACE CUANDO TODAS LAS MESAS ASOCIADOS A UN PRODUCTO ESTAN CERRADAS.
        --* -----------------------------------------------

        v_Todas =
            SELECT  MES."IdMesa",
                    MES."IdTipoProducto",
                    CASE MES."IdTipoProducto" 
                        WHEN 'MEME' THEN 'MSP'
                        WHEN 'MEFW' THEN 'MFW'
                        WHEN 'MERF' THEN 'MRF'
                    END AS "CodigoModulo",
                    COALESCE(PAR."IdEstadoMesa", MES."IdEstadoMesa") AS "IdEstadoNuevo"
            FROM    :v_Check Chk
                    --* ---------------------------------------------------------------------------------------
                    LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES 
                    ON (Chk."IdMesa" = MES."IdMesa" AND MES."IdEmpresa" = p_IdEmpresa)
                    --* ---------------------------------------------------------------------------------------
                    LEFT JOIN :p_DatosMesas PAR 
                    ON (PAR."IdMesa" = MES."IdMesa" AND MES."IdEmpresa" = p_IdEmpresa)
            WHERE   "IdEstadoRegistro" = 'ACT';

        v_Log_Procesos = 
            SELECT  DISTINCT CERR.*,
                    "ProcesoPendientes",
                    "ProcesoCierre",
                    "IdEstadoActual",
                    "IdEstadoNuevo"
            FROM    :v_Mesas MES
                    INNER JOIN (    SELECT  "CodigoModulo", "IdTipoProducto",
                                            SUM( CASE WHEN "IdEstadoNuevo" = 'C' THEN 1 ELSE 0 END ) AS "Cerrado", 
                                            SUM( CASE WHEN "IdEstadoNuevo" = 'A' THEN 1 ELSE 0 END ) AS "Abierto"                   
                                    FROM    :v_Todas 
                                    GROUP BY "CodigoModulo", "IdTipoProducto" ) CERR
                    ON ( MES."IdTipoProducto" = CERR."IdTipoProducto" );


        --* Deja marcados los procesos de acuerdo a los Modulos que esta cerrando.
        --* -----------------------------------------------------------------------
		if p_out_ErroresContador > 0 then
			v_texto_resultado = 'Presenta errores';
		else
			v_texto_resultado = 'OK';
		end if;
		
        INSERT INTO "BSCL.Plataforma.BD::PAU.ControlProceso"
        SELECT  p_IdEmpresa, 
                "BSCL.Plataforma.BD.Secuencias::IdControlProceso".NEXTVAL,
                "CodigoModulo",
                "Proceso",
                TO_VARCHAR(v_FechaHoraSistema,'YYYY-MM-DD'),
                v_FechaHoraLOCAL,
                v_CodigoUsuario,
				v_texto_resultado
        FROM    :v_Procesos PRC
                INNER JOIN :v_Log_Procesos MES
                ON  MES."CodigoModulo" = PRC."Modulo" 
                    AND "Cerrado" > 0 AND "Abierto" = 0
                    AND "IdEstadoNuevo" <> "IdEstadoActual"
        --* La valorizacion registra en el proceso de Valorizacion FWD
        WHERE   "Proceso" <> 'VALORIZACION_FWD';

    END;
END;