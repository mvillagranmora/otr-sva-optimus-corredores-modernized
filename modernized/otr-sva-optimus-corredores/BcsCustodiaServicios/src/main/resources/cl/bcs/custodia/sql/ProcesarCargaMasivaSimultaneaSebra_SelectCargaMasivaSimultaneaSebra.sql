SELECT top 10
     "BSCL.Custodia.BD.Secuencias::CargaSimultaneasSebra".nextval     AS "IdCargaSimultaneasSebra"
	,Portafolio."CodigoPortafolio"									  AS "CodigoPortafolio"
	,'T0'															  AS "IdCondicionLiquidacion"
	,'DCV'															  AS "IdBoveda"
	,CASE WHEN GES_TBCTTRTP."TIPO_OPERAC" = 'C' THEN 'CMSIMU' ELSE 'VNSIMU' END AS "IdConcepto"
	,COALESCE(Instrumento."IdInstrumento",0)						  AS "IdInstrumento"	
	,TRIM((GES_TBCTTRTP."NEMO"))									  AS "Nemotecnico"
	,COALESCE(Instrumento."IdTipoInstrumento",'')					  AS "IdTipoInstrumento"
	,COALESCE(Instrumento."IdEmisor",'') 							  AS "IdEmisor"
	,Instrumento."CorteMinimo"										  AS "CorteMinimo"
	,Instrumento."IdTipoReajuste"									  AS "IdTipoReajuste"
	,CAST(Instrumento."FechaEmision"		AS VARCHAR(10))			  AS "FechaEmision"
	,CAST(Instrumento."FechaVencimiento"	AS VARCHAR(10))			  AS "FechaVencimientoInstrumento"
	,GES_TBCTTRTP."FEC_VCTO_TP"						 				  AS "FechaVencimiento"
	,ABS(GES_TBCTTRTP."CANTIDAD")									  AS "Cantidad"
	,GES_TBCTTRTP."PRECIO"											  AS "Precio"
	,GES_TBCTTRTP."TASA"											  AS "Tasa"
	,GES_TBCTTRTP."MONTO"											  AS "Monto"
	,TO_DECIMAL(0,26,8)												  AS "ValorRescate"
	,''																  AS "NemoGarantizado" --vacio por mientras, al parecer no se ocupa
	,Instrumento."IdMoneda"											  AS "Moneda" --sacarla del java
	,'N'															  AS "EsFungible"
	,'BCS'															  AS "IdBolsa"
	,'SIMULTANEA MIGRADA DE SEBRA'									  AS "Glosa"
	,'VIG'															  AS "IdEstado"
	,''																  AS "ObservacionValidacion"
	,''																  AS "CuentaCustodio" --preguntar Mario, si es que es necesario obtenerlo
	,CASE WHEN GES_TBCTTRTP."TIPO_OPERAC" = 'C' THEN 'COMSIM' ELSE 'VENSIM' END AS "IdTipoOperacion"
	,COALESCE(TipoInstrumento."IdClasificacionTipoInstrumento",'')	AS "ClasificacionInstrumento"
	,0                                                              AS "Plazo"
	,CONCAT(GES_TBCTTRTP.COD_PER,'-01')							 	AS "FechaLiquidacion"
	,CAST(GES_TBCTTRTP."FEC_TRANS"  	AS VARCHAR(10)) 			AS "FechaOperacion"
	,TO_DECIMAL(0,26,8)												AS "TipoCambio"
	,GES_TBPLTBRV2."CANTIDAD" * GES_TBCTTRTP."PRECIO"				AS "MontoContado"
	,ABS(GES_TBCTTRTP."CANTIDAD") * GES_TBCTTRTP."PRECIO"			AS "MontoPlazo"
	,CASE GES_TBCTTRTP."IND_TIT_CUS" WHEN 'N' THEN 'S' ELSE 'N' END AS "EsNominal"
	,UPPER(TRIM(GES_TBCTTRTP."RUT_CLI")) 							AS "DocumentoIdentidad"
	,Cliente."IdCliente"											AS "IdCliente"
	,Persona."IdPersona"											AS "IdPersona"
	,Portafolio."IdPortafolio"										AS "IdPortafolio"
	,CTA."IdCuentaCustodioPortafolio"								AS "IdCuentaCustodioPortafolio" --revisar carga de socio negocio para que haga match
	,TIP."IdInstrumento"											AS "IdInstrumentoSubyacente"
	,TIP."Nemotecnico"												AS "NemotecnicoSubyacente"
		
