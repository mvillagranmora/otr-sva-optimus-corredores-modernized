SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/


DROP TABLE "BSCL.Normativa.BD::NOR.DatosPuntaOrdenes"				CASCADE;

DROP TABLE "BSCL.Normativa.BD::EJORD.EjecucionOrdenes"				CASCADE;


/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/



 
CREATE COLUMN TABLE "BSCL.Normativa.BD::NOR.DatosPuntaOrdenes"(
"FECHA" 			NVARCHAR(10) 	NOT NULL,
"MERCADO" 			NVARCHAR(2) 	NOT NULL,
"BOLSA" 			NVARCHAR(3) 	NOT NULL,
"NEMO" 				NVARCHAR(10) 	NOT NULL,		
"COD_CORR" 			DECIMAL(24,4) 	NOT NULL,
"CONDICION" 		NVARCHAR(2) 	NOT NULL,
"COM_VEN" 			NVARCHAR(1) 	NOT NULL,	
"MEJOR_BOLSA"		NVARCHAR(3) 	NOT NULL,
"MEJOR_HORA" 		NVARCHAR(6) 	NOT NULL,	
"MEJOR_PRECIO" 		DECIMAL(24,4) 	NOT NULL,
"PEOR_BOLSA" 		NVARCHAR(3) 	NOT NULL,
"PEOR_HORA" 		NVARCHAR(6) 	NOT NULL,	
"PEOR_PRECIO" 		DECIMAL(24,4) 	NOT NULL,
PRIMARY KEY ("FECHA","MERCADO","BOLSA","NEMO","COD_CORR","CONDICION","COM_VEN")
);
/*  
CREATE COLUMN TABLE "BSCL.Normativa.BD::NOR.InfoEjecucionOrdenes"(
"EJECUCION_ID"						BIGINT NOT NULL,
"FOLIO_ORDEN"						BIGINT NOT NULL,
"TIPO_OPERACION"					NVARCHAR(1) NOT NULL,
"MERCADO"							NVARCHAR(3) NOT NULL,
"NEMO"								NVARCHAR(30) NOT NULL,
"MONTO"								DECIMAL(24,4) NOT NULL,
"CANTIDAD"							DECIMAL(24,4) NOT NULL,
"PRECIO"							DECIMAL(24,4) NOT NULL,
"CONDICION_LIQUID"					NVARCHAR(2) NOT NULL,
"FECHA_RECEPCION"					DATE NOT NULL,
"FECHA_ACEPTACION"					DATE NOT NULL,
"TIPO_EJECUCION"					NVARCHAR(1) NOT NULL,
"FOLIO_PALO"						BIGINT NOT NULL,      ----quizas es necesario dejarla varchar, corregir
"FECHA_EJECUCION"					DATE NOT NULL,
"BOLSA_EJECUCION"					NVARCHAR(3) NOT NULL,
"MODALIDAD_EJECUCION"				NVARCHAR(6) NOT NULL,
"TIPO_CONTRAPARTE"					NVARCHAR(6) NOT NULL,
"PRECIO_EJECUCION"					DECIMAL(24,4) NOT NULL,
"MONTO_TRANSADO"					DECIMAL(24,4) NOT NULL,
"CANTIDAD_TRANSADA"					DECIMAL(24,4) NOT NULL,
"COMISION_TRANSADA"					DECIMAL(24,4) NOT NULL,
"MEJOR_PRECIO_VIGENTE_OTRA_BOLSA"	DECIMAL(24,4) NOT NULL,
"PRECIO_MINIMO_MERCADO"				DECIMAL(24,4) NOT NULL,
"HORA_PRECIO_MINIMO_MERCADO"		NVARCHAR(8) NOT NULL,
"BOLSA_PRECIO_MINIMO_MERCADO"		NVARCHAR(3) NOT NULL,
"PRECIO_MAXIMO_MERCADO"				DECIMAL(24,4) NOT NULL,
"HORA_PRECIO_MAXIMO_MERCADO"		NVARCHAR(8) NOT NULL,
"BOLSA_PRECIO_MAXIMO_MERCADO"		NVARCHAR(3) NOT NULL,
"MEJOR_PRECIO_MERCADO"				DECIMAL(24,4) NOT NULL,
"MEJOR_PRECIO_OTRA_COD_BOLSA"		NVARCHAR(3) NOT NULL,
"MAYOR_PRECIO_OPERACION_CORR"		DECIMAL(24,4) NOT NULL,
"HORA_PRECIO_MAYOR_CORR"			NVARCHAR(8) NOT NULL,
"MENOR_PRECIO_OPERACION_CORR"		DECIMAL(24,4) NOT NULL,
"HORA_PRECIO_MENOR_CORR"			NVARCHAR(8) NOT NULL,
"RUT_CLIENTE"						NVARCHAR(11) NOT NULL,
"CUENTA"							BIGINT NOT NULL,          ----quizas es necesario dejarla varchar, corregir
"NOMBRE_CLIENTE"					NVARCHAR(100) NOT NULL,
"CODIGO_AGENTE_DUENIO"				NVARCHAR(4) NOT NULL,
"NOMBRE_AGENTE_DUENIO"				NVARCHAR(100) NOT NULL,
"TELEFONO_AGENTE"					NVARCHAR(20) NOT NULL,
"CORREO_AGENTE"						NVARCHAR(80) NOT NULL,
"NOTIFICACION_MAIL"					NVARCHAR(1) NOT NULL,
"GENERACION_CARTA"					NVARCHAR(1) NOT NULL,
"CORREO_CLIENTE"					NVARCHAR(80) NOT NULL,
"FOLIO_GENERACION"					BIGINT NOT NULL,          ----quizas es necesario dejarla varchar, corregir
"SEC_MOVTO_FAC"						BIGINT NOT NULL,     
"FECHA_GENERACION"					DATE NOT NULL,
"CANTIDAD_MEJOR_PREC_VIG_OTRA_BOLSA"BIGINT NOT NULL,     	
"DIRECCION_CLIENTE"					NVARCHAR(200) NOT NULL,
"GENERACION_ALERTA"					NVARCHAR(1) NOT NULL,
"FOLIO_ORDEN_EXTERNO"				NVARCHAR(50) NOT NULL,
PRIMARY KEY ("EJECUCION_ID")
);
*/
/*
CREATE COLUMN TABLE "BSCL.Normativa.BD::EJORD.TipoDocumento" (
   "IdTipoDocumento" 				NVARCHAR(10) 	NOT NULL,
   "IdEstado"		 				NVARCHAR(3)  	NOT NULL DEFAULT 'ACT',
    PRIMARY KEY ("IdTipoDocumento")
);
*/

