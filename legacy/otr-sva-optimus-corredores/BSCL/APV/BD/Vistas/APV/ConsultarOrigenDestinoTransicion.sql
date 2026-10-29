SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.APV.BD.Vistas::APV.ConsultarOrigenDestinoTransicion"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::APV.ConsultarOrigenDestinoTransicion" AS 

SELECT
	"IdEmpresa"
	,"IdOrigenDestinoTransicion"
	,CASE	WHEN "Operacion" = 'DEV' THEN 'DEVOLUCION' 
			WHEN "Operacion" = 'DIS' THEN 'DISTRIBUCION' 
			WHEN "Operacion" = 'REZ' THEN 'REZAGO' 
			WHEN "Operacion" = 'BON' THEN 'BONIFICACION' 
			WHEN "Operacion" = 'DBF' THEN 'DEVOLUCION BONIFICACION' 
			WHEN "Operacion" = 'DTA' THEN 'DISTRIBUCION TRASPASO APV' 
			ELSE "Operacion" END AS "Operacion"
	,"MueveCuentaCorriente"	
	,"MueveCuentaInversion"	
	,"OrigenCtaBco"
	,"SocioNegocioCtaBco"
	,"DestinoCtaBco"
	,"IdEstado"
	,"HabilitaCuentaOrigen"
FROM "BSCL.APV.BD::APV.OrigenDestinoTransicion";

GRANT SELECT ON "BSCL.APV.BD.Vistas::APV.ConsultarOrigenDestinoTransicion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
