/********************************************************** 
Proposito	: Inserta la configuración de grilla usuario
autor		: GATBLAC
fecha		: 23/05/2018
***********************************************************/
INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionUsuario"(
	"IdConfiguracionUsuario", 
	"IdEmpresa", 
	"IdUsuario", 
	"Llave", 
	"Valor"
)
VALUES( 
"BSCL.Plataforma.BD.Secuencias::ConfiguracionUsuario".NEXTVAL,  
:p_IdEmpresa,
:p_IdUsuario,
:p_Llave,
:p_Valor);
