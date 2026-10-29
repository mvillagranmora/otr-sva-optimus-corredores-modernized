
SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/

-- Comprobantes
drop table "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" cascade;
drop table "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" cascade;
drop table "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" cascade;
drop table "BSCL.Tesoreria.BD::TES.Comprobante" cascade;
--drop table "BSCL.Tesoreria.BD::TES.TipoComprobante" cascade;

-- Movimientos Ingreso/Egreso
drop table "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" cascade;
drop table "BSCL.Tesoreria.BD::TES.IngresoEgreso" cascade;

drop table "BSCL.Tesoreria.BD::TES.NominaTransferencia"  cascade;
drop table "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle"  cascade;
drop table "BSCL.Tesoreria.BD::TES.Deposito" cascade;
drop table "BSCL.Tesoreria.BD::TES.DepositoDetalle" cascade;

drop table "BSCL.Tesoreria.BD::TES.CajaMoneda" cascade;
drop table "BSCL.Tesoreria.BD::TES.CajaUsuario" cascade;
drop table "BSCL.Tesoreria.BD::TES.Caja" cascade;
drop table "BSCL.Tesoreria.BD::TES.Chequera" cascade;
drop table "BSCL.Tesoreria.BD::TES.ConfiguracionChequera" cascade;
drop table "BSCL.Tesoreria.BD::TES.CuentaBanco"   cascade;
drop table "BSCL.Tesoreria.BD::TES.HorarioBancosPais" cascade;
drop table "BSCL.Tesoreria.BD::TES.EfectivoDenominacion" cascade;
drop table "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria" cascade;
drop table "BSCL.Tesoreria.BD::TES.EstadoTesoreria"   cascade;
drop table "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" cascade;
drop table "BSCL.Tesoreria.BD::TES.ProcesoTesoreria"  cascade;
drop table "BSCL.Tesoreria.BD::TES.MonedaTesoreria" cascade;
DROP TABLE "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria" CASCADE;

DROP TABLE "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" CASCADE;
DROP TABLE "BSCL.Tesoreria.BD::TES.TipoDeposito"              CASCADE;
-- Configuracion de reportes(RPT) 
DROP TABLE "BSCL.Tesoreria.BD::TES.TemplateTesoreria" 	      CASCADE;
-- Configuracion de archivo de transferencias bancarias
DROP TABLE "BSCL.Tesoreria.BD::TES.FormatoArchivo" 			  CASCADE;
DROP TABLE "BSCL.Tesoreria.BD::TES.FormatoArchivoDetalle" 	  CASCADE;
DROP TABLE "BSCL.Tesoreria.BD::TES.HomologacionArchivo" 	  CASCADE;
DROP TABLE "BSCL.Tesoreria.BD::CIE.RevisionCierreDiario" 	  CASCADE;
DROP TABLE "BSCL.Tesoreria.BD::TES.EnvioDocumento"            CASCADE; -- Envio de documentos desde caja
DROP TABLE "BSCL.Tesoreria.BD::TES.EnvioDocumentoDetalle"     CASCADE;


/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.Caja" (
 "IdCaja"				BIGINT 			NOT NULL,
 "IdEmpresa"  			BIGINT 			NOT NULL,
 "Codigo"				NVARCHAR(40) 	NOT NULL,
 "Nombre"				NVARCHAR(100) 	NOT NULL,
 "IdEstado"				NVARCHAR(3) 	NOT NULL,
PRIMARY KEY ("IdCaja") );


CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.CajaMoneda" (
 "IdCajaMoneda"			BIGINT 			NOT NULL,
 "IdEmpresa"  			BIGINT 			NOT NULL,
 "IdCaja"				BIGINT 			NOT NULL,
 "IdMoneda"				NVARCHAR(3) 	NOT NULL,
 "Saldo"				DECIMAL(26,8) 	DEFAULT 0,
 "IdTipoMovimiento"     NVARCHAR(6)     NOT NULL,  -- MOVING: Ingreso   MOVEGR: Egreso    TODOS
 "PorDefecto"           NVARCHAR(1) 	NOT NULL default 'N',  -- S / N caja por defecto
 "IdEstado"				NVARCHAR(3) 	NOT NULL,
PRIMARY KEY ("IdCajaMoneda") );



CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.CajaUsuario" (
 "IdCajaUsuario"		BIGINT 			NOT NULL,
 "IdEmpresa"  			BIGINT 			NOT NULL,
 "IdCaja"				BIGINT 			NOT NULL,
 "IdUsuario"			BIGINT			NOT NULL,
 "Sobregiro"            NVARCHAR(1) 	NOT NULL default 'N',  -- S / N 
 "FechaInicio"			DATE     		NOT NULL,
 "FechaTermino"			DATE 	    	NOT NULL,
 "IdEstado"				NVARCHAR(3) 	NOT NULL,
PRIMARY KEY ("IdCajaUsuario") );

CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.CajaProductoEmpresa" (
 "IdCajaProductoEmpresa" BIGINT 		NOT NULL,
 "IdEmpresa"  			BIGINT 			NOT NULL,
 "IdCaja"				BIGINT 			NOT NULL,
 "IdProductoEmpresa"	BIGINT			NOT NULL,
 "IdEstado"				NVARCHAR(3) 	NOT NULL,
PRIMARY KEY ("IdCajaProductoEmpresa") );

CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.HorarioBancosPais" (
"IdHorarioBancosPais"	BIGINT 			NOT NULL,		
"IdPais"				BIGINT 			NOT NULL,
"HoraInicioLocal"		NVARCHAR(8) 	NOT NULL,
"HoraFinLocal"			NVARCHAR(8) 	NOT NULL,
"HoraInicioPais"		NVARCHAR(8) 	NOT NULL,
"HoraFinPais"			NVARCHAR(8) 	NOT NULL,
"IdEstado"				NVARCHAR(3) 	NOT NULL,
PRIMARY KEY ("IdHorarioBancosPais") );



 
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.CuentaBanco" (
 "IdCuentaBanco"			BIGINT     		NOT NULL,
 "IdEmpresa"  				BIGINT 			NOT NULL,
 "IdBanco"  				BIGINT     		NOT NULL,	--//[FK];CuentaBancaria;IdBanco;Banco;IdBanco  
 "NombreCuentaBancaria"  	NVARCHAR(20)    NOT NULL,
 "IdTipoCuentaBancaria"  	NVARCHAR(4)     NOT NULL,	--//[FK];CuentaBancaria;IdTipoCuentaBancaria;TipoCuentaBancaria;IdTipoCuentaBancaria
 "IdMoneda"  				NVARCHAR(3)     NOT NULL,	--//[FK];CuentaBancaria;IdMoneda;TipoMonedaCuenta;IdTipoMonedaCuenta
 "NumeroCuentaBancaria"   	NVARCHAR(100)   NOT NULL,
 "Sucursal"					NVARCHAR(100)   NOT NULL DEFAULT '',
 "Direccion"				NVARCHAR(100)   NOT NULL DEFAULT '',
 "IdPlaza"	                BIGINT          NOT NULL DEFAULT 0,
 "Contacto"					NVARCHAR(100)   NOT NULL DEFAULT '',
 "MedioContacto"			NVARCHAR(100)   NOT NULL DEFAULT '', 
 "SaldoInicial"				DECIMAL(26,8) 	DEFAULT 0,
 "AfectaPosicionInicial"    NVARCHAR(1)     NOT NULL DEFAULT 'N',  --//S:Si   N:Especial
 "GeneraNomina"             NVARCHAR(1)     NOT NULL DEFAULT 'N',
 "GeneraTransferencia"      NVARCHAR(1)     NOT NULL DEFAULT 'N',
 "IdEmpleadoFirma1"    	    BIGINT     		NOT NULL DEFAULT 0,
 "MontoSegundaFirma"    	DECIMAL(26,8) 	NOT NULL DEFAULT 0,
 "IdEmpleadoFirma2"    	    BIGINT     		NOT NULL DEFAULT 0,
 "IdEstado"					NVARCHAR(3) 	NOT NULL,
