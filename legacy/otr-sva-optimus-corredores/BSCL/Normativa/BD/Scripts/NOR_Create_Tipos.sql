SET SCHEMA "BSCL";
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380AUT" CASCADE;

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/

DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_ART171CARTA1" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_ART171CARTA2" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_CIR63NOMINA" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_CIR63TRANSAC" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380Cli" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380ASIG" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380AUT" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380CUS" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380FAC" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380TRA" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380ORD" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380OPE" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380Instrucciones" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380Repactaciones" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_LIBROOPERACIONES" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_UAFCIR63PEP" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_UAFROE" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_UAFDOF" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_OPERACIONBASE" CASCADE;

		  
DROP TYPE "BSCL.Normativo.BD::NOR.SALIDA_INFORMEDERECHO" CASCADE;
--DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_OPERACION1064" CASCADE;
--DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_OPERACION1064RUEDA" CASCADE;
--DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_OPERACION1064FUERARUEDA" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_FATCADETALLE" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_FATCARESUMEN" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_PATRIMONIOFATCA" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_ANEXO4" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_ANEXO13" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_ANEXO1" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380OperacionesBur" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380Deudores" CASCADE;
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_LibroVentas" CASCADE;
--DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_CONSULTAR_ORDENES" CASCADE;

DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_CIR1804" CASCADE;






/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

 ---TABLAS DE SALIDA
 CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_ART171CARTA1" AS TABLE  
									   ( "NombreDestinatario"    			NVARCHAR(100),
									 	 "CargoDestinatario"      			NVARCHAR(40),
										 "FechaOperacion"					DATE,
										 "IdOperacion"						BIGINT,
										 "IdTipoOperacion"					NVARCHAR(6),
										 "NombreTipoOperacion" 				NVARCHAR(100),
   										 "IdClasificacionTipoInstrumento" 	NVARCHAR(15),
    									 "IdInstrumento"					BIGINT,
    									 "Nemotecnico" 						NVARCHAR(40),
    									 "Cantidad" 						DECIMAL(28,8),
    									 "Precio" 							DECIMAL(28,8),
    									 "Monto" 							DECIMAL(28,8),
    								     "IdMoneda" 						NVARCHAR(3),
										 "Moneda" 							NVARCHAR(100),
										 "IdCliente" 						BIGINT,
										 "NombreCliente" 					NVARCHAR(100),
										 "DocumentoIdentidad" 				NVARCHAR(40),
										 "TipoDocumentoIdentidad"			NVARCHAR(10),
										 "NombreTipoDocumentoIdentidad"		NVARCHAR(5),
										 "IdTipoRelacion" 					NVARCHAR(6),
										 "NombreTipoRelacion" 				NVARCHAR(520),    
										 "CodigoRelacionNorma" 				NVARCHAR(10),
										 "DescripcionRelacionNorma"			NVARCHAR(100)
									    ); 
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_ART171CARTA2" AS TABLE  
									   (
										"NombreDestinatario"    			NVARCHAR(100),
									 	 "CargoDestinatario"      			NVARCHAR(40), 
										 "FechaOperacion"					NVARCHAR(15),
										 "IdTipoOperacion"					NVARCHAR(6),
										 "NombreTipoOperacion" 				NVARCHAR(100),
   										 "IdClasificacionTipoInstrumento" 	NVARCHAR(15),
    									 "IdInstrumento"					BIGINT,
    									 "Nemotecnico" 						NVARCHAR(40),
    									 "Cantidad" 						DECIMAL(28,8),
    									 "Precio" 							DECIMAL(28,8),
    									 "Monto" 							DECIMAL(28,8),
    								     "IdMoneda" 						NVARCHAR(3),
										 "Moneda" 							NVARCHAR(100),
										 "IdCliente" 						BIGINT,
										 "NombreCliente" 					NVARCHAR(100),
										 "DocumentoIdentidad" 				NVARCHAR(40),
										 "TipoDocumentoIdentidad"			NVARCHAR(10),
										 "NombreTipoDocumentoIdentidad"		NVARCHAR(5),
										 "IdTipoRelacion" 					NVARCHAR(6),
										 "NombreTipoRelacion" 				NVARCHAR(520),    
										 "CodigoRelacionNorma" 				NVARCHAR(10),
										 "DescripcionRelacionNorma"			NVARCHAR(100),
										 "MesSistema"	    			    NVARCHAR(30),
										 "MesConsulta"    					NVARCHAR(30),
										 "YearSistema"						NVARCHAR(30),
										 "YearConsulta"						NVARCHAR(30),
										 "DiaSistema"						NVARCHAR(30),
										 "DiaConsulta"						NVARCHAR(30)
									    );
									    
 CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_CIR63NOMINA" AS TABLE  
									   ( "NombreDestinatario"    			NVARCHAR(100),
									 	 "CargoDestinatario"      			NVARCHAR(40),
										 "Direccion"						NVARCHAR(255),
										 "Comuna"							NVARCHAR(100),
										 "Ciudad" 							NVARCHAR(100),
 										 "IdPersona" 						BIGINT,
										 "NombrePersona" 					NVARCHAR(100),
  										 "IdPersonaEmpleador"				BIGINT,
 										 "NombreEmpleador" 					NVARCHAR(100),
										 "DocumentoIdentidad" 				NVARCHAR(40),
										 "TipoDocumentoIdentidad"			NVARCHAR(10),
										 "NombreTipoDocumentoIdentidad"		NVARCHAR(5),
										 "IdTipoRelacion" 					NVARCHAR(6),
										 "NombreTipoRelacion" 				NVARCHAR(520),    
										 "CodigoRelacionNorma" 				NVARCHAR(10),
										 "DescripcionRelacionNorma"			NVARCHAR(100),
										 "Periodo"			      			NVARCHAR(7)
									    ); 	
									    
 CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_CIR63TRANSAC" AS TABLE  
									   ( "NombreDestinatario"    			NVARCHAR(100),
									 	 "CargoDestinatario"      			NVARCHAR(40),
										 "FechaOperacion"					DATE,
										 "IdTipoOperacion"					NVARCHAR(6),
										 "NombreTipoOperacion" 				NVARCHAR(100),
   										 "IdClasificacionTipoInstrumento" 	NVARCHAR(15),
    									 "IdInstrumento"					BIGINT,
    									 "Nemotecnico" 						NVARCHAR(40),
     									 "Cantidad" 						DECIMAL(28,8),
    									 "Precio" 							DECIMAL(28,8),
    									 "Monto" 							DECIMAL(28,8),
    								     "IdMoneda" 						NVARCHAR(3),
										 "Moneda" 							NVARCHAR(100),
										 "IdCliente" 						BIGINT,
										 "NombreCliente" 					NVARCHAR(100),
										 "DocumentoIdentidad" 				NVARCHAR(40),
										 "TipoDocumentoIdentidad"			NVARCHAR(10),
										 "NombreTipoDocumentoIdentidad"		NVARCHAR(5),
										 "IdTipoRelacion" 					NVARCHAR(6),
										 "NombreTipoRelacion" 				NVARCHAR(520),    
										 "CodigoRelacionNorma" 				NVARCHAR(10),
										 "DescripcionRelacionNorma"			NVARCHAR(100),
										 "IdOperacion"						BIGINT,
										 "Mes"								NVARCHAR(20)
									    );									    

CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380Cli" AS TABLE  
									  (	"NumeroCliente"		BIGINT		DEFAULT 0,
										"Rut"				NVARCHAR(20)	DEFAULT '',
										"Dig"				NVARCHAR(1)	DEFAULT '',
										"Pasaporte"			NVARCHAR(100)	DEFAULT '',
										"Glei"				NVARCHAR(520)	DEFAULT '',
										"ApellidoPaterno"	NVARCHAR(100)	DEFAULT '',
										"ApellidoMaterno"	NVARCHAR(100)	DEFAULT '',
										"PrimerNombre"		NVARCHAR(100)	DEFAULT '',
										"SegundoNombre"		NVARCHAR(100)	DEFAULT '',
										"RazonSocial"		NVARCHAR(100)	DEFAULT '',
										"Telefono"			NVARCHAR(40)	DEFAULT '',
										"Email"	 			NVARCHAR(100)	DEFAULT '',
										"DireccionTipo"			NVARCHAR(100)	DEFAULT '',
										"DireccionNombre"		NVARCHAR(100)	DEFAULT '',
										"DireccionNumero"		NVARCHAR(40)	DEFAULT '',
										"DireccionDepto"		NVARCHAR(40)	DEFAULT '',
										"DireccionComuna"	 	NVARCHAR(40)	DEFAULT '',
										"DireccionCiudad"	 	NVARCHAR(100)	DEFAULT '',
										"DireccionEstado"		NVARCHAR(100)	DEFAULT '',
										"DireccionPais"	 		NVARCHAR(100)	DEFAULT '',
										"CodigoPostal"	 		NVARCHAR(100)	DEFAULT '',
										"Nacionalidad"	 		NVARCHAR(100)	DEFAULT '',
										"RelacionIntermediario"	 NVARCHAR(100)	DEFAULT '',
										"AdministracionCartera"	 NVARCHAR(1)	DEFAULT '',
										"RutEntidadEmpleadora"	NVARCHAR(20)	DEFAULT '',
										"DigEntidadEmpleadora"	NVARCHAR(10)	DEFAULT '',
										"RazonSocialEntidadEmpleadora"	NVARCHAR(100)	DEFAULT '',
										"EstadoCivil"	NVARCHAR(10)	DEFAULT '',
--------Campos Conyuge			
										"ConyRut"					NVARCHAR(20)	DEFAULT '',
										"ConyDig"					NVARCHAR(1)	DEFAULT '',
										"ConyPasaporte"		NVARCHAR(100)	DEFAULT '',
										"ConyApellidoPaterno"	NVARCHAR(100)	DEFAULT '',
										"ConyApellidoMaterno"	NVARCHAR(100)	DEFAULT '',
										"ConyPrimerNombre"	NVARCHAR(100)	DEFAULT '',
										"ConySegundoNombre"	NVARCHAR(100)	DEFAULT '',
										"ConyRazonSocial"	NVARCHAR(100)	DEFAULT '',
										"ConyTelefono"	NVARCHAR(40)	DEFAULT '',
										"ConyEmail"	 NVARCHAR(100)	DEFAULT '',
										"ConyDireccionTipo"	 NVARCHAR(100)	DEFAULT '',
										"ConyDireccionNombre"	 NVARCHAR(100)	DEFAULT '',
										"ConyDireccionNumero"	 NVARCHAR(40)	DEFAULT '',
										"ConyDireccionDepto"	 NVARCHAR(40)	DEFAULT '',
										"ConyDireccionComuna"	 NVARCHAR(40)	DEFAULT '',
										"ConyDireccionCiudad"	 NVARCHAR(100)	DEFAULT '',
										"ConyDireccionEstado"	 NVARCHAR(100)	DEFAULT '',
										"ConyDireccionPais"	 NVARCHAR(100)	DEFAULT '',
										"ConyCodigoPostal"	 NVARCHAR(100)	DEFAULT '',
										"ConyNacionalidad"	 NVARCHAR(100)	DEFAULT '',
										"ConyRelacionIntermediario"	 NVARCHAR(100)	DEFAULT '',
--------Campos Representante Legal
										"RepRut"	NVARCHAR(20)	DEFAULT '',
										"RepDig"	NVARCHAR(10)	DEFAULT '',
										"RepPasaporte"	NVARCHAR(100)	,
										"RepGlei"	NVARCHAR(520)	DEFAULT '',
										"RepApellidoPaterno"	NVARCHAR(100)	DEFAULT '',
										"RepApellidoMaterno"	NVARCHAR(100)	DEFAULT '',
										"RepPrimerNombre"	NVARCHAR(100)	DEFAULT '',
										"RepSegundoNombre"	NVARCHAR(100)	DEFAULT '',
										"RepRazonSocial"	NVARCHAR(100)	DEFAULT '',
										"RepTelefono"	NVARCHAR(40)	DEFAULT '',
										"RepEmail"	 NVARCHAR(100)	DEFAULT '',
										"RepDireccionTipo"	 NVARCHAR(100)	DEFAULT '',
										"RepDireccionNombre"	 NVARCHAR(100)	DEFAULT '',
										"RepDireccionNumero"	 NVARCHAR(40)	DEFAULT '',
										"RepDireccionDepto"	 NVARCHAR(40)	DEFAULT '',
										"RepDireccionComuna"	 NVARCHAR(40)	DEFAULT '',
										"RepDireccionCiudad"	 NVARCHAR(100)	DEFAULT '',
										"RepDireccionProvincia"	 NVARCHAR(100)	DEFAULT '',
										"RepDireccionPais"	 NVARCHAR(100)	DEFAULT '',
										"RepCodigoPostal"	 NVARCHAR(100)	DEFAULT '',
										"RepNacionalidad"	 NVARCHAR(100)	DEFAULT '',
										"RepRelacionIntermediario"	 NVARCHAR(100)	DEFAULT '',
										"RepEstadoCivil"	NVARCHAR(10)	DEFAULT '',
--------Campos Conyugue Representante		
										"ConyRepRut"	NVARCHAR(20)	DEFAULT '',
										"ConyRepDig"	NVARCHAR(10)	DEFAULT '',
										"ConyRepPasaporte"	NVARCHAR(100)		,
										"ConyRepApellidoPaterno"	NVARCHAR(100)	DEFAULT '',
										"ConyRepApellidoMaterno"	NVARCHAR(100)	DEFAULT '',
										"ConyRepPrimerNombre"	NVARCHAR(100)	DEFAULT '',
										"ConyRepSegundoNombre"	NVARCHAR(100)	DEFAULT '',
										"ConyRepRazonSocial"	NVARCHAR(100)	DEFAULT '',
										"ConyRepTelefono"	NVARCHAR(40)	DEFAULT '',
										"ConyRepEmail"	 NVARCHAR(100)	DEFAULT '',
										"ConyRepDireccionTipo"	 NVARCHAR(100)	DEFAULT '',
										"ConyRepDireccionNombre"	 NVARCHAR(100)	DEFAULT '',
										"ConyRepDireccionNumero"	 NVARCHAR(40)	DEFAULT '',
										"ConyRepDireccionDepto"	 NVARCHAR(40)	DEFAULT '',
										"ConyRepDireccionComuna"	 NVARCHAR(40)	DEFAULT '',
										"ConyRepDireccionCiudad"	 NVARCHAR(100)	DEFAULT '',
										"ConyRepDireccionEstado"	 NVARCHAR(100)	DEFAULT '',
										"ConyRepDireccionPais"	 NVARCHAR(100)	DEFAULT '',
										"ConyRepCodigoPostal"	 NVARCHAR(100)	DEFAULT '',
										"ConyRepNacionalidad"	 NVARCHAR(100)	DEFAULT '',
										"ConyRepRelacionIntermediario"	 NVARCHAR(100)	DEFAULT '',
										"CuentaBancariaUno"	 NVARCHAR(100)	DEFAULT '',
										"CuentaBancariaDos"	 NVARCHAR(100)	DEFAULT '',
										"CuentaBancariaTres"	 NVARCHAR(100)	DEFAULT '',
										"ClaseOrden"	 NVARCHAR(100)	DEFAULT '',
										"Perfil"	 NVARCHAR(40)	DEFAULT '',
										"PerfilAnterior"	 NVARCHAR(40)	DEFAULT '',
										"FechaInicioPerfilActual"	NVARCHAR(20)	DEFAULT ''	--SECONDDATE  	DEFAULT '0001-01-01 00:00:00' NULL 
										) ;


DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380ASIG" CASCADE;GO
									CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380ASIG"  AS TABLE  (
										"Rut" NVARCHAR(100),
										"Cuenta" NVARCHAR(50) DEFAULT 0,
										"Nombre" NVARCHAR(100),
										"Bolsa" NVARCHAR(100),
										"Folio" BIGINT DEFAULT 0,
										"Instrumento" NVARCHAR(100),
										"TipoOperacion" NVARCHAR(100),
										"NumeroOrden" BIGINT DEFAULT 0,
										"NumeroOperacion" BIGINT DEFAULT 0,
										"UnidadesAsignadas" DECIMAL(26,8) DEFAULT 0,
										"MontoTotal" DECIMAL(26,8) DEFAULT 0,
										"Precio" DECIMAL(26,8) DEFAULT 0,
										"Moneda" NVARCHAR(100),
										"FechaAsignacion" DATE,
										"HoraAsignacion" NVARCHAR(100),
										"TipoOperacionDetalle" NVARCHAR(100),
										"FolioOrden" NVARCHAR(100),
										"InstrumentoNegociacion" NVARCHAR(40) NOT NULL DEFAULT ''	
									);

										SET SCHEMA "BSCL";GO
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380AUT" CASCADE;GO
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380AUT"  AS TABLE  
									  (	"NumeroCliente"	BIGINT		DEFAULT 0,
									   	"RutCLI"			NVARCHAR(15)	DEFAULT '',
										"DigCLI"			NVARCHAR(10)	DEFAULT '',
									 	"NumeroPersona"	BIGINT		DEFAULT 0,
									 	"Rut"			NVARCHAR(15)	DEFAULT '',--BIGINT		DEFAULT 0,
										"Dig"			NVARCHAR(10)	DEFAULT '',
										"Pasaporte"		NVARCHAR(100)	DEFAULT '', 
										"Glei"			NVARCHAR(520)	DEFAULT '', 
										"ApellidoPaterno"	NVARCHAR(100)	DEFAULT '',
										"ApellidoMaterno"	NVARCHAR(100)	DEFAULT '',
										"PrimerNombre"		NVARCHAR(100)	DEFAULT '',
										"SegundoNombre"		NVARCHAR(100)	DEFAULT '',
										"RazonSocial"		NVARCHAR(100)	DEFAULT '',
										"Telefono"			NVARCHAR(100)	DEFAULT '',
										"Email"				NVARCHAR(100)	DEFAULT '',
										"DireccionTipo"		NVARCHAR(100)	DEFAULT '',
										"DireccionNombre"	NVARCHAR(100)	DEFAULT '',
										"DireccionNumero"	NVARCHAR(100)	DEFAULT '',
										"DireccionDepto"	NVARCHAR(100)	DEFAULT '',
										"DireccionComuna"	NVARCHAR(100)	DEFAULT '',
										"DireccionCiudad"	NVARCHAR(100)	DEFAULT '',
										"DireccionEstado"	NVARCHAR(100)	DEFAULT '',
										"DireccionPais"		NVARCHAR(100)	DEFAULT '',
										"DireccionCodigoPostal"	NVARCHAR(100)	DEFAULT '',
										"Nacionalidad"			NVARCHAR(100)	DEFAULT '',
										"RelacionIntermediario" NVARCHAR(100)	DEFAULT ''
		
		
										) ;GO
										
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380CUS"  AS TABLE  
									  (	"NumeroCliente"	NVARCHAR(15)	DEFAULT '', --ahora va rut completo
										"NumeroCuenta"	BIGINT		DEFAULT 0,
										"Fecha" 		DATE	DEFAULT '',
										"Hora"			NVARCHAR(100)	DEFAULT '', 
										"NumeroComprobante"	BIGINT		DEFAULT 0,
										"Emisor"			NVARCHAR(100)	DEFAULT '',
										"TipoMovimiento"	NVARCHAR(100)	DEFAULT '',
										"Movimiento"		NVARCHAR(100)	DEFAULT '',
										"ISIN"				NVARCHAR(100)	DEFAULT '',
										"Nemotecnico"		NVARCHAR(100)	DEFAULT '',
										"Cantidad"			DECIMAL(26,8)		DEFAULT 0,
										"Monto"				DECIMAL(26,8)		DEFAULT 0,
										"Moneda"			NVARCHAR(100)	DEFAULT '',
										"Saldo"				DECIMAL(26,8)		DEFAULT 0,
										"SubCustodio"		NVARCHAR(100)	DEFAULT '', 
										"Estado"			NVARCHAR(80)	DEFAULT '',
										"NombreConcepto"	NVARCHAR(80)	DEFAULT ''
									
										) ;

DROP TYPE BSCL."BSCL.Normativa.BD::NOR.SALIDA_NCG380FAC" CASCADE;GO
										CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380FAC" AS TABLE  (
											"FechaTransaccion" NVARCHAR(21),
											"Rut" NVARCHAR(100),
											"Cuenta" NVARCHAR(100),
											"Nombre" NVARCHAR(100),
											"Bolsa" NVARCHAR(100),
											"Folio" BIGINT DEFAULT 0,
											"Instrumento" NVARCHAR(100),
											"TipoOperacion" NVARCHAR(100),
											"NumeroOrden" BIGINT DEFAULT 0,
											"NumeroFactura" BIGINT DEFAULT 0,
											"Cantidad" DECIMAL(26,8) DEFAULT 0,
											"Precio" DECIMAL(26,8) DEFAULT 0,
											"Monto" DECIMAL(26,8) DEFAULT 0,
											"Moneda" NVARCHAR(100),
											"Comision" DECIMAL(26,8) DEFAULT 0,
											"DerechoBolsa" DECIMAL(26,8) DEFAULT 0,
											"Gastos" DECIMAL(26,8) DEFAULT 0,
											"Impuesto" DECIMAL(26,8) DEFAULT 0,
											"FechaFactura" DATE,
											"FacturaReferencia" BIGINT DEFAULT 0,
											"Anulacion" NVARCHAR(100),
											"FolioAnulacion" NVARCHAR(100),
											"NotaCredito" NVARCHAR(100),
											"FolioNotaCredito" NVARCHAR(100),
											"TipoComprobante" NVARCHAR(50),
											"FechaLiquidacion" DATE,
											"FechaEmision" NVARCHAR(50) DEFAULT '0001-01-01,'
											"HoraEmision" NVARCHAR(50) DEFAULT '00:00:00',
											"InstrumentoNegociacion" NVARCHAR(40) NOT NULL DEFAULT ''	
										);

DROP TYPE BSCL."BSCL.Normativa.BD::NOR.SALIDA_NCG380TRA" CASCADE;GO
CREATE TYPE BSCL."BSCL.Normativa.BD::NOR.SALIDA_NCG380TRA"  AS TABLE  
									  (	
											"Rut"				NVARCHAR(100)	DEFAULT '',
											"DIG"				NVARCHAR(100)	DEFAULT '', 
											"Pasaporte"			NVARCHAR(100)	DEFAULT '', 
											"ApellidoPaterno"	NVARCHAR(100)	DEFAULT '', 
											"ApellidoMaterno"	NVARCHAR(100)	DEFAULT '', 
											"PrimerNombre" 		NVARCHAR(100)	DEFAULT '', 
											"SegundoNombre"		NVARCHAR(100)	DEFAULT '', 
											"Telefono"			NVARCHAR(100)	DEFAULT '', 
											"Email"				NVARCHAR(100)	DEFAULT '', 
											"TipoDireccion"		NVARCHAR(100)	DEFAULT '', 
											"DireccionNombre"	NVARCHAR(100)	DEFAULT '', 
											"DireccionNumero"	NVARCHAR(100)	DEFAULT '', 
											"DireccionDepto"	NVARCHAR(100)	DEFAULT '', 
											"DireccionComuna"	NVARCHAR(100)	DEFAULT '', 
											"DireccionCiudad"	NVARCHAR(100)	DEFAULT '', 
											"DireccionEstado"	NVARCHAR(100)	DEFAULT '', 
											"DireccionPais"		NVARCHAR(100)	DEFAULT '', 
											"DireccionCodPostal"	NVARCHAR(100)	DEFAULT '', 
											"Nacionalidad"			NVARCHAR(100)	DEFAULT '', 
											"FechaIniFunciones"		DATE	DEFAULT '',
											"FechaTerFunciones"		DATE	DEFAULT '',
											"Funcion"				NVARCHAR(100)	DEFAULT '', 
											"FechaAcreditacion"		DATE	DEFAULT '',
											"InfoPrivilegiada"		NVARCHAR(100)	DEFAULT '',
											"Estado"				NVARCHAR(100)	DEFAULT ''
										) ;
										
										
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380ORD"  AS TABLE  
										(	
										"NumeroOrden"			 BIGINT			DEFAULT 0,
										"FolioOrden" 			 NVARCHAR(100)	DEFAULT '',
										"NumeroCliente"			 NVARCHAR(100)	DEFAULT '',
										"NumeroCuenta"			 NVARCHAR(100)	DEFAULT '',
										"NumeroPersonaOrden"	 NVARCHAR(100)	DEFAULT '',
										"NumeroEjecutivo"		 BIGINT			DEFAULT 0,
										"ClaseOrden"			 NVARCHAR(100)	DEFAULT '', 
										"TipoOrden"				 NVARCHAR(100)	DEFAULT '', 
										"Isin"					 NVARCHAR(100)	DEFAULT '', 
										"Nemotecnico"			 NVARCHAR(100)	DEFAULT '', 
										"TipoInstrumento"		 NVARCHAR(100)	DEFAULT '', 
										"Cantidad"				 DECIMAL(28,8),
										"Monto"					 DECIMAL(28,8),
										"Moneda"				 NVARCHAR(100)	DEFAULT '', 
										"Precio"				 DECIMAL(28,8),
										"PlazoValidez"			 BIGINT			DEFAULT 0,
										"PreferenciaBolsa"		 NVARCHAR(100)	DEFAULT '', 
										"PreferenciaMecanismo"	 NVARCHAR(100)	DEFAULT '', 
										"FechaRecepcion"		 DATE,
										"HoraRecepcion"			 NVARCHAR(100)	DEFAULT '',
										"FechaAceptacion"		 DATE,
										"HoraAceptacion"		 NVARCHAR(100)	DEFAULT '',
										"NUMEROPERSONA"			 NVARCHAR(100)	DEFAULT '',
										"Anulacion"				 BIGINT			DEFAULT 0,
										"NumeroOrdenReemplazo"	 BIGINT			DEFAULT 0,
										"FechaAnulacion"		 DATE,
										"Observaciones"			 NVARCHAR(100)	DEFAULT '',
										"DocumentoIdentidadRut"	 NVARCHAR(100)	DEFAULT '',
										"InstrumentoNegociacion" NVARCHAR(40) NOT NULL DEFAULT ''	
										) ;	

CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380Instrucciones"  AS TABLE  
									  (	"DocumentoIdentidad"	NVARCHAR(40)	DEFAULT '', 
										"CodigoISIN"			NVARCHAR(40)	DEFAULT '', 
										"Nemotecnico"			NVARCHAR(40)	DEFAULT '', 
										"IdEmisor"				NVARCHAR(40)	DEFAULT '', 
										"TipoJunta"				NVARCHAR(3)		DEFAULT '', 
										"Lugar"					NVARCHAR(100)	DEFAULT '', 
										"FechaRealizacion"		DATE	        DEFAULT '', 
										"Materias"				NVARCHAR(500)	DEFAULT '', 
										"Instruccion"			NVARCHAR(500)	DEFAULT '', 
										"CantidadDerecho"		DECIMAL(26,8)	DEFAULT 0
										) ;

CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380Repactaciones" AS TABLE
(
	"DocumentoIdentidad"			NVARCHAR(40)	DEFAULT '', 
	"DigitoVerificador"				NVARCHAR(10)	DEFAULT '', 
	"Pasaporte"						NVARCHAR(40)	DEFAULT '', 
	"GlobalLegalEntity"				NVARCHAR(520)	DEFAULT '', 
	"ApellidoPaterno"				NVARCHAR(100)	DEFAULT '',
	"ApellidoMaterno"				NVARCHAR(100)	DEFAULT '',
	"PrimerNombre"					NVARCHAR(100)	DEFAULT	'',
	"SegundoNombre"					NVARCHAR(100)	DEFAULT '',
	"RazonSocial"					NVARCHAR(100)	DEFAULT '',
	"Telefono"						NVARCHAR(40)	DEFAULT '',
	"DireccionElectronica"			NVARCHAR(100)	DEFAULT '',	
	"TipoDireccion"					NVARCHAR(40)	DEFAULT '',
	"Calle"							NVARCHAR(100)	DEFAULT '',
	"Numero"						NVARCHAR(40) 	DEFAULT '',
	"Departamento"					NVARCHAR(40) 	DEFAULT '',
	"Comuna"						NVARCHAR(100)	DEFAULT '',
	"Ciudad"						NVARCHAR(100)	DEFAULT '',
	"Provincia"						NVARCHAR(100)	DEFAULT '',
	"Pais"							NVARCHAR(100)	DEFAULT '',
	"CodigoPostal"					NVARCHAR(100)	DEFAULT '',
	"PaisNacimiento"				NVARCHAR(100)	DEFAULT '',
	"Fecha"							DATE			DEFAULT '0001-01-01',
	"Monto"							DECIMAL(26,8)	DEFAULT 0,
	"Moneda"						NVARCHAR(3)		DEFAULT '',
	"FechaRepactacion"				DATE			DEFAULT '0001-01-01',
	"FechaAbono"					DATE			DEFAULT '0001-01-01',
	"MontoAbono"					DECIMAL(26,8)	DEFAULT 0,
	"MonedaAbono"					NVARCHAR(3)		DEFAULT ''
);
	
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380Deudores" AS TABLE
(
	"DocumentoIdentidad"			NVARCHAR(40)	DEFAULT '', 
	"DigitoVerificador"				NVARCHAR(10)	DEFAULT '', 
	"Pasaporte"						NVARCHAR(40)	DEFAULT '', 
	"GlobalLegalEntity"				NVARCHAR(520)	DEFAULT '', 
	"ApellidoPaterno"				NVARCHAR(100)	DEFAULT '',
	"ApellidoMaterno"				NVARCHAR(100)	DEFAULT '',
	"PrimerNombre"					NVARCHAR(100)	DEFAULT	'',
	"SegundoNombre"					NVARCHAR(100)	DEFAULT '',
	"RazonSocial"					NVARCHAR(100)	DEFAULT '',
	"Telefono"						NVARCHAR(40)	DEFAULT '',
	"DireccionElectronica"			NVARCHAR(100)	DEFAULT '',	
	"TipoDireccion"					NVARCHAR(40)	DEFAULT '',
	"Calle"							NVARCHAR(100)	DEFAULT '',
	"Numero"						NVARCHAR(40) 	DEFAULT '',
	"Departamento"					NVARCHAR(40) 	DEFAULT '',
	"Comuna"						NVARCHAR(100)	DEFAULT '',
	"Ciudad"						NVARCHAR(100)	DEFAULT '',
	"Provincia"						NVARCHAR(100)	DEFAULT '',
	"Pais"							NVARCHAR(100)	DEFAULT '',
	"CodigoPostal"					NVARCHAR(100)	DEFAULT '',
	"PaisNacimiento"				NVARCHAR(100)	DEFAULT '',
	"Fecha"							DATE			DEFAULT '0001-01-01',
	"Monto"							DECIMAL(26,8)	DEFAULT 0,
	"Moneda"						NVARCHAR(3)		DEFAULT '',
	"FechaRepactacion"				DATE			DEFAULT '0001-01-01',
	"FechaAbono"					DATE			DEFAULT '0001-01-01',
	"MontoAbono"					DECIMAL(26,8)	DEFAULT 0,
	"MonedaAbono"					NVARCHAR(3)		DEFAULT ''
);
										
CREATE TYPE BSCL."BSCL.Normativa.BD::NOR.SALIDA_NCG380OPE" AS TABLE  (
	"NombreCliente" NVARCHAR(302),
	"RutCliente" NVARCHAR(40),
	"CodigoPortafolio" NVARCHAR(100),
	"CodigoBolsa" NVARCHAR(40),
	"Folio" NVARCHAR(40),
	"Instrumento" NVARCHAR(100),
	"NombreTipoOperacion" NVARCHAR(3),
	"IdOperacion" BIGINT DEFAULT 0,
	"IdBolsa" NVARCHAR(40),
	"Fecha" NVARCHAR(10),
	"Hora" NVARCHAR(8),
	"NumeroOperador" NVARCHAR(100),
	"ISIN" NVARCHAR(40),
	"Nemotecnico" NVARCHAR(100),
	"NemoCMF" NVARCHAR(50),
	"TipoInstrumento" NVARCHAR(5),
	"TipoOperacionCod" CHAR(40) DEFAULT '',
	"CondicionLiquidacion" NVARCHAR(3),
	"Cantidad" DECIMAL(27,8) DEFAULT 0,
	"MontoTotal" DECIMAL(27,8) DEFAULT 0,
	"Moneda" NVARCHAR(3),
	"Precio" DECIMAL(27,8) DEFAULT 0,
	"Mercado" NVARCHAR(3),
	"SistemaTransaccion" NVARCHAR(3),
	"ModalidadEjecucion" NVARCHAR(3),
	"Canal" NVARCHAR(4),
	"RutContraparte" NVARCHAR(20),
	"DigitoContraparte" NVARCHAR(1),
	"Pasaporte" NVARCHAR(20),
	"GLEI" NVARCHAR(20),
	"Observaciones" NVARCHAR(100),
	"FolioComprobante" NVARCHAR(100),
	"IdTipoComprobante" NVARCHAR(100),
	"TipoComprobante" NVARCHAR(100),
	"FolioCarteraCustodia" BIGINT DEFAULT 0,
    "InstrumentoNegociacion" NVARCHAR(40) NOT NULL DEFAULT ''		
);

CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_NCG380OperacionesBur" AS TABLE
(
	"Periodo"            			NVARCHAR(7)		DEFAULT '', 
	"NombreCorredor"				NVARCHAR(200)	DEFAULT '', 
	"CodigoCorredor"				NVARCHAR(50)	DEFAULT '', 
	--Clientes no mila
	"TotalMontoNoMila"				DECIMAL(26,8)	DEFAULT 0,
	"CompraMontoNoMila"				DECIMAL(26,8)	DEFAULT 0,
	"VentaMontoNoMila"				DECIMAL(26,8)	DEFAULT 0,
	"TotalNegNoMila"				DECIMAL(26,8)	DEFAULT 0,
	"CompraNegNoMila"				DECIMAL(26,8)	DEFAULT 0,
	"VentaNegNoMila"				DECIMAL(26,8)	DEFAULT 0,
	"NumeroClientesNoMila"			BIGINT 	DEFAULT 0,
	--Cliente mila
	"TotalMontoMila"				DECIMAL(26,8)	DEFAULT 0,
	"CompraMontoMila"				DECIMAL(26,8)	DEFAULT 0,
	"VentaMontoMila"				DECIMAL(26,8)	DEFAULT 0,
	"TotalNegMila"					DECIMAL(26,8)	DEFAULT 0,
	"CompraNegMila"					DECIMAL(26,8)	DEFAULT 0,
	"VentaNegMila"					DECIMAL(26,8)	DEFAULT 0,
	"NumeroClientesMila"			BIGINT 	DEFAULT 0,
	--Clientes institucionales
	"TotalMontoLocal"				DECIMAL(26,8)	DEFAULT 0,
	"CompraMontoLocal"				DECIMAL(26,8)	DEFAULT 0,
	"VentaMontoLocal"				DECIMAL(26,8)	DEFAULT 0,
	"TotalNegLocal"					DECIMAL(26,8)	DEFAULT 0,
	"CompraNegLocal"				DECIMAL(26,8)	DEFAULT 0,
	"VentaNegLocal"					DECIMAL(26,8)	DEFAULT 0,
	"NumeroClientesLocal"			BIGINT 	DEFAULT 0,
	--Clientes retail
	"TotalMontoRetail"				DECIMAL(26,8)	DEFAULT 0,
	"CompraMontoRetail"				DECIMAL(26,8)	DEFAULT 0,
	"VentaMontoRetail"				DECIMAL(26,8)	DEFAULT 0,
	"TotalNegRetail"				DECIMAL(26,8)	DEFAULT 0,
	"CompraNegRetail"				DECIMAL(26,8)	DEFAULT 0,
	"VentaNegRetail"				DECIMAL(26,8)	DEFAULT 0,
	"NumeroClientesRetail"			BIGINT 	DEFAULT 0,
	--Clientes cuenta propia y relacionados
	"TotalMontoPropia"				DECIMAL(26,8)	DEFAULT 0,
	"CompraMontoPropia"				DECIMAL(26,8)	DEFAULT 0,
	"VentaMontoPropia"				DECIMAL(26,8)	DEFAULT 0,
	"TotalNegPropia"				DECIMAL(26,8)	DEFAULT 0,
	"CompraNegPropia"				DECIMAL(26,8)	DEFAULT 0,
	"VentaNegPropia"				DECIMAL(26,8)	DEFAULT 0,
	"NumeroClientesPropia"			BIGINT 	DEFAULT 0,
	--Clientes otros
	"TotalMontoOtros"				DECIMAL(26,8)	DEFAULT 0,
	"CompraMontoOtros"				DECIMAL(26,8)	DEFAULT 0,
	"VentaMontoOtros"				DECIMAL(26,8)	DEFAULT 0,
	"TotalNegOtros"					DECIMAL(26,8)	DEFAULT 0,
	"CompraNegOtros"				DECIMAL(26,8)	DEFAULT 0,
	"VentaNegOtros"					DECIMAL(26,8)	DEFAULT 0,
	"NumeroClientesOtros"			BIGINT 	DEFAULT 0
	
);										


