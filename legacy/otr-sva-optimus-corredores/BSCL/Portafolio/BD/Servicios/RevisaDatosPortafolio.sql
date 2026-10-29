SET SCHEMA "BSCL";


DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::RevisaDatosPortafolio";


CREATE PROCEDURE
-- 
--  Proposito: Revision de la validez de las variables previo a la creacion del Portafolio, en caso de error se procedera a abotar la creacion de socionegocio.
--  Autor: MPIZARRO - BCS
--  Fecha Creacion: 10-10-2019
--

"BSCL.Portafolio.BD.Servicios::RevisaDatosPortafolio" (
    -- Parametros entrada comunes en todos los servicios publicos
    IN  p_IdEmpresa                     BIGINT,         --ID Empresa destino
    IN  p_IdUsuario                     BIGINT,         --ID del usuario que solicita el servicio
    IN  p_IdModulo                      NVARCHAR(3),    --ID del Modulo que lo invoca
    IN  p_UsaReglasNegocio              NVARCHAR(1),    -- S/N se usa en caso de servicios anidados.
          
    -- Parametros entrada del servicio publico
    IN  p_CodigoPortafolio              NVARCHAR(100) DEFAULT '',
    IN  p_DescripcionPortafolio         NVARCHAR(100) DEFAULT 'Portafolio Default',
    IN  p_PropositoPortafolio           NVARCHAR(520) DEFAULT 'PROPGENERALES',
    
    -- Parametros entrada del servicio para portafolio apv
    IN  p_CodigoRegimen                 NVARCHAR(10)  DEFAULT '',
    IN  p_NombrePlan                    NVARCHAR(10)  DEFAULT '',
    IN  p_CodigoAgrupador               NVARCHAR(100)  DEFAULT '',
    
    IN  p_IdCliente                     BIGINT,
    IN  p_IdPerfilComercialBase         NVARCHAR(40),
    IN  p_IdPersonaBrokerExtranjero     BIGINT DEFAULT 0,
    IN  p_Estado                        NVARCHAR(3)  DEFAULT 'ACT',
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
            DECLARE v_Alias                             NVARCHAR(100):= '';
        
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
                
                --Regla Cliente solo puede tener un plan (Activo o Abierto) que tenga un contrato SAAPV Vigente
                SELECT COUNT(Portafolio."IdPortafolio") INTO v_countPlanAhorro
                FROM "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
                INNER JOIN "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" AS Matriz
                        ON Matriz."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
                       AND Portafolio."Proposito" = Matriz."IdPropositoPortafolio"
                INNER JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" Contrato ON Contrato."IdPortafolio" = Portafolio."IdPortafolio"
                WHERE Portafolio."IdCliente" = :p_IdCliente
                AND Portafolio."Proposito" = :p_PropositoPortafolio
                --AND Matriz."Codigo" = :p_CodigoRegimen
                AND Matriz."IdPropositoPortafolio" = :p_PropositoPortafolio
                AND Matriz."NombrePlan" <> :p_NombrePlan
                AND Contrato."IdEstado" = 'VIG'
                --AND Matriz."CodigoAgrupador" <> :p_CodigoAgrupador
                ;
                
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
                v_out_ErroresDetalle =  select 'PORTAFOLIO_EXISTE' as "Codigo",'' as "Descripcion" from dummy;
                
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
