SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1874.DatosValorizar";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1874.DatosValorizar" (
   
	"Version",
	"IdEmpresa",
	"FuenteDatos",
	"IdValorizacion",
	"OrigenProcesamiento",
	"FechaValorizacion",
	
	"FechaOperacion", 						
	"FechaVencimientoOperacion",    		
	"PlazoEnDiasOperacion",							
	
	"IdClasificacionTipoInstrumento", 		
	"IdTipoInstrumento",					
	"IdInstrumento",
	"Nemotecnico",
	"InsFechaVencimiento",
	"InsFechaEmision",
	"IdMonedaInstrumento",
	"IdMonedaEmisionInstrumento",
	"IdTipoReajusteInstrumento",
	"Emisor",								
	"IdCondicionDeLiquidacion",				
	"FechaDeLiquidacion",
	"Cantidad",
	"TasaCompra",
	"PrecioCompra",
	--"ValorRescate",
	
	"PrecioSubyacente",  
	"TasaSubyacente",  	 
	"PrecioGarantia",  	
	"TasaGarantia",  	
	"TasaTIF",  		
	"IdPortafolio",		
	"TablaOrigen",				
	"IdTipoOperacion" 

) AS
SELECT
	DOC."Version",
	DOC."IdEmpresa",
	DJ."FuenteDatos",
	DJ."ID" AS "IdValorizacion",
	'MESA_UI' AS "OrigenProcesamiento",
	TO_VARCHAR (TO_date(DJ."FechaAdquisicion"))  AS "FechaValorizacion",
	-----------------------------------------------------------------------------------------plazo
	TO_VARCHAR (TO_date(DJ."FechaOperacion"))  AS "FechaOperacion", 						--Solo para operaciones a plazo
	NULL AS "FechaVencimientoOperacion",    		--Solo para operaciones a plazo
	0  AS "PlazoEnDiasOperacion",				--Solo para operaciones a plazo		
	-----------------------------------------------------------------------------------------categoria
	DJ."IdClasificacionTipoInstrumento", 		--RV/RF/IF
	DJ."IdTipoInstrumento",						--PAC/AUT/DEP
	DJ."IdInstrumento",
	-----------------------------------------------------------------------------------------instrumento
	DJ."Nemotecnico" AS "Nemotecnico",
	TO_VARCHAR (TO_date(DJ."InsFechaVencimiento")) AS"InsFechaVencimiento",
	TO_VARCHAR (TO_date(DJ."InsFechaEmision")) 	AS"InsFechaEmision",
	DJ."IdMonedaInstrumento",
	DJ."IdMonedaEmisionInstrumento",
	DJ."MonedaUnidadReajuste" AS "IdTipoReajusteInstrumento",
	DJ."IdEmisor"  AS"Emisor",								--valorizacionCL
   	-----------------------------------------------------------------------------------------operacion
	'T0' AS"IdCondicionDeLiquidacion",--DJ."IdCondicionDeLiquidacion",				--valorizacionCL
	TO_VARCHAR (TO_date(DJ."FechaLiquidacion")) AS "FechaDeLiquidacion",
	DJ."Cantidad" 	AS "Cantidad",
	DJ."Tasa"		AS "TasaCompra",
	DJ."Precio"		AS "PrecioCompra",
	--DJ."ValorRescate" 	AS "ValorRescate",
	-----------------------------------------------------------------------------------------garantias
	NULL  AS "PrecioSubyacente",  		 		-- PUEDE SER NULL 
	NULL  AS "TasaSubyacente",  		 		-- PUEDE SER NULL 
	NULL  AS "PrecioGarantia",  		 		-- PUEDE SER NULL
	NULL  AS "TasaGarantia",  		 			-- PUEDE SER NULL
	NULL  AS "TasaTIF",  						-- PUEDE SER NULL
	DJ."IdPortafolio" AS "IdPortafolio",		-- PUEDE SER 0L
	'CARTERA' AS "TablaOrigen",					--CARTERA/TRANSITO;		
	DJ."IdTipoOperacion" AS "IdTipoOperacion" 	--COMPAC/VENPAC
FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1874.PreDatos"   AS DJ
     INNER JOIN "BSCL.Normativa.BD::DOC.Documento" DOC ON (DOC."IdDocumento"=DJ."IdDocumento")
 WHERE     DJ."TipoDocumento" IN (1,2,4,6) --AND DJ."IdEmisor"<>''
 AND DJ."ValIndicadorValorizado" <> 'NO'
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1874.DatosValorizar" TO bcs_desarrollador WITH GRANT OPTION;