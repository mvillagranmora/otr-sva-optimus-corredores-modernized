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

SET SCHEMA BSCL;
--
--
-- LIBERACION SEMANA: 31-03-2016
--


--################################################
--      NROJAS <AJUSTES MODELO TESORERIA>  
--################################################


drop table "BSCL.Tesoreria.BD::TES.IngresoEgreso" cascade;
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.IngresoEgreso" (
 "IdIngresoEgreso"							BIGINT 			NOT NULL,
 "IdEmpresa" 								BIGINT 			NOT NULL,
 "IdModulo"									NVARCHAR(3)		NOT NULL,
 "IdUsuario" 								BIGINT 			NOT NULL,
 "IdTipoMovimiento"                 		NVARCHAR(6)     NOT NULL,  -- MOVING: Ingreso   MOVEGR: Egreso
 "IdConcepto" 								NVARCHAR(6) 	NOT NULL,
 "IdCaja"									BIGINT 			NOT NULL,
 "IdMoneda" 								NVARCHAR(3) 	NOT NULL,
 "Monto" 									DECIMAL(26,8) 	NOT NULL, 
 "FechaLiquidacion" 						DATE 		    NOT NULL, 
 "FechaIngresoRegistro"             		TIMESTAMP       NOT NULL,
 "FechaMovimiento"                 	 		DATE            NOT NULL,
 "IdTipoPersona"      						NVARCHAR(3) 	NOT NULL,   -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)   
 "IdPersona"								BIGINT 			NOT NULL,
 "NombrePersona"							NVARCHAR(300) 	NOT NULL, 
 "IdTipoDocumentoIdentidad" 	    		NVARCHAR(1) 	NOT NULL,
 "DocumentoIdentidad" 		        		NVARCHAR(40)	NOT NULL,
 "IdTipoPersonaTitularPago"      			NVARCHAR(3) 	NOT NULL, -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
 "IdPersonaTitularPago"						BIGINT 			NOT NULL,
 "NombrePersonaTitularPago"					NVARCHAR(300) 	NOT NULL, 
 "IdPortafolioTitularPago"					BIGINT			NOT NULL DEFAULT 0,
 "SecuenciaAgrupacion" 						NVARCHAR(40)  	NOT NULL DEFAULT '',
 "Glosa" 									NVARCHAR(260) 	NOT NULL DEFAULT '',
 
 "IdAgente" 								BIGINT  		NOT NULL DEFAULT 0,
 "IdSucursal" 								BIGINT  		NOT NULL DEFAULT 0,
 "IdTipoComprobanteFacturacion"				NVARCHAR(3) 	NOT NULL DEFAULT '',
 "IdComprobanteFacturacion"					BIGINT  		NOT NULL DEFAULT 0,
 "FechaComprobanteFacturacion"				TIMESTAMP       NOT NULL DEFAULT '0001-01-01 00:00:00', 
 "FechaLiquidacionComprobanteFacturacion"	SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
 "IdTipoProducto" 							NVARCHAR(4) 	NOT NULL DEFAULT '',
 "IdTipoOperacion" 							NVARCHAR(6) 	NOT NULL DEFAULT '', 
 "IdOperacion" 								BIGINT,
 "FechaOperacion"							SECONDDATE,
 "IdCondicionLiquidacion"					NVARCHAR(2),
 "IdBolsa" 									NVARCHAR(3) 	NOT NULL DEFAULT '0',
 "FolioTransaccion" 						BIGINT  		NOT NULL DEFAULT 0,
 "FechaTransaccion" 						SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
 "IdMonedaOrigen" 							NVARCHAR(3) 	NOT NULL DEFAULT '',
 "MontoOrigen" 								DECIMAL(26,8) 	NOT NULL DEFAULT 0,
 "FechaParidad" 							SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
 "IdTipoReajuste" 							NVARCHAR(3) 	NOT NULL DEFAULT '',
 "ValorParidad" 							DECIMAL(26,8) 	NOT NULL DEFAULT 0,
 "IdCartera" 								BIGINT  		NOT NULL DEFAULT 0,
 "IdInstrumento" 							BIGINT  		NOT NULL DEFAULT 0,
 "IdTipoInstrumento" 						NVARCHAR(3) 	NOT NULL DEFAULT '',
 "IdClasificacionTipoInstrumento" 			NVARCHAR(3) 	NOT NULL DEFAULT '', 
 
 "EsBilateral"								NVARCHAR(1) 	NOT NULL DEFAULT 'N', --asociado al cleaning
 "TitularPago"								NVARCHAR(1) 	NOT NULL DEFAULT 'S', --S: Socio que genera movimiento es quién cobra o paga
 "EsIngresoAutomatico"						NVARCHAR(1) 	NOT NULL DEFAULT 'N', --S: ingreso automático N: ingreso manual
 "AgruparEnComprobante"             		NVARCHAR(1) 	NOT NULL DEFAULT 'S', --S: permite agrupar varios mov. en un comprobante
 "IdUsuarioBloqueo" 						BIGINT 			NOT NULL DEFAULT 0,
 "FechaHoraBloqueo" 						SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
 "IdCarga" 									BIGINT 			NOT NULL DEFAULT 0,  
 "IdEstado"                   				NVARCHAR(3) 	NOT NULL,
