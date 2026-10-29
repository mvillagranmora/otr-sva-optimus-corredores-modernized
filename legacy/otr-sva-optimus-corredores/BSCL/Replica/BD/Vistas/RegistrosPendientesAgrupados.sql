SET SCHEMA "BSCL";

DROP VIEW "BSCL.Replica.BD.Vistas::RegistrosPendientesAgrupados";

CREATE VIEW  "BSCL.Replica.BD.Vistas::RegistrosPendientesAgrupados" AS
	SELECT "Table","Action", max("Stamp") AS stamp,count(1) as count
	FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack"
	WHERE "Stamp" >= ADD_SECONDS(CURRENT_TIMESTAMP,-60*20)   --REVISAMOS LOS ULTIMOS 20 MINUTOS
	AND "Status" = 0
	AND 
	(
		("Table" NOT IN ('Custodia_MovimientoCarteraIngresos',
						'Custodia_CustodiaFechaLiquidacion',
						'Custodia_CustodiaFechaOperacion')
		AND "Action" = 'A') OR "Action" = 'I'
	)
	GROUP BY "Table","Action"
	ORDER BY stamp;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::RegistrosPendientesAgrupados" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
	
