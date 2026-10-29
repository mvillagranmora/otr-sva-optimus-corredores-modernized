SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::REP.ConsultarInstrumentosSubyacentes";
GO

CREATE VIEW "BSCL.Custodia.BD.Vistas::REP.ConsultarInstrumentosSubyacentes" AS 
SELECT	 distinct

		 Sub."FolioCarteraRelacionada"
		,Sub."FolioSubyacente"					
		,Sub."IdTipoInstrumento" 				
		,Sub."IdInstrumento" 					
		,Sub."Nemotecnico"						
		,Sub."IdMonedaInstrumento" 				
		,Sub."IdClasificacionTipoInstrumento" 	
--		,Sub."IdBovedaOperacionRelacionada"							
		,Sub."IdEmisor"							
		,Sub."IdTipoReajuste"
		,Sub."IdEstado"
		,max(Sub."IdSubyacente") as "IdSubyacente"

  FROM "BSCL.Custodia.BD::CUS.Subyacente" as Sub
 
  group by 		
		 Sub."FolioCarteraRelacionada"
		,Sub."FolioSubyacente"					
		,Sub."IdTipoInstrumento" 				
		,Sub."IdInstrumento" 					
		,Sub."Nemotecnico"						
		,Sub."IdMonedaInstrumento" 				
		,Sub."IdClasificacionTipoInstrumento" 	
--		,Sub."IdBovedaOperacionRelacionada"							
		,Sub."IdEmisor"							
		,Sub."IdTipoReajuste"
		,Sub."IdEstado"
  
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::REP.ConsultarInstrumentosSubyacentes"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
