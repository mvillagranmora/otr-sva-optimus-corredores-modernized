SELECT 	
	CF."IdOperacion",
    CF."FolioCartera",
    CF."IdCartera",
    CF."IdCliente",
    CF."IdPortafolio", 
    CF."IdInstrumento",
    CF."INGCantidad",
    CF."Cantidad",
    CF."GeneraNuevoMovimiento",
    CF."IdEstado", 
    CF."IdEmisor", 
    CF."IdRegimenFondoMutuo", 
    CF."Monto"
FROM (
	SELECT
	    C."IdOperacion",
	    C."FolioCartera",
	    C."IdCartera",
	    C."IdCliente",
	    C."IdPortafolio", 
	    C."IdInstrumento",
	    C."INGCantidad",
	    CASE C."IdEstado" 
	        WHEN 'VIG' THEN C."INGCantidad" + E."EGRCantidad" 
	        ELSE E."EGRCantidad" + 0 
	    END AS "Cantidad",
	    'S' AS "GeneraNuevoMovimiento",
	    C."IdEstado", 
	    C."IdEmisor", 
	    C."IdRegimenFondoMutuo", 
	    C."INGMontoATasaMercadoActual" AS "Monto",
	    RANK() OVER (PARTITION BY C."FolioCartera" ORDER BY C."IdCartera" desc) AS "Rank"
	FROM 
	    "BSCL.Custodia.BD::CUS.Cartera" C
	INNER JOIN 
	    (
	    	SELECT 
		    	C2."IdOperacion",
			    C2."FolioCartera",
			    SUM(C2."EGRCantidad") AS "EGRCantidad"  
	    	FROM "BSCL.Custodia.BD::CUS.Cartera" C2
	    	WHERE C2."IdEstado" NOT IN ('VIG', 'ANU') 
	    		  AND C2."EGRIdOperacion" IN (:IdSolicitudAporteRescate)
	    		  AND (C2."IdModulo" = 'FMM' OR (C2."IdModulo" ='CUS' AND C2."IdTipoInstrumento" = 'FFM'))
	    	GROUP BY C2."IdOperacion", C2."FolioCartera", C2."IdModulo"
	    ) E ON (C."FolioCartera" = E."FolioCartera") 
	WHERE C."IdEstado" IN ('VIG', 'LIQ')
			AND NOT (C."IdEstado" IN ('LIQ') AND  (SELECT COUNT(1) 
	   											   FROM "BSCL.Custodia.BD::CUS.Cartera" C3
	   											   WHERE C3."FolioCartera" = C."FolioCartera" AND C3."IdEstado" = 'VIG') > 0)
	) CF
WHERE CF."Rank" = 1

UNION ALL																	
   																			
SELECT DISTINCT
    C."IdOperacion",
    C."FolioCartera",
    C."IdCartera",
    C."IdCliente",
    C."IdPortafolio", 
    C."IdInstrumento",
    C."INGCantidad",
    CASE C."IdEstado" 
        WHEN 'VIG' THEN C."INGCantidad" + E."EGRCantidad" 
        ELSE E."EGRCantidad" + 0 
    END AS "Cantidad",
    'N' AS "GeneraNuevoMovimiento",
    C."IdEstado", 
    C."IdEmisor", 
    C."IdRegimenFondoMutuo", 
    C."INGMontoATasaMercadoActual" AS "Monto"
FROM 
    "BSCL.Custodia.BD::CUS.Cartera" C
INNER JOIN 
    "BSCL.Custodia.BD::CUS.Cartera" E ON (C."FolioCartera" = E."FolioCartera")
WHERE C."IdEstado" IN ('VIG', 'LIQ') 
	  AND E."IdOperacion" IN (:IdSolicitudAporteRescate) 
	  AND E."INGIdConcepto" = 'APOTRS'
      AND (E."IdModulo" = 'FMM' OR (E."IdModulo" ='CUS' AND E."IdTipoInstrumento" = 'FFM'));