PRIMARY KEY ("IdCuentaBanco") );







CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.ConfiguracionChequera" (
 "IdConfiguracionChequera"	BIGINT			NOT NULL,
 "NombreConfChequera" 		NVARCHAR(100)	NOT NULL,
 "AnchoCheque"				DECIMAL(10,4)	NOT NULL,
 "AltoCheque"				DECIMAL(10,4)	NOT NULL, 
 "SigloImpreso"            	NVARCHAR(1) 	NOT NULL DEFAULT 'N',  -- S / N 
 "MontoFila"				DECIMAL(10,4)	NOT NULL,
 "MontoColumna"				DECIMAL(10,4)   NOT NULL,
 "ProvinciaFila"			DECIMAL(10,4)	NOT NULL,
 "ProvinciaColumna"			DECIMAL(10,4)	NOT NULL,
 "MesFila"					DECIMAL(10,4)	NOT NULL,
 "MesColumna"				DECIMAL(10,4)	NOT NULL,
 "AnioFila"					DECIMAL(10,4)	NOT NULL,
 "AnioColumna"				DECIMAL(10,4)	NOT NULL,
 "ExtendidoAFila"			DECIMAL(10,4)	NOT NULL,
 "ExtendidoAColumna"		DECIMAL(10,4)	NOT NULL,
 "PrimeraCantidadFila"		DECIMAL(10,4)	NOT NULL,
 "PrimeraCantidadColumna"	DECIMAL(10,4)	NOT NULL,
 "SegundaCantidadFila"		DECIMAL(10,4)	NOT NULL,
 "SegundaCantidadColumna"	DECIMAL(10,4)	NOT NULL, 
PRIMARY KEY ("IdConfiguracionChequera") );

CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.Chequera" (
 "IdChequera"				BIGINT     		NOT NULL,
 "IdCuentaBanco"  			BIGINT     		NOT NULL,
 "FechaRetiro"				DATE     		NOT NULL,
 "Serie"   					NVARCHAR(40)   	NOT NULL,
 "CantidadCheques"   		INTEGER   		NOT NULL,
 "Remanente" 				INTEGER         NOT NULL,
 "NumeroChequeDesde"   		INTEGER   		NOT NULL,
 "NumeroChequeHasta"   		INTEGER   		NOT NULL,
 "NumeroChequeActual"		INTEGER         NOT NULL,
 "IdEstado"					NVARCHAR(3) 	NOT NULL,
 "IdConfiguracionChequera"	BIGINT,
PRIMARY KEY ("IdChequera") );


CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.EfectivoDenominacion" (
"IdEfectivoDenominacion"	BIGINT 			NOT NULL,
"IdMoneda"					NVARCHAR(3) 	NOT NULL,
"Denominacion"				NVARCHAR(40) 	NOT NULL,
"Tipo"						NVARCHAR(1) 	NOT NULL, -- M:Moneda  B:Billete
"Orden"						INTEGER			NOT NULL,
"IdEstado"					NVARCHAR(3) 	NOT NULL,
PRIMARY KEY ("IdEfectivoDenominacion") );


CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria" (
"IdEmpresa"	                BIGINT 			NOT NULL,
"CodigoParametro"           NVARCHAR(40) 	NOT NULL,
"Descripcion"       		NVARCHAR(520) 	NOT NULL, 
"Valor"					    NVARCHAR(520) 	NOT NULL,
PRIMARY KEY ("IdEmpresa", "CodigoParametro"));


CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" (
 "IdTipoMovimientoMedioDePago"				BIGINT		NOT NULL,
 "IdTipoMovimiento"				   			NVARCHAR(6)	NOT NULL,
 "IdMedioDePago" 							NVARCHAR(3) NOT NULL,
 "AfectaSaldoEnEstado"						NVARCHAR(40),
 "EstadosPosibleTipoMovimientoMedioDePago"	NVARCHAR(40),
 "AfectoADiasRetencion"						NVARCHAR(1),
 "DesplegarEnCombobox"						NVARCHAR(1),
