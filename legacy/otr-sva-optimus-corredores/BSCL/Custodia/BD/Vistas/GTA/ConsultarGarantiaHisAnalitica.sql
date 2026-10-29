SET SCHEMA "BSCL";
DROP VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiaHisAnalitica";

CREATE VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiaHisAnalitica" AS 
SELECT
	 TO_CHAR(A."FechaRespaldo",'YYYY-MM-DD') as "FechaRespaldo",
	 A."IdGarantia" ,
	 A."FolioGarantia" ,
	 A."FolioCarteraOrigen" ,
	 A."FolioCarteraGarantizada" ,
	 A."EsEspecifica" ,
	 CASE WHEN A."IdInstrumentoCarteraGarantizada" = 0 THEN NULL ELSE A."IdInstrumentoCarteraGarantizada" END AS "IdInstrumentoCarteraGarantizada",
	 coalesce(CAST(Y."Nemotecnico" AS NVARCHAR(40)),'') as "NemotecnicoGarantizado" ,
	 CASE WHEN Y."IdTipoInstrumento" = '' then null else Y."IdTipoInstrumento" end AS "IdTipoInstrumentoGarantizado",
	 CASE WHEN A."IdContraparteGarantia" = 0 THEN NULL ELSE A."IdContraparteGarantia" END AS "IdContraparteGarantia"  ,
	 CAST(COALESCE(PER."RazonSocial",	'') AS NVARCHAR(100)) as "RazonSocialContraparte",
	 CASE WHEN A."IdOrigenGarantia" = '' THEN NULL ELSE A."IdOrigenGarantia" END AS "IdOrigenGarantia",
	 CAST(F."Nombre" AS NVARCHAR(40)) as "NombreOrigenGarantia" ,
	 CASE WHEN A."IdEspecieGarantia" = 0 THEN NULL ELSE A."IdEspecieGarantia" END AS "IdEspecieGarantia",
	 CAST(IFNULL(C."Descripcion",	'') AS NVARCHAR(100)) as "DescripcionEspecie" ,
	 CASE WHEN IFNULL(A."FechaIngresoRegistro",'')= '' THEN '0001-01-01 00:00:00' 
					 	                              ELSE  
					 	                                  CASE WHEN  A."FechaIngresoRegistro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
					 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (A."FechaIngresoRegistro", CONFIGURACION."Valor")),19)
					 	                                  END 
	  END as "FechaIngresoRegistro" ,
	  CASE WHEN IFNULL(A."FechaModificacionRegistro",'')= '' THEN '0001-01-01 00:00:00' 
					 	                              ELSE  
					 	                                  CASE WHEN   A."FechaModificacionRegistro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
					 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS ( A."FechaModificacionRegistro", CONFIGURACION."Valor")),19)
					 	                                  END 
	  END  as "FechaModificacionRegistro" ,
	 A."IdEmpresa" ,
	 CASE WHEN A."IdUsuario" = 0 THEN NULL ELSE A."IdUsuario" END AS "IdUsuario",
	 CASE WHEN A."IdModulo" = '' THEN NULL ELSE A."IdModulo" END AS "IdModulo",
	 CASE WHEN A."IdCliente" = 0 THEN NULL ELSE A."IdCliente" END AS "IdCliente",
	 CASE WHEN A."IdPortafolio"  = 0 THEN NULL ELSE A."IdPortafolio" END AS "IdPortafolio",
	 CAST(PERCLIENTE."NombreCompleto" AS NVARCHAR(302)) as "NombreCliente" ,
	 CAST(IDENT."DocumentoIdentidad" AS NVARCHAR(40)) AS "DocumentoIdentidad",
	 CAST(por."CodigoPortafolio" AS NVARCHAR(100)) AS "CodigoPortafolio",
	 CASE WHEN A."IdEstado" = '' THEN NULL ELSE A."IdEstado" END AS "IdEstado",
	 CAST(Z."Nombre" AS NVARCHAR(100)) as "NombreEstado" ,
	 CASE WHEN car."IdTipoProducto" = '' THEN NULL ELSE  car."IdTipoProducto" END as "IdTipoProductoOperacionOrigen" ,
	 CASE WHEN A."IdTipoCanalIngreso" = '' THEN NULL ELSE A."IdTipoCanalIngreso" END AS "IdTipoCanalIngreso" ,
	 CASE WHEN A."IdMonedaSistema" = '' THEN NULL ELSE A."IdMonedaSistema" END AS "IdMonedaSistema",
	 CAST(CONCAT(COALESCE(CONCAT (H."Codigo", ' - '),	''), COALESCE(H."Nombre",'')) AS NVARCHAR(143)) as "NombreMonedaSistema",
	 CAST(A."CodigoIFRS" AS NVARCHAR(40)) AS "CodigoIFRS" ,
	 CASE WHEN A."IdConcepto" = '' THEN NULL ELSE A."IdConcepto" END AS "IdConcepto",
	 CAST(B."Nombre" AS NVARCHAR(100)) as "NombreConcepto" ,
	 CASE WHEN CAR."IdTipoOperacion" = '' THEN NULL ELSE CAR."IdTipoOperacion" END as "IdTipoOperacionOperacionOrigen"  ,
	 LEFT(TO_CHAR(IFNULL(A."FechaOperacion",'0001-01-01')),10) as "FechaOperacion" ,
	 LEFT(TO_CHAR(IFNULL(A."FechaVencimientoOperacion",'0001-01-01')),10) as "FechaVencimientoOperacion" ,
     A."PlazoEnDiasOperacion" ,
	 A."FolioExternoReferencia" ,
	 CASE WHEN car."IdMesa" = 0 THEN NULL ELSE car."IdMesa" END as "IdMesaOperacionOrigen" ,
	 CASE WHEN car."IdBolsa" is null THEN 'BCS' ELSE car."IdBolsa" END as "IdBolsaOperacionOrigen" ,
	 --	J."Nombre" as "NombreBolsa" 				,
     CASE WHEN A."IdSucursal" = 0 THEN NULL ELSE A."IdSucursal" END AS "IdSucursal" ,
	 CASE WHEN A."IdAgente" = 0 THEN NULL ELSE A."IdAgente" END AS "IdAgente",
	 A."IdCondicionLiquidacion" ,
	 LEFT(TO_CHAR(IFNULL(A."FechaLiquidacion",'0001-01-01')),10) as "FechaLiquidacion" ,
	 CASE WHEN A."IdClasificacionTipoInstrumento" = '' THEN NULL ELSE A."IdClasificacionTipoInstrumento" END AS "IdClasificacionTipoInstrumento",
	 coalesce(CAST(i."Nombre" AS NVARCHAR(100)),'') as "ClasificacionTipoInstrumento"  ,
	 case when A."IdTipoInstrumento"  = '' THEN NULL ELSE A."IdTipoInstrumento" END AS "IdTipoInstrumento",
	 coalesce(CAST(D."Nombre" AS NVARCHAR(100)),'') as "NombreTipoInstrumento" ,
	 CASE WHEN A."IdInstrumento" in ( 0,-1) THEN NULL ELSE A."IdInstrumento" END AS "IdInstrumento" ,
	 CASE WHEN A."IdClasificacionTipoInstrumento" ='IF' THEN CAST(A."NemoSVS" AS NVARCHAR(40))ELSE CAST(A."Nemotecnico" AS NVARCHAR(40)) END as "Nemotecnico" ,
	 CASE when A."IdOrigenGarantia" = 'CUS' 
		THEN CAST(COALESCE(INS."Nemotecnico",	 '') AS NVARCHAR(100)) when A."IdOrigenGarantia" = 'CTA' 
		THEN CAST('GARANTIA EN EFECTIVO' AS NVARCHAR(100)) when A."IdOrigenGarantia" = 'ESP' 
		THEN CAST('GARANTIA EN ESPECIE' AS NVARCHAR(100)) 
	 END AS "NombreInstrumento",
	 CASE WHEN A."IdEmisor" = '' THEN 'OPTIMUS' ELSE CAST(A."IdEmisor" AS NVARCHAR(40))END AS "IdEmisor",
	-- A."NemoSVS" ,
	 CASE WHEN A."IdTipoReajuste" = '' THEN 'CLP' ELSE A."IdTipoReajuste" END AS "IdTipoReajuste"  ,
	 LEFT(TO_CHAR(IFNULL(A."FechaEmisionInstrumento",'0001-01-01')),10) as "FechaEmisionInstrumento" ,
	 LEFT(TO_CHAR(IFNULL(A."FechaVencimientoInstrumento",'0001-01-01')),10) as "FechaVencimientoInstrumento" ,
	 A."EsFungible" ,
	 CASE WHEN car."IdBoveda" is null THEN 'COR' ELSE car."IdBoveda" END  as "IdBovedaOperacionOrigen",
	 CASE WHEN A."IdMonedaInstrumento" = '' THEN NULL ELSE A."IdMonedaInstrumento" END AS "IdMonedaInstrumento"  ,
	 A."MontoGarantiaMonedaInstrumento" ,
	 CONCAT(COALESCE(CONCAT (H2."Codigo",	 ' - '),	''),	 COALESCE(H2."Nombre",	'')) as "NombreMonedaInstrumento",
	 --	E."IdClasificacionRiesgo" AS "IdClasificacionRiesgoInstrumento",
     coalesce(CAST(R."ClasificacionRiesgo" AS NVARCHAR(20)),'') AS "ClasificacionRiesgo",
     case when car."IdTipoComprobante" = '' THEN NULL ELSE car."IdTipoComprobante" END  as "IdTipoComprobanteOperacionOrigen",
	 coalesce(car."FolioComprobante",0) as "FolioComprobanteOperacionOrigen",
	 LEFT(TO_CHAR(IFNULL(car."FechaComprobante",'0001-01-01')),10) as "FechaComprobanteOperacionOrigen",
	 A."Cantidad" ,
	 A."PrecioGarantia" ,
	 A."MontoGarantia" ,
	 A."CorteMinimo" ,
	 A."ValorRescate" ,
	 A."PrecioCompraOperacionOrigen" ,
	 A."TasaCompraOperacionOrigen" ,
	 A."PrecioPromedioCompraOperacionOrigen",
	 A."MontoPrecioPromedioCompraOperacionOrigen",
	 A."MontoPrecioPromedioMonedaInstrumentoOperacionOrigen",
	 coalesce(car."ValorTipoCambioCierre",0) as "ValorTipoCambioCierre",
	 LEFT(TO_CHAR(IFNULL(A."INGFechaOperacion",'0001-01-01')),10) as "INGFechaOperacion" ,
	 LEFT(TO_CHAR(IFNULL(A."INGFechaLiquidacion",'0001-01-01')),10) as "INGFechaLiquidacion" ,
	 A."INGCantidad" ,
	 CASE WHEN A."IdOrigenGarantia" = 'CUS' 
		THEN A."INGCantidad" WHEN A."IdOrigenGarantia" = 'CTA' 
		THEN 1 WHEN A."IdOrigenGarantia" = 'ESP' 
		THEN 1 
	 END AS "CantidadGarantia",
	 A."INGPrecioGarantia" ,
	 A."INGValorRescate" ,
	 A."INGMontoATasaMercadoActual" as "INGMonto",
	 A."INGMontoATasaCompraActual" ,
	 CASE WHEN A."IdOrigenGarantia" = 'CUS' 
		THEN A."INGMontoATasaCompraActual" -- Monto valorizado
 		WHEN A."IdOrigenGarantia" = 'CTA' 
		THEN A."INGCantidad" -- Para garantias en dinero no se valoriza
 		WHEN A."IdOrigenGarantia" = 'ESP' 
		THEN A."MontoGarantia" -- Para garantias en especies no se valoriza
 	 END AS "MontoValorizado",
	 A."INGMontoATasaCompraAnterior" ,
	 A."INGPrecioMercadoActual" ,
	 A."INGTasaMercadoActual" ,
	 A."INGMontoATasaMercadoActual" ,
	 A."INGMontoATasaMercadoAnterior" ,
	 CASE WHEN aval."IdTipoInstrumento" = '' THEN NULL ELSE aval."IdTipoInstrumento" END as "IdTipoInstrumentoAval",
	 coalesce(CAST(tipoaval."Nombre" AS NVARCHAR(100)),'') as "NombreTipoInstrumentoAval",
	 CASE WHEN tipoaval."IdClasificacionTipoInstrumento" = '' THEN NULL ELSE tipoaval."IdClasificacionTipoInstrumento" END as "IdClasificacionTipoInstrumentoAval",
	 coalesce(CAST(clasifaval."Nombre" AS NVARCHAR(100)),'') as "NombreClasificacionTipoInstrumentoAval",
	 coalesce(CAST(E."ValorClasificacionRiesgo" AS NVARCHAR(20)),'') as "ValorClasificacionRiesgo",
	 CASE WHEN E."IdEntidadClasificadoraRiesgo" = '' THEN NULL ELSE E."IdEntidadClasificadoraRiesgo" END as "IdEntidadClasificadoraRiesgo",
	 CASE WHEN TCRiesgo."IdTipoClasificacionRiesgo" = '' THEN NULL ELSE TCRiesgo."IdTipoClasificacionRiesgo" END   as "IdTipoClasificacionRiesgo",
	 A."INGDurationCompraActual",
	 A."INGPrecioMercadoProximo",
	 A."INGTasaMercadoProximo",
	 A."INGMontoATasaCompraProximo",
	 A."INGMontoATasaMercadoProximo",
	 A."INGMontoInteresCompraProximo" ,
	 A."INGDurationCompraProximo" ,
	 A."INGMontoReajusteCompraProximo" ,
	 CAST(A."INGFuenteTasaPrecioMercadoActual" AS NVARCHAR(40)) AS "INGFuenteTasaPrecioMercadoActual",
	 LEFT(TO_CHAR(IFNULL(A."INGFechaValorizacionGarantia",'0001-01-01')),10) as "INGFechaValorizacionGarantia" ,
	 A."INGPrecioMercadoAnterior" ,
	 A."EGRCantidad" ,
	 LEFT(TO_CHAR(IFNULL(A."EGRFechaLiquidacion",'0001-01-01')),10) as "EGRFechaLiquidacion" ,
	 LEFT(TO_CHAR(IFNULL(A."EGRFechaOperacion",'0001-01-01')),10) as "EGRFechaOperacion" ,
	 coalesce(CAST(car."ISIN" AS NVARCHAR(40)),'') AS "ISIN",
	 A."INGMontoInteresCorridoCompraActual" ,
	 A."INGMontoReajusteCorridoCompraActual" ,
	 A."INGMontoInteresCorridoMercadoActual" ,
	 A."INGMontoReajusteCorridoMercadoActual" ,
	 LEFT(TO_CHAR(IFNULL(A."INGFechaValorizacionAnterior",'0001-01-01')),10) as "INGFechaValorizacionAnterior" ,
	 LEFT(TO_CHAR(IFNULL(A."INGFechaValorizacionActual",'0001-01-01')),10) as "INGFechaValorizacionActual" ,
	 coalesce((car."CorteMinimo"*A."INGCantidad"),0) AS "ValorNominal",
	 CASE WHEN A."EGRIdConcepto" = '' THEN NULL ELSE A."EGRIdConcepto" END AS "EGRIdConcepto",
	 CONCUS."EsGarantiaPrendaria" 
