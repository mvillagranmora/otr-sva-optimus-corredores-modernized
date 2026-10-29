SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1.EliminarDetalles";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1.EliminarDetalles"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    IN p_IdEmpresa BIGINT,

    -- ID usuario del login.
    IN p_IdUsuario BIGINT,

    -- ID modulo invocador.
    IN p_IdModulo NVARCHAR(3),

    ------------ Parametros de Entrada Propios ----------

    -- ID del documento.
    IN p_IdDocumento BIGINT DEFAULT NULL,

    -- Lista de los ID's de los detalles a eliminar.
    IN p_List TABLE("ID" BIGINT),

    ------------ Parametros de Salida Propios ----------

    ------------ Salidas Advertencias ----------

    -- Contador de advertencias.
    OUT p_out_AdvertenciasContador INT,

    -- Tabla de advertencias.
    OUT p_out_AdvertenciasDetalle TABLE("Codigo" NVARCHAR(40), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)),

    ------------ Salidas Errores ----------

    -- Contador de errores
    -- Importante: Debe ser cero para que se haga commit.
    OUT p_out_ErroresContador INT,

    -- Tabla de errores.
    OUT p_out_ErroresDetalle TABLE("Codigo" NVARCHAR(40), "Descripcion" NVARCHAR(5000))
)
    language sqlscript
    sql security invoker
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
    ------------ Variables Propias ----------
    DECLARE v_i INT;
    DECLARE v_arr_ids TABLE("ID" BIGINT);

    ------------ Bloque Comun de Inicializacion ----------

    DECLARE salida_controlada condition;
    DECLARE exit handler for salida_controlada
    BEGIN
        SELECT count(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
        SELECT count(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
    END;

    DECLARE exit handler FOR sqlexception
    BEGIN
        DECLARE v_errorExt NVARCHAR(40);
        IF ::sql_error_code > 10000 THEN
            -- Error pseudo-controlado pero desde un sub-sp.
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError"(::sql_error_code, v_errorExt);
            p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL
                                   SELECT v_errorExt, ::sql_error_message FROM dummy;
            SELECT count(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
        ELSE
            -- Error interno no manejado (el log sera captado por la capa de XS control).
            resignal;
        END IF;
    END;

    p_out_AdvertenciasContador := 0;
    p_out_ErroresContador := 0;
    p_out_AdvertenciasDetalle = SELECT '' AS "Codigo", '' AS "RequiereAutorizacion", '' AS "Descripcion" FROM dummy WHERE 1 = 2;
    p_out_ErroresDetalle = SELECT '' AS "Codigo", '' AS "Descripcion" FROM dummy WHERE 1 = 2;

    ------------ Logica de Actualizacion ----------
    IF p_IdDocumento IS NOT NULL THEN
        DELETE FROM "BSCL.Normativa.BD::TRI.DatosDJ1" WHERE "IdDocumento" = :p_IdDocumento;
    ELSE
        v_arr_ids = SELECT t1."ID"
	    FROM "BSCL.Normativa.BD::TRI.DatosDJ1" t1
	    JOIN (
	    	SELECT DISTINCT "KIdContrato"
	        FROM "BSCL.Normativa.BD::TRI.DatosDJ1" t3
	        WHERE EXISTS ( SELECT "ID" FROM :p_List AS t4 WHERE t3."ID" = t4."ID" )
	    ) t2 ON t1."KIdContrato" = t2."KIdContrato";
	    
	    DELETE FROM "BSCL.Normativa.BD::TRI.DatosDJ1" t1 WHERE EXISTS ( SELECT 1 FROM :v_arr_ids t2 WHERE t1."ID" = t2."ID" );
    END IF;
END;
