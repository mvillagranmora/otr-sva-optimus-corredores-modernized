set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1891.ObtenerTotales";

create procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1891.ObtenerTotales"
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

    -- Rango del ID cliente.
    in p_IdCliente_Desde bigint default null,
    in p_IdCliente_Hasta bigint default null,

    -- Rango del ID portafolio.
    in p_IdPortafolio_Desde bigint default null,
    in p_IdPortafolio_Hasta bigint default null,

    -- Rango numero del documento.
    in p_NumeroDocumento_Desde bigint default null,
    in p_NumeroDocumento_Hasta bigint default null,

    -- Rango tipo de documento a declarar.
    in p_TipoDocumento_Desde nvarchar(10) default null,
    in p_TipoDocumento_Hasta nvarchar(10) default null,

    -- Rango del tipo de transaccion.
    in p_TipoTransaccion_Desde nvarchar(6) default null,
    in p_TipoTransaccion_Hasta nvarchar(6) default null,

    -- Rango del tipo de operacion.
    in p_IdTipoOperacion_Desde nvarchar(6) default null,
    in p_IdTipoOperacion_Hasta nvarchar(6) default null,

    -- Rango del tipo de documento.
    in p_IdTipoProducto_Desde nvarchar(4) default null,
    in p_IdTipoProducto_Hasta nvarchar(4) default null,

    -- Rango fecha del documento.
    in p_FechaDocumento_Desde date default null,
    in p_FechaDocumento_Hasta date default null,

    -- Filtro RUT del emisor.
    in p_RutEmisor nvarchar(42) default null,

    -- Filtro del RUT del cliente.
    in p_RutCliente nvarchar(42) default null,

    -- Filtro del nemotecnico del instrumento.
    in p_Nemotecnico nvarchar(102) default null,

    -- Rango de la cantidad de acciones.
    in p_Cantidad_Desde bigint default null,
    in p_Cantidad_Hasta bigint default null,

    -- Rango del monto.
--    in p_Monto_Desde decimal(15,0) default null,
---   in p_Monto_Hasta decimal(15,0) default null,
	-- Rango Monto Compras
	IN p_MontoCompras_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_MontoCompras_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Monto Ventas
	IN p_MontoVentas_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_MontoVentas_Hasta DECIMAL(26,8) DEFAULT NULL,

	-- Rango Monto Adquisicion
	IN p_ValorAdquisicion_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_ValorAdquisicion_Hasta DECIMAL(26,8) DEFAULT NULL,

    in x_CodigoValidacion nvarchar(40) default null,

    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    out p_out_tbl table (
        "TYPE" nvarchar(100),
        "COUNT" bigint,
        "Cantidad" bigint,
    --    "Monto" bigint
        "MontoCompras" 				DECIMAL(26,8),
        "MontoVentas" 				DECIMAL(26,8),
        "ValorAdquisicion" 				DECIMAL(26,8)
    
    )
)
    language sqlscript
    sql security invoker
    default schema "BSCL"
as
begin
    ------------ Logica de Consulta ----------

	if x_CodigoValidacion is null then
	    p_out_tbl = select
	                    'TOTAL' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("Cantidad") as "Cantidad",
	                    --sum("Monto") as "Monto"
	                    SUM("MontoCompras") AS "MontoCompras",
	                    SUM("MontoVentas") AS "MontoVentas",
	                    SUM("ValorAdquisicion") AS "ValorAdquisicion"
	                    
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1891"
	                    where "IdDocumento" = p_IdDocumento
	                union all
	                select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("Cantidad") as "Cantidad",
