SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1890.GrabarDetalle";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1890.GrabarDetalle"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    IN p_IdEmpresa 											BIGINT		 ,
    -- ID usuario del login.
    IN p_IdUsuario 											BIGINT		 ,
    -- ID modulo invocador.
    IN p_IdModulo 											NVARCHAR(3)	 ,
    ------------ Parametros de Entrada Propios ----------
    -- ID del detalle.
    -- Si el id es mayor que cero, se asume que es un registro existente para actualizar.
    -- En caso contrario, se intenta crear un nuevo registro.
    IN p_ID 												BIGINT		 ,
    -- ID del documento.
    IN p_IdDocumento 										BIGINT		 ,
    -- VoBo del usuario sobre el registro (S/N).
    IN p_VboUsuario 										CHAR(1)		 ,
    -- Observacion ingresada por usuario que edito el registro.
    IN p_Observaciones 										NVARCHAR(250),
    -- Fuente de los datos.
    IN p_FuenteDatos 										NVARCHAR(40) ,
    IN p_IdCliente 											BIGINT		 ,
    IN p_IdPortafolio 										BIGINT		 ,
	IN p_Numero												BIGINT       ,
	IN p_RutInversionista									NVARCHAR(40) ,
	IN p_InteresesRealesDepositosPositivo					DECIMAL(26,8),
	IN p_InteresesRealesDepositosNegativo					DECIMAL(26,8),
	IN p_InteresesRealesMantencionPositivo					DECIMAL(26,8),
	IN p_InteresesRealesMantencionNegativo					DECIMAL(26,8),
	IN p_InteresesRealesOperacionesCaptacionPositivo		DECIMAL(26,8),
	IN p_InteresesRealesOperacionesCaptacionNegativo		DECIMAL(26,8),
	IN p_InteresesMonedaNacionalExtranjeraNominal			DECIMAL(26,8),
	IN p_ImpuestoAdicionalMondaNacionalExtranjeraNominal	DECIMAL(26,8),
	IN p_InteresesRealesDepositosPositivoActualizado		DECIMAL(26,8),
	IN p_InteresesRealesDepositosNegativoActualizado		DECIMAL(26,8),
	IN p_InteresesRealesMantencionPositivoActualizado		DECIMAL(26,8),
	IN p_InteresesRealesMantencionNegativoActualizado			DECIMAL(26,8),
	IN p_InteresesRealesOperacionesCaptacionPositivoActualizado	DECIMAL(26,8),
	IN p_InteresesRealesOperacionesCaptacionNegativoActualizado	DECIMAL(26,8),
	IN p_FechaVencimiento									DATE,
	IN p_FechaOperacion										DATE,
	IN p_FolioOperacion										BIGINT,
	IN p_MontoOperacionCaptacion							DECIMAL(26,8),
	IN p_MontoPercibidoFechaVencimiento						DECIMAL(26,8),
	IN p_InteresesRentasPagadas								DECIMAL(26,8),
	IN p_InteresesRealesPositivosNegativos					DECIMAL(26,8),
	IN p_FactorActualizacion  							    DECIMAL(26,8),
	IN p_NumeroCertificado									NVARCHAR(100),
    IN p_TipoDocumento 										NVARCHAR(10) ,
    IN p_NumeroDocumento 									BIGINT		 ,
    IN p_FechaDocumento 									DATE		 ,
    IN p_IdBaseDocumento 									BIGINT		 ,
    IN p_TipoTransaccion 									NVARCHAR(6)	 ,
    IN p_IdTipoOperacion 									NVARCHAR(6)	 ,
    IN p_IdTipoProducto 									NVARCHAR(4)	 ,


    ------------ Parametros de Salida Propios ----------
    -- ID del registro.
    OUT p_out_id 											BIGINT		 ,

    ------------ Salidas Advertencias ----------

    -- Contador de advertencias.
    OUT p_out_AdvertenciasContador 							INT			 ,

    -- Tabla de advertencias.
    OUT p_out_AdvertenciasDetalle TABLE("Codigo" NVARCHAR(40), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)),

    ------------ Salidas Errores ----------

    -- Contador de errores
    -- Importante: Debe ser cero para que se haga commit.
    OUT p_out_ErroresContador 							INT,

    -- Tabla de errores.
    OUT p_out_ErroresDetalle TABLE("Codigo" NVARCHAR(40), "Descripcion" NVARCHAR(5000))
)
    LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
    ------------ Variables Propias ----------

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
    p_out_AdvertenciasDetalle = SELECT '' AS "Codigo", '' AS "RequiereAutorizacion", '' AS "Descripcion" FROM dummy WHERE 1 = 2;
    p_out_ErroresDetalle = SELECT '' AS "Codigo", '' AS "Descripcion" FROM dummy WHERE 1 = 2;

    ------------ Logica de Actualizacion ----------

    IF p_ID > 0 THEN
        p_out_id := p_ID;

        UPDATE "BSCL.Normativa.BD::TRI.DatosDJ1890" SET
            "IdDocumento" 										= 	p_IdDocumento
			,"VboUsuario" 										= 	p_VboUsuario
			,"Observaciones" 									= 	p_Observaciones
			,"FuenteDatos" 										= 	p_FuenteDatos
			,"Numero"											=	p_Numero
			,"RutInversionista"									=	p_RutInversionista
			,"InteresesRealesDepositosPositivo"					=	p_InteresesRealesDepositosPositivo
			,"InteresesRealesDepositosNegativo"					=	p_InteresesRealesDepositosNegativo
			,"InteresesRealesMantencionPositivo"				=	p_InteresesRealesMantencionPositivo
			,"InteresesRealesMantencionNegativo"				=	p_InteresesRealesMantencionNegativo
			,"InteresesRealesOperacionesCaptacionPositivo"		=	p_InteresesRealesOperacionesCaptacionPositivo
			,"InteresesRealesOperacionesCaptacionNegativo"		=	p_InteresesRealesOperacionesCaptacionNegativo
			,"InteresesMonedaNacionalExtranjeraNominal"			=	p_InteresesMonedaNacionalExtranjeraNominal
			,"ImpuestoAdicionalMondaNacionalExtranjeraNominal"	=	p_ImpuestoAdicionalMondaNacionalExtranjeraNominal
			,"InteresesRealesDepositosPositivoActualizado"		=   p_InteresesRealesDepositosPositivoActualizado
			,"InteresesRealesDepositosNegativoActualizado"		=   p_InteresesRealesDepositosNegativoActualizado
			,"InteresesRealesMantencionPositivoActualizado"		=   p_InteresesRealesMantencionPositivoActualizado
			,"InteresesRealesMantencionNegativoActualizado"		=   p_InteresesRealesMantencionNegativoActualizado
			,"InteresesRealesOperacionesCaptacionPositivoActualizado"	=   p_InteresesRealesOperacionesCaptacionPositivoActualizado
			,"InteresesRealesOperacionesCaptacionNegativoActualizado"	=   p_InteresesRealesOperacionesCaptacionNegativoActualizado
			,"FechaVencimiento"									=   p_FechaVencimiento
			,"FechaOperacion"                                   =   p_FechaOperacion
			,"FolioOperacion"								    =   p_FolioOperacion
			,"MontoOperacionCaptacion"							=   p_MontoOperacionCaptacion
			,"MontoPercibidoFechaVencimiento"					=   p_MontoPercibidoFechaVencimiento
			,"InteresesRentasPagadas"							=   p_InteresesRentasPagadas
			,"InteresesRealesPositivosNegativos"				=   p_InteresesRealesPositivosNegativos
			,"FactorActualizacion"  							=   p_FactorActualizacion
			,"NumeroCertificado"								=	p_NumeroCertificado
			,"TipoDocumento" 									= 	p_TipoDocumento
			,"NumeroDocumento" 									= 	p_NumeroDocumento
			,"FechaDocumento" 									= 	p_FechaDocumento
			,"IdBaseDocumento" 									= 	p_IdBaseDocumento
			,"IdCliente" 										= 	p_IdCliente
			,"IdPortafolio" 									= 	p_IdPortafolio
			,"TipoTransaccion" 									= 	p_TipoTransaccion
			,"IdTipoOperacion" 									= 	p_IdTipoOperacion
			,"IdTipoProducto" 									= 	p_IdTipoProducto
        WHERE "ID" = p_ID;
    ELSE
        SELECT IFNULL(MAX("ID"),0)+1 INTO p_out_id FROM "BSCL.Normativa.BD::TRI.DatosDJ1890";

        insert into "BSCL.Normativa.BD::TRI.DatosDJ1890"(
            "ID"
			,"IdDocumento"
			,"VboUsuario" 
			,"Observaciones"
			,"FuenteDatos"						
			,"Numero"											
			,"RutInversionista"									
			,"InteresesRealesDepositosPositivo"					
			,"InteresesRealesDepositosNegativo"					
			,"InteresesRealesMantencionPositivo"				
			,"InteresesRealesMantencionNegativo"				
			,"InteresesRealesOperacionesCaptacionPositivo"		
			,"InteresesRealesOperacionesCaptacionNegativo"		
			,"InteresesMonedaNacionalExtranjeraNominal"			
			,"ImpuestoAdicionalMondaNacionalExtranjeraNominal"	
			,"InteresesRealesDepositosPositivoActualizado"
			,"InteresesRealesDepositosNegativoActualizado"
			,"InteresesRealesMantencionPositivoActualizado"
			,"InteresesRealesMantencionNegativoActualizado"
			,"InteresesRealesOperacionesCaptacionPositivoActualizado"
			,"InteresesRealesOperacionesCaptacionNegativoActualizado"
			,"FechaVencimiento"
			,"FechaOperacion"
			,"FolioOperacion"
			,"MontoOperacionCaptacion"
			,"MontoPercibidoFechaVencimiento"
			,"InteresesRentasPagadas"
			,"InteresesRealesPositivosNegativos"
			,"FactorActualizacion"
			,"NumeroCertificado"																												       
			,"TipoDocumento" 
			,"NumeroDocumento" 
			,"FechaDocumento" 
			,"IdBaseDocumento" 
			,"IdCliente" 
			,"IdPortafolio" 
			,"TipoTransaccion" 
			,"IdTipoOperacion" 
			,"IdTipoProducto"
        ) values (
            p_out_id
            ,p_IdDocumento
			,'N'
            ,''
            ,'MANTENEDOR'
			,p_Numero
			,p_RutInversionista
			,p_InteresesRealesDepositosPositivo
			,p_InteresesRealesDepositosNegativo
			,p_InteresesRealesMantencionPositivo
			,p_InteresesRealesMantencionNegativo
			,p_InteresesRealesOperacionesCaptacionPositivo
			,p_InteresesRealesOperacionesCaptacionNegativo
			,p_InteresesMonedaNacionalExtranjeraNominal
			,p_ImpuestoAdicionalMondaNacionalExtranjeraNominal
			,p_InteresesRealesDepositosPositivoActualizado
			,p_InteresesRealesDepositosNegativoActualizado
			,p_InteresesRealesMantencionPositivoActualizado
			,p_InteresesRealesMantencionNegativoActualizado
			,p_InteresesRealesOperacionesCaptacionPositivoActualizado
			,p_InteresesRealesOperacionesCaptacionNegativoActualizado
			,p_FechaVencimiento
			,p_FechaOperacion
			,p_FolioOperacion
			,p_MontoOperacionCaptacion
			,p_MontoPercibidoFechaVencimiento
			,p_InteresesRentasPagadas
			,p_InteresesRealesPositivosNegativos
			,p_FactorActualizacion
			,p_NumeroCertificado
			,p_TipoDocumento
			,p_NumeroDocumento
			,p_FechaDocumento
			,p_IdBaseDocumento
			,p_IdCliente
			,p_IdPortafolio
			,p_TipoTransaccion
			,p_IdTipoOperacion
			,p_IdTipoProducto
        );
    END IF;
END;