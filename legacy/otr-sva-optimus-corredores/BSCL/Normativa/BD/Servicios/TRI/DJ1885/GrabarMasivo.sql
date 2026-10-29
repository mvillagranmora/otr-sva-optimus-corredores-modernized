set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1885.GrabarMasivo";

create procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1885.GrabarMasivo"
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
    in p_TipoSocioNegocio   NVARCHAR(1)  default null,

    in p_TipoCartera        NVARCHAR(3)  default null,

    in p_IdMoneda           NVARCHAR(3)  default null,

    -- Valores a modificar.
    in x_VboUsuario char(1) default null,
    in x_Observaciones nvarchar(250) default null,

    in x_RutCliente nvarchar(40) default null,
    in x_IdCliente bigint default null,
    in x_IdPortafolio bigint default null,
    in x_TipoInstrumento nvarchar(40) default null,
    in x_TipoRegistro nvarchar(1) default null,
    in x_Nemotecnico nvarchar(100) default null,
    in x_FechaPago date default null,
    in x_SecuenciaEvento bigint default null,
    in x_RutSociedad nvarchar(40) default null,
    in x_RazonSocial nvarchar(100) default null,
    in x_TipoSociedad nvarchar(40) default null,
    in x_MontoPagado bigint default null,
    in x_MontoRecibido bigint default null,
    in x_CantidadAcciones31Dic bigint default null,
    in x_DividendosAfectos bigint default null,
    in x_DividendosExentos bigint default null,
    in x_DividendosNoRentaNoAcogidos bigint default null,
    in x_DividendosNoRentaAcogidos bigint default null,
    in x_IncrementoImpuesto bigint default null,
    in x_CreditosAfectosConDevolucion bigint default null,
    in x_CreditosAfectosSinDevolucion bigint default null,
    in x_CreditosExentosConDevolucion bigint default null,
    in x_CreditosExentosSinDevolucion bigint default null,
    in x_CreditosImpuestosExternos bigint default null,
    in x_CreditosImpuestoTasaAdicional bigint default null,
    in x_NumeroCertificado bigint default null,
    in x_CantidadDerecho bigint default null,
    in x_IdInstrumento bigint default null,
    in x_IdEventoCapitalCliente bigint default null,
    in x_TipoSocioNegocio nvarchar(1) default null,
    in x_TipoCartera nvarchar(3) default null,
    in x_IdMoneda nvarchar(3) default null,
    in x_TipoEventoCapital nvarchar(3) default null,

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

	-- Actualizacion RutCliente.
    if x_RutCliente is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "RutCliente" = x_RutCliente
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion IdCliente.
    if x_IdCliente is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "IdCliente" = x_IdCliente
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion IdPortafolio.
    if x_IdPortafolio is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "IdPortafolio" = x_IdPortafolio
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion TipoInstrumento.
    if x_TipoInstrumento is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "TipoInstrumento" = x_TipoInstrumento
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion TipoRegistro.
    if x_TipoRegistro is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "TipoRegistro" = x_TipoRegistro
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion Nemotecnico.
    if x_Nemotecnico is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "Nemotecnico" = x_Nemotecnico
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion FechaPago.
    if x_FechaPago is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "FechaPago" = x_FechaPago
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion SecuenciaEvento.
    if x_SecuenciaEvento is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "SecuenciaEvento" = x_SecuenciaEvento
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion RutSociedad.
    if x_RutSociedad is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "RutSociedad" = x_RutSociedad
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion RazonSocial.
    if x_RazonSocial is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "RazonSocial" = x_RazonSocial
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion TipoSociedad.
    if x_TipoSociedad is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "TipoSociedad" = x_TipoSociedad
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion MontoPagado.
    if x_MontoPagado is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "MontoPagado" = x_MontoPagado
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion MontoRecibido.
    if x_MontoRecibido is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "MontoRecibido" = x_MontoRecibido
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion CantidadAcciones31Dic.
    if x_CantidadAcciones31Dic is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "CantidadAcciones31Dic" = x_CantidadAcciones31Dic
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion DividendosAfectos.
    if x_DividendosAfectos is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "DividendosAfectos" = x_DividendosAfectos
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion DividendosExentos.
    if x_DividendosExentos is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "DividendosExentos" = x_DividendosExentos
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion DividendosNoRentaNoAcogidos.
    if x_DividendosNoRentaNoAcogidos is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "DividendosNoRentaNoAcogidos" = x_DividendosNoRentaNoAcogidos
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion DividendosNoRentaAcogidos.
    if x_DividendosNoRentaAcogidos is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "DividendosNoRentaAcogidos" = x_DividendosNoRentaAcogidos
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion IncrementoImpuesto.
    if x_IncrementoImpuesto is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "IncrementoImpuesto" = x_IncrementoImpuesto
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion CreditosAfectosConDevolucion.
    if x_CreditosAfectosConDevolucion is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "CreditosAfectosConDevolucion" = x_CreditosAfectosConDevolucion
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion CreditosAfectosSinDevolucion.
    if x_CreditosAfectosSinDevolucion is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "CreditosAfectosSinDevolucion" = x_CreditosAfectosSinDevolucion
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion CreditosExentosConDevolucion.
    if x_CreditosExentosConDevolucion is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "CreditosExentosConDevolucion" = x_CreditosExentosConDevolucion
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion CreditosExentosSinDevolucion.
    if x_CreditosExentosSinDevolucion is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "CreditosExentosSinDevolucion" = x_CreditosExentosSinDevolucion
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion CreditosImpuestosExternos.
    if x_CreditosImpuestosExternos is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "CreditosImpuestosExternos" = x_CreditosImpuestosExternos
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion CreditosImpuestoTasaAdicional.
    if x_CreditosImpuestoTasaAdicional is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "CreditosImpuestoTasaAdicional" = x_CreditosImpuestoTasaAdicional
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion NumeroCertificado.
    if x_NumeroCertificado is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "NumeroCertificado" = x_NumeroCertificado
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion CantidadDerecho.
    if x_CantidadDerecho is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "CantidadDerecho" = x_CantidadDerecho
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion IdInstrumento.
    if x_IdInstrumento is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "IdInstrumento" = x_IdInstrumento
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion IdEventoCapitalCliente.
    if x_IdEventoCapitalCliente is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "IdEventoCapitalCliente" = x_IdEventoCapitalCliente
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion TipoSocioNegocio.
    if x_TipoSocioNegocio is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "TipoSocioNegocio" = x_TipoSocioNegocio
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion TipoCartera.
    if x_TipoCartera is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "TipoCartera" = x_TipoCartera
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion IdMoneda.
    if x_IdMoneda is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "IdMoneda" = x_IdMoneda
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;

	-- Actualizacion TipoEventoCapital.
    if x_TipoEventoCapital is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1885"
        set "TipoEventoCapital" = x_TipoEventoCapital
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
        and ("TipoSocioNegocio" like p_TipoSocioNegocio or p_TipoSocioNegocio is null)
        and ("TipoCartera" like p_TipoCartera or p_TipoCartera is null)
        and ("IdMoneda" like p_IdMoneda or p_IdMoneda is null)
        ;
    end if;


end;