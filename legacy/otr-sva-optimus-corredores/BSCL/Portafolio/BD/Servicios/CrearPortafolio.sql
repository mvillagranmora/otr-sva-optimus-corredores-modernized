SET SCHEMA "BSCL";


DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::CrearPortafolio";


CREATE PROCEDURE
-- 
--  Proposito: Servicio para la creacion de portafolio de clientes modalidad enrolamiento rapido.
--  Autor: JGARCES - BCS
--  Fecha Creacion: 28-11-2015
--
--  Proposito: Incluye autorizacion, solicitud de cambio y LOG de auditoria.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 17-03-2016
--
--  Proposito: Se incorpora logica para evaluar mesa
--  Autor: CFLORES - BCS
--  Fecha Creacion: 17-03-2016
--
--  Proposito: Se agrega IdMatrizAgrupadorAPV al crear portafolio para que en el codigo de portafolio guarde el codigo de la Matriz Agrupador,
--             se agregan parametros de entrada del servicio para el filtro del IdMatrizagrupador
--  Autor: PronetIT
--	Fecha Creacion: 31-10-2017
--
--  Proposito: Se agrega el Alias al crear portafolio.
--  Autor: PronetIT
--	Fecha Creacion: 10-05-2017
--
"BSCL.Portafolio.BD.Servicios::CrearPortafolio" (
    -- Parametros entrada comunes en todos los servicios publicos
    IN  p_IdEmpresa                     BIGINT,         --ID Empresa destino
    IN  p_IdUsuario                     BIGINT,         --ID del usuario que solicita el servicio
    IN  p_IdModulo                      NVARCHAR(3),    --ID del Modulo que lo invoca
    IN  p_UsaReglasNegocio              NVARCHAR(1),    -- S/N se usa en caso de servicios anidados.
          
    -- Parametros entrada del servicio publico
    IN  p_CodigoPortafolio              NVARCHAR(100) DEFAULT '',
    IN  p_DescripcionPortafolio         NVARCHAR(100) DEFAULT 'Portafolio Default',
    IN  p_PropositoPortafolio           NVARCHAR(520) DEFAULT 'PROPGENERALES',
    IN  p_IdCuentaOrigen                BIGINT DEFAULT NULL,
    
    -- Parametros entrada del servicio para portafolio apv
    IN  p_CodigoRegimen                 NVARCHAR(10)  DEFAULT '',
    IN  p_NombrePlan                    NVARCHAR(10)  DEFAULT '',
    IN  p_CodigoAgrupador               NVARCHAR(100)  DEFAULT '',
    
    IN  p_IdCliente                     BIGINT,
    IN  p_IdPerfilComercialBase         NVARCHAR(40),
    IN  p_IdPersonaBrokerExtranjero     BIGINT DEFAULT 0,
    IN  p_Estado                        NVARCHAR(3)  DEFAULT 'ACT',
   	IN  p_AreaCliente					NVARCHAR(45) DEFAULT '',
	IN  p_DesdeEnrolamiento				NVARCHAR(1) DEFAULT '',
    --IN    p_FechaBloqueo                  nvarchar(27),
    --IN    p_ComentarioBloqueo             nvarchar(520),
    --IN    p_FechaIngreso                  nvarchar(27),
    --IN    p_FechaCreacion                 nvarchar(27),
    --IN    p_FechaModificacion             nvarchar(27),
    
    -- Parametros salida del servicio publico
    OUT p_out_RefId                     BIGINT,
        
    -- Salidas para mensajes de advertencias y errores
    OUT p_out_AdvertenciasContador      int,
    OUT p_out_AdvertenciasDetalle       table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),  
    OUT p_out_ErroresContador           int,
    OUT p_out_ErroresDetalle            table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))

) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
    --------------------
    --DECLARE Manejo de Errores
    --------------------    
    DECLARE SALIDA_CONTROLADA       CONDITION; 
    DECLARE v_errorcuenta           INT DEFAULT 0;  
    DECLARE v_errorhashmsg          table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
    
    DECLARE v_out_AdvertenciasDetalle   TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));
    DECLARE v_out_ErroresDetalle        TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
    DECLARE v_TablaCodigosMensajes      TABLE ("CodigoMensaje" NVARCHAR(100),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520));
    
    DECLARE v_erroroutmsg           nvarchar(520);  
    
                
    DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
    BEGIN
        --salida/error/advertencia controlado por este servicio
        
                                
                                
        select count(1) into p_out_ErroresContador 
        from :p_out_ErroresDetalle;
        select count(1) into p_out_AdvertenciasContador 
        from :p_out_AdvertenciasDetalle;    
                                
    END;
      
    p_out_AdvertenciasContador  := 0;
    p_out_ErroresContador       := 0;
    p_out_AdvertenciasDetalle   = select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
    p_out_ErroresDetalle        = select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
    v_out_AdvertenciasDetalle   = select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
    v_out_ErroresDetalle        = select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
    v_TablaCodigosMensajes      = SELECT '' AS "CodigoMensaje", '' as "CodigoDato", '' as "ValorDato" FROM DUMMY WHERE 1=2; 
    
    BEGIN   
        --------------------
        --Declare Variables
        --------------------
            DECLARE v_CodigoPortafolio              NVARCHAR(100):= p_CodigoPortafolio;
            DECLARE v_IdPortafolio                  BIGINT:=0;
            DECLARE v_FechaIngreso                  SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
            DECLARE v_IdConcepto                    NVARCHAR(6):='NUEPOR';
            DECLARE v_RequiereAutorizacion          NVARCHAR(1):='';
            DECLARE v_RequiereAutOrigen             NVARCHAR(1):='';
            DECLARE v_IdEstadoAutorizacion          NVARCHAR(3):='PEN';
            DECLARE v_out_AdvertenciasContador      INT; 
            DECLARE v_out_ErroresContador           INT;
            
            DECLARE v_out_RefId                     BIGINT:=0;
            
            DECLARE v_out_AuthAdvertenciasContador  INT;
            DECLARE v_out_AuthErroresContador       INT;
            
            DECLARE v_countPortafolio               INT:=0;
            DECLARE v_countPerfilComercial          INT:=0;
            DECLARE v_countPortafoliosVigentes      INT:=0;
            DECLARE v_IdEmpleadoEjecutivo           BIGINT;
            DECLARE v_countPlanAhorro               INT:=0;
            
            DECLARE v_countIdCuentaBanco            INT:=0;
            DECLARE v_IdCuentaBanco            		BIGINT;
            
            -- CUENTA DE INVERSION PESO
            DECLARE v_MonedaSistema                 NVARCHAR(3);
            DECLARE v_CreaCuentaInversionEUR        NVARCHAR(520);
            DECLARE v_out_refCtaInvPesoId           BIGINT; -- CUENTA INVERSION PORTAFOLIO

            DECLARE v_out_AdvertenciasContadorCtaInvPeso bigint;
            DECLARE v_out_AdvertenciasDetalleCtaInvPeso table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));
            DECLARE v_out_ErroresContadorCtaInvPeso int;
            DECLARE v_out_ErroresDetalleCtaInvPeso table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));   
            DECLARE v_out_InformacionContadorCtaInvPeso int;
            DECLARE v_out_InformacionDetalleCtaInvPeso table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));   

            ---------------
            -- PERFILES
            ---------------
            DECLARE v_IdPerfilProductos                 NVARCHAR(40);
            DECLARE v_IdPerfilCobros                    NVARCHAR(40);   
            DECLARE v_IdPerfilCobrosPeru                NVARCHAR(40);   
            DECLARE v_IdModeloFacturacion               NVARCHAR(40);
            DECLARE v_IdPerfilLimites                   NVARCHAR(40);
            DECLARE v_IdPerfilCondicionesOperativas     NVARCHAR(40);
            
            DECLARE v_IdPlantillaExtra                  NVARCHAR(40);
            DECLARE v_ValCreaPrefPagoVenComTer 			NVARCHAR(5);
            DECLARE v_CountMatrizAgrupadorAPV           INT:=0;
            DECLARE v_IdMatrizAgrupadorAPV              BIGINT:=0;
            DECLARE v_NombreAgrupador                   NVARCHAR(100);
            DECLARE v_IdProductoAgrupador               INT:=0;
            DECLARE vTablaMatrizAgrupador               TABLE ("IdMatrizAgrupadorAPV" BIGINT);
            DECLARE vCountEstadosMatrizAgrupador        INT:=0;
            DECLARE vIdPortafolioGrupoAPV               INT:=0;
            DECLARE vExistePortafolio                   INT:=0;
            DECLARE v_ProductoAgrupador                 NVARCHAR(100);
            DECLARE v_TipoEntidadReplica                NVARCHAR(50):= 'PORTAFOLIO';
            DECLARE v_TipoOperacion                     NVARCHAR(1):= 'U';
            DECLARE v_ImpideDoblePlanDeAhorro           NVARCHAR(1):= 'S';
			DECLARE v_NroRegistros           			INT:=0;
            DECLARE v_Alias                             NVARCHAR(100):= '';
           
           --Variables Logs
           	DECLARE v_fechaHoraSistema 						NVARCHAR(40);
	        DECLARE v_TipoEntidad							NVARCHAR(40):= 'PORTAFOLIO';
			DECLARE V_subregistro 							NVARCHAR(40):= '';
			DECLARE v_NombreProceso							NVARCHAR(100):='Ingreso Portafolio';
			DECLARE v_nombreCliente 						NVARCHAR(200):='';
			DECLARE v_documentoIdentidad 					NVARCHAR(40):='';
			DECLARE v_tipoDocumentoIdentidad 				NVARCHAR(40):='';
			DECLARE v_DescripcionPortafolio					NVARCHAR(100):='';
			DECLARE v_proposito								NVARCHAR(100):='';
			DECLARE v_perfilComercialBase					NVARCHAR(200):='';
			DECLARE v_estadoAutorizacion					NVARCHAR(40):='';
			DECLARE v_estadoAutorizacionPortafolioLargo		NVARCHAR(40):='';
        
        IF(p_CodigoPortafolio = '') THEN
            SELECT "Valor" INTO v_CodigoPortafolio FROM "BSCL.SocioDeNegocio.BD::SOC.ConfiguracionSocioNegocio"
            WHERE "CodigoParametro" = 'POR.CodigoPortafolioDefault';
        END IF;
        -------------------
        --Validaciones
        --------------------
        
        -- Existencia del portafolio.       
        BEGIN
            /*INICIO - PRONET IT*/
            IF(p_PropositoPortafolio <> 'PROPOSITOAPV')THEN
                SELECT COUNT("IdPortafolio") INTO v_countPortafolio 
                FROM "BSCL.Portafolio.BD::POR.Portafolio" 
                WHERE "CodigoPortafolio" = :v_CodigoPortafolio AND "IdCliente" = :p_IdCliente;
                
                SELECT COUNT(1) INTO v_countPortafoliosVigentes
                FROM "BSCL.Portafolio.BD::POR.Portafolio" 
                WHERE "IdCliente" = :p_IdCliente;
            END IF;
            
            IF(p_PropositoPortafolio = 'PROPOSITOAPV')THEN 
                
                SELECT COUNT("IdPortafolio") INTO v_countPortafolio
                FROM "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
                INNER JOIN "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" AS Matriz
                        ON Matriz."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
                       AND Portafolio."Proposito" = Matriz."IdPropositoPortafolio"
                WHERE Portafolio."IdCliente" = :p_IdCliente
                AND Portafolio."Proposito" = :p_PropositoPortafolio
                AND Matriz."Codigo" = :p_CodigoRegimen
                AND Matriz."IdPropositoPortafolio" = :p_PropositoPortafolio
                AND Matriz."NombrePlan" = :p_NombrePlan
                AND Matriz."CodigoAgrupador" = :p_CodigoAgrupador;
            
                SELECT COUNT("IdMatrizAgrupadorAPV") INTO v_CountMatrizAgrupadorAPV
                FROM "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario"
                WHERE "Codigo" = :p_CodigoRegimen
                  AND "IdPropositoPortafolio" = :p_PropositoPortafolio
                  AND "NombrePlan" = :p_NombrePlan
                  AND "CodigoAgrupador" = :p_CodigoAgrupador;                 
                
                IF (v_CountMatrizAgrupadorAPV > 0) THEN
                    SELECT "IdMatrizAgrupadorAPV","Codigo","IdProductoAgrupador","Producto" INTO v_IdMatrizAgrupadorAPV, v_CodigoPortafolio, v_IdProductoAgrupador, v_ProductoAgrupador 
                    FROM "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario"
                    WHERE "Codigo" = :p_CodigoRegimen
                    AND "IdPropositoPortafolio" = :p_PropositoPortafolio
                    AND "NombrePlan" = :p_NombrePlan
                    AND "CodigoAgrupador" = :p_CodigoAgrupador;
                    
                    vTablaMatrizAgrupador = SELECT "IdMatrizAgrupadorAPV" 
                                            FROM "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"
                                            WHERE "IdProductoAgrupador" = :v_IdProductoAgrupador;
                    
                    SELECT COUNT("IdEstado"), MAX("IdPortafolio") INTO vCountEstadosMatrizAgrupador, vIdPortafolioGrupoAPV --RESULT:2
                    FROM "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
                    INNER JOIN :vTablaMatrizAgrupador AS Matriz
                            ON Matriz."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
                    WHERE /*Portafolio."IdEstado" = 'ACT'
                    AND */Portafolio."IdCliente" = :p_IdCliente  --1423686
                    AND Portafolio."Proposito" = 'PROPOSITOAPV';
                END IF;
				
                --Regla Cliente solo puede tener un plan (Activo o Abierto)
                SELECT COUNT(1) INTO v_NroRegistros FROM "BSCL.Plataforma.BD::PLA.Configuracion" 
                WHERE "CodigoParametro" = 'APV.ImpideDoblePlanAhorro' AND "IdEmpresa" = :p_IdEmpresa;  
                IF v_NroRegistros > 0 THEN 
					SELECT "Valor" INTO v_ImpideDoblePlanDeAhorro FROM "BSCL.Plataforma.BD::PLA.Configuracion" 
					WHERE "CodigoParametro" = 'APV.ImpideDoblePlanAhorro' AND "IdEmpresa" = :p_IdEmpresa;	
                END IF;

                IF v_ImpideDoblePlanDeAhorro = 'S' THEN
	                SELECT COUNT(Portafolio."IdPortafolio") INTO v_countPlanAhorro
		                FROM "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
		                INNER JOIN "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" AS Matriz
		                        ON Matriz."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		                       AND Portafolio."Proposito" = Matriz."IdPropositoPortafolio"
		                WHERE Portafolio."IdCliente" = :p_IdCliente
		                AND Portafolio."Proposito" = :p_PropositoPortafolio
		                AND Portafolio."IdEstado" = 'ACT'
		                --AND Matriz."Codigo" = :p_CodigoRegimen
		                AND Matriz."IdPropositoPortafolio" = :p_PropositoPortafolio
		                AND Matriz."NombrePlan" <> :p_NombrePlan
		                --AND Matriz."CodigoAgrupador" <> :p_CodigoAgrupador
                ;
                END IF;
				
                IF(v_countPlanAhorro > 0 ) THEN
	                v_out_ErroresDetalle =  select 'REGLA_CLIENTE_PLAN_AHORRO' as "Codigo",'' as "Descripcion" from dummy;
	                
	                v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
	                                '' as "CodigoDato",
	                                '' as "ValorDato" 
	                                FROM :v_out_ErroresDetalle
	                                UNION ALL
	                                SELECT "Codigo" AS "CodigoMensaje",
	                                '' as "CodigoDato",
	                                '' as "ValorDato" 
	                                FROM :v_out_AdvertenciasDetalle;
	          
	                CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
	        
	                        
	                p_out_ErroresDetalle =  SELECT  errores."Codigo",
	                                            --errores."Descripcion",
	                                            msj."Descripcion"
	                                        FROM :v_out_ErroresDetalle errores
	                                        INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";
	                                        
	                p_out_AdvertenciasDetalle = SELECT  adv."Codigo",
	                                    adv."RequiereAutorizacion",
	                                    --adv."Descripcion",
	                                    msj."Descripcion"
	                                FROM :v_out_AdvertenciasDetalle adv
	                                INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";
	        
	                
	                SIGNAL SALIDA_CONTROLADA;   
	            END IF;
            END IF;
            /*FIN - PRONET IT*/
            
            IF(v_countPortafolio > 0 ) THEN
                v_out_AdvertenciasDetalle =  select 'PORTAFOLIO_EXISTE' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy;
                
                v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
                                '' as "CodigoDato",
                                '' as "ValorDato" 
                                FROM :v_out_AdvertenciasDetalle
                                UNION ALL
                                SELECT "Codigo" AS "CodigoMensaje",
                                '' as "CodigoDato",
                                '' as "ValorDato" 
                                FROM :p_out_ErroresDetalle;
          
                CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
        
                        
                p_out_ErroresDetalle =  SELECT  errores."Codigo",
                                            --errores."Descripcion",
                                            msj."Descripcion"
                                        FROM :v_out_ErroresDetalle errores
                                        INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";
                                        
                p_out_AdvertenciasDetalle = SELECT  adv."Codigo",
                                    adv."RequiereAutorizacion",
                                    --adv."Descripcion",
                                    msj."Descripcion"
                                FROM :v_out_AdvertenciasDetalle adv
                                INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";
        
                
                SIGNAL SALIDA_CONTROLADA;   
            END IF;
        END;
        
        -- Existencia del Perfil Comercial Base.
        BEGIN   
            SELECT COUNT("IdPerfilComercialBase") INTO v_countPerfilComercial
            FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilComercialBase"
            WHERE "IdPerfilComercialBase" = :p_IdPerfilComercialBase;
            IF(v_countPerfilComercial=0) THEN
                v_out_ErroresDetalle =  select 'PERFIL_COMERCIAL_NO_EXISTE' as "Codigo",'' as "Descripcion" from dummy;
                
                v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
                                '' as "CodigoDato",
                                '' as "ValorDato" 
                                FROM :v_out_ErroresDetalle
                                UNION ALL
                                SELECT "Codigo" AS "CodigoMensaje",
                                '' as "CodigoDato",
                                '' as "ValorDato" 
                                FROM :v_out_AdvertenciasDetalle;
          
                CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
                
                                
                p_out_ErroresDetalle =  SELECT  errores."Codigo",
                                            --errores."Descripcion",
                                            msj."Descripcion"
                                        FROM :v_out_ErroresDetalle errores
                                        INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";
                                        
                p_out_AdvertenciasDetalle = SELECT  adv."Codigo",
                                    adv."RequiereAutorizacion",
                                    --adv."Descripcion",
                                    msj."Descripcion"
                                FROM :v_out_AdvertenciasDetalle adv
                                INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";
        
                
                
                SIGNAL SALIDA_CONTROLADA;   
            END IF;
        END;
        
        --------------------
        --Reglas Negocio
        --------------------
        
        
        --------------------
        --L�gica Negocio
        --------------------
        BEGIN
            --Inicializacion de variables:
            p_out_RefId := -1;
            CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
            
            IF(v_countPortafolio=0) THEN
            
                -- Obtiene los perfiles asociados al perfil comercial de entrada.
                SELECT 
                    PCOM."IdPerfilProductos",
                    PCOM."IdPerfilCobros",
                    PCOM."IdPerfilCobrosPeru",
                    PCOM."IdPerfilLimites",
                    PCOM."IdPerfilCondicionesOperativas"
                INTO 
                    v_IdPerfilProductos,
                    v_IdPerfilCobros,
                    v_IdPerfilCobrosPeru,
                    v_IdPerfilLimites,
                    v_IdPerfilCondicionesOperativas
                FROM 
                    "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilComercialBase" PCOM
                WHERE 
                    "IdPerfilComercialBase" = :p_IdPerfilComercialBase;
            
                select "BSCL"."BSCL.Portafolio.BD.Secuencias::Portafolio".NEXTVAL into p_out_RefId from dummy;
                v_IdPortafolio:=p_out_RefId;
                -- PENDIENTE POR REVISAR!