--	                    sum("Monto") as "Monto"
	                    SUM("MontoCompras") AS "MontoCompras",
	                    SUM("MontoVentas") AS "MontoVentas",
	                    SUM("ValorAdquisicion") AS "ValorAdquisicion"

	                    from "BSCL.Normativa.BD::TRI.DatosDJ1891"
	                    where "IdDocumento" = p_IdDocumento
	                    and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
	                    and ("Observaciones" like p_Observaciones or p_Observaciones is null)
	                    and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)
	                    and ("RutEmisor" like p_RutEmisor or p_RutEmisor is null)
	                    and (trim("RutCliente") like trim(p_RutCliente) or p_RutCliente is null)
	                    and ("Nemotecnico" like p_Nemotecnico or p_Nemotecnico is null)
	                    and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
	                    and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
	                    and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
	                    and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
	                    and ("NumeroDocumento" >= p_NumeroDocumento_Desde or p_NumeroDocumento_Desde is null)
	                    and ("NumeroDocumento" <= p_NumeroDocumento_Hasta or p_NumeroDocumento_Hasta is null)
	                    and ("TipoDocumento" >= p_TipoDocumento_Desde or p_TipoDocumento_Desde is null)
	                    and ("TipoDocumento" <= p_TipoDocumento_Hasta or p_TipoDocumento_Hasta is null)
	                    and ("TipoTransaccion" >= p_TipoTransaccion_Desde or p_TipoTransaccion_Desde is null)
	                    and ("TipoTransaccion" <= p_TipoTransaccion_Hasta or p_TipoTransaccion_Hasta is null)
	                    and ("IdTipoOperacion" >= p_IdTipoOperacion_Desde or p_IdTipoOperacion_Desde is null)
	                    and ("IdTipoOperacion" <= p_IdTipoOperacion_Hasta or p_IdTipoOperacion_Hasta is null)
	                    and ("IdTipoProducto" >= p_IdTipoProducto_Desde or p_IdTipoProducto_Desde is null)
	                    and ("IdTipoProducto" <= p_IdTipoProducto_Hasta or p_IdTipoProducto_Hasta is null)
	                    and ("FechaDocumento" >= p_FechaDocumento_Desde or p_FechaDocumento_Desde is null)
	                    and ("FechaDocumento" <= p_FechaDocumento_Hasta or p_FechaDocumento_Hasta is null)
	                    and ("Cantidad" >= p_Cantidad_Desde or p_Cantidad_Desde is null)
	                    and ("Cantidad" <= p_Cantidad_Hasta or p_Cantidad_Hasta is null)
	                    and ("MontoCompras" >= p_MontoCompras_Desde or p_MontoCompras_Desde is null)
	                    and ("MontoCompras" <= p_MontoCompras_Hasta or p_MontoCompras_Hasta is null)
	                    
	                    and ("MontoVentas" >= p_MontoVentas_Desde or p_MontoVentas_Desde is null)
	                    and ("MontoVentas" <= p_MontoVentas_Hasta or p_MontoVentas_Hasta is null)
	                    and ("ValorAdquisicion" >= p_ValorAdquisicion_Desde or p_ValorAdquisicion_Desde is null)
	                    and ("ValorAdquisicion" <= p_ValorAdquisicion_Hasta or p_ValorAdquisicion_Hasta is null)
	                ;
      else
	    p_out_tbl = select
	                    'TOTAL' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("Cantidad") as "Cantidad",
--	                    sum("Monto") as "Monto"
	                    SUM("MontoCompras") AS "MontoCompras",
	                    SUM("MontoVentas") AS "MontoVentas",
	                    SUM("ValorAdquisicion") AS "ValorAdquisicion"

	                    from "BSCL.Normativa.BD::TRI.DatosDJ1891"
	                    where "IdDocumento" = p_IdDocumento
	                union all
	                select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("Cantidad") as "Cantidad",
--	                    sum("Monto") as "Monto"
	                    SUM("MontoCompras") AS "MontoCompras",
	                    SUM("MontoVentas") AS "MontoVentas",
	                    SUM("ValorAdquisicion") AS "ValorAdquisicion"

	                    from "BSCL.Normativa.BD::TRI.DatosDJ1891"
	                    where "IdDocumento" = p_IdDocumento
	                    and "ID" in (
	                        select a."IdDetalleDocumento"
	                        from "BSCL.Normativa.BD::TRI.DetalleValidacion" a
	                        left join "BSCL.Normativa.BD::TRI.ResumenValidacion" b
	                        on a."IdResumenValidacion"=b."ID"
	                        where b."CodigoValidacion"=x_CodigoValidacion
	                    )
	                ;
      end if;
  end;