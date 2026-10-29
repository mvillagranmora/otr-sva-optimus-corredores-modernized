/********************************************************** 
Proposito	: Script para insertar nuevos recursos de cartola
autor		: NRM
fecha		: 03/05/2017
***********************************************************/


INSERT INTO "BSCL.Normativa.BD::CAR.Recurso"(
	    "IdRecurso",
	    "IdPlantilla",
	    "Editable",
	    "Descripcion",
	    "Grupo",
	    "Nombre",
	    "Tipo",
	    "Valor",
	    "IdEstado"
)
VALUES (
	"BSCL.Normativa.BD.Secuencias::CAR.Global".NEXTVAL,
	:IdPlantilla,
	COALESCE(:Editable,''),
	COALESCE(:Descripcion,''),
	COALESCE(:Grupo,''),
	COALESCE(:Nombre,''),
	COALESCE(:Tipo,''),
	COALESCE(:Valor,''),
	COALESCE(:IdEstado,'INA') 
);