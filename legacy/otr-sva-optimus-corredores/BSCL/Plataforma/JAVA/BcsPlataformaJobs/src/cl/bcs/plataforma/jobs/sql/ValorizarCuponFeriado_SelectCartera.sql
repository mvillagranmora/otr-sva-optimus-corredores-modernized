WITH CTE AS (
--Cartera
SELECT TO_VARCHAR("INGFechaCorteCupon",'YYYY-MM-DD') AS "INGFechaCorteCupon" 
FROM "BSCL.Custodia.BD::CUS.Cartera" 
WHERE ("INGFechaCorteCupon" <> "INGFechaPagoCorteCupon") AND ("INGFechaCorteCupon" BETWEEN :FechaDesde AND :FechaHasta) AND "IdEstado" IN ('VIG','VCD') 
UNION ALL
--Cartera Negativa
SELECT TO_VARCHAR("FechaCorteCupon",'YYYY-MM-DD') AS "INGFechaCorteCupon" 
FROM "BSCL.Custodia.BD::CUS.CarteraNegativa" 
WHERE ("FechaCorteCupon" <> "FechaPagoCorteCupon") AND ("FechaCorteCupon" BETWEEN :FechaDesde AND :FechaHasta) AND "IdEstado" IN ('VIG','VCD')
UNION ALL
--Garantías
SELECT TO_VARCHAR(CA."INGFechaCorteCupon" ,'YYYY-MM-DD') AS "INGFechaCorteCupon"
FROM "BSCL.Custodia.BD::GTA.Garantia" GTA 
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CA ON (CA."FolioCartera" = GTA."FolioCarteraOrigen" AND CA."Nemotecnico" = GTA."Nemotecnico") 
WHERE (CA."INGFechaCorteCupon" <> CA."INGFechaPagoCorteCupon") AND (CA."INGFechaCorteCupon" BETWEEN :FechaDesde AND :FechaHasta) AND 	GTA."IdEstado" = 'VIG' AND 	CA."IdEstado" IN ('LIQ', 'RPA')
)
SELECT "INGFechaCorteCupon" FROM CTE GROUP BY "INGFechaCorteCupon"