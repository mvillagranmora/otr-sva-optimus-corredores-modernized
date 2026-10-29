
set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1889.GrabarMasivo";

create procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1889.GrabarMasivo"
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
    --in p_TipoAhorro	bigint,
    in p_TipoAhorro_Desde	bigint,
    in p_TipoAhorro_Hasta	bigint,
    
    -- Rango del monto nominal.
    in p_MontoNominal_Desde bigint default null,
    in p_MontoNominal_Hasta bigint default null,
    
    -- numero certificado
    in p_NumeroCertificado_Desde bigint default null,
    in p_NumeroCertificado_Hasta bigint default null,
    
    in p_RentabilidadPositiva_Desde  bigint default null,
    in p_RentabilidadPositiva_Hasta  bigint default null,

    in p_RentabilidadNegativa_Desde  bigint default null,
    in p_RentabilidadNegativa_Hasta  bigint default null,

    in p_Factor_Desde  bigint default null,
    in p_Factor_Hasta  bigint default null,

    in p_RentabilidadActualizadaPositiva_Desde  bigint default null,
    in p_RentabilidadActualizadaPositiva_Hasta  bigint default null,

    in p_RentabilidadActualizadaNegativa_Desde  bigint default null,
    in p_RentabilidadActualizadaNegativa_Hasta  bigint default null,

    
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
        update "BSCL.Normativa.BD::TRI.DatosDJ1889"
        SET "Observaciones" = x_Observaciones
        where "IdDocumento" = p_IdDocumento
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)
		and ("Numero" >= p_Numero_Desde or p_Numero_Desde is null)
        and ("Numero" <= p_Numero_Hasta or p_Numero_Hasta is null)
		and ("TipoAhorro" >= p_TipoAhorro_Desde or p_TipoAhorro_Desde is null)
        and ("TipoAhorro" <= p_TipoAhorro_Hasta or p_TipoAhorro_Hasta is null)        
		and ("MontoNominal" >= p_MontoNominal_Desde or p_MontoNominal_Desde is null)
        and ("MontoNominal" <= p_MontoNominal_Hasta or p_MontoNominal_Hasta is null)        

        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)        
        
        and ("RentabilidadPositiva" >= p_RentabilidadPositiva_Desde or p_RentabilidadPositiva_Desde is null)
        and ("RentabilidadPositiva" <= p_RentabilidadPositiva_Hasta or p_RentabilidadPositiva_Hasta is null)        
        
        and ("RentabilidadNegativa" >= p_RentabilidadNegativa_Desde or p_RentabilidadNegativa_Desde is null)
        and ("RentabilidadNegativa" <= p_RentabilidadNegativa_Hasta or p_RentabilidadNegativa_Hasta is null)        
        
        and ("RentabilidadActualizadaPositiva" >= p_RentabilidadActualizadaPositiva_Desde or p_RentabilidadActualizadaPositiva_Desde is null)
        and ("RentabilidadActualizadaPositiva" <= p_RentabilidadActualizadaPositiva_Hasta or p_RentabilidadActualizadaPositiva_Hasta is null)        

        and ("RentabilidadActualizadaNegativa" >= p_RentabilidadActualizadaNegativa_Desde or p_RentabilidadActualizadaNegativa_Desde is null)
        and ("RentabilidadActualizadaNegativa" <= p_RentabilidadActualizadaNegativa_Hasta or p_RentabilidadActualizadaNegativa_Hasta is null)        
        
        and ("NumeroCertificado" >= p_NumeroCertificado_Desde or p_NumeroCertificado_Desde is null)
        and ("NumeroCertificado" <= p_NumeroCertificado_Hasta or p_NumeroCertificado_Hasta is null)        
        ;
    end if;
        
-- Actualizacion Observaciones.
    if x_VboUsuario is not null then
        update "BSCL.Normativa.BD::TRI.DatosDJ1889"
        SET "VboUsuario" = x_VboUsuario
        where "IdDocumento" = p_IdDocumento
        and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
        and ("Observaciones" like p_Observaciones or p_Observaciones is null)
        and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)
		and ("Numero" >= p_Numero_Desde or p_Numero_Desde is null)
        and ("Numero" <= p_Numero_Hasta or p_Numero_Hasta is null)
		and ("TipoAhorro" >= p_TipoAhorro_Desde or p_TipoAhorro_Desde is null)
        and ("TipoAhorro" <= p_TipoAhorro_Hasta or p_TipoAhorro_Hasta is null)        
		and ("MontoNominal" >= p_MontoNominal_Desde or p_MontoNominal_Desde is null)
        and ("MontoNominal" <= p_MontoNominal_Hasta or p_MontoNominal_Hasta is null)        

        and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
        and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)        
        
        and ("RentabilidadPositiva" >= p_RentabilidadPositiva_Desde or p_RentabilidadPositiva_Desde is null)
        and ("RentabilidadPositiva" <= p_RentabilidadPositiva_Hasta or p_RentabilidadPositiva_Hasta is null)        
        
        and ("RentabilidadNegativa" >= p_RentabilidadNegativa_Desde or p_RentabilidadNegativa_Desde is null)
        and ("RentabilidadNegativa" <= p_RentabilidadNegativa_Hasta or p_RentabilidadNegativa_Hasta is null)        
        
        and ("RentabilidadActualizadaPositiva" >= p_RentabilidadActualizadaPositiva_Desde or p_RentabilidadActualizadaPositiva_Desde is null)
        and ("RentabilidadActualizadaPositiva" <= p_RentabilidadActualizadaPositiva_Hasta or p_RentabilidadActualizadaPositiva_Hasta is null)        

        and ("RentabilidadActualizadaNegativa" >= p_RentabilidadActualizadaNegativa_Desde or p_RentabilidadActualizadaNegativa_Desde is null)
        and ("RentabilidadActualizadaNegativa" <= p_RentabilidadActualizadaNegativa_Hasta or p_RentabilidadActualizadaNegativa_Hasta is null)        
        
        and ("NumeroCertificado" >= p_NumeroCertificado_Desde or p_NumeroCertificado_Desde is null)
        and ("NumeroCertificado" <= p_NumeroCertificado_Hasta or p_NumeroCertificado_Hasta is null)        
        ;
    end if;        

end;

