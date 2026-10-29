SET SCHEMA "BSCL"; 
 GO
DROP VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCentroCosto"; 
 GO
CREATE VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCentroCosto" AS
	SELECT
		 CEN."ProductoConcepto"				AS "IdProductoConcepto"
		,TPR."Nombre"						AS "ProductoConcepto"
		 
		,"Relacionado"						AS "IdRelacionado"
		,CASE CEN."Relacionado"		
			WHEN 'CL' THEN 'CLIENTE' 
			WHEN 'CP' THEN 'CUENTA PROPIA'
		END 								AS "Relacionado"
		,CEN."IdBolsa"						AS "IdBolsa"
		,BOL."Nombre"						AS "Bolsa"
		,"TipoCentro"						AS "TipoCentro"
		,"Business"							AS "Business"
		,"Vehiculo"							AS "Vehiculo"
		,"CentroBeneficio"					AS "CentroBeneficio"
		,"Tipo"								AS "Tipo"           
	FROM "BSCL.Contabilidad.BD::ITZ.CentroCosto"		AS CEN
	LEFT  JOIN "BSCL.Plataforma.BD::INS.TipoProducto"	AS TPR
	ON (TPR."IdTipoProducto" = CEN."ProductoConcepto")
	LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa"			AS BOL
	ON (CEN."IdBolsa" = BOL."IdBolsa")
	

GO			
GRANT SELECT ON "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarCentroCosto" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