------------------------------------------
                --select "BSCL"."BSCL.Portafolio.BD.Secuencias::PreferenciaFacturacion".NEXTVAL into p_out_RefId2 from dummy;           
                --insert into "BSCL"."BSCL.Portafolio.BD::POR.PreferenciaFacturacion"(
                --  "IdPreferenciaFacturacion"
                --  ,"IdPortafolio"
                --  ,"IdTipoFacturacion"
                --  ,"IdFacturacion"
                --  ,"FechaCreacion"
                --)
                --values (
                --  p_out_RefId2
                --  ,p_out_RefId
                --  ,'NORM'
                --  ,1
                --  ,FechaCreacion
                --);
------------------------------------------      
        
                v_IdPlantillaExtra := CONCAT('EXT_',CAST(:p_out_RefId as NVARCHAR));
                
                select "IdEmpleadoEjecutivo","NombreCompleto" into v_IdEmpleadoEjecutivo,v_Alias
                from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 
                WHERE "IdCliente" = :p_IdCliente;
                  
                insert into "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" (
                    "IdPortafolio",
                    "CodigoPortafolio",
                    "DescripcionPortafolio",
                    "Proposito", 
                    "IdCliente", 
                    "IdEstado",
                    "FechaBloqueo",
                    "ComentarioBloqueo", 
                    --Perfil Comercial
                    "IdPerfilComercialBaseOrigen",
                    "IdPersonaBrokerExtranjero",
                    "IdPerfilProductos",
                    "IdPerfilProductosExtra",
                    "IdPerfilCobros",
                    "IdPerfilCobrosExtra",
                    "IdPerfilCobrosPeru",
                    "IdPerfilCobrosPeruExtra",
                    "IdPerfilLimites",
                    "IdPerfilLimitesExtra",
                    "IdPerfilCondicionesOperativas",
                    "IdEmpleadoEjecutivo",
                    --Autorizacion
                    "IdEstadoAutorizacion",
                    --
                    "FechaIngreso", 
                    "FechaCreacion", 
                    "FechaModificacion",
                    "Alias",
                    "IdMatrizAgrupadorAPV",
                    "IdCuentaOrigen",
                    "AreaCliente"
                )
                values (
                    :p_out_RefId,
                    :v_CodigoPortafolio,
                    :p_DescripcionPortafolio,  
                    :p_PropositoPortafolio,
                    :p_IdCliente, 
                    :p_Estado,
                    '0001-01-01', 
                    '', 
                    :p_IdPerfilComercialBase,
                    :p_IdPersonaBrokerExtranjero,
                    :v_IdPerfilProductos,
                    :v_IdPlantillaExtra,
                    :v_IdPerfilCobros,
                    :v_IdPlantillaExtra,
                    :v_IdPerfilCobrosPeru,
                    :v_IdPlantillaExtra,
                    :v_IdPerfilLimites,
                    :v_IdPlantillaExtra,
                    :v_IdPerfilCondicionesOperativas,
                    ifnull(v_IdEmpleadoEjecutivo,0),
                    'PEN',
                    :v_FechaIngreso, --:p_FechaIngreso, 
                    CURRENT_UTCTIMESTAMP, --:p_FechaCreacion, 
                    CURRENT_UTCTIMESTAMP,  --:p_FechaModificacion
                    :v_Alias,
                    :v_IdMatrizAgrupadorAPV,
                    :p_IdCuentaOrigen,
                    :p_AreaCliente
                );
               
               --Heredad las preferencias del portafolio desde el modelo tipo cliente 
               INSERT INTO "BSCL.Plataforma.BD::IND.Indicador"(
					"IdIndicador",
					"IdEntidad",
					"IdTipoEntidad",
					"IdTipoIndicador",
					"ModeloCliente",
					"IdEmpresa",
					"ValorIndicador",
					"FechaIngreso",
					"FechaCreacion",
					"FechaModificacion",
					"IdEstadoAutorizacion",
					"FechaVencimiento"
				)SELECT
					"BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL AS "IdIndicador",
					:p_out_RefId AS "IdEntidad",
					ATR."IdTipoEntidad",
					ATR."IdTipoIndicador",
					ATR."ModeloCliente",
					ATR."IdEmpresa",
					CASE ATR."CodigoTipoIndicador"
						WHEN 'CAR_DIRELE' THEN TO_NVARCHAR(Cliente."IdDireccionElectronicaPrincipal")
						WHEN 'CAR_DIRFIS' THEN TO_NVARCHAR(Cliente."IdDireccionFisicaPrincipal")
						WHEN 'FAC_DIRELE' THEN TO_NVARCHAR(Cliente."IdDireccionElectronicaPrincipal")
						WHEN 'GEN_CODDIR' THEN TO_NVARCHAR(Cliente."IdDireccionFisicaPrincipal")
						WHEN 'GEN_DIR_PROPIA' THEN TO_NVARCHAR('S')
						ELSE ATR."ValorIndicador" 
					END AS "ValorIndicador",
					CURRENT_UTCTIMESTAMP,
					CURRENT_UTCTIMESTAMP,
					CURRENT_UTCTIMESTAMP,
					'ING' AS "IdEstadoAutorizacion",
					'0001-01-01 00:00:01'
				FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente
				INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" P ON Cliente."IdCliente" = P."IdCliente" AND P."IdPortafolio"  = :p_out_RefId
				INNER JOIN (
					SELECT TI."CodigoTipoIndicador", I.* FROM BSCL."BSCL.Plataforma.BD::IND.TipoIndicador" TI
					INNER JOIN BSCL."BSCL.Plataforma.BD::IND.Indicador" I ON TI."IdTipoIndicador" = I."IdTipoIndicador"
					WHERE TI."IdTipoEntidad" = 'PORTAFOLIO' AND TI."IdUso" = 'PRE'
				) ATR ON Cliente."IdTipoCliente" = ATR."ModeloCliente" AND ATR."IdTipoEntidad" = 'PORTAFOLIO' AND ATR."IdEntidad" = 0
				LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" I ON I."IdEntidad" = P."IdPortafolio" AND I."IdTipoEntidad" = 'PORTAFOLIO' AND ATR."IdIndicador" = I."IdIndicador"
				WHERE "IdPortafolio" = :p_out_RefId AND I."IdEntidad" IS NULL 
				--HEREDAR SOLO LAS SIGUIENTES PREFERENCIAS DE PORTAFOLIO DESDE EL MODELO TIPO CLIENTE
				AND ATR."CodigoTipoIndicador" IN ('CAR_DIRELE', 'CAR_DIRFIS', 'FAC_DIRELE',  'ORDMODALIDAD','GEN_CODDIR','GEN_DIR_PROPIA');
			
				
