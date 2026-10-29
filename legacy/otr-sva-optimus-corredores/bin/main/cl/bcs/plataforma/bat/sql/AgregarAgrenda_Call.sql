/********************************************************** 
Proposito	: inserta log progrma
autor		: RU
fecha		: 07/02/2017
***********************************************************/
CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda" (
 	p_IdEmpresa => :p_IdEmpresa,
    p_IdUsuario => :p_IdUsuario,
    p_IdModulo => :p_IdModulo,
    
    p_IdTipoTarea => :p_IdTipoTarea,
    p_EsDemonio    => :p_EsDemonio,
    p_DiaDeMes => :p_DiaDeMes,
    p_DiaDeSemana => :p_DiaDeSemana,
    p_Mes => :p_Mes,
    p_Hora => :p_Hora,
    p_Minuto => :p_Minuto,
    p_SoloDiasHabiales => :p_SoloDiasHabiales,
    p_SoloPrincipioMes => :p_SoloPrincipioMes,
    p_SoloFinDeMes => :p_SoloFinDeMes,
    p_habilitado => :p_habilitado,
    p_TablaParametros => #PARAMETROS_AGENDA,

    p_IdAgenda => @p_IdAgenda,
    p_IdPrograma => @p_IdPrograma,
    p_out_AdvertenciasContador => @p_out_AdvertenciasContador,
    p_out_AdvertenciasDetalle => $p_out_AdvertenciasDetalle,
    p_out_ErroresContador => @p_out_ErroresContador,
    p_out_ErroresDetalle => $p_out_ErroresDetalle
								
)