SET SCHEMA "BSCL";GO
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_LIBROOPERACIONES" CASCADE;GO										
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_LIBROOPERACIONES" AS TABLE
	(
				---CAMPOS COMUNES
			"FechaMovimiento"			DATETIME DEFAULT NULL,
			"FolioTransaccion"			NVARCHAR(40) DEFAULT '' NULL,
			"IdOrden"					NVARCHAR(40) DEFAULT '' NULL,
			"IdComprobante"				NVARCHAR(40) DEFAULT '' NULL,
			"IdOperacion"				BIGINT DEFAULT 0 NULL,
			"IdCliente"					BIGINT DEFAULT 0 NULL,
			"RutCliente"				NVARCHAR(40) DEFAULT '' NULL,
			"NombreCliente"				NVARCHAR(302) DEFAULT '' NULL,
			"IdPortafolio"				BIGINT DEFAULT 0 NULL,
			
			"CodigoPortafolio" 			NVARCHAR(100),
			"AreaCliente" 				NVARCHAR(100) DEFAULT '',
			"IdClasificacionTipoInstrumento" NVARCHAR(6) 	 DEFAULT '' NULL,
			"IdTipoInstrumento"  			 NVARCHAR(3)	 DEFAULT '' NULL,
			"IdTipoOperacion"				 NVARCHAR(6)     DEFAULT '' NULL,
			"ClasificacionTipoInstrumento"	 NVARCHAR(100) DEFAULT '' NULL,
			"TipoInstrumento"				 NVARCHAR(100) DEFAULT '' NULL,
			"TipoOperacion"					 NVARCHAR(100) DEFAULT '' NULL,
			"AccionOperacion"				NVARCHAR(40)  DEFAULT '' NULL,
			"Relacion"						NVARCHAR(2)  DEFAULT '' NULL,--SI/NO
			"CodigoTipoRelacion"			NVARCHAR(40)  DEFAULT '' NULL,
			"TipoRelacion"					NVARCHAR(255)  DEFAULT '' NULL,
			
			"IdEmisor"					NVARCHAR(40)  DEFAULT '' NULL,
			"Nemotecnico"				NVARCHAR(100) DEFAULT '' NULL,
			"Cantidad"					DECIMAL(27,8) DEFAULT 0 NULL,
			"Precio"					DECIMAL(27,8)	DEFAULT 0 NULL,
			"Tasa"						DECIMAL(27,8)	DEFAULT 0 NULL,
			"IdMoneda"					NVARCHAR(3)		DEFAULT '' NULL,
			"IdMonedaSecundaria"		NVARCHAR(3)		DEFAULT '' NULL,
				---ESPECIFICOS: FACTURACION
			"TasaCompra"				DECIMAL(26,8) DEFAULT 0 NULL,			
			"RelacionInstrumento"		NVARCHAR(40) DEFAULT '' NULL,
			"IdOperadorContraparte"		BIGINT DEFAULT 0 NULL,
			"IdOperadorCorredor"		BIGINT DEFAULT 0 NULL,			
			"IdTipoComprobante"			NVARCHAR(6) DEFAULT '' NULL,
			"NombreTipoComprobante"		NVARCHAR(100) DEFAULT '' NULL,
			"FolioComprobante"			BIGINT DEFAULT 0 NULL,
			"ValorPresenteCompraInst"	DECIMAL(27,8) DEFAULT 0 NULL,
			"FechaVencimiento"			DATETIME DEFAULT NULL,
			"MontoCompra"				DECIMAL(27,8)	DEFAULT 0 NULL,
			"MontoVenta"				DECIMAL(27,8)	DEFAULT 0 NULL,
			"Comision"					DECIMAL(27,8)	DEFAULT 0 NULL,
			"Derecho"					DECIMAL(27,8)	DEFAULT 0 NULL,
			"CargoFijo"					DECIMAL(27,8)	DEFAULT 0 NULL,
			"IVA"						DECIMAL(27,8)	DEFAULT 0 NULL,
			"TotalCompra"				DECIMAL(27,8)	DEFAULT 0 NULL,
			"TotalVenta"				DECIMAL(27,8)	DEFAULT 0 NULL,
			"IdCondicionLiquidacion" 	NVARCHAR(3) DEFAULT '' NULL,
			"IdDocumentoRelacion"		BIGINT DEFAULT 0 NULL,
			"FechaVencimientoPacto"		DATETIME DEFAULT NULL,
			"PrecioPlazo"				DECIMAL(27,8)	DEFAULT 0 NULL,
			"TasaPlazo"					DECIMAL(27,8)	DEFAULT 0 NULL,
			"IdMonedaPlazo"				NVARCHAR(3)		DEFAULT ''  NULL,
			"TotalCompraVencimiento"	DECIMAL(27,8)	DEFAULT 0 NULL,
			"TotalVentaVencimiento"		DECIMAL(27,8)	DEFAULT 0 NULL,
			"Custodia"					NVARCHAR(3) DEFAULT '' NULL,
			"IdBoveda"					NVARCHAR(6) DEFAULT '' NULL,
			"CantidadGarantia"			DECIMAL(27,8)	DEFAULT 0 NULL,
			"NemoSVS"					NVARCHAR(10) DEFAULT '' NULL,
			"TasaVenta"					DECIMAL(27,8)	DEFAULT 0 NULL,
			"ValorPresente"				DECIMAL(27,8)	DEFAULT 0 NULL,
			"UtilidadPerdida"			DECIMAL(27,8)	DEFAULT 0 NULL,
			"FechaOrigenPacto"			DATETIME DEFAULT NULL,
			"IdEstadoFactura"			NVARCHAR(3) DEFAULT '' NULL,
			"Canal"						NVARCHAR(3) DEFAULT '' NULL,
			"ValorIVA"					DECIMAL(11, 4) DEFAULT 0 NULL,
			"FolioComprobanteAsociado" 	BIGINT DEFAULT 0 NULL,
			"NombreComprador"			NVARCHAR(100) 	DEFAULT '' NULL,
			"NombreVendedor"			NVARCHAR(100) 	DEFAULT '' NULL,
			"RutComprador"				NVARCHAR(40) 	DEFAULT '' NULL,
			"RutVendedor"				NVARCHAR(40) 	DEFAULT '' NULL,
			"IdBolsa"					NVARCHAR(3)		DEFAULT '' NULL,
			"Proceso"					BIGINT 			DEFAULT 0 NULL,
			"EsVentaCorta"			 	NVARCHAR(2)     DEFAULT '' NULL,
			"EsVentaCortaUI"		 	NVARCHAR(40)    DEFAULT '' NULL,
			"FolioDTE"					NVARCHAR(40) 	DEFAULT '' NULL
					
	);			

CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_UAFCIR63PEP" AS TABLE
									(
									"IdPersona" 						BIGINT,
									"IdCliente" 						BIGINT,
									"CodigoPortafolio" 					NVARCHAR(100),
									"NombreCliente" 					NVARCHAR(100),
									"RutCliente"						NVARCHAR(40)	DEFAULT '',
									"CodigoEjecutivo" 					NVARCHAR(100),
									"NombreEjecutivo" 					NVARCHAR(100),
									"FechaConsulta"			      		NVARCHAR(10),
									"Titulos" 							NVARCHAR(255)
									);	

CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_UAFROE" AS TABLE  
								   ( 
								   
									--- DATOS SUCURSAL
									"RegionSucursal"					NVARCHAR(2)		DEFAULT '' NULL,
									"LocalidadSucursal"					NVARCHAR(6)		DEFAULT '' NULL,
									"CalleSucursal"						NVARCHAR(100)	DEFAULT '' NULL,
									"NumeroSucursal"					NVARCHAR(40)	DEFAULT '' NULL,
									"DeptoSucursal"						NVARCHAR(40)	DEFAULT '' NULL,
									"TelefonoSucursal"					NVARCHAR(40)	DEFAULT '' NULL,
									--- DATOS OPERACION
									"IdComprobante"						BIGINT			DEFAULT 0 NULL,
									"FechaLiquidacion"					NVARCHAR(10)	DEFAULT '' NULL,
									"IdPersona"							BIGINT			DEFAULT 0 NULL,
									"IdPersonaTitularPago"				BIGINT			DEFAULT 0 NULL,
									---ENTRANTE
									"IdMonedaEntrante"					NVARCHAR(3)		DEFAULT '' NULL,
									"MontoEntrante"						DECIMAL(27,8)	DEFAULT 0 NULL,
									"DenBilleteEntrante"				NVARCHAR(100)	DEFAULT '' NULL,
									---SALIENTE
									"IdMonedaSaliente"					NVARCHAR(3)		DEFAULT '' NULL,
									"MontoSaliente"						DECIMAL(27,8)	DEFAULT 0 NULL,
									"DenBilleteSaliente"				NVARCHAR(100)	DEFAULT '' NULL,
									"NumComprobante"					BIGINT			DEFAULT 0 NULL,
									"Fecha"								NVARCHAR(10)	DEFAULT '' NULL,
									----CLIENTE		
									"Identificacion" 					NVARCHAR(40)	DEFAULT '' NULL,
									"TipoDocumento"						NVARCHAR(10)	DEFAULT '' NULL,
									"RazonSocial"						NVARCHAR(100)	DEFAULT '' NULL,
									"ApellidoPaterno"					NVARCHAR(100)	DEFAULT '' NULL,
									"ApellidoMaterno"					NVARCHAR(100)	DEFAULT '' NULL,
									"Nacionalidad"						NVARCHAR(3)		DEFAULT '' NULL,
									"Habitualidad"						NVARCHAR(40)	DEFAULT '' NULL,
									"Pais"								NVARCHAR(3)		DEFAULT '' NULL,
									"Region"							NVARCHAR(2)		DEFAULT '' NULL,
									"Comuna"							NVARCHAR(6)		DEFAULT '' NULL,
									"Calle"								NVARCHAR(100)	DEFAULT '' NULL,
									"Numero"							NVARCHAR(40)	DEFAULT '' NULL,
									"Departamento"						NVARCHAR(40)	DEFAULT '' NULL,
									"Telefono"							NVARCHAR(40)	DEFAULT '' NULL,
									---REPRESENTANTE PAGADOR
									"RepIdentificacion"					NVARCHAR(40)	DEFAULT '' NULL,
									"RepTipoDocumento"					NVARCHAR(10)	DEFAULT '' NULL,
									"RepRazonSocial"					NVARCHAR(100)	DEFAULT '' NULL,
									"RepApellidoPaterno"				NVARCHAR(100)	DEFAULT '' NULL,
									"RepApellidoMaterno"				NVARCHAR(100)	DEFAULT '' NULL,
									"RepNacionalidad"					NVARCHAR(3)		DEFAULT '' NULL,
									"RepHabitualidad"					NVARCHAR(40)	DEFAULT '' NULL,
									"RepPais"							NVARCHAR(3)		DEFAULT '' NULL,
									"RepRegion"							NVARCHAR(2)		DEFAULT '' NULL,
									"RepComuna"							NVARCHAR(6)		DEFAULT '' NULL,
									"RepCalle"							NVARCHAR(100)	DEFAULT '' NULL,
									"RepNumero"							NVARCHAR(40)	DEFAULT '' NULL,
									"RepDepartamento"					NVARCHAR(40)	DEFAULT '' NULL,
									"RepTelefono"						NVARCHAR(40)	DEFAULT '' NULL
									--"IdTipoMovimiento"					NVARCHAR(10)	DEFAULT '' NULL,
									--"IdMoneda"							NVARCHAR(3)		DEFAULT '' NULL,
									--"Monto"								DECIMAL(27,8)	DEFAULT 0 NULL
								   ); 
			


CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_UAFDOF" AS TABLE  
								   ( 
 								    "p_FechaOperacionDesde"					NVARCHAR(10)	DEFAULT '' NULL,
								    "p_FechaOperacionHasta"					NVARCHAR(10)	DEFAULT '' NULL,
								    /*
								    "LogoCorredora" 						BLOB, 
								    "RazonCorredora"						NVARCHAR(100)	DEFAULT '' NULL,
									"DireccionCorredora"					NVARCHAR(200)	DEFAULT '' NULL,
									"RutCorredora"							NVARCHAR(40)	DEFAULT '' NULL,
									"TelefonoCorredora"						NVARCHAR(40)	DEFAULT '' NULL,
									*/
								    "IdComprobanteFormaDePago"				BIGINT			DEFAULT 0 NULL,
									"FechaLiquidacion"						NVARCHAR(10)	DEFAULT '' NULL,
									"IdComprobante"							BIGINT			DEFAULT 0 NULL,
									"IdTipoOperacion"						NVARCHAR(6)		DEFAULT '' NULL,
									"TipoOperacion"							NVARCHAR(100)	DEFAULT '' NULL,
									"IdPersona"								BIGINT			DEFAULT 0 NULL,
									"Identificacion"						NVARCHAR(40)	DEFAULT '' NULL,
									"TipoDocumento"							NVARCHAR(10)	DEFAULT '' NULL,
									"TipoPersona"							NVARCHAR(40)	DEFAULT '' NULL,
									"NombreCompleto"						NVARCHAR(350)	DEFAULT '' NULL,
									"RazonSocial"							NVARCHAR(100)	DEFAULT '' NULL,
									"ApellidoPaterno"						NVARCHAR(100)	DEFAULT '' NULL,
									"ApellidoMaterno"						NVARCHAR(100)	DEFAULT '' NULL,
									"Telefono"								NVARCHAR(40)	DEFAULT '' NULL,
									"IdPersonaTitularPago" 					BIGINT			DEFAULT 0 NULL,
									"NombreCompletoPagador"					NVARCHAR(350)	DEFAULT '' NULL,
									"IdentificacionPagador"					NVARCHAR(40)	DEFAULT '' NULL,
									"TipoDocumentoPagador"					NVARCHAR(10)	DEFAULT '' NULL,
									"MontoOperacion"						DECIMAL(27,8)	DEFAULT 0 NULL,
									"IdMonedaOperacion"						NVARCHAR(3)		DEFAULT '' NULL,
									"MontoComprobanteFormaDePago"			DECIMAL(27,8)	DEFAULT 0 NULL,
									"IdMonedaFormaDePago"					NVARCHAR(3)		DEFAULT '' NULL,
									"MonedaFormaDePago"						NVARCHAR(100)	DEFAULT '' NULL,
									"IdMedioDePago"							NVARCHAR(3)		DEFAULT '' NULL,
									"NombreMedioDePago" 					NVARCHAR(100)	DEFAULT '' NULL,
									"IdModalidadDePago"						NVARCHAR(100)	DEFAULT '' NULL,
									"NombreModalidadDePago"					NVARCHAR(100)	DEFAULT '' NULL,
									"IdEmpleadoDigitador"					BIGINT			DEFAULT 0 NULL,
									"NombreAgente"							NVARCHAR(345)	DEFAULT '' NULL,
									"IdSucursal"							BIGINT			DEFAULT 0 NULL,
									"CodigoSucursal"						NVARCHAR(40)	DEFAULT '' NULL,
									"NombreSucursal"						NVARCHAR(520)	DEFAULT '' NULL,
									"IdCliente"								BIGINT			DEFAULT 0 NULL,
									"IdDeclaracionOrigenFondos"				BIGINT			DEFAULT 0 NULL,
									"Comentario"							NVARCHAR(500)	DEFAULT '' NULL
								   ); 
								   
