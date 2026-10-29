/********************************************************** 
Proposito	: 
autor		: NRM
fecha		: 15/01/2018
***********************************************************/


WITH
	Custodias AS (
		SELECT
            'CARTERA' AS "TablaAfectada",c."IdCartera" AS "IdCustodia",c."FolioCartera" AS "FolioCustodia",
            c."IdMonedaSistema",c."IdCliente",c."IdPortafolio",c."IdTipoProducto",
            c."IdTipoOperacion",c."IdClasificacionTipoInstrumento",c."IdTipoInstrumento",c."IdInstrumento",c."Nemotecnico",c."IdMonedaInstrumento",
            '' AS "IdOrigenInstrumento",c."INGPrecioCompra",c."INGCantidad",c."IdMonedaAdicional",c."IdBoveda"
        FROM "BSCL.Custodia.BD::CUS.Cartera" AS c
        INNER JOIN "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Proceso_Paso" tpc ON tpc."Id" = c."IdCliente" AND tpc."TipoEntrada" = 'CLIENTE'
        INNER JOIN "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Proceso_Paso" tpp ON tpp."Id" = c."IdPortafolio" AND tpp."TipoEntrada" = 'PORTAFOLIO'
        INNER JOIN "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Proceso_Paso" tpi ON tpi."Id" = c."IdInstrumento" AND tpi."TipoEntrada" = 'INSTRUMENTO'
        WHERE c."IdEstado" = 'VIG'
        --AND (c."IdCliente" 		IN (:IdCliente) 	OR 1 = :FiltroClienteValido)
		--AND (c."IdPortafolio" 	IN (:IdPortafolio)	OR 1 = :FiltroPortafolioValido)
		--AND (c."IdInstrumento"	IN (:IdInstrumento)	OR 1 = :FiltroInstrumentoValido)
        UNION ALL
        SELECT
            'SUBYACENTE' AS "TablaAfectada",s."IdSubyacente" AS "IdCustodia",s."FolioSubyacente" AS "FolioCustodia",
            c."IdMonedaSistema",c."IdCliente",c."IdPortafolio",c."IdTipoProducto",c."IdTipoOperacion",
            s."IdClasificacionTipoInstrumento",s."IdTipoInstrumento",s."IdInstrumento",s."Nemotecnico",s."IdMonedaInstrumento",'' AS "IdOrigenInstrumento",
            s."PrecioCompraOperacionRelacionada" AS "INGPrecioCompra",s."INGCantidad",c."IdMonedaAdicional",c."IdBoveda"
        FROM "BSCL.Custodia.BD::CUS.Cartera" AS c
        INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" AS s ON (s."FolioCarteraRelacionada" = c."FolioCartera"  
                                                           AND s."IdEstado" = 'VIG')
        INNER JOIN "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Proceso_Paso" tpc ON tpc."Id" = c."IdCliente" AND tpc."TipoEntrada" = 'CLIENTE'
        INNER JOIN "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Proceso_Paso" tpp ON tpp."Id" = c."IdPortafolio" AND tpp."TipoEntrada" = 'PORTAFOLIO'
        INNER JOIN "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Proceso_Paso" tpi ON tpi."Id" = c."IdInstrumento" AND tpi."TipoEntrada" = 'INSTRUMENTO'
        WHERE c."IdEstado" = 'VIG'
        --AND (c."IdCliente" 		IN (:IdCliente) 	OR 1 = :FiltroClienteValido)
		--AND (c."IdPortafolio" 	IN (:IdPortafolio)	OR 1 = :FiltroPortafolioValido)
		--AND (s."IdInstrumento"	IN (:IdInstrumento)	OR 1 = :FiltroInstrumentoValido)
        UNION ALL
        SELECT
            'GARANTIA' AS "TablaAfectada",g."IdGarantia" AS "IdCustodia",g."FolioGarantia" AS "FolioCustodia",
            g."IdMonedaSistema",g."IdCliente",g."IdPortafolio",
            c."IdTipoProducto",c."IdTipoOperacion",g."IdClasificacionTipoInstrumento",g."IdTipoInstrumento",g."IdInstrumento",g."Nemotecnico",
            g."IdMonedaInstrumento",g."IdOrigenGarantia" AS "IdOrigenInstrumento",g."PrecioCompraOperacionOrigen" AS "INGPrecioCompra",g."INGCantidad",g."IdMonedaAdicional",c."IdBoveda"
        FROM "BSCL.Custodia.BD::GTA.Garantia" AS g
        INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS c ON (c."IdCartera" = g."IdCarteraOrigen")
        INNER JOIN "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Proceso_Paso" tpc ON tpc."Id" = c."IdCliente" AND tpc."TipoEntrada" = 'CLIENTE'
        INNER JOIN "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Proceso_Paso" tpp ON tpp."Id" = c."IdPortafolio" AND tpp."TipoEntrada" = 'PORTAFOLIO'
        INNER JOIN "BSCL.Custodia.BD::GBL.CalcularPrecioPromedio_Proceso_Paso" tpi ON tpi."Id" = c."IdInstrumento" AND tpi."TipoEntrada" = 'INSTRUMENTO'
        WHERE g."IdEstado" = 'VIG'
        --AND (g."IdCliente" 		IN (:IdCliente) 	OR 1 = :FiltroClienteValido)
		--AND (g."IdPortafolio" 	IN (:IdPortafolio)	OR 1 = :FiltroPortafolioValido)
		--AND (g."IdInstrumento"	IN (:IdInstrumento)	OR 1 = :FiltroInstrumentoValido)
	),
	Configuracion AS (
		SELECT
	            "IdConfiguracionPrecioPromedio",
	            CASE WHEN "IdTipoProducto" = '' THEN NULL ELSE "IdTipoProducto" END as  "IdTipoProducto",
	            CASE WHEN "IdTipoOperacion" = '' THEN NULL ELSE "IdTipoOperacion" END as "IdTipoOperacion",
	            CASE WHEN "IdClasificacionTipoInstrumento" = '' THEN NULL ELSE "IdClasificacionTipoInstrumento" END as "IdClasificacionTipoInstrumento",
	            CASE WHEN "IdTipoInstrumento" = '' THEN NULL ELSE "IdTipoInstrumento" END as "IdTipoInstrumento",
	            CASE WHEN "IdInstrumento" = 0 THEN NULL ELSE "IdInstrumento" END as "IdInstrumento",
	            CASE WHEN "IdOrigenInstrumento" = '' THEN NULL ELSE "IdOrigenInstrumento" END as "IdOrigenInstrumento"
	      FROM "BSCL.Custodia.BD::CUS.ConfiguracionPrecioPromedio"
	      WHERE "IdEstado" = 'ACT'
	)
