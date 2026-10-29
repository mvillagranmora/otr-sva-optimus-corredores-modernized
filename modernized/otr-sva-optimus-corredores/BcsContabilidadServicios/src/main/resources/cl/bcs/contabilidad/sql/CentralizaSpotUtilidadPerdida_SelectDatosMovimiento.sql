SELECT *
FROM (
	SELECT
		 ConsultarOperacionSpot."IdOperacionSpotOrigen" AS "IdMovimiento",
		 LEFT(ConsultarCliente."DocumentoIdentidad",60) AS "DocumentoIdentidad",
		 LEFT(ConsultarCliente."NombreCompleto",60) AS "NombreCliente",
		 LEFT(ConsultarPortafolio."CodigoPortafolio",16) AS "Portafolio",
		 ConsultarOperacionSpot."IdMonedaPrincipal" AS "Moneda",
		 ConsultarOperacionSpot."IdTipoOperacion" AS "Producto",
		 'SP' AS "ClaseInstrumento",
		 TI."IdTipoInstrumento" AS "TipoInstrumento",
		 TI."RazonSocial" AS "Nemo",
		 'Boveda' AS "Boveda",
		 CAST(ConsultarOperacionSpot."IdOperacionSpotOrigen" AS NVARCHAR(60)) AS "IdCartera",
		 'Emisor' AS "Emisor",
		 ConsultarOperacionSpot."IdMonedaPrincipal" AS "TipoReajuste",
		 'N' AS "CodigoIFRS",
		 IFNULL(ConsultarEjecutivo."CodigoEmpleado",'') AS "Agente",
		 LEFT(IFNULL(ConsultarEjecutivo."NombreCompleto",''),60) AS "NombreAgente",
		 IFNULL(ConsultarEjecutivo."CodigoSucursal",'') AS "SucursalAgente",
		 LEFT(IFNULL(ConsultarEjecutivo."NombreSucursal",''),60) AS "NombreSucursalAgente",
		 IFNULL(ConsultarOperacionSpot."IdTipoCanalIngreso",'') AS "Canal",
		 CASE 
			 WHEN ConsultarCliente."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad" THEN 'CP'
			 WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S' THEN 'RE'
			 ELSE 'NR'
		 END AS "Relacion",
		 '' AS "Bolsa",
		 LEFT(INDEMP."ValorIndicador",60) AS "CentroCostoAgente",
		 ConsultarOperacionSpot."TipoComprobanteFacturacion" AS "TipoComprobante",
		 CASE WHEN CO."Accion" = 'VENTA' THEN 'V' ELSE 'C' END AS "CompraVenta",
		 ConsultarCliente."Tipo" AS "TipoPersona",
		 LEFT(ConsultarOperacionSpot."FechaOperacion",10) AS "FechaOperacion",
		 COALESCE(IndClienteBancario."NombreValorIndicador",'NO BANCARIO') AS "ClienteBancario",
		 ConsultarOperacionSpot."OperacionFicticia" as "OperacionNoFacturable",
		 ROW_NUMBER() OVER (PARTITION BY ConsultarOperacionSpot."IdOperacionSpotOrigen"ORDER BY ConsultarOperacionSpot."IdOperacionSpotOrigen") AS rn
	FROM "BSCL.Mesa.BD::SPT.OperacionSpot" ConsultarOperacionSpot
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" TI 
		ON ConsultarOperacionSpot."IdInstrumento" = TI."IdInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" CO
		ON ConsultarOperacionSpot."IdTipoOperacion" = CO."IdTipoOperacion"
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ConsultarCliente
		ON ConsultarCliente."IdCliente" = ConsultarOperacionSpot."IdCliente"
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa
		ON ConsultarEmpresa."IdEmpresa" = ConsultarOperacionSpot."IdEmpresa"
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio 
		ON ConsultarPortafolio."IdPortafolio" = ConsultarOperacionSpot."IdPortafolio"
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo
		ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarOperacionSpot."IdAgente"
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente
		ON ConsultarIndicadoresCliente."IdCliente" = ConsultarOperacionSpot."IdCliente" 
		AND ConsultarIndicadoresCliente."CodigoTipoIndicador" = 'RELCORR'
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" AS IndClienteBancario
		ON IndClienteBancario."IdCliente" = ConsultarOperacionSpot."IdCliente" 
		AND IndClienteBancario."CodigoTipoIndicador" = 'CLIENTE_BANCARIO'
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" AS INDEMP
		ON INDEMP."IdEmpleado" = ConsultarPortafolio."IdEmpleadoEjecutivo" 
		AND INDEMP."CodigoTipoIndicador" = 'CENCOSTO'
	WHERE ConsultarOperacionSpot."IdEmpresa" = :IdEmpresa
	AND LEFT(ConsultarOperacionSpot."FechaOperacion",10) = :FechaContable
	AND ConsultarOperacionSpot."Estado" = 'VIG'
) t
WHERE rn = 1
UNION ALL
SELECT *
FROM (
	SELECT
		 "IdResultadoContableSpot" AS "IdMovimiento",
		 '' AS "DocumentoIdentidad",
		 '' AS "NombreCliente",
		 '' AS "Portafolio",
		 :MonedaSistema AS "Moneda",
		 '' AS "Producto",
		 'SP' AS "ClaseInstrumento",
		 '' AS "TipoInstrumento",
		 '' AS "Nemo",
		 '' AS "Boveda",
		 '0' AS "IdCartera",
		 '' AS "Emisor",
		 '' AS "TipoReajuste",
		 '' AS "CodigoIFRS",
		 '' AS "Agente",
		 '' AS "NombreAgente",
		 '' AS "SucursalAgente",
		 '' AS "NombreSucursalAgente",
		 '' AS "Canal",
		 '' AS "Relacion",
		 '' AS "Bolsa",
		 '' AS "CentroCostoAgente",
		 '' AS "TipoComprobante",
		 '' AS "CompraVenta",
		 '' AS "TipoPersona",
		 LEFT("Fecha",10) AS "FechaOperacion",
		 '' AS "ClienteBancario",
		 'N' AS "OperacionNoFacturable",
		 ROW_NUMBER() OVER (PARTITION BY "IdResultadoContableSpot"ORDER BY "IdResultadoContableSpot") AS rn
	FROM "BSCL.Mesa.BD::SPT.ResultadoContableSpot"
	WHERE "IdEstado" = 'ACT'
	AND "Fecha" = :FechaContable
) t2
WHERE rn = 1;