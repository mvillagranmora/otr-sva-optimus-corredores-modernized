
CALL "BSCL.FondoMutuo.CrystalReports.Servicios::Certificado31Traspasos" (
	p_IdEmpresa  =>:IdEmpresa,
    p_IdUsuario  => :IdUsuario,
    p_IdModulo 	 => :IdModulo,
	p_Traspaso   => #GenerarCertificado31Masivo_Traspasos,
	
	p_Salida     =>$p_Salida
)

