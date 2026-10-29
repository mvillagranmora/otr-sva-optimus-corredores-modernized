/********************************************************** 
Proposito	: Inserta la configuración de grilla predeterminada
autor		: GATBLAC
fecha		: 23/05/2018
***********************************************************/
INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionGrilla"(
	"IdConfiguracionGrilla", 
	"IdEmpresa", 
	"Llave", 
	"Valor"
)
VALUES( 
"BSCL.Plataforma.BD.Secuencias::PLA.ConfiguracionGrilla".NEXTVAL, 
:p_IdEmpresa,
:p_Llave,
:p_Valor);
