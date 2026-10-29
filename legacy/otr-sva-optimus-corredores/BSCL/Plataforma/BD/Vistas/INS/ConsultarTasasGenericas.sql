SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTasasGenericas"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTasasGenericas"
AS
SELECT 
	"IdTasaGenerica"		as "IdTasaGenerica"		
	,"IdTipoinstrumento"	as "IdTipoinstrumento"
	,"Tipoinstrumento"		as "Tipoinstrumento"	 
	,"IdTipoReajuste"		as "IdTipoReajuste"		
	,"IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
	,"Clasificacion" 		as "Clasificacion"
	,"Plazo"	 			as "Plazo"	
    ,"TasaCliente"	 		as "TasaCliente"					
    ,"TasaCorredora" 		as "TasaCorredora"	 				
    ,"FechaUltimaModificacion"	as "FechaUltimaModificacion"	
	FROM  "BSCL.Plataforma.BD::INS.TasasGenericas"
	
	
WITH READ ONLY;
go
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarTasasGenericas" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

	
GO