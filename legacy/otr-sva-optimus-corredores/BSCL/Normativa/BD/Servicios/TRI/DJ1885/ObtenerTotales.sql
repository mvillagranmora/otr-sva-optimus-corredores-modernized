set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1885.ObtenerTotales";

create procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1885.ObtenerTotales"
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

    -- Filtro del RUT del cliente.
    in p_RutCliente nvarchar(42) default null,

    -- Rango del ID cliente.
    in p_IdCliente_Desde bigint default null,
    in p_IdCliente_Hasta bigint default null,

    -- Rango del ID portafolio.
    in p_IdPortafolio_Desde bigint default null,
    in p_IdPortafolio_Hasta bigint default null,

    in p_TipoInstrumento_Desde nvarchar(40) default null,
    in p_TipoInstrumento_Hasta nvarchar(40) default null,

    in p_TipoRegistro_Desde nvarchar(1) default null,
    in p_TipoRegistro_Hasta nvarchar(1) default null,

    -- Filtro del nemotecnico del instrumento.
    in p_Nemotecnico nvarchar(102) default null,

    -- Rango fecha del documento.
    in p_FechaPago_Desde date default null,
    in p_FechaPago_Hasta date default null,

    -- Rango numero del documento.
    in p_SecuenciaEvento_Desde bigint default null,
    in p_SecuenciaEvento_Hasta bigint default null,

    -- Filtro del RUT del cliente.
    in p_RutSociedad nvarchar(42) default null,

    in p_RazonSocial nvarchar(102) default null,

    in p_TipoSociedad_desde nvarchar(40) default null,
    in p_TipoSociedad_hasta nvarchar(40) default null,

    -- Rango de la cantidad de acciones.
    in p_CantidadAcciones31Dic_Desde bigint default null,
    in p_CantidadAcciones31Dic_Hasta bigint default null,

    -- Rango del monto.
    in p_MontoPagado_Desde bigint default null,
    in p_MontoPagado_Hasta bigint default null,

    -- Rango del monto.
    in p_MontoRecibido_Desde bigint default null,
    in p_MontoRecibido_Hasta bigint default null,

    in p_DividendosAfectos_Desde bigint default null,
    in p_DividendosAfectos_Hasta bigint default null,

    in p_DividendosExentos_Desde bigint default null,
    in p_DividendosExentos_Hasta bigint default null,

    in p_DividendosNoRentaNoAcogidos_Desde bigint default null,
    in p_DividendosNoRentaNoAcogidos_Hasta bigint default null,

    in p_DividendosNoRentaAcogidos_Desde bigint default null,
    in p_DividendosNoRentaAcogidos_Hasta bigint default null,

    in p_IncrementoImpuesto_Desde bigint default null,
    in p_IncrementoImpuesto_Hasta bigint default null,

    in p_CreditosAfectosConDevolucion_Desde bigint default null,
    in p_CreditosAfectosConDevolucion_Hasta bigint default null,

    in p_CreditosAfectosSinDevolucion_Desde bigint default null,
    in p_CreditosAfectosSinDevolucion_Hasta bigint default null,

    in p_CreditosExentosConDevolucion_Desde bigint default null,
    in p_CreditosExentosConDevolucion_Hasta bigint default null,

    in p_CreditosExentosSinDevolucion_Desde bigint default null,
    in p_CreditosExentosSinDevolucion_Hasta bigint default null,

    in p_CreditosImpuestosExternos_Desde bigint default null,
    in p_CreditosImpuestosExternos_Hasta bigint default null,

    in p_CreditosImpuestoTasaAdicional_Desde bigint default null,
    in p_CreditosImpuestoTasaAdicional_Hasta bigint default null,

    in p_NumeroCertificado_Desde bigint default null,
    in p_NumeroCertificado_Hasta bigint default null,

