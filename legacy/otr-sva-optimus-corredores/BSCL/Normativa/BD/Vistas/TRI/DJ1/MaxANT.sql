SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxANT";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxANT"  AS
	SELECT 
		"FolioCartera", "IdEstado",
		CASE WHEN FPE."FechaLiquidacionComprobantePagoOperacion" > FPR."FechaLiquidacionComprobantePagoOperacion" THEN FPE."FechaLiquidacionComprobantePagoOperacion"
				   WHEN FPE."FechaLiquidacionComprobantePagoOperacion" < FPR."FechaLiquidacionComprobantePagoOperacion" THEN FPR."FechaLiquidacionComprobantePagoOperacion"
				   ELSE COALESCE(FPE."FechaLiquidacionComprobantePagoOperacion",FPR."FechaLiquidacionComprobantePagoOperacion")
			   END AS "fecha", 
	--	CASE WHEN FPE."FechaLiquidacionComprobantePagoOperacion" > FPR."FechaLiquidacionComprobantePagoOperacion" THEN CAST(FPE."FechaLiquidacionComprobantePagoOperacion" AS CHAR(7))
	--			   WHEN FPE."FechaLiquidacionComprobantePagoOperacion" < FPR."FechaLiquidacionComprobantePagoOperacion" THEN CAST(FPR."FechaLiquidacionComprobantePagoOperacion" AS CHAR(7))
	--			   ELSE CAST(COALESCE(FPE."FechaLiquidacionComprobantePagoOperacion",FPR."FechaLiquidacionComprobantePagoOperacion")  AS CHAR(7))
	--		   END AS "fecha", 	   
		MAX("IdCartera") AS "IdCartera"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite" AS CAR
		LEFT JOIN (
		        SELECT DISTINCT "IdOperacion", "FechaLiquidacionComprobantePagoOperacion","IdEmpresa"
		        FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
		        WHERE "IdTipoProducto" = 'MEFW' AND "RecibePaga" = 'P'
		    ) AS FPE
		    ON (FPE."IdOperacion" = CAR."IdCartera" AND FPE."IdEmpresa" = CAR."IdEmpresa")
		LEFT JOIN (
		        SELECT DISTINCT "IdOperacion", "FechaLiquidacionComprobantePagoOperacion","IdEmpresa"
		        FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
		        WHERE "IdTipoProducto" = 'MEFW' AND "RecibePaga" = 'R'
		    ) AS FPR
		   ON (FPR."IdOperacion" = CAR."IdCartera" AND FPR."IdEmpresa" = CAR."IdEmpresa")
		WHERE "IdTipoProducto" = 'MEFW'
		AND "IdEstado" = 'ANT'
		GROUP BY "FolioCartera", "IdEstado", "FechaVencimientoOperacion",
		--GROUP BY "FolioCartera", "IdEstado", CAST("FechaVencimientoOperacion" AS CHAR(7)),
		FPE."FechaLiquidacionComprobantePagoOperacion", FPR."FechaLiquidacionComprobantePagoOperacion"  

WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxANT" TO bcs_desarrollador WITH GRANT OPTION;