PRIMARY KEY ("IdTipoMovimientoMedioDePago") );

 

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
 "IdCondicionLiquidacion"					NVARCHAR(2), 
 "FechaLiquidacion" 						DATE 		    NOT NULL, 
 "FechaIngresoRegistro"             		TIMESTAMP       NOT NULL,
 "FechaMovimiento"                 	 		DATE            NOT NULL, 
 "IdTipoPersona"      						NVARCHAR(3) 	NOT NULL,   -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)   
 "IdPersona"								BIGINT 			NOT NULL,
 "IdPortafolio"								BIGINT			NOT NULL DEFAULT 0,
 "NombrePersona"							NVARCHAR(300) 	NOT NULL, 
 "IdTipoDocumentoIdentidad" 	    		NVARCHAR(1) 	NOT NULL,
 "DocumentoIdentidad" 		        		NVARCHAR(40)	NOT NULL, 
 "IdTipoPersonaTitularPago"      			NVARCHAR(3) 	NOT NULL, -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
 "IdPersonaTitularPago"						BIGINT 			NOT NULL,
 "NombrePersonaTitularPago"					NVARCHAR(300) 	NOT NULL, 
 "IdPortafolioTitularPago"					BIGINT			NOT NULL DEFAULT 0,
 "IdTipoDocumentoIdentidadTitularPago" 	    NVARCHAR(1) 	NOT NULL,
 "DocumentoIdentidadTitularPago" 		    NVARCHAR(40)	NOT NULL, 
 "SecuenciaAgrupacion" 						NVARCHAR(40)  	NOT NULL DEFAULT '', --Aquellos mov con la misma secuencia no son mezclados en distintos comprobante
 "SecuenciaRelacionada"						BIGINT,			 					 --Permite relacionar distintos mov y generar un orden de liquidación entre ellos.
 "Glosa" 									NVARCHAR(260) 	NOT NULL DEFAULT '',
 
 "IdEmpleadoDigitador" 						BIGINT,								--Empleado que genero la operación 
 "IdSucursalEmpleadoDigitador" 				BIGINT,								--Sucursal del empleado que genero la operacion
 "IdTipoComprobanteOrigen"					NVARCHAR(3) 	NOT NULL DEFAULT '',
 "IdComprobanteOrigen"						BIGINT  		NOT NULL DEFAULT 0,
 "FechaComprobanteOrigen"					TIMESTAMP       NOT NULL DEFAULT '0001-01-01 00:00:00', 
 "FechaLiquidacionComprobanteOrigen"		SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
 "IdTipoProducto" 							NVARCHAR(4) 	NOT NULL DEFAULT '',
 "IdTipoOperacion" 							NVARCHAR(6) 	NOT NULL DEFAULT '',
 "IdOperacion" 								BIGINT,
 "FechaOperacion"							SECONDDATE, 
 "IdBolsa" 									NVARCHAR(3) 	NOT NULL DEFAULT '0',
 "IdMonedaOperacion" 						NVARCHAR(3) 	NOT NULL DEFAULT '',
 "MontoOperacion" 							DECIMAL(26,8) 	NOT NULL DEFAULT 0,
 "FechaParidad" 							SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
 "IdTipoReajuste" 							NVARCHAR(3) 	NOT NULL DEFAULT '',
 "ValorParidad" 							DECIMAL(26,8) 	NOT NULL DEFAULT 0,
 "FolioCartera" 							BIGINT  		NOT NULL DEFAULT 0,
 "IdInstrumento" 							BIGINT  		NOT NULL DEFAULT 0,
 "IdTipoInstrumento" 						NVARCHAR(3) 	NOT NULL DEFAULT '',
 "IdClasificacionTipoInstrumento" 			NVARCHAR(3) 	NOT NULL DEFAULT '', 
 
 "EsBilateral"								NVARCHAR(1) 	NOT NULL DEFAULT 'N', --asociado al cleaning
 "TitularPago"								NVARCHAR(1) 	NOT NULL DEFAULT 'S', --S: Socio que genera movimiento es quién cobra o paga
 "EsIngresoAutomatico"						NVARCHAR(1) 	NOT NULL DEFAULT 'N', --S: ingreso automático N: ingreso manual
 "AgrupaComprobante"             			NVARCHAR(1) 	NOT NULL DEFAULT 'S', --S: permite agrupar varios mov. en un comprobante
 "NeteaComprobante"							NVARCHAR(1) 	NOT NULL DEFAULT 'N', --S: permite netear mov de Egreso con mov de Ingreso 
 "MueveCuentaInversion"						NVARCHAR(1)		NOT NULL DEFAULT 'S', --S: mueve cuenta inversion N: No mueve cuenta inversion
 "IdUsuarioEmision" 						BIGINT 			NOT NULL DEFAULT 0,
 "FechaHoraEmision" 						SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
 "IdUsuarioBloqueo" 						BIGINT 			NOT NULL DEFAULT 0,
 "FechaHoraBloqueo" 						SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
 "IdUsuarioAnulacion" 						BIGINT 			NOT NULL DEFAULT 0,
 "FechaHoraAnulacion" 						SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00', 
 "IdCarga" 									BIGINT 			NOT NULL DEFAULT 0,  
 "IdEstado"                   				NVARCHAR(3) 	NOT NULL,
PRIMARY KEY ("IdIngresoEgreso") );





CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" (
 "IdIngresoEgresoFormaDePago" 			BIGINT  		NOT NULL,
 "IdIngresoEgreso" 						BIGINT  		NOT NULL,
 "IdTipoMovimiento"                 	NVARCHAR(6)     NOT NULL,  -- MOVING: Ingreso   MOVEGR: Egreso
 "IdMedioDePago" 						NVARCHAR(3) 	DEFAULT '',
 "NombreMedioDePago" 					NVARCHAR(100) 	DEFAULT '', 
 "IdModalidadDePago"                  	NVARCHAR(6)     DEFAULT '',  -- MODCAJ: Por Caja   MODDEP: Deósito en Cta. Cte
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



CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.Comprobante" (
"IdComprobante"		                	BIGINT 			NOT NULL,
"IdEmpresa" 							BIGINT 			NOT NULL,
"IdUsuario" 							BIGINT 			NOT NULL,
"TipoComprobante"						NVARCHAR(6)		NOT NULL, -- PARING: Partida Ingreso, PAREGR: Partida Egreso, '': Otro	
"IdTipoMovimiento"				    	NVARCHAR(6)		NOT NULL, -- MOVING: Ingreso   MOVEGR: Egreso
"IdConcepto" 							NVARCHAR(6) 	NOT NULL,
"IdCaja"								BIGINT 			NOT NULL,
"IdMoneda" 								NVARCHAR(3) 	NOT NULL,

"Monto" 								DECIMAL(26,8) 	NOT NULL,
"FechaLiquidacion" 						DATE 		    NOT NULL, -- Fecha en que debe ser liquidado el comprobante (T0,T1,T2 ... Tn)
"FechaIngresoRegistro"             		TIMESTAMP       NOT NULL, -- Fecha en que fue ingresado el registro en la BD
"FechaMovimiento"                 		DATE            NOT NULL, -- Fecha Sistema
"IdTipoPersona"      					NVARCHAR(3) 	NOT NULL, -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)   
"IdPersona"								BIGINT 			NOT NULL,
"NombrePersona"							NVARCHAR(300) 	NOT NULL, 
"IdPortafolio"							BIGINT			NOT NULL DEFAULT 0,
"IdTipoDocumentoIdentidad" 	    		NVARCHAR(1) 	NOT NULL,
"DocumentoIdentidad" 		        	NVARCHAR(40)	NOT NULL,
"TitularPago"							NVARCHAR(1) 	NOT NULL DEFAULT 'N', --N: si la persona es distinta a quién realiza el pago o cobro

"IdTipoPersonaTitularPago"      		NVARCHAR(3) 	NOT NULL, -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
"IdPersonaTitularPago"					BIGINT 			NOT NULL,
"NombrePersonaTitularPago"				NVARCHAR(300) 	NOT NULL, 
"IdPortafolioTitularPago"				BIGINT			NOT NULL DEFAULT 0,
"IdTipoDocumentoIdentidadTitularPago"	NVARCHAR(1) 	NOT NULL,
"DocumentoIdentidadTitularPago" 		NVARCHAR(40)	NOT NULL,

