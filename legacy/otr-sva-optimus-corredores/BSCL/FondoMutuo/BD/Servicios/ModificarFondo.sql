SET SCHEMA "BSCL"; 
GO
DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ModificarFondo";
GO
CREATE PROCEDURE
--  Proposito: Crear Serie de Fondo
--  Autor: - Gat-Blac 
--  Fecha Creacion: 24/06/2016
"BSCL.FondoMutuo.BD.Servicios::ModificarFondo" (
	 --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 				BIGINT,
    
    IN p_Fondo								TABLE	(
	    										"IdInterno" 					BIGINT,
		    									"IdFondo"						NVARCHAR(40),
		    									"IdEmisor"						NVARCHAR(40),
		    									"Nemotecnico"					NVARCHAR(40),
		    									"NombreFondo"					NVARCHAR(100),
		    									"IdTipoFondo"					NVARCHAR(40),
		    									"IdPaisOrigen"					BIGINT,
		    									"CodigoSVS"						NVARCHAR(40),
		    									"MoneyMarket"   				NVARCHAR(1),
		    									"Vigencia"						NVARCHAR(1),
		    									"FondoExtranjero"				NVARCHAR(1),
		    									"IdMoneda"						NVARCHAR(3),
		    									"IdTipoInstrumento"				NVARCHAR(3),
		    									"IdCategoriaPerfilFondoMutuo"	NVARCHAR(40),
		    									"AperturaAutomaticaFondo"	  	NVARCHAR(1),
		    									"FondoNormativo"				NVARCHAR(1),
		    									"IdPaisEmisor"                  BIGINT,
                                                "IdTipoDocumentoIdentidadFondo" NVARCHAR(3),
                                                "DocumentoIdentidadFondo"       NVARCHAR(40),
                                                "IdMercadoFondoMutuo"		  	NVARCHAR(40)
    											),
     OUT p_out_Resultado					TABLE	(
		 										"IdInterno"						BIGINT,
		 										"IdFondo"						NVARCHAR(40),
		 										"IdEmisor"						NVARCHAR(40),
		 										"Nemotecnico"					NVARCHAR(40),
		    									"NombreFondo"					NVARCHAR(100),
		    									"IdTipoFondo"					NVARCHAR(40),
		    									"IdPaisOrigen"					BIGINT,
		    									"CodigoSVS"						NVARCHAR(40),
		    									"MoneyMarket"   				NVARCHAR(1),
		    									"FechaCreacion"					DATE,
		    									"FechaIngresoRegistro"			TIMESTAMP,
		    									"FechaConstitucion" 			DATE,
		    									"Vigencia"						NVARCHAR(1),
		    									"FondoExtranjero"				NVARCHAR(1),
		    									"IdMoneda"						NVARCHAR(3),
		    									"IdTipoInstrumento"				NVARCHAR(3),
		    									"IdEmpresa"						BIGINT,
		    									"IdCategoriaPerfilFondoMutuo" 	NVARCHAR(40),
		    									"AperturaAutomaticaFondo"	  	NVARCHAR(1),
		    									"FondoNormativo"				NVARCHAR(1),
		    									"IdPaisEmisor"                  BIGINT,
                                                "IdTipoDocumentoIdentidadFondo" NVARCHAR(3),
                                                "DocumentoIdentidadFondo"       NVARCHAR(40),
                                                "IdMercadoFondoMutuo"		  	NVARCHAR(40)
		 										)
	)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS 
