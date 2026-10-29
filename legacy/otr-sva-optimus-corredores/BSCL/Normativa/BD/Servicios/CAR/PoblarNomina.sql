set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::CAR.PoblarNomina";

-- Objetivo:
--     Insertar registros en la tabla "BSCL.Normativa.BD::CAR.Cartola",
--     a partir de los atributos de la tabla "BSCL.Normativa.BD::CAR.Nomina" y,
--     de los parametros asociados, registrados en la tabla "BSCL.Normativa.BD::CAR.Parametro".
--
-- Parametros:
--     Los parametros de inclusion/exclusion se definen de la siguiente forma:
--     IncluirPortafoliosConSaldo: false/true
--     IncluirPortafoliosConCustodia: false/true
--     IncluirPortafoliosConMovimientosCuentaInversion: false/true
--     IncluirCategoriaInversionista: Codigo segun tabla.
--     IncluirTipoPersona: Codigo segun tabla.
--     IncluirTipoCliente: Codigo segun tabla.
--     IncluirPortafoliosExcluidos: false/true
--     ExcluirPortafoliosIncluidosOtraNomina: false/true
--     ExcluirOperadoresDirectos: false/true
create procedure "BSCL.Normativa.BD.Servicios::CAR.PoblarNomina" (
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    in p_IdEmpresa bigint,

    -- ID usuario del login.
    in p_IdUsuario bigint,

    -- ID modulo invocador.
    in p_IdModulo nvarchar(3),

    ------------ Parametros de Entrada Propios ----------

    -- Id de la nomina.
    in p_IdNomina bigint,

    ------------ Parametros de Salida Propios ----------

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

    INSERT INTO "BSCL.Normativa.BD::CAR.Cartola" (
    "IdCartola",
    "IdNomina",
    "IdCliente",
    "IdPortafolio",
    "MensajeProceso",
    "EstadoProceso",
    "Notificar",
    "Reprocesar",
    "NombreArchivo",          
    "CartolaEncoded"          
)
SELECT TOP 100
    "BSCL.Normativa.BD.Secuencias::CAR.Global".NEXTVAL,
    p_IdNomina,
    "IdCliente",
    "IdPortafolio",
    'NO',
    'NO',
    '',
    '',
    '',       
    ''       
FROM "BSCL.Portafolio.BD::POR.Portafolio";

end;