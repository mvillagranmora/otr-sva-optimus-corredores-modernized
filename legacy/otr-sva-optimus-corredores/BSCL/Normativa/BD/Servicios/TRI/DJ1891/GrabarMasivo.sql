set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1891.GrabarMasivo";

create procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1891.GrabarMasivo"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    in p_IdEmpresa bigint,

    -- ID usuario del login.
    in p_IdUsuario bigint,

    -- ID modulo invocador.
    in p_IdModulo nvarchar(3),

    ------------ Parametros de Entrada Propios ----------

    -- ID del documento.
    in p_IdDocumento bigint,

    -- Filtro VoBo del usuario.
    in p_VboUsuario nvarchar(3) default null,

    -- Filtro observaciones.
    in p_Observaciones nvarchar(252) default null,

    -- Filtro fuentes de datos.
    in p_FuenteDatos nvarchar(42) default null,

    -- Rango tipo de documento a declarar.
    in p_TipoDocumento_Desde nvarchar(10) default null,
    in p_TipoDocumento_Hasta nvarchar(10) default null,

    -- Rango numero del documento.
    in p_NumeroDocumento_Desde bigint default null,
    in p_NumeroDocumento_Hasta bigint default null,

    -- Rango fecha del documento.
    in p_FechaDocumento_Desde date default null,
    in p_FechaDocumento_Hasta date default null,

    -- Filtro RUT del emisor.
    in p_RutEmisor nvarchar(42) default null,

    -- Filtro del nemotecnico del instrumento.
    in p_Nemotecnico nvarchar(102) default null,

    -- Filtro del RUT del cliente.
    in p_RutCliente nvarchar(42) default null,
    
    -- 
    in p_Cuenta nvarchar(10),

    -- Rango de la cantidad de acciones.
    in p_Cantidad_Desde bigint default null,
    in p_Cantidad_Hasta bigint default null,

    -- Rango del monto compras.
    in p_MontoCompras_Desde decimal(15,0) default null,
    in p_MontoCompras_Hasta decimal(15,0) default null,

    -- Rango del monto ventas.
    in p_MontoVentas_Desde decimal(15,0) default null,
    in p_MontoVentas_Hasta decimal(15,0) default null,
    
    -- Rango del valor adquisición.
    in p_ValorAdquisicion_Desde decimal(15,0) default null,
    in p_ValorAdquisicion_Hasta decimal(15,0) default null,
    

    -- Rango del tipo de transaccion.
    in p_TipoTransaccion_Desde nvarchar(6) default null,
    in p_TipoTransaccion_Hasta nvarchar(6) default null,

    -- Rango del tipo de operacion.
    in p_IdTipoOperacion_Desde nvarchar(6) default null,
    in p_IdTipoOperacion_Hasta nvarchar(6) default null,

    -- Rango del tipo de documento.
    in p_IdTipoProducto_Desde nvarchar(4) default null,
    in p_IdTipoProducto_Hasta nvarchar(4) default null,

    -- IdBaseDocumento
    in p_IdBaseDocumento_Desde bigint default null,
    in p_IdBaseDocumento_Hasta bigint default null,

    -- Rango del ID cliente.
    in p_IdCliente_Desde bigint default null,
    in p_IdCliente_Hasta bigint default null,

    -- Rango del ID portafolio.
    in p_IdPortafolio_Desde bigint default null,
    in p_IdPortafolio_Hasta bigint default null,

    -- LineaFactura
    in p_LineaFactura_Desde bigint default null,
    in p_LineaFactura_Hasta bigint default null,


    -- Valores a modificar.
    in x_VboUsuario char(1) default null,
    in x_Observaciones nvarchar(250) default null,
    in x_FuenteDatos nvarchar(40) default null,
    in x_TipoDocumento nvarchar(10) default null,
    in x_NumeroDocumento bigint default null,
    in x_FechaDocumento date default null,
    in x_RutEmisor nvarchar(40) default null,
    in x_Nemotecnico nvarchar(100) default null,
    in x_RutCliente nvarchar(40) default null,
    in x_Cuenta nvarchar(10) default null,
    in x_Cantidad bigint default null,
    in x_MontoCompras bigint default null,
    in x_MontoVentas bigint default null,
    in x_ValorAdquisicion bigint default null,
    in x_TipoTransaccion nvarchar(6) default null,
    in x_IdTipoOperacion nvarchar(6) default null,
    in x_IdTipoProducto nvarchar(4) default null,
    in x_IdBaseDocumento bigint default null,
    in x_IdCliente bigint default null,
    in x_IdPortafolio bigint default null,
    in x_LineaFactura bigint default null,

    ------------ Salidas ----------

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

	-- Actualizacion Cantidad.
    if x_Cantidad is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1891"
        set "Cantidad" = x_Cantidad
        where "IdDocumento" = p_IdDocumento
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)

        and ("TipoDocumento" >= p_TipoDocumento_Desde or p_TipoDocumento_Desde is null)
        and ("TipoDocumento" <= p_TipoDocumento_Hasta or p_TipoDocumento_Hasta is null)
        and ("NumeroDocumento" >= p_NumeroDocumento_Desde or p_NumeroDocumento_Desde is null)
        and ("NumeroDocumento" <= p_NumeroDocumento_Hasta or p_NumeroDocumento_Hasta is null)
        and ("FechaDocumento" >= p_FechaDocumento_Desde or p_FechaDocumento_Desde is null)
        and ("FechaDocumento" <= p_FechaDocumento_Hasta or p_FechaDocumento_Hasta is null)
        and ("RutEmisor" like p_RutEmisor or p_RutEmisor is null)
        and ("RutCliente" like p_RutCliente or p_RutCliente is null)
        and ("Nemotecnico" like p_Nemotecnico or p_Nemotecnico is null)
        and ("Cuenta" like p_Cuenta or p_Cuenta is null)
        and ("Cantidad" >= p_Cantidad_Desde or p_Cantidad_Desde is null)
        and ("Cantidad" <= p_Cantidad_Hasta or p_Cantidad_Hasta is null)
        and ("MontoCompras" >= p_MontoCompras_Desde or p_MontoCompras_Desde is null)
        and ("MontoCompras" <= p_MontoCompras_Hasta or p_MontoCompras_Hasta is null)
        and ("MontoVentas" >= p_MontoVentas_Desde or p_MontoVentas_Desde is null)
        and ("MontoVentas" <= p_MontoVentas_Hasta or p_MontoVentas_Hasta is null)
        and ("ValorAdquisicion" >= p_ValorAdquisicion_Desde or p_ValorAdquisicion_Desde is null)
        and ("ValorAdquisicion" <= p_ValorAdquisicion_Hasta or p_ValorAdquisicion_Hasta is null)
        and ("TipoTransaccion" >= p_TipoTransaccion_Desde or p_TipoTransaccion_Desde is null)
        and ("TipoTransaccion" <= p_TipoTransaccion_Hasta or p_TipoTransaccion_Hasta is null)
        and ("IdTipoOperacion" >= p_IdTipoOperacion_Desde or p_IdTipoOperacion_Desde is null)
        and ("IdTipoOperacion" <= p_IdTipoOperacion_Hasta or p_IdTipoOperacion_Hasta is null)
        and ("IdTipoProducto" >= p_IdTipoProducto_Desde or p_IdTipoProducto_Desde is null)
        and ("IdTipoProducto" <= p_IdTipoProducto_Hasta or p_IdTipoProducto_Hasta is null)
        and ("IdBaseDocumento" >= p_IdBaseDocumento_Desde or p_IdBaseDocumento_Desde is null)
        and ("IdBaseDocumento" <= p_IdBaseDocumento_Hasta or p_IdBaseDocumento_Hasta is null)
        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
        and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
        and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
        and ("LineaFactura" >= p_LineaFactura_Desde or p_LineaFactura_Desde is null)
        and ("LineaFactura" <= p_LineaFactura_Hasta or p_LineaFactura_Hasta is null)
        ;
    end if;

	-- Actualizacion FechaDocumento.
    if x_FechaDocumento is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1891"
        set "FechaDocumento" = x_FechaDocumento
        where "IdDocumento" = p_IdDocumento
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)

        and ("TipoDocumento" >= p_TipoDocumento_Desde or p_TipoDocumento_Desde is null)
        and ("TipoDocumento" <= p_TipoDocumento_Hasta or p_TipoDocumento_Hasta is null)
        and ("NumeroDocumento" >= p_NumeroDocumento_Desde or p_NumeroDocumento_Desde is null)
        and ("NumeroDocumento" <= p_NumeroDocumento_Hasta or p_NumeroDocumento_Hasta is null)
        and ("FechaDocumento" >= p_FechaDocumento_Desde or p_FechaDocumento_Desde is null)
        and ("FechaDocumento" <= p_FechaDocumento_Hasta or p_FechaDocumento_Hasta is null)
        and ("RutEmisor" like p_RutEmisor or p_RutEmisor is null)
        and ("RutCliente" like p_RutCliente or p_RutCliente is null)
        and ("Nemotecnico" like p_Nemotecnico or p_Nemotecnico is null)
        and ("Cuenta" like p_Cuenta or p_Cuenta is null)
        and ("Cantidad" >= p_Cantidad_Desde or p_Cantidad_Desde is null)
        and ("Cantidad" <= p_Cantidad_Hasta or p_Cantidad_Hasta is null)
        and ("MontoCompras" >= p_MontoCompras_Desde or p_MontoCompras_Desde is null)
        and ("MontoCompras" <= p_MontoCompras_Hasta or p_MontoCompras_Hasta is null)
        and ("MontoVentas" >= p_MontoVentas_Desde or p_MontoVentas_Desde is null)
        and ("MontoVentas" <= p_MontoVentas_Hasta or p_MontoVentas_Hasta is null)
        and ("ValorAdquisicion" >= p_ValorAdquisicion_Desde or p_ValorAdquisicion_Desde is null)
        and ("ValorAdquisicion" <= p_ValorAdquisicion_Hasta or p_ValorAdquisicion_Hasta is null)
        and ("TipoTransaccion" >= p_TipoTransaccion_Desde or p_TipoTransaccion_Desde is null)
        and ("TipoTransaccion" <= p_TipoTransaccion_Hasta or p_TipoTransaccion_Hasta is null)
        and ("IdTipoOperacion" >= p_IdTipoOperacion_Desde or p_IdTipoOperacion_Desde is null)
        and ("IdTipoOperacion" <= p_IdTipoOperacion_Hasta or p_IdTipoOperacion_Hasta is null)
        and ("IdTipoProducto" >= p_IdTipoProducto_Desde or p_IdTipoProducto_Desde is null)
        and ("IdTipoProducto" <= p_IdTipoProducto_Hasta or p_IdTipoProducto_Hasta is null)
        and ("IdBaseDocumento" >= p_IdBaseDocumento_Desde or p_IdBaseDocumento_Desde is null)
        and ("IdBaseDocumento" <= p_IdBaseDocumento_Hasta or p_IdBaseDocumento_Hasta is null)
        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
        and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
        and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
        and ("LineaFactura" >= p_LineaFactura_Desde or p_LineaFactura_Desde is null)
        and ("LineaFactura" <= p_LineaFactura_Hasta or p_LineaFactura_Hasta is null)
        ;
    end if;

	-- Actualizacion IdBaseDocumento.
    if x_IdBaseDocumento is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1891"
        set "IdBaseDocumento" = x_IdBaseDocumento
        where "IdDocumento" = p_IdDocumento
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)

        and ("TipoDocumento" >= p_TipoDocumento_Desde or p_TipoDocumento_Desde is null)
        and ("TipoDocumento" <= p_TipoDocumento_Hasta or p_TipoDocumento_Hasta is null)
        and ("NumeroDocumento" >= p_NumeroDocumento_Desde or p_NumeroDocumento_Desde is null)
        and ("NumeroDocumento" <= p_NumeroDocumento_Hasta or p_NumeroDocumento_Hasta is null)
        and ("FechaDocumento" >= p_FechaDocumento_Desde or p_FechaDocumento_Desde is null)
        and ("FechaDocumento" <= p_FechaDocumento_Hasta or p_FechaDocumento_Hasta is null)
        and ("RutEmisor" like p_RutEmisor or p_RutEmisor is null)
        and ("RutCliente" like p_RutCliente or p_RutCliente is null)
        and ("Nemotecnico" like p_Nemotecnico or p_Nemotecnico is null)
        and ("Cuenta" like p_Cuenta or p_Cuenta is null)
        and ("Cantidad" >= p_Cantidad_Desde or p_Cantidad_Desde is null)
        and ("Cantidad" <= p_Cantidad_Hasta or p_Cantidad_Hasta is null)
        and ("MontoCompras" >= p_MontoCompras_Desde or p_MontoCompras_Desde is null)
        and ("MontoCompras" <= p_MontoCompras_Hasta or p_MontoCompras_Hasta is null)
        and ("MontoVentas" >= p_MontoVentas_Desde or p_MontoVentas_Desde is null)
        and ("MontoVentas" <= p_MontoVentas_Hasta or p_MontoVentas_Hasta is null)
        and ("ValorAdquisicion" >= p_ValorAdquisicion_Desde or p_ValorAdquisicion_Desde is null)
        and ("ValorAdquisicion" <= p_ValorAdquisicion_Hasta or p_ValorAdquisicion_Hasta is null)
        and ("TipoTransaccion" >= p_TipoTransaccion_Desde or p_TipoTransaccion_Desde is null)
        and ("TipoTransaccion" <= p_TipoTransaccion_Hasta or p_TipoTransaccion_Hasta is null)
        and ("IdTipoOperacion" >= p_IdTipoOperacion_Desde or p_IdTipoOperacion_Desde is null)
        and ("IdTipoOperacion" <= p_IdTipoOperacion_Hasta or p_IdTipoOperacion_Hasta is null)
        and ("IdTipoProducto" >= p_IdTipoProducto_Desde or p_IdTipoProducto_Desde is null)
        and ("IdTipoProducto" <= p_IdTipoProducto_Hasta or p_IdTipoProducto_Hasta is null)
        and ("IdBaseDocumento" >= p_IdBaseDocumento_Desde or p_IdBaseDocumento_Desde is null)
        and ("IdBaseDocumento" <= p_IdBaseDocumento_Hasta or p_IdBaseDocumento_Hasta is null)
        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
        and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
        and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
        and ("LineaFactura" >= p_LineaFactura_Desde or p_LineaFactura_Desde is null)
        and ("LineaFactura" <= p_LineaFactura_Hasta or p_LineaFactura_Hasta is null)
        ;
    end if;

	-- Actualizacion IdCliente.
    if x_IdCliente is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1891"
        set "IdCliente" = x_IdCliente
        where "IdDocumento" = p_IdDocumento
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)

        and ("TipoDocumento" >= p_TipoDocumento_Desde or p_TipoDocumento_Desde is null)
        and ("TipoDocumento" <= p_TipoDocumento_Hasta or p_TipoDocumento_Hasta is null)
        and ("NumeroDocumento" >= p_NumeroDocumento_Desde or p_NumeroDocumento_Desde is null)
        and ("NumeroDocumento" <= p_NumeroDocumento_Hasta or p_NumeroDocumento_Hasta is null)
        and ("FechaDocumento" >= p_FechaDocumento_Desde or p_FechaDocumento_Desde is null)
        and ("FechaDocumento" <= p_FechaDocumento_Hasta or p_FechaDocumento_Hasta is null)
        and ("RutEmisor" like p_RutEmisor or p_RutEmisor is null)
        and ("RutCliente" like p_RutCliente or p_RutCliente is null)
        and ("Nemotecnico" like p_Nemotecnico or p_Nemotecnico is null)
        and ("Cuenta" like p_Cuenta or p_Cuenta is null)
        and ("Cantidad" >= p_Cantidad_Desde or p_Cantidad_Desde is null)
        and ("Cantidad" <= p_Cantidad_Hasta or p_Cantidad_Hasta is null)
        and ("MontoCompras" >= p_MontoCompras_Desde or p_MontoCompras_Desde is null)
        and ("MontoCompras" <= p_MontoCompras_Hasta or p_MontoCompras_Hasta is null)
        and ("MontoVentas" >= p_MontoVentas_Desde or p_MontoVentas_Desde is null)
        and ("MontoVentas" <= p_MontoVentas_Hasta or p_MontoVentas_Hasta is null)
        and ("ValorAdquisicion" >= p_ValorAdquisicion_Desde or p_ValorAdquisicion_Desde is null)
        and ("ValorAdquisicion" <= p_ValorAdquisicion_Hasta or p_ValorAdquisicion_Hasta is null)
        and ("TipoTransaccion" >= p_TipoTransaccion_Desde or p_TipoTransaccion_Desde is null)
        and ("TipoTransaccion" <= p_TipoTransaccion_Hasta or p_TipoTransaccion_Hasta is null)
        and ("IdTipoOperacion" >= p_IdTipoOperacion_Desde or p_IdTipoOperacion_Desde is null)
        and ("IdTipoOperacion" <= p_IdTipoOperacion_Hasta or p_IdTipoOperacion_Hasta is null)
        and ("IdTipoProducto" >= p_IdTipoProducto_Desde or p_IdTipoProducto_Desde is null)
        and ("IdTipoProducto" <= p_IdTipoProducto_Hasta or p_IdTipoProducto_Hasta is null)
        and ("IdBaseDocumento" >= p_IdBaseDocumento_Desde or p_IdBaseDocumento_Desde is null)
        and ("IdBaseDocumento" <= p_IdBaseDocumento_Hasta or p_IdBaseDocumento_Hasta is null)
        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
        and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
        and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
        and ("LineaFactura" >= p_LineaFactura_Desde or p_LineaFactura_Desde is null)
        and ("LineaFactura" <= p_LineaFactura_Hasta or p_LineaFactura_Hasta is null)
        ;
    end if;

	-- Actualizacion IdPortafolio.
    if x_IdPortafolio is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1891"
        set "IdPortafolio" = x_IdPortafolio
        where "IdDocumento" = p_IdDocumento
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)

        and ("TipoDocumento" >= p_TipoDocumento_Desde or p_TipoDocumento_Desde is null)
        and ("TipoDocumento" <= p_TipoDocumento_Hasta or p_TipoDocumento_Hasta is null)
        and ("NumeroDocumento" >= p_NumeroDocumento_Desde or p_NumeroDocumento_Desde is null)
        and ("NumeroDocumento" <= p_NumeroDocumento_Hasta or p_NumeroDocumento_Hasta is null)
        and ("FechaDocumento" >= p_FechaDocumento_Desde or p_FechaDocumento_Desde is null)
        and ("FechaDocumento" <= p_FechaDocumento_Hasta or p_FechaDocumento_Hasta is null)
        and ("RutEmisor" like p_RutEmisor or p_RutEmisor is null)
        and ("RutCliente" like p_RutCliente or p_RutCliente is null)
        and ("Nemotecnico" like p_Nemotecnico or p_Nemotecnico is null)
        and ("Cuenta" like p_Cuenta or p_Cuenta is null)
        and ("Cantidad" >= p_Cantidad_Desde or p_Cantidad_Desde is null)
        and ("Cantidad" <= p_Cantidad_Hasta or p_Cantidad_Hasta is null)
        and ("MontoCompras" >= p_MontoCompras_Desde or p_MontoCompras_Desde is null)
        and ("MontoCompras" <= p_MontoCompras_Hasta or p_MontoCompras_Hasta is null)
        and ("MontoVentas" >= p_MontoVentas_Desde or p_MontoVentas_Desde is null)
        and ("MontoVentas" <= p_MontoVentas_Hasta or p_MontoVentas_Hasta is null)
        and ("ValorAdquisicion" >= p_ValorAdquisicion_Desde or p_ValorAdquisicion_Desde is null)
        and ("ValorAdquisicion" <= p_ValorAdquisicion_Hasta or p_ValorAdquisicion_Hasta is null)
        and ("TipoTransaccion" >= p_TipoTransaccion_Desde or p_TipoTransaccion_Desde is null)
        and ("TipoTransaccion" <= p_TipoTransaccion_Hasta or p_TipoTransaccion_Hasta is null)
        and ("IdTipoOperacion" >= p_IdTipoOperacion_Desde or p_IdTipoOperacion_Desde is null)
        and ("IdTipoOperacion" <= p_IdTipoOperacion_Hasta or p_IdTipoOperacion_Hasta is null)
        and ("IdTipoProducto" >= p_IdTipoProducto_Desde or p_IdTipoProducto_Desde is null)
        and ("IdTipoProducto" <= p_IdTipoProducto_Hasta or p_IdTipoProducto_Hasta is null)
        and ("IdBaseDocumento" >= p_IdBaseDocumento_Desde or p_IdBaseDocumento_Desde is null)
        and ("IdBaseDocumento" <= p_IdBaseDocumento_Hasta or p_IdBaseDocumento_Hasta is null)
        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
        and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
        and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
        and ("LineaFactura" >= p_LineaFactura_Desde or p_LineaFactura_Desde is null)
        and ("LineaFactura" <= p_LineaFactura_Hasta or p_LineaFactura_Hasta is null)
        ;
    end if;

	-- Actualizacion IdTipoOperacion.
    if x_IdTipoOperacion is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1891"
        set "IdTipoOperacion" = x_IdTipoOperacion
        where "IdDocumento" = p_IdDocumento
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)

        and ("TipoDocumento" >= p_TipoDocumento_Desde or p_TipoDocumento_Desde is null)
        and ("TipoDocumento" <= p_TipoDocumento_Hasta or p_TipoDocumento_Hasta is null)
        and ("NumeroDocumento" >= p_NumeroDocumento_Desde or p_NumeroDocumento_Desde is null)
        and ("NumeroDocumento" <= p_NumeroDocumento_Hasta or p_NumeroDocumento_Hasta is null)
        and ("FechaDocumento" >= p_FechaDocumento_Desde or p_FechaDocumento_Desde is null)
        and ("FechaDocumento" <= p_FechaDocumento_Hasta or p_FechaDocumento_Hasta is null)
        and ("RutEmisor" like p_RutEmisor or p_RutEmisor is null)
        and ("RutCliente" like p_RutCliente or p_RutCliente is null)
        and ("Nemotecnico" like p_Nemotecnico or p_Nemotecnico is null)
        and ("Cuenta" like p_Cuenta or p_Cuenta is null)
        and ("Cantidad" >= p_Cantidad_Desde or p_Cantidad_Desde is null)
        and ("Cantidad" <= p_Cantidad_Hasta or p_Cantidad_Hasta is null)
        and ("MontoCompras" >= p_MontoCompras_Desde or p_MontoCompras_Desde is null)
        and ("MontoCompras" <= p_MontoCompras_Hasta or p_MontoCompras_Hasta is null)
        and ("MontoVentas" >= p_MontoVentas_Desde or p_MontoVentas_Desde is null)
        and ("MontoVentas" <= p_MontoVentas_Hasta or p_MontoVentas_Hasta is null)
        and ("ValorAdquisicion" >= p_ValorAdquisicion_Desde or p_ValorAdquisicion_Desde is null)
        and ("ValorAdquisicion" <= p_ValorAdquisicion_Hasta or p_ValorAdquisicion_Hasta is null)
        and ("TipoTransaccion" >= p_TipoTransaccion_Desde or p_TipoTransaccion_Desde is null)
        and ("TipoTransaccion" <= p_TipoTransaccion_Hasta or p_TipoTransaccion_Hasta is null)
        and ("IdTipoOperacion" >= p_IdTipoOperacion_Desde or p_IdTipoOperacion_Desde is null)
        and ("IdTipoOperacion" <= p_IdTipoOperacion_Hasta or p_IdTipoOperacion_Hasta is null)
        and ("IdTipoProducto" >= p_IdTipoProducto_Desde or p_IdTipoProducto_Desde is null)
        and ("IdTipoProducto" <= p_IdTipoProducto_Hasta or p_IdTipoProducto_Hasta is null)
        and ("IdBaseDocumento" >= p_IdBaseDocumento_Desde or p_IdBaseDocumento_Desde is null)
        and ("IdBaseDocumento" <= p_IdBaseDocumento_Hasta or p_IdBaseDocumento_Hasta is null)
        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
        and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
        and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
        and ("LineaFactura" >= p_LineaFactura_Desde or p_LineaFactura_Desde is null)
        and ("LineaFactura" <= p_LineaFactura_Hasta or p_LineaFactura_Hasta is null)
        ;
    end if;

	-- Actualizacion IdTipoProducto.
    if x_IdTipoProducto is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1891"
        set "IdTipoProducto" = x_IdTipoProducto
        where "IdDocumento" = p_IdDocumento
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)

        and ("TipoDocumento" >= p_TipoDocumento_Desde or p_TipoDocumento_Desde is null)
        and ("TipoDocumento" <= p_TipoDocumento_Hasta or p_TipoDocumento_Hasta is null)
        and ("NumeroDocumento" >= p_NumeroDocumento_Desde or p_NumeroDocumento_Desde is null)
        and ("NumeroDocumento" <= p_NumeroDocumento_Hasta or p_NumeroDocumento_Hasta is null)
        and ("FechaDocumento" >= p_FechaDocumento_Desde or p_FechaDocumento_Desde is null)
        and ("FechaDocumento" <= p_FechaDocumento_Hasta or p_FechaDocumento_Hasta is null)
        and ("RutEmisor" like p_RutEmisor or p_RutEmisor is null)
        and ("RutCliente" like p_RutCliente or p_RutCliente is null)
        and ("Nemotecnico" like p_Nemotecnico or p_Nemotecnico is null)
        and ("Cuenta" like p_Cuenta or p_Cuenta is null)
        and ("Cantidad" >= p_Cantidad_Desde or p_Cantidad_Desde is null)
        and ("Cantidad" <= p_Cantidad_Hasta or p_Cantidad_Hasta is null)
        and ("MontoCompras" >= p_MontoCompras_Desde or p_MontoCompras_Desde is null)
        and ("MontoCompras" <= p_MontoCompras_Hasta or p_MontoCompras_Hasta is null)
        and ("MontoVentas" >= p_MontoVentas_Desde or p_MontoVentas_Desde is null)
        and ("MontoVentas" <= p_MontoVentas_Hasta or p_MontoVentas_Hasta is null)
        and ("ValorAdquisicion" >= p_ValorAdquisicion_Desde or p_ValorAdquisicion_Desde is null)
        and ("ValorAdquisicion" <= p_ValorAdquisicion_Hasta or p_ValorAdquisicion_Hasta is null)
        and ("TipoTransaccion" >= p_TipoTransaccion_Desde or p_TipoTransaccion_Desde is null)
        and ("TipoTransaccion" <= p_TipoTransaccion_Hasta or p_TipoTransaccion_Hasta is null)
        and ("IdTipoOperacion" >= p_IdTipoOperacion_Desde or p_IdTipoOperacion_Desde is null)
        and ("IdTipoOperacion" <= p_IdTipoOperacion_Hasta or p_IdTipoOperacion_Hasta is null)
        and ("IdTipoProducto" >= p_IdTipoProducto_Desde or p_IdTipoProducto_Desde is null)
        and ("IdTipoProducto" <= p_IdTipoProducto_Hasta or p_IdTipoProducto_Hasta is null)
        and ("IdBaseDocumento" >= p_IdBaseDocumento_Desde or p_IdBaseDocumento_Desde is null)
        and ("IdBaseDocumento" <= p_IdBaseDocumento_Hasta or p_IdBaseDocumento_Hasta is null)
        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
        and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
        and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
        and ("LineaFactura" >= p_LineaFactura_Desde or p_LineaFactura_Desde is null)
        and ("LineaFactura" <= p_LineaFactura_Hasta or p_LineaFactura_Hasta is null)
        ;
    end if;

	-- Actualizacion MontoCompras.
    if x_MontoCompras is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1891"
        set "MontoCompras" = x_MontoCompras
        where "IdDocumento" = p_IdDocumento
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)

        and ("TipoDocumento" >= p_TipoDocumento_Desde or p_TipoDocumento_Desde is null)
        and ("TipoDocumento" <= p_TipoDocumento_Hasta or p_TipoDocumento_Hasta is null)
        and ("NumeroDocumento" >= p_NumeroDocumento_Desde or p_NumeroDocumento_Desde is null)
        and ("NumeroDocumento" <= p_NumeroDocumento_Hasta or p_NumeroDocumento_Hasta is null)
        and ("FechaDocumento" >= p_FechaDocumento_Desde or p_FechaDocumento_Desde is null)
        and ("FechaDocumento" <= p_FechaDocumento_Hasta or p_FechaDocumento_Hasta is null)
        and ("RutEmisor" like p_RutEmisor or p_RutEmisor is null)
        and ("RutCliente" like p_RutCliente or p_RutCliente is null)
        and ("Nemotecnico" like p_Nemotecnico or p_Nemotecnico is null)
        and ("Cuenta" like p_Cuenta or p_Cuenta is null)
        and ("Cantidad" >= p_Cantidad_Desde or p_Cantidad_Desde is null)
        and ("Cantidad" <= p_Cantidad_Hasta or p_Cantidad_Hasta is null)
        and ("MontoCompras" >= p_MontoCompras_Desde or p_MontoCompras_Desde is null)
        and ("MontoCompras" <= p_MontoCompras_Hasta or p_MontoCompras_Hasta is null)
        and ("MontoVentas" >= p_MontoVentas_Desde or p_MontoVentas_Desde is null)
        and ("MontoVentas" <= p_MontoVentas_Hasta or p_MontoVentas_Hasta is null)
        and ("ValorAdquisicion" >= p_ValorAdquisicion_Desde or p_ValorAdquisicion_Desde is null)
        and ("ValorAdquisicion" <= p_ValorAdquisicion_Hasta or p_ValorAdquisicion_Hasta is null)
        and ("TipoTransaccion" >= p_TipoTransaccion_Desde or p_TipoTransaccion_Desde is null)
        and ("TipoTransaccion" <= p_TipoTransaccion_Hasta or p_TipoTransaccion_Hasta is null)
        and ("IdTipoOperacion" >= p_IdTipoOperacion_Desde or p_IdTipoOperacion_Desde is null)
        and ("IdTipoOperacion" <= p_IdTipoOperacion_Hasta or p_IdTipoOperacion_Hasta is null)
        and ("IdTipoProducto" >= p_IdTipoProducto_Desde or p_IdTipoProducto_Desde is null)
        and ("IdTipoProducto" <= p_IdTipoProducto_Hasta or p_IdTipoProducto_Hasta is null)
        and ("IdBaseDocumento" >= p_IdBaseDocumento_Desde or p_IdBaseDocumento_Desde is null)
        and ("IdBaseDocumento" <= p_IdBaseDocumento_Hasta or p_IdBaseDocumento_Hasta is null)
        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
        and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
        and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
        and ("LineaFactura" >= p_LineaFactura_Desde or p_LineaFactura_Desde is null)
        and ("LineaFactura" <= p_LineaFactura_Hasta or p_LineaFactura_Hasta is null)
        ;
    end if;

