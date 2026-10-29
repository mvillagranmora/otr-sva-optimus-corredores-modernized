SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.InformeResultadoItemDig";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.InformeResultadoItemDig" (
	-- Parametros entrada comunes
	IN p_IdTipoInforme		BIGINT,
	IN p_IdIndice			BIGINT,
	IN p_IdConcepto			BIGINT,
	IN p_IdItem				BIGINT,
	IN p_FechaContable		DATE,
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio	  
		  
	-- Parametros entrada
	OUT p_out_CamposDigitadosSuperior TABLE(	
												"IdItem" 			BIGINT, 
												"NombreItem" 		NVARCHAR(210), 
												"IdTipoItem" 		NVARCHAR(3), 
												"MayorACeroItem" 	NVARCHAR(1),
												"MenorACeroItem" 	NVARCHAR(1), 
												"PermiteCeroItem" 	NVARCHAR(1), 
												"Monto" 			DECIMAL(26,8),
												"Saldo" 			DECIMAL(19,2)
											),
	OUT p_out_CamposDigitadosInferior TABLE(	
												"IdTipoInforme" 	BIGINT, 
												"NombreInforme" 	NVARCHAR(210), 
												"OrdenInforme" 		BIGINT, 
												"IdIndice" 			BIGINT,
												"NombreIndice" 		NVARCHAR(210), 
												"PosicionIndice" 	NVARCHAR(1), 
												"OperadorIndice" 	BIGINT,
												"FactorIndice" 		DECIMAL(26,8), 
												"IdConcepto" 		BIGINT, 
												"NombreConcepto" 	NVARCHAR(210), 
												"OperadorConcepto" BIGINT,
												"FactorConcepto" 	DECIMAL(26,8), 
												"IdItem" 			BIGINT, 
												"NombreItem" 		NVARCHAR(210),
												"IdTipoItem" 		NVARCHAR(3), 
												"MayorACeroItem" 	NVARCHAR(1), 
												"MenorACeroItem" 	NVARCHAR(1), 
												"PermiteCeroItem" 	NVARCHAR(1),
												"Monto" 			DECIMAL(26,8) 
											),	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000))DEFAULT EMPTY,	
	OUT p_out_ErroresContador		INT,
	OUT p_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))DEFAULT EMPTY
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
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN
		--------------------
		--Declare Variables
		--------------------
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
        

		
		--------------------
		--Validaciones
		--------------------
		
		
		--------------------
		--Reglas Negocio
		--------------------
		
		--------------------
		--Lógica de Negocio 
		--------------------
				
		BEGIN
		
			-----------------------------
			-- Consulta 
			-----------------------------		
			
			p_out_CamposDigitadosSuperior = 
				SELECT DISTINCT
					IT."IdItem"					AS "IdItem",
					IT."IdItemNombre"			AS "NombreItem",
					IT."IdTipoItem"				AS "IdTipoItem",
					CITC."MayorACero"			AS "MayorACeroItem",
					CITC."MenorACero"			AS "MenorACeroItem",
					CITC."PermiteCero"			AS "PermiteCeroItem",   
					COALESCE(IR."Monto"/1000, 0)		AS "Monto",
					
-- CELCONDOR-598
				SUM(CASE 
					WHEN CTB."SaldoDebeHaber" = 'D'
						THEN  COALESCE(SA."MontoDebe"/1000,0)
				 	WHEN CTB."SaldoDebeHaber" = 'H'
						THEN  COALESCE(SA."MontoHaber"/1000,0)
					WHEN H."TipoCuenta"  = 'A' AND CTB."SaldoDebeHaber" = 'S'
						THEN  	COALESCE((SA."MontoDebe" - SA."MontoHaber")/1000,0) 
					ELSE   	COALESCE((SA."MontoHaber"- SA."MontoDebe") /1000,0)
				END )							
												AS "Saldo"
					
				FROM "BSCL.Normativa.BD.Vistas::ILS.ConsultaInformeTipo" CIT
				INNER JOIN "BSCL.Normativa.BD::ILS.InformeConfiguracion" IC        
					ON CIT."IdTipoInforme" = IC."IdTipoInforme"
				INNER JOIN "BSCL.Normativa.BD.Vistas::ILS.ConsultaIndice" CI
				    ON CI."IdIndice" = IC."IdIndice"
				INNER JOIN "BSCL.Normativa.BD::ILS.IndiceConfiguracion" CIC
				    ON CI."IdIndice" = CIC."IdIndice"
				INNER JOIN "BSCL.Normativa.BD.Vistas::ILS.ConsultaConcepto" CC
				    ON CIC."IdConcepto" = CC."IdConcepto"
				INNER JOIN "BSCL.Normativa.BD.Vistas::ILS.ConsultaConceptoConfiguracion" CCC
					ON CCC."IdConcepto" = CIC."IdConcepto"
