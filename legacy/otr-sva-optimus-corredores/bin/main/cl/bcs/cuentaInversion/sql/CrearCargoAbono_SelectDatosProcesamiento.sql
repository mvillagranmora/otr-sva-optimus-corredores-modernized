SELECT	"BSCL.CuentaInversion.BD.Secuencias::CargoAbonoMovimiento".nextval as "IdCargoAbonoMovimiento"
		,CAG."NumeroCargoAbono" as "IdInterno"
		,CAG."IdPortafolio" AS "IdPortafolioInformado" 
		,CAG."IdMoneda" AS "IdMonedaInformado"
		,CAG."IdTipoMovimiento" AS "IdTipoMovimientoInformado"
		,CAG."IdConcepto" AS "IdConceptoInformado"
		,CAG."Descripcion"
		,CAG."IdCondicionDeLiquidacion"
		,CAG."FechaCondicionDeLiquidacion"
		,CAG."Monto"
		,CAG."IdEjecutivo"
 		,CAG."IdSucursal"
		,CAG."IdTipoProducto" AS "IdTipoProductoInformado"
		,CAG."IdTipoOperacion" AS "IdTipoOperacionInformado"
		,CAG."IdOperacion"
		,CAG."FechaOperacion"
		,CAG."FechaLiquidacionOperacion"
		,CAG."FechaVencimientoOperacion"
		,CAG."IdMonedaOperacion"
		,CAG."MontoOperacion"
		,CAG."TipoCambioOperacion"
		,CAG."IdContraparte"
		,CAG."IdTipoComprobante"
		,CAG."IdComprobante"
		,CAG."FolioComprobante"
		,CAG."IdComprobanteDetalle"
		,CAG."IdComprobanteFormaDePago"
		,CAG."IdTipoMovimientoAsociado"
 		,CAG."IdMovimientoAsociado"
 		,CAG."FechaComprobante"
		,CAG."IdPersonaPagoCobro"
		,CAG."IdInstrumento"
		,CAG."IdTipoInstrumento" AS "IdTipoInstrumentoInformado"
		,CAG."Nemo"
		,CAG."EsIndicadorBilateral"
		,CAG."EsCargoAbonoPorTraspaso"
		,CAG."IdPortafolioTraspaso"
		,CAG."GeneroContraparteEnTesoreria"
		,CAG."CertificadoAutomatico"
		,CAG."Cantidad"
		,CI."IdCuentaInversion"
		,CI."IdPortafolio"
		,CI."IdMoneda"
		,CI."IdEstado"			
		,CASE WHEN CAG."EsCargoAbonoPorTraspaso" = 'S' AND CI."IdPortafolio" <> CAG."IdPortafolio" AND TMV."IdTipoMovimiento" = 'MOVCAR' THEN 'MOVABN'
			  WHEN CAG."EsCargoAbonoPorTraspaso" = 'S' AND CI."IdPortafolio" <> CAG."IdPortafolio" AND TMV."IdTipoMovimiento" = 'MOVABN' THEN 'MOVCAR'
			  ELSE TMV."IdTipoMovimiento"
		 END AS "IdTipoMovimiento"			  			
		,CCP."IdConcepto"
		,MON."IdMoneda" AS "IdMonedaTesoreria"
		,MON."IdEstadoMonedaTesoreria"			
		,TOper."IdTipoOperacion"
		,Tprod."IdTipoProducto"
		,TIns."IdTipoInstrumento"
		,COALESCE(IndPor."OperaLineaSobregiro",'N') AS "OperaLineaSobregiro"
		,Cli."NombreCompleto"
		,case when (CAG."EsCargoAbonoPorTraspaso" = 'S' AND CI."IdPortafolio" <> CAG."IdPortafolio") OR CAG."IdConcepto" = 'RET104' then "NumeroCargoAbono" * -1 
		when CAG."IdConcepto" = 'RET104' then "NumeroCargoAbono" * -1 
		else "NumeroCargoAbono" end as "KeyTraspaso"
		,COALESCE(IndPor."OperaCuentaInversion",'N') AS "OperaCuentaInversion"
		,Cli."DocumentoIdentidad"
		,COALESCE(INDCUMP."ValorIndicador",'N') AS "BloqueadoPorCumplimiento"
		, PORTCAG."CodigoPortafolio" AS "CodigoPortafoliCargo" 
  FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_IN_CargoAbonoAGenerar" AS CAG
  INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CI 
  		 ON CI."IdMoneda" = CAG."IdMoneda" 
  		AND CI."IdPortafolio" IN (CAG."IdPortafolio",CAG."IdPortafolioTraspaso")	  
  LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarIndicadoresPortafolioCuentaInversion" AS IndPor 
  		 ON IndPor."IdPortafolio" IN (CASE WHEN CAG."IdTipoMovimiento" = 'MOVCAR' THEN CAG."IdPortafolio"
							 	           WHEN CAG."IdTipoMovimiento" = 'MOVABN' AND CAG."EsCargoAbonoPorTraspaso" = 'S' THEN CAG."IdPortafolioTraspaso"
                                       END)	  
  LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" as TMV 
		ON TMV."IdModulo" = 'CTA'
  		AND TMV."IdTipoMovimiento" = CAG."IdTipoMovimiento"
  LEFT JOIN "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" as CCP ON CCP."IdConcepto" = CAG."IdConcepto"
  LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarMonedaTesoreria" as MON ON MON."IdMoneda" = CAG."IdMoneda"	  
  LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TOper ON TOper."IdTipoOperacion" = CAG."IdTipoOperacion"
  LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" as Tprod ON Tprod."IdTipoProducto" = CAG."IdTipoProducto"
  LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TIns ON TIns."IdTipoInstrumento" = CAG."IdTipoInstrumento"
  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS Cli on Cli."IdCliente" = CI."IdCliente"
  LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTCAG ON PORTCAG."IdPortafolio" = CAG."IdPortafolio"

  LEFT JOIN 
  ( 
  	SELECT IND."IdEntidad", IND."ValorIndicador"
	FROM "BSCL.Plataforma.BD::IND.TipoIndicador" AS TIND
	INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" AS IND ON  (IND."IdTipoIndicador" = TIND."IdTipoIndicador" AND IND."IdTipoEntidad" = TIND."IdTipoEntidad")
	WHERE TIND."IdTipoEntidad" 		= 'CLIENTE' 
	AND TIND."CodigoTipoIndicador" 	= 'BCCUM' -- Bloqueo por cumplimiento
	AND TIND."IdEstado" 			= 'ACT'
  ) AS INDCUMP ON (INDCUMP."IdEntidad" = Cli."IdCliente");
  
  

  
  
