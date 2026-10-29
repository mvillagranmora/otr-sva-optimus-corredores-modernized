SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO2P2.PreOrigen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO2P2.PreOrigen" (

		  	"IdEmpresa",
		  	"MonedaSaldo",
			"Fecha",
			"Compras",
			"Ventas",
			"SaldoMonedaOriginal",
			"Paridad",
			--campos de control interno especifico
			"IdCargoAbonoMovimiento",
			"IdCliente",
			"IdTipoMovimiento",
			"FechaEfectivaMovimiento",
			"IdPortafolio"
	
) AS

		SELECT
			t."IdEmpresa",
			t."IdMoneda",
			t."Fecha",
			t."Compras",
			t."Ventas",
			t."SaldoDiario",
			t."Paridad",
			t."IdCargoAbonoMovimiento",
			t."IdCliente",
			t."IdTipoMovimiento",
			t."FechaEfectivaMovimiento",
			t."IdPortafolio"
		FROM (
			SELECT
				a."IdEmpresa",
				a."IdMoneda",
				a."Fecha",
				a."Compras",
				a."Ventas",
				a."SaldoDiario",
				a."Paridad",
				b."IdCargoAbonoMovimiento",
				b."IdCliente",
				b."IdTipoMovimiento",
				b."FechaEfectivaMovimiento",
				b."IdPortafolio"
			FROM (
				SELECT
					ms."IdEmpresa",
					ms."IdMonedaPrincipal" AS "IdMoneda",
					CAST(ms."FechaOperacion" AS DATE) AS "FechaEfectivaMovimiento",
					COALESCE(CEH."FechaHasta", LAST_DAY(ms."FechaOperacion")) AS "Fecha",
					SUM(CASE WHEN ms."IdTipoOperacion" = 'COMSPT' THEN ms."MontoMonedaPrincipal" ELSE 0 END) AS "Compras",
					SUM(CASE WHEN ms."IdTipoOperacion" = 'VENSPT' THEN ms."MontoMonedaPrincipal" ELSE 0 END) AS "Ventas",
					SUM(CASE
							WHEN ms."IdTipoOperacion" = 'COMSPT' THEN  ms."MontoMonedaPrincipal"
							WHEN ms."IdTipoOperacion" = 'VENSPT' THEN -ms."MontoMonedaPrincipal"
							ELSE 0
						END) AS "SaldoDiario",
					CASE WHEN ms."IdMonedaPrincipal" = 'USD' THEN 1.0 ELSE ptc."ParidadMercado" END AS "Paridad"
				FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" ms
				INNER JOIN (
					SELECT
						p."IdMoneda",
						CAST(p."Fecha" AS DATE) AS "Fecha",
						p."ParidadMercado"
					FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" p
					INNER JOIN (
						SELECT
							MAX("IdParidadYTipoCambio") AS "IdParidad"
						FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio"
						WHERE "IdEstado" = 'ACT'
						GROUP BY "IdMoneda", CAST("Fecha" AS DATE)
					) x
						ON x."IdParidad" = p."IdParidadYTipoCambio"
				) ptc
					ON ptc."IdMoneda" = ms."IdMonedaPrincipal"
				AND ptc."Fecha" = CAST(ms."FechaOperacion" AS DATE)
				LEFT JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarEsquemaHabil" CEH
					ON CEH."Esquema" = 'REPO2P2'
				AND CEH."Version" = TO_VARCHAR(YEAR(ms."FechaOperacion")) || '/' || LPAD(TO_VARCHAR(MONTH(ms."FechaOperacion")), 2, '0')
				WHERE
					ms."Estado" = 'VIG'  AND ms."EstadoConfirmacion" = 'CNF'
					AND ms."IdTipoOperacion" IN ('COMSPT','VENSPT')
				GROUP BY
					ms."IdEmpresa",
					ms."IdMonedaPrincipal",
					CAST(ms."FechaOperacion" AS DATE),
					COALESCE(CEH."FechaHasta", LAST_DAY(ms."FechaOperacion")),
					ptc."ParidadMercado"
			) a
			INNER JOIN (
				SELECT
					s."IdEmpresa",
					s."IdMonedaPrincipal" AS "IdMoneda",
					CAST(s."FechaOperacion" AS DATE) AS "FechaEfectivaMovimiento",
					s."IdOperacion" AS "IdCargoAbonoMovimiento",
					s."IdCliente" AS "IdCliente",
					s."IdTipoOperacion" AS "IdTipoMovimiento",
					s."IdPortafolio" AS "IdPortafolio",
					ROW_NUMBER() OVER (
						PARTITION BY
							s."IdEmpresa",
							s."IdMonedaPrincipal",
							CAST(s."FechaOperacion" AS DATE)
						ORDER BY
							s."FechaOperacion" DESC,
							s."IdOperacion" DESC
					) AS rn
				FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" s
				WHERE
					s."Estado" = 'VIG' AND s."EstadoConfirmacion" = 'CNF'
					AND s."IdTipoOperacion" IN ('COMSPT','VENSPT')
			) b
				ON b."IdEmpresa" = a."IdEmpresa"
			AND b."IdMoneda" = a."IdMoneda"
			AND b."FechaEfectivaMovimiento" = a."FechaEfectivaMovimiento"
			AND b.rn = 1
		) t
		WHERE
			t."SaldoDiario" <> 0
		ORDER BY t."Fecha" DESC

	 

WITH read ONLY;	

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.REPO2P2.PreOrigen" TO bcs_desarrollador WITH GRANT OPTION;
