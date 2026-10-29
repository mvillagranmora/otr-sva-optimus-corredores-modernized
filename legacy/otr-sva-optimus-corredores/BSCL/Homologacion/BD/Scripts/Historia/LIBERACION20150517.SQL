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
-- LIBERACION SEMANA: 10-05-2016
--
DROP TABLE "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoCarteraEnTransito";
CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoCarteraEnTransito" (
	 "IdMovimientoCarteraTransito" 			BIGINT 			not null,
	 "FechaIngresoRegistro" 				TIMESTAMP		default '0001-01-01' 	not null,
	 "FechaModificacionRegistro"			TIMESTAMP       default '0001-01-01' 	not null,
	 "FolioCarteraEgreso" 					BIGINT			default '0' 		 	not null,	 
	 "FolioCarteraRelacionada" 				BIGINT			default '0' 		 	not null,
	 "EsIngreso" 							NVARCHAR(1) 	default 'N'			 	not null,
	 "EsGarantia" 							NVARCHAR(1)  	default 'N'				not null,
	 "IdOrigenGarantia" 					NVARCHAR(3)  	default ''				not null,
	 "IdEspecieGarantia" 					BIGINT 			default '0' 			not null,
 	 "FolioCarteraGarantizada" 				BIGINT 		  	default '0' 			not null,
 	 "FolioCarteraOrigen"	 				BIGINT 		  	default '0' 			not null,
	 "EsEspecifica" 						NVARCHAR(1)   	default 'N' 			not null,
	 "IdInstrumentoGarantizado"	 			BIGINT        	default '0' 			not null,
	 "IdContraparteGarantia" 				BIGINT        	default '0' 			not null,
	 "IdEmpresa" 							BIGINT			default '0' 			not null,
	 "IdUsuario" 							BIGINT			default '0' 			not null,
	 "IdModulo" 							NVARCHAR(3) 	default '' 				not null,
	 "IdCliente" 							BIGINT			default '0' 			not null,
	 "IdPortafolio" 						BIGINT			default '0' 			not null,
	 "IdClienteRelacionado" 				BIGINT 			default '0' 			not null,
	 "IdPortafolioRelacionado" 				BIGINT 			default '0' 			not null,
	 "IdEstado" 							NVARCHAR(3) 	default '' 				not null,
	 "IdTipoProducto" 						NVARCHAR(4) 	default ''				not null,
	 "IdTipoCanalIngreso"					NVARCHAR(3) 	default '' 				not null,
	 "IdMonedaSistema"						NVARCHAR(3) 	default '' 				not null,
	 "CodigoIFRS" 							NVARCHAR(40) 	default '' 				not null,
	 "IdConcepto" 							NVARCHAR(6) 	default '' 				not null,
	 "IdTipoOperacion" 						NVARCHAR(6) 	default '' 				not null,	 
	 "IdOperacion" 							BIGINT			default '0' 			not null,
	 "FechaOperacion"						DATE            default '0001-01-01' 	not null,
	 "FechaVencimientoOperacion"			DATE            default '0001-01-01' 	not null,
	 "PlazoEnDiasOperacion"					BIGINT 			default '0' 			not null, 
	 "IdComprobanteFactReferencia"			BIGINT 			default '0' 			not null,
	 "FolioExternoReferencia" 				NVARCHAR(40) 	default '' 				not null,
	 "IdMesa" 								BIGINT			default '0' 			not null,
	 "IdBolsa" 								NVARCHAR(3) 	default '' 				not null,
	 "IdSucursal" 							BIGINT			default '0' 			not null,
	 "IdAgente" 							BIGINT			default '0' 			not null,
	 "IdCondicionLiquidacion" 				NVARCHAR(2) 	default '' 				not null,
	 "FechaLiquidacion" 					DATE            default '0001-01-01' 	not null,
	 "Glosa" 								NVARCHAR(500) 	default '' 				not null,
	 "EsNominal" 							NVARCHAR(1) 	default 'N' 			not null,
	 "EsIntraday" 							NVARCHAR(1) 	default 'N' 			not null,
	 "UsaGarantiaEspecifica" 				NVARCHAR(1) 	default 'N' 			not null,
	 "CobraPrimaAlInicio" 					NVARCHAR(1) 	default 'N' 			not null,
	 "IdTipoPrestamo" 						NVARCHAR(6)   	default ''  			not null, 						
	 "IdUsuarioReserva" 					BIGINT			default '0' 			not null,
	 "FechaReserva"							DATE            default '0001-01-01' 	not null,
	 "IdTipoPersona" 						NVARCHAR(3) 	default '' 				not null,
	 "IdPersona" 							BIGINT 			default 0 				not null,

	--INSTRUMENTO
	 "IdClasificacionTipoInstrumento" 		NVARCHAR(3) 	default '' 				not null,
	 "IdTipoInstrumento"					NVARCHAR(3) 	default '' 				not null,	 
	 "IdInstrumento" 						BIGINT			default '0' 			not null,
	 "Nemotecnico"							NVARCHAR(40)	default '' 				not null,
	 "IdEmisor" 							NVARCHAR(40) 	default '' 				not null,
	 "NemoSVS"	 							NVARCHAR(40) 	default '' 				not null,
	 "IdTipoReajuste" 						NVARCHAR(3) 	default '' 				not null,
	 "FechaEmisionInstrumento"				DATE            default '0001-01-01' 	not null,
	 "FechaVencimientoInstrumento"			DATE            default '0001-01-01' 	not null,
	 "EsFungible" 							NVARCHAR(1) 	default 'N'				not null,
	 "IdBoveda" 							NVARCHAR(3) 	default '' 				not null,
	 "IdMonedaInstrumento"					NVARCHAR(3) 	default '' 				not null,
	 
	 --CANTIDADES Y MONTOS
	 "Cantidad" 							DECIMAL(26,8)	default '0' 			not null,
	 "CorteMinimo" 							DECIMAL(26,8)	default '0' 			not null,
	 "ValorRescate" 						DECIMAL(26,8) 	default '0' 			not null,
	 "Precio"								DECIMAL(26,8) 	default '0' 			not null,
	 "Tasa" 								DECIMAL(26,8) 	default '0' 			not null,
	 "TasaAgente" 							DECIMAL(26,8) 	default '0' 			not null,
	 "Monto"		 						DECIMAL(26,8) 	default '0' 			not null,  --monto inicial
	 "MontoEnMonedaInstrumento"		 		DECIMAL(26,8) 	default '0' 			not null,  --monto inicial
	 "MontoAPlazo" 							DECIMAL(26,8) 	default '0' 			not null,
	 "PrecioAPlazo" 						DECIMAL(26,8) 	default '0' 			not null,
	 -- Necesarios para la migracion inicial de movimientos 
	 "IdCarga" 					  			BIGINT 			default '0' 			not null,
	 --- Datos Sebra en formato sebra 
	 "RUT_CLI"								NVARCHAR(11)	default '' 			    not null,	
	 "SEC_RUT_CLI"							BIGINT			default '0' 			not null,
	 "SEC_CAR_INVER"                        BIGINT          default '0' 			not null,
	 "COD_PER"								NVARCHAR(10)    default '' 			    not null,
	 "FOLIO_COMP_ADJ"                       BIGINT          default '0' 			not null,
	 "TIPO_COMP_ADJ"						NVARCHAR(10)	default '' 				not null, 
     "LIN_COMP_ADJ"                         BIGINT          default '0' 			not null,
	 "LIN_ORDEN"							BIGINT			default '0' 			not null,
	 "SEC_ORDEN"							BIGINT			default '0' 			not null,
	 "TIPO_CARTERA"							NVARCHAR(10)	default '' 			    not null,
	 "ID_ERROR"								BIGINT          default '0' 			not null,
	 "SEC_MOVTO"                            BIGINT          default '0' 			not null,
	 "CAN_GARANTIA"							DECIMAL(26,8)	default '0' 			not null,
	 PRIMARY KEY ("IdMovimientoCarteraTransito")
);

