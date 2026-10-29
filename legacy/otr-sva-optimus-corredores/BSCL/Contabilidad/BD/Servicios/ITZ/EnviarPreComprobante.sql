SET SCHEMA BSCL;
GO
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.EnviarPreComprobante";
GO
CREATE PROCEDURE 
-- 
--  Proposito: Envio de Información a SAP
--	
--  Id Servicio = 12057
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.EnviarPreComprobante" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT,
    IN p_IdUsuario          		BIGINT,
    IN p_IdModulo           		NVARCHAR(3),
    IN p_UsaReglasNegocio			NVARCHAR(1),
    --------------------------------------------------------------------------------------------
	IN p_SecCmbte					NVARCHAR(5000),
	IN p_Destino					NVARCHAR(1),
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
		DECLARE v_Parametros 				TABLE ("NombreParametro" nvarchar(100), "Valor" nvarchar(100));
		DECLARE v_out_AdvertenciasDetallePROGRAMA table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
	    DECLARE v_out_ErroresDetallePROGRAMA table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
		
		DECLARE v_FechaContable				NVARCHAR(10);
		DECLARE v_UsaCuentasSYS				NVARCHAR(1);

    	--PARAMETROS REGLAS NEGOCIO
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		DECLARE v_IdTipoContabilidad		BIGINT;
		DECLARE v_IdDefinicionAsiento		BIGINT;
		DECLARE v_CantidadCondicionados		BIGINT;

		--*******************************
		--*VALIDACION REGLAS BASICAS *
		--*******************************
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa
		                                                                 ,:p_IdUsuario
		                                                                 ,:p_IdModulo
		                                                                 ,:v_out_ErroresDetalle_Basicos
		                                                                 ,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			v_out_ErroresDetalle = SELECT 0 	AS "IdInterno",
			                              '' 	AS "Codigo",
			                              '' 	AS "ValorParametro",
			                              '' 	AS "NombreParametro"
			                         FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		-------------------------------------------------------------------------------------------------------------------------
		-- SECCIÓN NEGOCIO ------------------------------------------------------------------------------------------------------
		-------------------------------------------------------------------------------------------------------------------------
		
		v_Parametros =
		Select 'IdEmpresa' as "NombreParametro", TO_NVARCHAR(:p_IdEmpresa) 			as "Valor" from dummy UNION ALL
		Select 'IdUsuario' as "NombreParametro", TO_NVARCHAR(:p_IdUsuario) 			as "Valor" from dummy UNION ALL
		Select 'SecCmbte'  as "NombreParametro", TO_NVARCHAR(:p_SecCmbte) 			as "Valor" from dummy UNION ALL
		Select 'Destino'   as "NombreParametro", :p_Destino 			as "Valor" from dummy;
		
		v_Programas = SELECT 0 as "IdPrograma" FROM DUMMY WHERE 1=2;
		
		
		
		
		CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma" (
		 p_IdEmpresa => :p_IdEmpresa,
		 p_IdUsuario => :p_IdUsuario,
		 p_IdModulo => 'PLA',
		 p_IdAgenda => 0,
		 p_IdTipoTarea => 'ASIENTO_SAP',
		 p_Segundos => 1,
		 p_TablaParametros => :v_Parametros,
		 p_CreaParam => 'S',
		 p_out_IdPrograma => :v_Programas,
		 p_out_AdvertenciasContador => :p_out_AdvertenciasContador,
		 p_out_AdvertenciasDetalle => :v_out_AdvertenciasDetallePROGRAMA,
		 p_out_ErroresContador => :p_out_ErroresContador,
		 p_out_ErroresDetalle => :v_out_ErroresDetallePROGRAMA
		);
		
		
	END;
END;
GO