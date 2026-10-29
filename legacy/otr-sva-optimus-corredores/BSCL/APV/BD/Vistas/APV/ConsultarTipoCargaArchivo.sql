SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::APV.ConsultarTipoCargaArchivo"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::APV.ConsultarTipoCargaArchivo"  AS 

	SELECT "IdEmpresa"
     , "IdTipoCarga"
     , "NombreTipoCarga"
     , "CodigoTipoCarga"
	FROM "BSCL.APV.BD::APV.TipoCarga";	   

GRANT SELECT ON "BSCL.APV.BD.Vistas::APV.ConsultarTipoCargaArchivo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

