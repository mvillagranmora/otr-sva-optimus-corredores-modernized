set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1820.ObtenerTotales";

create procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1820.ObtenerTotales"
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
    
    -- Rango fecha del Operacion.
    in p_FechaOperacion_Desde date default null,
    in p_FechaOperacion_Hasta date default null,


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
    in p_Monto_Desde decimal(15,0) default null,
    in p_Monto_Hasta decimal(15,0) default null,

    in x_CodigoValidacion nvarchar(40) default null,

    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    out p_out_tbl table (
        "TYPE" nvarchar(100),
        "COUNT" bigint,
        --"Cantidad" bigint,
        "MontoNocional" bigint
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
	                   -- 0 as "Cantidad",
	                    --0 as "MontoNocional"
	                    sum("MontoNocional") as "MontoNocional"
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1820"
	                    where (TO_NVARCHAR("FechaOperacion",'YYYY-MM-DD') between TO_NVARCHAR(:p_FechaOperacion_Desde,'YYYY-MM-DD') and TO_NVARCHAR(:p_FechaOperacion_Hasta,'YYYY-MM-DD')) OR ("IdDocumento" = :p_IdDocumento and p_FechaOperacion_Hasta is null)  
	                  --  and ("RutCliente" like :p_RutCliente OR  :p_RutCliente is null)

	                union all
	                 select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                    --sum("Cantidad") as "Cantidad",
	                    sum("MontoNocional") as "MontoNocional"
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1820"
	                    where (TO_NVARCHAR("FechaOperacion",'YYYY-MM-DD') between TO_NVARCHAR(:p_FechaOperacion_Desde,'YYYY-MM-DD') and TO_NVARCHAR(:p_FechaOperacion_Hasta,'YYYY-MM-DD')) OR ("IdDocumento" = :p_IdDocumento and p_FechaOperacion_Hasta is null)  
	                    and ("RutCliente" like :p_RutCliente OR  :p_RutCliente is null)
	                ;
      else
	    p_out_tbl = select
	                    'TOTAL' as "TYPE",
	                    count(*) as "COUNT",
	                    --0 as "Cantidad",
	                    0 as "MontoNocional"
	                    from DUMMY
	                union all
	                   select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                   -- sum("Cantidad") as "Cantidad",
	                    sum("MontoNocional") as "MontoNocional"
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1820"
	                   
	                ;
      end if;
  end;

 
 
 