CALL "BSCL.Normativa.CrystalReports.Servicios::Certificado17" (
	p_IdEmpresa  => :IdEmpresa,
    p_IdUsuario  => :IdUsuario,
    p_IdModulo 	 => :IdModulo,
    p_Version	 => :Version,
	p_Rut        => #GenerarCertificado17Masivo_Rut,
	
	p_Salida     => $p_Salida
)