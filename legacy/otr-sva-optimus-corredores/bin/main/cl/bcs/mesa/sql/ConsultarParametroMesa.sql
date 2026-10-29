SELECT
	"IdEmpresa",
	"IdParametro",
	"NombreTipoParametro",
	"ValorParametro",
	"NombreParametro",
	"IdEstado",
	-- MMACHMAR - Se usa alias 'NumeroDiaSemana' en combobox para que día semana comience desde 1 y asi usar opcion TODOS='S'
	CASE WHEN "NombreTipoParametro" = 'DIASEMANAESPANOL' THEN "ValorParametro"+1 ELSE NULL END AS "NumeroDiaSemana"
FROM "BSCL.Mesa.BD::MES.Parametro"
WHERE "IdEmpresa" IN (:IdEmpresa)
  AND "NombreParametro" = (:NombreParametro)
  AND "NombreTipoParametro" = (:NombreTipoParametro)
  AND "IdEstado" = (:IdEstado);