DROP TABLE "BSCL"."BSCL.Homologacion.BD::HOM.CargoAbonoMovimiento";
CREATE COLUMN TABLE "BSCL"."BSCL.Homologacion.BD::HOM.CargoAbonoMovimiento" (
	 "IdCargoAbonoMovimiento"            BIGINT         NOT NULL ,
	 "FechaSistema"                      DATE           NOT NULL ,
	 "IdModulo"                          NVARCHAR(3)    NOT NULL ,
	 "IdTipoMovimiento"                  NVARCHAR(6)    NOT NULL ,
	 "EsIngresoManual"                   NVARCHAR(1)    DEFAULT 'N',
	 "IdCuentaInversion"                 BIGINT         NOT NULL ,
	 "NombreCliente"                     NVARCHAR(300)  DEFAULT '',
	 "IdConcepto"                        NVARCHAR(6)    NOT NULL ,
	 "Descripcion"                       NVARCHAR(520)  DEFAULT '',
	 "IdCondicionDeLiquidacion"          NVARCHAR(2)    NOT NULL ,
	 "FechaEfectivaMovimiento"           DATE           NOT NULL ,
	 "Monto"                             DECIMAL(26,8)  NOT NULL ,
	 "Saldo"                             DECIMAL(26,8)  DEFAULT 0, --Registra saldo por fecha de liquidación
	 "SaldoPorFechaMovimiento"			 DECIMAL(26,8)  DEFAULT 0, --Registra saldo por fecha de movimiento 
	 "IdEvento"                          BIGINT         DEFAULT 0,
	 "EsCertificadoAutomatico"           NVARCHAR(1)    DEFAULT 'N',
	 "IdUsuarioCertificador"             BIGINT         DEFAULT 0,
	 "FechaCertificacion"                TIMESTAMP      DEFAULT '0001-01-01',
	 "IdTipoComprobante"                 NVARCHAR(3)    DEFAULT '',
	 "IdComprobante"                     BIGINT         DEFAULT 0,
	 "FechaComprobante"                  SECONDDATE     DEFAULT '0001-01-01',	 
	 "IdTipoProducto"                    NVARCHAR(4)    DEFAULT '',
	 "IdTipoOperacion"                   NVARCHAR(6)    DEFAULT '',
	 "IdOperacion"                       BIGINT         DEFAULT 0,
	 "FechaOperacion"                    SECONDDATE     DEFAULT '0001-01-01',
	 "FechaLiquidacionOperacion"         DATE           DEFAULT '0001-01-01',
	 "FechaVencimientoOperacion"         DATE           DEFAULT '0001-01-01',
	 "IdMonedaOperacion"                 NVARCHAR(3)    DEFAULT '',
	 "MontoOperacion"                    DECIMAL(26,8)  DEFAULT 0,
	 "TipoCambioOperacion"               DECIMAL(26,8)  DEFAULT 0,
	 "IdTipoInstrumento"                 NVARCHAR(3)    DEFAULT '',
	 "IdInstrumento"                     BIGINT         DEFAULT 0,
	 "Nemo"                              NVARCHAR(40)   DEFAULT '',
	 "EsCargoAbonoPorTraspaso"           NVARCHAR(1)    DEFAULT 'N',
	 "IdCargoAbonoRelacionadoTraspaso"   BIGINT         DEFAULT 0,
	 "EsIndicadorBilateral"              NVARCHAR(1)    DEFAULT 'N',
	 "IdContraparte"                     BIGINT         DEFAULT 0,
	 "IdPersonaPagoCobro"                BIGINT         DEFAULT 0,
	 "FechaIngresoRegistro"              SECONDDATE     NOT NULL ,
	 "IdUsuario"                         BIGINT         DEFAULT 0,
	 "IdEmpresa"                         BIGINT         NOT NULL ,
	 "IdEstado"                          NVARCHAR(3)    NOT NULL ,
	 "IdCarga"                		     BIGINT         DEFAULT 0 NOT NULL,
	 "SecuenciaMovimientoMigracion"      BIGINT         DEFAULT 0 NOT NULL,
	 "FechaMovimientoMigracion"			 DATE			DEFAULT '0001-01-01' NOT NULL, 	       
	 "RUT_CLI"							 NVARCHAR(11)   DEFAULT '' NOT NULL, 			
	 "SEC_RUT_CLI"						 BIGINT			DEFAULT '0' NOT NULL,
	 "FEC_MVTO"							 NVARCHAR(10)	DEFAULT '0001-01-01' NOT NULL,
	 "SEC_MOVTO"						 BIGINT			DEFAULT '0' NOT NULL,
	 "COD_MOVTO"						 NVARCHAR(10)	DEFAULT '' NOT NULL,		 	
	 "FOLIO_COMP_ADJ"					 BIGINT			DEFAULT '0' NOT NULL,	
	 "TIPO_COMP_ADJ"					 NVARCHAR(2)	DEFAULT '' NOT NULL,
	 "LIN_COMP_ADJ"						 BIGINT			DEFAULT '0' NOT NULL,
	 "COD_BOVEDA"						 NVARCHAR(3)	DEFAULT '' NOT NULL,
	 "COD_SUC"							 NVARCHAR(40)	DEFAULT '' NOT NULL,
	 "FEC_TRANS"                         NVARCHAR(10)	DEFAULT '0001-01-01' NOT NULL,
	 "FEC_LIQUID"                        NVARCHAR(10)	DEFAULT '0001-01-01' NOT NULL,
	 "FEC_ING_MVTO"						 NVARCHAR(10)	DEFAULT '0001-01-01' NOT NULL,
	 "IND_TIT_CCTE"                      NVARCHAR(1)	DEFAULT '' NOT NULL, 
	 "COD_PER"  	                     NVARCHAR(7)	DEFAULT '' NOT NULL, 	 
	 PRIMARY KEY ("IdCargoAbonoMovimiento")
 );

--################################################
--       <INICIALES DESARROLLADOR>.<MOTIVO>
--################################################

-- PEW PEW!

--------------------------------------------------
--      FIN <MOTIVO>
--------------------------------------------------
