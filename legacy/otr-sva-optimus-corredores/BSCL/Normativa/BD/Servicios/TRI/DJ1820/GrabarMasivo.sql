set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1820.GrabarMasivo";

create procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1820.GrabarMasivo"
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
    
    -- Filtro del RUT del cliente.
    in p_RutCliente nvarchar(42) default null,

    -- Filtro del TAX ID del cliente.
	in p_TaxID nvarchar(40) default null,    
	
	-- Rango Codigo Pais Residencia
	in p_CodigoPaisResidencia_Desde nvarchar(10) default null,
	in p_CodigoPaisResidencia_Hasta nvarchar(10) default null,
	
	-- Rango Codigo Relacion
	in p_CodigoRelacion_Desde nvarchar(10) default null,
	in p_CodigoRelacion_Hasta nvarchar(10) default null,
	
	-- Rango Codigo Modo Contrato
	in p_CodigoModoContrato_Desde nvarchar(40) default null,
	in p_CodigoModoContrato_Hasta nvarchar(40) default null,
	
	-- Rango Numero Acuerdo Marco
	in p_NumeroAcuerdoMarco_Desde bigint default null,
	in p_NumeroAcuerdoMarco_Hasta bigint default null,	
	
	-- Rango Fecha Suscripcion Contrato
    in p_FechaSuscripcionContrato_Desde date default null,
    in p_FechaSuscripcionContrato_Hasta date default null,
	
	-- Rango Numero Contrato
	in p_NumeroContrato_Desde bigint default null,
	in p_NumeroContrato_Hasta bigint default null,

	-- Rango Fecha Suscripcion Acuerdo
	in p_FechaSuscripcionAcuerdo_Desde date default null,	
	in p_FechaSuscripcionAcuerdo_Hasta date default null,
	
	-- Rango Tipo Evento
	in p_TipoEvento_Desde nvarchar(40) default null,
	in p_TipoEvento_Hasta nvarchar(40) default null,
	
	-- Rango Tipo Contrato
	in p_TipoContrato_Desde nvarchar(40) default null,
	in p_TipoContrato_Hasta	nvarchar(40) default null,

	-- Filtro Instrumento
	in p_Instrumento nvarchar(40) default null,
	
	-- Rango Modalidad Cumplimiento
	in p_ModalidadCumplimiento_Desde  nvarchar(40) default null,
	in p_ModalidadCumplimiento_Hasta  nvarchar(40) default null,

	-- Rango Codigo Posicion Declarante
	in p_CodigoPosicionDeclarante_Desde  nvarchar(40) default null,
	in p_CodigoPosicionDeclarante_Hasta  nvarchar(40) default null,
	
	-- Rango Tipo Activo Subyacente
	in	p_TipoActivoSubyacente_Desde  nvarchar(40) default null,
	in 	p_TipoActivoSubyacente_Hasta  nvarchar(40) default null,

	-- Filtro Otro Activo Subyacente 
	in p_OtroActivoSubyacente nvarchar(40) default null,
	
	-- Rango Tasa Activo Subyacente
	in p_TasaActivoSubyacente_Desde  decimal(26,8) DEFAULT NULL,
	in p_TasaActivoSubyacente_Hasta	 decimal(26,8) DEFAULT NULL,
	
	-- Filtro Tipo Segundo Activo Subyacente
	in p_TipoSegundoActivoSubyacente nvarchar(40) default null,
	
	-- Filtro Codigo Segundo Activo Subyacente 
	in p_CodigoSegundoActivoSubyacente nvarchar(40) default null,

	-- Filtro Otro Segundo Activo Subyacente 
	in p_OtroSegundoActivoSubyacente nvarchar(40) default null,	
	
	-- Rango Tasa Segundo Activo Subyacente
	in p_TasaSegundoActivoSubyacente_Desde  decimal(26,8) DEFAULT NULL,
 	in p_TasaSegundoActivoSubyacente_Hasta  decimal(26,8) DEFAULT NULL,

 	-- Rango Codigo PrecioFuturo
	in p_CodigoPrecioFuturo_Desde  nvarchar(40) default null,
	in p_CodigoPrecioFuturo_Hasta  nvarchar(40) default null,
	
	-- Rango Precio Futuro
	in p_PrecioFuturo_Desde  decimal(27,8) DEFAULT NULL,
	in p_PrecioFuturo_Hasta  decimal(27,8) DEFAULT NULL,
	
	-- Filtro Codigo Moneda Futuro 
 	in p_CodigoMonedaFuturo nvarchar(40) default null,
 	
 	-- Rango 
	in p_CodigoUnidad_Desde  nvarchar(40) default null,
	in p_CodigoUnidad_Hasta  nvarchar(40) default null,
	
	-- Rango 
	in p_MontoNocional_Desde  decimal(27,8) DEFAULT NULL,
	in p_MontoNocional_Hasta  decimal(27,8) DEFAULT NULL,		

	-- Rango Codigo Segunda Unidad
	in p_CodigoSegundaUnidad_Desde  nvarchar(40) default null,
	in p_CodigoSegundaUnidad_Hasta	 nvarchar(40) default null,	
	
	-- Filtro Segundo Monto Nocional
	in p_SegundoMontoNocional nvarchar(50) default null,	
 	
	-- Rango  Fecha Vencimiento
	in p_FechaVencimiento_Desde date default null,
	in p_FechaVencimiento_Hasta date default null,
	

    -- Valores a modificar.
    in x_VboUsuario char(1) default null,
    in x_Observaciones nvarchar(250) default null,
    in x_FuenteDatos nvarchar(40) default null,
    
    in x_TaxID nvarchar(40) default null,
    in x_CodigoRelacion nvarchar(10) default null,
    in x_FechaSuscripcionAcuerdo date default null,
    in x_TipoAcuerdoMarco nvarchar(40) default null,


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
    
   -- Actualizacion TaxID.
   IF x_TaxID IS NOT NULL THEN
	update "BSCL.Normativa.BD::TRI.DatosDJ1820"
	set "TaxID" = x_TaxID
	where "IdDocumento" = p_IdDocumento
		and ("VboUsuario" >= p_VboUsuario_Desde or p_VboUsuario_Desde is null)
		and ("VboUsuario" >= p_VboUsuario_Hasta or p_VboUsuario_Hasta is null)		
		and ("Observaciones" like p_Observaciones or p_Observaciones is null)
		and ("FuenteDatos" 	like p_FuenteDatos or p_FuenteDatos is null)
		and ("RutCliente" 	like p_RutCliente or p_RutCliente is null)
		and ("TaxID" 		like p_TaxID or p_TaxID is null)		
		and ("CodigoPaisResidencia" >= p_CodigoPaisResidencia_Desde or p_CodigoPaisResidencia_Desde is null)
		and ("CodigoPaisResidencia" <= p_CodigoPaisResidencia_Hasta or p_CodigoPaisResidencia_Hasta is null)         
		and ("CodigoRelacion" >= p_CodigoRelacion_Desde or p_CodigoRelacion_Desde is null)
		and ("CodigoRelacion" <= p_CodigoRelacion_Hasta or p_CodigoRelacion_Hasta is null)         
		and ("CodigoModoContrato" >= p_CodigoModoContrato_Desde or p_CodigoModoContrato_Desde is null)
		and ("CodigoModoContrato" <= p_CodigoModoContrato_Hasta or p_CodigoModoContrato_Hasta is null)
		and ("NumeroAcuerdoMarco" >= p_NumeroAcuerdoMarco_Desde or p_NumeroAcuerdoMarco_Desde is null)
		and ("NumeroAcuerdoMarco" <= p_NumeroAcuerdoMarco_Hasta or p_NumeroAcuerdoMarco_Hasta is null)			
		and ("FechaSuscripcionContrato" >= p_FechaSuscripcionContrato_Desde or p_FechaSuscripcionContrato_Desde is null)
		and ("FechaSuscripcionContrato" <= p_FechaSuscripcionContrato_Hasta or p_FechaSuscripcionContrato_Hasta is null)
        and ("NumeroContrato" >= p_NumeroContrato_Desde or p_NumeroContrato_Desde is null)
        and ("NumeroContrato" <= p_NumeroContrato_Hasta or p_NumeroContrato_Hasta is null)        
		and ("FechaSuscripcionAcuerdo" >= p_FechaSuscripcionAcuerdo_Desde or p_FechaSuscripcionAcuerdo_Desde is null)
		and ("FechaSuscripcionAcuerdo" <= p_FechaSuscripcionAcuerdo_Hasta or p_FechaSuscripcionAcuerdo_Hasta is null)        
		and ("TipoEvento" >= p_TipoEvento_Desde or p_TipoEvento_Desde is null)
		and ("TipoEvento" <= p_TipoEvento_Hasta or p_TipoEvento_Hasta is null)        
		and ("TipoContrato" >= p_TipoContrato_Desde or p_TipoContrato_Desde is null)
		and ("TipoContrato" <= p_TipoContrato_Hasta or p_TipoContrato_Hasta is null)	
		and ("Instrumento" 		like p_Instrumento or p_Instrumento is null)		
		and ("ModalidadCumplimiento" >= p_ModalidadCumplimiento_Desde or p_ModalidadCumplimiento_Desde is null)
		and ("ModalidadCumplimiento" <= p_ModalidadCumplimiento_Hasta or p_ModalidadCumplimiento_Hasta is null)		
		and ("CodigoPosicionDeclarante" >= p_CodigoPosicionDeclarante_Desde or p_CodigoPosicionDeclarante_Desde is null)
		and ("CodigoPosicionDeclarante" <= p_CodigoPosicionDeclarante_Hasta or p_CodigoPosicionDeclarante_Hasta is null)
		and ("TipoActivoSubyacente" >= p_TipoActivoSubyacente_Desde or p_TipoActivoSubyacente_Desde is null)
		and ("TipoActivoSubyacente" <= p_TipoActivoSubyacente_Hasta or p_TipoActivoSubyacente_Hasta is null)		
		and ("OtroActivoSubyacente" like p_OtroActivoSubyacente or p_OtroActivoSubyacente is null)		
		and ("TasaActivoSubyacente" >= p_TasaActivoSubyacente_Desde or p_TasaActivoSubyacente_Desde is null)
		and ("TasaActivoSubyacente" <= p_TasaActivoSubyacente_Hasta or p_TasaActivoSubyacente_Hasta is null)		
		and ("TipoSegundoActivoSubyacente" like p_TipoSegundoActivoSubyacente or p_TipoSegundoActivoSubyacente is null)
		and ("CodigoSegundoActivoSubyacente" like p_CodigoSegundoActivoSubyacente or p_CodigoSegundoActivoSubyacente is null)
		and ("OtroSegundoActivoSubyacente" like p_OtroSegundoActivoSubyacente or p_OtroSegundoActivoSubyacente is null)			
		and ("TasaSegundoActivoSubyacente" >= p_TasaSegundoActivoSubyacente_Desde or p_TasaSegundoActivoSubyacente_Desde is null)
		and ("TasaSegundoActivoSubyacente" <= p_TasaSegundoActivoSubyacente_Hasta or p_TasaSegundoActivoSubyacente_Hasta is null)		
		and ("CodigoPrecioFuturo" >= p_CodigoPrecioFuturo_Desde or p_CodigoPrecioFuturo_Desde is null)
		and ("CodigoPrecioFuturo" <= p_CodigoPrecioFuturo_Hasta or p_CodigoPrecioFuturo_Hasta is null)		
		and ("PrecioFuturo" >= p_PrecioFuturo_Desde or p_PrecioFuturo_Desde is null)
		and ("PrecioFuturo" <= p_PrecioFuturo_Hasta or p_PrecioFuturo_Hasta is null)
		and ("CodigoMonedaFuturo" like p_CodigoMonedaFuturo or p_CodigoMonedaFuturo is null)	
		and ("CodigoUnidad" >= p_CodigoUnidad_Desde or p_CodigoUnidad_Desde is null)
		and ("CodigoUnidad" <= p_CodigoUnidad_Hasta or p_CodigoUnidad_Hasta is null)
		and ("MontoNocional" >= p_MontoNocional_Desde or p_MontoNocional_Desde is null)
		and ("MontoNocional" <= p_MontoNocional_Hasta or p_MontoNocional_Hasta is null)	
		and ("CodigoSegundaUnidad" >= p_CodigoSegundaUnidad_Desde or p_CodigoSegundaUnidad_Desde is null)
		and ("CodigoSegundaUnidad" <= p_CodigoSegundaUnidad_Hasta or p_CodigoSegundaUnidad_Hasta is null)		
		and ("SegundoMontoNocional" like p_SegundoMontoNocional or p_SegundoMontoNocional is null)		
		and ("FechaVencimiento" >= p_FechaVencimiento_Desde or p_FechaVencimiento_Desde is null)
		and ("FechaVencimiento" <= p_FechaVencimiento_Hasta or p_FechaVencimiento_Hasta is null);
    end if;

   -- Actualizacion CodigoRelacion.
   IF x_CodigoRelacion IS NOT NULL THEN
	update "BSCL.Normativa.BD::TRI.DatosDJ1820"
	set "CodigoRelacion" = x_CodigoRelacion
	where "IdDocumento" = p_IdDocumento
		and ("VboUsuario" >= p_VboUsuario_Desde or p_VboUsuario_Desde is null)
		and ("VboUsuario" >= p_VboUsuario_Hasta or p_VboUsuario_Hasta is null)		
		and ("Observaciones" like p_Observaciones or p_Observaciones is null)
		and ("FuenteDatos" 	like p_FuenteDatos or p_FuenteDatos is null)
		and ("RutCliente" 	like p_RutCliente or p_RutCliente is null)
		and ("TaxID" 		like p_TaxID or p_TaxID is null)		
		and ("CodigoPaisResidencia" >= p_CodigoPaisResidencia_Desde or p_CodigoPaisResidencia_Desde is null)
		and ("CodigoPaisResidencia" <= p_CodigoPaisResidencia_Hasta or p_CodigoPaisResidencia_Hasta is null)         
		and ("CodigoRelacion" >= p_CodigoRelacion_Desde or p_CodigoRelacion_Desde is null)
		and ("CodigoRelacion" <= p_CodigoRelacion_Hasta or p_CodigoRelacion_Hasta is null)         
		and ("CodigoModoContrato" >= p_CodigoModoContrato_Desde or p_CodigoModoContrato_Desde is null)
		and ("CodigoModoContrato" <= p_CodigoModoContrato_Hasta or p_CodigoModoContrato_Hasta is null)
		and ("NumeroAcuerdoMarco" >= p_NumeroAcuerdoMarco_Desde or p_NumeroAcuerdoMarco_Desde is null)
		and ("NumeroAcuerdoMarco" <= p_NumeroAcuerdoMarco_Hasta or p_NumeroAcuerdoMarco_Hasta is null)			
		and ("FechaSuscripcionContrato" >= p_FechaSuscripcionContrato_Desde or p_FechaSuscripcionContrato_Desde is null)
		and ("FechaSuscripcionContrato" <= p_FechaSuscripcionContrato_Hasta or p_FechaSuscripcionContrato_Hasta is null)
        and ("NumeroContrato" >= p_NumeroContrato_Desde or p_NumeroContrato_Desde is null)
        and ("NumeroContrato" <= p_NumeroContrato_Hasta or p_NumeroContrato_Hasta is null)        
		and ("FechaSuscripcionAcuerdo" >= p_FechaSuscripcionAcuerdo_Desde or p_FechaSuscripcionAcuerdo_Desde is null)
		and ("FechaSuscripcionAcuerdo" <= p_FechaSuscripcionAcuerdo_Hasta or p_FechaSuscripcionAcuerdo_Hasta is null)        
		and ("TipoEvento" >= p_TipoEvento_Desde or p_TipoEvento_Desde is null)
		and ("TipoEvento" <= p_TipoEvento_Hasta or p_TipoEvento_Hasta is null)        
		and ("TipoContrato" >= p_TipoContrato_Desde or p_TipoContrato_Desde is null)
		and ("TipoContrato" <= p_TipoContrato_Hasta or p_TipoContrato_Hasta is null)	
		and ("Instrumento" 		like p_Instrumento or p_Instrumento is null)		
		and ("ModalidadCumplimiento" >= p_ModalidadCumplimiento_Desde or p_ModalidadCumplimiento_Desde is null)
		and ("ModalidadCumplimiento" <= p_ModalidadCumplimiento_Hasta or p_ModalidadCumplimiento_Hasta is null)		
		and ("CodigoPosicionDeclarante" >= p_CodigoPosicionDeclarante_Desde or p_CodigoPosicionDeclarante_Desde is null)
		and ("CodigoPosicionDeclarante" <= p_CodigoPosicionDeclarante_Hasta or p_CodigoPosicionDeclarante_Hasta is null)
		and ("TipoActivoSubyacente" >= p_TipoActivoSubyacente_Desde or p_TipoActivoSubyacente_Desde is null)
		and ("TipoActivoSubyacente" <= p_TipoActivoSubyacente_Hasta or p_TipoActivoSubyacente_Hasta is null)		
		and ("OtroActivoSubyacente" like p_OtroActivoSubyacente or p_OtroActivoSubyacente is null)		
		and ("TasaActivoSubyacente" >= p_TasaActivoSubyacente_Desde or p_TasaActivoSubyacente_Desde is null)
		and ("TasaActivoSubyacente" <= p_TasaActivoSubyacente_Hasta or p_TasaActivoSubyacente_Hasta is null)		
		and ("TipoSegundoActivoSubyacente" like p_TipoSegundoActivoSubyacente or p_TipoSegundoActivoSubyacente is null)
		and ("CodigoSegundoActivoSubyacente" like p_CodigoSegundoActivoSubyacente or p_CodigoSegundoActivoSubyacente is null)
		and ("OtroSegundoActivoSubyacente" like p_OtroSegundoActivoSubyacente or p_OtroSegundoActivoSubyacente is null)			
		and ("TasaSegundoActivoSubyacente" >= p_TasaSegundoActivoSubyacente_Desde or p_TasaSegundoActivoSubyacente_Desde is null)
		and ("TasaSegundoActivoSubyacente" <= p_TasaSegundoActivoSubyacente_Hasta or p_TasaSegundoActivoSubyacente_Hasta is null)		
		and ("CodigoPrecioFuturo" >= p_CodigoPrecioFuturo_Desde or p_CodigoPrecioFuturo_Desde is null)
		and ("CodigoPrecioFuturo" <= p_CodigoPrecioFuturo_Hasta or p_CodigoPrecioFuturo_Hasta is null)		
		and ("PrecioFuturo" >= p_PrecioFuturo_Desde or p_PrecioFuturo_Desde is null)
		and ("PrecioFuturo" <= p_PrecioFuturo_Hasta or p_PrecioFuturo_Hasta is null)
		and ("CodigoMonedaFuturo" like p_CodigoMonedaFuturo or p_CodigoMonedaFuturo is null)	
		and ("CodigoUnidad" >= p_CodigoUnidad_Desde or p_CodigoUnidad_Desde is null)
		and ("CodigoUnidad" <= p_CodigoUnidad_Hasta or p_CodigoUnidad_Hasta is null)
		and ("MontoNocional" >= p_MontoNocional_Desde or p_MontoNocional_Desde is null)
		and ("MontoNocional" <= p_MontoNocional_Hasta or p_MontoNocional_Hasta is null)	
		and ("CodigoSegundaUnidad" >= p_CodigoSegundaUnidad_Desde or p_CodigoSegundaUnidad_Desde is null)
		and ("CodigoSegundaUnidad" <= p_CodigoSegundaUnidad_Hasta or p_CodigoSegundaUnidad_Hasta is null)		
		and ("SegundoMontoNocional" like p_SegundoMontoNocional or p_SegundoMontoNocional is null)		
		and ("FechaVencimiento" >= p_FechaVencimiento_Desde or p_FechaVencimiento_Desde is null)
		and ("FechaVencimiento" <= p_FechaVencimiento_Hasta or p_FechaVencimiento_Hasta is null);
    end if;
   
   
