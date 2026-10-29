set schema "BSCL";

drop view "BSCL.Normativa.BD.Vistas::TRI.DJ1829.Origen";

create view "BSCL.Normativa.BD.Vistas::TRI.DJ1829.Origen" (
    "IdEmpresa"
    ,"ID"
    ,"IdDocumento"
    ,"VboUsuario"
    ,"Observaciones"
    ,"FuenteDatos"
    ,"Numero"
	,"RutContraparte"
	,"TaxIDContraparte"
	,"CodigoPaisContraparte"
	,"TipoRelacionContraparte"
	,"ModalidadContratacion"
	,"AcuerdoMarcoTipo"
	,"AcuerdoMarcoNumero"
	,"AcuerdoMarcoFechaSuscripcion"
	,"ContratoNumero"
	,"ContratoFechaSuscripcion"
	,"ContratoVencido"
	,"ContratoEstadoConfirmacion"
	,"ContratoTipo"
	,"ContratoNombreInstrumento"
	,"ContratoModalidadCumplimiento"
	,"ContratoPosicionDeclarante"
	,"ActivoSubyacenteTipo"
	,"ActivoSubyacenteCodigo"
	,"ActivoSubyacenteOtros"
	,"ActivoSubyacenteTasaFija"
	,"SegundoActivoSubyacenteTipo"
	,"SegundoActivoSubyacenteCodigo"
	,"SegundoActivoSubyacenteOtros"
	,"SegundoActivoSubyacenteTasaFija"
	,"PrecioFuturoCodigoPrecio"
	,"PrecioFuturoPrecio"
	,"PrecioFuturoMoneda"
	,"Unidad"
	,"MontoCantidadContratadoNocional"
	,"SegundaUnidad"
	,"SegundoMontoNocional"
	,"FechaVencimiento"
	,"FechaLiquidacionEjercicioOpcion"
	,"PrecioMercadoCodigoPrecio"
	,"PrecioMercadoPrecio"
	,"ValorJustoContratoConfirnacion"
	,"ResultadoEjercicio"
	,"CuentaContableAsociadaResultadoEjercicio"
	,"EfectoPatrimonio"
	,"CuentaContableAsociadaEfectoPatrimonio"
	,"ComisionPactada"
	,"PrimaTotal"
	,"InversionInicial"
	,"OtrosGastosAsociadosContrato"
	,"OtrosIngresosAsociadosContrato"
	,"PagosMontoPagadosAdeudados"
	,"PagosExteriorModalidadPago"
	,"SaldoGarantiasCierre"
	,"IdCliente"
	,"IdPortafolio"
	,"FechaLiquidacion"
	,"FechaContrato"
	,"FolioOperUI"
) as


