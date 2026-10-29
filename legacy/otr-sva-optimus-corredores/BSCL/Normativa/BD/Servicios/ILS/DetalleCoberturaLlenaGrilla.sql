SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.DetalleCoberturaLlenaGrilla";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.DetalleCoberturaLlenaGrilla" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada
	IN p_IdTipoInforme		BIGINT,
	IN p_FechaContable		NVARCHAR(10),
	OUT p_tablaResultadoDetalleCobertura TABLE(
												"Titulo"					NVARCHAR(50)
												,"Categoria"				NVARCHAR(50)
												,"MontoGarantia"			BIGINT
												,"MontoBancosyEmpresas"		BIGINT
												,"FactorA"					BIGINT
												,"FactorB"					BIGINT
												,"MontoCobertura"			BIGINT
												,"MontoEntidadFinanciera" 	BIGINT
												,"Fecha"					NVARCHAR(10)
												,"IdEmpresa"				BIGINT	
								 
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
			
			p_tablaResultadoDetalleCobertura  = 
						SELECT "Titulo",
							"Categoria",
							"MontoGarantia",
							"MontoBancosyEmpresas",
							"FactorA",
							"FactorB",
							"MontoCobertura",
							"MontoEntidadFinanciera",
							"Fecha",
							"IdEmpresa"
			FROM (

---------------------------------
-- CATEGORIA A
---------------------------------
				
				SELECT  'CARTERA OBLIGACIONES'				AS "Titulo"
						, CP."Categoria" 					AS "Categoria"
						, 0									AS "MontoGarantia"
						, 0									AS "MontoBancosyEmpresas"
						, 0									AS "FactorA"
						, 0									AS "FactorB"
						, SUM(CP."MontoCobertura")			AS "MontoCobertura"
						, SUM(CP."MontoEntidadFinanciera")	AS "MontoEntidadFinanciera"
						,CP."Fecha"
						,CP."IdEmpresa"
					FROM
						(SELECT CASE 
									WHEN "Cartera" = 'Cartera Libre' 
										THEN "CatRie"
								 	ELSE "CatRiePactos"
								 END 						AS "Categoria"
								 	, 0						AS "MontoGarantia"
									, 0						AS "MontoBancosyEmpresas"
									, 0						AS "FactorA"
									, 0						AS "FactorB"
								,SUM("MontoCobertura")		AS "MontoCobertura"
								,SUM("ValorMercado")  		AS "MontoEntidadFinanciera"
								,"Fecha"
								,"IdEmpresa"
							FROM "BSCL.Mesa.BD::MRF.CoberturaPatrimonial"
						GROUP BY CASE 
									WHEN "Cartera" = 'Cartera Libre' 
										THEN "CatRie"
								 	ELSE "CatRiePactos"
								 	END
								 	,"Fecha"
								 	,"IdEmpresa"
						UNION SELECT 'A' 								AS "Categoria"
									, 0									AS "MontoGarantia"
									, 0									AS "MontoBancosyEmpresas"
									, 0									AS "FactorA"
									, 0									AS "FactorB"
									, 0									AS "MontoCobertura"
									, 0									AS "MontoEntidadFinanciera"
									,:p_FechaContable					AS "Fecha" 
									,:p_IdEmpresa						AS "IdEmpresa"
						FROM Dummy) CP
				WHERE "Categoria" ='A'
				GROUP BY  CP."Categoria",CP."Fecha",CP."IdEmpresa"
				
---------------------------------
-- CATEGORIA B
---------------------------------
				UNION ALL
				SELECT  ''				AS "Titulo"
						, CP."Categoria" 					AS "Categoria"
						, 0									AS "MontoGarantia"
						, 0									AS "MontoBancosyEmpresas"
						, 0									AS "FactorA"
						, 0									AS "FactorB"
						, SUM(CP."MontoCobertura")			AS "MontoCobertura"
						, SUM(CP."MontoEntidadFinanciera")	AS "MontoEntidadFinanciera"
						,CP."Fecha"
						,CP."IdEmpresa"
					FROM
						(SELECT CASE 
									WHEN "Cartera" = 'Cartera Libre' 
										THEN "CatRie"
								 	ELSE "CatRiePactos"
								 END AS "Categoria"
								, 0									AS "MontoGarantia"
								, 0									AS "MontoBancosyEmpresas"
								, 0									AS "FactorA"
								, 0									AS "FactorB"
								,SUM("MontoCobertura")AS "MontoCobertura"
								,SUM("ValorMercado")  AS "MontoEntidadFinanciera"
								,"Fecha"
								,"IdEmpresa"
							FROM "BSCL.Mesa.BD::MRF.CoberturaPatrimonial"
						GROUP BY CASE 
									WHEN "Cartera" = 'Cartera Libre' 
										THEN "CatRie"
								 	ELSE "CatRiePactos"
								 	END
								 	,"Fecha"
								 	,"IdEmpresa"
						UNION SELECT 'B' 								AS "Categoria"
									, 0									AS "MontoGarantia"
									, 0									AS "MontoBancosyEmpresas"
									, 0									AS "FactorA"
									, 0									AS "FactorB"
									, 0									AS "MontoCobertura"
									, 0									AS "MontoEntidadFinanciera"
									,:p_FechaContable					AS "Fecha" 
									,:p_IdEmpresa						AS "IdEmpresa"
						FROM Dummy) CP
				WHERE "Categoria" ='B'
				GROUP BY  CP."Categoria",CP."Fecha",CP."IdEmpresa"
				
---------------------------------
-- CATEGORIA C
---------------------------------
				UNION ALL
				SELECT  ''				AS "Titulo"
						, CP."Categoria" 					AS "Categoria"
						, 0									AS "MontoGarantia"
						, 0									AS "MontoBancosyEmpresas"
						, 0									AS "FactorA"
						, 0									AS "FactorB"
						, SUM(CP."MontoCobertura")			AS "MontoCobertura"
						, SUM(CP."MontoEntidadFinanciera")	AS "MontoEntidadFinanciera"
						,CP."Fecha"
						,CP."IdEmpresa"
					FROM
						(SELECT CASE 
									WHEN "Cartera" = 'Cartera Libre' 
										THEN "CatRie"
								 	ELSE "CatRiePactos"
								 END AS "Categoria"
								, 0									AS "MontoGarantia"
								, 0									AS "MontoBancosyEmpresas"
								, 0									AS "FactorA"
								, 0									AS "FactorB"
								,SUM("MontoCobertura")AS "MontoCobertura"
								,SUM("ValorMercado")  AS "MontoEntidadFinanciera"
								,"Fecha"
								,"IdEmpresa"
							FROM "BSCL.Mesa.BD::MRF.CoberturaPatrimonial"
						GROUP BY CASE 
									WHEN "Cartera" = 'Cartera Libre' 
										THEN "CatRie"
								 	ELSE "CatRiePactos"
								 	END
								 	,"Fecha"
								 	,"IdEmpresa"
						UNION SELECT 'C' 								AS "Categoria"
									, 0									AS "MontoGarantia"
									, 0									AS "MontoBancosyEmpresas"
									, 0									AS "FactorA"
									, 0									AS "FactorB"
									, 0									AS "MontoCobertura"
									, 0									AS "MontoEntidadFinanciera"
									,:p_FechaContable					AS "Fecha" 
									,:p_IdEmpresa						AS "IdEmpresa"
						FROM Dummy) CP
				WHERE "Categoria" ='C'
				GROUP BY  CP."Categoria",CP."Fecha",CP."IdEmpresa"
				
---------------------------------
-- CATEGORIA D TIPO 1
---------------------------------
				UNION ALL
				SELECT  ''				AS "Titulo"
						, CP."Categoria" 					AS "Categoria"
						, 0									AS "MontoGarantia"
						, 0									AS "MontoBancosyEmpresas"
						, 0									AS "FactorA"
						, 0									AS "FactorB"
						, SUM(CP."MontoCobertura")			AS "MontoCobertura"
						, SUM(CP."MontoEntidadFinanciera")	AS "MontoEntidadFinanciera"
						,CP."Fecha"
						,CP."IdEmpresa"
					FROM
						(SELECT CASE 
									WHEN "Cartera" = 'Cartera Libre' 
										THEN "CatRie"
								 	ELSE "CatRiePactos"
								 END AS "Categoria"
								, 0									AS "MontoGarantia"
								, 0									AS "MontoBancosyEmpresas"
								, 0									AS "FactorA"
								, 0									AS "FactorB"
								,SUM("MontoCobertura")AS "MontoCobertura"
								,SUM("ValorMercado")  AS "MontoEntidadFinanciera"
								,"Fecha"
								,"IdEmpresa"
							FROM "BSCL.Mesa.BD::MRF.CoberturaPatrimonial"
						GROUP BY CASE 
									WHEN "Cartera" = 'Cartera Libre' 
										THEN "CatRie"
								 	ELSE "CatRiePactos"
								 	END
								 	,"Fecha"
								 	,"IdEmpresa"
						UNION SELECT 'D TIPO 1'							AS "Categoria"
									, 0									AS "MontoGarantia"
									, 0									AS "MontoBancosyEmpresas"
									, 0									AS "FactorA"
									, 0									AS "FactorB"
									, 0									AS "MontoCobertura"
									, 0									AS "MontoEntidadFinanciera"
									,:p_FechaContable					AS "Fecha" 
									,:p_IdEmpresa						AS "IdEmpresa"
						FROM Dummy) CP
				WHERE "Categoria" ='D TIPO 1'
				GROUP BY  CP."Categoria",CP."Fecha",CP."IdEmpresa"
				
---------------------------------
-- CATEGORIA D TIPO 2
---------------------------------
				UNION ALL
				SELECT  ''				AS "Titulo"
						, CP."Categoria" 					AS "Categoria"
						, 0									AS "MontoGarantia"
						, 0									AS "MontoBancosyEmpresas"
						, 0									AS "FactorA"
						, 0									AS "FactorB"
						, SUM(CP."MontoCobertura")			AS "MontoCobertura"
						, SUM(CP."MontoEntidadFinanciera")	AS "MontoEntidadFinanciera"
						,CP."Fecha"
						,CP."IdEmpresa"
					FROM
						(SELECT CASE 
									WHEN "Cartera" = 'Cartera Libre' 
										THEN "CatRie"
								 	ELSE "CatRiePactos"
								 END AS "Categoria"
								, 0									AS "MontoGarantia"
								, 0									AS "MontoBancosyEmpresas"
								, 0									AS "FactorA"
								, 0									AS "FactorB"
								,SUM("MontoCobertura")AS "MontoCobertura"
								,SUM("ValorMercado")  AS "MontoEntidadFinanciera"
								,"Fecha"
								,"IdEmpresa"
							FROM "BSCL.Mesa.BD::MRF.CoberturaPatrimonial"
						GROUP BY CASE 
									WHEN "Cartera" = 'Cartera Libre' 
										THEN "CatRie"
								 	ELSE "CatRiePactos"
								 	END
								 	,"Fecha"
								 	,"IdEmpresa"
						UNION SELECT 'D TIPO 2' as "Categoria"
									, 0									AS "MontoGarantia"
									, 0									AS "MontoBancosyEmpresas"
									, 0									AS "FactorA"
									, 0									AS "FactorB"
									, 0									AS "MontoCobertura"
									, 0									AS "MontoEntidadFinanciera"
									,:p_FechaContable					AS "Fecha" 
									,:p_IdEmpresa						AS "IdEmpresa"
						FROM Dummy) CP
				WHERE "Categoria" ='D TIPO 2'
				GROUP BY  CP."Categoria",CP."Fecha",CP."IdEmpresa"
---------------------------------
-- CATEGORIA E TIPO 1
---------------------------------
				UNION ALL
				SELECT  ''				AS "Titulo"
						, CP."Categoria" 					AS "Categoria"
						, 0									AS "MontoGarantia"
						, 0									AS "MontoBancosyEmpresas"
						, 0									AS "FactorA"
						, 0									AS "FactorB"
						, SUM(CP."MontoCobertura")			AS "MontoCobertura"
						, SUM(CP."MontoEntidadFinanciera")	AS "MontoEntidadFinanciera"
						,CP."Fecha"
						,CP."IdEmpresa"
					FROM
						(SELECT CASE 
									WHEN "Cartera" = 'Cartera Libre' 
										THEN "CatRie"
								 	ELSE "CatRiePactos"
								 END AS "Categoria"
								, 0									AS "MontoGarantia"
								, 0									AS "MontoBancosyEmpresas"
								, 0									AS "FactorA"
								, 0									AS "FactorB"
								,SUM("MontoCobertura")AS "MontoCobertura"
								,SUM("ValorMercado")  AS "MontoEntidadFinanciera"
								,"Fecha"
								,"IdEmpresa"
							FROM "BSCL.Mesa.BD::MRF.CoberturaPatrimonial"
						GROUP BY CASE 
									WHEN "Cartera" = 'Cartera Libre' 
										THEN "CatRie"
								 	ELSE "CatRiePactos"
								 	END
								 	,"Fecha"
								 	,"IdEmpresa"
						UNION SELECT 'E TIPO 1' 						AS "Categoria"
									, 0									AS "MontoGarantia"
									, 0									AS "MontoBancosyEmpresas"
									, 0									AS "FactorA"
									, 0									AS "FactorB"
									, 0									AS "MontoCobertura"
									, 0									AS "MontoEntidadFinanciera"
									,:p_FechaContable					AS "Fecha" 
									,:p_IdEmpresa						AS "IdEmpresa"
						FROM Dummy) CP
				WHERE "Categoria" ='E TIPO 1'
				GROUP BY  CP."Categoria",CP."Fecha",CP."IdEmpresa"
				
---------------------------------
-- CATEGORIA E TIPO 2
---------------------------------
				UNION ALL
				SELECT  ''				AS "Titulo"
						, CP."Categoria" 					AS "Categoria"
						, 0									AS "MontoGarantia"
						, 0									AS "MontoBancosyEmpresas"
						, 0									AS "FactorA"
						, 0									AS "FactorB"
						, SUM(CP."MontoCobertura")			AS "MontoCobertura"
						, SUM(CP."MontoEntidadFinanciera")	AS "MontoEntidadFinanciera"
						,CP."Fecha"
						,CP."IdEmpresa"
					FROM
						(SELECT CASE 
									WHEN "Cartera" = 'Cartera Libre' 
										THEN "CatRie"
								 	ELSE "CatRiePactos"
								 END AS "Categoria"
								, 0									AS "MontoGarantia"
								, 0									AS "MontoBancosyEmpresas"
								, 0									AS "FactorA"
								, 0									AS "FactorB"
								,SUM("MontoCobertura")AS "MontoCobertura"
								,SUM("ValorMercado")  AS "MontoEntidadFinanciera"
								,"Fecha"
								,"IdEmpresa"
							FROM "BSCL.Mesa.BD::MRF.CoberturaPatrimonial"
						GROUP BY CASE
									WHEN "Cartera" = 'Cartera Libre' 
										THEN "CatRie"
								 	ELSE "CatRiePactos"
								 	END
								 	,"Fecha"
								 	,"IdEmpresa"
						UNION SELECT 'E TIPO 2' 						AS "Categoria"
									, 0									AS "MontoGarantia"
									, 0									AS "MontoBancosyEmpresas"
									, 0									AS "FactorA"
									, 0									AS "FactorB"
									, 0									AS "MontoCobertura"
									, 0									AS "MontoEntidadFinanciera"
									,:p_FechaContable					AS "Fecha" 
									,:p_IdEmpresa						AS "IdEmpresa"
									FROM Dummy) CP
				WHERE "Categoria" ='E TIPO 2'
					GROUP BY  CP."Categoria",CP."Fecha",CP."IdEmpresa"
				
---------------------------------
-- INDICE MONEDA EXT.
---------------------------------
				UNION ALL
				SELECT  'INDICES MONEDA EXTRANJERA'			AS "Titulo"
						, 'Derechos a Futuro < 7 dias' 		AS "Categoria"
						, 0									AS "MontoGarantia"
						, 0									AS "MontoBancosyEmpresas"
						, 0									AS "FactorA"
						, 0									AS "FactorB"
						, COALESCE("DerechosTasaPactosMenores8Dias",0) AS "MontoCobertura"
						, 0									AS "MontoEntidadFinanciera"
						,:p_FechaContable					AS "Fecha" 
						,:p_IdEmpresa						AS "IdEmpresa"
					FROM "BSCL.Mesa.BD::MRF.ObligacionesDerechosMonedaExtranjera"
					WHERE "FechaContable" = TO_DATE(:p_FechaContable,'YYYY-MM-DD')
					
				UNION ALL
				SELECT  ''																AS "Titulo"
						, 'Obligaciones por Op. Futuro < 7 dias (Compromiso Compra)' 	AS "Categoria"
						, 0																AS "MontoGarantia"
						, 0									AS "MontoBancosyEmpresas"
						, 0									AS "FactorA"
						, 0									AS "FactorB"
						, COALESCE("ObligacionesTasaPactosMenores8Dias",0)	AS "MontoCobertura"
						, 0									AS "MontoEntidadFinanciera"
						,:p_FechaContable					AS "Fecha" 
						,:p_IdEmpresa						AS "IdEmpresa"
					FROM "BSCL.Mesa.BD::MRF.ObligacionesDerechosMonedaExtranjera"
					WHERE "FechaContable" = TO_DATE(:p_FechaContable,'YYYY-MM-DD')
					
				UNION ALL	
				SELECT  ''									AS "Titulo"
						, 'Derechos en M/E' 				AS "Categoria"
						, 0									AS "MontoGarantia"
						, 0									AS "MontoBancosyEmpresas"
						, 0									AS "FactorA"
						, 0									AS "FactorB"
						, 0									AS "MontoCobertura"
						, 0									AS "MontoEntidadFinanciera"
						,:p_FechaContable					AS "Fecha" 
						,:p_IdEmpresa						AS "IdEmpresa"
					FROM Dummy
					
---------------------------------
-- SIMULTANEAS
---------------------------------
				UNION ALL
				SELECT  'SIMULTANEAS'			AS "Titulo"
						, 'Simultaneas (garantias)'		AS "Categoria"
						, 0									AS "MontoGarantia"
						, 0									AS "MontoBancosyEmpresas"
						, 0									AS "FactorA"
						, 0									AS "FactorB"
						, SUM(COALESCE(ccs."INGMontoAPlazo",0)) AS "MontoCobertura"
						, 0									AS "MontoEntidadFinanciera"
						,:p_FechaContable					AS "Fecha" 
						,:p_IdEmpresa						AS "IdEmpresa"
					FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" ccs
					INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" cpcp ON (cpcp."IdPortafolio" = ccs."IdPortafolio" AND cpcp."IdEmpresa" = :p_IdEmpresa)
					WHERE ccs."IdTipoOperacion" = 'VENSIM'
					AND TO_DATE(ccs."FechaVencimientoOperacion",'YYYY-MM-DD') BETWEEN TO_DATE(:p_FechaContable,'YYYY-MM-DD') and add_days(TO_DATE(:p_FechaContable,'YYYY-MM-DD'),7)
					AND ccs."IdEstado" = 'VIG'
				UNION ALL
				SELECT  ''			AS "Titulo"
						, 'Simultaneas (obligaciones)'		AS "Categoria"
						, 0									AS "MontoGarantia"
						, 0									AS "MontoBancosyEmpresas"
						, 0									AS "FactorA"
						, 0									AS "FactorB"
						, SUM(COALESCE(ccs."INGMontoAPlazo",0)) AS "MontoCobertura"
						, 0									AS "MontoEntidadFinanciera"
						,:p_FechaContable					AS "Fecha" 
						,:p_IdEmpresa						AS "IdEmpresa"
					FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" ccs
					INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" cpcp ON (cpcp."IdPortafolio" = ccs."IdPortafolio" AND cpcp."IdEmpresa" = :p_IdEmpresa)
					WHERE ccs."IdTipoOperacion" = 'VENSIM'
					AND TO_DATE(ccs."FechaVencimientoOperacion",'YYYY-MM-DD') BETWEEN TO_DATE(:p_FechaContable,'YYYY-MM-DD') and add_days(TO_DATE(:p_FechaContable,'YYYY-MM-DD'),7)
					AND ccs."IdEstado" = 'VIG'
					
				UNION ALL
				SELECT  ''									AS "Titulo"
						, 'Simultaneas (derechos)' 			AS "Categoria"
						, 0									AS "MontoGarantia"
						, 0									AS "MontoBancosyEmpresas"
						, 0									AS "FactorA"
						, 0									AS "FactorB"
						, SUM(COALESCE(ccs."INGMontoAPlazo",0)) AS "MontoCobertura"
						, 0									AS "MontoEntidadFinanciera"
						,:p_FechaContable					AS "Fecha" 
						,:p_IdEmpresa						AS "IdEmpresa"
					FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" ccs
					INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" cpcp ON (cpcp."IdPortafolio" = ccs."IdPortafolio" AND cpcp."IdEmpresa" = :p_IdEmpresa)
					WHERE ccs."IdTipoOperacion" = 'COMSIM'
					AND TO_DATE(ccs."FechaVencimientoOperacion",'YYYY-MM-DD') BETWEEN TO_DATE(:p_FechaContable,'YYYY-MM-DD') and add_days(TO_DATE(:p_FechaContable,'YYYY-MM-DD'),7)
					AND ccs."IdEstado" = 'VIG'
					) X
			WHERE X."IdEmpresa" = :p_IdEmpresa
				AND x."Fecha"	= :p_FechaContable;
					
		END;		
	END;	
END;