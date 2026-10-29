SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1932.GrabarDetalle";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1932.GrabarDetalle"
(
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
    IN p_ID 									BIGINT		 ,
    -- ID del documento.
    IN p_IdDocumento 							BIGINT		 ,
    -- VoBo del usuario sobre el registro (S/N).
    IN p_VboUsuario 							CHAR(1)		 ,
    -- Observacion ingresada por usuario que edito el registro.
    IN p_Observaciones 							NVARCHAR(250),
    -- Fuente de los datos.
    IN p_FuenteDatos 							NVARCHAR(40) ,
    IN p_IdCliente 								BIGINT		 ,
    IN p_IdPortafolio 							BIGINT		 ,
	IN p_Numero									BIGINT       ,
	IN p_Mes									BIGINT       ,
	IN p_RutDepositanteInversionistaPrestamista	NVARCHAR(40) ,
	IN p_TaxID									NVARCHAR(40) ,
	IN p_Pais									NVARCHAR(40) ,
	IN p_DepositosInversionesPrestamos			DECIMAL(26,8),
	IN p_RescatesDevoluciones					DECIMAL(26,8),
	IN p_InteresesPercibidas					DECIMAL(26,8),
	IN p_DividendosPercibidas					DECIMAL(26,8),
	IN p_ContratosPorDiferenciaPercibidas		DECIMAL(26,8),
	IN p_OtrasRentasPercibidas					DECIMAL(26,8),
	IN p_InteresesDevengadas					DECIMAL(26,8),
	IN p_OtrasRentasDevengadas					DECIMAL(26,8),
	IN p_IdOperacion                            BIGINT,
	IN p_FechaOperacion                         DATE,
    IN p_TipoDocumento 							NVARCHAR(10) ,
    IN p_NumeroDocumento 						BIGINT		 ,
    IN p_FechaDocumento 						DATE		 ,
    IN p_IdBaseDocumento 						BIGINT		 ,
    IN p_TipoTransaccion 						NVARCHAR(6)	 ,
    IN p_IdTipoOperacion 						NVARCHAR(6)	 ,
    IN p_IdTipoProducto 						NVARCHAR(4)	 ,


    ------------ Parametros de Salida Propios ----------
    -- ID del registro.
    OUT p_out_id 								BIGINT		 ,

    ------------ Salidas Advertencias ----------

    -- Contador de advertencias.
    OUT p_out_AdvertenciasContador 				INT			 ,

    -- Tabla de advertencias.
    OUT p_out_AdvertenciasDetalle TABLE("Codigo" NVARCHAR(40), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)),

    ------------ Salidas Errores ----------

    -- Contador de errores
    -- Importante: Debe ser cero para que se haga commit.
    OUT p_out_ErroresContador 					INT,

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

        UPDATE "BSCL.Normativa.BD::TRI.DatosDJ1932" SET
            "IdDocumento" 								= 	p_IdDocumento
			,"VboUsuario" 								= 	p_VboUsuario
			,"Observaciones" = (CASE WHEN LENGTH(TRIM(:p_Observaciones)) BETWEEN 4 AND 5 AND LEFT(UPPER(TRIM(:p_Observaciones)), 1) = 'H' THEN "Observaciones" ELSE :p_Observaciones END)
			,"FuenteDatos" 								= 	p_FuenteDatos
			,"Numero"									=	p_Numero
			,"Mes"										=	p_Mes
			,"RutDepositanteInversionistaPrestamista"	=	p_RutDepositanteInversionistaPrestamista
			,"TaxID"									=	p_TaxID
			,"Pais"										=	p_Pais
			,"DepositosInversionesPrestamos"			=	p_DepositosInversionesPrestamos
			,"RescatesDevoluciones"						=	p_RescatesDevoluciones
			,"InteresesPercibidas"						=	p_InteresesPercibidas
			,"DividendosPercibidas"						=	p_DividendosPercibidas
			,"ContratosPorDiferenciaPercibidas"			=	p_ContratosPorDiferenciaPercibidas
			,"OtrasRentasPercibidas"					=	p_OtrasRentasPercibidas
			,"InteresesDevengadas"						=	p_InteresesDevengadas
			,"OtrasRentasDevengadas"					=	p_OtrasRentasDevengadas
			,"IdCliente" 								= 	p_IdCliente
            ,"CodPaisRenta"                             =   p_Pais
        WHERE "ID" = p_ID;
    ELSE
        SELECT IFNULL(MAX("ID"),0)+1 INTO p_out_id FROM "BSCL.Normativa.BD::TRI.DatosDJ1932";

        insert into "BSCL.Normativa.BD::TRI.DatosDJ1932"(
            "ID"
			,"IdDocumento"
			,"VboUsuario" 
			,"Observaciones"
			,"FuenteDatos"						
			,"Numero"									
			,"Mes"										
			,"RutDepositanteInversionistaPrestamista"	
			,"TaxID"									
			,"Pais"										
			,"DepositosInversionesPrestamos"			
			,"RescatesDevoluciones"						
			,"InteresesPercibidas"						
			,"DividendosPercibidas"						
			,"ContratosPorDiferenciaPercibidas"			
			,"OtrasRentasPercibidas"					
			,"InteresesDevengadas"						
			,"OtrasRentasDevengadas"
			,"FechaOperacion"																									       
			,"IdCliente" 
            ,"CodPaisRenta"
        ) values (
            p_out_id
            ,p_IdDocumento
			,'N'
            ,''
            ,'MANTENEDOR'
			,COALESCE(p_Numero, 0)
			,p_Mes
			,p_RutDepositanteInversionistaPrestamista
			,p_TaxID
			,p_Pais
			,p_DepositosInversionesPrestamos
			,p_RescatesDevoluciones
			,p_InteresesPercibidas
			,p_DividendosPercibidas
			,p_ContratosPorDiferenciaPercibidas
			,p_OtrasRentasPercibidas
			,p_InteresesDevengadas
			,p_OtrasRentasDevengadas
			,p_FechaOperacion
			,p_IdCliente
            ,p_Pais
        );
    END IF;
END;