FROM "BSCL_VIRTUAL"."GES_TBCTTRTP" AS GES_TBCTTRTP
  		 		INNER JOIN (SELECT  MVTP."SEC_MOVTO" AS "SecMovto",	MAX(MVTP."COD_PER")	AS "CodPer" , MAX(MVTP."SEC_MOVTO_CTDO") AS "SecMovtoCtdo"
  				  	   		  FROM  "BSCL_VIRTUAL"."GES_TBCTTRTP" AS MVTP
  					  		 WHERE  TRIM(MVTP."COD_PER")  BETWEEN :CodPerIni AND :CodPerFin
  					  		 GROUP BY MVTP."SEC_MOVTO" ) as Simul
  		 		 		ON 	TRIM(GES_TBCTTRTP."COD_PER")	= TRIM(Simul."CodPer")
  		  				AND GES_TBCTTRTP."SEC_MOVTO" 		= Simul."SecMovto"
 		  		
				LEFT  JOIN 	"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
						ON 	DocumentoIdentidad."DocumentoIdentidad" = UPPER(TRIM(GES_TBCTTRTP."RUT_CLI"))
						
				INNER   JOIN 	"BSCL.SocioNegocio.BD::PER.Persona" AS Persona
						ON 	Persona."IdPersona" 		= DocumentoIdentidad."IdPersona"
						
				INNER  JOIN 	"BSCL.SocioNegocio.BD::CLI.Cliente" AS Cliente
						ON 	Cliente."IdPersona" = Persona."IdPersona"
						
				INNER  JOIN	"BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
						ON 	Portafolio."IdCliente" = Cliente."IdCliente"  AND 
							Portafolio."CodigoPortafolio" = TO_VARCHAR(GES_TBCTTRTP."SEC_RUT_CLI")
							
				LEFT  JOIN 	"BSCL.SocioNegocio.BD::EMP.Empleado" as EmpleadoEjecutivo
						ON 	EmpleadoEjecutivo."IdEmpleado" = Portafolio."IdEmpleadoEjecutivo"
					   
				LEFT  JOIN 	"BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
						ON 	TRIM((Instrumento."Nemotecnico")) = TRIM((GES_TBCTTRTP."NEMO"))
						
				LEFT  JOIN 	"BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
						ON 	TRIM((TipoInstrumento."IdTipoInstrumento")) = TRIM((Instrumento."IdTipoInstrumento")) 
						
						
			    LEFT JOIN 	"BSCL.Custodia.BD::CUS.TipoValorizacionPorTipoInstrumento" AS TipoValorizacionPorTipoInstrumento
						ON 	TRIM((TipoValorizacionPorTipoInstrumento."IdTipoInstrumento")) = TRIM((Instrumento."IdTipoInstrumento")) 

				LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio"			AS CTA
						ON (CTA."IdPortafolio" 	= Portafolio."IdPortafolio"
						AND CTA."IdBoveda" 		= 'BCS')
				LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento"		AS TIP
						ON (TIP."IdTipoOperacion" 	= CASE WHEN GES_TBCTTRTP."TIPO_OPERAC" = 'C' THEN 'COMSIM' ELSE 'VENSIM' END)	
				Left Outer Join "BSCL_VIRTUAL"."GES_TBPLTBRV" GES_TBPLTBRV 
						On (GES_TBPLTBRV."MERCADO"		= GES_TBCTTRTP."MERCADO"		and
							GES_TBPLTBRV."FECHA"		= GES_TBCTTRTP."FEC_TRANS"		and
							GES_TBPLTBRV."FOLIO_TRANS"	= GES_TBCTTRTP."FOLIO_TRANS"	and
							GES_TBPLTBRV."IND_BOLSA"	= GES_TBCTTRTP."IND_BOLSA"		and
							GES_TBPLTBRV."TIPO_OPERAC"	= GES_TBCTTRTP."TIPO_OPERAC")
				Left Outer Join "BSCL_VIRTUAL"."GES_TBPLTBRV" GES_TBPLTBRV2
				On (GES_TBPLTBRV2."MERCADO"		= GES_TBPLTBRV."MERCADO"		and
					GES_TBPLTBRV2."FECHA"		= GES_TBPLTBRV."FECHA"			and
					GES_TBPLTBRV2."FOLIO_TRANS"	= GES_TBPLTBRV."FOLIO_SIMUL"	and
					GES_TBPLTBRV2."IND_BOLSA"	= GES_TBPLTBRV."IND_BOLSA"		and
					GES_TBPLTBRV2."TIPO_OPERAC"	= Case When GES_TBPLTBRV."TIPO_OPERAC" = 'C' Then 'V' Else 'C' End)	
	     	WHERE 	TRIM(GES_TBCTTRTP."COD_PER")	= TRIM(Simul."CodPer")
  		  	  AND 	GES_TBCTTRTP."SEC_MOVTO" 	= Simul."SecMovto"
	     	  AND   GES_TBCTTRTP."FEC_VCTO_TP"  > :FechaVencimiento
			  AND 	GES_TBCTTRTP."CANTIDAD" 	 > 0;