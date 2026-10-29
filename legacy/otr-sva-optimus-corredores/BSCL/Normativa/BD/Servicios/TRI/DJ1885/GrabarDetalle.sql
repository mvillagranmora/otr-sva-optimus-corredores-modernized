set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1885.GrabarDetalle";

create procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1885.GrabarDetalle"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    in p_IdEmpresa bigint,

    -- ID usuario del login.
    in p_IdUsuario bigint,

    -- ID modulo invocador.
    in p_IdModulo nvarchar(3),

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

    in p_RutCliente nvarchar(40),

    in p_IdCliente bigint,

    in p_IdPortafolio bigint,

    in p_TipoInstrumento nvarchar(40),

    in p_TipoRegistro nvarchar(1),

    in p_Nemotecnico nvarchar(100),

    in p_FechaPago date,

    in p_SecuenciaEvento bigint,

    in p_RutSociedad nvarchar(40),

    in p_RazonSocial nvarchar(100),

    in p_TipoSociedad nvarchar(40),

    in p_CantidadAcciones31Dic bigint,

    in p_MontoPagado bigint,

    in p_MontoRecibido bigint,

    in p_DividendosAfectos bigint,

    in p_DividendosExentos bigint,

    in p_DividendosNoRentaNoAcogidos bigint,

    in p_DividendosNoRentaAcogidos bigint,

    in p_IncrementoImpuesto bigint,

    in p_CreditosAfectosConDevolucion bigint,

    in p_CreditosAfectosSinDevolucion bigint,

    in p_CreditosExentosConDevolucion bigint,

    in p_CreditosExentosSinDevolucion bigint,

    in p_CreditosImpuestosExternos bigint,

    in p_CreditosImpuestoTasaAdicional bigint,

    in p_NumeroCertificado bigint,

    in p_CantidadDerecho bigint,

    in p_IdInstrumento bigint,

    in p_IdEventoCapitalCliente bigint,

	--CLT, COR, CIA, BOL, CAM, CUS,
    in p_TipoSocioNegocio nvarchar(1),

    in p_TipoCartera nvarchar(3),

    in p_IdMoneda nvarchar(3),

    in p_TipoEventoCapital nvarchar(3),

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

        update "BSCL.Normativa.BD::TRI.DatosDJ1885" set
           "VboUsuario"  = p_VboUsuario,
           "Observaciones"  = p_Observaciones,
           "FuenteDatos"  = p_FuenteDatos,
           "RutCliente"  = p_RutCliente,
           "IdCliente"  = p_IdCliente,
           "IdPortafolio"  = p_IdPortafolio,
           "TipoInstrumento"  = p_TipoInstrumento,
           "TipoRegistro"  = p_TipoRegistro,
           "Nemotecnico"  = p_Nemotecnico,
           "FechaPago"  = p_FechaPago,
           "SecuenciaEvento"  = p_SecuenciaEvento,
           "RutSociedad"  = p_RutSociedad,
           "RazonSocial"  = p_RazonSocial,
           "TipoSociedad"  = p_TipoSociedad,
           "CantidadAcciones31Dic"  = p_CantidadAcciones31Dic,
           "MontoPagado"    = p_MontoPagado,
           "MontoRecibido"  = p_MontoRecibido,
           "DividendosAfectos"  = p_DividendosAfectos,
           "DividendosExentos"  = p_DividendosExentos,
           "DividendosNoRentaNoAcogidos"  = p_DividendosNoRentaNoAcogidos,
           "DividendosNoRentaAcogidos"  = p_DividendosNoRentaAcogidos,
           "IncrementoImpuesto"  = p_IncrementoImpuesto,
           "CreditosAfectosConDevolucion"  = p_CreditosAfectosConDevolucion,
           "CreditosAfectosSinDevolucion"  = p_CreditosAfectosSinDevolucion,
           "CreditosExentosConDevolucion"  = p_CreditosExentosConDevolucion,
           "CreditosExentosSinDevolucion"  = p_CreditosExentosSinDevolucion,
           "CreditosImpuestosExternos"  = p_CreditosImpuestosExternos,
           "CreditosImpuestoTasaAdicional"  = p_CreditosImpuestoTasaAdicional,
           "NumeroCertificado"  = p_NumeroCertificado,
           "CantidadDerecho"  = p_CantidadDerecho,
           "IdInstrumento"  = p_IdInstrumento,
           "IdEventoCapitalCliente"  = p_IdEventoCapitalCliente,
           "TipoSocioNegocio"  = p_TipoSocioNegocio,
           "TipoCartera"  = p_TipoCartera,
           "IdMoneda"  = p_IdMoneda,
           "TipoEventoCapital"  = p_TipoEventoCapital
        where "ID" = p_ID;
    else
        select ifnull(max("ID"),0)+1 into p_out_id from "BSCL.Normativa.BD::TRI.DatosDJ1885";

        insert into "BSCL.Normativa.BD::TRI.DatosDJ1885"(
            "ID",
            "IdDocumento",
            "VboUsuario",
            "Observaciones",
            "FuenteDatos",
            "RutCliente",
            "IdCliente",
            "IdPortafolio",
            "TipoInstrumento",
            "TipoRegistro",
            "Nemotecnico",
            "FechaPago",
            "SecuenciaEvento",
            "RutSociedad",
            "RazonSocial",
            "TipoSociedad",
            "CantidadAcciones31Dic",
            "MontoPagado",
            "MontoRecibido",
            "DividendosAfectos",
            "DividendosExentos",
            "DividendosNoRentaNoAcogidos",
            "DividendosNoRentaAcogidos",
            "IncrementoImpuesto",
            "CreditosAfectosConDevolucion",
            "CreditosAfectosSinDevolucion",
            "CreditosExentosConDevolucion",
            "CreditosExentosSinDevolucion",
            "CreditosImpuestosExternos",
            "CreditosImpuestoTasaAdicional",
            "NumeroCertificado",
            "CantidadDerecho",
            "IdInstrumento",
            "IdEventoCapitalCliente",
            "TipoSocioNegocio",
            "TipoCartera",
            "IdMoneda",
            "TipoEventoCapital"
        ) values (
            p_out_id,
            p_IdDocumento,
            'N',
            '',
            'MANTENEDOR',
            p_RutCliente,
            p_IdCliente,
            p_IdPortafolio,
            p_TipoInstrumento,
            p_TipoRegistro,
            p_Nemotecnico,
            p_FechaPago,
            p_SecuenciaEvento,
            p_RutSociedad,
            p_RazonSocial,
            p_TipoSociedad,
            p_CantidadAcciones31Dic,
            p_MontoPagado,
            p_MontoRecibido,
            p_DividendosAfectos,
            p_DividendosExentos,
            p_DividendosNoRentaNoAcogidos,
            p_DividendosNoRentaAcogidos,
            p_IncrementoImpuesto,
            p_CreditosAfectosConDevolucion,
            p_CreditosAfectosSinDevolucion,
            p_CreditosExentosConDevolucion,
            p_CreditosExentosSinDevolucion,
            p_CreditosImpuestosExternos,
            p_CreditosImpuestoTasaAdicional,
            p_NumeroCertificado,
            p_CantidadDerecho,
            p_IdInstrumento,
            p_IdEventoCapitalCliente,
            p_TipoSocioNegocio,
            p_TipoCartera,
            p_IdMoneda,
            p_TipoEventoCapital
        );
    end if;
end;