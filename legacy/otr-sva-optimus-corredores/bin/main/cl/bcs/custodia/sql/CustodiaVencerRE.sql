/********************************************************** 
Proposito	: Script para obtener las carteras para vencer
autor		: JG
fecha		: 01/02/2017
***********************************************************/
SELECT
    c."IdCartera",
    c."FolioCartera",
	c."IdTipoProducto", 
	c."IdTipoOperacion",
    c."IdClasificacionTipoInstrumento",
    c."IdTipoInstrumento",
    c."IdInstrumento",
    c."IdTipoComprobante",
    c."FolioComprobante",
    conc."IdConcepto",
    --------------
    s."IdPersona",
    s."IdCliente",
    c."IdPortafolio",
    coalesce(ip."ValorIndicador",'N') as "OperaContraCuentaInversion",
    c."IdAgente", 
	c."IdSucursal",
    -------------------
	c."FechaVencimientoOperacion",
	c."IdMonedaSistema",
	'T0' AS "IdCondicionLiquidacion",
	--:Fecha AS "FechaLiquidacion",
	c."Cantidad",
    c."PrecioAPlazo",
	c."MontoAPlazo",
	c."INGCantidad",
	c."INGTasaCompra",
	c."INGPrecioCompra",
	c."INGMontoATasaCompraActual",
	c."INGPrecioAPlazo",
	c."INGMontoAPlazo",
	c."FechaOperacion",
	c."IdMonedaInstrumento"
FROM "BSCL.Custodia.BD::CUS.Cartera" c
    INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" s on s."IdCliente" = c."IdCliente"
    LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarConceptosOrden" conc ON (c."IdTipoOperacion" = conc."IdTipoOperacion" AND conc."Uso" = 'VENCIMIENTO') 
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"ip on ip."IdPortafolio" = c."IdPortafolio" and 
	  ip."CodigoTipoIndicador" = 'GEN_OCCINV'
WHERE c."IdEstado" = 'VIG'  and	 
	  c."IdClasificacionTipoInstrumento" = 'RE' and 
	  c."FechaVencimientoOperacion" > '0001-01-01' and	  
	  c."FechaVencimientoOperacion" <= :Fecha and
	  c."IdEmpresa" = :IdEmpresa and
	  c."IdCarga"=0