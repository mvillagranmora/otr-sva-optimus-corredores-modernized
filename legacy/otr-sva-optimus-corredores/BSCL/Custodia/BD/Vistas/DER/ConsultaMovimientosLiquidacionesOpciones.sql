SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Custodia.BD.Vistas::DER.ConsultaMovimientosLiquidacionesOpciones";

CREATE VIEW "BSCL.Custodia.BD.Vistas::DER.ConsultaMovimientosLiquidacionesOpciones" 
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
	,''                          AS "CodigoRelacion"  
	,TO_VARCHAR('3') AS"CodigoModoContrato"	
	,TO_VARCHAR('1') AS"TipoAcuerdoMarco"
	,TO_BIGINT((ltrim(rtrim(substring(COALESCE(replace(PER."DocumentoIdentidad",'.',''),'0'),1,LENGTH(COALESCE(replace(PER."DocumentoIdentidad",'.',''),'0'))-2))))) AS"NumeroAcuerdoMarco" --15
	,'0001-01-01'											 AS"FechaSuscripcionAcuerdo"	    --16  --ver lo del contrato.
	,CAR."FolioCartera" AS"NumeroContrato"													    --17
	,CAR."FechaOperacion" AS"FechaSuscripcionContrato"  										--18
	,COALESCE (HEVT."ValorDestino",CAR."IdEstado")   AS"TipoEvento" 								--19
	,COALESCE (HTCON."ValorDestino",concat(INS."EsPutEsCall",INS."EstiloOpcion"))      AS"TipoContrato"	--21 
	,TO_VARCHAR('') AS"Instrumento" ---queda en blanco a menos que instrumento sea 12,13,o 14	--22 
	,COALESCE(HMOD."ValorDestino",'')  AS "ModalidadCumplimiento" --23
	,COALESCE(HPOS."ValorDestino",CAR."IdTipoOperacion") AS"CodigoPosicionDeclarante" 			--24 
	,TO_VARCHAR('1') AS"TipoActivoSubyacente" ---Moneda											--25
	,MONPRI."IdMoneda" AS"CodigoActivoSubyacente"												--26
	,TO_VARCHAR('') AS"OtroActivoSubyacente" ---VACIO											--27
	,TO_DECIMAL(0, 26, 8) AS"TasaActivoSubyacente"												--28
	
	---sOLO CUANDO SE INFORMA SWAP
	,TO_VARCHAR('0') AS"TipoSegundoActivoSubyacente"											--29
	,TO_VARCHAR('') AS"CodigoSegundoActivoSubyacente"											--30
	,TO_VARCHAR('') AS"OtroSegundoActivoSubyacente"												--31
	,TO_DECIMAL(0, 26, 8) AS"TasaSegundoActivoSubyacente"										--32	
	,TO_VARCHAR('1') AS"CodigoPrecioFuturo"														--33
	,INS."Precio" AS"PrecioFuturo"					               								--34	
	
	,MONPRI."IdMoneda" AS"CodigoMonedaFuturo"													--35
	,COALESCE(HUNIDMON."ValorDestino", TO_VARCHAR('13')) AS"CodigoUnidad"															--36
	,CAR."INGMontoATasaCompraAnterior" AS"MontoNocional"										--37
	,TO_VARCHAR('') AS"CodigoSegundaUnidad" --solo cuando es SWAP								--38
	,TO_VARCHAR('') AS"SegundoMontoNocional" --solo cuando es SWAP						        --39
	,CAR."FechaVencimientoInstrumento" AS"FechaVencimiento"										--40
	
FROM  "BSCL.Custodia.BD::CUS.Cartera" AS CAR
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  ON CLI."IdCliente" = CAR."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.Plataforma.BD::REG.Pais" PAIS ON PAIS."IdPais"=PER."IdPaisResidencia"
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS ON (INS."IdInstrumento" = CAR."IdInstrumento")
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONPRI ON MONPRI."IdMoneda" =CAR."IdMonedaInstrumento"
LEFT OUTER JOIN  "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAnexo" ANE ON (ANE."IdCliente"=CLI."IdCliente" AND  ANE."IdTipoDocumento"='CONTMARFWD' AND  ANE."IdEstado"='ACT')	 -- buscar anexo 	
--Homologaciones
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HPAIS
ON (HPAIS."Esquema"='DJ1820' AND TRIM(PAIS."CodigoPais")= TRIM(HPAIS."ValorOrigen") AND HPAIS."Tipo"='PAISCONTRA' AND  TO_DATE(CAR."FechaOperacion") BETWEEN TO_DATE(HPAIS."FechaDatosDesde") AND TO_DATE(HPAIS."FechaDatosHasta"))

LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HEVT
ON (HEVT."Esquema"='DJ1820' AND TRIM(CAR."IdEstado")= TRIM(HEVT."ValorOrigen") AND HEVT."Tipo"='TIPOEVENTO' AND  TO_DATE(CAR."FechaOperacion") BETWEEN TO_DATE(HEVT."FechaDatosDesde") AND TO_DATE(HEVT."FechaDatosHasta"))

LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HMOD
ON (HMOD."Esquema"='DJ1820' AND TRIM(INS."ModalidadLiquidacion")= TRIM(HMOD."ValorOrigen") AND HMOD."Tipo"='MODALIDADCUMPLIMIENTO' AND  TO_DATE(CAR."FechaOperacion") BETWEEN TO_DATE(HMOD."FechaDatosDesde") AND TO_DATE(HMOD."FechaDatosHasta"))

LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HPOS
ON (HPOS."Esquema"='DJ1820' AND TRIM(CAR."IdTipoOperacion")= TRIM(HPOS."ValorOrigen") AND HPOS."Tipo"='POSICIONDECLARANTE' AND  TO_DATE(CAR."FechaOperacion") BETWEEN TO_DATE(HPOS."FechaDatosDesde") AND TO_DATE(HPOS."FechaDatosHasta"))

LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HTCON
ON (HTCON."Esquema"='DJ1820' AND TRIM(concat(INS."EsPutEsCall",INS."EstiloOpcion"))= TRIM(HTCON."ValorOrigen") AND HPOS."Tipo"='TIPOCONTRATO' AND  TO_DATE(CAR."FechaOperacion") BETWEEN TO_DATE(HTCON."FechaDatosDesde") AND TO_DATE(HTCON."FechaDatosHasta"))

LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONSEC
		ON MONSEC."IdMoneda" =CAR."IdMonedaSecundaria"	
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HUNIDMON
ON (HUNIDMON."Esquema"='DJ1820' AND TRIM(MONSEC."IdMoneda")= TRIM(HUNIDMON."ValorOrigen") AND HUNIDMON."Tipo"='UNIDADMONEDA' AND  TO_DATE(CAR."FechaOperacion") BETWEEN TO_DATE(HUNIDMON."FechaDatosDesde") AND TO_DATE(HUNIDMON."FechaDatosHasta"))


WHERE CAR."IdClasificacionTipoInstrumento" = 'DV'
AND   CAR."IdTipoProducto" != 'MEFW'
AND   CAR."IdTipoInstrumento" IN ('IOA','IOE','IOI','IOD','IOB','IOC','IOU','IOM','IOF')
AND   CAR."IdConcepto"!= 'NETDER'

UNION ALL 

