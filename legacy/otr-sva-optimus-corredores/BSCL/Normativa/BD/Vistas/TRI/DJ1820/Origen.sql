SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1820.Origen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1820.Origen"  AS

SELECT 
 		"IdEmpresa","IdCliente"	,"IdPortafolio","IdCartera","FolioOperacion","FechaOperacion","FechaAnticipo","FechaLiquidacion" ,"IdInstrumento","IdTipoOperacion","IdClasificacionTipoInstrumento"
 		,"IdMonedaPrincipal","IdMonedaSecundaria","MontoMonedaPrincipal","MontoMonedaSecundaria","RutCliente","TaxID","CodigoPaisResidencia","CodigoRelacion","CodigoModoContrato","TipoAcuerdoMarco"
 		,"NumeroAcuerdoMarco","FechaSuscripcionAcuerdo","NumeroContrato","FechaSuscripcionContrato" ,"TipoEvento" ,"TipoContrato","Instrumento","ModalidadCumplimiento","CodigoPosicionDeclarante" 								
		,"TipoActivoSubyacente","CodigoActivoSubyacente","OtroActivoSubyacente","TasaActivoSubyacente","TipoSegundoActivoSubyacente","CodigoSegundoActivoSubyacente","OtroSegundoActivoSubyacente"										
		,"TasaSegundoActivoSubyacente","CodigoPrecioFuturo","PrecioFuturo","CodigoMonedaFuturo","CodigoUnidad","MontoNocional","CodigoSegundaUnidad","SegundoMontoNocional","FechaVencimiento"