SELECT 
	   F."IdEmpresa" as "IdEmpresa",
       0 as "ID",
       1 as "IdDocumento", 
       'N' as "VboUsuario", 
       '' as "Observaciones",
       '' as "FuenteDatos", 
       TO_BIGINT(F."FolioOperacion") as "Numero",										--C59
       F."RutCliente" as "RutContraparte", 												--C8
       TO_VARCHAR('') as "TaxIDContraparte", 											--C10
	   COALESCE(HEVT5."ValorDestino", PAIS."CodigoPais") as "CodigoPaisContraparte", 	--C11
       COALESCE(NULLIF(HREL."ValorDestino", ''),NULLIF(REL."CodigoValorIndicador", ''),'99') AS "TipoRelacionContraparte", 	--C12
       TO_VARCHAR('3') as "ModalidadContratacion", 																			--C13
	   TO_VARCHAR('1') as "AcuerdoMarcoTipo", 																				--C14
	   TO_BIGINT ((ltrim(rtrim(substring(COALESCE(replace(PER."DocumentoIdentidad",'.',''),'0'),1,LENGTH(COALESCE(replace(PER."DocumentoIdentidad",'.',''),'0'))-2)))))  as "AcuerdoMarcoNumero", --C15
	   COALESCE(TO_DATE(ANE."FechaModificacion"),'0001-01-01') as "AcuerdoMarcoFechaSuscripcion", 		--C16
	   TO_BIGINT(F."FolioOperacion") as "ContratoNumero", 																--C17
	   F."FechaOperacion" as "ContratoFechaSuscripcion", 												--C18
	   F."ContratoVencido" as "ContratoVencido", 																		--C19 OK
	   F."ContratoEstadoConfirmacion" as "ContratoEstadoConfirmacion",													--C20 OK
	   TO_VARCHAR('1') as "ContratoTipo", 												 	--C21
	   TO_VARCHAR('') as "ContratoNombreInstrumento",									 	--C22
	   COALESCE(HEVT3."ValorDestino",F."ModalidadLiquidacionValor") AS "ContratoModalidadCumplimiento", --C23
	   COALESCE(HEVT4."ValorDestino",F."IdTipoOperacion") AS "ContratoPosicionDeclarante", 	--C24
	   TO_VARCHAR('1') as "ActivoSubyacenteTipo", 										 	--C25
	   CASE
			WHEN F."IdMonedaPrincipal" = 'UF' OR MONPRI."IdMoneda" = 'UF' THEN 'CLF'
			ELSE COALESCE(F."IdMonedaPrincipal", MONPRI."IdMoneda")
		END AS "ActivoSubyacenteCodigo",													--C26
	   TO_VARCHAR('') as "ActivoSubyacenteOtros",										 	--C27
	   TO_DECIMAL(0, 26, 8) as "ActivoSubyacenteTasaFija", 									--C28
	   TO_VARCHAR('0') as "SegundoActivoSubyacenteTipo",								 	--C29
	   TO_VARCHAR('') as "SegundoActivoSubyacenteCodigo",								 	--C30
	   TO_VARCHAR('') as "SegundoActivoSubyacenteOtros",								 	--C31
	   TO_DECIMAL(0, 26, 8) as "SegundoActivoSubyacenteTasaFija",							--C32
	   TO_VARCHAR('1') as "PrecioFuturoCodigoPrecio", 									 	--C33
	   F."TipoCambioCierre" as "PrecioFuturoPrecio",									 	--C34
	   CASE
			WHEN F."IdMonedaPrincipal" = 'UF' OR MONPRI."IdMoneda" = 'UF' THEN 'CLF'
			ELSE COALESCE(F."IdMonedaPrincipal", MONPRI."IdMoneda")
		END AS "PrecioFuturoMoneda",															--C35
	   COALESCE(HEVT."ValorDestino", TO_VARCHAR('13'))  as "Unidad",						--C36
	   COALESCE(F."MontoMonedaPrincipal",0) as "MontoCantidadContratadoNocional",		 	--C37
	   TO_VARCHAR('') as "SegundaUnidad",												 	--C38
	   '0' as "SegundoMontoNocional",											 			--C39
	   F."FechaVencimiento" as "FechaVencimiento",							 				--C40
	   CASE WHEN F."EstadoVirtual" IN ('1') THEN '0001-01-01' 
 		    WHEN F."EstadoVirtual" IN ('2','3') AND YEAR(TO_DATE(F."FechaContrato"))=YEAR(TO_DATE(F."FechaLiquidacion")) THEN F."FechaLiquidacion" 
	   ELSE '0001-01-01' 
	   END as "FechaLiquidacionEjercicioOpcion",
	   TO_VARCHAR('1') as "PrecioMercadoCodigoPrecio", -- En un futuro puede cambiar 	 	--C42 OK
	   COALESCE(F."PrecioVencimiento",0) as "PrecioMercadoPrecio",							--C43 OK
	   COALESCE(LastFinalValue."MontoValorRazonableNeto",LastYearValue."MontoValorRazonableNeto", 0)  as "ValorJustoContratoConfirnacion",	--C44 OK
	   CASE WHEN F."ContratoVencido"= 2 THEN (COALESCE(LastYearValue."MontoValorRazonableNeto", 0)) - COALESCE(DJ."ResultadoEjercicio",0)
	   ELSE (CASE 
	   		WHEN F."MontoRecibimos" = 0  	THEN -F."MontoPagamos" 
			WHEN F."MontoPagamos" = 0 		THEN F."MontoRecibimos"
			WHEN F."SpotAsociado" != 0 		THEN F."ValorSpotVencimiento"
	   		ELSE 0 END)
		- (COALESCE(DJ."ResultadoEjercicio",0))  END AS "ResultadoEjercicio",--C45 OK ResultadoEjercicio
	    CASE WHEN  (CASE WHEN F."MontoRecibimos" = 0 THEN -F."MontoPagamos" WHEN F."MontoPagamos" = 0 THEN F."MontoRecibimos"
	   ELSE 0 END) - (COALESCE(DJ."ResultadoEjercicio",0)) >= 0 THEN COALESCE(CTAPOS."Valor",'')
	   ELSE COALESCE(CTANEG."Valor",'') END
	   as "CuentaContableAsociadaResultadoEjercicio",					 					--C46 OK 
	   0 as "EfectoPatrimonio",												 				--C47 OK
	   '' as "CuentaContableAsociadaEfectoPatrimonio",						 				--C48 OK
	   0 as "ComisionPactada",												 				--C49 OK
	   0 as "PrimaTotal",													 	  		 	--C50 OK
	   0 as "InversionInicial",												  				--C51 OK
	   0 as "OtrosGastosAsociadosContrato",									 	 			--C52 OK
	   0 as "OtrosIngresosAsociadosContrato",								 				--C53 OK
	   0 as "PagosMontoPagadosAdeudados",	-- Cliente debe completar registro				--C54 OK
	   1 as "PagosExteriorModalidadPago",									 				--C55 OK
	   0 as "SaldoGarantiasCierre",											 				--C56 OK
	   F."IdCliente" as "IdCliente",
	   F."IdPortafolio" as "IdPortafolio",
	   F."FechaLiquidacion" AS "FechaLiquidacion",
	   F."FechaContrato",
	   F."FolioOperUI"
	FROM 
	(
		SELECT 
			x."IdCliente",
			x."IdPortafolio",
			x."ContratoVencido", 															    --C19 OK
			x."ContratoEstadoConfirmacion",													--C20 OK
			x."IdEmpresa",
			x."RutCliente",
			x."FechaLiquidacion",
			x."FechaVencimiento",
			x."IdMonedaPrincipal",
			x."ModalidadLiquidacionValor",
			x."IdTipoOperacion",
			x."MontoMonedaPrincipal",
			x."IdCartera",
			x."FechaOperacion",
			x."PrecioVencimiento",
			x."IdMonedaSecundaria",
			x."TipoCambioCierre",
			x."Utilidad",
			x."FolioOperacion",
			COALESCE (TIPOCAMBIO."Valor",1) * X."MontoRecibimos" AS "MontoRecibimos",
			COALESCE (TIPOCAMBIO."Valor",1) * X."MontoPagamos"	 AS "MontoPagamos",
			x."EstadoVirtual",
			COALESCE(x."IdMonedaLiquidacion",'CLP') AS "IdMonedaLiquidacion",
			x."TipoVencimiento",
    		x."FechaAnticipo",
			x."FechaContrato",
			COALESCE(x."SpotAsociado",0) AS "SpotAsociado",
			COALESCE(x."ValorSpotVencimiento",0) AS "ValorSpotVencimiento" ,
			CASE 
			WHEN x."TipoVencimiento" = 'ANTICIPADA' THEN ADD_DAYS(x."FechaAnticipo",-1)
			WHEN x."FechaLiquidacion" IS NULL THEN x."FechaVencimiento"
			WHEN x."FechaVencimiento" < x."FechaLiquidacion" THEN x."FechaVencimiento"
			ELSE x."FechaLiquidacion"
			END AS "FechaCorteHistorico",
			YEAR(
				CASE 
					WHEN x."TipoVencimiento" = 'ANTICIPADA' THEN ADD_DAYS(x."FechaAnticipo",-1)
					WHEN x."FechaLiquidacion" IS NULL THEN x."FechaVencimiento"
					WHEN x."FechaVencimiento" < x."FechaLiquidacion" THEN x."FechaVencimiento"
					ELSE x."FechaLiquidacion"
				END
			) AS "AnioCorteHistorico",
			x."FolioOperUI" as "FolioOperUI",
			RANK() OVER(PARTITION BY "FolioOperacion", EXTRACT(YEAR FROM "FechaContrato") ORDER BY "IdCartera" DESC,"EstadoVirtual" DESC) AS "Rank"
	FROM (  
		--FORWARD CON FECHA CONTRATO SUSCRITO O DE OPERACION: VIGENTES
		SELECT 
			a."IdCliente",
			a."IdPortafolio",
			2 as "ContratoVencido", 																 --C19 OK
			CASE 
					WHEN (a."Estado" IN ('VIG') AND (a."FechaVencimiento" >= a."FechaOperacion")) OR (a."Estado" IN ('VIG') AND (a."FechaVencimiento" IS NULL)) 
						THEN '1'
					WHEN (a."Estado" IN ('MOD') AND (a."FechaVencimiento" >= a."FechaOperacion")) OR (a."Estado" IN ('MOD') AND (a."FechaVencimiento" IS NULL)) 
						THEN '2'
					WHEN (a."Estado" IN ('VIG','MOD') AND (YEAR(a."FechaOperacion") < YEAR(a."FechaVencimiento")))
						THEN '3'
					ELSE '1'
			END as "ContratoEstadoConfirmacion",													--C20 OK
			a."IdEmpresa",
			a."RutCliente",
			a."FechaLiquidacion",
			a."FechaVencimiento",
			a."IdMonedaPrincipal",
			a."ModalidadLiquidacionValor",
			a."IdTipoOperacion",
			a."MontoMonedaPrincipal",
			a."IdCartera",
			a."FechaOperacion",
			CASE 
				WHEN (a."PrecioVencimiento" = 0 OR a."PrecioVencimiento" IS NULL) AND a."Nemotecnico" = 'ARBITR' THEN a."ParidadCierre"
				WHEN (a."PrecioVencimiento" = 0 OR a."PrecioVencimiento" IS NULL) AND a."Nemotecnico" <> 'ARBITR' THEN a."TipoCambioCierre"
				ELSE a."PrecioVencimiento"
			END AS "PrecioVencimiento",
			a."IdMonedaSecundaria",
			CASE WHEN a."TipoCambioCierre"=0 THEN a."ParidadCierre" ELSE a."TipoCambioCierre" END AS "TipoCambioCierre",
			a."Utilidad",
			a."FolioOperacion",
			a."MontoRecibimos",
			a."MontoPagamos",
			1 AS "EstadoVirtual",
			a."IdMonedaLiquidacion", 
			a."TipoVencimiento",
			a."FechaAnticipo",
			a."EGRIdSpotAsociado" AS "SpotAsociado",
			0 AS "ValorSpotVencimiento",
			"FechaOperacion" AS "FechaContrato",
			a."IdOperacionUI" AS "FolioOperUI"
		FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD" a
		WHERE a."Estado" <> 'ANU'

UNION ALL


--FORWARD CON FECHA CONTRATO LIQUIDACION o VENCIMIENTO

SELECT DAT.* FROM (
SELECT 
			a."IdCliente",
			a."IdPortafolio",
			1 as "ContratoVencido", 																--C19 OK
	   		'0' as "ContratoEstadoConfirmacion",													--C20 OK
			a."IdEmpresa",
			a."RutCliente",
			CASE a."TipoVencimiento" WHEN 'ANTICIPADA' THEN a."FechaAnticipo" ELSE a."FechaLiquidacion" END  AS "FechaLiquidacion",
			a."FechaVencimiento",
			a."IdMonedaPrincipal",
			a."ModalidadLiquidacionValor",
			a."IdTipoOperacion",
			a."MontoMonedaPrincipal",
			a."IdCartera",
			a."FechaOperacion",
			CASE 
				WHEN (a."PrecioVencimiento" = 0 OR a."PrecioVencimiento" IS NULL) AND a."Nemotecnico" = 'ARBITR' THEN a."ParidadCierre"
				WHEN (a."PrecioVencimiento" = 0 OR a."PrecioVencimiento" IS NULL) AND a."Nemotecnico" <> 'ARBITR' THEN a."TipoCambioCierre"
				ELSE a."PrecioVencimiento"
			END AS "PrecioVencimiento",
			a."IdMonedaSecundaria",
			CASE WHEN a."TipoCambioCierre"=0 THEN a."ParidadCierre" ELSE a."TipoCambioCierre" END AS "TipoCambioCierre",
			a."Utilidad",
			a."FolioOperacion",
			a."MontoRecibimos",
			a."MontoPagamos",
			3  AS "EstadoVirtual",
			a."IdMonedaLiquidacion",
			a."TipoVencimiento",
			a."FechaAnticipo",
			a."EGRIdSpotAsociado" AS "SpotAsociado",
			SPT."ResultadoMonedaLocalTCSpot" AS "ValorSpotVencimiento",
	   		CASE WHEN a."TipoVencimiento" ='ANTICIPADA' 			THEN a."FechaAnticipo"
	   			 WHEN a."FechaLiquidacion" IS NULL 					THEN a."FechaVencimiento"
	   			 WHEN a."FechaVencimiento" < a."FechaLiquidacion"	THEN a."FechaVencimiento" 
	   			ELSE a."FechaLiquidacion"
	   			END AS "FechaContrato",
			a."IdOperacionUI" AS "FolioOperUI"
		FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD" a
		LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" SPT ON SPT."IdOperacionSpot"=a."EGRIdSpotAsociado" AND SPT."Estado"<>'ANU' AND SPT."EstadoConfirmacion" ='CNF' AND
		 a."ModalidadLiquidacion" LIKE '%ENTREGA%'
		WHERE a."Estado" <> 'ANU'
	) AS DAT
	LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" PLA ON (PLA."CodigoParametro"='PLA.FechaSistema')
	WHERE DAT."FechaContrato" < PLA."Valor"
	
		) AS x
		LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero"  AS TIPOCAMBIO	ON (TIPOCAMBIO."Fecha"= ADD_DAYS(x."FechaContrato", -1)
		AND TIPOCAMBIO."IdParametroFinanciero" = CASE x."IdMonedaLiquidacion" WHEN 'USD' THEN 'DO' ELSE COALESCE(x."IdMonedaLiquidacion",'CLP') END)
		
	)
	AS F
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" MONPRI ON MONPRI."IdMoneda" =F."IdMonedaPrincipal"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresCliente" REL on (REL."IdCliente" = F."IdCliente" and REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='DJ1820')
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" b on (b."IdCliente" = F."IdCliente")
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON (CLI."IdCliente"=F."IdCliente")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON  PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarPais" PAIS ON PAIS."IdPais"=PER."IdPaisResidencia"
LEFT JOIN "BSCL.Normativa.BD::TRI.DatosDJ1829" AS DJ --Obtener resultado ejercicio del periodo anterior cuando fecha vencimiento es del periodo actual
			ON  DJ."Numero"  = F."FolioOperacion"
			AND YEAR(DJ."FechaContrato") = (YEAR(F."FechaContrato")-1) 
			AND YEAR(DJ."FechaVencimiento") = YEAR(F."FechaVencimiento")