--direccion fisica

INSERT INTO "BSCL.Plataforma.BD::IND.Indicador"(
 "IdIndicador",
 "IdEntidad",
 "IdTipoEntidad",
 "IdTipoIndicador",
 "ModeloCliente",
 "IdEmpresa",
 "ValorIndicador",
 "FechaIngreso",
 "FechaCreacion",
 "FechaModificacion",
 "IdEstadoAutorizacion",
 "FechaVencimiento"
 )
SELECT
 "BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL AS "IdIndicador",
 dirTable."IdEntidad",
 dirTable."IdTipoEntidad",
 dirTable."IdTipoIndicador",
 dirTable."ModeloCliente",
 dirTable."IdEmpresa",
 dirTable."ValorIndicador",
 dirTable."FechaIngreso",
 dirTable."FechaCreacion",
 dirTable."FechaModificacion",
 dirTable."IdEstadoAutorizacion",
 dirTable."FechaVencimiento"
 FROM (SELECT top 1 
 :p_out_RefId AS "IdEntidad",
 ATR."IdTipoEntidad",
 ATR."IdTipoIndicador",
 ATR."ModeloCliente",
 ATR."IdEmpresa",
	 CASE ATR."CodigoTipoIndicador"
		 WHEN 'FAC_DIRFIS' THEN TO_NVARCHAR(DIR."IdDireccionFisica")
		 ELSE ATR."ValorIndicador"
	 END AS "ValorIndicador",
 CURRENT_UTCTIMESTAMP AS "FechaIngreso",
 CURRENT_UTCTIMESTAMP AS "FechaCreacion",
 CURRENT_UTCTIMESTAMP AS "FechaModificacion",
 'ING' AS "IdEstadoAutorizacion",
 '0001-01-01 00:00:01' AS "FechaVencimiento"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIR ON Cliente."IdPersona" = DIR."IdPersona" AND DIR."Predeterminado" = 'UI_SI'
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" P ON Cliente."IdCliente" = P."IdCliente" AND P."IdPortafolio"  = :p_out_RefId 
INNER JOIN (
	SELECT TI."CodigoTipoIndicador", I.* 
	FROM BSCL."BSCL.Plataforma.BD::IND.TipoIndicador" TI
 	INNER JOIN BSCL."BSCL.Plataforma.BD::IND.Indicador" I ON TI."IdTipoIndicador" = I."IdTipoIndicador"
	 WHERE TI."IdTipoEntidad" = 'PORTAFOLIO' AND TI."IdUso" = 'PRE' AND  TI."CodigoTipoIndicador" IN ('FAC_DIRFIS')
) ATR ON ATR."IdTipoEntidad" = 'PORTAFOLIO'-- AND ATR."IdEntidad" = 0
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" I ON I."IdEntidad" = P."IdPortafolio" AND I."IdTipoEntidad" = 'PORTAFOLIO' AND ATR."IdIndicador" = I."IdIndicador"
WHERE Cliente."IdCliente" = :p_IdCliente ) dirTable;



