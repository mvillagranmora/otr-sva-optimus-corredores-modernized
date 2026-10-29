SET SCHEMA "BSCL"; 

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
		   
DROP TABLE  "BSCL.Normativa.BD::GBL.SALIDA_INFOTRANOFIC";	   
DROP TABLE  "BSCL.Normativa.BD::GBL.SALIDA_CIRCULAR1518";

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Normativa.BD::GBL.SALIDA_INFOTRANOFIC"(
		"IdInterno"					BIGINT
		,"RutIntermediario"			NVARCHAR(40) DEFAULT '' NULL
		,"FechaTransaccion"			DATETIME DEFAULT NULL
		,"Mercado"					NVARCHAR(3) DEFAULT '' NULL
		,"SistemaTransaccion"		NVARCHAR(5) DEFAULT '' NULL
		,"Bolsa"					NVARCHAR(3) DEFAULT '' NULL
		,"Folio"					NVARCHAR(40) DEFAULT '' NULL
		,"RutContraparte"			NVARCHAR(40) DEFAULT '' NULL
		,"NumeroOrden"				BIGINT DEFAULT 0 NULL
		,"Nemotecnico"				NVARCHAR(100) DEFAULT '' NULL
		,"FechaVencimiento"			DATETIME DEFAULT NULL
		,"IdTipoInstrumento"		NVARCHAR(3) DEFAULT '' NULL
		,"HIdTipoInstrumento"		NVARCHAR(3) DEFAULT '' NULL
		,"IdTipoOperacion"			NVARCHAR(6) DEFAULT '' NULL
		,"HIdTipoOperacion"			NVARCHAR(6) DEFAULT '' NULL
		,"Persona"					NVARCHAR(1) DEFAULT '' NULL
		,"Plazo"					BIGINT DEFAULT 0 NULL
		,"IdCondicionLiquidacion"	NVARCHAR(3) DEFAULT '' NULL
		,"TipoCliente"				NVARCHAR(1) DEFAULT '' NULL
		,"UnidadesTransadas"		DECIMAL(27,8) DEFAULT 0 NULL
		,"PrecioTransaccion"		DECIMAL(27,8)	DEFAULT 0 NULL
		,"TotalTransado"			DECIMAL(27,8)	DEFAULT 0 NULL
		,"TipoUnidad"				NVARCHAR(3) DEFAULT '' NULL
		,"ValorPar"					DECIMAL(27,8)	DEFAULT 0 NULL
		,"TIR"						DECIMAL(27,8)	DEFAULT 0 NULL
		,"Rentabilidad"				DECIMAL(27,8)	DEFAULT 0 NULL
		,"NacionalidadCliente"		NVARCHAR(100) DEFAULT '' NULL
		,"RutCliente"				NVARCHAR(40) DEFAULT '' NULL
		,"NombreCliente"			NVARCHAR(100) DEFAULT '' NULL
		,"NumeroFactura"			BIGINT DEFAULT 0 NULL
		,"RutAutorizado"			NVARCHAR(40) DEFAULT '' NULL
		,"NombreAutorizado"			NVARCHAR(100) DEFAULT '' NULL
		,"IdClasificacionTipoInstrumento" NVARCHAR(6) DEFAULT '' NULL
		,"CodigoOperador"			NVARCHAR(100)  DEFAULT '' NULL
) ON COMMIT DELETE ROWS;

SET SCHEMA BSCL;

DROP TABLE  "BSCL.Normativa.BD::GBL.SALIDA_CIRCULAR1518";GO

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Normativa.BD::GBL.SALIDA_CIRCULAR1518"(
		"IdInterno"								BIGINT,
		"FechaCuadraturaCustodia"				SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
		"Nemotecnico" 							NVARCHAR(40) 	NOT NULL,
		"CantidadCustodia"						DECIMAL(26,8) 	NOT NULL,
		"CantidadGarantia"						DECIMAL(26,8) 	NOT NULL,
		"CantidadPlazoVenta"				DECIMAL(26,8) 	NOT NULL,	--3
		"CantidadPlazoCompra"				DECIMAL(26,8) 	NOT NULL,	--4
		"CantidadTotal"							DECIMAL(26,8) 	NOT NULL,
		"CantidadDCVRelacionado" 				DECIMAL(26,8) 	NOT NULL,
		"CantidadDCVTerceros" 					DECIMAL(26,8) 	NOT NULL,
		"CantidadDCVCuentaPropia" 				DECIMAL(26,8) 	NOT NULL,
		"CantidadBCS"	 						DECIMAL(26,8) 	NOT NULL, --BCS 
		"CantidadBEC"	 						DECIMAL(26,8) 	NOT NULL, --BEC 
		"CantidadBOVALPO"						DECIMAL(26,8) 	NOT NULL, --BOVALPO 
		"CantidadEmisores" 						DECIMAL(26,8) 	NOT NULL, --EMISORES
		"CantidadTransitoIngreso"				DECIMAL(26,8) 	NOT NULL,
		"CantidadTransitoEgreso"				DECIMAL(26,8) 	NOT NULL,
		"TotalCustodiosyEmisores"				DECIMAL(26,8)  	NOT NULL, -- Calculada (CantidadDCVRelacionado + CantidadDCVTerceros + CantidadDCVCuentaPropia + CantidadBCS + CantidadBEC + CantidadBOVALPO + CantidadEmisores) +  ( -CantidadTransitoIngreso + )
		"Diferencia"							DECIMAL(26,8)  	NOT NULL,
		"PrecioCierre"							DECIMAL(26,8)  	NOT NULL, -- Directo desde precio instrumento
		"Valorizacion" 							DECIMAL(26,8)	NOT NULL, -- desde Cuadratura
		"DiferenciaValorizada"					DECIMAL(26,8)	NOT NULL, -- calculada
		"ExplicacionDescuadratura"				NVARCHAR(100) 	NOT NULL,
		"IdCuadratura"							BIGINT			NOT NULL,
		"Mercado"								NVARCHAR(40)    NOT NULL,
		"TipoInstrumento"						NVARCHAR(40)    NOT NULL,
		"Tipo" 									NVARCHAR(40) 	DEFAULT 'ORI' NOT NULL,
		"CantidadComprasEnTransito" 			DECIMAL(26,8)  DEFAULT 0,
		"CantidadVentasEnTransito" 				DECIMAL(26,8)  DEFAULT 0
) ON COMMIT DELETE ROWS;GO


	     									     
 /*==============================================================*/
/* Sequences:       DROP & CREATE                               */
/*==============================================================*/

DROP sequence "BSCL.Normativa.BD.Secuencias::GBL.IdInfoTranOfic" cascade;


CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::GBL.IdInfoTranOfic" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;




commit;


 
 

 

 

 





 






 

 
 

