INSERT INTO "BSCL.SocioNegocio.BD::CLI.LogGenerarContratos"
("Idlog","CodigoFormulario","CampoBackend","IdEmpresa", "IdCliente", "IdPersona", "IdUsuario", "FechaSistema", "IdPrograma", "Estado", "Mensaje")
SELECT
	"BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos".NEXTVAL,
	'PORTAFOLIO_',
	:CampoModificado,
	:IdEmpresa,
	:IdCliente,
	0,
	:IdUsuario,
	LEFT(:FechaHoraSistema, 10),
	0,
	'PEN',
	'Cliente - ModificarPortafolio.java'
FROM DUMMY
