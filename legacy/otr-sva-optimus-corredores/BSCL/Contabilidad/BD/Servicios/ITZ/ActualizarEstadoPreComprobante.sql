SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ActualizarEstadoPreComprobante";
CREATE PROCEDURE 
-- 
--  Proposito: Envio de Información a SAP
--	
--  Id Servicio = 12057
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ActualizarEstadoPreComprobante" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT,
    IN p_IdUsuario          		BIGINT,
    IN p_IdModulo           		NVARCHAR(3),
    IN p_UsaReglasNegocio			NVARCHAR(1),
    --------------------------------------------------------------------------------------------
	IN p_SecCmbte					BIGINT,
	--------------------------------------- Parametros de Salida -------------------------------------------------------------------------
	-- Parametros salida Warnings
	OUT p_out_AdvertenciasContador INT,
	OUT p_out_AdvertenciasDetalle TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador INT,
	OUT p_out_ErroresDetalle TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
 )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorhashmsg 				TABLE ("Codigo" NVARCHAR(100), "Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  				NVARCHAR(520);	        	
	DECLARE v_out_AdvertenciasDetalle	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40)) DEFAULT 
												SELECT 0 AS "IdInterno", '' AS "Codigo", 'N' AS "RequiereAutorizacion", '' AS "ValorParametro",'' AS "NombreParametro" FROM DUMMY WHERE 1=2;
	DECLARE v_out_ErroresDetalle		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40)) DEFAULT 
												SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "ValorParametro", '' AS "NombreParametro" FROM DUMMY WHERE 1=2;
	
	

	DECLARE v_CantidadRegistros INT;

	--DECLARE v_count_operaciones			BIGINT;

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_ErroresContador 
		  FROM :v_out_ErroresDetalle;
		
		SELECT COUNT(1) INTO p_out_AdvertenciasContador 
		  FROM :v_out_AdvertenciasDetalle; 	
		
		v_TablaCodigosMensajes = SELECT "Codigo" 			AS "CodigoMensaje",
								        "NombreParametro" 	AS "CodigoDato",
  								        "ValorParametro" 	AS "ValorDato" 
								   FROM :v_out_ErroresDetalle
								 UNION ALL
								 SELECT "Codigo" 			AS "CodigoMensaje",
								        "NombreParametro" 	AS "CodigoDato",
								        "ValorParametro" 	AS "ValorDato" 
								   FROM :v_out_AdvertenciasDetalle;

		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes, v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle = 	SELECT errores."IdInterno",
									   errores."Codigo",
									   msj."Descripcion"
 								  FROM :v_out_ErroresDetalle errores INNER JOIN :v_out_ErroresDetalleMensajes msj on 
 								  														errores."Codigo" = msj."Codigo";

		p_out_AdvertenciasDetalle = SELECT adv."IdInterno",
									       adv."Codigo",
 									       adv."RequiereAutorizacion",
 									       msj."Descripcion"
 								    FROM :v_out_AdvertenciasDetalle adv INNER JOIN :v_out_ErroresDetalleMensajes msj on 
 								    													adv."Codigo" = msj."Codigo";
	END;

	

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	
	
	--------------------
	--Negocio Aqui
	--------------------
	BEGIN -- proceso de negocio
		
		UPDATE "BSCL.Contabilidad.BD::ITZ.PreComprobante"
		SET "Estado" = 'C'
		WHERE "IdPreComprobante" = :p_SecCmbte;
		
		
		UPDATE "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento"
		SET "Estado" = 'C'
		WHERE "IdPreComprobante" = :p_SecCmbte;
		
		
	END;
END;
