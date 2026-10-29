SELECT 
"Id"
,"IdCliente"
,"IdPortafolio"
,"IdMoneda"
,"IdMedioDePago"
,"IdCuentaBancaria"
,"IdPreferenciaPagoPortafolio"
FROM(
	SELECT 
	row_number() OVER (partition by PPP."IdPortafolio", PP."IdMoneda", PP."IdMedioDePago", PPP."IdCuentaBancaria" order BY PPP."IdPreferenciaPagoPortafolio" DESC) AS "Row"
	,PPP."IdPortafolio" || '_' || PP."IdMoneda" || '_' || PP."IdMedioDePago" || '_' || PPP."IdCuentaBancaria" AS "Id"
	,POR."IdCliente"
	,PPP."IdPortafolio"
	,PP."IdMoneda"
	,PP."IdMedioDePago"
	,PPP."IdCuentaBancaria"
	,PPP."IdPreferenciaPagoPortafolio"
	FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" PPP
	INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON PPP."IdPortafolio" = POR."IdPortafolio"
	INNER JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" as PP ON PPP."IdPreferenciaPago" = PP."IdPreferenciaPago"
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TIPOP ON TIPOP."IdTipoOperacion" = PP."IdTipoOperacion"
	WHERE 
	PPP."IdEmpresa" = :IdEmpresa
	AND POR."IdCliente" IN (:IdCliente)
	AND PP."IdMoneda" IN (:IdMoneda)
	AND PPP."IdCuentaBancaria" IN (:IdCuentaBancaria)
	AND PP."IdMedioDePago" IN (:IdMedioDePago)
	AND TIPOP."RecibePagaPortafolio" = 'R'
	AND TIPOP."IdTipoProducto" = 'INFM'
)
WHERE "Row" = 1