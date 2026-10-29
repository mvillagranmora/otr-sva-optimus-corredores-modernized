SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.ConsultaMovimientosForwardDJ1";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.ConsultaMovimientosForwardDJ1" AS

SELECT
	 "KIdContrato"
	,"KFechaContrato"
	,"HoraContrato"
	,"S1_RutCtraparte2"
	,"S1_LEICtraparte2"
	,"S1_NomCtraparte2"
	,COALESCE(HPAIS."ValorDestino", PAIS."CodigoPais")							AS "S1_PaisCtraparte2"
	,CASE WHEN M."EventoReporte" IN ('CES','ACS','MCS') THEN "S1_RutCedente" ELSE '' END AS "S1_RutCedente"
	,CASE WHEN M."EventoReporte" IN ('CES','ACS','MCS') THEN "S1_LEICedente" ELSE '' END AS "S1_LEICedente"
	,'FWD'																		AS "S2_Instrumento"
	,COALESCE(HMOD."ValorDestino", FWD."ModalidadLiquidacionValor")				AS "S2_ModalidadLiq"
	,CASE
		WHEN FWD."ModalidadLiquidacionValor" = 'COMPENSADA' 
		THEN COALESCE(HMONCOM."ValorDestino", FWD."IdMonedaCompensacion") 
		ELSE COALESCE(HMONPRI."ValorDestino", FWD."IdMonedaPrincipal") 
	 END 																		AS "S2_MonedaComp"
	,"S2_FechaInicio"
	,CASE FWD."ModalidadLiquidacionValor" WHEN 'COMPENSADA' THEN 
			CASE WHEN "S2_FechaFixing1" = '0001-01-01' THEN "S2_FechaTermino"
			 	 WHEN COALESCE("S2_FechaFixing1",'') = '' THEN "S2_FechaTermino" 
				 ELSE "S2_FechaFixing1" END 
		ELSE '0001-01-01' END AS "S2_FechaFixing1"
	,"S2_FechaTermino"
	,"S2_FechaPagOpe"
	,"S2_ConvenioMarco"
	,COALESCE(HPAIS."ValorDestino", PAIS."CodigoPais")							AS "S2_PaisContrato"
	,CASE WHEN M."EventoReporte" IN ('VIG','NUE') THEN '0' ELSE PAGO."CantidadPagos" END AS "S2_NumTotPagos"
	,CASE WHEN "S3_MonedaComprada"='UF' THEN 'CLF' ELSE "S3_MonedaComprada" END AS "S3_MonedaComprada"
	,"S3_MontoComprada"
	,CASE WHEN "S3_XR_MonedaVendida"='UF' THEN 'CLF' ELSE "S3_XR_MonedaVendida"  END AS "S3_XR_MonedaVendida"
	,"S3_XR_MontoVendida"
	,CONCAT(HMONPRI."ValorDestino", CONCAT('/', HMONSEC."ValorDestino"))			AS "S3_XR_ConvTipoCambio"
	,CASE WHEN FWD."S3_I_IdActivoSuby"='ARBITR' THEN
			CASE WHEN FWD."ParidadCierre"<>0 THEN FWD."ParidadCierre"
				ELSE FWD."ParidadCosto" END
		ELSE FWD."S3_PrecioFuturo" END AS "S3_PrecioFuturo"
	,CASE WHEN FWD."S3_I_IdActivoSuby"='ARBITR' THEN
		CASE WHEN FWD."ParidadCierre"<>0 THEN (-1)*FWD."ParidadCierre"
			ELSE (-1)*FWD."ParidadCosto" END
		ELSE FWD."S3_XR_PuntosForward" END AS "S3_XR_PuntosForward"
	,HMONSEC."ValorDestino"														AS "S8_MonedaValoriza"
	,"S8_ValorMercado"
	,"S8_ValorMercadoActivo"
	,"S8_ValorMercadoPasivo"
	,'FWD'																		AS "kProducto"
	,"kPeriodoComercial"
	,FWD."IdEmpresa"
 	,FWD."IdCliente"
   	,FWD."IdPortafolio"
   	,FWD."IdCartera"
   	,FWD."FolioOperacion"
   	,FWD."FechaOperacion"
	,FWD."FechaLiquidacion"
	,CASE WHEN FWD."EGRIdTipoOperacion" = 'ANTFWD' THEN FWD."FechaAnticipo" ELSE '0001-01-01' END AS "FechaAnticipo"
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
   	--,COALESCE(HREL."ValorDestino",COALESCE(REL."CodigoValorIndicador",TO_VARCHAR('99')))  AS"CodigoRelacion" --12
    ,TO_VARCHAR('3') AS"CodigoModoContrato"	--13
    ,TO_VARCHAR('1') AS"TipoAcuerdoMarco"	--14
	,TO_BIGINT ((ltrim(rtrim(substring(COALESCE(replace(PER."DocumentoIdentidad",'.',''),'0'),1,LENGTH(COALESCE(replace(PER."DocumentoIdentidad",'.',''),'0'))-2))))) AS"NumeroAcuerdoMarco" --15
	,COALESCE(TO_DATE(ANE."FechaFirmaCliente1"),'0001-01-01') AS"FechaSuscripcionAcuerdo"			--16
	,FWD."FolioOperacion" AS"NumeroContrato"													--17
	,FWD."FechaOperacion" AS"FechaSuscripcionContrato"  										--18
	,FWD."EstadoOperacion" AS"TipoEvento"  					  	--Segun el estado				--19
	 --No esta en Layout																		--20
	,FWD."IdTipoProducto" AS"TipoContrato"														--21
	,TO_VARCHAR('') AS"Instrumento" ---si el TipoContrato es 1 siempre es blanco				--22
	,COALESCE(HMOD."ValorDestino",FWD."ModalidadLiquidacionValor") AS"ModalidadCumplimiento" 	--23
	,COALESCE(HPOS."ValorDestino",FWD."IdTipoOperacion") AS"CodigoPosicionDeclarante"
	,TO_VARCHAR('1') AS"TipoActivoSubyacente" ---Moneda											--25
	,COALESCE(HMONPRI."ValorDestino", MONPRI."IdMoneda") AS"CodigoActivoSubyacente"				--26
	,TO_VARCHAR('') AS"OtroActivoSubyacente" ---VACIO											--27
	,TO_DECIMAL(0, 26, 8) AS"TasaActivoSubyacente"												--28
	--sOLO CUANDO SE INFORMA SWAP
	,TO_VARCHAR('0') AS"TipoSegundoActivoSubyacente"											--29
	,TO_VARCHAR('') AS"CodigoSegundoActivoSubyacente"											--30
	,TO_VARCHAR('') AS"OtroSegundoActivoSubyacente"												--31
	,TO_DECIMAL(0, 26, 8) AS"TasaSegundoActivoSubyacente"										--32	
	,TO_VARCHAR('1') AS"CodigoPrecioFuturo"														--33
	,FWD."Precio" AS"PrecioFuturo"																--34	
	,COALESCE(HMONPRI."ValorDestino", MONPRI."IdMoneda") AS"CodigoMonedaFuturo"					--35
	,COALESCE(HUNIDMON."ValorDestino", TO_VARCHAR('13')) AS"CodigoUnidad"						--36
	,FWD."MontoMonedaPrincipal" AS"MontoNocional"												--37
	,TO_VARCHAR('') AS"CodigoSegundaUnidad" --solo cuando es SWAP								--38
	,TO_VARCHAR('') AS"SegundoMontoNocional" --solo cuando es SWAP						        --39
	,FWD."FechaVencimiento" AS"FechaVencimiento"											  	--40
	,FWD."S3_I_TipoActivoSub"
	,FWD."S3_I_IdActivoSuby"
FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1.ConsultarMovimientosFWDDJ1" FWD
INNER JOIN "BSCL.Normativa.BD.Vistas::TRI.DJ1.Carteras" M -- Maximas carteras
	ON FWD."KIdContrato" = M."FolioCartera" AND FWD."IdCartera" = M."IdCartera"
LEFT JOIN (SELECT COUNT("IdComprobantePagoOperacion") AS "CantidadPagos", "IdOperacion" 
			FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
			WHERE "IdTipoProducto" = 'MEFW'
			GROUP BY "IdOperacion") PAGO
	ON PAGO."IdOperacion" = FWD."IdCartera"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
	ON (CLI."IdCliente"=FWD."IdCliente")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
	ON  PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.Plataforma.BD::REG.Pais" PAIS
	ON PAIS."IdPais"=PER."IdPaisResidencia"
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONPRI
	ON MONPRI."IdMoneda" =FWD."IdMonedaPrincipal"
LEFT OUTER JOIN  "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAnexo" ANE
	ON (ANE."IdCliente"=CLI."IdCliente" AND  ANE."IdTipoDocumento"='CONTMARFWD' AND  ANE."IdEstado"='ACT')			
---Homologaciones				
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HPAIS
	ON (HPAIS."Esquema" = 'DJ1' AND TRIM(PAIS."CodigoPais") = TRIM(HPAIS."ValorOrigen") AND HPAIS."Tipo" = 'CodPais' AND  TO_DATE(M."fecha") BETWEEN TO_DATE(HPAIS."FechaDatosDesde") AND TO_DATE(HPAIS."FechaDatosHasta"))
	
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HMOD
	ON (HMOD."Esquema" = 'DJ1' AND TRIM(FWD."ModalidadLiquidacionValor") = TRIM(HMOD."ValorOrigen") AND HMOD."Tipo" = 'ModaliLiq'  AND  TO_DATE(M."fecha") BETWEEN TO_DATE(HMOD."FechaDatosDesde") AND TO_DATE(HMOD."FechaDatosHasta"))
		
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HMONPRI
	ON (HMONPRI."Esquema" = 'DJ1' AND TRIM(FWD."IdMonedaPrincipal") = TRIM(HMONPRI."ValorOrigen") AND HMONPRI."Tipo" = 'CodMoneda' AND  TO_DATE(M."fecha") BETWEEN TO_DATE(HMONPRI."FechaDatosDesde") AND TO_DATE(HMONPRI."FechaDatosHasta"))
		
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HMONSEC
	ON (HMONSEC."Esquema" = 'DJ1' AND TRIM(FWD."IdMonedaSecundaria") = TRIM(HMONSEC."ValorOrigen") AND HMONSEC."Tipo" = 'CodMoneda' AND  TO_DATE(M."fecha") BETWEEN TO_DATE(HMONSEC."FechaDatosDesde") AND TO_DATE(HMONSEC."FechaDatosHasta"))
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONSEC
	ON MONSEC."IdMoneda" =FWD."IdMonedaSecundaria"			
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HUNIDMON
	ON (HUNIDMON."Esquema"='DJ1820' AND TRIM(MONSEC."IdMoneda")= TRIM(HUNIDMON."ValorOrigen") AND HUNIDMON."Tipo"='UNIDADMONEDA' AND  TO_DATE(M."fecha") BETWEEN TO_DATE(HUNIDMON."FechaDatosDesde") AND TO_DATE(HUNIDMON."FechaDatosHasta"))			
--indicadores cliente

LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HPOS
	ON (HPOS."Esquema"='DJ1' AND TRIM(FWD."IdTipoOperacion")= TRIM(HPOS."ValorOrigen") AND HPOS."Tipo"='S3_I_PosicionOpe' AND  TO_DATE(M."fecha") BETWEEN TO_DATE(HPOS."FechaDatosDesde") AND TO_DATE(HPOS."FechaDatosHasta"))
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HMONCOM
	ON (HMONCOM."Esquema" = 'DJ1' AND TRIM(FWD."IdMonedaCompensacion") = TRIM(HMONCOM."ValorOrigen") AND HMONCOM."Tipo" = 'CodMoneda' AND  TO_DATE(M."fecha") BETWEEN TO_DATE(HMONCOM."FechaDatosDesde") AND TO_DATE(HMONCOM."FechaDatosHasta"))
WHERE FWD."Estado" <> 'ANU';
	
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1.ConsultaMovimientosForwardDJ1" TO BCS_DESARROLLADOR WITH GRANT OPTION;