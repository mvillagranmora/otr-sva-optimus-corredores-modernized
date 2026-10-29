SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarTipoCanalIngresoCobro"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarTipoCanalIngresoCobro" 
AS
SELECT NULL AS "IdTipoCanalIngreso",'TODOS' AS "NombreTipoCanalIngreso" FROM DUMMY
UNION
SELECT "IdTipoCanalIngreso","Nombre" AS "NombreTipoCanalIngreso"
FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" WHERE "PermiteIngresoOrdenes" = 'S';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarTipoCanalIngresoCobro" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

