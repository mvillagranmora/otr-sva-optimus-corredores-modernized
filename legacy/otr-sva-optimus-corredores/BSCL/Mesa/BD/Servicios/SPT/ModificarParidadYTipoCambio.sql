SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ModificarParidadYTipoCambio";

-- 
--  Proposito: Modificar Paridad y Tipo Cambio
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 10/01/2017
-- 
GO
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ModificarParidadYTipoCambio" (
	-- Parametros entrada comunes
    IN p_IdEmpresa		BIGINT,			--ID Empresa destino
	IN p_IdUsuario		BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo		NVARCHAR(3),  	--ID del Modulo que lo invoca
    
    -- Parametros entrada
	IN p_Fecha			DATE,
	IN p_OrigenJob		NVARCHAR(1) DEFAULT 'N',
    IN p_DatosParidadYTipoCambio	TABLE (	"IdParidadYTipoCambio"		BIGINT
	    									,"IdMoneda"					NVARCHAR(3)
	    									,"NombreMoneda"				NVARCHAR(100)
											,"ParidadMercado"			DECIMAL(26,8)
											,"ParidadBID"				DECIMAL(26,8)
											,"ParidadASK" 				DECIMAL(26,8)
											,"LimiteSuperiorParidad"	DECIMAL(26,8)
											,"LimiteInferiorParidad"	DECIMAL(26,8)
											,"ParidadSBIF"				DECIMAL(26,8)
											,"TipoCambioMercado"		DECIMAL(26,8)
											,"LimiteSuperiorTipoCambio"	DECIMAL(26,8)
											,"LimiteInferiorTipoCambio"	DECIMAL(26,8)									
											,"PrecioSpotCompra" 		DECIMAL(26,8)
											,"PrecioSpotVenta"			DECIMAL(26,8)
											,"PrecioSpotPromedio"		DECIMAL(26,8)),
	
     -- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER  
