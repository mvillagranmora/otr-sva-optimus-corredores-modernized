SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Custodia.BD.Vistas::DER.ConsultaMovimientosLiquidacionesFuturo";

CREATE VIEW "BSCL.Custodia.BD.Vistas::DER.ConsultaMovimientosLiquidacionesFuturo" 

AS

--Carteras
SELECT
	 CAR."IdEmpresa"
	,CAR."IdCliente"
	,CAR."IdPortafolio"
	,CAR."IdCartera"
	,CAR."FolioComprobante" AS "FolioOperacion"		
	,CAR."FechaOperacion"
	,CAR."EGRFechaOperacion" AS "FechaAnticipo"
	,CAR."EGRFechaOperacion" AS "FechaLiquidacion"  
	,CAR."IdInstrumento"
	,CAR."IdTipoOperacion"
	,CAR."IdClasificacionTipoInstrumento"
	,CAR."IdMonedaInstrumento" AS "IdMonedaPrincipal"
	,CAR."IdMonedaInstrumento" AS "IdMonedaSecundaria"
	,CAR."INGMontoATasaCompraAnterior" 			 AS "MontoMonedaPrincipal"
	,CAR."INGMontoCompraActualMonedaInstrumento" AS "MontoMonedaSecundaria"
	,PER."DocumentoIdentidad" 					 AS "RutCliente"
	,'' 				 		 AS "TaxID" 		 
	,COALESCE(HPAIS."ValorDestino", PAIS."CodigoPais") AS"CodigoPaisResidencia" --11
	,'99'                          AS "CodigoRelacion"  
	, TO_VARCHAR('1') AS "CodigoModoContrato" -- 5
	, TO_VARCHAR('1') AS "TipoAcuerdoMarco" --6
	, TO_BIGINT ((ltrim(rtrim(substring(COALESCE(replace(PER."DocumentoIdentidad",'.',''),'0'),1,LENGTH(COALESCE(replace(PER."DocumentoIdentidad",'.',''),'0'))-2))))) AS "NumeroAcuerdoMarco" --7
	, '0001-01-01' AS "FechaSuscripcionAcuerdo" --8
	,CAR."FolioCartera" AS"NumeroContrato"													    --17
	,CAR."FechaOperacion" AS"FechaSuscripcionContrato"  										--18
	,EVTINFDO."ValorDestino"  AS"TipoEvento" 																--19
	,TO_VARCHAR('2')  AS"TipoContrato"								--21 
	,TO_VARCHAR('') AS"Instrumento" ---queda en blanco a menos que instrumento sea 12,13,o 14	--22  						
	,TO_VARCHAR('1') AS "ModalidadCumplimiento" --23
	
	,COALESCE(HPOS."ValorDestino",CAR."IdTipoOperacion") AS"CodigoPosicionDeclarante"		 --24 
	,TO_VARCHAR('1')     AS"TipoActivoSubyacente"	      										--25
	,TO_VARCHAR('1') AS"CodigoActivoSubyacente"												--26
	,TO_VARCHAR('') AS"OtroActivoSubyacente" ---VACIO											--27
	,TO_DECIMAL(0, 26, 8) AS"TasaActivoSubyacente"												--28
	
	---sOLO CUANDO SE INFORMA SWAP
	,TO_VARCHAR('0') AS"TipoSegundoActivoSubyacente"											--29
	,TO_VARCHAR('') AS"CodigoSegundoActivoSubyacente"											--30
	,TO_VARCHAR('') AS"OtroSegundoActivoSubyacente"											    --31
	,TO_DECIMAL(0, 26, 8) AS"TasaSegundoActivoSubyacente"										--32	
	,CDPRE."ValorDestino" AS"CodigoPrecioFuturo"												--33
	,INS."Precio" AS"PrecioFuturo"					               								--34	
	
	,TO_VARCHAR('1') AS"CodigoMonedaFuturo"												--35
	,TO_VARCHAR('1') AS"CodigoUnidad"															--36
	,CAR."INGMontoATasaCompraAnterior" AS"MontoNocional"										--37
	,TO_VARCHAR('') AS"CodigoSegundaUnidad" --solo cuando es SWAP								--38
	,TO_VARCHAR('') AS"SegundoMontoNocional" --solo cuando es SWAP						        --39
	,CAR."FechaVencimientoInstrumento" AS"FechaVencimiento"										--40

--FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS CAR
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
	ON (CAR."IdCliente" = CLI."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER
	ON (PER."IdPersona" = CLI."IdPersona")
INNER JOIN "BSCL.Plataforma.BD::REG.Pais" PAIS 
	ON PAIS."IdPais"=PER."IdPaisResidencia"
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MONPRI 
	ON MONPRI."IdMoneda" =CAR."IdMonedaInstrumento"
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS 
	ON (INS."IdInstrumento" = CAR."IdInstrumento")
--Homologaciones

	--Homologacion TipoEvento
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" EVTINFDO
	ON (EVTINFDO."Esquema"='DJ1820' AND EVTINFDO."Tipo" = 'TIPOEVENTO' 
		AND EVTINFDO."ValorOrigen" = CAR."IdEstado")
		
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HPOS
	ON (HPOS."Esquema"='DJ1820' AND TRIM(CAR."IdTipoOperacion")= TRIM(HPOS."ValorOrigen") AND HPOS."Tipo"='POSICIONDECLARANTE' AND  TO_DATE(CAR."FechaOperacion") BETWEEN TO_DATE(HPOS."FechaDatosDesde") AND TO_DATE(HPOS."FechaDatosHasta"))
	
	--Homologacion Otros Subyacentes
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" OTRSYT
	ON (OTRSYT."Esquema"='DJ1820' AND OTRSYT."Tipo" = 'OTROSSUBYACENTES' 
		AND OTRSYT."ValorOrigen" = CAR."IdTipoInstrumento")

LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" TPOSYT
	ON (TPOSYT."Esquema"='DJ1820' AND TPOSYT."Tipo" = 'TIPOACTIVOSUBYACENTE' 
		AND TPOSYT."ValorOrigen" = CAR."IdTipoInstrumento")
		
	--Homologacion Codigo Precio
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" CDPRE
	ON (CDPRE."Esquema"='DJ1820' AND CDPRE."Tipo" = 'CODIGOPRECIOFUTURO' 
		AND CDPRE."ValorOrigen" = CAR."IdTipoInstrumento")
	
	--Homologacion Moneda
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" CDMON
	ON (CDMON."Esquema"='DJ1820' AND CDMON."Tipo" = 'MONEDA' 
		AND CDMON."ValorOrigen" = CAR."IdTipoInstrumento")
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HPAIS
ON (HPAIS."Esquema"='DJ1820' AND TRIM(PAIS."CodigoPais")= TRIM(HPAIS."ValorOrigen") AND HPAIS."Tipo"='PAISCONTRA' AND  TO_DATE(CAR."FechaOperacion") BETWEEN TO_DATE(HPAIS."FechaDatosDesde") AND TO_DATE(HPAIS."FechaDatosHasta"))

WHERE
	CAR."IdClasificacionTipoInstrumento" = 'DV'
	AND CAR."IdTipoInstrumento" IN ('FDO', 'FIP', 'FBC', 'FUF', 'FIC')
	AND CAR."IdEstado" IN ('VIG', 'LIQ', 'VCD')
	AND CLI."IdCliente" IS NOT NULL


UNION ALL


--Liquidaciones
SELECT
	 CAR."IdEmpresa"
	,CAR."IdCliente"
	,CAR."IdPortafolio"
	,CAR."IdCartera"
	,CAR."FolioComprobante" AS "FolioOperacion"		
	,CAR."FechaOperacion"
	,CAR."EGRFechaOperacion" AS "FechaAnticipo"
	,CAR."EGRFechaOperacion" AS "FechaLiquidacion"  
	,CAR."IdInstrumento"
	,CAR."IdTipoOperacion"
	,CAR."IdClasificacionTipoInstrumento"
	,CAR."IdMonedaInstrumento" AS "IdMonedaPrincipal"
	,CAR."IdMonedaInstrumento" AS "IdMonedaSecundaria"
	,CAR."INGMontoATasaCompraAnterior" 			 AS "MontoMonedaPrincipal"
	,CAR."INGMontoCompraActualMonedaInstrumento" AS "MontoMonedaSecundaria"
	,PER."DocumentoIdentidad" 					 AS "RutCliente"
	,'' 				 		 AS "TaxID" 		 
	,COALESCE(HPAIS."ValorDestino", PAIS."CodigoPais") AS"CodigoPaisResidencia" --11
	,''                          AS "CodigoRelacion"  
	, TO_VARCHAR('1') AS "CodigoModoContrato" -- 5
	, TO_VARCHAR('1') AS "TipoAcuerdoMarco" --6
	, TO_BIGINT ((ltrim(rtrim(substring(COALESCE(replace(PER."DocumentoIdentidad",'.',''),'0'),1,LENGTH(COALESCE(replace(PER."DocumentoIdentidad",'.',''),'0'))-2))))) AS "NumeroAcuerdoMarco" --7
	, '0001-01-01' AS "FechaSuscripcionAcuerdo" --8
	,CAR."FolioCartera" AS"NumeroContrato"													    --17
	,CAR."FechaOperacion" AS"FechaSuscripcionContrato"  										--18
	,EVTINFDO."ValorDestino"  AS"TipoEvento" 																--19
	,TO_VARCHAR('2')  AS"TipoContrato"								--21 
	,TO_VARCHAR('') AS"Instrumento" ---queda en blanco a menos que instrumento sea 12,13,o 14	--22  						
	,TO_VARCHAR('1') AS "ModalidadCumplimiento" --23
	
	,COALESCE(HPOS."ValorDestino",CAR."IdTipoOperacion") AS"CodigoPosicionDeclarante"										--24 
	,TPOSYT."ValorDestino" AS"TipoActivoSubyacente"											--25
	,TO_VARCHAR('1') AS"CodigoActivoSubyacente"												--26
	,TO_VARCHAR('') AS"OtroActivoSubyacente" ---VACIO											--27
	,TO_DECIMAL(0, 26, 8) AS"TasaActivoSubyacente"												--28
	
	---sOLO CUANDO SE INFORMA SWAP
	,TO_VARCHAR('0') AS"TipoSegundoActivoSubyacente"											--29
	,TO_VARCHAR('') AS"CodigoSegundoActivoSubyacente"											--30
	,TO_VARCHAR('') AS"OtroSegundoActivoSubyacente"											--31
	,TO_DECIMAL(0, 26, 8) AS"TasaSegundoActivoSubyacente"										--32	
	,CDPRE."ValorDestino" AS"CodigoPrecioFuturo"														--33
	,INS."Precio" AS"PrecioFuturo"					               								--34	
	
	,TO_VARCHAR('1') AS"CodigoMonedaFuturo"													--35
	,TO_VARCHAR('1') AS"CodigoUnidad"															--36
	,CAR."INGMontoATasaCompraAnterior" AS"MontoNocional"										--37
	,TO_VARCHAR('') AS"CodigoSegundaUnidad" --solo cuando es SWAP								--38
	,TO_VARCHAR('') AS"SegundoMontoNocional" --solo cuando es SWAP						        --39
	,CAR."FechaVencimientoInstrumento" AS"FechaVencimiento"										--40

--FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS CAR
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos" AS MOV 
	ON "CAR"."IdCartera" = MOV."IdOperacion"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
	ON (CAR."IdCliente" = CLI."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER
	ON (PER."IdPersona" = CLI."IdPersona")
INNER JOIN "BSCL.Plataforma.BD::REG.Pais" PAIS 
	ON PAIS."IdPais"=PER."IdPaisResidencia"
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MONPRI 
	ON MONPRI."IdMoneda" =CAR."IdMonedaInstrumento"
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS 
	ON (INS."IdInstrumento" = CAR."IdInstrumento")
--Homologaciones

	--Homologacion TipoEvento
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" EVTINFDO
	ON (EVTINFDO."Esquema"='DJ1820' AND EVTINFDO."Tipo" = 'TIPOEVENTO' 
		AND EVTINFDO."ValorOrigen" = CAR."IdEstado")
		
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HPOS
	ON (HPOS."Esquema"='DJ1820' AND TRIM(CAR."IdTipoOperacion")= TRIM(HPOS."ValorOrigen") AND HPOS."Tipo"='POSICIONDECLARANTE' AND  TO_DATE(CAR."FechaOperacion") BETWEEN TO_DATE(HPOS."FechaDatosDesde") AND TO_DATE(HPOS."FechaDatosHasta"))

	--Homologacion Tipo Subyacente
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" TPOSYT
	ON (TPOSYT."Esquema"='DJ1820' AND TPOSYT."Tipo" = 'TIPOACTIVOSUBYACENTE' 
		AND TPOSYT."ValorOrigen" = CAR."IdTipoInstrumento")
	
	--Homologacion Otros Subyacentes
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" OTRSYT
	ON (OTRSYT."Esquema"='DJ1820' AND OTRSYT."Tipo" = 'OTROSSUBYACENTES' 
		AND OTRSYT."ValorOrigen" = CAR."IdTipoInstrumento")
		
	--Homologacion Codigo Precio
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" CDPRE
	ON (CDPRE."Esquema"='DJ1820' AND CDPRE."Tipo" = 'CODIGOPRECIOFUTURO' 
		AND CDPRE."ValorOrigen" = CAR."IdTipoInstrumento")
	
	--Homologacion Moneda
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" CDMON
	ON (CDMON."Esquema"='DJ1820' AND CDMON."Tipo" = 'MONEDA' 
		AND CDMON."ValorOrigen" = CAR."IdTipoInstrumento")
		
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HPAIS
ON (HPAIS."Esquema"='DJ1820' AND TRIM(PAIS."CodigoPais")= TRIM(HPAIS."ValorOrigen") AND HPAIS."Tipo"='PAISCONTRA' AND  TO_DATE(CAR."FechaOperacion") BETWEEN TO_DATE(HPAIS."FechaDatosDesde") AND TO_DATE(HPAIS."FechaDatosHasta"))

WHERE
	MOV."IdConcepto" IN ('CALFVE','CALFDI','ABLFVE','ABLFDI','ABEJ','CAEJ','ABSO','CASO')
	AND CAR."IdEstado" IN ('VIG', 'LIQ', 'VCD')
	AND CLI."IdCliente" IS NOT NULL
	
	
	
WITH READ ONLY;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::DER.ConsultaMovimientosLiquidacionesFuturo" TO BCS_DESARROLLADOR WITH GRANT OPTION;