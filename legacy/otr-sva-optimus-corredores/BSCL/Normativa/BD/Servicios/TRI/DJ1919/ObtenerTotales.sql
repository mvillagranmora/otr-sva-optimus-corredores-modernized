set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1919.ObtenerTotales";

create procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1919.ObtenerTotales"
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

    -- Filtro del nemotecnico del instrumento.
    in p_NemotecnicoSVS nvarchar(102) default null,

    -- Rango fecha del documento.
    in p_FechaOperacion_Desde date default null,
    in p_FechaOperacion_Hasta date default null,

	in p_TaxID nvarchar(102) default null,

    in p_RutEmisor nvarchar(102) default null,

    -- Valor de emision.
    in p_ValorEmision_Desde decimal(26,8) default null,
    in p_ValorEmision_Hasta decimal(26,8) default null,
    
    -- Valor de colocacion.
    in p_ValorColocacion_Desde decimal(26,8) default null,
    in p_ValorColocacion_Hasta decimal(26,8) default null,
    
    -- Valor de emision.
    in p_ValorAdquisicion_Desde decimal(26,8) default null,
    in p_ValorAdquisicion_Hasta decimal(26,8) default null,
    
    -- Valor de adquisicion en pesos.
    in p_ValorAdquisicionEnPesos_Desde decimal(26,8) default null,
    in p_ValorAdquisicionEnPesos_Hasta decimal(26,8) default null,
    
    -- Valor de enajenacion.
    in p_ValorEnajenacion_Desde decimal(26,8) default null,
    in p_ValorEnajenacion_Hasta decimal(26,8) default null,
    
    -- Valor de enajenacion en pesos.
    in p_ValorEnajenacionEnPesos_Desde decimal(26,8) default null,
    in p_ValorEnajenacionEnPesos_Hasta decimal(26,8) default null,
    
    -- Mayor valor de enajenacion.
    in p_MayorValorEnajenacion_Desde decimal(26,8) default null,
    in p_MayorValorEnajenacion_Hasta decimal(26,8) default null,
    
    -- Valor valor de enajenacion en pesos.
    in p_MayorValorEnajenacionEnPesos_Desde decimal(26,8) default null,
    in p_MayorValorEnajenacionEnPesos_Hasta decimal(26,8) default null,
    
    in p_Moneda nvarchar(3) default null,
    
    in x_CodigoValidacion nvarchar(40) default null,

    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    out p_out_tbl table (
        "TYPE" nvarchar(100),
        "COUNT" bigint,
        "ValorEmision" decimal(26,8),
        "ValorColocacion"   decimal(26,8),
        "ValorAdquisicion" decimal(26,8),
        "ValorAdquisicionEnPesos" decimal(26,8),
        "ValorEnajenacion" decimal(26,8),
        "ValorEnajenacionEnPesos" decimal(26,8),
        "MayorValorEnajenacion" decimal(26,8),
        "MayorValorEnajenacionEnPesos" decimal(26,8)
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
	                    sum("ValorEmision") as "ValorEmision",
	                    sum("ValorColocacion") as "ValorColocacion",
	                    sum("ValorAdquisicion") as "ValorAdquisicion",
	                    sum("ValorAdquisicionEnPesos") as "ValorAdquisicionEnPesos",
	                    sum("ValorEnajenacion") as "ValorEnajenacion",
	                    sum("ValorEnajenacionEnPesos") as "ValorEnajenacionEnPesos",
	                    sum("MayorValorEnajenacion") as "MayorValorEnajenacion",
	                    sum("MayorValorEnajenacionEnPesos") as "MayorValorEnajenacionEnPesos"
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1919"
	                    where "IdDocumento" = p_IdDocumento
	                union all
	                select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("ValorEmision") as "ValorEmision",
	                    sum("ValorColocacion") as "ValorColocacion",
	                    sum("ValorAdquisicion") as "ValorAdquisicion",
	                    sum("ValorAdquisicionEnPesos") as "ValorAdquisicionEnPesos",
	                    sum("ValorEnajenacion") as "ValorEnajenacion",
	                    sum("ValorEnajenacionEnPesos") as "ValorEnajenacionEnPesos",
	                    sum("MayorValorEnajenacion") as "MayorValorEnajenacion",
	                    sum("MayorValorEnajenacionEnPesos") as "MayorValorEnajenacionEnPesos"
	
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1919"
	                    where "IdDocumento" = p_IdDocumento
	                    and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
	                    and ("Observaciones" like p_Observaciones or p_Observaciones is null)
	                    and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)
	
	                    and ("RutCliente" like p_RutCliente or p_RutCliente is null)
	                    and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
	                    and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
	                    and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
	                    and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
	                    and ("NemotecnicoSVS" like p_NemotecnicoSVS or p_NemotecnicoSVS is null)
	                    and ("FechaOperacion" >= p_FechaOperacion_Desde or p_FechaOperacion_Desde is null)
	                    and ("FechaOperacion" <= p_FechaOperacion_Hasta or p_FechaOperacion_Hasta is null)
	                    and ("ValorColocacion" >= p_ValorColocacion_Desde or p_ValorColocacion_Desde is null)
	                    and ("ValorColocacion" <= p_ValorColocacion_Hasta or p_ValorColocacion_Hasta is null)
	                    and ("ValorAdquisicion" >= p_ValorAdquisicion_Desde or p_ValorAdquisicion_Desde is null)
	                    and ("ValorAdquisicion" <= p_ValorAdquisicion_Hasta or p_ValorAdquisicion_Hasta is null)
	                    and ("ValorAdquisicionEnPesos" >= p_ValorAdquisicionEnPesos_Desde or p_ValorAdquisicionEnPesos_Desde is null)
	                    and ("ValorAdquisicionEnPesos" <= p_ValorAdquisicionEnPesos_Hasta or p_ValorAdquisicionEnPesos_Hasta is null)
	                    and ("ValorEnajenacion" >= p_ValorEnajenacion_Desde or p_ValorEnajenacion_Desde is null)
	                    and ("ValorEnajenacion" <= p_ValorEnajenacion_Hasta or p_ValorEnajenacion_Hasta is null)
	                    and ("ValorEnajenacionEnPesos" >= p_ValorEnajenacionEnPesos_Desde or p_ValorEnajenacionEnPesos_Desde is null)
	                    and ("ValorEnajenacionEnPesos" <= p_ValorEnajenacionEnPesos_Hasta or p_ValorEnajenacionEnPesos_Hasta is null)
	                    and ("MayorValorEnajenacion" >= p_MayorValorEnajenacion_Desde or p_MayorValorEnajenacion_Desde is null)
	                    and ("MayorValorEnajenacion" <= p_MayorValorEnajenacion_Hasta or p_MayorValorEnajenacion_Hasta is null)
	                    and ("MayorValorEnajenacionEnPesos" >= p_MayorValorEnajenacionEnPesos_Desde or p_MayorValorEnajenacionEnPesos_Desde is null)
	                    and ("MayorValorEnajenacionEnPesos" <= p_MayorValorEnajenacionEnPesos_Hasta or p_MayorValorEnajenacionEnPesos_Hasta is null)
	                    and ("Moneda" like p_Moneda or p_Moneda is null);
      else
	    p_out_tbl = select
	                    'TOTAL' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("ValorEmision") as "ValorEmision",
	                    sum("ValorColocacion") as "ValorColocacion",
	                    sum("ValorAdquisicion") as "ValorAdquisicion",
	                    sum("ValorAdquisicionEnPesos") as "ValorAdquisicionEnPesos",
	                    sum("ValorEnajenacion") as "ValorEnajenacion",
	                    sum("ValorEnajenacionEnPesos") as "ValorEnajenacionEnPesos",
	                    sum("MayorValorEnajenacion") as "MayorValorEnajenacion",
	                    sum("MayorValorEnajenacionEnPesos") as "MayorValorEnajenacionEnPesos"
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1919"
	                    where "IdDocumento" = p_IdDocumento
	                union all
	                select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("ValorEmision") as "ValorEmision",
	                    sum("ValorColocacion") as "ValorColocacion",
	                    sum("ValorAdquisicion") as "ValorAdquisicion",
	                    sum("ValorAdquisicionEnPesos") as "ValorAdquisicionEnPesos",
	                    sum("ValorEnajenacion") as "ValorEnajenacion",
	                    sum("ValorEnajenacionEnPesos") as "ValorEnajenacionEnPesos",
	                    sum("MayorValorEnajenacion") as "MayorValorEnajenacion",
	                    sum("MayorValorEnajenacionEnPesos") as "MayorValorEnajenacionEnPesos"
	
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1919"
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
