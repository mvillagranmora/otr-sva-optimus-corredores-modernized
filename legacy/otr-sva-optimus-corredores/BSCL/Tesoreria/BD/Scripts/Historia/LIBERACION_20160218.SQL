-- ########################################################################################
--
--  =========================================================
--  ESTE ARCHIVO CONTIEN LOS SCRIPT HISTORICOS DE LIBERACION
--  =========================================================
--  * Debe incluir los ALTER de Tablas
--  * Debe incluir nuevas tablas, indices, secuencuias, etc.
--  * Debe incluir insert de datos maestros necesarios para el funcionamiento 
--  NOTA: Este archivo no reemplaza los scripts de creación que deben ir en cada módulo.
-- 
-- ########################################################################################

SET schema BSCL;
--
--
-- LIBERACION SEMANA: 18-02-2016
--
--################################################
--       			CHEQUERA
--################################################
		alter table "BSCL.Tesoreria.BD::TES.Chequera"
       add ("Remanente" INTEGER NOT NULL DEFAULT 0);
	   
		alter table "BSCL.Tesoreria.BD::TES.Chequera"
       add ("NumeroChequeActual" INTEGER NOT NULL DEFAULT 0);
--------------------------------------------------
--      		FIN CHEQUERA
--------------------------------------------------


--################################################
--       		CONFIGURACION CHEQUERA
--################################################

CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.ConfiguracionChequera" (
	 "IdConfiguracionChequera"	BIGINT			NOT NULL,
	 "NombreConfChequera" 		NVARCHAR(100)	NOT NULL,
	 "AnchoCheque"				INTEGER			NOT NULL,
	 "AltoCheque"				INTEGER			NOT NULL,
	 "SigloImpreso"            	NVARCHAR(1) 	NOT NULL DEFAULT 'N',  -- S / N 
	 "MontoFila"				INTEGER			NOT NULL,
	 "MontoColumna"				INTEGER			NOT NULL,
	 "ProvinciaFila"			INTEGER			NOT NULL,
	 "ProvinciaColumna"			INTEGER			NOT NULL,
	 "MesFila"					INTEGER			NOT NULL,
	 "MesColumna"				INTEGER			NOT NULL,
	 "AnioFila"					INTEGER			NOT NULL,
	 "AnioColumna"				INTEGER			NOT NULL,
	 "ExtendidoAFila"			INTEGER			NOT NULL,
	 "ExtendidoAColumna"		INTEGER			NOT NULL,
	 "PrimeraCantidadFila"		INTEGER			NOT NULL,
	 "PrimeraCantidadColumna"	INTEGER			NOT NULL,
	 "SegundaCantidadFila"		INTEGER			NOT NULL,
	 "SegundaCantidadColumna"	INTEGER			NOT NULL,
PRIMARY KEY ("IdConfiguracionChequera") );

CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::ConfiguracionChequera"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1; 
--------------------------------------------------
--      	FIN CONFIGURACION CHEQUERA
--------------------------------------------------