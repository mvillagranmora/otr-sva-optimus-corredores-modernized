CALL "BSCL.Normativa.BD.Servicios.CER::CrearCertificadoCliente"
( 
	p_IdEmpresa					=> :p_IdEmpresa,
	p_IdTipoDocumento  			=> :p_IdTipoDocumento,
    p_Periodo        			=> :p_Periodo,
    P_Certificados  			=> #GRABARCERTIFICADO_CUSTODIA_CIRCULAR
);