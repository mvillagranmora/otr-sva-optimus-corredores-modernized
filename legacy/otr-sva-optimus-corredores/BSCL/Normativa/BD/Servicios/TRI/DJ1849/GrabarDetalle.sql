SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1849.GrabarDetalle";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1849.GrabarDetalle"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    IN p_IdEmpresa 						BIGINT		 ,
    -- ID usuario del login.
    IN p_IdUsuario 						BIGINT		 ,
    -- ID modulo invocador.
    IN p_IdModulo 						NVARCHAR(3)	 ,
    ------------ Parametros de Entrada Propios ----------
    -- ID del detalle.
    -- Si el id es mayor que cero, se asume que es un registro existente para actualizar.
    -- En caso contrario, se intenta crear un nuevo registro.
    IN p_ID 							BIGINT		 ,
    -- ID del documento.
    IN p_IdDocumento 					BIGINT		 ,
    -- VoBo del usuario sobre el registro (S/N).
    IN p_VboUsuario 					CHAR(1)		 ,
    -- Observacion ingresada por usuario que edito el registro.
    IN p_Observaciones 					NVARCHAR(250),
    -- Fuente de los datos.
    IN p_FuenteDatos 					NVARCHAR(40) ,
    IN p_IdCartera                      BIGINT       ,
    IN p_FechaOperacion                 DATE         ,
    IN p_IdCliente 						BIGINT		 ,
    IN p_IdPortafolio 					BIGINT		 ,
   	IN p_Numero							BIGINT       ,
	IN p_RutEmisor						NVARCHAR(40) ,
	IN p_RutTenedor						NVARCHAR(40) ,
	IN p_TipoContribuyenteRutTenedor	NVARCHAR(100),
	IN p_NemotecnicoSVS					NVARCHAR(10) ,
	IN p_InteresDevengado				DECIMAL(26,8),
	IN p_RetencionImpuesto				DECIMAL(26,8),
    IN p_TipoDocumento 					NVARCHAR(10) ,
    IN p_NumeroDocumento 				BIGINT		 ,
    IN p_FechaDocumento 				DATE		 ,
    IN p_IdBaseDocumento 				BIGINT		 ,
    IN p_TipoTransaccion 				NVARCHAR(6)	 ,
    IN p_IdTipoOperacion 				NVARCHAR(6)	 ,
    IN p_IdTipoProducto 				NVARCHAR(4)	 ,


    ------------ Parametros de Salida Propios ----------
    -- ID del registro.
    OUT p_out_id 						BIGINT		 ,

    ------------ Salidas Advertencias ----------

    -- Contador de advertencias.
    OUT p_out_AdvertenciasContador 		INT			 ,

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

        UPDATE "BSCL.Normativa.BD::TRI.DatosDJ1849" SET
            "IdDocumento" 						= 	p_IdDocumento
			,"VboUsuario" 						= 	p_VboUsuario
			,"Observaciones" 					= 	p_Observaciones
			,"FuenteDatos" 						= 	p_FuenteDatos
			,"IdCartera"                        =   p_IdCartera
			,"FechaOperacion"                   =   p_FechaOperacion
			,"Numero"							=	p_Numero
			,"RutEmisor"						=	p_RutEmisor
			,"RutTenedor"						=	p_RutTenedor
			,"TipoContribuyenteRutTenedor"		=	p_TipoContribuyenteRutTenedor
			,"NemotecnicoSVS"					=	p_NemotecnicoSVS
			,"InteresDevengado"					=	p_InteresDevengado
			,"RetencionImpuesto"				=	p_RetencionImpuesto
			,"TipoDocumento" 					= 	p_TipoDocumento
			,"NumeroDocumento" 					= 	p_NumeroDocumento
			,"FechaDocumento" 					= 	p_FechaDocumento
			,"IdBaseDocumento" 					= 	p_IdBaseDocumento
			,"IdCliente" 						= 	p_IdCliente
			,"IdPortafolio" 					= 	p_IdPortafolio
			,"TipoTransaccion" 					= 	p_TipoTransaccion
			,"IdTipoOperacion" 					= 	p_IdTipoOperacion
			,"IdTipoProducto" 					= 	p_IdTipoProducto
        WHERE "ID" = p_ID;
    ELSE
        SELECT IFNULL(MAX("ID"),0)+1 INTO p_out_id FROM "BSCL.Normativa.BD::TRI.DatosDJ1849";

        insert into "BSCL.Normativa.BD::TRI.DatosDJ1849"(
            "ID"
			,"IdDocumento"
			,"VboUsuario" 
			,"Observaciones"
			,"FuenteDatos"
			,"IdCartera"
			,"FechaOperacion"
			,"Numero"						
			,"RutEmisor"					
			,"RutTenedor"					
			,"TipoContribuyenteRutTenedor"	
			,"NemotecnicoSVS"				
			,"InteresDevengado"				
			,"RetencionImpuesto"											       
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
            ,p_IdCartera
            ,p_FechaOperacion
			,p_Numero
			,p_RutEmisor
			,p_RutTenedor
			,p_TipoContribuyenteRutTenedor
			,p_NemotecnicoSVS
			,p_InteresDevengado
			,p_RetencionImpuesto
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