SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoOpciones"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoOpciones" AS 

	SELECT 	"Instrumento"."IdInstrumento",
			"Instrumento"."Nemotecnico",
			"Instrumento"."ISIN",
			"Instrumento"."CorteMinimo",
			"Instrumento"."EstaInscrito",
			"Instrumento"."EsDivisible",
			"Instrumento"."RazonSocial",
			--"Instrumento"."Lista",
			"Instrumento"."FechaEmision",
			"Instrumento"."FechaVencimiento",
			"Instrumento"."IdInstrumentoRelacionado",
			"Instrumento"."IdEmisor",
			"Instrumento"."IdTipoReajuste",
			"Instrumento"."Reajustable",
			"Instrumento"."IdMoneda",
			"Instrumento"."IdEstado",
			"Instrumento"."DecimalesPrecio",
			"GrupoTipoInstrumento"."IdGrupoProducto",
			"TipoInstrumento"."IdTipoInstrumento" AS "IdTipoInstrumento",
			"TipoInstrumento"."Codigo" AS "CodigoTipoInstrumento",
			"TipoInstrumento"."Nombre" AS "NombreTipoInstrumento",
			"ClasificacionTipoInstrumento"."IdClasificacionTipoInstrumento",
			"ClasificacionTipoInstrumento"."Codigo"  AS "CodigoClasificacionTipoInstrumento",
			"ClasificacionTipoInstrumento"."Nombre" AS "NombreClasificacionTipoInstrumento",
			"ConsultarEmisor"."DocumentoIdentidad" AS "DocumentoIdentidadEmisor", --RutEmisor
			"ConsultarEmisor"."NombreCompleto" as "NombreCompletoEmisor", --RazonSocialEmisor
			"TipoOperacionTipoInstrumento"."IdTipoOperacion",
			"TipoOperacion"."Accion",
			"TipoOperacion"."Nombre" as "NombreTipoOperacion",
			"Instrumento"."Tamano",
			--"Instrumento"."IdEntidadClasificadoraRiesgo",
			--"Instrumento"."ValorClasificacionRiesgo",
			"TipoClasificacionRiesgo"."IdTipoClasificacionRiesgo",
			"Instrumento"."TipoValorizacion",
			"Instrumento"."Tasa",
			"Instrumento"."Precio",
			"Instrumento"."ValorizaPor",
			"Instrumento"."TieneCupon",
			"Instrumento"."NumeroCupones",
			"Instrumento"."IndicadorBolson",
			--* [OPCIONES] ---------------------------------------
			LEFT("OPC"."Tipo",1) 	AS "IdTipoOpcion",
			"OPC"."Tipo"				AS "TipoOpcion" ,
			COALESCE("OPC"."Estilo", '') AS "IdEstilo",
			CASE COALESCE("OPC"."Estilo", '') 
				 WHEN 'A' THEN 'AMERICANA'
				 WHEN 'E' THEN 'EUROPEA'
				 ELSE '' END 
			AS "EstiloOpcion" ,
			COALESCE("OPC"."Modo", '') AS "IdModoOpcion",
			CASE COALESCE("OPC"."Modo", '') 
				 WHEN 'F' THEN 'ENTREGA FISICA'
				 WHEN 'C' THEN 'FINANCIERA'
				 ELSE '' END 
			AS "ModoOpcion" ,
			"Instrumento"."Tamano" AS "LotePadron" ,
			"OPC"."CodigoClasificacion" AS "CodigoClasificacionDV",
			COALESCE("Instrumento"."ModalidadEjercicio", '') AS "IdModalidadEjercicio",
		CASE COALESCE("Instrumento"."ModalidadEjercicio", '')
				 WHEN 'A' THEN 'AUTOMATICA'
				 WHEN 'M' THEN 'MANUAL' 
				 ELSE '' END
			AS "ModalidadEjercicio",
			"PrecioStrike"
			
	FROM  	"BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento" 
			LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS "ConsultarEmisor" 
			ON "ConsultarEmisor"."IdEmisor" = "Instrumento"."IdEmisor"
			--* -------------------------------------------------------------------------------------------------------------------
			LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento" 
			ON "TipoInstrumento"."IdTipoInstrumento" = "Instrumento"."IdTipoInstrumento"
			--* -------------------------------------------------------------------------------------------------------------------
			LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS "ClasificacionTipoInstrumento" 
			ON "ClasificacionTipoInstrumento"."IdClasificacionTipoInstrumento" = "TipoInstrumento"."IdClasificacionTipoInstrumento"
			--* -------------------------------------------------------------------------------------------------------------------
			LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS "TipoOperacionTipoInstrumento" 
			ON "Instrumento"."IdInstrumento" = "TipoOperacionTipoInstrumento"."IdInstrumento"
			--* -------------------------------------------------------------------------------------------------------------------
			LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion" 
			ON "TipoOperacionTipoInstrumento"."IdTipoOperacion" = "TipoOperacion"."IdTipoOperacion"
			--* -------------------------------------------------------------------------------------------------------------------
			LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo"  AS "TipoClasificacionRiesgo" 
			ON "TipoClasificacionRiesgo"."IdClasificacionTipoInstrumento" = "ClasificacionTipoInstrumento"."Codigo"
			--* -------------------------------------------------------------------------------------------------------------------			
			LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarSubyacenteOpciones"  AS "OPC" 
			ON "Instrumento"."IdInstrumento" = "OPC"."IdInstrumento"
			LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento" AS "GrupoTipoInstrumento"
			ON "GrupoTipoInstrumento"."IdTipoInstrumento" = "TipoInstrumento"."IdTipoInstrumento"

			

	WITH READ ONLY;
	
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoOpciones" TO BCS_DESARROLLADOR WITH GRANT OPTION;
