SELECT
	SOR."DocumentoIdentidad",
	CLI."IdCliente",
	CLI."IdPortafolio",
	SOR."PortafolioDerivados",
	SOR."IdTipoOperacion",
	SOR."Serie",
	SOR."EsPutEsCall",
	SOR."CodigoCorredora",
	SOR."CodigoClasificacion",
	SOR."IdTipoInstrumento",
	SOR."ModalidadLiquidacion",
	SOR."ModalidadEjercicio",
	SOR."EstiloOpcion",
	SOR."Fecha",
	SOR."FechaVencimiento",
	SOR."Subyacente",
	SOR."Strike",
	SOR."PrecioSubyacente",
	SOR."PerdidaGanancia",
	SOR."SaldoEjercicio",
	SOR."SaldoNeto",
	SOR."SaldoPrima",
	SOR."SaldoSorteo",
	SOR."IdFondo",
	SOR."IdFondoCliente",
	SOR."LadoDiaAnterior",
	SOR."LadoDia",
	SOR."LadoNeto",
	SOR."LadoTr",
	SOR."LotePadron",
	SOR."ContratosAbiertosNeto",
	SOR."ContratosAbiertosDiaAnterior",
	SOR."ContratosAbiertosDia",
	SOR."ContratosEjercidos",
	SOR."ContratosSorteados",
	SOR."CantidadContratosTr",
	SOR."Observacion"
FROM 
	"BSCL.Custodia.BD.Vistas::DER.ConsultaSorteoContratos"				SOR
INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"	CLI
ON (CLI."IdPortafolio" = SOR."IdPortafolio")
WHERE 
	SOR."Fecha" = :p_Fecha;
