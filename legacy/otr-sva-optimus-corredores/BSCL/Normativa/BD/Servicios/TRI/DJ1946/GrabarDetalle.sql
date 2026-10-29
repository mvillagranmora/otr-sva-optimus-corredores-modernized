set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1946.GrabarDetalle";

create procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1946.GrabarDetalle"
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

    in p_IdDocumento bigint,

    in p_VboUsuario char(1),

    in p_Observaciones nvarchar(250),

    in p_FuenteDatos nvarchar(40),

    in p_IdCliente bigint,

    in p_Numero bigint,
    
    in p_Operacion nvarchar(40),

    in p_TipoRenta nvarchar(40),

    in p_RentaBruta decimal(26,8),

    in p_TasaDeRetencion decimal(26,8),

    in p_Impuesto decimal(26,8),

    in p_RentaLiquida decimal(26,8),

    in p_CreditoIDPC decimal(26,8),

    in p_OtrosCreditos decimal(26,8),

    in p_F50Codigo bigint,

    in p_F50Folio bigint,

    in p_FechaPago date,

    in p_MonedaPago nvarchar(10),

    in p_TipoCambioPago decimal(26,8),

    in p_PagadorTipoJuridico nvarchar(40),

    in p_PagadorRazonSocial nvarchar(40),

    in p_PagadorNombre nvarchar(40),

    in p_PagadorRut nvarchar(40),

    in p_PagadorDomicilio nvarchar(250),

    in p_PagadorCiudad nvarchar(40),

    in p_PagadorRelacion nvarchar(40),

    in p_PagadorHolding nvarchar(60),

    in p_PerceptorTipoJuridico nvarchar(40),

    in p_PerceptorPaisResidencia nvarchar(40),

    in p_PerceptorRazonSocial nvarchar(150),

    in p_PerceptorNombre nvarchar(40),

    in p_PerceptorIdentificacionTributaria nvarchar(40),

    in p_PerceptorRut nvarchar(40),

    in p_PerceptorFechaNacimiento date,

    in p_PerceptorTipoDomicilio nvarchar(40),

    in p_PerceptorDomicilio nvarchar(250),

    in p_PerceptorCiudad nvarchar(40),

    in p_PerceptorPais nvarchar(40),

    in p_IntermediarioTipoJuridico nvarchar(40),

    in p_IntermediarioPaisResidencia nvarchar(40),

    in p_IntermediarioRazonSocial nvarchar(150),

    in p_IntermediarioNombre nvarchar(40),

    in p_IntermediarioIdentificacionTributaria nvarchar(40),

    in p_NumPerIdenTri_C51 nvarchar(40),

	in p_NumInterIdenTri_C52 nvarchar(40),


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

        update "BSCL.Normativa.BD::TRI.DatosDJ1946" set
		"IdDocumento" = p_IdDocumento,
        "VboUsuario" = p_VboUsuario,
        "Observaciones" = (CASE WHEN LENGTH(TRIM(:p_Observaciones)) BETWEEN 4 AND 5 AND LEFT(UPPER(TRIM(:p_Observaciones)), 1) = 'H' THEN "Observaciones" ELSE :p_Observaciones END),
        "FuenteDatos" = p_FuenteDatos,
        "IdCliente" = p_IdCliente,
        "IdEmpresa" = p_IdEmpresa,
        "Numero" = p_Numero,
        "Operacion" = p_Operacion,
        "TipoRenta" = p_TipoRenta,
        "RentaBruta" = p_RentaBruta,
        "TasaDeRetencion" = p_TasaDeRetencion,
        "Impuesto" = p_Impuesto,
        "RentaLiquida" = p_RentaLiquida,
        "CreditoIDPC" = p_CreditoIDPC,
        "OtrosCreditos" = p_OtrosCreditos,
        "F50Codigo" = p_F50Codigo,
        "F50Folio" = p_F50Folio,
        "FechaPago" = p_FechaPago,
        "MonedaPago" = p_MonedaPago,
        "TipoCambioPago" = p_TipoCambioPago,
        "PagadorTipoJuridico" = p_PagadorTipoJuridico,
        "PagadorRazonSocial" = p_PagadorRazonSocial,
        "PagadorNombre" = p_PagadorNombre,
        "PagadorRut" = p_PagadorRut,
        "PagadorDomicilio" = p_PagadorDomicilio,
        "PagadorCiudad" = p_PagadorCiudad,
        "PagadorRelacion" = p_PagadorRelacion,
        "PagadorHolding" = p_PagadorHolding,
        "PerceptorTipoJuridico" = p_PerceptorTipoJuridico,
        "PerceptorPaisResidencia" = p_PerceptorPaisResidencia,
        "PerceptorRazonSocial" = p_PerceptorRazonSocial,
        "PerceptorNombre" = p_PerceptorNombre,
        "PerceptorIdentificacionTributaria" = p_PerceptorIdentificacionTributaria,
        "PerceptorRut" = p_PerceptorRut,
        "PerceptorFechaNacimiento" = p_PerceptorFechaNacimiento,
        "PerceptorTipoDomicilio" = p_PerceptorTipoDomicilio,
        "PerceptorDomicilio" = p_PerceptorDomicilio,
        "PerceptorCiudad" = p_PerceptorCiudad,
        "PerceptorPais" = p_PerceptorPais,
        "IntermediarioTipoJuridico" = p_IntermediarioTipoJuridico,
        "IntermediarioPaisResidencia" = p_IntermediarioPaisResidencia,
        "IntermediarioRazonSocial" = p_IntermediarioRazonSocial,
        "IntermediarioNombre" = p_IntermediarioNombre,
        "IntermediarioIdentificacionTributaria" = p_IntermediarioIdentificacionTributaria,
        "NumPerIdenTri_C51" = p_NumPerIdenTri_C51,
	    "NumInterIdenTri_C52" = p_NumInterIdenTri_C52
        where "ID" = p_ID;
    else
        select ifnull(max("ID"),0)+1 into p_out_id from "BSCL.Normativa.BD::TRI.DatosDJ1946";

        insert into "BSCL.Normativa.BD::TRI.DatosDJ1946"(
        "ID",
        "IdDocumento",
        "VboUsuario",
        "Observaciones",
        "FuenteDatos",
        "IdCliente",
        "IdEmpresa",
        "Numero",
        "Operacion",
        "TipoRenta",
        "RentaBruta",
        "TasaDeRetencion",
        "Impuesto",
        "RentaLiquida",
        "CreditoIDPC",
        "OtrosCreditos",
        "F50Codigo",
        "F50Folio",
        "FechaPago",
        "MonedaPago",
        "TipoCambioPago",
        "PagadorTipoJuridico",
        "PagadorRazonSocial",
        "PagadorNombre",
        "PagadorRut",
        "PagadorDomicilio",
        "PagadorCiudad",
        "PagadorRelacion",
        "PagadorHolding",
        "PerceptorTipoJuridico",
        "PerceptorPaisResidencia",
        "PerceptorRazonSocial",
        "PerceptorNombre",
        "PerceptorIdentificacionTributaria",
        "PerceptorRut",
        "PerceptorFechaNacimiento",
        "PerceptorTipoDomicilio",
        "PerceptorDomicilio",
        "PerceptorCiudad",
        "PerceptorPais",
        "IntermediarioTipoJuridico",
        "IntermediarioPaisResidencia",
        "IntermediarioRazonSocial",
        "IntermediarioNombre",
        "IntermediarioIdentificacionTributaria",
        "NumPerIdenTri_C51",
	    "NumInterIdenTri_C52"
    ) values (
        p_out_id,
        p_IdDocumento,
        'N',
        '',
        'MANTENEDOR',
        p_IdCliente,
        p_IdEmpresa,
        p_Numero,
        p_Operacion,
        p_TipoRenta,
        p_RentaBruta,
        p_TasaDeRetencion,
        p_Impuesto,
        p_RentaLiquida,
        p_CreditoIDPC,
        p_OtrosCreditos,
        p_F50Codigo,
        p_F50Folio,
        p_FechaPago,
        p_MonedaPago,
        p_TipoCambioPago,
        p_PagadorTipoJuridico,
        p_PagadorRazonSocial,
        p_PagadorNombre,
        p_PagadorRut,
        p_PagadorDomicilio,
        p_PagadorCiudad,
        p_PagadorRelacion,
        p_PagadorHolding,
        p_PerceptorTipoJuridico,
        p_PerceptorPaisResidencia,
        p_PerceptorRazonSocial,
        p_PerceptorNombre,
        p_PerceptorIdentificacionTributaria,
        p_PerceptorRut,
        p_PerceptorFechaNacimiento,
        p_PerceptorTipoDomicilio,
        p_PerceptorDomicilio,
        p_PerceptorCiudad,
        p_PerceptorPais,
        p_IntermediarioTipoJuridico,
        p_IntermediarioPaisResidencia,
        p_IntermediarioRazonSocial,
        p_IntermediarioNombre,
        p_IntermediarioIdentificacionTributaria,
        p_NumPerIdenTri_C51,
	    p_NumInterIdenTri_C52
        );
    end if;
end;