SET SCHEMA "BSCL";GO
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_OPERACIONBASE";GO
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_OPERACIONBASE" AS TABLE
	(
				---CAMPOS COMUNES

			"FechaCreacion"				DATETIME DEFAULT NULL,    	--Fecha de BD
			"FechaMovimiento"			DATETIME DEFAULT NULL,		--Fecha del sistema
			"FolioTransacion"			NVARCHAR(40) DEFAULT '' NULL,
			"IdOrden"					NVARCHAR(40) DEFAULT '' NULL,
			"IdOperacion"				BIGINT DEFAULT 0 NULL,
			"IdTipoOperacion"			NVARCHAR(6) DEFAULT '' NULL,
			"IdCliente"					BIGINT DEFAULT 0 NULL,
			"IdPortafolio"				BIGINT DEFAULT 0 NULL,
			"IdClasificacionTipoInstrumento" NVARCHAR(6) DEFAULT '' NULL,
			--"Mercado"					NVARCHAR(6) DEFAULT '' NULL,
			"IdEmisor"					NVARCHAR(40)  DEFAULT '' NULL,
			"Nemotecnico"				NVARCHAR(100) DEFAULT '' NULL,
			"Cantidad"					DECIMAL(27,8) DEFAULT 0 NULL,
			"Precio"					DECIMAL(27,8)	DEFAULT 0 NULL, --Precio a Moneda operacion
			"Monto"						DECIMAL(27,8)	DEFAULT 0 NULL, --Monto de la operacion 
			"Tasa"						DECIMAL(27,8)	DEFAULT 0 NULL,
			"IdMoneda"					NVARCHAR(3)		DEFAULT '' NULL,--Moneda de la operacion
			"IdInstrumento"				BIGINT DEFAULT 0 NULL,
			"IdTipoInstrumento"  		NVARCHAR(3)	 DEFAULT '' NULL,	
			"IdTipoProducto" 			NVARCHAR(4)  DEFAULT '' NULL,
			--- (compra o Venta)
			"AccionOperacion"			NVARCHAR(40)  DEFAULT '' NULL,
			"MontoCompra"				DECIMAL(27,8)	DEFAULT 0 NULL,
			"MontoVenta"				DECIMAL(27,8)	DEFAULT 0 NULL,
			"TasaCompra"				DECIMAL(27,8)	DEFAULT 0 NULL,
			"TasaVenta"					DECIMAL(27,8)	DEFAULT 0 NULL,
			--aplicando comision/derecho/gasto
			"TotalCompra"				DECIMAL(27,8)	DEFAULT 0 NULL,
			"TotalVenta"				DECIMAL(27,8)	DEFAULT 0 NULL,
			
				---ESPECIFICOS: FACTURACION
			"IdTransaccion"				BIGINT DEFAULT 0 NULL,
			"FechaTransaccion"        	SECONDDATE DEFAULT '0001-01-01 00:00:00',
			"RelacionInstrumento"		NVARCHAR(40) DEFAULT '' NULL,
			"IdContraparte"				BIGINT DEFAULT 0 NULL,
			"IdOperadorContraparte"		BIGINT DEFAULT 0 NULL,
			"IdOperadorCorredor"		BIGINT DEFAULT 0 NULL,			
			"IdTipoComprobante"			NVARCHAR(6) DEFAULT '' NULL,
			"FolioComprobante"			BIGINT DEFAULT 0 NULL,
			"ValorPresenteCompraInst"	DECIMAL(27,8) DEFAULT 0 NULL,
			"FechaVencimiento"			DATETIME DEFAULT NULL,
			"Comision"					DECIMAL(27,8)	DEFAULT 0 NULL,
			"Derecho"					DECIMAL(27,8)	DEFAULT 0 NULL,
			"Gasto"						DECIMAL(27,8)	DEFAULT 0 NULL,
			"Impuesto"					DECIMAL(27,8)	DEFAULT 0 NULL,

			"IdCondicionLiquidacion" 	NVARCHAR(3) DEFAULT '' NULL,
			"IdDocumentoRelacion"		BIGINT DEFAULT 0 NULL,
			"FechaVencimientoPacto"		DATETIME DEFAULT NULL,
			"PrecioPlazo"				DECIMAL(27,8)	DEFAULT 0 NULL,
			"TasaPlazo"					DECIMAL(27,8)	DEFAULT 0 NULL,
			"IdMonedaPlazo"				NVARCHAR(3)		DEFAULT ''  NULL,
			"TotalCompraVencimiento"	DECIMAL(27,8)	DEFAULT 0 NULL,
			"TotalVentaVencimiento"		DECIMAL(27,8)	DEFAULT 0 NULL,
			"Custodia"					NVARCHAR(3) DEFAULT '' NULL,
			"IdBolsa"					NVARCHAR(3) DEFAULT '' NULL,
			"IdBoveda"					NVARCHAR(6) DEFAULT '' NULL,
			"CantidadGarantia"			DECIMAL(27,8)	DEFAULT 0 NULL,
			"NemoSVS"					NVARCHAR(10) DEFAULT '' NULL,
			"ValorPresente"				DECIMAL(27,8)	DEFAULT 0 NULL,
			"UtilidadPerdida"			DECIMAL(27,8)	DEFAULT 0 NULL,
			"FechaOrigenPacto"			DATETIME DEFAULT NULL,
			"DescripcionOperacion"		NVARCHAR(100) DEFAULT '' NULL,
			"IdEstadoFactura"			NVARCHAR(3) DEFAULT '' NULL,
			"Canal"						NVARCHAR(3) DEFAULT '' NULL,
			"ValorIVA"					DECIMAL(11, 4) DEFAULT 0 NULL,
			"FolioComprobanteAsociado" 	BIGINT DEFAULT 0 NULL,
			"ModalidadEjecucion"		NVARCHAR(3) DEFAULT '' NULL,
			"EsFungible"				CHAR(1) DEFAULT '' NULL,
			"IdMovimientoRelacionado"   BIGINT DEFAULT 0 NULL,
			"CodigoOperadorContraparte" NVARCHAR(40) DEFAULT '' NULL,
			"CodigoOperadorCorredor" NVARCHAR(40) DEFAULT '' NULL,
			---ESPECIFICOS: MESA RF	
			"TipoOperacionSecundaria"	NVARCHAR(6) DEFAULT '' NULL,
			"ValorRescateInicial"		DECIMAL(26,8) DEFAULT 0 NULL,
			---ESPECIFICOS: SPOT
			"IdMonedaSecundaria"		NVARCHAR(3)		DEFAULT '' NULL,
			--NORMATIVA
			"IdNormativa"              		 NVARCHAR(15)    DEFAULT '' NULL,
			"IdTipoDocumento" 				 NVARCHAR(10) 	 NOT NULL DEFAULT '',
			"HIdTipoProducto" 				 NVARCHAR(4) 	 DEFAULT '' NULL,
			"HIdClasificacionTipoInstrumento" NVARCHAR(6) 	 DEFAULT '' NULL,
			"HIdTipoInstrumento"  			 NVARCHAR(3)	 DEFAULT '' NULL,
			"HIdTipoOperacion"				 NVARCHAR(6)     DEFAULT '' NULL,
			"Uso"         					 NVARCHAR(10)    DEFAULT '' NULL,
			"EsEnRueda"				 		 NVARCHAR(2)     DEFAULT '' NULL,
			"EsVentaCorta"			 		 NVARCHAR(2)     DEFAULT '' NULL
			
			
	);GO						
	
