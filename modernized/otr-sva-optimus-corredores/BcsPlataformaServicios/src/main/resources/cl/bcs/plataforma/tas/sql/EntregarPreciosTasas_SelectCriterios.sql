
SELECT
	CST."IdCriterioSeleccionTasas" AS "IdCriterioSeleccionTasas"	
	,CST."Prioridad"
	,CST."TipoCartera"
	,CST."IdUso"
	,CST."IdTipoCriterioTasa"
	,CST."IdClasificacionTipoInstrumento"
	,CST."IdTipoInstrumento"
	,CST."DiasAntiguedad"   
	,CST."CodigoFuente"
	,CST."IdTipoTasa"
	,CST."IdTipoPrecio"
	,CST."Interpolar"
	,CST."Nemotecnico"
	,CST."IdEmpresa"

FROM "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas" AS CST
		
WHERE    CST."IdEmpresa"  = 1
AND      CST."Borrado"	  = 'N'
AND 	"IdUso" in (:idUsos)
AND 	"IdClasificacionTipoInstrumento" in (:idsClasificacionTipoInstrumento)


ORDER BY CST."Prioridad" ASC;