SELECT
    "TablaAfectada","IdCustodia","IdCliente","IdPortafolio","IdInstrumento","IdMonedaInstrumento","IdBoveda","INGCantidad" AS "Cantidad",
    MAX(case when "CantidadTotalInstrumento" = 0 then 0 else "SumMontoCompra"/"CantidadTotalInstrumento" end ) AS "PrecioPromedio"
FROM(
    SELECT
        r."TablaAfectada",r."IdCustodia",r."FolioCustodia",r."IdMonedaSistema",r."IdCliente",r."IdPortafolio",r."IdTipoProducto",
        r."IdTipoOperacion",r."IdClasificacionTipoInstrumento",r."IdTipoInstrumento",r."IdInstrumento",r."Nemotecnico",r."IdMonedaInstrumento",
        r."IdOrigenInstrumento",r."INGPrecioCompra",r."INGCantidad",r."IdMonedaAdicional",r."IdBoveda",
        SUM(r."INGPrecioCompra"*r."INGCantidad") over (partition by r."IdCliente",r."IdPortafolio",r."IdInstrumento",r."IdBoveda") AS "SumMontoCompra",
        SUM(r."INGCantidad") over (partition by r."IdCliente",r."IdPortafolio",r."IdInstrumento",r."IdBoveda") AS "CantidadTotalInstrumento",
        COUNT(r."IdCustodia") over (partition by r."IdCliente",r."IdPortafolio",r."IdInstrumento",r."IdBoveda") AS "CantidadRegistros"
    FROM Custodias r
    INNER JOIN (
                SELECT
                    rc."TablaAfectada",rc."IdCustodia",MAX("DatoRestringido") AS "DatoRestringido"
                FROM
                (
                    SELECT
                        "TablaAfectada","IdCustodia","IdConfiguracionPrecioPromedio",
                        CASE WHEN "IdTipoProductoRestringido" = 'S' THEN 'S'
                             WHEN "IdTipoOperacionRestringido" = 'S' THEN 'S'
                             WHEN "IdClasificacionTipoInstrumentoRestringido" = 'S' THEN 'S'
                             WHEN "IdTipoInstrumentoRestringido" = 'S' THEN 'S'
                             WHEN "IdInstrumentoRestringido" = 'S' THEN 'S'
                             WHEN "IdOrigenInstrumentoRestringido" = 'S' THEN 'S'
                             ELSE 'N'
                        END AS "DatoRestringido"
                    FROM
                    (
                        SELECT
                            r."TablaAfectada",r."IdCustodia",c."IdConfiguracionPrecioPromedio",
                            CASE WHEN c."IdTipoProducto" IS NULL THEN 'N'
                                 WHEN c."IdTipoProducto" =  r."IdTipoProducto" THEN 'S'
                                 ELSE 'N'
                            END AS "IdTipoProductoRestringido",
                            CASE WHEN c."IdTipoOperacion" IS NULL THEN 'N'
                                 WHEN c."IdTipoOperacion" =  r."IdTipoOperacion" THEN 'S'
                                 ELSE 'N'
                            END AS "IdTipoOperacionRestringido",
                            CASE WHEN c."IdClasificacionTipoInstrumento" IS NULL THEN 'N'
                                 WHEN c."IdClasificacionTipoInstrumento" =  r."IdClasificacionTipoInstrumento" THEN 'S'
                                 ELSE 'N'
                            END AS "IdClasificacionTipoInstrumentoRestringido",
                            CASE WHEN c."IdTipoInstrumento" IS NULL THEN 'N'
                                 WHEN c."IdTipoInstrumento" =  r."IdTipoInstrumento" THEN 'S'
                                 ELSE 'N'
                            END AS "IdTipoInstrumentoRestringido",
                            CASE WHEN c."IdInstrumento" IS NULL THEN 'N'
                                 WHEN c."IdInstrumento" =  r."IdInstrumento" THEN 'S'
                                 ELSE 'N'
                            END AS "IdInstrumentoRestringido",
                            CASE WHEN c."IdOrigenInstrumento" IS NULL THEN 'N'
                                 WHEN c."IdOrigenInstrumento" =  r."IdOrigenInstrumento" THEN 'S'
                                 ELSE 'N'
                            END AS "IdOrigenInstrumentoRestringido"
                        FROM Custodias AS r, Configuracion AS  c
                    )
                ) AS rc
                GROUP BY rc."TablaAfectada",rc."IdCustodia") AS rv
    ON (rv."TablaAfectada" = r."TablaAfectada"
    AND rv."IdCustodia" = r."IdCustodia"
    AND rv."DatoRestringido" = 'N')
)
GROUP BY  "TablaAfectada","IdCustodia","IdCliente","IdPortafolio","IdInstrumento","IdMonedaInstrumento","IdBoveda","INGCantidad";