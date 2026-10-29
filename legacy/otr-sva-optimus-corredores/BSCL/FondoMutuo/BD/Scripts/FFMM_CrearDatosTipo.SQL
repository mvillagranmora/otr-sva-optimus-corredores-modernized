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
delete from "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo";
delete from "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico";
delete from "BSCL.FondoMutuo.BD::FMM.ValorCuota";
delete from "BSCL.FondoMutuo.BD::FMM.FamiliaFondo";
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
	 --,'N' 				as "CobraComision"            
	 ,'S'				as "MoneyMarket"              
	 --,'N' 				as "Diferido"                       
	 ,'ARRIESGADO'		as "IdCategoriaInversionista"  -- ARRIESGADO, CALIFICADO, BALANCEADO, CONSERVADOR
	 ,'2005-12-01'		as "FechaCreacion"   
	 , CURRENT_UTCTIMESTAMP as "FechaIngresoRegistro"         
	 ,'2005-12-01'		as "FechaConstitucion"        
	 ,'0001-01-01'		as "FechaCierre"              
	 ,'V'				as "Vigencia"                  --- VIGENTE, NO VIGENTE PARA EL MERCADO
	 --,'N'				as "Omnibus"               
	 , 'N'	as "FondoExtranjero"
	 , 'CLP' as "IdMoneda"
	 , 'FFM' as "IdTipoInstrumento"
	 , '' as "IdFamiliaFondo"
	 , 1 				as "IdEmpresa"
from dummy;

/*insert into "BSCL.FondoMutuo.BD::FMM.Fondo"
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
	 ,'A'				as "EstadoRescate"                  --- ABIERTO CERRADO
	 ,'A'				as "EstadoAporte" -- ABIERTO CERRADO
	 ,TO_TIME('16:00:00', 'HH24:MI:SS') as "HoraCierreRescate" 
	 ,TO_TIME('16:00:00', 'HH24:MI:SS') as "HoraCierreAporte" 
	 , 1 				as "IdEmpresa"
from dummy;*/

--  =========================================================
--  DATOS de SERIE
--  =========================================================

insert into "BSCL.FondoMutuo.BD::FMM.SerieDeFondo"
("IdSerieFondo","IdInstrumento","IdFondo","NombreSerieFondo","CodigoSerie","PropositoSerie","Alias",
"DecimalesValorCuota","DecimalesCuota","MontoMinimo","MontoMaximo","DiasRetencionesRescate",
"DiasRetencionesAporte","DiasRetencionesReinversion","ExcluyeGarantia","CobraComision","Diferido",
"InstrumentoDCV","Omnibus","PatrimonioMinimo","IdSerieFondoOrigen","IdEstado","FechaIngresoRegistro","IdEmpresa")
select 
	 'FMLIQUIDEZ_A' 				as "IdSerieFondo" 				,
	 0								as "IdInstrumento"				,
	 'FMLIQUIDEZ'					as "IdFondo"					,
	 'Serie APV Fondos Liquidez' 	as "NombreSerieFondo"			,
	 'A' 							as "CodigoSerie"        		,
	 'APV'							as "PropositoSerie"     		,
	 'LIQUIDEZA'					as "Alias"              		,
	 0								as "DecimalesValorCuota"		,
	 0								as "DecimalesCuota"				,
	 0 								as "MontoMinimo"        		,
	 0 								as "MontoMaximo"        		,
	 3 								as "DiasRetencionesRescate"		,
	 0								as "DiasRetencionesAporte"  	,
	 0 								as "DiasRetencionesReinversion" ,
	 'S'							as "ExcluyeGarantia"           	,
	 'N'							as "CobraComision"				,
	 'N'							as "Diferido"					,
	 --''								as "InstrumentoAGF"				,
	 ''								as "InstrumentoDCV"				,
	 'N'							as "Omnibus"					,
	 0								as "PatrimonioMinimo"			,
	 '' 							as "IdSerieFondoOrigen" 		,
	 'ACT'							as "IdEstado"					,
	 CURRENT_UTCTIMESTAMP 			as "FechaIngresoRegistro"		,
 	 1 								as "IdEmpresa"
	 FROM DUMMY;

insert into "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico"
select
	1,
	'FMLIQUIDEZ_A',
	'2016-05-16',
	2300,
	'CLP',
	'2016-05-16',
	CURRENT_UTCTIMESTAMP,
	1
FROM DUMMY;

insert into "BSCL.FondoMutuo.BD::FMM.ValorCuota"
select
	'FMLIQUIDEZ_A',
	'2016-05-16',
	2300,
	'CLP',
	'2016-05-16',
	CURRENT_UTCTIMESTAMP,
	1
FROM DUMMY;

INSERT INTO "BSCL.FondoMutuo.BD::FMM.FamiliaFondo"
select
	'RV',
	'Renta Variable',
	'ACT',
	1
FROM DUMMY;

INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("IdConfiguracionFondoMutuo", "Key", "Value", "IdEmpresa") VALUES (1,'PERFIL_COMERCIAL','0',1);
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("IdConfiguracionFondoMutuo", "Key", "Value", "IdEmpresa") VALUES (2,'PERFIL_BACKOFFICE','0',1);
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("IdConfiguracionFondoMutuo", "Key", "Value", "IdEmpresa") VALUES (3 ,'HORA_CIERRE_APORTE_COMERCIAL' ,'00:00:00' ,1 );
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("IdConfiguracionFondoMutuo", "Key", "Value", "IdEmpresa") VALUES (4,'HORA_CIERRE_RESCATE_COMERCIAL','00:00:00',1);
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("IdConfiguracionFondoMutuo", "Key", "Value", "IdEmpresa") VALUES (5,'HORA_CIERRE_REINVERSION_COMERCIAL','00:00:00',1);
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("IdConfiguracionFondoMutuo", "Key", "Value", "IdEmpresa") VALUES (6,'HORA_CIERRE_LIQUIDEZ_COMERCIAL','00:00:00',1);
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("IdConfiguracionFondoMutuo", "Key", "Value", "IdEmpresa") VALUES (7,'HORA_CIERRE_APORTE_BACKOFFICE','00:00:00',1);
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("IdConfiguracionFondoMutuo", "Key", "Value", "IdEmpresa") VALUES (8,'HORA_CIERRE_RESCATE_BACKOFFICE','00:00:00',1);
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("IdConfiguracionFondoMutuo", "Key", "Value", "IdEmpresa") VALUES (9,'HORA_CIERRE_REINVERSION_BACKOFFICE','00:00:00',1);
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("IdConfiguracionFondoMutuo", "Key", "Value", "IdEmpresa") VALUES (10,'HORA_CIERRE_LIQUIDEZ_BACKOFFICE','00:00:00',1);


COMMIT;


--  =========================================================
--  DATOS de Movimientos de Aportes
--  =========================================================



--  =========================================================
--  DATOS de Movimientos de Rescates
--  =========================================================



-- OBS: Limitar Diferidos a Preferencia de Usuario