--				INNER JOIN "BSCL.Normativa.BD.Vistas::ConsultaItem" IT
				INNER JOIN "BSCL.Normativa.BD.Vistas::ILS.ConsultaItem" IT
					ON CCC."IdItem" =  IT."IdItem" AND IT."IdTipoItem"  = 'DIG'
					AND IT."Estado" = 'VIG'
				INNER JOIN "BSCL.Normativa.BD::ILS.ItemConfiguracion" CITC
					ON CITC."IdItem"  =   IT."IdItem"
					AND CITC."TipoItem" = 'D'
					AND CITC."Estado" =  'VIG' 
				LEFT OUTER JOIN "BSCL.Normativa.BD::ILS.InformeResultadoItemDig" IR
					ON IR."IdTipoInforme"  		= CIT."IdTipoInforme"
					AND CI."IdIndice"			= IR."IdIndice"
					AND CC."IdConcepto"			= IR."IdConcepto"
					AND IT."IdItem"				= IR."IdItem"
					AND IR."OperadorIndice"		= CASE WHEN CIC."Operacion" = '+' THEN 1 ELSE -1 END
					AND IR."FactorIndice"		= CIC."Factor"
					AND IR."OperacionConcepto"	= CASE WHEN CCC."Operacion" = '+' THEN 1 ELSE -1 END
					AND IR."FactorConcepto"		= CCC."Factor"
					AND IR."FechaContable"		= :p_FechaContable 
					AND IR."IdEmpresa"			= :p_IdEmpresa
					
-- CELCONDOR-598

			LEFT JOIN "BSCL.Normativa.BD::ILS.ItemConfiguracion" CTB
			ON CITC."IdItem" = CTB."IdItem"
			AND CTB."TipoItem" = 'C'
			
			LEFT JOIN "BSCL.Normativa.BD::ILS.p_SaldoCuentaContable" SA
			ON (SA."IdCodigoContabilidad" = CTB."CuentaContable")
		    
			LEFT  JOIN "BSCL.Contabilidad.BD::ITZ.HomologaCuenta" AS H
		    ON (H."CuentaContabilidad" = SA."IdCodigoContabilidad")
		    
