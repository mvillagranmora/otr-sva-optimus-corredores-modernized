SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTasa";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTasa" AS
SELECT
	 TAS."IdTasa"
	,TAS."IdFuente"
	,FU."Codigo" AS "CodigoFuente"
	,FU."Descripcion" AS "DescripcionFuente"
	,FU."Codigo" || ' - ' || FU."Descripcion" AS "CodDescFuente"
	,TAS."IdInstrumento"
	,TAS."Nemotecnico"
	,CLA."IdClasificacionTipoInstrumento"
	,COALESCE(CLA."Codigo",'') || ' - ' || COALESCE(CLA."Nombre",'') AS "Clasificacion"
	--,INS."IdTipoInstrumento"
	--,INS."IdTipoOperacion"
	--,INS."IdTipoReajuste"
	--,INS."IdMonedaInstrumento" AS "IdMoneda"
	--,COALESCE(EMI."DocumentoIdentidad",'') || ' - ' || COALESCE(EMI."IdEmisor",'') AS "Emisor"
	,TAS."Tasa"
	,TAS."TasaEstimada"
	,TAS."IdEstado"
	,EST."Descripcion" AS "Estado"
	,TAS."IdEmpresa"
	,TAS."IdUsuarioCreador"
	,TAS."FechaArchivo"
	,TAS."FechaIngreso"
	,TAS."FechaCreacion"
	,TAS."FechaModificacion"
	,TAS."RutaArchivo"
	,TAS."NombreArchivo"
	,TAS."Borrado"
FROM "BSCL.Plataforma.BD::TAS.Tasa" TAS
INNER JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente" FU
	ON (TAS."IdFuente" = FU."IdFuente" AND TAS."IdEmpresa" = FU."IdEmpresa")
--LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS
	--ON (TAS."IdInstrumento" = INS."IdInstrumento" AND INS."IdEstado" = 'ACT')
LEFT JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarInstrumentoCustodiaParaTasas" INS
	ON (TAS."IdInstrumento" = INS."IdInstrumento" AND TAS."Nemotecnico" = INS."Nemotecnico")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionTipoInstrumento" CLA
	ON (INS."IdClasificacionTipoInstrumento" = CLA."IdClasificacionTipoInstrumento" AND 
		INS."IdTipoInstrumento" = CLA."IdTipoInstrumento" AND
		CLA."IdEstado" = 'ACT')
--LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMI
	--ON (INS."IdEmisor" = EMI."IdEmisor")
INNER JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarEstadoTasa" EST
	ON (TAS."IdEstado" = EST."IdEstado")
ORDER BY TAS."IdTasa" ASC;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTasa" TO BCS_DESARROLLADOR WITH GRANT OPTION;
