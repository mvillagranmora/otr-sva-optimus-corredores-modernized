SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.PreOrigen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.PreOrigen" (

		  "IdEmpresa"
		, "RUTReportante"
		, "FechaOperacion"
		, "HoraOperacion"
		--------------------
		, "Accion"
		, "IdOperacionSpotOrigen"
		, "RUTContraparte"
		, "LEIContraparte"
		, "NombreContraparte"
		, "TipoCambioCierre"
		, "IdCliente"
		, "IdOperacionSpot"
		, "IdTipoOperacion"
		, "Nemotecnico"
		, "IdMonedaPrincipal" 
		, "IdMonedaSecundaria"
		, "MontoMonedaPrincipal"
		, "MontoMonedaSecundaria"
		, "PlataformaNegociacion"
		
) AS

 
		    SELECT 	

 	 		 	SPT."IdEmpresa" 										AS  "IdEmpresa",
			    COALESCE(REP."DocumentoIdentidad",'') 					AS	"RUTReportante",
			    TO_DATE(SPT."FechaOperacion")							AS	"FechaOperacion",
			    TO_NVARCHAR(ADD_SECONDS(SPT."FechaOperacion",Conf."Valor"),'HH24:MI:SS') AS "HoraOperacion",
			    TP."Accion"												AS  "Accion",
				SPT."IdOperacionSpotOrigen"  							AS	"IdOperacionSpotOrigen",
				COALESCE(IDENT."DocumentoIdentidad",'') 				AS  "RUTContraparte",
				COALESCE(LEI."ValorIndicador",'')						AS  "LEIContraparte",
				CASE PER."Tipo" WHEN 'N' THEN SUBSTRING((PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' ||PER."ApellidoMaterno"),1,50) 
								  ELSE SUBSTRING(PER."RazonSocial",1,50)			END AS "NombreContraparte",
				SPT."TipoCambioCierre"		AS  "TipoCambioCierre",
				SPT."IdCliente"				AS 	"IdCliente",
				SPT."IdOperacionSpot"		AS 	"IdOperacionSpot",
				SPT."IdTipoOperacion"		AS  "IdTipoOperacion",
				INS."Nemotecnico"			AS  "Nemotecnico",
				SPT."IdMonedaPrincipal"		AS 	"IdMonedaPrincipal",
				SPT."IdMonedaSecundaria"	AS 	"IdMonedaSecundaria",
				SPT."MontoMonedaPrincipal"	AS 	"MontoMonedaPrincipal",
				SPT."MontoMonedaSecundaria"	AS 	"MontoMonedaSecundaria",
				COALESCE(SPT."IdPlataformaNegociacion",'OTC')	AS "PlataformaNegociacion"
				
		  FROM   "BSCL.Mesa.BD::SPT.OperacionSpot" AS SPT
				INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" 		AS CLI   ON (CLI."IdCliente" =  SPT."IdCliente")
				INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" 		AS PER   ON (PER."IdPersona" = CLI."IdPersona")
				INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" 	AS INS	 ON (SPT."IdInstrumento" = INS."IdInstrumento")
				LEFT  JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
				LEFT  JOIN 	(
								SELECT E."IdEmpresa",D."DocumentoIdentidad" FROM  "BSCL.Plataforma.BD::PLA.Empresa" AS E 
								INNER JOIN  "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS S ON S."IdSocioDeNegocio" = E."IdSocioDeNegocio"
								INNER JOIN  "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS D ON D."IdPersona" = S."IdPersona" AND D."Predeterminado" ='S'
							) AS REP ON (SPT."IdEmpresa"=REP."IdEmpresa")
				LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TP ON TP."IdTipoOperacion" = SPT."IdTipoOperacion"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" AS LEI ON LEI."IdPersona" = PER."IdPersona"
			      AND LEI."IdUso" = 'IND' AND LEI."CodigoTipoIndicador" = 'LEI' AND LEI."ValorIndicador" <> ''
			    LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS Conf  ON SPT."IdEmpresa"  = Conf."IdEmpresa" AND Conf."CodigoParametro" = ('PLA.SegundosZonaHoraria')
			    LEFT JOIN "BSCL.Normativa.BD::NOR.Configuracion"  AS N 	   ON  N."CodigoParametro"='BCC_REPO3P1_HORAHASTA'  AND N."IdNormativa"='REPO3P1'
			  WHERE 
			  SPT."Estado"='VIG' AND SPT."EstadoConfirmacion"='CNF'
				 AND  SPT."IdMonedaPrincipal" ='USD' AND SPT."IdMonedaSecundaria"='CLP'
				 AND  SPT."FolioOrigenFW"=0 AND SPT."DolarObservado"='N'
   				 AND  TO_NVARCHAR(ADD_SECONDS(SPT."FechaOperacion",Conf."Valor"),'HH24:MI:SS')  <= (CASE  coalesce(TRIM(N."Valor"),'16:00:00')
   				 																					WHEN  '' THEN '16:00:00' 
   				 																					ELSE coalesce(TRIM(N."Valor"),'16:00:00') END)
				
WITH read ONLY;	
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.REPO3P1.PreOrigen" TO bcs_desarrollador WITH GRANT OPTION;