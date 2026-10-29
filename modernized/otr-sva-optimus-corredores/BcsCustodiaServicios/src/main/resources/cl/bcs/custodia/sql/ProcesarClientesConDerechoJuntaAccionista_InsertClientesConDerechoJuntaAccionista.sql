
-- Inserta clientes con derecho a junta de accionistas
INSERT INTO "BSCL.Custodia.BD::CUS.ClienteConDerechoJuntaAccionista"
(
	"IdClienteConDerechoJuntaAccionista", 
	"IdEventoJuntaAccionista", 
	"IdCliente", 
	"IdInstrumento", 
	"CantidadDerecho", 
	"CantidadDerechoCalculado", 
	"FechaCalculo",
	"Origen", 
	"IndicadorMandatoRepresentacion",
	"Instruccion", 
	"IdTipoInstruccion", 
	"FechaIngreso", 
	"FechaIngresoRegistro", 
	"IdUsuario", 
	"IdEstado"
)
VALUES
(	
	"BSCL.Custodia.BD.Secuencias::ClienteConDerechoJuntaAccionista".NEXTVAL, -- IdClienteConDerechoJuntaAccionista
	:IdEventoJuntaAccionista, 
	:IdCliente,
	:IdInstrumento,
	:CantidadDerecho,
	:CantidadDerechoCalculado, 
	:FechaCalculo,
	:Origen,
	:IndicadorMandatoRepresentacion,
	:Instruccion,
	:IdTipoInstruccion,
	:FechaIngreso,
	CURRENT_UTCTIMESTAMP, -- FechaIngresoRegistro 
	:IdUsuario, 
	:IdEstado
);	
		