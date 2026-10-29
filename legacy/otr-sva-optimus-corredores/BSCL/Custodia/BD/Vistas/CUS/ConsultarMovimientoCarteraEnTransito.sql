SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEnTransito";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEnTransito" AS
SELECT
	Mov."IdMovimientoCarteraEnTransito"
   ,Mov."FechaIngresoRegistro"
   ,Mov."FechaCertificacionRegistro"
   ,Mov."FechaAnulacionRegistro"
   ,Mov."IdTipoComprobante"
   ,Mov."FolioComprobante"
   --,0 as "FolioCartera"
   ,Mov."IdMesa" as "IdMesa" -- ???? dafuq
   /*
   ,CASE conf."IdAccionOperacion" WHEN 'ING' THEN 'INGRESO' WHEN 'RET' THEN 'RETIRO' ELSE NULL END AS "IndicadorIngEgr"
   ,CASE conf."IdAccionOperacion" WHEN 'ING' THEN 'I' WHEN 'RET' THEN 'R' ELSE NULL END AS "IndicadorIngEgrAbr"*/

   ,CASE
           WHEN conf."IdAccionOperacion" = 'ING' AND mov."IdTipoOperacion" = 'INGTIT' THEN 'INGRESO'
           WHEN conf."IdAccionOperacion" = 'ING' AND mov."MueveCustodia" = 'S' THEN 'INGRESO'
           WHEN conf."IdAccionOperacion" = 'RET' AND mov."MueveCustodia" = 'N' THEN 'INGRESO'
           WHEN conf."IdAccionOperacion" = 'ING' AND mov."MueveCustodia" = 'N' AND mov."IdTipoOperacion" = 'COMPAC' THEN 'INGRESO'
           WHEN conf."IdAccionOperacion" = 'ING' AND mov."MueveCustodia" = 'N' THEN 'RETIRO'
           WHEN conf."IdAccionOperacion" = 'VEN' AND mov."IdTipoOperacion" = 'VESIMV' THEN 'RETIRO'  -- Vencimiento simultanea venta
           WHEN conf."IdAccionOperacion" = 'VEN' AND mov."IdTipoOperacion" = 'VESIMC' THEN 'RETIRO'  -- Vencimiento simultanea compra
           WHEN conf."IdAccionOperacion" = 'RET' THEN 'RETIRO'
           ELSE NULL
       END AS "IndicadorIngEgr"
   ,CASE
           WHEN conf."IdAccionOperacion" = 'ING' AND mov."IdTipoOperacion" = 'INGTIT' THEN 'I'
           WHEN conf."IdAccionOperacion" = 'ING' AND mov."MueveCustodia" = 'S' THEN 'I'
           WHEN conf."IdAccionOperacion" = 'RET' AND mov."MueveCustodia" = 'N' THEN 'R'
           WHEN conf."IdAccionOperacion" = 'ING' AND mov."MueveCustodia" = 'N' AND mov."IdTipoOperacion" = 'COMPAC' THEN 'I'
           WHEN conf."IdAccionOperacion" = 'ING' AND mov."MueveCustodia" = 'N' THEN 'I'
           WHEN conf."IdAccionOperacion" = 'VEN' AND mov."IdTipoOperacion" = 'VESIMV' THEN 'R'  -- Vencimiento simultanea venta
           WHEN conf."IdAccionOperacion" = 'VEN' AND mov."IdTipoOperacion" = 'VESIMC' THEN 'R'  -- Vencimiento simultanea compra
           WHEN conf."IdAccionOperacion" = 'RET' THEN 'R'
           ELSE NULL
       END AS "IndicadorIngEgrAbr"
   ,CASE WHEN COALESCE(Mov."IdOrigenGarantia",'') <> '' THEN 'S' ELSE 'N' END AS "EsGarantia"
   ,Mov."IdEmpresa"
   ,Empresa."Nombre" AS "NombreEmpresa"
   ,Mov."IdUsuario"
   ,Mov."IdModulo"
   ,Mov."IdCliente"
   ,per."NombreCompleto" AS "NombreCliente"
   ,per."DocumentoIdentidad" AS "DocumentoIdentidad"
   ,Mov."IdPortafolio"
   ,Port."CodigoPortafolio"
   ,CASE WHEN Mov."IdConcepto" IN ('RETTRC', 'RETTRP', 'INGTRP') THEN COALESCE(ConCliPort."NombreCompleto", '') ELSE '' END AS "ClienteDestino"
   ,CASE WHEN Mov."IdConcepto" IN ('RETTRC', 'RETTRP', 'INGTRP') THEN COALESCE(ConCliPort."DocumentoIdentidad", '') ELSE '' END AS "DocumentoIdentidadDestino"
   ,CASE WHEN Mov."IdConcepto" IN ('RETTRC', 'RETTRP', 'INGTRP') THEN COALESCE(ConCliPort."CodigoPortafolio", '') ELSE '' END AS "PortafolioDestino"
   ,Mov."IdEstado"
   ,Estado."Nombre" AS "NombreEstado"
   ,Mov."IdTipoCanalIngreso"
   ,Mov."IdMonedaSistema"
   ,Moneda."Nombre" AS "NombreMoneda"
   ,Mov."CodigoIFRS"
   ,Mov."IdConcepto"
   ,Concepto."Nombre" AS "NombreConcepto"
   ,Mov."IdTipoOperacion"
   ,TipoOperacion."Nombre" AS "NombreTipoOperacion"
   ,TipoOperacion."Accion" AS "Accion"
   ,Mov."EsNominal"
   ,Mov."FechaOperacion"
   ,Mov."FolioExternoReferencia"
   ,Mov."IdBolsa"
   ,Bolsa."Nombre" AS "NombreBolsa"
   ,Mov."IdSucursal"
   ,Mov."IdAgente"
   ,Mov."IdCondicionLiquidacion"
   ,CondicionLiquidacion."Codigo" AS "CodigoCondicionLiquidacion"
   ,CondicionLiquidacion."Nombre" AS "NombreCondicionLiquidacion"
   ,Mov."FechaLiquidacion"
   ,Mov."Glosa"
   ,Mov."IdClasificacionTipoInstrumento"
   ,Mov."IdTipoInstrumento"
   ,TipoInstrumento."Descripcion" AS "TipoInstrumento"
   ,Mov."IdInstrumento"
   --,Riesgo."ClasificacionRiesgo"
   ,ICR."IdEntidadClasificadoraRiesgo"
   ,ICR."ValorClasificacionRiesgo"
   ,CASE WHEN Mov."IdTipoInstrumento" = 'SIM' THEN Comp."NemotecnicoSubyacente" ELSE Mov."Nemotecnico" END AS "Nemotecnico"
   ,Mov."IdEmisor"
   ,Emisor."NombreCompleto" AS "NombreEmisor"
   ,COALESCE(CASE WHEN mov."IdClasificacionTipoInstrumento" = 'IF' THEN Mov."NemoSVS" ELSE Comp."NemotecnicoSubyacente" END, '') AS "NemoSVS"
   ,Mov."IdTipoReajuste"
   ,TipoReajuste."Nombre" AS "NombreTipoReajuste"
   ,Mov."FechaEmisionInstrumento"
   ,Mov."FechaVencimientoInstrumento"
   ,Mov."FechaVencimientoOperacion"
   ,Mov."EsFungible"
   ,Mov."IdBoveda"
   ,Boveda."Nombre" AS "NombreBoveda"
   ,CCP."Codigo" AS "NumeroCuentaBoveda"
   ,CCP."IdTipoCustodia" || ' ' || CCP."Codigo" AS "CodigoCuentaCustodio"
   ,CCP."IdCuentaCustodioPortafolio"
   ,Mov."IdMonedaInstrumento"
   ,CASE WHEN mov."IdClasificacionTipoInstrumento" = 'IF' THEN mov."ValorRescate" WHEN Mov."IdConcepto" IN ('RETNOM', 'INGNOM') THEN (Mov."Cantidad" * -1) ELSE Mov."Cantidad" END AS "Cantidad"
   ,Mov."CorteMinimo"
   ,Mov."ValorRescate"
   ,Mov."PrecioCompra"
   ,Mov."TasaCompra"
   -- CGO
   --,Mov."MontoCompra"
   ,Coalesce("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(Mov."MontoCompra", FormCant."PrecioDecimales", FormCant."PrecioTrucaRedondea"), Mov."MontoCompra") AS "MontoCompra"
   ,Mov."MontoCompraMonedaInstrumento"
   ,Mov."FolioCarteraGarantizada"
   ,Mov."IdOrigenGarantia"
   ,Mov."IdEspecieGarantia"
   ,Mov."UsaGarantiaEspecifica"
   ,Mov."RazonSocialContraparteGarantia"
   ,Cli."IdPersona"
   ,coalesce(per."NombreCompleto",'') as "NombrePersona"
   ,COALESCE(Mov."PermiteCertificacionPorUsuario",'S') AS "PermiteCertificacionPorUsuario"
   ,CASE
   		WHEN Cli."IdCliente" = DatosCtaProp."IdCliente" THEN 'CPRO'				-- Cuenta propia
   		WHEN COALESCE(RelacionadosCtaProp."IdPersonaDestino",0) > 0 then 'REL'	-- Relacionado
   		ELSE 'TER'																-- Tercero
	END AS "Relacion"
   ,CASE
   		WHEN Mov."IdTipoReajuste" <> '' THEN Mov."IdTipoReajuste"
   		ELSE Mov."IdMonedaInstrumento"
	END AS "MonedaReajuste"
   ,IFNULL(ConcCus."GeneraTraspaso", 'N') AS "GeneraTraspaso"
   ,IFNULL(ConcCus."EsTransferenciaPortafolio", 'N') AS "EsTransferenciaPortafolio"
   ,IFNULL(ConcCus."EsTransferenciaCliente", 'N') AS "EsTransferenciaCliente"
   ,MatrizAgrupador."Producto"
   ,MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") AS "IdProductoAgrupador"
   ,CASE WHEN COALESCE(Port."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Port."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MatrizAgrupadorNoAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
   ,CASE WHEN COALESCE(Mov."ExcluyeMovimientoEnCartola", 'N') != 'N' THEN 'SI' ELSE 'NO' END AS "ExcluyeMovimientoEnCartola"
   ,Mov."IdOperacion"
   ,Mov."IdContraparteGarantia"
   ,Mov."PlazoEnDiasOperacion" AS "PlazoEnDias"
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS Mov
LEFT JOIN "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" AS conf
	   ON Mov."IdTipoOperacion" = conf."IdTipoOperacion"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Port
	   ON Port."IdPortafolio" = Mov."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cli
	   ON Cli."IdCliente" = Port."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS Empresa
	   ON Mov."IdEmpresa" = Empresa."IdEmpresa"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS Concepto
	   ON Mov."IdConcepto" = Concepto."IdConcepto"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TipoOperacion
	   ON Mov."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS Emisor
	   ON Mov."IdEmisor" = Emisor."IdEmisor"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
	   ON Mov."IdInstrumento" = Instrumento."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
	   ON TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" AS TICR
	   ON TICR."IdTipoInstrumento" = Instrumento."IdTipoInstrumento"
	  AND TICR."EntidadClasificadoraRiesgoPrincipal" = 'S'
LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR
	   ON ICR."IdInstrumento" = Instrumento."IdInstrumento"
	  AND ICR."IdEntidadClasificadoraRiesgo" = TICR."IdEntidadClasificadoraRiesgo"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" AS Boveda
	   ON Mov."IdBoveda" = Boveda."IdBoveda"
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoReajuste" AS TipoReajuste
	   ON Mov."IdTipoReajuste" = TipoReajuste."IdTipoReajuste"
LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" AS Bolsa
	   ON Mov."IdBolsa" = Bolsa."IdBolsa"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS Moneda
	   ON Mov."IdMonedaInstrumento" = Moneda."IdMoneda"
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS CondicionLiquidacion
	   ON Mov."IdCondicionLiquidacion" = CondicionLiquidacion."IdCondicionDeLiquidacion"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS Estado
	   ON Mov."IdEstado" = Estado."IdEstado"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS per
	   ON per."IdPersona" = cli."IdPersona"
LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MovRel
	   ON Mov."IdConcepto" IN ('RETTRC', 'RETTRP','INGTRP')
	  AND Mov."IdMovimientoCarteraEnTransitoRelacionado" = 0
	  AND Mov."IdMovimientoCarteraEnTransito" = MovRel."IdMovimientoCarteraEnTransitoRelacionado"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS ConCliPort
	   ON MovRel."IdPortafolio"  = ConCliPort."IdPortafolio"
	  AND MovRel."IdCliente" = ConCliPort."IdCliente"
LEFT JOIN "BSCL.Custodia.BD::CUS.ConceptoCustodia" AS ConcCus
	   ON Mov."IdConcepto" = ConcCus."IdConcepto"
	  AND ConcCus."NombreEntidad" = 'CUS.INGRESORETIROTITULO'
	  AND (Mov."IdClasificacionTipoInstrumento" = ConcCus."IdClasificacionTipoInstrumento" OR ConcCus."IdClasificacionTipoInstrumento" = '-' )
LEFT JOIN
(
	SELECT DISTINCT "IdEmpresa","IdPersona", "IdCliente", "DocumentoIdentidad"
	FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia"
	GROUP BY "IdEmpresa","IdPersona","IdCliente", "DocumentoIdentidad"
) AS DatosCtaProp
	   ON DatosCtaProp."IdEmpresa" = Mov."IdEmpresa"
LEFT JOIN
(
	SELECT DISTINCT PER."IdPersonaDestino", PER."IdPersonaOrigen", PER."IdEmpresa"
	FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" AS PER
	WHERE PER."IdEstado" = 'ACT'
) AS RelacionadosCtaProp
	   ON RelacionadosCtaProp."IdEmpresa" = Mov."IdEmpresa"
	  AND RelacionadosCtaProp."IdPersonaOrigen" = DatosCtaProp."IdPersona"
	  AND RelacionadosCtaProp."IdPersonaDestino" = per."IdPersona"
-- CGO
LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" AS FormCant
	   ON FormCant."IdMoneda" = CASE WHEN Mov."IdTipoReajuste" <> '' THEN Mov."IdTipoReajuste" ELSE Mov."IdMonedaInstrumento" END
	  AND FormCant."IdTipoOperacion" 	= ''
	  AND FormCant."IdTipoInstrumento" 	= Mov."IdTipoInstrumento"
	  AND FormCant."IdUsoMonedaFormato" 	= 'CUS'
LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCP
  	ON (CCP."IdCuentaCustodioPortafolio" = Mov."IdCuentaCustodioPortafolio")
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" AS MatrizAgrupador
	ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Port."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MatrizAgrupadorNoAPV
	ON MatrizAgrupadorNoAPV."CodigoPortafolio" = Port."CodigoPortafolio" AND  Port."IdMatrizAgrupadorAPV" = 0
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" Comp
	ON Comp."IdComprobante" = Mov."FolioComprobante"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEnTransito"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
