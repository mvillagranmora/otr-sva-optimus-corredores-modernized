SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarParidadYTipoCambioMoneda";

-- 
--  Proposito: Consulta de Paridad y Tipo Cambio por Moneda
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 07/11/2017
-- 
  
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarParidadYTipoCambioMoneda" (
	-- Parametros entrada comunes
	IN p_IdEmpresa	BIGINT,			--ID Empresa destino
	IN p_IdUsuario	BIGINT,			--ID del usuario que solicita el servicio
	
	-- Parametros entrada
	IN p_Fecha		DATE,
	IN p_IdMoneda	NVARCHAR(3),
	
	-- Parametro salida
	OUT p_out_ParidadesYTipoCambio	TABLE (	"IdParidadYTipoCambio"		BIGINT,
											"IdMoneda" 					NVARCHAR(3),
											"NombreMoneda" 				NVARCHAR(50),
											"ParidadMercado"			DECIMAL(26,8),
											"ParidadBID"				DECIMAL(26,8),
											"ParidadASK"				DECIMAL(26,8),
											"LimiteSuperiorParidad"		DECIMAL(26,8),
											"LimiteInferiorParidad" 	DECIMAL(26,8),
											"ParidadSBIF" 				DECIMAL(26,8),
											"TipoCambioMercado"			DECIMAL(26,8),
											"LimiteSuperiorTipoCambio"	DECIMAL(26,8),
											"LimiteInferiorTipoCambio"	DECIMAL(26,8),
											"PrecioSpotCompra" 			DECIMAL(26,8),
											"PrecioSpotVenta"			DECIMAL(26,8),
											"PrecioSpotPromedio" 		DECIMAL(26,8)),
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador		INT,
	OUT p_out_ErroresDetalle 		TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
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
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(100),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE v_out_AdvertenciasDetalle	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40))
										DEFAULT SELECT 0 AS "IdInterno",'' AS "Codigo",'' AS "RequiereAutorizacion",'' AS "ValorParametro",'' AS "NombreParametro" FROM DUMMY WHERE 1=2;
	DECLARE v_out_ErroresDetalle		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40))
										DEFAULT SELECT 0  AS "IdInterno",'' AS "Codigo",'' AS "ValorParametro",'' AS "NombreParametro" FROM DUMMY WHERE 1=2;
	DECLARE v_count BIGINT;
	
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
	v_out_AdvertenciasDetalle	= SELECT 0 as "IdInterno", '' as "Codigo", 'N' as "RequiereAutorizacion", '' as "ValorParametro", '' as "NombreParametro" from dummy where 1=2;
	v_out_ErroresDetalle		= SELECT 0 as "IdInterno", '' as "Codigo", '' as "ValorParametro", '' as "NombreParametro" from dummy where 1=2;
		
	BEGIN
   	 	
  		DECLARE v_resultadoRegla	NVARCHAR(40);
   	 	
   		--------------------
		--VALIDACIONES
		--------------------
    
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,'PLA',:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		
		--------------------
		--LOGICA
		--------------------
		
		p_out_ParidadesYTipoCambio =
			SELECT
				COALESCE(PYT."IdParidadYTipoCambio",0)	AS "IdParidadYTipoCambio",
				MON."IdMoneda"							AS "IdMoneda",
				MON."Nombre"							AS "NombreMoneda",
				COALESCE(PYT."ParidadMercado",0)		AS "ParidadMercado",
				COALESCE(PYT."ParidadBID",0)			AS "ParidadBID",
				COALESCE(PYT."ParidadASK",0)			AS "ParidadASK",
				COALESCE(PYT."LimiteSuperiorParidad",0)	AS "LimiteSuperiorParidad",
				COALESCE(PYT."LimiteInferiorParidad",0)	AS "LimiteInferiorParidad",
				COALESCE(PYT."ParidadSBIF",0)			AS "ParidadSBIF",
				COALESCE(PYT."TipoCambioMercado",0)		AS "TipoCambioMercado",
				COALESCE(PYT."LimiteSuperiorTipoCambio",0)	AS "LimiteSuperiorTipoCambio",
				COALESCE(PYT."LimiteInferiorTipoCambio",0)	AS "LimiteInferiorTipoCambio",
				COALESCE(PYT."PrecioSpotCompra",0)		AS "PrecioSpotCompra",
				COALESCE(PYT."PrecioSpotVenta",0)		AS "PrecioSpotVenta",
				COALESCE(PYT."PrecioSpotPromedio",0)	AS "PrecioSpotPromedio"
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" MON
			LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PYT
				ON (MON."IdMoneda" = PYT."IdMoneda" AND
					PYT."Fecha" = :p_Fecha)
			WHERE MON."IdMoneda" = :p_IdMoneda;
		
		
		SELECT COUNT(*) INTO v_count FROM :p_out_ParidadesYTipoCambio;
		
		IF (:v_count = 0) THEN
			p_out_ParidadesYTipoCambio =
				SELECT
					0			AS "IdParidadYTipoCambio",
					:p_IdMoneda AS "IdMoneda",
					:p_IdMoneda AS "NombreMoneda",
					0			AS "ParidadMercado",
					0			AS "ParidadBID",
					0			AS "ParidadASK",
					0			AS "LimiteSuperiorParidad",
					0			AS "LimiteInferiorParidad",
					0			AS "ParidadSBIF",
					0			AS "TipoCambioMercado",
					0			AS "LimiteSuperiorTipoCambio",
					0			AS "LimiteInferiorTipoCambio",
					0			AS "PrecioSpotCompra",
					0			AS "PrecioSpotVenta",
					0			AS "PrecioSpotPromedio"
				FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" MON;
		END IF;
		
		--Salida del SP
		SELECT * FROM :p_out_ParidadesYTipoCambio;
  		
  	END;
 END;
