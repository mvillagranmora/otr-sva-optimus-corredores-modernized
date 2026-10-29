UPDATE "BSCL.Tesoreria.BD::PAC.MandatoPAC"
SET
     "IdCliente"			    =:IdCliente
	,"IdPortafolio"				=:IdPortafolio
	,"FolioCSC"					=:FolioCSC
	,"CuentaCorriente"			=:CuentaCorriente
	,"FolioSAAPV"				=:FolioSAAPV
	,"Receptor"					=:Receptor
	,"Fecha"					=:Fecha
	,"NumeroMandato"			=:NumeroMandato
	,"Firma"					=:Firma
	,"IdentServicio"			=:IdentServicio
	,"IdEmpresa"				=:IdEmpresa
	,"Canal"					=:Canal
	,"IdCuentaBancaria"			=:IdCuentaBancaria
	,"EstadoPorAutorizar"		=COALESCE(:EstadoPorAutorizar,"EstadoPorAutorizar")
	,"DiaDescuentoPorAutorizar" =COALESCE(:DiaDescuentoPorAutorizar,"DiaDescuentoPorAutorizar")
	,"EstadoContratoPorAutorizar"=COALESCE(:EstadoContratoPorAutorizar,"EstadoContratoPorAutorizar")
	,"Estado"					=:Estado
WHERE  "IdMandatoPAC"= :IdMandatoPACBase

