SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarParidadYTipoCambio";

-- 
--  Proposito: Consulta de Paridad y Tipo Cambio
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 10/01/2017
-- 
  
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarParidadYTipoCambio" (
	-- Parametros entrada comunes
	IN p_IdEmpresa		BIGINT,			--ID Empresa destino
	IN p_IdUsuario		BIGINT,			--ID del usuario que solicita el servicio
	
	-- Parametros entrada
	IN p_Fecha			DATE,
	
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
											"PrecioSpotPromedio" 		DECIMAL(26,8),
											"MultiplicaODivide"			NVARCHAR(1),
											"UsuarioModificador"		NVARCHAR(100)),
	
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
	DECLARE v_count_operaciones			BIGINT;
	
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
  		DECLARE v_DolarObservado	DECIMAL(26,8);
  		DECLARE v_Count				BIGINT;
   	 	
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
		
		SELECT COUNT(1) INTO v_Count FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" WHERE "IdMoneda" = 'DO' AND "Fecha" = :p_Fecha;
		IF v_Count > 0 THEN
			SELECT PARDO."TipoCambioMercado" INTO v_DolarObservado
			FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PARDO
			WHERE PARDO."IdMoneda" = 'DO'
			  AND PARDO."Fecha" = :p_Fecha;
		END IF;
		v_DolarObservado = COALESCE(v_DolarObservado,0);
		
		p_out_ParidadesYTipoCambio =
			SELECT
				COALESCE(PYT."IdParidadYTipoCambio",0)	AS "IdParidadYTipoCambio",
				MON."IdMonedaPrincipal"					AS "IdMoneda",
				MON."NombreMonedaPrincipal"				AS "NombreMoneda",
				--COALESCE(PYT."ParidadMercado",0)		AS "ParidadMercado",
				CASE WHEN MON."IdMonedaPrincipal" = 'UF' AND COALESCE(PYT."TipoCambioMercado",0) <> 0 THEN v_DolarObservado / PYT."TipoCambioMercado"
					 ELSE COALESCE(PYT."ParidadMercado",0) END AS "ParidadMercado",
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
				COALESCE(PYT."PrecioSpotPromedio",0)	AS "PrecioSpotPromedio",
				COALESCE(MON."MultiplicaODivide",'M')	AS "MultiplicaODivide",
				COALESCE(USU."CodigoUsuario",'-')		AS "UsuarioModificador"
				
				
			FROM (	SELECT "IdMonedaPrincipal", "NombreMonedaPrincipal", "MultiplicaODivide"
					FROM "BSCL.Mesa.BD.Vistas::ConsultarMoneda"
					WHERE "IdEstado" = 'ACT'
					GROUP BY "IdMonedaPrincipal", "NombreMonedaPrincipal", "MultiplicaODivide") MON
			LEFT JOIN (	SELECT *
						FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio"
						WHERE "Fecha" BETWEEN TO_SECONDDATE(:p_Fecha) AND ADD_SECONDS(TO_SECONDDATE(:p_Fecha),86399)) PYT
				ON (MON."IdMonedaPrincipal" = PYT."IdMoneda")
			LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USU ON (PYT."UsuarioModificador" = USU."IdUsuario");
				
  		
  	END;
 END;