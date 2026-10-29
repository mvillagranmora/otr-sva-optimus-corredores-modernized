/********************************************************** 
Proposito	: inserta log progrma
autor		: RU
fecha		: 07/02/2017
***********************************************************/
CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarLogDebugPrograma" (
 	p_IdPrograma		=> :IdPrograma,
 	p_IdTipoPrograma	=> :IdTipoPrograma,
 	p_Mensaje			=> :Mensaje
	 							
)
