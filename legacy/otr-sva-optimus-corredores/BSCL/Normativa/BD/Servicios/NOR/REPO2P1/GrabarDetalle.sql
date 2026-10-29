SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.REPO2P1.GrabarDetalle";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.REPO2P1.GrabarDetalle"(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    IN p_IdEmpresa 								BIGINT		 ,
    -- ID usuario del login.
    IN p_IdUsuario 								BIGINT		 ,
    -- ID modulo invocador.
    IN p_IdModulo 								NVARCHAR(3)	 ,
    ------------ Parametros de Entrada Propios ----------
     -- ID del detalle.
    -- Si el id es mayor que cero, se asume que es un registro existente para actualizar.
    -- En caso contrario, se intenta crear un nuevo registro.
    IN p_ID                                     BIGINT   ,
    -- ID del documento.
    IN p_IdDocumento                            BIGINT   ,
    -- VoBo del usuario sobre el registro (S/N).
    IN p_Estado                             	CHAR(3)   ,
    -- Observacion ingresada por usuario que edito el registro.
    IN p_EstadoSICAM                          	NVARCHAR(3),
    
    IN p_SICAM                          		NVARCHAR(40),
    -- Fuente de los datos.
    IN p_FuenteDatos                            NVARCHAR(40) ,


    --camos layout    
    IN p_TipoRegistro							NVARCHAR(2),
	IN p_EventoReporte							NVARCHAR(1),
	IN p_TipoOperacion							NVARCHAR(2),
	IN p_FechaNegociacion						DATE,
    IN p_HoraNegociacion						NVARCHAR(8),
	IN p_FechaValutaMonedaRecibida				DATE,
	IN p_FechaValutaMonedaEntregada				DATE,
	IN p_Identificador							NVARCHAR(15),
	IN p_RUTContraparte1						NVARCHAR(11),
	IN p_LEIContraparte1						NVARCHAR(20),
	IN p_NombreContraparte1						NVARCHAR(50),
	IN p_RUTContraparte2						NVARCHAR(11),
	IN p_LEIContraparte2						NVARCHAR(20),
	IN p_NombreContraparte2						NVARCHAR(50),
	IN p_PaisContraparte2						NVARCHAR(3) , --Verificar HDU pais 1
	IN p_RUTIntermediario						NVARCHAR(11),
	IN p_PlataformaNegociacion 					NVARCHAR(4),
	IN p_CodigoOperacionCambio					NVARCHAR(6) ,
	IN p_PaisMonedaExtranjera					NVARCHAR(3) ,
	IN p_MonedaCompradaRecibida					NVARCHAR(3) ,
	IN p_MontoMonedaCompradaRecibida			DECIMAL(26,8),
	IN p_MonedaVendidaEntregada					NVARCHAR(3) ,
	IN p_MontoMonedaVendidaEntregada			DECIMAL(26,8),
	IN p_ConvencionTipoCambio					NVARCHAR(7),
	IN p_TipoCambio								DECIMAL(26,8),
	IN p_AfectaDerivados						NVARCHAR(1),
	IN p_IdentificadorContratoDerivados			NVARCHAR(52),
	IN p_FechaSuscripcionContrato				DATE,
	IN p_HoraSuscripcionContrato				NVARCHAR(8),
	IN p_OperacionEmpresaRelacionada			NVARCHAR(1),
	IN p_NumeroInscripcionObligacionExterior	NVARCHAR(50),
    IN p_NombreFinancistaAporante				NVARCHAR(50),
    IN p_LEIFinancistaAporante					NVARCHAR(20),
	IN p_NombreReceptorInversion				NVARCHAR(50),
    IN p_LEIReceptorInversion					NVARCHAR(20),
    IN p_ModalidadInversionCustodio				NVARCHAR(2),
--campos de control interno especifico IdentificadorContratoDerivados
	IN p_IdCliente								BIGINT,
	IN p_IdOperacion							BIGINT,
	IN p_IdTipoOperacion						NVARCHAR(6),
	IN p_Nemo									NVARCHAR(40),
	IN p_IdMonedaPrincipal						NVARCHAR(3), 
	IN p_IdMonedaSecundaria						NVARCHAR(3), 
	IN p_FolioOperacion							BIGINT,
	IN p_FechaLiquidacion						DATE,
	IN p_IdCartera								BIGINT,

    
    
    ------------ Parametros de Salida Propios ----------
    -- ID del registro.
    OUT p_out_id                                BIGINT,

    ------------ Salidas Advertencias ----------
    -- Contador de advertencias.
    OUT p_out_AdvertenciasContador              INT,

    -- Tabla de advertencias.
    OUT p_out_AdvertenciasDetalle TABLE("Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)),

    ------------ Salidas Errores ----------
    -- Contador de errores
    -- Importante: Debe ser cero para que se haga commit.
    OUT p_out_ErroresContador                   INT,

    -- Tabla de errores.
    OUT p_out_ErroresDetalle TABLE("Codigo" NVARCHAR(100), "Descripcion" NVARCHAR(5000))
)
    LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS BEGIN

	------------ Variables validaciones -----------

	DECLARE v_HoraOperacion TIME;

	DECLARE v_RUTContraparte1 NVARCHAR(40);
	DECLARE v_NombreContraparte1 NVARCHAR(100);


    ------------ Variables Propias ----------

	DECLARE v_LEIContraparte1 NVARCHAR(520);
	

    ------------ Variables autorizacion:
	DECLARE v_tipoEntidad						NVARCHAR(40);
	DECLARE v_IdConcepto	       				NVARCHAR(6);						
	DECLARE v_countEmpleado				 		BIGINT;
	DECLARE v_estadoAutorizacion				NVARCHAR(3);
	DECLARE v_RequiereAutorizacion				NVARCHAR(1);
	DECLARE v_secuencia_IdSolicitudCambio		BIGINT;
	DECLARE v_countCodigoEmpleado 				BIGINT:=0;
	DECLARE v_CodigoEmpleado 					NVARCHAR(40);
	DECLARE v_count_CodigoEmpleadoExiste		BIGINT:=0;
	DECLARE v_out_IdSecuencias 					table ("IdSecuencia" BIGINT);
	DECLARE p_out_RefId 						BIGINT;
	DECLARE v_fechaHoraSistema 					NVARCHAR(40);
	DECLARE v_erroroutmsg  						nvarchar(520);	
	DECLARE v_FechaDatosHasta					DATE;
	DECLARE v_FechaDatosDesde					DATE;
	DECLARE v_countValidacion			 		BIGINT;
	DECLARE v_Version 							NVARCHAR(10);
	
	
    ------------ Bloque Comun de Inicializacion ----------
    DECLARE salida_controlada condition;
    DECLARE exit handler FOR salida_controlada
    BEGIN
        SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
        SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
    END;

    DECLARE exit handler FOR sqlexception
    BEGIN
        DECLARE v_errorExt NVARCHAR(40);
        IF ::sql_error_code > 10000 THEN
             -- Error pseudo-controlado pero desde un sub-sp.
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError"(::sql_error_code, v_errorExt);
            p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL
                                   SELECT v_errorExt, ::sql_error_message FROM dummy;
            SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
        ELSE
            -- Error interno no manejado (el log sera captado por la capa de XS control).
            RESIGNAL;
        END IF;
    END;

    p_out_AdvertenciasContador := 0;
    p_out_ErroresContador := 0;
    p_out_AdvertenciasDetalle = select '' as "Codigo", '' as "RequiereAutorizacion", '' as "Descripcion" from dummy where 1 = 2;
    p_out_ErroresDetalle = select '' as "Codigo", '' as "Descripcion" from dummy where 1 = 2;
    
    --Inicializacion de variables autorizacion:
	v_secuencia_IdSolicitudCambio 	:= -1;
	v_estadoAutorizacion 			:= 'PEN';
	v_tipoEntidad					:= 'ID';
	v_IdConcepto					:= 'R2P1MD';			
	p_out_RefId						:=0;
	
	call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
	p_IdEmpresa => p_IdEmpresa,
	p_out_FechaHoraSistema => v_fechaHoraSistema
	);	
    
	----------- VALIDACIONES ---------------
	-- OPERACIONES 01 Y 02 (Compra y Venta)
	SELECT CLI."DocumentoIdentidad",EMP."RazonSocial" INTO v_RUTContraparte1,v_NombreContraparte1
	FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP
	INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (SOC."IdSocioDeNegocio"=EMP."IdSocioDeNegocio")	
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI  ON (CLI."IdCliente"=SOC."IdCliente")
	WHERE EMP."IdEmpresa"=p_IdEmpresa;

	select LEI1."Valor" INTO v_LEIContraparte1 from "BSCL.Normativa.BD::NOR.Configuracion" LEI1 			
	WHERE (LEI1."CodigoParametro"='BCC_REPO2P1_LEICONTRAPARTE1'  AND LEI1."IdNormativa"='REPO2P1');

	IF p_MonedaCompradaRecibida ='CLP' AND p_MonedaVendidaEntregada = 'USD' THEN
		p_ConvencionTipoCambio := 'USD/CLP';
	END IF;

	IF p_TipoOperacion = '01' OR p_TipoOperacion = '02' THEN
		IF NOT (p_HoraNegociacion LIKE_REGEXPR '^([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$') THEN
			p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
			v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_HORA_NEGOCIACION_FORMATO_INVALIDO', :v_hash, :v_erroroutmsg);
			p_out_AdvertenciasDetalle =
				SELECT 'UI_REPO2P1_HORA_NEGOCIACION_FORMATO_INVALIDO' AS "Codigo",
					'N' AS "RequiereAutorizacion",
					CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
				FROM dummy
				UNION ALL
				SELECT * FROM :p_out_AdvertenciasDetalle;

			SIGNAL salida_controlada;
		END IF;

		IF (p_FechaValutaMonedaRecibida = '' OR p_FechaValutaMonedaRecibida IS NULL OR p_FechaValutaMonedaRecibida = '0001-01-01') 
			OR (p_FechaValutaMonedaEntregada = '' OR p_FechaValutaMonedaEntregada  IS NULL OR p_FechaValutaMonedaEntregada = '0001-01-01') THEN
			p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
			v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_FECHAS_VALUTA', :v_hash, :v_erroroutmsg);
			p_out_AdvertenciasDetalle =
				SELECT 'UI_REPO2P1_FECHAS_VALUTA' AS "Codigo",
					'N' AS "RequiereAutorizacion",
					CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
				FROM dummy
				UNION ALL
				SELECT * FROM :p_out_AdvertenciasDetalle;

			SIGNAL salida_controlada;
		END IF;	

		

		IF p_LEIContraparte2 = '' AND p_RUTContraparte2 = '' THEN
			p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
			v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_LEI_O_RUTCONTRAPARTE2', :v_hash, :v_erroroutmsg);
			p_out_AdvertenciasDetalle =
				SELECT 'UI_REPO2P1_LEI_O_RUTCONTRAPARTE2' AS "Codigo",
					'N' AS "RequiereAutorizacion",
					CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
				FROM dummy
				UNION ALL
				SELECT * FROM :p_out_AdvertenciasDetalle;
			SIGNAL salida_controlada;
		END IF;

		IF p_RUTContraparte2 <> '' THEN
			IF NOT (p_RUTContraparte2 LIKE_REGEXPR '^[0-9]{7,8}-[0-9Kk]{1}$') THEN
				p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
				v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;

				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_RUT_CONTRAPARTE2', :v_hash, :v_erroroutmsg);

				p_out_AdvertenciasDetalle =
					SELECT 'UI_REPO2P1_RUT_CONTRAPARTE2' AS "Codigo",
						'N' AS "RequiereAutorizacion",
						CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
					FROM dummy
					UNION ALL
					SELECT * FROM :p_out_AdvertenciasDetalle;

				SIGNAL salida_controlada;
			END IF;
		END IF;

		IF p_RUTIntermediario <> '' THEN
			IF NOT (p_RUTIntermediario LIKE_REGEXPR '^[0-9]{7,8}-[0-9Kk]{1}$') THEN
				p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
				v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;

				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_RUT_INTERMEDIARIO_INCORRECTO', :v_hash, :v_erroroutmsg);

				p_out_AdvertenciasDetalle =
					SELECT 'UI_REPO2P1_RUT_INTERMEDIARIO_INCORRECTO' AS "Codigo",
						'N' AS "RequiereAutorizacion",
						CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
					FROM dummy
					UNION ALL
					SELECT * FROM :p_out_AdvertenciasDetalle;

				SIGNAL salida_controlada;
			END IF;
		END IF;

		IF (TRIM(p_MonedaCompradaRecibida) = '' OR TRIM(p_MonedaVendidaEntregada)= '') AND p_TipoOperacion IN ('01','02') THEN
			p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
            v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_MONEDA_COMPRADA_RECIBIDA_01_02', :v_hash, :v_erroroutmsg);
            p_out_AdvertenciasDetalle =
                SELECT 'UI_REPO2P1_MONEDA_COMPRADA_RECIBIDA_01_02' AS "Codigo",
                    'N' AS "RequiereAutorizacion",
                    CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
                FROM dummy
                UNION ALL
                SELECT * FROM :p_out_AdvertenciasDetalle;

            SIGNAL salida_controlada;
    	END IF;

		IF p_MontoMonedaVendidaEntregada <= CAST(0 AS DECIMAL(26,8)) OR p_MontoMonedaCompradaRecibida <= CAST(0 AS DECIMAL(26,8))  THEN
        	p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
            v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_MONTOS_VALOR_MAYOR_A_CERO', :v_hash, :v_erroroutmsg);
            p_out_AdvertenciasDetalle =
                SELECT 'UI_REPO2P1_MONTOS_VALOR_MAYOR_A_CERO' AS "Codigo",
                    'N' AS "RequiereAutorizacion",
                    CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
                FROM dummy
                UNION ALL
                SELECT * FROM :p_out_AdvertenciasDetalle;

            SIGNAL salida_controlada;
    	END IF;

		IF p_TipoCambio <= CAST(0 AS DECIMAL(26,8)) THEN
			p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
            v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_TIPO_CAMBIO_MAYOR_A_CERO', :v_hash, :v_erroroutmsg);
            p_out_AdvertenciasDetalle =
                SELECT 'UI_REPO2P1_TIPO_CAMBIO_MAYOR_A_CERO' AS "Codigo",
                    'N' AS "RequiereAutorizacion",
                    CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
                FROM dummy
                UNION ALL
                SELECT * FROM :p_out_AdvertenciasDetalle;

            SIGNAL salida_controlada;
    	END IF;

		IF p_ConvencionTipoCambio = '' OR LENGTH(p_MonedaCompradaRecibida || '/' || p_MonedaVendidaEntregada) <> 7  THEN
			p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
            v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_CONVENCION_TIPO_CAMBIO', :v_hash, :v_erroroutmsg);
            p_out_AdvertenciasDetalle =
                SELECT 'UI_REPO2P1_CONVENCION_TIPO_CAMBIO' AS "Codigo",
                    'N' AS "RequiereAutorizacion",
                    CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
                FROM dummy
                UNION ALL
                SELECT * FROM :p_out_AdvertenciasDetalle;

            SIGNAL salida_controlada;
    	END IF;

		


	END IF;
	----------------- OPERACION 03 Y 04 -------------------
	IF p_TipoOperacion = '03' OR p_TipoOperacion = '04' THEN
	
		IF NOT (p_HoraNegociacion LIKE_REGEXPR '^([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$') THEN
			p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
			v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_HORA_NEGOCIACION_FORMATO_INVALIDO', :v_hash, :v_erroroutmsg);
			p_out_AdvertenciasDetalle =
				SELECT 'UI_REPO2P1_HORA_NEGOCIACION_FORMATO_INVALIDO' AS "Codigo",
					'N' AS "RequiereAutorizacion",
					CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
				FROM dummy
				UNION ALL
				SELECT * FROM :p_out_AdvertenciasDetalle;

			SIGNAL salida_controlada;
			
		END IF;

		

		IF p_LEIContraparte2 = '' AND p_RUTContraparte2 = '' THEN
			p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
			v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_LEI_O_RUTCONTRAPARTE2', :v_hash, :v_erroroutmsg);
			p_out_AdvertenciasDetalle =
				SELECT 'UI_REPO2P1_LEI_O_RUTCONTRAPARTE2' AS "Codigo",
					'N' AS "RequiereAutorizacion",
					CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
				FROM dummy
				UNION ALL
				SELECT * FROM :p_out_AdvertenciasDetalle;
			SIGNAL salida_controlada;
		END IF;

		IF p_RUTContraparte2 <> '' THEN
			IF NOT (p_RUTContraparte2 LIKE_REGEXPR '^[0-9]{7,8}-[0-9Kk]{1}$') THEN
				p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
				v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;

				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_RUT_CONTRAPARTE2', :v_hash, :v_erroroutmsg);

				p_out_AdvertenciasDetalle =
					SELECT 'UI_REPO2P1_RUT_CONTRAPARTE2' AS "Codigo",
						'N' AS "RequiereAutorizacion",
						CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
					FROM dummy
					UNION ALL
					SELECT * FROM :p_out_AdvertenciasDetalle;

				SIGNAL salida_controlada;
			END IF;
		END IF;

		IF p_RUTIntermediario <> '' THEN
			IF NOT (p_RUTIntermediario LIKE_REGEXPR '^[0-9]{7,8}-[0-9Kk]{1}$') THEN
				p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
				v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;

				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_RUT_INTERMEDIARIO_INCORRECTO', :v_hash, :v_erroroutmsg);

				p_out_AdvertenciasDetalle =
					SELECT 'UI_REPO2P1_RUT_INTERMEDIARIO_INCORRECTO' AS "Codigo",
						'N' AS "RequiereAutorizacion",
						CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
					FROM dummy
					UNION ALL
					SELECT * FROM :p_out_AdvertenciasDetalle;

				SIGNAL salida_controlada;
			END IF;
		END IF;
		

		-- transferencia Ingreso

		IF (p_FechaValutaMonedaRecibida = '' OR p_FechaValutaMonedaRecibida IS NULL OR p_FechaValutaMonedaRecibida = '0001-01-01') AND p_TipoOperacion = '03'
		OR NOT (p_FechaValutaMonedaRecibida LIKE_REGEXPR '^[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$') THEN
			p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
            v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_FECHA_VALUTA_MONEDA_RECIBIDA_03', :v_hash, :v_erroroutmsg);
            p_out_AdvertenciasDetalle =
                SELECT 'UI_REPO2P1_FECHA_VALUTA_MONEDA_RECIBIDA_03' AS "Codigo",
                    'N' AS "RequiereAutorizacion",
                    CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
                FROM dummy
                UNION ALL
                SELECT * FROM :p_out_AdvertenciasDetalle;

            SIGNAL salida_controlada;
    	END IF;

		IF TRIM(p_MonedaCompradaRecibida) = '' AND p_TipoOperacion = '03' THEN
			p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
            v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_MONEDA_COMPRADA_RECIBIDA_03', :v_hash, :v_erroroutmsg);
            p_out_AdvertenciasDetalle =
                SELECT 'UI_REPO2P1_MONEDA_COMPRADA_RECIBIDA_03' AS "Codigo",
                    'N' AS "RequiereAutorizacion",
                    CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
                FROM dummy
                UNION ALL
                SELECT * FROM :p_out_AdvertenciasDetalle;

            SIGNAL salida_controlada;
    	END IF;

		IF p_MontoMonedaCompradaRecibida <= CAST(0 AS DECIMAL(26,8))  AND p_TipoOperacion = '03' THEN
			p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
            v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_MONTO_MONEDA_COMPRADA_RECIBIDA_03', :v_hash, :v_erroroutmsg);
            p_out_AdvertenciasDetalle =
                SELECT 'UI_REPO2P1_MONTO_MONEDA_COMPRADA_RECIBIDA_03' AS "Codigo",
                    'N' AS "RequiereAutorizacion",
                    CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
                FROM dummy
                UNION ALL
                SELECT * FROM :p_out_AdvertenciasDetalle;

            SIGNAL salida_controlada;
    	END IF;

		

		--TRANSFERENCIA EGRESO

		IF (p_FechaValutaMonedaEntregada = '' OR p_FechaValutaMonedaEntregada  IS NULL OR p_FechaValutaMonedaEntregada = '0001-01-01') AND p_TipoOperacion = '04'
		OR NOT (p_FechaValutaMonedaEntregada LIKE_REGEXPR '^[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$') THEN
			p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
            v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_FECHA_VALUTA_MONEDA_ENTREGADA_04', :v_hash, :v_erroroutmsg);
            p_out_AdvertenciasDetalle =
                SELECT 'UI_REPO2P1_FECHA_VALUTA_MONEDA_ENTREGADA_04' AS "Codigo",
                    'N' AS "RequiereAutorizacion",
                    CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
                FROM dummy
                UNION ALL
                SELECT * FROM :p_out_AdvertenciasDetalle;

            SIGNAL salida_controlada;
    	END IF;
		
		IF TRIM(p_MonedaVendidaEntregada ) = '' AND p_TipoOperacion = '04' THEN
			p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
            v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_MONEDA_VENDIDA_ENTREGADA_04', :v_hash, :v_erroroutmsg);
            p_out_AdvertenciasDetalle =
                SELECT 'UI_REPO2P1_MONEDA_VENDIDA_ENTREGADA_04' AS "Codigo",
                    'N' AS "RequiereAutorizacion",
                    CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
                FROM dummy
                UNION ALL
                SELECT * FROM :p_out_AdvertenciasDetalle;

            SIGNAL salida_controlada;
    	END IF;

		IF p_MontoMonedaVendidaEntregada <= CAST(0 AS DECIMAL(26,8))  AND p_TipoOperacion = '04' THEN
			p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
            v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_MONTO_MONEDA_VENDIDA_ENTREGADA', :v_hash, :v_erroroutmsg);
            p_out_AdvertenciasDetalle =
                SELECT 'UI_REPO2P1_MONEDA_COMPRADA_RECIBIDA_04' AS "Codigo",
                    'N' AS "RequiereAutorizacion",
                    CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
                FROM dummy
                UNION ALL
                SELECT * FROM :p_out_AdvertenciasDetalle;

            SIGNAL salida_controlada;
    	END IF;

		



		

	END IF;

	IF p_AfectaDerivados = 'S' THEN

			IF TRIM(p_IdentificadorContratoDerivados) = '' THEN
				p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
				v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_IDENTIFICADOR_DERIVADOS', :v_hash, :v_erroroutmsg);
				p_out_AdvertenciasDetalle =
					SELECT 'UI_REPO2P1_TIPO_CAMBIO_MAYOR_A_CERO' AS "Codigo",
						'N' AS "RequiereAutorizacion",
						CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
					FROM dummy
					UNION ALL
					SELECT * FROM :p_out_AdvertenciasDetalle;

				SIGNAL salida_controlada;
			END IF;

			IF p_FechaSuscripcionContrato = '' OR p_FechaSuscripcionContrato IS NULL OR p_FechaSuscripcionContrato = '0001-01-01' THEN
			p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
			v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_FECHA_SUSCRIPCION_FORMATO_INVALIDO', :v_hash, :v_erroroutmsg);
			p_out_AdvertenciasDetalle =
				SELECT 'UI_REPO2P1_FECHA_SUSCRIPCION_FORMATO_INVALIDO' AS "Codigo",
					'N' AS "RequiereAutorizacion",
					CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
				FROM dummy
				UNION ALL
				SELECT * FROM :p_out_AdvertenciasDetalle;
				SIGNAL salida_controlada;
			END IF;

			IF NOT (p_HoraSuscripcionContrato LIKE_REGEXPR '^([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$') THEN
			p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
			v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_HORA_SUSCRIPCION_FORMATO_INVALIDO', :v_hash, :v_erroroutmsg);
			p_out_AdvertenciasDetalle =
				SELECT 'UI_REPO2P1_HORA_SUSCRIPCION_FORMATO_INVALIDO' AS "Codigo",
					'N' AS "RequiereAutorizacion",
					CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
				FROM dummy
				UNION ALL
				SELECT * FROM :p_out_AdvertenciasDetalle;
				SIGNAL salida_controlada;
			END IF;

		ELSE 
			IF p_IdentificadorContratoDerivados <> '' THEN
				p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
				v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_SIN_DERIVADOS', :v_hash, :v_erroroutmsg);
				p_out_AdvertenciasDetalle =
					SELECT 'UI_REPO2P1_TIPO_CAMBIO_MAYOR_A_CERO' AS "Codigo",
						'N' AS "RequiereAutorizacion",
						CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
					FROM dummy
					UNION ALL
					SELECT * FROM :p_out_AdvertenciasDetalle;

				SIGNAL salida_controlada;
			END IF;

			IF p_FechaSuscripcionContrato <> '' AND p_FechaSuscripcionContrato IS NOT NULL AND p_FechaSuscripcionContrato <> '0001-01-01' THEN
			p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
			v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_SIN_DERIVADOS', :v_hash, :v_erroroutmsg);
			p_out_AdvertenciasDetalle =
				SELECT 'UI_REPO2P1_SIN_DERIVADOS' AS "Codigo",
					'N' AS "RequiereAutorizacion",
					CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
				FROM dummy
				UNION ALL
				SELECT * FROM :p_out_AdvertenciasDetalle;
				SIGNAL salida_controlada;
			END IF;

			IF p_HoraSuscripcionContrato IS NOT NULL AND p_HoraSuscripcionContrato <> '' THEN
				p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
				v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_REPO2P1_SIN_DERIVADOS', :v_hash, :v_erroroutmsg);
				p_out_AdvertenciasDetalle =
					SELECT 'UI_REPO2P1_SIN_DERIVADOS' AS "Codigo",
						'N' AS "RequiereAutorizacion",
						CAST('VALIDACION->' || :v_erroroutmsg AS NVARCHAR(520)) AS "Descripcion"
					FROM dummy
					UNION ALL
					SELECT * FROM :p_out_AdvertenciasDetalle;
					SIGNAL salida_controlada;
			END IF;
		
	END IF;
	
	
	
	
	--------------------CIERRE 03 Y 04------------------
	
	
 SELECT LAST_DAY(ESQ."FechaDatosDesde"),ESQ."FechaDatosDesde",ESQ."Version" INTO v_FechaDatosHasta,v_FechaDatosDesde, v_Version
 FROM "BSCL.Normativa.BD::DOC.Documento" DOC 
 INNER JOIN "BSCL.Normativa.BD::DOC.Esquema" ESQ ON (ESQ."Esquema"=DOC."Esquema" AND ESQ."Version"=DOC."Version")
 WHERE "IdDocumento"=p_IdDocumento;

 v_tablaValidacion =
	--'Condiciones'
 SELECT 'FechaNegociacion_Invalida' AS "VALIDACION", CASE DATS_IS_VALID(REPLACE(p_FechaNegociacion,'-',''))  WHEN 0 THEN 'FALSE' ELSE 'OK'  END AS "RESULTADO"
 FROM DUMMY
 UNION ALL 
 SELECT 'FechaNegociacion_Mayor' AS "VALIDACION",
        CASE WHEN  LAST_DAY(CASE DATS_IS_VALID(REPLACE(p_FechaNegociacion,'-','')) WHEN 0 THEN TO_DATE('0001-01-01') ELSE LAST_DAY(p_FechaNegociacion) END) > v_FechaDatosHasta THEN 'FALSE' ELSE 'OK' END AS "RESULTADO"
 FROM DUMMY
 UNION ALL 
 SELECT 'FechaNegociacion_Menor' AS "VALIDACION",
        CASE WHEN  (CASE DATS_IS_VALID(REPLACE(p_FechaNegociacion,'-','')) WHEN 0 THEN TO_DATE('9999-01-01') ELSE p_FechaNegociacion END) < v_FechaDatosDesde THEN 'FALSE' ELSE 'OK' END AS "RESULTADO"
 FROM DUMMY;
 
 SELECT COUNT(1) INTO v_countValidacion FROM :v_tablaValidacion WHERE "RESULTADO" ='FALSE';
 ---Descripcion
 if v_countValidacion > 0 THEN
 v_tablaValidacionDescripcion=
	 SELECT "VALIDACION",
		 CASE "VALIDACION" 	WHEN 'FechaNegociacion_Invalida' 	THEN 'UI_FECHA_INVALIDA'	WHEN 'FechaNegociacion_Mayor' 		THEN 'UI_FECHA_NEGOCIACION_MAYOR_AL_PERIODO'
							WHEN 'FechaNegociacion_Menor' 		THEN 'UI_FECHA_NEGOCIACION_MENOR_AL_PERIODO'
		 ELSE ''	END AS "CodigoMensaje",
		 CASE WHEN "VALIDACION" IN ('FechaNegociacion_Mayor','FechaNegociacion_Menor') 	THEN 'FechaNegociacion'
		 ELSE ''	END AS "CodigoDato1",
		 CASE WHEN "VALIDACION" IN ('FechaNegociacion_Mayor','FechaNegociacion_Menor')	THEN CAST (p_FechaNegociacion AS NVARCHAR(10))
		 ELSE''		END AS"ValorDato1",
		 CASE WHEN "VALIDACION" IN ('FechaNegociacion_Mayor','FechaNegociacion_Menor') 	THEN 'Periodo'
		 ELSE ''	END As "CodigoDato2",
		 CASE WHEN "VALIDACION" IN ('FechaNegociacion_Mayor','FechaNegociacion_Menor')	THEN v_Version
		 ELSE ''	END As "ValorDato2"
		 FROM :v_tablaValidacion WHERE "RESULTADO" ='FALSE';
 end if;
 
 if v_countValidacion > 0 then
	--Mensaje de Salida
	p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_countValidacion;	
	v_TablaCodigosMensajes = SELECT ROW_NUMBER() OVER (ORDER BY "CodigoMensaje") AS "IdInterno", "CodigoMensaje" AS "Codigo",  "CodigoDato1", "ValorDato1", "CodigoDato2", "ValorDato2"
							 FROM :v_tablaValidacionDescripcion;
	
	CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
	
	p_out_AdvertenciasDetalle =  select "Codigo" as "Codigo",'N' as "RequiereAutorizacion",CAST('VALIDACION->' || "Descripcion"  AS NVARCHAR(520))as "Descripcion" from :v_out_ErroresDetalleMensajes UNION ALL
							     select * from :p_out_AdvertenciasDetalle;
	SIGNAL salida_controlada;
 end if;	
 

	--------- FIN VALIDACIONES -----------
    ------------ Logica de Actualizacion ----------
    IF p_ID > 0 THEN
        p_out_id := p_ID;

        	SELECT "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio".NEXTVAL into v_secuencia_IdSolicitudCambio from dummy;
			
			    V_RegistroSolicitudCambio =SELECT	
					v_secuencia_IdSolicitudCambio AS "IdSolicitudCambio",	
					0 AS		"IdSecuenciaCambio", --- Se crea interno
					v_tipoEntidad	AS "IdTipoEntidad",	
					CAST (p_ID AS NVARCHAR(40))		AS "IdEntidadOrigen",
					MOD."COLUMN_NAME" as "CampoModificado",
					MOD."DATA_TYPE_NAME" AS "TipoDato",
					(CASE MOD."COLUMN_NAME" WHEN 'TipoRegistro'								THEN IFNULL(CAST (DJ."TipoRegistro" AS NVARCHAR(255)),'')
											WHEN 'EventoReporte'							THEN IFNULL(CAST (DJ."EventoReporte" AS NVARCHAR(255)),'')
											WHEN 'TipoOperacion'							THEN IFNULL(CAST (DJ."TipoOperacion" AS NVARCHAR(255)),'')
											WHEN 'FechaNegociacion'							THEN IFNULL(CAST (DJ."FechaNegociacion" AS NVARCHAR(255)),'')
											WHEN 'HoraNegociacion'							THEN IFNULL(CAST (DJ."HoraNegociacion" AS NVARCHAR(255)),'')
											WHEN 'FechaValutaMonedaRecibida'				THEN IFNULL(CAST (DJ."FechaValutaMonedaRecibida" AS NVARCHAR(255)),'')
											WHEN 'FechaValutaMonedaEntregada'				THEN IFNULL(CAST (DJ."FechaValutaMonedaEntregada" AS NVARCHAR(255)),'')
											WHEN 'Identificador'							THEN IFNULL(CAST (DJ."Identificador" AS NVARCHAR(255)),'')
											WHEN 'RUTContraparte1'							THEN IFNULL(CAST (DJ."RUTContraparte1" AS NVARCHAR(255)),'')
											WHEN 'LEIContraparte1'							THEN IFNULL(CAST (DJ."LEIContraparte1" AS NVARCHAR(255)),'')											
											WHEN 'NombreContraparte1'						THEN IFNULL(CAST (DJ."NombreContraparte1" AS NVARCHAR(255)),'')
											WHEN 'RUTContraparte2'							THEN IFNULL(CAST (DJ."RUTContraparte2" AS NVARCHAR(255)),'')
											WHEN 'LEIContraparte2'							THEN IFNULL(CAST (DJ."LEIContraparte2" AS NVARCHAR(255)),'')
											WHEN 'NombreContraparte2'						THEN IFNULL(CAST (DJ."NombreContraparte2" AS NVARCHAR(255)),'')
											WHEN 'PaisContraparte2'							THEN IFNULL(CAST (DJ."PaisContraparte2" AS NVARCHAR(255)),'')	
											WHEN 'RUTIntermediario'							THEN IFNULL(CAST (DJ."RUTIntermediario" AS NVARCHAR(255)),'')
											WHEN 'PlataformaNegociacion'					THEN IFNULL(CAST (DJ."PlataformaNegociacion" AS NVARCHAR(255)),'')	
											WHEN 'CodigoOperacionCambio'					THEN IFNULL(CAST (DJ."CodigoOperacionCambio" AS NVARCHAR(255)),'')
											WHEN 'PaisMonedaExtranjera'						THEN IFNULL(CAST (DJ."PaisMonedaExtranjera" AS NVARCHAR(255)),'')
											WHEN 'MonedaCompradaRecibida'					THEN IFNULL(CAST (DJ."MonedaCompradaRecibida" AS NVARCHAR(255)),'')
											WHEN 'MontoMonedaCompradaRecibida'				THEN IFNULL(CAST (DJ."MontoMonedaCompradaRecibida" AS NVARCHAR(255)),'')
											WHEN 'MonedaVendidaEntregada'					THEN IFNULL(CAST (DJ."MonedaVendidaEntregada" AS NVARCHAR(255)),'')
											WHEN 'MontoMonedaVendidaEntregada'				THEN IFNULL(CAST (DJ."MontoMonedaVendidaEntregada" AS NVARCHAR(255)),'')																
											WHEN 'ConvencionTipoCambio'						THEN IFNULL(CAST (DJ."ConvencionTipoCambio" AS NVARCHAR(255)),'')	
											WHEN 'TipoCambio'								THEN IFNULL(CAST (DJ."TipoCambio" AS NVARCHAR(255)),'')
											WHEN 'AfectaDerivados'							THEN IFNULL(CAST (DJ."AfectaDerivados" AS NVARCHAR(255)),'')
											WHEN 'IdentificadorContratoDerivados'			THEN IFNULL(CAST (DJ."IdentificadorContratoDerivados" AS NVARCHAR(255)),'')											
											WHEN 'FechaSuscripcionContrato'					THEN IFNULL(CAST (DJ."FechaSuscripcionContrato" AS NVARCHAR(255)),'')
											WHEN 'HoraSuscripcionContrato'					THEN IFNULL(CAST (DJ."HoraSuscripcionContrato" AS NVARCHAR(255)),'')
											WHEN 'OperacionEmpresaRelacionada'				THEN IFNULL(CAST (DJ."OperacionEmpresaRelacionada" AS NVARCHAR(255)),'')
											WHEN 'NumeroInscripcionObligacionExterior'		THEN IFNULL(CAST (DJ."NumeroInscripcionObligacionExterior" AS NVARCHAR(255)),'')
											WHEN 'NombreFinancistaAporante'					THEN IFNULL(CAST (DJ."NombreFinancistaAporante" AS NVARCHAR(255)),'')
											WHEN 'LEIFinancistaAporante'					THEN IFNULL(CAST (DJ."LEIFinancistaAporante" AS NVARCHAR(255)),'')											
											WHEN 'NombreReceptorInversion'					THEN IFNULL(CAST (DJ."NombreReceptorInversion" AS NVARCHAR(255)),'')
											WHEN 'LEIReceptorInversion'						THEN IFNULL(CAST (DJ."LEIReceptorInversion" AS NVARCHAR(255)),'')
											WHEN 'ModalidadInversionCustodio'				THEN IFNULL(CAST (DJ."ModalidadInversionCustodio" AS NVARCHAR(255)),'')
											
											
											
					END) AS "ValorAntiguo",
					(CASE MOD."COLUMN_NAME" WHEN 'TipoRegistro'								THEN IFNULL(CAST (p_TipoRegistro AS NVARCHAR(255)),'')
											WHEN 'EventoReporte'							THEN IFNULL(CAST (p_EventoReporte AS NVARCHAR(255)),'')
											WHEN 'TipoOperacion'							THEN IFNULL(CAST (p_TipoOperacion AS NVARCHAR(255)),'')
											WHEN 'FechaNegociacion'							THEN IFNULL(CAST (p_FechaNegociacion AS NVARCHAR(255)),'')
											WHEN 'HoraNegociacion'							THEN IFNULL(CAST (p_HoraNegociacion AS NVARCHAR(255)),'')
											WHEN 'FechaValutaMonedaRecibida'				THEN IFNULL(CAST (p_FechaValutaMonedaRecibida AS NVARCHAR(255)),'')
											WHEN 'FechaValutaMonedaEntregada'				THEN IFNULL(CAST (p_FechaValutaMonedaEntregada AS NVARCHAR(255)),'')
											WHEN 'Identificador'							THEN IFNULL(CAST (p_Identificador AS NVARCHAR(255)),'')
											WHEN 'RUTContraparte1'							THEN IFNULL(CAST (p_RUTContraparte1 AS NVARCHAR(255)),'')
											WHEN 'LEIContraparte1'							THEN IFNULL(CAST (p_LEIContraparte1 AS NVARCHAR(255)),'')											
											WHEN 'NombreContraparte1'						THEN IFNULL(CAST (p_NombreContraparte1 AS NVARCHAR(255)),'')
											WHEN 'RUTContraparte2'							THEN IFNULL(CAST (p_RUTContraparte2 AS NVARCHAR(255)),'')
											WHEN 'LEIContraparte2'							THEN IFNULL(CAST (p_LEIContraparte2 AS NVARCHAR(255)),'')
											WHEN 'NombreContraparte2'						THEN IFNULL(CAST (p_NombreContraparte2 AS NVARCHAR(255)),'')
											WHEN 'PaisContraparte2'							THEN IFNULL(CAST (p_PaisContraparte2 AS NVARCHAR(255)),'')	
											WHEN 'RUTIntermediario'							THEN IFNULL(CAST (p_RUTIntermediario AS NVARCHAR(255)),'')
											WHEN 'PlataformaNegociacion'					THEN IFNULL(CAST (p_PlataformaNegociacion AS NVARCHAR(255)),'')	
											WHEN 'CodigoOperacionCambio'					THEN IFNULL(CAST (p_CodigoOperacionCambio AS NVARCHAR(255)), '')
											WHEN 'PaisMonedaExtranjera'						THEN IFNULL(CAST (p_PaisMonedaExtranjera AS NVARCHAR(255)),'')
											WHEN 'MonedaCompradaRecibida'					THEN IFNULL(CAST (p_MonedaCompradaRecibida AS NVARCHAR(255)),'')
											WHEN 'MontoMonedaCompradaRecibida'				THEN IFNULL(CAST (p_MontoMonedaCompradaRecibida AS NVARCHAR(255)),'')
											WHEN 'MonedaVendidaEntregada'					THEN IFNULL(CAST (p_MonedaVendidaEntregada AS NVARCHAR(255)),'')
											WHEN 'MontoMonedaVendidaEntregada'				THEN IFNULL(CAST (p_MontoMonedaVendidaEntregada AS NVARCHAR(255)),'')																
											WHEN 'ConvencionTipoCambio'						THEN IFNULL(CAST (p_MonedaCompradaRecibida || '/' || p_MonedaVendidaEntregada AS NVARCHAR(255)), '')	
											WHEN 'TipoCambio'								THEN IFNULL(CAST (p_TipoCambio AS NVARCHAR(255)),'')
											WHEN 'AfectaDerivados'							THEN IFNULL(CAST (p_AfectaDerivados AS NVARCHAR(255)),'')
											WHEN 'IdentificadorContratoDerivados'			THEN IFNULL(CAST (p_IdentificadorContratoDerivados AS NVARCHAR(255)),'')											
											WHEN 'FechaSuscripcionContrato'					THEN IFNULL(CAST (p_FechaSuscripcionContrato AS NVARCHAR(255)),'')
											WHEN 'HoraSuscripcionContrato'					THEN IFNULL(CAST (p_HoraSuscripcionContrato AS NVARCHAR(255)),'')
											WHEN 'OperacionEmpresaRelacionada'				THEN IFNULL(CAST (p_OperacionEmpresaRelacionada AS NVARCHAR(255)),'')
											WHEN 'NumeroInscripcionObligacionExterior'		THEN IFNULL(CAST (p_NumeroInscripcionObligacionExterior AS NVARCHAR(255)),'')
											WHEN 'NombreFinancistaAporante'					THEN IFNULL(CAST (p_NombreFinancistaAporante AS NVARCHAR(255)),'')
											WHEN 'LEIFinancistaAporante'					THEN IFNULL(CAST (p_LEIFinancistaAporante AS NVARCHAR(255)),'')											
											WHEN 'NombreReceptorInversion'					THEN IFNULL(CAST (p_NombreReceptorInversion AS NVARCHAR(255)),'')
											WHEN 'LEIReceptorInversion'						THEN IFNULL(CAST (p_LEIReceptorInversion AS NVARCHAR(255)),'')
											WHEN 'ModalidadInversionCustodio'				THEN IFNULL(CAST (p_ModalidadInversionCustodio AS NVARCHAR(255)),'')
					END) AS "ValorNuevo",	
					CURRENT_UTCTIMESTAMP AS "FechaSolicitud",
					CURRENT_UTCTIMESTAMP AS "FechaRevision",
					'PEN' AS "IdEstadoAutorizacion"
				FROM "BSCL.Normativa.BD::NOR.DatosREPO2P1" DJ
				INNER JOIN 	"BSCL.Normativa.BD::DOC.Documento" DOC ON (DOC."IdDocumento"= DJ."IdDocumento" AND DOC."Esquema"='REPO2P1')
				INNER JOIN  "BSCL.Normativa.BD::DOC.Campo" 	CAM ON (CAM."Esquema"=DOC."Esquema" AND CAM."Version"=DOC."Version" AND CAM."Editable"='S')
				INNER JOIN 	(SELECT COL."DATA_TYPE_NAME",
										COL."COLUMN_NAME"
								FROM SYS.TABLE_COLUMNS COL
								WHERE COL."TABLE_NAME"='BSCL.Normativa.BD::NOR.DatosREPO2P1' AND COL.SCHEMA_NAME='BSCL'
							) MOD ON (MOD."COLUMN_NAME"=CAM."Nombre")
				WHERE DJ."ID"=	:p_ID;				
	
			-- 1) DETERMINA SI LA MODIFICACION  REQUIERE DE UNA AUTORIZACION PREVIA
							
			------------------------------------------
			--Pregunta Si requiere autorizacion
			------------------------------------------
			call "BSCL.Plataforma.BD.Servicios::AUT.RequiereEventoAutorizacion" (
				:p_IdEmpresa,
				:p_IdUsuario,
				:p_IdModulo,
				:V_RegistroSolicitudCambio,
				:v_IdConcepto,
				:v_RequiereAutorizacion
			);
			
        
        
      	if v_RequiereAutorizacion='S' THEN
	
	

		------------------------------------------
		--Genera Evento de Autorizacion
		------------------------------------------
			DECLARE	v_Glosa				NVARCHAR(520);
			declare v_out_idAuth 		bigint;
			declare v_out_AuthAdvertenciasContador 	int;
			declare v_out_AuthErroresContador 		int;
			v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
			v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
			
			SELECT 'SICAM REPORTE 2.1 / FECHA : ' || LTRIM(RTRIM(PLA."Valor"))
			INTO v_Glosa
			FROM "BSCL.Plataforma.BD::PLA.Configuracion" PLA
			WHERE PLA."CodigoParametro" = 'PLA.FechaSistema';
			CALL  "BSCL.Plataforma.BD.Servicios::PLA.CrearRegistroSolicitudCambio"
			(
				:V_RegistroSolicitudCambio,
				v_out_IdSecuencias
				
			);
				
			CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
				p_IdEmpresa 				=> p_IdEmpresa,
				p_IdUsuario 				=> p_IdUsuario,
				p_IdModulo 					=> p_IdModulo,
				p_UsaReglasNegocio 			=> 'N',								
				p_IdTipoEvento 				=> v_IdConcepto,
				p_IdModuloSolicita 			=> p_IdModulo,
				p_IdUsuarioSolicita 		=> p_IdUsuario,
				p_Fecha 					=> TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'), 
				p_IdConcepto 				=> v_IdConcepto,
				p_NumeroOperacionConcepto 	=> CAST (v_secuencia_IdSolicitudCambio AS NVARCHAR(40)), --v_secuencia_IdSolicitudCambio,
				p_FechaOperacionConcepto 	=> TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'),
				p_IdFuncionario             => 0,
				p_IdCliente 				=> 0, 
				p_Glosa 					=> v_Glosa,
				--salida
				p_out_IdEvento 				=> v_out_idAuth,
				p_out_AdvertenciasContador 	=> v_out_AuthAdvertenciasContador,
				p_out_AdvertenciasDetalle 	=> v_out_AuthAdvertenciasDetalle,
				p_out_ErroresContador 		=> v_out_AuthErroresContador,
				p_out_ErroresDetalle 		=> v_out_AuthErroresDetalle);				

			
			 p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AuthAdvertenciasContador;	
  			 
			 p_out_AdvertenciasDetalle = Select "Codigo","RequiereAutorizacion", "Descripcion" 
                                          From :v_out_AuthAdvertenciasDetalle Union All 
                                          Select "Codigo","RequiereAutorizacion", "Descripcion"
                                          From :p_out_AdvertenciasDetalle;  
                                          
                                          
              p_out_ErroresContador := p_out_ErroresContador + v_out_AuthErroresContador;
                                         
              p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
                                     From :v_out_AuthErroresDetalle Union All 
                                     Select "Codigo", "Descripcion" 
                                     From :p_out_ErroresDetalle; 

						                 
                                     
			  if 	p_out_ErroresContador > 0 then
						SIGNAL salida_controlada;
						
					else
						--Mensaje de Salida
						p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;	
						v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
						CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('REQUIERE_AUTORIZACION',:v_hash,:v_erroroutmsg);
						p_out_AdvertenciasDetalle =  select 'REQUIERE_AUTORIZACION' as "Codigo",'S' as "RequiereAutorizacion",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
												     select * from :p_out_AdvertenciasDetalle;
						SIGNAL salida_controlada;
			  end if;								
			
				
                                         
				
		ELSE
			
	        UPDATE "BSCL.Normativa.BD::NOR.DatosREPO2P1" 
			SET
	            "IdDocumento"								= p_IdDocumento
				,"FuenteDatos"								= p_FuenteDatos
	            ,"Estado"									= p_Estado
				,"EstadoSICAM"								= p_EstadoSICAM
				,"SICAM"									= p_SICAM
			    ,"TipoRegistro"								= p_TipoRegistro
				,"EventoReporte"							= p_EventoReporte
				,"TipoOperacion"							= p_TipoOperacion
			    ,"FechaNegociacion"							= p_FechaNegociacion
			    ,"HoraNegociacion"							= p_HoraNegociacion
				,"FechaValutaMonedaRecibida"				= p_FechaValutaMonedaRecibida
			    ,"FechaValutaMonedaEntregada"				= p_FechaValutaMonedaEntregada
				,"Identificador"							= p_Identificador
			    ,"RUTContraparte2"							= p_RUTContraparte2
			    ,"LEIContraparte2"							= p_LEIContraparte2
				,"NombreContraparte2"						= p_NombreContraparte2
				,"PaisContraparte2"							= p_PaisContraparte2
				,"RUTIntermediario"							= p_RUTIntermediario
				,"PlataformaNegociacion"					= p_PlataformaNegociacion
				,"CodigoOperacionCambio"					= p_CodigoOperacionCambio
				,"PaisMonedaExtranjera"						= p_PaisMonedaExtranjera
				,"MonedaCompradaRecibida"					= p_MonedaCompradaRecibida
				,"MontoMonedaCompradaRecibida"				= p_MontoMonedaCompradaRecibida
				,"MonedaVendidaEntregada"					= p_MonedaVendidaEntregada
				,"MontoMonedaVendidaEntregada"				= p_MontoMonedaVendidaEntregada
				,"ConvencionTipoCambio"						= p_ConvencionTipoCambio
				,"TipoCambio"								= p_TipoCambio
				,"AfectaDerivados"							= p_AfectaDerivados
				,"IdentificadorContratoDerivados"			= p_IdentificadorContratoDerivados
				,"FechaSuscripcionContrato"					= p_FechaSuscripcionContrato
				,"HoraSuscripcionContrato"					= p_HoraSuscripcionContrato
				,"OperacionEmpresaRelacionada"				= p_OperacionEmpresaRelacionada
				,"NumeroInscripcionObligacionExterior"		= p_NumeroInscripcionObligacionExterior
				,"NombreFinancistaAporante"					= p_NombreFinancistaAporante
				,"LEIFinancistaAporante"					= p_LEIFinancistaAporante
				,"NombreReceptorInversion"					= p_NombreReceptorInversion
				,"LEIReceptorInversion"						= p_LEIReceptorInversion
				,"ModalidadInversionCustodio"				= p_ModalidadInversionCustodio
				,"IdCliente"								= p_IdCliente
				,"IdOperacion"								= p_IdOperacion
				,"IdTipoOperacion"							= p_IdTipoOperacion
				,"Nemo"										= TRIM(p_Nemo)
				,"IdMonedaPrincipal"						= p_IdMonedaPrincipal
				,"IdMonedaSecundaria"						= p_IdMonedaSecundaria
	        WHERE "ID" = p_ID;

			--LOG DE AUDITORIA (Inicio)
			V_CambiosModificados = SELECT * FROM :V_RegistroSolicitudCambio WHERE "ValorAntiguo" <> "ValorNuevo";
				
				BEGIN
					DECLARE p_IdLog BIGINT;
					DECLARE v_fechaHoraSistema NVARCHAR(40);

					SELECT "BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL INTO p_IdLog FROM dummy;

					call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
						p_IdEmpresa => p_IdEmpresa,
						p_out_FechaHoraSistema => v_fechaHoraSistema
					);

					v_log = SELECT :p_IdLog AS "IdCabeceraLog"
									,'MODIFICACION' AS "IdTipoLog"
									,:p_IdModulo AS "IdModulo"
									,:p_IdUsuario AS "IdUsuario"
									,:p_IdEmpresa AS "IdEmpresa"
									,TO_VARCHAR (COALESCE(:v_fechaHoraSistema, '0001-01-01'), 'YYYY-MM-DD') as "FechaSistemaLog"
									,:p_IdUsuario AS "IdUsuarioAtendido"
									,0  AS "IdPersona"
									,TO_VARCHAR(COALESCE(:p_IdCliente, 0))  AS "IdCliente"
									,'' AS "EstadoCliente"
									,'' AS "NombreCliente"
									,'' AS "IdTipoDocumentoIdentidad"
									,'' AS "IdDocumentoIdentidad"
									,0  AS "IdPortafolio"
									,'' AS "EstadoPortafolio"
									,'MODIFICAR_REPO2P1' AS "IdTipoRegistro"
									,'' AS "IdRegistro"
									,'' AS "EstadoRegistro"
									,'' AS "IdTipoOperacion"
									,0 AS "Folio"
									,0 AS "Cantidad"
									,0 AS "PrecioTasa"
									,0 AS "Monto"
									,'' AS "IdMonedaOrigen"
									,'' AS "IdMonedaLiquidacion"
									,'' AS "TipoCambio"
									,'' AS "Nemo"
									,0  AS "IdProceso" 
									,'' AS "EstadoProceso"
									,'ModificarRepo2P1' AS "NombreProceso"
							FROM DUMMY;

					v_LogDetalle =
							SELECT
								:p_IdLog as "IdCabeceraLog"
								,'Identificador: ' || p_Identificador as "SubRegistro"
								,COALESCE(logDetalle1."CampoModificado",'CAMPO') as "CampoRegistro"
								,COALESCE(logDetalle1."ValorAntiguo",'') as "ValorAntiguo"
								,COALESCE(logDetalle1."ValorNuevo", '') as "ValorNuevo"
							FROM :V_CambiosModificados AS logDetalle1;

					CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_LogDetalle);
				END;
			--LOG DE AUDITORIA (Fin)
        
       END IF;        
        
    ELSE
        SELECT IFNULL(MAX("ID"),0)+1 INTO p_out_id FROM "BSCL"."BSCL.Normativa.BD::NOR.DatosREPO2P1";
        INSERT INTO "BSCL.Normativa.BD::NOR.DatosREPO2P1"(
            	"ID"
				,"IdDocumento"								
				,"FuenteDatos"								
	            ,"Estado"									
				,"EstadoSICAM"								
				,"SICAM"														
			    ,"TipoRegistro"								
				,"EventoReporte"						
				,"TipoOperacion"							
			    ,"FechaNegociacion"							
			    ,"HoraNegociacion"						
				,"FechaValutaMonedaRecibida"				
			    ,"FechaValutaMonedaEntregada"				
				,"Identificador"							
			    ,"RUTContraparte1"							
			    ,"LEIContraparte1"							
				,"NombreContraparte1"						
			    ,"RUTContraparte2"							
			    ,"LEIContraparte2"							
				,"NombreContraparte2"						
				,"PaisContraparte2"							
				,"RUTIntermediario"							
				,"PlataformaNegociacion"					
				,"CodigoOperacionCambio"					
				,"PaisMonedaExtranjera"						
				,"MonedaCompradaRecibida"					
				,"MontoMonedaCompradaRecibida"				
				,"MonedaVendidaEntregada"				
				,"MontoMonedaVendidaEntregada"		
				,"ConvencionTipoCambio"					
				,"TipoCambio"						
				,"AfectaDerivados"						
				,"IdentificadorContratoDerivados"		
				,"FechaSuscripcionContrato"					
				,"HoraSuscripcionContrato"					
				,"OperacionEmpresaRelacionada"				
				,"NumeroInscripcionObligacionExterior"		
				,"NombreFinancistaAporante"					
				,"LEIFinancistaAporante"					
				,"NombreReceptorInversion"					
				,"LEIReceptorInversion"						
				,"ModalidadInversionCustodio"				
				,"IdCliente"								
				,"IdOperacion"								
				,"IdTipoOperacion"							
				,"Nemo"	
				,"IdMonedaPrincipal"
				,"IdMonedaSecundaria"
				,"FolioOperacion"
				,"IdCartera"
				,"FechaLiquidacion"
			
        ) VALUES (
            	p_out_id
            	,p_IdDocumento
				,COALESCE(p_FuenteDatos,'MANTENEDOR')
				,COALESCE(p_Estado,'ACT')
				,COALESCE(p_EstadoSICAM,'')
				,COALESCE(p_SICAM,'')            
			    ,p_TipoRegistro
				,p_EventoReporte
				,p_TipoOperacion
			    ,p_FechaNegociacion
			    ,p_HoraNegociacion
				,p_FechaValutaMonedaRecibida
			    ,p_FechaValutaMonedaEntregada
				,p_Identificador
			    ,v_RUTContraparte1
			    ,v_LEIContraparte1
				,v_NombreContraparte1
			    ,p_RUTContraparte2
			    ,p_LEIContraparte2
				,p_NombreContraparte2
				,p_PaisContraparte2
				,p_RUTIntermediario
				,p_PlataformaNegociacion
				,p_CodigoOperacionCambio
				,p_PaisMonedaExtranjera
				,p_MonedaCompradaRecibida
				,p_MontoMonedaCompradaRecibida
				,p_MonedaVendidaEntregada
				,p_MontoMonedaVendidaEntregada
				,CASE 
					WHEN p_TipoOperacion IN ('01','02') 
						THEN COALESCE(p_ConvencionTipoCambio, p_MonedaCompradaRecibida || '/' || p_MonedaVendidaEntregada)
					ELSE ''
				END
				,p_TipoCambio
				,p_AfectaDerivados
				,p_IdentificadorContratoDerivados
				,p_FechaSuscripcionContrato
				,p_HoraSuscripcionContrato
				,p_OperacionEmpresaRelacionada
				,p_NumeroInscripcionObligacionExterior
				,p_NombreFinancistaAporante
				,p_LEIFinancistaAporante
				,p_NombreReceptorInversion
				,p_LEIReceptorInversion
				,p_ModalidadInversionCustodio
				,p_IdCliente
				,p_IdOperacion
				,COALESCE(p_IdTipoOperacion,'')
				,COALESCE(TRIM(p_Nemo),'')
				,COALESCE(p_IdMonedaPrincipal,'')
				,COALESCE(p_IdMonedaSecundaria,'')
				,COALESCE(p_FolioOperacion, 0)
				,COALESCE(p_IdCartera, 0)
				,COALESCE(p_FechaLiquidacion, DATE'0001-01-01')		
        );
		--LOG DE AUDITORIA (Inicio)
			V_CambiosModificados = SELECT * FROM :V_RegistroSolicitudCambio WHERE "ValorAntiguo" <> "ValorNuevo";
				
				BEGIN
					DECLARE p_IdLog BIGINT;
					DECLARE v_fechaHoraSistema NVARCHAR(40);

					SELECT "BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL INTO p_IdLog FROM dummy;

					call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
						p_IdEmpresa => p_IdEmpresa,
						p_out_FechaHoraSistema => v_fechaHoraSistema
					);

					v_log = SELECT :p_IdLog AS "IdCabeceraLog"
									,'INGRESO' AS "IdTipoLog"
									,:p_IdModulo AS "IdModulo"
									,:p_IdUsuario AS "IdUsuario"
									,:p_IdEmpresa AS "IdEmpresa"
									,TO_VARCHAR (COALESCE(:v_fechaHoraSistema, '0001-01-01'), 'YYYY-MM-DD') as "FechaSistemaLog"
									,:p_IdUsuario AS "IdUsuarioAtendido"
									,0  AS "IdPersona"
									,TO_VARCHAR(COALESCE(:p_IdCliente, 0))  AS "IdCliente"
									,'' AS "EstadoCliente"
									,'' AS "NombreCliente"
									,'' AS "IdTipoDocumentoIdentidad"
									,'' AS "IdDocumentoIdentidad"
									,0  AS "IdPortafolio"
									,'' AS "EstadoPortafolio"
									,'CREAR_REPO2P1' AS "IdTipoRegistro"
									,'' AS "IdRegistro"
									,'' AS "EstadoRegistro"
									,'' AS "IdTipoOperacion"
									,0 AS "Folio"
									,0 AS "Cantidad"
									,0 AS "PrecioTasa"
									,0 AS "Monto"
									,'' AS "IdMonedaOrigen"
									,'' AS "IdMonedaLiquidacion"
									,'' AS "TipoCambio"
									,'' AS "Nemo"
									,0  AS "IdProceso" 
									,'' AS "EstadoProceso"
									,'CrearRepo2P1' AS "NombreProceso"
							FROM DUMMY;

					v_LogDetalle =
							SELECT
								:p_IdLog as "IdCabeceraLog"
								,'NUEVO' as "SubRegistro"
								,'Identificador' as "CampoRegistro"
								,'' as "ValorAntiguo"
								,COALESCE(p_Identificador, '') as "ValorNuevo"
							FROM DUMMY;

					CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_LogDetalle);
				END;
			--LOG DE AUDITORIA (Fin)
    END IF;
END;