"SecuenciaAgrupacion" 					NVARCHAR(40)  	NOT NULL DEFAULT '',
"EsBilateral"							NVARCHAR(1) 	NOT NULL DEFAULT 'N', --asociado al cleaning
"ComprobanteAgrupado"             		NVARCHAR(1) 	NOT NULL DEFAULT 'N', --N: Comprobante no se puede mezclar con otros
"ComprobanteNeteado"					NVARCHAR(1) 	NOT NULL DEFAULT 'N', --S: Comprobante generado a partir del neteo de movimientos de Egreso e Ingreso
"SeparadoPorConcepto"					NVARCHAR(1) 	NOT NULL DEFAULT 'N', --N: Multiconceptos
"GeneradoDesdeMovimientos" 				NVARCHAR(1) 	NOT NULL DEFAULT 'S', --S: Comprobante generado a partir de movimientos, N: Comprobante generado a partir de comprobantes
"MueveCuentaInversion"					NVARCHAR(1)		NOT NULL DEFAULT 'S', --S: mueve cuenta inversion N: No mueve cuenta inversion
"IdUsuarioBloqueo" 						BIGINT 			NOT NULL DEFAULT 0,
"FechaHoraBloqueo" 						TIMESTAMP 		NOT NULL DEFAULT '0001-01-01',
"IdUsuarioAnulacion" 					BIGINT 			NOT NULL DEFAULT 0,
"FechaHoraAnulacion" 					SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
"IdCarga" 								BIGINT 			NOT NULL DEFAULT 0,
"IdEvento"                          	BIGINT          NOT NULL DEFAULT 0,  -- Id evento de autorización
"IdEstado"                   			NVARCHAR(3) 	NOT NULL,
PRIMARY KEY ("IdComprobante") );


CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" (
 "IdComprobanteFormaDePago" 			BIGINT  		NOT NULL,
 "IdComprobante" 						BIGINT  		NOT NULL,
 "IdTipoMovimiento"                 	NVARCHAR(6)     NOT NULL,  -- MOVING: Ingreso   MOVEGR: Egreso
 "IdMedioDePago" 						NVARCHAR(3) 	NOT NULL,
 "NombreMedioDePago" 					NVARCHAR(100) 	DEFAULT '', 
 "IdModalidadDePago"                  	NVARCHAR(6)     NOT NULL,  -- MODCAJ: Por Caja   MODDEP: Deósito en Cta. Cte
 "NombreModalidadDePago" 				NVARCHAR(100) 	DEFAULT '',
  
 "IdCuentaBancariaCorredora" 			BIGINT  		DEFAULT 0, 			-- Cuenta del cliente, tercero o corredora    
 "NombreCuentaBancariaCorredora" 		NVARCHAR(100) 	DEFAULT '', 
 "NumeroCuentaBancariaCorredora"   	    NVARCHAR(100)  	NOT NULL default '',
 "IdTipoCuentaBancariaCorredora"  	    NVARCHAR(4)     NOT NULL,
 "NombreTipoCuentaBancariaCorredora"    NVARCHAR(100)   NOT NULL default '' ,
 "IdTarjetaBancariaCorredora" 			BIGINT  		DEFAULT 0,
 "IdTipoTarjetaBancariaCorredora" 		NVARCHAR(40) 	DEFAULT '',
 "NombreTipoTarjetaBancariaCorredora" 	NVARCHAR(100) 	DEFAULT '', 
 "NumeroTarjetaBancariaCorredora"   	NVARCHAR(100)  	NOT NULL default '',
 "IdChequera"   						BIGINT,
 "NumeroDocumentoCorredora"           	BIGINT  		NOT NULL default 0, 
 "FechaDocumentoPagoCorredora" 			SECONDDATE		DEFAULT '0001-01-01',
 
 "IdCuentaBancariaSocio" 				BIGINT  		DEFAULT 0, 			-- Cuenta del cliente, tercero o corredora  
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
 
 "SaldoDisponible"						DECIMAL(26,8) 	DEFAULT 0,
 "SaldoContable" 						DECIMAL(26,8) 	DEFAULT 0,
 
 "IdPlaza"								BIGINT			NOT NULL DEFAULT 0,
 "DiasRetencion"						BIGINT			NOT NULL DEFAULT 0,
 "FechaAfectaSaldo"						TIMESTAMP       DEFAULT '0001-01-01',	-- Fecha en que la FP comienza a ser considerada a nivel de Saldo 
 "IdUsuarioCertificacion"    			BIGINT          DEFAULT 0,
 "FechaHoraCertificacion"        		TIMESTAMP       DEFAULT '0001-01-01',  
 "IdUsuarioLiquidacion"    				BIGINT          DEFAULT 0,
 "FechaHoraLiquidacion"        			TIMESTAMP       DEFAULT '0001-01-01',
 "FechaIngresoRegistro" 				SECONDDATE 		NOT NULL, 		 		-- Fecha en que fue ingresado el registro en la BD
 
 "IdUsuarioGiro"            	        BIGINT          DEFAULT 0,
 "FechaGiro"               	            TIMESTAMP       DEFAULT '0001-01-01',
 "IdComprobantePartida" 				BIGINT, 								--Id de la partida de egreso o ingreso a la cual quedo relacionado el comprobante
 "IdNominaTransferencia"				BIGINT,									--Id de la nomina o transferencia
 "IdDeposito"							BIGINT,									--Id del comprobante de deposito
 "Depositado"							NVARCHAR(1),
 "IdEstado" 							NVARCHAR(3) 	NOT NULL,
PRIMARY KEY ("IdComprobanteFormaDePago") ); 


CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" (
"IdComprobanteDetalle"						BIGINT 			NOT NULL,
"IdComprobante"		                		BIGINT 			NOT NULL,
"IdTipoMovimiento"                 			NVARCHAR(6)     NOT NULL,  -- MOVING: Ingreso   MOVEGR: Egreso
"IdConcepto" 								NVARCHAR(6) 	NOT NULL,
"NroLinea"                          		INTEGER         NOT NULL,
"IdMoneda" 									NVARCHAR(3) 	NOT NULL,
"Monto" 									DECIMAL(26,8) 	NOT NULL,
"SecuenciaAgrupacion" 						NVARCHAR(40)  	NOT NULL DEFAULT '',
"SecuenciaRelacionada"						BIGINT,
"IdEmpleadoDigitador" 						BIGINT  		NOT NULL DEFAULT 0,
"IdSucursalEmpleadoDigitador" 				BIGINT  		NOT NULL DEFAULT 0,
"IdTipoComprobanteOrigen"					NVARCHAR(3) 	NOT NULL DEFAULT '',
"IdComprobanteOrigen"						BIGINT  		NOT NULL DEFAULT 0,
"FechaComprobanteOrigen"					TIMESTAMP       NOT NULL DEFAULT '0001-01-01 00:00:00', 
"FechaLiquidacionComprobanteOrigen"			SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
"IdTipoProducto" 							NVARCHAR(4) 	NOT NULL DEFAULT '',
"IdTipoOperacion" 							NVARCHAR(6) 	NOT NULL DEFAULT '',
"IdBolsa"                           		NVARCHAR(3)     NOT NULL DEFAULT '',
"IdMonedaOperacion" 						NVARCHAR(3) 	NOT NULL DEFAULT '',
"MontoOperacion" 							DECIMAL(26,8) 	NOT NULL DEFAULT 0,
"FechaParidad" 								SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
"IdTipoReajuste" 							NVARCHAR(3) 	NOT NULL DEFAULT '',
"ValorParidad" 								DECIMAL(26,8) 	NOT NULL DEFAULT 0,
"FolioCartera" 								BIGINT  		NOT NULL DEFAULT 0,
"IdInstrumento" 							BIGINT  		NOT NULL DEFAULT 0,
"IdTipoInstrumento" 						NVARCHAR(3) 	NOT NULL DEFAULT '',
"IdClasificacionTipoInstrumento" 			NVARCHAR(3) 	NOT NULL DEFAULT '',
"IdCarga" 									BIGINT 			NOT NULL DEFAULT 0,
"IdEstado" 									NVARCHAR(3) 	NOT NULL, 
PRIMARY KEY ("IdComprobanteDetalle") );



CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" (
"IdComprobanteDetalleOrigen"	    BIGINT 			NOT NULL,
"IdComprobanteDetalle"				BIGINT 			NOT NULL,
"IdIngresoEgreso"					BIGINT,
"IdComprobante"					    BIGINT,
PRIMARY KEY ("IdComprobanteDetalleOrigen"));


CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.EstadoTesoreria" (
	"IdEstadoTesoreria" 		BIGINT          NOT NULL,
	"IdEstado"                	NVARCHAR(3),
	"NombreEntidad"           	NVARCHAR(100), 
	PRIMARY KEY ("IdEstadoTesoreria")
);

CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" (
	"IdConcepto" 				NVARCHAR(6)		not null,
	"NombreEntidad" 			NVARCHAR(100),
	"MultipleConcepto" 			NVARCHAR(1)		default 'N',
	"Tipo"                  	NVARCHAR(10),
	"DesplegarEnComboBox"   	NVARCHAR(1)     default 'S',
	"DisponibleIngresoManual"	NVARCHAR(1)     default 'S',
	"VisiblePorEjecutivo"   	NVARCHAR(1)     default 'N',
	"Origen" 					NVARCHAR(3),
	PRIMARY KEY ("IdConcepto")
);


CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.ProcesoTesoreria" (
 "IdProceso"			   BIGINT 			NOT NULL, 
 "IdEmpresa"  			   BIGINT 			NOT NULL, 
 "Codigo"				   NVARCHAR(40) 	NOT NULL, 
 "FechaProceso"            DATE     		NOT NULL,
 "FechaSistema"            DATE     		NOT NULL,
 "Observaciones"           NVARCHAR(250)    NOT NULL  DEFAULT '',
 "TransaccionesAfectadas"  BIGINT           NOT NULL  DEFAULT 0,
 "IdUsuario"               BIGINT	        NOT NULL,
 "IdEstado"				   NVARCHAR(3) 	    NOT NULL,
PRIMARY KEY ("IdProceso") );



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

CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" (
 "IdNominaTransferenciaDetalle" BIGINT 		  NOT NULL,
 "IdNominaTransferencia"     	BIGINT 		  NOT NULL,
 "IdComprobanteFormaDePago"		BIGINT 		  NOT NULL,
 "IdUsuario"                    BIGINT	      NOT NULL,
 "IdEstado"				        NVARCHAR(3)   NOT NULL, 
PRIMARY KEY ("IdNominaTransferenciaDetalle") );


CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.Deposito" (
 "IdDeposito"     			BIGINT 		  NOT NULL,
 "IdEmpresa"  			    BIGINT 		  NOT NULL,
 "FechaMovimiento"			TIMESTAMP     NOT NULL DEFAULT '0001-01-01 00:00:00',  --Fecha sistema
 "FechaIngresoRegistro"		TIMESTAMP	  NOT NULL DEFAULT '0001-01-01 00:00:00', --CurrentDate

 "IdTipoDeposito"			NVARCHAR(6)	  NOT NULL,
 "IdTipoCuentaBanco"  	    NVARCHAR(4)   NOT NULL,
 "IdCuentaBanco" 			BIGINT        DEFAULT 0, -- Corredora
 "IdMoneda" 				NVARCHAR(3)   NOT NULL,
 "Monto" 					DECIMAL(26,8) DEFAULT 0, 
 "IdUsuarioLiquidador"    	BIGINT        DEFAULT 0,
 "FechaHoraLiquidacion"		TIMESTAMP     DEFAULT '0001-01-01 00:00:00',
 "IdConcepto"				NVARCHAR(6),
 "Descripcion"				NVARCHAR(520),
 "IdUsuario"				BIGINT	      NOT NULL,
 "IdEstado"					NVARCHAR(3)   NOT NULL,
PRIMARY KEY ("IdDeposito") );


CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.DepositoDetalle" (
 "IdDepositoDetalle" 		BIGINT 		  NOT NULL,
 "IdDeposito"     			BIGINT 		  NOT NULL,
 "IdMedioDePago"			NVARCHAR(3)   NOT NULL, 
 "IdTipoDeposito"			NVARCHAR(6)	  NOT NULL, --Mismo Banco, Otro Banco, Efectivo
 "IdComprobanteFormaDePago"	BIGINT 		  NOT NULL, 
 "IdConcepto"				NVARCHAR(6),
 "Descripcion"				NVARCHAR(520),	
 "IdEstado"					NVARCHAR(3)   NOT NULL, 
PRIMARY KEY ("IdDepositoDetalle") );


CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.TipoDeposito" (
 "IdTipoDeposito" 	NVARCHAR(6) 	NOT NULL ,
 "Nombre" 			NVARCHAR(100) 	NOT NULL ,
 "IdEstado" 		NVARCHAR(3),
PRIMARY KEY ("IdTipoDeposito")
);

CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.MonedaTesoreria" (
 "IdMoneda" 					NVARCHAR(3) NOT NULL, 
 "IdEmpresa"  			     	BIGINT 		NOT NULL,
 "IdUsuario"                    BIGINT	    NOT NULL,
 "FechaIngresoRegistro"			TIMESTAMP	NOT NULL DEFAULT '0001-01-01 00:00:00',
 "IdEstado" 					NVARCHAR(3)	NOT NULL,