FROM "BSCL.Custodia.BD::HIS.Garantia" A 
CROSS JOIN (select top 1 "Valor" from "BSCL.Plataforma.BD::PLA.Configuracion"
			            where "CodigoParametro" = 'PLA.SegundosZonaHoraria') AS CONFIGURACION 
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoCartera" as ultCar on ultCar."IdEmpresa" = A."IdEmpresa" 
and ultCar."FolioCartera" = A."FolioCarteraOrigen" 
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" as car on car."IdCartera" = ultCar."IdCartera" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" B ON B."IdConcepto" = A."IdConcepto" 
LEFT JOIN "BSCL.Custodia.BD::CUS.ConceptoCustodia" CONCUS ON B."IdConcepto" = CONCUS."IdConcepto" AND CONCUS."NombreEntidad" = 'CUS.GARANTIA'
LEFT JOIN "BSCL.Custodia.BD::GTA.EspecieEnGarantia" C ON C."IdEspecieGarantia" = A."IdEspecieGarantia" 
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" i ON i."IdClasificacionTipoInstrumento" = A."IdClasificacionTipoInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" D ON D."IdTipoInstrumento" = A."IdTipoInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = A."IdInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" E ON E."IdInstrumento" = A."IdInstrumento"  AND E."IdEntidadClasificadoraRiesgo" 
	IN (select top 1 "Valor" from "BSCL.Plataforma.BD::PLA.Configuracion" where "CodigoParametro" = 'PLA.EntidadClasificadora')
