SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarReporteAPV";

CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarReporteAPV" AS
SELECT 
	 REP."IdReporteAPV"      		
    ,REP."DocumentoIdentidad"    		
    ,REP."NombreCliente"         		
    ,REP."CodigoRegimenTributario"
    ,REP."NombreRegimenTributario"
    ,REP."TipoProducto"
    ,REP."IdPlanTipoInversion"
    ,REP."NombrePlanTipoInversion"
    ,REP."FechaCierre"
    ,REP."SaldoAcumulado"
    ,REP."SaldoRegimenTributarioA"
    ,REP."SaldoRegimenTributarioB"
    ,REP."SaldoBonificacionEstatal"
    ,REP."SaldoAPVCEmpleador"
 	,REP."SaldoAPVCTrabajadorA"
 	,REP."SaldoAPVCTrabajadorB"
 	,REP."SaldoAPVCBonificacionEstatal"
FROM "BSCL.Normativa.BD::NOR.ReporteAPV" AS REP 
ORDER BY "IdReporteAPV"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.ConsultarReporteAPV"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
