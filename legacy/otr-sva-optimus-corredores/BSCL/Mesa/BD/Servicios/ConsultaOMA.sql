SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::ConsultaOMA";

-- 
--  Proposito: Generar data para archivo OMA de operaciones Spot
--  Fecha Creacion: 08/12/2015
--
-- IMPSURAOPT-1407, Spot- Interfaz OMA- Estructura del archivo TXT al cargarlo en BCCh indica que tiene error
-- M.SANDOVAL, 23-JUNIO-2020
-- Redondea TC/Cierre, Mto USD a 2 decimales
-- El monto en CLP lo calcula multiplicando Mto.USD * TC/Cierre y redondeado a 2 decimales
-- 
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::ConsultaOMA" ( 
	IN p_IdEmpresa      BIGINT,
	IN p_Fecha          NVARCHAR(10),
	IN p_CodigoComercio BIGINT,
	--circular 990
	IN p_FechaHoraDesde     NVARCHAR(19) DEFAULT '',
	IN p_FechaHoraHasta     NVARCHAR(19) DEFAULT '',
	--circular 990
   	OUT p_out_encabezadoOMA table (
						 "TipoRegistro"		NVARCHAR(2),		
						 "FechaPlanilla"	NVARCHAR(10),
						 "Entidad"          NVARCHAR(3),	
						 "EspacioLibre"     NVARCHAR(200)
						 ),
	
						


	 OUT p_out_registro10_OMA table (
						 "TipoRegistro"		    NVARCHAR(2),		-- 1  TIPO REGISTRO 			9(02) 
						 "FolioPlanilla"	    NVARCHAR(6),		-- 2  NUMERO DE LA PLANILLA 	9(06) 						 
						 "DocumentoIdentidad"	NVARCHAR(10),		-- 3  RUT DEL INTERESADO 		X(10)
						 "NombreCompleto"		NVARCHAR(50),		-- 4  NOMBRE DEL INTERESADO		X(50) 
						 "TipoDoc"		        BIGINT,				-- 5  TIPO DE DOCUMENTO 		9(01) 
						 "CodigoComercio"	    NVARCHAR(6),		-- 6  CODIGO OPERACION CAMBIO 	9(05)						 
						 "Pais"		            NVARCHAR(3),		-- 7  PAIS DE LA OPERACION 		9(03)
						 "IdMonedaPrincipal"	NVARCHAR(3),		-- 8  MONEDA DE LA OPERACION 	9(03)
						 "MontoMonedaPrincipal"	NVARCHAR(14),		-- 9  MONTO OPERACION EN MONEDA ORIGEN 	9(12)V9(02)
						 "MontoDolar"	        NVARCHAR(14),		-- 10 MONTO OPERACION EN DOLARES 	9(12)V9(02)						 
						 "TipoCambio"	        NVARCHAR(14),		-- 11 TIPO DE CAMBIO 				9(06)V9(02) 
						 "MtoEquiClp"	        NVARCHAR(16),		-- 12 MONTO EQUIVALENTE EN PESOS 	9(14)V9(02) 
						 "AfectoDer"		    NVARCHAR(1),		-- 13 AFECTO A DERIVADOS 			9(01)
						 "OpZonaFra"		    NVARCHAR(1),		-- 14 OPERACION ZONA FRANCA 		9(01) 
						 "TipoAuto"		        NVARCHAR(2),		-- 15 TIPO DE AUTORIZACION 			X(02) 
						 "NumAuto"		        NVARCHAR(6),		-- 16 NUMERO DE LA AUTORIZACION		9(06) 
						 "FecAuto"		        NVARCHAR(8),		-- 17 FECHA DE LA AUTORIZACION		9(08) 
						 "CodEntidad"		    NVARCHAR(3),		-- 18 CODIGO ENTIDAD 				9(03) 
						 "FecPlanilla"          NVARCHAR(8),		-- 19 FECHA PRESENTACION PLANILLA 	9(08) 
						 "NumPlanilla"          NVARCHAR(6),		-- 20 NUMERO PRESENTACION PLANILLA	9(06) 						 
						 "DolarObservado"       NVARCHAR(12),		-- 21 DOLAR OBSERVADO
						 "EspacioLibre"     	NVARCHAR(34),       -- 22 Espacio libre para completar 200 caracteres	X(33)
						 "Glosa"				NVARCHAR(200)		-- 23 Glosa registro 60 200 caracteres
						),
						
		 OUT p_out_registro99_OMA table (
						 "TipoRegistro"		    NVARCHAR(2),		
						 "CantidadRegistros10"	NVARCHAR(6),   		-- CANTIDAD DE REGISTROS 10	   9(06)
						 "CantidadRegistros20"	NVARCHAR(6),   		-- CANTIDAD DE REGISTROS 10	   9(06)
						 "CantidadRegistros30"	NVARCHAR(6),   		-- CANTIDAD DE REGISTROS 10	   9(06)
						 "CantidadRegistros40"	NVARCHAR(6),   		-- CANTIDAD DE REGISTROS 10	   9(06)
						 "CantidadRegistros50"	NVARCHAR(6),   		-- CANTIDAD DE REGISTROS 10	   9(06)
						 "CantidadRegistros60"	NVARCHAR(6),   		-- CANTIDAD DE REGISTROS 10	   9(06)
						 "TotalRegistros"	    NVARCHAR(6),   		-- TOTAL DE REGISTROS 	       9(06)
						 "EspacioLibre"     	NVARCHAR(156)        -- 21 Espacio libre para completar 200 caracteres	X(156)
						 )					


)

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN SEQUENTIAL EXECUTION
	
	DECLARE p_operaciones  TABLE (
			
		 "FolioPlanilla"	    NVARCHAR(6),
		 "DocumentoIdentidad"	NVARCHAR(10),		 
		 "NombreCompleto"		NVARCHAR(500),
		 "Tipo"                 NVARCHAR(2),
		 "Nombres" 				NVARCHAR(500),
		 "ApellidoPaterno" 		NVARCHAR(500),
		 "ApellidoMaterno" 		NVARCHAR(500),
		 "TipoDoc"		        BIGINT,			
		 "CodigoComercio"	    NVARCHAR(6),			
		 "Pais"		            NVARCHAR(3),				
		 "IdMonedaPrincipal"	NVARCHAR(3),			
		 "MontoMonedaPrincipal"	DECIMAL(12,2),		
		 "MontoDolar"	        DECIMAL(12,2),		
		 "TipoCambio"	        DECIMAL(12,2),		
		 "MtoEquiClp"	        DECIMAL(14,2),		
		 "AfectoDer"		    BIGINT,			
		 "Registro50"  	    	BIGINT,		
		 "NroConDeri"  	    	BIGINT,
		 "Registro60"  	    	BIGINT,		
		 "Observacion"      	NVARCHAR(198),
		 "InformaReg2"      	BIGINT,	 
		 "InformaReg3"      	BIGINT,		 
		 "InformaReg4"      	BIGINT,		 
		 "InformaReg6"      	BIGINT,
		 "DolarObservado"       BIGINT
	);


	DECLARE v_Count BIGINT := 0;
	DECLARE v_CountGlosa BIGINT := 0;
	DECLARE v_CountOperaciones BIGINT := 0;
	DECLARE v_CountRegistro10 BIGINT := 0;
	DECLARE v_CountRegistro60 BIGINT := 0;	
	DECLARE v_CodigoCorredora NVARCHAR(40) := '000';
	DECLARE v_GlosaModificada NVARCHAR(1) := 'N';
	DECLARE v_NombreCompletoCliente NVARCHAR(500) := '';
	DECLARE v_NombresCliente NVARCHAR(500) := '';
	DECLARE v_ApellidoPaternoCliente NVARCHAR(500) := '';
	DECLARE v_ApellidoMaternoCliente NVARCHAR(500) := '';
	DECLARE v_NombreFinalCliente NVARCHAR(50) := '';
	
	DECLARE SALIDA_CONTROLADA CONDITION;    	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 

	BEGIN 
	END;
		
	SELECT COUNT(1) INTO v_Count
	FROM "BSCL.Mesa.BD.Vistas::ConsultarConfiguracionMesa"
	WHERE "IdEmpresa" = :p_IdEmpresa
	  AND "IdTipoProducto" = 'MEME'
	  AND "CodigoParametro" = 'CODIGOCORREDORAOMA';
	
	  --SELECT :v_Count FROM dummy;
	IF v_Count > 0 THEN
		SELECT "Valor" INTO v_CodigoCorredora
		FROM "BSCL.Mesa.BD.Vistas::ConsultarConfiguracionMesa"
		WHERE "IdEmpresa" = :p_IdEmpresa
		  AND "IdTipoProducto" = 'MEME'
		  AND "CodigoParametro" = 'CODIGOCORREDORAOMA';
	END IF;	
	
	SELECT COUNT(1) INTO v_CountGlosa
	FROM "BSCL.Mesa.BD.Vistas::ConsultarConfiguracionMesa"
	WHERE "IdEmpresa" = :p_IdEmpresa
	  AND "IdTipoProducto" = 'MEME'
	  AND "CodigoParametro" = 'OMAGLOSAMODIFICADA';
	
	IF v_CountGlosa > 0 THEN
		SELECT "Valor" INTO v_GlosaModificada
		FROM "BSCL.Mesa.BD.Vistas::ConsultarConfiguracionMesa"
		WHERE "IdEmpresa" = :p_IdEmpresa
		  AND "IdTipoProducto" = 'MEME'
		  AND "CodigoParametro" = 'OMAGLOSAMODIFICADA';
	END IF;
		
	---------------------------------------------------	
	-- OBTIENE DATOS DE OPERACIONES SPOT 
	---------------------------------------------------
		
	p_operaciones =
		SELECT			
			 SPT."FolioPlanilla"
			,Cliente."DocumentoIdentidad"			
			,Cliente."NombreCompleto"
			,Cliente."Tipo" --se refiere a tipo Persona Juridica o Persona Normal ( J , N )
			,Cliente."Nombres"
			,Cliente."ApellidoPaterno"
			,Cliente."ApellidoMaterno"
			,1 AS "TipoDoc" 
			,SPT."CodigoComercio"			
			,EMP."IdPais" AS "Pais"
			,COALESCE(MON."CodigoNumerico",'0')	AS "IdMonedaPrincipal"
			
			,Round(SPT."MontoMonedaPrincipal",2) AS "MontoMonedaPrincipal"
			,CASE WHEN SPT."IdMonedaPrincipal"  = SPT."IdMonedaEquivalente" THEN Round(SPT."MontoMonedaPrincipal", 2)
				  WHEN SPT."IdMonedaSecundaria" = SPT."IdMonedaEquivalente" THEN Round(SPT."MontoMonedaSecundaria", 2)
			      ELSE 0 
			 END AS "MontoDolar"				  
			,Round(SPT."TipoCambioCierre",2) AS "TipoCambio"
			,CASE WHEN SPT."IdMonedaSecundaria" = 'CLP' 
				  THEN Round(Round(SPT."MontoMonedaPrincipal",2) * Round(COALESCE(SPT."TipoCambioCierre",0),2),2)
				  ELSE SPT."MontoMonedaSecundaria" * COALESCE(PAR."TipoCambioMercado",0) END AS "MtoEquiClp"
			,CASE WHEN SPT."IdTipoCanalIngreso" = 'FWD' THEN 1 ELSE 
						 CASE WHEN SPT."AfectoADerivado" = 'S' THEN 1 
							  WHEN SPT."AfectoADerivado" = 'N' THEN 0 
							  ELSE 0 
					 	 END 
			 END AS "AfectoDer"
			,CASE WHEN SPT."IdTipoCanalIngreso" = 'FWD' THEN 50 ELSE 0 END AS "Registro50" -- Registro 50		
			,CASE WHEN SPT."IdTipoCanalIngreso" = 'FWD' THEN "FolioInterfaz" ELSE 0 END	AS "NroConDeri"	-- Registro 50
			,CASE WHEN "Observacion" <>'' THEN 60 ELSE 0 END AS "Registro60" -- Registro 60
			,LPAD(COALESCE("Observacion",''),198,'') AS "Observacion" -- Registro 60
			,CASE WHEN COALESCE(RegistroComercio20."Estado",'INA') = 'ACT' THEN 1 ELSE 0 END AS "InformaReg2"
			,CASE WHEN COALESCE(RegistroComercio30."Estado",'INA') = 'ACT' THEN 1 ELSE 0 END AS "InformaReg3"
			,CASE WHEN COALESCE(RegistroComercio40."Estado",'INA') = 'ACT' THEN 1 ELSE 0 END AS "InformaReg4"
			,CASE WHEN COALESCE(RegistroComercio60."Estado",'INA') = 'ACT' THEN 1 ELSE 0 END AS "InformaReg6"
			,CASE WHEN SPT."DolarObservado" = 'S' THEN 1 
				  WHEN SPT."DolarObservado" = 'N' THEN 0
				  ELSE 0 
			 END AS "DolarObservado"
		FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente 
			ON Cliente."IdCliente" = SPT."IdCliente"
		INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP
			ON (EMP."IdEmpresa" = :p_IdEmpresa)
		
		LEFT JOIN "BSCL.Mesa.BD::SPT.RegistroComercio" RegistroComercio20  -- ???
			ON RegistroComercio20."CodigoComercio" = 20 AND SPT."CodigoComercio" = RegistroComercio20."IdRegistro" 
		LEFT JOIN "BSCL.Mesa.BD::SPT.RegistroComercio" RegistroComercio30
			ON RegistroComercio30."CodigoComercio" = 30 AND SPT."CodigoComercio" = RegistroComercio30."IdRegistro"
		LEFT JOIN "BSCL.Mesa.BD::SPT.RegistroComercio" RegistroComercio40
			ON RegistroComercio40."CodigoComercio" = 40 AND SPT."CodigoComercio" = RegistroComercio40."IdRegistro"
		LEFT JOIN "BSCL.Mesa.BD::SPT.RegistroComercio" RegistroComercio60
			ON RegistroComercio60."CodigoComercio" = 60 AND SPT."CodigoComercio" = RegistroComercio60."IdRegistro"			
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MON   ON (SPT."IdMonedaPrincipal" = MON."IdMoneda")	
		
		LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR
			ON (SPT."IdMonedaPrincipal" = PAR."IdMoneda" AND SPT."FechaOperacion" = PAR."Fecha")
		WHERE SPT."OperacionFicticia" = 'N'
		--  AND TO_VARCHAR(SPT."FechaOperacion",'YYYY-MM-DD') = :p_Fecha
		  --circular 990
		AND (( :p_FechaHoraDesde <> '' AND :p_FechaHoraHasta <> '' AND (SPT."FechaOperacion" BETWEEN (LOCALTOUTC (TO_TIMESTAMP(:p_FechaHoraDesde, 'YYYY-MM-DD HH24:MI:SS'), 'America/Santiago','platform')) AND (LOCALTOUTC (TO_TIMESTAMP(:p_FechaHoraHasta, 'YYYY-MM-DD HH24:MI:SS'), 'America/Santiago','platform')))) OR (TO_VARCHAR(SPT."FechaOperacion",'YYYY-MM-DD') = :p_Fecha AND :p_FechaHoraDesde = '' AND :p_FechaHoraHasta = ''))
		  --CIRCULAR 990
		  AND SPT."Estado" = 'VIG';
	

	
	----------------------------------------------------
	-- PREPARANDO SALIDA REGISTRO ENCABEZADO
	----------------------------------------------------
	p_out_encabezadoOMA =
		SELECT
			 '00' as "TipoRegistro"								-- 1 TIPO REGISTRO 		 	9(02)
			,TO_CHAR(p_Fecha,'yyyyMMdd') as "FechaPlanilla"		-- 2 FECHA DE LA PLANILLA 	9(08)
			,COALESCE(:v_CodigoCorredora,'000') AS "Entidad"  	-- 3 ENTIDAD QUE PRESENTA 	9(03)    
			,LPAD('',187,' ') as "EspacioLibre"					-- 4 Espacio libre para completar 200 caracteres	X(187)
		FROM DUMMY;
		
		
	----------------------------------------------------
	-- PREPARANDO SALIDA REGISTRO 10
	----------------------------------------------------
	p_out_registro10_OMA =
		SELECT
			 '10' AS "TipoRegistro" 									-- 1  TIPO REGISTRO 			9(02) 
			,LPAD("FolioPlanilla",6,0) 		AS "FolioPlanilla"			-- 2  NUMERO DE LA PLANILLA 	9(06)
			,LPAD(REPLACE("DocumentoIdentidad",'-',''),10,0) AS "DocumentoIdentidad"	-- 3  RUT DEL INTERESADO 		X(10)
			,CASE -- 4  NOMBRE DEL INTERESADO		X(50)
				--Caso cuando la persona es Juridica ("Tipo"='J')
				WHEN ("Tipo"='J') THEN RPAD("NombreCompleto",50,' ') 
				--Caso cuando la persona es Natural ("Tipo"='N')
				WHEN ("Tipo"='N' AND (LENGTH(CONCAT("ApellidoPaterno","ApellidoMaterno")) <= 46)) THEN RPAD(CONCAT(CONCAT(CONCAT(CONCAT(SUBSTRING("Nombres",1,(50 - LENGTH(CONCAT("ApellidoPaterno","ApellidoMaterno")) - 2)),' '),"ApellidoPaterno"), ' '),"ApellidoMaterno"),50,' ')
				WHEN ("Tipo"='N' AND (LENGTH(CONCAT("ApellidoPaterno","ApellidoMaterno")) > 46)) THEN RPAD(SUBSTRING(CONCAT("ApellidoPaterno","ApellidoMaterno"),1,50),50,' ')
			END AS "NombreCompleto"
			,"TipoDoc" 													-- 5  TIPO DE DOCUMENTO 		9(01)			
			,LPAD("CodigoComercio",6,0)		AS "CodigoComercio"			-- 6  CODIGO OPERACION CAMBIO 	9(05)
			,LPAD(CASE WHEN "Pais" = 44 THEN 997 ELSE "Pais" END ,3,0)	AS "Pais"	-- 7  PAIS DE LA OPERACION 		9(03)
			,LPAD("IdMonedaPrincipal",3,0)	AS "IdMonedaPrincipal"		-- 8  MONEDA DE LA OPERACION 	9(03)
			
			,LPAD(to_decimal("MontoMonedaPrincipal",23,0), 12, 0) || RPAD(REPLACE(MOD("MontoMonedaPrincipal", 1),'0.',''), 2, 0) AS "MontoMonedaPrincipal"   -- 10 MONTO OPERACION EN MONEDA DE ORIGEN 	9(12)V9(02)
			,LPAD(to_decimal("MontoDolar",23,0), 12, 0) || RPAD(REPLACE(MOD("MontoDolar", 1),'0.',''), 2, 0) AS "MontoDolar"    -- 11 MONTO OPERACION EN DOLARES 	9(12)V9(02)
			,LPAD(to_decimal("TipoCambio",23,0), 6, 0)  || RPAD(REPLACE(MOD("TipoCambio", 1),'0.',''), 2, 0) AS "TipoCambio"    -- 12 TIPO DE CAMBIO 	9(06)V9(02) 	
			,LPAD(to_decimal("MtoEquiClp",23,0), 14, 0) || RPAD(REPLACE(MOD("MtoEquiClp", 1),'0.',''), 2, 0) AS "MtoEquiClp"	-- 13 MONTO EQUIVALENTE EN PESOS 	9(14)V9(02) 
			
			,"AfectoDer"												-- 13 AFECTO A DERIVADOS 			9(01)			
			,0 							AS "OpZonaFra"					-- 14 OPERACION ZONA FRANCA 		9(01) 
			,RPAD('', 2, ' ')			AS "TipoAuto"   				-- 15 TIPO DE AUTORIZACION 			X(02) 
			,LPAD('', 6, 0) 			AS "NumAuto"    				-- 16 NUMERO DE LA AUTORIZACION		9(06)
			,LPAD('', 8, 0) 			AS "FecAuto"					-- 17 FECHA DE LA AUTORIZACION		9(08)			
			,LPAD('', 3, 0) 			AS "CodEntidad"					-- 18 CODIGO ENTIDAD 				9(03) 
			,LPAD('', 8, 0) 			AS "FecPlanilla"				-- 19 FECHA PRESENTACION PLANILLA 	9(08)
			,LPAD('', 6, 0) 			AS "NumPlanilla"				-- 20 NUMERO PRESENTACION PLANILLA	9(06) 
			,"DolarObservado"											-- 21 DOLAR OBSERVADO
			,RPAD('',32,' ') 			AS "EspacioLibre"				-- 22 Espacio libre para completar 200 caracteres	X(32)				
			,CASE WHEN "MontoMonedaPrincipal" >= 10000 AND "Observacion" <> '' AND :v_GlosaModificada <> 'S' THEN CONCAT('60', "Observacion")
				  WHEN "MontoMonedaPrincipal" >= 10000 AND :v_GlosaModificada = 'S' THEN '60OPERACION10000USD'
				  ELSE '' END AS "Glosa" 								-- 23 Glosa registro 60		
		FROM :p_operaciones;

		
		
	----------------------------------------------------
	-- PREPARANDO SALIDA REGISTRO 99
	----------------------------------------------------
	SELECT Count(1) INTO v_CountOperaciones FROM :p_operaciones;
	SELECT Count(1) INTO v_CountRegistro10 FROM :p_out_registro10_OMA;
	SELECT Count(1) INTO v_CountRegistro60 FROM :p_operaciones WHERE "MontoMonedaPrincipal" >= 10000;
	
	IF v_CountOperaciones > 0 THEN
		p_out_registro99_OMA =
			SELECT
				 '99' as "TipoRegistro"											-- TIPO REGISTRO 		 	   9(02)			
				 ,LPAD(:v_CountRegistro10, 6, 0)  	AS  "CantidadRegistros10"	-- CANTIDAD DE REGISTROS 10	   9(06)		
				 ,LPAD('', 6, 0)  					AS  "CantidadRegistros20"	-- CANTIDAD DE REGISTROS 20	   9(06)
				 ,LPAD('', 6, 0)  					AS  "CantidadRegistros30"	-- CANTIDAD DE REGISTROS 30	   9(06)
				 ,LPAD('', 6, 0)  					AS  "CantidadRegistros40"	-- CANTIDAD DE REGISTROS 40	   9(06)
				 ,LPAD('', 6, 0)  					AS  "CantidadRegistros50"	-- CANTIDAD DE REGISTROS 50	   9(06)
				 ,LPAD(:v_CountRegistro60, 6, 0)  					AS  "CantidadRegistros60"	-- CANTIDAD DE REGISTROS 60	   9(06)			 
				 ,LPAD(:v_CountRegistro10 + :v_CountRegistro60, 6, 0)  	AS  "TotalRegistros"		-- TOTAL DE REGISTROS 	       9(06)			 
				,LPAD('',156,' ') as "EspacioLibre"								-- Espacio libre para completar 200 caracteres	X(156)
			FROM DUMMY;
	END IF;

		
		
	SIGNAL SALIDA_CONTROLADA;
	
END;
