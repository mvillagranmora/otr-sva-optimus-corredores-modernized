SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::PLA.CrearModeloTipoCliente" ;
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio para la creacion de modelos tipo cliente
--  Autor: CHERNABDEZ - BCS
--  Fecha Creacion: 10-01-2018
-- 

"BSCL.Plataforma.BD.Servicios::PLA.CrearModeloTipoCliente" (
    -- Parametros entrada comunes en todos los servicios publicos
    IN  p_IdEmpresa                 bigint,         -- ID  Empresa destino
    IN  p_IdUsuario                 bigint,         -- ID  del usuario que solicita el servicio
    IN  p_IdModulo                  nvarchar(3),    -- ID  del Modulo que lo invoca
    IN  p_UsaReglasNegocio          nvarchar(1) DEFAULT 'S',    -- S/N se usa en caso de servicios anidados.
          
    -- Parametros entrada del servicio publico
    IN  p_IdTipoCliente             NVARCHAR(40),
    IN  p_Nombre                    NVARCHAR(40),
    IN  p_IdEstado                  NVARCHAR(3),
    IN  p_TipoEnrolamiento          NVARCHAR(40) DEFAULT 'FULL',
    IN  p_PorDefecto                NVARCHAR(1)  DEFAULT 'N',
           
    -- Salidas para mensajes de advertencias y errores
    -- Salidas para mensajes de advertencias y errores
    OUT p_out_AdvertenciasContador int,
    OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),    
    OUT p_out_ErroresContador int,
    OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
    --------------------
    --DECLARE Manejo de Errores
    --------------------    
    DECLARE SALIDA_CONTROLADA CONDITION; 
    DECLARE v_errorcuenta INT DEFAULT 0;  
    DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
    DECLARE v_erroroutmsg  nvarchar(520);               
    DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA   
    BEGIN
        --salida/error/advertencia controlado por este servicio
        select count(1) into p_out_ErroresContador 
        from :p_out_ErroresDetalle;
        select count(1) into p_out_AdvertenciasContador 
        from :p_out_AdvertenciasDetalle;                                
    END;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        declare v_errorExt nvarchar(40);
        IF ::SQL_ERROR_CODE > 10000 THEN
            --error pseudo-controlado pero desde un Sub-SP
            call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);         
            p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
                                   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;                
            select count(1) into p_out_ErroresContador 
            from :p_out_ErroresDetalle;                                     
        ELSE
            --Error interno no manejado  (El log será captado por la capa de XS control)        
            RESIGNAL; 
        END IF;     
    END;    
    p_out_AdvertenciasContador  := 0;
    p_out_ErroresContador       := 0;
    p_out_AdvertenciasDetalle   = select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
    p_out_ErroresDetalle        = select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;

--LOGICA    
    BEGIN
        DECLARE v_count BIGINT;
        
        SELECT COUNT(*) INTO v_count FROM "BSCL.SocioNegocio.BD::CLI.TipoCliente" WHERE "IdTipoCliente" = :p_IdTipoCliente AND "IdEmpresa" = :p_IdEmpresa;
        
        IF v_count > 0 THEN
            v_hash= select 'UI_MODELO_TIPO_CLIENTE_YA_EXISTE' as "Codigo", '' as "Valor" FROM DUMMY;
                CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_MODELO_TIPO_CLIENTE_YA_EXISTE',:v_hash,:v_erroroutmsg);
                p_out_ErroresDetalle =  select 'UI_MODELO_TIPO_CLIENTE_YA_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
                                        select * from :p_out_ErroresDetalle;  
           SIGNAL SALIDA_CONTROLADA;
        END IF;
       
        INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoCliente"("IdTipoCliente", "Nombre", "IdUsuarioCreador", "FechaCreacion", "IdEmpresa", "IdEstado","TipoEnrolamiento", "PorDefecto")
        VALUES(p_IdTipoCliente, p_Nombre, p_IdUsuario, CURRENT_UTCTIMESTAMP, p_IdEmpresa, p_IdEstado, p_TipoEnrolamiento, p_PorDefecto);
          
        IF (:p_PorDefecto='S') THEN
        BEGIN
            UPDATE "BSCL.SocioNegocio.BD::CLI.TipoCliente"
            SET "PorDefecto"  = 'N'
            WHERE "TipoEnrolamiento" = :p_TipoEnrolamiento
            AND "IdTipoCliente" <> :p_IdTipoCliente
            AND "IdEmpresa" = :p_IdEmpresa;				 	
            END;
        END IF;
    END;    
END;