FROM (
select 
			 FWD."IdEmpresa"
		 	,FWD."IdCliente"
		   	,FWD."IdPortafolio"
		   	,FWD."IdCartera"
		   	,FWD."FolioOperacion"		   	
		   	,FWD."FechaOperacion"
			,FWD."FechaLiquidacion"
			,FWD."FechaAnticipo"
		   	,FWD."IdInstrumento"
		   	,FWD."IdTipoOperacion"
		   	,FWD."IdClasificacionTipoInstrumento"
		   	,FWD."IdMonedaPrincipal"
		   	,FWD."IdMonedaSecundaria"
		   	,FWD."MontoMonedaPrincipal"
			,FWD."MontoMonedaSecundaria"
		    ,FWD."RutCliente"					--08
		    ,TO_VARCHAR('') AS"TaxID"			--10
		    ,COALESCE(HPAIS."ValorDestino", PAIS."CodigoPais") AS"CodigoPaisResidencia" --11
		   	,COALESCE(HREL."ValorDestino",COALESCE(REL."CodigoValorIndicador",TO_VARCHAR('99')))  AS"CodigoRelacion" --12
		    ,TO_VARCHAR('3') AS"CodigoModoContrato"	--13
		    ,TO_VARCHAR('1') AS"TipoAcuerdoMarco"	--14
			,TO_BIGINT ((ltrim(rtrim(substring(COALESCE(replace(PER."DocumentoIdentidad",'.',''),'0'),1,LENGTH(COALESCE(replace(PER."DocumentoIdentidad",'.',''),'0'))-2))))) AS"NumeroAcuerdoMarco" --15
			,COALESCE(TO_DATE(ANE."FechaVigencia"),'0001-01-01') AS"FechaSuscripcionAcuerdo"			--16
			,FWD."IdCartera"	AS"NumeroContrato"													--17
			,FWD."FechaOperacion" AS"FechaSuscripcionContrato"  										--18
			,COALESCE(HEVT."ValorDestino",FWD."Estado") AS"TipoEvento"  --Segun el estado				--19
			 --No esta en Layout																		--20
			,TO_VARCHAR('1') AS"TipoContrato"															--21
			,TO_VARCHAR('') AS"Instrumento" ---si el TipoContrato es 1 siempre es blanco				--22
			,COALESCE(HMOD."ValorDestino",FWD."ModalidadLiquidacionValor") AS"ModalidadCumplimiento" 	--23
			,COALESCE(HPOS."ValorDestino",FWD."IdTipoOperacion") AS"CodigoPosicionDeclarante"
			,TO_VARCHAR('1') AS"TipoActivoSubyacente" ---Moneda											--25
			,COALESCE(HMONPRI."ValorDestino", MONPRI."IdMoneda") AS"CodigoActivoSubyacente"				--26
			,TO_VARCHAR('') AS"OtroActivoSubyacente" ---VACIO											--27
			,TO_DECIMAL(0, 26, 8) AS"TasaActivoSubyacente"												--28
			---sOLO CUANDO SE INFORMA SWAP
			,TO_VARCHAR('0') AS"TipoSegundoActivoSubyacente"											--29
			,TO_VARCHAR('') AS"CodigoSegundoActivoSubyacente"											--30
			,TO_VARCHAR('') AS"OtroSegundoActivoSubyacente"												--31
			,TO_DECIMAL(0, 26, 8) AS"TasaSegundoActivoSubyacente"										--32	
			,TO_VARCHAR('1') AS"CodigoPrecioFuturo"														--33
			,FWD."TipoCambioCierre" AS"PrecioFuturo"													--34	
			,COALESCE(HMONPRI."ValorDestino", MONPRI."IdMoneda") AS"CodigoMonedaFuturo"					--35
			,COALESCE(HUNIDMON."ValorDestino", TO_VARCHAR('13')) AS"CodigoUnidad"						--36
			,FWD."MontoMonedaPrincipal" AS"MontoNocional"												--37
			,TO_VARCHAR('') AS"CodigoSegundaUnidad" --solo cuando es SWAP								--38
			,'0' AS"SegundoMontoNocional" --solo cuando es SWAP						--39
			,FWD."FechaVencimiento" AS"FechaVencimiento"											  	--40		

FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD" FWD
--FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDHistorico" FWD
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
ON (CLI."IdCliente"=FWD."IdCliente")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
		ON  PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.Plataforma.BD::REG.Pais" PAIS
		ON PAIS."IdPais"=PER."IdPaisResidencia"
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONPRI
		ON MONPRI."IdMoneda" =FWD."IdMonedaPrincipal"
LEFT OUTER JOIN  "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAnexo" ANE
		ON (ANE."IdCliente"=CLI."IdCliente" AND  ANE."IdTipoDocumento"='CONTRATOCONDICIONESGRALES' AND  ANE."IdEstado"='ACT')				
---Homologaciones				
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HPAIS
		ON (HPAIS."Esquema"='DJ1820' AND TRIM(PAIS."CodigoPais")= TRIM(HPAIS."ValorOrigen") AND HPAIS."Tipo"='PAISCONTRA' AND  TO_DATE(FWD."FechaOperacion") BETWEEN TO_DATE(HPAIS."FechaDatosDesde") AND TO_DATE(HPAIS."FechaDatosHasta"))
		
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HMONPRI
		ON (HMONPRI."Esquema"='DJ1820' AND TRIM(MONPRI."IdMoneda")= TRIM(HMONPRI."ValorOrigen") AND HMONPRI."Tipo"='MONEDA' AND  TO_DATE(FWD."FechaOperacion") BETWEEN TO_DATE(HMONPRI."FechaDatosDesde") AND TO_DATE(HMONPRI."FechaDatosHasta"))
		
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONSEC
		ON MONSEC."IdMoneda" =FWD."IdMonedaSecundaria"			
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HUNIDMON
		ON (HUNIDMON."Esquema"='DJ1820' AND TRIM(MONSEC."IdMoneda")= TRIM(HUNIDMON."ValorOrigen") AND HUNIDMON."Tipo"='UNIDADMONEDA' AND  TO_DATE(FWD."FechaOperacion") BETWEEN TO_DATE(HUNIDMON."FechaDatosDesde") AND TO_DATE(HUNIDMON."FechaDatosHasta"))
				
		--indicadores cliente
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" REL
		ON REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='DJ1820' AND REL."CodigoValorIndicador" <>'' AND REL."IdCliente" =FWD."IdCliente"
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::TRI.DJ1820.HomologacionTipoIndicador" HREL	
		ON (HREL."CodigoTipoIndicador"= REL."CodigoTipoIndicador" AND HREL."Codigo"=REL."CodigoValorIndicador" AND TO_DATE(FWD."FechaOperacion") BETWEEN TO_DATE(HREL."FechaDatosDesde") AND TO_DATE(HREL."FechaDatosHasta"))
		
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HEVT
		ON (HEVT."Esquema"='DJ1820' AND TRIM(FWD."Estado")= TRIM(HEVT."ValorOrigen") AND HEVT."Tipo"='TIPOEVENTO' AND  TO_DATE(FWD."FechaOperacion") BETWEEN TO_DATE(HEVT."FechaDatosDesde") AND TO_DATE(HEVT."FechaDatosHasta"))
		
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HMOD
		ON (HMOD."Esquema"='DJ1820' AND TRIM(FWD."ModalidadLiquidacionValor")= TRIM(HMOD."ValorOrigen") AND HMOD."Tipo"='MODALIDADCUMPLIMIENTO' AND  TO_DATE(FWD."FechaOperacion") BETWEEN TO_DATE(HMOD."FechaDatosDesde") AND TO_DATE(HMOD."FechaDatosHasta"))
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HPOS
		ON (HPOS."Esquema"='DJ1820' AND TRIM(FWD."IdTipoOperacion")= TRIM(HPOS."ValorOrigen") AND HPOS."Tipo"='POSICIONDECLARANTE' AND  TO_DATE(FWD."FechaOperacion") BETWEEN TO_DATE(HPOS."FechaDatosDesde") AND TO_DATE(HPOS."FechaDatosHasta"))
		WHERE FWD."Estado" <> 'ANU'
UNION ALL 
SELECT 
	 "IdEmpresa","IdCliente"	,"IdPortafolio","IdCartera","FolioOperacion","FechaOperacion","FechaAnticipo","FechaLiquidacion" ,"IdInstrumento","IdTipoOperacion","IdClasificacionTipoInstrumento"
 	,"IdMonedaPrincipal","IdMonedaSecundaria","MontoMonedaPrincipal","MontoMonedaSecundaria","RutCliente","TaxID","CodigoPaisResidencia","CodigoRelacion","CodigoModoContrato","TipoAcuerdoMarco"
 	,"NumeroAcuerdoMarco","FechaSuscripcionAcuerdo","NumeroContrato","FechaSuscripcionContrato" ,"TipoEvento" ,"TipoContrato","Instrumento","ModalidadCumplimiento","CodigoPosicionDeclarante" 								
	,"TipoActivoSubyacente","CodigoActivoSubyacente","OtroActivoSubyacente","TasaActivoSubyacente","TipoSegundoActivoSubyacente","CodigoSegundoActivoSubyacente","OtroSegundoActivoSubyacente"										
	,"TasaSegundoActivoSubyacente","CodigoPrecioFuturo","PrecioFuturo","CodigoMonedaFuturo","CodigoUnidad","MontoNocional","CodigoSegundaUnidad","SegundoMontoNocional","FechaVencimiento"
FROM "BSCL.Custodia.BD.Vistas::DER.ConsultaMovimientosLiquidacionesOpciones"
UNION ALL 
SELECT 
	 "IdEmpresa","IdCliente"	,"IdPortafolio","IdCartera","FolioOperacion","FechaOperacion","FechaAnticipo","FechaLiquidacion" ,"IdInstrumento","IdTipoOperacion","IdClasificacionTipoInstrumento"
 	,"IdMonedaPrincipal","IdMonedaSecundaria","MontoMonedaPrincipal","MontoMonedaSecundaria","RutCliente","TaxID","CodigoPaisResidencia","CodigoRelacion","CodigoModoContrato","TipoAcuerdoMarco"
 	,"NumeroAcuerdoMarco","FechaSuscripcionAcuerdo","NumeroContrato","FechaSuscripcionContrato" ,"TipoEvento" ,"TipoContrato","Instrumento","ModalidadCumplimiento","CodigoPosicionDeclarante" 								
	,"TipoActivoSubyacente","CodigoActivoSubyacente","OtroActivoSubyacente","TasaActivoSubyacente","TipoSegundoActivoSubyacente","CodigoSegundoActivoSubyacente","OtroSegundoActivoSubyacente"										
	,"TasaSegundoActivoSubyacente","CodigoPrecioFuturo","PrecioFuturo","CodigoMonedaFuturo","CodigoUnidad","MontoNocional","CodigoSegundaUnidad","SegundoMontoNocional","FechaVencimiento"
FROM "BSCL.Custodia.BD.Vistas::DER.ConsultaMovimientosLiquidacionesFuturo"
)

WITH READ ONLY;

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1820.Origen" to bcs_desarrollador with grant option;