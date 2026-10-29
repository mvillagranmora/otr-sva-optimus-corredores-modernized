INSERT INTO "BSCL.Custodia.BD::DER.OpcionesEjercicioBloqueo"
("IdEjercicio", "IdCartera", "Bloqueado", "ContratosEjercidos", "ContratosSorteados", "FechaHora", "IdEstado", "IdEstadoCCLV", "DescripcionEstadoCCLV", "IdCliente", "IdPortafolio", "IdEmpresa")
VALUES(
	:IdEjercicio,
	:IdCartera, 
	:Bloqueado,
	:ContratosEjercidos,
	:ContratosSorteados,
	:FechaHora,
	:IdEstado,
	:IdEstadoCCLV,
	:DescripcionEstadoCCLV,
	:IdCliente,
	:IdPortafolio,
	:IdEmpresa
);