PRIMARY KEY ("IdIngresoEgreso") );



drop table "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" cascade;
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" (
 "IdIngresoEgresoFormaDePago" 			BIGINT  		NOT NULL,
 "IdIngresoEgreso" 						BIGINT  		NOT NULL,
 "IdTipoMovimiento"                 	NVARCHAR(6)     NOT NULL,  -- MOVING: Ingreso   MOVEGR: Egreso
 "IdMedioDePago" 						NVARCHAR(3) 	DEFAULT '',
 "NombreMedioDePago" 					NVARCHAR(100) 	DEFAULT '', 
 "IdModalidadPago"                  	NVARCHAR(6)     DEFAULT '',  -- MODCAJ: Por Caja   MODDEP: Deósito en Cta. Cte
 "NombreModalidadDePago" 				NVARCHAR(100) 	DEFAULT '',
 
 "IdCuentaBancariaCorredora" 			BIGINT  		DEFAULT 0, -- Cuenta del cliente, tercero o corredora,
 "IdTipoCuentaBancariaCorredora"  	    NVARCHAR(4)     DEFAULT '',
 "NombreTipoCuentaBancariaCorredora"    NVARCHAR(100)   DEFAULT '',  
 "NombreCuentaBancariaCorredora" 		NVARCHAR(100) 	DEFAULT '', 
 "NumeroCuentaBancariaCorredora"   	    NVARCHAR(100)  	DEFAULT '',
 "IdTarjetaBancariaCorredora" 			BIGINT  		DEFAULT 0,
 "IdTipoTarjetaBancariaCorredora" 		NVARCHAR(40) 	DEFAULT '',
 "NombreTipoTarjetaBancariaCorredora" 	NVARCHAR(100) 	DEFAULT '',
 "NumeroTarjetaBancariaCorredora"   	NVARCHAR(100)  	DEFAULT '',
 "IdChequera"   						BIGINT   		NOT NULL default 0,
 "NumeroDocumentoCorredora"         	BIGINT  		NOT NULL default 0,
 "FechaDocumentoPagoCorredora" 			SECONDDATE		DEFAULT '0001-01-01',

 "IdCuentaBancariaSocio" 				BIGINT  		DEFAULT 0, -- Cuenta del cliente, tercero o corredora,
 "IdBancoSocio" 				        BIGINT  		DEFAULT 0,
 "IdTipoCuentaBancariaSocio"  	        NVARCHAR(4)     DEFAULT '',
 "NombreTipoCuentaBancariaSocio"        NVARCHAR(100)   DEFAULT '',  
 "NombreCuentaBancariaSocio" 			NVARCHAR(100) 	DEFAULT '', 
 "NumeroCuentaBancariaSocio"   	        NVARCHAR(100)  	DEFAULT '',
 "IdTarjetaBancariaSocio" 				BIGINT  		DEFAULT 0,
 "IdTipoTarjetaBancariaSocio" 			NVARCHAR(40) 	DEFAULT '',
 "NombreTipoTarjetaBancariaSocio" 		NVARCHAR(100) 	DEFAULT '',
 "NumeroTarjetaBancariaSocio"   	    NVARCHAR(100)  	DEFAULT '',
 "FechaVctoTarjetaBancariaSocio" 		SECONDDATE		DEFAULT '0001-01-01',
 "NumeroSerieSocio"   					NVARCHAR(40)   	NOT NULL default '',
 "NumeroDocumentoSocio"   	       		BIGINT  		NOT NULL default 0,
 "FechaDocumentoPagoSocio" 				SECONDDATE		DEFAULT '0001-01-01',

 "IdMoneda" 							NVARCHAR(3) 	NOT NULL,
 "Monto" 								DECIMAL(26,8)  	DEFAULT 0,
 "FechaIngresoRegistro" 				SECONDDATE 		NOT NULL,
 "IdEstado" 							NVARCHAR(3) 	NOT NULL, 