--liquidaciones
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
	,TO_VARCHAR('3') AS"CodigoModoContrato"	
	,TO_VARCHAR('1') AS"TipoAcuerdoMarco"
	,TO_BIGINT((ltrim(rtrim(substring(COALESCE(replace(PER."DocumentoIdentidad",'.',''),'0'),1,LENGTH(COALESCE(replace(PER."DocumentoIdentidad",'.',''),'0'))-2))))) AS"NumeroAcuerdoMarco" --15
	,'0001-01-01'											 AS"FechaSuscripcionAcuerdo"	    --16  --ver lo del contrato.
	,CAR."FolioCartera" AS"NumeroContrato"													    --17
	,CAR."FechaOperacion" AS"FechaSuscripcionContrato"  										--18
	,COALESCE (HEVT."ValorDestino",CAR."IdEstado")   AS"TipoEvento" 								--19
	,COALESCE (HTCON."ValorDestino",concat(INS."EsPutEsCall",INS."EstiloOpcion"))      AS"TipoContrato"	--21 
	,TO_VARCHAR('') AS"Instrumento" ---queda en blanco a menos que instrumento sea 12,13,o 14	--22 
	,COALESCE(HMOD."ValorDestino",'')  AS "ModalidadCumplimiento" --23
	,COALESCE(HPOS."ValorDestino",CAR."IdTipoOperacion") AS"CodigoPosicionDeclarante" 			--24 
	,TO_VARCHAR('1') AS"TipoActivoSubyacente" ---Moneda											--25
	,MONPRI."IdMoneda" AS"CodigoActivoSubyacente"												--26
	,TO_VARCHAR('') AS"OtroActivoSubyacente" ---VACIO											--27
	,TO_DECIMAL(0, 26, 8) AS"TasaActivoSubyacente"												--28
	
	---sOLO CUANDO SE INFORMA SWAP
	,TO_VARCHAR('0') AS"TipoSegundoActivoSubyacente"											--29
	,TO_VARCHAR('') AS"CodigoSegundoActivoSubyacente"											--30
	,TO_VARCHAR('') AS"OtroSegundoActivoSubyacente"												--31
	,TO_DECIMAL(0, 26, 8) AS"TasaSegundoActivoSubyacente"										--32	
	,TO_VARCHAR('1') AS"CodigoPrecioFuturo"														--33
	,INS."Precio" AS"PrecioFuturo"					               								--34	
	
	,MONPRI."IdMoneda" AS"CodigoMonedaFuturo"													--35
	,COALESCE(HUNIDMON."ValorDestino", TO_VARCHAR('13')) AS"CodigoUnidad"															--36
	,CAR."INGMontoATasaCompraAnterior" AS"MontoNocional"										--37
	,TO_VARCHAR('') AS"CodigoSegundaUnidad" --solo cuando es SWAP								--38
	,TO_VARCHAR('') AS"SegundoMontoNocional" --solo cuando es SWAP						        --39
	,CAR."FechaVencimientoInstrumento" AS"FechaVencimiento"										--40
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos" AS "MOV"
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS "CAR" ON "CAR"."IdCartera" = "MOV"."IdOperacion"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  ON CLI."IdCliente" = CAR."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.Plataforma.BD::REG.Pais" PAIS ON PAIS."IdPais"=PER."IdPaisResidencia"
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS ON (INS."IdInstrumento" = CAR."IdInstrumento")
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONPRI ON MONPRI."IdMoneda" =CAR."IdMonedaInstrumento"
--Homologaciones
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HPAIS
ON (HPAIS."Esquema"='DJ1820' AND TRIM(PAIS."CodigoPais")= TRIM(HPAIS."ValorOrigen") AND HPAIS."Tipo"='PAISCONTRA' AND  TO_DATE(CAR."FechaOperacion") BETWEEN TO_DATE(HPAIS."FechaDatosDesde") AND TO_DATE(HPAIS."FechaDatosHasta"))

LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HEVT
ON (HEVT."Esquema"='DJ1820' AND TRIM(CAR."IdEstado")= TRIM(HEVT."ValorOrigen") AND HEVT."Tipo"='TIPOEVENTO' AND  TO_DATE(CAR."FechaOperacion") BETWEEN TO_DATE(HEVT."FechaDatosDesde") AND TO_DATE(HEVT."FechaDatosHasta"))

LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HMOD
ON (HMOD."Esquema"='DJ1820' AND TRIM(INS."ModalidadLiquidacion")= TRIM(HMOD."ValorOrigen") AND HMOD."Tipo"='MODALIDADCUMPLIMIENTO' AND  TO_DATE(CAR."FechaOperacion") BETWEEN TO_DATE(HMOD."FechaDatosDesde") AND TO_DATE(HMOD."FechaDatosHasta"))

LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HPOS
ON (HPOS."Esquema"='DJ1820' AND TRIM(CAR."IdTipoOperacion")= TRIM(HPOS."ValorOrigen") AND HPOS."Tipo"='POSICIONDECLARANTE' AND  TO_DATE(CAR."FechaOperacion") BETWEEN TO_DATE(HPOS."FechaDatosDesde") AND TO_DATE(HPOS."FechaDatosHasta"))

LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HTCON
ON (HTCON."Esquema"='DJ1820' AND TRIM(concat(INS."EsPutEsCall",INS."EstiloOpcion"))= TRIM(HTCON."ValorOrigen") AND HPOS."Tipo"='TIPOCONTRATO' AND  TO_DATE(CAR."FechaOperacion") BETWEEN TO_DATE(HTCON."FechaDatosDesde") AND TO_DATE(HTCON."FechaDatosHasta"))

LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONSEC
		ON MONSEC."IdMoneda" =CAR."IdMonedaSecundaria"	
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HUNIDMON
ON (HUNIDMON."Esquema"='DJ1820' AND TRIM(MONSEC."IdMoneda")= TRIM(HUNIDMON."ValorOrigen") AND HUNIDMON."Tipo"='UNIDADMONEDA' AND  TO_DATE(CAR."FechaOperacion") BETWEEN TO_DATE(HUNIDMON."FechaDatosDesde") AND TO_DATE(HUNIDMON."FechaDatosHasta"))

WHERE MOV."IdConcepto" IN ('ABEJ','CAEJ','ABSO','CASO')

WITH READ ONLY;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::DER.ConsultaMovimientosLiquidacionesOpciones" TO BCS_DESARROLLADOR WITH GRANT OPTION;

