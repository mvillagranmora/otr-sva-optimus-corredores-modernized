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
-- LIBERACION SEMANA: 18-03-2016
--


--################################################
--      NROJAS <AJUSTES TESORERIA>  
--################################################


drop table "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" cascade;
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" (
 "IdIngresoEgresoFormaDePago" 			BIGINT  		NOT NULL,
 "IdIngresoEgreso" 						BIGINT  		NOT NULL,
 "IdTipoMovimiento"                 	NVARCHAR(6)     NOT NULL,  -- MOVING: Ingreso   MOVEGR: Egreso
 --"SentidoPago"                      NVARCHAR(1)     NOT NULL,  -- C: Corredora      S: Socio Negocio
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
 "FechaVctoTarjetaBancariaCorredora" 	SECONDDATE		DEFAULT '0001-01-01',
 "NumeroSerieCorredora"   				NVARCHAR(40)   	NOT NULL default '',
 "NumeroChequeCorredora"   	        	NVARCHAR(100)  	NOT NULL default '',
 "FechaEmisionChequeCorredora" 			SECONDDATE		DEFAULT '0001-01-01',
 "IdMonedaCorredora" 					NVARCHAR(3) 	NOT NULL, --Asociada o a la Cuenta Bancaria, Tarjeto o cheque
 
 "IdCuentaBancariaSocio" 				BIGINT  		DEFAULT 0, -- Cuenta del cliente, tercero o corredora,
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
 "NumeroChequeSocio"   	        		NVARCHAR(100)  	NOT NULL default '',
 "FechaEmisionChequeSocio" 				SECONDDATE		DEFAULT '0001-01-01',
 "IdMonedaSocio" 						NVARCHAR(3) 	NOT NULL,--Asociada o a la Cuenta Bancaria, Tarjeto o cheque
 
 --"IdBanco" 							BIGINT  		DEFAULT 0, 
 --"NombreBanco" 						NVARCHAR(100) 	DEFAULT '',
 --"Sucursal" 						NVARCHAR(250) 	DEFAULT '',
 
 "Monto" 								DECIMAL(26,8)  	DEFAULT 0,
 "FechaIngresoRegistro" 				SECONDDATE 		NOT NULL,
 "IdEstado" 							NVARCHAR(3) 	NOT NULL, 
PRIMARY KEY ("IdIngresoEgresoFormaDePago") );



drop table "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" cascade;
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" (
 "IdComprobanteFormaDePago" 			BIGINT  		NOT NULL,
 "IdComprobante" 						BIGINT  		NOT NULL,
 "IdTipoMovimiento"                 	NVARCHAR(6)     NOT NULL,  -- MOVING: Ingreso   MOVEGR: Egreso
 --"SentidoPago"                      NVARCHAR(1)     NOT NULL,  -- C: Corredora      S: Socio Negocio
 "IdMedioDePago" 						NVARCHAR(3) 	NOT NULL,
 "NombreMedioDePago" 					NVARCHAR(100) 	DEFAULT '', 
 "IdModalidadPago"                  	NVARCHAR(6)     NOT NULL,  -- MODCAJ: Por Caja   MODDEP: Deósito en Cta. Cte
 "NombreModalidadDePago" 				NVARCHAR(100) 	DEFAULT '',
 
 "IdCuentaBancariaCorredora" 			BIGINT  		DEFAULT 0, -- Cuenta del cliente, tercero o corredora
 "IdTipoCuentaBancariaCorredora"  	    NVARCHAR(4)     NOT NULL,
 "NombreTipoCuentaBancariaCorredora"    NVARCHAR(100)   NOT NULL default '' ,  
 "NombreCuentaBancariaCorredora" 		NVARCHAR(100) 	DEFAULT '', 
 "NumeroCuentaBancariaCorredora"   	    NVARCHAR(100)  	NOT NULL default '',
 "IdTarjetaBancariaCorredora" 			BIGINT  		DEFAULT 0,
 "IdTipoTarjetaBancariaCorredora" 		NVARCHAR(40) 	DEFAULT '',
 "NombreTipoTarjetaBancariaCorredora" 	NVARCHAR(100) 	DEFAULT '',
 "NumeroTarjetaBancariaCorredora"   	NVARCHAR(100)  	NOT NULL default '',
 "FechaVctoTarjetaBancariaCorredora" 	SECONDDATE		DEFAULT '0001-01-01',
 "NumeroSerieCorredora"   				NVARCHAR(40)   	NOT NULL default '',
 "NumeroChequeCorredora"   	        	NVARCHAR(100)  	NOT NULL default '',
 "FechaEmisionChequeCorredora" 			SECONDDATE		DEFAULT '0001-01-01',
 "IdMonedaCorredora" 					NVARCHAR(3) 	NOT NULL, --Asociada o a la Cuenta Bancaria, Tarjeto o cheque
 
 "IdCuentaBancariaSocio" 				BIGINT  		DEFAULT 0, -- Cuenta del cliente, tercero o corredora
 "IdTipoCuentaBancariaSocio"  	        NVARCHAR(4)     NOT NULL,
 "NombreTipoCuentaBancariaSocio"        NVARCHAR(100)   NOT NULL default '' ,  
 "NombreCuentaBancariaSocio" 			NVARCHAR(100) 	DEFAULT '', 
 "NumeroCuentaBancariaSocio"   	        NVARCHAR(100)  	NOT NULL default '',
 "IdTarjetaBancariaSocio" 				BIGINT  		DEFAULT 0,
 "IdTipoTarjetaBancariaSocio" 			NVARCHAR(40) 	DEFAULT '',
 "NombreTipoTarjetaBancariaSocio" 		NVARCHAR(100) 	DEFAULT '',
 "NumeroTarjetaBancariaSocio"   	    NVARCHAR(100)  	NOT NULL default '',
 "FechaVctoTarjetaBancariaSocio" 		SECONDDATE		DEFAULT '0001-01-01',
 "NumeroSerieSocio"   					NVARCHAR(40)   	NOT NULL default '',
 "NumeroChequeSocio"   	        		NVARCHAR(100)  	NOT NULL default '',
 "FechaEmisionChequeSocio" 				SECONDDATE		DEFAULT '0001-01-01', 
 "IdMonedaSocio" 						NVARCHAR(3) 	NOT NULL,--Asociada o a la Cuenta Bancaria, Tarjeto o cheque
 
 --"IdBanco" 							BIGINT  		DEFAULT 0, 
 --"NombreBanco" 						NVARCHAR(100) 	DEFAULT '',
 --"Sucursal" 							NVARCHAR(250) 	DEFAULT '', 
 
 "Monto" 								DECIMAL(26,8)  	DEFAULT 0,
 "FechaIngresoRegistro" 				SECONDDATE 		NOT NULL,
 "IdUsuarioCertificador"            	BIGINT          DEFAULT 0,
 "FechaCertificacion"               	TIMESTAMP       DEFAULT '0001-01-01', 
 "IdUsuarioGiro"            	        BIGINT          DEFAULT 0,
 "FechaGiro"               	            TIMESTAMP       DEFAULT '0001-01-01',
 "IdEstado" 							NVARCHAR(3) 	NOT NULL, 
