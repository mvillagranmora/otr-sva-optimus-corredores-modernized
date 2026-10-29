SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1848.GrabarDetalle";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1848.GrabarDetalle"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    IN p_IdEmpresa 												BIGINT		 ,
    -- ID usuario del login.
    IN p_IdUsuario 												BIGINT		 ,
    -- ID modulo invocador.
    IN p_IdModulo 												NVARCHAR(3)	 ,
    ------------ Parametros de Entrada Propios ----------
    -- ID del detalle.
    -- Si el id es mayor que cero, se asume que es un registro existente para actualizar.
    -- En caso contrario, se intenta crear un nuevo registro.
    IN p_ID 													BIGINT		 ,
    -- ID del documento.
    IN p_IdDocumento 											BIGINT		 ,
    -- VoBo del usuario sobre el registro (S/N).
    IN p_VboUsuario 											CHAR(1)		 ,
    -- Observacion ingresada por usuario que edito el registro.
    IN p_Observaciones 											NVARCHAR(250),
    -- Fuente de los datos.
    IN p_FuenteDatos 											NVARCHAR(40) ,
    IN p_IdCliente 												BIGINT		 ,
    IN p_IdPortafolio 											BIGINT		 ,
   	IN p_Numero													BIGINT       ,
	IN p_InversionistaExtranjeroRut								NVARCHAR(40) ,
	IN p_InversionistaExtranjeroModalidadActuacion				NVARCHAR(100),
	IN p_InversionistaPaisResidencia                            NVARCHAR(10) ,
	IN p_TransaccionesTipoOperacionInversionista				NVARCHAR(100),
	IN p_TransaccionesTipoInstrumentoContrato					NVARCHAR(100),
	IN p_TransaccionesFechaOperacionSituacionInversionista		DATE         ,
	IN p_TransaccionesMontoOperacionPesos						DECIMAL(26,8),
	IN p_TransaccionesMonedaOriginal							NVARCHAR(10) ,
	IN p_TransaccionesMontoOperacionMonedaOriginal				DECIMAL(26,8),
	IN p_TransaccionesGastos									DECIMAL(26,8),
	IN p_TransaccionesFormaDeclaracion                          BIGINT,
	IN p_CumplimientoTipoInversionista							NVARCHAR(100),
	IN p_CumplimientoFechaContrato								DATE         ,
	IN p_CumplimientoTipoInstrumento                            BIGINT       ,
	IN p_CumplimientoFechaInstrumento                           DATE         ,
	IN p_CumplimientoFolioDJ1848                                BIGINT       ,
	IN p_CumplimientoSaldoInversion                             DECIMAL(26,8),
	IN p_CumplimientoReorganizacion                             DECIMAL(26,8),
	IN p_RetencionesRutResponsaleImpuesto						NVARCHAR(40) ,
	IN p_RetencionesBaseImponible								DECIMAL(26,8),
	IN p_RetencionesTasaImpuesto								DECIMAL(26,8),
	IN p_RetencionesCreditoImpuesto								DECIMAL(26,8),
	IN p_RetencionesImpuesto									DECIMAL(26,8),
	IN p_RetencionesFolioF50									NVARCHAR(100),
	IN p_RetencionesPeriodoTributarioF50                        BIGINT,
	IN p_BeneficiosNormaBeneficioTributario						NVARCHAR(100),
	IN p_BeneficiosReferenciaLegalOtrosBeneficiosTributarios	NVARCHAR(100),
	IN p_BeneficiosMontoExento									DECIMAL(26,8),
    IN p_TipoDocumento 											NVARCHAR(10) ,
    IN p_NumeroDocumento 										BIGINT		 ,
    IN p_FechaDocumento 										DATE		 ,
    IN p_IdBaseDocumento 										BIGINT		 ,
    IN p_TipoTransaccion 										NVARCHAR(6)	 ,
    IN p_IdTipoOperacion 										NVARCHAR(6)	 ,
    IN p_IdTipoProducto 										NVARCHAR(4)	 ,
    IN p_FechaOperacion                                         DATE         ,
	IN p_IdOperacion                                            BIGINT       ,
    ------------ Parametros de Salida Propios ----------
    -- ID del registro.
    OUT p_out_id 												BIGINT		 ,

    ------------ Salidas Advertencias ----------

    -- Contador de advertencias.
    OUT p_out_AdvertenciasContador 								INT			 ,

    -- Tabla de advertencias.
    OUT p_out_AdvertenciasDetalle TABLE("Codigo" NVARCHAR(40), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)),

    ------------ Salidas Errores ----------

    -- Contador de errores
    -- Importante: Debe ser cero para que se haga commit.
    OUT p_out_ErroresContador INT,

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

        UPDATE "BSCL.Normativa.BD::TRI.DatosDJ1848" SET
            "IdDocumento" 												= 	p_IdDocumento
			,"VboUsuario" 												= 	p_VboUsuario
			,"Observaciones" 											= 	p_Observaciones
			,"FuenteDatos" 												= 	p_FuenteDatos
			,"Numero"													=	p_Numero
			,"InversionistaExtranjeroRut"								=	p_InversionistaExtranjeroRut
			,"InversionistaExtranjeroModalidadActuacion"				=	p_InversionistaExtranjeroModalidadActuacion
			,"InversionistaPaisResidencia"								=   p_InversionistaPaisResidencia
			,"TransaccionesTipoOperacionInversionista"					=	p_TransaccionesTipoOperacionInversionista
			,"TransaccionesTipoInstrumentoContrato"						=	p_TransaccionesTipoInstrumentoContrato
			,"TransaccionesFechaOperacionSituacionInversionista"		=	p_TransaccionesFechaOperacionSituacionInversionista
			,"TransaccionesMontoOperacionPesos"							=	p_TransaccionesMontoOperacionPesos
			,"TransaccionesMonedaOriginal"								=	p_TransaccionesMonedaOriginal
			,"TransaccionesMontoOperacionMonedaOriginal"				=	p_TransaccionesMontoOperacionMonedaOriginal
			,"TransaccionesGastos"										=	p_TransaccionesGastos
			,"TransaccionesFormaDeclaracion"                            =   p_TransaccionesFormaDeclaracion
			,"CumplimientoTipoInversionista"							=	p_CumplimientoTipoInversionista
			,"CumplimientoFechaContrato"								=	p_CumplimientoFechaContrato
			,"CumplimientoTipoInstrumento"                              =   p_CumplimientoTipoInstrumento
			,"CumplimientoFechaInstrumento"                             =   p_CumplimientoFechaInstrumento
			,"CumplimientoFolioDJ1848"                                  =   p_CumplimientoFolioDJ1848
			,"CumplimientoSaldoInversion"                               =   p_CumplimientoSaldoInversion
			,"CumplimientoReorganizacion"                               =   p_CumplimientoReorganizacion
			,"RetencionesRutResponsaleImpuesto"							=	p_RetencionesRutResponsaleImpuesto
			,"RetencionesBaseImponible"									=	p_RetencionesBaseImponible
			,"RetencionesTasaImpuesto"									=	p_RetencionesTasaImpuesto
			,"RetencionesCreditoImpuesto"								=	p_RetencionesCreditoImpuesto
			,"RetencionesImpuesto"										=	p_RetencionesImpuesto
			,"RetencionesFolioF50"										=	p_RetencionesFolioF50
			,"RetencionesPeriodoTributarioF50"                          =   p_RetencionesPeriodoTributarioF50
			,"BeneficiosNormaBeneficioTributario"						=	p_BeneficiosNormaBeneficioTributario
			,"BeneficiosReferenciaLegalOtrosBeneficiosTributarios"		=	p_BeneficiosReferenciaLegalOtrosBeneficiosTributarios
			,"BeneficiosMontoExento"									=	p_BeneficiosMontoExento
			,"TipoDocumento" 											= 	p_TipoDocumento
			,"NumeroDocumento" 											= 	p_NumeroDocumento
			,"FechaDocumento" 											= 	p_FechaDocumento
			,"IdBaseDocumento" 											= 	p_IdBaseDocumento
			,"IdCliente" 												= 	p_IdCliente
			,"IdPortafolio" 											= 	p_IdPortafolio
			,"TipoTransaccion" 											= 	p_TipoTransaccion
			,"IdTipoOperacion" 											= 	p_IdTipoOperacion
			,"IdTipoProducto" 											= 	p_IdTipoProducto
			,"FechaOperacion" 											=   p_FechaOperacion
			,"IdOperacion" 												=   p_IdOperacion
        WHERE "ID" = p_ID;
    ELSE
        SELECT IFNULL(MAX("ID"),0)+1 INTO p_out_id FROM "BSCL.Normativa.BD::TRI.DatosDJ1848";

        insert into "BSCL.Normativa.BD::TRI.DatosDJ1848"(
            "ID"
			,"IdDocumento"
			,"VboUsuario" 
			,"Observaciones"
			,"FuenteDatos"
			,"Numero"												
			,"InversionistaExtranjeroRut"							
			,"InversionistaExtranjeroModalidadActuacion"	
			,"InversionistaPaisResidencia"		
			,"TransaccionesTipoOperacionInversionista"				
			,"TransaccionesTipoInstrumentoContrato"					
			,"TransaccionesFechaOperacionSituacionInversionista"	
			,"TransaccionesMontoOperacionPesos"						
			,"TransaccionesMonedaOriginal"							
			,"TransaccionesMontoOperacionMonedaOriginal"			
			,"TransaccionesGastos"		
			,"TransaccionesFormaDeclaracion"							
			,"CumplimientoTipoInversionista"						
			,"CumplimientoFechaContrato"		
			,"CumplimientoTipoInstrumento"
			,"CumplimientoFechaInstrumento"		
			,"CumplimientoFolioDJ1848"		
			,"CumplimientoSaldoInversion"	
			,"CumplimientoReorganizacion"	
			,"RetencionesRutResponsaleImpuesto"						
			,"RetencionesBaseImponible"								
			,"RetencionesTasaImpuesto"								
			,"RetencionesCreditoImpuesto"							
			,"RetencionesImpuesto"									
			,"RetencionesFolioF50"
			,"RetencionesPeriodoTributarioF50"									
			,"BeneficiosNormaBeneficioTributario"					
			,"BeneficiosReferenciaLegalOtrosBeneficiosTributarios"	
			,"BeneficiosMontoExento"								       
			,"TipoDocumento" 
			,"NumeroDocumento" 
			,"FechaDocumento" 
			,"IdBaseDocumento" 
			,"IdCliente" 
			,"IdPortafolio" 
			,"TipoTransaccion" 
			,"IdTipoOperacion" 
			,"IdTipoProducto"
			,"FechaOperacion"
			,"IdOperacion"
        ) values (
            p_out_id
            ,p_IdDocumento
			,'N'
            ,''
            ,'MANTENEDOR'
			,p_Numero
			,p_InversionistaExtranjeroRut
			,p_InversionistaExtranjeroModalidadActuacion
			,p_InversionistaPaisResidencia
			,p_TransaccionesTipoOperacionInversionista
			,p_TransaccionesTipoInstrumentoContrato
			,p_TransaccionesFechaOperacionSituacionInversionista
			,p_TransaccionesMontoOperacionPesos
			,p_TransaccionesMonedaOriginal
			,p_TransaccionesMontoOperacionMonedaOriginal
			,p_TransaccionesGastos
			,p_TransaccionesFormaDeclaracion
			,p_CumplimientoTipoInversionista
			,p_CumplimientoFechaContrato
			,p_CumplimientoTipoInstrumento
			,p_CumplimientoFechaInstrumento
			,p_CumplimientoFolioDJ1848
			,p_CumplimientoSaldoInversion
			,p_CumplimientoReorganizacion
			,p_RetencionesRutResponsaleImpuesto
			,p_RetencionesBaseImponible
			,p_RetencionesTasaImpuesto
			,p_RetencionesCreditoImpuesto
			,p_RetencionesImpuesto
			,p_RetencionesFolioF50
			,p_RetencionesPeriodoTributarioF50
			,p_BeneficiosNormaBeneficioTributario
			,p_BeneficiosReferenciaLegalOtrosBeneficiosTributarios
			,p_BeneficiosMontoExento
			,p_TipoDocumento
			,p_NumeroDocumento
			,p_FechaDocumento
			,p_IdBaseDocumento
			,p_IdCliente
			,p_IdPortafolio
			,p_TipoTransaccion
			,p_IdTipoOperacion
			,p_IdTipoProducto
			,p_FechaOperacion
			,p_IdOperacion
        );
    END IF;
END;