-- Actualiza FechaSuscripcionAcuerdo
  IF x_FechaSuscripcionAcuerdo IS NOT NULL THEN
	update "BSCL.Normativa.BD::TRI.DatosDJ1820"
	set "FechaSuscripcionAcuerdo" = x_FechaSuscripcionAcuerdo
	where "IdDocumento" = p_IdDocumento
		and ("VboUsuario" >= p_VboUsuario_Desde or p_VboUsuario_Desde is null)
		and ("VboUsuario" >= p_VboUsuario_Hasta or p_VboUsuario_Hasta is null)		
		and ("Observaciones" like p_Observaciones or p_Observaciones is null)
		and ("FuenteDatos" 	like p_FuenteDatos or p_FuenteDatos is null)
		and ("RutCliente" 	like p_RutCliente or p_RutCliente is null)
		and ("TaxID" 		like p_TaxID or p_TaxID is null)		
		and ("CodigoPaisResidencia" >= p_CodigoPaisResidencia_Desde or p_CodigoPaisResidencia_Desde is null)
		and ("CodigoPaisResidencia" <= p_CodigoPaisResidencia_Hasta or p_CodigoPaisResidencia_Hasta is null)         
		and ("CodigoRelacion" >= p_CodigoRelacion_Desde or p_CodigoRelacion_Desde is null)
		and ("CodigoRelacion" <= p_CodigoRelacion_Hasta or p_CodigoRelacion_Hasta is null)         
		and ("CodigoModoContrato" >= p_CodigoModoContrato_Desde or p_CodigoModoContrato_Desde is null)
		and ("CodigoModoContrato" <= p_CodigoModoContrato_Hasta or p_CodigoModoContrato_Hasta is null)
		and ("NumeroAcuerdoMarco" >= p_NumeroAcuerdoMarco_Desde or p_NumeroAcuerdoMarco_Desde is null)
		and ("NumeroAcuerdoMarco" <= p_NumeroAcuerdoMarco_Hasta or p_NumeroAcuerdoMarco_Hasta is null)			
		and ("FechaSuscripcionContrato" >= p_FechaSuscripcionContrato_Desde or p_FechaSuscripcionContrato_Desde is null)
		and ("FechaSuscripcionContrato" <= p_FechaSuscripcionContrato_Hasta or p_FechaSuscripcionContrato_Hasta is null)
        and ("NumeroContrato" >= p_NumeroContrato_Desde or p_NumeroContrato_Desde is null)
        and ("NumeroContrato" <= p_NumeroContrato_Hasta or p_NumeroContrato_Hasta is null)        
		and ("FechaSuscripcionAcuerdo" >= p_FechaSuscripcionAcuerdo_Desde or p_FechaSuscripcionAcuerdo_Desde is null)
		and ("FechaSuscripcionAcuerdo" <= p_FechaSuscripcionAcuerdo_Hasta or p_FechaSuscripcionAcuerdo_Hasta is null)        
		and ("TipoEvento" >= p_TipoEvento_Desde or p_TipoEvento_Desde is null)
		and ("TipoEvento" <= p_TipoEvento_Hasta or p_TipoEvento_Hasta is null)        
		and ("TipoContrato" >= p_TipoContrato_Desde or p_TipoContrato_Desde is null)
		and ("TipoContrato" <= p_TipoContrato_Hasta or p_TipoContrato_Hasta is null)	
		and ("Instrumento" 		like p_Instrumento or p_Instrumento is null)		
		and ("ModalidadCumplimiento" >= p_ModalidadCumplimiento_Desde or p_ModalidadCumplimiento_Desde is null)
		and ("ModalidadCumplimiento" <= p_ModalidadCumplimiento_Hasta or p_ModalidadCumplimiento_Hasta is null)		
		and ("CodigoPosicionDeclarante" >= p_CodigoPosicionDeclarante_Desde or p_CodigoPosicionDeclarante_Desde is null)
		and ("CodigoPosicionDeclarante" <= p_CodigoPosicionDeclarante_Hasta or p_CodigoPosicionDeclarante_Hasta is null)
		and ("TipoActivoSubyacente" >= p_TipoActivoSubyacente_Desde or p_TipoActivoSubyacente_Desde is null)
		and ("TipoActivoSubyacente" <= p_TipoActivoSubyacente_Hasta or p_TipoActivoSubyacente_Hasta is null)		
		and ("OtroActivoSubyacente" like p_OtroActivoSubyacente or p_OtroActivoSubyacente is null)		
		and ("TasaActivoSubyacente" >= p_TasaActivoSubyacente_Desde or p_TasaActivoSubyacente_Desde is null)
		and ("TasaActivoSubyacente" <= p_TasaActivoSubyacente_Hasta or p_TasaActivoSubyacente_Hasta is null)		
		and ("TipoSegundoActivoSubyacente" like p_TipoSegundoActivoSubyacente or p_TipoSegundoActivoSubyacente is null)
		and ("CodigoSegundoActivoSubyacente" like p_CodigoSegundoActivoSubyacente or p_CodigoSegundoActivoSubyacente is null)
		and ("OtroSegundoActivoSubyacente" like p_OtroSegundoActivoSubyacente or p_OtroSegundoActivoSubyacente is null)			
		and ("TasaSegundoActivoSubyacente" >= p_TasaSegundoActivoSubyacente_Desde or p_TasaSegundoActivoSubyacente_Desde is null)
		and ("TasaSegundoActivoSubyacente" <= p_TasaSegundoActivoSubyacente_Hasta or p_TasaSegundoActivoSubyacente_Hasta is null)		
		and ("CodigoPrecioFuturo" >= p_CodigoPrecioFuturo_Desde or p_CodigoPrecioFuturo_Desde is null)
		and ("CodigoPrecioFuturo" <= p_CodigoPrecioFuturo_Hasta or p_CodigoPrecioFuturo_Hasta is null)		
		and ("PrecioFuturo" >= p_PrecioFuturo_Desde or p_PrecioFuturo_Desde is null)
		and ("PrecioFuturo" <= p_PrecioFuturo_Hasta or p_PrecioFuturo_Hasta is null)
		and ("CodigoMonedaFuturo" like p_CodigoMonedaFuturo or p_CodigoMonedaFuturo is null)	
		and ("CodigoUnidad" >= p_CodigoUnidad_Desde or p_CodigoUnidad_Desde is null)
		and ("CodigoUnidad" <= p_CodigoUnidad_Hasta or p_CodigoUnidad_Hasta is null)
		and ("MontoNocional" >= p_MontoNocional_Desde or p_MontoNocional_Desde is null)
		and ("MontoNocional" <= p_MontoNocional_Hasta or p_MontoNocional_Hasta is null)	
		and ("CodigoSegundaUnidad" >= p_CodigoSegundaUnidad_Desde or p_CodigoSegundaUnidad_Desde is null)
		and ("CodigoSegundaUnidad" <= p_CodigoSegundaUnidad_Hasta or p_CodigoSegundaUnidad_Hasta is null)		
		and ("SegundoMontoNocional" like p_SegundoMontoNocional or p_SegundoMontoNocional is null)		
		and ("FechaVencimiento" >= p_FechaVencimiento_Desde or p_FechaVencimiento_Desde is null)
		and ("FechaVencimiento" <= p_FechaVencimiento_Hasta or p_FechaVencimiento_Hasta is null);
    end if;

  --A ctualiza TipoAcuerdoMarco	
  IF x_TipoAcuerdoMarco IS NOT NULL THEN
	update "BSCL.Normativa.BD::TRI.DatosDJ1820"
	set "TipoAcuerdoMarco" = x_TipoAcuerdoMarco
	where "IdDocumento" = p_IdDocumento
		and ("VboUsuario" >= p_VboUsuario_Desde or p_VboUsuario_Desde is null)
		and ("VboUsuario" >= p_VboUsuario_Hasta or p_VboUsuario_Hasta is null)		
		and ("Observaciones" like p_Observaciones or p_Observaciones is null)
		and ("FuenteDatos" 	like p_FuenteDatos or p_FuenteDatos is null)
		and ("RutCliente" 	like p_RutCliente or p_RutCliente is null)
		and ("TaxID" 		like p_TaxID or p_TaxID is null)		
		and ("CodigoPaisResidencia" >= p_CodigoPaisResidencia_Desde or p_CodigoPaisResidencia_Desde is null)
		and ("CodigoPaisResidencia" <= p_CodigoPaisResidencia_Hasta or p_CodigoPaisResidencia_Hasta is null)         
		and ("CodigoRelacion" >= p_CodigoRelacion_Desde or p_CodigoRelacion_Desde is null)
		and ("CodigoRelacion" <= p_CodigoRelacion_Hasta or p_CodigoRelacion_Hasta is null)         
		and ("CodigoModoContrato" >= p_CodigoModoContrato_Desde or p_CodigoModoContrato_Desde is null)
		and ("CodigoModoContrato" <= p_CodigoModoContrato_Hasta or p_CodigoModoContrato_Hasta is null)
		and ("NumeroAcuerdoMarco" >= p_NumeroAcuerdoMarco_Desde or p_NumeroAcuerdoMarco_Desde is null)
		and ("NumeroAcuerdoMarco" <= p_NumeroAcuerdoMarco_Hasta or p_NumeroAcuerdoMarco_Hasta is null)			
		and ("FechaSuscripcionContrato" >= p_FechaSuscripcionContrato_Desde or p_FechaSuscripcionContrato_Desde is null)
		and ("FechaSuscripcionContrato" <= p_FechaSuscripcionContrato_Hasta or p_FechaSuscripcionContrato_Hasta is null)
        and ("NumeroContrato" >= p_NumeroContrato_Desde or p_NumeroContrato_Desde is null)
        and ("NumeroContrato" <= p_NumeroContrato_Hasta or p_NumeroContrato_Hasta is null)        
		and ("FechaSuscripcionAcuerdo" >= p_FechaSuscripcionAcuerdo_Desde or p_FechaSuscripcionAcuerdo_Desde is null)
		and ("FechaSuscripcionAcuerdo" <= p_FechaSuscripcionAcuerdo_Hasta or p_FechaSuscripcionAcuerdo_Hasta is null)        
		and ("TipoEvento" >= p_TipoEvento_Desde or p_TipoEvento_Desde is null)
		and ("TipoEvento" <= p_TipoEvento_Hasta or p_TipoEvento_Hasta is null)        
		and ("TipoContrato" >= p_TipoContrato_Desde or p_TipoContrato_Desde is null)
		and ("TipoContrato" <= p_TipoContrato_Hasta or p_TipoContrato_Hasta is null)	
		and ("Instrumento" 		like p_Instrumento or p_Instrumento is null)		
		and ("ModalidadCumplimiento" >= p_ModalidadCumplimiento_Desde or p_ModalidadCumplimiento_Desde is null)
		and ("ModalidadCumplimiento" <= p_ModalidadCumplimiento_Hasta or p_ModalidadCumplimiento_Hasta is null)		
		and ("CodigoPosicionDeclarante" >= p_CodigoPosicionDeclarante_Desde or p_CodigoPosicionDeclarante_Desde is null)
		and ("CodigoPosicionDeclarante" <= p_CodigoPosicionDeclarante_Hasta or p_CodigoPosicionDeclarante_Hasta is null)
		and ("TipoActivoSubyacente" >= p_TipoActivoSubyacente_Desde or p_TipoActivoSubyacente_Desde is null)
		and ("TipoActivoSubyacente" <= p_TipoActivoSubyacente_Hasta or p_TipoActivoSubyacente_Hasta is null)		
		and ("OtroActivoSubyacente" like p_OtroActivoSubyacente or p_OtroActivoSubyacente is null)		
		and ("TasaActivoSubyacente" >= p_TasaActivoSubyacente_Desde or p_TasaActivoSubyacente_Desde is null)
		and ("TasaActivoSubyacente" <= p_TasaActivoSubyacente_Hasta or p_TasaActivoSubyacente_Hasta is null)		
		and ("TipoSegundoActivoSubyacente" like p_TipoSegundoActivoSubyacente or p_TipoSegundoActivoSubyacente is null)
		and ("CodigoSegundoActivoSubyacente" like p_CodigoSegundoActivoSubyacente or p_CodigoSegundoActivoSubyacente is null)
		and ("OtroSegundoActivoSubyacente" like p_OtroSegundoActivoSubyacente or p_OtroSegundoActivoSubyacente is null)			
		and ("TasaSegundoActivoSubyacente" >= p_TasaSegundoActivoSubyacente_Desde or p_TasaSegundoActivoSubyacente_Desde is null)
		and ("TasaSegundoActivoSubyacente" <= p_TasaSegundoActivoSubyacente_Hasta or p_TasaSegundoActivoSubyacente_Hasta is null)		
		and ("CodigoPrecioFuturo" >= p_CodigoPrecioFuturo_Desde or p_CodigoPrecioFuturo_Desde is null)
		and ("CodigoPrecioFuturo" <= p_CodigoPrecioFuturo_Hasta or p_CodigoPrecioFuturo_Hasta is null)		
		and ("PrecioFuturo" >= p_PrecioFuturo_Desde or p_PrecioFuturo_Desde is null)
		and ("PrecioFuturo" <= p_PrecioFuturo_Hasta or p_PrecioFuturo_Hasta is null)
		and ("CodigoMonedaFuturo" like p_CodigoMonedaFuturo or p_CodigoMonedaFuturo is null)	
		and ("CodigoUnidad" >= p_CodigoUnidad_Desde or p_CodigoUnidad_Desde is null)
		and ("CodigoUnidad" <= p_CodigoUnidad_Hasta or p_CodigoUnidad_Hasta is null)
		and ("MontoNocional" >= p_MontoNocional_Desde or p_MontoNocional_Desde is null)
		and ("MontoNocional" <= p_MontoNocional_Hasta or p_MontoNocional_Hasta is null)	
		and ("CodigoSegundaUnidad" >= p_CodigoSegundaUnidad_Desde or p_CodigoSegundaUnidad_Desde is null)
		and ("CodigoSegundaUnidad" <= p_CodigoSegundaUnidad_Hasta or p_CodigoSegundaUnidad_Hasta is null)		
		and ("SegundoMontoNocional" like p_SegundoMontoNocional or p_SegundoMontoNocional is null)		
		and ("FechaVencimiento" >= p_FechaVencimiento_Desde or p_FechaVencimiento_Desde is null)
		and ("FechaVencimiento" <= p_FechaVencimiento_Hasta or p_FechaVencimiento_Hasta is null);
    end if;   

end;