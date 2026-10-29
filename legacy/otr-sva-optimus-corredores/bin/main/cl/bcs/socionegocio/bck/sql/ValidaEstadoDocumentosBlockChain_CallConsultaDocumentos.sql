CALL "BSCL.SocioNegocio.BD.Servicios::DOC.ConsultarDocumentos" (
	p_IdEmpresa					=> :p_IdEmpresa,
	p_IdCliente					=> :p_IdCliente,
	p_out_Documentos		    =>  $p_out_Documentos
);