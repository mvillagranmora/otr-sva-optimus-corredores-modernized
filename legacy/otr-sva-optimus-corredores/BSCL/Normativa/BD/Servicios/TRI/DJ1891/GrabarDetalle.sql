SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1891.GrabarDetalle";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1891.GrabarDetalle"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    IN p_IdEmpresa BIGINT,
    -- ID usuario del login.
    IN p_IdUsuario BIGINT,
    -- ID modulo invocador.
    IN p_IdModulo NVARCHAR(3),

    ------------ Parametros de Entrada Propios ----------

    -- ID del detalle.
    -- Si el id es mayor que cero, se asume que es un registro existente para actualizar.
    -- En caso contrario, se intenta crear un nuevo registro.

    in p_ID bigint,
    in p_IdDocumento bigint,
    in p_VboUsuario char(1),
    in p_Observaciones nvarchar(250),
    in p_FuenteDatos nvarchar(40),
    in p_TipoDocumento nvarchar(10),
    in p_NumeroDocumento bigint,
    in p_FechaDocumento date default null,
    in p_RutEmisor nvarchar(40),
    in p_Nemotecnico nvarchar(100),
    in p_RutCliente nvarchar(40),
    IN p_Cuenta nvarchar(10),
    in p_Cantidad bigint,
    in p_MontoCompras bigint,
    in p_MontoVentas bigint,
    in p_ValorAdquisicion bigint,
    in p_TipoTransaccion nvarchar(6),
    in p_IdTipoOperacion nvarchar(6),
    in p_IdTipoProducto nvarchar(4),
    in p_IdBaseDocumento bigint default null,
    in p_IdCliente bigint default null,
    in p_IdPortafolio bigint default null,
    in p_LineaFactura bigint,

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

    if p_ID > 0 then
        p_out_id := p_ID;

        update "BSCL.Normativa.BD::TRI.DatosDJ1891" set
            "VboUsuario" = p_VboUsuario,
            "Observaciones" = (CASE WHEN LENGTH(TRIM(:p_Observaciones)) BETWEEN 4 AND 5 AND LEFT(UPPER(TRIM(:p_Observaciones)), 1) = 'H' THEN "Observaciones" ELSE :p_Observaciones END),
            "FuenteDatos" = p_FuenteDatos,
            "TipoDocumento" = p_TipoDocumento,
            "NumeroDocumento" = p_NumeroDocumento,
            "FechaDocumento" = p_FechaDocumento,
            "RutEmisor" = p_RutEmisor,
            "Nemotecnico" = p_Nemotecnico,
            "RutCliente" = p_RutCliente,
            "Cuenta" = p_Cuenta,
            "Cantidad" = p_Cantidad,
            "MontoCompras" = p_MontoCompras,
            "MontoVentas" = p_MontoVentas,
            "ValorAdquisicion" = p_ValorAdquisicion,
            "TipoTransaccion" = p_TipoTransaccion,
            "IdTipoOperacion" = p_IdTipoOperacion,
            "IdTipoProducto" = p_IdTipoProducto,
            "IdBaseDocumento" = p_IdBaseDocumento,
            "IdCliente" = p_IdCliente,
            "IdPortafolio" = p_IdPortafolio,
            "LineaFactura" = p_LineaFactura
        where "ID" = p_ID;
    else
        select ifnull(max("ID"),0)+1 into p_out_id from "BSCL.Normativa.BD::TRI.DatosDJ1891";

        insert into "BSCL.Normativa.BD::TRI.DatosDJ1891"(
            "ID",
            "IdDocumento",
            "VboUsuario",
            "Observaciones",
            "FuenteDatos",
            "TipoDocumento",
            "NumeroDocumento",
            "FechaDocumento",
            "RutEmisor",
            "Nemotecnico",
            "RutCliente",
            "Cuenta",
            "Cantidad",
            "MontoCompras",
            "MontoVentas",
            "ValorAdquisicion",
            "TipoTransaccion",
            "IdTipoOperacion",
            "IdTipoProducto",
            "IdCliente",
            "IdPortafolio",
            "LineaFactura"
        ) values (
            p_out_id,
            p_IdDocumento,
            'N',
            '',
            'MANTENEDOR',
            p_TipoDocumento,
            p_NumeroDocumento,
            p_FechaDocumento,
            p_RutEmisor,
            p_Nemotecnico,
            p_RutCliente,
            p_Cuenta,
            p_Cantidad,
            p_MontoCompras,
            p_MontoVentas,
            p_ValorAdquisicion,
            p_TipoTransaccion,
            p_IdTipoOperacion,
            p_IdTipoProducto,
            p_IdCliente,
            p_IdPortafolio,
            p_LineaFactura
        );
    end if;
end;