PRIMARY KEY ("IdIngresoEgresoFormaDePago") );


drop table "BSCL.Tesoreria.BD::TES.Comprobante" cascade;
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.Comprobante" (
"IdComprobante"		                BIGINT 			NOT NULL,
"IdEmpresa" 						BIGINT 			NOT NULL,
"IdUsuario" 						BIGINT 			NOT NULL,
"TipoComprobante"					NVARCHAR(6)		NOT NULL, --Partida Ingreso, Partida Egreso, Otro
"IdTipoMovimiento"				    NVARCHAR(6)		NOT NULL, -- MOVING: Ingreso   MOVEGR: Egreso
"IdConcepto" 						NVARCHAR(6) 	NOT NULL,
"IdCaja"							BIGINT 			NOT NULL,
"IdMoneda" 							NVARCHAR(3) 	NOT NULL,
"Monto" 							DECIMAL(26,8) 	NOT NULL,
"FechaLiquidacion" 					DATE 		    NOT NULL,
"FechaIngresoRegistro"             	TIMESTAMP       NOT NULL,
"FechaMovimiento"                 	DATE            NOT NULL,
"IdTipoPersona"      				NVARCHAR(3) 	NOT NULL, -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)   
"IdPersona"							BIGINT 			NOT NULL,
"NombrePersona"						NVARCHAR(300) 	NOT NULL, 
"IdTipoDocumentoIdentidad" 	    	NVARCHAR(1) 	NOT NULL,
"DocumentoIdentidad" 		        NVARCHAR(40)	NOT NULL,
"TitularPago"						NVARCHAR(1) 	NOT NULL DEFAULT 'N', --S: si la persona es distinta a quién realiza el pago o cobro
"IdTipoPersonaTitularPago"      	NVARCHAR(3) 	NOT NULL, -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
"IdPersonaTitularPago"				BIGINT 			NOT NULL,
"NombrePersonaTitularPago"			NVARCHAR(300) 	NOT NULL, 
"IdPortafolioTitularPago"			BIGINT			NOT NULL DEFAULT 0,
"SecuenciaAgrupacion" 				NVARCHAR(40)  	NOT NULL DEFAULT '',
"EsBilateral"						NVARCHAR(1) 	NOT NULL DEFAULT 'N', --asociado al cleaning 
"SeparaPorTipoMovimiento"			NVARCHAR(1) 	NOT NULL DEFAULT 'N', --S: indica que el comprobante posee movimientos de ingreoss o egresos
"SeparaPorConcepto"					NVARCHAR(1) 	NOT NULL DEFAULT 'N', --N: Multiconceptos
"IdUsuarioBloqueo" 					BIGINT 			NOT NULL DEFAULT 0,
"FechaHoraBloqueo" 					TIMESTAMP 		NOT NULL DEFAULT '0001-01-01',
"IdUsuarioLiquida" 					BIGINT 			NOT NULL DEFAULT 0,
"FechaHoraLiquida" 					TIMESTAMP 		NOT NULL DEFAULT '0001-01-01',
"IdCarga" 							BIGINT 			NOT NULL DEFAULT 0, 
"IdEstado"                   		NVARCHAR(3) 	NOT NULL,
PRIMARY KEY ("IdComprobante") );


