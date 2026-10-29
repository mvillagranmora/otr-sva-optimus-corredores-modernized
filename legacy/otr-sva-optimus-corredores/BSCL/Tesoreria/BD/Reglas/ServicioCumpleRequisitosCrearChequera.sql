SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearChequera";

CREATE PROCEDURE
-- 
--  Proposito: Validaciones al crear una chequera
--  Autor: CESPINOSA - BCS
--  Fecha Creacion: 13/05/2016
--     
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearChequera"(
	-- Parametros entrada
	IN p_simularOperacion 			NVARCHAR(1),
	IN p_IdCuentaBanco 				BIGINT,
	
	OUT p_out_CuentaBancoRech		TABLE ("IdCuentaBanco" BIGINT),
	
	-- Salidas para mensajes de errores
	OUT p_out_ErroresDetalle        TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(520)),
	OUT p_out_resultadoRegla        NVARCHAR(40) --OK   = Cumple todos los requisitos,		
												 --ADVE = Advertencia
                                         		 --RECH = Alguna de las reglas detienen la operacion
)
LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE SALIDA_CONTROLADA CONDITION; 	
	DECLARE SALIDA_CONTROLADA_NO_DATA CONDITION; 
	DECLARE v_erroroutmsg  			NVARCHAR(520);
	DECLARE v_ErrorNoData  			NVARCHAR(100); 
	DECLARE v_resultadoRegla       	NVARCHAR(40);
	DECLARE v_ErroresContador 		INT; 
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA     
	BEGIN
		p_out_resultadoRegla	:= 'RECH';	
		v_hash = SELECT '' AS "Codigo", 0 AS "Valor" FROM DUMMY;
		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData, :v_hash, :v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
	END; 
	
	p_out_resultadoRegla	:= 'OK';
	p_out_ErroresDetalle	= SELECT '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_CuentaBancoRech	= SELECT '' AS "IdCuentaBanco" FROM dummy WHERE 1=2;
	
	BEGIN
		DECLARE v_error_General			BIGINT;
	   	DECLARE v_error_Negocio			BIGINT;
	   	DECLARE v_TablaCodigosMensajes	TABLE ("CodigoMensaje" NVARCHAR(100),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520));
	   	
	   	DECLARE v_TablaRequisitos     	TABLE (   	"Prioridad" 		INT
													,"Cumple" 			BIGINT
													,"CodigoError" 		NVARCHAR(100)
												    ,"CodigoDato" 		NVARCHAR(100)
												    ,"ValorDato" 		NVARCHAR(520)
												    ,"IdCuentaBanco"	BIGINT
											  );
											  
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299  -- No se han encontrado datos
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
		
		v_TablaCodigosMensajes	  = SELECT '' AS "CodigoMensaje",'' AS "CodigoDato",'' AS "ValorDato" FROM dummy WHERE 1=2;
		v_TablaRequisitos    	  = SELECT  0 AS "Prioridad", 0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato", '' AS "ValorDato", 0 AS "IdCuentaBanco" FROM dummy WHERE (1=2);		
		
		---------------------------------------------------------------
		-- VALIDACIONES BASICAS
		---------------------------------------------------------------
		--Validar si existe Cuenta Bancaria
		v_TablaRequisitos =
		SELECT 	1 AS "Prioridad",	
		       	CASE 
		       		WHEN COUNT("IdCuentaBanco") < 1 THEN 0 
		       		ELSE 1 
				END AS "Cumple", 
		       	'CUENTA_BANCO_NO_EXISTE' AS "CodigoError",
		       	'' AS "CodigoDato",
		       	'' AS "ValorDato"
		       	,p_IdCuentaBanco AS "IdCuentaBanco"
		FROM "BSCL.Tesoreria.BD::TES.CuentaBanco"
		WHERE "IdCuentaBanco" = p_IdCuentaBanco
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdCuentaBanco" FROM :v_TablaRequisitos;
		
		---------------------------------------------------------------
		-- ANALISIS DE DATOS
		---------------------------------------------------------------
		SELECT   COUNT(CASE WHEN "Prioridad" = 1 THEN 1 END)
	   			,COUNT(CASE WHEN "Prioridad" > 1 THEN 1 END)
	   	INTO v_error_General, v_error_Negocio
	   	FROM :v_TablaRequisitos WHERE "Cumple" = 0;
		
		p_out_resultadoRegla := CASE WHEN :v_error_General > 0 THEN 'RECH'
									 WHEN :v_error_Negocio > 0 THEN 'RECH' -- 'ADVE'
	   								 ELSE 'OK'
	   							END;
	   							
	   	p_out_CuentaBancoRech	= 					
			   	SELECT DISTINCT TR."IdCuentaBanco"
				 FROM :v_TablaRequisitos as TR
				 WHERE "Prioridad" > 1
				 AND "Cumple" = 0;
				 
		-- Asigna códigos de mensajes de errores
	    v_TablaCodigosMensajes =
				SELECT 	"CodigoError" AS "CodigoMensaje"
					   ,"CodigoDato"
					   ,"ValorDato" 
				FROM :v_TablaRequisitos
				WHERE "Cumple" = 0
				ORDER By "Prioridad" DESC;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,p_out_ErroresDetalle);
		
		p_out_ErroresDetalle =
				SELECT ED."Codigo",
					   ED."Descripcion" as "Descripcion"
				FROM :p_out_ErroresDetalle as ED
				INNER JOIN :v_TablaRequisitos as TR ON TR."CodigoError" = ED."Codigo"
				WHERE "Cumple" = 0;
		
	END;
	
END;
