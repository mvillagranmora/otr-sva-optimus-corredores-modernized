CALL "BSCL.Normativa.CrystalReports.Servicios::Certificado43" (
	p_IdEmpresa  => :IdEmpresa,
    p_IdUsuario  => :IdUsuario,
    p_IdModulo 	 => :IdModulo,
    p_Version	 => :Version,
	p_Rut        => #GenerarCertificado43Masivo_RUT,
	
	p_Salida     => $p_Salida
)