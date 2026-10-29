set schema "BSCL";

drop procedure "BSCL.Plataforma.BD.Servicios::INS.GuardarCupones";

create procedure "BSCL.Plataforma.BD.Servicios::INS.GuardarCupones"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    in p_IdEmpresa bigint,

    -- ID usuario del login.
    in p_IdUsuario bigint,

    -- ID modulo invocador.
    in p_IdModulo nvarchar(3),
    
    ------------ Parametros de Entrada Propios ----------

    in p_Nemotecnico nvarchar(40),

    in p_Cupones table
    (
        "Numero" bigint,
        "Fecha" date,
        "Call" nvarchar(1),
        "Intereses" decimal(13,10),
        "Amortizacion" decimal(13,10),
        "SaldoPorAmortizar" decimal(13,10)
    ),

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

    declare max_id_cupon bigint;

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

    if coalesce(p_Nemotecnico, '') = '' then
        p_out_ErroresDetalle = select 'NEMOTECNICO_VACIO' as "Codigo", '' as "Descripcion" from dummy;
	    select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
    else
        delete from "BSCL.Plataforma.BD::INS.Cupon" where "Nemotecnico" = p_Nemotecnico;

        select coalesce(max("IdCupon"),0) into max_id_cupon from "BSCL.Plataforma.BD::INS.Cupon";

        v_cupones = select
            :max_id_cupon + 1 +  row_number() over (order by p_cups."Numero") as "IdCupon",
            p_Nemotecnico as "Nemotecnico",
            p_cups."Numero" as "Numero",
            coalesce("Fecha",'0001-01-01') as "Fecha",
            coalesce("Call",'N') as "Call",
            coalesce("Intereses",0) as "Intereses",
            coalesce("Amortizacion",0) as "Amortizacion",
            coalesce("SaldoPorAmortizar",0) as "SaldoPorAmortizar"
            from :p_Cupones p_cups;

        insert into "BSCL.Plataforma.BD::INS.Cupon" (
            "IdCupon",
            "Nemotecnico",
            "Numero",
            "Fecha",
            "Call",
            "Intereses",
            "Amortizacion",
            "SaldoPorAmortizar"
        ) select
            "IdCupon",
            "Nemotecnico",
            "Numero",
            "Fecha",
            "Call",
            "Intereses",
            "Amortizacion",
            "SaldoPorAmortizar"
        from :v_cupones;
    end if;

end;