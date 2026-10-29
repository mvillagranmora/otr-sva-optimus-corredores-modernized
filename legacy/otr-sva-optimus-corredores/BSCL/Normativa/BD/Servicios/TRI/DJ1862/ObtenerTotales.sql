set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1862.ObtenerTotales";

create procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1862.ObtenerTotales"
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

    in p_RutCliente nvarchar(42) default null,

    -- Rango del ID cliente.
    in p_IdCliente_Desde bigint default null,
    in p_IdCliente_Hasta bigint default null,

    -- Rango del ID portafolio.
    in p_IdPortafolio_Desde bigint default null,
    in p_IdPortafolio_Hasta bigint default null,

 	in p_FechaOperacion_Desde date default null,
    in p_FechaOperacion_Hasta date default null,

    in x_CodigoValidacion nvarchar(40) default null,
    
    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    out p_out_tbl table (
        "TYPE" nvarchar(100),
        "COUNT" bigint,
        "TotalRegistros" bigint
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
	                    count(*) as "TotalRegistros"
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1919"
	                    where "IdDocumento" = p_IdDocumento
	    			union all
	                select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                    count(*) as "TotalRegistros"
	                    
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1862"
	                    where "IdDocumento" = p_IdDocumento
	                    and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
	                    and ("Observaciones" like p_Observaciones or p_Observaciones is null)
	                    and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)
	
	                    and ("RutCliente" like p_RutCliente or p_RutCliente is null)
	                    and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
	                    and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
	                    and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
	                    and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null);
      else
	    p_out_tbl = select
	                    'TOTAL' as "TYPE",
	                    count(*) as "COUNT",
	                    count(*) as "TotalRegistros"
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1862"
	                    where "IdDocumento" = p_IdDocumento
	                union all
	                select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                    count(*) as "TotalRegistros"
	                    
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1862"
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
