CREATE LOCAL TEMPORARY TABLE "#ValidarYAsignarContratosARecaudaciones_Recaudaciones" 
(
	"IdRecaudacionCliente" BIGINT
	, "RutCliente" NVARCHAR(40)
	, "MontoRecaudacion" BIGINT
	, "TipoRegimen" NVARCHAR(10)
	, "Regimen" NVARCHAR(50)
	, "RutEmpleador" NVARCHAR(40)
	, "IdContratoSAAPV" BIGINT
);