LEFT JOIN "BSCL.Custodia.BD::GTA.OrigenGarantia" F ON F."IdOrigenGarantia" = A."IdOrigenGarantia" 
LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte" CONTRAPARTE ON CONTRAPARTE."IdContraparte" = A."IdContraparteGarantia" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CONTRAPARTE."IdPersona" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" H on H."IdMoneda" = A."IdMonedaSistema" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" H2 on H2."IdMoneda" = A."IdMonedaInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS Z ON Z."IdEstado" = A."IdEstado" 
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" Y ON Y."IdInstrumento" = A."IdInstrumentoCarteraGarantizada" 
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS aval ON aval."IdInstrumento" = A."IdInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoaval ON tipoaval."IdTipoInstrumento" = A."IdTipoInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS clasifaval ON clasifaval."IdClasificacionTipoInstrumento" = tipoaval."IdClasificacionTipoInstrumento" 
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" cli on cli."IdCliente" = A."IdCliente" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" pergta on pergta."IdPersona" = CLI."IdPersona" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "IDENT" ON pergta."IdDocumentoIdentidad" = "IDENT"."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PERCLIENTE ON PERCLIENTE."IdPersona" = pergta."IdPersona"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" por on por."IdPortafolio" = A."IdPortafolio" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo" TCRiesgo on TCRiesgo."IdClasificacionTipoInstrumento" = tipoaval."IdClasificacionTipoInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" R ON R."ClasificacionRiesgo" = E."ValorClasificacionRiesgo" 
AND R."IdEntidadClasificadoraRiesgo" = E."IdEntidadClasificadoraRiesgo" 
AND R."IdTipoClasificacionRiesgo" = TCRiesgo."IdTipoClasificacionRiesgo";

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiaHisAnalitica" TO BCS_DESARROLLADOR WITH GRANT OPTION;