--direccion electronica notificacion
INSERT INTO "BSCL.Plataforma.BD::IND.Indicador"(
 "IdIndicador",
 "IdEntidad",
 "IdTipoEntidad",
 "IdTipoIndicador",
 "ModeloCliente",
 "IdEmpresa",
 "ValorIndicador",
 "FechaIngreso",
 "FechaCreacion",
 "FechaModificacion",
 "IdEstadoAutorizacion",
 "FechaVencimiento"
 )
SELECT
 "BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL AS "IdIndicador",
 NOTEMAIL."IdEntidad",
 NOTEMAIL."IdTipoEntidad",
 NOTEMAIL."IdTipoIndicador",
 NOTEMAIL."ModeloCliente",
 NOTEMAIL."IdEmpresa",
 NOTEMAIL."ValorIndicador",
 NOTEMAIL."FechaIngreso",
 NOTEMAIL."FechaCreacion",
 NOTEMAIL."FechaModificacion",
 NOTEMAIL."IdEstadoAutorizacion",
 NOTEMAIL."FechaVencimiento"
 FROM (SELECT TOP 1  
 :p_out_RefId AS "IdEntidad",
 ATR."IdTipoEntidad",
 ATR."IdTipoIndicador",
 ATR."ModeloCliente",
 ATR."IdEmpresa",
	 CASE ATR."CodigoTipoIndicador"
		 WHEN 'NOT_EMAIL' THEN TO_NVARCHAR(DIR."IdDireccionElectronica")
		 ELSE ATR."ValorIndicador"
	 END AS "ValorIndicador",
 CURRENT_UTCTIMESTAMP AS "FechaIngreso",
 CURRENT_UTCTIMESTAMP AS "FechaCreacion",
 CURRENT_UTCTIMESTAMP AS "FechaModificacion",
 'ING' AS "IdEstadoAutorizacion",
 '0001-01-01 00:00:01' AS "FechaVencimiento"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" DIR ON Cliente."IdPersona" = DIR."IdPersona" AND DIR."Predeterminado" = 'UI_SI'
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" P ON Cliente."IdCliente" = P."IdCliente" AND P."IdPortafolio"  = :p_out_RefId 
INNER JOIN (
	SELECT TI."CodigoTipoIndicador", I.* 
	FROM BSCL."BSCL.Plataforma.BD::IND.TipoIndicador" TI
 	INNER JOIN BSCL."BSCL.Plataforma.BD::IND.Indicador" I ON TI."IdTipoIndicador" = I."IdTipoIndicador"
	 WHERE TI."IdTipoEntidad" = 'PORTAFOLIO' AND TI."IdUso" = 'PRE' AND  TI."CodigoTipoIndicador" IN ('NOT_EMAIL')
) ATR ON ATR."IdTipoEntidad" = 'PORTAFOLIO'
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" I ON I."IdEntidad" = P."IdPortafolio" AND I."IdTipoEntidad" = 'PORTAFOLIO' AND ATR."IdIndicador" = I."IdIndicador"
WHERE Cliente."IdCliente" = :p_IdCliente) NOTEMAIL;
				
			--direccion electronica de facturacion
