set schema "BSCL";
GO
drop procedure "BSCL.Normativa.BD.Servicios::CAR.GrabarPlantilla";
GO
create procedure "BSCL.Normativa.BD.Servicios::CAR.GrabarPlantilla"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    in p_IdEmpresa bigint,

    -- ID usuario del login.
    in p_IdUsuario bigint,

    -- ID modulo invocador.
    in p_IdModulo nvarchar(3),

    ------------ Parametros de Entrada Propios ----------

    -- Id de la plantilla.
    -- Si el id es mayor que cero, se asume que es un registro existente para actualizar.
    -- En caso contrario, se intenta crear un nuevo registro.
    in p_IdPlantilla bigint,

    -- Nombre de la plantilla.
    in p_Nombre nvarchar(100),

    -- Indicador de vigencia.
    in p_Vigente char(2),

    -- Fecha de vigencia desde.
    in p_VigenteDesde nvarchar(40),

    ------------ Parametros de Salida Propios ----------

    -- ID del registro.
    out p_out_id bigint,

    ------------ Salidas Advertencias ----------

    -- Contador de advertencias.
    out p_out_AdvertenciasContador int,

    -- Tabla de advertencias.
    --out p_out_AdvertenciasDetalle table("Codigo" nvarchar(40), "RequiereAutorizacion" nvarchar(40), "Descripcion" nvarchar(5000)),

    ------------ Salidas Errores ----------

    -- Contador de errores
    -- Importante: Debe ser cero para que se haga commit.
    out p_out_ErroresContador int

    -- Tabla de errores.
    --out p_out_ErroresDetalle table("Codigo" nvarchar(40), "Descripcion" nvarchar(5000))
)
    language sqlscript
    sql security invoker
    default schema "BSCL"
as
begin
    ------------ Variables Propias ----------

    ------------ Bloque Comun de Inicializacion ----------

    /*declare salida_controlada condition;
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
    end;*/

    p_out_AdvertenciasContador := 0;
    p_out_ErroresContador := 0;
    --p_out_AdvertenciasDetalle = select '' as "Codigo", '' as "RequiereAutorizacion", '' as "Descripcion" from dummy where 1 = 2;
    --p_out_ErroresDetalle = select '' as "Codigo", '' as "Descripcion" from dummy where 1 = 2;

    ------------ Logica de Actualizacion ----------

    if p_IdPlantilla > 0 then
        p_out_id := p_IdPlantilla;

        update "BSCL.Normativa.BD::CAR.Plantilla" set
            "IdEmpresa" = p_IdEmpresa,
            "Nombre" = p_Nombre,
            "VigenteDesde" = p_VigenteDesde
        where "IdPlantilla" = p_IdPlantilla;
    ELSE
    	select "BSCL.Normativa.BD.Secuencias::CAR.Global".NEXTVAL into p_out_id from DUMMY;
    	
        insert into "BSCL.Normativa.BD::CAR.Plantilla"(
            "IdPlantilla",
            "IdEmpresa",
            "Nombre",            
            "VigenteDesde"
        ) values (
            p_out_id,
            p_IdEmpresa,
            p_Nombre,            
            p_VigenteDesde
        );
    end if;
end;
GO