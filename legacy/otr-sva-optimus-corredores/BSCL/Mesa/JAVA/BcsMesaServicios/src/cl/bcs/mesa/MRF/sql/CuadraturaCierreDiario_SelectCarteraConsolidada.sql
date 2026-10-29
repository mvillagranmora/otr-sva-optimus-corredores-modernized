SELECT   CCC."FolioCartera" AS "IdOperacion"
		,CCC."FolioOperacion"
		,CCC."RazonSocial"
		,CCC."ValorCompraEnPesos" AS "MontoOperacion"
		,CCC."ValorCompraEnMoneda" AS "MontoOperacionMoneda"
		,CCC."MonedaPapel" AS "IdMoneda"
		,CCC."MonedaPapel" AS "IdMonedaLiquidacion"
		,'' AS "IdMesaOrigen"
		,'' AS "IdEstado"
		---------------
		,COALESCE (CCC."ValorCompraEnPesos",0)  AS "MontoCompra"
		
		,COALESCE (CCC."ValorCompraEnMoneda",0) AS "ValorCompraHoy"
		,COALESCE (CCC."Interes",0) AS "Interes"
		,COALESCE (CCC."InteresAcumulado",0) AS "InteresAcumulado"
		,COALESCE (CCC."ValorMercado",0) AS "ValorMercado"
		,COALESCE (CCC."InteresDevengado",0) AS "InteresDevengado"
		,CCC."NombreMesa" AS "IdMesaOrigen"
FROM "BSCL.Custodia.BD.Vistas::MRF.ConsultarCarteraConsolidada" 
(PLACEHOLDER."$$p_fecha$$"=>:Fecha
,PLACEHOLDER."$$p_idempresa$$"=> :IdEmpresa
,PLACEHOLDER."$$p_idusuario$$"=> :IdUsuario
,PLACEHOLDER."$$p_idmodulo$$"=>  :IdModulo
,PLACEHOLDER."$$p_idpais$$"=>0
,PLACEHOLDER."$$p_tipocartera$$"=>'TOD'
,PLACEHOLDER."$$p_idcliente$$"=>0
,PLACEHOLDER."$$p_idportafolio$$"=>0) AS CCC
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" Car ON Car."FolioCartera" = CCC."FolioCartera"
WHERE CCC."FechaOperacion" < :Fecha AND COALESCE (Car."FechaLiquidacion", CCC."FechaOperacion") <= :Fecha;