INSERT INTO "BSCL.Plataforma.BD::IND.Indicador"(
 "IdIndicador",
 "IdEntidad",
 "IdTipoEntidad",
 "IdTipoIndicador",
 "ModeloCliente",
 "IdEmpresa",
 "ValorIndicador",
 "FechaIngreso",
 "FechaCreacion",
 "FechaModificacion",
 "IdEstadoAutorizacion",
 "FechaVencimiento"
 )
SELECT
 "BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL AS "IdIndicador",
 dirElecTable."IdEntidad",
 dirElecTable."IdTipoEntidad",
 dirElecTable."IdTipoIndicador",
 dirElecTable."ModeloCliente",
 dirElecTable."IdEmpresa",
 dirElecTable."ValorIndicador",
 dirElecTable."FechaIngreso",
 dirElecTable."FechaCreacion",
 dirElecTable."FechaModificacion",
 dirElecTable."IdEstadoAutorizacion",
 dirElecTable."FechaVencimiento"
 FROM (SELECT TOP 1  
 :p_out_RefId AS "IdEntidad",
 ATR."IdTipoEntidad",
 ATR."IdTipoIndicador",
 ATR."ModeloCliente",
 ATR."IdEmpresa",
	 CASE ATR."CodigoTipoIndicador"
		 WHEN 'ORDMAILNTF' THEN TO_NVARCHAR(DIR."IdDireccionElectronica")
		 ELSE ATR."ValorIndicador"
	 END AS "ValorIndicador",
 CURRENT_UTCTIMESTAMP AS "FechaIngreso",
 CURRENT_UTCTIMESTAMP AS "FechaCreacion",
 CURRENT_UTCTIMESTAMP AS "FechaModificacion",
 'ING' AS "IdEstadoAutorizacion",
 '0001-01-01 00:00:01' AS "FechaVencimiento"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" DIR ON Cliente."IdPersona" = DIR."IdPersona" --AND DIR."Predeterminado" = 'UI_SI'
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" P ON Cliente."IdCliente" = P."IdCliente" AND P."IdPortafolio"  = :p_out_RefId 
INNER JOIN (
	SELECT TI."CodigoTipoIndicador", I.* 
	FROM BSCL."BSCL.Plataforma.BD::IND.TipoIndicador" TI
 	INNER JOIN BSCL."BSCL.Plataforma.BD::IND.Indicador" I ON TI."IdTipoIndicador" = I."IdTipoIndicador"
	 WHERE TI."IdTipoEntidad" = 'PORTAFOLIO' AND TI."IdUso" = 'PRE' AND  TI."CodigoTipoIndicador" IN ('ORDMAILNTF')
) ATR ON ATR."IdTipoEntidad" = 'PORTAFOLIO'
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" I ON I."IdEntidad" = P."IdPortafolio" AND I."IdTipoEntidad" = 'PORTAFOLIO' AND ATR."IdIndicador" = I."IdIndicador"
WHERE Cliente."IdCliente" = :p_IdCliente) dirElecTable;	
				--Indicador Opera contra Cuenta de Inversion (GEN_OCCINV) queda marcado para PORTAFOLIO APV
			    INSERT INTO "BSCL.Plataforma.BD::IND.Indicador"(
					"IdIndicador",
					"IdEntidad",
					"IdTipoEntidad",
					"IdTipoIndicador",
					"ModeloCliente",
					"IdEmpresa",
					"ValorIndicador",
					"FechaIngreso",
					"FechaCreacion",
					"FechaModificacion",
					"IdEstadoAutorizacion",
					"FechaVencimiento"
				)
				SELECT
					"BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL AS "IdIndicador",
					:p_out_RefId AS "IdEntidad",
					TI."IdTipoEntidad",
					TI."IdTipoIndicador",
					Cliente."IdTipoCliente",
					TI."IdEmpresa",
					'S' as "ValorIndicador",
					CURRENT_UTCTIMESTAMP,
					CURRENT_UTCTIMESTAMP,
					CURRENT_UTCTIMESTAMP,
					'ING' AS "IdEstadoAutorizacion",
					'0001-01-01 00:00:01'
				FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente
				INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" P ON Cliente."IdCliente" = P."IdCliente" AND P."IdPortafolio" = :p_out_RefId
				INNER JOIN BSCL."BSCL.Plataforma.BD::IND.TipoIndicador" TI
					ON (TI."CodigoTipoIndicador" = 'GEN_OCCINV' AND
						TI."IdTipoEntidad" = 'PORTAFOLIO' AND
						TI."IdUso" = 'PRE' AND
						TI."IdEmpresa" = :p_IdEmpresa)
				WHERE P."IdPortafolio" = :p_out_RefId
				AND :p_PropositoPortafolio = 'PROPOSITOAPV'
				;
                
                -- INSERT CABECERA DE PLANTILLAS POR DEFECTO.
                INSERT INTO "BSCL.Plataforma.BD::COM.PerfilCobros" (
                    "IdPerfilCobros",
                    "DescripcionPerfilCobros",
                    "EsPerfilPersonalizado",
                    "TipoEntidadReferencia",
                    "IdReferencia",
                    "FechaCreacion",
                    "FechaModificacion",
                    "IdEstado",
                    "PorDefecto",
                    "IdEmpresa",
                    "FechaIngreso",
                    "IdEstadoAutorizacion",
                    "IdUsuarioCreador" ) 
                VALUES (
                      :v_IdPlantillaExtra,
                      :v_IdPlantillaExtra,
                      'S',
                      '0',
                      '0',
                      CURRENT_UTCTIMESTAMP,
                      CURRENT_UTCTIMESTAMP,               
                      'ACT',
                      'N',
                      :p_IdEmpresa,
                      :v_FechaIngreso,
                      'AUT',
                      :p_IdUsuario
                );
                
                -- INSERT CABECERA DE PLANTILLAS POR DEFECTO.
                INSERT INTO "BSCL.Plataforma.BD::COM_2.PerfilCobros" (
                    "IdPerfilCobros",
                    "DescripcionPerfilCobros",
                    "EsPerfilPersonalizado",
                    "TipoEntidadReferencia",
                    "IdReferencia",
                    "FechaCreacion",
                    "FechaModificacion",
                    "IdEstado",
                    "IdEmpresa",
                    "FechaIngreso",
                    "IdEstadoAutorizacion",
                    "IdUsuarioCreador" ) 
                VALUES (
                      :v_IdPlantillaExtra,
                      :v_IdPlantillaExtra,
                      'S',
                      '0',
                      '0',
                      CURRENT_UTCTIMESTAMP,
                      CURRENT_UTCTIMESTAMP,               
                      'ACT',
                      :p_IdEmpresa,
                      :v_FechaIngreso,
                      'AUT',
                      :p_IdUsuario
                );
                
                insert into "BSCL.Plataforma.BD::COM.PerfilProductos" (
                    "IdPerfilProductos",
                    "DescripcionPerfilProductos",
                    "IdCategoriaInversionista",
                    "EsPerfilPersonalizado",
                    "TipoEntidadReferencia",
                    "IdReferencia",
                    "FechaCreacion",
                    "FechaModificacion",
                    "IdEstado",
                    "PorDefecto",
                    "IdEmpresa",
                    "FechaIngreso",
                    "IdEstadoAutorizacion",
                    "IdUsuarioCreador" ) 
                VALUES (
                      :v_IdPlantillaExtra,
                      :v_IdPlantillaExtra,
                      '0',
                      'S',
                      '0',
                      '0',
                      CURRENT_UTCTIMESTAMP, --p_Creacion, -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
                      CURRENT_UTCTIMESTAMP,               
                      'ACT',
                      'N',
                      :p_IdEmpresa,
                      :v_FechaIngreso,
                      'AUT',
                      :p_IdUsuario
                );
                
                insert into "BSCL.Plataforma.BD::LIM.PlantillaLimite" (
                    "IdPlantillaLimite",
                    "DescripcionPlantillaLimite",
                    "FechaCreacion",
                    "FechaModificacion",
                    "FechaIngreso",
                    "IdEstadoAutorizacion",
                    "IdUsuarioCreador",
                    "IdEstado",
                    "PorDefecto",
                    "IdEmpresa" ) 
                VALUES (
                      :v_IdPlantillaExtra,
                      :v_IdPlantillaExtra,
                      CURRENT_UTCTIMESTAMP, --p_Creacion, -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
                      CURRENT_UTCTIMESTAMP,
                      :v_FechaIngreso,
                      'AUT',
                      :p_IdUsuario,
                      'ACT',
                      'N',
                      :p_IdEmpresa
                );
                
                
                IF (p_out_RefId > 0) THEN
                    -- CUENTA INVERSION
                    SELECT "Valor" INTO v_MonedaSistema 
                    FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.MonedaSistema' AND "IdEmpresa" = :p_IdEmpresa;

                    v_CuentaInversion =
                     SELECT :p_IdCliente AS "IdCliente",
                            :p_out_RefId AS "IdPortafolio",
                            :v_MonedaSistema AS "IdMoneda",
                            null AS "IdCuentaInversion",
                            null AS "Nombre",
                            null AS "CuentaInversionPrincipal",
                            'I' AS "Accion"
                       FROM dummy UNION ALL
                     SELECT :p_IdCliente AS "IdCliente",
                            :p_out_RefId AS "IdPortafolio",
                            'USD' AS "IdMoneda",
                            null AS "IdCuentaInversion",
                            null AS "Nombre",
                            null AS "CuentaInversionPrincipal",
                            'I' AS "Accion"
                       FROM dummy;
                       
                       SELECT "Valor" INTO v_CreaCuentaInversionEUR 
                        FROM "BSCL.SocioDeNegocio.BD::SOC.ConfiguracionSocioNegocio" 
                        WHERE "CodigoParametro" = 'CLI.CrearCuentaInversionEUR' AND "IdEmpresa" = :p_IdEmpresa;
                       
                       IF COALESCE(:v_CreaCuentaInversionEUR, 'S') = 'S' THEN
                        v_CuentaInversion = SELECT * FROM :v_CuentaInversion
                                            UNION ALL SELECT :p_IdCliente AS "IdCliente",
                                                            :p_out_RefId AS "IdPortafolio",
                                                            'EUR' AS "IdMoneda",
                                                            null AS "IdCuentaInversion",
                                                            null AS "Nombre",
                                                            null AS "CuentaInversionPrincipal",
                                                            'I' AS "Accion"
                                                       FROM dummy;
                       END IF;
                       
                    CALL "BSCL.CuentaInversion.BD.Servicios::GestionCuentaInversion" (  
                         p_IdEmpresa                => :p_IdEmpresa
                        ,p_IdUsuario                => :p_IdUsuario
                        ,p_IdModulo                 => :p_IdModulo
                        ,p_UsaReglasNegocio         => 'S'
                        ,p_CuentaInversion          => :v_CuentaInversion   
                        ,p_out_ResultadoProceso     => :v_ResultadoProceso
                        ,p_out_InformacionContador  => :v_out_InformacionContadorCtaInvPeso
                        ,p_out_InformacionDetalle   => :v_out_InformacionDetalleCtaInvPeso
                        ,p_out_AdvertenciasContador => :v_out_AdvertenciasContadorCtaInvPeso
                        ,p_out_AdvertenciasDetalle  => :v_out_AdvertenciasDetalleCtaInvPeso
                        ,p_out_ErroresContador      => :v_out_ErroresContadorCtaInvPeso
                        ,p_out_ErroresDetalle       => :v_out_ErroresDetalleCtaInvPeso
                    );
                END IF;
                
                --Primer Portafolio se crea cuenta por defecto
                IF v_countPortafoliosVigentes = 0 THEN
          
					SELECT COUNT("IdCuentaBanco") INTO v_countIdCuentaBanco
					  FROM "BSCL.Tesoreria.BD::TES.CuentaBanco"
					 WHERE "Default" = 'S' AND "IdMoneda" = :v_MonedaSistema; 
					
					IF (v_countIdCuentaBanco > 0) THEN
						-- evita que ingrese mas de una cuenta por defecto
						SELECT TOP 1 "IdCuentaBanco" INTO v_IdCuentaBanco
						  FROM "BSCL.Tesoreria.BD::TES.CuentaBanco"
						 WHERE "Default" = 'S' AND "IdMoneda" = :v_MonedaSistema
						 ORDER BY "APV" DESC;
						
	                    insert into "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio"
	                    (                   
	                        "IdPreferenciaPagoPortafolio",
	                        "IdPortafolio",
	                        "IdPreferenciaPago",
	                        "IdCuentaBancaria",
	                        "IdTarjetaBancaria",
	                        "FechaCreacion",
	                        "FechaModificacion",
	                        "IdUsuarioCreador",
	                        "IdEstado",
	                        "IdEmpresa",            
	                        "FechaIngreso",                  
	                        "IdEstadoAutorizacion",
	                        "PorDefecto"
	                        ) 
	                        SELECT "BSCL.Portafolio.BD.Secuencias::PreferenciaPagoPortafolio".NEXTVAL,
	                        :p_out_RefId,
	                        PRE."IdPreferenciaPago",
	                        v_IdCuentaBanco,
	                        0,
	                        CURRENT_UTCTIMESTAMP, --p_Creacion, -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
	                        CURRENT_UTCTIMESTAMP,
	                        p_IdUsuario,
	                        'ACT',
	                        p_IdEmpresa,
	                        v_FechaIngreso,
	                        'AUT',
	                        'S' 
	                        FROM "BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago" PRE
	                        WHERE PRE."IdMedioDePago" = 'TRA' AND PRE."IdMoneda" = :v_MonedaSistema AND PRE."RecibePagaPortafolio" = 'P'
	                        AND PRE."IdTipoProducto" NOT IN ('MEME','MERF','MEFW');						
					END IF;
                END IF;
                
                
              SELECT  PCOM."IdTipoCliente"
                INTO 
                   v_ValCreaPrefPagoVenComTer
                FROM 
                    (SELECT
						    IFNULL(
						        ATR."ValorIndicador",
						        TPO."ValorIndicadorDefecto"
						    ) as "IdTipoCliente"
						FROM
						    "BSCL.Plataforma.BD::IND.TipoIndicador" TPO
						    LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR ON (
						        ATR."IdTipoIndicador" = TPO."IdTipoIndicador"
						        AND ATR."IdEntidad" = 0
						        AND ATR."ModeloCliente" = (
						            SELECT
						                Cliente."IdTipoCliente"
						            FROM
						                "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente
						                INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" P ON Cliente."IdCliente" = P."IdCliente"
						                AND P."IdPortafolio" = :p_out_RefId
						                INNER JOIN BSCL."BSCL.Plataforma.BD::IND.TipoIndicador" TI ON (
						                    TI."CodigoTipoIndicador" = 'GEN_OCCINV'
						                    AND TI."IdTipoEntidad" = 'PORTAFOLIO'
						                    AND TI."IdUso" = 'PRE'
						                    AND TI."IdEmpresa" = :p_IdEmpresa
						                )
						            WHERE
						                P."IdPortafolio" = :p_out_RefId
						        )
						    )
						WHERE
						    TPO."IdTipoEntidad" = 'PORTAFOLIO'
						    AND TPO."CodigoTipoIndicador" IN ('GEN_OCCINV')
						    AND TPO."IdUso" = 'PRE') PCOM;
            
