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
--	BDH: Se crea este archivo con la finalidad de una futura liberacion a UAT y no tocar los otros scripts ya liberados 
-- ########################################################################################

SET SCHEMA BSCL;

ALTER TABLE BSCL."BSCL.APV.BD::SCS.ContratoSAAPV"
  DROP (	  
    "OrigenAhorroCV" 				,
	"OrigenAhorroDC"				,
    "MontoAhorroCV"					,
	"UFAhorroCV" 					,
	"PorcentajeRegimenImponibleCV"	,
	"MontoAhorroDC" 				,
	"UFAhorroDC" 					,
	"PorcentajeRegimenImponibleDC"  ,
	"Pago" 							,
	"PeriodoInicio" 				,
	"PeriodoTermino"				,
	"RegimenTributario" 			);
		  
	
ALTER TABLE BSCL."BSCL.APV.BD::SCS.PortafolioContratoSAAPV"
  ADD (	  "OrigenAhorro"				NVARCHAR(10)			NULL,
  		  "MontoPesos" 					DECIMAL(26,8)			NULL,
		  "MontoUF" 					DECIMAL(26,8)			NULL,
		  "PorcentajeRegimenImponible" 	DECIMAL(26,8)			NULL,
		  "Pago" 						NVARCHAR(20)			NULL,
		  "PeriodoInicio" 				NVARCHAR(7)			    NULL,
		  "PeriodoTermino" 				NVARCHAR(7)			    NULL);
	  
		  
/*
ALTER TABLE BSCL."BSCL.APV.BD::SCS.PortafolioContratoSAAPV"
  DROP (  "MontoAhorro",
		  "UFAhorro",
		  "PorcentajeRegimenImponible" ,
		  "Pago",
		  "PeriodoInicio",
		  "PeriodoTermino");
		  */