BEGIN

	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	BEGIN
	DECLARE v_FechaSistema 			NVARCHAR(10);
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
			
	v_fondos_finales = SELECT 
							"FondoMaestro"."IdInterno",
							"FondoMaestro"."IdFondo",
							"FondoMaestro"."IdEmisor",
							"FondoMaestro"."Nemotecnico",
							"FondoMaestro"."NombreFondo",
							"FondoMaestro"."IdTipoFondo",
							"FondoMaestro"."IdPaisOrigen",
							"FondoMaestro"."CodigoSVS",
							"FondoMaestro"."MoneyMarket",
							"FondoMaestro"."Vigencia",
							"FondoMaestro"."FondoExtranjero",
							"FondoMaestro"."IdMoneda",
							"FondoMaestro"."IdTipoInstrumento",
							:p_IdEmpresa AS "IdEmpresa",
							"FondoMaestro"."IdCategoriaPerfilFondoMutuo",
							"FondoMaestro"."AperturaAutomaticaFondo",
							"FondoMaestro"."FondoNormativo",
							"FondoMaestro"."IdPaisEmisor",
                            "FondoMaestro"."IdTipoDocumentoIdentidadFondo",
                            "FondoMaestro"."DocumentoIdentidadFondo",
                            "FondoMaestro"."IdMercadoFondoMutuo"
						FROM :p_Fondo AS "FondoMaestro";
	
	UPDATE Fondo
	SET "IdEmisor" 						= Fondos_finales."IdEmisor",
		"Nemotecnico" 					= Fondos_finales."Nemotecnico",
		"NombreFondo" 					= Fondos_finales."NombreFondo",
		"IdTipoFondo" 					= Fondos_finales."IdTipoFondo",
		"IdPaisOrigen" 					= Fondos_finales."IdPaisOrigen",
		"CodigoSVS" 					= Fondos_finales."CodigoSVS",
		"MoneyMarket" 					= Fondos_finales."MoneyMarket",
		"Vigencia" 						= Fondos_finales."Vigencia",
		"FondoExtranjero" 				= Fondos_finales."FondoExtranjero",
		"IdMoneda" 						= Fondos_finales."IdMoneda",
		"IdTipoInstrumento" 			= Fondos_finales."IdTipoInstrumento",
		"IdCategoriaPerfilFondoMutuo" 	= Fondos_finales."IdCategoriaPerfilFondoMutuo",
		"AperturaAutomaticaFondo"		= Fondos_finales."AperturaAutomaticaFondo",
		"FondoNormativo"				= Fondos_finales."FondoNormativo",
		"IdPaisEmisor"                  = Fondos_finales."IdPaisEmisor",
		"IdTipoDocumentoIdentidadFondo" = Fondos_finales."IdTipoDocumentoIdentidadFondo",
		"DocumentoIdentidadFondo"       = Fondos_finales."DocumentoIdentidadFondo",
		"IdMercadoFondoMutuo"			= Fondos_finales."IdMercadoFondoMutuo"
	FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo
	INNER JOIN :v_fondos_finales AS Fondos_finales ON (Fondo."IdFondo" = Fondos_finales."IdFondo")
	WHERE Fondo."IdEmpresa" = Fondos_finales."IdEmpresa";
	
	p_out_Resultado = SELECT 	
								"Fondo"."IdInterno",
								"Fondo"."IdFondo",
								"Fondo"."IdEmisor", 
								"Fondo"."Nemotecnico", 
								"Fondo"."NombreFondo", 
								"Fondo"."IdTipoFondo", 
								"Fondo"."IdPaisOrigen", 
								"Fondo"."CodigoSVS", 
								"Fondo"."MoneyMarket", 
								"FondoMaestro"."FechaCreacion",
								"FondoMaestro"."FechaIngresoRegistro",
								"FondoMaestro"."FechaConstitucion",
								"Fondo"."Vigencia", 
								"Fondo"."FondoExtranjero",
								"Fondo"."IdMoneda",
								"Fondo"."IdTipoInstrumento",
								"Fondo"."IdEmpresa",
								"Fondo"."IdCategoriaPerfilFondoMutuo",
								"Fondo"."AperturaAutomaticaFondo",
								"Fondo"."FondoNormativo",
								"Fondo"."IdPaisEmisor",
                                "Fondo"."IdTipoDocumentoIdentidadFondo",
                                "Fondo"."DocumentoIdentidadFondo",
                                "Fondo"."IdMercadoFondoMutuo"
						FROM :v_fondos_finales AS "Fondo"
						INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "FondoMaestro" ON ("Fondo"."IdFondo" = "FondoMaestro"."IdFondo");
							
	END;
	
END; 
GO
