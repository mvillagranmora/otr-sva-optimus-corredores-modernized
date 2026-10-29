SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1874.GrabarDetalle";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1874.GrabarDetalle"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    IN p_IdEmpresa           BIGINT   ,
    -- ID usuario del login.
    IN p_IdUsuario           BIGINT   ,
    -- ID modulo invocador.
    IN p_IdModulo           NVARCHAR(3)  ,
    ------------ Parametros de Entrada Propios ----------
    -- ID del detalle.
    -- Si el id es mayor que cero, se asume que es un registro existente para actualizar.
    -- En caso contrario, se intenta crear un nuevo registro.
    IN p_ID            BIGINT   ,
    -- ID del documento.
    IN p_IdDocumento          BIGINT   ,
    -- VoBo del usuario sobre el registro (S/N).
    IN p_VboUsuario          CHAR(1)   ,
    -- Observacion ingresada por usuario que edito el registro.
    IN p_Observaciones          NVARCHAR(250),
    -- Fuente de los datos.
    IN p_FuenteDatos          NVARCHAR(40) ,
    IN p_IdCliente           BIGINT   ,
    IN p_IdPortafolio          BIGINT   ,
 IN p_Numero           BIGINT       ,
 IN p_RutEmisor          NVARCHAR(40) ,
 IN p_RutInversionista        NVARCHAR(40) ,
 IN p_TipoContribuyente        NVARCHAR(100),
 IN p_NemotecnicoSVS         NVARCHAR(40) ,
 in p_Nemotecnico          NVARCHAR(40) ,
 IN p_FechaOperacion         DATE         ,
 IN p_FechaAdquisicion        DATE   ,
 IN p_MonedaUnidadReajuste       NVARCHAR(10) ,
 IN p_NumeroCuentaDCV        NVARCHAR(100),
 IN p_MovimientoSaldoInicial       DECIMAL(26,8),
 IN p_MovimientoAdquision       DECIMAL(26,8),
 IN p_MovimientoEnajenacion       DECIMAL(26,8),
 IN p_MovimientoRescateAnticipado     DECIMAL(26,8),
 IN p_MovimientoSaldoFinal       DECIMAL(26,8),
 IN p_SaldoFinalNominal        DECIMAL(26,8),
 IN p_InteresDevengadoMonedaUnidadReajuste   DECIMAL(26,8),
 IN p_InteresDevengadoPesos       DECIMAL(26,8),
 IN p_ResultadoEnajenacionMonedaUnidadReajuste  DECIMAL(26,8),
 IN p_ResultadoEnajenacionPesos      DECIMAL(26,8),
 IN p_RetencionMonedaUnidadReajuste     DECIMAL(26,8),
 IN p_RetencionPesos         DECIMAL(26,8),
 IN p_RetencionMonedaUnidadReajuste4porciento  DECIMAL(26,8),
 IN p_RetencionPesos4porciento      DECIMAL(26,8),
    IN p_TipoDocumento          NVARCHAR(10) ,
    IN p_NumeroDocumento         BIGINT   ,
    IN p_FechaDocumento         DATE   ,
    IN p_IdBaseDocumento         BIGINT   ,
    IN p_TipoTransaccion         NVARCHAR(6)  ,
    IN p_IdTipoOperacion         NVARCHAR(6)  ,
    IN p_IdTipoProducto         NVARCHAR(4)  ,


    ------------ Parametros de Salida Propios ----------
    -- ID del registro.
    OUT p_out_id           BIGINT   ,

    ------------ Salidas Advertencias ----------

    -- Contador de advertencias.
    OUT p_out_AdvertenciasContador       INT    ,

    -- Tabla de advertencias.
    OUT p_out_AdvertenciasDetalle TABLE("Codigo" NVARCHAR(40), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)),

    ------------ Salidas Errores ----------

    -- Contador de errores
    -- Importante: Debe ser cero para que se haga commit.
    OUT p_out_ErroresContador        INT,

    -- Tabla de errores.
    OUT p_out_ErroresDetalle TABLE("Codigo" NVARCHAR(40), "Descripcion" NVARCHAR(5000))
)
    LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
    ------------ Variables Propias ----------
 DECLARE v_erroroutmsg   nvarchar(520);
 
 ------------ Variables validaciones -----------

 DECLARE v_NemoEsART104 BOOLEAN DEFAULT TRUE;

    ------------ Bloque Comun de Inicializacion ----------

    DECLARE salida_controlada condition;
    DECLARE exit handler FOR salida_controlada
    BEGIN
        SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
        SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
    END;

    DECLARE exit handler FOR sqlexception
    BEGIN
        DECLARE v_errorExt NVARCHAR(40);
        IF ::sql_error_code > 10000 THEN
            -- Error pseudo-controlado pero desde un sub-sp.
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError"(::sql_error_code, v_errorExt);
            p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL
                                   SELECT v_errorExt, ::sql_error_message FROM dummy;
            SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
        ELSE
            -- Error interno no manejado (el log sera captado por la capa de XS control).
            RESIGNAL;
        END IF;
    END;

    p_out_AdvertenciasContador := 0;
    p_out_ErroresContador := 0;
    p_out_AdvertenciasDetalle = SELECT '' AS "Codigo", '' AS "RequiereAutorizacion", '' AS "Descripcion" FROM dummy WHERE 1 = 2;
    p_out_ErroresDetalle = SELECT '' AS "Codigo", '' AS "Descripcion" FROM dummy WHERE 1 = 2;
   
    ----------- VALIDACION ART 104 ---------------
   
   IF p_NemotecnicoSVS <> '' THEN
     SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END INTO v_NemoEsART104 FROM "BSCL.Normativa.BD::TRI.InstrumentosArticulo104" WHERE "Instrumento" = p_NemotecnicoSVS;
     IF NOT v_NemoEsART104 = TRUE THEN
         p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
             v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
             CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_NEMO_NO_ES_ART104', :v_hash, :v_erroroutmsg);
             p_out_AdvertenciasDetalle =
                 SELECT 'UI_NEMO_NO_ES_ART104' AS "Codigo",
                     'N' AS "RequiereAutorizacion",
                     CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
                 FROM dummy
                 UNION ALL
                 SELECT * FROM :p_out_AdvertenciasDetalle;
 
             SIGNAL salida_controlada;
      END IF;
    END IF;
   
    --------- FIN VALIDACIONES ART 104 -----------
   
   
   
    ------------ Logica de Actualizacion ----------
 
    IF p_ID > 0 THEN
        p_out_id := p_ID;

        UPDATE "BSCL.Normativa.BD::TRI.DatosDJ1874" SET
            "IdDocumento"          =  p_IdDocumento
   ,"VboUsuario"          =  p_VboUsuario
   ,"Observaciones"         = (CASE WHEN LENGTH(TRIM(:p_Observaciones)) BETWEEN 4 AND 5 AND LEFT(UPPER(TRIM(:p_Observaciones)), 1) = 'H' THEN "Observaciones" ELSE :p_Observaciones END)
   ,"FuenteDatos"          =  p_FuenteDatos
   ,"Numero"          = p_Numero
   ,"RutEmisor"         = p_RutEmisor
   ,"RutInversionista"        = p_RutInversionista
   ,"TipoContribuyente"       = p_TipoContribuyente
   ,"NemotecnicoSVS"        = p_NemotecnicoSVS
   ,"Nemotecnico"         = p_NemotecnicoSVS
   ,"FechaOperacion"        = p_FechaOperacion
   ,"FechaAdquisicion"        = p_FechaAdquisicion
   ,"MonedaUnidadReajuste"       = p_MonedaUnidadReajuste
   ,"NumeroCuentaDCV"        = p_NumeroCuentaDCV
   ,"MovimientoSaldoInicial"      = p_MovimientoSaldoInicial
   ,"MovimientoAdquision"       = p_MovimientoAdquision
   ,"MovimientoEnajenacion"      = p_MovimientoEnajenacion
   ,"MovimientoRescateAnticipado"     = p_MovimientoRescateAnticipado
   ,"MovimientoSaldoFinal"       = p_MovimientoSaldoFinal
   ,"SaldoFinalNominal"       = p_SaldoFinalNominal
   ,"InteresDevengadoMonedaUnidadReajuste"   = p_InteresDevengadoMonedaUnidadReajuste
   ,"InteresDevengadoPesos"      = p_InteresDevengadoPesos
   ,"ResultadoEnajenacionMonedaUnidadReajuste"  = p_ResultadoEnajenacionMonedaUnidadReajuste
   ,"ResultadoEnajenacionPesos"     = p_ResultadoEnajenacionPesos
   ,"RetencionMonedaUnidadReajuste"    = p_RetencionMonedaUnidadReajuste
   ,"RetencionPesos"        = p_RetencionPesos
   ,"RetencionMonedaUnidadReajuste4porciento"  = p_RetencionMonedaUnidadReajuste4porciento
   ,"RetencionPesos4porciento"      = p_RetencionPesos4porciento
   ,"TipoDocumento"         =  p_TipoDocumento
   ,"NumeroDocumento"         =  p_NumeroDocumento
   ,"FechaDocumento"         =  p_FechaDocumento
   ,"IdBaseDocumento"         =  p_IdBaseDocumento
   ,"IdCliente"          =  p_IdCliente
   ,"IdPortafolio"         =  p_IdPortafolio
   ,"TipoTransaccion"         =  p_TipoTransaccion
   ,"IdTipoOperacion"         =  p_IdTipoOperacion
   ,"IdTipoProducto"         =  p_IdTipoProducto
        WHERE "ID" = p_ID;
    ELSE
        SELECT IFNULL(MAX("ID"),0)+1 INTO p_out_id FROM "BSCL.Normativa.BD::TRI.DatosDJ1874";

        insert into "BSCL.Normativa.BD::TRI.DatosDJ1874"(
            "ID"
   ,"IdDocumento"
   ,"VboUsuario" 
   ,"Observaciones"
   ,"FuenteDatos"     
   ,"Numero"         
   ,"RutEmisor"        
   ,"RutInversionista"       
   ,"TipoContribuyente"      
   ,"NemotecnicoSVS"
   ,"Nemotecnico"              
   ,"FechaOperacion"
   ,"FechaAdquisicion"      
   ,"MonedaUnidadReajuste"      
   ,"NumeroCuentaDCV"       
   ,"MovimientoSaldoInicial"     
   ,"MovimientoAdquision"      
   ,"MovimientoEnajenacion"     
   ,"MovimientoRescateAnticipado"    
   ,"MovimientoSaldoFinal"      
   ,"SaldoFinalNominal"      
   ,"InteresDevengadoMonedaUnidadReajuste"  
   ,"InteresDevengadoPesos"     
   ,"ResultadoEnajenacionMonedaUnidadReajuste" 
   ,"ResultadoEnajenacionPesos"    
   ,"RetencionMonedaUnidadReajuste"   
   ,"RetencionPesos"
   ,"RetencionMonedaUnidadReajuste4porciento"
   ,"RetencionPesos4porciento"
   ,"TipoDocumento" 
   ,"NumeroDocumento" 
   ,"FechaDocumento" 
   ,"IdBaseDocumento" 
   ,"IdCliente" 
   ,"IdPortafolio" 
   ,"TipoTransaccion" 
   ,"IdTipoOperacion" 
   ,"IdTipoProducto"
        ) values (
            p_out_id
            ,p_IdDocumento
   ,'N'
            ,''
            ,'MANTENEDOR'
   ,COALESCE(p_Numero, 0)
   ,p_RutEmisor
   ,p_RutInversionista
   ,p_TipoContribuyente
   ,p_NemotecnicoSVS
   ,p_NemotecnicoSVS 
   ,p_FechaOperacion
   ,p_FechaAdquisicion
   ,p_MonedaUnidadReajuste
   ,p_NumeroCuentaDCV
   ,p_MovimientoSaldoInicial
   ,p_MovimientoAdquision
   ,p_MovimientoEnajenacion
   ,p_MovimientoRescateAnticipado
   ,p_MovimientoSaldoFinal
   ,p_SaldoFinalNominal
   ,p_InteresDevengadoMonedaUnidadReajuste
   ,p_InteresDevengadoPesos
   ,p_ResultadoEnajenacionMonedaUnidadReajuste
   ,p_ResultadoEnajenacionPesos
   ,p_RetencionMonedaUnidadReajuste
   ,p_RetencionPesos
   ,p_RetencionMonedaUnidadReajuste4porciento
   ,p_RetencionPesos4porciento
   ,COALESCE(p_TipoDocumento,0)
   ,p_NumeroDocumento
   ,p_FechaDocumento
   ,p_IdBaseDocumento
   ,p_IdCliente
   ,p_IdPortafolio
   ,p_TipoTransaccion
   ,p_IdTipoOperacion
   ,p_IdTipoProducto
        );
    END IF;
END;