LEFT JOIN
(
    SELECT
        HISDEC."FolioOperacion",
        HISDEC."RutCliente",
        HISDEC."IdCartera",
        HISDEC."MontoValorRazonableNeto",
        HISDEC."FechaOperacion",
        HISDEC."FechaRespaldo",
        YEAR(HISDEC."FechaRespaldo") AS "AnioRespaldo"
    FROM
    (
        SELECT
            h."FolioOperacion",
            h."RutCliente",
            h."MontoValorRazonableNeto",
            h."FechaOperacion",
            h."FechaRespaldo",
            h."IdCartera",
            RANK() OVER
            (
                PARTITION BY h."FolioOperacion", h."RutCliente", YEAR(h."FechaRespaldo")
                ORDER BY h."FechaRespaldo" DESC
            ) AS "RankNum"
        FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDHistorico" AS h
    ) AS HISDEC
    WHERE HISDEC."RankNum" = 1
) AS LastYearValue
    ON (LastYearValue."FolioOperacion" = F."FolioOperacion" OR LastYearValue."IdCartera" = F."IdCartera")
   AND LastYearValue."RutCliente" = F."RutCliente"
   AND LastYearValue."AnioRespaldo" = YEAR(F."FechaContrato")
   AND YEAR(F."FechaContrato") < F."AnioCorteHistorico"


