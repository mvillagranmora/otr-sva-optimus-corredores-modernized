SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa" AS
SELECT
	 PTA."IdPrecioTasa"
	,PTA."IdFuente"
	,COALESCE(FU."Codigo",' -') AS "CodigoFuente"
	,COALESCE(FU."Descripcion",' -') AS "DescripcionFuente"
	,COALESCE(FU."Codigo",'') || ' - ' || COALESCE(FU."Descripcion",'') AS "CodDescFuente"
	,PTA."IdTipoInstrumento"
	,PTA."IdInstrumento"
	,PTA."Nemotecnico"
	,PTA."IdClasificacionTipoInstrumento"
	,COALESCE(CLA."Codigo",'') || ' - ' || COALESCE(CLA."Nombre",'') AS "Clasificacion"
	,PTA."IdMoneda"
	,PTA."IdMoneda" AS "MonedaFormato"
	,COALESCE(EMI."DocumentoIdentidad",'') || ' - ' || COALESCE(EMI."IdEmisor",'') AS "Emisor"
	,PTA."IdTipoTasa"
	,TTA."Descripcion" AS "TipoTasa"
	,PTA."Tasa"
	,PTA."TasaEstimada"
	,PTA."IdTipoPrecio"
	,TPR."Descripcion" AS "TipoPrecio"
	,PTA."Precio"
	,PTA."Plazo"
	,PTA."IdEstado"
	,EST."Descripcion" AS "Estado"
	,PTA."IdEmpresa"
	,PTA."IdUsuarioCreador"
	,PTA."IdUsuarioModificacion"
	,PTA."FechaSolicitud"
	,PTA."FechaIngreso"
	,PTA."FechaCreacion"
	,PTA."FechaModificacion"
	,PTA."FechaPublicacion"
	,PTA."RutaArchivo"
	,PTA."NombreArchivo"
	,PTA."Borrado"
	,PTA."TipoRegistro"
	,PTA."FechaOficializacion"
	,PTA."IdUsuarioOficializador"
	,TO_CHAR(PTA."FechaSolicitud", 'YYYY-MM-DD') AS "FechaSolicitudString"
FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" PTA
LEFT JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente" FU
	ON (PTA."IdFuente" = FU."IdFuente" AND PTA."IdEmpresa" = FU."IdEmpresa")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS
	ON (PTA."IdInstrumento" = INS."IdInstrumento" AND INS."IdEstado" = 'ACT')
LEFT JOIN (SELECT DISTINCT("IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento", "Codigo", "Nombre", "IdEstado"
		   FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionTipoInstrumento") CLA
	ON (PTA."IdClasificacionTipoInstrumento" = CLA."IdClasificacionTipoInstrumento" AND 
		CLA."IdEstado" = 'ACT')
LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMI
	ON (INS."IdEmisor" = EMI."IdEmisor")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoTasa" TTA
	ON (PTA."IdTipoTasa" = TTA."IdTipoTasa" AND PTA."IdEmpresa" = TTA."IdEmpresa" AND TTA."IdEstado" = 'ACT')
LEFT JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoPrecio" TPR
	ON (PTA."IdTipoPrecio" = TPR."IdTipoPrecio" AND PTA."IdEmpresa" = TPR."IdEmpresa" AND TPR."IdEstado" = 'ACT')
INNER JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarEstadoTasa" EST
	ON (PTA."IdEstado" = EST."IdEstado")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MON
	ON (PTA."IdMoneda" = MON."IdMoneda" AND MON."IdEstado" = 'ACT')
ORDER BY
	 --PTA."IdPrecioTasa" ASC,
	 PTA."IdFuente" ASC
	,PTA."IdClasificacionTipoInstrumento" ASC
	,PTA."Nemotecnico" ASC
	,PTA."IdMoneda" ASC
	,PTA."TipoRegistro" ASC
	,PTA."IdTipoTasa" ASC
	,PTA."IdTipoPrecio" ASC
	,PTA."Plazo" ASC
;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa" TO BCS_DESARROLLADOR WITH GRANT OPTION;
