SET SCHEMA "BSCL";
go
DROP VIEW "BSCL.Custodia.BD.Vistas::SIM.ConsultarInstrumentosSubyacentes";
go

CREATE VIEW "BSCL.Custodia.BD.Vistas::SIM.ConsultarInstrumentosSubyacentes" AS 
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
go
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::SIM.ConsultarInstrumentosSubyacentes"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
go