drop table "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" cascade;
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" (
 "IdComprobanteFormaDePago" 			BIGINT  		NOT NULL,
 "IdComprobante" 						BIGINT  		NOT NULL,
 "IdTipoMovimiento"                 	NVARCHAR(6)     NOT NULL,  -- MOVING: Ingreso   MOVEGR: Egreso
 "IdMedioDePago" 						NVARCHAR(3) 	NOT NULL,
 "NombreMedioDePago" 					NVARCHAR(100) 	DEFAULT '', 
 "IdModalidadPago"                  	NVARCHAR(6)     NOT NULL,  -- MODCAJ: Por Caja   MODDEP: Deósito en Cta. Cte
 "NombreModalidadDePago" 				NVARCHAR(100) 	DEFAULT '',
  
 "IdCuentaBancariaCorredora" 			BIGINT  		DEFAULT 0, -- Cuenta del cliente, tercero o corredora    
 "NombreCuentaBancariaCorredora" 		NVARCHAR(100) 	DEFAULT '', 
 "NumeroCuentaBancariaCorredora"   	    NVARCHAR(100)  	NOT NULL default '',
 "IdTipoCuentaBancariaCorredora"  	    NVARCHAR(4)     NOT NULL,
 "NombreTipoCuentaBancariaCorredora"    NVARCHAR(100)   NOT NULL default '' ,
 "IdTarjetaBancariaCorredora" 			BIGINT  		DEFAULT 0,
 "IdTipoTarjetaBancariaCorredora" 		NVARCHAR(40) 	DEFAULT '',
 "NombreTipoTarjetaBancariaCorredora" 	NVARCHAR(100) 	DEFAULT '', 
 "NumeroTarjetaBancariaCorredora"   	NVARCHAR(100)  	NOT NULL default '',
 "IdChequera"   						BIGINT   		NOT NULL default 0,
 "NumeroDocumentoCorredora"           	BIGINT  		NOT NULL default 0,
 "FechaDocumentoPagoCorredora" 			SECONDDATE		DEFAULT '0001-01-01',

 
 "IdCuentaBancariaSocio" 				BIGINT  		DEFAULT 0, -- Cuenta del cliente, tercero o corredora  
 "IdBancoSocio" 				        BIGINT  		DEFAULT 0, 
 "NombreCuentaBancariaSocio" 			NVARCHAR(100) 	DEFAULT '', 
 "NumeroCuentaBancariaSocio"   	        NVARCHAR(100)  	NOT NULL default '',
 "IdTipoCuentaBancariaSocio"  	        NVARCHAR(4)     NOT NULL,
 "NombreTipoCuentaBancariaSocio"        NVARCHAR(100)   NOT NULL default '' ,
 "IdTarjetaBancariaSocio" 				BIGINT  		DEFAULT 0,
 "IdTipoTarjetaBancariaSocio" 			NVARCHAR(40) 	DEFAULT '', 
 "NombreTipoTarjetaBancariaSocio" 		NVARCHAR(100) 	DEFAULT '', 
 "NumeroTarjetaBancariaSocio"   	    NVARCHAR(100)  	NOT NULL default '',
 "NumeroSerieSocio"   					NVARCHAR(40)   	NOT NULL default '',
 "NumeroDocumentoSocio"         		BIGINT  		NOT NULL default 0,
 "FechaDocumentoPagoSocio" 				SECONDDATE		DEFAULT '0001-01-01', 

 "IdMoneda" 							NVARCHAR(3) 	NOT NULL,
 "Monto" 								DECIMAL(26,8)  	DEFAULT 0,
 "FechaIngresoRegistro" 				SECONDDATE 		NOT NULL,
 "IdUsuarioCertificador"            	BIGINT          DEFAULT 0,
 "FechaCertificacion"               	TIMESTAMP       DEFAULT '0001-01-01', 
 "IdUsuarioGiro"            	        BIGINT          DEFAULT 0,
 "FechaGiro"               	            TIMESTAMP       DEFAULT '0001-01-01',
 "IdEstado" 							NVARCHAR(3) 	NOT NULL, 