LEFT JOIN
(
    SELECT
        HISFIN."FolioOperacion",
        HISFIN."RutCliente",
        HISFIN."IdCartera",
        HISFIN."MontoValorRazonableNeto",
        HISFIN."FechaOperacion",
        HISFIN."FechaRespaldo"
    FROM
    (
        SELECT
            h."FolioOperacion",
            h."RutCliente",
            h."MontoValorRazonableNeto",
            h."FechaOperacion",
            h."FechaRespaldo",
            h."IdCartera"
        FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDHistorico" AS h
    ) AS HISFIN
) AS LastFinalValue
    ON (LastFinalValue."FolioOperacion" = F."FolioOperacion" OR LastFinalValue."IdCartera" = F."IdCartera")
   AND LastFinalValue."RutCliente" = F."RutCliente"
   AND YEAR(F."FechaContrato") = F."AnioCorteHistorico"
   AND YEAR(LastFinalValue."FechaRespaldo") = YEAR(F."FechaContrato")
   AND LastFinalValue."FechaRespaldo" =
        (
            SELECT MAX(h2."FechaRespaldo")
            FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDHistorico" h2
            WHERE (h2."FolioOperacion" = F."FolioOperacion" OR h2."IdCartera" = F."IdCartera")
              AND h2."RutCliente" = F."RutCliente"
              AND YEAR(h2."FechaRespaldo") = YEAR(F."FechaContrato")
              AND h2."FechaRespaldo" <= F."FechaCorteHistorico"
        )




LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HEVT ON (HEVT."Esquema"='DJ1829' AND F."IdMonedaLiquidacion" =HEVT."ValorOrigen" AND HEVT."Tipo"='UNIDADMONEDA' AND F."FechaContrato" BETWEEN TO_DATE(HEVT."FechaDatosDesde") AND TO_DATE(HEVT."FechaDatosHasta"))
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HEVT2 ON (HEVT2."Esquema"='DJ1829' AND F."IdMonedaPrincipal"=HEVT2."ValorOrigen" AND HEVT2."Tipo"='UNIDADMONEDA' AND F."FechaContrato" BETWEEN TO_DATE(HEVT2."FechaDatosDesde") AND TO_DATE(HEVT2."FechaDatosHasta"))
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HEVT3 ON (HEVT3."Esquema"='DJ1829' AND F."ModalidadLiquidacionValor"=HEVT3."ValorOrigen" AND HEVT3."Tipo"='MODALIDADCUMPLIMIENTO' AND F."FechaContrato" BETWEEN TO_DATE(HEVT3."FechaDatosDesde") AND TO_DATE(HEVT3."FechaDatosHasta"))
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HEVT4 ON (HEVT4."Esquema"='DJ1829' AND F."IdTipoOperacion"=HEVT4."ValorOrigen" AND HEVT4."Tipo"='POSICIONDECLARANTE' AND F."FechaContrato" BETWEEN TO_DATE(HEVT4."FechaDatosDesde") AND TO_DATE(HEVT4."FechaDatosHasta"))
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HEVT5 ON (HEVT5."Esquema"='DJ1829' AND PAIS."CodigoPais"=HEVT5."ValorOrigen" AND HEVT5."Tipo"='PAISCONTRA' AND F."FechaContrato" BETWEEN TO_DATE(HEVT5."FechaDatosDesde") AND TO_DATE(HEVT5."FechaDatosHasta"))
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::TRI.DJ1820.HomologacionTipoIndicador" HREL ON (HREL."Esquema" = 'DJ1820' AND HREL."CodigoTipoIndicador"= REL."CodigoTipoIndicador" AND HREL."Codigo"=REL."CodigoValorIndicador" AND TO_DATE(F."FechaContrato") BETWEEN TO_DATE(HREL."FechaDatosDesde") AND TO_DATE(HREL."FechaDatosHasta"))
LEFT OUTER JOIN  "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAnexo" ANE ON (ANE."IdCliente"=F."IdCliente" AND ANE."IdTipoDocumento"='CONTRATOCONDICIONESGRALES' AND  ANE."IdEstado"='ACT')
LEFT OUTER JOIN "BSCL.Normativa.BD::NOR.Configuracion" CTAPOS ON  (CTAPOS."IdNormativa"='DJ1829' AND CTAPOS."CodigoParametro" = 'TRI_CTACONTABLE_DJ1829_POS')
LEFT OUTER JOIN "BSCL.Normativa.BD::NOR.Configuracion" CTANEG ON  (CTANEG."IdNormativa"='DJ1829' AND CTANEG."CodigoParametro" = 'TRI_CTACONTABLE_DJ1829_NEG')
WHERE "Rank" = 1

