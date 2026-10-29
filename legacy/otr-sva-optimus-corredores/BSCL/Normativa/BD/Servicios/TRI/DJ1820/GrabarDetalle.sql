set schema "BSCL";


drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1820.GrabarDetalle";


create procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1820.GrabarDetalle"
(
    ------------ Parametros de Entrada Comunes ----------


    -- ID empresa del login.
    in p_IdEmpresa bigint,


    -- ID usuario del login.
    in p_IdUsuario bigint,


    -- ID modulo invocador.
    in p_IdModulo nvarchar(3),


    ------------ Parametros de Entrada Propios ----------


    -- ID del detalle.
    -- Si el id es mayor que cero, se asume que es un registro existente para actualizar.
    -- En caso contrario, se intenta crear un nuevo registro.
    in p_ID bigint,


     --ID del documento.
    in p_IdDocumento bigint,


    -- VoBo del usuario sobre el registro (S/N).
    --in p_VboUsuario char(1),


    -- Observacion ingresada por usuario que edito el registro.
    --in p_Observaciones nvarchar(250),


    -- Fuente de los datos.
    --in p_FuenteDatos nvarchar(40),


	-- fin comunes
    --in p_IdCliente 						bigint,
    --in p_IdPortafolio 					bigint,
    in p_RutCliente 					nvarchar(40),    
    in p_TaxID 							nvarchar(40),
    in p_CodigoPaisResidencia 			nvarchar(10),
    in p_CodigoRelacion 				nvarchar(10),
    in p_CodigoModoContrato 			nvarchar(40),
    in p_TipoAcuerdoMarco 				nvarchar(40),
    in p_NumeroAcuerdoMarco 			bigint,
    in p_FechaSuscripcionAcuerdo		date,
    in p_NumeroContrato					bigint,
    in p_FechaSuscripcionContrato		date,
    in p_TipoEvento						nvarchar(40),
    in p_TipoContrato					nvarchar(40),
    in p_Instrumento					nvarchar(40),
    in p_ModalidadCumplimiento			nvarchar(40),
    in p_CodigoPosicionDeclarante 		nvarchar(40),
    in p_TipoActivoSubyacente 			nvarchar(40),
    in p_CodigoActivoSubyacente 		nvarchar(40),
    in p_OtroActivoSubyacente			nvarchar(40),
    in p_TasaActivoSubyacente			DECIMAL(26,8),
    in p_TipoSegundoActivoSubyacente	nvarchar(40),
    in p_CodigoSegundoActivoSubyacente	nvarchar(40),
    in p_OtroSegundoActivoSubyacente	nvarchar(40),
    in p_TasaSegundoActivoSubyacente	DECIMAL(26,8),
    in p_CodigoPrecioFuturo			nvarchar(40),
    in p_PrecioFuturo 					DECIMAL(27,8),
    in p_CodigoMonedaFuturo			nvarchar(40),
    in p_CodigoUnidad					nvarchar(40),
    in p_MontoNocional					DECIMAL(27,8),
    in p_CodigoSegundaUnidad			nvarchar(40),
    in p_SegundoMontoNocional			nvarchar(40),--DECIMAL(27,8),
    in p_FechaVencimiento				date,
    IN p_FechaOperacion					date,
    
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


    if p_ID > 0 then
        p_out_id := p_ID;


        update "BSCL.Normativa.BD::TRI.DatosDJ1820" set
            --"VboUsuario"  					= p_VboUsuario,
            --"Observaciones" 				= p_Observaciones,
            --"FuenteDatos"  					= p_FuenteDatos,
            ---
         	--"IdCliente" 					= p_IdCliente,
		   	--"IdPortafolio" 					= p_IdPortafolio,
		    "RutCliente" 					= p_RutCliente,
		    "TaxID" 						= p_TaxID,
		    "CodigoPaisResidencia" 			= p_CodigoPaisResidencia,
		    "CodigoRelacion" 				= p_CodigoRelacion,
		    "CodigoModoContrato" 			= p_CodigoModoContrato,
		    "TipoAcuerdoMarco" 				= p_TipoAcuerdoMarco,
		    "NumeroAcuerdoMarco" 			= p_NumeroAcuerdoMarco,
		 	"FechaSuscripcionAcuerdo"		= p_FechaSuscripcionAcuerdo,
			"NumeroContrato"				= p_NumeroContrato,
			"FechaSuscripcionContrato"		= p_FechaSuscripcionContrato,
			"TipoEvento"					= p_TipoEvento,
			"TipoContrato"					= p_TipoContrato,
			"Instrumento"					= p_Instrumento,
			"ModalidadCumplimiento"			= p_ModalidadCumplimiento,
			"CodigoPosicionDeclarante"		= p_CodigoPosicionDeclarante,
			"TipoActivoSubyacente"			= p_TipoActivoSubyacente,
			"CodigoActivoSubyacente"		= p_CodigoActivoSubyacente,
			"OtroActivoSubyacente"			= p_OtroActivoSubyacente,
			"TasaActivoSubyacente"			= p_TasaActivoSubyacente,
			"TipoSegundoActivoSubyacente"	= p_TipoSegundoActivoSubyacente,
			"CodigoSegundoActivoSubyacente"	= p_CodigoSegundoActivoSubyacente,
			"OtroSegundoActivoSubyacente"	= p_OtroSegundoActivoSubyacente,
			"TasaSegundoActivoSubyacente"	= p_TasaSegundoActivoSubyacente,
			"CodigoPrecioFuturo"			= p_CodigoPrecioFuturo,
			"PrecioFuturo" 					= p_PrecioFuturo,
			"CodigoMonedaFuturo"			= p_CodigoMonedaFuturo,
			"CodigoUnidad"					= p_CodigoUnidad,
			"MontoNocional"					= p_MontoNocional,
			"CodigoSegundaUnidad"			= p_CodigoSegundaUnidad,
			"SegundoMontoNocional"			= p_SegundoMontoNocional,
			"FechaVencimiento"				= p_FechaVencimiento,
			"FechaOperacion"				= p_FechaOperacion
        where "ID" = p_ID;
    else
        select ifnull(max("ID"),0)+1 into p_out_id from "BSCL.Normativa.BD::TRI.DatosDJ1820";


        insert into "BSCL.Normativa.BD::TRI.DatosDJ1820"(
            "ID",
            "IdDocumento",
            "VboUsuario",
            "Observaciones",
            "FuenteDatos",
            ---
        	--"IdCliente" ,
		   	--"IdPortafolio",
		    "RutCliente",
		    "TaxID",
		    "CodigoPaisResidencia",
		    "CodigoRelacion",
		    "CodigoModoContrato",
		    "TipoAcuerdoMarco",
		    "NumeroAcuerdoMarco",
		 	"FechaSuscripcionAcuerdo",
			"NumeroContrato",
			"FechaSuscripcionContrato",
			"TipoEvento",
			"TipoContrato",
			"Instrumento",
			"ModalidadCumplimiento",
			"CodigoPosicionDeclarante",
			"TipoActivoSubyacente",
			"CodigoActivoSubyacente",
			"OtroActivoSubyacente",
			"TasaActivoSubyacente",
			"TipoSegundoActivoSubyacente",
			"CodigoSegundoActivoSubyacente",
			"OtroSegundoActivoSubyacente",
			"TasaSegundoActivoSubyacente",
			"CodigoPrecioFuturo",
			"PrecioFuturo",
			"CodigoMonedaFuturo",
			"CodigoUnidad",
			"MontoNocional",
			"CodigoSegundaUnidad",
			"SegundoMontoNocional",
			"FechaVencimiento",
			"FechaOperacion"
        ) values (
            p_out_id,
            p_IdDocumento,
            'N',
            '',
       	    'MANTENEDOR',
        ---
            --0,--p_IdCliente,
            --0,--p_IdPortafolio,
            p_RutCliente,
            p_TaxID,
            p_CodigoPaisResidencia,
            p_CodigoRelacion,
		    p_CodigoModoContrato,
		    p_TipoAcuerdoMarco,
		    p_NumeroAcuerdoMarco,
		    p_FechaSuscripcionAcuerdo,
		    p_NumeroContrato,
			p_FechaSuscripcionContrato,
			p_TipoEvento,
			p_TipoContrato,
			p_Instrumento,
			p_ModalidadCumplimiento,
			p_CodigoPosicionDeclarante,
			p_TipoActivoSubyacente,
			p_CodigoActivoSubyacente,
			p_OtroActivoSubyacente,
			p_TasaActivoSubyacente,
			p_TipoSegundoActivoSubyacente,
			p_CodigoSegundoActivoSubyacente,
			p_OtroSegundoActivoSubyacente,
			p_TasaSegundoActivoSubyacente,
			p_CodigoPrecioFuturo,
			p_PrecioFuturo,
			p_CodigoMonedaFuturo,
			p_CodigoUnidad,
			p_MontoNocional,
			p_CodigoSegundaUnidad,
			p_SegundoMontoNocional,
			p_FechaVencimiento,
			p_FechaOperacion
	          
        );
    end if;
end;