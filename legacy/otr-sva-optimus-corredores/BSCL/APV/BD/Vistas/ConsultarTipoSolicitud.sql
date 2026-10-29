SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.APV.BD.Vistas::ConsultarTipoSolicitud"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::ConsultarTipoSolicitud" AS 

SELECT 'AUT' 			AS "Codigo"
     , 'AUTORIZACION' 	AS "Nombre"
FROM DUMMY
UNION ALL
SELECT 'TRA' 			AS "Codigo"
     , 'TRASPASO' 	AS "Nombre"
FROM DUMMY
UNION ALL
SELECT 'MOD' 			AS "Codigo"
     , 'MODIFICACION' 	AS "Nombre"
FROM DUMMY;

GRANT SELECT ON "BSCL.APV.BD.Vistas::ConsultarTipoSolicitud" TO  BCS_DESARROLLADOR WITH GRANT OPTION;