UNION ALL 

SELECT 
	 "IdEmpresa"
	 ,0 as "ID"
     ,1 as "IdDocumento" 
     ,'N' as "VboUsuario"
     ,'' as "Observaciones"
     ,'' as "FuenteDatos"
     ,0 as "Numero"												--C59
	,"RutCliente" as "RutContraparte" 							--C8
 	,"TaxID" as "TaxIDContraparte" 								--C10
 	,"CodigoPaisResidencia" as "CodigoPaisContraparte" 			--C11
 	,"CodigoRelacion" as "TipoRelacionContraparte"				--C12
 	,"CodigoModoContrato" as "ModalidadContratacion" 			--C13
 	,"TipoAcuerdoMarco"  as "AcuerdoMarcoTipo" 					--C14
 	,"NumeroAcuerdoMarco" as "AcuerdoMarcoNumero"				--C15
 	,"FechaSuscripcionAcuerdo" as "AcuerdoMarcoFechaSuscripcion"--C16
 	,"NumeroContrato"  as "ContratoNumero" 						--C17
 	,"FechaSuscripcionContrato" as "ContratoFechaSuscripcion"   --C18
 	,1 as "ContratoVencido" 																		--C19 OK
	,'0' as "ContratoEstadoConfirmacion"
 	,"TipoContrato"  as "ContratoTipo"							--C21
 	,"Instrumento"	as "ContratoNombreInstrumento"				--C22
 	,"ModalidadCumplimiento" AS "ContratoModalidadCumplimiento" --C23
 	,"CodigoPosicionDeclarante" AS "ContratoPosicionDeclarante" --C24								
	,"TipoActivoSubyacente"	as "ActivoSubyacenteTipo"			--C25
	,"CodigoActivoSubyacente" as "ActivoSubyacenteCodigo"		--C26
	,"OtroActivoSubyacente" as "ActivoSubyacenteOtros"			--C27
	,"TasaActivoSubyacente" as "ActivoSubyacenteTasaFija"		--C28
	,"TipoSegundoActivoSubyacente"  as "SegundoActivoSubyacenteTipo" 	--C29
	,"CodigoSegundoActivoSubyacente" as "SegundoActivoSubyacenteCodigo" --C30
	,"OtroSegundoActivoSubyacente" as "SegundoActivoSubyacenteOtros"	--C31								
	,"TasaSegundoActivoSubyacente" as "SegundoActivoSubyacenteTasaFija"	--C32
	,"CodigoPrecioFuturo" as "PrecioFuturoCodigoPrecio"					--C33
	,"PrecioFuturo" as "PrecioFuturoPrecio"								--C34
	,"CodigoMonedaFuturo" as "PrecioFuturoMoneda"						--C35
	,"CodigoUnidad" as "Unidad"											--C36
	,"MontoNocional" as "MontoCantidadContratadoNocional"				--C37
	,"CodigoSegundaUnidad" as "SegundaUnidad"							--C38
	,"SegundoMontoNocional" as "SegundoMontoNocional"					--C39
	,"FechaVencimiento" as "FechaVencimiento"							--C40
	,"FechaLiquidacion" as "FechaLiquidacionEjercicioOpcion"			--C41
    ,TO_VARCHAR('1') as "PrecioMercadoCodigoPrecio"						--C42
 	,1 as "PrecioMercadoPrecio"											--C43
    ,0 as "ValorJustoContratoConfirnacion"								--C44
    ,0 as "ResultadoEjercicio"											--C45
    ,'' as "CuentaContableAsociadaResultadoEjercicio"					--C46
    ,0 as "EfectoPatrimonio"											--C47
    ,'' as "CuentaContableAsociadaEfectoPatrimonio"						--C48
    ,0 as "ComisionPactada"												--C49
    ,0 as "PrimaTotal"													--C50
    ,0 as "InversionInicial"											--C51
    ,0 as "OtrosGastosAsociadosContrato"								--C52
    ,0 as "OtrosIngresosAsociadosContrato"								--C53
    ,0 as "PagosMontoPagadosAdeudados"									--C54
    ,0 as "PagosExteriorModalidadPago"									--C55
    ,0 as "SaldoGarantiasCierre"										--C56
	,"IdCliente" as "IdCliente"
	,"IdPortafolio" as "IdPortafolio"
	,YEAR("FechaSuscripcionContrato") || '-12-31' AS "FechaLiquidacion"
	,"FechaSuscripcionContrato" AS "FechaContrato"
	,0
