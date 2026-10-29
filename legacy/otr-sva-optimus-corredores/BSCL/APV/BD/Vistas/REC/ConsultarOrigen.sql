SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.APV.BD.Vistas::REC.ConsultarOrigen"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::REC.ConsultarOrigen"  AS 

SELECT
	Origen."IdOrigen"
	,Origen."Codigo"
	,Origen."Nombre"
	,Origen."Tipo"
	,Origen."IdEstado"
	,OrigenDestinoTraspaso."DestinoCtaBco"
FROM "BSCL.APV.BD::REC.Origen" Origen
LEFT JOIN "BSCL.APV.BD.Vistas::APV.ConsultarOrigenDestinoTransicion" OrigenDestinoTraspaso 
ON OrigenDestinoTraspaso."IdOrigenDestinoTransicion" = 6 
AND Origen."Codigo" = 'TRASPA'
AND OrigenDestinoTraspaso."IdEstado" = 'ACT';

GRANT SELECT ON "BSCL.APV.BD.Vistas::REC.ConsultarOrigen" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 