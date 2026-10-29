SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.PreOrigen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.PreOrigen" (

		  "IdEmpresa"
		, "RUTContraparte2"
		, "LEIContraparte2"
		, "NombreContraparte2"
		, "FechaNegociacion"
		, "HoraNegociacion"
		, "Accion"
		--------------------
		, "FolioOperacion"
		, "IdOperacion"
		, "IdCartera"
		, "FechaLiquidacion"
		, "TipoCambioCierre"
		, "IdCliente"
		, "IdTipoOperacion"
		, "Nemo"
		, "IdMonedaPrincipal" 
		, "IdMonedaSecundaria"
		, "MontoMonedaPrincipal"
		, "MontoMonedaSecundaria"
		, "RUTContraparte1"
		, "FechaCondicionesGenerales"
		, "HoraCondicionesGenerales"
		, "FechaRecibimos"
		, "FechaEntregamos"
		, "LEIContraparte1"
		, "NombreContraparte1"
		, "CodigoOperacionCambio"
		, "PaisMonedaExtranjera"
		, "AfectaDerivados"
		, "TipoOrigen"
		, "PlataformaNegociacion"		
		, "IdPersona"
		, "IdPais"
		, "OperacionEmpresaRelacionada"
		, "Modalidad"
) AS

 
	SELECT 	  DAT."IdEmpresa"
			, DAT."RUTContraparte2"
			, DAT."LEIContraparte2"
			, DAT."NombreContraparte2"
			, DAT."FechaNegociacion"
			, DAT."HoraNegociacion"
			, DAT."Accion"
			, DAT."FolioOperacion"
			, DAT."IdOperacion"
			, DAT."IdCartera"
			, DAT."FechaLiquidacion"
			, DAT."TipoCambioCierre"
			, DAT."IdCliente"
			, DAT."IdTipoOperacion"
			, DAT."Nemo"
			, DAT."IdMonedaPrincipal" 
			, DAT."IdMonedaSecundaria"
			, DAT."MontoMonedaPrincipal"			
			, DAT."MontoMonedaSecundaria"
			, DAT."RUTContraparte1"
			, DAT."FechaCondicionesGenerales"
			, DAT."HoraCondicionesGenerales"
			, DAT."FechaRecibimos"
			, DAT."FechaEntregamos"
			, DAT."LEIContraparte1"
			, DAT."NombreContraparte1"
			, DAT."CodigoOperacionCambio"
			, DAT."PaisMonedaExtranjera"
			, COALESCE(DAT."AfectaDerivados",'N') AS "AfectaDerivados"
			, DAT."TipoOrigen"
			, DAT."PlataformaNegociacion"
			, DAT."IdPersona"
			, COALESCE(DIR."IdPais",44) AS "IdPais"
			, CASE WHEN COALESCE(REL."IdPersona",0)>0 THEN 'S' ELSE 'N' END  AS "OperacionEmpresaRelacionada"
			, DAT."Modalidad"
			FROM
				(
					    SELECT 	
			 	 		 	FWD."IdEmpresa" 										AS  "IdEmpresa",
						    COALESCE(FWD."RutCliente",'') 							AS	"RUTContraparte2",
						    COALESCE(LEI."ValorIndicador",'')						AS  "LEIContraparte2",
						    SUBSTRING(FWD."NombreCliente",1,50) 					AS  "NombreContraparte2",
						    CASE FWD."Estado" 	WHEN 'LIQ' THEN TO_DATE(OPE."FechaLiquidacion")
											    WHEN 'ANT' THEN TO_DATE(OPE."FechaLiquidacion")
											    WHEN 'VCD' THEN TO_DATE(OPE."FechaVencimiento")
											    ELSE TO_DATE(FWD."FechaOperacion")	
						    
							END						    							AS	"FechaNegociacion",
							CASE FWD."Estado" 	WHEN 'LIQ' THEN TO_CHAR(OPE."FechaLiquidacion",'HH24:MI:SS')
											    WHEN 'ANT' THEN TO_CHAR(OPE."FechaLiquidacion",'HH24:MI:SS')
											    WHEN 'VCD' THEN TO_CHAR(OPE."FechaVencimiento",'HH24:MI:SS')
											    ELSE FWD."HoraOperacion"	

						    END 													AS	"HoraNegociacion",
						    FWD."Accion"											AS  "Accion",
							FWD."FolioOperacion"  									AS	"FolioOperacion",
							FWD."IdOperacion"										AS  "IdOperacion",
							FWD."IdCartera" 										AS 	"IdCartera",
							FWD."FechaLiquidacion"									AS  "FechaLiquidacion",
							FWD."TipoCambioCierre"									AS  "TipoCambioCierre",
							FWD."IdCliente"											AS 	"IdCliente",
							FWD."IdTipoOperacion"									AS  "IdTipoOperacion",
							FWD."Nemotecnico"										AS  "Nemo",
							FWD."IdMonedaPrincipal"									AS 	"IdMonedaPrincipal",
							FWD."IdMonedaSecundaria"								AS 	"IdMonedaSecundaria",
							FWD."MontoMonedaPrincipal"								AS 	"MontoMonedaPrincipal",
							FWD."MontoMonedaSecundaria"								AS 	"MontoMonedaSecundaria",
							COALESCE(EMP."DocumentoIdentidad",'')					AS  "RUTContraparte1",
							COALESCE(TO_DATE(ANE."FechaModificacion"),'0001-01-01') AS  "FechaCondicionesGenerales",
							COALESCE(TO_CHAR(ANE."FechaModificacion",'HH24:MI:SS'),'00:00:00') AS  "HoraCondicionesGenerales",
							FWD."FechaRecibimos" 									AS "FechaRecibimos",
							FWD."FechaEntregamos" 									AS "FechaEntregamos",
							LTRIM(RTRIM(COALESCE(LEI1."Valor",''))) 				AS "LEIContraparte1",
							SUBSTRING(EMP."RazonSocial",1,50) 						AS "NombreContraparte1",
							CASE FWD."Accion" WHEN 'COMPRA' THEN '20050' ELSE '10050' END AS "CodigoOperacionCambio",
							COALESCE(PAIS."Valor",'')								AS "PaisMonedaExtranjera",
							'S' 													AS "AfectaDerivados",
							'FWD' 													AS "TipoOrigen",
							RANK() OVER(PARTITION BY FWD."FolioOperacion", FWD."FechaOperacion" ORDER BY "IdCartera" DESC) AS "Rank",
							'OTC'													AS "PlataformaNegociacion",
							PER."IdPersona",
							FWD."ModalidadLiquidacionValor"	AS "Modalidad"
						  FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD"	AS FWD
					  		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" 			AS PER   				ON (PER."IdPersona" = FWD."IdPersona")
					  		INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" AS EMP				ON (FWD."IdEmpresa" = EMP."IdEmpresa")
					  		INNER JOIN "BSCL.Mesa.BD::MES.Operacion" AS OPE									ON (OPE."IdOperacion" = FWD."FolioOperacion" AND OPE."IdTipoProducto" = 'MEFW')
				  			LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" AS LEI 	ON (LEI."IdPersona" = PER."IdPersona")
						      AND LEI."IdUso" = 'IND' AND LEI."CodigoTipoIndicador" = 'LEI'
						    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAnexo" ANE ON (ANE."IdCliente"=FWD."IdCliente"	 AND ANE."IdTipoDocumento"='CONTRATOCONDICIONESGRALES' AND  ANE."IdEstado"='ACT')
						    LEFT JOIN "BSCL.Normativa.BD::NOR.Configuracion" LEI1 			ON (LEI1."CodigoParametro"='BCC_REPO2P1_LEICONTRAPARTE1'  AND LEI1."IdNormativa"='REPO2P1')
							LEFT JOIN "BSCL.Normativa.BD::NOR.Configuracion" PAIS 			ON (PAIS."CodigoParametro"='BCC_REPO2P1_PAISMONEDAEXTRANJERA'  AND PAIS."IdNormativa"='REPO2P1')
	 					  WHERE 
						  FWD."Estado" IN ('ANT','VCD','LIQ')
						  
						  UNION ALL 
						  
						   SELECT 	
			 	 		 	SPT."IdEmpresa" 										AS  "IdEmpresa",
						    COALESCE(IDENT."DocumentoIdentidad",'')					AS	"RUTContraparte2",
						    COALESCE(LEI."ValorIndicador",'')						AS  "LEIContraparte2",
						    SUBSTRING( 	(CASE PER."Tipo" 	WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' ||PER."ApellidoMaterno" 
															ELSE PER."RazonSocial" END),1,50) 					AS  "NombreContraparte2",
						    TO_DATE(SPT."FechaOperacion")							AS	"FechaNegociacion",
						    TO_NVARCHAR(ADD_SECONDS(SPT."FechaOperacion",Conf."Valor"),'HH24:MI:SS') AS "HoraNegociacion",
						    TP."Accion"												AS  "Accion",
							SPT."IdOperacionSpotOrigen"  							AS	"FolioOperacion",
							SPT."IdOperacionSpotOrigen"								AS  "IdOperacion",
							0				 										AS 	"IdCartera",
							SPT."FechaLiquidacion"									AS  "FechaLiquidacion",
							SPT."TipoCambioCierre"									AS  "TipoCambioCierre",
							SPT."IdCliente"											AS 	"IdCliente",
							SPT."IdTipoOperacion"									AS  "IdTipoOperacion",
							INS."Nemotecnico"										AS  "Nemo",
							SPT."IdMonedaPrincipal"									AS 	"IdMonedaPrincipal",
							SPT."IdMonedaSecundaria"								AS 	"IdMonedaSecundaria",
							SPT."MontoMonedaPrincipal"								AS 	"MontoMonedaPrincipal",
							SPT."MontoMonedaSecundaria"								AS 	"MontoMonedaSecundaria",
							COALESCE(REP."DocumentoIdentidad",'')					AS  "RUTContraparte1",
							'0001-01-01'											AS  "FechaCondicionesGenerales",
							'00:00:00'												AS  "HoraCondicionesGenerales",
							TO_DATE(SPT."FechaRecibimos") 							AS "FechaRecibimos",
							TO_DATE(SPT."FechaEntregamos") 							AS "FechaEntregamos",
							LTRIM(RTRIM(COALESCE(LEI1."Valor",''))) 				AS "LEIContraparte1",
							SUBSTRING(EMP."RazonSocial",1,50) 						AS "NombreContraparte1",
							CAST(SPT."CodigoComercio" as NVARCHAR(10))				AS "CodigoOperacionCambio",
							COALESCE(PAIS."Valor",'')								AS "PaisMonedaExtranjera",
							SPT."AfectoADerivado"									AS "AfectaDerivados",
							'SPT' 													AS "TipoOrigen",
							1 AS "Rank",
							COALESCE(SPT."IdPlataformaNegociacion",'OTC')			AS "PlataformaNegociacion",
							PER."IdPersona",
							''	AS "Modalidad"
						  	  FROM  "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" AS SPT
						  	  	INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" AS EMP				ON (SPT."IdEmpresa" = EMP."IdEmpresa")
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
							    LEFT JOIN "BSCL.Normativa.BD::NOR.Configuracion" LEI1 		ON (LEI1."CodigoParametro"='BCC_REPO2P1_LEICONTRAPARTE1'  AND LEI1."IdNormativa"='REPO2P1')
								LEFT JOIN "BSCL.Normativa.BD::NOR.Configuracion" PAIS 		ON (PAIS."CodigoParametro"='BCC_REPO2P1_PAISMONEDAEXTRANJERA'  AND PAIS."IdNormativa"='REPO2P1')
							  WHERE 
							  SPT."Estado"='VIG' AND SPT."EstadoConfirmacion"='CNF'

							
			     ) AS DAT
			     LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS DIR ON (DIR."IdPersona" = DAT."IdPersona" AND  DIR."IdEstado"='ACT' AND DIR."Predeterminado"='UI_SI')
			     LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" 	  AS REL ON (REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='NCG380PER' AND REL."CodigoValorIndicador" ='MIEMBRO' 
			     																						 AND REL."IdPersona" = DAT."IdPersona")
			     WHERE DAT."Rank" = 1 
  

WITH read ONLY;	
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.PreOrigen" TO bcs_desarrollador WITH GRANT OPTION;