set schema "BSCL";

drop procedure "BSCL.Plataforma.BD.Servicios::INS.GuardarBono";

create procedure "BSCL.Plataforma.BD.Servicios::INS.GuardarBono"
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
	in p_FechaDevengo date,
	in p_AjusteFinDeMes nvarchar(1),
	in p_CallConfirmado nvarchar(1),
    in p_ConvencionDias nvarchar(40),
	in p_CuponeraVariable nvarchar(1),
 	in p_TasaEmision decimal(8,6),
    in p_TenorInflacion nvarchar(40) default null,
    in p_TipoLibor nvarchar(40) default null,
 	in p_TipoCalculoInteresesCupon int,
 	in p_TipoCalculoInteresesDevengados int,
    in p_IdCalendario nvarchar(3) default null,
    in p_IdTipoInflacion nvarchar(3) default null,
	in p_FechaPrepago date default null,
    in p_CUSIP nvarchar(40) default null,
    in p_COMMON nvarchar(40) default null,
    in p_SEDOL nvarchar(40) default null,
    in p_TickerBloomberg nvarchar(40) default null,
    in p_MercadoLocal nvarchar(1),
    in p_MercadoExtrabursatil nvarchar(1),
    in p_MercadoExtranjero nvarchar(1),
 	in p_PresenciaBursatil nvarchar(1),
    in p_IdPaisTransaccion bigint default null,
 	in p_ValorBolsa decimal(26,2) default null,
 	in p_ValorBloomberg decimal(26,2) default null,
 	in p_DurationBolsa decimal(8,2) default null,
 	in p_DurationBloomberg decimal(8,2) default null,
	in p_FechaValorizacion date default null,
 	in p_ValorNominal decimal(26,2) default null,
 	in p_PrecioValorizacion decimal(8,2) default null,

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

	select "IdInstrumento" into p_out_id from "BSCL.Plataforma.BD::INS.Instrumento" where "Nemotecnico" = p_Nemotecnico;

	delete from "BSCL.Plataforma.BD::INS.InstrumentoBono" where "IdInstrumento" = p_out_id;

	insert into "BSCL.Plataforma.BD::INS.InstrumentoBono"(
	    "IdInstrumento",
		"FechaDevengo",
		"AjusteFinDeMes",
		"CallConfirmado",
	    "ConvencionDias",
		"CuponeraVariable",
	 	"TasaEmision",
	    "TenorInflacion",
	    "TipoLibor",
	 	"TipoCalculoInteresesCupon",
	 	"TipoCalculoInteresesDevengados",
	    "IdCalendario",
	    "IdTipoInflacion",
		"FechaPrepago",
	    "CUSIP",
	    "COMMON",
	    "SEDOL",
	    "TickerBloomberg",
	    "MercadoLocal",
	    "MercadoExtrabursatil",
	    "MercadoExtranjero",
	 	"PresenciaBursatil",
	    "IdPaisTransaccion",
 	    "ValorBolsa",
 	    "ValorBloomberg",
 	    "DurationBolsa",
 	    "DurationBloomberg",
	    "FechaValorizacion",
 	    "ValorNominal",
 	    "PrecioValorizacion"
	) VALUES (
	    p_out_id,
		p_FechaDevengo,
		p_AjusteFinDeMes,
		p_CallConfirmado,
	    p_ConvencionDias,
		p_CuponeraVariable,
	 	p_TasaEmision,
	    p_TenorInflacion,
	    p_TipoLibor,
	 	p_TipoCalculoInteresesCupon,
	 	p_TipoCalculoInteresesDevengados,
	    p_IdCalendario,
	    p_IdTipoInflacion,
		p_FechaPrepago,
	    p_CUSIP,
	    p_COMMON,
	    p_SEDOL,
	    p_TickerBloomberg,
	    p_MercadoLocal,
	    p_MercadoExtrabursatil,
	    p_MercadoExtranjero,
	 	p_PresenciaBursatil,
	    p_IdPaisTransaccion,
 	    p_ValorBolsa,
 	    p_ValorBloomberg,
 	    p_DurationBolsa,
 	    p_DurationBloomberg,
	    p_FechaValorizacion,
 	    p_ValorNominal,
 	    p_PrecioValorizacion
	);
end;