set schema "BSCL";
GO
drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1889.GrabarDetalle";
GO
create procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1889.GrabarDetalle"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    in p_IdEmpresa bigint,

    -- ID usuario del login.
    in p_IdUsuario bigint,

    -- ID modulo invocador.
    in p_IdModulo nvarchar(3),

    IN p_IdBaseDocumento bigint,
    ------------ Parametros de Entrada Propios ----------

    -- ID del detalle.
    -- Si el id es mayor que cero, se asume que es un registro existente para actualizar.
    -- En caso contrario, se intenta crear un nuevo registro.
    in p_ID bigint,

    -- ID del documento.
    in p_IdDocumento bigint,

    -- VoBo del usuario sobre el registro (S/N).
    in p_VboUsuario char(1),

    -- Observacion ingresada por usuario que edito el registro.
    in p_Observaciones nvarchar(250),

    -- Fuente de los datos.
    in p_FuenteDatos nvarchar(40),
    -- fin comunes
    
    in p_Numero BIGINT,
    in p_RutCliente nvarchar(40),
    in p_IdCliente bigint,
    in p_FechaOperacion DATE,
    in p_TipoAhorro bigint,
    ---
    in p_MontoNominal decimal(26,8),
    --
    in p_RentabilidadPositiva decimal(26,8),
    in p_RentabilidadNegativa decimal(26,8),
    --Actualizada ($)
    IN p_Factor decimal(26,8),
    in p_RentabilidadActualizadaPositiva decimal(26,8),
    in p_RentabilidadActualizadaNegativa decimal(26,8),
    --
    in p_NumeroCertificado bigint,
    ------------ Parametros de Salida Propios ----------

    -- ID del registro.
    out p_out_id bigint,

    ------------ Salidas Advertencias ----------

    -- Contador de advertencias.
    out p_out_AdvertenciasContador int,

    -- Tabla de advertencias.
    out p_out_AdvertenciasDetalle table("Codigo" nvarchar(40), "RequiereAutorizacion" nvarchar(40), "Descripcion" nvarchar(5000)),

    ------------ Salidas Errores ----------

    -- Contador de errores
    -- Importante: Debe ser cero para que se haga commit.
    out p_out_ErroresContador int,

    -- Tabla de errores.
    out p_out_ErroresDetalle table("Codigo" nvarchar(40), "Descripcion" nvarchar(5000))
)
    language sqlscript
    sql security invoker
    default schema "BSCL"
as
begin
    ------------ Variables Propias ----------

    ------------ Bloque Comun de Inicializacion ----------

    declare salida_controlada condition;
    declare exit handler for salida_controlada
    begin
        select count(1) into p_out_AdvertenciasContador from :p_out_AdvertenciasDetalle;
        select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
    end;

    declare exit handler for sqlexception
    begin
        declare v_errorExt nvarchar(40);
        if ::sql_error_code > 10000 then
            -- Error pseudo-controlado pero desde un sub-sp.
            call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError"(::sql_error_code, v_errorExt);
            p_out_ErroresDetalle = select * from :p_out_ErroresDetalle union all
                                   select v_errorExt, ::sql_error_message from dummy;
            select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
        else
            -- Error interno no manejado (el log sera captado por la capa de XS control).
            resignal;
        end if;
    end;

    p_out_AdvertenciasContador := 0;
    p_out_ErroresContador := 0;
    p_out_AdvertenciasDetalle = select '' as "Codigo", '' as "RequiereAutorizacion", '' as "Descripcion" from dummy where 1 = 2;
    p_out_ErroresDetalle = select '' as "Codigo", '' as "Descripcion" from dummy where 1 = 2;

    ------------ Logica de Actualizacion ----------

    IF (:p_ID > 0) THEN
        p_out_id := p_ID;

        UPDATE "BSCL.Normativa.BD::TRI.DatosDJ1889"
        SET
            "IdDocumento" =:p_IdDocumento,
            "VboUsuario" =:p_VboUsuario,
            "Observaciones" = (CASE WHEN LENGTH(TRIM(:p_Observaciones)) BETWEEN 4 AND 5 AND LEFT(UPPER(TRIM(:p_Observaciones)), 1) = 'H' THEN "Observaciones" ELSE :p_Observaciones END),
            "FuenteDatos" =:p_FuenteDatos,
            ----------------------------------------
            "Numero" =:p_Numero,
            "FechaOperacion" = :p_FechaOperacion,
            "TipoAhorro" = :p_TipoAhorro,
            ---
            "MontoNominal" = :p_MontoNominal,
            --
            "RentabilidadPositiva" = :p_RentabilidadPositiva,
            "RentabilidadNegativa" = :p_RentabilidadNegativa,
            --Actualizada ($)
            "Factor" = :p_Factor,
            "RentabilidadActualizadaPositiva" = :p_RentabilidadActualizadaPositiva,
            "RentabilidadActualizadaNegativa" = :p_RentabilidadActualizadaNegativa,
            --
            "NumeroCertificado" = :p_NumeroCertificado
        WHERE "ID" = :p_ID;
    ELSE
        select ifnull(max("ID"),0)+1 into p_out_id from "BSCL.Normativa.BD::TRI.DatosDJ1889";
        INSERT INTO "BSCL.Normativa.BD::TRI.DatosDJ1889" (
            "ID","IdDocumento","VboUsuario","Observaciones","FuenteDatos",
            ----------------------------------------
            "Numero","RutCliente","IdCliente","FechaOperacion","TipoAhorro",
            ---
            "MontoNominal",
            --
            "RentabilidadPositiva","RentabilidadNegativa",
            --Actualizada ($)
            "Factor","RentabilidadActualizadaPositiva","RentabilidadActualizadaNegativa",
            --
            "NumeroCertificado")
         VALUES (
            :p_out_id,:p_IdDocumento,'N','','MANTENEDOR',
            ----------------------------------------
            :p_Numero,:p_RutCliente,:p_IdCliente,:p_FechaOperacion,:p_TipoAhorro,
            ---
            :p_MontoNominal,
            --
            :p_RentabilidadPositiva,:p_RentabilidadNegativa,
            --Actualizada ($)
            :p_Factor,:p_RentabilidadActualizadaPositiva,:p_RentabilidadActualizadaNegativa,
            --
            :p_NumeroCertificado);
    END IF;
END;
GO