CREATE COLUMN TABLE "BSCL.Normativa.BD::EJORD.EjecucionOrdenes"(
 "IdEjecucion"						BIGINT 			NOT NULL
,"IdEmpresa" 						BIGINT  		NOT NULL 
,"IdDocumento" 						BIGINT 			NOT NULL DEFAULT 0
,"FolioGeneracion"					BIGINT 			DEFAULT 0 NOT NULL
,"IdOrden"							BIGINT 			DEFAULT 0 NOT NULL
,"Folio" 							NVARCHAR(40)	DEFAULT ''NOT NULL 
,"IdCliente" 						BIGINT NOT NULL
,"IdPortafolio"	 					BIGINT NOT NULL
,"IdClasificacionTipoInstrumento" 	NVARCHAR(6) 	DEFAULT '' NULL
,"IdTipoInstrumento"				NVARCHAR(3) 	DEFAULT '' NULL
,"IdTipoOperacion"					NVARCHAR(6) 	DEFAULT '' NULL
,"IdInstrumento" 					BIGINT 			DEFAULT 0 NOT NULL
,"Cantidad" 						DECIMAL(26,8) 	DEFAULT 0 NOT NULL
,"Monto" 							DECIMAL(26,8) 	DEFAULT 0 NOT NULL
,"MontoValorizado"					DECIMAL(26,8) 	DEFAULT 0 NOT NULL
,"Precio" 							DECIMAL(26,8) 	DEFAULT 0 NOT NULL
,"IdMoneda" 						NVARCHAR(3)   	DEFAULT ''NOT NULL 
,"IdCondicionDeLiquidacion" 		NVARCHAR(2)  	DEFAULT ''NOT NULL 
,"FechaRecepcion" 					SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00'
,"FechaAceptacion"	 				SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00'--Sistema externo
,"TipoEjecucion"					NVARCHAR(1) 	DEFAULT '' NOT NULL --Parcial/Total
,"FechaEjecucion"					SECONDDATE 		DEFAULT '0001-01-01 00:00:00' NOT NULL 
,"IdBolsa" 							NVARCHAR(3) 	DEFAULT ''  NOT NULL 
,"EsOD"								NVARCHAR(1) 	DEFAULT 'N' NOT NULL
,"PrecioEjecucion"					DECIMAL(26,8) 	DEFAULT 0 NOT NULL
,"MontoTransado" 					DECIMAL(26,8) 	DEFAULT 0 NOT NULL
,"CantidadTransada"					DECIMAL(26,8) 	DEFAULT 0 NOT NULL
,"ComisionTransada"					DECIMAL(26,8) 	DEFAULT 0 NOT NULL
--Mejor
,"MejorPrecioOtraBolsa"				DECIMAL(26,8) 	DEFAULT 0 NOT NULL
,"CantidadMejorPrecioOtraBolsa" 	DECIMAL(26,8) 	DEFAULT 0 NOT NULL
,"MejorPrecioMercado"				DECIMAL(26,8) 	DEFAULT 0 NOT NULL
--Minimo
,"PrecioMinimoMercado"				DECIMAL(26,8) 	DEFAULT 0 NOT NULL
,"HoraPrecioMinimoMercado"			NVARCHAR(8)		DEFAULT ''  NOT NULL
,"IdBolsaPrecioMinimoMercado" 		NVARCHAR(3) 	DEFAULT ''  NOT NULL 
--Maximo
,"PrecioMaximoMercado"				DECIMAL(26,8) 	DEFAULT 0 NOT NULL
,"HoraPrecioMaximoMercado"			NVARCHAR(8)		DEFAULT ''  NOT NULL
,"IdBolsaPrecioMaximoMercado" 		NVARCHAR(3) 	DEFAULT ''  NOT NULL 
--Mayor corredor
,"MayorPrecioOperacionCorredor"		DECIMAL(26,8) 	DEFAULT 0 NOT NULL
,"HoraPrecioMayorCorredor"			NVARCHAR(8)		DEFAULT ''  NOT NULL
--Menor corredor
,"MenorPrecioOperacionCorredor"		DECIMAL(26,8) 	DEFAULT 0 NOT NULL
,"HoraPrecioMenorCorredor"			NVARCHAR(8)		DEFAULT ''  NOT NULL
--IdAgente
,"IdEmpleadoDigitador"				BIGINT 			DEFAULT 0 NOT NULL
,"FolioComprobanteFacturacion"      BIGINT 			DEFAULT 0 NOT NULL
,"FechaGeneracion" 					SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00'
,"FolioExterno" 					NVARCHAR(40)	DEFAULT ''NOT NULL
,"IdEstadoNotificacion"		 		NVARCHAR(3)  	NOT NULL DEFAULT 'PEN'
,"IdEstadoCarta"		 			NVARCHAR(3)  	NOT NULL DEFAULT 'PEN'
,"FechaIngreso"      				SECONDDATE      DEFAULT '0001-01-01 00:00:00' NOT NULL
,"FechaCreacion"     				SECONDDATE      DEFAULT '0001-01-01 00:00:00' NOT NULL
,"IdUsuarioCreador"   				BIGINT  		DEFAULT 0,
   
-- En linea
--"TIPO_CONTRAPARTE"					NVARCHAR(6) NOT NULL,
--"MEJOR_PRECIO_OTRA_COD_BOLSA"		NVARCHAR(3) NOT NULL,

--"NOMBRE_CLIENTE"					NVARCHAR(100) NOT NULL,
--"CODIGO_AGENTE_DUENIO"				NVARCHAR(4) NOT NULL,
--"NOMBRE_AGENTE_DUENIO"				NVARCHAR(100) NOT NULL,
--"TELEFONO_AGENTE"					NVARCHAR(20) NOT NULL,
--"CORREO_AGENTE"						NVARCHAR(80) NOT NULL,

--"NOTIFICACION_MAIL"					NVARCHAR(1) NOT NULL,
--"GENERACION_CARTA"					NVARCHAR(1) NOT NULL,
--"CORREO_CLIENTE"					NVARCHAR(80) NOT NULL,

--"DIRECCION_CLIENTE"					NVARCHAR(200) NOT NULL,
--"GENERACION_ALERTA"					NVARCHAR(1) NOT NULL,
PRIMARY KEY ("IdEjecucion")
);


									    								     									     
 /*==============================================================*/
/* Sequences:       DROP & CREATE                               */
/*==============================================================*/
drop sequence "BSCL.Normativa.BD.Secuencias::EjecucionOrdenes";
create sequence "BSCL.Normativa.BD.Secuencias::EjecucionOrdenes" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;





