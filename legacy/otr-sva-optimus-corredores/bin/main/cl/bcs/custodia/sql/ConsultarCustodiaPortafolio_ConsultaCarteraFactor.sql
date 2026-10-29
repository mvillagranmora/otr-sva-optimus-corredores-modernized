SELECT
	"Cantidad",
	--solicitados por normativa, Mario Santander
	"TipoFactor",
    "FechaFactor",
    "ValorFactor",
    "MontoCompraFactor"
FROM "BSCL.Custodia.BD::CUS.Cartera"
WHERE "IdEmpresa"=:IdEmpresa AND "IdCartera" IN (:IdCartera);