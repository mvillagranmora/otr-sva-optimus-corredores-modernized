UPDATE "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas"
SET

	"FolioCSC"					=:FolioCSC
	,"IdCliente"				=:IdCliente
    ,"IdPortafolio" 			=:IdPortafolio
	,"Hora" 					=:Hora
    ,"Fecha"  					=:Fecha
    ,"MontoPesos"				=:MontoPesos
    ,"MontoUF"					=:MontoUF
    ,"TipoPago"					=:TipoPago
    ,"IdBanco"					=:IdBanco
    ,"DomicilioSucursal"		=:DomicilioSucursal
    ,"CodigoSucursal"			=:CodigoSucursal
    ,"NombreFuncionario"		=:NombreFuncionario
    ,"DocumentoIdentificacionFuncionario"			=:DocumentoIdentificacionFuncionario
    ,"FirmaFuncionario"			=:IdCliente
    ,"DiaDescuento"				=:DiaDescuento
    ,"AceptoCondiciones"		=:AceptoCondiciones
WHERE  "FolioCSC"= :FolioCSC
