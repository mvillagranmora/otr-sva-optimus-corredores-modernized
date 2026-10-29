-- ########################################################################################
--
--  =========================================================
--  ESTE ARCHIVO CONTIEN IGRESO DE DATOS TIPO PARA PRUEBAS
--  =========================================================
--
-- ########################################################################################
SET SCHEMA BSCL;


--  =========================================================
--  DATOS de FONDOS
--  =========================================================
delete from "BSCL.FondoMutuo.BD::FMM.SerieDeFondo";
delete from "BSCL.FondoMutuo.BD::FMM.Fondo";

insert into "BSCL.FondoMutuo.BD::FMM.Fondo"
select 

	 'FMLIQUIDEZ' 		as "IdFondo"      -- IDENTIFICADOR INTERNO
	 ,'CREDICORPAGF'	as "IdEmisor"                 
	 ,'FM CC LIQUIDEZ' 	as "Nemotecnico"  -- NOMBRE CORTO PARA SVS            	 
	 ,'FONDO MUTUO CREDICORP CAPITAL LIQUIDEZ' 				as "NombreFondo"              
	 ,'1'				as "IdTipoFondo"     
		/*
			Tipo SVS
			1. De Inversión en Instrumentos de Deuda de Corto Plazo con Duración menor o igual a 90 días
			2. De Inversión en Instrumentos de Deuda de Corto Plazo con Duración menor o igual a 365 días
			3. De Inversión en Instrumentos de Deuda de Mediano y Largo Plazo
			4. Mixto
			5. De Inversión en Instrumentos de Capitalización
			6. De Libre Inversión
			7. Estructurado
			8. Dirigido a Inversionistas Calificados
		*/
	 ,'ART.107'			as "IdRegimenTributario"      
	 ,44				as "IdPaisOrigen"             
	 ,'8401-8' 			as "CodigoSVS"                
	 ,'N' 				as "CobraComision"            
	 ,'S'				as "MoneyMarket"              
	 ,'N' 				as "Diferido"                 
	 ,0					as "TipoCambioDiferido"       
	 ,'ARRIESGADO'		as "IdCategoriaInversionista"  -- ARRIESGADO, CALIFICADO, BALANCEADO, CONSERVADOR
	 ,'2005-12-01'		as "FechaCreacion"            
	 ,'2005-12-01'		as "FechaConstitucion"        
	 ,'1900-01-01'		as "FechaCierre"              
	 ,'V'				as "Vigencia"                  --- VIGENTE, NO VIGENTE PARA EL MERCADO
	 ,'N'				as "Omnibus"                  
	 ,'A'				as "IdEstado"                  --- ABIERTO CERRADO
	 , 1 				as "IdEmpresa"
from dummy;

insert into "BSCL.FondoMutuo.BD::FMM.Fondo"
select 

	 'BIEXTRAGESTION' 		as "IdFondo"      -- IDENTIFICADOR INTERNO
	 ,'BICEAGF'	as "IdEmisor"                 
	 ,'EXTRA GESTION' 	as "Nemotecnico"  -- NOMBRE CORTO PARA SVS            	 
	 ,'FONDO MUTUO BICE EXTRA GESTION' 				as "NombreFondo"              
	 ,'6'				as "IdTipoFondo"     
		/*
			Tipo SVS
			1. De Inversión en Instrumentos de Deuda de Corto Plazo con Duración menor o igual a 90 días
			2. De Inversión en Instrumentos de Deuda de Corto Plazo con Duración menor o igual a 365 días
			3. De Inversión en Instrumentos de Deuda de Mediano y Largo Plazo
			4. Mixto
			5. De Inversión en Instrumentos de Capitalización
			6. De Libre Inversión / 6-FM DE LIBRE INVERSION
			7. Estructurado
			8. Dirigido a Inversionistas Calificados
		*/
	 ,'ART.107'			as "IdRegimenTributario"      
	 ,44				as "IdPaisOrigen"             
	 ,'8933-8' 			as "CodigoSVS"                
	 ,'N' 				as "CobraComision"            
	 ,'S'				as "MoneyMarket"              
	 ,'N' 				as "Diferido"                 
	 ,0					as "TipoCambioDiferido"       
	 ,'ARRIESGADO'		as "IdCategoriaInversionista"  -- ARRIESGADO, CALIFICADO, BALANCEADO, CONSERVADOR
	 ,'2012-05-22'		as "FechaCreacion"            
	 ,'2012-05-22'		as "FechaConstitucion"        
	 ,'1900-01-01'		as "FechaCierre"              
	 ,'V'				as "Vigencia"                  --- VIGENTE, NO VIGENTE PARA EL MERCADO
	 ,'S'				as "Omnibus"                  
	 ,'A'				as "IdEstado"                  --- ABIERTO CERRADO
	 , 1 				as "IdEmpresa"
from dummy;

--  =========================================================
--  DATOS de SERIE
--  =========================================================

insert into "BSCL.FondoMutuo.BD::FMM.SerieDeFondo"
select 
	 'FMLIQUIDEZ_A' 				as "IdSerieFondo" 				,
	 0								as "IdInstrumento"				,
	 'FMLIQUIDEZ'					as "IdFondo"					,
	 'Serie APV Fondos Liquidez' 	as "NombreSerieFondo"			,
	 'A' 							as "CodigoSerie"        		,
	 'APV'							as "PropositoSerie"     		,
	 'LIQUIDEZA'					as "Alias"              		,
	 0 								as "MontoMinimo"        		,
	 0 								as "MontoMaximo"        		,
	 3 								as "DiasRetencionesRescate"		,
	 0								as "DiasRetencionesAporte"  	,
	 0 								as "DiasRetencionesReinversion" ,
	 'S'							as "ParaGarantia"           	,
	 0								as "PatrimonioMinimo"			,
	 '' 							as "IdSerieFondoOrigen" 		,
 	 1 								as "IdEmpresa"
	 FROM DUMMY;

COMMIT;
--  =========================================================
--  DATOS de Movimientos de Aportes
--  =========================================================



--  =========================================================
--  DATOS de Movimientos de Rescates
--  =========================================================



-- OBS: Limitar Diferidos a Preferencia de Usuario