-- CELCONDOR-598					
				WHERE	CIT."IdTipoInforme"	= :p_IdTipoInforme 
				AND 	CIT."IdEmpresa"		= :p_IdEmpresa
				GROUP BY IT."IdItem",
						 IT."IdItemNombre",
						 IT."IdTipoItem",
						 CITC."MayorACero",
						 CITC."MenorACero",
						 CITC."PermiteCero",
						 IR."Monto";
							
			
			
			p_out_CamposDigitadosInferior = 
				SELECT 
					CIT."IdTipoInforme"																	AS "IdTipoInforme",
					CONCAT(TRIM(TO_NVARCHAR(CIT."IdTipoInforme")), CONCAT(' - ', TRIM(CIT."Nombre")))	AS "NombreInforme",
					IC."Orden"																			AS "OrdenInforme",
					CI."IdIndice"																		AS "IdIndice",
					CONCAT(TRIM(TO_NVARCHAR(CI."IdIndice")), CONCAT(' - ', TRIM(CI."Nombre"))) 			AS "NombreIndice", 
					CIC."Posicion"																		AS "PosicionIndice",
					CASE CIC."Operacion"
						WHEN '+' THEN +1
						WHEN '-' THEN -1
						ELSE	0
					END																					AS "OperadorIndice",
					CIC."Factor"																		AS "FactorIndice",
					CC."IdConcepto"																		AS "IdConcepto",
					CC."IdConceptoNombre"																AS "NombreConcepto",
					CASE CCC."Operacion" 
						WHEN '+' THEN +1
						WHEN '-' THEN -1
						ELSE	0
					END																					AS "OperadorConcepto",
					CCC."Factor"																		AS "FactorConcepto",
					IT."IdItem"																			AS "IdItem",
					IT."IdItemNombre"																	AS "NombreItem",
					IT."IdTipoItem"																		AS "IdTipoItem",
					CITC."MayorACero"																	AS "MayorACeroItem",
					CITC."MenorACero"																	AS "MenorACeroItem",
					CITC."PermiteCero"																	AS "PermiteCeroItem",   
					COALESCE(IR."Monto",0)																AS "Monto"
				FROM "BSCL.Normativa.BD.Vistas::ILS.ConsultaInformeTipo" CIT
				INNER JOIN "BSCL.Normativa.BD::ILS.InformeConfiguracion" IC		
					ON CIT."IdTipoInforme" = IC."IdTipoInforme"
				INNER JOIN "BSCL.Normativa.BD.Vistas::ILS.ConsultaIndice" CI
					ON CI."IdIndice" = IC."IdIndice" AND
					  (CI."IdIndice" = :p_IdIndice OR :p_IdIndice IS NULL OR :p_IdIndice =0)
				INNER JOIN "BSCL.Normativa.BD::ILS.IndiceConfiguracion" CIC
					ON CI."IdIndice" = CIC."IdIndice"
				INNER JOIN "BSCL.Normativa.BD.Vistas::ILS.ConsultaConcepto" CC
					ON CIC."IdConcepto" = CC."IdConcepto" AND  
					  (CC."IdConcepto"	= :p_IdConcepto OR :p_IdConcepto IS NULL OR :p_IdConcepto = 0)
				INNER JOIN "BSCL.Normativa.BD.Vistas::ILS.ConsultaConceptoConfiguracion" CCC
					ON CCC."IdConcepto" = CIC."IdConcepto"
--				INNER JOIN "BSCL.Normativa.BD.Vistas::ConsultaItem" IT
				INNER JOIN "BSCL.Normativa.BD.Vistas::ILS.ConsultaItem" IT
					ON CCC."IdItem" =  IT."IdItem" AND 
					   IT."Estado" 	= 'VIG'					   					
				INNER JOIN "BSCL.Normativa.BD::ILS.ItemConfiguracion" CITC
					ON CITC."IdItem"		=   IT."IdItem" AND
					   CITC."Estado"		=  'VIG' 		AND
					  (IT."IdItem" = :p_IdItem OR :p_IdItem IS NULL OR :p_IdItem = 0 )
				LEFT JOIN "BSCL.Normativa.BD::ILS.InformeResultadoItemDig" IR
					ON IR."IdTipoInforme" 		= CIT."IdTipoInforme" 									AND
					   CI."IdIndice"			= IR."IdIndice"											AND
					   CC."IdConcepto"			= IR."IdConcepto" 										AND
					   IT."IdItem"				= IR."IdItem"											AND
					   IR."OperadorIndice"		= CASE WHEN CIC."Operacion" = '+' THEN 1 ELSE -1 END	AND	
					   IR."FactorIndice"		= CIC."Factor" 											AND
					   IR."OperacionConcepto"	= CASE WHEN CCC."Operacion" = '+' THEN 1 ELSE -1 END	AND
					   IR."FactorConcepto"		= CCC."Factor"											AND
					   IR."PosicionIndice"      = CIC."Posicion"     									AND  
					   IR."FechaContable" 		= :p_FechaContable										AND
					   IR."IdEmpresa" 			= :p_IdEmpresa
				WHERE	CIT."IdTipoInforme"	= :p_IdTipoInforme 
				AND		(CI."IdIndice"		= :p_IdIndice OR :p_IdIndice IS NULL OR :p_IdIndice = 0 )	
				AND		IT."IdTipoItem"		= 'DIG'
				ORDER BY	CC."IdConcepto", IT."IdItem", IT."IdTipoItem";
								
		
			-----------------------------
			-- Elimina 
			-----------------------------
			
			-----------------------------
			-- Modifica
			-----------------------------
			
			-----------------------------
			-- Inserta 
			-----------------------------
			
						
		END;		
	END;	
END;