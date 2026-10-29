SELECT
	"IdCuadraturaCircular1518" 										AS "IdCuadraturaCircular"
	,"Nemotecnico"
	,"FechaCuadratura"
	,COALESCE("CantidadDCVCuentaRelacionado", 0)					AS "CantidadDCVCuentaRelacionado"
	,COALESCE("CantidadDisponibleDCVCuentaRelacionado", 0)			AS "CantidadDisponibleDCVCuentaRelacionado"
	,COALESCE("CantidadDCVCuentaPropia", 0)							AS "CantidadDCVCuentaPropia"
	,COALESCE("CantidadDisponibleDCVCuentaPropia", 0)				AS "CantidadDisponibleDCVCuentaPropia"
	,COALESCE("CantidadDCVCuentaTercero", 0)						AS "CantidadDCVCuentaTercero"
	,COALESCE("CantidadDisponibleDCVCuentaTercero", 0)				AS "CantidadDisponibleDCVCuentaTercero"
	,COALESCE("CantidadSorteoLetras", 0) 							AS "CantidadSorteoLetras"
FROM "BSCL.Custodia.BD::CUS.CuadraturaCircular1518" 
WHERE "Nemotecnico" IN (:Nemotecnico) AND "FechaCuadratura" = :FechaCuadratura;