FROM "BSCL.Custodia.BD.Vistas::DER.ConsultaMovimientosLiquidacionesOpciones"

UNION ALL 

SELECT 
	 "IdEmpresa"
	 ,0 as "ID"
     ,1 as "IdDocumento" 
     ,'N' as "VboUsuario"
     ,'' as "Observaciones"
     ,'' as "FuenteDatos"
     ,0 as "Numero"
 	,"RutCliente" as "RutContraparte" 							--C8
 	,"TaxID" as "TaxIDContraparte" 								--C10
 	,"CodigoPaisResidencia" as "CodigoPaisContraparte" 			--C11
 	,"CodigoRelacion" as "TipoRelacionContraparte"				--C12
 	,"CodigoModoContrato" as "ModalidadContratacion" 			--C13
 	,"TipoAcuerdoMarco"  as "AcuerdoMarcoTipo" 					--C14
 	,"NumeroAcuerdoMarco" as "AcuerdoMarcoNumero"				--C15
 	,"FechaSuscripcionAcuerdo" as "AcuerdoMarcoFechaSuscripcion"--C16
 	,"NumeroContrato"  as "ContratoNumero" 						--C17
 	,"FechaSuscripcionContrato" as "ContratoFechaSuscripcion"   --C18
 	,'1' as "ContratoVencido" 																		--C19 OK
	,'0' as "ContratoEstadoConfirmacion"													--C20 OK
 	,"TipoContrato"  as "ContratoTipo"							--C21
 	,"Instrumento"	as "ContratoNombreInstrumento"				--C22
 	,"ModalidadCumplimiento" AS "ContratoModalidadCumplimiento" --C23
 	,"CodigoPosicionDeclarante" AS "ContratoPosicionDeclarante" --C24								
	,"TipoActivoSubyacente"	as "ActivoSubyacenteTipo"			--C25
	,"CodigoActivoSubyacente" as "ActivoSubyacenteCodigo"		--C26
	,"OtroActivoSubyacente" as "ActivoSubyacenteOtros"			--C27
	,"TasaActivoSubyacente" as "ActivoSubyacenteTasaFija"		--C28
	,"TipoSegundoActivoSubyacente"  as "SegundoActivoSubyacenteTipo" 	--C29
	,"CodigoSegundoActivoSubyacente" as "SegundoActivoSubyacenteCodigo" --C30
	,"OtroSegundoActivoSubyacente" as "SegundoActivoSubyacenteOtros"	--C31								
	,"TasaSegundoActivoSubyacente" as "SegundoActivoSubyacenteTasaFija"	--C32
	,"CodigoPrecioFuturo" as "PrecioFuturoCodigoPrecio"					--C33
	,"PrecioFuturo" as "PrecioFuturoPrecio"								--C34
	,"CodigoMonedaFuturo" as "PrecioFuturoMoneda"						--C35
	,"CodigoUnidad" as "Unidad"											--C36
	,"MontoNocional" as "MontoCantidadContratadoNocional"				--C37
	,"CodigoSegundaUnidad" as "SegundaUnidad"							--C38
	,"SegundoMontoNocional" as "SegundoMontoNocional"					--C39
	,"FechaVencimiento" as "FechaVencimiento"							--C40
	,"FechaLiquidacion" as "FechaLiquidacionEjercicioOpcion"			--C41
    ,TO_VARCHAR('1') as "PrecioMercadoCodigoPrecio"						--C42
 	,1 as "PrecioMercadoPrecio"											--C43
    ,0 as "ValorJustoContratoConfirnacion"								--C44
    ,0 as "ResultadoEjercicio"											--C45
    ,'' as "CuentaContableAsociadaResultadoEjercicio"					--C46
    ,0 as "EfectoPatrimonio"											--C47
    ,'' as "CuentaContableAsociadaEfectoPatrimonio"						--C48
    ,0 as "ComisionPactada"												--C49
    ,0 as "PrimaTotal"													--C50
    ,0 as "InversionInicial"											--C51
    ,0 as "OtrosGastosAsociadosContrato"								--C52
    ,0 as "OtrosIngresosAsociadosContrato"								--C53
    ,0 as "PagosMontoPagadosAdeudados"									--C54
    ,0 as "PagosExteriorModalidadPago"									--C55
    ,0 as "SaldoGarantiasCierre"										--C56
	,"IdCliente" as "IdCliente"
	,"IdPortafolio" as "IdPortafolio"
	,YEAR("FechaSuscripcionContrato") || '-12-31' AS "FechaLiquidacion"
	,"FechaSuscripcionContrato" AS "FechaContrato"
	,0
 
FROM "BSCL.Custodia.BD.Vistas::DER.ConsultaMovimientosLiquidacionesFuturo"

with read only;

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1829.Origen" to bcs_desarrollador with grant option;
