SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.Carteras";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.Carteras" AS
((SELECT 
		MCA."FolioCartera",
		TO_DATE(OPE."FechaLiquidacion") AS "fecha",
		MCA."IdCartera",
		MCA."EventoReporte"
	FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxCarteras" MCA
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite" CAR
		ON MCA."IdCartera" = CAR."IdCartera" AND CAR."IdEstado" <> 'MOD'
	INNER JOIN "BSCL.Mesa.BD::MES.Operacion" AS OPE
		ON (OPE."IdOperacion" = CAR."IdOperacion" AND OPE."IdTipoProducto" = CAR."IdTipoProducto")
	LEFT JOIN (
	        SELECT DISTINCT "IdOperacion", "FechaLiquidacionComprobantePagoOperacion", "IdEmpresa"
	        FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
	        WHERE "IdTipoProducto" = 'MEFW' AND "RecibePaga" = 'P'
	    ) AS FPE
	    ON (FPE."IdOperacion" = CAR."IdCartera" AND FPE."IdEmpresa" = CAR."IdEmpresa")
	LEFT JOIN (
	        SELECT DISTINCT "IdOperacion", "FechaLiquidacionComprobantePagoOperacion", "IdEmpresa"
	        FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
	        WHERE "IdTipoProducto" = 'MEFW' AND "RecibePaga" = 'R'
	    ) AS FPR
	   ON (FPR."IdOperacion" = CAR."IdCartera" AND FPR."IdEmpresa" = CAR."IdEmpresa")) UNION (SELECT	
		MCA."FolioCartera",
		CAR."FechaOperacion"	AS "fecha",
		MCA."IdCartera",
		MCA."EventoReporte"
	FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxVIG" MCA
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite" CAR
		ON MCA."IdCartera" = CAR."IdCartera") ORDER BY "FolioCartera", "fecha", "IdCartera")
WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1.Carteras" TO bcs_desarrollador WITH GRANT OPTION;