SET SCHEMA "BSCL";
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarMovimientosCuentaInversionAnalitica";

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarMovimientosCuentaInversionAnalitica" AS
SELECT
	CASE WHEN cargoabonomov."IdCargoAbonoMovimiento" = 0 THEN NULL ELSE cargoabonomov."IdCargoAbonoMovimiento" END AS "IdCargoAbonoMovimiento",
	cargoabonomov."IdEmpresa",
	LEFT(TO_CHAR(IFNULL(cargoabonomov."FechaSistema",'0001-01-01')),10) AS "FechaMovimiento",
	CASE WHEN IFNULL(cargoabonomov."FechaIngresoRegistro",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
					 	                              ELSE  
					 	                                  CASE WHEN  cargoabonomov."FechaIngresoRegistro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
					 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (cargoabonomov."FechaIngresoRegistro", CONFIGURACION."Valor")),27)
					 	                                  END 
	END AS "FechaIngresoRegistro",
	cargoabonomov."EsIngresoManual",
	CASE WHEN cuentainversion."IdCuentaInversion" = 0 THEN NULL ELSE cuentainversion."IdCuentaInversion" END AS "IdCuentaInversion",
	CAST(cuentainversion."Codigo" AS NVARCHAR(40)) AS "CodigoCuentaInversion" ,
	CAST(cuentainversion."Nombre" AS NVARCHAR(100)) AS "NombreCuentaInversion" ,
	CASE WHEN cuentainversion."IdCliente" = 0 THEN NULL ELSE cuentainversion."IdCliente" END AS "IdCliente",
	CAST(documento."DocumentoIdentidad" AS NVARCHAR(40)) AS "DocumentoIdentidad",
	CAST(CASE persona."Tipo" WHEN 'N' THEN persona."Nombres" ||' '|| persona."ApellidoPaterno" || ' ' ||persona."ApellidoMaterno" 
				ELSE persona."RazonSocial" END AS NVARCHAR(302)) AS "NombreCliente",
	CASE WHEN cuentainversion."IdPortafolio" = 0 THEN NULL ELSE cuentainversion."IdPortafolio" END AS "IdPortafolio",
	CAST(COALESCE(portafolio."CodigoPortafolio",'') AS NVARCHAR(100)) as "CodigoPortafolio",
	CAST(COALESCE(portafolio."DescripcionPortafolio",'') AS NVARCHAR(100)) as "DescripcionPortafolio" ,
	CAST(COALESCE(portafolio."Proposito",'') AS NVARCHAR(520)) as "Proposito" ,
	CASE WHEN cuentainversion."IdMoneda" = '' THEN NULL ELSE cuentainversion."IdMoneda" END AS "IdMoneda",
	CAST(moneda."Codigo" AS NVARCHAR(40)) AS "CodigoMoneda" ,
	CAST(moneda."Nombre" AS NVARCHAR(100)) AS "NombreMoneda" ,
	moneda."Simbolo" AS "SimboloMoneda" ,
	Formato."CantidadDecimales" AS "DecimalesMoneda" ,
	CASE WHEN cargoabonomov."IdTipoMovimiento" = 'MOVABN' THEN 'ABONO'
	ELSE 'CARGO'
	END AS "TipoMovimiento",
	cargoabonomov."Monto",
	CASE WHEN cargoabonomov."IdConcepto" = '' THEN NULL ELSE cargoabonomov."IdConcepto" END  AS "IdConcepto" ,
	CAST(COALESCE(conceptomov."Nombre",'') AS NVARCHAR(100)) AS "NombreConcepto",
	CAST(cargoabonomov."Descripcion" AS NVARCHAR(100)) AS "Descripcion",
	cargoabonomov."IdCondicionDeLiquidacion" AS "IdCondicionDeLiquidacion",
	coalesce(condiciondeliquidacion."Codigo",'') AS "CodigoCondicionDeLiquidacion" ,
	coalesce(CAST(condiciondeliquidacion."Nombre" AS NVARCHAR(40)),'') AS "NombreCondicionDeLiquidacion" ,
	to_decimal(coalesce(condiciondeliquidacion."PlazoEnDias",0),26,8) AS "PlazoCondicionDeLiquidacion",
	LEFT(TO_VARCHAR(IFNULL(cargoabonomov."FechaEfectivaMovimiento",'0001-01-01')),10) AS "FechaEfectivaMovimiento",
	CASE WHEN cargoabonomov."IdTipoMovimiento" = 'MOVCAR' 
	      THEN cargoabonomov."Monto" 
	      ELSE 0 
	END AS "Cargo",
	CASE WHEN cargoabonomov."IdTipoMovimiento" = 'MOVABN' 
	      THEN cargoabonomov."Monto" 
	      ELSE 0 
	END AS "Abono",
	cargoabonomov."Saldo" as "SaldoPorFechaLiquidacion",
	cargoabonomov."SaldoPorFechaMovimiento",  
	cargoabonomov."IdTipoComprobante" AS "IdTipoComprobante",
	CASE WHEN cargoabonomov."IdModulo" = 'FAC' 
	THEN coalesce(TipoComprobanteFAC."Codigo",'') WHEN cargoabonomov."IdModulo" = 'TES' 
	THEN '' WHEN cargoabonomov."IdModulo" = 'FMM' 
	THEN '' 
	else ''
	END AS "CodigoTipoComprobante",
	      CASE WHEN cargoabonomov."IdModulo" = 'FAC' 
	THEN coalesce(CAST(TipoComprobanteFAC."Nombre" AS NVARCHAR(100)),'') WHEN cargoabonomov."IdModulo" = 'TES' 
	THEN coalesce(CAST(TipoComprobanteTES."Nombre" AS NVARCHAR(100)),'') WHEN cargoabonomov."IdModulo" = 'FMM' 
	THEN 'OPERACION FFMM' 
	else ''
	END AS "NombreTipoComprobante",
	CASE WHEN cargoabonomov."IdComprobante" = 0 THEN NULL ELSE cargoabonomov."IdComprobante" END AS "IdComprobante",
	CAST(cargoabonomov."FolioComprobante" AS NVARCHAR(40)) AS "FolioComprobante",
	CASE WHEN cargoabonomov."IdComprobanteDetalle" = 0 THEN NULL ELSE cargoabonomov."IdComprobanteDetalle" END AS "IdComprobanteDetalle",
	CASE WHEN cargoabonomov."IdComprobanteFormaDePago" = 0 THEN NULL ELSE cargoabonomov."IdComprobanteFormaDePago" END AS "IdComprobanteFormaDePago",
	cargoabonomov."IdTipoMovimientoAsociado" AS "IdTipoMovimientoAsociado",
	CASE WHEN cargoabonomov."IdMovimientoAsociado" = 0 THEN NULL ELSE cargoabonomov."IdMovimientoAsociado" END AS "IdMovimientoAsociado",
	LEFT(TO_VARCHAR(IFNULL(cargoabonomov."FechaComprobante",'0001-01-01')),10) as "FechaComprobante",
	case when cargoabonomov."IdTipoOperacion" = '' then null else cargoabonomov."IdTipoOperacion" end  AS "IdTipoOperacion",
	CAST(COALESCE(tipooperacion."Codigo",'') AS NVARCHAR(40)) AS "CodigoTipoOperacion",
	CAST(COALESCE(tipooperacion."Nombre",'') AS NVARCHAR(100)) AS "NombreTipoOperacion",
	CASE WHEN cargoabonomov."IdOperacion" = 0 THEN NULL ELSE cargoabonomov."IdOperacion" END AS "IdOperacion",
	LEFT(TO_CHAR(IFNULL(cargoabonomov."FechaOperacion",'0001-01-01')),10) AS "FechaOperacion",
	LEFT(TO_CHAR(IFNULL(cargoabonomov."FechaLiquidacionOperacion",'0001-01-01')),10) AS "FechaLiquidacionOperacion",
	LEFT(TO_CHAR(IFNULL(cargoabonomov."FechaVencimientoOperacion",'0001-01-01')),10) AS "FechaVencimientoOperacion",
	CASE WHEN cargoabonomov."IdMonedaOperacion" = '' THEN NULL ELSE cargoabonomov."IdMonedaOperacion" END AS "IdMonedaOperacion",
	cargoabonomov."MontoOperacion",
	cargoabonomov."TipoCambioOperacion",
	case when cargoabonomov."IdTipoProducto" = '' then null else cargoabonomov."IdTipoProducto" end AS "IdTipoProducto",
	coalesce(CAST(tipoproducto."Codigo" AS NVARCHAR(40)),'') AS "CodigoTipoProducto" ,
	coalesce(CAST(tipoproducto."Nombre" AS NVARCHAR(100)),'') AS "NombreTipoProducto" ,
	case when cargoabonomov."IdTipoInstrumento" = '' then null else cargoabonomov."IdTipoInstrumento" end   AS "IdTipoInstrumento",
	CAST(COALESCE ( TIPINS."Nombre" ,      '' ) AS NVARCHAR(100)) AS "NombreTipoInstrumento" ,
	case when TIPINS."IdClasificacionTipoInstrumento" = '' then null else TIPINS."IdClasificacionTipoInstrumento" end  AS "IdClasificacionTipoInstrumento" ,
	CAST(COALESCE ( CLAINS."Nombre" ,'' ) AS NVARCHAR(100))AS "NombreClasificacionTipoInstrumento" , 
	CASE WHEN cargoabonomov."IdInstrumento" = 0 THEN NULL ELSE cargoabonomov."IdInstrumento" END AS "IdInstrumento" ,
	CAST(COALESCE ( cargoabonomov."Nemo" ,  '' ) AS NVARCHAR(40)) AS "Nemotecnico" ,
	cargoabonomov."EsCargoAbonoPorTraspaso",
	CASE WHEN cargoabonomov."IdCargoAbonoRelacionadoTraspaso" = 0 THEN NULL ELSE cargoabonomov."IdCargoAbonoRelacionadoTraspaso" END AS "IdCargoAbonoRelacionadoTraspaso",
	coalesce(CAST(RelTransferencia."NombreCliente" AS NVARCHAR(302)),'') AS "NombreClienteRelacionado",
	coalesce(CAST(portafolioTrans."CodigoPortafolio" AS NVARCHAR(150)),'')  AS "CodigoPortafolioRelacionado", 
	cargoabonomov."EsCertificadoAutomatico",
	cargoabonomov."IdUsuarioCertificador" AS "IdUsuarioCertificador",
	coalesce(CAST(UsuarioCertificador."CodigoUsuario" AS NVARCHAR(40)),'') AS "CodigoUsuarioCertificador",
	LEFT(TO_CHAR(IFNULL(cargoabonomov."FechaCertificacion",'0001-01-01')),10) AS "FechaCertificacion",
	CASE WHEN cargoabonomov."IdPersonaPagoCobro" = 0 THEN NULL ELSE cargoabonomov."IdPersonaPagoCobro" END AS "IdPersonaPagoCobro",
	CASE WHEN cargoabonomov."IdContraparte" = 0 THEN NULL ELSE cargoabonomov."IdContraparte" END AS "IdContraparte",
	coalesce(cargoabonomov."EsIndicadorBilateral",'') as "EsIndicadorBilateral",
	
	CASE WHEN cargoabonomov."IdEjecutivo" = 0 THEN (select "IdEntidad" from "BSCL.Plataforma.BD::ACC.Usuario" where "IdUsuario" = 0) 
	ELSE cargoabonomov."IdEjecutivo" END AS "IdEmpleadoDigitador",
		
	CASE WHEN empleado."IdEmpleado" = 0 THEN NULL ELSE empleado."IdEmpleado" END AS "IdEjecutivo",
	CASE WHEN cargoabonomov."IdSucursal" = 0 THEN NULL ELSE cargoabonomov."IdSucursal" END AS "IdSucursal",
	CASE WHEN cargoabonomov."IdModulo" = '' THEN NULL ELSE cargoabonomov."IdModulo" END AS "IdModulo",
	--CASE WHEN cargoabonomov."IdUsuario" = 0 THEN NULL ELSE cargoabonomov."IdUsuario" END AS "IdUsuario",
	cargoabonomov."IdUsuario" AS "IdUsuario",
	CAST(UsuarioCreador."CodigoUsuario" AS NVARCHAR(40)) AS "CodigoUsuarioCreador",
	CAST(COALESCE ( UsuarioCreador."CodigoUsuario" ,'' ) AS NVARCHAR(302)) AS "NombreUsuario",
	CASE WHEN cargoabonomov."IdEstado" = '' THEN NULL ELSE cargoabonomov."IdEstado" END AS "IdEstado",
	CAST(estadoctainv."Nombre" AS NVARCHAR(100))  AS "NombreEstado",
	cargoabonomov."IdCarga" AS "IdCarga",
	cargoabonomov."SecuenciaMovimientoMigracion" AS "SecuenciaMovimientoMigracion",
	solicitud."IdTipoMovimientoFondoMutuo",
    solicitud."IdRegimenTributario",
	CASE WHEN COALESCE(portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador",
	CASE WHEN COALESCE(portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MAT."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC",
	CASE WHEN cargoabonomov."IdTipoOperacion" = 'RETGTA' THEN  COALESCE(RETGTA."ExcluyeMovimientoEnCartola", 'N' )
		WHEN cargoabonomov."IdTipoOperacion" = 'INGGTA' THEN  COALESCE(INGGTA."ExcluyeMovimientoEnCartola", 'N' )
		ELSE 'N' END AS "ExcluyeMovimientoEnCartola"
	FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabonomov 
	LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON cuentainversion."IdCuentaInversion" = cargoabonomov."IdCuentaInversion" 
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" cliente ON cliente."IdCliente" = cuentainversion."IdCliente"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" persona ON persona."IdPersona" = cliente."IdPersona"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" documento ON documento."IdDocumentoIdentidad" = persona."IdDocumentoIdentidad"
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" portafolio ON portafolio."IdPortafolio" = cuentainversion."IdPortafolio"
	LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" UsuarioCreador ON UsuarioCreador."IdUsuario" = cargoabonomov."IdUsuario" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS CONFIGURACION ON "CodigoParametro" = 'PLA.SegundosZonaHoraria' 
	LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" as UsuarioCertificador ON UsuarioCertificador."IdUsuario" = cargoabonomov."IdUsuarioCertificador" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS condiciondeliquidacion ON condiciondeliquidacion."IdCondicionDeLiquidacion" = cargoabonomov."IdCondicionDeLiquidacion" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS moneda ON moneda."IdMoneda" = cuentainversion."IdMoneda" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS conceptomov ON conceptomov."IdConcepto" = cargoabonomov."IdConcepto" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS estadoctainv ON (estadoctainv."IdEstado" = cargoabonomov."IdEstado") 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" AS TipoMovimiento ON TipoMovimiento."IdTipoMovimiento" = cargoabonomov."IdTipoMovimiento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS viewconsultarins ON viewconsultarins."IdInstrumento" = cargoabonomov."IdInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPINS ON TIPINS."IdTipoInstrumento" = viewconsultarins."IdTipoInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CLAINS ON CLAINS."IdClasificacionTipoInstrumento" = TIPINS."IdClasificacionTipoInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS tipooperacion ON tipooperacion."IdTipoOperacion" = cargoabonomov."IdTipoOperacion" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS tipoproducto ON tipoproducto."IdTipoProducto" = cargoabonomov."IdTipoProducto" 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" AS TipoComprobanteTES ON SUBSTRING(TipoComprobanteTES."IdTipoMovimiento", 4, 3) = cargoabonomov."IdTipoComprobante"  AND TipoComprobanteTES."IdModulo" = 'TES'
	LEFT JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS TipoComprobanteFAC ON TipoComprobanteFAC."IdTipoComprobante" = cargoabonomov."IdTipoComprobante" 
	LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS RelTransferencia ON RelTransferencia."IdCargoAbonoMovimiento" = cargoabonomov."IdCargoAbonoRelacionadoTraspaso"
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" portafolioTrans ON portafolioTrans."IdPortafolio" = cuentainversion."IdPortafolio"
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" solicitud ON cargoabonomov."IdComprobante" = solicitud."IdSolicitudAporteRescate" AND cargoabonomov."IdTipoComprobante" = 'FFM'
	LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" AS Formato
       ON Formato."IdMoneda" = cuentainversion."IdMoneda" AND Formato."IdUsoMonedaFormato" = 'GEN' AND Formato."IdTipoOperacion" = '' AND Formato."IdTipoInstrumento" = ''
    LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" empleado ON empleado."IdPersona" = cliente."IdPersonaEjecutivo"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" MAT ON MAT."IdMatrizAgrupadorAPV" = portafolio."IdMatrizAgrupadorAPV"
    LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = portafolio."CodigoPortafolio" AND portafolio."IdMatrizAgrupadorAPV" = 0
    LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS RETGTA ON RETGTA."IdMovimientoCarteraEnTransito" = cargoabonomov."IdMovimientoAsociado" AND cargoabonomov."IdTipoOperacion" = 'RETGTA'
	LEFT JOIN "BSCL.Custodia.BD::GTA.Garantia" AS GAR ON cargoabonomov."IdComprobante" = GAR."IdGarantia" AND cargoabonomov."IdTipoOperacion" = 'INGGTA'
	LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS INGGTA ON GAR."IdMovimientoCarteraEnTransito" = INGGTA."IdMovimientoCarteraEnTransito";

GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarMovimientosCuentaInversionAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
