SET SCHEMA "BSCL";
GO 
DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosEditarCuentaBancaria";
GO
CREATE PROCEDURE
-- 
--  Proposito: Validar que los dato de la cuenta bancaria de la corredora cumplen con los requisitos
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 12/05/2016
--      	
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosEditarCuentaBancaria"(
 
   IN  p_Accion								   NVARCHAR(1),     -- C: Crear  M:Modificar
	-- Parametros entrada
   IN  p_TablaCuentasBancarias             		TABLE
   (
       "IdCuentaBancaria"  						BIGINT,         -- Id Temporal(1,2..)
	   "IdBanco"  								BIGINT,
	   "NombreCuentaBancaria"  					NVARCHAR(20),
	   "IdTipoCuentaBancaria"  					NVARCHAR(4),
	   "IdMoneda"  								NVARCHAR(3),
	   "NumeroCuentaBancaria"   				NVARCHAR(100),
	   "Sucursal"								NVARCHAR(100),
	   "Direccion"								NVARCHAR(100),

	   "IdPlaza"  								BIGINT,
	   "Contacto"								NVARCHAR(100),
	   "TelefonoContacto"			 			NVARCHAR(100),
	   "EmailContacto"				 			NVARCHAR(100),
	   "SaldoInicial"				            DECIMAL(26,8),
	   "AfectaPosicionInicial"                  NVARCHAR(1),    -- S:Si   N:Especial
	   "GeneraNomina"                          NVARCHAR(1),  
	   "GeneraTransferencia"                    NVARCHAR(1),  
	   "IdEstado"								NVARCHAR(3)
   ),
   
   OUT p_out_RegistrosRech	 TABLE ("IdCuentaBancaria"      BIGINT),
	
   -- Salidas para mensajes de errores
   OUT p_out_ErroresDetalle    TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000)),
   OUT p_out_resultadoRegla    NVARCHAR(40)   	--OK=Cumple todos los requisitos,		
												--ADVE = 	 								  	   
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
	
	p_out_resultadoRegla	:= 'OK';
	p_out_ErroresDetalle	= SELECT '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_MovimientosRech	= SELECT 0 AS "Id", '' AS "Entidad", '' AS "BloquearDesbloquear" FROM dummy WHERE 1=2;
	
	BEGIN	
		DECLARE v_error_General			BIGINT;
	   	DECLARE v_error_Negocio			BIGINT;
		DECLARE v_TablaCodigosMensajes	TABLE ("CodigoMensaje" NVARCHAR(100),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(5000));

		DECLARE v_TablaRequisitos     	TABLE ("Prioridad" INT,"Cumple" BIGINT, "CodigoError" NVARCHAR(40),
											   "CodigoDato" NVARCHAR(100), "ValorDato" NVARCHAR(520),"IdCuentaBancaria" BIGINT);
		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	= SELECT '' AS "CodigoMensaje",'' AS "CodigoDato",'' AS "ValorDato" FROM dummy WHERE 1=2;	
		v_TablaRequisitos    	= SELECT  0 AS "Prioridad", 0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato",'' AS "ValorDato", 0 as "IdCuentaBancaria" FROM dummy WHERE (1=2);


		--------------------------------------------
		-- VALIDACIONES DE NEGOCIO
		-------------------------------------------- 	

		-- Valida que exista la cuenta bancaria a editar
		v_TablaRequisitos = 
		SELECT 2 AS "Prioridad",
				CASE
					 WHEN (:p_Accion = 'M') AND (COALESCE(CTA."IdCuentaBanco",0) = 0 OR TCB."IdCuentaBancaria" = 0) THEN 0
					 ELSE  1
				END AS "Cumple",				
				CASE
					 WHEN (:p_Accion = 'M') AND (COALESCE(CTA."IdCuentaBanco",0) = 0 OR TCB."IdCuentaBancaria" = 0)  THEN 'CUENTA_BANCARIA_NO_VALIDA'
					 ELSE  ''
				END AS "CodigoError"
				,'IdCuentaBancaria' AS "CodigoDato"
				,CAST(COALESCE(TCB."IdCuentaBancaria",0) as NVARCHAR(50)) AS "ValorDato"
				,COALESCE(TCB."IdCuentaBancaria",0) as "IdCuentaBancaria"
		FROM :p_TablaCuentasBancarias AS TCB
		LEFT JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CTA ON (TCB."IdCuentaBancaria" = CTA."IdCuentaBanco")	
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdCuentaBancaria" FROM :v_TablaRequisitos;


		-- Valida datos obligatorios
		v_TablaRequisitos = 
		SELECT  2 AS "Prioridad",		
				CASE
					 WHEN (COALESCE(TCB."IdTipoCuentaBancaria",'') = '') THEN 0
					 WHEN (COALESCE(TCB."NumeroCuentaBancaria",'') = '') THEN 0
					 WHEN (COALESCE(TCB."NombreCuentaBancaria",'') = '') THEN 0
					 ELSE  1
				END AS "Cumple",				
				CASE 
					 WHEN (COALESCE(TCB."IdTipoCuentaBancaria",'') = '') THEN 'TIPO_CUENTA_BANCARIA_VACIA'
					 WHEN (COALESCE(TCB."NumeroCuentaBancaria",'') = '') THEN 'NUMERO_CUENTA_BANCARIA_VACIA'
					 WHEN (COALESCE(TCB."NombreCuentaBancaria",'') = '') THEN 'NOMBRE_CUENTA_BANCARIA_VACIA'
					 ELSE  ''
				END AS "CodigoError",
				CASE 
					 WHEN (COALESCE(TCB."IdTipoCuentaBancaria",'') = '') THEN 'IdTipoCuentaBancaria'
					 WHEN (COALESCE(TCB."NumeroCuentaBancaria",'') = '') THEN 'NumeroCuentaBancaria'
					 WHEN (COALESCE(TCB."NombreCuentaBancaria",'') = '') THEN 'NombreCuentaBancaria'
					 ELSE  ''
				END AS "CodigoDato",
				CASE 
					 WHEN (COALESCE(TCB."IdTipoCuentaBancaria",'') = '') THEN TCB."IdTipoCuentaBancaria"
					 WHEN (COALESCE(TCB."NumeroCuentaBancaria",'') = '') THEN TCB."NumeroCuentaBancaria"
					 WHEN (COALESCE(TCB."NombreCuentaBancaria",'') = '') THEN TCB."NombreCuentaBancaria"
					 ELSE  ''
				END AS "ValorDato",
				TCB."IdCuentaBancaria"
		FROM :p_TablaCuentasBancarias AS TCB
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON (TCB."IdMoneda" = MON."IdMoneda")		
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdCuentaBancaria" FROM :v_TablaRequisitos;
		



			
		-- Valida que la moneda exista y esté activa
		v_TablaRequisitos = 
		SELECT  2 AS "Prioridad",		
				CASE
					 WHEN (COALESCE(MON."IdMoneda",'') = '') THEN 0
					 WHEN (COALESCE(MON."IdEstado",'') = 'INA') THEN 0
					 ELSE  1
				END AS "Cumple",
				
				CASE
					 WHEN (COALESCE(MON."IdMoneda",'') = '')    THEN 'MONEDA_NO_EXISTE'
					 WHEN (COALESCE(MON."IdEstado",'') = 'INA') THEN 'MONEDA_NO_ACTIVA'
					 ELSE  ''
				END AS "CodigoError"
				,'IdMoneda' AS "CodigoDato"				
				,CAST(TCB."IdMoneda" as NVARCHAR(50)) AS "ValorDato"				
				,TCB."IdCuentaBancaria"
		FROM :p_TablaCuentasBancarias AS TCB
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON (TCB."IdMoneda" = MON."IdMoneda")		
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdCuentaBancaria" FROM :v_TablaRequisitos;
		


		-- Valida que el banco exista y que esté configurado con su contraparte
		v_TablaRequisitos = 
		SELECT 2 AS "Prioridad",
				CASE
					 WHEN (COALESCE(BAN."IdBanco",0) = 0) THEN 0
					 WHEN (COALESCE(BAN."IdPersona",0) = 0) THEN 0
					 ELSE  1
				END AS "Cumple",
				CASE
					 WHEN (COALESCE(BAN."IdBanco",0) = 0)  THEN 'BANCO_NO_EXISTE'
					 WHEN (COALESCE(BAN."IdPersona",0) = 0) THEN 'BANCO_CUENTA_CORRIENTE_SIN_CONFIGURACION'
					 ELSE  ''
				END AS "CodigoError"
				,'IdBanco' AS "CodigoDato"
				,CAST(TCB."IdBanco" as NVARCHAR(50)) AS "ValorDato"
				,TCB."IdCuentaBancaria"
		FROM :p_TablaCuentasBancarias AS TCB
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco" AS BAN ON (TCB."IdBanco" = BAN."IdBanco")
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdCuentaBancaria" FROM :v_TablaRequisitos;



		-- Valida que no exista una cuenta bancaria igual a la que se está creando
		v_TablaRequisitos = 
		SELECT 2 AS "Prioridad",
				CASE
					 WHEN (COUNT(CTA."IdCuentaBanco") > 0) THEN 0
					 ELSE  1
				END AS "Cumple",				
				CASE
					 WHEN (COUNT(CTA."IdCuentaBanco") > 0)  THEN 'CUENTA_BANCARIA_EXISTE'
					 ELSE  ''
				END AS "CodigoError"				
				,'IdCuentaBancaria' AS "CodigoDato"
				,CAST(MAX(TCB."IdCuentaBancaria") as NVARCHAR(50)) AS "ValorDato"
				,MAX(TCB."IdCuentaBancaria") as "IdCuentaBancaria"
		FROM :p_TablaCuentasBancarias AS TCB
		LEFT JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CTA ON 
		(
			    TCB."IdBanco" = CTA."IdBanco"
			AND TCB."IdTipoCuentaBancaria" = CTA."IdTipoCuentaBancaria"
			AND TCB."IdMoneda" = CTA."IdMoneda"
			AND TCB."NumeroCuentaBancaria" = CTA."NumeroCuentaBancaria"
		)	
		AND :p_Accion = 'C' -- C: crear
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdCuentaBancaria" FROM :v_TablaRequisitos;
		
		
		-- Valida que no exista una cuenta bancaria igual distinta a la que se está modificando
		v_TablaRequisitos = 
		SELECT 2 AS "Prioridad",
				CASE
					 WHEN (COUNT(CTA."IdCuentaBanco") > 0) THEN 0
					 ELSE  1
				END AS "Cumple",				
				CASE
					 WHEN (COUNT(CTA."IdCuentaBanco") > 0)  THEN 'CUENTA_BANCARIA_EXISTE'
					 ELSE  ''
				END AS "CodigoError"				
				,'IdCuentaBancaria' AS "CodigoDato"
				,CAST(MAX(TCB."IdCuentaBancaria") as NVARCHAR(50)) AS "ValorDato"
				,MAX(TCB."IdCuentaBancaria") as "IdCuentaBancaria"
		FROM :p_TablaCuentasBancarias AS TCB
		LEFT JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CTA ON 
		(
			    TCB."IdBanco" = CTA."IdBanco"
			AND TCB."IdTipoCuentaBancaria" = CTA."IdTipoCuentaBancaria"
			AND TCB."IdMoneda" = CTA."IdMoneda"
			AND TCB."NumeroCuentaBancaria" = CTA."NumeroCuentaBancaria"
			AND TCB."IdCuentaBancaria" != CTA."IdCuentaBanco"
		)	
		AND :p_Accion = 'M' -- M: modificar
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdCuentaBancaria" FROM :v_TablaRequisitos;


		-- select 'v_TablaRequisitos', * from :v_TablaRequisitos;
				
		--------------------------------------------
		-- ANALISIS DE DATOS
		--------------------------------------------

		SELECT   COUNT(CASE WHEN "Prioridad" = 1 THEN 1 END)
	   			,COUNT(CASE WHEN "Prioridad" > 1 THEN 1 END)
	   			INTO v_error_General, v_error_Negocio
	   	FROM :v_TablaRequisitos WHERE "Cumple" = 0;
	   	
	   	
	   	p_out_resultadoRegla := CASE WHEN :v_error_General > 0 THEN 'RECH'
	   								 WHEN :v_error_Negocio > 0 THEN 'RECH'
	   								 ELSE 'OK'
	   							END;
	    
	    
	    v_TablaCodigosMensajes =
		SELECT 	"CodigoError" AS "CodigoMensaje"
			   ,"CodigoDato"
			   ,"ValorDato" 
		FROM :v_TablaRequisitos
		WHERE "Cumple" = 0
		ORDER By "Prioridad" DESC;
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes, p_out_ErroresDetalle);


		p_out_RegistrosRech = 
	    SELECT "IdCuentaBancaria"
	    FROM :v_TablaRequisitos
	    WHERE "Prioridad" > 1
	    AND "Cumple" = 0; 
		
		select 'p_out_RegistrosRech', * from :p_out_RegistrosRech;
		
		p_out_ErroresDetalle =
		SELECT ED."Codigo",
			   CASE WHEN "Prioridad" > 1 THEN (ED."Descripcion" || ' (' || TR."ValorDato" || ')')
					ELSE ED."Descripcion"
				END as "Descripcion"
		  FROM :p_out_ErroresDetalle as ED
		 INNER JOIN :v_TablaRequisitos as TR ON TR."CodigoError" = ED."Codigo"
		 
		 WHERE "Cumple" = 0;
    END;	
END;