SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos";
GO
--Agregar nuevos campos al final. Actualizar adicionalmente los siguientes objetos:
--"BSCL.CuentaInversion.BD.Tipos::CTA.CartolaDeMovimiento"
--"BSCL.CuentaInversion.BD.Vistas::CartolaDeMovimientos"

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos" AS 	 
SELECT	
	 cargoabonomov."IdCargoAbonoMovimiento" ,
	 cargoabonomov."FechaSistema" ,
	 cargoabonomov."FechaIngresoRegistro" ,
	 cuentainversion."IdCuentaInversion" ,
	 cuentainversion."Codigo" AS "CodigoCuentaInversion" ,
	 cuentainversion."Nombre" AS "NombreCuentaInversion" ,
	 cuentainversion."IdCliente" ,
	 cargoabonomov."NombreCliente" ,
	 cuentainversion."IdPortafolio" ,
	 cuentainversion."IdMoneda" ,
	 moneda."Codigo" AS "CodigoMoneda" ,
	 moneda."Nombre" AS "NombreMoneda" ,
	 moneda."Simbolo" AS "SimboloMoneda" ,
	 moneda."Decimales" AS "DecimalesMoneda" ,
	 cargoabonomov."IdTipoMovimiento" ,
	 TipoMovimiento."Tipo" AS "TipoMovimiento" ,
	 TipoMovimiento."Nombre" AS "NombreTipoMovimiento" ,
	 cargoabonomov."IdConcepto" ,
	 conceptomov."NombreConcepto",
	 conceptomov."NombreConceptoCartola",
	 conceptomov."ExclusivoCartola",
	 conceptomov."Origen" AS "OrigenConcepto",
	 cargoabonomov."Descripcion" ,
	 cargoabonomov."IdCondicionDeLiquidacion" ,
	 condiciondeliquidacion."Codigo" AS "CodigoCondicionDeLiquidacion" ,
	 condiciondeliquidacion."Nombre" AS "NombreCondicionDeLiquidacion" ,
	 condiciondeliquidacion."PlazoEnDias" AS "PlazoCondicionDeLiquidacion" ,
	 cargoabonomov."FechaEfectivaMovimiento" ,
	 cargoabonomov."Monto" ,
	 cargoabonomov."Saldo" as "SaldoPorFechaEfectiva",
	 cargoabonomov."SaldoPorFechaMovimiento",
	 cargoabonomov."IdModulo" ,
	 cargoabonomov."IdTipoProducto" ,
	 tipoproducto."Codigo" AS "CodigoTipoProducto" ,
	 tipoproducto."Nombre" AS "NombreTipoProducto" ,
	 cargoabonomov."IdTipoOperacion" ,
	 tipooperacion."Codigo" AS "CodigoTipoOperacion" ,
	 tipooperacion."Nombre" AS "NombreTipoOperacion" ,
	 tipooperacion."Accion" AS "AccionTipoOperacion" ,
	 cargoabonomov."IdOperacion" ,
	 COALESCE(cargoabonomov."FechaOperacion",'0001-01-01') AS  "FechaOperacion",
	 COALESCE(cargoabonomov."FechaLiquidacionOperacion",'0001-01-01') AS "FechaLiquidacionOperacion",
	 COALESCE(cargoabonomov."FechaVencimientoOperacion",'0001-01-01') AS "FechaVencimientoOperacion",		 
	 cargoabonomov."IdMonedaOperacion" ,
	 cargoabonomov."MontoOperacion" ,
	 cargoabonomov."TipoCambioOperacion" ,		 
	 cargoabonomov."IdTipoInstrumento" ,
	 cargoabonomov."IdCarga",
	 ctainversion."AfectaRentabilidad", --nuevo
	  
	  
	 conceptos."TipoConcepto", --nuevo
	 COALESCE ( TipoInstrumento."Nombre" ,'' ) AS "NombreTipoInstrumento" ,
	 COALESCE ( TipoInstrumento."IdClasificacionTipoInstrumento" ,'' ) AS "IdClasificacionTipoInstrumento" ,
	 COALESCE ( ClasificacionTipoInstrumento."Nombre" ,'' ) AS "NombreClasificacionTipoInstrumento" ,
	 cargoabonomov."IdInstrumento" ,
	 cargoabonomov."Cantidad",
	 COALESCE ( cargoabonomov."Nemo" ,'' ) AS "NemotecnicoOriginal" ,
	 cargoabonomov."EsCargoAbonoPorTraspaso",
	 "BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("EsCargoAbonoPorTraspaso") AS "EsCargoAbonoPorTraspasoTraducido",
	 cargoabonomov."IdCargoAbonoRelacionadoTraspaso",
	 RelTransferencia."NombreClienteRelacionado",
	 RelTransferencia."CodigoPortafolioRelacionado",
	 cargoabonomov."IdTipoComprobante",
	 CASE WHEN cargoabonomov."IdModulo" = 'FAC' THEN TipoComprobanteFAC."Codigo" 
	 	  WHEN cargoabonomov."IdModulo" = 'TES' THEN ''
	 	  WHEN cargoabonomov."IdModulo" = 'FMM' THEN ''
	 	  WHEN cargoabonomov."IdModulo" = 'MFW' THEN ''
	  END AS "CodigoTipoComprobante",
	 CASE WHEN cargoabonomov."IdModulo" = 'FAC' THEN TipoComprobanteFAC."Nombre" 
	 	  WHEN cargoabonomov."IdModulo" = 'TES' THEN TipoComprobanteTES."Nombre"
	 	  WHEN cargoabonomov."IdModulo" = 'FMM' THEN 'OPERACION FFMM'
	 	  --WHEN cargoabonomov."IdModulo" = 'MFW' THEN 'VENCIMIENTO FWD POR COMPENSACION'
	 	  WHEN cargoabonomov."IdModulo" = 'MFW' THEN
	 	  	CASE WHEN cartera."IdModalidadCumplimiento" = 7 
	 	  		THEN CASE WHEN cargoabonomov."IdConcepto" = 'ANTFWD' THEN 'ANTICIPO FWD POR ENTREGA FISICA' ELSE 'VENCIMIENTO FWD POR ENTREGA FISICA' END
	 	  		ELSE CASE WHEN cargoabonomov."IdConcepto" = 'ANTFWD' THEN 'ANTICIPO FWD POR COMPENSACION' ELSE 'VENCIMIENTO FWD POR COMPENSACION' END
	 	  	END 
	  END AS "NombreTipoComprobante",
	 cargoabonomov."IdComprobante",
	 CASE WHEN cargoabonomov."IdTipoComprobante" = 'POL' OR cargoabonomov."IdTipoComprobante" = 'NCO' OR cargoabonomov."IdTipoComprobante" = 'NDR' OR cargoabonomov."IdTipoComprobante" = 'FOC' OR cargoabonomov."IdTipoComprobante" = 'NDE' OR cargoabonomov."IdTipoComprobante" = 'FAC' OR cargoabonomov."IdTipoComprobante" = 'NEO' OR cargoabonomov."IdTipoComprobante" = 'FOX' OR cargoabonomov."IdTipoComprobante" = 'FOE' OR cargoabonomov."IdTipoComprobante" = 'NCE' OR cargoabonomov."IdTipoComprobante" = 'NEE' OR cargoabonomov."IdTipoComprobante"= 'FEE' OR cargoabonomov."IdTipoComprobante" = 'FAE' THEN 
	 		CAST(ComprobanteFAC."FolioComprobante" AS VARCHAR) 
	 	ELSE cargoabonomov."FolioComprobante" END AS "FolioComprobante", 
	 IFNULL(ComprobanteFAC."EsVentaCorta", 'N') AS "EsVentaCorta",
	 COALESCE(cargoabonomov."FechaComprobante",'0001-01-01') AS "FechaComprobante",
	 cargoabonomov."IdComprobanteDetalle" ,
	 cargoabonomov."IdPersonaPagoCobro" ,
	 cargoabonomov."EsCertificadoAutomatico" ,
	 cargoabonomov."IdUsuarioCertificador",
	 COALESCE(cargoabonomov."FechaCertificacion",'0001-01-01') AS "FechaCertificacion",
	 cargoabonomov."IdContraparte" ,
	 cargoabonomov."EsIndicadorBilateral" ,
	 cargoabonomov."IdEmpresa" ,
	 empresa."Codigo" AS "CodigoEmpresa" ,
	 empresa."Nombre" AS "NombreEmpresa" ,
	 cargoabonomov."EsIngresoManual" ,
	 cargoabonomov."IdEvento" ,
	 cargoabonomov."IdUsuario" ,
	 COALESCE ( usuario."CodigoUsuario" ,'' ) AS "NombreUsuario" ,
	 cargoabonomov."IdEstado" ,
	 estado."Nombre" AS "NombreEstado",
	 COALESCE(ComprobanteFAC."FolioComprobante",0) AS "FolioComprobanteFacturacion",
	 COALESCE ( usuarioCert."CodigoUsuario" ,'' ) AS "NombreUsuarioCert", -- nuevo 
	 cargoabonomov."IdComprobanteTesoreria",
	 COALESCE(ComprobanteFAC."IdEstadoSII",'') AS "IdEstadoSII",
	 COALESCE(ComprobanteFAC."IdRespuestaDTE",0) AS "IdRespuestaDTE"
   FROM  "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabonomov
   LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS conceptos ON conceptos."IdConcepto" = cargoabonomov."IdConcepto"
   LEFT OUTER JOIN "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" AS ctainversion ON ctainversion."IdConcepto" = cargoabonomov."IdConcepto"
   LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" AS TipoMovimiento ON  TipoMovimiento."IdTipoMovimiento" = cargoabonomov."IdTipoMovimiento"   
   LEFT OUTER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabonomov."IdCuentaInversion"
   LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS condiciondeliquidacion ON  condiciondeliquidacion."IdCondicionDeLiquidacion" = cargoabonomov."IdCondicionDeLiquidacion"         
   LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS moneda ON  moneda."IdMoneda" = cuentainversion."IdMoneda"   
   LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS empresa ON  empresa."IdEmpresa" = cargoabonomov."IdEmpresa"
   LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS estado ON  estado."IdEstado" = cargoabonomov."IdEstado"   
   LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS ins ON  ins."IdInstrumento" = cargoabonomov."IdInstrumento"
   LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = ins."IdTipoInstrumento"
   LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS ClasificacionTipoInstrumento ON ClasificacionTipoInstrumento."IdClasificacionTipoInstrumento" = TipoInstrumento."IdClasificacionTipoInstrumento"
   LEFT OUTER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarConceptosCuentaInversion" AS conceptomov ON  conceptomov."IdConcepto" = cargoabonomov."IdConcepto"   
   LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS tipooperacion ON  tipooperacion."IdTipoOperacion" = cargoabonomov."IdTipoOperacion"   
   LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS tipoproducto ON  tipoproducto."IdTipoProducto" = cargoabonomov."IdTipoProducto"   
   LEFT OUTER JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS usuario ON  usuario."IdUsuario" = cargoabonomov."IdUsuario"
   LEFT OUTER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarTipoComprobante" AS TipoComprobanteTES ON TipoComprobanteTES."IdTipoComprobante" = cargoabonomov."IdTipoComprobante"
   LEFT OUTER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarTipoComprobante" AS TipoComprobanteFAC ON TipoComprobanteFAC."IdTipoComprobante" =  cargoabonomov."IdTipoComprobante"
   LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS ComprobanteFAC ON ComprobanteFAC."IdComprobante" =  cargoabonomov."IdComprobante"
   LEFT OUTER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonosRelacionadoTransferencia" AS RelTransferencia ON RelTransferencia."IdCargoAbonoMovimiento" = cargoabonomov."IdCargoAbonoRelacionadoTraspaso"
   LEFT OUTER JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS usuarioCert ON  usuarioCert."IdUsuario" = cargoabonomov."IdUsuarioCertificador"
   LEFT OUTER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS cartera ON cartera."IdCartera" = cargoabonomov."IdOperacion" AND cartera."IdInstrumento" = cargoabonomov."IdInstrumento" AND cartera."IdModulo" = cargoabonomov."IdModulo"
WITH READ ONLY;	 	
GO

GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

