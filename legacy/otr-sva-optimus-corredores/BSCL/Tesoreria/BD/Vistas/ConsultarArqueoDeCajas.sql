SET SCHEMA "BSCL";
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarArqueoDeCajas";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarArqueoDeCajas" AS
SELECT 	 CJ."IdCaja"
		,CJ."IdMedioDePago"
		,CJ."NombreMedioDePago"
		,CJ."IdModalidadDePago"
		,CFP."IdMoneda"
		,COALESCE(COUNT(CFP."IdMedioDePago"),0) as "Cantidad"
		,COALESCE(SUM(CFP."Monto"),0) as "Monto"
  FROM ( SELECT CJ."IdCaja", MMP."IdMedioDePago", MDP."Nombre" as "NombreMedioDePago", MMP."IdModalidadDePago"
		   FROM "BSCL.Tesoreria.BD::TES.Caja" as CJ,"BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" as MMP
		  INNER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" as MDP ON MDP."IdMedioDePago" = MMP."IdMedioDePago"
  	   ) as CJ
  LEFT JOIN ( SELECT C."IdCaja",CFP."IdModalidadDePago",CFP."IdMoneda",CFP."IdMedioDePago",CFP."Monto"
			    FROM "BSCL.Tesoreria.BD::TES.Comprobante" as C 
			   INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP 
			  		   ON CFP."IdComprobante" = C."IdComprobante"
			  		  AND CFP."IdEstado" IN ('ING','CRT')
			  		  AND CFP."IdMedioDePago" <> 'EFE'
			   UNION ALL
			  SELECT C."IdCaja",CFP."IdModalidadDePago",CFP."IdMoneda",CFP."IdMedioDePago",CFP."Monto"
			    FROM "BSCL.Tesoreria.BD::TES.Comprobante" as C 
			   INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP 
			  		   ON CFP."IdComprobante" = C."IdComprobante"
			  		  AND (CFP."IdEstado" = 'ING'
			  		  		OR
			  		  	   (CFP."IdEstado" = 'LIQ' AND COALESCE(CFP."IdDeposito",0) = 0 AND CFP."Depositado" = 'N')	
			  		  	  )
			  		  AND CFP."IdMedioDePago" = 'EFE'
  			) as CFP 
  		 ON CFP."IdCaja" = CJ."IdCaja"
  		AND CFP."IdMedioDePago" = CJ."IdMedioDePago"		
 GROUP BY CJ."IdCaja",CJ."IdMedioDePago",CJ."NombreMedioDePago",CJ."IdModalidadDePago",CFP."IdMoneda"
  WITH READ ONLY;

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarArqueoDeCajas" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
 
