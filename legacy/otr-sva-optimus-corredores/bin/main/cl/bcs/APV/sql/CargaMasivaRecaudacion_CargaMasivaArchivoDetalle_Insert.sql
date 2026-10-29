INSERT INTO "BSCL.APV.BD::APV.CargaMasivaDetalleRecaudacion"
(	
	  "IdCargaMasiva"
	, "Fecha"
	, "TipoDeposito"
	, "FolioCartola"
	, "FolioLineaCartola"
	, "Banco"
	, "Cuenta"
	, "Monto"
	, "PeriodoPago"
	, "TipoPago"
	, "RUTEmpleador"
	, "RUTCliente"
	, "TipoDeAfiliado"
	, "Regimen"
	, "MensajeValidacion"
	, "EstadoRendicion"
	, "EstadoProceso")
VALUES(
	 :IdCargaMasiva
	,:Fecha
	,:TipoDeposito 
	,:FolioCartola
	,:FolioLineaCartola
	,:Banco
	,:Cuenta
	,:Monto
	,:PeriodoPago
	,:TipoPago
	,:RUTEmpleador
	,:RUTCliente
	,:TipodeAfiliado
	,:Regimen
	,''
	,''
	,''
);