-- Actualizacion MontoVentas.
    if x_MontoVentas is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1891"
        set "MontoVentas" = x_MontoVentas
        where "IdDocumento" = p_IdDocumento
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)

        and ("TipoDocumento" >= p_TipoDocumento_Desde or p_TipoDocumento_Desde is null)
        and ("TipoDocumento" <= p_TipoDocumento_Hasta or p_TipoDocumento_Hasta is null)
        and ("NumeroDocumento" >= p_NumeroDocumento_Desde or p_NumeroDocumento_Desde is null)
        and ("NumeroDocumento" <= p_NumeroDocumento_Hasta or p_NumeroDocumento_Hasta is null)
        and ("FechaDocumento" >= p_FechaDocumento_Desde or p_FechaDocumento_Desde is null)
        and ("FechaDocumento" <= p_FechaDocumento_Hasta or p_FechaDocumento_Hasta is null)
        and ("RutEmisor" like p_RutEmisor or p_RutEmisor is null)
        and ("RutCliente" like p_RutCliente or p_RutCliente is null)
        and ("Nemotecnico" like p_Nemotecnico or p_Nemotecnico is null)
        and ("Cuenta" like p_Cuenta or p_Cuenta is null)
        and ("Cantidad" >= p_Cantidad_Desde or p_Cantidad_Desde is null)
        and ("Cantidad" <= p_Cantidad_Hasta or p_Cantidad_Hasta is null)
        and ("MontoCompras" >= p_MontoCompras_Desde or p_MontoCompras_Desde is null)
        and ("MontoCompras" <= p_MontoCompras_Hasta or p_MontoCompras_Hasta is null)
        and ("MontoVentas" >= p_MontoVentas_Desde or p_MontoVentas_Desde is null)
        and ("MontoVentas" <= p_MontoVentas_Hasta or p_MontoVentas_Hasta is null)
        and ("ValorAdquisicion" >= p_ValorAdquisicion_Desde or p_ValorAdquisicion_Desde is null)
        and ("ValorAdquisicion" <= p_ValorAdquisicion_Hasta or p_ValorAdquisicion_Hasta is null)
        and ("TipoTransaccion" >= p_TipoTransaccion_Desde or p_TipoTransaccion_Desde is null)
        and ("TipoTransaccion" <= p_TipoTransaccion_Hasta or p_TipoTransaccion_Hasta is null)
        and ("IdTipoOperacion" >= p_IdTipoOperacion_Desde or p_IdTipoOperacion_Desde is null)
        and ("IdTipoOperacion" <= p_IdTipoOperacion_Hasta or p_IdTipoOperacion_Hasta is null)
        and ("IdTipoProducto" >= p_IdTipoProducto_Desde or p_IdTipoProducto_Desde is null)
        and ("IdTipoProducto" <= p_IdTipoProducto_Hasta or p_IdTipoProducto_Hasta is null)
        and ("IdBaseDocumento" >= p_IdBaseDocumento_Desde or p_IdBaseDocumento_Desde is null)
        and ("IdBaseDocumento" <= p_IdBaseDocumento_Hasta or p_IdBaseDocumento_Hasta is null)
        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
        and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
        and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
        and ("LineaFactura" >= p_LineaFactura_Desde or p_LineaFactura_Desde is null)
        and ("LineaFactura" <= p_LineaFactura_Hasta or p_LineaFactura_Hasta is null)
        ;
    end if;
    
    -- Actualizacion ValorAdquisicion.
    if x_ValorAdquisicion is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1891"
        set "ValorAdquisicion" = x_ValorAdquisicion
        where "IdDocumento" = p_IdDocumento
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)

        and ("TipoDocumento" >= p_TipoDocumento_Desde or p_TipoDocumento_Desde is null)
        and ("TipoDocumento" <= p_TipoDocumento_Hasta or p_TipoDocumento_Hasta is null)
        and ("NumeroDocumento" >= p_NumeroDocumento_Desde or p_NumeroDocumento_Desde is null)
        and ("NumeroDocumento" <= p_NumeroDocumento_Hasta or p_NumeroDocumento_Hasta is null)
        and ("FechaDocumento" >= p_FechaDocumento_Desde or p_FechaDocumento_Desde is null)
        and ("FechaDocumento" <= p_FechaDocumento_Hasta or p_FechaDocumento_Hasta is null)
        and ("RutEmisor" like p_RutEmisor or p_RutEmisor is null)
        and ("RutCliente" like p_RutCliente or p_RutCliente is null)
        and ("Nemotecnico" like p_Nemotecnico or p_Nemotecnico is null)
        and ("Cuenta" like p_Cuenta or p_Cuenta is null)
        and ("Cantidad" >= p_Cantidad_Desde or p_Cantidad_Desde is null)
        and ("Cantidad" <= p_Cantidad_Hasta or p_Cantidad_Hasta is null)
        and ("MontoCompras" >= p_MontoCompras_Desde or p_MontoCompras_Desde is null)
        and ("MontoCompras" <= p_MontoCompras_Hasta or p_MontoCompras_Hasta is null)
        and ("MontoVentas" >= p_MontoVentas_Desde or p_MontoVentas_Desde is null)
        and ("MontoVentas" <= p_MontoVentas_Hasta or p_MontoVentas_Hasta is null)
        and ("ValorAdquisicion" >= p_ValorAdquisicion_Desde or p_ValorAdquisicion_Desde is null)
        and ("ValorAdquisicion" <= p_ValorAdquisicion_Hasta or p_ValorAdquisicion_Hasta is null)
        and ("TipoTransaccion" >= p_TipoTransaccion_Desde or p_TipoTransaccion_Desde is null)
        and ("TipoTransaccion" <= p_TipoTransaccion_Hasta or p_TipoTransaccion_Hasta is null)
        and ("IdTipoOperacion" >= p_IdTipoOperacion_Desde or p_IdTipoOperacion_Desde is null)
        and ("IdTipoOperacion" <= p_IdTipoOperacion_Hasta or p_IdTipoOperacion_Hasta is null)
        and ("IdTipoProducto" >= p_IdTipoProducto_Desde or p_IdTipoProducto_Desde is null)
        and ("IdTipoProducto" <= p_IdTipoProducto_Hasta or p_IdTipoProducto_Hasta is null)
        and ("IdBaseDocumento" >= p_IdBaseDocumento_Desde or p_IdBaseDocumento_Desde is null)
        and ("IdBaseDocumento" <= p_IdBaseDocumento_Hasta or p_IdBaseDocumento_Hasta is null)
        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
        and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
        and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
        and ("LineaFactura" >= p_LineaFactura_Desde or p_LineaFactura_Desde is null)
        and ("LineaFactura" <= p_LineaFactura_Hasta or p_LineaFactura_Hasta is null)
        ;
    end if;
    

	-- Actualizacion Nemotecnico.
    if x_Nemotecnico is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1891"
        set "Nemotecnico" = x_Nemotecnico
        where "IdDocumento" = p_IdDocumento
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)

        and ("TipoDocumento" >= p_TipoDocumento_Desde or p_TipoDocumento_Desde is null)
        and ("TipoDocumento" <= p_TipoDocumento_Hasta or p_TipoDocumento_Hasta is null)
        and ("NumeroDocumento" >= p_NumeroDocumento_Desde or p_NumeroDocumento_Desde is null)
        and ("NumeroDocumento" <= p_NumeroDocumento_Hasta or p_NumeroDocumento_Hasta is null)
        and ("FechaDocumento" >= p_FechaDocumento_Desde or p_FechaDocumento_Desde is null)
        and ("FechaDocumento" <= p_FechaDocumento_Hasta or p_FechaDocumento_Hasta is null)
        and ("RutEmisor" like p_RutEmisor or p_RutEmisor is null)
        and ("RutCliente" like p_RutCliente or p_RutCliente is null)
        and ("Nemotecnico" like p_Nemotecnico or p_Nemotecnico is null)
        and ("Cuenta" like p_Cuenta or p_Cuenta is null)
        and ("Cantidad" >= p_Cantidad_Desde or p_Cantidad_Desde is null)
        and ("Cantidad" <= p_Cantidad_Hasta or p_Cantidad_Hasta is null)
        and ("MontoCompras" >= p_MontoCompras_Desde or p_MontoCompras_Desde is null)
        and ("MontoCompras" <= p_MontoCompras_Hasta or p_MontoCompras_Hasta is null)
        and ("MontoVentas" >= p_MontoVentas_Desde or p_MontoVentas_Desde is null)
        and ("MontoVentas" <= p_MontoVentas_Hasta or p_MontoVentas_Hasta is null)
        and ("ValorAdquisicion" >= p_ValorAdquisicion_Desde or p_ValorAdquisicion_Desde is null)
        and ("ValorAdquisicion" <= p_ValorAdquisicion_Hasta or p_ValorAdquisicion_Hasta is null)
        and ("TipoTransaccion" >= p_TipoTransaccion_Desde or p_TipoTransaccion_Desde is null)
        and ("TipoTransaccion" <= p_TipoTransaccion_Hasta or p_TipoTransaccion_Hasta is null)
        and ("IdTipoOperacion" >= p_IdTipoOperacion_Desde or p_IdTipoOperacion_Desde is null)
        and ("IdTipoOperacion" <= p_IdTipoOperacion_Hasta or p_IdTipoOperacion_Hasta is null)
        and ("IdTipoProducto" >= p_IdTipoProducto_Desde or p_IdTipoProducto_Desde is null)
        and ("IdTipoProducto" <= p_IdTipoProducto_Hasta or p_IdTipoProducto_Hasta is null)
        and ("IdBaseDocumento" >= p_IdBaseDocumento_Desde or p_IdBaseDocumento_Desde is null)
        and ("IdBaseDocumento" <= p_IdBaseDocumento_Hasta or p_IdBaseDocumento_Hasta is null)
        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
        and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
        and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
        and ("LineaFactura" >= p_LineaFactura_Desde or p_LineaFactura_Desde is null)
        and ("LineaFactura" <= p_LineaFactura_Hasta or p_LineaFactura_Hasta is null)
        ;
    end if;

	-- Actualizacion NumeroDocumento.
    if x_NumeroDocumento is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1891"
        set "NumeroDocumento" = x_NumeroDocumento
        where "IdDocumento" = p_IdDocumento
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)

        and ("TipoDocumento" >= p_TipoDocumento_Desde or p_TipoDocumento_Desde is null)
        and ("TipoDocumento" <= p_TipoDocumento_Hasta or p_TipoDocumento_Hasta is null)
        and ("NumeroDocumento" >= p_NumeroDocumento_Desde or p_NumeroDocumento_Desde is null)
        and ("NumeroDocumento" <= p_NumeroDocumento_Hasta or p_NumeroDocumento_Hasta is null)
        and ("FechaDocumento" >= p_FechaDocumento_Desde or p_FechaDocumento_Desde is null)
        and ("FechaDocumento" <= p_FechaDocumento_Hasta or p_FechaDocumento_Hasta is null)
        and ("RutEmisor" like p_RutEmisor or p_RutEmisor is null)
        and ("RutCliente" like p_RutCliente or p_RutCliente is null)
        and ("Nemotecnico" like p_Nemotecnico or p_Nemotecnico is null)
        and ("Cuenta" like p_Cuenta or p_Cuenta is null)
        and ("Cantidad" >= p_Cantidad_Desde or p_Cantidad_Desde is null)
        and ("Cantidad" <= p_Cantidad_Hasta or p_Cantidad_Hasta is null)
        and ("MontoCompras" >= p_MontoCompras_Desde or p_MontoCompras_Desde is null)
        and ("MontoCompras" <= p_MontoCompras_Hasta or p_MontoCompras_Hasta is null)
        and ("MontoVentas" >= p_MontoVentas_Desde or p_MontoVentas_Desde is null)
        and ("MontoVentas" <= p_MontoVentas_Hasta or p_MontoVentas_Hasta is null)
        and ("ValorAdquisicion" >= p_ValorAdquisicion_Desde or p_ValorAdquisicion_Desde is null)
        and ("ValorAdquisicion" <= p_ValorAdquisicion_Hasta or p_ValorAdquisicion_Hasta is null)
        and ("TipoTransaccion" >= p_TipoTransaccion_Desde or p_TipoTransaccion_Desde is null)
        and ("TipoTransaccion" <= p_TipoTransaccion_Hasta or p_TipoTransaccion_Hasta is null)
        and ("IdTipoOperacion" >= p_IdTipoOperacion_Desde or p_IdTipoOperacion_Desde is null)
        and ("IdTipoOperacion" <= p_IdTipoOperacion_Hasta or p_IdTipoOperacion_Hasta is null)
        and ("IdTipoProducto" >= p_IdTipoProducto_Desde or p_IdTipoProducto_Desde is null)
        and ("IdTipoProducto" <= p_IdTipoProducto_Hasta or p_IdTipoProducto_Hasta is null)
        and ("IdBaseDocumento" >= p_IdBaseDocumento_Desde or p_IdBaseDocumento_Desde is null)
        and ("IdBaseDocumento" <= p_IdBaseDocumento_Hasta or p_IdBaseDocumento_Hasta is null)
        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
        and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
        and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
        and ("LineaFactura" >= p_LineaFactura_Desde or p_LineaFactura_Desde is null)
        and ("LineaFactura" <= p_LineaFactura_Hasta or p_LineaFactura_Hasta is null)
        ;
    end if;

	-- Actualizacion Observaciones.
    if x_Observaciones is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1891"
        set "Observaciones" = x_Observaciones 
        where "IdDocumento" = p_IdDocumento
        --where "ID" = p_ID
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)

        and ("TipoDocumento" >= p_TipoDocumento_Desde or p_TipoDocumento_Desde is null)
        and ("TipoDocumento" <= p_TipoDocumento_Hasta or p_TipoDocumento_Hasta is null)
        and ("NumeroDocumento" >= p_NumeroDocumento_Desde or p_NumeroDocumento_Desde is null)
        and ("NumeroDocumento" <= p_NumeroDocumento_Hasta or p_NumeroDocumento_Hasta is null)
        and ("FechaDocumento" >= p_FechaDocumento_Desde or p_FechaDocumento_Desde is null)
        and ("FechaDocumento" <= p_FechaDocumento_Hasta or p_FechaDocumento_Hasta is null)
        and ("RutEmisor" like p_RutEmisor or p_RutEmisor is null)
        and ("RutCliente" like p_RutCliente or p_RutCliente is null)
        and ("Nemotecnico" like p_Nemotecnico or p_Nemotecnico is null)
        and ("Cuenta" like p_Cuenta or p_Cuenta is null)
        and ("Cantidad" >= p_Cantidad_Desde or p_Cantidad_Desde is null)
        and ("Cantidad" <= p_Cantidad_Hasta or p_Cantidad_Hasta is null)
        and ("MontoCompras" >= p_MontoCompras_Desde or p_MontoCompras_Desde is null)
        and ("MontoCompras" <= p_MontoCompras_Hasta or p_MontoCompras_Hasta is null)
        and ("MontoVentas" >= p_MontoVentas_Desde or p_MontoVentas_Desde is null)
        and ("MontoVentas" <= p_MontoVentas_Hasta or p_MontoVentas_Hasta is null)
        and ("ValorAdquisicion" >= p_ValorAdquisicion_Desde or p_ValorAdquisicion_Desde is null)
        and ("ValorAdquisicion" <= p_ValorAdquisicion_Hasta or p_ValorAdquisicion_Hasta is null)
        and ("TipoTransaccion" >= p_TipoTransaccion_Desde or p_TipoTransaccion_Desde is null)
        and ("TipoTransaccion" <= p_TipoTransaccion_Hasta or p_TipoTransaccion_Hasta is null)
        and ("IdTipoOperacion" >= p_IdTipoOperacion_Desde or p_IdTipoOperacion_Desde is null)
        and ("IdTipoOperacion" <= p_IdTipoOperacion_Hasta or p_IdTipoOperacion_Hasta is null)
        and ("IdTipoProducto" >= p_IdTipoProducto_Desde or p_IdTipoProducto_Desde is null)
        and ("IdTipoProducto" <= p_IdTipoProducto_Hasta or p_IdTipoProducto_Hasta is null)
        and ("IdBaseDocumento" >= p_IdBaseDocumento_Desde or p_IdBaseDocumento_Desde is null)
        and ("IdBaseDocumento" <= p_IdBaseDocumento_Hasta or p_IdBaseDocumento_Hasta is null)
        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
        and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
        and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
        and ("LineaFactura" >= p_LineaFactura_Desde or p_LineaFactura_Desde is null)
        and ("LineaFactura" <= p_LineaFactura_Hasta or p_LineaFactura_Hasta is null)
        ;
    end if;

	-- Actualizacion RutCliente.
    if x_RutCliente is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1891"
        set "RutCliente" = x_RutCliente
        where "IdDocumento" = p_IdDocumento
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)

        and ("TipoDocumento" >= p_TipoDocumento_Desde or p_TipoDocumento_Desde is null)
        and ("TipoDocumento" <= p_TipoDocumento_Hasta or p_TipoDocumento_Hasta is null)
        and ("NumeroDocumento" >= p_NumeroDocumento_Desde or p_NumeroDocumento_Desde is null)
        and ("NumeroDocumento" <= p_NumeroDocumento_Hasta or p_NumeroDocumento_Hasta is null)
        and ("FechaDocumento" >= p_FechaDocumento_Desde or p_FechaDocumento_Desde is null)
        and ("FechaDocumento" <= p_FechaDocumento_Hasta or p_FechaDocumento_Hasta is null)
        and ("RutEmisor" like p_RutEmisor or p_RutEmisor is null)
        and ("RutCliente" like p_RutCliente or p_RutCliente is null)
        and ("Nemotecnico" like p_Nemotecnico or p_Nemotecnico is null)
        and ("Cuenta" like p_Cuenta or p_Cuenta is null)
        and ("Cantidad" >= p_Cantidad_Desde or p_Cantidad_Desde is null)
        and ("Cantidad" <= p_Cantidad_Hasta or p_Cantidad_Hasta is null)
        and ("MontoCompras" >= p_MontoCompras_Desde or p_MontoCompras_Desde is null)
        and ("MontoCompras" <= p_MontoCompras_Hasta or p_MontoCompras_Hasta is null)
        and ("MontoVentas" >= p_MontoVentas_Desde or p_MontoVentas_Desde is null)
        and ("MontoVentas" <= p_MontoVentas_Hasta or p_MontoVentas_Hasta is null)
        and ("ValorAdquisicion" >= p_ValorAdquisicion_Desde or p_ValorAdquisicion_Desde is null)
        and ("ValorAdquisicion" <= p_ValorAdquisicion_Hasta or p_ValorAdquisicion_Hasta is null)
        and ("TipoTransaccion" >= p_TipoTransaccion_Desde or p_TipoTransaccion_Desde is null)
        and ("TipoTransaccion" <= p_TipoTransaccion_Hasta or p_TipoTransaccion_Hasta is null)
        and ("IdTipoOperacion" >= p_IdTipoOperacion_Desde or p_IdTipoOperacion_Desde is null)
        and ("IdTipoOperacion" <= p_IdTipoOperacion_Hasta or p_IdTipoOperacion_Hasta is null)
        and ("IdTipoProducto" >= p_IdTipoProducto_Desde or p_IdTipoProducto_Desde is null)
        and ("IdTipoProducto" <= p_IdTipoProducto_Hasta or p_IdTipoProducto_Hasta is null)
        and ("IdBaseDocumento" >= p_IdBaseDocumento_Desde or p_IdBaseDocumento_Desde is null)
        and ("IdBaseDocumento" <= p_IdBaseDocumento_Hasta or p_IdBaseDocumento_Hasta is null)
        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
        and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
        and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
        and ("LineaFactura" >= p_LineaFactura_Desde or p_LineaFactura_Desde is null)
        and ("LineaFactura" <= p_LineaFactura_Hasta or p_LineaFactura_Hasta is null)
        ;
    end if;

	-- Actualizacion RutEmisor.
    if x_RutEmisor is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1891"
        set "RutEmisor" = x_RutEmisor
        where "IdDocumento" = p_IdDocumento
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)

        and ("TipoDocumento" >= p_TipoDocumento_Desde or p_TipoDocumento_Desde is null)
        and ("TipoDocumento" <= p_TipoDocumento_Hasta or p_TipoDocumento_Hasta is null)
        and ("NumeroDocumento" >= p_NumeroDocumento_Desde or p_NumeroDocumento_Desde is null)
        and ("NumeroDocumento" <= p_NumeroDocumento_Hasta or p_NumeroDocumento_Hasta is null)
        and ("FechaDocumento" >= p_FechaDocumento_Desde or p_FechaDocumento_Desde is null)
        and ("FechaDocumento" <= p_FechaDocumento_Hasta or p_FechaDocumento_Hasta is null)
        and ("RutEmisor" like p_RutEmisor or p_RutEmisor is null)
        and ("RutCliente" like p_RutCliente or p_RutCliente is null)
        and ("Nemotecnico" like p_Nemotecnico or p_Nemotecnico is null)
        and ("Cuenta" like p_Cuenta or p_Cuenta is null)
        and ("Cantidad" >= p_Cantidad_Desde or p_Cantidad_Desde is null)
        and ("Cantidad" <= p_Cantidad_Hasta or p_Cantidad_Hasta is null)
        and ("MontoCompras" >= p_MontoCompras_Desde or p_MontoCompras_Desde is null)
        and ("MontoCompras" <= p_MontoCompras_Hasta or p_MontoCompras_Hasta is null)
        and ("MontoVentas" >= p_MontoVentas_Desde or p_MontoVentas_Desde is null)
        and ("MontoVentas" <= p_MontoVentas_Hasta or p_MontoVentas_Hasta is null)
        and ("ValorAdquisicion" >= p_ValorAdquisicion_Desde or p_ValorAdquisicion_Desde is null)
        and ("ValorAdquisicion" <= p_ValorAdquisicion_Hasta or p_ValorAdquisicion_Hasta is null)
        and ("TipoTransaccion" >= p_TipoTransaccion_Desde or p_TipoTransaccion_Desde is null)
        and ("TipoTransaccion" <= p_TipoTransaccion_Hasta or p_TipoTransaccion_Hasta is null)
        and ("IdTipoOperacion" >= p_IdTipoOperacion_Desde or p_IdTipoOperacion_Desde is null)
        and ("IdTipoOperacion" <= p_IdTipoOperacion_Hasta or p_IdTipoOperacion_Hasta is null)
        and ("IdTipoProducto" >= p_IdTipoProducto_Desde or p_IdTipoProducto_Desde is null)
        and ("IdTipoProducto" <= p_IdTipoProducto_Hasta or p_IdTipoProducto_Hasta is null)
        and ("IdBaseDocumento" >= p_IdBaseDocumento_Desde or p_IdBaseDocumento_Desde is null)
        and ("IdBaseDocumento" <= p_IdBaseDocumento_Hasta or p_IdBaseDocumento_Hasta is null)
        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
        and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
        and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
        and ("LineaFactura" >= p_LineaFactura_Desde or p_LineaFactura_Desde is null)
        and ("LineaFactura" <= p_LineaFactura_Hasta or p_LineaFactura_Hasta is null)
        ;
    end if;

	-- Actualizacion TipoDocumento.
    if x_TipoDocumento is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1891"
        set "TipoDocumento" = x_TipoDocumento
        where "IdDocumento" = p_IdDocumento
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)

        and ("TipoDocumento" >= p_TipoDocumento_Desde or p_TipoDocumento_Desde is null)
        and ("TipoDocumento" <= p_TipoDocumento_Hasta or p_TipoDocumento_Hasta is null)
        and ("NumeroDocumento" >= p_NumeroDocumento_Desde or p_NumeroDocumento_Desde is null)
        and ("NumeroDocumento" <= p_NumeroDocumento_Hasta or p_NumeroDocumento_Hasta is null)
        and ("FechaDocumento" >= p_FechaDocumento_Desde or p_FechaDocumento_Desde is null)
        and ("FechaDocumento" <= p_FechaDocumento_Hasta or p_FechaDocumento_Hasta is null)
        and ("RutEmisor" like p_RutEmisor or p_RutEmisor is null)
        and ("RutCliente" like p_RutCliente or p_RutCliente is null)
        and ("Nemotecnico" like p_Nemotecnico or p_Nemotecnico is null)
        and ("Cuenta" like p_Cuenta or p_Cuenta is null)
        and ("Cantidad" >= p_Cantidad_Desde or p_Cantidad_Desde is null)
        and ("Cantidad" <= p_Cantidad_Hasta or p_Cantidad_Hasta is null)
        and ("MontoCompras" >= p_MontoCompras_Desde or p_MontoCompras_Desde is null)
        and ("MontoCompras" <= p_MontoCompras_Hasta or p_MontoCompras_Hasta is null)
        and ("MontoVentas" >= p_MontoVentas_Desde or p_MontoVentas_Desde is null)
        and ("MontoVentas" <= p_MontoVentas_Hasta or p_MontoVentas_Hasta is null)
        and ("ValorAdquisicion" >= p_ValorAdquisicion_Desde or p_ValorAdquisicion_Desde is null)
        and ("ValorAdquisicion" <= p_ValorAdquisicion_Hasta or p_ValorAdquisicion_Hasta is null)
        and ("TipoTransaccion" >= p_TipoTransaccion_Desde or p_TipoTransaccion_Desde is null)
        and ("TipoTransaccion" <= p_TipoTransaccion_Hasta or p_TipoTransaccion_Hasta is null)
        and ("IdTipoOperacion" >= p_IdTipoOperacion_Desde or p_IdTipoOperacion_Desde is null)
        and ("IdTipoOperacion" <= p_IdTipoOperacion_Hasta or p_IdTipoOperacion_Hasta is null)
        and ("IdTipoProducto" >= p_IdTipoProducto_Desde or p_IdTipoProducto_Desde is null)
        and ("IdTipoProducto" <= p_IdTipoProducto_Hasta or p_IdTipoProducto_Hasta is null)
        and ("IdBaseDocumento" >= p_IdBaseDocumento_Desde or p_IdBaseDocumento_Desde is null)
        and ("IdBaseDocumento" <= p_IdBaseDocumento_Hasta or p_IdBaseDocumento_Hasta is null)
        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
        and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
        and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
        and ("LineaFactura" >= p_LineaFactura_Desde or p_LineaFactura_Desde is null)
        and ("LineaFactura" <= p_LineaFactura_Hasta or p_LineaFactura_Hasta is null)
        ;
    end if;

	-- Actualizacion TipoTransaccion.
    if x_TipoTransaccion is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1891"
        set "TipoTransaccion" = x_TipoTransaccion
        where "IdDocumento" = p_IdDocumento
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)

        and ("TipoDocumento" >= p_TipoDocumento_Desde or p_TipoDocumento_Desde is null)
        and ("TipoDocumento" <= p_TipoDocumento_Hasta or p_TipoDocumento_Hasta is null)
        and ("NumeroDocumento" >= p_NumeroDocumento_Desde or p_NumeroDocumento_Desde is null)
        and ("NumeroDocumento" <= p_NumeroDocumento_Hasta or p_NumeroDocumento_Hasta is null)
        and ("FechaDocumento" >= p_FechaDocumento_Desde or p_FechaDocumento_Desde is null)
        and ("FechaDocumento" <= p_FechaDocumento_Hasta or p_FechaDocumento_Hasta is null)
        and ("RutEmisor" like p_RutEmisor or p_RutEmisor is null)
        and ("RutCliente" like p_RutCliente or p_RutCliente is null)
        and ("Nemotecnico" like p_Nemotecnico or p_Nemotecnico is null)
        and ("Cuenta" like p_Cuenta or p_Cuenta is null)
        and ("Cantidad" >= p_Cantidad_Desde or p_Cantidad_Desde is null)
        and ("Cantidad" <= p_Cantidad_Hasta or p_Cantidad_Hasta is null)
        and ("MontoCompras" >= p_MontoCompras_Desde or p_MontoCompras_Desde is null)
        and ("MontoCompras" <= p_MontoCompras_Hasta or p_MontoCompras_Hasta is null)
        and ("MontoVentas" >= p_MontoVentas_Desde or p_MontoVentas_Desde is null)
        and ("MontoVentas" <= p_MontoVentas_Hasta or p_MontoVentas_Hasta is null)
        and ("ValorAdquisicion" >= p_ValorAdquisicion_Desde or p_ValorAdquisicion_Desde is null)
        and ("ValorAdquisicion" <= p_ValorAdquisicion_Hasta or p_ValorAdquisicion_Hasta is null)
        and ("TipoTransaccion" >= p_TipoTransaccion_Desde or p_TipoTransaccion_Desde is null)
        and ("TipoTransaccion" <= p_TipoTransaccion_Hasta or p_TipoTransaccion_Hasta is null)
        and ("IdTipoOperacion" >= p_IdTipoOperacion_Desde or p_IdTipoOperacion_Desde is null)
        and ("IdTipoOperacion" <= p_IdTipoOperacion_Hasta or p_IdTipoOperacion_Hasta is null)
        and ("IdTipoProducto" >= p_IdTipoProducto_Desde or p_IdTipoProducto_Desde is null)
        and ("IdTipoProducto" <= p_IdTipoProducto_Hasta or p_IdTipoProducto_Hasta is null)
        and ("IdBaseDocumento" >= p_IdBaseDocumento_Desde or p_IdBaseDocumento_Desde is null)
        and ("IdBaseDocumento" <= p_IdBaseDocumento_Hasta or p_IdBaseDocumento_Hasta is null)
        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
        and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
        and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
        and ("LineaFactura" >= p_LineaFactura_Desde or p_LineaFactura_Desde is null)
        and ("LineaFactura" <= p_LineaFactura_Hasta or p_LineaFactura_Hasta is null)
        ;
    end if;

	-- Actualizacion VboUsuario.
    if x_VboUsuario is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1891"
        set "VboUsuario" = x_VboUsuario
        where "IdDocumento" = p_IdDocumento
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)

        and ("TipoDocumento" >= p_TipoDocumento_Desde or p_TipoDocumento_Desde is null)
        and ("TipoDocumento" <= p_TipoDocumento_Hasta or p_TipoDocumento_Hasta is null)
        and ("NumeroDocumento" >= p_NumeroDocumento_Desde or p_NumeroDocumento_Desde is null)
        and ("NumeroDocumento" <= p_NumeroDocumento_Hasta or p_NumeroDocumento_Hasta is null)
        and ("FechaDocumento" >= p_FechaDocumento_Desde or p_FechaDocumento_Desde is null)
        and ("FechaDocumento" <= p_FechaDocumento_Hasta or p_FechaDocumento_Hasta is null)
        and ("RutEmisor" like p_RutEmisor or p_RutEmisor is null)
        and ("RutCliente" like p_RutCliente or p_RutCliente is null)
        and ("Nemotecnico" like p_Nemotecnico or p_Nemotecnico is null)
        and ("Cuenta" like p_Cuenta or p_Cuenta is null)
        and ("Cantidad" >= p_Cantidad_Desde or p_Cantidad_Desde is null)
        and ("Cantidad" <= p_Cantidad_Hasta or p_Cantidad_Hasta is null)
        and ("MontoCompras" >= p_MontoCompras_Desde or p_MontoCompras_Desde is null)
        and ("MontoCompras" <= p_MontoCompras_Hasta or p_MontoCompras_Hasta is null)
        and ("MontoVentas" >= p_MontoVentas_Desde or p_MontoVentas_Desde is null)
        and ("MontoVentas" <= p_MontoVentas_Hasta or p_MontoVentas_Hasta is null)
        and ("ValorAdquisicion" >= p_ValorAdquisicion_Desde or p_ValorAdquisicion_Desde is null)
        and ("ValorAdquisicion" <= p_ValorAdquisicion_Hasta or p_ValorAdquisicion_Hasta is null)
        and ("TipoTransaccion" >= p_TipoTransaccion_Desde or p_TipoTransaccion_Desde is null)
        and ("TipoTransaccion" <= p_TipoTransaccion_Hasta or p_TipoTransaccion_Hasta is null)
        and ("IdTipoOperacion" >= p_IdTipoOperacion_Desde or p_IdTipoOperacion_Desde is null)
        and ("IdTipoOperacion" <= p_IdTipoOperacion_Hasta or p_IdTipoOperacion_Hasta is null)
        and ("IdTipoProducto" >= p_IdTipoProducto_Desde or p_IdTipoProducto_Desde is null)
        and ("IdTipoProducto" <= p_IdTipoProducto_Hasta or p_IdTipoProducto_Hasta is null)
        and ("IdBaseDocumento" >= p_IdBaseDocumento_Desde or p_IdBaseDocumento_Desde is null)
        and ("IdBaseDocumento" <= p_IdBaseDocumento_Hasta or p_IdBaseDocumento_Hasta is null)
        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
        and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
        and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
        and ("LineaFactura" >= p_LineaFactura_Desde or p_LineaFactura_Desde is null)
        and ("LineaFactura" <= p_LineaFactura_Hasta or p_LineaFactura_Hasta is null)
        ;
    end if;

end;