PRIMARY KEY ("IdComprobanteFormaDePago") ); 


drop table  "BSCL.Tesoreria.BD::TES.NominaTransferencia"  cascade;
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.NominaTransferencia" (
 "IdNominaTransferencia"     	BIGINT 		  NOT NULL, 
 "IdEmpresa"  			     	BIGINT 		  NOT NULL,  
 "IdComprobantePartidaEgreso"	BIGINT 		  NOT NULL DEFAULT 0,
 "FechaMovimiento"				TIMESTAMP     NOT NULL DEFAULT '0001-01-01 00:00:00',  --Fecha sistema
 "FechaIngresoRegistro"			TIMESTAMP	  NOT NULL DEFAULT '0001-01-01 00:00:00', --CurrentDate
 "FechaLiquidacion"				DATE 		  NOT NULL DEFAULT '0001-01-01', -- 
 "FechaRecepcion"				DATE 		  NOT NULL DEFAULT '0001-01-01', -- Fecha en que el banco recibio con exito la nomina
 "IdMedioDePago"                NVARCHAR(3)   NOT NULL,
 "IdModalidadDePago"            NVARCHAR(6)   NOT NULL,  -- MODCAJ: Por Caja   MODDEP: Deósito en Cta. Cte
 "IdCuentaBanco" 				BIGINT        DEFAULT 0, -- Corredora
 "IdTipoCuentaBanco"  	    	NVARCHAR(4)   NOT NULL,
 "IdMoneda" 					NVARCHAR(3)   NOT NULL,
 "Monto" 						DECIMAL(26,8) DEFAULT 0, 
 "NombreArchivo"	            NVARCHAR(100) NOT NULL DEFAULT '', 
 "IdFormatoArchivo"				BIGINT 		  NOT NULL DEFAULT 0,
 "IdFormatoNomina"				BIGINT 		  NOT NULL DEFAULT 0,
 "EsNomina"                     NVARCHAR(1)   NOT NULL DEFAULT 'N',
 "EsTransferencia"              NVARCHAR(1)   NOT NULL DEFAULT 'N',
 "UsaTalonarioCheque"			NVARCHAR(1)	  NOT NULL DEFAULT 'N',  --S: Impresion de cheque por talorario, N: Impresión de cheque manual
 "IdUsuario"                    BIGINT	      NOT NULL,
 "IdEstado"				        NVARCHAR(3)   NOT NULL, 
PRIMARY KEY ("IdNominaTransferencia") );

