 
 SELECT CASE WHEN APVTransicion."OrigenCtaBco" = CorredoraCtaBco."IdCuentaBanco" THEN 'ORIGEN'  
			WHEN APVTransicion."DestinoCtaBco" = CorredoraCtaBco."IdCuentaBanco" THEN 'DESTINO'
			END "OrigenDestino"
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
			,"IdPortafolioDefecto"
			,Empresa."IdPersona"
FROM "BSCL.APV.BD::APV.OrigenDestinoTransicion" APVTransicion
INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" CorredoraCtaBco
	ON APVTransicion."OrigenCtaBco" = CorredoraCtaBco."IdCuentaBanco" --109
OR APVTransicion."DestinoCtaBco" = CorredoraCtaBco."IdCuentaBanco"
INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" Empresa
	ON Empresa."IdEmpresa" = :IdEmpresa
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente
	ON Cliente."IdSocioDeNegocio" = Empresa."IdSocioDeNegocio"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" SocioNegocioCtaBco
	ON APVTransicion."SocioNegocioCtaBco" = SocioNegocioCtaBco."IdCuentaBancaria"
WHERE APVTransicion."IdEmpresa" = :IdEmpresa
AND "Operacion" = :Operacion
ORDER BY CASE WHEN APVTransicion."OrigenCtaBco" = CorredoraCtaBco."IdCuentaBanco" THEN 'ORIGEN'  
			WHEN APVTransicion."DestinoCtaBco" = CorredoraCtaBco."IdCuentaBanco" THEN 'DESTINO'
			END  DESC