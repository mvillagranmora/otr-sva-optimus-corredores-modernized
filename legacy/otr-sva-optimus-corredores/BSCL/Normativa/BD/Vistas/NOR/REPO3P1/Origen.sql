SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.Origen";
		  
CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.Origen"(
	"ID",
	"IdEmpresa",
	"IdDocumento",
	"FuenteDatos",
    "Estado",
    "EstadoSICAM",
    "SICAM",
--camos layout    
    "TipoRegistro",
    "RUTReportante",
    "FechaOperacion",
    "HoraOperacion",
    "TipoOperacion",
	"EventoReporte",
	"Identificador",
	"RUTContraparte",
	"LEIContraparte",
	"NombreContraparte",
	"PlataformaNegociacion",
	"MontoMonedaComprada",
	"MontoMonedaVendida",
	"TipoCambio",
--campos de control interno especifico
	"IdCliente",
	"IdOperacion",
	"IdTipoOperacion",
	"Nemo",
	"IdMonedaPrincipal",
	"IdMonedaSecundaria",
	"Observaciones"
		

	
) AS

 	 		  SELECT 	
 	 		 	0 								AS 	"ID",
 	 		 	SPT."IdEmpresa" 				AS "IdEmpresa",
				0 								AS 	"IdDocumento",
				'' 								AS 	"FuenteDatos",
			    'ACT'							AS	"Estado",
			    ''								AS	"EstadoSICAM",
			    ''								AS	"SICAM",
 	 		  --camos layout    
			    '01'							AS	"TipoRegistro",
			    SPT."RUTReportante"				AS	"RUTReportante",
			    SPT."FechaOperacion"			AS	"FechaOperacion",
			    SPT."HoraOperacion" 			AS	"HoraOperacion",
			    CASE SPT."Accion" WHEN 'COMPRA' THEN '1' ELSE '2' END 	AS 	"TipoOperacion",
				SPT."EventoReporte"				AS "EventoReporte",
				CAST(SPT."IdOperacionSpotOrigen" as NVARCHAR(15)) 	AS "Identificador",
				SPT."RUTContraparte" 			AS "RUTContraparte",
				SPT."LEIContraparte" 			AS "LEIContraparte",
				SPT."NombreContraparte"			AS "NombreContraparte",
				SPT."PlataformaNegociacion"		AS "PlataformaNegociacion",
				CASE SPT."Accion" WHEN 'COMPRA' THEN  SPT."MontoMonedaPrincipal"  ELSE SPT."MontoMonedaSecundaria" END AS "MontoMonedaComprada",
				CASE SPT."Accion" WHEN 'VENTA'  THEN  SPT."MontoMonedaPrincipal"  ELSE SPT."MontoMonedaSecundaria" END AS "MontoMonedaVendida",
				SPT."TipoCambioCierre"			AS "TipoCambio",
			--campos de control interno especifico
				SPT."IdCliente"					AS 	"IdCliente",
				SPT."IdOperacionSpot" 			AS "IdOperacion",
				SPT."IdTipoOperacion"			AS "IdTipoOperacion",
				SPT."Nemotecnico"				AS "Nemo",
				SPT."IdMonedaPrincipal"			AS "IdMonedaPrincipal",
				SPT."IdMonedaSecundaria"		AS "IdMonedaSecundaria",
				'' 								AS "Observaciones"
			FROM(
			  ---OPERACIONES NUEVAS 
			  SELECT '1' AS "EventoReporte",OPE.* 
			  FROM   "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.PreOrigen" AS OPE
				INNER JOIN "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.FechaReporte" 	AS FRE	 ON (TO_DATE(OPE."FechaOperacion") =TO_DATE(FRE."FechaReporte"))
			  WHERE 
		
				  NOT EXISTS	 (SELECT 1 FROM 
				 					"BSCL.Normativa.BD.Vistas::NOR.REPO3P1.Datos" DAT WHERE  DAT."FuenteDatos"='INICIAL' AND  DAT."EstadoSICAM" IN ('ENV','RZD','REC')
				 					AND TO_DATE(OPE."FechaOperacion") =TO_DATE(DAT."FechaOperacion")
				 					AND OPE."RUTContraparte" = DAT."RUTContraparte"
				 					AND CAST(OPE."IdOperacionSpotOrigen" as NVARCHAR(15))= DAT."Identificador"
								 )
			  ---OPERACIONES MODIFICADAS 
			  UNION ALL
			  SELECT '2' AS "EventoReporte",OPE.* 
			  FROM   "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.PreOrigen" AS OPE
				INNER JOIN "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.FechaReporte" 	AS FRE	 ON (TO_DATE(OPE."FechaOperacion") =TO_DATE(FRE."FechaReporte"))
			  WHERE 
				   EXISTS	 (SELECT 1 FROM 
				 					"BSCL.Normativa.BD.Vistas::NOR.REPO3P1.Datos" DAT WHERE  DAT."FuenteDatos"='INICIAL' AND  DAT."EstadoSICAM" IN ('ENV','RZD','REC')
				 					AND TO_DATE(OPE."FechaOperacion") =TO_DATE(DAT."FechaOperacion")
				 					AND OPE."RUTContraparte" = DAT."RUTContraparte"
				 					AND CAST(OPE."IdOperacionSpotOrigen" as NVARCHAR(15))= DAT."Identificador"
				 					AND (OPE."HoraOperacion" <> DAT."HoraOperacion" 
				 						OR 
				 						 OPE."NombreContraparte" <> DAT."NombreContraparte"	
				 						OR
				 						 OPE."TipoCambioCierre" <> DAT."TipoCambio"
				 						OR 
				 						 OPE."MontoMonedaPrincipal" <>  (CASE OPE."Accion" WHEN 'COMPRA' THEN "MontoMonedaComprada" ELSE "MontoMonedaVendida"  END) 
				 						OR 
				 						 OPE."MontoMonedaSecundaria" <> (CASE OPE."Accion" WHEN 'COMPRA' THEN "MontoMonedaVendida"  ELSE "MontoMonedaComprada" END)
				 					    )
								 )
							
								 
			    )AS SPT	
			  	     
			     
			     
			     
			     
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.Origen" TO bcs_desarrollador WITH GRANT OPTION;