PRIMARY KEY ("IdMoneda","IdEmpresa")
);

CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria" (
	"IdMedioDePago"				NVARCHAR(3) NOT NULL,	--Medio de pago plataforma
	"IdMoneda"					NVARCHAR(3) NOT NULL,	--Moneda tesoreria
	"IdEmpresa"					BIGINT		NOT NULL,
	"RequiereRevisionDocumento" NVARCHAR(1) NOT NULL DEFAULT  'N', -- Para envio/devolucion de documentos desde caja
	"IdEstado" 					NVARCHAR(3)	NOT NULL,
PRIMARY KEY ("IdMedioDePago","IdMoneda")	
);




-- Para configuración de reportes(RPT) de tesoreria
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.TemplateTesoreria" (
	"IdTemplateTesoreria"    NVARCHAR(6)     NOT NULL  DEFAULT '',	
	"NombreReporte"          NVARCHAR(100)   NOT NULL  DEFAULT '',
	"Descripcion"		 	 NVARCHAR(100) 	 NOT NULL  DEFAULT '',
	"IdServicio"     		 BIGINT   		 NOT NULL  DEFAULT 0,
	"FormatoSalida"  		 NVARCHAR(3)     NOT NULL  DEFAULT '',
	"IdEmpresa"  			 BIGINT 		 NOT NULL  DEFAULT 0,
	"IdEstado"				 NVARCHAR(3) 	 NOT NULL  DEFAULT '',
PRIMARY KEY ("IdTemplateTesoreria") );


CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.FormatoArchivo" (
	"IdFormatoArchivo"		BIGINT 			NOT NULL  DEFAULT 0,
	"IdBanco"		        BIGINT 			NOT NULL  DEFAULT 0,
 	"IdMoneda"				NVARCHAR(3) 	NOT NULL  DEFAULT '',
	"IdMedioDePago"     	NVARCHAR(3) 	NOT NULL  DEFAULT '',
	"FormatoSalida"			NVARCHAR(3) 	NOT NULL  DEFAULT '', -- TXT CSV
	"Descripcion"			NVARCHAR(100) 	NOT NULL  DEFAULT '',
	"IdEmpresa"  			BIGINT 			NOT NULL  DEFAULT 0,
	"TipoGlosa"				INTEGER         NOT NULL  DEFAULT 0,
	"Servicio"			    NVARCHAR(250) 	NOT NULL  DEFAULT '',
	"IdEstado"				NVARCHAR(3) 	NOT NULL  DEFAULT '',
PRIMARY KEY ("IdFormatoArchivo") );

CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.FormatoArchivoDetalle" (
	"IdFormatoArchivoDetalle"		BIGINT 			NOT NULL DEFAULT 0,	
	"IdFormatoArchivo"				BIGINT 			NOT NULL DEFAULT 0,	
	"TipoRegistro"					INTEGER   		NOT NULL DEFAULT 0,	
	"Campo"							NVARCHAR(50) 	NOT NULL DEFAULT '',	
	"TipoDato"						NVARCHAR(50) 	NOT NULL DEFAULT '',	
	"Largo"	                        INTEGER   		NOT NULL DEFAULT 0,	
	"Decimales"	                    INTEGER   		NOT NULL DEFAULT 0,	
	"ValorDefecto"					NVARCHAR(100) 	NOT NULL DEFAULT '',	
	"Orden"	                        INTEGER   		NOT NULL DEFAULT 0,	
	"Descripcion"					NVARCHAR(100) 	NOT NULL DEFAULT '',	
PRIMARY KEY ("IdFormatoArchivoDetalle"));

CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.HomologacionArchivo" (
	"IdHomologacionArchivo"		    BIGINT 			NOT NULL DEFAULT 0,
	"IdFormatoArchivo"				BIGINT 			NOT NULL DEFAULT 0,
	"Campo"							NVARCHAR(50) 	NOT NULL DEFAULT '',
	"ValorOptimus"					NVARCHAR(100) 	NOT NULL DEFAULT '',
	"ValorExterno"					NVARCHAR(100) 	NOT NULL DEFAULT '',
	"Descripcion"					NVARCHAR(100) 	NOT NULL DEFAULT '',
PRIMARY KEY ("IdHomologacionArchivo"));


CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor" (
	"IdPreferenciasPagoCorredor"  	BIGINT  		NOT NULL DEFAULT 0,
	"IdTipoMovimiento"  			NVARCHAR(6)		NOT NULL DEFAULT '',
	"IdConcepto"       				NVARCHAR(6)		NOT NULL DEFAULT '',
	"IdMedioDePago"    				NVARCHAR(3)		NOT NULL DEFAULT '',
	"IdCuentaBanco"    				BIGINT 			NOT NULL DEFAULT 0,
	"UnicaOpcionDePago"  			NVARCHAR(1)		NOT NULL DEFAULT '',	
	"IdEmpresa"    					BIGINT 			NOT NULL DEFAULT 0,
	"IdEstado"  					NVARCHAR(3) 	NOT NULL DEFAULT '',
PRIMARY KEY ("IdPreferenciasPagoCorredor") );



CREATE COLUMN TABLE "BSCL.Tesoreria.BD::CIE.RevisionCierreDiario" ( 
	"IdRevisionCierreDiario" 		BIGINT 			NOT NULL DEFAULT 0,
	"NombreFuncionProceso"   		NVARCHAR(100) 	NOT NULL DEFAULT '',
	"IdUsuario"  			 		BIGINT 			NOT NULL DEFAULT 0,
	"FechaCierre"			 		SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaRegistro"			 		SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
	"TotalOperaciones"       		INTEGER  		NOT NULL DEFAULT 0,
	"TotalConProblemas"      		INTEGER  		NOT NULL DEFAULT 0,
	"Resultado"              		NVARCHAR(520)	NOT NULL DEFAULT '',
	"IdEstado"				 		NVARCHAR(3) 	NOT NULL DEFAULT '',
PRIMARY KEY ("IdRevisionCierreDiario") );


-- Cabecera Envio de Documentos desde caja
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.EnvioDocumento" (
	"IdEnvioDocumento" 	BIGINT 			NOT NULL  DEFAULT 0,
	"IdCaja"			BIGINT 			NOT NULL  DEFAULT 0,
	"FechaHoraEnvio" 	SECONDDATE 		NOT NULL  DEFAULT '0001-01-01 00:00:00',
	"IdEmpresa"  		BIGINT 			NOT NULL  DEFAULT 0,
	"IdMedioDePago"     NVARCHAR(3)     NOT NULL  DEFAULT '',
	"IdMoneda"          NVARCHAR(3)     NOT NULL  DEFAULT '',
	"IdUsuario"			BIGINT			NOT NULL  DEFAULT 0,
	"IdEstado"			NVARCHAR(3) 	NOT NULL  DEFAULT '',