IF v_ValCreaPrefPagoVenComTer = 'S' THEN

		--hacemos el insert
		insert into
		    "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" (
		        "IdPreferenciaPagoPortafolio",
		        "IdPortafolio",
		        "IdPreferenciaPago",
		        "IdCuentaBancaria",
		        "IdTarjetaBancaria",
		        "FechaCreacion",
		        "FechaModificacion",
		        "IdUsuarioCreador",
		        "IdEstado",
		        "IdEmpresa",
		        "FechaIngreso",
		        "IdEstadoAutorizacion",
		        "PorDefecto"
		    )
		SELECT
		    "BSCL.Portafolio.BD.Secuencias::PreferenciaPagoPortafolio".NEXTVAL,
		    :p_out_RefId,
		    PRE."IdPreferenciaPago",
		    0,
		    0,
		    CURRENT_UTCTIMESTAMP,
		    --p_Creacion, -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		    CURRENT_UTCTIMESTAMP,
		    p_IdUsuario,
		    'ACT',
		    p_IdEmpresa,
		    v_FechaIngreso,
		    'AUT',
		    'S'
		FROM
		    "BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago" PRE
		WHERE
		    PRE."IdMedioDePago" = 'CIN'
		    AND PRE."IdMoneda" = :v_MonedaSistema
		    AND "IdTipoOperacion" IN ('COMTER', 'VENTER');

