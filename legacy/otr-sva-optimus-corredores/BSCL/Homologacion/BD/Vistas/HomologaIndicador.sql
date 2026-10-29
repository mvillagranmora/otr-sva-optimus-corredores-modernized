SET SCHEMA "BSCL"; 
--GO 
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaIndicador";
--GO
create view "BSCL.Homologacion.BD.Vistas::HomologaIndicador" as
SELECT 114  AS ind_id,'Autoriza Mercados Extranjeros' AS ind_nombre, 114 as OPTIMUS_ID from DUMMY UNION ALL
SELECT 120  AS ind_id,'Contrato Factura Electrónica' AS ind_nombre, 120 as OPTIMUS_ID  from DUMMY UNION ALL
SELECT 125  AS ind_id,'Advisor Asociado' AS ind_nombre, 125 as OPTIMUS_ID  from DUMMY UNION ALL
SELECT 0  AS ind_id,'NULL' AS ind_nombre, 0 as OPTIMUS_ID  from DUMMY
;
--GO
GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaIndicador" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
--GOahi conec