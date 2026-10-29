SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1874.ConsultarValorizacionDJ1874";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1874.ConsultarValorizacionDJ1874"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    IN p_IdEmpresa 										BIGINT		 ,
    -- ID usuario del login.
    IN p_IdUsuario 										BIGINT		 ,
    -- ID modulo invocador.
    IN p_IdModulo 										NVARCHAR(3)	 ,
    ------------ Parametros de Entrada Propios ----------
    -- ID del detalle.
    IN p_ID 											BIGINT		 ,
    IN p_Version 										NVARCHAR(40) ,
	


    ------------ Parametros de Salida Propios ----------
    -- ID del registro.
    OUT p_out_id 										BIGINT		 ,
    OUT p_out_ValResultadoTotal							INT		     ,
    OUT p_out_ValResultado 								INT		     ,

    ------------ Salidas Advertencias ----------

    -- Contador de advertencias.
    OUT p_out_AdvertenciasContador 						INT			 ,

    -- Tabla de advertencias.
    OUT p_out_AdvertenciasDetalle TABLE("Codigo" NVARCHAR(40), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)),

    ------------ Salidas Errores ----------

    -- Contador de errores
    -- Importante: Debe ser cero para que se haga commit.
    OUT p_out_ErroresContador 							INT,

    -- Tabla de errores.
    OUT p_out_ErroresDetalle TABLE("Codigo" NVARCHAR(40), "Descripcion" NVARCHAR(5000))
)
    LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
    ------------ Variables Propias ----------

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

    ------------ Logica de Actualizacion ----------


    SELECT COUNT(1) "IdCliente" INTO p_out_ValResultadoTotal FROM "BSCL.Normativa.BD::TRI.DatosDJ1874" WHERE "ValResultado" = 'OK' AND YEAR("FechaOperacion") = p_Version;


    IF p_out_ValResultadoTotal > 0 THEN
        p_out_ValResultado := 1; 
    END IF;

	IF p_out_ValResultadoTotal = 0 THEN
        p_out_ValResultado := 0;   
    END IF;

END;