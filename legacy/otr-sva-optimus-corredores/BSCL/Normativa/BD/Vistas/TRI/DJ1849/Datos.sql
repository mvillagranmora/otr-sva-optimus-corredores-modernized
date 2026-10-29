SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1849.Datos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1849.Datos" (
    "ID"
    ,"IdDocumento"
    ,"VboUsuario"
    ,"Observaciones"
    ,"FuenteDatos"
    ,"IdCartera"
    ,"FechaOperacion"
   	,"Numero"						
	,"RutEmisor"						
	,"RutTenedor"					
	,"TipoContribuyenteRutTenedor"	
	,"NemotecnicoSVS"				
	,"InteresDevengado"				
	,"RetencionImpuesto"												
	,"TipoDocumento"
	,"NumeroDocumento"
	,"FechaDocumento"
	,"IdBaseDocumento"
	,"IdCliente"
	,"IdPortafolio"
	,"TipoTransaccion"
	,"IdTipoOperacion"
	,"IdTipoProducto"
) AS
SELECT
    a."ID"
    ,a."IdDocumento"
    ,a."VboUsuario"
    ,a."Observaciones"
    ,a."FuenteDatos"
    ,a."IdCartera"
    ,a."FechaOperacion"
    ,a."Numero"						
	,a."RutEmisor"						
	,a."RutTenedor"					
	,a."TipoContribuyenteRutTenedor"	
	,a."NemotecnicoSVS"				
	,a."InteresDevengado"				
	,a."RetencionImpuesto"																				
	,a."TipoDocumento"
	,a."NumeroDocumento"
	,a."FechaDocumento"
	,a."IdBaseDocumento"
	,a."IdCliente"
	,a."IdPortafolio"
	,a."TipoTransaccion"
	,a."IdTipoOperacion"
	,a."IdTipoProducto"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1849" a
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1849.Datos" TO bcs_desarrollador WITH GRANT OPTION;
