SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos";
GO

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" AS 	 

SELECT
	cargoabonomov."IdCargoAbonoMovimiento"
   ,cargoabonomov."FechaSistema"
   ,cargoabonomov."FechaIngresoRegistro"
   ,cuentainversion."IdCuentaInversion"
   ,cuentainversion."Codigo" AS "CodigoCuentaInversion"
   ,cuentainversion."Nombre" AS "NombreCuentaInversion"
   ,cuentainversion."IdCliente"
   

   ,documentoIdentidad."DocumentoIdentidad"
   

   ,CASE persona."Tipo" 
			WHEN 'N' THEN persona."Nombres" ||' '|| persona."ApellidoPaterno" || ' ' ||persona."ApellidoMaterno" 			
			ELSE persona."RazonSocial"
	END  as "NombreCliente"
   
   ,cuentainversion."IdPortafolio"
   ,portafolio."CodigoPortafolio"
   ,portafolio."DescripcionPortafolio"
   ,portafolio."Proposito"
   ,cargoabonomov."IdEjecutivo" AS "IdEmpleadoDigitador"
   

   ,empleado."IdEmpleado" as "IdEjecutivo"
   
   ,cargoabonomov."IdSucursal"
   ,cuentainversion."IdMoneda"
   ,moneda."Codigo" AS "CodigoMoneda"
   ,moneda."Nombre" AS "NombreMoneda"
   ,(moneda."Codigo" ||' - '|| moneda."Nombre") AS "CodigoNombreMoneda"
   ,moneda."Simbolo" AS "SimboloMoneda"
   ,moneda."Decimales" AS "DecimalesMoneda"
   ,cargoabonomov."IdTipoMovimiento"
   ,TipoMovimiento."Nombre" AS "NombreTipoMovimiento"
   ,cargoabonomov."IdConcepto"
   ,conceptomov."Nombre" AS "NombreConcepto"
   ,cargoabonomov."Descripcion"
   ,cargoabonomov."IdCondicionDeLiquidacion"
   ,condiciondeliquidacion."Codigo" AS "CodigoCondicionDeLiquidacion"
   ,condiciondeliquidacion."Nombre" AS "NombreCondicionDeLiquidacion"
   ,condiciondeliquidacion."PlazoEnDias" AS "PlazoCondicionDeLiquidacion"
   ,cargoabonomov."FechaEfectivaMovimiento"
   ,cargoabonomov."Monto"
   ,CASE WHEN cargoabonomov."IdTipoMovimiento" = 'MOVCAR' THEN cargoabonomov."Monto" ELSE 0 END "Cargo"
   ,CASE WHEN cargoabonomov."IdTipoMovimiento" = 'MOVABN' THEN cargoabonomov."Monto" ELSE 0 END "Abono"
   ,cargoabonomov."Saldo"
   ,cargoabonomov."SaldoPorFechaMovimiento"
   ,cargoabonomov."SaldoLibroInventario"
   ,cargoabonomov."IdModulo"
   ,cargoabonomov."IdTipoProducto"
   ,tipoproducto."Codigo" AS "CodigoTipoProducto"
   ,tipoproducto."Nombre" AS "NombreTipoProducto"
   ,cargoabonomov."IdTipoOperacion"
   ,tipooperacion."Codigo" AS "CodigoTipoOperacion"
   ,tipooperacion."Nombre" AS "NombreTipoOperacion"
   ,tipooperacion."Accion" AS "AccionTipoOperacion"
   ,cargoabonomov."IdOperacion"
   ,COALESCE(cargoabonomov."FechaOperacion",'0001-01-01') AS  "FechaOperacion"
   ,COALESCE(cargoabonomov."FechaLiquidacionOperacion",'0001-01-01') AS "FechaLiquidacionOperacion"
   ,COALESCE(cargoabonomov."FechaVencimientoOperacion",'0001-01-01') AS "FechaVencimientoOperacion"
   ,cargoabonomov."IdMonedaOperacion"
   ,cargoabonomov."MontoOperacion"
   ,cargoabonomov."TipoCambioOperacion"
   ,cargoabonomov."IdTipoInstrumento"
   ,COALESCE ( TINS."Nombre" ,'' ) AS "NombreTipoInstrumento"
   ,COALESCE ( CTINS."IdClasificacionTipoInstrumento" ,'' ) AS "IdClasificacionTipoInstrumento"
   ,COALESCE ( CTINS."Nombre" ,'' ) AS "NombreClasificacionTipoInstrumento"
   ,cargoabonomov."IdInstrumento"
   ,COALESCE ( cargoabonomov."Nemo" ,'' ) AS "NemotecnicoOriginal"
   ,cargoabonomov."EsCargoAbonoPorTraspaso"
   ,"BSCL.Plataforma.BD.Utilitarios::ObtenerTraduccion"("EsCargoAbonoPorTraspaso") AS "EsCargoAbonoPorTraspasoTraducido"
   ,cargoabonomov."IdCargoAbonoRelacionadoTraspaso"
   ,RelTransferencia."NombreClienteRelacionado"
   ,RelTransferencia."CodigoPortafolioRelacionado"
   ,cargoabonomov."IdTipoComprobante"
   ,CASE
   		WHEN cargoabonomov."IdModulo" = 'FAC' THEN TipoComprobanteFAC."Codigo"
   		WHEN cargoabonomov."IdModulo" = 'TES' THEN ''
   		WHEN cargoabonomov."IdModulo" = 'FMM' THEN ''
	END AS "CodigoTipoComprobante"
   ,CASE
   		WHEN cargoabonomov."IdModulo" = 'FAC' THEN TipoComprobanteFAC."Nombre"
   		WHEN cargoabonomov."IdModulo" = 'TES' THEN TipoComprobanteTES."Nombre"
   		WHEN cargoabonomov."IdModulo" = 'FMM' AND cargoabonomov."IdConcepto" = 'ACIAPV' THEN 'INGRESO APV'
        WHEN cargoabonomov."IdModulo" = 'FMM' AND cargoabonomov."IdConcepto" != 'ACIAPV' THEN 'OPERACION FFMM'
	END AS "NombreTipoComprobante"
   ,cargoabonomov."IdComprobante"
   ,cargoabonomov."FolioComprobante"
   ,cargoabonomov."IdComprobanteDetalle"
   ,cargoabonomov."IdComprobanteFormaDePago"
   ,cargoabonomov."IdTipoMovimientoAsociado"
   ,cargoabonomov."IdMovimientoAsociado"
   ,COALESCE(cargoabonomov."FechaComprobante",'0001-01-01') AS "FechaComprobante"
   ,cargoabonomov."IdPersonaPagoCobro"
   ,cargoabonomov."EsCertificadoAutomatico"
   ,cargoabonomov."IdUsuarioCertificador"
   ,UsuarioCertificador."CodigoUsuario" as "CodigoUsuarioCertificador"
   ,COALESCE(cargoabonomov."FechaCertificacion",'0001-01-01') AS "FechaCertificacion"
   ,cargoabonomov."IdContraparte"
   ,cargoabonomov."EsIndicadorBilateral"
   ,cargoabonomov."IdEmpresa"
   ,cargoabonomov."EsIngresoManual"
   ,cargoabonomov."IdEvento"
   ,consultarevento."IdTipoEvento"
   ,TipoEvento."Nombre" AS "Autorizacion"
   ,TipoEvento."Nombre" ||' - '|| estadoevento."NombreEstado" as "AutorizacionEstado"
   ,consultarevento."Glosa" AS "GlosaAnulacion"
   ,estadoevento."IdEstado" as "IdEstadoEvento"
   ,estadoevento."NombreEstado" as "NombreEstadoEvento"
   ,cargoabonomov."IdUsuario"
   ,UsuarioCreador."CodigoUsuario" as "CodigoUsuarioCreador"
   ,COALESCE(UsuarioCreador."CodigoUsuario" ,'') AS "NombreUsuario"
   ,cargoabonomov."IdEstado"
   ,estadoctainv."Nombre" as "NombreEstado"
   ,cargoabonomov."IdCarga" AS "IdCarga"
   ,cargoabonomov."SecuenciaMovimientoMigracion"  AS "SecuenciaMovimientoMigracion"
   

   ,productoAgrupador."NombreProductoAgrupador" AS "Producto"

	

   ,MAP(matrizAgrupadorAPV."IdProductoAgrupador",NULL,0,9999,0,matrizAgrupadorAPV."IdProductoAgrupador") AS "IdProductoAgrupador"
	


   ,regimenTributario."Codigo" AS "RegimenMatrizAgrupador"
   
   ,portafolio."CodigoPortafolio" ||' '||portafolio."Proposito" as "NombrePortafolio"
   ,cargoabonomov."Cantidad"
   ,CASE WHEN cuentainversion."IdCliente" IN (SELECT  "IdCliente" FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia"GROUP BY "IdPersona","IdCliente", "DocumentoIdentidad") THEN 'CPRO' -- Cuenta propia
										WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
										ELSE 'TER'                                                  -- Tercero
										END AS "Relacion"
FROM  "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabonomov
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabonomov."IdCuentaInversion"



INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"  AS cliente   ON (cliente."IdCliente" = cuentainversion."IdCliente")
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona"  AS persona   ON (persona."IdPersona" = cliente."IdPersona")
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS documentoIdentidad ON persona."IdDocumentoIdentidad" = documentoIdentidad."IdDocumentoIdentidad"

	


INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS portafolio ON (portafolio."IdPortafolio" = cuentainversion."IdPortafolio")

LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS empleado	 ON (empleado."IdPersona" = cliente."IdPersonaEjecutivo")
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS matrizAgrupadorAPV ON matrizAgrupadorAPV."IdMatrizAgrupadorAPV" = portafolio."IdMatrizAgrupadorAPV"

	   
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS productoAgrupador
		ON productoAgrupador."IdProductoAgrupador" = matrizAgrupadorAPV."IdProductoAgrupador"
	   AND productoAgrupador."IdPlanTipoInversion" = matrizAgrupadorAPV."IdPlanTipoInversion"
	   AND productoAgrupador."IdEstado" = 'VIG'

	 
LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" AS regimenTributario
		ON regimenTributario."IdRegimenTributario" = matrizAgrupadorAPV."IdRegimenTributario"
	   AND regimenTributario."IdEstado" = 'VIG'
	   



LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" as UsuarioCreador ON UsuarioCreador."IdUsuario" = cargoabonomov."IdUsuario"





    LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" as UsuarioCertificador ON UsuarioCertificador."IdUsuario" = cargoabonomov."IdUsuarioCertificador"

LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS condiciondeliquidacion ON  condiciondeliquidacion."IdCondicionDeLiquidacion" = cargoabonomov."IdCondicionDeLiquidacion"
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS moneda ON  moneda."IdMoneda" = cuentainversion."IdMoneda"
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS conceptomov ON  conceptomov."IdConcepto" = cargoabonomov."IdConcepto"
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS estadoctainv ON (estadoctainv."IdEstado" = cargoabonomov."IdEstado")

LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" AS TipoMovimiento ON  TipoMovimiento."IdTipoMovimiento" = cargoabonomov."IdTipoMovimiento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS viewconsultarins ON  viewconsultarins."IdInstrumento" = cargoabonomov."IdInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = viewconsultarins."IdTipoInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CTINS ON CTINS."IdClasificacionTipoInstrumento" = TINS."IdClasificacionTipoInstrumento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS tipooperacion ON  tipooperacion."IdTipoOperacion" = cargoabonomov."IdTipoOperacion"
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS tipoproducto ON  tipoproducto."IdTipoProducto" = cargoabonomov."IdTipoProducto"

LEFT OUTER JOIN "BSCL.Plataforma.BD::AUT.Evento" AS consultarevento ON consultarevento."IdEvento" = cargoabonomov."IdEvento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::AUT.TipoEvento" AS TipoEvento ON consultarevento."IdTipoEvento" = TipoEvento."IdTipoEvento" AND TipoEvento."IdEmpresa" = consultarevento."IdEmpresa"
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEstadoAutorizacion" AS estadoevento ON (estadoevento."IdEstado" = consultarevento."IdEstado" AND upper(estadoevento."NombreEntidad") = upper('AUT.Evento'))
LEFT OUTER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarTipoComprobante" AS TipoComprobanteTES ON TipoComprobanteTES."IdTipoComprobante" = cargoabonomov."IdTipoComprobante"
LEFT OUTER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarTipoComprobante" AS TipoComprobanteFAC ON TipoComprobanteFAC."IdTipoComprobante" =  cargoabonomov."IdTipoComprobante"
LEFT OUTER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonosRelacionadoTransferencia" AS RelTransferencia ON RelTransferencia."IdCargoAbonoMovimiento" = cargoabonomov."IdCargoAbonoRelacionadoTraspaso"
LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarRelacionCorredora" AS REL  ON (REL."IdPersonaDestino" = cliente."IdPersona")




WITH READ ONLY;	 	
GO

GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO