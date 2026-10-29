CALL "BSCL.Normativa.CrystalReports.Servicios::Certificado24" (
	p_IdEmpresa  => :IdEmpresa,
    p_IdUsuario  => :IdUsuario,
    p_IdModulo 	 => :IdModulo,
    p_Version	 => :Version,
	p_Rut        => #GenerarCertificado24Masivo_Rut,
	
	p_Salida     => $p_Salida
)