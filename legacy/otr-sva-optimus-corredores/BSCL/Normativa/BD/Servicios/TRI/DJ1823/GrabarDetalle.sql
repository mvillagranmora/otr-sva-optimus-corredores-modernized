set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1823.GrabarDetalle";

create procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1823.GrabarDetalle"
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
    
    in p_IdSolicitud bigint,
    
    in p_Monto decimal(26,8),
    
    in p_Spread decimal(26,8),
    
    in p_FechaCertificacion date,
    
    in p_IdCliente bigint,

    in p_IdPortafolio bigint,

    in p_Numero bigint,
    
    in p_RutInstitucionReceptora nvarchar(40),

	in p_RutCliente nvarchar(40),
	
	in p_ValorEmision decimal(26,8),
	
	in p_SaldoAhorroNetoHasta31Dic2014MontoPositivo decimal(26,8),
	
	in p_SaldoAhorroNetoHasta31Dic2014MontoNegativo decimal(26,8),
	
	in p_MontoRetiroInversion57BIS decimal(26,8),
	
	in p_SaldoAhorroNetoDesde01Ene2015MontoPositivo decimal(26,8),
	
	in p_SaldoAhorroNetoDesde01Ene2015MontoNegativo decimal(26,8),
	
	in p_SaldoArrastreEjercicioDesde01Ene2015MontoPositivo decimal(26,8),
	
	in p_SaldoArrastreEjercicioDesde01Ene2015MontoNegativo decimal(26,8),
	
	in p_MontoInversionEjercicioDesde01Ene2015 decimal(26,8),
	
	in p_MontoRetiroEjercicioDesde01Ene2015 decimal(26,8),
	
	in p_MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo decimal(26,8),
	
	in p_MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo decimal(26,8),
	
	in p_MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo decimal(26,8),
	
	in p_MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo decimal(26,8),
	
	in p_NumeroCertificado bigint,
	
    in p_TipoDocumento nvarchar(10),

    in p_NumeroDocumento bigint,

    in p_FechaDocumento date,

    in p_IdBaseDocumento bigint,

    in p_TipoTransaccion nvarchar(6),

    in p_IdTipoOperacion nvarchar(6),

    in p_IdTipoProducto nvarchar(4),


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

        update "BSCL.Normativa.BD::TRI.DatosDJ1823" set
            "IdDocumento" = p_IdDocumento,
			"VboUsuario" = p_VboUsuario,
			"Observaciones" = p_Observaciones,
			"FuenteDatos" = p_FuenteDatos,
			"IdSolicitud" = p_IdSolicitud,
			"Monto" = p_Monto,
			"Spread" = p_Spread,
			"FechaCertificacion" = p_FechaCertificacion,
			"Numero" = p_Numero,
			"RutInstitucionReceptora" = p_RutInstitucionReceptora,
			"RutCliente" = p_RutCliente,
			"SaldoAhorroNetoHasta31Dic2014MontoPositivo" = p_SaldoAhorroNetoHasta31Dic2014MontoPositivo,
			"SaldoAhorroNetoHasta31Dic2014MontoNegativo" = p_SaldoAhorroNetoHasta31Dic2014MontoNegativo,
			"MontoRetiroInversion57BIS" = p_MontoRetiroInversion57BIS,
			"SaldoAhorroNetoDesde01Ene2015MontoPositivo" = p_SaldoAhorroNetoDesde01Ene2015MontoPositivo,
			"SaldoAhorroNetoDesde01Ene2015MontoNegativo" = p_SaldoAhorroNetoDesde01Ene2015MontoNegativo,
			"SaldoArrastreEjercicioDesde01Ene2015MontoPositivo" = p_SaldoArrastreEjercicioDesde01Ene2015MontoPositivo,
			"SaldoArrastreEjercicioDesde01Ene2015MontoNegativo" = p_SaldoArrastreEjercicioDesde01Ene2015MontoNegativo,
			"MontoInversionEjercicioDesde01Ene2015" = p_MontoInversionEjercicioDesde01Ene2015,
			"MontoRetiroEjercicioDesde01Ene2015" = p_MontoRetiroEjercicioDesde01Ene2015,
			"MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo" = p_MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo,
			"MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo" = p_MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo,
			"MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo" = p_MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo,
			"MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo" = p_MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo,
			"NumeroCertificado" = p_NumeroCertificado,
			"TipoDocumento" = p_TipoDocumento,
			"NumeroDocumento" = p_NumeroDocumento,
			"FechaDocumento" = p_FechaDocumento,
			"IdBaseDocumento" = p_IdBaseDocumento,
			"IdCliente" = p_IdCliente,
			"IdPortafolio" = p_IdPortafolio,
			"TipoTransaccion" = p_TipoTransaccion,
			"IdTipoOperacion" = p_IdTipoOperacion,
			"IdTipoProducto" = p_IdTipoProducto
        where "ID" = p_ID;
    else
        select ifnull(max("ID"),0)+1 into p_out_id from "BSCL.Normativa.BD::TRI.DatosDJ1823";

        insert into "BSCL.Normativa.BD::TRI.DatosDJ1823"(
            "ID",
			"IdDocumento",
			"VboUsuario",
			"Observaciones",
			"FuenteDatos",
			"IdSolicitud",
			"Monto",
			"Spread",
			"FechaCertificacion",
			"Numero",
			"RutInstitucionReceptora",
			"RutCliente",
			"SaldoAhorroNetoHasta31Dic2014MontoPositivo",
			"SaldoAhorroNetoHasta31Dic2014MontoNegativo",
			"MontoRetiroInversion57BIS",
			"SaldoAhorroNetoDesde01Ene2015MontoPositivo",
			"SaldoAhorroNetoDesde01Ene2015MontoNegativo",
			"SaldoArrastreEjercicioDesde01Ene2015MontoPositivo",
			"SaldoArrastreEjercicioDesde01Ene2015MontoNegativo",
			"MontoInversionEjercicioDesde01Ene2015",
			"MontoRetiroEjercicioDesde01Ene2015",
			"MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo",
			"MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo",
			"MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo",
			"MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo",
			"NumeroCertificado",
			"TipoDocumento",
			"NumeroDocumento",
			"FechaDocumento",
			"IdBaseDocumento",
			"IdCliente",
			"IdPortafolio",
			"TipoTransaccion",
			"IdTipoOperacion",
			"IdTipoProducto"
        ) values (
            p_out_id,
            p_IdDocumento,
			'N',
            '',
            'MANTENEDOR',
            p_IdSolicitud,
            p_Monto,
            p_Spread,
            p_FechaCertificacion,
			p_Numero,
			p_RutInstitucionReceptora,
			p_RutCliente,
			p_SaldoAhorroNetoHasta31Dic2014MontoPositivo,
			p_SaldoAhorroNetoHasta31Dic2014MontoNegativo,
			p_MontoRetiroInversion57BIS,
			p_SaldoAhorroNetoDesde01Ene2015MontoPositivo,
			p_SaldoAhorroNetoDesde01Ene2015MontoNegativo,
			p_SaldoArrastreEjercicioDesde01Ene2015MontoPositivo,
			p_SaldoArrastreEjercicioDesde01Ene2015MontoNegativo,
			p_MontoInversionEjercicioDesde01Ene2015,
			p_MontoRetiroEjercicioDesde01Ene2015,
			p_MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo,
			p_MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo,
			p_MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo,
			p_MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo,
			p_NumeroCertificado,
			p_TipoDocumento,
			p_NumeroDocumento,
			p_FechaDocumento,
			p_IdBaseDocumento,
			p_IdCliente,
			p_IdPortafolio,
			p_TipoTransaccion,
			p_IdTipoOperacion,
			p_IdTipoProducto
        );
    end if;
end;
