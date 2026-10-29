
SELECT 'ORIGEN' AS "OrigenDestino"
			,"MueveCuentaCorriente"	
			,"MueveCuentaInversion"
			,Empresa."IdSocioDeNegocio"
			,"IdCuentaBanco"
			,CorredoraCtaBco."IdBanco"
			,CorredoraCtaBco."IdTipoCuentaBancaria"
			,CorredoraCtaBco."NombreTipoCuentaBancaria"
			,"SocioNegocioCtaBco"
			,SocioNegocioCtaBco."IdBanco" AS "IdBancoSocio"
			,SocioNegocioCtaBco."IdTipoCuentaBancaria" AS "IdTipoCuentaBancariaSocio"
			,SocioNegocioCtaBco."NumeroCuentaBancaria" AS "NumeroCuentaBancariaSocio"
			,Cliente."IdPortafolioDefecto"
			,Empresa."IdPersona" 
			,SUM("MontoDepositoConvenido"+"MontoDepositoAPV") AS "Monto"
			,DetalleCliente."IdEstado"
			,CLI."IdPersona" AS "IdPersonaCliente"
			,DetalleCliente."Origen"
FROM "BSCL.APV.BD::REC.Cliente" DetalleCliente
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI 
	ON CLI."IdCliente" = DetalleCliente."IdCliente"
INNER JOIN "BSCL.APV.BD::REC.Recaudacion" Recaudacion
	ON DetalleCliente."IdRecaudacion" = Recaudacion."IdRecaudacion"
INNER JOIN "BSCL.APV.BD::APV.OrigenDestinoTransicion" APVTransicion
	ON APVTransicion."Operacion" = :Operacion
INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" CorredoraCtaBco
	ON (	Recaudacion."IdBanco" = CorredoraCtaBco."IdBanco"
		AND Recaudacion."CuentaCorriente" = CorredoraCtaBco."NumeroCuentaBancaria"
		AND DetalleCliente."IdEstado" IN('DIS','CON'))
	OR ((SELECT "DestinoCtaBco" FROM "BSCL.APV.BD::APV.OrigenDestinoTransicion"
				WHERE "Operacion" = 'REZ') = CorredoraCtaBco."IdCuentaBanco"
		 AND DetalleCliente."IdEstado" = 'REZ')
INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" Empresa
	ON Empresa."IdEmpresa" = :IdEmpresa
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente
	ON Cliente."IdSocioDeNegocio" = Empresa."IdSocioDeNegocio"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" SocioNegocioCtaBco
	ON APVTransicion."SocioNegocioCtaBco" = SocioNegocioCtaBco."IdCuentaBancaria"
WHERE APVTransicion."IdEmpresa" = :IdEmpresa
AND "IdRecaudacionCliente" IN (:IdsRecaudacionCliente)
GROUP BY "MueveCuentaCorriente"	
			,"MueveCuentaInversion"
			,Empresa."IdSocioDeNegocio"
			,"IdCuentaBanco"
			,CorredoraCtaBco."IdBanco"
			,CorredoraCtaBco."IdTipoCuentaBancaria"
			,CorredoraCtaBco."NombreTipoCuentaBancaria"
			,"SocioNegocioCtaBco"
			,SocioNegocioCtaBco."IdBanco"
			,SocioNegocioCtaBco."IdTipoCuentaBancaria"
			,SocioNegocioCtaBco."NumeroCuentaBancaria"
			,Cliente."IdPortafolioDefecto"
			,Empresa."IdPersona"  
			,DetalleCliente."IdEstado"
			,CLI."IdPersona" 
			,DetalleCliente."Origen"