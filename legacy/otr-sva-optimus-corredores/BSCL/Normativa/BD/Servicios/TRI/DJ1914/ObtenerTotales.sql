set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1914.ObtenerTotales";

create procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1914.ObtenerTotales"
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

 	in p_FechaInversionOriginal_Desde date default null,
    in p_FechaInversionOriginal_Hasta date default null,

	in p_InversionReinversion54BISMontoNominal_Desde decimal(26,8) default null,
    in p_InversionReinversion54BISMontoNominal_Hasta decimal(26,8) default null,
    
    in p_InversionReinversion54BISInteresPositivo_Desde decimal(26,8) default null,
    in p_InversionReinversion54BISInteresPositivo_Hasta decimal(26,8) default null,
    
    in p_InversionReinversion54BISInteresNegativo_Desde decimal(26,8) default null,
    in p_InversionReinversion54BISInteresNegativo_Hasta decimal(26,8) default null,
    
    in p_RetiroInversion54BISMontoNominal_Desde decimal(26,8) default null,
    in p_RetiroInversion54BISMontoNominal_Hasta decimal(26,8) default null,
    
    in p_RetiroInversion54BISInteresPositivo_Desde decimal(26,8) default null,
    in p_RetiroInversion54BISInteresPositivo_Hasta decimal(26,8) default null,
    
    in p_RetiroInversion54BISInteresNegativo_Desde decimal(26,8) default null,
    in p_RetiroInversion54BISInteresNegativo_Hasta decimal(26,8) default null,
        
    in x_CodigoValidacion nvarchar(40) default null,
    
    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    out p_out_tbl table (
        "TYPE" nvarchar(100),
        "COUNT" bigint,
        "InversionReinversion54BISMontoNominal" decimal(26,8),
        "InversionReinversion54BISInteresPositivo" decimal(26,8),
        "InversionReinversion54BISInteresNegativo" decimal(26,8),
        "RetiroInversion54BISMontoNominal" decimal(26,8),
        "RetiroInversion54BISInteresPositivo" decimal(26,8),
        "RetiroInversion54BISInteresNegativo" decimal(26,8)
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
	                    sum("InversionReinversion54BISMontoNominal") as "InversionReinversion54BISMontoNominal",
	                    sum("InversionReinversion54BISInteresPositivo") as "InversionReinversion54BISInteresPositivo",
	                    sum("InversionReinversion54BISInteresNegativo") as "InversionReinversion54BISInteresNegativo",
	                    sum("RetiroInversion54BISMontoNominal") as "RetiroInversion54BISMontoNominal",
	                    sum("RetiroInversion54BISInteresPositivo") as "RetiroInversion54BISInteresPositivo",
	                    sum("RetiroInversion54BISInteresNegativo") as "RetiroInversion54BISInteresNegativo"
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1914"
	                    where "IdDocumento" = p_IdDocumento
	    			union all
	                select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("InversionReinversion54BISMontoNominal") as "InversionReinversion54BISMontoNominal",
	                    sum("InversionReinversion54BISInteresPositivo") as "InversionReinversion54BISInteresPositivo",
	                    sum("InversionReinversion54BISInteresNegativo") as "InversionReinversion54BISInteresNegativo",
	                    sum("RetiroInversion54BISMontoNominal") as "RetiroInversion54BISMontoNominal",
	                    sum("RetiroInversion54BISInteresPositivo") as "RetiroInversion54BISInteresPositivo",
	                    sum("RetiroInversion54BISInteresNegativo") as "RetiroInversion54BISInteresNegativo"
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1914"
	                    where "IdDocumento" = p_IdDocumento
	                    and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
	                    and ("Observaciones" like p_Observaciones or p_Observaciones is null)
	                    and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)
	
	                    and ("RutCliente" like p_RutCliente or p_RutCliente is null)
	                    and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
	                    and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
	                    and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
	                    and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
	                    and ("InversionReinversion54BISMontoNominal" >= p_InversionReinversion54BISMontoNominal_Desde or p_InversionReinversion54BISMontoNominal_Desde is null)
	                    and ("InversionReinversion54BISMontoNominal" <= p_InversionReinversion54BISMontoNominal_Hasta or p_InversionReinversion54BISMontoNominal_Hasta is null)
	                    and ("InversionReinversion54BISInteresPositivo" >= p_InversionReinversion54BISInteresPositivo_Desde or p_InversionReinversion54BISInteresPositivo_Desde is null)
	                    and ("InversionReinversion54BISInteresPositivo" <= p_InversionReinversion54BISInteresPositivo_Hasta or p_InversionReinversion54BISInteresPositivo_Hasta is null)
	                    and ("InversionReinversion54BISInteresNegativo" >= p_InversionReinversion54BISInteresNegativo_Desde or p_InversionReinversion54BISInteresNegativo_Desde is null)
	                    and ("InversionReinversion54BISInteresNegativo" <= p_InversionReinversion54BISInteresNegativo_Hasta or p_InversionReinversion54BISInteresNegativo_Hasta is null)
	                    and ("RetiroInversion54BISMontoNominal" >= p_RetiroInversion54BISMontoNominal_Desde or p_RetiroInversion54BISMontoNominal_Desde is null)
	                    and ("RetiroInversion54BISMontoNominal" <= p_RetiroInversion54BISMontoNominal_Hasta or p_RetiroInversion54BISMontoNominal_Hasta is null)
	                    and ("RetiroInversion54BISInteresPositivo" >= p_RetiroInversion54BISInteresPositivo_Desde or p_RetiroInversion54BISInteresPositivo_Desde is null)
	                    and ("RetiroInversion54BISInteresPositivo" <= p_RetiroInversion54BISInteresPositivo_Hasta or p_RetiroInversion54BISInteresPositivo_Hasta is null)
	                    and ("RetiroInversion54BISInteresNegativo" >= p_RetiroInversion54BISInteresNegativo_Desde or p_RetiroInversion54BISInteresNegativo_Desde is null)
	                    and ("RetiroInversion54BISInteresNegativo" <= p_RetiroInversion54BISInteresNegativo_Hasta or p_RetiroInversion54BISInteresNegativo_Hasta is null);
      else
	    p_out_tbl = select
	                    'TOTAL' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("InversionReinversion54BISMontoNominal") as "InversionReinversion54BISMontoNominal",
	                    sum("InversionReinversion54BISInteresPositivo") as "InversionReinversion54BISInteresPositivo",
	                    sum("InversionReinversion54BISInteresNegativo") as "InversionReinversion54BISInteresNegativo",
	                    sum("RetiroInversion54BISMontoNominal") as "RetiroInversion54BISMontoNominal",
	                    sum("RetiroInversion54BISInteresPositivo") as "RetiroInversion54BISInteresPositivo",
	                    sum("RetiroInversion54BISInteresNegativo") as "RetiroInversion54BISInteresNegativo"
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1914"
	                    where "IdDocumento" = p_IdDocumento
	                union all
	                select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("InversionReinversion54BISMontoNominal") as "InversionReinversion54BISMontoNominal",
	                    sum("InversionReinversion54BISInteresPositivo") as "InversionReinversion54BISInteresPositivo",
	                    sum("InversionReinversion54BISInteresNegativo") as "InversionReinversion54BISInteresNegativo",
	                    sum("RetiroInversion54BISMontoNominal") as "RetiroInversion54BISMontoNominal",
	                    sum("RetiroInversion54BISInteresPositivo") as "RetiroInversion54BISInteresPositivo",
	                    sum("RetiroInversion54BISInteresNegativo") as "RetiroInversion54BISInteresNegativo"
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1914"
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
