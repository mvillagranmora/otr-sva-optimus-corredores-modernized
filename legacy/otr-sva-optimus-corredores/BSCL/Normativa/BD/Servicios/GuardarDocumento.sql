set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::GuardarDocumento";

create procedure "BSCL.Normativa.BD.Servicios::GuardarDocumento"
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

    -- RUT del declarante.
    in p_RutDeclarante nvarchar(40),

    -- RUT del representante.
    in p_RutRepresentante nvarchar(40),

    -- Direccion de la declaracion.
    in p_Direccion nvarchar(100),

    -- Comuna de la declaracion.
    in p_Comuna nvarchar(100),

    -- Email de la declaracion.
    in p_Email nvarchar(100),

    -- Telefono de la declaracion.
    in p_Telefono nvarchar(100),

    -- Define Fecha Certificado de la declaracion.
    in p_DefineFechaCertificado nvarchar(1),

    -- Fecha Certificado de la declaracion.
    in p_FechaCertificado date default '2000-01-01',

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

    update "BSCL.Normativa.BD::DOC.Documento" set
        "DocumentoIdentidadRepresentante" = p_RutRepresentante,
        "DocumentoIdentidadDeclarante" = p_RutDeclarante,
        "DireccionFisicaDeclaracion" = p_Direccion,
        "ComunaDeclaracion" = p_Comuna,
        "DireccionElectronicaDeclaracion" = p_Email,
        "TelefonoDeclaracion" = p_Telefono,
        "DefineFechaCertificado" = p_DefineFechaCertificado,
        "FechaCertificado" = p_FechaCertificado
    where "IdDocumento" = p_IdDocumento;
end;