END IF;
            ------------------------------------------
            -- Pregunta Si requiere autorizacion
            ------------------------------------------
            V_RegistroSolicitudCambio = SELECT  * FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" WHERE 1=2;
            
            call "BSCL.Plataforma.BD.Servicios::AUT.RequiereEventoAutorizacion" (
            :p_IdEmpresa,
            :p_IdUsuario,
            :p_IdModulo,
            :V_RegistroSolicitudCambio,
            :v_IdConcepto,
            :v_RequiereAutOrigen
            );

            v_RequiereAutorizacion = :v_RequiereAutOrigen;
            
            IF(:vCountEstadosMatrizAgrupador > 0) THEN
                v_RequiereAutorizacion ='N';
            END IF;
            
            
        IF (p_UsaReglasNegocio != 'N' and :vCountEstadosMatrizAgrupador = 0 and p_PropositoPortafolio = 'PROPOSITOAPV') then
            
            v_RequiereAutorizacion = 'S'; 
		Else 
			v_RequiereAutOrigen ='N';
			
        end if; 
                
                
            v_out_AuthAdvertenciasDetalle   = select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
            v_out_AuthErroresDetalle        = select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;           
                            
            if v_RequiereAutorizacion = 'S' and p_CodigoRegimen != 'BF' then
                    ------------------------------------------
                    --Genera Evento de Autorizacion
                    ------------------------------------------
        
                        DECLARE  v_Glosa        NVARCHAR(255);
                        DECLARE v_out_idAuth        bigint;
                        
                        /*
                        SELECT "DocumentoIdentidad" || '/ CodPort:' || :v_CodigoPortafolio  into  v_Glosa from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" WHERE "IdCliente"=p_IdCliente;       
                        */
                        IF(:v_ProductoAgrupador != '') THEN
                            v_CodigoPortafolio = :v_ProductoAgrupador; 
                        END IF;
                        
                        SELECT CASE "Tipo" 
                                WHEN 'J' THEN "DocumentoIdentidad" || ' / ' || "RazonSocial" || ' / CodPort:' || :v_CodigoPortafolio 
                                ELSE "DocumentoIdentidad" || ' / ' || "Nombres" || ' ' || "ApellidoPaterno" || ' ' || "ApellidoMaterno" || ' / CodPort:' || :v_CodigoPortafolio 
                               END AS "Nom" INTO v_Glosa 
                        FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" WHERE "IdCliente"=p_IdCliente;
                        
                                CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
                                    p_IdEmpresa => p_IdEmpresa,
                                    p_IdUsuario => p_IdUsuario,
                                    p_IdModulo => p_IdModulo,
                                    p_UsaReglasNegocio => 'N',                              
                                    p_IdTipoEvento => v_IdConcepto,--'NUEDOC',
                                    p_IdModuloSolicita => p_IdModulo,
                                    p_IdUsuarioSolicita => p_IdUsuario,
                                    p_Fecha => TO_VARCHAR (v_FechaIngreso, 'YYYY-MM-DD'), 
                                    p_IdConcepto => v_IdConcepto,
                                    p_NumeroOperacionConcepto =>  CAST (v_IdPortafolio AS NVARCHAR(40)),--v_secuencia_IdSolicitudCambio,
                                    p_FechaOperacionConcepto => TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD'),
                                    p_IdCliente => p_IdCliente, --v_IdCliente,
                                    p_Glosa => v_Glosa,
                                    --salida
                                    p_out_IdEvento => v_out_idAuth,
                                    p_out_AdvertenciasContador => v_out_AuthAdvertenciasContador,
                                    p_out_AdvertenciasDetalle =>  v_out_AuthAdvertenciasDetalle,
                                    p_out_ErroresContador =>  v_out_AuthErroresContador,
                                    p_out_ErroresDetalle =>  v_out_AuthErroresDetalle);     
                            
                     --p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AuthAdvertenciasContador;   
                     --v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
                     --CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('REQUIERE_AUTORIZACION_PORTAFOLIO',:v_hash,:v_erroroutmsg);
                                         
                     v_out_AdvertenciasDetalle = Select "Codigo","RequiereAutorizacion", "Descripcion" 
                                                 From :v_out_AuthAdvertenciasDetalle Union All 
                                                 Select "Codigo","RequiereAutorizacion", "Descripcion"
                                                 From :v_out_AdvertenciasDetalle UNION ALL
                                                 select 'REQUIERE_AUTORIZACION_PORTAFOLIO' as "Codigo",'S' as "RequiereAutorizacion",'' as "Descripcion" 
                                                 from dummy;
                                                 
                                        
                                                                     
                     v_out_ErroresDetalle = Select "Codigo", "Descripcion" 
                                            From :v_out_AuthErroresDetalle Union All 
                                            Select "Codigo", "Descripcion" 
                                            From :v_out_ErroresDetalle; 
                                                             
                                                                 
                    Else    --Confirma la creacion y cambia el estado del documento

                        ---------------------------------------------------------------------------------------------------------------------
                        --+ Pronet IT 
                        --+ Valida si es un Portafolio APV el que cambiara de estado, debe cumplir una de estas condiciones:
                        --+ 1.- Si ya existe un portafolio del mismo grupo con una solicitud de autorizacion => No debe cambiar de estado 
                        --+ 2.- Si exite un portafolio del mismo grupo en cualquier estado => No debe cambiar de estado   
                        --+ 3.- Si y solo si es el primer portafolio el que viene ingresando => Debe entrar a cambiar el estado
                        --+ 4.- En caso de que no sea APV el vCountEstadosMatrizAgrupador = 0
                        ---------------------------------------------------------------------------------------------------------------------
            
                        IF(:v_RequiereAutOrigen = 'N' AND :vCountEstadosMatrizAgrupador = 0) THEN
						
                        	IF(:p_DesdeEnrolamiento = 'N') THEN
								INSERT INTO BSCL."BSCL.SocioNegocio.BD::CLI.LogGenerarContratos"
								("Idlog","CodigoFormulario","CampoBackend","IdEmpresa", "IdCliente", "IdPersona", "IdUsuario", "FechaSistema", "IdPrograma", "Estado", "Mensaje")
								SELECT "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos".NEXTVAL, 'PORTAFOLIO_', 'Creado', :p_IdEmpresa, :p_IdCliente, 0, :p_IdUsuario, LEFT(:v_FechaIngreso,10) , 0, 'PEN', 'Cliente - CrearPortafolio.sql' FROM dummy;
                        	END IF;						
						
                            v_IdEstadoAutorizacion:='ING';
                            CALL "BSCL.Portafolio.BD.Servicios::ConfirmarCrearPortafolio"(
                                p_IdEmpresa,
                                p_IdUsuario,
                                p_IdModulo,
                                p_UsaReglasNegocio,
                                v_IdPortafolio,
                                v_IdEstadoAutorizacion,
                                v_out_RefId,
                                v_out_AuthAdvertenciasContador,
                                v_out_AuthAdvertenciasDetalle,
                                v_out_AuthErroresContador,
                                v_out_AuthErroresDetalle);
                     END IF;
                               
                               

                        
                        IF(:vCountEstadosMatrizAgrupador > 0) THEN
                            
                            CALL "BSCL.APV.BD.Servicios::ReplicaPortafolioAPV" (
                                :p_IdEmpresa,
                                :p_IdUsuario,
                                :p_IdModulo,
                                :p_UsaReglasNegocio,
                                vIdPortafolioGrupoAPV, 
                                vIdPortafolioGrupoAPV, --IdEntidad
                                v_TipoEntidadReplica, 
                                v_TipoOperacion,
                                v_out_AdvertenciasContador,
                                v_out_AdvertenciasDetalle,
                                v_out_ErroresContador,
                                v_out_ErroresDetalle
                            );
                        END IF;
                        ---------------------------------------------------------------------------------------------------------------------
                        --+ Fin
                        ---------------------------------------------------------------------------------------------------------------------
                                         
                         v_out_AdvertenciasDetalle = Select "Codigo","RequiereAutorizacion", "Descripcion" 
                                                     From :v_out_AuthAdvertenciasDetalle Union All 
                                                     Select "Codigo","RequiereAutorizacion", "Descripcion"
                                                     From :v_out_AdvertenciasDetalle;  
                                                                          
                         v_out_ErroresDetalle = Select "Codigo", "Descripcion" 
                                                     From :v_out_AuthErroresDetalle Union All 
                                                     Select "Codigo", "Descripcion" 
                                                     From :v_out_ErroresDetalle; 
                                    
                                
                end if;--fin evento de autorizacion
            END IF;
           
           --actualizar idPortafolioDefecto del cliente
	   IF (p_out_RefId > 0) THEN
				UPDATE "BSCL.SocioNegocio.BD::CLI.Cliente"
                	SET "IdPortafolioDefecto" = :p_out_RefId
                WHERE "IdCliente" = :p_IdCliente;
               
               --INICIO Log de Auditoria
               BEGIN 

					SELECT  
						COALESCE("CLI"."NombreCompleto",'')							AS "NombreComppleto",
						COALESCE("CLI"."DocumentoIdentidad",'')						AS "DocumentoIdentidad",
						COALESCE("CLI"."TipoDocumentoIdentidad",'')					AS "TipoDocumentoIdentidad",
						COALESCE("POR"."CodigoPortafolio",'')						AS "CodigoPortafolio",
						COALESCE("POR"."DescripcionPortafolio",'')					AS "DescripcionPortafolio",
						COALESCE("PROPOS"."Descripcion", '')						AS "PropositoPortafolio",
						COALESCE("POR"."IdPerfilComercialBaseOrigen",'')			AS "PerfilComercialBase",
						COALESCE("POR"."EstadoPortafolioAutorizacion",'')			AS "EstadoPortafolioAutorizacion"	 
					INTO
						v_nombreCliente,
						v_documentoIdentidad,
						v_tipoDocumentoIdentidad,
						v_CodigoPortafolio,
						v_DescripcionPortafolio,
						v_proposito,
						v_perfilComercialBase,
						v_estadoAutorizacionPortafolioLargo
							FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 					AS "POR" --WHERE "IdCliente" =1426707; 
							LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 			AS "CLI"
								ON("POR"."IdCliente" = "CLI"."IdCliente")
							LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPropositoPortafolio"		AS "PROPOS"
								ON("POR"."Proposito" = "PROPOS"."IdPropositoPortafolio")
							WHERE "POR"."IdPortafolio" = :p_out_RefId;

					V_subregistro:= v_TipoEntidad;
					
					IF(v_IdEstadoAutorizacion <> 'ING') THEN
							v_NombreProceso:= :v_estadoAutorizacionPortafolioLargo||' '||:v_NombreProceso;
					END IF;
					
					p_DetalleLog = (SELECT v_subregistro AS "SubRegistro", 'CodigoPortafolio' AS "CampoRegistro", '' AS "ValorAntiguo", v_CodigoPortafolio AS "ValorNuevo"
										FROM dummy
									UNION ALL 
									SELECT v_subregistro AS "SubRegistro", 'DescripcionPortafolio' AS "CampoRegistro", '' AS "ValorAntiguo", v_DescripcionPortafolio AS "ValorNuevo"
										FROM dummy
									UNION ALL
									SELECT v_subregistro AS "SubRegistro", 'Proposito' AS "CampoRegistro", '' AS "ValorAntiguo", v_proposito AS "ValorNuevo"
										FROM dummy
									UNION ALL
									SELECT v_subregistro AS "SubRegistro", 'IdPerfilComercialBaseOrigen' AS "CampoRegistro", '' AS "ValorAntiguo", v_perfilComercialBase AS "ValorNuevo"
										FROM dummy);
									
					CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
						p_IdEmpresa => p_IdEmpresa,
						p_out_FechaHoraSistema => v_fechaHoraSistema
					);

		           	CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
						p_IdTipoLog => 'INGRESO' 
						,p_IdModulo => 'SOC' 
						,p_IdUsuario => p_IdUsuario 
						,p_IdEmpresa => p_IdEmpresa 
						,p_FechaSistemaLog => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD')
						,p_IdUsuarioAtendido => p_IdUsuario
						,p_IdCliente => :p_IdCliente
						,p_NombreCliente => :v_nombreCliente
						,p_IdDocumentoIdentidad => :v_documentoIdentidad
						,p_IdTipoDocumentoIdentidad => :v_tipoDocumentoIdentidad
						,p_IdTipoRegistro => :v_TipoEntidad--'PORTAFOLIO'
						,p_DetalleLog => :p_DetalleLog	-- hacia arriba obligatorios
						,p_NombreProceso => :v_NombreProceso-- hacia abajo opcionales
						,p_IdRegistro => :p_out_RefId
						,p_EstadoProceso => :v_IdEstadoAutorizacion
					);
	           END;
	          --FIN Log de Auditoria
           END IF;
        END;

        
        
        v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
                                '' as "CodigoDato",
                                '' as "ValorDato" 
                                FROM :v_out_ErroresDetalle
                                UNION ALL
                                SELECT "Codigo" AS "CodigoMensaje",
                                '' as "CodigoDato",
                                '' as "ValorDato" 
                                FROM :v_out_AdvertenciasDetalle;
          
        CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
        
                        
        p_out_ErroresDetalle =  SELECT  errores."Codigo",
                                    --errores."Descripcion",
                                    msj."Descripcion"
                                FROM :v_out_ErroresDetalle errores
                                INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";
                                
        p_out_AdvertenciasDetalle = SELECT  adv."Codigo",
                                    adv."RequiereAutorizacion",
                                    --adv."Descripcion",
                                    msj."Descripcion"
                                FROM :v_out_AdvertenciasDetalle adv
                                INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";
        
        SIGNAL SALIDA_CONTROLADA;   
        --------------------
        --FIN SP 
        --------------------
    END;    
END;