/*  in p_CantidadDerecho            bigint,
    in p_IdInstrumento              bigint,
    in p_IdEventoCapitalCliente     bigint,
*/
    --CLT, COR, CIA, BOL, CAM, CUS,
    in p_TipoSocioNegocio nvarchar(1) default null,

    in p_TipoCartera nvarchar(3) default null,

    in p_IdMoneda nvarchar(3) default null,
    
    in x_CodigoValidacion nvarchar(40) default null,

    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    out p_out_tbl table (
        "TYPE" nvarchar(100),
        "COUNT" bigint,
        "CantidadAcciones31Dic" bigint,
        "MontoPagado"   bigint,
        "MontoRecibido" bigint,
        "DividendosAfectos" bigint,
        "DividendosExentos" bigint,
        "DividendosNoRentaNoAcogidos" bigint,
        "DividendosNoRentaAcogidos" bigint,
        "IncrementoImpuesto" bigint,
        "CreditosAfectosConDevolucion" bigint,
        "CreditosAfectosSinDevolucion" bigint,
        "CreditosExentosConDevolucion" bigint,
        "CreditosExentosSinDevolucion" bigint,
        "CreditosImpuestosExternos" bigint,
        "CreditosImpuestoTasaAdicional" bigint
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
	                    sum("CantidadAcciones31Dic") as "CantidadAcciones31Dic",
	                    sum("MontoPagado") as "MontoPagado",
	                    sum("MontoRecibido") as "MontoRecibido",
	                    sum("DividendosAfectos") as "DividendosAfectos",
	                    sum("DividendosExentos") as "DividendosExentos",
	                    sum("DividendosNoRentaNoAcogidos") as "DividendosNoRentaNoAcogidos",
	                    sum("DividendosNoRentaAcogidos") as "DividendosNoRentaAcogidos",
	                    sum("IncrementoImpuesto") as "IncrementoImpuesto",
	                    sum("CreditosAfectosConDevolucion") as "CreditosAfectosConDevolucion",
	                    sum("CreditosAfectosSinDevolucion") as "CreditosAfectosSinDevolucion",
	                    sum("CreditosExentosConDevolucion") as "CreditosExentosConDevolucion",
	                    sum("CreditosExentosSinDevolucion") as "CreditosExentosSinDevolucion",
	                    sum("CreditosImpuestosExternos") as "CreditosImpuestosExternos",
	                    sum("CreditosImpuestoTasaAdicional") as "CreditosImpuestoTasaAdicional"
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1885"
	                    where "IdDocumento" = p_IdDocumento
	                union all
	                select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("CantidadAcciones31Dic") as "CantidadAcciones31Dic",
	                    sum("MontoPagado") as "MontoPagado",
	                    sum("MontoRecibido") as "MontoRecibido",
	                    sum("DividendosAfectos") as "DividendosAfectos",
	                    sum("DividendosExentos") as "DividendosExentos",
	                    sum("DividendosNoRentaNoAcogidos") as "DividendosNoRentaNoAcogidos",
	                    sum("DividendosNoRentaAcogidos") as "DividendosNoRentaAcogidos",
	                    sum("IncrementoImpuesto") as "IncrementoImpuesto",
	                    sum("CreditosAfectosConDevolucion") as "CreditosAfectosConDevolucion",
	                    sum("CreditosAfectosSinDevolucion") as "CreditosAfectosSinDevolucion",
	                    sum("CreditosExentosConDevolucion") as "CreditosExentosConDevolucion",
	                    sum("CreditosExentosSinDevolucion") as "CreditosExentosSinDevolucion",
	                    sum("CreditosImpuestosExternos") as "CreditosImpuestosExternos",
	                    sum("CreditosImpuestoTasaAdicional") as "CreditosImpuestoTasaAdicional"
	
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1885"
	                    where "IdDocumento" = p_IdDocumento
	                    and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
	                    and ("Observaciones" like p_Observaciones or p_Observaciones is null)
	                    and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)
	
	                    and ("RutCliente" like p_RutCliente or p_RutCliente is null)
	                    and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
	                    and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
	                    and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
	                    and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
	                    and ("TipoInstrumento" >= p_TipoInstrumento_Desde or p_TipoInstrumento_Desde is null)
	                    and ("TipoInstrumento" <= p_TipoInstrumento_Hasta or p_TipoInstrumento_Hasta is null)
	                    and ("TipoRegistro" >= p_TipoRegistro_Desde or p_TipoRegistro_Desde is null)
	                    and ("TipoRegistro" <= p_TipoRegistro_Hasta or p_TipoRegistro_Hasta is null)
	                    and ("Nemotecnico" like p_Nemotecnico or p_Nemotecnico is null)
	                    and ("FechaPago" >= p_FechaPago_Desde or p_FechaPago_Desde is null)
	                    and ("FechaPago" <= p_FechaPago_Hasta or p_FechaPago_Hasta is null)
	                    and ("SecuenciaEvento" >= p_SecuenciaEvento_Desde or p_SecuenciaEvento_Desde is null)
	                    and ("SecuenciaEvento" <= p_SecuenciaEvento_Hasta or p_SecuenciaEvento_Hasta is null)
	                    and ("RutSociedad" like p_RutSociedad or p_RutSociedad is null)
	                    and ("RazonSocial" like p_RazonSocial or p_RazonSocial is null)
	                    and ("TipoSociedad" >= p_TipoSociedad_desde or p_TipoSociedad_desde is null)
	                    and ("TipoSociedad" <= p_TipoSociedad_hasta or p_TipoSociedad_hasta is null)
	                    and ("CantidadAcciones31Dic" >= p_CantidadAcciones31Dic_Desde or p_CantidadAcciones31Dic_Desde is null)
	                    and ("CantidadAcciones31Dic" <= p_CantidadAcciones31Dic_Hasta or p_CantidadAcciones31Dic_Hasta is null)
	                    and ("MontoPagado" >= p_MontoPagado_Desde or p_MontoPagado_Desde is null)
	                    and ("MontoPagado" <= p_MontoPagado_Hasta or p_MontoPagado_Hasta is null)
	                    and ("MontoRecibido" >= p_MontoRecibido_Desde or p_MontoRecibido_Desde is null)
	                    and ("MontoRecibido" <= p_MontoRecibido_Hasta or p_MontoRecibido_Hasta is null)
	                    and ("DividendosAfectos" >= p_DividendosAfectos_Desde or p_DividendosAfectos_Desde is null)
	                    and ("DividendosAfectos" <= p_DividendosAfectos_Hasta or p_DividendosAfectos_Hasta is null)
	                    and ("DividendosExentos" >= p_DividendosExentos_Desde or p_DividendosExentos_Desde is null)
	                    and ("DividendosExentos" <= p_DividendosExentos_Hasta or p_DividendosExentos_Hasta is null)
	                    and ("DividendosNoRentaNoAcogidos" >= p_DividendosNoRentaNoAcogidos_Desde or p_DividendosNoRentaNoAcogidos_Desde is null)
	                    and ("DividendosNoRentaNoAcogidos" <= p_DividendosNoRentaNoAcogidos_Hasta or p_DividendosNoRentaNoAcogidos_Hasta is null)
	                    and ("DividendosNoRentaAcogidos" >= p_DividendosNoRentaAcogidos_Desde or p_DividendosNoRentaAcogidos_Desde is null)
	                    and ("DividendosNoRentaAcogidos" <= p_DividendosNoRentaAcogidos_Hasta or p_DividendosNoRentaAcogidos_Hasta is null)
	                    and ("IncrementoImpuesto" >= p_IncrementoImpuesto_Desde or p_IncrementoImpuesto_Desde is null)
	                    and ("IncrementoImpuesto" <= p_IncrementoImpuesto_Hasta or p_IncrementoImpuesto_Hasta is null)
	                    and ("CreditosAfectosConDevolucion" >= p_CreditosAfectosConDevolucion_Desde or p_CreditosAfectosConDevolucion_Desde is null)
	                    and ("CreditosAfectosConDevolucion" <= p_CreditosAfectosConDevolucion_Hasta or p_CreditosAfectosConDevolucion_Hasta is null)
	                    and ("CreditosAfectosSinDevolucion" >= p_CreditosAfectosSinDevolucion_Desde or p_CreditosAfectosSinDevolucion_Desde is null)
	                    and ("CreditosAfectosSinDevolucion" <= p_CreditosAfectosSinDevolucion_Hasta or p_CreditosAfectosSinDevolucion_Hasta is null)
	                    and ("CreditosExentosConDevolucion" >= p_CreditosExentosConDevolucion_Desde or p_CreditosExentosConDevolucion_Desde is null)
	                    and ("CreditosExentosConDevolucion" <= p_CreditosExentosConDevolucion_Hasta or p_CreditosExentosConDevolucion_Hasta is null)
	                    and ("CreditosExentosSinDevolucion" >= p_CreditosExentosSinDevolucion_Desde or p_CreditosExentosSinDevolucion_Desde is null)
	                    and ("CreditosExentosSinDevolucion" <= p_CreditosExentosSinDevolucion_Hasta or p_CreditosExentosSinDevolucion_Hasta is null)
	                    and ("CreditosImpuestosExternos" >= p_CreditosImpuestosExternos_Desde or p_CreditosImpuestosExternos_Desde is null)
	                    and ("CreditosImpuestosExternos" <= p_CreditosImpuestosExternos_Hasta or p_CreditosImpuestosExternos_Hasta is null)
	                    and ("CreditosImpuestoTasaAdicional" >= p_CreditosImpuestoTasaAdicional_Desde or p_CreditosImpuestoTasaAdicional_Desde is null)
	                    and ("CreditosImpuestoTasaAdicional" <= p_CreditosImpuestoTasaAdicional_Hasta or p_CreditosImpuestoTasaAdicional_Hasta is null)
	                    and ("NumeroCertificado" >= p_NumeroCertificado_Desde or p_NumeroCertificado_Desde is null)
	                    and ("NumeroCertificado" <= p_NumeroCertificado_Hasta or p_NumeroCertificado_Hasta is null)
	
	                    /*  in p_CantidadDerecho            bigint,
                        in p_IdInstrumento              bigint,
                        in p_IdEventoCapitalCliente     bigint,
                    */
	                    and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
	                    and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
	                    and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
	                ;
      else
	    p_out_tbl = select
	                    'TOTAL' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("CantidadAcciones31Dic") as "CantidadAcciones31Dic",
	                    sum("MontoPagado") as "MontoPagado",
	                    sum("MontoRecibido") as "MontoRecibido",
	                    sum("DividendosAfectos") as "DividendosAfectos",
	                    sum("DividendosExentos") as "DividendosExentos",
	                    sum("DividendosNoRentaNoAcogidos") as "DividendosNoRentaNoAcogidos",
	                    sum("DividendosNoRentaAcogidos") as "DividendosNoRentaAcogidos",
	                    sum("IncrementoImpuesto") as "IncrementoImpuesto",
	                    sum("CreditosAfectosConDevolucion") as "CreditosAfectosConDevolucion",
	                    sum("CreditosAfectosSinDevolucion") as "CreditosAfectosSinDevolucion",
	                    sum("CreditosExentosConDevolucion") as "CreditosExentosConDevolucion",
	                    sum("CreditosExentosSinDevolucion") as "CreditosExentosSinDevolucion",
	                    sum("CreditosImpuestosExternos") as "CreditosImpuestosExternos",
	                    sum("CreditosImpuestoTasaAdicional") as "CreditosImpuestoTasaAdicional"
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1885"
	                    where "IdDocumento" = p_IdDocumento
	                union all
	                select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("CantidadAcciones31Dic") as "CantidadAcciones31Dic",
	                    sum("MontoPagado") as "MontoPagado",
	                    sum("MontoRecibido") as "MontoRecibido",
	                    sum("DividendosAfectos") as "DividendosAfectos",
	                    sum("DividendosExentos") as "DividendosExentos",
	                    sum("DividendosNoRentaNoAcogidos") as "DividendosNoRentaNoAcogidos",
	                    sum("DividendosNoRentaAcogidos") as "DividendosNoRentaAcogidos",
	                    sum("IncrementoImpuesto") as "IncrementoImpuesto",
	                    sum("CreditosAfectosConDevolucion") as "CreditosAfectosConDevolucion",
	                    sum("CreditosAfectosSinDevolucion") as "CreditosAfectosSinDevolucion",
	                    sum("CreditosExentosConDevolucion") as "CreditosExentosConDevolucion",
	                    sum("CreditosExentosSinDevolucion") as "CreditosExentosSinDevolucion",
	                    sum("CreditosImpuestosExternos") as "CreditosImpuestosExternos",
	                    sum("CreditosImpuestoTasaAdicional") as "CreditosImpuestoTasaAdicional"
	
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1885"
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