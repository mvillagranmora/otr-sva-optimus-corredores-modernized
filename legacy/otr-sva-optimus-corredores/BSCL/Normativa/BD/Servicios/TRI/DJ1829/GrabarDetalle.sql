SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1829.GrabarDetalle";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1829.GrabarDetalle"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    IN p_IdEmpresa 									BIGINT		 ,
    -- ID usuario del login.
    IN p_IdUsuario 									BIGINT		 ,
    -- ID modulo invocador.
    IN p_IdModulo 									NVARCHAR(3)	 ,
    ------------ Parametros de Entrada Propios ----------
    -- ID del detalle.
    -- Si el id es mayor que cero, se asume que es un registro existente para actualizar.
    -- En caso contrario, se intenta crear un nuevo registro.
    IN p_ID 										BIGINT		 ,
    -- ID del documento.
    IN p_IdDocumento 								BIGINT		 ,
    -- VoBo del usuario sobre el registro (S/N).
    IN p_VboUsuario 								CHAR(1)		 ,
    -- Observacion ingresada por usuario que edito el registro.
    IN p_Observaciones 								NVARCHAR(250),
    -- Fuente de los datos.
    IN p_FuenteDatos 								NVARCHAR(40) ,
    --IN p_IdCliente 									BIGINT		 ,
    --IN p_IdPortafolio 								BIGINT		 ,
   	IN p_Numero										BIGINT       ,
	IN p_RutContraparte								NVARCHAR(40) ,
	IN p_TaxIDContraparte							NVARCHAR(40) ,
	IN p_CodigoPaisContraparte						NVARCHAR(10) ,
	IN p_TipoRelacionContraparte					NVARCHAR(10) ,
	IN p_ModalidadContratacion						NVARCHAR(10) ,
	IN p_AcuerdoMarcoTipo							NVARCHAR(100),
	IN p_AcuerdoMarcoNumero							DECIMAL(26,8),
	IN p_AcuerdoMarcoFechaSuscripcion				DATE         ,
	IN p_ContratoNumero								DECIMAL(26,8),
	IN p_ContratoFechaSuscripcion					DATE         ,
	IN p_ContratoVencido							NVARCHAR(100),
	IN p_ContratoEstadoConfirmacion					NVARCHAR(100),
	IN p_ContratoTipo								NVARCHAR(100),
	IN p_ContratoNombreInstrumento					NVARCHAR(100),
	IN p_ContratoModalidadCumplimiento				NVARCHAR(100),
	IN p_ContratoPosicionDeclarante					NVARCHAR(100),
	IN p_ActivoSubyacenteTipo						NVARCHAR(100),
	IN p_ActivoSubyacenteCodigo						NVARCHAR(100),
	IN p_ActivoSubyacenteOtros						NVARCHAR(100),
	IN p_ActivoSubyacenteTasaFija					DECIMAL(26,8),
	IN p_SegundoActivoSubyacenteTipo				NVARCHAR(100),
	IN p_SegundoActivoSubyacenteCodigo				NVARCHAR(100),
	IN p_SegundoActivoSubyacenteOtros				NVARCHAR(100),
	IN p_SegundoActivoSubyacenteTasaFija			DECIMAL(26,8),
	IN p_PrecioFuturoCodigoPrecio					NVARCHAR(100),
	IN p_PrecioFuturoPrecio							DECIMAL(26,8),
	IN p_PrecioFuturoMoneda							NVARCHAR(10) ,
	IN p_Unidad										NVARCHAR(100),
	IN p_MontoCantidadContratadoNocional			DECIMAL(26,8),
	IN p_SegundaUnidad								NVARCHAR(100),
	IN p_SegundoMontoNocional						DECIMAL(26,8),
	IN p_FechaVencimiento							DATE         ,
	IN p_FechaLiquidacionEjercicioOpcion			DATE         ,
	IN p_PrecioMercadoCodigoPrecio					NVARCHAR(100),
	IN p_PrecioMercadoPrecio						DECIMAL(26,8),
	IN p_ValorJustoContratoConfirnacion				DECIMAL(26,8),
	IN p_ResultadoEjercicio							DECIMAL(26,8),
	IN p_CuentaContableAsociadaResultadoEjercicio	NVARCHAR(100),
	IN p_EfectoPatrimonio							DECIMAL(26,8),
	IN p_CuentaContableAsociadaEfectoPatrimonio		NVARCHAR(100),
	IN p_ComisionPactada							DECIMAL(26,8),
	IN p_PrimaTotal									DECIMAL(26,8),
	IN p_InversionInicial							DECIMAL(26,8),
	IN p_OtrosGastosAsociadosContrato				DECIMAL(26,8),
	IN p_OtrosIngresosAsociadosContrato				DECIMAL(26,8),
	IN p_PagosMontoPagadosAdeudados					DECIMAL(26,8),
	IN p_PagosExteriorModalidadPago					NVARCHAR(100),
	IN p_SaldoGarantiasCierre						DECIMAL(26,8),


    ------------ Parametros de Salida Propios ----------
    -- ID del registro.
    OUT p_out_id 									BIGINT		 ,

    ------------ Salidas Advertencias ----------

    -- Contador de advertencias.
    OUT p_out_AdvertenciasContador 					INT			 ,

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

        UPDATE "BSCL.Normativa.BD::TRI.DatosDJ1829" SET
            "IdDocumento" 								= 	p_IdDocumento
			,"VboUsuario" 								= 	p_VboUsuario
			,"Observaciones" = (CASE WHEN LENGTH(TRIM(:p_Observaciones)) BETWEEN 4 AND 5 AND LEFT(UPPER(TRIM(:p_Observaciones)), 1) = 'H' THEN "Observaciones" ELSE :p_Observaciones END)
			,"FuenteDatos" 								= 	p_FuenteDatos
			,"Numero"                                   =	p_Numero
			,"RutContraparte"                           =	p_RutContraparte
			,"TaxIDContraparte"                         =	p_TaxIDContraparte
			,"CodigoPaisContraparte"                    =	p_CodigoPaisContraparte
			,"TipoRelacionContraparte"                  =	p_TipoRelacionContraparte
			,"ModalidadContratacion"                    =	p_ModalidadContratacion
			,"AcuerdoMarcoTipo"                         =	p_AcuerdoMarcoTipo
			,"AcuerdoMarcoNumero"                       =	p_AcuerdoMarcoNumero
			,"AcuerdoMarcoFechaSuscripcion"             =	p_AcuerdoMarcoFechaSuscripcion
			,"ContratoNumero"                           =	p_ContratoNumero
			,"ContratoFechaSuscripcion"                 =	p_ContratoFechaSuscripcion
			,"ContratoVencido"                          =	p_ContratoVencido
			,"ContratoEstadoConfirmacion"               =	p_ContratoEstadoConfirmacion
			,"ContratoTipo"                             =	p_ContratoTipo
			,"ContratoNombreInstrumento"                =	p_ContratoNombreInstrumento
			,"ContratoModalidadCumplimiento"            =	p_ContratoModalidadCumplimiento
			,"ContratoPosicionDeclarante"               =	p_ContratoPosicionDeclarante
			,"ActivoSubyacenteTipo"                     =	p_ActivoSubyacenteTipo
			,"ActivoSubyacenteCodigo"                   =	p_ActivoSubyacenteCodigo
			,"ActivoSubyacenteOtros"                    =	p_ActivoSubyacenteOtros
			,"ActivoSubyacenteTasaFija"                 =	p_ActivoSubyacenteTasaFija
			,"SegundoActivoSubyacenteTipo"              =	p_SegundoActivoSubyacenteTipo
			,"SegundoActivoSubyacenteCodigo"            =	p_SegundoActivoSubyacenteCodigo
			,"SegundoActivoSubyacenteOtros"             =	p_SegundoActivoSubyacenteOtros
			,"SegundoActivoSubyacenteTasaFija"          =	p_SegundoActivoSubyacenteTasaFija
			,"PrecioFuturoCodigoPrecio"                 =	p_PrecioFuturoCodigoPrecio
			,"PrecioFuturoPrecio"                       =	p_PrecioFuturoPrecio
			,"PrecioFuturoMoneda"                       =	p_PrecioFuturoMoneda
			,"Unidad"                                   =	p_Unidad
			,"MontoCantidadContratadoNocional"          =	p_MontoCantidadContratadoNocional
			,"SegundaUnidad"                            =	p_SegundaUnidad
			,"SegundoMontoNocional"                     =	p_SegundoMontoNocional
			,"FechaVencimiento"                         =	p_FechaVencimiento
			,"FechaLiquidacionEjercicioOpcion"          =	p_FechaLiquidacionEjercicioOpcion
			,"PrecioMercadoCodigoPrecio"                =	p_PrecioMercadoCodigoPrecio
			,"PrecioMercadoPrecio"                      =	p_PrecioMercadoPrecio
			,"ValorJustoContratoConfirnacion"           =	p_ValorJustoContratoConfirnacion
			,"ResultadoEjercicio"                       =	p_ResultadoEjercicio
			,"CuentaContableAsociadaResultadoEjercicio" =	p_CuentaContableAsociadaResultadoEjercicio
			,"EfectoPatrimonio"                         =	p_EfectoPatrimonio
			,"CuentaContableAsociadaEfectoPatrimonio"   =	p_CuentaContableAsociadaEfectoPatrimonio
			,"ComisionPactada"                          =	p_ComisionPactada
			,"PrimaTotal"                               =	p_PrimaTotal
			,"InversionInicial"                         =	p_InversionInicial
			,"OtrosGastosAsociadosContrato"             =	p_OtrosGastosAsociadosContrato
			,"OtrosIngresosAsociadosContrato"           =	p_OtrosIngresosAsociadosContrato
			,"PagosMontoPagadosAdeudados"               =	p_PagosMontoPagadosAdeudados
			,"PagosExteriorModalidadPago"               =	p_PagosExteriorModalidadPago
			,"SaldoGarantiasCierre"                     =	p_SaldoGarantiasCierre
			--,"IdCliente" 								= 	p_IdCliente
			--,"IdPortafolio" 							= 	p_IdPortafolio
        WHERE "ID" = p_ID;
    ELSE
        SELECT IFNULL(MAX("ID"),0)+1 INTO p_out_id FROM "BSCL.Normativa.BD::TRI.DatosDJ1829";

        insert into "BSCL.Normativa.BD::TRI.DatosDJ1829"(
            "ID"
			,"IdDocumento"
			,"VboUsuario" 
			,"Observaciones"
			,"FuenteDatos"
			,"Numero"                                  
			,"RutContraparte"                          
			,"TaxIDContraparte"                        
			,"CodigoPaisContraparte"                   
			,"TipoRelacionContraparte"                 
			,"ModalidadContratacion"                   
			,"AcuerdoMarcoTipo"                        
			,"AcuerdoMarcoNumero"                      
			,"AcuerdoMarcoFechaSuscripcion"            
			,"ContratoNumero"                          
			,"ContratoFechaSuscripcion"                
			,"ContratoVencido"                         
			,"ContratoEstadoConfirmacion"              
			,"ContratoTipo"                            
			,"ContratoNombreInstrumento"               
			,"ContratoModalidadCumplimiento"           
			,"ContratoPosicionDeclarante"              
			,"ActivoSubyacenteTipo"                    
			,"ActivoSubyacenteCodigo"                  
			,"ActivoSubyacenteOtros"                   
			,"ActivoSubyacenteTasaFija"                
			,"SegundoActivoSubyacenteTipo"             
			,"SegundoActivoSubyacenteCodigo"           
			,"SegundoActivoSubyacenteOtros"            
			,"SegundoActivoSubyacenteTasaFija"         
			,"PrecioFuturoCodigoPrecio"                
			,"PrecioFuturoPrecio"                      
			,"PrecioFuturoMoneda"                      
			,"Unidad"                                  
			,"MontoCantidadContratadoNocional"         
			,"SegundaUnidad"                           
			,"SegundoMontoNocional"                    
			,"FechaVencimiento"                        
			,"FechaLiquidacionEjercicioOpcion"         
			,"PrecioMercadoCodigoPrecio"               
			,"PrecioMercadoPrecio"                     
			,"ValorJustoContratoConfirnacion"          
			,"ResultadoEjercicio"                      
			,"CuentaContableAsociadaResultadoEjercicio"
			,"EfectoPatrimonio"                        
			,"CuentaContableAsociadaEfectoPatrimonio"  
			,"ComisionPactada"                         
			,"PrimaTotal"                              
			,"InversionInicial"                        
			,"OtrosGastosAsociadosContrato"            
			,"OtrosIngresosAsociadosContrato"          
			,"PagosMontoPagadosAdeudados"              
			,"PagosExteriorModalidadPago"              
			,"SaldoGarantiasCierre"  
			,"IdCliente" 
			,"IdPortafolio" 
        ) values (
            p_out_id
            ,p_IdDocumento
			,'N'
            ,''
            ,'MANTENEDOR'
			,p_Numero
			,p_RutContraparte
			,p_TaxIDContraparte
			,p_CodigoPaisContraparte
			,p_TipoRelacionContraparte
			,p_ModalidadContratacion
			,p_AcuerdoMarcoTipo
			,p_AcuerdoMarcoNumero
			,p_AcuerdoMarcoFechaSuscripcion
			,p_ContratoNumero
			,p_ContratoFechaSuscripcion
			,p_ContratoVencido
			,p_ContratoEstadoConfirmacion
			,p_ContratoTipo
			,p_ContratoNombreInstrumento
			,p_ContratoModalidadCumplimiento
			,p_ContratoPosicionDeclarante
			,p_ActivoSubyacenteTipo
			,p_ActivoSubyacenteCodigo
			,p_ActivoSubyacenteOtros
			,p_ActivoSubyacenteTasaFija
			,p_SegundoActivoSubyacenteTipo
			,p_SegundoActivoSubyacenteCodigo
			,p_SegundoActivoSubyacenteOtros
			,p_SegundoActivoSubyacenteTasaFija
			,p_PrecioFuturoCodigoPrecio
			,p_PrecioFuturoPrecio
			,p_PrecioFuturoMoneda
			,p_Unidad
			,p_MontoCantidadContratadoNocional
			,p_SegundaUnidad
			,p_SegundoMontoNocional
			,p_FechaVencimiento
			,p_FechaLiquidacionEjercicioOpcion
			,p_PrecioMercadoCodigoPrecio
			,p_PrecioMercadoPrecio
			,p_ValorJustoContratoConfirnacion
			,p_ResultadoEjercicio
			,p_CuentaContableAsociadaResultadoEjercicio
			,p_EfectoPatrimonio
			,p_CuentaContableAsociadaEfectoPatrimonio
			,p_ComisionPactada
			,p_PrimaTotal
			,p_InversionInicial
			,p_OtrosGastosAsociadosContrato
			,p_OtrosIngresosAsociadosContrato
			,p_PagosMontoPagadosAdeudados
			,p_PagosExteriorModalidadPago
			,p_SaldoGarantiasCierre
			,0
			,0
        );
    END IF;
END;