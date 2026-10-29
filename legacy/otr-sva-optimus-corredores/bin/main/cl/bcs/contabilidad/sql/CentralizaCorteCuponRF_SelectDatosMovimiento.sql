SELECT
	cargoabono."IdCargoAbonoMovimiento"  AS "IdMovimiento"
	,TEC."Nombre" AS "TipoDocumento"
	,cargoabono."IdConcepto" AS "IdConcepto"
	,CASE WHEN ECC."IdTipoEventoCapital" = 'CC' THEN 'CIERRE CORTE CUPON'
	 WHEN ECC."IdTipoEventoCapital" = 'SL' THEN 'CIERRE SORTEO LETRAS' 
	 WHEN cargoabono."IdConcepto" = 'RET104' THEN 'RETENCION ART.104 CIERRE CORTE CUPON' ELSE '' END  AS "NombreConcepto"
	,'' AS "IdComprobante"
	,YEAR(EC."FechaPago") ||'-'|| RIGHT('0' || MONTH(EC."FechaPago"),2) ||'-'|| RIGHT('0' || DAYOFMONTH(EC."FechaPago"),2) AS "FechaDocumento"
	,'' AS "NombreCuentaCorriente"
	,'' AS "NumeroCuentaCorriente"
	,'' AS "NumeroDocumentoPago"
	,'' AS "FechaDocumentoPagoCorredora"
	,'999999999' AS "SerieDocumento"
	,ECC."DocumentoIdentidad" AS "DocumentoIdentidad"
	,ECC."NombreCliente" AS "NombreCliente"
	,ECC."CodigoPortafolio" AS "Portafolio"
	,EC."IdMoneda" AS "Moneda"
	,TO_BIGINT(0) AS "CodigoCaja"
	,'' AS "EmitidoA"
	,'' AS "EsBilateral"
	,ConsultarPersona."Tipo" AS "TipoPersona"
	,cargoabono."FechaSistema" AS "FechaLiquidacion"
	,'' AS "Producto" --CA."IdTipoProducto"
	,CASE WHEN ECC."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad" THEN 'CP'
          WHEN UPPER(ConsultarIndicadoresCliente."CodigoValorIndicador") = 'VINCULADO' THEN 'RE'
          ELSE 'NR'
        END AS "Relacion"
	,ECC."IdBolsa" AS "Bolsa"
	,'' AS "CentroCostoAgente"
	,'' AS "ComprobanteTipo"
	,ECC."Nemo" AS "Nemotecnico"
	,'' AS "OpCentros"
	,'' AS "TipoEmisor"
	,''	AS "Mesa"
	,ECC."TipoCartera"
	,CASE ECC."DocumentoIdentidad" 
		WHEN ConsultarEmpresa."DocumentoIdentidad" THEN 'SI' 
		ELSE 'NO' 
	END	AS "EsCuentaPropia" 
	,COALESCE(EC."IdEmisor", '') AS "Emisor"
	,COALESCE(IndClienteBancario."NombreValorIndicador",'NO BANCARIO') AS "ClienteBancario"	
	,COALESCE(INS."IdTipoReajuste",'') AS "IdTipoReajuste"
	,cargoabono."IdOperacion" AS "Folio"
	,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor" (INS."IdEmisor", INS."IdClasificacionTipoInstrumento", INS."IdTipoInstrumento", ECC."Nemo", INS."IdTipoReajuste") AS "ClaseEmisor"
	,CASE WHEN ECC."MontoInteres" + ECC."MontoCapital" < 0 THEN 'SI' ELSE 'NO' END AS "EsCuentaTercero"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabono
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabono."IdCuentaInversion"
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalCliente" AS ECC
ON ECC."IdEventoCapitalCliente" = cargoabono."IdOperacion"
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" AS EC
ON EC."IdEventoCapital" = ECC."IdEventoCapital"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS 
ON EC."IdInstrumento" = INS."IdInstrumento"
LEFT JOIN "BSCL.Custodia.BD::CUS.TipoEventoCapital" TEC
ON EC."IdTipoEventoCapital" = TEC."IdTipoEventoCapital"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SN
ON ECC."IdSocioNegocio" = SN."IdSocioDeNegocio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" ConsultarPersona
ON SN."IdPersona" = ConsultarPersona."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa
ON ConsultarEmpresa."IdEmpresa" = EC."IdEmpresa"
LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio 
ON (ConsultarPortafolio."IdPortafolio"  = ECC."IdPortafolio") 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo
ON (ConsultarEjecutivo."IdEmpresa"              = EC."IdEmpresa"   and
    ConsultarEjecutivo."IdEmpleadoEjecutivo"    = ConsultarPortafolio."IdEmpleadoEjecutivo")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado
ON (ConsultarIndicadoresEmpleado."IdEmpresa" =  ConsultarEjecutivo."IdEmpresa"              AND
    ConsultarIndicadoresEmpleado."IdEmpleado"=  ConsultarEjecutivo."IdEmpleadoEjecutivo"    AND 
    ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO')
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente
ON ConsultarIndicadoresCliente."IdEmpresa" = ConsultarIndicadoresEmpleado."IdEmpresa"  AND
ConsultarIndicadoresCliente."IdPersona" =  ConsultarPersona."IdPersona"             AND 
((ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR' AND :LocSistema = 'CL') OR 
(ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'GENPRIMREL' AND :LocSistema = 'PE'))
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"   AS IndClienteBancario	
	ON IndClienteBancario."IdCliente" =  ECC."IdCliente"
	 AND IndClienteBancario."CodigoTipoIndicador"  = 'CLIENTE_BANCARIO'
WHERE cargoabono."FechaSistema" = :p_FechaContable 
AND cargoabono."IdConcepto" IN ('CIECCC','CICCMI','CICCAM','CIECSL','RET104')
AND cargoabono."IdEstado" = 'CRT'
AND ECC."IdTipoEventoCapital" IN ('CC','SL')
AND cargoabono."IdEmpresa" = :p_IdEmpresa;