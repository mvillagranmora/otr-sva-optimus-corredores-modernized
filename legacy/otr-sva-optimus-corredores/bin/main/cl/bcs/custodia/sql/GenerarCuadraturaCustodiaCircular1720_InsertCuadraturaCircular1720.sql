INSERT INTO "BSCL.Custodia.BD::CUS.CuadraturaCircular1720"
	("IdCuadraturaCircular1720"
	, "IdCarga"
	, "FechaCuadratura"
	, "Nemotecnico"
	, "IdClasificacionTipoInstrumento"
	, "IdTipoInstrumento"
	, "CantidadCustodia"
	, "CantidadGarantia"
	, "CantidadVentaPlazo"
	, "CantidadCompraPlazo"
	, "TotalCustodia"
	, "TotalCustodioEmisores"
	, "CompraEnTransito"
	, "VentaEnTransito"
	, "TransitoIngreso"
	, "TransitoEgreso"
	, "Diferencia"
	, "Precio"
	, "Valorizacion"
	, "DiferenciaValorizada"
	, "IdInstrumento" 
	, "IdEmisor"
	, "TotalCustodiaCuentaPropia"
	, "ReplicaBaseTotalCustodioEmisores"
	, "ReplicaBaseDiferencia"
	, "ReplicaBaseDiferenciaValorizada"
	, "NemoExisteEnOptimus"
	)
	VALUES(
	"BSCL.Custodia.BD.Secuencias::CuadraturaCircular1720".NEXTVAL
	, :IdCarga
	, :FechaCuadratura
	, :Nemotecnico
	, :IdClasificacionTipoInstrumento
	, :IdTipoInstrumento
	, :CantidadCustodia
	, :CantidadGarantia
	, :CantidadVentaPlazo
	, :CantidadCompraPlazo
	, :TotalCustodia
	, :TotalCustodioEmisores
	, :CompraEnTransito
	, :VentaEnTransito
	, :TransitoIngreso
	, :TransitoEgreso
	, :Diferencia
	, :Precio
	, :Valorizacion
	, :DiferenciaValorizada
	, :IdInstrumento
	, :IdEmisor
	, :TotalCustodiaCuentaPropia
	, :TotalCustodioEmisores
	, :Diferencia
	, :DiferenciaValorizada
	, 'S'
	);
	