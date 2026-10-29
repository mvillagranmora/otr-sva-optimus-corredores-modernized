SELECT 
	CM."IdCargaMasiva"
	, CM."NombreCargaArchivo"
	, CM."CantidadRegistros"
	, CM."IdUsuario"
	, CM."IdTipoCarga"
	, CM."Descripcion"
	, TO_VARCHAR(CM."FechaCarga", 'YYYY-MM-DD') AS "FechaCarga"
	, CM."IdEstado"
FROM "BSCL.APV.BD::APV.CargaMasiva" AS CM
WHERE CM."NombreCargaArchivo" = :NombreCargaArchivo AND CM."Descripcion" = 'Carga Previred';