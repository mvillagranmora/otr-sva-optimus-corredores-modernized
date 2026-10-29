SET SCHEMA "BSCL"; 
GO
DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CrearFondo";
GO
CREATE PROCEDURE
--  Proposito: Crear Fondo
--  Autor: - Gat-Blac 
--  Fecha Creacion: 24/06/2016
"BSCL.FondoMutuo.BD.Servicios::CrearFondo" (
	 --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 						BIGINT,    
	IN p_Fondo							TABLE	(
		    									"IdInterno" 				  BIGINT,
		    									"IdFondo"					  NVARCHAR(40),
		    									"IdEmisor"					  NVARCHAR(40),
		    									"Nemotecnico"				  NVARCHAR(40),
		    									"NombreFondo"				  NVARCHAR(100),
		    									"IdTipoFondo"				  NVARCHAR(40),
		    									"IdPaisOrigen"				  BIGINT,
		    									"CodigoSVS"					  NVARCHAR(40),
		    									"MoneyMarket"   			  NVARCHAR(1),
		    									"Vigencia"					  NVARCHAR(1),
		    									"FondoExtranjero"			  NVARCHAR(1),
		    									"IdMoneda"					  NVARCHAR(3),
		    									"IdTipoInstrumento"			  NVARCHAR(3),
		    									"IdCategoriaPerfilFondoMutuo" NVARCHAR(40),
		    									"AperturaAutomaticaFondo"	  NVARCHAR(1),
		    									"FondoNormativo"			  NVARCHAR(1),
		    									"IdPaisEmisor"                BIGINT,
		    									"IdTipoDocumentoIdentidadFondo" NVARCHAR(3),
		    									"DocumentoIdentidadFondo"     NVARCHAR(40),
		    									"IdMercadoFondoMutuo"		  NVARCHAR(40)
		    							   	),
	OUT p_out_Resultado					TABLE	(
		 										"IdInterno"					BIGINT,
		 										"IdFondo"					NVARCHAR(40),
		 										"IdEmisor"					NVARCHAR(40),
		 										"Nemotecnico"				NVARCHAR(40),
		    									"NombreFondo"				NVARCHAR(100),
		    									"IdTipoFondo"				NVARCHAR(40),
		    									"IdPaisOrigen"				BIGINT,
		    									"CodigoSVS"					NVARCHAR(40),
		    									"MoneyMarket"   			NVARCHAR(1),
		    									"FechaCreacion"				DATE,
		    									"FechaIngresoRegistro"		TIMESTAMP,
		    									"FechaConstitucion" 		DATE,
		    									"Vigencia"					NVARCHAR(1),
		    									"FondoExtranjero"			NVARCHAR(1),
		    									"IdMoneda"					NVARCHAR(3),
		    									"IdTipoInstrumento"			NVARCHAR(3),
		    									"IdEmpresa"					BIGINT,
		    									"IdCategoriaPerfilFondoMutuo" NVARCHAR(40),
		    									"AperturaAutomaticaFondo"	  NVARCHAR(1),
		    									"FondoNormativo"			  NVARCHAR(1),
		    									"IdPaisEmisor"                BIGINT,
                                                "IdTipoDocumentoIdentidadFondo" NVARCHAR(3),
                                                "DocumentoIdentidadFondo"     NVARCHAR(40),
                                                "IdMercadoFondoMutuo"		  NVARCHAR(40)
		 									)
	)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS 
BEGIN
	BEGIN
	DECLARE v_FechaSistema 			NVARCHAR(10);
	DECLARE v_resultadoRegla 		NVARCHAR(40);
	DECLARE v_count_idfondos		BIGINT;
	DECLARE v_out_error				NVARCHAR(500);
				
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
	
	v_fondos_maestro = SELECT 
							"Fondo"."IdInterno",
							"Fondo"."IdFondo",
							"Fondo"."IdEmisor",
							"Fondo"."Nemotecnico",
							"Fondo"."NombreFondo",
							"Fondo"."IdTipoFondo",
							"Fondo"."IdPaisOrigen",
							"Fondo"."CodigoSVS",
							"Fondo"."MoneyMarket",
							:v_FechaSistema AS "FechaCreacion",
							:v_FechaSistema AS "FechaConstitucion",
							CURRENT_UTCTIMESTAMP AS "FechaIngresoRegistro",
							"Fondo"."Vigencia",
							"Fondo"."FondoExtranjero",
							"Fondo"."IdMoneda",
							"Fondo"."IdTipoInstrumento",
							:p_IdEmpresa AS "IdEmpresa",
							"Fondo"."IdCategoriaPerfilFondoMutuo",
							"Fondo"."AperturaAutomaticaFondo",
							"Fondo"."FondoNormativo",
							:v_FechaSistema AS "FechaFondo",
							"Fondo"."IdPaisEmisor" AS "IdPaisEmisor",
                            "Fondo"."IdTipoDocumentoIdentidadFondo" AS "IdTipoDocumentoIdentidadFondo",
                            "Fondo"."DocumentoIdentidadFondo" AS "DocumentoIdentidadFondo",
                            "Fondo"."IdMercadoFondoMutuo"
						FROM :p_Fondo AS "Fondo";
						
						
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
							"FondoMaestro"."FechaCreacion",
							"FondoMaestro"."FechaIngresoRegistro",
							"FondoMaestro"."FechaConstitucion",
							"FondoMaestro"."Vigencia",
							"FondoMaestro"."FondoExtranjero",
							"FondoMaestro"."IdMoneda",
							"FondoMaestro"."IdTipoInstrumento",
							"FondoMaestro"."IdEmpresa",
							"FondoMaestro"."IdCategoriaPerfilFondoMutuo",
							"FondoMaestro"."AperturaAutomaticaFondo",
							"FondoMaestro"."FondoNormativo",
							"FondoMaestro"."FechaFondo",
							"FondoMaestro"."IdPaisEmisor" AS "IdPaisEmisor",
                            "FondoMaestro"."IdTipoDocumentoIdentidadFondo" AS "IdTipoDocumentoIdentidadFondo",
                            "FondoMaestro"."DocumentoIdentidadFondo" AS "DocumentoIdentidadFondo",
                            "FondoMaestro"."IdMercadoFondoMutuo"
						FROM :v_fondos_maestro AS "FondoMaestro";
						
	
	INSERT INTO "BSCL.FondoMutuo.BD::FMM.Fondo" (
			"IdFondo", "IdEmisor", "Nemotecnico", "NombreFondo", "IdTipoFondo", "IdPaisOrigen", "CodigoSVS", "MoneyMarket", 
			"FechaCreacion", "FechaIngresoRegistro", "FechaConstitucion", "Vigencia", "FondoExtranjero", "IdMoneda", 
			"IdTipoInstrumento", "IdEmpresa", "IdCategoriaPerfilFondoMutuo", "AperturaAutomaticaFondo","FondoNormativo","FechaFondo",
			"IdPaisEmisor","IdTipoDocumentoIdentidadFondo","DocumentoIdentidadFondo","IdMercadoFondoMutuo")
	SELECT "IdFondo", "IdEmisor", "Nemotecnico", "NombreFondo", "IdTipoFondo", "IdPaisOrigen", "CodigoSVS", "MoneyMarket", 
			"FechaCreacion", "FechaIngresoRegistro", "FechaConstitucion", "Vigencia", "FondoExtranjero", "IdMoneda", 
			"IdTipoInstrumento", "IdEmpresa", "IdCategoriaPerfilFondoMutuo", "AperturaAutomaticaFondo","FondoNormativo","FechaFondo",
            "IdPaisEmisor","IdTipoDocumentoIdentidadFondo","DocumentoIdentidadFondo","IdMercadoFondoMutuo"
	FROM :v_fondos_finales;
	
	p_out_Resultado = SELECT 	
								"IdInterno",
								"IdFondo",
								"IdEmisor", 
								"Nemotecnico", 
								"NombreFondo", 
								"IdTipoFondo", 
								"IdPaisOrigen", 
								"CodigoSVS",
								"MoneyMarket",
								"FechaCreacion", 
								"FechaIngresoRegistro", 
								"FechaConstitucion",
								"Vigencia", 
								"FondoExtranjero",
		    					"IdMoneda",
		    					"IdTipoInstrumento",
								"IdEmpresa",
								"IdCategoriaPerfilFondoMutuo",
								"AperturaAutomaticaFondo",
								"FondoNormativo",
								"IdPaisEmisor",
								"IdTipoDocumentoIdentidadFondo",
								"DocumentoIdentidadFondo",
								"IdMercadoFondoMutuo"
						FROM :v_fondos_finales;		
	END;
	
END;
GO
