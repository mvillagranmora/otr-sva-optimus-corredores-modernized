SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1894.GrabarDetalle";


CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1894.GrabarDetalle"
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
   -- IN p_IdComprobante                          BIGINT       ,
   
	IN p_Numero 							BIGINT,
	IN p_FolioCartera 						DECIMAL(26,8),
	IN p_IdFondo 							NVARCHAR(40),
	IN p_Nemo 								NVARCHAR(40),
	IN p_IdEmisor 							NVARCHAR(40),
	IN p_RutEmisor 							NVARCHAR(40),
	IN p_RutParticipe 						NVARCHAR(40),
	IN p_RutFondo 							NVARCHAR(40),
	IN p_RunFondo 							NVARCHAR(40),
	IN p_TipoOperacion 						NVARCHAR(10),
	IN p_FechaOperacion 					DATE,	
	IN p_FechaInversion 					DATE,
	IN p_MontoNominalInversionActualizado	DECIMAL(26,8) ,
	IN p_MontoHistorico 					DECIMAL(26,8) ,
	IN p_FechaRescate 						DATE ,
	IN p_MontoNominalRescate 				DECIMAL(26,8),
	IN p_MayorValorActualizado 				DECIMAL(26,8),
	IN p_MenorValorActualizado 				DECIMAL(26,8),
	IN p_RutAdminitradoraFondoReceptor 		NVARCHAR(40) ,
	IN p_MontoReinvertido 					DECIMAL(26,8),
	IN p_NumeroCertificado 					BIGINT,
	IN p_CostoUf 							DECIMAL(26,8),
	IN p_IdCliente 							BIGINT,
	IN p_IdPortafolio 						BIGINT,
	IN p_TipoDeclarante 					NVARCHAR(40),   


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

        UPDATE "BSCL.Normativa.BD::TRI.DatosDJ1894" 
		SET 
			"IdDocumento"								= p_IdDocumento
			,"VboUsuario"								= p_VboUsuario
			,"Observaciones" = (CASE WHEN LENGTH(TRIM(:p_Observaciones)) BETWEEN 4 AND 5 AND LEFT(UPPER(TRIM(:p_Observaciones)), 1) = 'H' THEN "Observaciones" ELSE :p_Observaciones END)
			,"FuenteDatos"								= p_FuenteDatos
			,"Numero"									= p_Numero
			,"FolioCartera"								= p_FolioCartera
			,"IdFondo"									= p_IdFondo
			,"Nemo"										= p_Nemo
			,"IdEmisor"									= p_IdEmisor
			,"RutEmisor"								= p_RutEmisor
			,"RutParticipe"								= p_RutParticipe
			,"RutFondo"									= p_RutFondo
			,"RunFondo"									= p_RunFondo
			,"TipoOperacion"							= p_TipoOperacion
			--,"FechaOperacion"							= p_FechaOperacion
			,"FechaOperacion"							= CASE WHEN p_FechaOperacion = '0001-01-01' THEN '1900-01-01' ELSE p_FechaOperacion END
			--,"FechaInversion"							= p_FechaInversion
			,"FechaInversion"							= CASE WHEN p_FechaInversion = '0001-01-01' THEN '1900-01-01' ELSE p_FechaInversion END
			,"MontoNominalInversionActualizado"			= p_MontoNominalInversionActualizado
			,"MontoHistorico"							= p_MontoHistorico
			--,"FechaRescate"								= p_FechaRescate
			,"FechaRescate"								= CASE WHEN p_FechaRescate = '0001-01-01' THEN '1900-01-01' ELSE p_FechaRescate END
			,"MontoNominalRescate"						= p_MontoNominalRescate
			,"MayorValorActualizado"					= p_MayorValorActualizado
			,"MenorValorActualizado"					= p_MenorValorActualizado
			,"RutAdminitradoraFondoReceptor"			= p_RutAdminitradoraFondoReceptor
			,"MontoReinvertido"							= p_MontoReinvertido
			,"NumeroCertificado"						= p_NumeroCertificado
			,"CostoUf"									= p_CostoUf
			,"IdCliente"								= p_IdCliente
			,"IdPortafolio"								= p_IdPortafolio
			,"TipoDeclarante"							= p_TipoDeclarante
WHERE "ID" = p_ID;
    ELSE
        SELECT IFNULL(MAX("ID"),0)+1 INTO p_out_id FROM "BSCL.Normativa.BD::TRI.DatosDJ1894";

        insert into "BSCL.Normativa.BD::TRI.DatosDJ1894"(
             "ID"
			,"IdDocumento"
			,"VboUsuario"
			,"Observaciones" 
			,"FuenteDatos"
			,"Numero"
			,"FolioCartera"
			,"IdFondo"
			,"Nemo"
			,"IdEmisor"
			,"RutEmisor"
			,"RutParticipe"
			,"RutFondo"
			,"RunFondo"
			,"TipoOperacion"
			,"FechaOperacion"
			,"FechaInversion"
			,"MontoNominalInversionActualizado"
			,"MontoHistorico"
			,"FechaRescate"
			,"MontoNominalRescate"
			,"MayorValorActualizado"
			,"MenorValorActualizado"
			,"RutAdminitradoraFondoReceptor"
			,"MontoReinvertido"
			,"NumeroCertificado"
			,"CostoUf"
			,"IdCliente"
			,"IdPortafolio"
			,"TipoDeclarante"



        ) values (
             p_out_id
			,p_IdDocumento
			,'N'
			,COALESCE(p_Observaciones,'')
			,'MANTENEDOR'
			,COALESCE(p_Numero, 0)										
			,p_FolioCartera 									
			,p_IdFondo 										
			,p_Nemo 											
			,p_IdEmisor 										
			,p_RutEmisor 										
			,p_RutParticipe 									
			,p_RutFondo 										
			,p_RunFondo 										
			,p_TipoOperacion 									
			,p_FechaOperacion
			,p_FechaInversion 								
			,p_MontoNominalInversionActualizado				
			,p_MontoHistorico 								
			,p_FechaRescate
			,p_MontoNominalRescate 							
			,p_MayorValorActualizado 							
			,p_MenorValorActualizado 							
			,p_RutAdminitradoraFondoReceptor 					
			,p_MontoReinvertido 								
			,p_NumeroCertificado 								
			,p_CostoUf 										
			,p_IdCliente 										
			,p_IdPortafolio 									
			,p_TipoDeclarante 								
        );
    END IF;
END;