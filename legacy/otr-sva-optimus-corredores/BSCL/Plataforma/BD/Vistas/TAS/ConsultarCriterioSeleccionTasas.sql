SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarCriterioSeleccionTasas";
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarCriterioSeleccionTasas" AS
SELECT
	 CST."IdCriterioSeleccionTasas"
	,CST."Prioridad"
	,CST."TipoCartera"
	,COALESCE(CNF."Descripcion",'TODOS') AS "DscTipoCartera"
	,CST."IdClasificacionTipoInstrumento"
	,CLA."Nombre" AS "NombreClasTipoInst"
	,CLA."Codigo" || ' - ' || CLA."Nombre" AS "Clasificacion"
	,CST."IdTipoInstrumento"
	,COALESCE(TINS."Nombre",'TODOS') AS "NombreTipoInst"
	,COALESCE(TINS."Codigo" || ' - ' || TINS."Nombre",'TODOS') AS "TipoInstrumento"
	,CST."IdTipoOperacion"
	,COALESCE(TOPE."Nombre",' -') AS "TipoOperacion"
	,CST."IdEmisor"
	,CASE WHEN CST."IdEmisor" = '' THEN 'UI_TODOS' ELSE CST."IdEmisor" END AS "EmisorUI"
	,CST."IdMoneda"
	,CASE WHEN CST."IdMoneda" = '' THEN 'UI_TODOS' ELSE CST."IdMoneda" END AS "MonedaUI"
	,CST."IdInstrumento"
	,CST."Nemotecnico"
	,CASE WHEN CST."Nemotecnico" = '' THEN 'UI_TODOS' ELSE CST."Nemotecnico" END AS "NemotecnicoUI"
	,CST."DiasAntiguedad"
	,CST."IdTipoCriterioTasa"
	,COALESCE(TCT."Descripcion",'TODOS') AS "TipoCriterioTasa"
	,CST."CodigoFuente"
	,FU."IdFuente"
	,FU."Descripcion" AS "DescripcionFuente"
	,FU."Codigo" || ' - ' || FU."Descripcion" AS "CodDescFuente"
	,CST."IdTipoTasa"
	,COALESCE(TTA."Descripcion",' -') AS "TipoTasa"
	,CST."IdTipoPrecio"
	,COALESCE(TPR."Descripcion",' -') AS "TipoPrecio"
	,CST."Interpolar"
	,CASE WHEN CST."Interpolar"='S' THEN 'UI_SI' ELSE 'UI_NO' END AS "InterpolarUI"
	,CST."IdEstado"
	,EST."Descripcion" AS "Estado"
	,CST."Borrado"
	,CST."IdEmpresa"
	,CST."IdUsuarioModificacion"
	,CST."FechaCreacion"
	,CST."FechaModificacion"
	,CST."IdUso"
	,USO."Descripcion" AS "Uso"
	,CST."IdContraparte" AS "IdContraparte"
	,IFNULL(CED."NombreEntidad",'') AS "NombreContraparte"
FROM "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas" CST
LEFT JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarConfiguracionTasas" CNF
	ON (CST."TipoCartera" = CNF."Valor" AND CNF."NombreEntidad" = 'TIPOCARTERATASAS' AND CST."IdEmpresa" = CNF."IdEmpresa")
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLA
	ON (CST."IdClasificacionTipoInstrumento" = CLA."IdClasificacionTipoInstrumento" AND CLA."IdEstado" = 'ACT')
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TINS
	ON (CST."IdTipoInstrumento" = TINS."IdTipoInstrumento" AND TINS."IdEstado" = 'VIG')
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TOPE
	ON (CST."IdTipoOperacion" = TOPE."IdTipoOperacion")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoCriterioTasa" TCT
	ON (CST."IdTipoCriterioTasa" = TCT."IdTipoCriterioTasa" AND CST."IdEmpresa" = TCT."IdEmpresa")
INNER JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente" FU
	ON (CST."CodigoFuente" = FU."Codigo" AND CST."IdEmpresa" = FU."IdEmpresa")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoTasa" TTA
	ON (CST."IdTipoTasa" = TTA."IdTipoTasa" AND CST."IdEmpresa" = TTA."IdEmpresa" AND TTA."IdEstado" = 'ACT')
LEFT JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoPrecio" TPR
	ON (CST."IdTipoPrecio" = TPR."IdTipoPrecio" AND CST."IdEmpresa" = TPR."IdEmpresa" AND TPR."IdEstado" = 'ACT')
INNER JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarEstadoTasa" EST
	ON (CST."IdEstado" = EST."IdEstado" AND EST."NombreEntidad" = 'MANTENEDORCRITERIOS')
LEFT JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarUso" USO
	ON (CST."IdUso" = USO."IdUso" AND CST."IdEmpresa" = USO."IdEmpresa" AND USO."IdEstado" = 'ACT')
LEFT JOIN "BSCL.Custodia.BD.Vistas::GTA.ConsultarEntidadDestinoGarantia" CED
	ON (CST."IdContraparte" = CED."IdContraparte")
ORDER BY CST."IdUso" ASC, CST."IdClasificacionTipoInstrumento" ASC, CST."Prioridad" ASC;
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TAS.ConsultarCriterioSeleccionTasas" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO