SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.AnularPreComprobanteMultiseleccion";

CREATE PROCEDURE 
-- 
--  Proposito: Anular
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 13/10/2016
--  Id Servicio = 12043
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.AnularPreComprobanteMultiseleccion" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT,
    IN p_IdUsuario          		BIGINT,
    IN p_IdModulo           		NVARCHAR(3),
	-- Variables de Entrada variables
	IN p_PreComprobantes			TABLE ("IdPreComprobante" BIGINT),
	-- Parametros de Salida
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
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
	DECLARE v_out_AdvertenciasDetalle	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)) DEFAULT 
												SELECT 0 AS "IdInterno", '' AS "Codigo", 'N' AS "RequiereAutorizacion", '' AS "Descripcion" FROM DUMMY WHERE 1=2;
	DECLARE v_out_ErroresDetalle		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT 
												SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "Descripcion" FROM DUMMY WHERE 1=2;
	DECLARE v_count_operaciones			BIGINT;
	DECLARE v_out_AdvertenciasContador 	INT;
	DECLARE v_out_ErroresContador 	    INT;
	 
	 /* 
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
*/
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;

	--------------------
	--Negocio Aqui
	--------------------
	BEGIN -- proceso de negocio      
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		DECLARE v_TienePreComprobante		BIGINT;
		DECLARE v_TieneEvento				BIGINT;
		DECLARE v_GlosaError				NVARCHAR(200);
		DECLARE v_ArrayPreComprobante 		BIGINT ARRAY; 
		DECLARE v_Index 		INTEGER;
		DECLARE v_IdPreComprobante BIGINT := 0;
		
		v_errores = select * from :v_out_ErroresDetalle;
		
		v_ArrayPreComprobante:= ARRAY_AGG(:p_PreComprobantes."IdPreComprobante" ORDER BY "IdPreComprobante");	
		FOR v_Index IN 1 .. CARDINALITY(:v_ArrayPreComprobante) DO
		
			v_IdPreComprobante := :v_ArrayPreComprobante[:v_Index];
		
			
			
			CALL "BSCL.Contabilidad.BD.Servicios::ITZ.EliminarPreComprobante" (
	
    			p_IdEmpresa 				=> :p_IdEmpresa,
    			p_IdUsuario					=> :p_IdUsuario,
    			p_IdModulo					=> :p_IdModulo,
    			p_UsaReglasNegocio			=> 'N',
    --------------------------------------------------------------------------------------------
				p_IdPreComprobante			=>	v_IdPreComprobante,
				p_IdPreComprobanteAsiento	=>  0,
				p_IdEvento					=>  0,
				p_Origen					=>  'PR',
	--------------------------------------- Parametros de Salida -------------------------------------------------------------------------
				p_out_AdvertenciasContador 	=> :p_out_AdvertenciasContador,
				p_out_AdvertenciasDetalle 	=> :p_out_AdvertenciasDetalle,
				p_out_ErroresContador 		=> :p_out_ErroresContador,
				p_out_ErroresDetalle 		=> :p_out_ErroresDetalle
 			);
 			
 			v_errores = select * from :p_out_ErroresDetalle UNION ALL
 						select * from :v_errores;
			
			
			--concatenar errores
					
		END FOR;
		
		
		p_out_ErroresDetalle = select * from :v_errores;
	
		
	END;
END;
