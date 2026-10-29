-- HANA-SOURCE: cl/bcs/tesoreria/sql/CrearIngresoEgreso_SelectIngresoEgresoFormaDePagoProceso.sql
-- SUM(CASE .. THEN 1 ELSE 0 END) is INTEGER on HANA and BIGINT on H2; the legacy maps it with field.set onto an Integer field

SELECT
	 PRO."NumeroReferencia"
	,PRO."NumeroReferenciaFormaDePago"

	 -- Datos para validaciones
	,MEP."IdMedioDePago" AS "IdMedioDePago_Real"
	

	,MEDMOD."IdModalidadDePago" AS "IdModalidadDePago_Real"
	
	,COALESCE(CBC."IdCuentaBanco",0)    AS "IdCuentaBancariaCorredora_Real"
	,COALESCE(CBS."IdCuentaBancaria",0) AS "IdCuentaBancariaSocio_Real"
	,MON."IdMoneda" AS "IdMoneda_Real"
	,MON."IdEstado" AS "IdEstadoMoneda_Real"
	,COALESCE(TBC."IdTarjetaBancaria",0) AS "IdTarjetaCorredoraBancaria_Real"
	,COALESCE(TBS."IdTarjetaBancaria",0) AS "IdTarjetaSocioBancaria_Real"
	,MEP."Nombre" AS "NombreMedioDePago"
	,MOP."Nombre" AS "NombreModalidadDePago"
	
	,CAST(COALESCE(ARG."ExisteDescuadreEnMontos",0) AS INTEGER) AS "ExisteDescuadreEnMontos"   -- Monto de formas de pago no cuadra con el monto del movimiento 
	,CAST(COALESCE(ARG."ExisteDiferenciaMoneda",0) AS INTEGER) AS "ExisteDiferenciaMoneda"	 -- Diferente moneda entre movimiento y sus formas de pago 
	
	-- Datos de la cuenta bancaria de la corredora
	,CBC."IdTipoCuentaBancaria" AS "IdTipoCuentaBancariaCorredora"
	,TCC."NombreTipoCuentaBancaria" AS "NombreTipoCuentaBancariaCorredora"
	,CBC."NombreCuentaBancaria" AS "NombreCuentaBancariaCorredora"
	,CBC."NumeroCuentaBancaria" AS "NumeroCuentaBancariaCorredora"
					
	-- Datos de la cuenta bancaria del socio	
	,COALESCE(CBS."IdTipoCuentaBancaria",'') AS "IdTipoCuentaBancariaSocio"
	,COALESCE(CBS."NumeroCuentaBancaria",'') AS "NumeroCuentaBancariaSocio"
	,COALESCE(TCS."NombreTipoCuentaBancaria",'') AS "NombreTipoCuentaBancariaSocio"
	,COALESCE(CBS."IdBanco",0) AS "IdBancoSocio"
FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgresoFormaDePago_Proceso" as PRO
LEFT OUTER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS MEP ON (MEP."IdMedioDePago" = PRO."IdMedioDePago")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PAG.ModalidadDePago" AS MOP ON (MOP."IdModalidadDePago" = PRO."IdModalidadDePago")

LEFT OUTER JOIN "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" AS MEDMOD ON (MEDMOD."IdMedioDePago" = PRO."IdMedioDePago" AND  MEDMOD."IdModalidadDePago" = PRO."IdModalidadDePago" AND MEDMOD."IdEstado" = 'ACT')





LEFT OUTER JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CBC ON (CBC."IdCuentaBanco" = PRO."IdCuentaBancariaCorredora")
LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS TCC ON (TCC."IdTipoCuentaBancaria"=CBC."IdTipoCuentaBancaria")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CBS ON (CBS."IdCuentaBancaria" = PRO."IdCuentaBancariaSocio")
LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" AS TCS ON (TCS."IdTipoCuentaBancaria" = CBS."IdTipoCuentaBancaria")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON (MON."IdMoneda" = PRO."IdMoneda")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" AS TBC ON (TBC."IdTarjetaBancaria" = PRO."IdTarjetaBancariaCorredora")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" AS TBS ON (TBS."IdTarjetaBancaria" = PRO."IdTarjetaBancariaSocio")

/*
LEFT OUTER JOIN ( 	SELECT DISTINCT FPP."NumeroReferencia",
						SUM(CASE WHEN SUM(FPP."Monto") <> IEP."Monto" THEN 1 ELSE 0 END) OVER (partition by FPP."NumeroReferencia") AS "ExisteDescuadreEnMontos",
						SUM(CASE WHEN FPP."IdMoneda" <> IEP."IdMoneda" THEN 1 ELSE 0 END ) OVER (partition by FPP."NumeroReferencia") AS "ExisteDiferenciaMoneda"
			 		FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgresoFormaDePago_Proceso" AS FPP
			 		INNER JOIN "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Proceso" AS IEP ON (IEP."NumeroReferencia" = FPP."NumeroReferencia")
			) AS ARG  ON (ARG."NumeroReferencia" = PRO."NumeroReferencia")
*/			

LEFT OUTER JOIN (SELECT 
						 FPP."NumeroReferencia"
						,SUM(CASE WHEN SUM(FPP."Monto") <> MAX(IEP."Monto") THEN 1 ELSE 0 END) OVER (partition by FPP."NumeroReferencia") AS "ExisteDescuadreEnMontos"
						,SUM(CASE
								WHEN COUNT(Distinct FPP."IdMoneda") > 1 THEN 1
								WHEN MAX(FPP."IdMoneda") <> MAX(IEP."IdMoneda") THEN 1
								ELSE 0
							 END) OVER (partition by FPP."NumeroReferencia") AS "ExisteDiferenciaMoneda"
				 FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgresoFormaDePago_Proceso" AS FPP
				 INNER JOIN "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Proceso" AS IEP ON (IEP."NumeroReferencia" = FPP."NumeroReferencia")
				 GROUP BY FPP."NumeroReferencia") AS ARG  ON (ARG."NumeroReferencia" = PRO."NumeroReferencia");

