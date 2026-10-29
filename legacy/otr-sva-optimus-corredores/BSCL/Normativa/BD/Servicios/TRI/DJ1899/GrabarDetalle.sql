set schema "BSCL";
GO
drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1899.GrabarDetalle";
GO
create procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1899.GrabarDetalle"
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
    ----
    in p_Numero BIGINT,
    in p_IdMoneda NVARCHAR(3) DEFAULT null,
    --in p_RutTrabajador nvarchar(40),
    --in p_IdTrabajador bigint,
    in p_RutCliente nvarchar(40),--p_RutTrabajador
    in p_IdCliente bigint,--p_IdTrabajador
    in p_ActivoPensionado int,
    in p_RutEmpleador nvarchar(40),
    --in p_IdEmpleador bigint,
    in p_FechaOperacion DATE,
    in p_DepositoEmpleador decimal(26,8),--uf
    ---Aporte Empleador
    in p_AporteEmpleador decimal(26,8),--uf
    in p_TraspasoEmpleador decimal(26,8),--uf
    in p_RetiroEmpleadorActualizado decimal(26,8),--($)
    in p_MontoRetencionActualizado decimal(26,8),--15%($)
    --Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
    in p_AhorrosModalidadIndirectaEmpleador decimal(26,8),
    in p_AhorrosModalidadDirectaTrabajador decimal(26,8),
    --DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente
    --del ART. 42 Nª1 de la LIR en UF
    in p_CotizacionesModalidadIndirectaEmpleador decimal(26,8),--uf
    in p_CotizacionesModalidadDirectaTrabajador  decimal(26,8),--uf
    --DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
    --del Art.42 Nª2 de la LIR en UF
    in p_CotizacionesTrabajadorIndependiente decimal(26,8),--uf
    --Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
    --del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
    --los aportes del empleador, actualizados en $
    in p_MontoRetiroPorAhorrosIncisoPrimero decimal(26,8),
    --Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
    in p_RetencionImpuestoSobreRetiros decimal(26,8),--15%($)
    --Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
    in p_MontoRetiroPorAhorrosIncisoSegundo decimal(26,8),
    --N° CERTIFICADO TRABAJADOR
    in p_NumeroCertificadoTrabajador bigint,
    --N° CERTIFICADO EMPLEADOR
    in p_NumeroCertificadoEmpleador bigint,
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

        UPDATE "BSCL.Normativa.BD::TRI.DatosDJ1899"
        SET
            "IdDocumento" =:p_IdDocumento,
            "VboUsuario" =:p_VboUsuario,
            "Observaciones" = (CASE WHEN LENGTH(TRIM(:p_Observaciones)) BETWEEN 4 AND 5 AND LEFT(UPPER(TRIM(:p_Observaciones)), 1) = 'H' THEN "Observaciones" ELSE :p_Observaciones END),
            "FuenteDatos" =:p_FuenteDatos,
            ----------------------------------------
            "Numero" =:p_Numero,
            "IdMoneda" = NULL,
            "ActivoPensionado" = :p_ActivoPensionado,
            "RutCliente" = :p_RutCliente,
            "RutEmpleador" = :p_RutEmpleador,
            "FechaOperacion" = :p_FechaOperacion,
            "DepositoEmpleador"=:p_DepositoEmpleador,
            ---Aporte Empleador
            "AporteEmpleador"=:p_AporteEmpleador,
            "TraspasoEmpleador"=:p_TraspasoEmpleador,
            "RetiroEmpleadorActualizado"=:p_RetiroEmpleadorActualizado,
            "MontoRetencionActualizado"=:p_MontoRetencionActualizado,
            --Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
            "AhorrosModalidadIndirectaEmpleador"=:p_AhorrosModalidadIndirectaEmpleador,
            "AhorrosModalidadDirectaTrabajador"=:p_AhorrosModalidadDirectaTrabajador,
            --DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente
            --del ART. 42 Nª1 de la LIR en UF
            "CotizacionesModalidadIndirectaEmpleador"=:p_CotizacionesModalidadIndirectaEmpleador,
            "CotizacionesModalidadDirectaTrabajador"=:p_CotizacionesModalidadDirectaTrabajador,
            --DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
            --del Art.42 Nª2 de la LIR en UF
            "CotizacionesTrabajadorIndependiente"=:p_CotizacionesTrabajadorIndependiente,
            --Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
            --del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
            --los aportes del empleador, actualizados en $
            "MontoRetiroPorAhorrosIncisoPrimero"=:p_MontoRetiroPorAhorrosIncisoPrimero,
            --Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
            "RetencionImpuestoSobreRetiros"=:p_RetencionImpuestoSobreRetiros,
            --Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
            "MontoRetiroPorAhorrosIncisoSegundo"=:p_MontoRetiroPorAhorrosIncisoSegundo,
            --N° CERTIFICADO TRABAJADOR
            "NumeroCertificadoTrabajador"=:p_NumeroCertificadoTrabajador,
            --N° CERTIFICADO EMPLEADOR
            "NumeroCertificadoEmpleador"=:p_NumeroCertificadoEmpleador
        WHERE "ID" = :p_ID;
    ELSE
        select ifnull(max("ID"),0)+1 into p_out_id from "BSCL.Normativa.BD::TRI.DatosDJ1899";
        INSERT INTO "BSCL.Normativa.BD::TRI.DatosDJ1899" (
            "ID","IdDocumento","VboUsuario","Observaciones","FuenteDatos",
            ----------------------------------------
            "Numero","IdMoneda","RutCliente","IdCliente","ActivoPensionado","RutEmpleador",--"IdEmpleador",
            "FechaOperacion","DepositoEmpleador",
            ---Aporte Empleador
            "AporteEmpleador","TraspasoEmpleador","RetiroEmpleadorActualizado","MontoRetencionActualizado",
            --Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
            "AhorrosModalidadIndirectaEmpleador","AhorrosModalidadDirectaTrabajador",
            --DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente
            --del ART. 42 Nª1 de la LIR en UF
            "CotizacionesModalidadIndirectaEmpleador","CotizacionesModalidadDirectaTrabajador",
            --DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
            --del Art.42 Nª2 de la LIR en UF
            "CotizacionesTrabajadorIndependiente",
            --Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
            --del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
            --los aportes del empleador, actualizados en $
            "MontoRetiroPorAhorrosIncisoPrimero",
            --Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
            "RetencionImpuestoSobreRetiros",
            --Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
            "MontoRetiroPorAhorrosIncisoSegundo",
            --N° CERTIFICADO TRABAJADOR
            "NumeroCertificadoTrabajador",
            --N° CERTIFICADO EMPLEADOR
            "NumeroCertificadoEmpleador")
         VALUES (
            :p_out_id,:p_IdDocumento,'N','','MANTENEDOR',
            ----------------------------------------
            ----------------------------------------
            COALESCE(:p_Numero, 0),:p_IdMoneda,:p_RutCliente,0,:p_ActivoPensionado,:p_RutEmpleador,
            :p_FechaOperacion,:p_DepositoEmpleador,
            ---Aporte Empleador
            :p_AporteEmpleador,:p_TraspasoEmpleador,:p_RetiroEmpleadorActualizado,:p_MontoRetencionActualizado,
            --Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
            :p_AhorrosModalidadIndirectaEmpleador,:p_AhorrosModalidadDirectaTrabajador,
            --DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente
            --del ART. 42 Nª1 de la LIR en UF
            :p_CotizacionesModalidadIndirectaEmpleador,:p_CotizacionesModalidadDirectaTrabajador,
            --DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
            --del Art.42 Nª2 de la LIR en UF
            :p_CotizacionesTrabajadorIndependiente,
            --Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
            --del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
            --los aportes del empleador, actualizados en $
            :p_MontoRetiroPorAhorrosIncisoPrimero,
            --Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
            :p_RetencionImpuestoSobreRetiros,
            --Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
            :p_MontoRetiroPorAhorrosIncisoSegundo,
            --N° CERTIFICADO TRABAJADOR
            :p_NumeroCertificadoTrabajador,
            --N° CERTIFICADO EMPLEADOR
            :p_NumeroCertificadoEmpleador);
    END IF;
END;
GO