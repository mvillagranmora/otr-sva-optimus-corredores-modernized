SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Custodia.BD.Vistas::PRT.ConsultarAsignacionesPrestamo";

CREATE VIEW "BSCL.Custodia.BD.Vistas::PRT.ConsultarAsignacionesPrestamo"
AS 

SELECT 
	 prt."FolioCartera" as "FolioCarteraPrestamo"
	,prt."IdEmpresa"
	,AVG(prt."Cantidad") as "Cantidad"
	,COALESCE(SUM(emp."INGCantidad"),0) as "CantidadAsignada"
	,AVG(prt."INGCantidad") - COALESCE(SUM(emp."INGCantidad"),0) as "CantidadPorAsignar"
FROM "BSCL.Custodia.BD::CUS.Cartera" as prt
LEFT JOIN  "BSCL.Custodia.BD::CUS.Cartera" as emp on 
	prt."IdEmpresa" = emp."IdEmpresa" and
	prt."FolioCartera" = emp."FolioCarteraRelacionada" and
	emp."IdTipoInstrumento" IN ('EMP', 'ERF') and
	emp."IdEstado" = 'VIG' and
	emp."FolioCarteraRelacionada" > 0

WHERE
	prt."IdTipoInstrumento" IN ('PRT','PRF')
	and prt."IdEstado" = 'VIG'
GROUP BY  prt."IdEmpresa",prt."FolioCartera"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::PRT.ConsultarAsignacionesPrestamo" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
