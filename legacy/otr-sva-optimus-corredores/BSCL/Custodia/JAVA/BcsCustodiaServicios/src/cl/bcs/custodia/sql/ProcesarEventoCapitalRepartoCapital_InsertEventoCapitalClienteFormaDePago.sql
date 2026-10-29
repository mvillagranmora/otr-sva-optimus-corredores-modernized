INSERT INTO "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"(
	"IdEventoCapitalClienteFormaDePago",
	"IdEventoCapitalCliente",
	"IdPreferenciaPagoPortafolio",
	"IdTipoMovimiento",
	"IdModalidadPago",
	"IdMedioDePago",
	"NombreMedioDePago",
	"IdBanco",
	"NombreBanco",
	"IdTipoCuentaBancaria",
	"NombreTipoCuentaBancaria",
	"IdCuentaBancaria",
	"NombreCuentaBancaria",
	"NumeroCuentaBancaria",
	"IdTipoTarjetaBancaria",
	"NumeroTarjetaBancaria",
	"NumeroMedioDePago",
	"Sucursal",
	"FechaVctoDocumento",
	"Monto",
	"FechaHoraIngresoRegistro",
	"IdUsuarioIngreso",
	"IdEstado")
SELECT 
    "BSCL.Custodia.BD.Secuencias::EventoCapitalClienteFormaDePago".NEXTVAL,
	Ecc."IdEventoCapitalCliente"					AS "IdEventoCapitalCliente",
	Efp."IdPreferenciaPagoPortafolio"				AS "IdPreferenciaPagoPortafolio",
	'I'												AS "IdTipoMovimiento",
	CASE
    	WHEN SocNeg."IdCliente" IS NULL
			THEN MEDMOD."IdModalidadDePago"
		WHEN Ecc."CantidadDerecho" < 0
			THEN IFNULL(MdP."IdModalidadDePago", MEDMOD."IdModalidadDePago")
		ELSE 
			IFNULL(MdP."IdModalidadDePago",'MODDEP')	
	END 											AS "IdModalidadPago",
	CASE
		WHEN SocNeg."IdCliente" IS NULL
			THEN MED."IdMedioDePago"
		WHEN Ecc."CantidadDerecho" < 0
			THEN IFNULL(Efp."IdMedioDePago", MED."IdMedioDePago")
		ELSE 
			IFNULL(Efp."IdMedioDePago", 'CIN')			
	END 											AS "IdMedioDePago",
	CASE 
		WHEN SocNeg."IdCliente" IS NULL
			THEN MED."Nombre"
		WHEN Ecc."CantidadDerecho" < 0
			THEN IFNULL(Efp."MedioDePago", MED."Nombre")
		ELSE 
			IFNULL(Efp."MedioDePago",'CUENTA INVERSION')
	END												AS "NombreMedioDePago",
	IFNULL(Efp."IdBancoCuentaBancaria",0)			AS "IdBanco",
	IFNULL(Efp."NombreBancoCuentaBancaria",'')		AS "NombreBanco",
	IFNULL(Efp."IdTipoCuentaBancaria",'')			AS "IdTipoCuentaBancaria",
	IFNULL(Efp."NombreTipoCuentaBancaria",'')		AS "NombreTipoCuentaBancaria",
	IFNULL(Efp."IdCuentaBancaria",0)				AS "IdCuentaBancaria",
	'' 												AS "NombreCuentaBancaria",
	IFNULL(Efp."NumeroCuentaBancaria",'')			AS "NumeroCuentaBancaria",
	IFNULL(Efp."IdTipoTarjetaBancaria",'')			AS "IdTipoTarjetaBancaria",
	IFNULL(Efp."NumeroTarjetaBancaria",'')			AS "NumeroTarjetaBancaria",
	'' 												AS "NumeroMedioDePago",
	IFNULL(Ecc."CodigoSucursal",'')					AS "Sucursal",
	IFNULL(Ecc."FechaPago",'')						AS "FechaVctoDocumento",
	IFNULL(Ecc."Total",0)							AS "Monto",
	CURRENT_UTCTIMESTAMP 							AS "FechaHoraIngresoRegistro",
	:IdUsuarioIngreso								AS "IdUsuarioIngreso",	
	'VIG'											AS "IdEstado"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" 							Ecc
	LEFT JOIN "BSCL.Custodia.BD.Vistas::EVC.ConsultarPreferenciaPagoEventoCapital" 	Efp
		ON Ecc."IdPortafolio" 		= Efp."IdPortafolio"
	   AND Efp."IdTipoOperacion" 	= 'DIVDIN'
	   AND Ecc."IdMoneda" 			= Efp."IdMoneda"
	   AND Efp."Prioridad" = 1
	LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SocNeg
		ON Ecc."IdSocioNegocio" = SocNeg."IdSocioDeNegocio"
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PAG.ConsultarModalidadDePago" 		MdP
		ON Efp."IdMedioDePago" 		= MdP."IdMedioDePago",
	"BSCL.Custodia.BD::CUS.Configuracion" CONF
	INNER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" MED 
		ON (CONF."Valor" = MED."IdMedioDePago")	
	INNER JOIN "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" MEDMOD
		ON MEDMOD."IdMedioDePago" = MED."IdMedioDePago"
WHERE "IdEventoCapital" 		= :IdEventoCapital
   AND COALESCE(Efp."IdMedioDePago", MED."IdMedioDePago") != ''
   AND CONF."CodigoParametro" = 'CUS.MedioPagoDefectoEVCAP'
   AND CONF."IdEmpresa" = :IdEmpresa;
   
   
   
   
   