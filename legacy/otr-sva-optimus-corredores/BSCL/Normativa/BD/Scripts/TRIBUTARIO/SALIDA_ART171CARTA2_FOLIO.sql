SET SCHEMA "BSCL";

DROP TYPE "BSCL.Normativa.BD::NOR.SALIDA_ART171CARTA2" CASCADE;

CREATE TYPE "BSCL.Normativa.BD::NOR.SALIDA_ART171CARTA2" AS TABLE  
									   ("Folio"								NVARCHAR(100), 
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
										 "DiaConsulta"						NVARCHAR(30),
                                         "LogoCorredora"                    BLOB,
										 "FirmaCorredora"                   BLOB,
                                         "rut_representante"                NVARCHAR(40),
                                         "nombre_representante"             NVARCHAR(100)
									    );


commit;