/*	
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_OPERACION1064" AS TABLE
(
		
"Registro"				NVARCHAR(4000) DEFAULT '' NULL
		
);	

CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_OPERACION1064RUEDA" AS TABLE
(
		
"Registro"				NVARCHAR(4000) DEFAULT '' NULL
		
);	

CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_OPERACION1064FUERARUEDA" AS TABLE
(
		
"Registro"				NVARCHAR(4000) DEFAULT '' NULL
		
);	
*/
/*
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_OPERACION1064" AS TABLE
(
			---CAMPOS COMUNES
		
		"Fecha" 					NVARCHAR(8) DEFAULT '' NULL
		,"Hora"						NVARCHAR(6) DEFAULT '' NULL
		,"Mercado"					NVARCHAR(1) DEFAULT '' NULL
		,"Bolsa"					NVARCHAR(2) DEFAULT '' NULL
		,"Folio"					NVARCHAR(7) DEFAULT '' NULL
		,"Secuencia"				NVARCHAR(3) DEFAULT '' NULL
		,"Interm_inf"				NVARCHAR(5) DEFAULT '' NULL
		,"Interm_con"				NVARCHAR(5) DEFAULT '' NULL
		,"Num_orden"				NVARCHAR(4) DEFAULT '' NULL
		,"Nemo"						NVARCHAR(20) DEFAULT '' NULL
		,"Fecven"					NVARCHAR(8) DEFAULT '' NULL
		,"Tipo_ope"					NVARCHAR(7) DEFAULT '' NULL
		,"Plazo"					NVARCHAR(5) DEFAULT '' NULL
		,"Liquidacion"				NVARCHAR(2) DEFAULT '' NULL
		,"Sistema"					NVARCHAR(4) DEFAULT '' NULL
		,"Tipo_cli"					NVARCHAR(2) DEFAULT '' NULL
		,"Cantidad"					NVARCHAR(10) DEFAULT '' NULL
		,"Tipo_unid"				NVARCHAR(2) DEFAULT '' NULL
		,"Tipo_dolar"				NVARCHAR(4) DEFAULT '' NULL
		,"Numcon"					NVARCHAR(7) DEFAULT '' NULL
		,"Precio"					NVARCHAR(11) DEFAULT '' NULL
		,"Monto"					NVARCHAR(11) DEFAULT '' NULL
		,"Tipo_reaj"				NVARCHAR(2) DEFAULT '' NULL
		,"Valpar"					NVARCHAR(3) DEFAULT '' NULL
		,"Tir"						NVARCHAR(2) DEFAULT '' NULL
		,"Rentab"					NVARCHAR(3) DEFAULT '' NULL
		,"Tipo_reaj_pac"			NVARCHAR(2) DEFAULT '' NULL
		,"Observ"					NVARCHAR(2) DEFAULT '' NULL
		,"Modific"					NVARCHAR(2) DEFAULT '' NULL
		,"Folio_modif"				NVARCHAR(12) DEFAULT '' NULL
		,"Espacios"					NVARCHAR(18) DEFAULT '' NULL
);								

CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_OPERACION1064RUEDA" AS TABLE
(
			---CAMPOS COMUNES
		
		"Fecha" 					NVARCHAR(10) DEFAULT '' NULL
		,"Corredor"					NVARCHAR(3) DEFAULT '' NULL
		,"NemoCorr"					NVARCHAR(10) DEFAULT '' NULL
		,"RutCorr"					NVARCHAR(11) DEFAULT '' NULL
		,"RutCli"					NVARCHAR(11) DEFAULT '' NULL
		,"SecuenciaCli"				NVARCHAR(2) DEFAULT '' NULL
		,"NemoIns"				    NVARCHAR(10) DEFAULT '' NULL
		,"TipoIns"					NVARCHAR(2) DEFAULT '' NULL
		,"Rueda"					NVARCHAR(1) DEFAULT '' NULL
		,"TipoRel"					NVARCHAR(1) DEFAULT '' NULL
		,"TipoOpe"					NVARCHAR(2) DEFAULT '' NULL
		,"FolioTrans"				NVARCHAR(8) DEFAULT '' NULL
		,"CantidadTrans"			NVARCHAR(15) DEFAULT '' NULL
		,"MontoTrans"				NVARCHAR(15) DEFAULT '' NULL
		
);				


CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_OPERACION1064FUERARUEDA" AS TABLE
(
			---CAMPOS COMUNES
		
		"CodCorr" 					NVARCHAR(5) DEFAULT '' NULL
		,"TipoTrans"				NVARCHAR(1) DEFAULT '' NULL
		,"CodCorrCont"				NVARCHAR(5) DEFAULT '' NULL
		,"Cantidad"					NVARCHAR(13) DEFAULT '' NULL
		,"Nemotecnico"				NVARCHAR(10) DEFAULT '' NULL
		,"Plazo"					NVARCHAR(4) DEFAULT '' NULL
		,"Tir"				    	NVARCHAR(5) DEFAULT '' NULL
		,"Precio"					NVARCHAR(5) DEFAULT '' NULL
		,"Condicion"				NVARCHAR(2) DEFAULT '' NULL
		,"Disponible"				NVARCHAR(2) DEFAULT '' NULL
		,"IndComVen"				NVARCHAR(1) DEFAULT '' NULL
		,"TipoReaj"					NVARCHAR(2) DEFAULT '' NULL
		,"Emisor"					NVARCHAR(10) DEFAULT '' NULL
		,"NumComprobante"			NVARCHAR(4) DEFAULT '' NULL
		,"TipoOpe"					NVARCHAR(1) DEFAULT '' NULL
		,"PlazoPac"					NVARCHAR(5) DEFAULT '' NULL
		,"RentPac"					NVARCHAR(5) DEFAULT '' NULL
		,"TipoRenPac"				NVARCHAR(2) DEFAULT '' NULL
		,"NumBolsa"					NVARCHAR(2) DEFAULT '' NULL
		,"ValCaptacion"				NVARCHAR(11) DEFAULT '' NULL
		,"Disponible2"				NVARCHAR(19) DEFAULT '' NULL
);
*/
/*
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_INFOTRANOFIC" AS TABLE
(
			---CAMPOS COMUNES
		
		"RutIntermediario"			NVARCHAR(40) DEFAULT '' NULL
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
		,"IdTipoOperacion"			NVARCHAR(6) DEFAULT '' NULL
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
);					
*/
	
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_FATCADETALLE" AS TABLE
(
--"IdClienteFatca"			BIGINT          NOT NULL,
"Rut"						NVARCHAR(10)	DEFAULT '' NULL,
"TipoPersona"				NVARCHAR(20)	DEFAULT '' NULL,
"Indicios"					NVARCHAR(2)		DEFAULT '' NULL,
"ClasificacionFatca"        NVARCHAR(20)	DEFAULT '' NULL,
"ClienteFatca"				NVARCHAR(2)		DEFAULT '' NULL,
"AutorizaOrgFatca"			NVARCHAR(2)		DEFAULT '' NULL,
"PatrimonioClp"				NVARCHAR(100)	DEFAULT '' NULL,
"PatrimonioUsd"				NVARCHAR(100)	DEFAULT '' NULL,
"FechaEnrolamiento"			NVARCHAR(10)	DEFAULT '' NULL,
"FechaModIndicios"			NVARCHAR(10)	DEFAULT '' NULL,
"FechaModFacta"				NVARCHAR(10)	DEFAULT '' NULL,
						
"Residencia"				NVARCHAR(10)	DEFAULT '' NULL,
"LugarNacimiento"           NVARCHAR(10)	DEFAULT '' NULL,
"PaisConstitucion"          NVARCHAR(10)	DEFAULT '' NULL,
"CasaMatriz"                NVARCHAR(10)	DEFAULT '' NULL,
"Nacionalidad"              NVARCHAR(10)	DEFAULT '' NULL,
"CodPostal"                 NVARCHAR(10)	DEFAULT '' NULL,
"NumTelefono"               NVARCHAR(10)	DEFAULT '' NULL,
"OrdenPermanente"           NVARCHAR(10)	DEFAULT '' NULL,
"PoderEfectivo"             NVARCHAR(10)	DEFAULT '' NULL,
"Beneficiario"              NVARCHAR(10)	DEFAULT '' NULL,
--------------------------------------------------------------Info Relacionado						 
"NumIdentificacion"         NVARCHAR(10)	DEFAULT '' NULL,
"TipoPersonaRel"            NVARCHAR(20)	DEFAULT '' NULL,
"TipoRelacion"              NVARCHAR(30)	DEFAULT '' NULL,
"ClasificacionFatcaRel"     NVARCHAR(10)	DEFAULT '' NULL,
"ClienteFatcaRel"           NVARCHAR(10)	DEFAULT '' NULL,
"UsPersonRel"               NVARCHAR(10)	DEFAULT '' NULL,
"ResidenciaRel"             NVARCHAR(10)	DEFAULT '' NULL,
"LugarNacimientoRel"        NVARCHAR(10)	DEFAULT '' NULL,
"PaisConstitucionRel"       NVARCHAR(10)	DEFAULT '' NULL,
"CasaMatrizRel"             NVARCHAR(10)	DEFAULT '' NULL,
"NacionalidadRel"           NVARCHAR(10)	DEFAULT '' NULL,
"CodPostalRel"              NVARCHAR(10)	DEFAULT '' NULL,
"NumTelefonoRel"            NVARCHAR(10)	DEFAULT '' NULL,
"OrdenPermanenteRel"        NVARCHAR(10)	DEFAULT '' NULL,
"PoderEfectivoRel"          NVARCHAR(10)	DEFAULT '' NULL,
"BeneficiarioRel"           NVARCHAR(10)	DEFAULT '' NULL,
"FechaCargaInformacion"     NVARCHAR(10)	DEFAULT '' NULL

);	
	
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_FATCARESUMEN" AS TABLE
(
 "Rut"                        NVARCHAR(10)	DEFAULT '' NULL,
 "TipoPersona"                NVARCHAR(20)	DEFAULT '' NULL,
 "Indicios"                   NVARCHAR(10)	DEFAULT '' NULL,
 "ClasificacionFatca"         NVARCHAR(20)	DEFAULT '' NULL,
 "ClienteFatca"               NVARCHAR(10)	DEFAULT '' NULL,
 "AutorizaOrgFatca"           NVARCHAR(10)	DEFAULT '' NULL,
 "PatrimonioClp"              NVARCHAR(100)	DEFAULT '' NULL,
 "PatrimonioUsd"              NVARCHAR(100)	DEFAULT '' NULL,
 "FechaEnrolamiento"          NVARCHAR(10)	DEFAULT '' NULL,
 "FechaModIndicios"           NVARCHAR(10)	DEFAULT '' NULL,
 "FechaModFacta"              NVARCHAR(10)	DEFAULT '' NULL,
 "Residencia"                 NVARCHAR(10)	DEFAULT '' NULL,
 "LugarNacimiento"            NVARCHAR(10)	DEFAULT '' NULL,
 "PaisConstitucion"           NVARCHAR(10)	DEFAULT '' NULL,
 "CasaMatriz"                 NVARCHAR(10)	DEFAULT '' NULL,
 "Nacionalidad"               NVARCHAR(10)	DEFAULT '' NULL,
 "CodPostal"                  NVARCHAR(10)	DEFAULT '' NULL,
 "NumTelefono"                NVARCHAR(10)	DEFAULT '' NULL,
 "OrdenPermanente"            NVARCHAR(10)	DEFAULT '' NULL,
 "PoderEfectivo"              NVARCHAR(10)	DEFAULT '' NULL,
 "Beneficiario"               NVARCHAR(10)	DEFAULT '' NULL,
 "FechaCargaInformacion"      NVARCHAR(10)	DEFAULT '' NULL
);
	
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_PATRIMONIOFATCA" AS TABLE
(
"IdCliente" 				BIGINT  DEFAULT 0 NULL,
"IdPortafolio" 				BIGINT  DEFAULT 0 NULL,
"item" 						NVARCHAR(1024) DEFAULT '' NULL  ,
"monto" 					DECIMAL(26,8)  ,
"roundMont" 				NVARCHAR(1024) DEFAULT '' NULL
 );	
	


CREATE TYPE "BSCL.Normativo.BD::NOR.SALIDA_INFORMEDERECHO" AS TABLE ( 
"TipoRegistro" NVARCHAR(1)
,"Folio" NVARCHAR(6)
,"Mercado" NVARCHAR(2)
,"CorredorCompra" NVARCHAR(3)
,"CorredorVenta" NVARCHAR(3)
,"FechaVencimiento" NVARCHAR(10)
,"Nemotecnico" NVARCHAR(10)
,"Precio" NVARCHAR(17)
,"Tasa" NVARCHAR(7)
,"SignoTasa" NVARCHAR(1)
,"DocumentoIdentidad" NVARCHAR(11)
,"Cantidad" NVARCHAR(17)
,"TipoOperacion" NVARCHAR(1)
,"TipoReajuste" NVARCHAR(1)
,"Monto" NVARCHAR(17)
,"Emisor" NVARCHAR(10)
,"RegistroConcatenado" NVARCHAR(121)
);





/*
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_ANEXO1" AS TABLE  
( 
"FechaTransacciones"				DATE,
"Corredor"    						NVARCHAR(100),
"MontoFacturado"      				DECIMAL(28,8),
"McdoACC"							NVARCHAR(10),
"McdoIRFIIF"						NVARCHAR(10),
"McdoCFI"							NVARCHAR(10),
"McdoFuturos"	 					NVARCHAR(10),
"McdoMonetarios" 					NVARCHAR(10),
"FolioOperacion"					NVARCHAR(100),
"DerechosBolsa"						NVARCHAR(10),
"CPC"								NVARCHAR(10),
"CPV"								NVARCHAR(10),
"ESPC" 								NVARCHAR(10),
"ESPV" 								NVARCHAR(10),
"FechaDigitacion"					DATE
); 
*/


CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_ANEXO1" AS TABLE  
( 
"FechaMovimiento"				DATE,
"IdOperacion"						BIGINT DEFAULT 0 NULL,
"IdCliente"							BIGINT DEFAULT 0 NULL,
"IdPortafolio"						BIGINT DEFAULT 0 NULL,
"IdClasificacionTipoInstrumento" 	NVARCHAR(6) 	 DEFAULT '' NULL,
"IdTipoInstrumento"  			 	NVARCHAR(3)	 DEFAULT '' NULL,
"IdTipoOperacion"				 	NVARCHAR(6)     DEFAULT '' NULL,
"IdMoneda"							NVARCHAR(3)		DEFAULT '' NULL,
"MontoCLP"      					DECIMAL(27,8),
"Monto"								DECIMAL(27,8)	DEFAULT 0 NULL,
"EsCtaPropia"						NVARCHAR(1)		DEFAULT 'N' NULL,
"DerechoPrivilegiado"				NVARCHAR(1)		DEFAULT 'N' NULL,
"DerechoBolsa"						NVARCHAR(1)		DEFAULT 'N' NULL,
"AccionOperacion"				    NVARCHAR(40)  DEFAULT '' NULL,
"HIdClasificacionTipoInstrumento"	NVARCHAR(4) 	DEFAULT ''	not null
); 

/*
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_ANEXO13" AS TABLE  
( 
 "FechaTransacciones"				DATE,
 "Corredor"    						NVARCHAR(100),
 "InterContrap"      				NVARCHAR(10),
 "Bolsa"							NVARCHAR(10),
 "TipoOperacion"					NVARCHAR(10),
 "Cantidad"							DECIMAL(28,8),
 "Instrumento"	 					NVARCHAR(10),
 "Precio" 							DECIMAL(28,8),
 "MontoTransado"					DECIMAL(28,8),
 "FechaSistema"						DATE
); 
*/

CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_ANEXO13" AS TABLE  
( 
"FechaMovimiento"				DATE,
"IdOperacion"						BIGINT DEFAULT 0 NULL,
"IdCliente"							BIGINT DEFAULT 0 NULL,
"IdPortafolio"						BIGINT DEFAULT 0 NULL,
"IdClasificacionTipoInstrumento" 	NVARCHAR(6) 	 DEFAULT '' NULL,
"IdTipoInstrumento"  			 	NVARCHAR(3)	 DEFAULT '' NULL,
"IdTipoOperacion"				 	NVARCHAR(6)     DEFAULT '' NULL,
"IdMoneda"							NVARCHAR(3)		DEFAULT '' NULL,
"MontoCLP"      					DECIMAL(27,8),
"Monto"								DECIMAL(27,8)	DEFAULT 0 NULL,
"EsCtaPropia"						NVARCHAR(1)		DEFAULT 'N' NULL,
"Precio"							DECIMAL(27,8)	DEFAULT 0 NULL, --Precio a Moneda operacion
"Cantidad"							DECIMAL(27,8) DEFAULT 0 NULL,
"Nemotecnico"						NVARCHAR(100) DEFAULT '' NULL,
"IdInstrumento"						BIGINT DEFAULT 0 NULL,
"IdBolsa"							NVARCHAR(3) DEFAULT '' NULL,
"CodigoOperadorContraparte" 		NVARCHAR(40) DEFAULT '' NULL,
"AccionOperacion"				    NVARCHAR(40)  DEFAULT '' NULL
); 


/*
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_ANEXO4" AS TABLE  
( 
 "FechaTransacciones"				DATE,
 "Corredor"    						NVARCHAR(100),
 "CuentaTerceros"      				DECIMAL(28,8),
 "EspecialesOtrosOIB"				DECIMAL(28,8),
 "CuentaPropiaAcciones"				DECIMAL(28,8),
 "CuentaPropiaPlazo"				DECIMAL(28,8),
 "CuentaPropiaOIB" 					DECIMAL(28,8),
 "OperacionPlazoCli" 				DECIMAL(28,8),
 "CliPrivilegiados"					DECIMAL(28,8),
 "CliConvenio" 						DECIMAL(28,8),
 "CliConvenioOIB" 					DECIMAL(28,8),
 "TotalGralDia" 					DECIMAL(28,8),
 "TotalCompraAcciones" 				DECIMAL(28,8),
 "TotalCompraSimultaneas" 			DECIMAL(28,8),
 "TotalCompraAccionesOIB" 			DECIMAL(28,8),
 "TotalVentaAcciones" 				DECIMAL(28,8),
 "TotalVentaSimultaneas" 			DECIMAL(28,8),
 "TotalVentaAccionesOIB" 			DECIMAL(28,8),
 "TotalGral"						DECIMAL(28,8)
); */
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_ANEXO4" AS TABLE  
( 
 "FechaMovimiento"				DATE,
 "CuentaTerceros"      				DECIMAL(28,8),
 "EspecialesOtros"					DECIMAL(28,8),
 "CuentaPropiaAcciones"				DECIMAL(28,8),
 "CuentaPropiaPlazo"				DECIMAL(28,8),
 "CliOperacionPlazo" 				DECIMAL(28,8),
 "CliPrivilegiados"					DECIMAL(28,8),
 "CliConvenio" 						DECIMAL(28,8),
 "TotalDeclarado"					DECIMAL(28,8),
 "TotalCompraAcciones" 				DECIMAL(28,8),
 "TotalCompraPlazo" 				DECIMAL(28,8),
 "TotalVentaAcciones" 				DECIMAL(28,8),
 "TotalVentaPlazo" 					DECIMAL(28,8),
 "TotalGeneral"						DECIMAL(28,8)
); 

    
        CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_CIR1804" AS TABLE  
( 
    "IdPersonaAGF"                      NVARCHAR(7),
    "IdSolicitudAporteRescate"          NVARCHAR(7),
    "PeriodoInformado"                  NVARCHAR(7),
    "RazonSocialAGF"                    NVARCHAR(100),
    "DocumentoIdentidadAGF"             VARCHAR(3),
    "NombreFondo"                       NVARCHAR(100),
    "DocumentoIdentidadFondo"           NVARCHAR(40),
    "IdMoneda"                          NVARCHAR(3),
    "FechaTransaccion"                  NVARCHAR(10),
    "DocumentoIdentidadDelParticipe"    NVARCHAR(40),
    "CodigoDelParticipe"                NVARCHAR(2),
    "PatrimonioNetoInicial"             DECIMAL(28,8),
    "PorcentajeSaldoInicial"            DECIMAL(28,8),
    "PatrimonioNeto"                    DECIMAL(28,8),
    "MontoTransadoEntrada"              DECIMAL(28,8),    
    "MontoTransadoSalida"               DECIMAL(28,8),
    "SaldoIndividual"                   DECIMAL(28,8),
    "SaldoIndividual_P"                 DECIMAL(28,8),
    "SaldoTotal"                        DECIMAL(28,8),
    "SaldoTotal_P"                      DECIMAL(28,8)
);  


SET SCHEMA "BSCL";
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_LibroVentas" CASCADE;										
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_LibroVentas" AS TABLE
	(
				---CAMPOS COMUNES
			"FechaMovimiento"			DATETIME DEFAULT NULL,
			"FolioTransacion"			NVARCHAR(40) DEFAULT '' NULL,
			"SecuenciaOrden"			NVARCHAR(40) DEFAULT '' NULL,
			"IdOperacion"				BIGINT DEFAULT 0 NULL,
			"IdCliente"					BIGINT DEFAULT 0 NULL,
			"RutCliente"				NVARCHAR(40) DEFAULT '' NULL,
			"NombreCliente"				NVARCHAR(302) DEFAULT '' NULL,
			"IdPortafolio"				BIGINT DEFAULT 0 NULL,
			"CodigoPortafolio" 				 NVARCHAR(100),
			"IdClasificacionTipoInstrumento" NVARCHAR(6) 	 DEFAULT '' NULL,
			"IdTipoInstrumento"  			 NVARCHAR(3)	 DEFAULT '' NULL,
			"IdTipoOperacion"				 NVARCHAR(6)     DEFAULT '' NULL,
			"ClasificacionTipoInstrumento"	 NVARCHAR(100) DEFAULT '' NULL,
			"TipoInstrumento"				 NVARCHAR(100) DEFAULT '' NULL,
			"TipoOperacion"					 NVARCHAR(100) DEFAULT '' NULL,
			"AccionOperacion"				NVARCHAR(40)  DEFAULT '' NULL,
			"ClaseOperacion"				NVARCHAR(2)  DEFAULT '' NULL,
			"IdTipoProducto" 			NVARCHAR(4)  DEFAULT '' NULL,
			"IdEmisor"					NVARCHAR(40)  DEFAULT '' NULL,
			"Nemotecnico"				NVARCHAR(100) DEFAULT '' NULL,
			"Cantidad"					DECIMAL(27,8) DEFAULT 0 NULL,
			"Precio"					DECIMAL(27,8)	DEFAULT 0 NULL,
			"Tasa"						DECIMAL(27,8)	DEFAULT 0 NULL,
			"IdMoneda"					NVARCHAR(3)		DEFAULT '' NULL,			
				---ESPECIFICOS: FACTURACION
			"IdTipoComprobante"			NVARCHAR(6) DEFAULT '' NULL,
			"NombreTipoComprobante"		NVARCHAR(100) DEFAULT '' NULL,
			"FolioComprobante"			BIGINT DEFAULT 0 NULL,
			"MontoOperado"				DECIMAL(27,8)	DEFAULT 0 NULL,
			"Comision"					DECIMAL(27,8)	DEFAULT 0 NULL,
			"Derecho"					DECIMAL(27,8)	DEFAULT 0 NULL,
			"CargoFijo"					DECIMAL(27,8)	DEFAULT 0 NULL,
			"IVA"						DECIMAL(27,8)	DEFAULT 0 NULL,
			"MontoAfecto"				DECIMAL(27,8)	DEFAULT 0 NULL,
			"MontoExento"				DECIMAL(27,8)	DEFAULT 0 NULL,
			"MontoTotal"				DECIMAL(27,8)	DEFAULT 0 NULL
				
	);			

	
SET SCHEMA "BSCL"; GO
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_CIRCULAR1064SVS" CASCADE;GO
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_CIRCULAR1064SVS" AS TABLE
	(
		"IdInterno"					BIGINT
		,"EsCtaPropia"				NVARCHAR(1) DEFAULT '' NULL
		,"Uso"         				NVARCHAR(10)    DEFAULT '' NULL
		,"RutIntermediario"			NVARCHAR(40) DEFAULT '' NULL
		,"DVIntermediario"			NVARCHAR(1) DEFAULT '' NULL
		,"CodigoIntermediario"		NVARCHAR(3) DEFAULT '' NULL
		,"FechaTransaccionSistema"	DATETIME DEFAULT NULL
		,"FechaTransaccion"			NVARCHAR(8) DEFAULT '' NULL
		,"HoraTransaccion"			NVARCHAR(8) DEFAULT '' NULL
		,"Mercado"					NVARCHAR(3) DEFAULT '' NULL
		,"SistemaTransaccion"		NVARCHAR(5) DEFAULT '' NULL
		,"Bolsa"					NVARCHAR(3) DEFAULT '' NULL
		,"Folio"					NVARCHAR(40) DEFAULT '' NULL
		,"Secuencia"				NVARCHAR(3) DEFAULT '' NULL
		,"RutContraparte"			NVARCHAR(40) DEFAULT '' NULL
		,"DVContraparte"			NVARCHAR(1) DEFAULT '' NULL
		,"CodigoContraparte"		NVARCHAR(3) DEFAULT '' NULL
		,"NumeroOrden"				NVARCHAR(40) DEFAULT '' NULL
		,"UltimosDigitosNumeroOrden" NVARCHAR(40) DEFAULT '' NULL
		,"Nemotecnico"				NVARCHAR(100) DEFAULT '' NULL
		,"FechaVencimientoSistema"	DATETIME DEFAULT NULL
		,"FechaVencimiento"			NVARCHAR(8) DEFAULT '' NULL
		,"IdTipoInstrumento"		NVARCHAR(3) DEFAULT '' NULL
		,"HIdTipoInstrumento"		NVARCHAR(3) DEFAULT '' NULL
		,"IdTipoOperacion"			NVARCHAR(6) DEFAULT '' NULL
		,"HIdTipoOperacion"			NVARCHAR(6) DEFAULT '' NULL
		,"TipoOperacion1064"		NVARCHAR(7) DEFAULT '' NULL
		,"Persona"					NVARCHAR(1) DEFAULT '' NULL
		,"Plazo"					NVARCHAR(5) DEFAULT '' NULL
		,"IdCondicionLiquidacion"	NVARCHAR(3) DEFAULT '' NULL
		,"TipoCliente"				NVARCHAR(1) DEFAULT '' NULL
		,"UnidadesTransadasSistema"	DECIMAL(27,8) DEFAULT 0 NULL
		,"UnidadesTransadas"		NVARCHAR(40) DEFAULT '' NULL
		,"PrecioTransaccionSistema"	DECIMAL(27,8)	DEFAULT 0 NULL
		,"PrecioTransaccion"		NVARCHAR(40) DEFAULT '' NULL
		,"TotalTransadoSistema"		DECIMAL(27,8)	DEFAULT 0 NULL
		,"TotalTransado"			NVARCHAR(40) DEFAULT '' NULL
		,"TipoUnidad"				NVARCHAR(3) DEFAULT '' NULL
		,"TipoDolar"				NVARCHAR(4) DEFAULT '' NULL
		,"UnidadReajusteTransaccion"	NVARCHAR(3) DEFAULT '' NULL
		,"NumeroContratoFuturo"		NVARCHAR(40) DEFAULT '' NULL
		,"ValorParSistema"			DECIMAL(27,8)	DEFAULT 0 NULL
		,"ValorPar"					NVARCHAR(40) DEFAULT '' NULL
		,"PorcentajeValorPar"		NVARCHAR(40) DEFAULT '' NULL
		,"TIRSistema"				DECIMAL(27,8)	DEFAULT 0 NULL
		,"TIR"						NVARCHAR(40) DEFAULT '' NULL
		,"RentabilidadSistema"		DECIMAL(27,8)	DEFAULT 0 NULL
		,"Rentabilidad"				NVARCHAR(40) DEFAULT '' NULL
		,"TipoReajustePacto"		NVARCHAR(3) DEFAULT '' NULL
		,"CodObservacion"			NVARCHAR(2) DEFAULT '' NULL
		,"CodModificacion"			NVARCHAR(2) DEFAULT '' NULL
		,"FolioModificacion"		NVARCHAR(12) DEFAULT '' NULL
		,"EspaciosSVS"				NVARCHAR(18) DEFAULT '' NULL
		,"NacionalidadCliente"		NVARCHAR(100) DEFAULT '' NULL
		,"RutCliente"				NVARCHAR(40) DEFAULT '' NULL
		,"DVCliente"				NVARCHAR(1) DEFAULT '' NULL
		,"NombreCliente"			NVARCHAR(100) DEFAULT '' NULL
		,"NumeroFactura"			NVARCHAR(40) DEFAULT '' NULL
		,"RutAutorizado"			NVARCHAR(40) DEFAULT '' NULL
		,"DVAutorizado"			    NVARCHAR(1) DEFAULT '' NULL
		,"NombreAutorizado"			NVARCHAR(100) DEFAULT '' NULL
		,"IdClasificacionTipoInstrumento" NVARCHAR(6) DEFAULT '' NULL
		,"CodigoOperador"			NVARCHAR(100)  DEFAULT '' NULL
		,"IdMoneda"					NVARCHAR(3) DEFAULT '' NULL
	);GO

