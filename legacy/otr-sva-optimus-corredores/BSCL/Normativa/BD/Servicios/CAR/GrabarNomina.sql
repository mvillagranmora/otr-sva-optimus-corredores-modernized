set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::CAR.GrabarNomina";

create procedure "BSCL.Normativa.BD.Servicios::CAR.GrabarNomina"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    in p_IdEmpresa bigint,

    -- ID usuario del login.
    in p_IdUsuario bigint,

    -- ID modulo invocador.
    in p_IdModulo nvarchar(3),

    ------------ Parametros de Entrada Propios ----------

    -- Id de la nomina.
    -- Si el id es mayor que cero, se asume que es un registro existente para actualizar.
    -- En caso contrario, se intenta crear un nuevo registro.
    in p_IdNomina bigint,
    
    -- Periodo de las cartolas.
	in p_Periodo char(7) default null,
	
	-- Fecha desde de los datos de las cartolas.
    in p_FechaDesde date default null,
	
	-- Fecha hasta de los datos de las cartolas.
    in p_FechaHasta date default null,
    
    -- Comentario de creacion.
    in p_Comentario nvarchar(100) default null,
    
    -- Indica si esta certificada.
    in p_Certificada char(2),
    
    -- Fecha de la certificacion.
    in p_FechaCertificacion date default null,
    
    -- Id del usuario certificador.
    in p_IdUsuarioCertificador bigint default null,
    
    -- Nombre del usuario certificador.
    in p_NombreUsuarioCertificador nvarchar(100) default null,
    
    -- Nota de certificacion.
    in p_NotaCertificacion nvarchar(100) default null,
    
    -- Tipo de proceso
    in p_TipoProceso nvarchar(20),
    
    -- Estado de proceso.
    in p_EstadoProceso nvarchar(20),
    
    -- Segundos de espera de proceso.
    in p_SegundosEsperaProceso bigint,
    
    -- Mensaje de proceso.
    in p_MensajeProceso nvarchar(1000) default null,

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

    -- Id de la plantilla.
    declare v_id_plantilla bigint;
    
    declare v_certificada char(2);
    declare v_fecha_certificacion date;
    
    declare v_estado_proceso nvarchar(20);
    declare v_fecha_inicio_proceso timestamp;
    declare v_fecha_termino_proceso timestamp;
    
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

    if p_IdNomina > 0 then
        p_out_id := p_IdNomina;
        
        v_fecha_certificacion := p_FechaCertificacion;
        select "Certificada" into v_certificada from "BSCL.Normativa.BD::CAR.Nomina"
            where "IdNomina" = p_IdNomina;
        if v_certificada = 'NO' and p_Certificada = 'SI' then
        	select current_date into v_fecha_certificacion from dummy;
        end if;

        select "FechaInicioProceso" into v_fecha_inicio_proceso from "BSCL.Normativa.BD::CAR.Nomina"
            where "IdNomina" = p_IdNomina;
        select "FechaTerminoProceso" into v_fecha_termino_proceso from "BSCL.Normativa.BD::CAR.Nomina"
            where "IdNomina" = p_IdNomina;
        select "EstadoProceso" into v_estado_proceso from "BSCL.Normativa.BD::CAR.Nomina"
            where "IdNomina" = p_IdNomina;
        if p_EstadoProceso = 'EN PROCESO' and v_estado_proceso<> 'EN PROCESO' then
        	select current_timestamp into v_fecha_inicio_proceso from dummy;
        end if;
        if p_EstadoProceso = 'TERMINADO' and v_estado_proceso<> 'TERMINADO' then
        	select current_timestamp into v_fecha_termino_proceso from dummy;
        end if;
        if p_EstadoProceso = 'ERROR' and v_estado_proceso<> 'ERROR' then
        	select current_timestamp into v_fecha_termino_proceso from dummy;
        end if;

        update "BSCL.Normativa.BD::CAR.Nomina" set
		    "IdEmpresa" = p_IdEmpresa,
		    "IdUsuario" = p_IdUsuario,
			"Periodo" = coalesce(p_Periodo, '2000-01'),
		    "FechaDesde" = p_FechaDesde,
		    "FechaHasta" = p_FechaHasta,
		    "Comentario" = p_Comentario,
		    "Certificada" = p_Certificada,
		    "FechaCertificacion" = v_fecha_certificacion,
		    "IdUsuarioCertificador" = p_IdUsuarioCertificador,
		    "NombreUsuarioCertificador" = p_NombreUsuarioCertificador,
		    "NotaCertificacion" = p_NotaCertificacion,
		    "TipoProceso" = p_TipoProceso,
		    "EstadoProceso" = p_EstadoProceso,
		    "FechaInicioProceso" = v_fecha_inicio_proceso,
		    "FechaTerminoProceso" = v_fecha_termino_proceso,
		    "MensajeProceso" = p_MensajeProceso
        where "IdNomina" = p_IdNomina;
    else
        select "BSCL.Normativa.BD.Secuencias::CAR.Global".NEXTVAL into p_out_id from DUMMY;
        select max("IdPlantilla") into v_id_plantilla from "BSCL.Normativa.BD::CAR.Plantilla"
            where "VigenteDesde" <= current_date;
            
        insert into "BSCL.Normativa.BD::CAR.Nomina"(
		    "IdNomina",
		    "IdEmpresa",
		    "IdUsuario",
		    "IdPlantilla",
			"Periodo",
		    "FechaDesde",
		    "FechaHasta",
		    "Comentario",
		    "Certificada",
		    "IdUsuarioCertificador",
		    "NombreUsuarioCertificador",
		    "NotaCertificacion",
		    "TipoProceso",
		    "EstadoProceso",
		    "FechaProgramadaProceso",
		    "FechaInicioProceso",
		    "FechaTerminoProceso",
		    "MensajeProceso"
        ) values (
		    p_out_id,
		    p_IdEmpresa,
		    p_IdUsuario,
		    v_id_plantilla,
			coalesce(p_Periodo, '2000-01'),
		    p_FechaDesde,
		    p_FechaHasta,
		    p_Comentario,
		    'NO',
		    null,
		    null,
		    null,
		    p_TipoProceso,
		    'INGRESADO',
		    add_seconds(current_timestamp, p_SegundosEsperaProceso),
		    null,
		    null,
		    null
        );
    end if;
end;