PRIMARY KEY ("IdComprobanteFormaDePago") ); 


drop table "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" cascade;
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" (
"IdComprobanteDetalle"						BIGINT 			NOT NULL,
"IdComprobante"		                		BIGINT 			NOT NULL,
"IdConcepto" 								NVARCHAR(6) 	NOT NULL,
"NroLinea"                          		INTEGER         NOT NULL,
"IdMoneda" 									NVARCHAR(3) 	NOT NULL,
"Monto" 									DECIMAL(26,8) 	NOT NULL,
"SecuenciaAgrupacion" 						NVARCHAR(40)  	NOT NULL DEFAULT '',
"IdAgente" 									BIGINT  		NOT NULL DEFAULT 0,
"IdSucursal" 								BIGINT  		NOT NULL DEFAULT 0,
"IdTipoComprobanteFacturacion"				NVARCHAR(3) 	NOT NULL DEFAULT '',
"IdComprobanteFacturacion"					BIGINT  		NOT NULL DEFAULT 0,
"FechaComprobanteFacturacion"				TIMESTAMP       NOT NULL DEFAULT '0001-01-01 00:00:00', 
"FechaLiquidacionComprobanteFacturacion"	SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
"IdTipoProducto" 							NVARCHAR(4) 	NOT NULL DEFAULT '',
"IdTipoOperacion" 							NVARCHAR(6) 	NOT NULL DEFAULT '',
"IdBolsa"                           		NVARCHAR(3)     NOT NULL DEFAULT '',
"FolioTransaccion" 							BIGINT  		NOT NULL DEFAULT 0,
"FechaTransaccion" 							SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
"IdMonedaOrigen" 							NVARCHAR(3) 	NOT NULL DEFAULT '',
"MontoOrigen" 								DECIMAL(26,8) 	NOT NULL DEFAULT 0,
"FechaParidad" 								SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
"IdTipoReajuste" 							NVARCHAR(3) 	NOT NULL DEFAULT '',
"ValorParidad" 								DECIMAL(26,8) 	NOT NULL DEFAULT 0,
"IdCartera" 								BIGINT  		NOT NULL DEFAULT 0,
"IdInstrumento" 							BIGINT  		NOT NULL DEFAULT 0,
"IdTipoInstrumento" 						NVARCHAR(3) 	NOT NULL DEFAULT '',
"IdClasificacionTipoInstrumento" 			NVARCHAR(3) 	NOT NULL DEFAULT '',
"IdCarga" 									BIGINT 			NOT NULL DEFAULT 0,
"IdEstado" 									NVARCHAR(3) 	NOT NULL, 
PRIMARY KEY ("IdComprobanteDetalle") );


drop table "BSCL.Tesoreria.BD::TES.Deposito" cascade;
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.Deposito" (
 "IdDeposito"     			BIGINT 		  NOT NULL,
 "IdEmpresa"  			    BIGINT 		  NOT NULL,
 "FechaMovimiento"			TIMESTAMP     NOT NULL DEFAULT '0001-01-01 00:00:00',  --Fecha sistema
 "FechaIngresoRegistro"		TIMESTAMP	  NOT NULL DEFAULT '0001-01-01 00:00:00', --CurrentDate

 "IdMedioDePago"			NVARCHAR(3)   NOT NULL,
 "IdTipoCuentaBanco"  	    NVARCHAR(4)   NOT NULL,
 "IdCuentaBanco" 			BIGINT        DEFAULT 0, -- Corredora
 "IdMoneda" 				NVARCHAR(3)   NOT NULL,
 "Monto" 					DECIMAL(26,8) DEFAULT 0, 
 "FechaCertificacion"		DATE 		  DEFAULT '0001-01-01', -- Fecha en que el banco recibio con exito la nomina
 "IdUsuarioCertificador"	BIGINT		  DEFAULT 0,
 "IdUsuario"				BIGINT	      NOT NULL,
 "IdEstado"					NVARCHAR(3)   NOT NULL,
