SET SCHEMA "BSCL"; 
 GO
DROP VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarClaseEmisor"; 
 GO
CREATE VIEW "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarClaseEmisor" AS
	SELECT
		 CLE."TipoOperacion"					AS	"TipoOperacion"			
		,TIN."Nombre" 							AS	"ClasificacionInstrumento"
		,CLE."ClaseInstrumento"      			AS	"IdClasificacionInstrumento"
		,CLE."TipoBonoIRF"						AS  "IdTipoBonoIRF"
		,CASE CLE."TipoBonoIRF"      			
			WHEN 'SOB' THEN 'BONOS SOBERANOS'
			WHEN 'COR' THEN 'CORPORATIVOS'
			WHEN 'ALL' THEN 'ALL'
			ELSE ''
		END 	  								AS	"TipoBonoIRF"
		,CLE."OrigenInstrumento"				AS  "IdOrigenInstrumento"
		,CASE CLE."OrigenInstrumento"	
			WHEN 'P'THEN 'PERUANO'
			ELSE		 'EXTRANJERO'
		END										AS	"OrigenInstrumento"
		
		,CLE."Plaza"							AS	"IdPlaza"		
		,CASE CLE."Plaza"
			WHEN 'C' THEN 'CAVALI'
			WHEN 'E' THEN 'OTRO'
			ELSE ''
		 END 									AS	"Plaza"

		 ,CLE."CodigoIFRS"						AS	"IdCodigoIFRS"
		,CASE CLE."CodigoIFRS"
			WHEN 'D' THEN 'DISPONIBLE A LA VENTA'
			WHEN 'T' THEN 'TRADING'
			WHEN 'V' THEN 'AL VENCIMIENTO' 
			ELSE ''
		END										AS	"CodigoIFRS"		

		,CLE."RelacionadoGrupo"					AS  "IdRelacionadoGrupo"
		,CASE CLE."RelacionadoGrupo" 
			WHEN 'X' THEN '' 
			ELSE CLE."RelacionadoGrupo" 
		END 									AS  "RelacionadoGrupo"
		
		,CLE."PresenciaBursatil"				AS	"IdPresenciaBursatil"     
		,CASE CLE."PresenciaBursatil"
			WHEN 'X' THEN ''
			ELSE CLE."PresenciaBursatil"
		END 									AS	"PresenciaBursatil"
		
		,CLE."FormadorMercado"					AS	"IdFormadorMercado"
		,CASE CLE."FormadorMercado"
		 	WHEN 'X' THEN ''
		 	ELSE CLE."FormadorMercado"
		 END 									AS	"FormadorMercado"
		,CLE."PactoAndino"      				AS	"IdPactoAndino"
		,CASE CLE."PactoAndino"
			WHEN 'X' THEN ''
			ELSE CLE."PactoAndino"
		END 									AS	"PactoAndino"
		
		,CLE."ClaseEmisor"      				AS	"ClaseEmisor"           
		,CLE."LargoPlazo"       				AS	"LargoPlazo"            
	FROM "BSCL.Contabilidad.BD::ITZ.ClaseEmisor" 										AS CLE
	INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" 					AS TIN
	ON (TIN."IdClasificacionTipoInstrumento" = CLE."ClaseInstrumento")
	
	ORDER BY CLE."TipoOperacion", "ClaseEmisor" ASC 

GO			
GRANT SELECT ON "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarClaseEmisor" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
