SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1871.GrabarDetalle";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1871.GrabarDetalle"
(
    ------------ Parametros de Entrada Comunes ----------
    IN p_IdEmpresa BIGINT,
    IN p_IdUsuario BIGINT,
    IN p_IdModulo NVARCHAR(3),
    IN p_IdBaseDocumento BIGINT,
    ------------ Parametros de Entrada Propios ----------
    IN p_ID BIGINT,
    IN p_IdDocumento BIGINT,
    IN p_VboUsuario CHAR(1),
    IN p_Observaciones NVARCHAR(250),
    IN p_FuenteDatos NVARCHAR(40),
    IN p_Numero BIGINT,
    IN p_RutCliente NVARCHAR(40),
    --IN p_IdCliente BIGINT,
    IN p_FechaOperacion DATE,
    IN p_AnoAhorro BIGINT,
    --Ahorros
    IN p_AhorrosTrabajador DECIMAL(26,8),
    IN p_AhorrosDeposito DECIMAL(26,8),
    IN p_AhorrosCotizaciones DECIMAL(26,8),
    --Retiros
    IN p_RetirosTrabajador DECIMAL(26,8),
    IN p_RetirosDeposito DECIMAL(26,8),
    IN p_RetirosCotizaciones DECIMAL(26,8),
    ------------ Parametros de Salida Propios ----------
    OUT p_out_id BIGINT,
    OUT p_out_AdvertenciasContador INT,
    OUT p_out_AdvertenciasDetalle TABLE("Codigo" NVARCHAR(40), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)),
    OUT p_out_ErroresContador INT,
    OUT p_out_ErroresDetalle TABLE("Codigo" NVARCHAR(40), "Descripcion" NVARCHAR(5000))
)
    LANGUAGE SQLSCRIPT
    SQL SECURITY INVOKER
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
    DECLARE salida_controlada condition;
    DECLARE exit handler for salida_controlada
    BEGIN
        SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
        SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
    END;

    DECLARE exit handler FOR SQLEXCEPTION
    BEGIN
        DECLARE v_errorExt NVARCHAR(40);
        IF ::sql_error_code > 10000 THEN
            -- Error pseudo-controlado pero desde un sub-sp.
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError"(::sql_error_code, v_errorExt);
            p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL
                                   SELECT v_errorExt, ::sql_error_message FROM DUMMY;
            SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
        ELSE
            -- Error interno no manejado (el log sera captado por la capa de XS control).
            resignal;
        END IF;
    END;

    p_out_AdvertenciasContador := 0;
    p_out_ErroresContador := 0;
    p_out_AdvertenciasDetalle = SELECT '' AS "Codigo", '' AS "RequiereAutorizacion", '' AS "Descripcion" FROM DUMMY WHERE 1 = 2;
    p_out_ErroresDetalle = SELECT '' AS "Codigo", '' AS "Descripcion" FROM DUMMY WHERE 1 = 2;

    ------------ Logica de Actualizacion ----------

    IF (:p_ID > 0) THEN
        p_out_id := p_ID;

        UPDATE "BSCL.Normativa.BD::TRI.DatosDJ1871"
        SET
            "IdDocumento" =:p_IdDocumento,
            "VboUsuario" =:p_VboUsuario,
            "Observaciones" = (CASE WHEN LENGTH(TRIM(:p_Observaciones)) BETWEEN 4 AND 5 AND LEFT(UPPER(TRIM(:p_Observaciones)), 1) = 'H' THEN "Observaciones" ELSE :p_Observaciones END),
            "FuenteDatos" =:p_FuenteDatos,
            ----------------------------------------
            "Numero" =:p_Numero,
            "RutCliente" =:p_RutCliente,
            "FechaOperacion" = :p_FechaOperacion,
            "AnoAhorro" =:p_AnoAhorro,
            --Ahorros
            "AhorrosTrabajador" =:p_AhorrosTrabajador,
            "AhorrosDeposito" =:p_AhorrosDeposito,
            "AhorrosCotizaciones" =:p_AhorrosCotizaciones,
            --Retiros
            "RetirosTrabajador" =:p_RetirosTrabajador,
            "RetirosDeposito" =:p_RetirosDeposito,
            "RetirosCotizaciones" =:p_RetirosCotizaciones 
        WHERE "ID" = :p_ID;
    ELSE
        SELECT IFNULL(MAX("ID"),0)+1 
          INTO p_out_id 
          FROM "BSCL.Normativa.BD::TRI.DatosDJ1871";
        
        INSERT INTO "BSCL.Normativa.BD::TRI.DatosDJ1871" (
            "ID","IdDocumento","VboUsuario","Observaciones","FuenteDatos",
            ----------------------------------------
            "EjecutadoComplementoIPC",
            ----------------------------------------
            "Numero","RutCliente","FechaOperacion","AnoAhorro",
            --Ahorros
            "AhorrosTrabajador","AhorrosDeposito","AhorrosCotizaciones",
            --Retiros
            "RetirosTrabajador","RetirosDeposito","RetirosCotizaciones")
         VALUES (
            :p_out_id,:p_IdDocumento,'N','','MANTENEDOR',
            ----------------------------------------
            'N',
            ----------------------------------------
            COALESCE(:p_Numero, 0),:p_RutCliente,:p_FechaOperacion,:p_AnoAhorro,
            --Ahorros
            :p_AhorrosTrabajador,:p_AhorrosDeposito,:p_AhorrosCotizaciones,
            --Retiros
            :p_RetirosTrabajador,:p_RetirosDeposito,:p_RetirosCotizaciones);
    END IF;
END;
GO
