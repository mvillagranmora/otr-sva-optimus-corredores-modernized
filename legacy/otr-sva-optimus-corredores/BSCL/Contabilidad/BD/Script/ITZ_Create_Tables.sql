CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.Centralizacion" ("IdCentralizacion" 		BIGINT 			NOT NULL,
																	   "IdTipoCentralizacion" 	BIGINT 			DEFAULT 0 NOT NULL,
																	   "IdEmpresa" 				BIGINT 			DEFAULT 0 NOT NULL,
																	   "FechaProceso" 			NVARCHAR(10) 	DEFAULT '' NOT NULL,
																	   "HoraProceso" 			NVARCHAR(8) 	DEFAULT '' NOT NULL,
																	   "FechaContable" 			NVARCHAR(10) 	DEFAULT '' NOT NULL,
																	   "Glosa" 					NVARCHAR(50) 	DEFAULT '' NOT NULL,
																	   "CodigoUsuario" 			NVARCHAR(40) 	DEFAULT '' NOT NULL,
																	   "Estado" 				NVARCHAR(1) 	DEFAULT '' NOT NULL,
																	   "TipoGeneracion" 		NVARCHAR(1) 	DEFAULT '' NOT NULL,
																	   PRIMARY KEY ("IdCentralizacion"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.CentralizacionParametro" ("IdParametroCentralizacion" BIGINT NOT NULL,
																				"IdTipoCentralizacion" 		BIGINT DEFAULT 0 NOT NULL,
																				"NombreParametro" 			NVARCHAR(20) DEFAULT '' NOT NULL,
																				"TipoDato" NVARCHAR(50) 	DEFAULT '' NOT NULL,
																				"TipoObjeto" NVARCHAR(3) 	DEFAULT '' NOT NULL,
																				"Largo" BIGINT CS_FIXED 	DEFAULT 0 NOT NULL,
																				"Formato" NVARCHAR(50) 		DEFAULT '' NOT NULL,
																				"Orden" BIGINT CS_FIXED 	DEFAULT 0 NOT NULL,
																				"Obligatorio" NVARCHAR(1) 	DEFAULT '' NOT NULL,
																				"Estado" NVARCHAR(1) 		DEFAULT '' NOT NULL,
																				PRIMARY KEY ("IdParametroCentralizacion"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.CentralizacionParametroValor" ("IdParametroCentralizacion" 	BIGINT NOT NULL,
																					 "Valor" NVARCHAR(60) 			DEFAULT '' NOT NULL,
																					 "Detalle" NVARCHAR(60) 		DEFAULT '' NOT NULL,
																					 "Estado" NVARCHAR(1) 			DEFAULT '' NOT NULL,
																					 PRIMARY KEY ("IdParametroCentralizacion", "Valor"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo" ("IdTipoCentralizacion" 	BIGINT DEFAULT 0 NOT NULL , 
																		   "IdEmpresa" 				BIGINT DEFAULT 0 NOT NULL,
																		   "Nombre" 				NVARCHAR(50) 	DEFAULT '' NOT NULL,
																		   "Nemo" 					NVARCHAR(20) DEFAULT '' NOT NULL,
																		   "Estado" 				NVARCHAR(1) DEFAULT '' NOT NULL,
																		   "TipoEnvio" 				NVARCHAR(1),
																		   PRIMARY KEY ("IdTipoCentralizacion"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento" ("IdDefinicionAsiento" BIGINT NOT NULL,
																					  "IdTipoContabilidad" 	BIGINT DEFAULT 0 NOT NULL,
																					  "IdCuentaContable" 	NVARCHAR(12) DEFAULT '' NOT NULL,
																					  "Ejercicio" 			BIGINT DEFAULT 0 NOT NULL,
																					  "Orden" 				BIGINT DEFAULT 0 NOT NULL,
																					  "DebeHaber" 			NVARCHAR(1) DEFAULT '' NOT NULL,
																					  "TipoEvento" 			NVARCHAR(1) DEFAULT '' NOT NULL,
																					  "Condicionado" 		NVARCHAR(1) DEFAULT '' NOT NULL,
																					  "OmiteCondicionCmbte" NVARCHAR(1) DEFAULT '' NOT NULL,
																					  "Estado" 				NVARCHAR(1) DEFAULT '' NOT NULL,
																					  "Formato" 			NVARCHAR(1) DEFAULT 'R' NOT NULL,
																					  PRIMARY KEY ("IdDefinicionAsiento"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionTipoEvento" ("IdTipoEvento" 		NVARCHAR(40) DEFAULT '0' NOT NULL,
																						 "IdDefinicionAsiento" 	BIGINT NOT NULL,
																						 "Operador" 			NVARCHAR(10) DEFAULT '' NOT NULL,
																						 "Estado" 				NVARCHAR(1) DEFAULT '' NOT NULL,
																						 PRIMARY KEY ("IdTipoEvento", "IdDefinicionAsiento"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.ContabilidadTipo" ("IdTipoContabilidad" 	BIGINT NOT NULL,
																		 "Nombre" 				NVARCHAR(60) DEFAULT '' NOT NULL,
																		 "Nemo" 				NVARCHAR(20) DEFAULT '' NOT NULL,
																		 "Tipo" 				NVARCHAR(1) DEFAULT '' NOT NULL,
																		 "Reversa" 				NVARCHAR(1) DEFAULT '' NOT NULL,
																		 "Agrupa" 				NVARCHAR(1) DEFAULT '' NOT NULL,
																		 "Condicionado" 		NVARCHAR(1) DEFAULT '' NOT NULL,
																		 "TipoGeneracion" 		NVARCHAR(1) DEFAULT '' NOT NULL,
																		 "IdEmpresa" 			BIGINT DEFAULT 0 NOT NULL,
																		 "Ejercicio" 			BIGINT DEFAULT 0 NOT NULL,
																		 "Formato" 				NVARCHAR(1) DEFAULT '' NOT NULL,
																		 "Estado" 				NVARCHAR(1) DEFAULT '' NOT NULL,
																		 PRIMARY KEY ("IdTipoContabilidad"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoAsientoCondicion" ("IdCondicion" 			BIGINT NOT NULL,
																						 "IdDefinicionAsiento" 	BIGINT NOT NULL,
																						 "IdTipoContabilidad" 	BIGINT DEFAULT 0 NOT NULL,
																						 "IdTipoDatoEvento" 	NVARCHAR(40) DEFAULT '' NOT NULL,
																						 "ValorVarchar" 		NVARCHAR(60) DEFAULT '' NOT NULL,
																						 "ValorDecimal" 		DECIMAL(18,4) DEFAULT 0 NOT NULL,
																						 "Condicion" 			NVARCHAR(10) DEFAULT '' NOT NULL,
																						 "Conector" 			NVARCHAR(10) DEFAULT '' NOT NULL,
																						 "TipoCondicion" 		NVARCHAR(1) DEFAULT '' NOT NULL,
																						 "Estado" 				NVARCHAR(1) DEFAULT '' NOT NULL,
																						 PRIMARY KEY ("IdCondicion"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoCentralizacionTipo" ("IdTipoCentralizacion" 	BIGINT DEFAULT 0 NOT NULL,
																						   "IdTipoContabilidad" 	BIGINT DEFAULT 0 NOT NULL,
																						   "Estado" NVARCHAR(1) 	DEFAULT '' NOT NULL,
																						   PRIMARY KEY ("IdTipoCentralizacion", "IdTipoContabilidad"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoGlosa" ("IdTipoContabilidad" 		BIGINT DEFAULT 0 NOT NULL,
																			  "IdDefinicionAsiento" 	BIGINT NOT NULL,
																			  "Origen" 					NVARCHAR(3) DEFAULT '' NOT NULL,
																			  "Orden" 					BIGINT DEFAULT 0 NOT NULL,
																			  "GlosaCampo" 				NVARCHAR(120) DEFAULT '' NOT NULL,
																			  "NombreCampoMovimiento" 	NVARCHAR(10) DEFAULT '' NOT NULL,
																			  "IdTipoDatoEvento" 		NVARCHAR(40) DEFAULT '' NOT NULL,
																			  "Estado" 					NVARCHAR(1) DEFAULT '' NOT NULL,
																			  PRIMARY KEY ("IdTipoContabilidad", "IdDefinicionAsiento", "Orden"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.Empresa" ("IdEmpresa" BIGINT DEFAULT 0 NOT NULL,
																"Nemo" 		NVARCHAR(10) DEFAULT '' NOT NULL,
																"Nombre" 	NVARCHAR(60) DEFAULT '' NOT NULL,
																"Rut" 		NVARCHAR(11) DEFAULT '' NOT NULL,
																"Estado" 	NVARCHAR(1) DEFAULT '' NOT NULL,
																PRIMARY KEY ("IdEmpresa"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.Evento" ("IdEvento" 			BIGINT NOT NULL,
															   "IdEmpresa" 			BIGINT DEFAULT 0 NOT NULL,
															   "IdTipoEvento" 		NVARCHAR(40) DEFAULT '0' NOT NULL,
															   "IdCentralizacion" 	BIGINT NOT NULL,
															   "IdMovimiento" 		BIGINT NOT NULL,
															   "FechaContable" 		NVARCHAR(10) DEFAULT '' NOT NULL,
															   "Monto" 				DECIMAL(18,2) DEFAULT 0 NOT NULL,
															   "CodigoMoneda" 		NVARCHAR(3) DEFAULT '' NOT NULL,
															   "CodigoUsuario" 		NVARCHAR(40) DEFAULT '' NOT NULL,
															   "FechaProceso" 		NVARCHAR(10) DEFAULT '' NOT NULL,
															   "HoraProceso" 		NVARCHAR(8) DEFAULT '' NOT NULL,
															   "Estado" 			NVARCHAR(1) DEFAULT '' NOT NULL,
															   PRIMARY KEY ("IdEvento"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" ("IdPreComprobanteAsiento" 	BIGINT NOT NULL,
																					"IdEvento" 					BIGINT NOT NULL,
																					"Operador" 					NVARCHAR(10) DEFAULT '' NOT NULL,
																					"Linea" 					BIGINT DEFAULT 0 NOT NULL,
																					"Estado" 					NVARCHAR(1) DEFAULT '' NOT NULL,
																					"GlosaDetalle" 				NVARCHAR(120) DEFAULT '' NOT NULL,
																					PRIMARY KEY ("IdPreComprobanteAsiento", "IdEvento"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.EventoTipo" ("IdTipoEvento" 	NVARCHAR(40) DEFAULT '0' NOT NULL,
																   "Nombre" 		NVARCHAR(60) DEFAULT '' NOT NULL,
																   "Estado" 		NVARCHAR(1) DEFAULT '' NOT NULL,
																   PRIMARY KEY ("IdTipoEvento"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.EventoTipoCentralizacionTipo" ("IdTipoCentralizacion" BIGINT DEFAULT 0 NOT NULL,
																					 "IdTipoEvento" 		NVARCHAR(40) DEFAULT '0' NOT NULL,
																					 "Estado" 				NVARCHAR(1) DEFAULT '' NOT NULL,
																					 PRIMARY KEY ("IdTipoCentralizacion", "IdTipoEvento"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" ("IdTipoDatoEvento" 	NVARCHAR(40) DEFAULT '' NOT NULL,
																	   "TipoDato" 			NVARCHAR(1) DEFAULT '' NOT NULL,
																	   "ValorLibre" 		NVARCHAR(1) DEFAULT '' NOT NULL,
																	   "LargoVarchar" 		BIGINT DEFAULT 0 NOT NULL,
																	   "Enteros" 			BIGINT DEFAULT 0 NOT NULL,
																	   "Decimales" 			BIGINT DEFAULT 0 NOT NULL,
																	   "FormatoEdicion" 	NVARCHAR(30) DEFAULT '' NOT NULL,
																	   "DatosPreDefinidos" 	NVARCHAR(1) DEFAULT '' NOT NULL,
																	   "Estado" 			NVARCHAR(1) DEFAULT '' NOT NULL,
																	   PRIMARY KEY ("IdTipoDatoEvento"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo" ("IdTipoMovimiento" NVARCHAR(10) DEFAULT '' NOT NULL,
																					 "IdTipoDatoEvento" NVARCHAR(40) DEFAULT '' NOT NULL,
																					 "Estado" 			NVARCHAR(1) DEFAULT '' NOT NULL,
																					 PRIMARY KEY ("IdTipoMovimiento", "IdTipoDatoEvento"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoValor" ("IdTipoDatoEvento" 	NVARCHAR(40) DEFAULT '' NOT NULL,
																			"ValorVarchar" 		NVARCHAR(60) DEFAULT '' NOT NULL,
																			"ValorDecimal" 		DECIMAL(18,4) DEFAULT 0 NOT NULL,
																			"Indice" 			BIGINT DEFAULT 0 NOT NULL,
																			"Estado" 			NVARCHAR(1) DEFAULT '' NOT NULL,
																			PRIMARY KEY ("IdTipoDatoEvento", "ValorVarchar", "ValorDecimal"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.HomologaCuenta" (
	"CuentaOptimus"  NVARCHAR (15) NULL DEFAULT NULL,
	"CuentaContabilidad" NVARCHAR(15) DEFAULT '' NOT NULL,
	"CodigoContabilidad" NVARCHAR(15) DEFAULT '' NOT NULL,
	"CuentaProveedor" 	NVARCHAR(12) DEFAULT '' NOT NULL,
	"CuentaEEFF" 		NVARCHAR(12) DEFAULT '' NOT NULL,
	"CentroCosto" 		NVARCHAR(12) DEFAULT '' NOT NULL,
	"NombreCuenta" 		NVARCHAR(70) DEFAULT '' NOT NULL,
	"DetalleCuenta" 		NVARCHAR(70) DEFAULT '' NOT NULL,
PRIMARY KEY ("CuentaOptimus"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.ListaControl" ("IdListaControl" 	BIGINT NOT NULL,
																	 "Nombre" 			NVARCHAR(50) DEFAULT '' NOT NULL,
																	 "Control" 			NVARCHAR(20) DEFAULT '' NOT NULL,
																	 "TipoLista" 		NVARCHAR(20) DEFAULT '' NOT NULL,
																	 "IdLista" 			BIGINT NOT NULL,
																	 "Estado" 			NVARCHAR(1) DEFAULT '' NOT NULL,
																	 PRIMARY KEY ("IdListaControl"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.Log" ("IdLog" 					BIGINT NOT NULL,
															"IdTipoLog" 				BIGINT NOT NULL,
															"IdEmpresa" 				BIGINT DEFAULT 0 NOT NULL,
															"Ejercicio" 				BIGINT NOT NULL,
															"IdTipoProceso" 			BIGINT NOT NULL,
															"Proceso" 					NVARCHAR(20) DEFAULT '' NOT NULL,
															"Fecha" 					NVARCHAR(10) DEFAULT '' NOT NULL,
															"Hora" 						NVARCHAR(8) DEFAULT '' NOT NULL,
															"CodigoUsuario" 			NVARCHAR(40) DEFAULT '' NOT NULL,
															"Glosa" 					NVARCHAR(600) DEFAULT '' NOT NULL,
															"IdCentralizacion" 			BIGINT NOT NULL,
															"IdMovimiento" 				BIGINT NOT NULL,
															"IdEvento" 					BIGINT NOT NULL,
															"IdDefinicionAsiento" 		BIGINT NOT NULL,
															"IdPreComprobante" 			BIGINT NOT NULL,
															"IdPreComprobanteAsiento" 	BIGINT NOT NULL,
															"IdCuentaContable" 			NVARCHAR(12) DEFAULT '' NOT NULL,
															"IdCondicion" 				BIGINT NOT NULL,
															"IdTipoContabilidad" 		BIGINT NOT NULL,
															"IdTipoEvento" 				NVARCHAR(40) NOT NULL,
															"IdTipoDatoEvento" 			NVARCHAR(40) DEFAULT '' NOT NULL,
															"IdTipoCentralizacion" 		BIGINT NOT NULL,
															"IdTipoMovimiento" 			NVARCHAR(10) DEFAULT '' NOT NULL,
															"NumeroMovimiento" 			BIGINT NOT NULL,
															"FechaMovimiento" 			NVARCHAR(10) DEFAULT '' NOT NULL,
															"RutMovimiento" 			NVARCHAR(11) DEFAULT '' NOT NULL,
															"CuentaMovimiento" 			BIGINT NOT NULL,
															"IndicadorError" 			NVARCHAR(50) DEFAULT '' NOT NULL,
															"Estado" 					NVARCHAR(1) DEFAULT '' NOT NULL,
															PRIMARY KEY ("IdLog"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.LogTipo" ("IdTipoLog" BIGINT NOT NULL,
																"Nombre" 	NVARCHAR(50) DEFAULT '' NOT NULL,
																"Nemo" 		NVARCHAR(20) DEFAULT '' NOT NULL,
																"Estado" 	NVARCHAR(1) DEFAULT '' NOT NULL,
																PRIMARY KEY ("IdTipoLog"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.LogTipoProceso" ("IdTipoProceso" 	BIGINT NOT NULL,
																	   "Nombre" 		NVARCHAR(40) DEFAULT '' NOT NULL,
																	   "Nemo" 			NVARCHAR(20) DEFAULT '' NOT NULL,
																	   "Estado" 		NVARCHAR(1) DEFAULT '' NOT NULL,
																	   PRIMARY KEY ("IdTipoProceso"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.Movimiento" (
	"IdMovimiento" 			BIGINT 						NOT NULL,
   	"IdTipoMovimiento" 		NVARCHAR(10) 	DEFAULT ''	NOT NULL,
   	"IdEmpresa" 			BIGINT 			DEFAULT 0 	NOT NULL,
   	"Numero" 				BIGINT		 	DEFAULT 0 	NOT NULL,
   	"Linea" 				BIGINT 			DEFAULT 0 	NOT NULL,
   	"Fecha" 				NVARCHAR(10) 	DEFAULT '' 	NOT NULL,
   	"DocumentoIdentidad"	NVARCHAR(40) 	DEFAULT '' 	NOT NULL,
   	"Cuenta" 				BIGINT 			DEFAULT 0 	NOT NULL,
   	"Ejercicio" 			BIGINT 			DEFAULT 0 	NOT NULL,
   	"Periodo" 				NVARCHAR(7) 	DEFAULT '' 	NOT NULL,
   	"IdCentralizacion" 		BIGINT 						NOT NULL,
   	"IdMoneda"				NVARCHAR(3)		DEFAULT ''	NOT NULL,
   	"Monto" 				DECIMAL(18,2) 	DEFAULT 0 	NOT NULL,
   	"Estado" 				NVARCHAR(1) 	DEFAULT '' 	NOT NULL,
PRIMARY KEY ("IdMovimiento"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.MovimientoDato" (
	"IdMovimiento" 		BIGINT 						NOT NULL,
   	"IdTipoDatoEvento" 	NVARCHAR(40) 	DEFAULT '' 	NOT NULL,
   	"ValorVarchar" 		NVARCHAR(60) 	DEFAULT '' 	NOT NULL,
   	"ValorDecimal" 		DECIMAL(18,4) 	DEFAULT 0 	NOT NULL,
   	"Estado" 			NVARCHAR(1) 	DEFAULT '' 	NOT NULL,
PRIMARY KEY ("IdMovimiento", "IdTipoDatoEvento"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.MovimientoTipo" ("IdTipoMovimiento" 	NVARCHAR(10) DEFAULT '' NOT NULL,
																	   "Nombre" 			NVARCHAR(60) DEFAULT '' NOT NULL,
																	   "Estado" 			NVARCHAR(1) DEFAULT '' NOT NULL,
																	   PRIMARY KEY ("IdTipoMovimiento"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo" ("IdTipoCentralizacion" BIGINT DEFAULT 0 NOT NULL,
																						 "IdTipoMovimiento" 	NVARCHAR(10) DEFAULT '' NOT NULL,
																						 "Estado" 				NVARCHAR(1) DEFAULT '' NOT NULL,
																						 PRIMARY KEY ("IdTipoCentralizacion", "IdTipoMovimiento"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.PreComprobante" ("IdPreComprobante" 	BIGINT NOT NULL,
																	   "IdTipoContabilidad" BIGINT DEFAULT 0 NOT NULL,
																	   "IdEmpresa" 			BIGINT DEFAULT 0 NOT NULL,
																	   "Ejercicio" 			BIGINT DEFAULT 0 NOT NULL,
																	   "Tipo" 				NVARCHAR(1) DEFAULT '' NOT NULL,
																	   "FechaContable" 		NVARCHAR(10) DEFAULT '' NOT NULL,
																	   "Glosa" 				NVARCHAR(40) DEFAULT '' NOT NULL,
																	   "FechaProceso" 		NVARCHAR(10) DEFAULT '' NOT NULL,
																	   "HoraProceso" 		NVARCHAR(8) DEFAULT '' NOT NULL,
																	   "CodigoUsuario" 		NVARCHAR(40) DEFAULT '' NOT NULL,
																	   "FolioContabilidad" 	BIGINT NOT NULL,
																	   "IdReversa" 			BIGINT NOT NULL,
																	   "Estado" 			NVARCHAR(1) DEFAULT '' NOT NULL,
																	   PRIMARY KEY ("IdPreComprobante"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" ("IdPreComprobanteAsiento" BIGINT NOT NULL,
																			  "IdEmpresa" 				BIGINT DEFAULT 0 NOT NULL,
																			  "IdPreComprobante" 		BIGINT NOT NULL,
																			  "IdCuentaContable" 		NVARCHAR(12) DEFAULT '' NOT NULL,
																			  "Ejercicio" 				BIGINT DEFAULT 0 NOT NULL,
																			  "Linea" 					BIGINT DEFAULT 0 NOT NULL,
																			  "MontoDebe" 				DECIMAL(18,2) DEFAULT 0 NOT NULL,
																			  "MontoHaber" 				DECIMAL(18,2) DEFAULT 0 NOT NULL,
																			  "FechaContable" 			NVARCHAR(10) DEFAULT '' NOT NULL,
																			  "Glosa" 					NVARCHAR(120) DEFAULT '' NOT NULL,
																			  "Estado" 					NVARCHAR(1) DEFAULT '' NOT NULL,
																			  PRIMARY KEY ("IdPreComprobanteAsiento"));
																			  
CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.EstadoContabilidad" 
("IdEstado" NVARCHAR(3), "IdEstadoContabilidad" NVARCHAR(1), PRIMARY KEY ("IdEstado"));

CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.ConfiguracionContabilidad" (
	"IdEmpresa" BIGINT NOT NULL,
	"CodigoParametro" NVARCHAR(40) NOT NULL,
	"Descripcion" NVARCHAR(520) NOT NULL,
	"Valor" NVARCHAR(520) NOT NULL,
	"EsModificableUsuario" NVARCHAR(1) NOT NULL DEFAULT 'N',
	"Uso" NVARCHAR(5000) NOT NULL DEFAULT  '',
	"ValorDefecto" NVARCHAR(5000) NOT NULL DEFAULT  '',
	"ValoresPosibles" NVARCHAR(5000) NOT NULL DEFAULT  '',
	"ModificablePorUsuario" NVARCHAR(512) NOT NULL DEFAULT  '',
	"ModificablePostProduccion" NVARCHAR(1) NOT NULL DEFAULT  '',
	"CreadorParametro" NVARCHAR(512) NOT NULL DEFAULT  '',
	"FechaCreacion" SECONDDATE NOT NULL DEFAULT '0001-01-01',
	"ModificadorParametro" NVARCHAR(512) NOT NULL DEFAULT  '',
	"FechaModificacion" SECONDDATE NOT NULL DEFAULT '0001-01-01',
	"Observaciones" NVARCHAR NOT NULL DEFAULT  '',
	PRIMARY KEY ("IdEmpresa","CodigoParametro")
);
CREATE COLUMN TABLE "BSCL.Contabilidad.BD::ITZ.InterfazCuenta" 
(
	"CuentaContabilidad" VARCHAR(15) NOT NULL,
	"C08Imprime" VARCHAR(1) DEFAULT 'S' NOT NULL,	
	"C08Codigo" VARCHAR(2),	
	"C08Banda" VARCHAR(2),
	"C08CodigoSBIF" VARCHAR(6) NOT NULL,	
	"C40Imprime" VARCHAR(1) DEFAULT 'S' NOT NULL,
	"C40Codigo" VARCHAR(2),
	"C40Banda" VARCHAR(2),
	"C40CodigoSBIF" VARCHAR(6) NOT NULL,	
	PRIMARY KEY ("CuentaContabilidad")
);