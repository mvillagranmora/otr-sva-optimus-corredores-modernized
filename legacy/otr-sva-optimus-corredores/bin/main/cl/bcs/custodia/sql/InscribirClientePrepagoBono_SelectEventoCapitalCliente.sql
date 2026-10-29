SELECT
	(CAR."IdCliente" || CAR."IdPortafolio" || CAR."IdClasificacionTipoInstrumento"  || CAR."IdTipoInstrumento"  || CAR."IdInstrumento" || CAR."IdCartera") AS "Llave",	
	CAR."IdCartera",
	COALESCE(SOC."IdSocioDeNegocio",0) AS "IdSocioDeNegocio",
	CAR."TipoDocumentoIdentidad",
	CAR."DocumentoIdentidad",
	CAR."RazonSocial",
	CAR."Cantidad",
	CAR."PrecioCompra" AS "Precio",
	CAR."INGIdMonedaPagoCupon",
	CAR."INGNumeroCupon",
	
	COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0) AS "IdEmpleadoEjecutivoPortafolio",		
	COALESCE(EJE."CodigoEmpleado",'') AS "CodigoEmpleado",
	COALESCE(EJE."NombreCompletoEjecutivo",'') AS "NombreCompletoEjecutivo",
	COALESCE(EJE."CodigoSucursal",'') AS "CodigoSucursal",
	CAR."IdBoveda",
	CAR."IdBolsa",
	CAR."IdTipoReajuste",
	CAR."INGFechaPagoCorteCupon",
	CAR."IdCuentaCustodioPortafolio"
 
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS CAR
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"	AS EJE ON (EJE."IdPortafolio" = CAR."IdPortafolio")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 		AS SOC ON (SOC."IdCliente" = CAR."IdCliente")
WHERE   (CAR."IdCliente" || CAR."IdPortafolio" || CAR."IdClasificacionTipoInstrumento" || CAR."IdTipoInstrumento" || CAR."IdInstrumento" || CAR."IdCartera") IN (:LlavesCarCliPor) -- 174993 40738 RF BON 177542
--WHERE   (CAR."IdCliente" || CAR."IdPortafolio" || CAR."IdClasificacionTipoInstrumento" || CAR."IdTipoInstrumento" || CAR."IdInstrumento") IN ('17499340738RFBON177542')
AND      CAR."IdEstado"  = 'VIG';