PRIMARY KEY ("IdDeposito") );


drop table "BSCL.Tesoreria.BD::TES.DepositoDetalle" cascade;
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.DepositoDetalle" (
 "IdDepositoDetalle" 		BIGINT 		  NOT NULL,
 "IdDeposito"     			BIGINT 		  NOT NULL,
 "TipoDeposito"				NVARCHAR(6)	  NOT NULL, --Mismo Banco, Otro Banco, Efectivo
 "IdComprobanteFormaDePago"	BIGINT 		  NOT NULL, 
 "IdEstado"					NVARCHAR(3)   NOT NULL, 
PRIMARY KEY ("IdDepositoDetalle") );



--########################################################################
--    NR - Creación se secuencias
--########################################################################

CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::Deposito"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::DepositoDetalle"
	   START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;


--########################################################################
--    NR - Nuevos Estados
--########################################################################

INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT (MAX("IdEstadoTesoreria")+1),'DEP','TES.ComprobanteFormaDePago' From "BSCL.Tesoreria.BD::TES.EstadoTesoreria";

INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT (MAX("IdEstadoTesoreria")+1),'ING','TES.Deposito' From "BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT (MAX("IdEstadoTesoreria")+1),'ANU','TES.Deposito' From "BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT (MAX("IdEstadoTesoreria")+1),'DEP','TES.Deposito' From "BSCL.Tesoreria.BD::TES.EstadoTesoreria";


INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT (MAX("IdEstadoTesoreria")+1),'ING','TES.DepositoDetalle' From "BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT (MAX("IdEstadoTesoreria")+1),'ANU','TES.DepositoDetalle' From "BSCL.Tesoreria.BD::TES.EstadoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT (MAX("IdEstadoTesoreria")+1),'DEP','TES.DepositoDetalle' From "BSCL.Tesoreria.BD::TES.EstadoTesoreria";


DELETE FROM "BSCL.Tesoreria.BD::TES.EstadoTesoreria" WHERE "IdEstado" = 'CRT' AND "NombreEntidad" = 'TES.MovimientoIngresoEgreso';

--########################################################################
--    NR - Eliminacion de Servicio que no se utiliza
--########################################################################
DROP "BSCL.Tesoreria.BD.Servicios::ConsultarMovimientoIngresoEgresoAsociadoASecuencia";


--########################################################################
--    NR - Incorporación de nuevos conceptos 
--########################################################################
ALTER TABLE "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ADD ("MultipleConcepto" NVARCHAR(1) 	NOT NULL DEFAULT 'N');

INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto")  
SELECT (MAX("IdConceptoTesoreria")+1),'MULCPT','TES.IngresoEgreso','S' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";

INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto")  
SELECT (MAX("IdConceptoTesoreria")+1),'PARING','TES.IngresoEgreso','S' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto")  
SELECT (MAX("IdConceptoTesoreria")+1),'PAREGR','TES.IngresoEgreso','S' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto")  
SELECT (MAX("IdConceptoTesoreria")+1),'EFTIVO','TES.IngresoEgreso','S' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto")  
SELECT (MAX("IdConceptoTesoreria")+1),'MIBNCO','TES.IngresoEgreso','S' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";
INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto")  
SELECT (MAX("IdConceptoTesoreria")+1),'OTBNCO','TES.IngresoEgreso','S' From "BSCL.Tesoreria.BD::TES.ConceptoTesoreria";



-------------------------------------------------
--      FIN <AJUSTES TESORERIA>
--------------------------------------------------
