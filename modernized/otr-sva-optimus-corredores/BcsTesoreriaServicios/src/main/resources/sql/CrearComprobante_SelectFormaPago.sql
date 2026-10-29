select 
    IngEgrAGenerar."IdIngresoEgreso",
	IngrEgr."IdEmpresa", 
	IngrEgr."IdTipoProducto", 
	IngrEgr."IdTipoMovimiento", 
	ingEgrFP."IdMedioDePago", 
	ingEgrFP."NombreMedioDePago", 
	ingEgrFP."NombreModalidadDePago",
	IngrEgr."IdMoneda", 
	IngrEgr."Monto", 
	IngrEgr."FechaIngresoRegistro", 
	ingEgrFP."IdEstado",
	ingEgrFP."IdModalidadDePago",
	ingEgrFP."IdCuentaBancariaCorredora",
	ingEgrFP."NombreCuentaBancariaCorredora",
	COALESCE(ingEgrFP."NumeroCuentaBancariaCorredora",'000'),
	COALESCE(ingEgrFP."IdTipoCuentaBancariaCorredora",'1'),
	COALESCE(ingEgrFP."NombreTipoCuentaBancariaCorredora",''),
	ingEgrFP."IdTarjetaBancariaCorredora",
	ingEgrFP."IdTipoTarjetaBancariaCorredora",
	ingEgrFP."NombreTipoTarjetaBancariaCorredora",
	ingEgrFP."NumeroTarjetaBancariaCorredora",
	ingEgrFP."IdChequera",
	ingEgrFP."NumeroDocumentoCorredora", 
	ingEgrFP."FechaDocumentoPagoCorredora", 
	ingEgrFP."IdCuentaBancariaSocio",
	ingEgrFP."IdBancoSocio",
	ingEgrFP."NombreCuentaBancariaSocio",
	ingEgrFP."NumeroCuentaBancariaSocio",
	ingEgrFP."IdTipoCuentaBancariaSocio",
	ingEgrFP."NombreTipoCuentaBancariaSocio",
	ingEgrFP."IdTarjetaBancariaSocio",
	ingEgrFP."IdTipoTarjetaBancariaSocio",
	ingEgrFP."NombreTipoTarjetaBancariaSocio",
	ingEgrFP."NumeroTarjetaBancariaSocio",
	ingEgrFP."NumeroSerieSocio",
	ingEgrFP."NumeroDocumentoSocio", 
	ingEgrFP."FechaDocumentoPagoSocio",
	'0' as "IdMoneda",--ingEgrFP."IdMoneda",CAST(SUM(ingEgrFP."Monto") AS DECIMAL(26,8)) AS "Monto",
	'0001-01-01' as "FechaAfectaSaldo"
	--COALESCE(ingEgrFP."IdPlaza",0)
	
	FROM   #ComprobanteIngresEgresoAProcesar  As IngEgrAGenerar
	LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" As IngrEgr ON IngEgrAGenerar."IdIngresoEgreso" = IngrEgr."IdIngresoEgreso"
	INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" ingEgrFP ON  IngrEgr."IdIngresoEgreso" = ingEgrFP."IdIngresoEgreso"
	WHERE IngrEgr."IdEmpresa" = :p_IdEmpresa;



