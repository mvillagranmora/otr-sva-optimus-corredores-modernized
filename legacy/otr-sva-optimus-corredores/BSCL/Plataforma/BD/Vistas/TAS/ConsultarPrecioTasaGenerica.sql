SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasaGenerica";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasaGenerica" AS
	SELECT	TAS."IdPrecioTasaTipoInstrumento", 
			INS."IdClasificacionTipoInstrumento",
			CLS."Nombre" AS "NombreClasificacion",
			TAS."IdTipoInstrumento", 
			INS."IdTipoInstrumento" || ' - ' || INS."Nombre" AS "NombreInstrumento",
			TAS."IdTipoReajuste",
			TAS."Tasa", 
			TAS."Precio", 
			TAS."PlazoDesde", 
			TAS."PlazoHasta", 
			TAS."FechaModificacion", 
			TAS."IdUsuarioModificacion", 
			TAS."IdEstado"
	FROM 	"BSCL.Plataforma.BD::TAS.PrecioTasaTipoInstrumento" TAS
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" INS 
					  ON TAS."IdTipoInstrumento" = INS."IdTipoInstrumento"
			LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLS
					  ON INS."IdClasificacionTipoInstrumento" = CLS."IdClasificacionTipoInstrumento";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasaGenerica" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
