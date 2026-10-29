
set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1922.GrabarMasivo";

create procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1922.GrabarMasivo"
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

    -- Filtro VoBo del usuario.
    in p_VboUsuario nvarchar(3) default null,

    -- Filtro observaciones.
    in p_Observaciones nvarchar(252) default null,

    -- Filtro fuentes de datos.
    in p_FuenteDatos nvarchar(42) default null,
    
    -- Filtro numero
    --in p_Numero	bigint,
    in p_Numero_Desde bigint default null,
    in p_Numero_Hasta bigint default null,

    -- Filtro del RUT del cliente.
    in p_RutCliente nvarchar(42) default null,
    
    -- Rango del ID cliente.
    in p_IdCliente_Desde bigint default null,
    in p_IdCliente_Hasta bigint default null,
    
    
    -- Rango fecha operacion.
    in p_FechaOperacion_Desde date default null,
    in p_FechaOperacion_Hasta date default null,
    
	-- Filtro tipo ahorro
    --in p_RutAdministradora	bigint,
    in p_RutAdministradora	nvarchar(42) default null,
    
    -- Rango del monto nominal.
    in p_MontoNominalAportes_Desde bigint default null,
    in p_MontoNominalAportes_Hasta bigint default null,
    
    -- numero certificado
    in p_NumeroCertificado_Desde bigint default null,
    in p_NumeroCertificado_Hasta bigint default null,
    
    in p_MontoNominalAportesActualizado_Desde  bigint default null,
    in p_MontoNominalAportesActualizado_Hasta  bigint default null,

    in p_NumeroSerieCuotas_Desde  bigint default null,
    in p_NumeroSerieCuotas_Hasta  bigint default null,

    in p_Factor_Desde  bigint default null,
    in p_Factor_Hasta  bigint default null,

    in p_CantidadCuotasParticipe_Desde  bigint default null,
    in p_CantidadCuotasParticipe_Hasta  bigint default null,

    in p_ValorCuotaPesos_Desde  bigint default null,
    in p_ValorCuotaPesos_Hasta  bigint default null,

    
    in p_EjecutadoComplementoIPC  nvarchar(1) default null,

    -- Valores a modificar.
    in x_VboUsuario char(1) default null,
    in x_Observaciones nvarchar(250) default null,

    ------------ Salidas ----------

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

	-- Actualizacion Observaciones.
    if x_Observaciones is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1922"
        SET "Observaciones" = x_Observaciones
        where "IdDocumento" = p_IdDocumento
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)
		and ("Numero" >= p_Numero_Desde or p_Numero_Desde is null)
        and ("Numero" <= p_Numero_Hasta or p_Numero_Hasta is null)
		and ("RutAdministradora" like p_RutAdministradora or p_RutAdministradora is null)
		and ("MontoNominalAportes" >= p_MontoNominalAportes_Desde or p_MontoNominalAportes_Desde is null)
        and ("MontoNominalAportes" <= p_MontoNominalAportes_Hasta or p_MontoNominalAportes_Hasta is null)        

        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)        
        
        and ("MontoNominalAportesActualizado" >= p_MontoNominalAportesActualizado_Desde or p_MontoNominalAportesActualizado_Desde is null)
        and ("MontoNominalAportesActualizado" <= p_MontoNominalAportesActualizado_Hasta or p_MontoNominalAportesActualizado_Hasta is null)        
        
        and ("NumeroSerieCuotas" >= p_NumeroSerieCuotas_Desde or p_NumeroSerieCuotas_Desde is null)
        and ("NumeroSerieCuotas" <= p_NumeroSerieCuotas_Hasta or p_NumeroSerieCuotas_Hasta is null)        
        
        and ("CantidadCuotasParticipe" >= p_CantidadCuotasParticipe_Desde or p_CantidadCuotasParticipe_Desde is null)
        and ("CantidadCuotasParticipe" <= p_CantidadCuotasParticipe_Hasta or p_CantidadCuotasParticipe_Hasta is null)        

        and ("ValorCuotaPesos" >= p_ValorCuotaPesos_Desde or p_ValorCuotaPesos_Desde is null)
        and ("ValorCuotaPesos" <= p_ValorCuotaPesos_Hasta or p_ValorCuotaPesos_Hasta is null)        
        
        and ("NumeroCertificado" >= p_NumeroCertificado_Desde or p_NumeroCertificado_Desde is null)
        and ("NumeroCertificado" <= p_NumeroCertificado_Hasta or p_NumeroCertificado_Hasta is null)        
        ;
    end if;
        
-- Actualizacion Observaciones.
    if x_VboUsuario is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1922"
        SET "VboUsuario" = x_VboUsuario
        where "IdDocumento" = p_IdDocumento
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)
		and ("Numero" >= p_Numero_Desde or p_Numero_Desde is null)
        and ("Numero" <= p_Numero_Hasta or p_Numero_Hasta is null)
		and ("RutAdministradora" like p_RutAdministradora or p_RutAdministradora is null)
		and ("MontoNominalAportes" >= p_MontoNominalAportes_Desde or p_MontoNominalAportes_Desde is null)
        and ("MontoNominalAportes" <= p_MontoNominalAportes_Hasta or p_MontoNominalAportes_Hasta is null)        

        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)        
        
        and ("MontoNominalAportesActualizado" >= p_MontoNominalAportesActualizado_Desde or p_MontoNominalAportesActualizado_Desde is null)
        and ("MontoNominalAportesActualizado" <= p_MontoNominalAportesActualizado_Hasta or p_MontoNominalAportesActualizado_Hasta is null)        
        
        and ("NumeroSerieCuotas" >= p_NumeroSerieCuotas_Desde or p_NumeroSerieCuotas_Desde is null)
        and ("NumeroSerieCuotas" <= p_NumeroSerieCuotas_Hasta or p_NumeroSerieCuotas_Hasta is null)        
        
        and ("CantidadCuotasParticipe" >= p_CantidadCuotasParticipe_Desde or p_CantidadCuotasParticipe_Desde is null)
        and ("CantidadCuotasParticipe" <= p_CantidadCuotasParticipe_Hasta or p_CantidadCuotasParticipe_Hasta is null)        

        and ("ValorCuotaPesos" >= p_ValorCuotaPesos_Desde or p_ValorCuotaPesos_Desde is null)
        and ("ValorCuotaPesos" <= p_ValorCuotaPesos_Hasta or p_ValorCuotaPesos_Hasta is null)        
        
        and ("NumeroCertificado" >= p_NumeroCertificado_Desde or p_NumeroCertificado_Desde is null)
        and ("NumeroCertificado" <= p_NumeroCertificado_Hasta or p_NumeroCertificado_Hasta is null)        
        ;
    end if;        

end;