PRIMARY KEY ("IdEnvioDocumento") );


-- Detalle Envio de Documentos desde caja
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TES.EnvioDocumentoDetalle" (
	"IdEnvioDocumentoDetalle"	BIGINT 		 NOT NULL  DEFAULT 0,
	"IdEnvioDocumento" 			BIGINT 		 NOT NULL  DEFAULT 0,
	"IdComprobanteFormaDePago" 	BIGINT  	 NOT NULL  DEFAULT 0,
	"FechaHoraRevision" 		SECONDDATE 	 NOT NULL  DEFAULT '0001-01-01 00:00:00',
	"IdUsuarioRevision"			BIGINT		           DEFAULT null,
	"IdEstado"					NVARCHAR(3)  NOT NULL  DEFAULT '',
PRIMARY KEY ("IdEnvioDocumentoDetalle"));


/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/
/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.Caja"
	   add constraint  FK_Empresa_Caja foreign key ("IdEmpresa")
		      references "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa") on delete restrict on update restrict;

/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.CajaMoneda" 
	   add constraint  FK_Caja_CajaMoneda foreign key ("IdCaja")
	      references "BSCL.Tesoreria.BD::TES.Caja"  ("IdCaja") on delete restrict on update restrict;
	
	alter table "BSCL.Tesoreria.BD::TES.CajaMoneda" 
	   add constraint  FK_Moneda_CajaMoneda foreign key ("IdMoneda")
	      references "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.CajaMoneda"
	   add constraint  FK_Estado_CajaMoneda foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.CajaUsuario" 
	   add constraint  FK_Caja_CajaUsuario foreign key ("IdCaja")
	      references "BSCL.Tesoreria.BD::TES.Caja"  ("IdCaja") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.CajaUsuario" 
	   add constraint  FK_Usuario_CajaUsuario foreign key ("IdUsuario")
	      references "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.CajaUsuario"
	   add constraint  FK_Estado_CajaUsuario foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.CajaProductoEmpresa" 
	   add constraint  FK_Caja_CajaProductoEmpresa foreign key ("IdCaja")
	      references "BSCL.Tesoreria.BD::TES.Caja"  ("IdCaja") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.CajaProductoEmpresa" 
	   add constraint  FK_ProductoEmpresa_CajaProductoEmpresa foreign key ("IdProductoEmpresa")
	      references "BSCL.Plataforma.BD::INS.ProductoEmpresa" ("IdProductoEmpresa") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.CajaProductoEmpresa"
	   add constraint  FK_Estado_CajaProductoEmpresa foreign key ("IdEstado")
		  references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.HorarioBancosPais"
	   add constraint  FK_Pais_HorarioBancosPais foreign key ("IdPais")
		      references "BSCL.Plataforma.BD::REG.Pais" ("IdPais") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.HorarioBancosPais"
	   add constraint  FK_Estado_HorarioBancosPais foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.CuentaBanco"
	   add constraint  FK_Empresa_CuentaBanco foreign key ("IdEmpresa")
		      references "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.CuentaBanco"
	   add constraint  FK_Banco_CuentaBanco foreign key ("IdBanco")
		      references "BSCL.Plataforma.BD::BAN.Banco" ("IdBanco") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.CuentaBanco"
	   add constraint  FK_TipoCuentaBancaria_CuentaBanco foreign key ("IdTipoCuentaBancaria")
		      references "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" ("IdTipoCuentaBancaria") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.CuentaBanco"
	   add constraint  FK_Moneda_CuentaBanco foreign key ("IdMoneda")
		      references "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.CuentaBanco"
	   add constraint  FK_Estado_CuentaBanco foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.Chequera"
	   add constraint  FK_CuentaBanco_Chequera foreign key ("IdCuentaBanco")
		      references "BSCL.Tesoreria.BD::TES.CuentaBanco" ("IdCuentaBanco") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.Chequera"
	   add constraint  FK_Estado_Chequera foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.EfectivoDenominacion"
	   add constraint  FK_Moneda_EfectivoDenominacion foreign key ("IdMoneda")
		      references "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.EfectivoDenominacion"
	   add constraint  FK_Estado_EfectivoDenominacion foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_Empresa_IngresoEgreso foreign key ("IdEmpresa")
		      references "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_Modulo_IngresoEgreso foreign key ("IdModulo")
		      references "BSCL.Plataforma.BD::PLA.Modulo" ("IdModulo") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_Usuario_IngresoEgreso foreign key ("IdUsuario")
		      references "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_Concepto_IngresoEgreso foreign key ("IdConcepto")
		      references "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_Moneda_IngresoEgreso foreign key ("IdMoneda")
		      references "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_TipoDocumentoIdentidad_IngresoEgreso foreign key ("IdTipoDocumentoIdentidad")
		      references "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" ("IdTipoDocumentoIdentidad") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_Caja_IngresoEgreso foreign key ("IdCaja")
		      references "BSCL.Tesoreria.BD::TES.Caja" ("IdCaja") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_Estado_IngresoEgreso foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_TipoSocioDeNegocio_IngresoEgreso foreign key ("IdTipoPersona")
		      references "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio" ("IdTipoSocioDeNegocio") on delete restrict on update restrict;



/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago"
	   add constraint  FK_MedioDePago_IngresoEgresoFormaDePago foreign key ("IdMedioDePago")
		      references "BSCL.Plataforma.BD::PAG.MedioDePago" ("IdMedioDePago") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago"
	   add constraint  FK_Estado_IngresoEgresoFormaDePago foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago"
	   add constraint  FK_IngresoEgreso_IngresoEgresoFormaDePago foreign key ("IdIngresoEgreso")
		      references "BSCL.Tesoreria.BD::TES.IngresoEgreso" ("IdIngresoEgreso") on delete restrict on update restrict;


/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.Comprobante"
	   add constraint  FK_Empresa_Comprobante foreign key ("IdEmpresa")
		      references "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.Comprobante" 
	   add constraint  FK_Usuario_Comprobante foreign key ("IdUsuario")
	      references "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.Comprobante" 
	   add constraint  FK_Concepto_Comprobante foreign key ("IdConcepto")
	      references "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.Comprobante" 
	   add constraint  FK_Moneda_Comprobante foreign key ("IdMoneda")
	      references "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.Comprobante" 
	   add constraint  FK_Persona_Comprobante foreign key ("IdPersona")
	      references "BSCL.SocioNegocio.BD::PER.Persona" ("IdPersona") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.Comprobante"
	   add constraint  FK_TipoDocumentoIdentidad_Comprobante foreign key ("IdTipoDocumentoIdentidad")
	      references "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" ("IdTipoDocumentoIdentidad") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.Comprobante"
	   add constraint  FK_Estado_Comprobante foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;


