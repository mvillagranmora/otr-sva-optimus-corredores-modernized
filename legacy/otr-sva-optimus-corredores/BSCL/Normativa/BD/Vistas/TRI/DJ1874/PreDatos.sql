SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1874.PreDatos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1874.PreDatos" (
    "ID"
    ,"IdDocumento"
    ,"VboUsuario"
    ,"Observaciones"
    ,"FuenteDatos"
   	,"Numero"								
	,"RutEmisor"									
	,"RutInversionista"							
	,"TipoContribuyente"							
	,"NemotecnicoSVS"	
	,"Nemotecnico" 
	,"FechaOperacion"							
	,"MonedaUnidadReajuste"
	,"MonedaUnidadReajusteDescripcion"
	,"NumeroCuentaDCV"							
	,"MovimientoSaldoInicial"					
	,"MovimientoAdquision"						
	,"MovimientoEnajenacion"						
	,"MovimientoRescateAnticipado"				
	,"MovimientoSaldoFinal"						
	,"SaldoFinalNominal"							
	,"InteresDevengadoMonedaUnidadReajuste"		
	,"InteresDevengadoPesos"						
	,"ResultadoEnajenacionMonedaUnidadReajuste"	
	,"ResultadoEnajenacionPesos"					
	,"RetencionMonedaUnidadReajuste"				
	,"RetencionPesos"																					
	,"TipoDocumento"
	,"NumeroDocumento"
	,"FechaDocumento"
	,"IdBaseDocumento"
	,"IdCliente"
	,"IdPortafolio"
	,"TipoTransaccion"
	,"IdTipoOperacion"
	,"IdTipoProducto"
	,"IdClasificacionTipoInstrumento"
	,"IdTipoInstrumento"
	,"IdInstrumento"
	,"IdMonedaInstrumento"
	,"IdMonedaEmisionInstrumento"
	,"IdEmisor"
	,"Cantidad"
	,"Tasa"
	,"Precio"
	,"InsFechaVencimiento"
	,"InsFechaEmision"
	,"FechaLiquidacion"
	,"FolioCartera"
	,"ValIndicadorValorizado"
	,"ValResultado"
	,"ValMontoMonedaInstrumento"		
	,"ValMontoDevengadoAMonedaInstrumento"
	,"ValMontoMonedaSistema"				
	,"ValMontoDevengadoAMonedaSistema"	
	,"ValMontoMonedaInstrumentoFechaVenta"
	,"ValMontoMonedaSistemaFechaVenta"
	,"FechaAdquisicion"
	,"RetencionMonedaUnidadReajuste4porciento"				
	,"RetencionPesos4porciento"
) AS
SELECT
    a."ID"
    ,a."IdDocumento"
    ,a."VboUsuario"
    ,COALESCE(VAL."CodigoValidacion", a."Observaciones") AS "Observaciones"
    ,a."FuenteDatos"
    ,a."Numero"								
	,a."RutEmisor"									
	,a."RutInversionista"							
	,a."TipoContribuyente"							
	,Case 
	    When (a."NemotecnicoSVS" IS NULL OR a."NemotecnicoSVS" = '') AND a."FuenteDatos" = 'EXTERNA'  then a."Nemotecnico" 
	    Else a."NemotecnicoSVS"
	 End As "NemotecnicoSVS"
	,a."Nemotecnico" 
	,a."FechaOperacion"							
	,a."MonedaUnidadReajuste"
	,b."Descripcion" as "MonedaUnidadReajusteDescripcion"							
	,a."NumeroCuentaDCV"							
	,a."MovimientoSaldoInicial"					
	,a."MovimientoAdquision"						
	,a."MovimientoEnajenacion"						
	,a."MovimientoRescateAnticipado"				
	,a."MovimientoSaldoFinal"						
	,a."SaldoFinalNominal"							
	,a."InteresDevengadoMonedaUnidadReajuste"		
	,a."InteresDevengadoPesos"						
	,a."ResultadoEnajenacionMonedaUnidadReajuste"	
	,a."ResultadoEnajenacionPesos"					
	,a."RetencionMonedaUnidadReajuste"				
	,a."RetencionPesos"																													
	,a."TipoDocumento"
	,a."NumeroDocumento"
	,a."FechaDocumento"
	,a."IdBaseDocumento"
	,a."IdCliente"
	,a."IdPortafolio"
	,a."TipoTransaccion"
	,a."IdTipoOperacion"
	,a."IdTipoProducto"
	,a."IdClasificacionTipoInstrumento"
	,Case 
	    When (a."IdTipoInstrumento" = '' OR a."IdTipoInstrumento" IS NULL) AND a."FuenteDatos" = 'EXTERNA' AND ins."IdTipoInstrumento" IS NOT NULL  then ins."IdTipoInstrumento"
	    Else COALESCE(a."IdTipoInstrumento", '')
	 End As "IdTipoInstrumento"
	,Case 
	    When (a."IdInstrumento" = '0' OR a."IdInstrumento" IS NULL) AND a."FuenteDatos" = 'EXTERNA' AND ins."IdInstrumento" IS NOT NULL  then ins."IdInstrumento"
	    Else COALESCE(a."IdInstrumento", 0)
	 End As "IdInstrumento"
	,Case 
	    When (a."IdMonedaInstrumento" = '' OR a."IdMonedaInstrumento" IS NULL) AND a."FuenteDatos" = 'EXTERNA' AND ins."IdMoneda" IS NOT NULL  then ins."IdMoneda"
	    Else COALESCE(a."IdMonedaInstrumento", '')
	 End As "IdMonedaInstrumento"
	,Case 
	    When (a."IdMonedaEmisionInstrumento" = '' OR a."IdMonedaEmisionInstrumento" IS NULL) AND a."FuenteDatos" = 'EXTERNA' AND ins."IdMonedaEmision" IS NOT NULL  then ins."IdMonedaEmision"
	    Else COALESCE(a."IdMonedaEmisionInstrumento", '')
	 End As "IdMonedaEmisionInstrumento"
	,a."IdEmisor"
	,a."Cantidad"
	,Case 
	    When a."FuenteDatos" = 'EXTERNA' AND T."Tasa_Facial" IS NOT NULL  then  T."Tasa_Facial"
	    Else COALESCE(a."Tasa", 0)
	 End As "Tasa"
	,a."Precio"
	,a."InsFechaVencimiento"
	,a."InsFechaEmision"
	,a."FechaLiquidacion"
	,a."FolioCartera" 
	,a."ValIndicadorValorizado"	
	,a."ValResultado"
	,a."ValMontoMonedaInstrumento"		
	,a."ValMontoDevengadoAMonedaInstrumento"
	,a."ValMontoMonedaSistema"				
	,a."ValMontoDevengadoAMonedaSistema"	
	,a."ValMontoMonedaInstrumentoFechaVenta"
	,a."ValMontoMonedaSistemaFechaVenta"	
	,a."FechaAdquisicion"
	,a."RetencionMonedaUnidadReajuste4porciento"		 -- complemento 4%		
	,a."RetencionPesos4porciento"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1874" a
LEFT  JOIN "BSCL.Normativa.BD::DOC.Diccionario" b    on b."Tipo"='DJ1874MonedaUnidadReajuste' and a."MonedaUnidadReajuste"=b."Codigo"
LEFT  JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 		AS ins ON ins."Nemotecnico" 			= a."Nemotecnico"
INNER JOIN (SELECT DISTINCT "Instrumento", MAX("Tasa_Facial") "Tasa_Facial"
            FROM "BSCL.Normativa.BD::TRI.InstrumentosArticulo104" WHERE "Periodo" >= 2019 GROUP BY "Instrumento"
            ) T ON trim(a."Nemotecnico") = trim(T."Instrumento")
LEFT JOIN (
           	SELECT "CodigoValidacion", "IdDocumento" ,"IdCliente"
			,"Numero" ,"RutEmisor" ,"RutInversionista"							
			,"TipoContribuyente"	,"NemotecnicoSVS"	,"FechaOperacion"				
			,"MonedaUnidadReajuste"			,"NumeroCuentaDCV"	,"IdPortafolio"
			,"IdTipoOperacion"	,"IdTipoProducto"	,"IdClasificacionTipoInstrumento"
			,"IdInstrumento"	,"IdMonedaInstrumento"	,"IdMonedaEmisionInstrumento"
			,"IdEmisor"	,"Cantidad"	,"Tasa"	,"Precio"	,"FechaLiquidacion"		
			,"FechaAdquisicion","IdTipoInstrumento"
			FROM (
			    SELECT 
			        R."CodigoValidacion" ,DT."IdDocumento",DT."IdCliente"
			        ,DT."Numero",DT."RutEmisor"	,DT."RutInversionista"	,DT."TipoContribuyente"							
					,DT."NemotecnicoSVS" ,DT."FechaOperacion" 				
					,DT."MonedaUnidadReajuste"	,DT."NumeroCuentaDCV"	,DT."IdPortafolio"
					,DT."IdTipoOperacion",DT."IdTipoProducto",DT."IdClasificacionTipoInstrumento"
					,DT."IdEmisor",DT."Cantidad",DT."Precio",DT."FechaLiquidacion"		
					,DT."FechaAdquisicion",DT."IdTipoInstrumento",DT."IdInstrumento",DT."IdMonedaInstrumento"
					,DT."IdMonedaEmisionInstrumento" ,DT."Tasa"
			        ,ROW_NUMBER() OVER (
			            PARTITION BY DT."RutEmisor" 
			            ORDER BY R."ID" ASC
			        ) AS rn
			    FROM "BSCL.Normativa.BD::TRI.ResumenValidacion" AS R
			    INNER JOIN "BSCL.Normativa.BD::TRI.DatosDJ1874" AS DT         ON DT."IdDocumento" = R."IdDocumento"
			    INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" AS DV ON DV."IdResumenValidacion" = R."ID" AND DV."IdDetalleDocumento" = DT."ID" 
			    LEFT  JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 		AS ins ON ins."Nemotecnico" 			= DT."Nemotecnico"
				INNER JOIN (SELECT DISTINCT "Instrumento", MAX("Tasa_Facial") "Tasa_Facial"
	            FROM "BSCL.Normativa.BD::TRI.InstrumentosArticulo104" WHERE "Periodo" >= 2019 GROUP BY "Instrumento"
	            ) B ON trim(DT."Nemotecnico") = trim(B."Instrumento")
			) AS t
			WHERE rn = 1
        ) AS VAL ON  a."IdDocumento" = VAL."IdDocumento" AND a."IdCliente" = VAL."IdCliente"
         AND a."Numero" =VAL."Numero" AND a."RutEmisor" = VAL."RutEmisor" AND a."RutInversionista" = VAL."RutInversionista" AND a."TipoContribuyente" = VAL."TipoContribuyente" 
        AND a."NemotecnicoSVS" = VAL."NemotecnicoSVS" AND a."FechaOperacion" = VAL."FechaOperacion" AND a."MonedaUnidadReajuste" = VAL."MonedaUnidadReajuste"
        AND a."NumeroCuentaDCV" = VAL."NumeroCuentaDCV" AND a."IdPortafolio" = VAL."IdPortafolio" AND a."IdTipoOperacion" = VAL."IdTipoOperacion" AND a."IdTipoProducto" = VAL."IdTipoProducto"
        AND a."IdClasificacionTipoInstrumento" = VAL."IdClasificacionTipoInstrumento" 
        AND COALESCE(a."IdTipoInstrumento",'') = COALESCE(VAL."IdTipoInstrumento",'')
        AND COALESCE(a."IdInstrumento",0) = COALESCE(VAL."IdInstrumento",0) 
        AND COALESCE(a."IdMonedaInstrumento",'') =COALESCE(VAL."IdMonedaInstrumento",'') 
        AND COALESCE(a."IdMonedaEmisionInstrumento",'') =COALESCE(VAL."IdMonedaEmisionInstrumento",'')         
       	AND COALESCE(a."Tasa",0) = COALESCE(VAL."Tasa",0) 
        AND a."IdEmisor" = VAL."IdEmisor" AND a."Cantidad" = VAL."Cantidad"  AND a."Precio" = VAL."Precio" AND a."FechaLiquidacion" = VAL."FechaLiquidacion" AND a."FechaAdquisicion" = VAL."FechaAdquisicion" 

WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1874.PreDatos" TO bcs_desarrollador WITH GRANT OPTION;