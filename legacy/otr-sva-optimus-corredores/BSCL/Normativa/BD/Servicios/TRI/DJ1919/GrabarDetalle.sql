set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1919.GrabarDetalle";

create procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1919.GrabarDetalle"
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

    --in p_IdCliente bigint,

    --in p_IdPortafolio bigint,

    in p_Numero bigint,
    
    in p_IdOperacion bigint,

    in p_RutEmisor nvarchar(40),

    in p_TaxID nvarchar(100),

    in p_CodigoPaisConstitucion nvarchar(10),

    in p_CodigoPaisResidencia nvarchar(10),

    in p_PorcentajePropiedadDirectaIndirecta bigint,

    in p_ParticipacionDirectaIndirectaControl bigint,

    in p_NemotecnicoSVS nvarchar(100),

    in p_FechaOperacion date,

    in p_NumeroCuentaDCV nvarchar(50),

    in p_Moneda nvarchar(3),

    in p_ValorEmision decimal(26,8),

    in p_ValorColocacion decimal(26,8),

    in p_ValorAdquisicion decimal(26,8),

    in p_ValorAdquisicionEnPesos decimal(26,8),

    in p_ValorEnajenacion decimal(26,8),

    in p_ValorEnajenacionEnPesos decimal(26,8),

    in p_MayorValorEnajenacion decimal(26,8),

    in p_MayorValorEnajenacionEnPesos decimal(26,8),

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

        update "BSCL.Normativa.BD::TRI.DatosDJ1919" set
            "IdDocumento" = p_IdDocumento,
			"VboUsuario" = p_VboUsuario,
			"Observaciones" = (CASE WHEN LENGTH(TRIM(:p_Observaciones)) BETWEEN 4 AND 5 AND LEFT(UPPER(TRIM(:p_Observaciones)), 1) = 'H' THEN "Observaciones" ELSE :p_Observaciones END),
			"FuenteDatos" = p_FuenteDatos,
			"Numero" = p_Numero,
			"IdOperacion" = COALESCE(p_IdOperacion, 0),
			"RutEmisor" = p_RutCliente,
			"TaxID" = p_TaxID,
			"RutCliente" = p_RutCliente,
			"CodigoPaisConstitucion" = p_CodigoPaisConstitucion,
			"CodigoPaisResidencia" = p_CodigoPaisResidencia,
			"PorcentajePropiedadDirectaIndirecta" = p_PorcentajePropiedadDirectaIndirecta,
			"ParticipacionDirectaIndirectaControl" = p_ParticipacionDirectaIndirectaControl,
			"NemotecnicoSVS" = p_NemotecnicoSVS,
			"FechaOperacion" = p_FechaOperacion,
			"NumeroCuentaDCV" = p_NumeroCuentaDCV,
			"Moneda" = p_Moneda,
			"ValorEmision" = p_ValorEmision,
			"ValorColocacion" = p_ValorColocacion,
			"ValorAdquisicion" = p_ValorAdquisicion,
			"ValorAdquisicionEnPesos" = p_ValorAdquisicionEnPesos,
			"ValorEnajenacion" = p_ValorEnajenacion,
			"ValorEnajenacionEnPesos" = p_ValorEnajenacionEnPesos,
			"MayorValorEnajenacion" = p_MayorValorEnajenacion,
			"MayorValorEnajenacionEnPesos" = p_MayorValorEnajenacionEnPesos,
			"TipoDocumento" = p_TipoDocumento,
			"NumeroDocumento" = p_NumeroDocumento,
			"FechaDocumento" = p_FechaDocumento,
			"IdBaseDocumento" = p_IdBaseDocumento,
			--"IdCliente" = p_IdCliente,
			--"IdPortafolio" = p_IdPortafolio,
			"TipoTransaccion" = p_TipoTransaccion,
			"IdTipoOperacion" = p_IdTipoOperacion,
			"IdTipoProducto" = p_IdTipoProducto
        where "ID" = p_ID;
    else
        select ifnull(max("ID"),0)+1 into p_out_id from "BSCL.Normativa.BD::TRI.DatosDJ1919";

        insert into "BSCL.Normativa.BD::TRI.DatosDJ1919"(
            "ID",
			"IdDocumento", 
			"VboUsuario", 
			"Observaciones", 
			"FuenteDatos",
			"IdOperacion", 
			"Numero", 
			"RutEmisor", 
			"TaxID", 
			"RutCliente", 
			"CodigoPaisConstitucion", 
			"CodigoPaisResidencia", 
			"PorcentajePropiedadDirectaIndirecta", 
			"ParticipacionDirectaIndirectaControl", 
			"NemotecnicoSVS", 
			"FechaOperacion", 
			"NumeroCuentaDCV",
			"Moneda", 
			"ValorEmision", 
			"ValorColocacion", 
			"ValorAdquisicion", 
			"ValorAdquisicionEnPesos", 
			"ValorEnajenacion", 
			"ValorEnajenacionEnPesos", 
			"MayorValorEnajenacion", 
			"MayorValorEnajenacionEnPesos", 
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
            COALESCE(p_IdOperacion, 0),
			p_Numero,
			p_RutCliente,
			p_TaxID,
			p_RutCliente,
			p_CodigoPaisConstitucion,
			p_CodigoPaisResidencia,
			p_PorcentajePropiedadDirectaIndirecta,
			p_ParticipacionDirectaIndirectaControl,
			p_NemotecnicoSVS,
			p_FechaOperacion,
			p_NumeroCuentaDCV,
			p_Moneda,
			p_ValorEmision,
			p_ValorColocacion,
			p_ValorAdquisicion,
			p_ValorAdquisicionEnPesos,
			p_ValorEnajenacion,
			p_ValorEnajenacionEnPesos,
			p_MayorValorEnajenacion,
			p_MayorValorEnajenacionEnPesos,
			p_TipoDocumento,
			p_NumeroDocumento,
			p_FechaDocumento,
			p_IdBaseDocumento,
			0,--p_IdCliente,
			0,--p_IdPortafolio,
			p_TipoTransaccion,
			p_IdTipoOperacion,
			p_IdTipoProducto
        );
    end if;
end;