/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"
	   add constraint  FK_Comprobante_ComprobanteFormaDePago foreign key ("IdComprobante")
		      references "BSCL.Tesoreria.BD::TES.Comprobante" ("IdComprobante") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"
	   add constraint  FK_MedioDePago_ComprobanteFormaDePago foreign key ("IdMedioDePago")
		      references "BSCL.Plataforma.BD::PAG.MedioDePago" ("IdMedioDePago") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"
	   add constraint  FK_Estado_ComprobanteFormaDePago foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: */
	/*alter table "BSCL.Tesoreria.BD::TES.ComprobanteDetalle"
	   add constraint  FK_Comprobante_ComprobanteDetalle foreign key ("IdComprobante")
		      references "BSCL.Tesoreria.BD::TES.Comprobante" ("IdComprobante") on delete restrict on update restrict;
    */
    
	alter table "BSCL.Tesoreria.BD::TES.ComprobanteDetalle"
	   add constraint  FK_Estado_ComprobanteDetalle foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: */
	/*alter table "BSCL.Tesoreria.BD::TES.ComprobanteDetalleIngresoEgreso"
	   add constraint  FK_ComprobanteDetalle_ComprobanteDetalleIngresoEgreso foreign key ("IdComprobanteDetalle")
		      references "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" ("IdComprobanteDetalle") on delete restrict on update restrict;
   */
   
	alter table "BSCL.Tesoreria.BD::TES.ComprobanteDetalleIngresoEgreso"
	   add constraint  FK_IngresoEgreso_ComprobanteDetalleIngresoEgreso foreign key ("IdIngresoEgreso")
		      references "BSCL.Tesoreria.BD::TES.IngresoEgreso" ("IdIngresoEgreso") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.ComprobanteDetalle"
	   add constraint  FK_Estado_ComprobanteDetalleIngresoEgreso foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.ConceptoTesoreria"
	   add constraint "FK_Concepto_ConceptoTesoreria" foreign key ("IdConcepto")
	      references "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto") on delete restrict on update restrict;

/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.EstadoTesoreria"
	   add constraint "FK_Estado_EstadoTesoreria" foreign key ("IdEstado")
	      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;
	      
	     
/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.Chequera"
	add constraint "FK_ConfiguracionChequera_Chequera" foreign key ("IdConfiguracionChequera")
	references "BSCL.Tesoreria.BD::TES.ConfiguracionChequera" ("IdConfiguracionChequera") on delete restrict on update restrict;


/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.ProcesoTesoreria"
	   add constraint  FK_Usuario_ProcesoTesoreria foreign key ("IdUsuario")
		  references "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario") on delete restrict on update restrict;


	alter table "BSCL.Tesoreria.BD::TES.ProcesoTesoreria"
	   add constraint  FK_Estado_ProcesoTesoreria foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;
		      
		      


/* Table: BSCL.Tesoreria.BD::TES.NominaTransferencia */
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
	      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

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


/* Table: */
alter table "BSCL.Tesoreria.BD::TES.MonedaTesoreria"
   add constraint  FK_Moneda_MonedaTesoreria foreign key ("IdMoneda")
	      references "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.MonedaTesoreria"
   add constraint  FK_Estado_MonedaTesoreria foreign key ("IdEstado")
	      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;


/* Table: */
alter table BSCL."BSCL.Tesoreria.BD::TES.TemplateTesoreria"
   add constraint  FK_Estado_TemplateTesoreria foreign key ("IdEstado")
	      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

alter table BSCL."BSCL.Tesoreria.BD::TES.TemplateTesoreria"
   add constraint  FK_Empresa_TemplateTesoreria foreign key ("IdEmpresa")
	      references "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa") on delete restrict on update restrict;	
	      
/* Table: */
alter table BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivo"
   add constraint  FK_Estado_FormatoArchivo foreign key ("IdEstado")
	      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

alter table BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivo"
   add constraint  FK_Empresa_FormatoArchivo foreign key ("IdEmpresa")
	      references "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa") on delete restrict on update restrict;	 
	      
alter table BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivo"
   add constraint  FK_Moneda_FormatoArchivo foreign key ("IdMoneda")
	      references "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda") on delete restrict on update restrict;

alter table BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivo"
   add constraint  FK_Banco_FormatoArchivo foreign key ("IdBanco")
	      references "BSCL.Plataforma.BD::BAN.Banco" ("IdBanco") on delete restrict on update restrict;	 
	      
/* Table: */
alter table BSCL."BSCL.Tesoreria.BD::TES.FormatoArchivoDetalle"
   add constraint  FK_FormatoArchivo_FormatoArchivoDetalle foreign key ("IdFormatoArchivo")
	      references "BSCL.Tesoreria.BD::TES.FormatoArchivo" ("IdFormatoArchivo") on delete restrict on update restrict;	
	      
/* Table: */
alter table BSCL."BSCL.Tesoreria.BD::TES.HomologacionArchivo"
   add constraint  FK_FormatoArchivo_HomologacionArchivo foreign key ("IdFormatoArchivo")
	      references "BSCL.Tesoreria.BD::TES.FormatoArchivo" ("IdFormatoArchivo") on delete restrict on update restrict;	
	      
	      
	      
-- Table: BSCL.Tesoreria.BD::TES.EnvioDocumento
alter table "BSCL.Tesoreria.BD::TES.EnvioDocumento"
   add constraint  FK_Empresa_EnvioDocumento foreign key ("IdEmpresa")
	      references "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.EnvioDocumento" 
   add constraint  FK_Usuario_EnvioDocumento foreign key ("IdUsuario")
      references "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.EnvioDocumento"
   add constraint  FK_Estado_EnvioDocumento foreign key ("IdEstado")
	      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;



-- Table: BSCL.Tesoreria.BD::TES.EnvioDocumentoDetalle
alter table "BSCL.Tesoreria.BD::TES.EnvioDocumentoDetalle"
   add constraint  FK_EnvioDocumento_EnvioDocumentoDetalle foreign key ("IdEnvioDocumento")
	      references "BSCL.Tesoreria.BD::TES.EnvioDocumento" ("IdEnvioDocumento") on delete restrict on update restrict;

alter table "BSCL.Tesoreria.BD::TES.EnvioDocumentoDetalle"
   add constraint  FK_Estado_EnvioDocumentoDetalle foreign key ("IdEstado")
	      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;
	      	      
/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/* Table: */
--create index "INX01_" on "BSCL.Plataforma.BD::GTA.("");


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
Commit
--Rollback

