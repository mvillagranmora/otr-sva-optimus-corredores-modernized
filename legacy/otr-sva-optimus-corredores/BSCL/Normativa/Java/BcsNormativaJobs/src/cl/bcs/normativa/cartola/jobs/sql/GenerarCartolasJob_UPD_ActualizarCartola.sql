/********************************************************** 
Proposito	: 
autor		: NRM
fecha		: 08/02/2018
***********************************************************/


UPDATE "BSCL.Normativa.BD::CAR.Cartola"
SET "MensajeProceso"	= COALESCE(:MensajeProceso,"MensajeProceso"), 
	"EstadoProceso"		= COALESCE(:EstadoProceso,"EstadoProceso"), 
	"Notificar"			= COALESCE(:Notificar,"Notificar"), 
	"Reprocesar"		= COALESCE(:Reprocesar,"Reprocesar"),
	"CartolaEncoded"	= :CartolaEncoded,
	"NombreArchivo"		= COALESCE(:NombreArchivo,"NombreArchivo")
WHERE "IdNomina"= :IdNomina AND "IdCliente" = :IdCliente AND "IdPortafolio" = :IdPortafolio;