SET SCHEMA "BSCL"; GO
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_CIRCULAR1064RUEDA" CASCADE;GO
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_CIRCULAR1064RUEDA" AS TABLE
	(
		"IdInterno"					BIGINT
		,"EsCtaPropia"				NVARCHAR(1) DEFAULT '' NULL
		,"Uso"         				NVARCHAR(10)    DEFAULT '' NULL
		,"RutIntermediario"			NVARCHAR(40) DEFAULT '' NULL
		,"CodigoIntermediario"		NVARCHAR(3) DEFAULT '' NULL
		,"RazonSocialIntermediario"	NVARCHAR(100) DEFAULT '' NULL
		,"FechaTransaccionSistema"	DATETIME DEFAULT NULL
		,"FechaTransaccion"			NVARCHAR(8) DEFAULT '' NULL
		,"HoraTransaccion"			NVARCHAR(8) DEFAULT '' NULL
		,"Mercado"					NVARCHAR(3) DEFAULT '' NULL
		,"SistemaTransaccion"		NVARCHAR(5) DEFAULT '' NULL
		,"Bolsa"					NVARCHAR(3) DEFAULT '' NULL
		,"Folio"					NVARCHAR(40) DEFAULT '' NULL
		,"Secuencia"				NVARCHAR(3) DEFAULT '' NULL
		,"RutContraparte"			NVARCHAR(40) DEFAULT '' NULL
		,"DVContraparte"			NVARCHAR(1) DEFAULT '' NULL
		,"CodigoContraparte"		NVARCHAR(3) DEFAULT '' NULL
		,"NumeroOrden"				NVARCHAR(40) DEFAULT '' NULL
		,"UltimosDigitosNumeroOrden" NVARCHAR(40) DEFAULT '' NULL
		,"Nemotecnico"				NVARCHAR(100) DEFAULT '' NULL
		,"FechaVencimientoSistema"	DATETIME DEFAULT NULL
		,"FechaVencimiento"			NVARCHAR(8) DEFAULT '' NULL
		,"IdTipoInstrumento"		NVARCHAR(3) DEFAULT '' NULL
		,"HIdTipoInstrumento"		NVARCHAR(3) DEFAULT '' NULL
		,"IdTipoInstrumentoRueda"	NVARCHAR(3) DEFAULT '' NULL
		,"IdTipoOperacion"			NVARCHAR(6) DEFAULT '' NULL
		,"HIdTipoOperacion"			NVARCHAR(6) DEFAULT '' NULL
		,"Persona"					NVARCHAR(1) DEFAULT '' NULL
		,"Plazo"					NVARCHAR(5) DEFAULT '' NULL
		,"IdCondicionLiquidacion"	NVARCHAR(3) DEFAULT '' NULL
		,"TipoCliente"				NVARCHAR(1) DEFAULT '' NULL
		,"UnidadesTransadasSistema"	DECIMAL(27,8) DEFAULT 0 NULL
		,"UnidadesTransadas"		NVARCHAR(40) DEFAULT '' NULL
		,"PrecioTransaccionSistema"	DECIMAL(27,8)	DEFAULT 0 NULL
		,"PrecioTransaccion"		NVARCHAR(40) DEFAULT '' NULL
		,"TotalTransadoSistema"		DECIMAL(27,8)	DEFAULT 0 NULL
		,"TotalTransado"			NVARCHAR(40) DEFAULT '' NULL
		,"TipoUnidad"				NVARCHAR(3) DEFAULT '' NULL
		,"TipoDolar"				NVARCHAR(4) DEFAULT '' NULL
		,"UnidadReajusteTransaccion"	NVARCHAR(3) DEFAULT '' NULL
		,"NumeroContratoFuturo"		NVARCHAR(40) DEFAULT '' NULL
		,"ValorParSistema"			DECIMAL(27,8)	DEFAULT 0 NULL
		,"ValorPar"					NVARCHAR(40) DEFAULT '' NULL
		,"PorcentajeValorPar"		NVARCHAR(40) DEFAULT '' NULL
		,"TIRSistema"				DECIMAL(27,8)	DEFAULT 0 NULL
		,"TIR"						NVARCHAR(40) DEFAULT '' NULL
		,"RentabilidadSistema"		DECIMAL(27,8)	DEFAULT 0 NULL
		,"Rentabilidad"				NVARCHAR(40) DEFAULT '' NULL
		,"TipoReajustePacto"		NVARCHAR(3) DEFAULT '' NULL
		,"CodObservacion"			NVARCHAR(2) DEFAULT '' NULL
		,"CodModificacion"			NVARCHAR(2) DEFAULT '' NULL
		,"FolioModificacion"		NVARCHAR(12) DEFAULT '' NULL
		,"EspaciosSVS"				NVARCHAR(18) DEFAULT '' NULL
		,"NacionalidadCliente"		NVARCHAR(100) DEFAULT '' NULL
		,"RutCliente"				NVARCHAR(40) DEFAULT '' NULL
		--,"DVCliente"				NVARCHAR(1) DEFAULT '' NULL
		,"CodigoPortafolio" 		NVARCHAR(100) DEFAULT '' NULL
		,"NombreCliente"			NVARCHAR(100) DEFAULT '' NULL
		,"NumeroFactura"			NVARCHAR(40) DEFAULT '' NULL
		,"RutAutorizado"			NVARCHAR(40) DEFAULT '' NULL
		,"DVAutorizado"			    NVARCHAR(1) DEFAULT '' NULL
		,"NombreAutorizado"			NVARCHAR(100) DEFAULT '' NULL
		,"IdClasificacionTipoInstrumento" NVARCHAR(6) DEFAULT '' NULL
		,"CodigoOperador"			NVARCHAR(100)  DEFAULT '' NULL
		,"IdMoneda"					NVARCHAR(3) DEFAULT '' NULL
		,"Rueda"					NVARCHAR(1) DEFAULT '' NULL
		,"TipoOperacionRueda"		NVARCHAR(2) DEFAULT '' NULL
		
	);GO

SET SCHEMA "BSCL"; GO
DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_CIRCULAR1064FUERARUEDA" CASCADE;GO
CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_CIRCULAR1064FUERARUEDA" AS TABLE
	(
		 "IdInterno"					BIGINT
		,"CodigoCorredor"			NVARCHAR(3) DEFAULT '' NULL
		,"FechaInformacion"			NVARCHAR(5) DEFAULT '' NULL
		,"Disponible1"				NVARCHAR(200) DEFAULT '' NULL
		,"EsCtaPropia"				NVARCHAR(1) DEFAULT '' NULL
		,"Uso"         				NVARCHAR(10)    DEFAULT '' NULL
		,"RutIntermediario"			NVARCHAR(40) DEFAULT '' NULL
		,"DVIntermediario"			NVARCHAR(1) DEFAULT '' NULL
		,"CodigoIntermediario"		NVARCHAR(3) DEFAULT '' NULL
		,"FechaTransaccionSistema"	DATETIME DEFAULT NULL
		,"FechaTransaccion"			NVARCHAR(8) DEFAULT '' NULL
		,"HoraTransaccion"			NVARCHAR(8) DEFAULT '' NULL
		,"Mercado"					NVARCHAR(3) DEFAULT '' NULL
		,"SistemaTransaccion"		NVARCHAR(5) DEFAULT '' NULL
		,"Bolsa"					NVARCHAR(3) DEFAULT '' NULL
		,"Folio"					NVARCHAR(40) DEFAULT '' NULL
		,"Secuencia"				NVARCHAR(3) DEFAULT '' NULL
		,"RutContraparte"			NVARCHAR(40) DEFAULT '' NULL
		,"DVContraparte"			NVARCHAR(1) DEFAULT '' NULL
		,"CodigoContraparte"		NVARCHAR(3) DEFAULT '' NULL
		,"NumeroOrden"				NVARCHAR(40) DEFAULT '' NULL
		,"UltimosDigitosNumeroOrden" NVARCHAR(40) DEFAULT '' NULL
		,"Nemotecnico"				NVARCHAR(100) DEFAULT '' NULL
		,"FechaVencimientoSistema"	DATETIME DEFAULT NULL
		,"FechaVencimiento"			NVARCHAR(8) DEFAULT '' NULL
		,"IdTipoInstrumento"		NVARCHAR(3) DEFAULT '' NULL
		,"HIdTipoInstrumento"		NVARCHAR(3) DEFAULT '' NULL
		,"IdTipoOperacion"			NVARCHAR(6) DEFAULT '' NULL
		,"HIdTipoOperacion"			NVARCHAR(6) DEFAULT '' NULL
		,"TipoOperacion1064"		NVARCHAR(7) DEFAULT '' NULL
		,"Persona"					NVARCHAR(1) DEFAULT '' NULL
		,"Plazo"					NVARCHAR(5) DEFAULT '' NULL
		,"PlazoPacto"				NVARCHAR(5) DEFAULT '' NULL
		,"IdCondicionLiquidacion"	NVARCHAR(3) DEFAULT '' NULL
		,"TipoCliente"				NVARCHAR(1) DEFAULT '' NULL
		,"UnidadesTransadasSistema"	DECIMAL(27,8) DEFAULT 0 NULL
		,"UnidadesTransadas"		NVARCHAR(40) DEFAULT '' NULL
		,"PrecioTransaccionSistema"	DECIMAL(27,8)	DEFAULT 0 NULL
		,"PrecioTransaccion"		NVARCHAR(40) DEFAULT '' NULL
		,"TotalTransadoSistema"		DECIMAL(27,8)	DEFAULT 0 NULL
		,"TotalTransado"			NVARCHAR(40) DEFAULT '' NULL
		,"TipoUnidad"				NVARCHAR(3) DEFAULT '' NULL
		,"TipoDolar"				NVARCHAR(4) DEFAULT '' NULL
		,"UnidadReajusteTransaccion"	NVARCHAR(3) DEFAULT '' NULL
		,"NumeroContratoFuturo"		NVARCHAR(40) DEFAULT '' NULL
		,"ValorParSistema"			DECIMAL(27,8)	DEFAULT 0 NULL
		,"ValorPar"					NVARCHAR(40) DEFAULT '' NULL
		,"PorcentajeValorPar"		NVARCHAR(40) DEFAULT '' NULL
		,"TIRSistema"				DECIMAL(27,8)	DEFAULT 0 NULL
		,"TIR"						NVARCHAR(40) DEFAULT '' NULL
		,"RentabilidadSistema"		DECIMAL(27,8)	DEFAULT 0 NULL
		,"Rentabilidad"				NVARCHAR(40) DEFAULT '' NULL
		,"TipoReajustePacto"		NVARCHAR(3) DEFAULT '' NULL
		,"CodObservacion"			NVARCHAR(2) DEFAULT '' NULL
		,"CodModificacion"			NVARCHAR(2) DEFAULT '' NULL
		,"FolioModificacion"		NVARCHAR(12) DEFAULT '' NULL
		,"EspaciosSVS"				NVARCHAR(18) DEFAULT '' NULL
		,"NacionalidadCliente"		NVARCHAR(100) DEFAULT '' NULL
		,"RutCliente"				NVARCHAR(40) DEFAULT '' NULL
		,"DVCliente"				NVARCHAR(1) DEFAULT '' NULL
		,"NombreCliente"			NVARCHAR(100) DEFAULT '' NULL
		,"NumeroFactura"			NVARCHAR(40) DEFAULT '' NULL
		,"RutAutorizado"			NVARCHAR(40) DEFAULT '' NULL
		,"DVAutorizado"			    NVARCHAR(1) DEFAULT '' NULL
		,"NombreAutorizado"			NVARCHAR(100) DEFAULT '' NULL
		,"IdClasificacionTipoInstrumento" NVARCHAR(6) DEFAULT '' NULL
		,"CodigoOperador"			NVARCHAR(100)  DEFAULT '' NULL
		,"IdMoneda"					NVARCHAR(3) DEFAULT '' NULL
		,"Disponible2"				NVARCHAR(200) DEFAULT '' NULL
		,"Disponible3"				NVARCHAR(200) DEFAULT '' NULL
		,"IdEmisor"					NVARCHAR(40)  DEFAULT '' NULL
		,"TipoTransaccion"			NVARCHAR(1)  DEFAULT '' NULL
		,"TipoOperacionRueda"		NVARCHAR(1)  DEFAULT '' NULL
		,"TipoOperacionRueda2"		NVARCHAR(1)  DEFAULT '' NULL
		,"TipoRentabilidad"			NVARCHAR(2)  DEFAULT '' NULL
	);GO

	
/* Commit:                                                      */
/*==============================================================*/
commit






