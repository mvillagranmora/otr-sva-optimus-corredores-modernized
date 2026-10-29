SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosEliminarCuentaBancaria";


CREATE PROCEDURE
-- 
--  Proposito: Valida si una cuenta bancaria puede ser eliminada
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 10/06/2016
--     
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosEliminarCuentaBancaria"(
	-- Parametros entrada

    -- Parámetros para lógica
    IN  p_IdCuentaBanco                          BIGINT,         -- Id de la cuenta bancaria a eliminar
	
	-- Salidas para mensajes de errores	
	OUT p_out_ErroresDetalle         TABLE ("Codigo" nvarchar(100), "Descripcion" nvarchar(520)),
	OUT p_out_AdvertenciasDetalle	 TABLE ("Codigo" nvarchar(100), "RequiereAutorizacion" nvarchar(40), "Descripcion" nvarchar(5000)),
	
    OUT p_out_ResultadoRegla     NVARCHAR(40) --OK=Cumple todos los requisitos,		
											  --ADVE=Alguna de las reglas posee advertencias
                                              --RECH=Alguna de las reglas detienen la operacion


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
		v_hASh = SELECT 'FolioCartera' AS "Codigo", 0 AS "Valor" FROM DUMMY;		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hASh,:v_erroroutmsg);

		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;

								
	END; 
	
	p_out_resultadoRegla	  := 'OK';
	p_out_ErroresDetalle	   = SELECT '' AS "Codigo",'' AS "Descripcion"  FROM dummy WHERE 1=2;
	p_out_AdvertenciasDetalle  = SELECT '' AS "Codigo", '' AS "RequiereAutorizacion", '' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_RegistrosRech	       = SELECT 0 AS "Id", '' AS "Entidad" FROM dummy WHERE 1=2;
	p_out_RegistrosRaut		   = SELECT 0 AS "Id", '' AS "Entidad" FROM dummy WHERE 1=2;
	
	
	
	
	BEGIN	
		DECLARE v_error_General			BIGINT;
	   	DECLARE v_error_Negocio_ADVE	BIGINT;
		DECLARE v_TablaCodigosMensajes	TABLE ("CodigoMensaje" NVARCHAR(100),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520));

		DECLARE v_TablaRequisitos     	TABLE ("Prioridad" INT,"Cumple" BIGINT, "CodigoError" NVARCHAR(100), 
											   "CodigoDato" NVARCHAR(100), "ValorDato" NVARCHAR(520),"Id" BIGINT, "RequiereAutorizacion" NVARCHAR(1));
	
		
			
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	= SELECT '' AS "CodigoMensaje",'' AS "CodigoDato",'' AS "ValorDato" FROM dummy WHERE 1=2;	
		v_TablaRequisitos    	= SELECT  0 AS "Prioridad", 0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato", '' AS "ValorDato", 0 AS "Id", '' AS "RequiereAutorizacion" FROM dummy WHERE (1=2);


		------------------------------------------------------
		-- VALIDACIONES BASICAS
		------------------------------------------------------
		-- Valida que la cuenta bancaria a eliminar no tenga chequeras asignadas
		v_TablaRequisitos =
		SELECT 	1 AS "Prioridad",
		       	CASE 
		       		WHEN COUNT(CHE."IdChequera") > 0 THEN 0 
		       		ELSE 1 
				END AS "Cumple", 				
		       	'CUENTA_BANCARIA_CON_CHEQUERA_ASIGNADA' AS "CodigoError",
		       	'IdCuentaBanco' AS "CodigoDato",
		       	CAST(:p_IdCuentaBanco as NVARCHAR(50)) AS "ValorDato",
		       	:p_IdCuentaBanco  AS "Id",
		       	'N' AS "RequiereAutorizacion"
		FROM "BSCL.Tesoreria.BD.Vistas::ConsultarChequeras" AS CHE
		WHERE CHE."IdCuentaBanco" = :p_IdCuentaBanco		
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","Id","RequiereAutorizacion" FROM :v_TablaRequisitos;


		-- Valida que la cuenta bancaria a eliminar no esté asociada a formas de pago de movimientos
		v_TablaRequisitos =
		SELECT 	1 AS "Prioridad",
		       	CASE 
		       		WHEN COUNT(IEFP."IdCuentaBancariaCorredora") > 0 THEN 0 
		       		ELSE 1 
				END AS "Cumple", 				
		       	'CUENTA_BANCARIA_ASOCIADA_A_FORMA_DE_PAGO_MOVIMIENTO' AS "CodigoError",
		       	'IdCuentaBanco' AS "CodigoDato",
		       	CAST(:p_IdCuentaBanco as NVARCHAR(50)) AS "ValorDato",
		       	:p_IdCuentaBanco  AS "Id",	
		       	'N' AS "RequiereAutorizacion"
		FROM "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgresoFormaDePago" AS IEFP
		WHERE IEFP."IdCuentaBancariaCorredora" = :p_IdCuentaBanco		
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","Id","RequiereAutorizacion" FROM :v_TablaRequisitos;

		-- Valida que la cuenta bancaria a eliminar no esté asociada a formas de pago de comprobantes
		v_TablaRequisitos =
		SELECT 	1 AS "Prioridad",
		       	CASE 
		       		WHEN COUNT(CFP."IdCuentaBancariaCorredora") > 0 THEN 0 
		       		ELSE 1 
				END AS "Cumple", 				
		       	'CUENTA_BANCARIA_ASOCIADA_A_FORMA_DE_PAGO_COMPROBANTE' AS "CodigoError",
		       	'IdCuentaBanco' AS "CodigoDato",
		       	CAST(:p_IdCuentaBanco as NVARCHAR(50)) AS "ValorDato",
		       	:p_IdCuentaBanco  AS "Id",	
		       	'N' AS "RequiereAutorizacion"
		FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" AS CFP
		WHERE CFP."IdCuentaBancariaCorredora" = :p_IdCuentaBanco		
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","Id","RequiereAutorizacion" FROM :v_TablaRequisitos;


	   	select 'v_TablaRequisitos', * from :v_TablaRequisitos;
	   	

		-------------------------------------------------
		-- ANALISIS DE DATOS
		-------------------------------------------------

		SELECT   COUNT(CASE WHEN "Prioridad" = 1 THEN 1 END)
	   			,COUNT(CASE WHEN "Prioridad" = 2 THEN 1 END)
	   	INTO v_error_General, v_error_Negocio_ADVE
	   	FROM :v_TablaRequisitos WHERE "Cumple" = 0;
	   	
	   	select 'v_error_General', v_error_General, v_error_Negocio_ADVE from dummy;
	   	
	   	
	   	p_out_resultadoRegla := CASE WHEN :v_error_General > 0      THEN 'RECH'
	   								 WHEN :v_error_Negocio_ADVE > 0 THEN 'ADVE'
	   								 ELSE 'OK'
	   							END;
		
	    v_TablaCodigosMensajes =
		SELECT 	"CodigoError" AS "CodigoMensaje"
			   ,"CodigoDato"
			   ,"ValorDato" 
		FROM :v_TablaRequisitos
		WHERE "Cumple" = 0
		ORDER By "Prioridad" DESC;
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,p_out_ErroresDetalle);

		 
		p_out_AdvertenciasDetalle =
		SELECT ED."Codigo",	
			   TR."RequiereAutorizacion",
			   CASE WHEN "Prioridad" > 1 THEN (ED."Descripcion" || ' (' || TR."Id" || ')')
					ELSE ED."Descripcion"
			   END AS "Descripcion"
		 FROM :p_out_ErroresDetalle AS ED
		 INNER JOIN :v_TablaRequisitos AS TR ON (TR."CodigoError" = ED."Codigo")
		 WHERE TR."Cumple" = 0
		 AND   TR."Prioridad" > 1;  -- Errores de Negocio (ADVE)
		 
		
		p_out_ErroresDetalle =
		SELECT ED."Codigo",		
			   CASE WHEN "Prioridad" > 1 THEN (ED."Descripcion" || ' (' || TR."Id" || ')')
					ELSE ED."Descripcion"
			   END AS "Descripcion"
		 FROM :p_out_ErroresDetalle AS ED
		 INNER JOIN :v_TablaRequisitos AS TR ON (TR."CodigoError" = ED."Codigo")
		 WHERE TR."Cumple" = 0
		 AND   TR."Prioridad" = 1;  -- Errores generales (RECH)

		 
    END;
	
END;
