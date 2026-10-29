/********************************************************** 
Proposito	: Script para loguear en control pauta
autor		: JG
fecha		: 01/02/2017
***********************************************************/
CALL "BSCL.Plataforma.BD.Servicios::AgregarControlPauta" (
	p_IdTrabajo  			=> :p_IdTrabajo,
	P_OUT_ERRORESCONTADOR  	=> @P_OUT_ERRORESCONTADOR
);
