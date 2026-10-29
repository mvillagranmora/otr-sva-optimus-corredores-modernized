set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1871.GrabarMasivo";

create procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1871.GrabarMasivo"
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

    -- Rango VoBo del usuario.
    in p_VboUsuario_Desde nvarchar(3) default null,
    in p_VboUsuario_Hasta nvarchar(3) default null,

    -- Filtro observaciones.
    in p_Observaciones nvarchar(252) default null,

    -- Filtro fuentes de datos.
    in p_FuenteDatos nvarchar(40) default null,
    
    
     ---- propios
    
    
    in p_AhorrosCotizaciones_Desde 		decimal(27,8) DEFAULT NULL,
    in p_AhorrosCotizaciones_Hasta 		decimal(27,8) DEFAULT NULL,
    
	in p_AhorrosDeposito_Desde 			decimal(27,8) DEFAULT NULL,
	in p_AhorrosDeposito_Hasta 			decimal(27,8) DEFAULT NULL,
	
	in p_AhorrosTrabajador_Desde 		decimal(27,8) DEFAULT NULL,
	in p_AhorrosTrabajador_Hasta 		decimal(27,8) DEFAULT NULL,
		
	in p_AnoAhorro_Desde				bigint default null,
	in p_AnoAhorro_Hasta				bigint default null,
	
	in p_Numero_Desde					bigint default null,
	in p_Numero_Hasta					bigint default null,
	
	in p_RetirosCotizaciones_Desde 		decimal(27,8) DEFAULT NULL,
	in p_RetirosCotizaciones_Hasta 		decimal(27,8) DEFAULT NULL,
	
	in p_RetirosDeposito_Desde     		decimal(27,8) DEFAULT NULL,
	in p_RetirosDeposito_Hasta     		decimal(27,8) DEFAULT NULL,	
	
	in p_RetirosTrabajador_Desde   		decimal(27,8) DEFAULT NULL,
	in p_RetirosTrabajador_Hasta   		decimal(27,8) DEFAULT NULL,	
	
	in p_RutCliente						nvarchar(42) default null,

	
	
	
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
    
   -- Actualizacion 
   IF x_VboUsuario IS NOT NULL THEN
	update "BSCL.Normativa.BD::TRI.DatosDJ1871"
	set "VboUsuario" = x_VboUsuario
	where "IdDocumento" = p_IdDocumento
	
		and ("VboUsuario" >= p_VboUsuario_Desde or p_VboUsuario_Desde is null)
		and ("VboUsuario" <= p_VboUsuario_Hasta or p_VboUsuario_Hasta is null)		
		and ("Observaciones" like p_Observaciones or p_Observaciones is null)
		and ("FuenteDatos" 	like p_FuenteDatos or p_FuenteDatos is null)
	
		and ("AhorrosCotizaciones" >= p_AhorrosCotizaciones_Desde or p_AhorrosCotizaciones_Desde is null)
		and ("AhorrosCotizaciones" <= p_AhorrosCotizaciones_Hasta or p_AhorrosCotizaciones_Hasta is null)	
		
		and ("AhorrosDeposito" >= p_AhorrosDeposito_Desde or p_AhorrosDeposito_Desde is null)
		and ("AhorrosDeposito" <= p_AhorrosDeposito_Hasta or p_AhorrosDeposito_Hasta is null)	

  		and ("AhorrosTrabajador" >= p_AhorrosTrabajador_Desde or p_AhorrosTrabajador_Desde is null)
		and ("AhorrosTrabajador" <= p_AhorrosTrabajador_Hasta or p_AhorrosTrabajador_Hasta is null)	
  
		and ("AnoAhorro" >= p_AnoAhorro_Desde or p_AnoAhorro_Desde is null)
		and ("AnoAhorro" <= p_AnoAhorro_Hasta or p_AnoAhorro_Hasta is null)	
		
		and ("Numero" >= p_Numero_Desde or p_Numero_Desde is null)
		and ("Numero" <= p_Numero_Hasta or p_Numero_Hasta is null)	
		
		and ("RetirosCotizaciones" >= p_RetirosCotizaciones_Desde or p_RetirosCotizaciones_Desde is null)
		and ("RetirosCotizaciones" <= p_RetirosCotizaciones_Hasta or p_RetirosCotizaciones_Hasta is null)	
		
	
		and ("RetirosDeposito" >= p_RetirosDeposito_Desde or p_RetirosDeposito_Desde is null)
		and ("RetirosDeposito" <= p_RetirosDeposito_Hasta or p_RetirosDeposito_Hasta is null)	
		

		and ("RetirosTrabajador" >= p_RetirosTrabajador_Desde or p_RetirosTrabajador_Desde is null)
		and ("RetirosTrabajador" <= p_RetirosTrabajador_Hasta or p_RetirosTrabajador_Hasta is null)	

     	and ("RutCliente" 	like p_RutCliente or p_RutCliente is null);
	
    end if;
   
    IF x_Observaciones IS NOT NULL THEN
	update "BSCL.Normativa.BD::TRI.DatosDJ1871"
	set "Observaciones" = x_Observaciones
	where "IdDocumento" = p_IdDocumento
	
		and ("VboUsuario" >= p_VboUsuario_Desde or p_VboUsuario_Desde is null)
		and ("VboUsuario" <= p_VboUsuario_Hasta or p_VboUsuario_Hasta is null)		
		and ("Observaciones" like p_Observaciones or p_Observaciones is null)
		and ("FuenteDatos" 	like p_FuenteDatos or p_FuenteDatos is null)
	
		and ("AhorrosCotizaciones" >= p_AhorrosCotizaciones_Desde or p_AhorrosCotizaciones_Desde is null)
		and ("AhorrosCotizaciones" <= p_AhorrosCotizaciones_Hasta or p_AhorrosCotizaciones_Hasta is null)	
		
		and ("AhorrosDeposito" >= p_AhorrosDeposito_Desde or p_AhorrosDeposito_Desde is null)
		and ("AhorrosDeposito" <= p_AhorrosDeposito_Hasta or p_AhorrosDeposito_Hasta is null)	

  		and ("AhorrosTrabajador" >= p_AhorrosTrabajador_Desde or p_AhorrosTrabajador_Desde is null)
		and ("AhorrosTrabajador" <= p_AhorrosTrabajador_Hasta or p_AhorrosTrabajador_Hasta is null)	
  
		and ("AnoAhorro" >= p_AnoAhorro_Desde or p_AnoAhorro_Desde is null)
		and ("AnoAhorro" <= p_AnoAhorro_Hasta or p_AnoAhorro_Hasta is null)	
		
		and ("Numero" >= p_Numero_Desde or p_Numero_Desde is null)
		and ("Numero" <= p_Numero_Hasta or p_Numero_Hasta is null)	
		
		and ("RetirosCotizaciones" >= p_RetirosCotizaciones_Desde or p_RetirosCotizaciones_Desde is null)
		and ("RetirosCotizaciones" <= p_RetirosCotizaciones_Hasta or p_RetirosCotizaciones_Hasta is null)	
		
	
		and ("RetirosDeposito" >= p_RetirosDeposito_Desde or p_RetirosDeposito_Desde is null)
		and ("RetirosDeposito" <= p_RetirosDeposito_Hasta or p_RetirosDeposito_Hasta is null)	
		

		and ("RetirosTrabajador" >= p_RetirosTrabajador_Desde or p_RetirosTrabajador_Desde is null)
		and ("RetirosTrabajador" <= p_RetirosTrabajador_Hasta or p_RetirosTrabajador_Hasta is null)	

     	and ("RutCliente" 	like p_RutCliente or p_RutCliente is null);
	
    end if;

end;