CALL "BSCL.Normativa.BD.Servicios::CIR.CertificadoCustodia"		   		  
(
  p_IdEmpresa				  => :p_IdEmpresa,
  p_IdCliente				  => :p_IdCliente,
  p_IdPortafolio			  => :p_IdPortafolio,
  p_FechaConsulta			  => :p_FechaConsulta,	
  p_FormaSalida				  => :p_FormaSalida, --'CARTA'
  p_UnificarPortafolios		  => :p_UnificarPortafolios,
  p_IdUsuario				  => :p_IdUsuario,
  p_CartaConfirmacionCliente  => :p_CartaConfirmacionCliente
  ,p_Salida     =>$p_Salida
);