alter table "BSCL.Tesoreria.BD::TES.NominaTransferencia"
   add constraint  FK_Empresa_NominaTransferencia foreign key ("IdEmpresa")
	      references "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.NominaTransferencia"
   add constraint  FK_MedioDePago_NominaTransferencia foreign key ("IdMedioDePago")
	      references "BSCL.Plataforma.BD::PAG.MedioDePago" ("IdMedioDePago") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.NominaTransferencia"
   add constraint  FK_CuentaBanco_NominaTransferencia foreign key ("IdCuentaBanco")
	      references "BSCL.Tesoreria.BD::TES.CuentaBanco" ("IdCuentaBanco") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.NominaTransferencia"
	   add constraint  FK_TipoCuentaBancaria_NominaTransferencia foreign key ("IdTipoCuentaBanco")
		      references "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" ("IdTipoCuentaBancaria") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.NominaTransferencia"
	   add constraint  FK_Moneda_NominaTransferencia foreign key ("IdMoneda")
		      references "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.NominaTransferencia" 
   add constraint  FK_Usuario_NominaTransferencia foreign key ("IdUsuario")
      references "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario") on delete restrict on update restrict;
      
alter table "BSCL.Tesoreria.BD::TES.NominaTransferencia"
   add constraint  FK_Estado_NominaTransferencia foreign key ("IdEstado")
	      references "BSCL.Tesoreria.BD::TES.EstadoTesoreria" ("IdEstadoTesoreria") on delete restrict on update restrict;


drop table  "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle"  cascade;
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" (
 "IdNominaTransferenciaDetalle" BIGINT 		  NOT NULL,
 "IdNominaTransferencia"     	BIGINT 		  NOT NULL,
 "IdComprobanteFormaDePago"		BIGINT 		  NOT NULL,
 "IdUsuario"                    BIGINT	      NOT NULL,
 "IdEstado"				        NVARCHAR(3)   NOT NULL, 
PRIMARY KEY ("IdNominaTransferenciaDetalle") );


/* Table: BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle */
alter table "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle"
   add constraint  FK_NominaTransferencia_NominaTransferenciaDetalle foreign key ("IdNominaTransferencia")
	      references "BSCL.Tesoreria.BD::TES.NominaTransferencia" ("IdNominaTransferencia") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle"
   add constraint  FK_ComprobanteFormaDePago_NominaTransferenciaDetalle foreign key ("IdComprobanteFormaDePago")
	      references "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" ("IdComprobanteFormaDePago") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" 
   add constraint  FK_Usuario_NominaTransferenciaDetalle foreign key ("IdUsuario")
      references "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario") on delete restrict on update restrict;
      
alter table "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle"
   add constraint  FK_Estado_NominaTransferenciaDetalle foreign key ("IdEstado")
	      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;


--------------------------------------------------
--      FIN <AJUSTES TESORERIA>
--------------------------------------------------

--################################################
--      JVILLAGRAN <AJUSTES TESORERIA>  
--################################################

-- Nueco concepto para ComprobanteFormaDePago
INSERT INTO "BSCL.Tesoreria.BD::TES.EstadoTesoreria"("IdEstadoTesoreria","IdEstado","NombreEntidad")
SELECT (MAX("IdEstadoTesoreria")+1),'GIR','TES.ComprobanteFormaDePago' From "BSCL.Tesoreria.BD::TES.EstadoTesoreria"; -- Girado


-- Nueva Tabla Preferencias Giro
drop table "BSCL.Tesoreria.BD::TES.PreferenciaGiro"   cascade;
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.PreferenciaGiro" (
 "IdPreferenciaGiro"      BIGINT 		 NOT NULL,
 "IdConcepto" 			  NVARCHAR(6) 	 NOT NULL,
 "IdMedioDePago"          NVARCHAR(3)    NOT NULL,
 "IdCuentaBanco"		  BIGINT     	 NOT NULL,
 "IdEstado"				  NVARCHAR(3) 	 NOT NULL, 
PRIMARY KEY ("IdPreferenciaGiro") );


/* constraint: */
alter table "BSCL.Tesoreria.BD::TES.PreferenciaGiro"
   add constraint  FK_MedioDePago_PreferenciaGiro foreign key ("IdMedioDePago")
	      references "BSCL.Plataforma.BD::PAG.MedioDePago" ("IdMedioDePago") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.PreferenciaGiro"
   add constraint  FK_Concepto_PreferenciaGiro foreign key ("IdConcepto")
	      references "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.PreferenciaGiro"
   add constraint  FK_CuentaBanco_PreferenciaGiro foreign key ("IdCuentaBanco")
	      references "BSCL.Tesoreria.BD::TES.CuentaBanco" ("IdCuentaBanco") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.PreferenciaGiro"
   add constraint  FK_Estado_PreferenciaGiro foreign key ("IdEstado")
	      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;


-- Nuevos Campos ComprobanteFormaDePago para control de giro
ALTER TABLE "BSCL"."BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"
ADD ("IdUsuarioGiro"    BIGINT    DEFAULT 0);

ALTER TABLE "BSCL"."BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"
ADD ("FechaGiro"    TIMESTAMP       DEFAULT '0001-01-01');

--------------------------------------------------
--      FIN <AJUSTES TESORERIA>
--------------------------------------------------