DEFAULT SCHEMA "BSCL"
--READS SQL DATA   
AS
BEGIN
	
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE valida_existencia INT;
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(100),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);
	DECLARE v_out_AdvertenciasDetalle	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40))
										DEFAULT SELECT 0 AS "IdInterno",'' AS "Codigo",'' AS "RequiereAutorizacion",'' AS "ValorParametro",'' AS "NombreParametro" FROM DUMMY WHERE 1=2;
	DECLARE v_out_ErroresDetalle		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40))
										DEFAULT SELECT 0  AS "IdInterno",'' AS "Codigo",'' AS "ValorParametro",'' AS "NombreParametro" FROM DUMMY WHERE 1=2;

	DECLARE v_diaHabilSiguiente NVARCHAR(10);
	DECLARE v_idPaisSistema BIGINT := 0;
	--datos para logs 
	DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
	DECLARE v_fechaHoraSistema NVARCHAR(40);
	DECLARE v_NombreProceso	NVARCHAR(100):='Modificación de datos de Paridad y Tipo Cambio';
    DECLARE v_CantRegistro INT;
	DECLARE v_tipoLog NVARCHAR(11);

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador from :v_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador from :v_out_AdvertenciasDetalle;
		
		v_TablaCodigosMensajes =
			SELECT "Codigo" AS "CodigoMensaje", "NombreParametro" as "CodigoDato", "ValorParametro" as "ValorDato" FROM :v_out_ErroresDetalle
			UNION ALL
			SELECT "Codigo" AS "CodigoMensaje", "NombreParametro" as "CodigoDato", "ValorParametro" as "ValorDato" FROM :v_out_AdvertenciasDetalle;
		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle =	SELECT errores."IdInterno", errores."Codigo", msj."Descripcion"
								FROM :v_out_ErroresDetalle errores
								INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";
								
		p_out_AdvertenciasDetalle =	SELECT adv."IdInterno", adv."Codigo", adv."RequiereAutorizacion", msj."Descripcion"
									FROM :v_out_AdvertenciasDetalle adv
									INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";
	END;
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	v_out_AdvertenciasDetalle 	= SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	v_out_ErroresDetalle 		= SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	
    BEGIN
	    IF (:p_OrigenJob = 'S') THEN
			--Obtener IdPaisSistema
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion"(p_IdEmpresa => :p_IdEmpresa, p_CodigoParametro => 'PLA.PaisSistema', p_out_Valor => :v_idPaisSistema);
			
			--Obtener dia habil siguiente. Es la fecha donde es ingresado el valor del Dolar Observado desde Job: ActualizarParametrosFinancieros
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaSiguiente" ( 	
				p_Fecha => :p_Fecha,
				p_NumeroDias => 1,
				p_EsHabil => 'S',
				p_IdPais => :v_idPaisSistema,
				p_out_FechaSiguente => :v_diaHabilSiguiente
			);
		END IF;

		--TIPO LOG
		SELECT COUNT(DPTC."IdParidadYTipoCambio") INTO v_CantRegistro FROM :p_DatosParidadYTipoCambio DPTC;
 		IF (v_CantRegistro = 1) THEN
 			v_tipoLog = 'INGRESO';
 		ELSE 
 			v_tipoLog = 'MODIFICADOR';
 		END IF;
		
		--DATOS CABECERA LOG
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa => p_IdEmpresa, p_out_FechaHoraSistema => v_fechaHoraSistema);
	--se guarda detalle log antes de actualizar datos.	
	IF (v_tipoLog = 'MODIFICADOR') THEN 
		p_DetalleLog =
			SELECT 
			"SubRegistro", "CampoRegistro","ValorAntiguo","ValorNuevo"
			FROM (SELECT
				PTC."IdMoneda",
				'PARIDADYTIPOCAMBIO' AS "SubRegistro",
				CASE X."Field"
					WHEN 'ParidadMercado' THEN CONCAT(PTC."IdMoneda", ' - Paridad Mercado')
					WHEN 'ParidadBID' THEN CONCAT(PTC."IdMoneda", ' - Paridad BID')
					WHEN 'ParidadASK' THEN CONCAT(PTC."IdMoneda", ' - Paridad ASK')
					WHEN 'LimiteSuperiorParidad' THEN CONCAT(PTC."IdMoneda", ' - Limite Superior Paridad')
					WHEN 'LimiteInferiorParidad' THEN CONCAT(PTC."IdMoneda", ' - Limite Inferior Paridad')
					WHEN 'ParidadSBIF' THEN CONCAT(PTC."IdMoneda", ' - Paridad SBIF')
					WHEN 'TipoCambioMercado' THEN CONCAT(PTC."IdMoneda", ' - Tipo Cambio Mercado')
					WHEN 'LimiteSuperiorTipoCambio' THEN CONCAT(PTC."IdMoneda", ' - Limite Superior Tipo Cambio')
					WHEN 'LimiteInferiorTipoCambio' THEN CONCAT(PTC."IdMoneda", ' - Limite Inferior Tipo Cambio')
					WHEN 'PrecioSpotCompra' THEN CONCAT(PTC."IdMoneda", ' - Precio Spot Compra')
					WHEN 'PrecioSpotVenta' THEN CONCAT(PTC."IdMoneda", ' - Precio Spot Venta')
					WHEN 'PrecioSpotPromedio' THEN CONCAT(PTC."IdMoneda", ' - Precio Spot Promedio')
				END AS "CampoRegistro",
				CASE X."Field"
					WHEN 'ParidadMercado' THEN PTC."ParidadMercado"
					WHEN 'ParidadBID' THEN PTC."ParidadBID"
					WHEN 'ParidadASK' THEN PTC."ParidadASK"
					WHEN 'LimiteSuperiorParidad' THEN PTC."LimiteSuperiorParidad"
					WHEN 'LimiteInferiorParidad' THEN PTC."LimiteInferiorParidad"
					WHEN 'ParidadSBIF' THEN PTC."ParidadSBIF"
					WHEN 'TipoCambioMercado' THEN PTC."TipoCambioMercado"
					WHEN 'LimiteSuperiorTipoCambio' THEN PTC."LimiteSuperiorTipoCambio"
					WHEN 'LimiteInferiorTipoCambio' THEN PTC."LimiteInferiorTipoCambio"
					WHEN 'PrecioSpotCompra' THEN PTC."PrecioSpotCompra"
					WHEN 'PrecioSpotVenta' THEN PTC."PrecioSpotVenta"
					WHEN 'PrecioSpotPromedio' THEN PTC."PrecioSpotPromedio"
				END AS "ValorAntiguo" ,
				CASE X."Field"
					WHEN 'ParidadMercado' THEN DPTC."ParidadMercado"
					WHEN 'ParidadBID' THEN DPTC."ParidadBID"
					WHEN 'ParidadASK' THEN DPTC."ParidadASK"
					WHEN 'LimiteSuperiorParidad' THEN DPTC."LimiteSuperiorParidad"
					WHEN 'LimiteInferiorParidad' THEN DPTC."LimiteInferiorParidad"
					WHEN 'ParidadSBIF' THEN DPTC."ParidadSBIF"
					WHEN 'TipoCambioMercado' THEN DPTC."TipoCambioMercado"
					WHEN 'LimiteSuperiorTipoCambio' THEN DPTC."LimiteSuperiorTipoCambio"
					WHEN 'LimiteInferiorTipoCambio' THEN DPTC."LimiteInferiorTipoCambio"
					WHEN 'PrecioSpotCompra' THEN DPTC."PrecioSpotCompra"
					WHEN 'PrecioSpotVenta' THEN DPTC."PrecioSpotVenta"
					WHEN 'PrecioSpotPromedio' THEN DPTC."PrecioSpotPromedio"
				END AS "ValorNuevo"
			FROM 
				"BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTC
				INNER JOIN :p_DatosParidadYTipoCambio  DPTC
				ON (PTC."IdParidadYTipoCambio" = DPTC."IdParidadYTipoCambio" 
					OR (PTC."IdMoneda" = DPTC."IdMoneda" 
						AND PTC."Fecha" = (CASE WHEN :p_OrigenJob = 'S' AND DPTC."IdMoneda" = 'DO' THEN TO_SECONDDATE(:v_diaHabilSiguiente) ELSE TO_SECONDDATE(:p_Fecha) END) 
						AND PTC."IdParidadYTipoCambio" > 0 
						AND DPTC."IdParidadYTipoCambio" = 0))
				CROSS JOIN (
					SELECT 'ParidadMercado' AS "Field" FROM dummy
					UNION ALL (SELECT 'ParidadBID' AS "Field" FROM dummy)
					UNION ALL (SELECT 'ParidadASK' AS "Field"  FROM dummy)
					UNION ALL (SELECT 'LimiteSuperiorParidad' AS "Field"  FROM dummy)
					UNION ALL (SELECT 'LimiteInferiorParidad' AS "Field"  FROM dummy)
					UNION ALL (SELECT 'ParidadSBIF' AS "Field"  FROM dummy)
					UNION ALL (SELECT 'TipoCambioMercado' AS "Field"  FROM dummy)
					UNION ALL (SELECT 'LimiteSuperiorTipoCambio' AS "Field"  FROM dummy)
					UNION ALL (SELECT 'LimiteInferiorTipoCambio' AS "Field"  FROM dummy)
					UNION ALL (SELECT 'PrecioSpotCompra' AS "Field"  FROM dummy)
					UNION ALL (SELECT 'PrecioSpotVenta' AS "Field"  FROM dummy)
					UNION ALL (SELECT 'PrecioSpotPromedio' AS "Field"  FROM dummy)
				
				) AS X
			WHERE 
				(X."Field" = 'ParidadMercado' AND PTC."ParidadMercado" != DPTC."ParidadMercado")
				OR (X."Field" = 'ParidadBID' AND PTC."ParidadBID" != DPTC."ParidadBID")
				OR (X."Field" = 'ParidadASK' AND PTC."ParidadASK" != DPTC."ParidadASK")
				OR (X."Field" = 'LimiteSuperiorParidad' AND PTC."LimiteSuperiorParidad" != DPTC."LimiteSuperiorParidad")
				OR (X."Field" = 'LimiteInferiorParidad' AND PTC."LimiteInferiorParidad" != DPTC."LimiteInferiorParidad")
				OR (X."Field" = 'ParidadSBIF' AND PTC."ParidadSBIF" != DPTC."ParidadSBIF")
				OR (X."Field" = 'TipoCambioMercado' AND PTC."TipoCambioMercado" != DPTC."TipoCambioMercado")
				OR (X."Field" = 'LimiteSuperiorTipoCambio' AND PTC."LimiteSuperiorTipoCambio" != DPTC."LimiteSuperiorTipoCambio")
				OR (X."Field" = 'LimiteInferiorTipoCambio' AND PTC."LimiteInferiorTipoCambio" != DPTC."LimiteInferiorTipoCambio")
				OR (X."Field" = 'PrecioSpotCompra' AND PTC."PrecioSpotCompra" != DPTC."PrecioSpotCompra")
				OR (X."Field" = 'PrecioSpotVenta' AND PTC."PrecioSpotVenta" != DPTC."PrecioSpotVenta")
				OR (X."Field" = 'PrecioSpotPromedio' AND PTC."PrecioSpotPromedio" != DPTC."PrecioSpotPromedio")
			) ORDER BY "IdMoneda", "CampoRegistro";
  	ELSE
	    p_DetalleLog =
		SELECT 'PARIDADYTIPOCAMBIO' AS "SubRegistro", 'USD - Tipo Cambio Mercado' AS "CampoRegistro", 0 AS "ValorAntiguo", DPTC."TipoCambioMercado" AS "ValorNuevo"
		FROM :p_DatosParidadYTipoCambio  DPTC
		WHERE DPTC."IdMoneda" = 'USD';
	END IF;

  		-- Registros modificados
  		UPDATE PTC
		SET	
			 PTC."Fecha"					= CASE WHEN :p_OrigenJob = 'S' AND DPTC."IdMoneda" = 'DO' THEN TO_SECONDDATE(:v_diaHabilSiguiente) ELSE TO_SECONDDATE(:p_Fecha) END
			,PTC."ParidadMercado"			= COALESCE(DPTC."ParidadMercado",0)
			,PTC."ParidadBID"				= COALESCE(DPTC."ParidadBID",0)
			,PTC."ParidadASK" 				= COALESCE(DPTC."ParidadASK",0)
			,PTC."LimiteSuperiorParidad"	= COALESCE(DPTC."LimiteSuperiorParidad",0)
			,PTC."LimiteInferiorParidad"	= COALESCE(DPTC."LimiteInferiorParidad",0)
			,PTC."ParidadSBIF"				= COALESCE(DPTC."ParidadSBIF",0)
			,PTC."TipoCambioMercado"		= COALESCE(DPTC."TipoCambioMercado",0)
			,PTC."LimiteSuperiorTipoCambio"	= COALESCE(DPTC."LimiteSuperiorTipoCambio",0)
			,PTC."LimiteInferiorTipoCambio"	= COALESCE(DPTC."LimiteInferiorTipoCambio",0)				
			,PTC."PrecioSpotCompra" 		= COALESCE(DPTC."PrecioSpotCompra",0)
			,PTC."PrecioSpotVenta"			= COALESCE(DPTC."PrecioSpotVenta",0)
			,PTC."PrecioSpotPromedio"		= COALESCE(DPTC."PrecioSpotPromedio",0)
			,PTC."UsuarioModificador"		= COALESCE(:p_IdUsuario,0)
		FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTC
		INNER JOIN :p_DatosParidadYTipoCambio DPTC
			ON (PTC."IdParidadYTipoCambio" = DPTC."IdParidadYTipoCambio" OR 
				(PTC."IdMoneda"				= DPTC."IdMoneda" AND 
				 PTC."Fecha"				= (CASE WHEN :p_OrigenJob = 'S' AND DPTC."IdMoneda" = 'DO' THEN TO_SECONDDATE(:v_diaHabilSiguiente) ELSE TO_SECONDDATE(:p_Fecha) END) AND
				 PTC."IdParidadYTipoCambio"	> 0 AND 
				 DPTC."IdParidadYTipoCambio"= 0))
		--WHERE DPTC."IdParidadYTipoCambio" != 0
		;
		
		-- Nuevos registros
		INSERT INTO "BSCL.Mesa.BD::SPT.ParidadYTipoCambio"(
			 "IdParidadYTipoCambio"
			,"IdMoneda"
			,"NombreMoneda"
			,"Fecha"
			,"IdEstado"
			,"Origen"
			,"IdUsuarioIngreso"
			,"ParidadMercado"
			,"ParidadBID"
			,"ParidadASK"
			,"LimiteSuperiorParidad"
			,"LimiteInferiorParidad"
			,"ParidadSBIF"
			,"TipoCambioMercado"
			,"LimiteSuperiorTipoCambio"
			,"LimiteInferiorTipoCambio"
			,"PrecioSpotCompra"
			,"PrecioSpotVenta"
			,"PrecioSpotPromedio"
			,"UsuarioModificador"
		) 
		SELECT
			 "BSCL.Mesa.BD.Secuencias::IdParidadYTipoCambio".NEXTVAL
			,COALESCE(DPTC."IdMoneda", '')
			,COALESCE(DPTC."NombreMoneda", '')
			,CASE WHEN :p_OrigenJob = 'S' AND DPTC."IdMoneda" = 'DO' THEN TO_SECONDDATE(:v_diaHabilSiguiente) ELSE TO_SECONDDATE(:p_Fecha) END
			,'ACT'
			,'OBSERVADO'
			,:p_IdUsuario
			,COALESCE(DPTC."ParidadMercado", 0) 
			,COALESCE(DPTC."ParidadBID", 0)
			,COALESCE(DPTC."ParidadASK", 0)
			,COALESCE(DPTC."LimiteSuperiorParidad", 0)
			,COALESCE(DPTC."LimiteInferiorParidad", 0)
			,COALESCE(DPTC."ParidadSBIF", 0)
			,COALESCE(DPTC."TipoCambioMercado", 0)
			,COALESCE(DPTC."LimiteSuperiorTipoCambio", 0)
			,COALESCE(DPTC."LimiteInferiorTipoCambio", 0)
			,COALESCE(DPTC."PrecioSpotCompra", 0)
			,COALESCE(DPTC."PrecioSpotVenta", 0)
			,COALESCE(DPTC."PrecioSpotPromedio", 0)
			,COALESCE(:p_IdUsuario,0)
		FROM :p_DatosParidadYTipoCambio DPTC
		LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTC
			ON (DPTC."IdMoneda" = PTC."IdMoneda" AND PTC."Fecha" = CASE WHEN :p_OrigenJob = 'S' AND DPTC."IdMoneda" = 'DO' THEN TO_SECONDDATE(:v_diaHabilSiguiente) ELSE TO_SECONDDATE(:p_Fecha) END)
		WHERE DPTC."IdParidadYTipoCambio" = 0
		  AND PTC."IdParidadYTipoCambio" IS NULL;
  			    
  		-- Inserto mensaje de aviso de actualización
		INSERT INTO "BSCL.Plataforma.BD::PLA.MensajeWebsocket"
		("IdEmpresa","FechaRegistro","Evento","DATA","EsProcesado")
		VALUES(1,CURRENT_UTCTIMESTAMP,'ACTPTC',	'',	'N'	); 
		  	
	
	
		--agregamos call a logs luego de todos los insert y updates realizados confirmamos que termino correctamente
	CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
			p_IdTipoLog			=> :v_tipoLog 
			,p_IdModulo			=> p_IdModulo 
			,p_IdUsuario		=> p_IdUsuario 
			,p_IdEmpresa		=> p_IdEmpresa 
			,p_FechaSistemaLog	=> TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD')
			,p_IdUsuarioAtendido=> p_IdUsuario
			,p_IdTipoRegistro	=> 'PARIDADYTIPOCAMBIO'
			,p_DetalleLog		=> :p_DetalleLog
			,p_NombreProceso	=> :v_NombreProceso
		);
    END;
    
        --Se agrega el valor de TipoCambioMercado a la tabla Posicion Inicial Mesa Spot para mostrar este valor en el campo Cierre en la UI
    BEGIN
	    UPDATE PIM
        SET PIM."CierreIngresado" = PTC."TipoCambioMercado"  
        FROM "BSCL.Mesa.BD::SPT.PosicionInicialMesa" PIM
        INNER JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTC ON PTC."IdMoneda" = PIM."IdMoneda" AND TO_VARCHAR(PIM."Fecha",'YYYY-MM-DD') = TO_VARCHAR(PTC."Fecha",'YYYY-MM-DD')
        WHERE TO_VARCHAR(PIM."Fecha",'YYYY-MM-DD') = :p_Fecha AND PIM."IdMoneda" = 'USD';   
    END;
   

END;