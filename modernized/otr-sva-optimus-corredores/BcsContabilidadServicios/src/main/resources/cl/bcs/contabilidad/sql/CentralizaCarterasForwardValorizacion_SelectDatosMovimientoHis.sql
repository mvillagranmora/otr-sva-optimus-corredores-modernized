SELECT
 ConsultarCartera."IdCartera" 							AS "IdMovimiento"		 
,LEFT(ConsultarCliente."DocumentoIdentidad",60)			AS "DocumentoIdentidad"                                    
,LEFT(ConsultarCliente."NombreCompleto",60)				AS "NombreCliente"                                       
,LEFT(ConsultarPortafolio."CodigoPortafolio",60)			AS "Portafolio"
,ConsultarCartera."IdMonedaPrincipal"					AS "MonedaPrincipal"
,ConsultarCartera."IdMonedaSecundaria"					AS "MonedaSecundaria"
,CASE ConsultarCartera."IdModalidadCumplimiento"
		WHEN '6' THEN 'C' 
		ELSE 'F'									
END 													AS "ModalidaPago"
,ConsultarCartera."IdTipoProducto"						AS "Producto"
,ConsultarCartera."IdClasificacionTipoInstrumento" 		AS "ClaseInstrumento"
,ConsultarCartera."IdTipoInstrumento" 					AS "TipoInstrumento"
,ConsultarCartera."IdTipoOperacion" 					AS "TipoOperacion"
,ConsultarCartera."Nemotecnico"							AS "Nemo"
,IFNULL(ConsultarEjecutivo."CodigoEmpleado",'')    		 AS "Agente"
,LEFT(IFNULL(ConsultarEjecutivo."NombreCompleto",''),60) AS "NombreAgente"
,IFNULL(ConsultarEjecutivo."CodigoSucursal",'')			 AS "SucursalAgente"
,LEFT(IFNULL(ConsultarEjecutivo."NombreSucursal",''),60) AS "NombreSucursalAgente"
,LEFT(IFNULL(ConsultarIndicadoresEmpleado."ValorIndicador",''),60)	AS "CentroCostoAgente"
,CASE WHEN ConsultarCliente."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad"
				THEN 'CP'
  	  WHEN ConsultarIndicadoresCliente."ValorIndicador" = 'S' 	
  				THEN 'RE'
  	ELSE	'NR'
  END 													AS "Relacion"
,ConsultarCartera."IdBolsa"								AS "Bolsa"
,ConsultarCartera."ValorTipoCambioCierre"				AS "TasaCambio"
,ConsultarCliente."Tipo"								AS "TipoPersona"
,case when coalesce(ConsultarCartera."EGRIdConcepto",'') <> '' then coalesce(ConsultarCartera."EGRIdConcepto",'') else coalesce(ConsultarCartera."IdConcepto",'') END AS "Concepto"
,ConsultarCartera."FechaVencimientoOperacion" 			AS "FechaLiquidacion"
,CASE WHEN ConsultarCartera."EGRCantidad" = 0 THEN ConsultarCartera."INGFechaOperacion"
						ELSE ConsultarCartera."EGRFechaOperacion" END
														AS "FechaOperacion",
CASE SUBSTRING(ConsultarCartera."IdTipoOperacion",1,2)
		WHEN 'CO' THEN 'COMPRA'
		ELSE 'VENTA'
		END 											AS "CompraVenta"														
	
FROM "BSCL.Custodia.BD::HIS.Cartera" ConsultarCartera
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ConsultarCliente
ON ConsultarCliente."IdCliente" = ConsultarCartera."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa
ON ConsultarEmpresa."IdEmpresa" = ConsultarCartera."IdEmpresa"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ConsultarPortafolio 
ON ConsultarPortafolio."IdPortafolio" = ConsultarCartera."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo
ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarCartera."IdAgente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado
ON (ConsultarIndicadoresEmpleado."IdEmpleado" = ConsultarEjecutivo."IdEmpleadoEjecutivo" AND 
	ConsultarIndicadoresEmpleado."CodigoTipoIndicador"  = 'CENCOSTO')
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ConsultarIndicadoresCliente
ON ConsultarIndicadoresCliente."IdCliente" =  ConsultarCartera."IdCliente" AND ConsultarIndicadoresCliente."CodigoTipoIndicador"  = 'RELCORR'
WHERE ConsultarCartera."IdEmpresa"			= :IdEmpresa
  AND ConsultarCartera."FechaRespaldo" = :FechaContable
  AND ConsultarCartera."IdTipoProducto"		= 'MEFW'
  AND ConsultarCartera."INGFechaOperacion"	<= :FechaContable
  AND ConsultarCartera."FechaVencimientoOperacion" > :FechaContable
  AND ConsultarCartera."IdEstado"			= 'VIG'