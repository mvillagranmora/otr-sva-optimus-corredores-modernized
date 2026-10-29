SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoLiquidarSaldo";

CREATE PROCEDURE
-- 
--  Proposito: Validar si se puede liquidar un saldo de cuenta inversion de un cliente
--  Autor: JVILLAGRAN - BCS 
--  Fecha Creacion: 13/06/2016
-- 
--
"BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoLiquidarSaldo" (
    IN p_IdEmpresa						BIGINT,
    IN p_IdUsuario						BIGINT,
    IN p_IdModulo						NVARCHAR(3),
    IN p_FechaSistema              		NVARCHAR(40),
    	
    IN p_CuentaInversionALiquidar 		TABLE(	
											    "NumeroReferencia"					BIGINT,
    											"IdCuentaInversion"					BIGINT,
    											"IdPersona"    						BIGINT,   
											    "IdPortafolio"						BIGINT,
											    "IdMoneda"							NVARCHAR(3),
											    "MontoALiquidar"					DECIMAL(26,8) -- Total o Parcial
											 ),
											 										   

	-- Salidas para mensajes de errores
	OUT p_out_MovimientosRech			TABLE (
												"NumeroReferencia"			BIGINT,
												"IdPortafolio"				BIGINT,
											    "IdMoneda"					NVARCHAR(3),
											    "IdCuentaInversion"			BIGINT
										  	  ),

										   
	OUT p_out_ErroresDetalle    		TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000)),
	OUT p_out_resultadoRegla    		NVARCHAR(40)   	--OK=Cumple todos los requisitos,		
														--ADVE = 	 								  	   
                                                   		--RECH=Alguna de lAS reglAS detienen la operacion
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
BEGIN
	DECLARE SALIDA_CONTROLADA_NO_DATA CONDITION;
	DECLARE v_erroroutmsg  			NVARCHAR(5000);
	DECLARE v_ErrorNoData  			NVARCHAR(100);

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		p_out_resultadoRegla 	:= 'RECH';
		v_ErrorNoData 			:= 'DATA_NO_EXISTE';
		v_hash					 = SELECT '' as "Codigo",'' as "Valor" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
	END;
	
	p_out_resultadoRegla	:= 'OK';
	p_out_ErroresDetalle	= SELECT '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_MovimientosRech	= SELECT 0 AS "NumeroReferencia", 0 AS "IdPortafolio",'' AS "IdMoneda",0 AS "IdCuentaInversion" FROM dummy WHERE 1=2;
	
	
	BEGIN
		DECLARE v_AdvertenciasContador  INT;
		DECLARE v_ErroresContador       INT;
		DECLARE v_error_General			BIGINT;
	   	DECLARE v_error_Negocio			BIGINT;
		DECLARE v_TablaCodigosMensajes	TABLE ("CodigoMensaje" NVARCHAR(100),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(5000));
		DECLARE v_TablaRequisitos     	TABLE ("Prioridad" INT,"Cumple" BIGINT, "CodigoError" NVARCHAR(40),"CodigoDato" NVARCHAR(100),
											   "ValorDato" NVARCHAR(5000),"IdPortafolio" BIGINT,"IdMoneda" NVARCHAR(3),"IdCuentaInversion" BIGINT, "NumeroReferencia" BIGINT);
						
        DECLARE v_CuentaInversionAConsultarSaldo TABLE ("IdPortafolio"  BIGINT, "IdMoneda"   NVARCHAR(3));
						
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	= SELECT '' AS "CodigoMensaje",'' AS "CodigoDato",'' AS "ValorDato" FROM dummy WHERE 1=2;	
		v_TablaRequisitos    	= SELECT  0 AS "Prioridad", 0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato",'' AS "ValorDato",
										  0 AS "IdPortafolio",'' AS "IdMoneda",0 AS "IdCuentaInversion", 0 AS "NumeroReferencia" FROM dummy WHERE (1=2);
        v_CuentaInversionAConsultarSaldo   = SELECT  0 As "IdPortafolio", '' As "IdMoneda"   FROM dummy WHERE (1=2);
        		
		
		
		--**********************************************************************************************
		--********************************* VALIDACIONES BASICAS ***************************************
		--**********************************************************************************************
		
		-- Valida que vengan datos
		v_TablaRequisitos =
		SELECT 	 1 AS "Prioridad"
		       	,CASE WHEN COUNT(1) = 0 THEN 0 ELSE 1 END AS "Cumple"
		       	,'NO_VIENEN_MOVIMIENTOS' AS "CodigoError"
		       	,'' AS "CodigoDato"
		       	,'' AS "ValorDato"
		       	,0  AS "IdPortafolio"
				,'' AS "IdMoneda"
				,0  AS "IdCuentaInversion"				
				,0  AS "NumeroReferencia"
		 FROM :p_CuentaInversionALiquidar
		 UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdPortafolio","IdMoneda","IdCuentaInversion","NumeroReferencia" FROM :v_TablaRequisitos;

		
		
		--**********************************************************************************************
		--***************************************  NEGOCIO  ********************************************
		--**********************************************************************************************	
		
		-- Valida que la Cuenta inversión exista y esté activa
		v_TablaRequisitos =
		SELECT  2 AS "Prioridad"		
		       	,CASE WHEN COALESCE(CI."IdCuentaInversion",0) = 0 THEN 0
		       		  WHEN (CI."IdEstado" = 'INA') THEN 0
		       		  ELSE 1 
				 END AS "Cumple"				 
		       	,CASE WHEN COALESCE(CI."IdCuentaInversion",0) = 0 THEN 'CUENTA_INVERSION_NO_EXISTE'
		       		  WHEN (CI."IdEstado" = 'INA') THEN 'CUENTA_INVERSION_NO_ACTIVA'
					  ELSE  ''
				 END AS "CodigoError"				 
				,CASE WHEN COALESCE(CI."IdCuentaInversion",0) = 0 THEN 'IdCuentaInversion'
					  WHEN (CI."IdEstado") = 'INA' THEN 'IdCuentaInversion'
					  ELSE  ''
				 END AS "CodigoDato"				 
				,CASE WHEN COALESCE(CI."IdCuentaInversion",0) = 0 THEN CAST(CI."IdCuentaInversion" as NVARCHAR)
					  WHEN CI."IdEstado" = 'INA' THEN CAST(CI."IdCuentaInversion" as NVARCHAR)
					  ELSE  ''
				 END AS "ValorDato"				 
				,CIL."IdPortafolio"
				,CIL."IdMoneda"
				,CIL."IdCuentaInversion"
				,CIL."NumeroReferencia"
		FROM :p_CuentaInversionALiquidar AS CIL
		LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CI ON (CI."IdCuentaInversion" = CIL."IdCuentaInversion")
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdPortafolio","IdMoneda","IdCuentaInversion","NumeroReferencia" FROM :v_TablaRequisitos;

		

		-- Valida datos obligatorios
		v_TablaRequisitos =
		  SELECT  2 AS "Prioridad"
		       	,CASE WHEN LENGTH(COALESCE(CIL."IdMoneda",'')) = 0 THEN 0
		       		  WHEN COALESCE(MON."IdMoneda",'') = '' THEN 0		       		  
		       		  WHEN MON."IdMoneda" = 'INA' THEN 0
		       		  WHEN COALESCE(CIL."IdPersona",0) = 0 THEN 0
		       		  WHEN COALESCE(CIL."IdPortafolio",0) = 0 THEN 0
					  WHEN COALESCE(CIL."MontoALiquidar",0) = 0 THEN 0
		       		  ELSE 1 
				 END AS "Cumple"
		       	,CASE WHEN LENGTH(COALESCE(CIL."IdMoneda",'')) = 0 THEN 'MONEDA_VACIA'
		       		  WHEN COALESCE(MON."IdMoneda",'') = '' THEN 'MONEDA_NO_EXISTE'		       		  
		       		  WHEN MON."IdMoneda" = 'INA' THEN 'MONEDA_NO_ACTIVA'
		       		  WHEN COALESCE(CIL."IdPersona",0) = 0 THEN 'PERSONA_VACIO'
		       		  WHEN COALESCE(CIL."IdPortafolio",0) = 0 THEN 'PORTAFOLIO_VACIO'
					  WHEN COALESCE(CIL."MontoALiquidar",0) = 0 THEN 'MONTO_LIQUIDAR_VACIO'
					  ELSE  ''
				 END AS "CodigoError"
				,CASE WHEN LENGTH(COALESCE(CIL."IdMoneda",'')) = 0 THEN 'IdMoneda'
					  WHEN COALESCE(MON."IdMoneda",'') = '' THEN 'IdMoneda'
					  WHEN MON."IdMoneda" = 'INA' THEN 'IdMoneda'
					  WHEN COALESCE(CIL."IdPersona",0) = 0 THEN 'IdPersona'
					  WHEN COALESCE(CIL."IdPortafolio",0) = 0 THEN 'IdPortafolio'
					  WHEN COALESCE(CIL."MontoALiquidar",0) = 0 THEN 'MontoALiquidar'
					  ELSE  ''
				 END AS "CodigoDato"
				,CASE WHEN LENGTH(COALESCE(CIL."IdMoneda",'')) = 0 THEN CIL."IdMoneda"
					  WHEN COALESCE(MON."IdMoneda",'') = '' THEN CIL."IdMoneda"
					  WHEN MON."IdMoneda" = 'INA' THEN CIL."IdMoneda"
					  WHEN COALESCE(CIL."IdPersona",0) = 0 THEN CAST(CIL."IdPersona" as NVARCHAR)
					  WHEN COALESCE(CIL."IdPortafolio",0) = 0 THEN CAST(CIL."IdPortafolio" as NVARCHAR)
					  WHEN COALESCE(CIL."MontoALiquidar",0) = 0 THEN CAST(CIL."MontoALiquidar" as NVARCHAR)
					  ELSE  ''
				 END AS "ValorDato"				 
				,CIL."IdPortafolio"
				,CIL."IdMoneda"
				,CIL."IdCuentaInversion"				
				,CIL."NumeroReferencia"
		   FROM :p_CuentaInversionALiquidar as CIL
		   LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CI ON (CI."IdPortafolio" = CIL."IdPortafolio" AND CI."IdMoneda" = CIL."IdMoneda")	 
		   LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON (MON."IdMoneda" = CIL."IdMoneda")
		  UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdPortafolio","IdMoneda","IdCuentaInversion","NumeroReferencia"  FROM :v_TablaRequisitos;



		-- Validar que el saldo a liquidar no supere al saldo disponible de la cta. inv.
		INSERT INTO "BSCL.CuentaInversion.BD::CTA.SaldoCuentaInversion_IN_CuentaInversionAConsultar"
		SELECT 
			"IdPortafolio",
			null As "IdMoneda"
		FROM :p_CuentaInversionALiquidar;

		-- Consulta el saldo disponible
		CALL "BSCL.CuentaInversion.BD.Servicios::SaldoCuentaInversion"
		(
			 p_IdEmpresa          			=> :p_IdEmpresa												  
			,p_FechaDesde 					=> :p_FechaSistema
			,p_FechaHasta 					=> :p_FechaSistema
		);
		
		-- Validar que el saldo a liquidar no supere al saldo disponible de la cta. inv.
		v_TablaRequisitos =
		SELECT  2 AS "Prioridad"		
		       	,CASE WHEN (COALESCE(CIL."MontoALiquidar",0) > "SAL"."Saldo") THEN 0
		       		  ELSE 1 
				 END AS "Cumple"				 
		       	,CASE WHEN (COALESCE(CIL."MontoALiquidar",0) > "SAL"."Saldo") THEN 'SALDO_A_LIQUIDAR_SUPERA_SALDO_DISPONIBLE'
					  ELSE  ''
				 END AS "CodigoError"				 
				,CASE WHEN (COALESCE(CIL."MontoALiquidar",0) > "SAL"."Saldo") THEN 'IdCuentaInversion'
					  ELSE  ''
				 END AS "CodigoDato"				 
				,CASE WHEN (COALESCE(CIL."MontoALiquidar",0) > "SAL"."Saldo") THEN CAST(CI."IdCuentaInversion" as NVARCHAR)					  
					  ELSE  ''
				 END AS "ValorDato"
				,CIL."IdPortafolio"
				,CIL."IdMoneda"
				,CIL."IdCuentaInversion"
				,CIL."NumeroReferencia"
		FROM :p_CuentaInversionALiquidar AS CIL
		INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CI ON (CI."IdCuentaInversion" = CIL."IdCuentaInversion")
		INNER JOIN "BSCL.CuentaInversion.BD::CTA.SaldoCuentaInversion_OUT_SaldoPorFechaEfectiva" AS SAL ON (CIL."IdCuentaInversion" = SAL."IdCuentaInversion" AND CIL."IdPortafolio" = SAL."IdPortafolio")		
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdPortafolio","IdMoneda","IdCuentaInversion","NumeroReferencia" FROM :v_TablaRequisitos;



		
		--**********************************************************************************************
		--***********************************  ANALISIS DE DATOS  **************************************
		--**********************************************************************************************
		SELECT   COUNT(CASE WHEN "Prioridad" = 1 THEN 1 END)
	   			,COUNT(CASE WHEN "Prioridad" = 2 THEN 1 END)
	   			INTO v_error_General, v_error_Negocio
	   	  FROM :v_TablaRequisitos WHERE "Cumple" = 0;
	   	
	   	p_out_resultadoRegla := CASE WHEN :v_error_General > 0 THEN 'RECH'
	   								 WHEN :v_error_Negocio > 0 THEN 'ADVE'
	   								 ELSE 'OK'
	   							END;
	    	
		
		p_out_MovimientosRech =
	    SELECT DISTINCT "NumeroReferencia","IdPortafolio","IdMoneda","IdCuentaInversion"
	      FROM :v_TablaRequisitos
	     WHERE "Prioridad" > 1
	       AND "Cumple" = 0;
	    
	    
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
			   ED."Descripcion"
		  FROM :p_out_ErroresDetalle as ED
		 INNER JOIN :v_TablaRequisitos as TR ON TR."CodigoError" = ED."Codigo"
		 WHERE "Cumple" = 0;
    END;
END;
